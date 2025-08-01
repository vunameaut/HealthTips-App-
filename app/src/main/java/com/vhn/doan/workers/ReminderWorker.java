package com.vhn.doan.workers;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.vhn.doan.data.Reminder;
import com.vhn.doan.data.repository.ReminderRepository;
import com.vhn.doan.data.repository.ReminderRepositoryImpl;
import com.vhn.doan.services.NotificationService;

/**
 * WorkManager Worker để xử lý thông báo nhắc nhở
 * Hoạt động mạnh mẽ hơn AlarmManager trong việc xử lý Doze mode
 */
public class ReminderWorker extends Worker {

    private static final String TAG = "ReminderWorker";

    // Input data keys
    public static final String KEY_REMINDER_ID = "reminder_id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "ReminderWorker started");

        // Acquire wake lock để đảm bảo thiết bị không sleep
        PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "HealthTips:ReminderWorker"
        );

        try {
            wakeLock.acquire(30 * 1000); // 30 giây timeout

            String reminderId = getInputData().getString(KEY_REMINDER_ID);
            String title = getInputData().getString(KEY_TITLE);
            String message = getInputData().getString(KEY_MESSAGE);

            Log.d(TAG, "Processing reminder - ID: " + reminderId + ", Title: " + title);

            if (reminderId == null || title == null || message == null) {
                Log.w(TAG, "Missing reminder data");
                return Result.failure();
            }

            // Hiển thị thông báo ngay lập tức
            NotificationService.showReminderNotification(getApplicationContext(), title, message, reminderId);
            Log.d(TAG, "Notification shown successfully");

            // Cập nhật trạng thái reminder
            updateReminderStatus(reminderId);

            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Error in ReminderWorker", e);
            return Result.failure();
        } finally {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    private void updateReminderStatus(String reminderId) {
        ReminderRepository reminderRepository = new ReminderRepositoryImpl();
        reminderRepository.getReminderById(reminderId, new ReminderRepository.RepositoryCallback<Reminder>() {
            @Override
            public void onSuccess(Reminder reminder) {
                if (reminder != null) {
                    Log.d(TAG, "Successfully retrieved reminder for update: " + reminder.getTitle());

                    // Cập nhật lần thông báo cuối
                    reminder.setLastNotified(System.currentTimeMillis());

                    // Nếu không phải reminder lặp lại, đánh dấu là đã hoàn thành
                    if (!reminder.isRepeating()) {
                        reminder.setCompleted(true);
                        reminder.setActive(false);
                    }

                    reminderRepository.updateReminder(reminder, new ReminderRepository.RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Log.d(TAG, "Reminder status updated successfully");
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Failed to update reminder status: " + error);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to retrieve reminder for update: " + error);
            }
        });
    }
}
