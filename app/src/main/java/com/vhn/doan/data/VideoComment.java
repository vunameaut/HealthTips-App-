package com.vhn.doan.data;

import com.google.firebase.database.PropertyName;
import com.google.firebase.database.ServerValue;

/**
 * Model cho bình luận video
 * Hỗ trợ cả bình luận gốc và reply
 */
public class VideoComment {
    @PropertyName("id")
    private String id;

    @PropertyName("userId")
    private String userId;

    @PropertyName("text")
    private String text;

    @PropertyName("createdAt")
    private Object createdAt;

    @PropertyName("parentId")
    private String parentId; // null cho bình luận gốc, commentId cho reply

    @PropertyName("likeCount")
    private long likeCount;

    @PropertyName("replyCount")
    private long replyCount;

    // Constructor mặc định
    public VideoComment() {
    }

    // Constructor cho bình luận mới
    public VideoComment(String userId, String text, String parentId) {
        this.userId = userId;
        this.text = text;
        this.parentId = parentId;
        this.createdAt = ServerValue.TIMESTAMP;
        this.likeCount = 0;
        this.replyCount = 0;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getText() {
        return text;
    }

    public Object getCreatedAt() {
        return createdAt;
    }

    public String getParentId() {
        return parentId;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public long getReplyCount() {
        return replyCount;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCreatedAt(Object createdAt) {
        this.createdAt = createdAt;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public void setReplyCount(long replyCount) {
        this.replyCount = replyCount;
    }

    // Helper methods
    public boolean isReply() {
        return parentId != null && !parentId.isEmpty();
    }

    public boolean isRootComment() {
        return parentId == null || parentId.isEmpty();
    }

    @Override
    public String toString() {
        return "VideoComment{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", text='" + text + '\'' +
                ", createdAt=" + createdAt +
                ", parentId='" + parentId + '\'' +
                ", likeCount=" + likeCount +
                ", replyCount=" + replyCount +
                '}';
    }
}
