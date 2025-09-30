package com.vhn.doan.presentation.profile;

import com.vhn.doan.data.HealthTip;
import com.vhn.doan.presentation.base.BaseView;

import java.util.List;

/**
 * Interface FavoriteView định nghĩa các phương thức cần thiết
 * cho view hiển thị danh sách bài viết yêu thích
 * Tuân theo kiến trúc MVP (Model-View-Presenter)
 */
public interface FavoriteView extends BaseView {

    /**
     * Hiển thị danh sách bài viết yêu thích
     * @param favoriteHealthTips danh sách bài viết yêu thích
     */
    void displayFavoriteHealthTips(List<HealthTip> favoriteHealthTips);

    /**
     * Hiển thị trạng thái rỗng khi không có bài viết yêu thích
     */
    void showEmptyState();

    /**
     * Điều hướng đến màn hình chi tiết bài viết
     * @param healthTipId ID của bài viết
     */
    void navigateToHealthTipDetail(String healthTipId);

    /**
     * Cập nhật số lượng bài viết yêu thích
     * @param count số lượng bài viết yêu thích
     */
    void updateFavoriteCount(int count);

    /**
     * Hiển thị thông báo cho người dùng
     * @param message nội dung thông báo
     */
    void showMessage(String message);
}
