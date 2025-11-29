package com.vhn.doan.model;

/**
 * Model class for support ticket chat messages
 */
public class SupportMessage {
    private String id;
    private String text;
    private String imageUrl;
    private String senderId;
    private String senderType; // "user" or "admin"
    private String senderName;
    private long timestamp;

    public SupportMessage() {
        // Required empty constructor for Firebase
    }

    public SupportMessage(String text, String imageUrl, String senderId,
                         String senderType, String senderName, long timestamp) {
        this.text = text;
        this.imageUrl = imageUrl;
        this.senderId = senderId;
        this.senderType = senderType;
        this.senderName = senderName;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
