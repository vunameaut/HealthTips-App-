package com.vhn.doan.model;

import java.util.Map;

public class UserNotification {
    private String id;
    private String type; // "ADMIN_RESPONSE", "SYSTEM", etc.
    private String title;
    private String message;
    private Map<String, Object> data;
    private boolean read;
    private long createdAt;
    private String priority; // "low", "medium", "high", "critical"

    public UserNotification() {
        // Required empty constructor for Firebase
    }

    public UserNotification(String id, String type, String title, String message,
                          Map<String, Object> data, boolean read, long createdAt, String priority) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.message = message;
        this.data = data;
        this.read = read;
        this.createdAt = createdAt;
        this.priority = priority;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
