package com.vhn.doan.presentation.video;

import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.repository.VideoRepository;
import com.vhn.doan.presentation.base.BasePresenter;

import java.util.List;

/**
 * VideoPresenter xử lý logic nghiệp vụ cho màn hình video short
 * Tuân theo kiến trúc MVP
 */
public class VideoPresenter extends BasePresenter<VideoView> {

    private final VideoRepository videoRepository;
    private List<ShortVideo> currentVideos;
    private int currentPosition = 0;
    private String currentUserId;
    private String currentCountry;

    /**
     * Constructor với dependency injection
     * @param videoRepository Repository để lấy dữ liệu video
     */
    public VideoPresenter(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @Override
    public void start() {
        // Method từ BasePresenter - có thể để trống hoặc implement logic khởi tạo
        // VideoPresenter sẽ được start thông qua loadVideoFeed() method
    }

    /**
     * Tải video feed cho user
     * @param userId ID của user để lấy preferences
     * @param country Quốc gia để lấy trending videos
     */
    public void loadVideoFeed(String userId, String country) {
        if (!isViewAttached()) return;

        this.currentUserId = userId;
        this.currentCountry = country;

        view.showLoadingVideos();
        view.hideEmptyState();

        videoRepository.getFeed(userId, country, new VideoRepository.VideoCallback() {
            @Override
            public void onSuccess(List<ShortVideo> videos) {
                // Đảm bảo UI updates chỉ được thực hiện khi view vẫn được attach
                if (!isViewAttached()) return;

                view.hideLoadingVideos();

                if (videos == null || videos.isEmpty()) {
                    view.showEmptyState();
                    return;
                }

                currentVideos = videos;
                view.showVideoFeed(videos);

                // Tự động phát video đầu tiên
                if (!videos.isEmpty()) {
                    currentPosition = 0;
                    view.playVideoAtPosition(currentPosition);
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Đảm bảo UI updates chỉ được thực hiện khi view vẫn được attach
                if (!isViewAttached()) return;

                view.hideLoadingVideos();
                view.showError(errorMessage);
            }
        });
    }

    /**
     * Refresh video feed
     */
    public void refreshVideoFeed() {
        if (currentUserId != null) {
            loadVideoFeed(currentUserId, currentCountry);
        }
    }

    /**
     * Chuyển đến video tiếp theo
     */
    public void nextVideo() {
        if (!isViewAttached() || currentVideos == null || currentVideos.isEmpty()) {
            return;
        }

        view.pauseCurrentVideo();

        if (currentPosition < currentVideos.size() - 1) {
            currentPosition++;
        } else {
            // Quay về video đầu tiên nếu đã ở cuối danh sách
            currentPosition = 0;
        }

        view.playVideoAtPosition(currentPosition);
    }

    /**
     * Quay lại video trước đó
     */
    public void previousVideo() {
        if (!isViewAttached() || currentVideos == null || currentVideos.isEmpty()) {
            return;
        }

        view.pauseCurrentVideo();

        if (currentPosition > 0) {
            currentPosition--;
        } else {
            // Chuyển đến video cuối nếu đang ở đầu danh sách
            currentPosition = currentVideos.size() - 1;
        }

        view.playVideoAtPosition(currentPosition);
    }

    /**
     * Chuyển đến video ở vị trí cụ thể
     * @param position Vị trí video
     */
    public void playVideoAt(int position) {
        if (!isViewAttached() || currentVideos == null ||
            position < 0 || position >= currentVideos.size()) {
            return;
        }

        view.pauseCurrentVideo();
        currentPosition = position;
        view.playVideoAtPosition(currentPosition);
    }

    /**
     * Toggle play/pause video hiện tại
     */
    public void togglePlayPause() {
        if (!isViewAttached()) return;

        // Logic này sẽ được implement trong View layer
        // Presenter chỉ quản lý state
    }

    /**
     * Xử lý khi user like/unlike video
     * @param position Vị trí video trong danh sách
     */
    public void toggleLike(int position) {
        if (!isViewAttached() || currentVideos == null ||
            position < 0 || position >= currentVideos.size()) {
            return;
        }

        ShortVideo video = currentVideos.get(position);

        // Tạm thời tăng/giảm like count locally
        // TODO: Implement actual like/unlike logic với Firebase
        long newLikeCount = video.getLikeCount() + 1; // hoặc -1 nếu unlike
        video.setLikeCount(newLikeCount);

        view.updateVideoInfo(video, position);
    }

    /**
     * Cập nhật view count cho video
     * @param position Vị trí video
     */
    public void incrementViewCount(int position) {
        if (currentVideos == null || position < 0 || position >= currentVideos.size()) {
            return;
        }

        ShortVideo video = currentVideos.get(position);
        video.setViewCount(video.getViewCount() + 1);

        if (isViewAttached()) {
            view.updateVideoInfo(video, position);
        }

        // TODO: Implement actual view count update với Firebase
    }

    /**
     * Lấy video hiện tại
     * @return Video đang phát hoặc null
     */
    public ShortVideo getCurrentVideo() {
        if (currentVideos == null || currentPosition < 0 ||
            currentPosition >= currentVideos.size()) {
            return null;
        }
        return currentVideos.get(currentPosition);
    }

    /**
     * Lấy vị trí video hiện tại
     * @return Vị trí hiện tại
     */
    public int getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Lấy tổng số video
     * @return Số lượng video trong feed
     */
    public int getVideoCount() {
        return currentVideos != null ? currentVideos.size() : 0;
    }

    /**
     * Kiểm tra view có được attach không
     */
    @Override
    public boolean isViewAttached() {
        return view != null;
    }

    @Override
    public void detachView() {
        if (view != null) {
            view.pauseCurrentVideo();
        }
        super.detachView();
    }
}
