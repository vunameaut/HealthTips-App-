package com.vhn.doan.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.vhn.doan.data.local.entity.VideoEntity;

import java.util.List;

/**
 * DAO cho Video
 */
@Dao
public interface VideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(VideoEntity video);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<VideoEntity> videos);

    @Update
    void update(VideoEntity video);

    @Delete
    void delete(VideoEntity video);

    /**
     * Lấy tất cả videos (sorted by upload_date DESC)
     */
    @Query("SELECT * FROM videos ORDER BY upload_date DESC")
    LiveData<List<VideoEntity>> getAllVideos();

    /**
     * Lấy videos được like
     */
    @Query("SELECT * FROM videos WHERE is_liked = 1 ORDER BY upload_date DESC")
    LiveData<List<VideoEntity>> getLikedVideos();

    /**
     * Lấy videos trending (sorted by like_count)
     */
    @Query("SELECT * FROM videos ORDER BY like_count DESC LIMIT :limit")
    LiveData<List<VideoEntity>> getTrendingVideos(int limit);

    /**
     * Lấy video theo ID
     */
    @Query("SELECT * FROM videos WHERE id = :id LIMIT 1")
    LiveData<VideoEntity> getVideoById(String id);

    /**
     * Lấy video theo ID (synchronous)
     */
    @Query("SELECT * FROM videos WHERE id = :id LIMIT 1")
    VideoEntity getVideoByIdSync(String id);

    /**
     * Tìm kiếm videos
     */
    @Query("SELECT * FROM videos WHERE title LIKE '%' || :query || '%' OR caption LIKE '%' || :query || '%' ORDER BY upload_date DESC")
    LiveData<List<VideoEntity>> searchVideos(String query);

    /**
     * Lấy videos theo uploader
     */
    @Query("SELECT * FROM videos WHERE uploader_id = :uploaderId ORDER BY upload_date DESC")
    LiveData<List<VideoEntity>> getVideosByUploader(String uploaderId);

    /**
     * Đếm số videos
     */
    @Query("SELECT COUNT(*) FROM videos")
    LiveData<Integer> getVideoCount();

    /**
     * Xóa videos cũ (cache cleanup)
     */
    @Query("DELETE FROM videos WHERE cached_at < :timestamp")
    void deleteOldVideos(long timestamp);

    /**
     * Xóa tất cả
     */
    @Query("DELETE FROM videos")
    void deleteAll();

    /**
     * Update like status
     */
    @Query("UPDATE videos SET is_liked = :isLiked WHERE id = :id")
    void updateLikeStatus(String id, boolean isLiked);

    /**
     * Update view count
     */
    @Query("UPDATE videos SET view_count = :viewCount WHERE id = :id")
    void updateViewCount(String id, long viewCount);

    /**
     * Update like count
     */
    @Query("UPDATE videos SET like_count = :likeCount WHERE id = :id")
    void updateLikeCount(String id, long likeCount);
}
