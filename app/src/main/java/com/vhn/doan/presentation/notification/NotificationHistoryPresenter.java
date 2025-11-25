package com.vhn.doan.presentation.notification;

import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.vhn.doan.data.NotificationHistory;
import com.vhn.doan.data.repository.NotificationHistoryRepository;
import com.vhn.doan.data.repository.RepositoryCallback;
import com.vhn.doan.presentation.base.BasePresenter;
import com.vhn.doan.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Presenter cho chức năng Notification History theo kiến trúc MVP
 */
public class NotificationHistoryPresenter extends BasePresenter<NotificationHistoryContract.View>
        implements NotificationHistoryContract.Presenter {

    private static final String TAG = "NotifHistoryPresenter";
    private static final int PAGE_SIZE = 20;

    private NotificationHistoryRepository repository;
    private SessionManager sessionManager;
    private LifecycleOwner lifecycleOwner;

    private List<NotificationHistory> allNotifications;
    private boolean isLoading = false;
    private boolean isLoadingMore = false;
    private int currentOffset = 0;
    private boolean hasMoreData = true;
    private boolean isObserving = false; // Flag để tránh observe nhiều lần
    private boolean isObservingUnreadCount = false; // Flag để tránh observe unread count nhiều lần

    public NotificationHistoryPresenter(NotificationHistoryRepository repository,
                                       SessionManager sessionManager,
                                       LifecycleOwner lifecycleOwner) {
        this.repository = repository;
        this.sessionManager = sessionManager;
        this.lifecycleOwner = lifecycleOwner;
        this.allNotifications = new ArrayList<>();
    }

    @Override
    public void attachView(NotificationHistoryContract.View view) {
        super.attachView(view);
    }

    @Override
    public void start() {
        loadNotifications();
    }

    @Override
    public void loadNotifications() {
        if (!isViewAttached()) {
            return;
        }

        String userId = sessionManager.getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            view.showError("Vui lòng đăng nhập");
            return;
        }

        // Chỉ observe một lần duy nhất
        if (!isObserving) {
            isObserving = true;
            isLoading = true;
            view.showLoading();

            // Observe LiveData from repository - CHỈ MỘT LẦN
            repository.getAllNotifications(userId).observe(lifecycleOwner, new Observer<List<NotificationHistory>>() {
                @Override
                public void onChanged(List<NotificationHistory> notifications) {
                    isLoading = false;
                    if (isViewAttached()) {
                        view.hideLoading();
                        allNotifications.clear();
                        allNotifications.addAll(notifications);

                        if (notifications.isEmpty()) {
                            view.showEmptyState();
                        } else {
                            view.hideEmptyState();
                            view.showNotifications(notifications);
                        }
                    }
                }
            });

            // Load unread count
            loadUnreadCount();
        }
    }

    @Override
    public void refreshNotifications() {
        // LiveData tự động refresh khi database thay đổi
        // Không cần làm gì, chỉ reset offset cho pagination
        currentOffset = 0;
        hasMoreData = true;
    }

    @Override
    public void loadMoreNotifications() {
        if (isLoadingMore || !hasMoreData) {
            return;
        }

        if (!isViewAttached()) {
            return;
        }

        String userId = sessionManager.getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            return;
        }

        isLoadingMore = true;
        view.showLoadingMore();

        currentOffset += PAGE_SIZE;

        repository.getPagedNotifications(userId, PAGE_SIZE, currentOffset,
            new RepositoryCallback<List<NotificationHistory>>() {
                @Override
                public void onSuccess(List<NotificationHistory> notifications) {
                    isLoadingMore = false;
                    if (isViewAttached()) {
                        view.hideLoadingMore();

                        if (notifications.isEmpty()) {
                            hasMoreData = false;
                        } else {
                            view.appendNotifications(notifications);
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    isLoadingMore = false;
                    if (isViewAttached()) {
                        view.hideLoadingMore();
                        view.showError("Lỗi khi tải thêm: " + error);
                    }
                }
            });
    }

    @Override
    public void onNotificationClicked(NotificationHistory notification) {
        if (!isViewAttached()) {
            return;
        }

        // Mark as read if unread
        if (notification.isUnread()) {
            markAsRead(notification);
        }

        // Navigate to content
        view.navigateToContent(notification);
    }

    @Override
    public void markAsRead(NotificationHistory notification) {
        if (notification.isRead()) {
            return;
        }

        repository.markAsRead(notification.getId(), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                notification.markAsRead();
                if (isViewAttached()) {
                    view.updateNotificationItem(notification);
                }
                loadUnreadCount();
                Log.d(TAG, "Marked as read: " + notification.getId());
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error marking as read: " + error);
            }
        });
    }

    @Override
    public void markAllAsRead() {
        if (!isViewAttached()) {
            return;
        }

        String userId = sessionManager.getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            return;
        }

        view.showLoading();

        repository.markAllAsRead(userId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isViewAttached()) {
                    view.hideLoading();
                    view.showSuccess("Đã đánh dấu tất cả là đã đọc");
                    refreshNotifications();
                }
            }

            @Override
            public void onError(String error) {
                if (isViewAttached()) {
                    view.hideLoading();
                    view.showError("Lỗi: " + error);
                }
            }
        });
    }

    @Override
    public void deleteNotification(NotificationHistory notification) {
        repository.deleteNotification(notification.getId(), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isViewAttached()) {
                    view.removeNotificationItem(notification);
                    view.showUndoSnackbar("Đã xóa thông báo", notification);
                }
                loadUnreadCount();
                Log.d(TAG, "Deleted notification: " + notification.getId());
            }

            @Override
            public void onError(String error) {
                if (isViewAttached()) {
                    view.showError("Lỗi khi xóa: " + error);
                }
            }
        });
    }

    @Override
    public void deleteAllNotifications() {
        if (!isViewAttached()) {
            return;
        }

        view.showDeleteAllConfirmDialog();
    }

    public void confirmDeleteAll() {
        String userId = sessionManager.getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            return;
        }

        view.showLoading();

        repository.deleteAllNotifications(userId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isViewAttached()) {
                    view.hideLoading();
                    view.showSuccess("Đã xóa tất cả thông báo");
                    refreshNotifications();
                }
            }

            @Override
            public void onError(String error) {
                if (isViewAttached()) {
                    view.hideLoading();
                    view.showError("Lỗi: " + error);
                }
            }
        });
    }

    @Override
    public void deleteReadNotifications() {
        if (!isViewAttached()) {
            return;
        }

        view.showDeleteReadConfirmDialog();
    }

    public void confirmDeleteRead() {
        String userId = sessionManager.getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            return;
        }

        view.showLoading();

        repository.deleteAllReadNotifications(userId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isViewAttached()) {
                    view.hideLoading();
                    view.showSuccess("Đã xóa các thông báo đã đọc");
                    refreshNotifications();
                }
            }

            @Override
            public void onError(String error) {
                if (isViewAttached()) {
                    view.hideLoading();
                    view.showError("Lỗi: " + error);
                }
            }
        });
    }

    @Override
    public void undoDeleteNotification(NotificationHistory notification) {
        repository.saveNotification(notification, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isViewAttached()) {
                    view.showSuccess("Đã hoàn tác");
                    refreshNotifications();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error undoing delete: " + error);
            }
        });
    }

    @Override
    public void loadUnreadCount() {
        String userId = sessionManager.getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            return;
        }

        // Chỉ observe unread count một lần duy nhất
        if (!isObservingUnreadCount) {
            isObservingUnreadCount = true;
            repository.getUnreadCount(userId).observe(lifecycleOwner, new Observer<Integer>() {
                @Override
                public void onChanged(Integer count) {
                    if (isViewAttached() && count != null) {
                        view.updateUnreadCount(count);
                    }
                }
            });
        }
    }
}
