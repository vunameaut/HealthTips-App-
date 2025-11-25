package com.vhn.doan.data.repository;

import androidx.lifecycle.LiveData;

import com.vhn.doan.data.NotificationHistory;

import java.util.List;

/**
 * Interface định nghĩa các phương thức để làm việc với Notification History
 * Repository cho local database (Room)
 */
public interface NotificationHistoryRepository {

    /**
     * Lưu một notification vào history
     * @param notification Notification cần lưu
     * @param callback Callback xử lý kết quả
     */
    void saveNotification(NotificationHistory notification, RepositoryCallback<Void> callback);

    /**
     * Lấy tất cả notification của user (LiveData - auto update UI)
     * @param userId ID của user
     * @return LiveData với danh sách notification
     */
    LiveData<List<NotificationHistory>> getAllNotifications(String userId);

    /**
     * Lấy notification với phân trang
     * @param userId ID của user
     * @param limit Số lượng notification
     * @param offset Vị trí bắt đầu
     * @param callback Callback xử lý kết quả
     */
    void getPagedNotifications(String userId, int limit, int offset, RepositoryCallback<List<NotificationHistory>> callback);

    /**
     * Lấy số lượng notification chưa đọc (LiveData)
     * @param userId ID của user
     * @return LiveData với số lượng chưa đọc
     */
    LiveData<Integer> getUnreadCount(String userId);

    /**
     * Đánh dấu notification là đã đọc
     * @param notificationId ID của notification
     * @param callback Callback xử lý kết quả
     */
    void markAsRead(String notificationId, RepositoryCallback<Void> callback);

    /**
     * Đánh dấu tất cả notification của user là đã đọc
     * @param userId ID của user
     * @param callback Callback xử lý kết quả
     */
    void markAllAsRead(String userId, RepositoryCallback<Void> callback);

    /**
     * Xóa một notification
     * @param notificationId ID của notification cần xóa
     * @param callback Callback xử lý kết quả
     */
    void deleteNotification(String notificationId, RepositoryCallback<Void> callback);

    /**
     * Xóa tất cả notification của user
     * @param userId ID của user
     * @param callback Callback xử lý kết quả
     */
    void deleteAllNotifications(String userId, RepositoryCallback<Void> callback);

    /**
     * Xóa tất cả notification đã đọc của user
     * @param userId ID của user
     * @param callback Callback xử lý kết quả
     */
    void deleteAllReadNotifications(String userId, RepositoryCallback<Void> callback);

    /**
     * Xóa notification cũ hơn số ngày chỉ định
     * @param days Số ngày
     * @param callback Callback xử lý kết quả
     */
    void deleteOlderThan(int days, RepositoryCallback<Void> callback);

    /**
     * Lấy tổng số notification của user
     * @param userId ID của user
     * @param callback Callback xử lý kết quả
     */
    void getCount(String userId, RepositoryCallback<Integer> callback);
}
