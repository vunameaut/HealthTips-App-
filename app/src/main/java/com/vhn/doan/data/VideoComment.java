package com.vhn.doan.data;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model đại diện cho bình luận video cùng thông tin phụ trợ như avatar, tên, lượt thích và phản hồi
 */
public class VideoComment {
    private String id;
    private String userId;
    private String userName;
    private String avatarUrl;
    private String comment;
    private long timestamp;
    private Map<String, Boolean> likes;
    private List<VideoComment> replies;

    public VideoComment() {
        // Firebase requires empty constructor
    }

    public VideoComment(String userId, String comment, long timestamp) {
        this.userId = userId;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    // region Getters & Setters
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
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

    public Map<String, Boolean> getLikes() {
        if (likes == null) {
            likes = new HashMap<>();
        }
        return likes;
    }

    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes;
    }

    public List<VideoComment> getReplies() {
        if (replies == null) {
            replies = new ArrayList<>();
        }
        return replies;
    }

    public void setReplies(List<VideoComment> replies) {
        this.replies = replies;
    }
    // endregion

    @Exclude
    public int getLikeCount() {
        return likes == null ? 0 : likes.size();
    }

    @Exclude
    public boolean isLikedBy(String userId) {
        return likes != null && likes.containsKey(userId);
    }
}
