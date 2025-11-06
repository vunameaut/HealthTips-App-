package com.vhn.doan.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.vhn.doan.data.local.entity.CategoryEntity;

import java.util.List;

/**
 * DAO cho Category
 */
@Dao
public interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CategoryEntity category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CategoryEntity> categories);

    @Update
    void update(CategoryEntity category);

    @Delete
    void delete(CategoryEntity category);

    /**
     * Lấy tất cả categories (sorted by order_index)
     */
    @Query("SELECT * FROM categories ORDER BY order_index ASC")
    LiveData<List<CategoryEntity>> getAllCategories();

    /**
     * Lấy category theo ID
     */
    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    LiveData<CategoryEntity> getCategoryById(String id);

    /**
     * Lấy category theo ID (synchronous)
     */
    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    CategoryEntity getCategoryByIdSync(String id);

    /**
     * Tìm kiếm categories
     */
    @Query("SELECT * FROM categories WHERE name LIKE '%' || :query || '%' ORDER BY order_index ASC")
    LiveData<List<CategoryEntity>> searchCategories(String query);

    /**
     * Đếm số categories
     */
    @Query("SELECT COUNT(*) FROM categories")
    LiveData<Integer> getCategoryCount();

    /**
     * Xóa categories cũ
     */
    @Query("DELETE FROM categories WHERE cached_at < :timestamp")
    void deleteOldCategories(long timestamp);

    /**
     * Xóa tất cả
     */
    @Query("DELETE FROM categories")
    void deleteAll();
}
