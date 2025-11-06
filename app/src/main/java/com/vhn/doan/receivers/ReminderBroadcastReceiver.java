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
     * FIXED: Phân biệt rõ ràng giữa notification thường và full screen alarm
     */
    private void handleReminderTrigger(Context context, Intent intent) {
        Log.d(TAG, "🚨 handleReminderTrigger: Bắt đầu xử lý reminder trigger");
        String reminderId = intent.getStringExtra("reminder_id");
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        if (reminderId == null || title == null) {
            Log.w(TAG, "⚠️ handleReminderTrigger: reminderId or title is null");
            return;
        }

        Log.d(TAG, "📋 Reminder ID: " + reminderId + ", Title: " + title);

        // Kiểm tra reminder còn active không
        ReminderRepository repository = new ReminderRepositoryImpl();
        repository.getReminderById(reminderId, new ReminderRepository.RepositoryCallback<Reminder>() {
            @Override
            public void onSuccess(Reminder reminder) {
                Log.d(TAG, "✅ Fetched reminder from repository: " + (reminder != null ? reminder.getTitle() : "null"));

                if (reminder != null && reminder.isActive()) {
                    Log.d(TAG, "✅ Reminder is active, checking alarm style...");

                    // PHÂN BIỆT: Full screen alarm vs Notification thường
                    if (reminder.isAlarmStyle()) {
                        // === FULL SCREEN ALARM ===
                        Log.d(TAG, "🚨 Hiển thị FULL SCREEN ALARM");
                        showFullScreenAlarm(context, reminder);
                    } else {
                        // === NOTIFICATION THƯỜNG + ÂM THANH + RUNG ===
                        Log.d(TAG, "🔔 Hiển thị NOTIFICATION THƯỜNG với âm thanh và rung");
                        showNotificationWithSound(context, reminder);
                    }

                    // Lên lịch lặp lại nếu cần
                    if (reminder.getRepeatType() != Reminder.RepeatType.NO_REPEAT) {
                        Log.d(TAG, "🔄 Scheduling next repeat");
                        ReminderService reminderService = new ReminderService(context);
                        reminderService.scheduleNextRepeat(reminder);
                    }
                } else {
                    Log.w(TAG, "⚠️ Reminder is null or inactive, skipping notification");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error fetching reminder from repository: " + error);
                // Fallback: hiển thị notification thường
                Log.d(TAG, "🔔 Fallback: Showing basic notification");
                NotificationService.showReminderNotification(context, title, message, reminderId);
            }
        });
    }

    /**
     * Hiển thị full screen alarm activity
     * Hoạt động cho cả khi app đang mở và khi app bị tắt
     */
    private void showFullScreenAlarm(Context context, Reminder reminder) {
        try {
            Log.d(TAG, "🚨 Launching AlarmActivity for: " + reminder.getTitle());

            Intent alarmIntent = new Intent(context, AlarmActivity.class);
            alarmIntent.putExtra(AlarmActivity.EXTRA_REMINDER_ID, reminder.getId());
            alarmIntent.putExtra(AlarmActivity.EXTRA_TITLE, reminder.getTitle());
            alarmIntent.putExtra(AlarmActivity.EXTRA_MESSAGE, reminder.getDescription());

            // Flags để hiển thị activity từ background
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP);

            // Trên Android 10+ (API 29+), cần thêm flag để hiển thị từ background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                alarmIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            }

            context.startActivity(alarmIntent);
            Log.d(TAG, "✅ AlarmActivity launched successfully");

        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi launch AlarmActivity", e);
            // Fallback: hiển thị notification
            NotificationService notificationService = new NotificationService(context);
            notificationService.showReminderNotification(reminder);
        }
    }

    /**
     * Hiển thị notification thường với âm thanh và rung
     * Sử dụng khi user không chọn alarm style
     */
    private void showNotificationWithSound(Context context, Reminder reminder) {
        try {
            Log.d(TAG, "🔔 Showing notification with sound and vibration");

            NotificationService notificationService = new NotificationService(context);

            // Nếu có âm thanh tùy chỉnh, sử dụng method với sound URI
            if (reminder.getSoundUri() != null && !reminder.getSoundUri().isEmpty()) {
                notificationService.showReminderNotificationWithSound(reminder, reminder.getSoundUri());
            } else {
                // Sử dụng notification thường với âm thanh mặc định
                notificationService.showReminderNotification(reminder);
            }

            Log.d(TAG, "✅ Notification displayed successfully");

        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi hiển thị notification", e);
        }
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
