package com.vhn.doan.presentation.profile;

import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.repository.VideoRepository;
import com.vhn.doan.presentation.base.BasePresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * Presenter quản lý logic nghiệp vụ cho màn hình video đã like
 * Tuân theo kiến trúc MVP
 */
public class LikedVideosPresenter extends BasePresenter<LikedVideosView> {

    private final VideoRepository videoRepository;
    private List<ShortVideo> likedVideos = new ArrayList<>();
    private String currentUserId;

    public LikedVideosPresenter(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @Override
    public void start() {
        // Sẽ được gọi từ loadLikedVideos
    }

    /**
     * Tải danh sách video đã like của user
     */
    public void loadLikedVideos(String userId) {
        if (!isViewAttached()) return;

        if (userId == null || userId.isEmpty()) {
            android.util.Log.e("LikedVideosPresenter", "User ID null hoặc empty");
            view.showError("Vui lòng đăng nhập để xem video đã like");
            return;
        }

        android.util.Log.d("LikedVideosPresenter", "Bắt đầu load liked videos cho user: " + userId);
        this.currentUserId = userId;
        view.showLoading();

        videoRepository.getLikedVideos(userId, new VideoRepository.VideoCallback() {
            @Override
            public void onSuccess(List<ShortVideo> videos) {
                android.util.Log.d("LikedVideosPresenter", "Load thành công " + (videos != null ? videos.size() : 0) + " liked videos");
                if (!isViewAttached()) return;

                view.hideLoading();
                likedVideos = videos;

                if (videos == null || videos.isEmpty()) {
                    android.util.Log.d("LikedVideosPresenter", "Danh sách liked videos trống, hiển thị empty state");
                    view.showEmptyState();
                } else {
                    android.util.Log.d("LikedVideosPresenter", "Hiển thị " + videos.size() + " liked videos");
                    view.showLikedVideos(videos);
                }
            }

            @Override
            public void onError(String errorMessage) {
                android.util.Log.e("LikedVideosPresenter", "Lỗi khi load liked videos: " + errorMessage);
                if (!isViewAttached()) return;

                view.hideLoading();
                view.showError(errorMessage);
            }
        });
    }

    /**
     * Refresh danh sách video đã like
     */
    public void refreshLikedVideos() {
        if (currentUserId != null) {
            loadLikedVideos(currentUserId);
        }
    }

    /**
     * Xử lý khi user click vào một video trong grid
     */
    public void onVideoClick(int position) {
        if (!isViewAttached() || likedVideos == null || position < 0 || position >= likedVideos.size()) {
            return;
        }

        // Mở màn hình phát video từ vị trí được chọn
        view.openVideoPlayer(likedVideos, position);
    }

    /**
     * Lấy danh sách video đã like hiện tại
     */
    public List<ShortVideo> getLikedVideos() {
        return likedVideos;
    }

    /**
     * Lấy số lượng video đã like
     */
    public int getLikedVideosCount() {
        return likedVideos != null ? likedVideos.size() : 0;
    }

    /**
     * Cập nhật trạng thái like của video (khi user unlike từ video player)
     */
    public void updateVideoLikeStatus(String videoId, boolean isLiked) {
        if (likedVideos == null || videoId == null) return;

        for (int i = 0; i < likedVideos.size(); i++) {
            ShortVideo video = likedVideos.get(i);
            if (videoId.equals(video.getId())) {
                if (!isLiked) {
                    // Video đã bị unlike, remove khỏi danh sách
                    likedVideos.remove(i);
                    if (isViewAttached()) {
                        view.removeVideoFromGrid(i);
                    }
                }
                break;
            }
        }

        // Kiểm tra nếu danh sách trống sau khi remove
        if (likedVideos.isEmpty() && isViewAttached()) {
            view.showEmptyState();
        }
    }
}
