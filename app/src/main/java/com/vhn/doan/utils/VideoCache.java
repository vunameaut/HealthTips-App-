package com.vhn.doan.utils;

import android.content.Context;
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;

/**
 * Singleton class để quản lý video cache cho ExoPlayer
 * Giúp cải thiện hiệu suất và giảm lag khi phát video
 */
public class VideoCache {

    private static SimpleCache sSimpleCache;
    private static final long MAX_CACHE_SIZE = 100 * 1024 * 1024; // 100MB

    public static SimpleCache getInstance(Context context) {
        if (sSimpleCache == null) {
            // Tạo thư mục cache
            File cacheDir = new File(context.getCacheDir(), "video_cache");

            // Tạo cache với LRU eviction policy
            LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE);

            // Khởi tạo SimpleCache với database provider
            sSimpleCache = new SimpleCache(
                cacheDir,
                evictor,
                new StandaloneDatabaseProvider(context)
            );
        }
        return sSimpleCache;
    }

    /**
     * Giải phóng cache khi app bị destroy
     */
    public static void releaseCache() {
        if (sSimpleCache != null) {
            try {
                sSimpleCache.release();
                sSimpleCache = null;
            } catch (Exception e) {
                android.util.Log.e("VideoCache", "Lỗi khi release cache", e);
            }
        }
    }
}
