package com.vhn.doan.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.vhn.doan.data.local.entity.NotificationHistoryEntity;

import java.util.List;

/**
 * DAO cho Notification History - Data Access Object
 * Sử dụng LiveData để observe changes tự động
 */
@Dao
public interface NotificationHistoryDao {

    // ============ INSERT ============
    /**
     * Insert hoặc replace notification
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NotificationHistoryEntity notification);

    /**
     * Insert multiple notifications
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<NotificationHistoryEntity> notifications);

    // ============ UPDATE ============
    /**
     * Update notification
     */
    @Update
    void update(NotificationHistoryEntity notification);

    /**
     * Đánh dấu notification là đã đọc
     */
    @Query("UPDATE notification_history SET is_read = 1, read_at = :readAt, updated_at = :updatedAt WHERE id = :id")
    void markAsRead(String id, long readAt, long updatedAt);

    /**
     * Đánh dấu tất cả notification của user là đã đọc
     */
    @Query("UPDATE notification_history SET is_read = 1, read_at = :readAt, updated_at = :updatedAt WHERE user_id = :userId AND is_read = 0 AND is_deleted = 0")
    void markAllAsReadByUser(String userId, long readAt, long updatedAt);

    /**
     * Soft delete notification
     */
    @Query("UPDATE notification_history SET is_deleted = 1, updated_at = :timestamp WHERE id = :id")
    void softDelete(String id, long timestamp);

    // ============ DELETE ============
    /**
     * Delete notification
     */
    @Delete
    void delete(NotificationHistoryEntity notification);

    /**
     * Xóa notification theo ID
     */
    @Query("DELETE FROM notification_history WHERE id = :id")
    void deleteById(String id);

    /**
     * Xóa tất cả notification của user
     */
    @Query("DELETE FROM notification_history WHERE user_id = :userId")
    void deleteAllByUser(String userId);

    /**
     * Xóa tất cả notification đã đọc của user
     */
    @Query("DELETE FROM notification_history WHERE user_id = :userId AND is_read = 1")
    void deleteAllReadByUser(String userId);

    /**
     * Xóa notification cũ hơn timestamp
     */
    @Query("DELETE FROM notification_history WHERE received_at < :timestamp")
    void deleteOlderThan(long timestamp);

    // ============ QUERY - GET ALL ============
    /**
     * Lấy tất cả notification của user (LiveData - tự động update UI)
     */
    @Query("SELECT * FROM notification_history WHERE user_id = :userId AND is_deleted = 0 ORDER BY received_at DESC")
    LiveData<List<NotificationHistoryEntity>> getAllByUser(String userId);

    /**
     * Lấy tất cả notification của user (synchronous)
     */
    @Query("SELECT * FROM notification_history WHERE user_id = :userId AND is_deleted = 0 ORDER BY received_at DESC")
    List<NotificationHistoryEntity> getAllByUserSync(String userId);

    /**
     * Lấy notification với phân trang
     */
    @Query("SELECT * FROM notification_history WHERE user_id = :userId AND is_deleted = 0 ORDER BY received_at DESC LIMIT :limit OFFSET :offset")
    List<NotificationHistoryEntity> getPagedByUser(String userId, int limit, int offset);

    // ============ QUERY - FILTER BY READ STATUS ============
    /**
     * Lấy notification theo trạng thái đã đọc/chưa đọc (LiveData)
     */
    @Query("SELECT * FROM notification_history WHERE user_id = :userId AND is_read = :isRead AND is_deleted = 0 ORDER BY received_at DESC")
    LiveData<List<NotificationHistoryEntity>> getByReadStatus(String userId, boolean isRead);

    /**
     * Lấy notification theo trạng thái đã đọc/chưa đọc (synchronous)
     */
    @Query("SELECT * FROM notification_history WHERE user_id = :userId AND is_read = :isRead AND is_deleted = 0 ORDER BY received_at DESC")
    List<NotificationHistoryEntity> getByReadStatusSync(String userId, boolean isRead);

    /**
     * Lấy notification chưa đọc với phân trang
     */
    @Query("SELECT * FROM notification_history WHERE user_id = :userId AND is_read = :isRead AND is_deleted = 0 ORDER BY received_at DESC LIMIT :limit OFFSET :offset")
    List<NotificationHistoryEntity> getPagedByReadStatus(String userId, boolean isRead, int limit, int offset);

    // ============ QUERY - FILTER BY TYPE ============
    /**
     * Lấy notification theo loại (LiveData)
     */
    @Query("SELECT * FROM notification_history WHERE user_id = :userId AND type = :type AND is_deleted = 0 ORDER BY received_at DESC")
    LiveData<List<NotificationHistoryEntity>> getByType(String userId, String type);

    /**
     * Lấy notification theo loại (synchronous)
     */
    @Query("SELECT * FROM notification_history WHERE user_id = :userId AND type = :type AND is_deleted = 0 ORDER BY received_at DESC")
    List<NotificationHistoryEntity> getByTypeSync(String userId, String type);

    /**
     * Lấy notification theo loại với phân trang
     */
    @Query("SELECT * FROM notification_history WHERE user_id = :userId AND type = :type AND is_deleted = 0 ORDER BY received_at DESC LIMIT :limit OFFSET :offset")
    List<NotificationHistoryEntity> getPagedByType(String userId, String type, int limit, int offset);

    // ============ QUERY - GET BY ID ============
    /**
     * Lấy notification theo ID
     */
    @Query("SELECT * FROM notification_history WHERE id = :id")
    NotificationHistoryEntity getById(String id);

    // ============ QUERY - COUNT ============
    /**
     * Đếm tổng số notification của user
     */
    @Query("SELECT COUNT(*) FROM notification_history WHERE user_id = :userId AND is_deleted = 0")
    int getCountByUser(String userId);

    /**
     * Đếm số notification chưa đọc (LiveData)
     */
    @Query("SELECT COUNT(*) FROM notification_history WHERE user_id = :userId AND is_read = 0 AND is_deleted = 0")
    LiveData<Integer> getUnreadCountByUser(String userId);

    /**
     * Đếm số notification chưa đọc (synchronous)
     */
    @Query("SELECT COUNT(*) FROM notification_history WHERE user_id = :userId AND is_read = 0 AND is_deleted = 0")
    int getUnreadCountByUserSync(String userId);

    /**
     * Đếm số notification theo loại
     */
    @Query("SELECT COUNT(*) FROM notification_history WHERE user_id = :userId AND type = :type AND is_deleted = 0")
    int getCountByType(String userId, String type);

    // ============ MAINTENANCE ============
    /**
     * Giữ lại tối đa maxCount notification mới nhất, xóa các notification cũ hơn
     */
    @Query("DELETE FROM notification_history WHERE user_id = :userId AND is_deleted = 0 " +
           "AND id NOT IN (SELECT id FROM notification_history WHERE user_id = :userId AND is_deleted = 0 ORDER BY received_at DESC LIMIT :maxCount)")
    void trimToMaxCount(String userId, int maxCount);

    /**
     * Xóa tất cả data (dùng cho testing hoặc cleanup)
     */
    @Query("DELETE FROM notification_history")
    void deleteAll();
}
