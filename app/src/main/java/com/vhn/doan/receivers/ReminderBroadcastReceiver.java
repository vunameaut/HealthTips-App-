package com.vhn.doan.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.vhn.doan.data.Reminder;
import com.vhn.doan.data.repository.ReminderRepository;
import com.vhn.doan.data.repository.ReminderRepositoryImpl;
import com.vhn.doan.services.ReminderService;

/**
 * BroadcastReceiver để xử lý khi thời gian nhắc nhở đã đến
 */
public class ReminderBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Acquire wake lock để đảm bảo thiết bị không sleep trong quá trình xử lý
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "HealthTips:ReminderReceiver"
        );

        try {
            // Acquire wake lock với timeout 30 giây (tăng từ 10 giây)
            wakeLock.acquire(30 * 1000);

            Log.d(TAG, "🔄 ReminderBroadcastReceiver triggered");

            String action = intent.getAction();
            if (action == null) {
                Log.w(TAG, "❌ Action is null");
                return;
            }

            switch (action) {
                case ReminderService.ACTION_REMINDER_TRIGGER:
                    handleReminderTrigger(context, intent);
                    break;
                case "REMINDER_STATUS_CHANGED":
                    handleReminderStatusChanged(context, intent);
                    break;
                default:
                    Log.w(TAG, "⚠️ Unknown action: " + action);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error in onReceive", e);
        } finally {
            // Luôn release wake lock
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    private void handleReminderTrigger(Context context, Intent intent) {
        String reminderId = intent.getStringExtra("reminder_id");
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        Log.d(TAG, "🔄 Handling reminder trigger - ID: " + reminderId + ", Title: " + title);

        if (reminderId == null || title == null || message == null) {
            Log.w(TAG, "❌ Missing reminder data");
            return;
        }

        // ✅ THÊM: Hiển thị notification ngay lập tức trước khi xử lý database
        showNotificationImmediately(context, reminderId, title, message);

        // Cập nhật trạng thái reminder
        updateReminderStatus(context, reminderId, title, message);
    }

    /**
     * Hiển thị notification ngay lập tức
     */
    private void showNotificationImmediately(Context context, String reminderId, String title, String message) {
        try {
            Log.d(TAG, "🔄 Hiển thị notification ngay lập tức: " + title);
            
            // Sử dụng Foreground Service để đảm bảo thông báo hiển thị
            com.vhn.doan.services.ReminderForegroundService.showReminder(context, reminderId, title, message);
            
            Log.d(TAG, "✅ Đã gửi yêu cầu hiển thị notification");
        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi hiển thị notification", e);
            
            // Fallback: hiển thị notification trực tiếp
            try {
                com.vhn.doan.services.NotificationService.showReminderNotification(context, title, message, reminderId);
                Log.d(TAG, "✅ Đã hiển thị notification qua fallback");
            } catch (Exception fallbackError) {
                Log.e(TAG, "❌ Lỗi khi hiển thị notification qua fallback", fallbackError);
            }
        }
    }

    /**
     * Cập nhật trạng thái reminder
     */
    private void updateReminderStatus(Context context, String reminderId, String title, String message) {
        ReminderRepository reminderRepository = new ReminderRepositoryImpl();
        reminderRepository.getReminderById(reminderId, new ReminderRepository.RepositoryCallback<Reminder>() {
            @Override
            public void onSuccess(Reminder reminder) {
                if (reminder != null) {
                    Log.d(TAG, "✅ Successfully retrieved reminder: " + reminder.getTitle());

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
                            Log.d(TAG, "✅ Reminder updated successfully");
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "❌ Failed to update reminder: " + error);
                        }
                    });
                } else {
                    Log.w(TAG, "⚠️ Reminder not found in database, but notification was shown");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to get reminder: " + error);
                // Vẫn hiển thị thông báo ngay cả khi không lấy được reminder từ database
                // (đã được xử lý trong showNotificationImmediately)
            }
        });
    }

    private void handleReminderStatusChanged(Context context, Intent intent) {
        String reminderId = intent.getStringExtra("reminder_id");
        boolean isActive = intent.getBooleanExtra("is_active", false);

        Log.d(TAG, "🔄 Reminder status changed - ID: " + reminderId + ", Active: " + isActive);

        // Xử lý thay đổi trạng thái reminder nếu cần
        if (!isActive) {
            // Hủy alarm nếu reminder bị tắt
            ReminderService.cancelReminder(context, reminderId);
            Log.d(TAG, "✅ Đã hủy reminder: " + reminderId);
        }
    }
}
