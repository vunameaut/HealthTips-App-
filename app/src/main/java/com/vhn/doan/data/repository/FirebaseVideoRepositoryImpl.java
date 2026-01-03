package com.vhn.doan.data.repository;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.ShortVideoDeserializer;
import com.vhn.doan.data.VideoComment;
import com.vhn.doan.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Triển khai VideoRepository sử dụng Firebase Realtime Database
 * Đọc dữ liệu t�� /videos, /users/{uid}/preferences, /trendingVideos/{country}
 * Sắp xếp theo: tags khớp preferences → uploadDate mới �� viewCount → likeCount
 */
public class FirebaseVideoRepositoryImpl implements VideoRepository {

    private final FirebaseDatabase database;
    private final DatabaseReference videosRef;
    private final DatabaseReference usersRef;
    private final DatabaseReference trendingVideosRef;

    // Map để quản lý các listener realtime
    private final Map<String, ValueEventListener> activeListeners = new HashMap<>();

    // Cache để lưu trạng thái like của các video
    private final Map<String, Boolean> likeStatusCache = new HashMap<>();

    /**
     * Constructor mặc định
     */
    public FirebaseVideoRepositoryImpl() {
        database = FirebaseDatabase.getInstance();
        videosRef = database.getReference(Constants.VIDEOS_REF);
        usersRef = database.getReference(Constants.USERS_REF);
        trendingVideosRef = database.getReference(Constants.TRENDING_VIDEOS_REF);
    }

