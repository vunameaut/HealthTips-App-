package com.vhn.doan.presentation.notification;

import com.vhn.doan.data.NotificationHistory;
import com.vhn.doan.presentation.base.BaseView;

import java.util.List;

/**
 * Contract interface định nghĩa các phương thức cho NotificationHistory MVP
 */
public interface NotificationHistoryContract {

    interface View extends BaseView {
        /**
         * Hiển thị danh sách thông báo
         */
        void showNotifications(List<NotificationHistory> notifications);

        /**
         * Hiển thị thông báo thành công
         */
        void showSuccess(String message);

        /**
         * Hiển thị empty state khi chưa có thông báo
         */
        void showEmptyState();

        /**
         * Ẩn empty state
         */
        void hideEmptyState();

        /**
         * Cập nhật số lượng chưa đọc
         */
        void updateUnreadCount(int count);

        /**
         * Cập nhật một notification item (đã đọc/chưa đọc)
         */
        void updateNotificationItem(NotificationHistory notification);

        /**
         * Xóa một notification item khỏi danh sách
         */
        void removeNotificationItem(NotificationHistory notification);

        /**
         * Hiển thị dialog xác nhận xóa
         */
        void showDeleteConfirmDialog(NotificationHistory notification);

        /**
         * Hiển thị dialog xác nhận xóa tất cả
         */
        void showDeleteAllConfirmDialog();

        /**
         * Hiển thị dialog xác nhận xóa đã đọc
         */
        void showDeleteReadConfirmDialog();

        /**
         * Điều hướng đến nội dung liên quan
         */
        void navigateToContent(NotificationHistory notification);

        /**
         * Dừng refresh animation
         */
        void stopRefreshing();

        /**
         * Hiển thị loading cho pagination
         */
        void showLoadingMore();

        /**
         * Ẩn loading cho pagination
         */
        void hideLoadingMore();

        /**
         * Thêm notifications vào cuối list (pagination)
         */
        void appendNotifications(List<NotificationHistory> notifications);

        /**
         * Hiển thị snackbar với action undo
         */
        void showUndoSnackbar(String message, NotificationHistory notification);
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
         * Load danh sách thông báo
         */
        void loadNotifications();

        /**
         * Refresh danh sách
         */
        void refreshNotifications();

        /**
         * Load more notifications (pagination)
         */
        void loadMoreNotifications();

        /**
         * Khi user click vào một notification
         */
        void onNotificationClicked(NotificationHistory notification);

        /**
         * Đánh dấu một notification đã đọc
         */
        void markAsRead(NotificationHistory notification);

        /**
         * Đánh dấu tất cả đã đọc
         */
        void markAllAsRead();

        /**
         * Xóa một notification
         */
        void deleteNotification(NotificationHistory notification);

        /**
         * Xóa tất cả notifications
         */
        void deleteAllNotifications();

        /**
         * Xóa các notifications đã đọc
         */
        void deleteReadNotifications();

        /**
         * Hoàn tác xóa notification
         */
        void undoDeleteNotification(NotificationHistory notification);

        /**
         * Load số lượng chưa đọc
         */
        void loadUnreadCount();
    }
}
