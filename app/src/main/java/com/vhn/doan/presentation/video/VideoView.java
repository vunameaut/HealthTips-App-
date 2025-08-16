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
     * @param videos Danh sách video đã được sắp xếp
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
     * Hiển thị thông báo không có video
     */
    void showEmptyState();

    /**
     * Ẩn thông báo không có video
     */
    void hideEmptyState();

    /**
     * Refresh video feed
     */
    void refreshVideoFeed();
}
