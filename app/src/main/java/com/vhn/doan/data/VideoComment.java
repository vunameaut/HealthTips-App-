package com.vhn.doan.data;

/**
 * Model đơn giản đại diện cho bình luận video
 */
public class VideoComment {
    private String userId;
    private String comment;
    private long timestamp;

    public VideoComment() {
    }

    public VideoComment(String userId, String comment, long timestamp) {
        this.userId = userId;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
