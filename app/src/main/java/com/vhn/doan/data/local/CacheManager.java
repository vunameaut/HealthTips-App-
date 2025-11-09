package com.vhn.doan.data.local;

import android.content.Context;
import android.util.Log;

import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.local.dao.HealthTipDao;
import com.vhn.doan.data.local.entity.HealthTipEntity;

/**
 * CacheManager - Quản lý cache thông minh theo kiểu TikTok/Facebook
 *
 * Chiến lược:
 * 1. Cache NGAY KHI USER SCROLL QUA item (passive caching)
 * 2. Giới hạn số lượng items trong cache
 * 3. Tự động cleanup theo LRU (Least Recently Used)
 * 4. Cache videos đã xem
 */
public class CacheManager {

    private static final String TAG = "CacheManager";

    // Giới hạn cache (có thể config)
    private static final int MAX_CACHED_TIPS = 100; // Tối đa 100 tips
    private static final long MAX_CACHE_SIZE_MB = 50; // Tối đa 50MB
    private static final long CACHE_EXPIRY_DAYS = 7; // Xóa cache > 7 ngày

    private static CacheManager instance;
    private final HealthTipDao healthTipDao;
    private final Context context;

    private CacheManager(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(this.context);
        this.healthTipDao = db.healthTipDao();
    }

    public static synchronized CacheManager getInstance(Context context) {
        if (instance == null) {
            instance = new CacheManager(context);
        }
        return instance;
    }

    /**
     * Cache một health tip NGAY LẬP TỨC
     * Gọi method này khi user SCROLL QUA hoặc XEM một item
     *
     * GIỐNG TIKTOK/FACEBOOK: Cache ngay khi nhìn thấy!
     */
    public void cacheHealthTipImmediately(HealthTip healthTip) {
        if (healthTip == null || healthTip.getId() == null) {
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Update timestamp để LRU tracking
                HealthTipEntity entity = HealthTipEntity.fromHealthTip(healthTip);
                entity.setCachedAt(System.currentTimeMillis());

                healthTipDao.insert(entity);

                Log.d(TAG, "✓ Cached tip: " + healthTip.getId() + " - " + healthTip.getTitle());

                // Check và cleanup nếu cần
                checkAndCleanupIfNeeded();

            } catch (Exception e) {
                Log.e(TAG, "Error caching tip: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Cache nhiều tips cùng lúc (batch)
     * Dùng khi load list từ API
     */
    public void cacheHealthTipsBatch(java.util.List<HealthTip> healthTips) {
        if (healthTips == null || healthTips.isEmpty()) {
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                java.util.List<HealthTipEntity> entities = new java.util.ArrayList<>();
                long now = System.currentTimeMillis();

                for (HealthTip tip : healthTips) {
                    if (tip != null && tip.getId() != null) {
                        HealthTipEntity entity = HealthTipEntity.fromHealthTip(tip);
                        entity.setCachedAt(now);
                        entities.add(entity);
                    }
                }

                healthTipDao.insertAll(entities);
                Log.d(TAG, "✓ Batch cached " + entities.size() + " tips");

                checkAndCleanupIfNeeded();

            } catch (Exception e) {
                Log.e(TAG, "Error batch caching: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Kiểm tra và cleanup nếu vượt quá giới hạn
     *
     * Logic cleanup:
     * 1. Đếm số items trong cache
     * 2. Nếu > MAX_CACHED_TIPS → Xóa items cũ nhất (LRU)
     * 3. Xóa items quá 7 ngày
     */
    private void checkAndCleanupIfNeeded() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // 1. Cleanup items quá hạn (> 7 ngày)
                long expiryTime = System.currentTimeMillis() - (CACHE_EXPIRY_DAYS * 24 * 60 * 60 * 1000L);
                healthTipDao.deleteOldHealthTips(expiryTime);

                // 2. Đếm số items hiện tại
                int currentCount = healthTipDao.getHealthTipCountSync();

                if (currentCount > MAX_CACHED_TIPS) {
                    // 3. Xóa items cũ nhất để giữ số lượng = MAX_CACHED_TIPS
                    int itemsToDelete = currentCount - MAX_CACHED_TIPS;
                    healthTipDao.deleteOldestItems(itemsToDelete);

                    Log.d(TAG, "🧹 Cleanup: Deleted " + itemsToDelete + " oldest items. Current: " + MAX_CACHED_TIPS);
                } else {
                    Log.d(TAG, "📊 Cache size: " + currentCount + "/" + MAX_CACHED_TIPS);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error in cleanup: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Force cleanup toàn bộ cache
     * Dùng trong Settings hoặc khi user request
     */
    public void clearAllCache() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            healthTipDao.deleteAll();
            Log.d(TAG, "🗑️ All cache cleared");
        });
    }

    /**
     * Lấy thông tin cache stats
     */
    public void getCacheStats(CacheStatsCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int count = healthTipDao.getHealthTipCountSync();

            // Estimate size (rough calculation)
            // Giả sử mỗi tip ~50KB (title, content, images URLs, etc.)
            long estimatedSizeMB = (count * 50) / 1024; // KB to MB

            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            mainHandler.post(() -> callback.onStatsReady(count, estimatedSizeMB, MAX_CACHED_TIPS));
        });
    }

    /**
     * Callback cho cache stats
     */
    public interface CacheStatsCallback {
        void onStatsReady(int itemCount, long sizeInMB, int maxItems);
    }

    /**
     * Update access time của một tip (LRU)
     * Gọi khi user XEM chi tiết một tip
     */
    public void updateAccessTime(String tipId) {
        if (tipId == null) return;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                healthTipDao.updateCachedAt(tipId, System.currentTimeMillis());
                Log.d(TAG, "⏰ Updated access time for: " + tipId);
            } catch (Exception e) {
                Log.e(TAG, "Error updating access time: " + e.getMessage());
            }
        });
    }
}
