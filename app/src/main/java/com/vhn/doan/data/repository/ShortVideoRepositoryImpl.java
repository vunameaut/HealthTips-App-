package com.vhn.doan.data.repository;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.data.ShortVideo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation của ShortVideoRepository
 * Xử lý việc lấy và quản lý dữ liệu video ngắn từ Firebase
 */
public class ShortVideoRepositoryImpl implements ShortVideoRepository {

    private final DatabaseReference databaseReference;
    private final DatabaseReference videosRef;
    private final DatabaseReference usersRef;
    private final DatabaseReference userTopicsRef;
    private final DatabaseReference trendingVideosRef;

    public ShortVideoRepositoryImpl() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
        this.videosRef = databaseReference.child("videos");
        this.usersRef = databaseReference.child("users");
        this.userTopicsRef = databaseReference.child("user_topics");
        this.trendingVideosRef = databaseReference.child("trendingVideos").child("vietnam");
    }

    @Override
    public void getRecommendedVideos(String userId, int limit, RepositoryCallback<List<ShortVideo>> callback) {
        // Lấy sở thích người dùng trước
        getUserPreferences(userId, new RepositoryCallback<Map<String, Float>>() {
            @Override
            public void onSuccess(Map<String, Float> userPreferences) {
                if (userPreferences == null || userPreferences.isEmpty()) {
                    // Người dùng mới hoặc chưa có sở thích -> lấy video trending
                    getTrendingVideos(limit, callback);
                } else {
                    // Lấy video theo sở thích người dùng
                    getVideosBasedOnPreferences(userPreferences, limit, callback);
                }
            }

            @Override
            public void onError(String error) {
                // Fallback về trending videos nếu có lỗi
                getTrendingVideos(limit, callback);
            }
        });
    }

    @Override
    public void getVideosByCategory(String categoryId, int limit, RepositoryCallback<List<ShortVideo>> callback) {
        Query query = videosRef.orderByChild("categoryId").equalTo(categoryId).limitToLast(limit);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<ShortVideo> videos = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ShortVideo video = snapshot.getValue(ShortVideo.class);
                    if (video != null) {
                        // Tự động set ID từ Firebase key
                        video.setId(snapshot.getKey());
                        videos.add(video);
                    }
                }

                // Sắp xếp theo uploadDate giảm dần (mới nhất trước)
                videos.sort((v1, v2) -> Long.compare(v2.getUploadDate(), v1.getUploadDate()));

                callback.onSuccess(videos);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    @Override
    public void getTrendingVideos(int limit, RepositoryCallback<List<ShortVideo>> callback) {
        // Lấy video trending từ node riêng hoặc sắp xếp theo viewCount + likeCount
        videosRef.orderByChild("viewCount").limitToLast(limit * 2)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<ShortVideo> videos = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ShortVideo video = snapshot.getValue(ShortVideo.class);
                    if (video != null) {
                        // Tự động set ID từ Firebase key
                        video.setId(snapshot.getKey());
                        videos.add(video);
                    }
                }

                // Sắp xếp theo điểm trending (kết hợp viewCount, likeCount và thời gian)
                Collections.sort(videos, new Comparator<ShortVideo>() {
                    @Override
                    public int compare(ShortVideo v1, ShortVideo v2) {
                        // Tính điểm trending: viewCount + likeCount * 2 + bonus cho video mới
                        long now = System.currentTimeMillis();
                        long dayInMillis = 24 * 60 * 60 * 1000;

                        int score1 = v1.getViewCount() + v1.getLikeCount() * 2;
                        int score2 = v2.getViewCount() + v2.getLikeCount() * 2;

                        // Bonus cho video được đăng trong 7 ngày gần đây
                        if (now - v1.getUploadDate() < 7 * dayInMillis) {
                            score1 += 50;
                        }
                        if (now - v2.getUploadDate() < 7 * dayInMillis) {
                            score2 += 50;
                        }

                        return Integer.compare(score2, score1); // Giảm dần
                    }
                });

                // Lấy số lượng video theo limit
                if (videos.size() > limit) {
                    videos = videos.subList(0, limit);
                }

                callback.onSuccess(videos);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    @Override
    public void incrementViewCount(String videoId, RepositoryCallback<Void> callback) {
        // Kiểm tra videoId không null để tránh crash
        if (videoId == null || videoId.trim().isEmpty()) {
            callback.onError("Video ID không hợp lệ");
            return;
        }

        DatabaseReference videoRef = videosRef.child(videoId).child("viewCount");

        videoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer currentCount = dataSnapshot.getValue(Integer.class);
                int newCount = (currentCount != null) ? currentCount + 1 : 1;

                videoRef.setValue(newCount).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError("Không thể cập nhật view count");
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    @Override
    public void updateLikeCount(String videoId, boolean isLiked, RepositoryCallback<Void> callback) {
        DatabaseReference videoRef = videosRef.child(videoId).child("likeCount");

        videoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer currentCount = dataSnapshot.getValue(Integer.class);
                int currentLikes = (currentCount != null) ? currentCount : 0;
                int newCount = isLiked ? currentLikes + 1 : Math.max(0, currentLikes - 1);

                videoRef.setValue(newCount).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError("Không thể cập nhật like count");
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    @Override
    public void getUserPreferences(String userId, RepositoryCallback<Map<String, Float>> callback) {
        Map<String, Float> combinedPreferences = new HashMap<>();

        // Lấy preferences từ /users/{userId}/preferences
        usersRef.child(userId).child("preferences").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot preferencesSnapshot) {
                // Xử lý preferences
                if (preferencesSnapshot.exists()) {
                    for (DataSnapshot child : preferencesSnapshot.getChildren()) {
                        String tag = child.getKey();
                        Object value = child.getValue();

                        if (value instanceof Number) {
                            combinedPreferences.put(tag, ((Number) value).floatValue());
                        }
                    }
                }

                // Tiếp tục lấy user_topics
                getUserTopicsAsPreferences(userId, combinedPreferences, callback);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Nếu lỗi khi lấy preferences, vẫn thử lấy user_topics
                getUserTopicsAsPreferences(userId, combinedPreferences, callback);
            }
        });
    }

    /**
     * Lấy user topics và chuyển đổi thành preferences
     */
    private void getUserTopicsAsPreferences(String userId, Map<String, Float> existingPreferences,
                                          RepositoryCallback<Map<String, Float>> callback) {
        userTopicsRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot topicsSnapshot) {
                if (topicsSnapshot.exists()) {
                    for (DataSnapshot child : topicsSnapshot.getChildren()) {
                        String topic = child.getKey();
                        String mappedTag = mapTopicToTag(topic);

                        if (mappedTag != null) {
                            // Cho điểm thấp hơn preferences trực tiếp (0.5)
                            existingPreferences.put(mappedTag,
                                existingPreferences.getOrDefault(mappedTag, 0.0f) + 0.5f);
                        }
                    }
                }

                callback.onSuccess(existingPreferences);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onSuccess(existingPreferences);
            }
        });
    }

    /**
     * Ánh xạ topic từ AI chat thành tag video
     */
    private String mapTopicToTag(String topic) {
        if (topic == null) return null;

        String lowerTopic = topic.toLowerCase();

        // Ánh xạ các chủ đề thành tag tương ứng
        if (lowerTopic.contains("giảm cân") || lowerTopic.contains("giamcan")) {
            return "giamcan";
        } else if (lowerTopic.contains("tập luyện") || lowerTopic.contains("tapluyen")) {
            return "tapluyen";
        } else if (lowerTopic.contains("dinh dưỡng") || lowerTopic.contains("dinhduong")) {
            return "dinhduong";
        } else if (lowerTopic.contains("tim mạch") || lowerTopic.contains("timmach")) {
            return "timmach";
        } else if (lowerTopic.contains("yoga")) {
            return "yoga";
        } else if (lowerTopic.contains("sức khỏe") || lowerTopic.contains("suckhoe")) {
            return "suckhoe";
        }

        return null;
    }

    /**
     * Lấy video dựa trên sở thích người dùng
     */
    private void getVideosBasedOnPreferences(Map<String, Float> userPreferences, int limit,
                                           RepositoryCallback<List<ShortVideo>> callback) {
        videosRef.orderByChild("uploadDate").limitToLast(limit * 3)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<ShortVideo> allVideos = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ShortVideo video = snapshot.getValue(ShortVideo.class);
                    if (video != null) {
                        // Tự động set ID từ Firebase key
                        video.setId(snapshot.getKey());
                        allVideos.add(video);
                    }
                }

                // Tính điểm cho mỗi video dựa trên sở thích người dùng
                List<VideoWithScore> videosWithScore = new ArrayList<>();
                for (ShortVideo video : allVideos) {
                    float score = calculateVideoScore(video, userPreferences);
                    videosWithScore.add(new VideoWithScore(video, score));
                }

                // Sắp xếp theo điểm giảm dần
                Collections.sort(videosWithScore, (v1, v2) -> Float.compare(v2.score, v1.score));

                // Chuyển về List<ShortVideo> và giới hạn số lượng
                List<ShortVideo> recommendedVideos = new ArrayList<>();
                for (int i = 0; i < Math.min(limit, videosWithScore.size()); i++) {
                    recommendedVideos.add(videosWithScore.get(i).video);
                }

                callback.onSuccess(recommendedVideos);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    /**
     * Tính điểm cho video dựa trên sở thích người dùng
     */
    private float calculateVideoScore(ShortVideo video, Map<String, Float> userPreferences) {
        float score = 0.0f;

        // Điểm từ tags khớp với sở thích
        if (video.getTags() != null) {
            for (String tag : video.getTags().keySet()) {
                if (Boolean.TRUE.equals(video.getTags().get(tag)) && userPreferences.containsKey(tag)) {
                    score += userPreferences.get(tag) * 10; // Nhân 10 để tăng trọng số
                }
            }
        }

        // Bonus cho video mới
        long daysSinceUpload = (System.currentTimeMillis() - video.getUploadDate()) / (24 * 60 * 60 * 1000);
        if (daysSinceUpload <= 7) {
            score += 5.0f; // Bonus cho video mới trong 7 ngày
        }

        // Bonus từ engagement (view và like)
        score += video.getViewCount() * 0.01f + video.getLikeCount() * 0.1f;

        return score;
    }

    /**
     * Class helper để lưu video kèm điểm số
     */
    private static class VideoWithScore {
        final ShortVideo video;
        final float score;

        VideoWithScore(ShortVideo video, float score) {
            this.video = video;
            this.score = score;
        }
    }
}
