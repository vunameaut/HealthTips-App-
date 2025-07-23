package com.vhn.doan.presentation.favorite;

import com.vhn.doan.data.HealthTip;
import com.vhn.doan.presentation.base.BaseView;

import java.util.List;

/**
 * Interface định nghĩa các phương thức UI cho màn hình yêu thích
 * Tuân thủ kiến trúc MVP
 */
public interface FavoriteView extends BaseView {

    /**
     * Hiển thị danh sách mẹo sức khỏe yêu thích
     * @param favoriteHealthTips Danh sách mẹo sức khỏe yêu thích
     */
    void showFavoriteHealthTips(List<HealthTip> favoriteHealthTips);

    /**
     * Hiển thị trạng thái trống khi không có mẹo yêu thích
     */
    void showEmptyFavorites();

    /**
     * Điều hướng đến chi tiết mẹo sức khỏe
     * @param healthTip Mẹo sức khỏe được chọn
     */
    void navigateToHealthTipDetail(HealthTip healthTip);

    /**
     * Hiển thị thông báo khi xóa khỏi yêu thích thành công
     * @param healthTipTitle Tiêu đề mẹo sức khỏe đã xóa
     */
    void showRemovedFromFavorites(String healthTipTitle);

    /**
     * Cập nhật danh sách sau khi thay đổi trạng thái yêu thích
     */
    void refreshFavoritesList();
}
