package com.vhn.doan.presentation.shortvideo;

import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.presentation.base.BaseView;

import java.util.List;

/**
 * Contract interface định nghĩa tương tác giữa View và Presenter
 * cho chức năng video ngắn
 */
public interface ShortVideoContract {

    interface View extends BaseView {
        /**
         * Hiển thị danh sách video
         */
        void showVideos(List<ShortVideo> videos);

        /**
         * Hiển thị thông báo lỗi
         */
        void showError(String message);

        /**
         * Hiển thị thông báo không có video
         */
        void showEmptyState();

        /**
         * Cập nhật like count cho video tại vị trí cụ thể
         */
        void updateVideoLike(int position, boolean isLiked, int newLikeCount);

        /**
         * Cập nhật view count cho video tại vị trí cụ thể
         */
        void updateVideoView(int position, int newViewCount);

        /**
         * Hiển thị thông báo thành công
         */
        void showSuccess(String message);

        /**
         * Refresh danh sách video
         */
        void refreshVideoList();

        /**
         * Set presenter cho view
         */
        void setPresenter(Presenter presenter);
    }

    interface Presenter {
        /**
         * Attach view to presenter
         */
        void attachView(View view);

        /**
         * Detach view from presenter
         */
        void detachView();

        /**
         * Start presenter
         */
        void start();

        /**
         * Tải danh sách video được đề xuất
         */
        void loadRecommendedVideos();

        /**
         * Tải video theo category
         */
        void loadVideosByCategory(String categoryId);

        /**
         * Tải video trending
         */
        void loadTrendingVideos();

        /**
         * Tải danh sách video đã like của người dùng
         */
        void loadLikedVideos();

        /**
         * Xử lý khi người dùng like/unlike video
         */
        void onVideoLiked(int position, String videoId, boolean isCurrentlyLiked);

        /**
         * Xử lý khi video được xem
         */
        void onVideoViewed(int position, String videoId);

        /**
         * Refresh dữ liệu
         */
        void refreshData();

        /**
         * Tải thêm video (pagination)
         */
        void loadMoreVideos();

        /**
         * Xử lý khi người dùng chia sẻ video
         */
        void onVideoShared(String videoId);

        /**
         * Lọc video theo tag
         */
        void filterVideosByTag(String tag);

        /**
         * Thêm bình luận cho video
         */
        void addComment(String videoId, String commentText);

        /**
         * Get video at position
         */
        ShortVideo getVideoAt(int position);
    }
}
