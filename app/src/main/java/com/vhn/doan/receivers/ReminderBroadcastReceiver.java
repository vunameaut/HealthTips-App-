package com.vhn.doan.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import com.vhn.doan.data.Reminder;
import com.vhn.doan.data.repository.ReminderRepository;
import com.vhn.doan.data.repository.ReminderRepositoryImpl;
import com.vhn.doan.presentation.reminder.AlarmActivity;
import com.vhn.doan.services.NotificationService;
import com.vhn.doan.services.ReminderService;

import android.util.Log;

/**
 * BroadcastReceiver để xử lý khi thời gian nhắc nhở đã đến
 * - Khởi động AlarmActivity thay vì hiển thị notification
 * - Cập nhật trạng thái reminder
 */
public class ReminderBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: Received intent with action: " + intent.getAction());
        // Acquire wake lock để đảm bảo thiết bị không sleep trong quá trình xử lý
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "HealthTips:ReminderReceiver"
        );

        try {
            // Acquire wake lock với timeout 10 giây
            wakeLock.acquire(10 * 1000);

            String action = intent.getAction();
            if (action == null) {
                Log.w(TAG, "onReceive: Action is null");
                return;
            }

            switch (action) {
                case ReminderService.ACTION_REMINDER_TRIGGER:
                    handleReminderTrigger(context, intent);
                    break;
                case Intent.ACTION_BOOT_COMPLETED:
                case Intent.ACTION_MY_PACKAGE_REPLACED:
                case Intent.ACTION_PACKAGE_REPLACED:
                    handleSystemReboot(context);
                    break;
            }

        } catch (Exception e) {
            Log.e(TAG, "onReceive: Exception", e);
        } finally {
            // Release wake lock
            if (wakeLock.isHeld()) {
                wakeLock.release();
                Log.d(TAG, "onReceive: WakeLock released");
            }
        }
    }

    /**
     * Xử lý khi nhắc nhở được kích hoạt
     */
    private void handleReminderTrigger(Context context, Intent intent) {
        Log.d(TAG, "handleReminderTrigger: Handling reminder trigger");
        String reminderId = intent.getStringExtra("reminder_id");
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        if (reminderId == null || title == null) {
            Log.w(TAG, "handleReminderTrigger: reminderId or title is null");
            return;
        }

        Log.d(TAG, "handleReminderTrigger: Reminder ID: " + reminderId + ", Title: " + title);

        // Kiểm tra reminder còn active không
        ReminderRepository repository = new ReminderRepositoryImpl();
        repository.getReminderById(reminderId, new ReminderRepository.RepositoryCallback<Reminder>() {
            @Override
            public void onSuccess(Reminder reminder) {
                Log.d(TAG, "onSuccess: Fetched reminder from repository: " + reminder);
                if (reminder != null && reminder.isActive()) {
                    Log.d(TAG, "onSuccess: Reminder is active, showing notification");
                    // Hiển thị thông báo
                    NotificationService notificationService = new NotificationService(context);
                    notificationService.showReminderNotification(reminder);

                    // Lên lịch lặp lại nếu cần
                    if (reminder.getRepeatType() != Reminder.RepeatType.NO_REPEAT) {
                        Log.d(TAG, "onSuccess: Scheduling next repeat");
                        ReminderService reminderService = new ReminderService(context);
                        reminderService.scheduleNextRepeat(reminder);
                    }
                } else {
                    Log.w(TAG, "onSuccess: Reminder is null or inactive");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: Error fetching reminder from repository: " + error);
                // Fallback: vẫn hiển thị thông báo
                Log.d(TAG, "onError: Showing fallback notification");
                NotificationService.showReminderNotification(context, title, message, reminderId);
            }
        });
    }

    /**
     * Xử lý khi hệ thống khởi động lại
     */
    private void handleSystemReboot(Context context) {
        Log.d(TAG, "handleSystemReboot: System rebooted, rescheduling reminders");
        // Lên lịch lại tất cả các reminder đang active
        ReminderRepository repository = new ReminderRepositoryImpl();
        // Tạm thời bỏ qua việc lấy userId - cần cải thiện trong tương lai
        // Có thể lưu userId vào SharedPreferences hoặc cơ chế khác
    }
}
