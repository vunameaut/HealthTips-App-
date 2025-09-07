package com.vhn.doan.presentation.profile;

import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.presentation.base.BaseView;

import java.util.List;

/**
 * View interface cho màn hình video đã like
 * Tuân theo kiến trúc MVP
 */
public interface LikedVideosView extends BaseView {

    /**
     * Hiển thị danh sách video đã like dạng lưới
     */
    void showLikedVideos(List<ShortVideo> videos);

    /**
     * Hiển thị trạng thái loading
     */
    void showLoading();

    /**
     * Ẩn trạng thái loading
     */
    void hideLoading();

    /**
     * Hiển thị trạng thái empty khi không có video nào được like
     */
    void showEmptyState();

    /**
     * Ẩn trạng thái empty
     */
    void hideEmptyState();

    /**
     * Hiển thị thông báo lỗi
     */
    void showError(String message);

    /**
     * Mở video player với danh sách video và vị trí bắt đầu
     */
    void openVideoPlayer(List<ShortVideo> videos, int startPosition);

    /**
     * Remove một video khỏi grid tại vị trí chỉ định
     */
    void removeVideoFromGrid(int position);

    /**
     * Refresh toàn bộ grid
     */
    void refreshGrid();
}
