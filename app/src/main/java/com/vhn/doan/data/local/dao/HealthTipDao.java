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
     * @deprecated Sử dụng getAllHealthTipsLimited() để tối ưu performance
     */
    @Query("SELECT * FROM health_tips ORDER BY created_at DESC")
    LiveData<List<HealthTipEntity>> getAllHealthTips();

    /**
     * Lấy health tips với giới hạn số lượng (LiveData - OPTIMIZED)
     * @param limit Số lượng items tối đa
     */
    @Query("SELECT * FROM health_tips ORDER BY created_at DESC LIMIT :limit")
    LiveData<List<HealthTipEntity>> getAllHealthTipsLimited(int limit);

    /**
     * Lấy tất cả health tips (synchronous - không phải LiveData)
     * Dùng cho offline-first strategy
     * @deprecated Sử dụng getAllHealthTipsSyncLimited() để tối ưu performance
     */
    @Query("SELECT * FROM health_tips ORDER BY created_at DESC")
    List<HealthTipEntity> getAllHealthTipsSync();

    /**
     * Lấy health tips với giới hạn số lượng (synchronous - OPTIMIZED)
     * @param limit Số lượng items tối đa
     */
    @Query("SELECT * FROM health_tips ORDER BY created_at DESC LIMIT :limit")
    List<HealthTipEntity> getAllHealthTipsSyncLimited(int limit);

    /**
     * Lấy health tips theo category
     * @deprecated Sử dụng getHealthTipsByCategoryLimited() để tối ưu performance
     */
    @Query("SELECT * FROM health_tips WHERE category_id = :categoryId ORDER BY created_at DESC")
    LiveData<List<HealthTipEntity>> getHealthTipsByCategory(String categoryId);

    /**
     * Lấy health tips theo category với giới hạn (OPTIMIZED)
     * @param categoryId ID của category
     * @param limit Số lượng items tối đa
     */
    @Query("SELECT * FROM health_tips WHERE category_id = :categoryId ORDER BY created_at DESC LIMIT :limit")
    LiveData<List<HealthTipEntity>> getHealthTipsByCategoryLimited(String categoryId, int limit);

    /**
     * Lấy health tips theo category (synchronous)
     * @deprecated Sử dụng getHealthTipsByCategorySyncLimited() để tối ưu performance
     */
    @Query("SELECT * FROM health_tips WHERE category_id = :categoryId ORDER BY created_at DESC")
    List<HealthTipEntity> getHealthTipsByCategorySync(String categoryId);

    /**
     * Lấy health tips theo category với giới hạn (synchronous - OPTIMIZED)
     * @param categoryId ID của category
     * @param limit Số lượng items tối đa
     */
    @Query("SELECT * FROM health_tips WHERE category_id = :categoryId ORDER BY created_at DESC LIMIT :limit")
    List<HealthTipEntity> getHealthTipsByCategorySyncLimited(String categoryId, int limit);

    /**
     * Lấy health tips được recommend (sorted by score)
     */
    @Query("SELECT * FROM health_tips ORDER BY recommendation_score DESC LIMIT :limit")
    LiveData<List<HealthTipEntity>> getRecommendedHealthTips(int limit);

    /**
     * Lấy health tips được yêu thích
     * @deprecated Sử dụng getFavoriteHealthTipsLimited() để tối ưu performance
     */
    @Query("SELECT * FROM health_tips WHERE is_favorite = 1 ORDER BY created_at DESC")
    LiveData<List<HealthTipEntity>> getFavoriteHealthTips();

    /**
     * Lấy health tips được yêu thích với giới hạn (OPTIMIZED)
     * @param limit Số lượng items tối đa
     */
    @Query("SELECT * FROM health_tips WHERE is_favorite = 1 ORDER BY created_at DESC LIMIT :limit")
    LiveData<List<HealthTipEntity>> getFavoriteHealthTipsLimited(int limit);

    /**
     * Lấy health tips được like
     * @deprecated Sử dụng getLikedHealthTipsLimited() để tối ưu performance
     */
    @Query("SELECT * FROM health_tips WHERE is_liked = 1 ORDER BY created_at DESC")
    LiveData<List<HealthTipEntity>> getLikedHealthTips();

    /**
     * Lấy health tips được like với giới hạn (OPTIMIZED)
     * @param limit Số lượng items tối đa
     */
    @Query("SELECT * FROM health_tips WHERE is_liked = 1 ORDER BY created_at DESC LIMIT :limit")
    LiveData<List<HealthTipEntity>> getLikedHealthTipsLimited(int limit);

    /**
     * Lấy health tips xem nhiều nhất
     */
    @Query("SELECT * FROM health_tips ORDER BY view_count DESC LIMIT :limit")
    LiveData<List<HealthTipEntity>> getMostViewedHealthTips(int limit);

    /**
     * Lấy health tips xem nhiều nhất (synchronous)
     */
    @Query("SELECT * FROM health_tips ORDER BY view_count DESC LIMIT :limit")
    List<HealthTipEntity> getMostViewedHealthTipsSync(int limit);

    /**
     * Lấy health tips mới nhất (synchronous)
     */
    @Query("SELECT * FROM health_tips ORDER BY created_at DESC LIMIT :limit")
    List<HealthTipEntity> getLatestHealthTipsSync(int limit);

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
     * @deprecated Sử dụng searchHealthTipsLimited() để tối ưu performance
     */
    @Query("SELECT * FROM health_tips WHERE title LIKE '%' || :query || '%' ORDER BY created_at DESC")
    LiveData<List<HealthTipEntity>> searchHealthTips(String query);

    /**
     * Tìm kiếm health tips theo title với giới hạn (OPTIMIZED)
     * @param query Từ khóa tìm kiếm
     * @param limit Số lượng kết quả tối đa
     */
    @Query("SELECT * FROM health_tips WHERE title LIKE '%' || :query || '%' ORDER BY created_at DESC LIMIT :limit")
    LiveData<List<HealthTipEntity>> searchHealthTipsLimited(String query, int limit);

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

    /**
     * Đếm số lượng health tips trong cache (synchronous)
     * Dùng cho CacheManager
     */
    @Query("SELECT COUNT(*) FROM health_tips")
    int getHealthTipCountSync();

    /**
     * Xóa N items cũ nhất (LRU cleanup)
     * Sử dụng cached_at để xác định items cũ nhất
     */
    @Query("DELETE FROM health_tips WHERE id IN (SELECT id FROM health_tips ORDER BY cached_at ASC LIMIT :count)")
    void deleteOldestItems(int count);

    /**
     * Update cached_at timestamp (cho LRU tracking)
     * Gọi khi user xem/access một tip
     */
    @Query("UPDATE health_tips SET cached_at = :timestamp WHERE id = :id")
    void updateCachedAt(String id, long timestamp);
}
