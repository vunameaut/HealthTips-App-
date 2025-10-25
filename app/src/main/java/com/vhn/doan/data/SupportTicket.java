package com.vhn.doan.data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Model đại diện cho một ticket hỗ trợ/báo cáo từ người dùng
 */
public class SupportTicket {

    public enum TicketType {
        BUG_REPORT("bug_report", "Báo cáo lỗi"),
        CONTENT_REPORT("content_report", "Báo cáo nội dung"),
        FEATURE_REQUEST("feature_request", "Đề xuất tính năng"),
        ACCOUNT_ISSUE("account_issue", "Vấn đề tài khoản"),
        GENERAL_INQUIRY("general_inquiry", "Câu hỏi chung"),
        OTHER("other", "Khác");

        private final String value;
        private final String displayName;

        TicketType(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public String getValue() {
            return value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static TicketType fromValue(String value) {
            for (TicketType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            return OTHER;
        }
    }

    public enum TicketStatus {
        OPEN("open", "Đang mở"),
        IN_PROGRESS("in_progress", "Đang xử lý"),
        RESOLVED("resolved", "Đã giải quyết"),
        CLOSED("closed", "Đã đóng");

        private final String value;
        private final String displayName;

        TicketStatus(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public String getValue() {
            return value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static TicketStatus fromValue(String value) {
            for (TicketStatus status : values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            return OPEN;
        }
    }

    public enum Priority {
        LOW("low", "Thấp"),
        MEDIUM("medium", "Trung bình"),
        HIGH("high", "Cao"),
        URGENT("urgent", "Khẩn cấp");

        private final String value;
        private final String displayName;

        Priority(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public String getValue() {
            return value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Priority fromValue(String value) {
            for (Priority priority : values()) {
                if (priority.value.equals(value)) {
                    return priority;
                }
            }
            return MEDIUM;
        }
    }

    private String ticketId;
    private String userId;
    private String userEmail;
    private String userName;
    private TicketType type;
    private String subject;
    private String description;
    private String relatedContentId; // ID của nội dung bị báo cáo (nếu có)
    private String relatedContentType; // "health_tip", "comment", "user", etc.
    private TicketStatus status;
    private Priority priority;
    private Date createdAt;
    private Date updatedAt;
    private Date resolvedAt;
    private String adminResponse;
    private String adminId;
    private String deviceInfo;
    private String appVersion;
    private String screenshotUrl; // URL ảnh chụp màn hình (nếu có)

    // Constructors
    public SupportTicket() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.status = TicketStatus.OPEN;
        this.priority = Priority.MEDIUM;
    }

    public SupportTicket(String userId, String userEmail, String userName,
                        TicketType type, String subject, String description) {
        this();
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.type = type;
        this.subject = subject;
        this.description = description;
    }

    // Getters and Setters
    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public TicketType getType() {
        return type;
    }

    public void setType(TicketType type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRelatedContentId() {
        return relatedContentId;
    }

    public void setRelatedContentId(String relatedContentId) {
        this.relatedContentId = relatedContentId;
    }

    public String getRelatedContentType() {
        return relatedContentType;
    }

    public void setRelatedContentType(String relatedContentType) {
        this.relatedContentType = relatedContentType;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
        this.updatedAt = new Date();
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Date resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getAdminResponse() {
        return adminResponse;
    }

    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
        this.updatedAt = new Date();
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getScreenshotUrl() {
        return screenshotUrl;
    }

    public void setScreenshotUrl(String screenshotUrl) {
        this.screenshotUrl = screenshotUrl;
    }

    // Chuyển đổi sang Map cho Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("ticketId", ticketId);
        map.put("userId", userId);
        map.put("userEmail", userEmail);
        map.put("userName", userName);
        map.put("type", type != null ? type.getValue() : null);
        map.put("subject", subject);
        map.put("description", description);
        map.put("relatedContentId", relatedContentId);
        map.put("relatedContentType", relatedContentType);
        map.put("status", status != null ? status.getValue() : TicketStatus.OPEN.getValue());
        map.put("priority", priority != null ? priority.getValue() : Priority.MEDIUM.getValue());

        // Lưu Date dưới dạng timestamp (Long) để Firebase xử lý đúng
        map.put("createdAt", createdAt != null ? createdAt.getTime() : System.currentTimeMillis());
        map.put("updatedAt", updatedAt != null ? updatedAt.getTime() : System.currentTimeMillis());
        map.put("resolvedAt", resolvedAt != null ? resolvedAt.getTime() : null);

        map.put("adminResponse", adminResponse);
        map.put("adminId", adminId);
        map.put("deviceInfo", deviceInfo);
        map.put("appVersion", appVersion);
        map.put("screenshotUrl", screenshotUrl);
        return map;
    }

    // Tạo từ Map từ Firebase
    public static SupportTicket fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        SupportTicket ticket = new SupportTicket();
        ticket.setTicketId((String) map.get("ticketId"));
        ticket.setUserId((String) map.get("userId"));
        ticket.setUserEmail((String) map.get("userEmail"));
        ticket.setUserName((String) map.get("userName"));

        // Parse TicketType - xử lý cả String và Enum object
        Object typeObj = map.get("type");
        if (typeObj instanceof String) {
            String typeValue = (String) typeObj;
            ticket.setType(typeValue != null ? TicketType.fromValue(typeValue) : TicketType.OTHER);
        } else if (typeObj instanceof Map) {
            // Trường hợp Firebase lưu enum dưới dạng object
            @SuppressWarnings("unchecked")
            Map<String, Object> typeMap = (Map<String, Object>) typeObj;
            String typeValue = (String) typeMap.get("value");
            ticket.setType(typeValue != null ? TicketType.fromValue(typeValue) : TicketType.OTHER);
        }

        ticket.setSubject((String) map.get("subject"));
        ticket.setDescription((String) map.get("description"));
        ticket.setRelatedContentId((String) map.get("relatedContentId"));
        ticket.setRelatedContentType((String) map.get("relatedContentType"));

        // Parse TicketStatus
        Object statusObj = map.get("status");
        if (statusObj instanceof String) {
            String statusValue = (String) statusObj;
            ticket.setStatus(statusValue != null ? TicketStatus.fromValue(statusValue) : TicketStatus.OPEN);
        } else if (statusObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> statusMap = (Map<String, Object>) statusObj;
            String statusValue = (String) statusMap.get("value");
            ticket.setStatus(statusValue != null ? TicketStatus.fromValue(statusValue) : TicketStatus.OPEN);
        }

        // Parse Priority
        Object priorityObj = map.get("priority");
        if (priorityObj instanceof String) {
            String priorityValue = (String) priorityObj;
            ticket.setPriority(priorityValue != null ? Priority.fromValue(priorityValue) : Priority.MEDIUM);
        } else if (priorityObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> priorityMap = (Map<String, Object>) priorityObj;
            String priorityValue = (String) priorityMap.get("value");
            ticket.setPriority(priorityValue != null ? Priority.fromValue(priorityValue) : Priority.MEDIUM);
        }

        // Parse Date từ timestamp (Firebase lưu Date dưới dạng Long)
        Object createdAtObj = map.get("createdAt");
        if (createdAtObj instanceof Long) {
            ticket.setCreatedAt(new Date((Long) createdAtObj));
        } else if (createdAtObj instanceof Date) {
            ticket.setCreatedAt((Date) createdAtObj);
        } else if (createdAtObj instanceof Integer) {
            ticket.setCreatedAt(new Date(((Integer) createdAtObj).longValue()));
        }

        Object updatedAtObj = map.get("updatedAt");
        if (updatedAtObj instanceof Long) {
            ticket.setUpdatedAt(new Date((Long) updatedAtObj));
        } else if (updatedAtObj instanceof Date) {
            ticket.setUpdatedAt((Date) updatedAtObj);
        } else if (updatedAtObj instanceof Integer) {
            ticket.setUpdatedAt(new Date(((Integer) updatedAtObj).longValue()));
        }

        Object resolvedAtObj = map.get("resolvedAt");
        if (resolvedAtObj instanceof Long) {
            ticket.setResolvedAt(new Date((Long) resolvedAtObj));
        } else if (resolvedAtObj instanceof Date) {
            ticket.setResolvedAt((Date) resolvedAtObj);
        } else if (resolvedAtObj instanceof Integer) {
            ticket.setResolvedAt(new Date(((Integer) resolvedAtObj).longValue()));
        }

        ticket.setAdminResponse((String) map.get("adminResponse"));
        ticket.setAdminId((String) map.get("adminId"));
        ticket.setDeviceInfo((String) map.get("deviceInfo"));
        ticket.setAppVersion((String) map.get("appVersion"));
        ticket.setScreenshotUrl((String) map.get("screenshotUrl"));

        return ticket;
    }
}

