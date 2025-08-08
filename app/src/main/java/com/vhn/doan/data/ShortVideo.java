package com.vhn.doan.data;

import com.google.firebase.database.PropertyName;
import java.util.Map;

/**
 * Model class để đại diện cho video ngắn trong ứng dụng
 * Được sử dụng với Firebase Realtime Database
 */
public class ShortVideo {
    @PropertyName("id")
    private String id;

    @PropertyName("title")
    private String title;

    @PropertyName("caption")
    private String caption;

    @PropertyName("uploadDate")
    private long uploadDate;

    @PropertyName("videoUrl")
    private String videoUrl;

    @PropertyName("thumbnailUrl")
    private String thumbnailUrl;

    @PropertyName("categoryId")
    private String categoryId;

    @PropertyName("tags")
    private Map<String, Boolean> tags;

    @PropertyName("viewCount")
    private int viewCount;

    @PropertyName("likeCount")
    private int likeCount;

    @PropertyName("userId")
    private String userId;

    // Constructor mặc định cần thiết cho Firebase
    public ShortVideo() {
    }

    public ShortVideo(String id, String title, String caption, long uploadDate,
                     String videoUrl, String thumbnailUrl, String categoryId,
                     Map<String, Boolean> tags, int viewCount, int likeCount, String userId) {
        this.id = id;
        this.title = title;
        this.caption = caption;
        this.uploadDate = uploadDate;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.categoryId = categoryId;
        this.tags = tags;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.userId = userId;
    }

    // Getters và Setters
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

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public long getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(long uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public Map<String, Boolean> getTags() {
        return tags;
    }

    public void setTags(Map<String, Boolean> tags) {
        this.tags = tags;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "ShortVideo{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", caption='" + caption + '\'' +
                ", uploadDate=" + uploadDate +
                ", videoUrl='" + videoUrl + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", tags=" + tags +
                ", viewCount=" + viewCount +
                ", likeCount=" + likeCount +
                ", userId='" + userId + '\'' +
                '}';
    }
}
