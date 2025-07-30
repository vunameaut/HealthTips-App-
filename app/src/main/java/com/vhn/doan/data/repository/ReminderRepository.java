package com.vhn.doan.data.repository;

import com.vhn.doan.data.Reminder;

import java.util.List;

/**
 * Interface định nghĩa các phương thức để làm việc với dữ liệu Reminder
 */
public interface ReminderRepository {

    /**
     * Thêm một nhắc nhở mới
     * @param reminder Nhắc nhở cần thêm
     * @param callback Callback xử lý kết quả
     */
    void addReminder(Reminder reminder, RepositoryCallback<String> callback);

    /**
     * Cập nhật thông tin nhắc nhở
     * @param reminder Nhắc nhở cần cập nhật
     * @param callback Callback xử lý kết quả
     */
    void updateReminder(Reminder reminder, RepositoryCallback<Void> callback);

    /**
     * Xóa nhắc nhở
     * @param reminderId ID của nhắc nhở cần xóa
     * @param callback Callback xử lý kết quả
     */
    void deleteReminder(String reminderId, RepositoryCallback<Void> callback);

    /**
     * Lấy tất cả nhắc nhở của một người dùng
     * @param userId ID của người dùng
     * @param callback Callback xử lý kết quả
     */
    void getUserReminders(String userId, RepositoryCallback<List<Reminder>> callback);

    /**
     * Lấy nhắc nhở theo ID
     * @param reminderId ID của nhắc nhở
     * @param callback Callback xử lý kết quả
     */
    void getReminderById(String reminderId, RepositoryCallback<Reminder> callback);

    /**
     * Lấy tất cả nhắc nhở đang hoạt động của một người dùng
     * @param userId ID của người dùng
     * @param callback Callback xử lý kết quả
     */
    void getActiveReminders(String userId, RepositoryCallback<List<Reminder>> callback);

    /**
     * Bật/tắt nhắc nhở
     * @param reminderId ID của nhắc nhở
     * @param isActive Trạng thái hoạt động
     * @param callback Callback xử lý kết quả
     */
    void toggleReminder(String reminderId, boolean isActive, RepositoryCallback<Void> callback);

    /**
     * Lấy các nhắc nhở theo mẹo sức khỏe
     * @param healthTipId ID của mẹo sức khỏe
     * @param callback Callback xử lý kết quả
     */
    void getRemindersByHealthTip(String healthTipId, RepositoryCallback<List<Reminder>> callback);

    /**
     * Interface callback cho các thao tác repository
     */
    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
