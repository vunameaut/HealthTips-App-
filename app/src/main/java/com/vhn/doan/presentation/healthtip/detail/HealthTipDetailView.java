package com.vhn.doan.presentation.healthtip.detail;

import com.vhn.doan.data.HealthTip;
import com.vhn.doan.presentation.base.BaseView;

/**
 * Interface HealthTipDetailView định nghĩa các phương thức cần thiết
 * cho view hiển thị chi tiết bài viết sức khỏe
 * Tuân theo kiến trúc MVP (Model-View-Presenter)
 */
public interface HealthTipDetailView extends BaseView {

    /**
     * Hiển thị thông tin chi tiết của một bài viết sức khỏe
     * @param healthTip đối tượng HealthTip chứa thông tin chi tiết
     */
    void displayHealthTipDetails(HealthTip healthTip);

    /**
     * Cập nhật trạng thái yêu thích của bài viết
     * @param isFavorite true nếu bài viết được yêu thích, false nếu ngược lại
     */
    void updateFavoriteStatus(boolean isFavorite);

    /**
     * Cập nhật trạng thái thích của bài viết
     * @param isLiked true nếu bài viết được thích, false nếu ngược lại
     */
    void updateLikeStatus(boolean isLiked);

    /**
     * Cập nhật số lượt thích của bài viết
     * @param likeCount số lượt thích mới
     */
    void updateLikeCount(int likeCount);

    /**
     * Cập nhật số lượt xem của bài viết
     * @param viewCount số lượt xem mới
     */
    void updateViewCount(int viewCount);

    /**
     * Chia sẻ nội dung bài viết
     * @param content nội dung cần chia sẻ
     */
    void shareContent(String content);
}
