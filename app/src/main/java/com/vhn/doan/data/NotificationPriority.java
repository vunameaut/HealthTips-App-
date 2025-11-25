package com.vhn.doan.data;

/**
 * Enum định nghĩa mức độ ưu tiên của thông báo
 */
public enum NotificationPriority {
    DEFAULT(0, "Mặc định"),
    HIGH(1, "Cao"),
    MAX(2, "Tối đa");

    private final int value;
    private final String displayName;

    NotificationPriority(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Chuyển đổi từ int value sang enum
     * @param value Int value của priority
     * @return NotificationPriority tương ứng, hoặc DEFAULT nếu không tìm thấy
     */
    public static NotificationPriority fromValue(int value) {
        for (NotificationPriority priority : values()) {
            if (priority.value == value) {
                return priority;
            }
        }
        return DEFAULT;
    }
}
