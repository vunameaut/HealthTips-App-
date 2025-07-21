package com.vhn.doan.presentation.healthtip.detail;

import com.vhn.doan.presentation.base.BasePresenterInterface;

/**
 * Interface HealthTipDetailPresenter định nghĩa các phương thức cho presenter
 * xử lý logic cho màn hình chi tiết bài viết sức khỏe
 * Tuân theo kiến trúc MVP (Model-View-Presenter)
 */
public interface HealthTipDetailPresenter extends BasePresenterInterface {

    /**
     * Lấy thông tin chi tiết của một bài viết sức khỏe
     * @param tipId ID của bài viết
     */
    void loadHealthTipDetail(String tipId);

    /**
     * Xử lý khi người dùng nhấn nút yêu thích
     * @param tipId ID của bài viết
     */
    void onFavoriteClick(String tipId);

    /**
     * Xử lý khi người dùng nhấn nút thích
     * @param tipId ID của bài viết
     */
    void onLikeClick(String tipId);

    /**
     * Xử lý khi người dùng nhấn nút chia sẻ
     * @param tipId ID của bài viết
     */
    void onShareClick(String tipId);

    /**
     * Cập nhật số lượt xem
     * @param tipId ID của bài viết
     */
    void updateViewCount(String tipId);
}
