package com.vhn.doan.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Room Entity cho Notification History - Cache local database
 * Lưu trữ lịch sử tất cả thông báo (FCM, Reminders, System)
 */
@Entity(
    tableName = "notification_history",
    indices = {
        @Index(value = {"user_id"}),
        @Index(value = {"received_at"}),
        @Index(value = {"is_read"}),
        @Index(value = {"type"}),
        @Index(value = {"user_id", "received_at"}),
        @Index(value = {"user_id", "is_read", "received_at"})
    }
)
public class NotificationHistoryEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "notification_id")
    private String notificationId;

    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;

    // Content
    @NonNull
    @ColumnInfo(name = "title")
    private String title;

    @NonNull
    @ColumnInfo(name = "body")
    private String body;

    @ColumnInfo(name = "image_url")
    private String imageUrl;

    @ColumnInfo(name = "large_icon_url")
    private String largeIconUrl;

    // Type & Category
    @NonNull
    @ColumnInfo(name = "type")
    private String type; // NotificationType enum value

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "priority", defaultValue = "0")
    private int priority;

    // Navigation
    @ColumnInfo(name = "deep_link")
    private String deepLink;

    @ColumnInfo(name = "target_id")
    private String targetId;

    @ColumnInfo(name = "target_type")
    private String targetType;

    // Metadata
    @ColumnInfo(name = "is_read", defaultValue = "0")
    private boolean isRead;

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    private boolean isDeleted;

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    private boolean isSynced;

    // Timestamps
    @ColumnInfo(name = "received_at")
    private long receivedAt;

    @ColumnInfo(name = "read_at")
    private Long readAt;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    // Extra data
    @ColumnInfo(name = "extra_data")
    private String extraData; // JSON string for flexible data

    // Constructor
    public NotificationHistoryEntity() {
    }

    // Getters
    @NonNull
    public String getId() {
        return id;
    }

    public String getNotificationId() {
        return notificationId;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getBody() {
        return body;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLargeIconUrl() {
        return largeIconUrl;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public int getPriority() {
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

    // Setters
    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public void setBody(@NonNull String body) {
        this.body = body;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setLargeIconUrl(String largeIconUrl) {
        this.largeIconUrl = largeIconUrl;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPriority(int priority) {
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
