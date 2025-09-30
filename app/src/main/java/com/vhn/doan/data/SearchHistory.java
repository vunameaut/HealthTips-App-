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
    private Long timestamp; // Thay đổi từ long thành Long để tương thích với Firebase

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

    public Long getTimestamp() {
        return timestamp != null ? timestamp : System.currentTimeMillis();
    }

    /**
     * Setter an toàn để xử lý conversion từ Object sang Long
     * Khắc phục lỗi Firebase DatabaseException với conversion
     */
    public void setTimestamp(Object timestamp) {
        if (timestamp == null) {
            this.timestamp = System.currentTimeMillis();
        } else if (timestamp instanceof Long) {
            this.timestamp = (Long) timestamp;
        } else if (timestamp instanceof Integer) {
            this.timestamp = ((Integer) timestamp).longValue();
        } else if (timestamp instanceof String) {
            try {
                this.timestamp = Long.parseLong((String) timestamp);
            } catch (NumberFormatException e) {
                this.timestamp = System.currentTimeMillis();
            }
        } else {
            this.timestamp = System.currentTimeMillis();
        }
    }

    @Override
    public String toString() {
        return "SearchHistory{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", keyword='" + keyword + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchHistory that = (SearchHistory) o;

        if (!id.equals(that.id)) return false;
        if (!userId.equals(that.userId)) return false;
        return keyword.equals(that.keyword);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + userId.hashCode();
        result = 31 * result + keyword.hashCode();
        return result;
    }
}
