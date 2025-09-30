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
    private Long uploadDate; // Thay đổi từ long thành Long

    @PropertyName("categoryId")
    private String categoryId;

    @PropertyName("tags")
    private Map<String, Boolean> tags;

    @PropertyName("viewCount")
    private Long viewCount; // Thay đổi từ long thành Long

    @PropertyName("likeCount")
    private Long likeCount; // Thay đổi từ long thành Long

    @PropertyName("userId")
    private String userId;

    @PropertyName("cldPublicId")
    private String cldPublicId;

    @PropertyName("cldVersion")
    private Long cldVersion; // Thay đổi từ long thành Long

    @PropertyName("thumbnailUrl")
    private String thumbnailUrl;

    @PropertyName("thumb")
    private String thumb;

    @PropertyName("status")
    private String status;

    @PropertyName("commentCount")
    private Long commentCount; // Thay đổi từ long thành Long

    @PropertyName("duration")
    private Long duration; // Thay đổi từ long thành Long

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

    public Long getUploadDate() {
        return uploadDate != null ? uploadDate : System.currentTimeMillis();
    }

    /**
     * Setter an toàn để xử lý conversion từ Object sang Long
     */
    public void setUploadDate(Object uploadDate) {
        if (uploadDate == null) {
            this.uploadDate = System.currentTimeMillis();
        } else if (uploadDate instanceof Long) {
            this.uploadDate = (Long) uploadDate;
        } else if (uploadDate instanceof Integer) {
            this.uploadDate = ((Integer) uploadDate).longValue();
        } else if (uploadDate instanceof String) {
            try {
                this.uploadDate = Long.parseLong((String) uploadDate);
            } catch (NumberFormatException e) {
                this.uploadDate = System.currentTimeMillis();
            }
        } else {
            this.uploadDate = System.currentTimeMillis();
        }
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

    public Long getViewCount() {
        return viewCount != null ? viewCount : 0L;
    }

    /**
     * Setter an toàn để xử lý conversion từ Object sang Long
     */
    public void setViewCount(Object viewCount) {
        if (viewCount == null) {
            this.viewCount = 0L;
        } else if (viewCount instanceof Long) {
            this.viewCount = (Long) viewCount;
        } else if (viewCount instanceof Integer) {
            this.viewCount = ((Integer) viewCount).longValue();
        } else if (viewCount instanceof String) {
            try {
                this.viewCount = Long.parseLong((String) viewCount);
            } catch (NumberFormatException e) {
                this.viewCount = 0L;
            }
        } else {
            this.viewCount = 0L;
        }
    }

    public Long getLikeCount() {
        return likeCount != null ? likeCount : 0L;
    }

    /**
     * Setter an toàn để xử lý conversion từ Object sang Long
     */
    public void setLikeCount(Object likeCount) {
        if (likeCount == null) {
            this.likeCount = 0L;
        } else if (likeCount instanceof Long) {
            this.likeCount = (Long) likeCount;
        } else if (likeCount instanceof Integer) {
            this.likeCount = ((Integer) likeCount).longValue();
        } else if (likeCount instanceof String) {
            try {
                this.likeCount = Long.parseLong((String) likeCount);
            } catch (NumberFormatException e) {
                this.likeCount = 0L;
            }
        } else {
            this.likeCount = 0L;
        }
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

    public Long getCldVersion() {
        return cldVersion != null ? cldVersion : 0L;
    }

    /**
     * Setter an toàn để xử lý conversion từ Object sang Long
     */
    public void setCldVersion(Object cldVersion) {
        if (cldVersion == null) {
            this.cldVersion = 0L;
        } else if (cldVersion instanceof Long) {
            this.cldVersion = (Long) cldVersion;
        } else if (cldVersion instanceof Integer) {
            this.cldVersion = ((Integer) cldVersion).longValue();
        } else if (cldVersion instanceof String) {
            try {
                this.cldVersion = Long.parseLong((String) cldVersion);
            } catch (NumberFormatException e) {
                this.cldVersion = 0L;
            }
        } else {
            this.cldVersion = 0L;
        }
    }

    public String getThumbnailUrl() {
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

    public Long getCommentCount() {
        return commentCount != null ? commentCount : 0L;
    }

    /**
     * Setter an toàn để xử lý conversion từ Object sang Long
     */
    public void setCommentCount(Object commentCount) {
        if (commentCount == null) {
            this.commentCount = 0L;
        } else if (commentCount instanceof Long) {
            this.commentCount = (Long) commentCount;
        } else if (commentCount instanceof Integer) {
            this.commentCount = ((Integer) commentCount).longValue();
        } else if (commentCount instanceof String) {
            try {
                this.commentCount = Long.parseLong((String) commentCount);
            } catch (NumberFormatException e) {
                this.commentCount = 0L;
            }
        } else {
            this.commentCount = 0L;
        }
    }

    public Long getDuration() {
        return duration != null ? duration : 0L;
    }

    /**
     * Setter an toàn để xử lý conversion từ Object sang Long
     */
    public void setDuration(Object duration) {
        if (duration == null) {
            this.duration = 0L;
        } else if (duration instanceof Long) {
            this.duration = (Long) duration;
        } else if (duration instanceof Integer) {
            this.duration = ((Integer) duration).longValue();
        } else if (duration instanceof String) {
            try {
                this.duration = Long.parseLong((String) duration);
            } catch (NumberFormatException e) {
                this.duration = 0L;
            }
        } else {
            this.duration = 0L;
        }
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    // Additional methods for video URL generation
    public String getVideoUrl() {
        if (cldPublicId != null) {
            return "https://res.cloudinary.com/healthtips/video/upload/" + cldPublicId;
        }
        return null;
    }

    public String getOptimizedVideoUrl() {
        if (cldPublicId != null) {
            return "https://res.cloudinary.com/healthtips/video/upload/q_auto,f_auto/" + cldPublicId;
        }
        return null;
    }

    public String getThumbnailUrlFromCloudinary() {
        if (cldPublicId != null) {
            return "https://res.cloudinary.com/healthtips/video/upload/so_0,w_300,h_200,c_fill/" + cldPublicId + ".jpg";
        }
        return thumbnailUrl;
    }

    @Override
    public String toString() {
        return "ShortVideo{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", caption='" + caption + '\'' +
                ", uploadDate=" + uploadDate +
                ", viewCount=" + viewCount +
                ", likeCount=" + likeCount +
                ", duration=" + duration +
                '}';
    }
}
