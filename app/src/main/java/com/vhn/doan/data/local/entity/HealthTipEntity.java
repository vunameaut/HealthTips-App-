package com.vhn.doan.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Room Entity cho HealthTip - Cache local database
 * Index trên categoryId để tìm kiếm nhanh
 */
@Entity(
    tableName = "health_tips",
    indices = {
        @Index(value = "category_id"),
        @Index(value = "created_at")
    }
)
public class HealthTipEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "excerpt")
    private String excerpt;

    @ColumnInfo(name = "category_id")
    private String categoryId;

    @ColumnInfo(name = "category_name")
    private String categoryName;

    @ColumnInfo(name = "view_count")
    private int viewCount;

    @ColumnInfo(name = "like_count")
    private int likeCount;

    @ColumnInfo(name = "image_url")
    private String imageUrl;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "is_favorite")
    private boolean isFavorite;

    @ColumnInfo(name = "is_liked")
    private boolean isLiked;

    @ColumnInfo(name = "recommendation_score")
    private int recommendationScore;

    @ColumnInfo(name = "cached_at")
    private long cachedAt; // Thời điểm cache vào DB

    // Constructors
    public HealthTipEntity() {
        this.cachedAt = System.currentTimeMillis();
    }

    @Ignore
    public HealthTipEntity(@NonNull String id, String title, String excerpt, String categoryId,
                           String categoryName, int viewCount, int likeCount, String imageUrl,
                           long createdAt, boolean isFavorite, boolean isLiked,
                           int recommendationScore) {
        this.id = id;
        this.title = title;
        this.excerpt = excerpt;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.isFavorite = isFavorite;
        this.isLiked = isLiked;
        this.recommendationScore = recommendationScore;
        this.cachedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public int getRecommendationScore() {
        return recommendationScore;
    }

    public void setRecommendationScore(int recommendationScore) {
        this.recommendationScore = recommendationScore;
    }

    public long getCachedAt() {
        return cachedAt;
    }

    public void setCachedAt(long cachedAt) {
        this.cachedAt = cachedAt;
    }
}
