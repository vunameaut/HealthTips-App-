package com.vhn.doan.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Model cho tin nhắn chat giữa người dùng và AI
 */
public class ChatMessage {
    private String id;
    private String userId;
    private String content;
    private boolean isFromUser; // true nếu tin nhắn từ user, false nếu từ AI
    private long timestamp;
    private String topic; // Chủ đề được trích xuất từ nội dung

    public ChatMessage() {
        // Constructor rỗng cho Firebase
    }

    public ChatMessage(String userId, String content, boolean isFromUser, long timestamp) {
        this.userId = userId;
        this.content = content;
        this.isFromUser = isFromUser;
        this.timestamp = timestamp;
    }

    public ChatMessage(String id, String userId, String content, boolean isFromUser, long timestamp, String topic) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.isFromUser = isFromUser;
        this.timestamp = timestamp;
        this.topic = topic;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public boolean isFromUser() {
        return isFromUser;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTopic() {
        return topic;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFromUser(boolean fromUser) {
        isFromUser = fromUser;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * Chuyển đổi object thành Map để lưu vào Firebase
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("content", content);
        map.put("isFromUser", isFromUser);
        map.put("timestamp", timestamp);
        map.put("topic", topic);
        return map;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", content='" + content + '\'' +
                ", isFromUser=" + isFromUser +
                ", timestamp=" + timestamp +
                ", topic='" + topic + '\'' +
                '}';
    }
}
