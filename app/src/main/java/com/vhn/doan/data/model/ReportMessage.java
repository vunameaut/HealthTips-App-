package com.vhn.doan.data.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Model đại diện cho một tin nhắn trong Report chat
 * Phân biệt rõ tin nhắn từ user hay admin
 */
public class ReportMessage implements Serializable {

    public static final String SENDER_TYPE_USER = "user";
    public static final String SENDER_TYPE_ADMIN = "admin";

    private String id;
    private String senderId;
    private String senderType;      // "user" hoặc "admin"
    private String senderName;
    private String text;
    private String imageUrl;        // Ảnh trong tin nhắn (optional)
    private long timestamp;
    private boolean read;

    // Constructor mặc định cho Firebase
    public ReportMessage() {
        this.timestamp = System.currentTimeMillis();
        this.read = false;
    }

    // Constructor cho user message
    public static ReportMessage createUserMessage(String id, String userId, String userName, String text) {
        ReportMessage message = new ReportMessage();
        message.setId(id);
        message.setSenderId(userId);
        message.setSenderType(SENDER_TYPE_USER);
        message.setSenderName(userName);
        message.setText(text);
        return message;
    }

    // Constructor cho admin message
    public static ReportMessage createAdminMessage(String id, String adminId, String adminName, String text) {
        ReportMessage message = new ReportMessage();
        message.setId(id);
        message.setSenderId(adminId);
        message.setSenderType(SENDER_TYPE_ADMIN);
        message.setSenderName(adminName);
        message.setText(text);
        return message;
    }

    /**
     * Chuyển đổi Message thành Map để lưu vào Firebase
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("senderId", senderId);
        map.put("senderType", senderType);
        map.put("senderName", senderName);
        map.put("text", text);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            map.put("imageUrl", imageUrl);
        }
        map.put("timestamp", timestamp);
        map.put("read", read);
        return map;
    }

    /**
     * Kiểm tra tin nhắn từ user hay admin
     */
    public boolean isFromUser() {
        return SENDER_TYPE_USER.equals(senderType);
    }

    public boolean isFromAdmin() {
        return SENDER_TYPE_ADMIN.equals(senderType);
    }

    /**
     * Kiểm tra tin nhắn có ảnh không
     */
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.isEmpty();
    }

    /**
     * Lấy preview text ngắn gọn
     */
    public String getPreviewText(int maxLength) {
        if (text == null || text.isEmpty()) {
            return hasImage() ? "[Hình ảnh]" : "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}

