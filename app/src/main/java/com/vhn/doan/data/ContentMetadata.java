package com.vhn.doan.data;

/**
 * Class đại diện cho metadata của một ContentBlock
 * Chứa các thông tin bổ sung cho các loại nội dung khác nhau
 */
public class ContentMetadata {
    private Integer level; // Cấp độ heading (1-6)
    private String alt;    // Alt text cho ảnh
    private String caption; // Caption cho ảnh

    /**
     * Constructor rỗng cho Firebase
     */
    public ContentMetadata() {
        // Constructor rỗng cần thiết cho Firebase Realtime Database
    }

    /**
     * Constructor đầy đủ
     * @param level Cấp độ heading (1-6)
     * @param alt Alt text cho ảnh
     * @param caption Caption cho ảnh
     */
    public ContentMetadata(Integer level, String alt, String caption) {
        this.level = level;
        this.alt = alt;
        this.caption = caption;
    }

    // Getters và Setters

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    @Override
    public String toString() {
        return "ContentMetadata{" +
                "level=" + level +
                ", alt='" + alt + '\'' +
                ", caption='" + caption + '\'' +
                '}';
    }
}
