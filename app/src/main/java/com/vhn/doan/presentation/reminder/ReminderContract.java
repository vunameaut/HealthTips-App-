package com.vhn.doan.presentation.reminder;

import com.vhn.doan.data.Reminder;
import com.vhn.doan.presentation.base.BaseView;

import java.util.List;

/**
 * Contract interface định nghĩa các phương thức cho Reminder MVP
 */
public interface ReminderContract {

    interface View extends BaseView {
        /**
         * Hiển thị danh sách nhắc nhở
         */
        void showReminders(List<Reminder> reminders);

        /**
         * Hiển thị thông báo thành công
         */
        void showSuccess(String message);

        /**
         * Hiển thị dialog tạo/chỉnh sửa nhắc nhở
         */
        void showReminderDialog(Reminder reminder);

        /**
         * Hiển thị dialog xác nhận xóa
         */
        void showDeleteConfirmDialog(Reminder reminder);

        /**
         * Cập nhật một item trong danh sách
         */
        void updateReminderItem(Reminder reminder);

        /**
         * Xóa một item khỏi danh sách
         */
        void removeReminderItem(Reminder reminder);

        /**
         * Thêm một item mới vào danh sách
         */
        void addReminderItem(Reminder reminder);

        /**
         * Hiển thị trạng thái empty
         */
        void showEmptyState();

        /**
         * Ẩn trạng thái empty
         */
        void hideEmptyState();
    }

    interface Presenter {
        /**
         * Gắn view vào presenter
         */
        void attachView(View view);

        /**
         * Gỡ bỏ view khỏi presenter
         */
        void detachView();

        /**
         * Khởi tạo presenter
         */
        void start();

        /**
         * Tải danh sách nhắc nhở
         */
        void loadReminders();

        /**
         * Tạo nhắc nhở mới
         */
        void createReminder();

        /**
         * Chỉnh sửa nhắc nhở
         */
        void editReminder(Reminder reminder);

        /**
         * Xóa nhắc nhở
         */
        void deleteReminder(Reminder reminder);

        /**
         * Lưu nhắc nhở (tạo mới hoặc cập nhật)
         */
        void saveReminder(Reminder reminder);

        /**
         * Bật/tắt nhắc nhở
         */
        void toggleReminder(Reminder reminder);

        /**
         * Làm mới danh sách
         */
        void refreshReminders();

        /**
         * Tìm kiếm nhắc nhở
         */
        void searchReminders(String query);

        /**
         * Lọc nhắc nhở theo trạng thái
         */
        void filterReminders(boolean activeOnly);
    }
}
