package com.vhn.doan.data;

/**
 * Class đại diện cho một danh mục mẹo sức khỏe
 */
public class Category {
    private String id;
    private String name;
    private String description;
    private String iconUrl;
    private boolean active;

    /**
     * Constructor rỗng cho Firebase
     */
    public Category() {
        // Constructor rỗng cần thiết cho Firebase Realtime Database
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
        this.active = active;
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
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", active=" + active +
                '}';
    }
}
