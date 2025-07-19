package com.vhn.doan.data;

/**
 * Class đại diện cho một mẹo sức khỏe
 */
public class HealthTip {
    private String id;
    private String title;
    private String content;
    private String categoryId;
    private int viewCount;
    private int likeCount;
    private String imageUrl;
    private long createdAt;
    private boolean isFavorite;

    /**
     * Constructor rỗng cho Firebase
     */
    public HealthTip() {
        // Constructor rỗng cần thiết cho Firebase Realtime Database
    }

    /**
     * Constructor đầy đủ
     * @param id ID của mẹo sức khỏe
     * @param title Tiêu đề mẹo sức khỏe
     * @param content Nội dung chi tiết
     * @param categoryId ID của danh mục chứa mẹo này
     * @param viewCount Số lượt xem
     * @param likeCount Số lượt thích
     */
    public HealthTip(String id, String title, String content, String categoryId, int viewCount, int likeCount) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.createdAt = System.currentTimeMillis();
        this.isFavorite = false;
    }

    /**
     * Constructor đầy đủ với ảnh và thời gian tạo
     */
    public HealthTip(String id, String title, String content, String categoryId, int viewCount,
                     int likeCount, String imageUrl, long createdAt, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.isFavorite = isFavorite;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @Override
    public String toString() {
        return "HealthTip{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", viewCount=" + viewCount +
                ", likeCount=" + likeCount +
                ", imageUrl='" + imageUrl + '\'' +
                ", createdAt=" + createdAt +
                ", isFavorite=" + isFavorite +
                '}';
    }
}
