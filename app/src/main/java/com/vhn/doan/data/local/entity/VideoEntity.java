package com.vhn.doan.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Room Entity cho ShortVideo - Cache local database
 * Index trên uploadDate để sắp xếp nhanh
 */
@Entity(
    tableName = "videos",
    indices = {
        @Index(value = "upload_date"),
        @Index(value = "like_count")
    }
)
public class VideoEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "caption")
    private String caption;

    @ColumnInfo(name = "video_url")
    private String videoUrl;

    @ColumnInfo(name = "thumbnail_url")
    private String thumbnailUrl;

    @ColumnInfo(name = "uploader_id")
    private String uploaderId;

    @ColumnInfo(name = "uploader_name")
    private String uploaderName;

    @ColumnInfo(name = "uploader_avatar")
    private String uploaderAvatar;

    @ColumnInfo(name = "view_count")
    private long viewCount;

    @ColumnInfo(name = "like_count")
    private long likeCount;

    @ColumnInfo(name = "comment_count")
    private long commentCount;

    @ColumnInfo(name = "share_count")
    private long shareCount;

    @ColumnInfo(name = "upload_date")
    private long uploadDate;

    @ColumnInfo(name = "duration")
    private long duration;

    @ColumnInfo(name = "is_liked")
    private boolean isLiked;

    @ColumnInfo(name = "cached_at")
    private long cachedAt;

    public VideoEntity() {
        this.cachedAt = System.currentTimeMillis();
    }

    @Ignore
    public VideoEntity(@NonNull String id, String title, String caption, String videoUrl,
                       String thumbnailUrl, String uploaderId, String uploaderName,
                       String uploaderAvatar, long viewCount, long likeCount,
                       long commentCount, long shareCount, long uploadDate,
                       long duration, boolean isLiked) {
        this.id = id;
        this.title = title;
        this.caption = caption;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.uploaderId = uploaderId;
        this.uploaderName = uploaderName;
        this.uploaderAvatar = uploaderAvatar;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.shareCount = shareCount;
        this.uploadDate = uploadDate;
        this.duration = duration;
        this.isLiked = isLiked;
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

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
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

    public String getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(String uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getUploaderAvatar() {
        return uploaderAvatar;
    }

    public void setUploaderAvatar(String uploaderAvatar) {
        this.uploaderAvatar = uploaderAvatar;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    public long getShareCount() {
        return shareCount;
    }

    public void setShareCount(long shareCount) {
        this.shareCount = shareCount;
    }

    public long getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(long uploadDate) {
        this.uploadDate = uploadDate;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public long getCachedAt() {
        return cachedAt;
    }

    public void setCachedAt(long cachedAt) {
        this.cachedAt = cachedAt;
    }
}
