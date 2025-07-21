package com.vhn.doan.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Model User đại diện cho thông tin người dùng trong ứng dụng
 * Được lưu trữ trong Firebase Realtime Database theo cấu trúc users/{uid}/
 */
public class User {
    private String uid;
    private String email;
    private String displayName;
    private String photoUrl;
    private String phoneNumber;
    private long createdAt;
    private long lastLoginAt;
    private boolean isActive;
    private Map<String, Object> preferences;
    private Map<String, Boolean> favoriteHealthTips;
    private Map<String, Boolean> likedHealthTips;

    /**
     * Constructor mặc định cần thiết cho Firebase
     */
    public User() {
        this.preferences = new HashMap<>();
        this.favoriteHealthTips = new HashMap<>();
        this.likedHealthTips = new HashMap<>();
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.lastLoginAt = System.currentTimeMillis();
    }

    /**
     * Constructor với các thông tin cơ bản
     */
    public User(String uid, String email, String displayName) {
        this();
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
    }

    // Getters và Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(long lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Map<String, Object> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, Object> preferences) {
        this.preferences = preferences;
    }

    public Map<String, Boolean> getFavoriteHealthTips() {
        return favoriteHealthTips;
    }

    public void setFavoriteHealthTips(Map<String, Boolean> favoriteHealthTips) {
        this.favoriteHealthTips = favoriteHealthTips;
    }

    public Map<String, Boolean> getLikedHealthTips() {
        return likedHealthTips;
    }

    public void setLikedHealthTips(Map<String, Boolean> likedHealthTips) {
        this.likedHealthTips = likedHealthTips;
    }

    /**
     * Thêm bài viết vào danh sách yêu thích
     */
    public void addFavoriteHealthTip(String healthTipId) {
        if (favoriteHealthTips == null) {
            favoriteHealthTips = new HashMap<>();
        }
        favoriteHealthTips.put(healthTipId, true);
    }

    /**
     * Xóa bài viết khỏi danh sách yêu thích
     */
    public void removeFavoriteHealthTip(String healthTipId) {
        if (favoriteHealthTips != null) {
            favoriteHealthTips.remove(healthTipId);
        }
    }

    /**
     * Thêm bài viết vào danh sách đã thích
     */
    public void addLikedHealthTip(String healthTipId) {
        if (likedHealthTips == null) {
            likedHealthTips = new HashMap<>();
        }
        likedHealthTips.put(healthTipId, true);
    }

    /**
     * Xóa bài viết khỏi danh sách đã thích
     */
    public void removeLikedHealthTip(String healthTipId) {
        if (likedHealthTips != null) {
            likedHealthTips.remove(healthTipId);
        }
    }

    /**
     * Cập nhật thời gian đăng nhập cuối
     */
    public void updateLastLogin() {
        this.lastLoginAt = System.currentTimeMillis();
    }
}
