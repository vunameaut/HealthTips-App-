package com.vhn.doan.utils;

import android.content.Context;
import android.util.Log;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.vhn.doan.data.Reminder;
import com.vhn.doan.workers.ReminderWorker;

import java.util.concurrent.TimeUnit;

/**
 * Utility class để quản lý việc lên lịch và hủy nhắc nhở sử dụng WorkManager
 */
public class ReminderScheduler {

    private static final String TAG = "ReminderScheduler";
    private static final String WORK_NAME_PREFIX = "reminder_";

    private final Context context;
    private final WorkManager workManager;

    public ReminderScheduler(Context context) {
        this.context = context.getApplicationContext();
        this.workManager = WorkManager.getInstance(this.context);
    }

    /**
     * Lên lịch nhắc nhở mới
     */
    public void scheduleReminder(Reminder reminder) {
        if (reminder == null || reminder.getId() == null) {
            Log.e(TAG, "Reminder hoặc ID không hợp lệ");
            return;
        }

        try {
            long currentTime = System.currentTimeMillis();
            long reminderTime = reminder.getReminderTime();

            if (reminderTime <= currentTime) {
                Log.w(TAG, "Thời gian nhắc nhở đã qua, không lên lịch: " + reminder.getId());
                return;
            }

            long delayMillis = reminderTime - currentTime;

            // Tạo input data cho Worker
            Data inputData = new Data.Builder()
                .putString(ReminderWorker.KEY_REMINDER_ID, reminder.getId())
                .putString(ReminderWorker.KEY_TITLE, reminder.getTitle())
                .putString(ReminderWorker.KEY_MESSAGE, reminder.getDescription())
                .putString(ReminderWorker.KEY_SOUND_ID, reminder.getSoundId())
                .putString(ReminderWorker.KEY_SOUND_URI, reminder.getSoundUri())
                .putBoolean(ReminderWorker.KEY_VIBRATE, reminder.isVibrate())
                .putInt(ReminderWorker.KEY_VOLUME, reminder.getVolume())
                .putBoolean(ReminderWorker.KEY_IS_ALARM_STYLE, reminder.isAlarmStyle())
                .build();

            // Tạo WorkRequest
            OneTimeWorkRequest reminderWork = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(WORK_NAME_PREFIX + reminder.getId())
                .build();

            // Lên lịch work với unique name để tránh trùng lặp
            String workName = WORK_NAME_PREFIX + reminder.getId();
            workManager.enqueueUniqueWork(
                workName,
                ExistingWorkPolicy.REPLACE,
                reminderWork
            );

            Log.d(TAG, "Đã lên lịch reminder: " + reminder.getId() +
                " sau " + (delayMillis / 1000) + " giây");

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lên lịch reminder: " + reminder.getId(), e);
        }
    }

    /**
     * Hủy nhắc nhở theo ID
     */
    public void cancelReminder(String reminderId) {
        if (reminderId == null || reminderId.isEmpty()) {
            Log.e(TAG, "Reminder ID không hợp lệ");
            return;
        }

        try {
            String workName = WORK_NAME_PREFIX + reminderId;
            workManager.cancelUniqueWork(workName);

            Log.d(TAG, "Đã hủy reminder: " + reminderId);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hủy reminder: " + reminderId, e);
        }
    }

    /**
     * Hủy tất cả nhắc nhở của một người dùng
     */
    public void cancelAllRemindersForUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID không hợp lệ");
            return;
        }

        try {
            // Hủy tất cả work có tag bắt đầu bằng reminder_
            workManager.cancelAllWorkByTag(WORK_NAME_PREFIX + "user_" + userId);

            Log.d(TAG, "Đã hủy tất cả reminder cho user: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hủy tất cả reminder cho user: " + userId, e);
        }
    }

    /**
     * Hủy tất cả nhắc nhở
     */
    public void cancelAllReminders() {
        try {
            workManager.cancelAllWork();
            Log.d(TAG, "Đã hủy tất cả reminder");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hủy tất cả reminder", e);
        }
    }

    /**
     * Cập nhật lại lịch nhắc nhở (hủy cũ và tạo mới)
     */
    public void rescheduleReminder(Reminder reminder) {
        if (reminder == null || reminder.getId() == null) {
            Log.e(TAG, "Reminder không hợp lệ để reschedule");
            return;
        }

        // Hủy nhắc nhở cũ trước
        cancelReminder(reminder.getId());

        // Lên lịch mới nếu reminder còn active
        if (reminder.isActive()) {
            scheduleReminder(reminder);
        }
    }

    /**
     * Kiểm tra xem có work đang chạy cho reminder này không
     */
    public void checkReminderStatus(String reminderId, ReminderStatusCallback callback) {
        if (reminderId == null || reminderId.isEmpty()) {
            callback.onResult(false, "Reminder ID không hợp lệ");
            return;
        }

        try {
            String workName = WORK_NAME_PREFIX + reminderId;

            // Sử dụng ListenableFuture thay vì LiveData
            com.google.common.util.concurrent.ListenableFuture<java.util.List<androidx.work.WorkInfo>> future =
                workManager.getWorkInfosForUniqueWork(workName);

            future.addListener(() -> {
                try {
                    java.util.List<androidx.work.WorkInfo> workInfos = future.get();
                    if (workInfos != null && !workInfos.isEmpty()) {
                        androidx.work.WorkInfo workInfo = workInfos.get(0);
                        boolean isScheduled = workInfo.getState() == androidx.work.WorkInfo.State.ENQUEUED ||
                                            workInfo.getState() == androidx.work.WorkInfo.State.RUNNING;
                        callback.onResult(isScheduled, "Work state: " + workInfo.getState());
                    } else {
                        callback.onResult(false, "Không tìm thấy work");
                    }
                } catch (Exception e) {
                    callback.onResult(false, "Lỗi kiểm tra: " + e.getMessage());
                }
            }, context.getMainExecutor());

        } catch (Exception e) {
            callback.onResult(false, "Lỗi kiểm tra: " + e.getMessage());
        }
    }

    /**
     * Interface callback để kiểm tra trạng thái reminder
     */
    public interface ReminderStatusCallback {
        void onResult(boolean isScheduled, String message);
    }

    /**
     * Singleton pattern để sử dụng toàn ứng dụng
     */
    private static ReminderScheduler instance;

    public static ReminderScheduler getInstance(Context context) {
        if (instance == null) {
            synchronized (ReminderScheduler.class) {
                if (instance == null) {
                    instance = new ReminderScheduler(context);
                }
            }
        }
        return instance;
    }
}
