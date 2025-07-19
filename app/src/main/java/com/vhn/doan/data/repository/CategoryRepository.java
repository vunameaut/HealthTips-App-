package com.vhn.doan.data.repository;

import com.vhn.doan.data.Category;

import java.util.List;

/**
 * Interface CategoryRepository định nghĩa các phương thức để truy cập và quản lý dữ liệu danh mục
 * Tuân theo nguyên tắc thiết kế repository trong kiến trúc MVP
 */
public interface CategoryRepository {

    /**
     * Interface callback để nhận kết quả từ các thao tác async
     */
    interface CategoryCallback {
        /**
         * Được gọi khi thao tác thành công
         * @param categories danh sách các danh mục
         */
        void onSuccess(List<Category> categories);

        /**
         * Được gọi khi có một danh mục được trả về
         * @param category danh mục
         */
        void onSingleCategoryLoaded(Category category);

        /**
         * Được gọi khi thao tác thất bại
         * @param errorMessage thông báo lỗi
         */
        void onError(String errorMessage);
    }

    /**
     * Interface callback cho các thao tác không trả về dữ liệu
     */
    interface CategoryOperationCallback {
        /**
         * Được gọi khi thao tác thành công
         * @param categoryId ID của danh mục đã được xử lý
         */
        void onSuccess(String categoryId);

        /**
         * Được gọi khi thao tác thất bại
         * @param errorMessage thông báo lỗi
         */
        void onError(String errorMessage);
    }

    /**
     * Lấy tất cả danh mục
     * @param callback callback để nhận kết quả
     */
    void getAllCategories(CategoryCallback callback);

    /**
     * Lấy danh mục theo ID
     * @param categoryId ID của danh mục cần lấy
     * @param callback callback để nhận kết quả
     */
    void getCategoryById(String categoryId, CategoryCallback callback);

    /**
     * Lấy các danh mục theo trạng thái hoạt động
     * @param isActive trạng thái hoạt động cần lọc
     * @param callback callback để nhận kết quả
     */
    void getCategoriesByActiveStatus(boolean isActive, CategoryCallback callback);

    /**
     * Thêm một danh mục mới
     * @param category danh mục cần thêm
     * @param callback callback để nhận kết quả
     */
    void addCategory(Category category, CategoryOperationCallback callback);

    /**
     * Cập nhật thông tin danh mục
     * @param category danh mục cần cập nhật
     * @param callback callback để nhận kết quả
     */
    void updateCategory(Category category, CategoryOperationCallback callback);

    /**
     * Xóa một danh mục
     * @param categoryId ID của danh mục cần xóa
     * @param callback callback để nhận kết quả
     */
    void deleteCategory(String categoryId, CategoryOperationCallback callback);

    /**
     * Cập nhật trạng thái hoạt động của danh mục
     * @param categoryId ID của danh mục
     * @param isActive trạng thái hoạt động mới
     * @param callback callback để nhận kết quả
     */
    void updateCategoryActiveStatus(String categoryId, boolean isActive, CategoryOperationCallback callback);

    /**
     * Cập nhật thứ tự hiển thị của danh mục
     * @param categoryId ID của danh mục
     * @param order thứ tự hiển thị mới
     * @param callback callback để nhận kết quả
     */
    void updateCategoryOrder(String categoryId, int order, CategoryOperationCallback callback);

    /**
     * Lắng nghe sự thay đổi của danh sách danh mục
     * @param callback callback để nhận kết quả khi có thay đổi
     * @return một object để dừng lắng nghe sau này
     */
    Object listenToCategories(CategoryCallback callback);

    /**
     * Dừng lắng nghe sự thay đổi
     * @param listener listener đã đăng ký trước đó
     */
    void removeListener(Object listener);
}
