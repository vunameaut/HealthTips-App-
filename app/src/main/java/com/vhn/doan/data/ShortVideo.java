package com.vhn.doan.data;

import com.google.firebase.database.PropertyName;
import java.io.Serializable;
import java.util.Map;

/**
 * POJO class đại diện cho Short Video trong ứng dụng
 * Sử dụng PropertyName annotations để đảm bảo tương thích với Firebase
 */
public class ShortVideo implements Serializable {
    @PropertyName("id")
    private String id;

    @PropertyName("title")
    private String title;

    @PropertyName("caption")
    private String caption;

    @PropertyName("uploadDate")
    private long uploadDate;

    @PropertyName("categoryId")
    private String categoryId;

    @PropertyName("tags")
    private Map<String, Boolean> tags;

    @PropertyName("viewCount")
    private long viewCount;

    @PropertyName("likeCount")
    private long likeCount;

    @PropertyName("userId")
    private String userId;

    @PropertyName("cldPublicId")
    private String cldPublicId;

    @PropertyName("cldVersion")
    private long cldVersion;

    @PropertyName("thumbnailUrl")
    private String thumbnailUrl;

    @PropertyName("thumb")
    private String thumb; // Thêm field thumb từ Firebase

    @PropertyName("status")
    private String status;

    @PropertyName("commentCount")
    private long commentCount;

    @PropertyName("duration")
    private long duration; // Duration in seconds

    // Thuộc tính để theo dõi trạng thái like của video hiện tại
    private boolean isLiked = false;

    // Constructors
    public ShortVideo() {
        // Required for Firebase
    }

    public ShortVideo(String id, String title, String caption, String userId) {
        this.id = id;
        this.title = title;
        this.caption = caption;
        this.userId = userId;
        this.uploadDate = System.currentTimeMillis();
        this.status = "ready";
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCldPublicId() {
        return cldPublicId;
    }

    public void setCldPublicId(String cldPublicId) {
        this.cldPublicId = cldPublicId;
    }

    public long getCldVersion() {
        return cldVersion;
    }

    public void setCldVersion(long cldVersion) {
        this.cldVersion = cldVersion;
    }

    public String getThumbnailUrl() {
        // Ưu tiên sử dụng thumb từ Firebase trước, sau đó mới dùng thumbnailUrl
        if (thumb != null && !thumb.isEmpty()) {
            return thumb;
        }
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * Kiểm tra trạng thái like hiện tại của video
     * @return true nếu video được like, false nếu chưa
     */
    public boolean isLiked() {
        return isLiked;
    }

    /**
     * Thiết lập trạng thái like cho video
     * @param liked trạng thái like mới
     */
    public void setLiked(boolean liked) {
        this.isLiked = liked;
    }

    /**
     * Phương thức alias cho setLiked để tương thích với code hiện có
     */
    public void setIsLiked(boolean liked) {
        this.isLiked = liked;
    }

    /**
     * Phương thức alias cho isLiked để tương thích với code hiện có
     */
    public boolean getIsLiked() {
        return isLiked;
    }

    /**
     * Lấy URL video từ Cloudinary public ID
     * @return URL video hoặc null nếu không có
     */
    public String getVideoUrl() {
        if (cldPublicId != null && !cldPublicId.isEmpty()) {
            // Tạo URL video từ Cloudinary public ID
            return "https://res.cloudinary.com/your-cloud-name/video/upload/v" + cldVersion + "/" + cldPublicId + ".mp4";
        }
        return null;
    }

    @Override
    public String toString() {
        return "ShortVideo{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", caption='" + caption + '\'' +
                ", uploadDate=" + uploadDate +
                ", categoryId='" + categoryId + '\'' +
                ", viewCount=" + viewCount +
                ", likeCount=" + likeCount +
                ", userId='" + userId + '\'' +
                ", cldPublicId='" + cldPublicId + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", thumb='" + thumb + '\'' +
                ", status='" + status + '\'' +
                ", duration=" + duration +
                ", isLiked=" + isLiked +
                '}';
    }
}
