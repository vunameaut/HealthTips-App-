package com.vhn.doan.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.local.Converters;

import java.util.List;
import java.util.Map;

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
@TypeConverters(Converters.class)
public class HealthTipEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "content")
    private String content;

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

    // Các trường bổ sung để support offline mode đầy đủ
    @ColumnInfo(name = "content_blocks")
    private List<Map<String, Object>> contentBlocks;

    @ColumnInfo(name = "tags")
    private List<String> tags;

    @ColumnInfo(name = "author")
    private String author;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "published_at")
    private Long publishedAt;

    @ColumnInfo(name = "updated_at")
    private Long updatedAt;

    @ColumnInfo(name = "is_feature")
    private Boolean isFeature;

    @ColumnInfo(name = "is_pinned")
    private Boolean isPinned;

    @ColumnInfo(name = "seo_title")
    private String seoTitle;

    @ColumnInfo(name = "seo_description")
    private String seoDescription;

    @ColumnInfo(name = "scheduled_at")
    private Long scheduledAt;

    @ColumnInfo(name = "slug")
    private String slug;

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

    /**
     * Tạo HealthTipEntity từ HealthTip model
     */
    @Ignore
    public static HealthTipEntity fromHealthTip(HealthTip healthTip) {
        HealthTipEntity entity = new HealthTipEntity();
        entity.setId(healthTip.getId());
        entity.setTitle(healthTip.getTitle());
        entity.setContent(healthTip.getContent());
        entity.setExcerpt(healthTip.getExcerpt());
        entity.setCategoryId(healthTip.getCategoryId());
        entity.setCategoryName(healthTip.getCategoryName());
        entity.setViewCount(healthTip.getViewCount() != null ? healthTip.getViewCount() : 0);
        entity.setLikeCount(healthTip.getLikeCount() != null ? healthTip.getLikeCount() : 0);
        entity.setImageUrl(healthTip.getImageUrl());
        entity.setCreatedAt(healthTip.getCreatedAt() != null ? healthTip.getCreatedAt() : System.currentTimeMillis());
        entity.setFavorite(healthTip.isFavorite());
        entity.setLiked(healthTip.isLiked());
        entity.setRecommendationScore(healthTip.getRecommendationScore() != null ? healthTip.getRecommendationScore() : 0);
        entity.setContentBlocks(healthTip.getContentBlocks());
        entity.setTags(healthTip.getTags());
        entity.setAuthor(healthTip.getAuthor());
        entity.setStatus(healthTip.getStatus());
        entity.setPublishedAt(healthTip.getPublishedAt());
        entity.setUpdatedAt(healthTip.getUpdatedAt());
        entity.setIsFeature(healthTip.getIsFeature());
        entity.setIsPinned(healthTip.getIsPinned());
        entity.setSeoTitle(healthTip.getSeoTitle());
        entity.setSeoDescription(healthTip.getSeoDescription());
        entity.setScheduledAt(healthTip.getScheduledAt());
        entity.setSlug(healthTip.getSlug());
        entity.setCachedAt(System.currentTimeMillis());
        return entity;
    }

    /**
     * Chuyển đổi Entity thành HealthTip model
     */
    public HealthTip toHealthTip() {
        HealthTip healthTip = new HealthTip();
        healthTip.setId(this.id);
        healthTip.setTitle(this.title);
        healthTip.setContent(this.content);
        healthTip.setExcerpt(this.excerpt);
        healthTip.setCategoryId(this.categoryId);
        healthTip.setCategoryName(this.categoryName);
        healthTip.setViewCount(this.viewCount);
        healthTip.setLikeCount(this.likeCount);
        healthTip.setImageUrl(this.imageUrl);
        healthTip.setCreatedAt(this.createdAt);
        healthTip.setFavorite(this.isFavorite);
        healthTip.setLiked(this.isLiked);
        healthTip.setRecommendationScore(this.recommendationScore);
        healthTip.setContentBlocks(this.contentBlocks);
        healthTip.setTags(this.tags);
        healthTip.setAuthor(this.author);
        healthTip.setStatus(this.status);
        healthTip.setPublishedAt(this.publishedAt);
        healthTip.setUpdatedAt(this.updatedAt);
        healthTip.setIsFeature(this.isFeature);
        healthTip.setIsPinned(this.isPinned);
        healthTip.setSeoTitle(this.seoTitle);
        healthTip.setSeoDescription(this.seoDescription);
        healthTip.setScheduledAt(this.scheduledAt);
        healthTip.setSlug(this.slug);
        return healthTip;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Map<String, Object>> getContentBlocks() {
        return contentBlocks;
    }

    public void setContentBlocks(List<Map<String, Object>> contentBlocks) {
        this.contentBlocks = contentBlocks;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Long publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsFeature() {
        return isFeature;
    }

    public void setIsFeature(Boolean isFeature) {
        this.isFeature = isFeature;
    }

    public Boolean getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }

    public String getSeoTitle() {
        return seoTitle;
    }

    public void setSeoTitle(String seoTitle) {
        this.seoTitle = seoTitle;
    }

    public String getSeoDescription() {
        return seoDescription;
    }

    public void setSeoDescription(String seoDescription) {
        this.seoDescription = seoDescription;
    }

    public Long getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Long scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
