package com.vhn.doan.data;

import java.util.Date;

/**
 * Model đại diện cho một mẹo sức khỏe được yêu thích
 * Tuân theo mô hình MVP và Firebase Realtime Database structure
 */
public class Favorite {
    private String id;
    private String userId;
    private String healthTipId;
    private Date createdAt;
    private Date updatedAt;

    /**
     * Constructor mặc định cho Firebase
     */
    public Favorite() {
        // Required empty constructor for Firebase
    }

    /**
     * Constructor đầy đủ
     * @param id ID của favorite
     * @param userId ID của người dùng
     * @param healthTipId ID của mẹo sức khỏe được yêu thích
     * @param createdAt Thời gian tạo
     * @param updatedAt Thời gian cập nhật
     */
    public Favorite(String id, String userId, String healthTipId, Date createdAt, Date updatedAt) {
        this.id = id;
        this.userId = userId;
        this.healthTipId = healthTipId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Constructor để tạo favorite mới
     * @param userId ID của người dùng
     * @param healthTipId ID của mẹo sức khỏe được yêu thích
     */
    public Favorite(String userId, String healthTipId) {
        this.userId = userId;
        this.healthTipId = healthTipId;
        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getHealthTipId() {
        return healthTipId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setHealthTipId(String healthTipId) {
        this.healthTipId = healthTipId;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Favorite favorite = (Favorite) obj;
        return id != null ? id.equals(favorite.id) : favorite.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Favorite{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", healthTipId='" + healthTipId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
