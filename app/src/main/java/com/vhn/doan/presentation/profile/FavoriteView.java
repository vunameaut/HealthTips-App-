package com.vhn.doan.presentation.profile;

import com.vhn.doan.data.HealthTip;
import com.vhn.doan.presentation.base.BaseView;

import java.util.List;

/**
 * Interface định nghĩa các phương thức view cho màn hình danh sách bài viết yêu thích
 */
public interface FavoriteView extends BaseView {

    /**
     * Hiển thị danh sách bài viết yêu thích
     * @param favoriteHealthTips danh sách bài viết yêu thích
     */
    void showFavoriteHealthTips(List<HealthTip> favoriteHealthTips);

    /**
     * Hiển thị trạng thái khi không có bài viết yêu thích nào
     */
    void showEmptyFavorites();

    /**
     * Điều hướng đến màn hình chi tiết bài viết
     * @param healthTip bài viết được chọn
     */
    void navigateToHealthTipDetail(HealthTip healthTip);

    /**
     * Hiển thị thông báo khi xóa khỏi danh sách yêu thích thành công
     * @param message thông báo
     */
    void showRemoveFavoriteSuccess(String message);

    /**
     * Hiển thị thông báo lỗi khi xóa khỏi danh sách yêu thích thất bại
     * @param message thông báo lỗi
     */
    void showRemoveFavoriteError(String message);
}
