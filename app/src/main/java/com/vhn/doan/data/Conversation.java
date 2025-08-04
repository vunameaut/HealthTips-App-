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
    private boolean isPinned; // Cuộc trò chuyện có được ghim không
    private boolean isMuted; // Cuộc trò chuyện có bị tắt thông báo không
    private boolean isRead; // Cuộc trò chuyện đã được đọc chưa

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
                       long createdTime, String topic, boolean isPinned, boolean isMuted, boolean isRead) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.isFromUser = isFromUser;
        this.messageCount = messageCount;
        this.createdTime = createdTime;
        this.topic = topic;
        this.isPinned = isPinned;
        this.isMuted = isMuted;
        this.isRead = isRead;
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

    public boolean isPinned() {
        return isPinned;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public boolean isRead() {
        return isRead;
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

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public void setRead(boolean read) {
        isRead = read;
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
        map.put("isPinned", isPinned);
        map.put("isMuted", isMuted);
        map.put("isRead", isRead);
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
        
        // Loại bỏ các ký tự đặc biệt và emoji
        trimmed = trimmed.replaceAll("[^\\p{L}\\p{N}\\s\\?\\!\\.]", "");
        
        // Cắt tại dấu câu đầu tiên để tạo tiêu đề ngắn gọn
        String[] sentences = trimmed.split("[.!?]");
        if (sentences.length > 0 && !sentences[0].trim().isEmpty()) {
            String firstSentence = sentences[0].trim();
            // Nếu câu đầu tiên đủ ngắn, sử dụng luôn không cần dấu ...
            if (firstSentence.length() <= 25) {
                return firstSentence;
            }
            trimmed = firstSentence;
        }
        
        // Lấy tối đa 25 ký tự để có thể hiển thị đầy đủ hơn
        if (trimmed.length() <= 25) {
            return trimmed;
        } else {
            // Cắt tại từ cuối cùng để tránh cắt giữa từ
            String shortened = trimmed.substring(0, 25);
            int lastSpace = shortened.lastIndexOf(' ');
            if (lastSpace > 15) { // Đảm bảo tiêu đề không quá ngắn
                return trimmed.substring(0, lastSpace) + "...";
            } else {
                return shortened + "...";
            }
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
                ", isPinned=" + isPinned +
                ", isMuted=" + isMuted +
                ", isRead=" + isRead +
                '}';
    }
}
