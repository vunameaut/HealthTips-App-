package com.vhn.doan.model;

public class SupportTicket {
    private String id;
    private String issueType;
    private String subject;
    private String description;
    private String imageUrl; // Image attached to the initial report
    private String status; // "pending", "in_progress", "resolved", "closed"
    private long timestamp;
    private String userId;
    private String userEmail;
    private String adminResponse;
    private long respondedAt;

    public SupportTicket() {
        // Required empty constructor for Firebase
    }

    public SupportTicket(String id, String issueType, String subject, String description,
                        String status, long timestamp, String userId, String userEmail) {
        this.id = id;
        this.issueType = issueType;
        this.subject = subject;
        this.description = description;
        this.status = status;
        this.timestamp = timestamp;
        this.userId = userId;
        this.userEmail = userEmail;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }

    public long getRespondedAt() { return respondedAt; }
    public void setRespondedAt(long respondedAt) { this.respondedAt = respondedAt; }
}
