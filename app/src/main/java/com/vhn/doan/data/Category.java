package com.vhn.doan.data;

/**
 * Class đại diện cho một danh mục mẹo sức khỏe
 */
public class Category {
    private String id;
    private String name;
    private String description;
    private String iconUrl;
    private String imageUrl; // Thêm trường imageUrl để tương thích với dữ liệu test
    private boolean active;
    private long createdAt; // Thêm field createdAt
    private int tipCount; // Thêm field tipCount để phù hợp với data test

    /**
     * Constructor rỗng cho Firebase
     */
    public Category() {
        // Constructor rỗng cần thiết cho Firebase Realtime Database
        this.createdAt = System.currentTimeMillis();
        this.active = true;
        this.tipCount = 0;
    }

    /**
     * Constructor đầy đủ
     * @param id ID của danh mục
     * @param name Tên danh mục
     * @param description Mô tả danh mục
     * @param iconUrl URL hình ảnh đại diện
     * @param active Trạng thái hoạt động của danh mục
     */
    public Category(String id, String name, String description, String iconUrl, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.imageUrl = iconUrl; // Đồng bộ cả hai trường
        this.active = active;
        this.createdAt = System.currentTimeMillis();
        this.tipCount = 0;
    }

    /**
     * Constructor với createdAt và tipCount
     */
    public Category(String id, String name, String description, String iconUrl, boolean active, long createdAt, int tipCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.imageUrl = iconUrl; // Đồng bộ cả hai trường
        this.active = active;
        this.createdAt = createdAt;
        this.tipCount = tipCount;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        // Ưu tiên imageUrl nếu có, sau đó mới đến iconUrl
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return imageUrl;
        }
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        // Đồng bộ với imageUrl nếu imageUrl chưa được set
        if (this.imageUrl == null || this.imageUrl.isEmpty()) {
            this.imageUrl = iconUrl;
        }
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        // Đồng bộ với iconUrl nếu iconUrl chưa được set
        if (this.iconUrl == null || this.iconUrl.isEmpty()) {
            this.iconUrl = imageUrl;
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getTipCount() {
        return tipCount;
    }

    public void setTipCount(int tipCount) {
        this.tipCount = tipCount;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", tipCount=" + tipCount +
                '}';
    }
}
