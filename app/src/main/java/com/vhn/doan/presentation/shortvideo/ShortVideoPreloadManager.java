package com.vhn.doan.presentation.shortvideo;

import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.repository.RepositoryCallback;
import com.vhn.doan.data.repository.ShortVideoRepository;
import com.vhn.doan.data.repository.ShortVideoRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton hỗ trợ preload danh sách video ngắn để tránh giật lag khi mở fragment.
 * Khi ứng dụng khởi động sẽ tải sẵn một số video. Sau đó khi người dùng xem
 * gần cuối danh sách, manager sẽ tự tải thêm video tiếp theo.
 */
public class ShortVideoPreloadManager {

    private static ShortVideoPreloadManager instance;

    private final ShortVideoRepository repository;
    private final List<ShortVideo> cachedVideos = new ArrayList<>();

    private ShortVideoPreloadManager() {
        repository = new ShortVideoRepositoryImpl();
    }

    public static synchronized ShortVideoPreloadManager getInstance() {
        if (instance == null) {
            instance = new ShortVideoPreloadManager();
        }
        return instance;
    }

    /**
     * Tải trước 10 video đầu tiên. Chỉ thực hiện một lần khi ứng dụng khởi chạy.
     */
    public void preloadInitialVideos() {
        ensurePreloaded(10, null);
    }

    /**
     * Đảm bảo rằng có ít nhất {@code requiredCount} video được tải sẵn.
     * Nếu chưa đủ, sẽ gọi repository để tải thêm.
     */
    public void ensurePreloaded(int requiredCount, RepositoryCallback<List<ShortVideo>> callback) {
        if (cachedVideos.size() >= requiredCount) {
            if (callback != null) {
                callback.onSuccess(new ArrayList<>(cachedVideos));
            }
            return;
        }

        repository.getTrendingVideos(requiredCount, new RepositoryCallback<List<ShortVideo>>() {
            @Override
            public void onSuccess(List<ShortVideo> videos) {
                cachedVideos.clear();
                cachedVideos.addAll(videos);
                if (callback != null) {
                    callback.onSuccess(new ArrayList<>(videos));
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    /**
     * Lấy danh sách video đã được cache.
     */
    public List<ShortVideo> getCachedVideos() {
        return new ArrayList<>(cachedVideos);
    }

    /**
     * Gán danh sách video sẵn có (ví dụ: danh sách video đã like hoặc kết quả tìm kiếm)
     */
    public void setCachedVideos(List<ShortVideo> videos) {
        cachedVideos.clear();
        if (videos != null) {
            cachedVideos.addAll(videos);
        }
    }
}
