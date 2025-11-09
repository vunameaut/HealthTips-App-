package com.vhn.doan.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.repository.HealthTipRepository;
import com.vhn.doan.data.repository.HealthTipRepositoryImpl;

import java.util.List;

/**
 * WorkManager Worker để sync health tips từ Firebase định kỳ
 * Chạy ngầm để tự động cập nhật cache local
 */
public class HealthTipSyncWorker extends Worker {

    private static final String TAG = "HealthTipSyncWorker";
    private final HealthTipRepository healthTipRepository;

    public HealthTipSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.healthTipRepository = new HealthTipRepositoryImpl(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting health tips sync...");

        try {
            // Sync health tips từ Firebase
            syncHealthTips();

            Log.d(TAG, "Health tips sync completed successfully");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Error syncing health tips: " + e.getMessage(), e);

            // Retry nếu có lỗi
            if (getRunAttemptCount() < 3) {
                return Result.retry();
            }

            return Result.failure();
        }
    }

    /**
     * Sync health tips từ Firebase và lưu vào Room cache
     */
    private void syncHealthTips() {
        final Object lock = new Object();
        final boolean[] syncCompleted = {false};
        final boolean[] syncSuccess = {false};

        healthTipRepository.getAllHealthTips(new HealthTipRepository.HealthTipCallback() {
            @Override
            public void onSuccess(List<HealthTip> healthTips) {
                synchronized (lock) {
                    Log.d(TAG, "Synced " + healthTips.size() + " health tips");
                    syncSuccess[0] = true;
                    syncCompleted[0] = true;
                    lock.notifyAll();
                }
            }

            @Override
            public void onError(String errorMessage) {
                synchronized (lock) {
                    Log.e(TAG, "Sync error: " + errorMessage);
                    syncSuccess[0] = false;
                    syncCompleted[0] = true;
                    lock.notifyAll();
                }
            }
        });

        // Wait cho sync hoàn thành (timeout 60 giây)
        synchronized (lock) {
            try {
                if (!syncCompleted[0]) {
                    lock.wait(60000); // 60 seconds timeout
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e(TAG, "Sync interrupted", e);
            }
        }

        if (!syncSuccess[0]) {
            throw new RuntimeException("Failed to sync health tips");
        }
    }
}
