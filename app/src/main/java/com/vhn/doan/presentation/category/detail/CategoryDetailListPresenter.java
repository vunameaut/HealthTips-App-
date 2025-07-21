package com.vhn.doan.presentation.category.detail;

/**
 * Interface định nghĩa các phương thức của presenter cho màn hình danh sách chi tiết danh mục
 */
public interface CategoryDetailListPresenter {

    /**
     * Load các mẹo sức khỏe theo categoryId
     * @param categoryId ID của danh mục cần hiển thị
     */
    void loadHealthTipsByCategory(String categoryId);

    /**
     * Load thông tin chi tiết của danh mục
     * @param categoryId ID của danh mục
     */
    void loadCategoryDetails(String categoryId);

    /**
     * Xử lý khi người dùng chọn một mẹo sức khỏe
     * @param healthTipId ID của mẹo được chọn
     */
    void onHealthTipSelected(String healthTipId);

    /**
     * Hủy các subscription, listener khi không cần thiết nữa
     */
    void detachView();

    /**
     * Gắn view vào presenter
     * @param view View cần gắn
     */
    void attachView(CategoryDetailListView view);
}
