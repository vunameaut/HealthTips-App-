package com.vhn.doan.data;

import com.google.firebase.database.PropertyName;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Exclude;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class đại diện cho một nhắc nhở sức khỏe với chức năng báo thức
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

    // Trường mới cho âm thanh và báo thức
    private String soundId; // ID của âm thanh được chọn
    private String soundName; // Tên hiển thị của âm thanh
    private String soundUri; // URI của âm thanh
    private boolean vibrate = true; // Có rung hay không
    private int snoozeMinutes = 5; // Số phút báo lại
    private int volume = 80; // Âm lượng từ 0-100
    private boolean isAlarmStyle = true; // Hiển thị dạng báo thức thay vì notification

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
        this.vibrate = true;
        this.snoozeMinutes = 5;
        this.volume = 80;
        this.isAlarmStyle = true;
        // Âm thanh mặc định
        this.soundId = "default_alarm";
        this.soundName = "Báo thức mặc định";
    }

    // Constructor với tham số
    public Reminder(String id, String userId, String title, String description, Long reminderTime) {
        this();
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.reminderTime = reminderTime;
    }

    // Getters và Setters hiện tại
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getReminderTime() { return reminderTime; }
    public void setReminderTime(Long reminderTime) { this.reminderTime = reminderTime; }

    public int getRepeatType() { return repeatType; }
    public void setRepeatType(int repeatType) { this.repeatType = repeatType; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }

    public String getHealthTipId() { return healthTipId; }
    public void setHealthTipId(String healthTipId) { this.healthTipId = healthTipId; }

    public Long getLastNotified() { return lastNotified; }
    public void setLastNotified(Long lastNotified) { this.lastNotified = lastNotified; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    // Getters và Setters mới cho âm thanh và báo thức
    public String getSoundId() { return soundId; }
    public void setSoundId(String soundId) { this.soundId = soundId; }

    public String getSoundName() { return soundName; }
    public void setSoundName(String soundName) { this.soundName = soundName; }

    public String getSoundUri() { return soundUri; }
    public void setSoundUri(String soundUri) { this.soundUri = soundUri; }

    public boolean isVibrate() { return vibrate; }
    public void setVibrate(boolean vibrate) { this.vibrate = vibrate; }

    public int getSnoozeMinutes() { return snoozeMinutes; }
    public void setSnoozeMinutes(int snoozeMinutes) { this.snoozeMinutes = snoozeMinutes; }

    public int getVolume() { return volume; }
    public void setVolume(int volume) { this.volume = volume; }

    public boolean isAlarmStyle() { return isAlarmStyle; }
    public void setAlarmStyle(boolean alarmStyle) { isAlarmStyle = alarmStyle; }

    // Utility methods
    @Exclude
    public Date getReminderTimeAsDate() {
        return reminderTime != null ? new Date(reminderTime) : null;
    }

    @Exclude
    public void setReminderTimeFromDate(Date date) {
        this.reminderTime = date != null ? date.getTime() : null;
    }

    @Exclude
    public String getRepeatTypeText() {
        switch (repeatType) {
            case RepeatType.DAILY: return "Hàng ngày";
            case RepeatType.WEEKLY: return "Hàng tuần";
            case RepeatType.MONTHLY: return "Hàng tháng";
            default: return "Không lặp";
        }
    }

    // Tự động cập nhật updatedAt khi có thay đổi
    public void touch() {
        this.updatedAt = System.currentTimeMillis();
    }

    // Tính toán thời gian nhắc nhở tiếp theo (cho lặp lại)
    @Exclude
    public Long getNextReminderTime() {
        if (reminderTime == null || repeatType == RepeatType.NO_REPEAT) {
            return reminderTime;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(reminderTime);

        Calendar now = Calendar.getInstance();

        // Nếu thời gian hiện tại đã qua thời gian nhắc nhở, tính toán lần tiếp theo
        while (calendar.before(now)) {
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
        }

        return calendar.getTimeInMillis();
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", reminderTime=" + reminderTime +
                ", isActive=" + isActive +
                ", soundName='" + soundName + '\'' +
                '}';
    }

    // Phương thức thiếu cho Firebase
    @Exclude
    public Map<String, Object> toFirebaseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("userId", userId);
        map.put("title", title);
        map.put("description", description);
        map.put("reminderTime", reminderTime);
        map.put("repeatType", repeatType);
        map.put("isActive", isActive);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        map.put("healthTipId", healthTipId);
        map.put("soundId", soundId);
        map.put("soundName", soundName);
        map.put("soundUri", soundUri);
        map.put("vibrate", vibrate);
        map.put("snoozeMinutes", snoozeMinutes);
        map.put("volume", volume);
        map.put("isAlarmStyle", isAlarmStyle);
        map.put("lastNotified", lastNotified);
        map.put("completed", completed);
        return map;
    }

    // Phương thức hiển thị tên loại lặp lại
    @Exclude
    public String getRepeatTypeDisplayName() {
        switch (repeatType) {
            case RepeatType.DAILY: return "Hàng ngày";
            case RepeatType.WEEKLY: return "Hàng tuần";
            case RepeatType.MONTHLY: return "Hàng tháng";
            default: return "Không lặp";
        }
    }

    // Phương thức kiểm tra xem nhắc nhở có đến hạn không
    @Exclude
    public boolean isDue() {
        if (reminderTime == null || !isActive) {
            return false;
        }

        long currentTime = System.currentTimeMillis();

        // Kiểm tra nếu là lặp lại
        if (repeatType != RepeatType.NO_REPEAT) {
            Long nextTime = getNextReminderTime();
            return nextTime != null && nextTime <= currentTime;
        }

        // Nhắc nhở một lần
        return reminderTime <= currentTime && !completed;
    }
}
