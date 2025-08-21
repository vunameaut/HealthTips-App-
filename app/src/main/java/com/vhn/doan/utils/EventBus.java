// filepath: D:/app/doan/app/src/main/java/com/vhn/doan/utils/EventBus.java
package com.vhn.doan.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;
import java.util.Map;

/**
 * EventBus đơn giản sử dụng LiveData để truyền các sự kiện trong ứng dụng
 */
public class EventBus {

    private static EventBus instance;

    // LiveData để theo dõi sự kiện thích/bỏ thích video
    private final MutableLiveData<Map<String, Boolean>> videoLikeStatusLiveData = new MutableLiveData<>(new HashMap<>());

    // Private constructor để ngăn việc tạo instance từ bên ngoài
    private EventBus() {
    }

    /**
     * Singleton instance
     */
    public static synchronized EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    /**
     * Đăng ký lắng nghe sự kiện thay đổi trạng thái thích video
     *
     * @return LiveData đại diện cho các cập nhật trạng thái thích video
     */
    public LiveData<Map<String, Boolean>> getVideoLikeStatusLiveData() {
        return videoLikeStatusLiveData;
    }

    /**
     * Cập nhật trạng thái thích video
     *
     * @param videoId ID của video
     * @param isLiked Trạng thái thích (true: đã thích, false: chưa thích)
     */
    public void updateVideoLikeStatus(@NonNull String videoId, boolean isLiked) {
        Map<String, Boolean> currentMap = videoLikeStatusLiveData.getValue();
        if (currentMap == null) {
            currentMap = new HashMap<>();
        } else {
            // Tạo map mới để trigger LiveData observers
            currentMap = new HashMap<>(currentMap);
        }

        // Cập nhật trạng thái thích của video
        currentMap.put(videoId, isLiked);
        videoLikeStatusLiveData.postValue(currentMap);
    }

    /**
     * Kiểm tra trạng thái thích của video theo ID
     *
     * @param videoId ID của video
     * @return true nếu video được thích, false nếu không
     */
    public boolean isVideoLiked(String videoId) {
        Map<String, Boolean> statusMap = videoLikeStatusLiveData.getValue();
        if (statusMap != null && statusMap.containsKey(videoId)) {
            return statusMap.get(videoId);
        }
        return false; // Mặc định là chưa thích nếu không có thông tin
    }
}
