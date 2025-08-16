package com.vhn.doan.data.repository;

import com.vhn.doan.data.ShortVideo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test repository để cung cấp dữ liệu video mẫu khi Firebase chưa có dữ liệu
 * Giúp test chức năng video player và khắc phục màn hình đen
 */
public class TestVideoRepository implements VideoRepository {

    @Override
    public void getFeed(String userId, String country, VideoCallback callback) {
        // Tạo fake data để test
        List<ShortVideo> testVideos = createTestVideos();

        // Giả lập delay như Firebase
        new Thread(() -> {
            try {
                Thread.sleep(1000); // 1 giây delay
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onSuccess(testVideos));
            } catch (InterruptedException e) {
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onError("Test error: " + e.getMessage()));
            }
        }).start();
    }

    @Override
    public List<ShortVideo> getFeed(String userId, String country) {
        return createTestVideos();
    }

    private List<ShortVideo> createTestVideos() {
        List<ShortVideo> videos = new ArrayList<>();

        // Video 1: Test video với URL Cloudinary thực tế
        Map<String, Boolean> tags1 = new HashMap<>();
        tags1.put("sức khỏe", true);
        tags1.put("thể dục", true);

        ShortVideo video1 = new ShortVideo(
                "test_video_1",
                "5 Bài Tập Thể Dục Buổi Sáng",
                "Hướng dẫn các bài tập thể dục đơn giản để bắt đầu ngày mới đầy năng lượng",
                System.currentTimeMillis() - 86400000, // 1 ngày trước
                "fitness",
                tags1,
                15420, // view count
                892, // like count
                "user_health_expert",
                "ky-thuat-tho-sau_ip7gzo", // Cloudinary public ID
                1755156221, // Cloudinary version
                "ready"
        );
        videos.add(video1);

        // Video 2: Test video thứ 2 với cùng URL
        Map<String, Boolean> tags2 = new HashMap<>();
        tags2.put("dinh dưỡng", true);
        tags2.put("ăn uống", true);

        ShortVideo video2 = new ShortVideo(
                "test_video_2",
                "Thực Phẩm Tốt Cho Tim Mạch",
                "Khám phá những thực phẩm giúp bảo vệ và tăng cường sức khỏe tim mạch",
                System.currentTimeMillis() - 172800000, // 2 ngày trước
                "nutrition",
                tags2,
                8234, // view count
                456, // like count
                "user_nutrition_expert",
                "ky-thuat-tho-sau_ip7gzo", // Cloudinary public ID
                1755156221, // Cloudinary version
                "ready"
        );
        videos.add(video2);

        // Video 3: Test video thứ 3 với cùng URL
        Map<String, Boolean> tags3 = new HashMap<>();
        tags3.put("giấc ngủ", true);
        tags3.put("thư giãn", true);

        ShortVideo video3 = new ShortVideo(
                "test_video_3",
                "Cách Có Giấc Ngủ Ngon",
                "Những bí quyết đơn giản để có một giấc ngủ sâu và phục hồi hoàn toàn",
                System.currentTimeMillis() - 259200000, // 3 ngày trước
                "sleep",
                tags3,
                12567, // view count
                723, // like count
                "user_sleep_expert",
                "ky-thuat-tho-sau_ip7gzo", // Cloudinary public ID
                1755156221, // Cloudinary version
                "ready"
        );
        videos.add(video3);

        android.util.Log.d("TestVideoRepository", "Tạo " + videos.size() + " test videos với Cloudinary URL");
        return videos;
    }
}
