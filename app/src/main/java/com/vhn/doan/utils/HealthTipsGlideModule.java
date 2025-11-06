package com.vhn.doan.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

/**
 * HealthTipsGlideModule - Custom Glide configuration
 * Tối ưu cache cho hình ảnh và video thumbnails
 */
@GlideModule
public class HealthTipsGlideModule extends AppGlideModule {

    private static final String TAG = "HealthTipsGlideModule";

    // Cache sizes
    private static final int DISK_CACHE_SIZE = 250 * 1024 * 1024; // 250 MB
    private static final String DISK_CACHE_DIR = "image_cache";

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        // Tính toán memory cache size dựa trên device
        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context)
                .setMemoryCacheScreens(2) // Cache 2 màn hình worth của images
                .setBitmapPoolScreens(3)   // Pool 3 màn hình worth của bitmaps
                .build();

        // Cấu hình Memory Cache
        builder.setMemoryCache(new LruResourceCache(calculator.getMemoryCacheSize()));

        // Cấu hình Disk Cache
        builder.setDiskCache(new InternalCacheDiskCacheFactory(
                context,
                DISK_CACHE_DIR,
                DISK_CACHE_SIZE
        ));

        // Cấu hình default request options
        RequestOptions defaultOptions = new RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565) // Giảm memory usage
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // Tự động chọn strategy
                .skipMemoryCache(false); // Cho phép memory cache

        builder.setDefaultRequestOptions(defaultOptions);

        // Cấu hình log level
        if (isDebugMode(context)) {
            builder.setLogLevel(Log.DEBUG);
        } else {
            builder.setLogLevel(Log.ERROR);
        }

        Log.d(TAG, "Glide configured - Memory Cache: " +
                (calculator.getMemoryCacheSize() / 1024 / 1024) + "MB, " +
                "Disk Cache: " + (DISK_CACHE_SIZE / 1024 / 1024) + "MB");
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        // Có thể register custom ModelLoaders, Decoders, Encoders ở đây nếu cần
        super.registerComponents(context, glide, registry);
    }

    @Override
    public boolean isManifestParsingEnabled() {
        // Tắt manifest parsing để tăng performance
        return false;
    }

    /**
     * Kiểm tra debug mode
     */
    private boolean isDebugMode(Context context) {
        return (context.getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    /**
     * Clear Glide cache (memory + disk)
     */
    public static void clearCache(Context context) {
        try {
            // Clear memory cache (main thread)
            Glide.get(context).clearMemory();

            // Clear disk cache (background thread)
            new Thread(() -> {
                try {
                    Glide.get(context).clearDiskCache();
                    Log.d(TAG, "Glide cache cleared");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to clear disk cache", e);
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear cache", e);
        }
    }

    /**
     * Get configured cache size (bytes)
     */
    public static long getCacheSize() {
        return DISK_CACHE_SIZE;
    }
}
