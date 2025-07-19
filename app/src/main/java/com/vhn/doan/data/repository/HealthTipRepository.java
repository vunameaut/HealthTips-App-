package com.vhn.doan.data.repository;

import com.vhn.doan.data.HealthTip;

import java.util.List;

/**
 * Interface HealthTipRepository định nghĩa các phương thức để truy cập và quản lý dữ liệu mẹo sức khỏe
 * Tuân theo nguyên tắc thiết kế repository trong kiến trúc MVP
 */
public interface HealthTipRepository {

    /**
     * Interface callback để nhận kết quả từ các thao tác async
     */
    interface HealthTipCallback {
        /**
         * Được gọi khi thao tác thành công
         * @param healthTips danh sách các mẹo sức khỏe
         */
        void onSuccess(List<HealthTip> healthTips);

        /**
         * Được gọi khi thao tác thất bại
         * @param errorMessage thông báo lỗi
         */
        void onError(String errorMessage);
    }

    /**
     * Interface callback cho các thao tác không trả về dữ liệu
     */
    interface HealthTipOperationCallback {
        /**
         * Được gọi khi thao tác thành công
         * @param healthTipId ID của mẹo sức khỏe đã được xử lý
         */
        void onSuccess(String healthTipId);

        /**
         * Được gọi khi thao tác thất bại
         * @param errorMessage thông báo lỗi
         */
        void onError(String errorMessage);
    }

    /**
     * Lấy tất cả mẹo sức khỏe
     * @param callback callback để nhận kết quả
     */
    void getAllHealthTips(HealthTipCallback callback);

    /**
     * Lấy các mẹo sức khỏe theo danh mục
     * @param categoryId ID của danh mục
     * @param callback callback để nhận kết quả
     */
    void getHealthTipsByCategory(String categoryId, HealthTipCallback callback);

    /**
     * Lấy mẹo sức khỏe theo ID
     * @param healthTipId ID của mẹo sức khỏe cần lấy
     * @param callback callback để nhận kết quả
     */
    void getHealthTipById(String healthTipId, HealthTipCallback callback);

    /**
     * Lấy các mẹo sức khỏe mới nhất
     * @param limit số lượng mẹo cần lấy
     * @param callback callback để nhận kết quả
     */
    void getLatestHealthTips(int limit, HealthTipCallback callback);

    /**
     * Lấy các mẹo sức khỏe có lượt xem nhiều nhất
     * @param limit số lượng mẹo cần lấy
     * @param callback callback để nhận kết quả
     */
    void getMostViewedHealthTips(int limit, HealthTipCallback callback);

    /**
     * Lấy các mẹo sức khỏe có lượt thích nhiều nhất
     * @param limit số lượng mẹo cần lấy
     * @param callback callback để nhận kết quả
     */
    void getMostLikedHealthTips(int limit, HealthTipCallback callback);

    /**
     * Tìm kiếm mẹo sức khỏe theo từ khóa
     * @param query từ khóa tìm kiếm
     * @param callback callback để nhận kết quả
     */
    void searchHealthTips(String query, HealthTipCallback callback);

    /**
     * Thêm một mẹo sức khỏe mới
     * @param healthTip mẹo sức khỏe cần thêm
     * @param callback callback để nhận kết quả
     */
    void addHealthTip(HealthTip healthTip, HealthTipOperationCallback callback);

    /**
     * Cập nhật thông tin mẹo sức khỏe
     * @param healthTip mẹo sức khỏe cần cập nhật
     * @param callback callback để nhận kết quả
     */
    void updateHealthTip(HealthTip healthTip, HealthTipOperationCallback callback);

    /**
     * Xóa một mẹo sức khỏe
     * @param healthTipId ID của mẹo sức khỏe cần xóa
     * @param callback callback để nhận kết quả
     */
    void deleteHealthTip(String healthTipId, HealthTipOperationCallback callback);

    /**
     * Tăng số lượt xem của một mẹo sức khỏe
     * @param healthTipId ID của mẹo sức khỏe
     * @param callback callback để nhận kết quả
     */
    void incrementViewCount(String healthTipId, HealthTipOperationCallback callback);

    /**
     * Thay đổi trạng thái thích của một mẹo sức khỏe
     * @param healthTipId ID của mẹo sức khỏe
     * @param like true để tăng lượt thích, false để giảm
     * @param callback callback để nhận kết quả
     */
    void toggleLike(String healthTipId, boolean like, HealthTipOperationCallback callback);

    /**
     * Lắng nghe sự thay đổi của các mẹo sức khỏe mới nhất
     * @param limit số lượng mẹo cần lấy
     * @param callback callback để nhận kết quả khi có thay đổi
     * @return một object để dừng lắng nghe sau này
     */
    Object listenToLatestHealthTips(int limit, HealthTipCallback callback);

    /**
     * Dừng lắng nghe sự thay đổi
     * @param listener listener đã đăng ký trước đó
     */
    void removeListener(Object listener);
}
