package com.vhn.doan.presentation.video;

import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.presentation.base.BaseView;

import java.util.List;

/**
 * Interface VideoView định nghĩa các phương thức giao diện cho màn hình video short
 * Tuân theo kiến trúc MVP
 */
public interface VideoView extends BaseView {

    /**
     * Hiển thị danh sách video feed
     * @param videos Danh sách video đã đư���c sắp xếp
     */
    void showVideoFeed(List<ShortVideo> videos);

    /**
     * Hiển thị thông báo lỗi khi tải video
     * @param message Thông báo lỗi
     */
    void showError(String message);

    /**
     * Hiển thị trạng thái loading khi tải video
     */
    void showLoadingVideos();

    /**
     * Ẩn trạng thái loading
     */
    void hideLoadingVideos();

    /**
     * Hiển thị video ở vị trí cụ thể
     * @param position Vị trí video trong danh sách
     */
    void playVideoAtPosition(int position);

    /**
     * Dừng video hiện tại
     */
    void pauseCurrentVideo();

    /**
     * Cập nhật thông tin video (view count, like count)
     * @param video Video cần cập nhật
     * @param position Vị trí trong danh sách
     */
    void updateVideoInfo(ShortVideo video, int position);

    /**
     * Cập nhật trạng thái like của video
     * @param position Vị trí video trong danh sách
     * @param isLiked Trạng thái like mới
     */
    void updateVideoLikeStatus(int position, boolean isLiked);

    /**
     * Revert UI khi like operation thất bại
     * @param position Vị trí video trong danh sách
     */
    void revertVideoLikeUI(int position);

    /**
     * Hiển thị bottom sheet comment cho video
     * @param videoId ID của video
     */
    void showCommentBottomSheet(String videoId);

    /**
     * Chia sẻ video
     * @param video Video cần chia sẻ
     */
    void shareVideo(ShortVideo video);

    /**
     * Hiển thị thông báo ngắn
     * @param message Nội dung thông báo
     */
    void showMessage(String message);

    /**
     * Hiển thị trạng thái empty khi không có video
     */
    void showEmptyState();

    /**
     * Ẩn trạng thái empty
     */
    void hideEmptyState();

    /**
     * Refresh video feed
     */
    void refreshVideoFeed();
}
