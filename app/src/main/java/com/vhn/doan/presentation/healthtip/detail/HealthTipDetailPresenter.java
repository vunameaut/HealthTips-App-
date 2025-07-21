package com.vhn.doan.presentation.healthtip.detail;

import com.vhn.doan.presentation.base.BasePresenter;

/**
 * Interface HealthTipDetailPresenter định nghĩa các phương thức cho presenter
 * xử lý logic cho màn hình chi tiết bài viết sức khỏe
 * Tuân theo kiến trúc MVP (Model-View-Presenter)
 */
public interface HealthTipDetailPresenter {

    /**
     * Lấy thông tin chi tiết của một bài viết sức khỏe
     * @param healthTipId ID của bài viết cần hiển thị
     */
    void loadHealthTipDetails(String healthTipId);

    /**
     * Thay đổi trạng thái yêu thích của bài viết
     * @param healthTipId ID của bài viết
     * @param isFavorite trạng thái yêu thích mới
     */
    void toggleFavoriteStatus(String healthTipId, boolean isFavorite);

    /**
     * Tăng số lượt xem của bài viết
     * @param healthTipId ID của bài viết
     */
    void incrementViewCount(String healthTipId);

    /**
     * Thực hiện thao tác like/unlike bài viết
     * @param healthTipId ID của bài viết
     * @param isLiked true nếu người dùng like, false nếu unlike
     */
    void toggleLike(String healthTipId, boolean isLiked);
}
