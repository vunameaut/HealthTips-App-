package com.vhn.doan.data;

/**
 * Enum định nghĩa các loại thông báo trong hệ thống
 */
public enum NotificationType {
    // FCM Notification Types
    COMMENT_REPLY("comment_reply", "Trả lời bình luận"),
    NEW_HEALTH_TIP("new_health_tip", "Mẹo sức khỏe mới"),
    NEW_VIDEO("new_video", "Video mới"),
    COMMENT_LIKE("comment_like", "Lượt thích bình luận"),
    HEALTH_TIP_RECOMMENDATION("health_tip_recommendation", "Đề xuất"),
    SUPPORT_REPLY("support_reply", "Trả lời hỗ trợ"),
    ADMIN_REPLY("ADMIN_REPLY", "Phản hồi từ Admin"),

    // Reminder Types
    REMINDER_ALERT("reminder_alert", "Nhắc nhở"),
    REMINDER_ALARM("reminder_alarm", "Báo thức"),

    // System Types
    SYSTEM_UPDATE("system_update", "Cập nhật hệ thống"),
    SYSTEM_MESSAGE("system_message", "Thông báo hệ thống"),

    // General
    OTHER("other", "Khác");

    private final String value;
    private final String displayName;

    NotificationType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Chuyển đổi từ string value sang enum
     * @param value String value của notification type
     * @return NotificationType tương ứng, hoặc OTHER nếu không tìm thấy
     */
    public static NotificationType fromValue(String value) {
        if (value == null) {
            return OTHER;
        }

        for (NotificationType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return OTHER;
    }
}
