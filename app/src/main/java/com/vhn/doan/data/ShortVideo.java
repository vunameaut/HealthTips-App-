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

    @PropertyName("status")
    private String status;

    @PropertyName("commentCount")
    private long commentCount = 0;

    // Thuộc tính để theo dõi trạng thái like của video hiện tại
    // Không đánh dấu bằng PropertyName vì đây là trạng thái local, không lưu trữ trên Firebase
    private boolean liked = false;

    // Constructor mặc định
    public ShortVideo() {
    }

    // Constructor đầy đủ tham số
    public ShortVideo(String id, String title, String caption, long uploadDate, 
                     String categoryId, Map<String, Boolean> tags, long viewCount, 
                     long likeCount, String userId, String cldPublicId, 
                     long cldVersion, String thumbnailUrl, String status) {
        this.id = id;
        this.title = title;
        this.caption = caption;
        this.uploadDate = uploadDate;
        this.categoryId = categoryId;
        this.tags = tags;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.userId = userId;
        this.cldPublicId = cldPublicId;
        this.cldVersion = cldVersion;
        this.thumbnailUrl = thumbnailUrl;
        this.status = status;
        this.commentCount = 0; // Mặc định 0 comment
    }

    // Constructor mở rộng với commentCount
    public ShortVideo(String id, String title, String caption, long uploadDate,
                     String categoryId, Map<String, Boolean> tags, long viewCount,
                     long likeCount, String userId, String cldPublicId,
                     long cldVersion, String thumbnailUrl, String status, long commentCount) {
        this.id = id;
        this.title = title;
        this.caption = caption;
        this.uploadDate = uploadDate;
        this.categoryId = categoryId;
        this.tags = tags;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.userId = userId;
        this.cldPublicId = cldPublicId;
        this.cldVersion = cldVersion;
        this.thumbnailUrl = thumbnailUrl;
        this.status = status;
        this.commentCount = commentCount;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCaption() {
        return caption;
    }

    public long getUploadDate() {
        return uploadDate;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public Map<String, Boolean> getTags() {
        return tags;
    }

    public long getViewCount() {
        return viewCount;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public String getUserId() {
        return userId;
    }

    public String getCldPublicId() {
        return cldPublicId;
    }

    public long getCldVersion() {
        return cldVersion;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getStatus() {
        return status;
    }

    /**
     * Lấy số lượng bình luận của video
     * @return số lượng bình luận
     */
    public long getCommentCount() {
        return commentCount;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setUploadDate(long uploadDate) {
        this.uploadDate = uploadDate;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setTags(Map<String, Boolean> tags) {
        this.tags = tags;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCldPublicId(String cldPublicId) {
        this.cldPublicId = cldPublicId;
    }

    public void setCldVersion(long cldVersion) {
        this.cldVersion = cldVersion;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Thiết lập số lượng bình luận cho video
     * @param commentCount số lượng bình luận mới
     */
    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    /**
     * Kiểm tra trạng thái like hiện tại của video
     * @return true nếu video được like, false nếu chưa
     */
    public boolean isLiked() {
        return liked;
    }

    /**
     * Thiết lập trạng thái like cho video
     * @param liked trạng thái like mới
     */
    public void setLiked(boolean liked) {
        this.liked = liked;
    }


    /**
     * Lấy URL video để chia sẻ
     * @return URL của video
     */
    public String getVideoUrl() {
        // Tạo URL video từ Cloudinary public ID nếu có
        if (cldPublicId != null && !cldPublicId.isEmpty()) {
            return "https://res.cloudinary.com/your-cloud-name/video/upload/" + cldPublicId;
        }
        return null;
    }

    /**
     * Lấy upload date dưới dạng Date object
     * @return Date object từ timestamp
     */
    public java.util.Date getUploadDateAsDate() {
        return new java.util.Date(uploadDate);
    }

    @Override
    public String toString() {
        return "ShortVideo{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", caption='" + caption + '\'' +
                ", uploadDate=" + uploadDate +
                ", categoryId='" + categoryId + '\'' +
                ", tags=" + tags +
                ", viewCount=" + viewCount +
                ", likeCount=" + likeCount +
                ", userId='" + userId + '\'' +
                ", cldPublicId='" + cldPublicId + '\'' +
                ", cldVersion=" + cldVersion +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
