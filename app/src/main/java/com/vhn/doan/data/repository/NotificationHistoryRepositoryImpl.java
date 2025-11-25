package com.vhn.doan.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.vhn.doan.data.NotificationHistory;
import com.vhn.doan.data.NotificationPriority;
import com.vhn.doan.data.NotificationType;
import com.vhn.doan.data.local.AppDatabase;
import com.vhn.doan.data.local.dao.NotificationHistoryDao;
import com.vhn.doan.data.local.entity.NotificationHistoryEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation của NotificationHistoryRepository sử dụng Room Database
 */
public class NotificationHistoryRepositoryImpl implements NotificationHistoryRepository {

    private static final String TAG = "NotificationHistoryRepo";
    private static NotificationHistoryRepositoryImpl instance;

    private final NotificationHistoryDao dao;
    private final AppDatabase database;

    /**
     * Constructor
     */
    private NotificationHistoryRepositoryImpl(Context context) {
        this.database = AppDatabase.getInstance(context.getApplicationContext());
        this.dao = database.notificationHistoryDao();
    }

    /**
     * Singleton instance
     */
    public static synchronized NotificationHistoryRepositoryImpl getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationHistoryRepositoryImpl(context);
        }
        return instance;
    }

    @Override
    public void saveNotification(NotificationHistory notification, RepositoryCallback<Void> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                NotificationHistoryEntity entity = toEntity(notification);
                dao.insert(entity);
                callback.onSuccess(null);
                Log.d(TAG, "Notification saved: " + notification.getId());
            } catch (Exception e) {
                Log.e(TAG, "Error saving notification", e);
                callback.onError("Lỗi khi lưu thông báo: " + e.getMessage());
            }
        });
    }

    @Override
    public LiveData<List<NotificationHistory>> getAllNotifications(String userId) {
        LiveData<List<NotificationHistoryEntity>> entities = dao.getAllByUser(userId);
        return Transformations.map(entities, this::toModelList);
    }

    @Override
    public void getPagedNotifications(String userId, int limit, int offset, RepositoryCallback<List<NotificationHistory>> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<NotificationHistoryEntity> entities = dao.getPagedByUser(userId, limit, offset);
                List<NotificationHistory> notifications = toModelList(entities);
                callback.onSuccess(notifications);
            } catch (Exception e) {
                Log.e(TAG, "Error getting paged notifications", e);
                callback.onError("Lỗi khi lấy danh sách thông báo: " + e.getMessage());
            }
        });
    }

    @Override
    public LiveData<Integer> getUnreadCount(String userId) {
        return dao.getUnreadCountByUser(userId);
    }

    @Override
    public void markAsRead(String notificationId, RepositoryCallback<Void> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                dao.markAsRead(notificationId, now, now);
                callback.onSuccess(null);
                Log.d(TAG, "Notification marked as read: " + notificationId);
            } catch (Exception e) {
                Log.e(TAG, "Error marking notification as read", e);
                callback.onError("Lỗi khi đánh dấu đã đọc: " + e.getMessage());
            }
        });
    }

    @Override
    public void markAllAsRead(String userId, RepositoryCallback<Void> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                dao.markAllAsReadByUser(userId, now, now);
                callback.onSuccess(null);
                Log.d(TAG, "All notifications marked as read for user: " + userId);
            } catch (Exception e) {
                Log.e(TAG, "Error marking all notifications as read", e);
                callback.onError("Lỗi khi đánh dấu tất cả đã đọc: " + e.getMessage());
            }
        });
    }

    @Override
    public void deleteNotification(String notificationId, RepositoryCallback<Void> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                dao.deleteById(notificationId);
                callback.onSuccess(null);
                Log.d(TAG, "Notification deleted: " + notificationId);
            } catch (Exception e) {
                Log.e(TAG, "Error deleting notification", e);
                callback.onError("Lỗi khi xóa thông báo: " + e.getMessage());
            }
        });
    }

    @Override
    public void deleteAllNotifications(String userId, RepositoryCallback<Void> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                dao.deleteAllByUser(userId);
                callback.onSuccess(null);
                Log.d(TAG, "All notifications deleted for user: " + userId);
            } catch (Exception e) {
                Log.e(TAG, "Error deleting all notifications", e);
                callback.onError("Lỗi khi xóa tất cả thông báo: " + e.getMessage());
            }
        });
    }

    @Override
    public void deleteAllReadNotifications(String userId, RepositoryCallback<Void> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                dao.deleteAllReadByUser(userId);
                callback.onSuccess(null);
                Log.d(TAG, "All read notifications deleted for user: " + userId);
            } catch (Exception e) {
                Log.e(TAG, "Error deleting read notifications", e);
                callback.onError("Lỗi khi xóa thông báo đã đọc: " + e.getMessage());
            }
        });
    }

    @Override
    public void deleteOlderThan(int days, RepositoryCallback<Void> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                long timestamp = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L);
                dao.deleteOlderThan(timestamp);
                callback.onSuccess(null);
                Log.d(TAG, "Notifications older than " + days + " days deleted");
            } catch (Exception e) {
                Log.e(TAG, "Error deleting old notifications", e);
                callback.onError("Lỗi khi xóa thông báo cũ: " + e.getMessage());
            }
        });
    }

    @Override
    public void getCount(String userId, RepositoryCallback<Integer> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int count = dao.getCountByUser(userId);
                callback.onSuccess(count);
            } catch (Exception e) {
                Log.e(TAG, "Error getting count", e);
                callback.onError("Lỗi khi đếm thông báo: " + e.getMessage());
            }
        });
    }

    // ============ HELPER METHODS - CONVERSION ============

    /**
     * Chuyển đổi từ Model sang Entity
     */
    private NotificationHistoryEntity toEntity(NotificationHistory model) {
        NotificationHistoryEntity entity = new NotificationHistoryEntity();
        entity.setId(model.getId());
        entity.setNotificationId(model.getNotificationId());
        entity.setUserId(model.getUserId());
        entity.setTitle(model.getTitle());
        entity.setBody(model.getBody());
        entity.setImageUrl(model.getImageUrl());
        entity.setLargeIconUrl(model.getLargeIconUrl());
        entity.setType(model.getType() != null ? model.getType().getValue() : NotificationType.OTHER.getValue());
        entity.setCategory(model.getCategory());
        entity.setPriority(model.getPriority() != null ? model.getPriority().getValue() : NotificationPriority.DEFAULT.getValue());
        entity.setDeepLink(model.getDeepLink());
        entity.setTargetId(model.getTargetId());
        entity.setTargetType(model.getTargetType());
        entity.setRead(model.isRead());
        entity.setDeleted(model.isDeleted());
        entity.setSynced(model.isSynced());
        entity.setReceivedAt(model.getReceivedAt());
        entity.setReadAt(model.getReadAt());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());
        entity.setExtraData(model.getExtraData());
        return entity;
    }

    /**
     * Chuyển đổi từ Entity sang Model
     */
    private NotificationHistory toModel(NotificationHistoryEntity entity) {
        NotificationHistory model = new NotificationHistory();
        model.setId(entity.getId());
        model.setNotificationId(entity.getNotificationId());
        model.setUserId(entity.getUserId());
        model.setTitle(entity.getTitle());
        model.setBody(entity.getBody());
        model.setImageUrl(entity.getImageUrl());
        model.setLargeIconUrl(entity.getLargeIconUrl());
        model.setType(NotificationType.fromValue(entity.getType()));
        model.setCategory(entity.getCategory());
        model.setPriority(NotificationPriority.fromValue(entity.getPriority()));
        model.setDeepLink(entity.getDeepLink());
        model.setTargetId(entity.getTargetId());
        model.setTargetType(entity.getTargetType());
        model.setRead(entity.isRead());
        model.setDeleted(entity.isDeleted());
        model.setSynced(entity.isSynced());
        model.setReceivedAt(entity.getReceivedAt());
        model.setReadAt(entity.getReadAt());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setExtraData(entity.getExtraData());
        return model;
    }

    /**
     * Chuyển đổi danh sách Entity sang Model
     */
    private List<NotificationHistory> toModelList(List<NotificationHistoryEntity> entities) {
        List<NotificationHistory> models = new ArrayList<>();
        if (entities != null) {
            for (NotificationHistoryEntity entity : entities) {
                models.add(toModel(entity));
            }
        }
        return models;
    }
}
