package com.vhn.doan.data;

/**
 * Enum định nghĩa các kiểu sắp xếp cho danh sách nhắc nhở
 */
public enum ReminderSortType {
    /** Sắp xếp theo thời gian tạo - mới nhất trước */
    CREATED_TIME_DESC("Mới nhất trước", "created_time_desc"),

    /** Sắp xếp theo thời gian tạo - cũ nhất trước */
    CREATED_TIME_ASC("Cũ nhất trước", "created_time_asc"),

    /** Sắp xếp theo thời gian nhắc nhở - sớm nhất trước */
    REMINDER_TIME_ASC("Thời gian nhắc nhở (sớm nhất)", "reminder_time_asc"),

    /** Sắp xếp theo thời gian nhắc nhở - muộn nhất trước */
    REMINDER_TIME_DESC("Thời gian nhắc nhở (muộn nhất)", "reminder_time_desc"),

    /** Sắp xếp theo tên tiêu đề A-Z */
    TITLE_ASC("Tiêu đề (A-Z)", "title_asc"),

    /** Sắp xếp theo tên tiêu đề Z-A */
    TITLE_DESC("Tiêu đề (Z-A)", "title_desc"),

    /** Sắp xếp theo trạng thái - hoạt động trước */
    STATUS_ACTIVE_FIRST("Hoạt động trước", "status_active_first"),

    /** Sắp xếp theo trạng thái - tắt trước */
    STATUS_INACTIVE_FIRST("Tắt trước", "status_inactive_first");

    private final String displayName;
    private final String key;

    ReminderSortType(String displayName, String key) {
        this.displayName = displayName;
        this.key = key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getKey() {
        return key;
    }

    /**
     * Tìm ReminderSortType từ key
     */
    public static ReminderSortType fromKey(String key) {
        for (ReminderSortType type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }
        return CREATED_TIME_DESC; // Default
    }
}
