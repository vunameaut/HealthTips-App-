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
         */
        void onSuccess();

        /**
         * Được gọi khi thao tác thất bại
         * @param errorMessage thông báo lỗi
         */
        void onError(String errorMessage);
    }

    /**
     * Lấy danh sách tất cả mẹo sức khỏe
     * @param callback callback để nhận kết quả
     */
    void getAllHealthTips(HealthTipCallback callback);

    /**
     * Lấy danh sách mẹo sức khỏe theo danh mục
     * @param categoryId ID của danh mục
     * @param callback callback để nhận kết quả
     */
    void getHealthTipsByCategory(String categoryId, HealthTipCallback callback);

    /**
     * Lấy thông tin chi tiết một mẹo sức khỏe
     * @param tipId ID của mẹo sức khỏe
     * @param callback callback để nhận kết quả
     */
    void getHealthTipDetail(String tipId, SingleHealthTipCallback callback);

    /**
     * Lấy danh sách mẹo sức khỏe mới nhất
     * @param limit số lượng mẹo cần lấy
     * @param callback callback để nhận kết quả
     * @return listener object để có thể remove sau này
     */
    Object listenToLatestHealthTips(int limit, HealthTipCallback callback);

    /**
     * Cập nhật trạng thái yêu thích của một mẹo sức khỏe
     * @param tipId ID của mẹo sức khỏe
     * @param isFavorite trạng thái yêu thích
     * @param callback callback để nhận kết quả
     */
    void updateFavoriteStatus(String tipId, boolean isFavorite, HealthTipOperationCallback callback);

    /**
     * Cập nhật trạng thái thích của một mẹo sức khỏe
     * @param tipId ID của mẹo sức khỏe
     * @param isLiked trạng thái thích
     * @param callback callback để nhận kết quả
     */
    void updateLikeStatus(String tipId, boolean isLiked, HealthTipOperationCallback callback);

    /**
     * Cập nhật số lượt xem của một mẹo sức khỏe
     * @param tipId ID của mẹo sức khỏe
     * @param callback callback để nhận kết quả
     */
    void updateViewCount(String tipId, HealthTipOperationCallback callback);

    /**
     * Tìm kiếm mẹo sức khỏe theo từ khóa
     * @param query từ khóa tìm kiếm
     * @param callback callback để nhận kết quả
     */
    void searchHealthTips(String query, HealthTipCallback callback);

    /**
     * Lấy danh sách mẹo sức khỏe yêu thích của người dùng
     * @param userId ID của người dùng
     * @param callback callback để nhận kết quả
     */
    void getFavoriteHealthTips(String userId, HealthTipCallback callback);

    /**
     * Lấy danh sách mẹo sức khỏe mới nhất theo giới hạn
     * @param limit số lượng mẹo sức khỏe cần lấy
     * @param callback callback để nhận kết quả
     */
    void getLatestHealthTips(int limit, HealthTipCallback callback);

    /**
     * Lấy danh sách mẹo sức khỏe được xem nhiều nhất theo giới hạn
     * @param limit số lượng mẹo sức khỏe cần lấy
     * @param callback callback để nhận kết quả
     */
    void getMostViewedHealthTips(int limit, HealthTipCallback callback);

    /**
     * Lấy danh sách mẹo sức khỏe được thích nhiều nhất theo giới hạn
     * @param limit số lượng mẹo sức khỏe cần lấy
     * @param callback callback để nhận kết quả
     */
    void getMostLikedHealthTips(int limit, HealthTipCallback callback);

    /**
     * Lấy danh sách mẹo sức khỏe được đề xuất cho người dùng
     * Logic đề xuất dựa trên các bài viết phổ biến từ nhiều danh mục khác nhau
     * @param limit số lượng mẹo sức khỏe cần lấy
     * @param callback callback để nhận kết quả
     */
    void getRecommendedHealthTips(int limit, HealthTipCallback callback);

    /**
     * Lấy danh sách mẹo sức khỏe được đề xuất cho ngày cụ thể
     * Logic đề xuất dựa trên thuật toán seed theo ngày để đảm bảo tính nhất quán
     * @param date ngày để lấy đề xuất (format: yyyy-MM-dd)
     * @param limit số lượng mẹo sức khỏe cần lấy
     * @param callback callback để nhận kết quả
     */
    void getDailyRecommendedHealthTips(String date, int limit, HealthTipCallback callback);

    /**
     * Lấy danh sách mẹo sức khỏe được đề xuất cho ngày hiện tại
     * @param limit số lượng mẹo sức khỏe cần lấy
     * @param callback callback để nhận kết quả
     */
    void getTodayRecommendedHealthTips(int limit, HealthTipCallback callback);

    /**
     * Thêm một mẹo sức khỏe mới
     * @param healthTip đối tượng mẹo sức khỏe cần thêm
     * @param callback callback để nhận kết quả
     */
    void addHealthTip(HealthTip healthTip, HealthTipOperationCallback callback);

    /**
     * Remove listener để tránh memory leak
     * @param listener listener object cần remove
     */
    void removeListener(Object listener);
}
