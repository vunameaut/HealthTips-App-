package com.vhn.doan.data.repository;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.ShortVideoDeserializer;
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
 * Đọc dữ liệu từ /videos, /users/{uid}/preferences, /trendingVideos/{country}
 * Sắp xếp theo: tags khớp preferences → uploadDate mới → viewCount → likeCount
 */
public class FirebaseVideoRepositoryImpl implements VideoRepository {

    private final FirebaseDatabase database;
    private final DatabaseReference videosRef;
    private final DatabaseReference usersRef;
    private final DatabaseReference trendingVideosRef;

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

        // Dữ liệu tạm để collect từ 3 sources
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
}