    @Override
    public void getFeed(String userId, String country, VideoCallback callback) {
        if (callback == null) {
            return;
        }

        // Dữ liệu tạm để collect từ 6 sources
        final List<ShortVideo> allVideos = new ArrayList<>();
        final Map<String, Boolean> userPreferences = new HashMap<>();
        final List<String> favoriteCategories = new ArrayList<>();
        final List<String> trendingVideoIds = new ArrayList<>();
        final Map<String, Long> watchedVideoIds = new HashMap<>(); // 🎯 Watched videos with timestamp
        final java.util.Set<String> likedVideoIds = new java.util.HashSet<>(); // 🎯 Liked videos
        final CountDownLatch latch = new CountDownLatch(6); // Tăng lên 6

        // 1. Lấy tất cả videos (không filter theo status, sẽ filter trong code)
        // Chấp nhận status: "ready", "published" (bỏ qua: "draft", "processing", "failed")
        videosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            android.util.Log.d("VideoRepository", "🎬 Tổng số video trong Firebase: " + dataSnapshot.getChildrenCount());

                            int readyCount = 0;
                            int publishedCount = 0;
                            int otherCount = 0;

                            for (DataSnapshot videoSnapshot : dataSnapshot.getChildren()) {
                                try {
                                    // Kiểm tra status trước khi deserialize
                                    String status = videoSnapshot.child("status").getValue(String.class);

                                    // Chỉ load video có status = "ready" hoặc "published"
                                    if ("ready".equals(status) || "published".equals(status)) {
                                        // Sử dụng custom deserializer thay vì getValue(ShortVideo.class)
                                        ShortVideo video = ShortVideoDeserializer.fromDataSnapshot(videoSnapshot);
                                        if (video != null) {
                                            allVideos.add(video);
                                            android.util.Log.d("VideoRepository", "✅ Loaded video [" + status + "]: " + video.getId() + " - " + video.getTitle());

                                            if ("ready".equals(status)) readyCount++;
                                            else publishedCount++;
                                        } else {
                                            android.util.Log.w("VideoRepository", "⚠️ Video null sau deserialize: " + videoSnapshot.getKey());
                                        }
                                    } else {
                                        android.util.Log.d("VideoRepository", "⏭️ Bỏ qua video với status='" + status + "': " + videoSnapshot.getKey());
                                        otherCount++;
                                    }
                                } catch (Exception e) {
                                    // Log lỗi conversion cho video cụ thể và skip video đó
                                    android.util.Log.w("VideoRepository",
                                        "❌ Không thể convert video với ID: " + videoSnapshot.getKey() +
                                        ", Lỗi: " + e.getMessage());
                                }
                            }
                            android.util.Log.d("VideoRepository", "📦 Đã load thành công " + allVideos.size() + " videos " +
                                "(ready: " + readyCount + ", published: " + publishedCount + ", bỏ qua: " + otherCount + ")");
                        } catch (Exception e) {
                            android.util.Log.e("VideoRepository", "Lỗi khi đọc videos từ Firebase", e);
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        android.util.Log.e("VideoRepository", "Firebase query bị hủy", databaseError.toException());
                        latch.countDown();
                    }
                });

        // 2. Lấy user preferences
        if (userId != null && !userId.isEmpty()) {
            usersRef.child(userId).child(Constants.USER_PREFERENCES_REF)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot prefSnapshot : dataSnapshot.getChildren()) {
                                    String key = prefSnapshot.getKey();
                                    Boolean value = prefSnapshot.getValue(Boolean.class);
                                    if (key != null && value != null) {
                                        userPreferences.put(key, value);
                                    }
                                }
                            }
                            latch.countDown();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            latch.countDown();
                        }
                    });
        } else {
            latch.countDown();
        }

        // 🎯 NEW: Lấy favorite categories của user
        if (userId != null && !userId.isEmpty()) {
            usersRef.child(userId).child("favoriteCategories")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                                    String categoryName = categorySnapshot.getValue(String.class);
                                    if (categoryName != null && !categoryName.isEmpty()) {
                                        favoriteCategories.add(categoryName);
                                    }
                                }
                            }
                            latch.countDown();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            latch.countDown();
                        }
                    });
        } else {
            latch.countDown();
        }

        // 3. Lấy trending videos cho country
        if (country != null && !country.isEmpty()) {
            trendingVideosRef.child(country)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot trendingSnapshot : dataSnapshot.getChildren()) {
                                    // Xử lý cả trường hợp videoId là String hoặc Number
                                    Object videoIdValue = trendingSnapshot.getValue();
                                    String videoId = null;

                                    if (videoIdValue instanceof String) {
                                        videoId = (String) videoIdValue;
                                    } else if (videoIdValue instanceof Number) {
                                        videoId = String.valueOf(videoIdValue);
                                    } else if (videoIdValue != null) {
                                        videoId = videoIdValue.toString();
                                    }

                                    if (videoId != null && !videoId.isEmpty()) {
                                        trendingVideoIds.add(videoId);
                                    }
                                }
                            }
                            latch.countDown();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            latch.countDown();
                        }
                    });
        } else {
            latch.countDown();
        }

        // 🎯 Lấy danh sách videos đã xem của user
        if (userId != null && !userId.isEmpty()) {
            usersRef.child(userId).child("watchedVideos")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot watchedSnapshot : dataSnapshot.getChildren()) {
                                    String videoId = watchedSnapshot.getKey();
                                    Long timestamp = watchedSnapshot.getValue(Long.class);
                                    if (videoId != null && timestamp != null) {
                                        watchedVideoIds.put(videoId, timestamp);
                                    }
                                }
                                android.util.Log.d("VideoRepository", "Loaded " + watchedVideoIds.size() + " watched videos for user");
                            }
                            latch.countDown();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            android.util.Log.e("VideoRepository", "Error loading watched videos", databaseError.toException());
                            latch.countDown();
                        }
                    });
        } else {
            latch.countDown();
        }

        // 🎯 NEW: Lấy danh sách videos đã like của user
        if (userId != null && !userId.isEmpty()) {
            videosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int likeCount = 0;
                    for (DataSnapshot videoSnapshot : dataSnapshot.getChildren()) {
                        String videoId = videoSnapshot.getKey();
                        if (videoId != null) {
                            // Check if user liked this video
                            DataSnapshot likesSnapshot = videoSnapshot.child(Constants.VIDEO_LIKES_REF).child(userId);
                            if (likesSnapshot.exists()) {
                                likedVideoIds.add(videoId);
                                likeCount++;
                            }
                        }
                    }
                    android.util.Log.d("VideoRepository", "Loaded " + likeCount + " liked videos for user");
                    latch.countDown();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    android.util.Log.e("VideoRepository", "Error loading liked videos", databaseError.toException());
                    latch.countDown();
                }
            });
        } else {
            latch.countDown();
        }

        // Chờ tất cả requests hoàn thành và sắp xếp kết quả
        new Thread(() -> {
            try {
                // Timeout sau 10 giây
                if (latch.await(10, TimeUnit.SECONDS)) {
                    android.util.Log.d("VideoRepository", "📊 Trước khi filter - Total videos: " + allVideos.size() +
                        ", Preferences: " + userPreferences.size() +
                        ", Favorite categories: " + favoriteCategories.size() +
                        ", Trending: " + trendingVideoIds.size());

                    // 🎯 Filter and sort videos with TikTok-style algorithm
                    List<ShortVideo> sortedVideos = filterAndSortVideosSmartly(
                        allVideos,
                        userPreferences,
                        favoriteCategories,
                        trendingVideoIds,
                        watchedVideoIds,
                        likedVideoIds
                    );

                    android.util.Log.d("VideoRepository", "📊 Sau khi filter - Total videos: " + allVideos.size() +
                        ", Watched: " + watchedVideoIds.size() +
                        ", Liked: " + likedVideoIds.size() +
                        ", Final feed: " + sortedVideos.size());

                    if (sortedVideos.isEmpty()) {
                        android.util.Log.w("VideoRepository", "⚠️ CẢNH BÁO: Danh sách video cuối cùng trống!");
                    }

                    // Đảm bảo callback được gọi trên Main UI Thread
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> callback.onSuccess(sortedVideos));
                } else {
                    // Đảm bảo callback lỗi cũng được gọi trên Main UI Thread
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> callback.onError("Timeout khi tải dữ liệu video"));
                }
            } catch (InterruptedException e) {
                // Đảm bảo callback lỗi được gọi trên Main UI Thread
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onError("Lỗi khi tải dữ liệu video: " + e.getMessage()));
            }
        }).start();
    }

    @Override
    public List<ShortVideo> getFeed(String userId, String country) {
        final List<ShortVideo> result = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);

        getFeed(userId, country, new VideoCallback() {
            @Override
            public void onSuccess(List<ShortVideo> videos) {
                result.addAll(videos);
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                latch.countDown();
            }
        });

        try {
            latch.await(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // Return empty list if interrupted
        }

        return result;
    }

    /**
     * 🎯 TikTok-STYLE ALGORITHM: Filter and sort videos intelligently
     *
     * Strategy:
     * 1. Filter out watched AND liked videos (unless all videos are filtered - then recycle)
     * 2. Categorize videos into groups: Favorite, Trending, Diverse
     * 3. Mix groups intelligently to prevent boredom
     * 4. Sort within each group by engagement metrics
     */
    private List<ShortVideo> filterAndSortVideosSmartly(
            List<ShortVideo> videos,
            Map<String, Boolean> userPreferences,
            List<String> favoriteCategories,
            List<String> trendingVideoIds,
            Map<String, Long> watchedVideoIds,
            java.util.Set<String> likedVideoIds) {

        // Step 1: Filter watched AND liked videos
        List<ShortVideo> availableVideos = new ArrayList<>();
        for (ShortVideo video : videos) {
            // Loại bỏ video đã xem HOẶC đã like
            if (!watchedVideoIds.containsKey(video.getId()) && !likedVideoIds.contains(video.getId())) {
                availableVideos.add(video);
            }
        }

        // 🎯 RECYCLING LOGIC: If no available videos, allow all videos again
        if (availableVideos.isEmpty() && !videos.isEmpty()) {
            android.util.Log.d("VideoRepository", "All videos watched/liked! Recycling all videos...");
            availableVideos = new ArrayList<>(videos);
        }

        // Step 2: Categorize videos into groups
        List<ShortVideo> favoriteVideos = new ArrayList<>();
        List<ShortVideo> trendingVideos = new ArrayList<>();
        List<ShortVideo> diverseVideos = new ArrayList<>();

        for (ShortVideo video : availableVideos) {
            boolean isFavorite = isFavoriteCategory(video, favoriteCategories) ||
                               calculateTagMatchScore(video, userPreferences) > 0;
            boolean isTrending = trendingVideoIds.contains(video.getId());

            if (isFavorite) {
                favoriteVideos.add(video);
            } else if (isTrending) {
                trendingVideos.add(video);
            } else {
                diverseVideos.add(video);
            }
        }

        // Step 3: Sort each group by engagement
        Comparator<ShortVideo> engagementComparator = new Comparator<ShortVideo>() {
            @Override
            public int compare(ShortVideo v1, ShortVideo v2) {
                // Calculate engagement score
                double score1 = calculateEngagementScore(v1, userPreferences, favoriteCategories, trendingVideoIds);
                double score2 = calculateEngagementScore(v2, userPreferences, favoriteCategories, trendingVideoIds);
                return Double.compare(score2, score1); // Higher score first
            }
        };

        Collections.sort(favoriteVideos, engagementComparator);
        Collections.sort(trendingVideos, engagementComparator);
        Collections.sort(diverseVideos, engagementComparator);

        // Step 4: 🎯 SMART MIXING to prevent boredom (like TikTok)
        // Pattern: 2 favorites → 1 trending → 1 diverse → repeat
        List<ShortVideo> finalFeed = new ArrayList<>();
        int favIndex = 0, trendIndex = 0, divIndex = 0;
        int pattern = 0;

        while (favIndex < favoriteVideos.size() ||
               trendIndex < trendingVideos.size() ||
               divIndex < diverseVideos.size()) {

            // Add 2 favorite videos
            if (pattern == 0 || pattern == 1) {
                if (favIndex < favoriteVideos.size()) {
                    finalFeed.add(favoriteVideos.get(favIndex++));
                }
            }
            // Add 1 trending video
            else if (pattern == 2) {
                if (trendIndex < trendingVideos.size()) {
                    finalFeed.add(trendingVideos.get(trendIndex++));
                } else if (favIndex < favoriteVideos.size()) {
                    // Fallback to favorite if no trending
                    finalFeed.add(favoriteVideos.get(favIndex++));
                }
            }
            // Add 1 diverse video
            else if (pattern == 3) {
                if (divIndex < diverseVideos.size()) {
                    finalFeed.add(diverseVideos.get(divIndex++));
                } else if (favIndex < favoriteVideos.size()) {
                    // Fallback to favorite if no diverse
                    finalFeed.add(favoriteVideos.get(favIndex++));
                }
            }

            pattern = (pattern + 1) % 4; // Cycle through pattern
        }

        android.util.Log.d("VideoRepository", "Smart mix - Favorites: " + favoriteVideos.size() +
            ", Trending: " + trendingVideos.size() +
            ", Diverse: " + diverseVideos.size() +
            ", Final: " + finalFeed.size());

        return finalFeed;
    }

    /**
     * Calculate engagement score for a video based on multiple factors
     */
    private double calculateEngagementScore(ShortVideo video,
                                           Map<String, Boolean> userPreferences,
                                           List<String> favoriteCategories,
                                           List<String> trendingVideoIds) {
        double score = 0.0;

        // Factor 1: Favorite category (high weight)
        if (isFavoriteCategory(video, favoriteCategories)) {
            score += 100.0;
        }

        // Factor 2: Tag matching (medium-high weight)
        int tagMatches = calculateTagMatchScore(video, userPreferences);
        score += tagMatches * 20.0;

        // Factor 3: Trending status (medium weight)
        if (trendingVideoIds.contains(video.getId())) {
            score += 50.0;
        }

        // Factor 4: Engagement metrics (normalized)
        // Like ratio (likes / views) - max 30 points
        if (video.getViewCount() > 0) {
            double likeRatio = (double) video.getLikeCount() / video.getViewCount();
            score += Math.min(likeRatio * 1000, 30.0);
        }

        // Factor 5: View count (logarithmic scale) - max 20 points
        if (video.getViewCount() > 0) {
            score += Math.min(Math.log10(video.getViewCount()) * 5, 20.0);
        }

        // Factor 6: Recency (newer videos get boost) - max 15 points
        long daysSinceUpload = (System.currentTimeMillis() - video.getUploadDate()) / (1000 * 60 * 60 * 24);
        if (daysSinceUpload < 7) {
            score += (7 - daysSinceUpload) * 2.0; // New videos get more points
        }

        return score;
    }

    /**
     * Tính điểm khớp tags giữa video và user preferences
     */
    private int calculateTagMatchScore(ShortVideo video, Map<String, Boolean> userPreferences) {
        if (video.getTags() == null || video.getTags().isEmpty() ||
            userPreferences == null || userPreferences.isEmpty()) {
            return 0;
        }

        int matchCount = 0;
        for (Map.Entry<String, Boolean> videoTag : video.getTags().entrySet()) {
            String tagName = videoTag.getKey();
            Boolean tagEnabled = videoTag.getValue();

            // Chỉ tính các tag được bật trong video
            if (tagEnabled != null && tagEnabled) {
                Boolean userPref = userPreferences.get(tagName);
                // Nếu user cũng bật preference này
                if (userPref != null && userPref) {
                    matchCount++;
                }
            }
        }

        return matchCount;
    }

    /**
     * 🎯 NEW: Kiểm tra video có thuộc favorite category không
     */
    private boolean isFavoriteCategory(ShortVideo video, List<String> favoriteCategories) {
        if (video == null || video.getCategoryId() == null ||
            favoriteCategories == null || favoriteCategories.isEmpty()) {
            return false;
        }

        // Lấy category ID/name từ video và check xem có trong favoriteCategories không
        String videoCategoryId = video.getCategoryId();

        // So sánh với favorite categories (case-insensitive)
        for (String favCategory : favoriteCategories) {
            if (favCategory != null &&
                (favCategory.equalsIgnoreCase(videoCategoryId) ||
                 videoCategoryId.toLowerCase().contains(favCategory.toLowerCase()) ||
                 favCategory.toLowerCase().contains(videoCategoryId.toLowerCase()))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void getVideoById(String videoId, SingleVideoCallback callback) {
        if (callback == null || videoId == null || videoId.isEmpty()) {
            if (callback != null) {
                callback.onError("Video ID không hợp lệ");
            }
            return;
        }

        videosRef.child(videoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        ShortVideo video = ShortVideoDeserializer.fromDataSnapshot(dataSnapshot);
                        if (video != null) {
                            callback.onSuccess(video);
                        } else {
                            callback.onError("Không thể parse dữ liệu video");
                        }
                    } catch (Exception e) {
                        callback.onError("Lỗi khi đọc dữ liệu video: " + e.getMessage());
                    }
                } else {
                    callback.onError("Video không tồn tại");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Lỗi Firebase: " + databaseError.getMessage());
            }
        });
    }

    // ==================== LIKE OPERATIONS ====================

    @Override
    public void likeVideo(String videoId, String userId, BooleanCallback callback) {
        if (!validateParams(videoId, userId, callback)) return;

        DatabaseReference likeRef = videosRef.child(videoId).child(Constants.VIDEO_LIKES_REF).child(userId);

        // Tạo tài liệu like (optimistic UI)
        Map<String, Object> likeData = new HashMap<>();
        likeData.put("timestamp", com.google.firebase.database.ServerValue.TIMESTAMP);

        likeRef.setValue(likeData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Cập nhật trạng thái like vào cache
                cacheLikeStatus(videoId, userId, true);
                callback.onSuccess(true);
            } else {
                callback.onError("Không thể like video: " +
                    (task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định"));
            }
        });
    }

    @Override
    public void unlikeVideo(String videoId, String userId, BooleanCallback callback) {
        if (!validateParams(videoId, userId, callback)) return;

        DatabaseReference likeRef = videosRef.child(videoId).child(Constants.VIDEO_LIKES_REF).child(userId);

        likeRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Xóa trạng thái like khỏi cache
                clearLikeStatusCache(videoId, userId);
                callback.onSuccess(false);
            } else {
                callback.onError("Không thể unlike video: " +
                    (task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định"));
            }
        });
    }

    @Override
    public void isVideoLiked(String videoId, String userId, BooleanCallback callback) {
        if (!validateParams(videoId, userId, callback)) return;

        // Kiểm tra trạng thái like trong cache trước
        Boolean cachedStatus = getCachedLikeStatus(videoId, userId);
        if (cachedStatus != null) {
            callback.onSuccess(cachedStatus);
            return;
        }

        DatabaseReference likeRef = videosRef.child(videoId).child(Constants.VIDEO_LIKES_REF).child(userId);

        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isLiked = dataSnapshot.exists();
                // Lưu trạng thái like vào cache
                cacheLikeStatus(videoId, userId, isLiked);
                callback.onSuccess(isLiked);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Lỗi khi kiểm tra like status: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void listenToVideoLikeStatus(String videoId, String userId, BooleanCallback callback) {
        if (!validateParams(videoId, userId, callback)) return;

        String listenerKey = "like_" + videoId + "_" + userId;
        DatabaseReference likeRef = videosRef.child(videoId).child(Constants.VIDEO_LIKES_REF).child(userId);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                callback.onSuccess(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Lỗi realtime like listener: " + databaseError.getMessage());
            }
        };

        // Lưu listener để cleanup sau
        activeListeners.put(listenerKey, listener);
        likeRef.addValueEventListener(listener);
    }

    // ==================== COMMENT OPERATIONS ====================

    @Override
    public void getVideoComments(String videoId, CommentsCallback callback) {
        if (callback == null || videoId == null || videoId.isEmpty()) {
            if (callback != null) {
                callback.onError("Video ID không hợp lệ");
            }
            return;
        }

        DatabaseReference commentsRef = videosRef.child(videoId).child("comments");

        // Thay vì query, lấy tất cả comments và filter sau
        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<VideoComment> comments = new ArrayList<>();

                for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                    try {
                        VideoComment comment = commentSnapshot.getValue(VideoComment.class);
                        if (comment != null) {
                            comment.setId(commentSnapshot.getKey());

                            // Chỉ lấy comments gốc (không c�� parentId hoặc parentId = null/empty)
                            if (comment.getParentId() == null || comment.getParentId().isEmpty()) {
                                comments.add(comment);
                                android.util.Log.d("VideoRepository", "Found root comment: " + comment.getId() +
                                    " with replyCount: " + comment.getReplyCount());
                            }
                        }
                    } catch (Exception e) {
                        // Log và bỏ qua comment lỗi
                        android.util.Log.w("VideoRepository",
                            "Không thể parse comment: " + commentSnapshot.getKey(), e);
                    }
                }

                // Sắp xếp theo thời gian tạo (mới nhất trước)
                Collections.sort(comments, (c1, c2) -> {
                    if (c1.getCreatedAt() instanceof Long && c2.getCreatedAt() instanceof Long) {
                        return Long.compare((Long) c2.getCreatedAt(), (Long) c1.getCreatedAt());
                    }
                    return 0;
                });

                android.util.Log.d("VideoRepository", "Loaded " + comments.size() + " root comments for video " + videoId);
                callback.onSuccess(comments);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Lỗi khi tải comments: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void getCommentReplies(String videoId, String parentCommentId, CommentsCallback callback) {
        if (callback == null || videoId == null || parentCommentId == null ||
            videoId.isEmpty() || parentCommentId.isEmpty()) {
            if (callback != null) {
                callback.onError("Tham số không hợp lệ");
            }
            return;
        }

        DatabaseReference commentsRef = videosRef.child(videoId).child("comments");

        // Lấy tất cả comments và filter những cái có parentId khớp
        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<VideoComment> replies = new ArrayList<>();

                for (DataSnapshot replySnapshot : dataSnapshot.getChildren()) {
                    try {
                        VideoComment reply = replySnapshot.getValue(VideoComment.class);
                        if (reply != null) {
                            reply.setId(replySnapshot.getKey());

                            // Chỉ lấy replies có parentId khớp
                            if (parentCommentId.equals(reply.getParentId())) {
                                replies.add(reply);
                                android.util.Log.d("VideoRepository", "Found reply: " + reply.getId() +
                                    " for parent: " + parentCommentId);
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.w("VideoRepository",
                            "Không thể parse reply: " + replySnapshot.getKey(), e);
                    }
                }

                // Sắp xếp theo thời gian tạo (cũ nhất trước cho replies)
                Collections.sort(replies, (r1, r2) -> {
                    if (r1.getCreatedAt() instanceof Long && r2.getCreatedAt() instanceof Long) {
                        return Long.compare((Long) r1.getCreatedAt(), (Long) r2.getCreatedAt());
                    }
                    return 0;
                });

                android.util.Log.d("VideoRepository", "Loaded " + replies.size() + " replies for comment " + parentCommentId);
                callback.onSuccess(replies);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Lỗi khi tải replies: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void addComment(String videoId, String userId, String text, String parentId, CommentCallback callback) {
        if (callback == null || !validateCommentParams(videoId, userId, text)) {
            if (callback != null) {
                callback.onError("Tham số không hợp lệ");
            }
            return;
        }

        VideoComment comment = new VideoComment(userId, text.trim(), parentId);
        DatabaseReference commentsRef = videosRef.child(videoId).child("comments");
        DatabaseReference newCommentRef = commentsRef.push();

        comment.setId(newCommentRef.getKey());

        newCommentRef.setValue(comment).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(comment);
            } else {
                callback.onError("Không thể thêm comment: " +
                    (task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định"));
            }
        });
    }

    @Override
    public void likeComment(String videoId, String commentId, String userId, BooleanCallback callback) {
        if (!validateCommentLikeParams(videoId, commentId, userId, callback)) return;

        DatabaseReference likeRef = videosRef.child(videoId).child("comments")
            .child(commentId).child("likes").child(userId);

        Map<String, Object> likeData = new HashMap<>();
        likeData.put("timestamp", com.google.firebase.database.ServerValue.TIMESTAMP);

        likeRef.setValue(likeData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(true);
            } else {
                callback.onError("Không thể like comment: " +
                    (task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định"));
            }
        });
    }

    @Override
    public void unlikeComment(String videoId, String commentId, String userId, BooleanCallback callback) {
        if (!validateCommentLikeParams(videoId, commentId, userId, callback)) return;

        DatabaseReference likeRef = videosRef.child(videoId).child("comments")
            .child(commentId).child("likes").child(userId);

        likeRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(false);
            } else {
                callback.onError("Không thể unlike comment: " +
                    (task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định"));
            }
        });
    }

    @Override
    public void isCommentLiked(String videoId, String commentId, String userId, BooleanCallback callback) {
        if (!validateCommentLikeParams(videoId, commentId, userId, callback)) return;

        DatabaseReference likeRef = videosRef.child(videoId).child("comments")
            .child(commentId).child("likes").child(userId);

        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                callback.onSuccess(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Lỗi khi kiểm tra comment like status: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void listenToVideoComments(String videoId, CommentsCallback callback) {
        if (callback == null || videoId == null || videoId.isEmpty()) {
            if (callback != null) {
                callback.onError("Video ID không hợp lệ");
            }
            return;
        }

        String listenerKey = "comments_" + videoId;
        DatabaseReference commentsRef = videosRef.child(videoId).child("comments");
        Query query = commentsRef.orderByChild("parentId").equalTo(null);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<VideoComment> comments = new ArrayList<>();

                for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                    try {
                        VideoComment comment = commentSnapshot.getValue(VideoComment.class);
                        if (comment != null) {
                            comment.setId(commentSnapshot.getKey());
                            comments.add(comment);
                        }
                    } catch (Exception e) {
                        android.util.Log.w("VideoRepository",
                            "Không thể parse comment trong realtime: " + commentSnapshot.getKey(), e);
                    }
                }

                Collections.sort(comments, (c1, c2) -> {
                    if (c1.getCreatedAt() instanceof Long && c2.getCreatedAt() instanceof Long) {
                        return Long.compare((Long) c2.getCreatedAt(), (Long) c1.getCreatedAt());
                    }
                    return 0;
                });

                callback.onSuccess(comments);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Lỗi realtime comments listener: " + databaseError.getMessage());
            }
        };

        activeListeners.put(listenerKey, listener);
        query.addValueEventListener(listener);
    }

    @Override
    public void listenToCommentReplies(String videoId, String parentCommentId, CommentsCallback callback) {
        if (callback == null || videoId == null || parentCommentId == null ||
            videoId.isEmpty() || parentCommentId.isEmpty()) {
            if (callback != null) {
                callback.onError("Tham số không hợp lệ");
            }
            return;
        }

        String listenerKey = "replies_" + videoId + "_" + parentCommentId;
        DatabaseReference commentsRef = videosRef.child(videoId).child("comments");
        Query query = commentsRef.orderByChild("parentId").equalTo(parentCommentId);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<VideoComment> replies = new ArrayList<>();

                for (DataSnapshot replySnapshot : dataSnapshot.getChildren()) {
                    try {
                        VideoComment reply = replySnapshot.getValue(VideoComment.class);
                        if (reply != null) {
                            reply.setId(replySnapshot.getKey());
                            replies.add(reply);
                        }
                    } catch (Exception e) {
                        android.util.Log.w("VideoRepository",
                            "Không thể parse reply trong realtime: " + replySnapshot.getKey(), e);
                    }
                }

                Collections.sort(replies, (r1, r2) -> {
                    if (r1.getCreatedAt() instanceof Long && r2.getCreatedAt() instanceof Long) {
                        return Long.compare((Long) r1.getCreatedAt(), (Long) r2.getCreatedAt());
                    }
                    return 0;
                });

                callback.onSuccess(replies);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Lỗi realtime replies listener: " + databaseError.getMessage());
            }
        };

        activeListeners.put(listenerKey, listener);
        query.addValueEventListener(listener);
    }

    // ==================== CLEANUP OPERATIONS ====================

    @Override
    public void cleanup() {
        // Dọn dẹp tất cả listeners để tránh memory leak
        for (Map.Entry<String, ValueEventListener> entry : activeListeners.entrySet()) {
            String key = entry.getKey();
            ValueEventListener listener = entry.getValue();

            try {
                if (key.startsWith("like_")) {
                    // Extract videoId và userId từ key
                    String[] parts = key.split("_");
                    if (parts.length >= 3) {
                        String videoId = parts[1];
                        String userId = parts[2];
                        videosRef.child(videoId).child("likes").child(userId).removeEventListener(listener);
                    }
                } else if (key.startsWith("comments_")) {
                    String videoId = key.substring("comments_".length());
                    videosRef.child(videoId).child("comments").removeEventListener(listener);
                } else if (key.startsWith("replies_")) {
                    String[] parts = key.split("_");
                    if (parts.length >= 3) {
                        String videoId = parts[1];
                        videosRef.child(videoId).child("comments").removeEventListener(listener);
                    }
                }
            } catch (Exception e) {
                android.util.Log.w("VideoRepository", "Lỗi khi cleanup listener: " + key, e);
            }
        }

        activeListeners.clear();
    }

    @Override
    public void incrementViewCount(String videoId, BooleanCallback callback) {
        if (videoId == null || videoId.isEmpty()) {
            if (callback != null) {
                callback.onError("Video ID không hợp lệ");
            }
            return;
        }

        DatabaseReference videoRef = videosRef.child(videoId);

        // Cập nhật view count bằng cách tăng giá trị hiện tại lên 1
        videoRef.child("viewCount").runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(com.google.firebase.database.MutableData mutableData) {
                Integer currentViewCount = mutableData.getValue(Integer.class);
                if (currentViewCount == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(currentViewCount + 1);
                }
                return com.google.firebase.database.Transaction.success(mutableData);
            }

            @Override
            public void onComplete(com.google.firebase.database.DatabaseError databaseError,
                                 boolean committed,
                                 com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (callback != null) {
                    if (databaseError == null && committed) {
                        callback.onSuccess(true);
                    } else {
                        callback.onError("Không thể cập nhật view count: " +
                            (databaseError != null ? databaseError.getMessage() : "Transaction không thành công"));
                    }
                }
            }
        });
    }

    /**
     * 🎯 NEW: Track video view for personalization
     * Lưu video vào danh sách đã xem của user để không hiển thị lại
     */
    public void trackVideoView(String videoId, String userId) {
        if (videoId == null || videoId.isEmpty() || userId == null || userId.isEmpty()) {
            return;
        }

        // Lưu timestamp khi user xem video
        DatabaseReference watchedRef = usersRef.child(userId).child("watchedVideos").child(videoId);
        watchedRef.setValue(com.google.firebase.database.ServerValue.TIMESTAMP)
            .addOnSuccessListener(aVoid -> {
                android.util.Log.d("VideoRepository", "Tracked view for video: " + videoId);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("VideoRepository", "Failed to track view for video: " + videoId, e);
            });
    }

    /**
     * 🎯 NEW: Track user interaction with video for learning
     * Lưu các tương tác (like, comment, watch time) để cải thiện đề xuất
     */
    public void trackVideoInteraction(String videoId, String userId, String interactionType, long watchTimeMs) {
        if (videoId == null || videoId.isEmpty() || userId == null || userId.isEmpty()) {
            return;
        }

        Map<String, Object> interaction = new HashMap<>();
        interaction.put("timestamp", com.google.firebase.database.ServerValue.TIMESTAMP);
        interaction.put("type", interactionType); // "view", "like", "comment", "share"
        interaction.put("watchTimeMs", watchTimeMs);

        DatabaseReference interactionRef = usersRef.child(userId)
            .child("videoInteractions")
            .child(videoId)
            .push();

        interactionRef.setValue(interaction)
            .addOnSuccessListener(aVoid -> {
                android.util.Log.d("VideoRepository", "Tracked interaction (" + interactionType + ") for video: " + videoId);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("VideoRepository", "Failed to track interaction for video: " + videoId, e);
            });
    }

    // ==================== HELPER METHODS ====================

    private boolean validateParams(String videoId, String userId, BooleanCallback callback) {
        if (videoId == null || videoId.isEmpty() || userId == null || userId.isEmpty()) {
            if (callback != null) {
                callback.onError("Video ID và User ID không được để trống");
            }
            return false;
        }

        if (callback == null) {
            return false;
        }

        return true;
    }

    private boolean validateCommentParams(String videoId, String userId, String text) {
        return videoId != null && !videoId.isEmpty() &&
               userId != null && !userId.isEmpty() &&
               text != null && !text.trim().isEmpty() && text.trim().length() <= 500;
    }

    private boolean validateCommentLikeParams(String videoId, String commentId, String userId, BooleanCallback callback) {
        if (videoId == null || videoId.isEmpty() ||
            commentId == null || commentId.isEmpty() ||
            userId == null || userId.isEmpty()) {
            if (callback != null) {
                callback.onError("Tham số không hợp lệ");
            }
            return false;
        }

        if (callback == null) {
            return false;
        }

        return true;
    }

    /**
     * Tạo key cho cache trạng thái like
     */
    private String getLikeStatusCacheKey(String videoId, String userId) {
        return videoId + "_" + userId;
    }

    /**
     * Lưu trạng thái like vào cache
     */
    private void cacheLikeStatus(String videoId, String userId, boolean isLiked) {
        if (videoId != null && userId != null && !videoId.isEmpty() && !userId.isEmpty()) {
            String cacheKey = getLikeStatusCacheKey(videoId, userId);
            likeStatusCache.put(cacheKey, isLiked);
        }
    }

    /**
     * Lấy trạng thái like từ cache
     * @return Boolean trạng thái like hoặc null nếu không có trong cache
     */
    private Boolean getCachedLikeStatus(String videoId, String userId) {
        if (videoId == null || userId == null || videoId.isEmpty() || userId.isEmpty()) {
            return null;
        }
        String cacheKey = getLikeStatusCacheKey(videoId, userId);
        return likeStatusCache.get(cacheKey);
    }

    /**
     * Xóa trạng thái like khỏi cache
     */
    private void clearLikeStatusCache(String videoId, String userId) {
        if (videoId != null && userId != null && !videoId.isEmpty() && !userId.isEmpty()) {
            String cacheKey = getLikeStatusCacheKey(videoId, userId);
            likeStatusCache.remove(cacheKey);
        }
    }

    @Override
    public void getLikedVideos(String userId, VideoCallback callback) {
        android.util.Log.d("FirebaseVideoRepo", "getLikedVideos được gọi với userId: " + userId);

        if (callback == null || userId == null || userId.isEmpty()) {
            android.util.Log.e("FirebaseVideoRepo", "Tham số không hợp lệ - callback: " + (callback != null) + ", userId: " + userId);
            if (callback != null) {
                callback.onError("User ID không hợp lệ");
            }
            return;
        }

        android.util.Log.d("FirebaseVideoRepo", "Bắt đầu query Firebase cho videos có status=ready hoặc published");

        // Lấy tất cả videos, filter theo status trong code (vì Firebase không hỗ trợ OR query)
        videosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        android.util.Log.d("FirebaseVideoRepo", "Firebase query trả về " + dataSnapshot.getChildrenCount() + " videos");

                        List<ShortVideo> likedVideos = new ArrayList<>();

                        // Tạo map để lưu trữ dữ liệu video theo ID
                        Map<String, DataSnapshot> videoDataMap = new HashMap<>();
                        List<String> videoIds = new ArrayList<>();

                        // Collect all video data and IDs (chỉ lấy video có status = "ready" hoặc "published")
                        for (DataSnapshot videoSnapshot : dataSnapshot.getChildren()) {
                            String videoId = videoSnapshot.getKey();
                            String status = videoSnapshot.child("status").getValue(String.class);

                            if (videoId != null && ("ready".equals(status) || "published".equals(status))) {
                                videoIds.add(videoId);
                                videoDataMap.put(videoId, videoSnapshot);
                                android.util.Log.d("FirebaseVideoRepo", "Tìm thấy video [" + status + "]: " + videoId);
                            }
                        }

                        android.util.Log.d("FirebaseVideoRepo", "Tổng cộng " + videoIds.size() + " videos (ready/published) để kiểm tra like status");

                        if (videoIds.isEmpty()) {
                            android.util.Log.d("FirebaseVideoRepo", "Không có video nào trong database");
                            callback.onSuccess(likedVideos);
                            return;
                        }

                        final CountDownLatch latch = new CountDownLatch(videoIds.size());

                        // Check each video if user has liked it
                        for (String videoId : videoIds) {
                            android.util.Log.d("FirebaseVideoRepo", "Kiểm tra like status cho video: " + videoId);

                            DatabaseReference likeRef = videosRef.child(videoId)
                                    .child(Constants.VIDEO_LIKES_REF).child(userId);

                            likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot likeSnapshot) {
                                    android.util.Log.d("FirebaseVideoRepo", "Like check cho video " + videoId + " - exists: " + likeSnapshot.exists());

                                    if (likeSnapshot.exists()) {
                                        android.util.Log.d("FirebaseVideoRepo", "User đã like video: " + videoId);
                                        // User has liked this video, get video details từ map
                                        DataSnapshot videoData = videoDataMap.get(videoId);
                                        if (videoData != null) {
                                            try {
                                                ShortVideo video = ShortVideoDeserializer.fromDataSnapshot(videoData);
                                                if (video != null) {
                                                    video.setLiked(true);
                                                    synchronized (likedVideos) {
                                                        likedVideos.add(video);
                                                        android.util.Log.d("FirebaseVideoRepo", "Đã thêm liked video: " + video.getTitle() + " (ID: " + videoId + ")");
                                                    }
                                                } else {
                                                    android.util.Log.w("FirebaseVideoRepo", "ShortVideoDeserializer trả về null cho video: " + videoId);
                                                }
                                            } catch (Exception e) {
                                                android.util.Log.w("FirebaseVideoRepo",
                                                    "Không thể parse liked video: " + videoId, e);
                                            }
                                        } else {
                                            android.util.Log.w("FirebaseVideoRepo", "Không tìm thấy videoData trong map cho videoId: " + videoId);
                                        }
                                    }
                                    latch.countDown();
                                    android.util.Log.d("FirebaseVideoRepo", "Còn lại " + latch.getCount() + " video cần kiểm tra");
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    android.util.Log.w("FirebaseVideoRepo",
                                        "Lỗi khi kiểm tra like status cho video: " + videoId, databaseError.toException());
                                    latch.countDown();
                                }
                            });
                        }

                        // Wait for all checks to complete then sort by upload date
                        new Thread(() -> {
                            try {
                                android.util.Log.d("FirebaseVideoRepo", "Đang chờ tất cả like checks hoàn thành...");

                                if (latch.await(15, TimeUnit.SECONDS)) {
                                    android.util.Log.d("FirebaseVideoRepo", "Tất cả checks hoàn thành. Tìm thấy " + likedVideos.size() + " liked videos");

                                    // Sort by upload date (most recent first)
                                    synchronized (likedVideos) {
                                        Collections.sort(likedVideos, (v1, v2) ->
                                            Long.compare(v2.getUploadDate(), v1.getUploadDate()));
                                    }

                                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                                    mainHandler.post(() -> {
                                        android.util.Log.d("FirebaseVideoRepo", "Trả về kết quả với " + likedVideos.size() + " liked videos");
                                        callback.onSuccess(new ArrayList<>(likedVideos));
                                    });
                                } else {
                                    android.util.Log.e("FirebaseVideoRepo", "Timeout khi chờ like checks");
                                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                                    mainHandler.post(() -> callback.onError("Timeout khi tải video đã like"));
                                }
                            } catch (InterruptedException e) {
                                android.util.Log.e("FirebaseVideoRepo", "Thread bị interrupt", e);
                                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                                mainHandler.post(() -> callback.onError("Lỗi khi tải video đã like: " + e.getMessage()));
                            }
                        }).start();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        android.util.Log.e("FirebaseVideoRepo", "Firebase query bị cancelled", databaseError.toException());
                        callback.onError("Lỗi Firebase: " + databaseError.getMessage());
                    }
                });
    }
}
