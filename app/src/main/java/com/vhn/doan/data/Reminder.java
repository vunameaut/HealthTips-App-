package com.vhn.doan.data;

import java.util.Calendar;
import java.util.Date;

/**
 * Model class đại diện cho một nhắc nhở sức khỏe
 */
public class Reminder {
    private String id;
    private String userId;
    private String title;
    private String description;
    private Date reminderTime;
    private int repeatType; // 0: Không lặp, 1: Hàng ngày, 2: Hàng tuần, 3: Hàng tháng
    private boolean isActive;
    private Date createdAt;
    private Date updatedAt;
    private String healthTipId; // ID của mẹo sức khỏe liên quan (nếu có)

    // Enum cho loại lặp lại
    public static class RepeatType {
        public static final int NO_REPEAT = 0;
        public static final int DAILY = 1;
        public static final int WEEKLY = 2;
        public static final int MONTHLY = 3;
    }

    // Constructor mặc định (cần thiết cho Firebase)
    public Reminder() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isActive = true;
        this.repeatType = RepeatType.NO_REPEAT;
    }

    // Constructor đầy đủ
    public Reminder(String id, String userId, String title, String description,
                   Date reminderTime, int repeatType, boolean isActive, String healthTipId) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.reminderTime = reminderTime;
        this.repeatType = repeatType;
        this.isActive = isActive;
        this.healthTipId = healthTipId;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Date getReminderTime() {
        return reminderTime;
    }

    public int getRepeatType() {
        return repeatType;
    }

    public boolean isActive() {
        return isActive;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getHealthTipId() {
        return healthTipId;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = new Date();
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = new Date();
    }

    public void setReminderTime(Date reminderTime) {
        this.reminderTime = reminderTime;
        this.updatedAt = new Date();
    }

    public void setRepeatType(int repeatType) {
        this.repeatType = repeatType;
        this.updatedAt = new Date();
    }

    public void setActive(boolean active) {
        isActive = active;
        this.updatedAt = new Date();
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setHealthTipId(String healthTipId) {
        this.healthTipId = healthTipId;
        this.updatedAt = new Date();
    }

    /**
     * Tính toán thời gian nhắc nhở tiếp theo dựa trên loại lặp lại
     */
    public Date getNextReminderTime() {
        if (reminderTime == null || repeatType == RepeatType.NO_REPEAT) {
            return reminderTime;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(reminderTime);

        switch (repeatType) {
            case RepeatType.DAILY:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case RepeatType.WEEKLY:
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case RepeatType.MONTHLY:
                calendar.add(Calendar.MONTH, 1);
                break;
        }

        return calendar.getTime();
    }

    /**
     * Lấy tên hiển thị cho loại lặp lại
     */
    public String getRepeatTypeDisplayName() {
        switch (repeatType) {
            case RepeatType.DAILY:
                return "Hàng ngày";
            case RepeatType.WEEKLY:
                return "Hàng tuần";
            case RepeatType.MONTHLY:
                return "Hàng tháng";
            case RepeatType.NO_REPEAT:
            default:
                return "Không lặp";
        }
    }

    /**
     * Kiểm tra xem nhắc nhở có đã đến thời gian hay chưa
     */
    public boolean isDue() {
        if (reminderTime == null || !isActive) {
            return false;
        }

        Date now = new Date();
        return now.getTime() >= reminderTime.getTime();
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", reminderTime=" + reminderTime +
                ", repeatType=" + repeatType +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", healthTipId='" + healthTipId + '\'' +
                '}';
    }

    /**
     * Chuyển đổi object thành Map để lưu vào Firebase
     */
    public java.util.Map<String, Object> toMap() {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", id);
        result.put("userId", userId);
        result.put("title", title);
        result.put("description", description);
        result.put("reminderTime", reminderTime != null ? reminderTime.getTime() : null);
        result.put("repeatType", repeatType);
        result.put("active", isActive);
        result.put("createdAt", createdAt != null ? createdAt.getTime() : null);
        result.put("updatedAt", updatedAt != null ? updatedAt.getTime() : null);
        result.put("healthTipId", healthTipId);
        return result;
    }
}
