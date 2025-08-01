package com.vhn.doan.data;

import com.google.firebase.database.PropertyName;
import com.google.firebase.database.ServerValue;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Model class đại diện cho một nhắc nhở sức khỏe
 * Được tối ưu hóa để tương thích với Firebase Realtime Database
 */
public class Reminder {
    private String id;
    private String userId;
    private String title;
    private String description;
    private Long reminderTime; // Sử dụng Long thay vì Date để tương thích Firebase
    private int repeatType; // 0: Không lặp, 1: Hàng ngày, 2: Hàng tuần, 3: Hàng tháng
    private boolean isActive;
    private Long createdAt; // Sử dụng Long thay vì Date
    private Long updatedAt; // Sử dụng Long thay vì Date
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
        long currentTime = System.currentTimeMillis();
        this.createdAt = currentTime;
        this.updatedAt = currentTime;
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
        this.reminderTime = reminderTime != null ? reminderTime.getTime() : null;
        this.repeatType = repeatType;
        this.isActive = isActive;
        this.healthTipId = healthTipId;
        long currentTime = System.currentTimeMillis();
        this.createdAt = currentTime;
        this.updatedAt = currentTime;
    }

    // Getters với Firebase annotations
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

    // Getter cho Firebase - trả về Long
    @PropertyName("reminderTime")
    public Long getReminderTimestamp() {
        return reminderTime;
    }

    // Getter cho UI - trả về Date
    public Date getReminderTime() {
        return reminderTime != null ? new Date(reminderTime) : null;
    }

    public int getRepeatType() {
        return repeatType;
    }

    public boolean isActive() {
        return isActive;
    }

    // Getter cho Firebase - trả về Long
    @PropertyName("createdAt")
    public Long getCreatedAtTimestamp() {
        return createdAt;
    }

    // Getter cho UI - trả về Date
    public Date getCreatedAt() {
        return createdAt != null ? new Date(createdAt) : null;
    }

    // Getter cho Firebase - trả về Long
    @PropertyName("updatedAt")
    public Long getUpdatedAtTimestamp() {
        return updatedAt;
    }

    // Getter cho UI - trả về Date
    public Date getUpdatedAt() {
        return updatedAt != null ? new Date(updatedAt) : null;
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
        this.updatedAt = System.currentTimeMillis();
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = System.currentTimeMillis();
    }

    // Setter cho Firebase - nhận Long
    @PropertyName("reminderTime")
    public void setReminderTimestamp(Long reminderTime) {
        this.reminderTime = reminderTime;
        this.updatedAt = System.currentTimeMillis();
    }

    // Setter cho UI - nhận Date
    public void setReminderTime(Date reminderTime) {
        this.reminderTime = reminderTime != null ? reminderTime.getTime() : null;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setRepeatType(int repeatType) {
        this.repeatType = repeatType;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setActive(boolean active) {
        isActive = active;
        this.updatedAt = System.currentTimeMillis();
    }

    // Setter cho Firebase - nhận Long
    @PropertyName("createdAt")
    public void setCreatedAtTimestamp(Long createdAt) {
        this.createdAt = createdAt;
    }

    // Setter cho UI - nhận Date
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt != null ? createdAt.getTime() : null;
    }

    // Setter cho Firebase - nhận Long
    @PropertyName("updatedAt")
    public void setUpdatedAtTimestamp(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Setter cho UI - nhận Date
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt != null ? updatedAt.getTime() : null;
    }

    public void setHealthTipId(String healthTipId) {
        this.healthTipId = healthTipId;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * Tính toán thời gian nhắc nhở tiếp theo dựa trên loại lặp lại
     */
    public Date getNextReminderTime() {
        if (reminderTime == null || repeatType == RepeatType.NO_REPEAT) {
            return getReminderTime();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getReminderTime());

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
            default:
                return getReminderTime();
        }

        return calendar.getTime();
    }

    /**
     * Kiểm tra xem nhắc nhở có đã đến giờ hay chưa
     */
    public boolean isDue() {
        if (reminderTime == null || !isActive) {
            return false;
        }
        return System.currentTimeMillis() >= reminderTime;
    }

    /**
     * Lấy Map để ghi vào Firebase với ServerValue.TIMESTAMP
     */
    public Map<String, Object> toFirebaseMap() {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", id);
        map.put("userId", userId);
        map.put("title", title);
        map.put("description", description);
        map.put("reminderTime", reminderTime);
        map.put("repeatType", repeatType);
        map.put("isActive", isActive);
        map.put("healthTipId", healthTipId);
        map.put("createdAt", createdAt != null ? createdAt : ServerValue.TIMESTAMP);
        map.put("updatedAt", ServerValue.TIMESTAMP);
        return map;
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", reminderTime=" + getReminderTime() +
                ", isActive=" + isActive +
                '}';
    }
}
