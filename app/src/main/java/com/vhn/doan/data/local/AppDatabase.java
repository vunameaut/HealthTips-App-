package com.vhn.doan.data.local;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.vhn.doan.data.local.dao.CategoryDao;
import com.vhn.doan.data.local.dao.HealthTipDao;
import com.vhn.doan.data.local.dao.VideoDao;
import com.vhn.doan.data.local.entity.CategoryEntity;
import com.vhn.doan.data.local.entity.HealthTipEntity;
import com.vhn.doan.data.local.entity.VideoEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AppDatabase - Room Database cho HealthTips App
 * Singleton pattern để đảm bảo chỉ có 1 instance
 */
@Database(
    entities = {
        HealthTipEntity.class,
        CategoryEntity.class,
        VideoEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = "AppDatabase";
    private static final String DATABASE_NAME = "healthtips_database";

    private static volatile AppDatabase INSTANCE;

    // ExecutorService để chạy database operations trên background thread
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Abstract methods để lấy DAOs
    public abstract HealthTipDao healthTipDao();
    public abstract CategoryDao categoryDao();
    public abstract VideoDao videoDao();

    /**
     * Lấy instance của database (Singleton)
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    // Không cho phép main thread queries (force background thread)
                    .fallbackToDestructiveMigration() // Xóa và tạo lại DB khi migrate fails
                    .build();

                    Log.d(TAG, "Database instance created");
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Đóng database
     */
    public static void closeDatabase() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            INSTANCE.close();
            INSTANCE = null;
            Log.d(TAG, "Database closed");
        }
    }

    /**
     * Xóa tất cả data (dùng cho testing hoặc logout)
     */
    public void clearAllTables() {
        databaseWriteExecutor.execute(() -> {
            healthTipDao().deleteAll();
            categoryDao().deleteAll();
            videoDao().deleteAll();
            Log.d(TAG, "All tables cleared");
        });
    }

    /**
     * Xóa cache cũ (gọi định kỳ để cleanup)
     * Xóa data cũ hơn 7 ngày
     */
    public void cleanupOldCache() {
        databaseWriteExecutor.execute(() -> {
            long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
            healthTipDao().deleteOldHealthTips(sevenDaysAgo);
            categoryDao().deleteOldCategories(sevenDaysAgo);
            videoDao().deleteOldVideos(sevenDaysAgo);
            Log.d(TAG, "Old cache cleaned up (older than 7 days)");
        });
    }
}
