package com.vhn.doan.data;

import java.io.Serializable;
import java.util.UUID;

/**
 * Model class đại diện cho một notification trong lịch sử
 * Bao gồm FCM notifications, local reminders và system notifications
 */
public class NotificationHistory implements Serializable {

    private String id;
    private String notificationId;
    private String userId;

    // Content
    private String title;
    private String body;
    private String imageUrl;
    private String largeIconUrl;

    // Type & Category
    private NotificationType type;
    private String category;
    private NotificationPriority priority;

    // Navigation
    private String deepLink;
    private String targetId;
    private String targetType;

    // Metadata
    private boolean isRead;
    private boolean isDeleted;
    private boolean isSynced;

    // Timestamps
    private long receivedAt;
    private Long readAt;
    private long createdAt;
    private long updatedAt;

    // Extra
    private String extraData;

    /**
     * Constructor mặc định
     */
    public NotificationHistory() {
        this.id = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
        this.receivedAt = now;
        this.isRead = false;
        this.isDeleted = false;
        this.isSynced = false;
        this.priority = NotificationPriority.DEFAULT;
        this.type = NotificationType.OTHER;
    }

    /**
     * Constructor với tham số
     */
    public NotificationHistory(String userId, String title, String body, NotificationType type) {
        this();
        this.userId = userId;
        this.title = title;
        this.body = body;
        this.type = type;
    }

    // ============ HELPER METHODS ============

    /**
     * Đánh dấu notification là đã đọc
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * Soft delete notification
     */
    public void softDelete() {
        this.isDeleted = true;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * Kiểm tra notification chưa đọc
     */
    public boolean isUnread() {
        return !isRead;
    }

    /**
     * Cập nhật timestamp
     */
    public void touch() {
        this.updatedAt = System.currentTimeMillis();
    }

    // ============ GETTERS ============

    public String getId() {
        return id;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLargeIconUrl() {
        return largeIconUrl;
    }

    public NotificationType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public String getDeepLink() {
        return deepLink;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getTargetType() {
        return targetType;
    }

    public boolean isRead() {
        return isRead;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public long getReceivedAt() {
        return receivedAt;
    }

    public Long getReadAt() {
        return readAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public String getExtraData() {
        return extraData;
    }

    // ============ SETTERS ============

    public void setId(String id) {
        this.id = id;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setLargeIconUrl(String largeIconUrl) {
        this.largeIconUrl = largeIconUrl;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    public void setReceivedAt(long receivedAt) {
        this.receivedAt = receivedAt;
    }

    public void setReadAt(Long readAt) {
        this.readAt = readAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }
}
