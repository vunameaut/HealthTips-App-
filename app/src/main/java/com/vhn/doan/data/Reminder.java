package com.vhn.doan.data;

import com.google.firebase.database.PropertyName;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Exclude;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Model class đại diện cho một nhắc nhở sức khỏe
 * Được tối ưu hóa để tương thích với Firebase Realtime Database
 */
public class Reminder implements Serializable {
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

    /**
     * Các method bổ sung cho Reminder
     */

    // Field bổ sung cho lastNotified và completed
    private Long lastNotified;
    private boolean completed = false;

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

    // Firebase getter - trả về Long
    public Long getReminderTime() {
        return reminderTime;
    }

    // UI helper method - trả về Date
    @Exclude
    public Date getReminderTimeAsDate() {
        return reminderTime != null ? new Date(reminderTime) : null;
    }

    public int getRepeatType() {
        return repeatType;
    }

    @PropertyName("isActive")
    public boolean isActive() {
        return isActive;
    }

    // Firebase getter - trả về Long
    public Long getCreatedAt() {
        return createdAt;
    }

    // UI helper method - trả về Date
    @Exclude
    public Date getCreatedAtAsDate() {
        return createdAt != null ? new Date(createdAt) : null;
    }

    // Firebase getter - trả về Long
    public Long getUpdatedAt() {
        return updatedAt;
    }

    // UI helper method - trả về Date
    @Exclude
    public Date getUpdatedAtAsDate() {
        return updatedAt != null ? new Date(updatedAt) : null;
    }

    public String getHealthTipId() {
        return healthTipId;
    }

    // Field bổ sung cho lastNotified và completed
    public Long getLastNotified() {
        return lastNotified;
    }

    public boolean isCompleted() {
        return completed;
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

    // Firebase setter - nhận Long
    public void setReminderTime(Long reminderTime) {
        this.reminderTime = reminderTime;
        this.updatedAt = System.currentTimeMillis();
    }

    // UI helper method - nhận Date
    @Exclude
    public void setReminderTimeFromDate(Date reminderTime) {
        this.reminderTime = reminderTime != null ? reminderTime.getTime() : null;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setRepeatType(int repeatType) {
        this.repeatType = repeatType;
        this.updatedAt = System.currentTimeMillis();
    }

    @PropertyName("isActive")
    public void setActive(boolean active) {
        isActive = active;
        this.updatedAt = System.currentTimeMillis();
    }

    // Firebase setter - nhận Long
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    // UI helper method - nhận Date
    @Exclude
    public void setCreatedAtFromDate(Date createdAt) {
        this.createdAt = createdAt != null ? createdAt.getTime() : null;
    }

    // Firebase setter - nhận Long
    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // UI helper method - nhận Date
    @Exclude
    public void setUpdatedAtFromDate(Date updatedAt) {
        this.updatedAt = updatedAt != null ? updatedAt.getTime() : null;
    }

    public void setHealthTipId(String healthTipId) {
        this.healthTipId = healthTipId;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setLastNotified(Long lastNotified) {
        this.lastNotified = lastNotified;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * Tính toán thời gian nhắc nhở tiếp theo dựa trên loại lặp lại
     */
    @Exclude
    public Date getNextReminderTime() {
        if (reminderTime == null || repeatType == RepeatType.NO_REPEAT) {
            return getReminderTimeAsDate();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getReminderTimeAsDate());

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
                return getReminderTimeAsDate();
        }

        return calendar.getTime();
    }

    /**
     * Kiểm tra xem nhắc nhở có đã đến giờ hay chưa
     */
    @Exclude
    public boolean isDue() {
        if (reminderTime == null || !isActive) {
            return false;
        }
        return System.currentTimeMillis() >= reminderTime;
    }

    /**
     * Lấy tên hiển thị cho loại lặp lại
     */
    @Exclude
    public String getRepeatTypeDisplayName() {
        switch (repeatType) {
            case RepeatType.NO_REPEAT:
                return "Không lặp lại";
            case RepeatType.DAILY:
                return "Hàng ngày";
            case RepeatType.WEEKLY:
                return "Hàng tuần";
            case RepeatType.MONTHLY:
                return "Hàng tháng";
            default:
                return "Không xác định";
        }
    }

    /**
     * Lấy Map để ghi vào Firebase với ServerValue.TIMESTAMP
     */
    @Exclude
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
                ", reminderTime=" + getReminderTimeAsDate() +
                ", isActive=" + isActive +
                '}';
    }

    /**
     * Kiểm tra xem reminder có lặp lại hay không
     */
    @Exclude
    public boolean isRepeating() {
        return repeatType != RepeatType.NO_REPEAT;
    }
}
