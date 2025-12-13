package com.vhn.doan.data.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Model đại diện cho một Report (báo cáo) từ người dùng
 * Mỗi report là một báo cáo riêng biệt với đoạn chat riêng
 */
public class Report implements Serializable {

    // Trạng thái report
    public static final String STATUS_PENDING = "pending";           // Chưa xử lý
    public static final String STATUS_IN_PROGRESS = "in_progress";   // Đang trao đổi
    public static final String STATUS_RESOLVED = "resolved";         // Đã xử lý
    public static final String STATUS_CLOSED = "closed";             // Đã đóng

    // Loại report
    public static final String TYPE_BUG = "bug";
    public static final String TYPE_FEEDBACK = "feedback";
    public static final String TYPE_QUESTION = "question";
    public static final String TYPE_OTHER = "other";

    private String id;
    private String userId;
    private String userEmail;
    private String userName;
    private String title;           // Loại báo cáo
    private String content;         // Nội dung chi tiết
    private String imageUrl;        // Ảnh đính kèm (optional)
    private String status;          // Trạng thái xử lý
    private long createdAt;         // Thời gian tạo
    private long updatedAt;         // Thời gian cập nhật
    private long lastMessageAt;     // Thời gian tin nhắn cuối
    private String lastMessagePreview; // Preview tin nhắn cuối
    private String deviceInfo;      // Thông tin thiết bị

    // Constructor mặc định cho Firebase
    public Report() {
        this.status = STATUS_PENDING;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Constructor đầy đủ
    public Report(String id, String userId, String userEmail, String userName,
                  String title, String content, String imageUrl) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.status = STATUS_PENDING;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * Chuyển đổi Report thành Map để lưu vào Firebase
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("userId", userId);
        map.put("userEmail", userEmail);
        map.put("userName", userName);
        map.put("title", title);
        map.put("content", content);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            map.put("imageUrl", imageUrl);
        }
        map.put("status", status);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        map.put("lastMessageAt", lastMessageAt);
        if (lastMessagePreview != null) {
            map.put("lastMessagePreview", lastMessagePreview);
        }
        if (deviceInfo != null) {
            map.put("deviceInfo", deviceInfo);
        }
        return map;
    }

    /**
     * Kiểm tra report có thể chat được không
     */
    public boolean canChat() {
        return !STATUS_CLOSED.equals(status);
    }

    /**
     * Lấy tên trạng thái hiển thị
     */
    public String getStatusDisplayName() {
        switch (status) {
            case STATUS_PENDING:
                return "Chưa xử lý";
            case STATUS_IN_PROGRESS:
                return "Đang trao đổi";
            case STATUS_RESOLVED:
                return "Đã xử lý";
            case STATUS_CLOSED:
                return "Đã đóng";
            default:
                return "Không xác định";
        }
    }

    /**
     * Lấy màu trạng thái
     */
    public int getStatusColor() {
        switch (status) {
            case STATUS_PENDING:
                return 0xFFFF9800; // Orange
            case STATUS_IN_PROGRESS:
                return 0xFF2196F3; // Blue
            case STATUS_RESOLVED:
                return 0xFF4CAF50; // Green
            case STATUS_CLOSED:
                return 0xFF9E9E9E; // Gray
            default:
                return 0xFF9E9E9E;
        }
    }

    /**
     * Lấy tên loại report hiển thị
     */
    public String getTitleDisplayName() {
        switch (title) {
            case TYPE_BUG:
                return "Báo cáo lỗi";
            case TYPE_FEEDBACK:
                return "Góp ý";
            case TYPE_QUESTION:
                return "Câu hỏi";
            case TYPE_OTHER:
                return "Khác";
            default:
                return title;
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(long lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public void setLastMessagePreview(String lastMessagePreview) {
        this.lastMessagePreview = lastMessagePreview;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public boolean hasImage() {
        return imageUrl != null && !imageUrl.isEmpty();
    }
}

