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
     * Interface callback cho việc lấy một mẹo sức khỏe đơn lẻ
     */
    interface SingleHealthTipCallback {
        /**
         * Được gọi khi thao tác thành công
         * @param healthTip mẹo sức khỏe
         */
        void onSuccess(HealthTip healthTip);

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
    void getHealthTipById(String healthTipId, SingleHealthTipCallback callback);

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
     * Cập nhật trạng thái yêu thích của một mẹo sức khỏe
     * @param healthTipId ID của mẹo sức khỏe
     * @param isFavorite trạng thái yêu thích mới
     * @param callback callback để nhận kết quả
     */
    void updateFavoriteStatus(String healthTipId, boolean isFavorite, HealthTipOperationCallback callback);

    /**
     * Tăng số lượt xem của một mẹo sức khỏe
     * @param healthTipId ID của mẹo sức khỏe
     * @param callback callback để nhận kết quả
     */
    void incrementViewCount(String healthTipId, HealthTipOperationCallback callback);

    /**
     * Cập nhật trạng thái thích của một mẹo sức khỏe
     * @param healthTipId ID của mẹo sức khỏe
     * @param isLiked trạng thái thích mới
     * @param callback callback để nhận kết quả
     */
    void updateLikeStatus(String healthTipId, boolean isLiked, HealthTipOperationCallback callback);

    /**
     * Đăng ký lắng nghe các thay đổi từ Firebase cho dữ liệu mẹo sức khỏe mới nhất
     * @param limit Số lượng mẹo muốn lấy
     * @param callback Callback để nhận kết quả
     * @return Object định danh của listener để có thể hủy đăng ký sau này
     */
    Object listenToLatestHealthTips(int limit, HealthTipCallback callback);

    /**
     * Hủy đăng ký lắng nghe thay đổi từ Firebase
     * @param listener Đối tượng listener cần hủy, nhận được từ phương thức đăng ký tương ứng
     */
    void removeListener(Object listener);
}
