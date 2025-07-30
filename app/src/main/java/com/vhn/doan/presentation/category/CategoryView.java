package com.vhn.doan.presentation.category;

import com.vhn.doan.data.Category;
import com.vhn.doan.presentation.base.BaseView;

import java.util.List;

/**
 * Interface định nghĩa các phương thức của view cho màn hình danh mục
 */
public interface CategoryView extends BaseView {

    /**
     * Hiển thị danh sách các danh mục
     * @param categories Danh sách các danh mục
     */
    void displayCategories(List<Category> categories);

    /**
     * Hiển thị thông báo khi không có danh mục nào
     */
    void showEmptyView();

    /**
     * Chuyển đến màn hình chi tiết của danh mục
     * @param category Danh mục được chọn
     */
    void navigateToCategoryDetail(Category category);
}
