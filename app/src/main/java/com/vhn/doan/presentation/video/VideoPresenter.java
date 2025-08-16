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
            position < 0 || position >= currentVideos.size() || currentUserId == null) {
            return;
        }

        ShortVideo video = currentVideos.get(position);

        // Kiểm tra trạng thái like hiện tại trước
        videoRepository.isVideoLiked(video.getId(), currentUserId, new VideoRepository.BooleanCallback() {
            @Override
            public void onSuccess(boolean isLiked) {
                if (!isViewAttached()) return;

                if (isLiked) {
                    // Unlike video
                    unlikeVideo(video, position);
                } else {
                    // Like video
                    likeVideo(video, position);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (!isViewAttached()) return;
                view.showError("Không thể kiểm tra trạng thái like: " + errorMessage);
            }
        });
    }

    /**
     * Like video
     */
    private void likeVideo(ShortVideo video, int position) {
        videoRepository.likeVideo(video.getId(), currentUserId, new VideoRepository.BooleanCallback() {
            @Override
            public void onSuccess(boolean result) {
                if (!isViewAttached()) return;

                // Cập nhật local data
                video.setLikeCount(video.getLikeCount() + 1);
                view.updateVideoLikeStatus(position, true);
                view.updateVideoInfo(video, position);
                view.showMessage("Đã thích video");
            }

            @Override
            public void onError(String errorMessage) {
                if (!isViewAttached()) return;
                view.revertVideoLikeUI(position);
                view.showError("Không thể thích video: " + errorMessage);
            }
        });
    }

    /**
     * Unlike video
     */
    private void unlikeVideo(ShortVideo video, int position) {
        videoRepository.unlikeVideo(video.getId(), currentUserId, new VideoRepository.BooleanCallback() {
            @Override
            public void onSuccess(boolean result) {
                if (!isViewAttached()) return;

                // Cập nhật local data
                video.setLikeCount(Math.max(0, video.getLikeCount() - 1));
                view.updateVideoLikeStatus(position, false);
                view.updateVideoInfo(video, position);
                view.showMessage("Đã bỏ thích video");
            }

            @Override
            public void onError(String errorMessage) {
                if (!isViewAttached()) return;
                view.revertVideoLikeUI(position);
                view.showError("Không thể bỏ thích video: " + errorMessage);
            }
        });
    }

    /**
     * Kiểm tra và cập nhật trạng thái like cho tất cả video đang hiển thị
     */
    public void checkLikeStatusForVisibleVideos() {
        if (!isViewAttached() || currentVideos == null || currentUserId == null) return;

        for (int i = 0; i < currentVideos.size(); i++) {
            final int position = i;
            ShortVideo video = currentVideos.get(i);

            videoRepository.isVideoLiked(video.getId(), currentUserId, new VideoRepository.BooleanCallback() {
                @Override
                public void onSuccess(boolean isLiked) {
                    if (!isViewAttached()) return;
                    view.updateVideoLikeStatus(position, isLiked);
                }

                @Override
                public void onError(String errorMessage) {
                    // Bỏ qua lỗi cho việc kiểm tra trạng thái like
                }
            });
        }
    }

    /**
     * Xử lý khi user click vào comment
     */
    public void onCommentClick(int position) {
        if (!isViewAttached() || currentVideos == null ||
            position < 0 || position >= currentVideos.size()) {
            return;
        }

        ShortVideo video = currentVideos.get(position);
        view.showCommentBottomSheet(video.getId());
    }

    /**
     * Xử lý khi user click share
     */
    public void onShareClick(int position) {
        if (!isViewAttached() || currentVideos == null ||
            position < 0 || position >= currentVideos.size()) {
            return;
        }

        ShortVideo video = currentVideos.get(position);
        view.shareVideo(video);
    }

    /**
     * Set user ID hiện tại
     */
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
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
     * Cập nhật view count cho video khi user xem
     * @param position Vị trí video trong danh sách
     */
    public void incrementViewCount(int position) {
        if (!isViewAttached() || currentVideos == null ||
            position < 0 || position >= currentVideos.size()) {
            return;
        }

        ShortVideo video = currentVideos.get(position);

        // Cập nhật view count locally trước
        video.setViewCount(video.getViewCount() + 1);
        view.updateVideoInfo(video, position);

        // TODO: Implement actual view count update với Firebase
        // Có thể implement sau khi có Firebase Functions để đảm bảo view count chính xác
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
