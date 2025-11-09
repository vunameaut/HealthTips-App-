package com.vhn.doan.utils;

import android.content.Context;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.vhn.doan.workers.HealthTipSyncWorker;

import java.util.concurrent.TimeUnit;

/**
 * Helper class để schedule các background sync tasks
 */
public class SyncScheduler {

    private static final String TAG = "SyncScheduler";
    private static final String HEALTH_TIP_SYNC_WORK_NAME = "health_tip_sync";

    /**
     * Schedule periodic sync cho health tips
     * Mặc định: mỗi 6 giờ khi có mạng
     */
    public static void scheduleHealthTipSync(Context context) {
        scheduleHealthTipSync(context, 6, TimeUnit.HOURS);
    }

    /**
     * Schedule periodic sync cho health tips với interval tùy chỉnh
     *
     * @param context  Application context
     * @param interval Khoảng thời gian giữa các lần sync
     * @param timeUnit Đơn vị thời gian (HOURS, MINUTES, etc.)
     */
    public static void scheduleHealthTipSync(Context context, long interval, TimeUnit timeUnit) {
        Log.d(TAG, "Scheduling health tip sync every " + interval + " " + timeUnit);

        // Constraints: chỉ chạy khi có mạng
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // Tạo periodic work request
        PeriodicWorkRequest syncWorkRequest = new PeriodicWorkRequest.Builder(
                HealthTipSyncWorker.class,
                interval,
                timeUnit
        )
                .setConstraints(constraints)
                .addTag("sync")
                .build();

        // Enqueue work với KEEP policy (giữ work hiện tại nếu đã tồn tại)
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                HEALTH_TIP_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest
        );

        Log.d(TAG, "Health tip sync scheduled successfully");
    }

    /**
     * Hủy scheduled sync
     */
    public static void cancelHealthTipSync(Context context) {
        Log.d(TAG, "Cancelling health tip sync");
        WorkManager.getInstance(context).cancelUniqueWork(HEALTH_TIP_SYNC_WORK_NAME);
    }

    /**
     * Sync ngay lập tức (one-time)
     */
    public static void syncNow(Context context) {
        Log.d(TAG, "Triggering immediate sync");

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // Tạo one-time work request
        androidx.work.OneTimeWorkRequest syncWorkRequest = new androidx.work.OneTimeWorkRequest.Builder(
                HealthTipSyncWorker.class
        )
                .setConstraints(constraints)
                .addTag("sync")
                .addTag("immediate")
                .build();

        WorkManager.getInstance(context).enqueue(syncWorkRequest);
    }
}
