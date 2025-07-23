package com.vhn.doan.presentation.category.detail;

import com.vhn.doan.data.Category;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.presentation.base.BaseView;

import java.util.List;

/**
 * View interface cho màn hình danh sách mẹo sức khỏe theo danh mục
 * Tuân theo kiến trúc MVP
 */
public interface CategoryDetailListView extends BaseView {

    /**
     * Hiển thị danh sách các mẹo sức khỏe
     * @param healthTips Danh sách mẹo sức khỏe cần hiển thị
     */
    void displayHealthTips(List<HealthTip> healthTips);

    /**
     * Hiển thị thông tin chi tiết danh mục
     * @param category Thông tin danh mục
     */
    void displayCategoryDetails(Category category);

    /**
     * Hiển thị trạng thái loading
     * @param loading true để hiển thị, false để ẩn
     */
    void showLoading(boolean loading);

    /**
     * Hiển thị giao diện khi không có dữ liệu
     */
    void showEmptyView();

    /**
     * Hiển thị trạng thái rỗng khi không có dữ liệu
     */
    void showEmptyState();

    /**
     * Hiển thị thông báo lỗi
     * @param message Nội dung thông báo lỗi
     */
    void showError(String message);

    /**
     * Cập nhật tiêu đề danh mục
     * @param title Tiêu đề cần hiển thị
     */
    void setCategoryTitle(String title);

    /**
     * Chuyển đến màn hình chi tiết mẹo sức khỏe
     * @param healthTipId ID của mẹo sức khỏe được chọn
     */
    void navigateToHealthTipDetails(String healthTipId);
}
