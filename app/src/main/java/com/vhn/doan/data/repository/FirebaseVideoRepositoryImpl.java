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
 * Sắp xếp theo: tags khớp preferences → uploadDate mới → viewCount → likeCount
 */
public class FirebaseVideoRepositoryImpl implements VideoRepository {

    private final FirebaseDatabase database;
    private final DatabaseReference videosRef;
    private final DatabaseReference usersRef;
    private final DatabaseReference trendingVideosRef;

    // Map để quản lý các listener realtime
    private final Map<String, ValueEventListener> activeListeners = new HashMap<>();

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

        // Dữ li���u tạm để collect từ 3 sources
        final List<ShortVideo> allVideos = new ArrayList<>();
        final Map<String, Boolean> userPreferences = new HashMap<>();
        final List<String> trendingVideoIds = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(3);

        // 1. Lấy tất cả videos có status == "ready"
        videosRef.orderByChild("status").equalTo("ready")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            for (DataSnapshot videoSnapshot : dataSnapshot.getChildren()) {
                                try {
                                    // Sử dụng custom deserializer thay vì getValue(ShortVideo.class)
                                    ShortVideo video = ShortVideoDeserializer.fromDataSnapshot(videoSnapshot);
                                    if (video != null) {
                                        allVideos.add(video);
                                    }
                                } catch (Exception e) {
                                    // Log lỗi conversion cho video cụ thể và skip video đó
                                    android.util.Log.w("VideoRepository",
                                        "Không thể convert video với ID: " + videoSnapshot.getKey() +
                                        ", Lỗi: " + e.getMessage());
                                }
                            }
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

        // Chờ tất cả requests hoàn thành và sắp xếp kết quả
        new Thread(() -> {
            try {
                // Timeout sau 10 giây
                if (latch.await(10, TimeUnit.SECONDS)) {
                    List<ShortVideo> sortedVideos = sortVideosByPreferences(allVideos, userPreferences, trendingVideoIds);

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
     * Sắp xếp videos theo thứ tự ưu tiên:
     * 1. Tags khớp với preferences
     * 2. Upload date mới nhất
     * 3. View count cao nhất
     * 4. Like count cao nhất
     */
    private List<ShortVideo> sortVideosByPreferences(List<ShortVideo> videos,
                                                    Map<String, Boolean> userPreferences,
                                                    List<String> trendingVideoIds) {
        Collections.sort(videos, new Comparator<ShortVideo>() {
            @Override
            public int compare(ShortVideo v1, ShortVideo v2) {
                // 1. Ưu tiên videos có tags khớp với preferences
                int matchScore1 = calculateTagMatchScore(v1, userPreferences);
                int matchScore2 = calculateTagMatchScore(v2, userPreferences);

                if (matchScore1 != matchScore2) {
                    return Integer.compare(matchScore2, matchScore1); // Descending
                }

                // 2. Ưu tiên trending videos
                boolean isTrending1 = trendingVideoIds.contains(v1.getId());
                boolean isTrending2 = trendingVideoIds.contains(v2.getId());

                if (isTrending1 != isTrending2) {
                    return Boolean.compare(isTrending2, isTrending1); // Trending first
                }

                // 3. Upload date mới nhất
                if (v1.getUploadDate() != v2.getUploadDate()) {
                    return Long.compare(v2.getUploadDate(), v1.getUploadDate()); // Descending
                }

                // 4. View count cao nhất
                if (v1.getViewCount() != v2.getViewCount()) {
                    return Long.compare(v2.getViewCount(), v1.getViewCount()); // Descending
                }

                // 5. Like count cao nhất
                return Long.compare(v2.getLikeCount(), v1.getLikeCount()); // Descending
            }
        });

        return videos;
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

        DatabaseReference likeRef = videosRef.child(videoId).child(Constants.VIDEO_LIKES_REF).child(userId);

        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                callback.onSuccess(dataSnapshot.exists());
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

                            // Chỉ lấy comments gốc (không có parentId hoặc parentId = null/empty)
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
}
