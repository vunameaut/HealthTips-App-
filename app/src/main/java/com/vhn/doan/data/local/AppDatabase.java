package com.vhn.doan.data.local;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.vhn.doan.data.local.Converters;
import com.vhn.doan.data.local.dao.CategoryDao;
import com.vhn.doan.data.local.dao.HealthTipDao;
import com.vhn.doan.data.local.dao.NotificationHistoryDao;
import com.vhn.doan.data.local.dao.VideoDao;
import com.vhn.doan.data.local.entity.CategoryEntity;
import com.vhn.doan.data.local.entity.HealthTipEntity;
import com.vhn.doan.data.local.entity.NotificationHistoryEntity;
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
        VideoEntity.class,
        NotificationHistoryEntity.class
    },
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters.class)
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
    public abstract NotificationHistoryDao notificationHistoryDao();

    // Migration từ version 3 sang 4 - Thêm bảng notification_history
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Log.d(TAG, "Migrating database from version 3 to 4");

            // Drop bảng cũ nếu tồn tại (để tránh conflict)
            database.execSQL("DROP TABLE IF EXISTS notification_history");

            // Tạo bảng notification_history mới với schema chính xác
            database.execSQL(
                "CREATE TABLE notification_history (" +
                "id TEXT PRIMARY KEY NOT NULL, " +
                "notification_id TEXT, " +
                "user_id TEXT NOT NULL, " +
                "title TEXT NOT NULL, " +
                "body TEXT NOT NULL, " +
                "image_url TEXT, " +
                "large_icon_url TEXT, " +
                "type TEXT NOT NULL, " +
                "category TEXT, " +
                "priority INTEGER NOT NULL DEFAULT 0, " +
                "deep_link TEXT, " +
                "target_id TEXT, " +
                "target_type TEXT, " +
                "is_read INTEGER NOT NULL DEFAULT 0, " +
                "is_deleted INTEGER NOT NULL DEFAULT 0, " +
                "is_synced INTEGER NOT NULL DEFAULT 0, " +
                "received_at INTEGER NOT NULL, " +
                "read_at INTEGER, " +
                "created_at INTEGER NOT NULL, " +
                "updated_at INTEGER NOT NULL, " +
                "extra_data TEXT)"
            );

            // Tạo các index để tối ưu performance
            database.execSQL("CREATE INDEX IF NOT EXISTS index_notification_history_user_id ON notification_history(user_id)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_notification_history_received_at ON notification_history(received_at)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_notification_history_is_read ON notification_history(is_read)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_notification_history_type ON notification_history(type)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_notification_history_user_id_received_at ON notification_history(user_id, received_at)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_notification_history_user_id_is_read_received_at ON notification_history(user_id, is_read, received_at)");

            Log.d(TAG, "Migration from 3 to 4 completed successfully");
        }
    };

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
                    .addMigrations(MIGRATION_3_4) // Thêm migration strategy
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
            notificationHistoryDao().deleteAll();
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
