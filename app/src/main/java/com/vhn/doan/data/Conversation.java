package com.vhn.doan.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Model cho một cuộc trò chuyện với AI
 */
public class Conversation {
    private String id;
    private String userId;
    private String title; // Tiêu đề cuộc trò chuyện (tự động tạo từ tin nhắn đầu tiên)
    private String lastMessage; // Tin nhắn cuối cùng trong cuộc trò chuyện
    private long lastMessageTime; // Thời gian tin nhắn cuối cùng
    private boolean isFromUser; // Tin nhắn cuối cùng có phải từ user không
    private int messageCount; // Số lượng tin nhắn trong cuộc trò chuyện
    private long createdTime; // Thời gian tạo cuộc trò chuyện
    private String topic; // Chủ đề chính của cuộc trò chuyện

    public Conversation() {
        // Constructor rỗng cho Firebase
    }

    public Conversation(String userId, String title, long createdTime) {
        this.userId = userId;
        this.title = title;
        this.createdTime = createdTime;
        this.lastMessageTime = createdTime;
        this.messageCount = 0;
    }

    public Conversation(String id, String userId, String title, String lastMessage,
                       long lastMessageTime, boolean isFromUser, int messageCount,
                       long createdTime, String topic) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.isFromUser = isFromUser;
        this.messageCount = messageCount;
        this.createdTime = createdTime;
        this.topic = topic;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public boolean isFromUser() {
        return isFromUser;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public long getCreatedTime() {
        return createdTime;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public void setFromUser(boolean fromUser) {
        isFromUser = fromUser;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
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
        map.put("title", title);
        map.put("lastMessage", lastMessage);
        map.put("lastMessageTime", lastMessageTime);
        map.put("isFromUser", isFromUser);
        map.put("messageCount", messageCount);
        map.put("createdTime", createdTime);
        map.put("topic", topic);
        return map;
    }

    /**
     * Tạo tiêu đề tự động từ nội dung tin nhắn đầu tiên
     */
    public static String generateTitle(String firstMessage) {
        if (firstMessage == null || firstMessage.trim().isEmpty()) {
            return "Cuộc trò chuyện mới";
        }

        String trimmed = firstMessage.trim();
        // Lấy tối đa 30 ký tự đầu tiên
        if (trimmed.length() <= 30) {
            return trimmed;
        } else {
            return trimmed.substring(0, 27) + "...";
        }
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                ", lastMessageTime=" + lastMessageTime +
                ", isFromUser=" + isFromUser +
                ", messageCount=" + messageCount +
                ", createdTime=" + createdTime +
                ", topic='" + topic + '\'' +
                '}';
    }
}
