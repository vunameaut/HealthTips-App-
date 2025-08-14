package com.vhn.doan.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;
import com.vhn.doan.services.CloudinaryUrlBuilder;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class để đại diện cho video ngắn trong ứng dụng
 * Được sử dụng với Firebase Realtime Database
 * Video URLs sẽ đến từ Cloudinary thay vì Firebase Storage
 */
public class ShortVideo implements Parcelable {
    @PropertyName("id")
    private String id;

    @PropertyName("title")
    private String title;

    @PropertyName("caption")
    private String caption;

    @PropertyName("uploadDate")
    private long uploadDate;

    // Cloudinary identifiers
    @PropertyName("cldPublicId")
    private String cldPublicId;

    @PropertyName("cldVersion")
    private long cldVersion;

    // Legacy field kept only for backward compatibility
    @PropertyName("videoUrl_deprecated")
    private String videoUrlDeprecated;

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

    @Exclude
    private boolean likedByCurrentUser;

    // Constructor mặc định cần thiết cho Firebase
    public ShortVideo() {
    }

    // Constructor cập nhật sử dụng Cloudinary
    public ShortVideo(String id, String title, String caption, long uploadDate,
                     String cldPublicId, long cldVersion, String thumbnailUrl, String categoryId,
                     Map<String, Boolean> tags, int viewCount, int likeCount, String userId) {
        this.id = id;
        this.title = title;
        this.caption = caption;
        this.uploadDate = uploadDate;
        this.cldPublicId = cldPublicId;
        this.cldVersion = cldVersion;
        this.thumbnailUrl = thumbnailUrl;
        this.categoryId = categoryId;
        this.tags = tags;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.userId = userId;
    }

    // Constructor cho Parcelable
    protected ShortVideo(Parcel in) {
        id = in.readString();
        title = in.readString();
        caption = in.readString();
        uploadDate = in.readLong();
        cldPublicId = in.readString();
        cldVersion = in.readLong();
        videoUrlDeprecated = in.readString();
        thumbnailUrl = in.readString();
        categoryId = in.readString();
        viewCount = in.readInt();
        likeCount = in.readInt();
        userId = in.readString();
        likedByCurrentUser = in.readByte() != 0;

        // Đọc tags
        int tagsSize = in.readInt();
        if (tagsSize > 0) {
            tags = new HashMap<>();
            for (int i = 0; i < tagsSize; i++) {
                String key = in.readString();
                boolean value = in.readByte() != 0;
                tags.put(key, value);
            }
        }
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

    public String getVideoUrlDeprecated() {
        return videoUrlDeprecated;
    }

    public void setVideoUrlDeprecated(String videoUrlDeprecated) {
        this.videoUrlDeprecated = videoUrlDeprecated;
    }

    public String getThumbnailUrl() {
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            return thumbnailUrl;
        }
        return CloudinaryUrlBuilder.posterUrlFrom(cldPublicId, cldVersion);
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

    @Exclude
    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    @Exclude
    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }

    /**
     * Kiểm tra xem video có sử dụng Cloudinary không
     * @return true nếu video URL từ Cloudinary
     */
    public boolean isCloudinaryVideo() {
        return cldPublicId != null && !cldPublicId.isEmpty();
    }

    /**
     * Lấy optimized video URL cho mobile
     * @return URL video được tối ưu cho mobile
     */
    public String getOptimizedVideoUrl() {
        return CloudinaryUrlBuilder.mp4UrlFrom(cldPublicId, cldVersion);
    }

    public String getPosterUrl() {
        return CloudinaryUrlBuilder.posterUrlFrom(cldPublicId, cldVersion);
    }

    // Parcelable implementation
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(caption);
        dest.writeLong(uploadDate);
        dest.writeString(cldPublicId);
        dest.writeLong(cldVersion);
        dest.writeString(videoUrlDeprecated);
        dest.writeString(thumbnailUrl);
        dest.writeString(categoryId);
        dest.writeInt(viewCount);
        dest.writeInt(likeCount);
        dest.writeString(userId);
        dest.writeByte((byte) (likedByCurrentUser ? 1 : 0));

        // Ghi tags
        if (tags != null) {
            dest.writeInt(tags.size());
            for (Map.Entry<String, Boolean> entry : tags.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeByte((byte) (entry.getValue() ? 1 : 0));
            }
        } else {
            dest.writeInt(0);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ShortVideo> CREATOR = new Creator<ShortVideo>() {
        @Override
        public ShortVideo createFromParcel(Parcel in) {
            return new ShortVideo(in);
        }

        @Override
        public ShortVideo[] newArray(int size) {
            return new ShortVideo[size];
        }
    };

    @Override
    public String toString() {
        return "ShortVideo{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", caption='" + caption + '\'' +
                ", uploadDate=" + uploadDate +
                ", cldPublicId='" + cldPublicId + '\'' +
                ", cldVersion=" + cldVersion +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", tags=" + tags +
                ", viewCount=" + viewCount +
                ", likeCount=" + likeCount +
                ", userId='" + userId + '\'' +
                '}';
    }
}
