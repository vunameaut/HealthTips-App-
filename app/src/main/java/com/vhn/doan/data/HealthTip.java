package com.vhn.doan.data;

import com.vhn.doan.services.CloudinaryVideoHelper;

/**
 * Class đại diện cho một mẹo sức khỏe
 * Hỗ trợ load ảnh từ Cloudinary với tối ưu hóa cho mobile
 */
public class HealthTip {
    private String id;
    private String title;
    private String content;
    private String categoryId;
    private String categoryName; // Thêm field cho tên category
    private int viewCount;
    private int likeCount;
    private String imageUrl;
    private long createdAt;
    private boolean isFavorite;
    private boolean isLiked; // Thêm field cho trạng thái like

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
        this.isLiked = false;
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
        this.isLiked = false;
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

    // Thêm getter/setter cho categoryName
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

    /**
     * Lấy nội dung tóm tắt của mẹo sức khỏe (100 ký tự đầu)
     * @return nội dung tóm tắt
     */
    public String getSummary() {
        if (content == null || content.trim().isEmpty()) {
            return "Nội dung đang được cập nhật...";
        }
        if (content.length() <= 100) {
            return content;
        }
        return content.substring(0, 100) + "...";
    }

    /**
     * Kiểm tra xem HealthTip có hợp lệ không
     * @return true nếu có đủ thông tin cơ bản
     */
    public boolean isValid() {
        return id != null && !id.isEmpty() &&
               title != null && !title.isEmpty() &&
               content != null && !content.isEmpty();
    }

    /**
     * Kiểm tra xem ảnh có sử dụng Cloudinary không
     * @return true nếu image URL từ Cloudinary
     */
    public boolean isCloudinaryImage() {
        return CloudinaryVideoHelper.isCloudinaryImageUrl(this.imageUrl);
    }

    /**
     * Lấy optimized image URL cho mobile
     * @return URL ảnh được tối ưu cho mobile
     */
    public String getOptimizedImageUrl() {
        if (isCloudinaryImage()) {
            // Tạo URL tối ưu cho ảnh mobile
            return getCloudinaryOptimizedImageUrl(this.imageUrl, 800, 600);
        }
        return this.imageUrl; // Trả về URL gốc nếu không phải Cloudinary
    }

    /**
     * Lấy thumbnail URL cho danh sách
     * @return URL thumbnail nhỏ để hiển thị trong danh sách
     */
    public String getThumbnailUrl() {
        if (isCloudinaryImage()) {
            // Tạo thumbnail nhỏ cho danh sách
            return getCloudinaryOptimizedImageUrl(this.imageUrl, 300, 200);
        }
        return this.imageUrl; // Trả về URL gốc nếu không phải Cloudinary
    }

    /**
     * Tạo URL ảnh Cloudinary được tối ưu
     * @param originalUrl URL gốc
     * @param width Chiều rộng mong muốn
     * @param height Chiều cao mong muốn
     * @return URL ảnh được tối ưu
     */
    private String getCloudinaryOptimizedImageUrl(String originalUrl, int width, int height) {
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            return "";
        }

        try {
            // Tìm vị trí của "/upload/" để chèn transformations
            String uploadMarker = "/upload/";
            int uploadIndex = originalUrl.indexOf(uploadMarker);

            if (uploadIndex != -1) {
                String beforeUpload = originalUrl.substring(0, uploadIndex + uploadMarker.length());
                String afterUpload = originalUrl.substring(uploadIndex + uploadMarker.length());

                // Loại bỏ transformations cũ nếu có
                if (afterUpload.matches("^[a-z]_.*")) {
                    int firstSlash = afterUpload.indexOf('/');
                    if (firstSlash != -1) {
                        afterUpload = afterUpload.substring(firstSlash + 1);
                    }
                }

                // Tạo URL với optimization cho mobile
                String optimizedUrl =
                    beforeUpload +
                    "w_" + width + ",h_" + height + ",c_fill,f_auto,q_auto:good/" + afterUpload;

                return optimizedUrl;
            }
        } catch (Exception e) {
            android.util.Log.e("HealthTip", "Lỗi khi tối ưu ảnh URL: " + e.getMessage());
        }

        return originalUrl; // Trả về URL gốc nếu có lỗi
    }

    @Override
    public String toString() {
        return "HealthTip{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", viewCount=" + viewCount +
                ", likeCount=" + likeCount +
                ", imageUrl='" + imageUrl + '\'' +
                ", createdAt=" + createdAt +
                ", isFavorite=" + isFavorite +
                ", isLiked=" + isLiked +
                '}';
    }
}
