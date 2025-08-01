package com.vhn.doan.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import com.vhn.doan.data.Reminder;
import com.vhn.doan.data.repository.ReminderRepository;
import com.vhn.doan.data.repository.ReminderRepositoryImpl;
import com.vhn.doan.services.ReminderForegroundService;
import com.vhn.doan.services.ReminderService;
import com.vhn.doan.utils.NotificationDebugHelper;

/**
 * BroadcastReceiver để xử lý khi thời gian nhắc nhở đã đến
 * - Hiển thị thông báo ngay lập tức khi nhận broadcast
 * - Khởi động lại ReminderForegroundService nếu cần
 * - Cập nhật trạng thái reminder
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
            // Acquire wake lock với timeout 10 giây
            wakeLock.acquire(10 * 1000);

            Log.d(TAG, "ReminderBroadcastReceiver triggered");

            String action = intent.getAction();
            if (action == null) {
                Log.w(TAG, "Action is null");
                return;
            }

            switch (action) {
                case ReminderService.ACTION_REMINDER_TRIGGER:
                    handleReminderTrigger(context, intent);
                    break;
                case "REMINDER_STATUS_CHANGED":
                    handleReminderStatusChanged(context, intent);
                    break;
                case "ACTION_RESTART_REMINDER_SERVICE":
                    // Khởi động lại ReminderForegroundService khi có yêu cầu
                    handleRestartReminderService(context);
                    break;
                case Intent.ACTION_BOOT_COMPLETED:
                    // Khởi động lại service sau khi thiết bị khởi động
                    handleBootCompleted(context);
                    break;
                default:
                    Log.w(TAG, "Unknown action: " + action);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onReceive", e);
            // Fallback để đảm bảo reminder vẫn được hiển thị ngay cả khi có lỗi
            if (intent != null && ReminderService.ACTION_REMINDER_TRIGGER.equals(intent.getAction())) {
                showReminderDirectly(context, intent);
            }
        } finally {
            // Luôn release wake lock
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    private void handleReminderTrigger(Context context, Intent intent) {
        // Hiển thị thông báo ngay lập tức để đảm bảo người dùng thấy thông báo
        showReminderDirectly(context, intent);

        // Sau đó xử lý cập nhật dữ liệu
        String reminderId = intent.getStringExtra("reminder_id");
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        Log.d(TAG, "Handling reminder trigger - ID: " + reminderId + ", Title: " + title);

        if (reminderId == null || title == null || message == null) {
            Log.w(TAG, "Missing reminder data");
            return;
        }

        // Cập nhật trạng thái reminder
        ReminderRepository reminderRepository = new ReminderRepositoryImpl();
        reminderRepository.getReminderById(reminderId, new ReminderRepository.RepositoryCallback<Reminder>() {
            @Override
            public void onSuccess(Reminder reminder) {
                if (reminder != null) {
                    Log.d(TAG, "Successfully retrieved reminder: " + reminder.getTitle());

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
                            Log.d(TAG, "Reminder updated successfully");
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Failed to update reminder: " + error);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to get reminder: " + error);
            }
        });
    }

    private void handleReminderStatusChanged(Context context, Intent intent) {
        String reminderId = intent.getStringExtra("reminder_id");
        boolean isActive = intent.getBooleanExtra("is_active", false);

        Log.d(TAG, "Reminder status changed - ID: " + reminderId + ", Active: " + isActive);

        // Xử lý thay đổi trạng thái reminder nếu cần
        if (!isActive) {
            // Hủy alarm nếu reminder bị tắt
            ReminderService.cancelReminder(context, reminderId);
        }
    }

    /**
     * Xử lý khởi động lại ReminderForegroundService
     */
    private void handleRestartReminderService(Context context) {
        Log.d(TAG, "Handling restart reminder service");
        try {
            // Khởi động lại ReminderForegroundService
            Intent serviceIntent = new Intent(context, ReminderForegroundService.class);
            serviceIntent.setAction("RESTART_SERVICE");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            Log.d(TAG, "ReminderForegroundService restarted successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error restarting ReminderForegroundService", e);
        }
    }

    /**
     * Xử lý khi thiết bị khởi động xong
     */
    private void handleBootCompleted(Context context) {
        Log.d(TAG, "Handling boot completed");
        try {
            // Khởi động ReminderForegroundService sau khi thiết bị khởi động
            Intent serviceIntent = new Intent(context, ReminderForegroundService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            Log.d(TAG, "ReminderForegroundService started after boot");
        } catch (Exception e) {
            Log.e(TAG, "Error starting ReminderForegroundService after boot", e);
        }
    }

    /**
     * Hiển thị thông báo ngay lập tức không đợi service
     * Dùng làm fallback khi có lỗi hoặc để đảm bảo thông báo được hiển thị ngay
     */
    private void showReminderDirectly(Context context, Intent intent) {
        try {
            String reminderId = intent.getStringExtra("reminder_id");
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");

            if (reminderId != null && title != null && message != null) {
                // Kiểm tra quyền thông báo
                boolean hasPermission = NotificationDebugHelper.checkNotificationPermission(context);
                Log.d(TAG, "Notification permission: " + (hasPermission ? "Granted" : "Denied"));

                // Hiển thị thông báo trực tiếp
                ReminderForegroundService.showReminder(context, reminderId, title, message);
                Log.d(TAG, "Reminder notification shown directly: " + title);
            } else {
                Log.w(TAG, "Cannot show reminder directly: Missing data");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing reminder directly", e);
        }
    }
}
