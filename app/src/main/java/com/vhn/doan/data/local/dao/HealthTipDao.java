package com.vhn.doan.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.vhn.doan.data.local.entity.HealthTipEntity;

import java.util.List;

/**
 * DAO cho HealthTip - Data Access Object
 * Sử dụng LiveData để observe changes tự động
 */
@Dao
public interface HealthTipDao {

    /**
     * Insert hoặc replace health tip
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HealthTipEntity healthTip);

    /**
     * Insert multiple health tips
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<HealthTipEntity> healthTips);

    /**
     * Update health tip
     */
    @Update
    void update(HealthTipEntity healthTip);

    /**
     * Delete health tip
     */
    @Delete
    void delete(HealthTipEntity healthTip);

    /**
     * Lấy tất cả health tips (LiveData - tự động update UI)
     */
    @Query("SELECT * FROM health_tips ORDER BY created_at DESC")
    LiveData<List<HealthTipEntity>> getAllHealthTips();

    /**
     * Lấy health tips theo category
     */
    @Query("SELECT * FROM health_tips WHERE category_id = :categoryId ORDER BY created_at DESC")
    LiveData<List<HealthTipEntity>> getHealthTipsByCategory(String categoryId);

    /**
     * Lấy health tips được recommend (sorted by score)
     */
    @Query("SELECT * FROM health_tips ORDER BY recommendation_score DESC LIMIT :limit")
    LiveData<List<HealthTipEntity>> getRecommendedHealthTips(int limit);

    /**
     * Lấy health tips được yêu thích
     */
    @Query("SELECT * FROM health_tips WHERE is_favorite = 1 ORDER BY created_at DESC")
    LiveData<List<HealthTipEntity>> getFavoriteHealthTips();

    /**
     * Lấy health tips được like
     */
    @Query("SELECT * FROM health_tips WHERE is_liked = 1 ORDER BY created_at DESC")
    LiveData<List<HealthTipEntity>> getLikedHealthTips();

    /**
     * Lấy health tips xem nhiều nhất
     */
    @Query("SELECT * FROM health_tips ORDER BY view_count DESC LIMIT :limit")
    LiveData<List<HealthTipEntity>> getMostViewedHealthTips(int limit);

    /**
     * Lấy health tips thích nhiều nhất
     */
    @Query("SELECT * FROM health_tips ORDER BY like_count DESC LIMIT :limit")
    LiveData<List<HealthTipEntity>> getMostLikedHealthTips(int limit);

    /**
     * Lấy health tip theo ID
     */
    @Query("SELECT * FROM health_tips WHERE id = :id LIMIT 1")
    LiveData<HealthTipEntity> getHealthTipById(String id);

    /**
     * Lấy health tip theo ID (synchronous - không phải LiveData)
     */
    @Query("SELECT * FROM health_tips WHERE id = :id LIMIT 1")
    HealthTipEntity getHealthTipByIdSync(String id);

    /**
     * Tìm kiếm health tips theo title
     */
    @Query("SELECT * FROM health_tips WHERE title LIKE '%' || :query || '%' ORDER BY created_at DESC")
    LiveData<List<HealthTipEntity>> searchHealthTips(String query);

    /**
     * Đếm số lượng health tips
     */
    @Query("SELECT COUNT(*) FROM health_tips")
    LiveData<Integer> getHealthTipCount();

    /**
     * Xóa health tips cũ (cache cleanup)
     * Xóa những tip đã cache quá 7 ngày
     */
    @Query("DELETE FROM health_tips WHERE cached_at < :timestamp")
    void deleteOldHealthTips(long timestamp);

    /**
     * Xóa tất cả health tips
     */
    @Query("DELETE FROM health_tips")
    void deleteAll();

    /**
     * Update favorite status
     */
    @Query("UPDATE health_tips SET is_favorite = :isFavorite WHERE id = :id")
    void updateFavoriteStatus(String id, boolean isFavorite);

    /**
     * Update like status
     */
    @Query("UPDATE health_tips SET is_liked = :isLiked WHERE id = :id")
    void updateLikeStatus(String id, boolean isLiked);

    /**
     * Update view count
     */
    @Query("UPDATE health_tips SET view_count = :viewCount WHERE id = :id")
    void updateViewCount(String id, int viewCount);

    /**
     * Update like count
     */
    @Query("UPDATE health_tips SET like_count = :likeCount WHERE id = :id")
    void updateLikeCount(String id, int likeCount);
}
