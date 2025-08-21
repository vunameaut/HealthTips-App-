package com.vhn.doan.data;

import com.google.firebase.database.PropertyName;

/**
 * POJO class đại diện cho lịch sử tìm kiếm của người dùng
 */
public class SearchHistory {
    @PropertyName("id")
    private String id;

    @PropertyName("userId")
    private String userId;

    @PropertyName("keyword")
    private String keyword;

    @PropertyName("timestamp")
    private long timestamp;

    /**
     * Constructor rỗng cho Firebase
     */
    public SearchHistory() {
        // Constructor rỗng cần thiết cho Firebase Realtime Database
    }

    /**
     * Constructor với tham số
     * @param id ID của lịch sử tìm kiếm
     * @param userId ID của người dùng
     * @param keyword Từ khóa tìm kiếm
     */
    public SearchHistory(String id, String userId, String keyword) {
        this.id = id;
        this.userId = userId;
        this.keyword = keyword;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters và setters
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

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
