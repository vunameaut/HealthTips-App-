package com.vhn.doan.data.local;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.database.StandaloneDatabaseProvider;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;

/**
 * VideoCacheManager - Quản lý cache video cho ExoPlayer
 *
 * Sử dụng ExoPlayer's SimpleCache để:
 * 1. Cache video khi online
 * 2. Play video từ cache khi offline
 * 3. Tự động cleanup theo LRU
 *
 * Giống TikTok: Video đã xem có thể play lại khi offline!
 */
public class VideoCacheManager {

    private static final String TAG = "VideoCacheManager";

    // Cấu hình cache
    private static final long MAX_CACHE_SIZE = 500 * 1024 * 1024; // 500 MB
    private static final String CACHE_DIR_NAME = "exoplayer_video_cache";

    private static VideoCacheManager instance;
    private Cache videoCache;
    private File cacheDirectory; // 🎯 Lưu reference để dùng sau

    private VideoCacheManager(Context context) {
        try {
            cacheDirectory = new File(context.getCacheDir(), CACHE_DIR_NAME);

            // Tạo SimpleCache với:
            // - LRU evictor: Tự động xóa video cũ khi hết dung lượng
            // - StandaloneDatabaseProvider: Quản lý metadata
            LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE);
            StandaloneDatabaseProvider databaseProvider = new StandaloneDatabaseProvider(context);

            videoCache = new SimpleCache(
                cacheDirectory,
                evictor,
                databaseProvider
            );

            Log.d(TAG, "✅ Video cache initialized - Max size: " + (MAX_CACHE_SIZE / 1024 / 1024) + " MB");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error initializing video cache", e);
        }
    }

    public static synchronized VideoCacheManager getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new VideoCacheManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Lấy Cache instance cho ExoPlayer
     * ExoPlayer sẽ tự động cache videos khi playback
     */
    public Cache getCache() {
        return videoCache;
    }

    /**
     * Kiểm tra xem video đã được cache chưa
     */
    public boolean isVideoCached(String videoUrl) {
        if (videoCache == null || videoUrl == null) {
            return false;
        }

        try {
            // Check xem có cached spans cho URL này không
            long cachedBytes = videoCache.getCachedBytes(videoUrl, 0, Long.MAX_VALUE);
            boolean isCached = cachedBytes > 0;

            if (isCached) {
                Log.d(TAG, "✅ Video cached: " + videoUrl + " (" + (cachedBytes / 1024) + " KB)");
            }

            return isCached;

        } catch (Exception e) {
            Log.e(TAG, "Error checking cache for: " + videoUrl, e);
            return false;
        }
    }

    /**
     * Lấy thông tin cache stats
     */
    public CacheStats getCacheStats() {
        if (videoCache == null) {
            return new CacheStats(0, 0, 0);
        }

        try {
            long currentSize = videoCache.getCacheSpace();
            int fileCount = 0; // SimpleCache không expose file count trực tiếp

            return new CacheStats(
                currentSize,
                MAX_CACHE_SIZE,
                fileCount
            );

        } catch (Exception e) {
            Log.e(TAG, "Error getting cache stats", e);
            return new CacheStats(0, MAX_CACHE_SIZE, 0);
        }
    }

    /**
     * Xóa toàn bộ cache
     */
    public void clearCache() {
        if (videoCache != null) {
            try {
                // Release cache trước
                videoCache.release();

                // Xóa cache directory (sử dụng field thay vì method không tồn tại)
                if (cacheDirectory != null && cacheDirectory.exists()) {
                    deleteDirectory(cacheDirectory);
                }

                Log.d(TAG, "🗑️ Video cache cleared");

            } catch (Exception e) {
                Log.e(TAG, "Error clearing cache", e);
            }
        }
    }

    /**
     * Release cache (gọi khi app destroy)
     */
    public void release() {
        if (videoCache != null) {
            try {
                videoCache.release();
                Log.d(TAG, "🔓 Video cache released");
            } catch (Exception e) {
                Log.e(TAG, "Error releasing cache", e);
            }
        }
    }

    // Helper method để xóa directory
    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }

    /**
     * Cache stats data class
     */
    public static class CacheStats {
        public final long currentSize;
        public final long maxSize;
        public final int fileCount;

        public CacheStats(long currentSize, long maxSize, int fileCount) {
            this.currentSize = currentSize;
            this.maxSize = maxSize;
            this.fileCount = fileCount;
        }

        public double getUsagePercent() {
            if (maxSize == 0) return 0;
            return (currentSize * 100.0) / maxSize;
        }

        public String getCurrentSizeMB() {
            return String.format("%.2f MB", currentSize / 1024.0 / 1024.0);
        }

        public String getMaxSizeMB() {
            return String.format("%.2f MB", maxSize / 1024.0 / 1024.0);
        }
    }
}

