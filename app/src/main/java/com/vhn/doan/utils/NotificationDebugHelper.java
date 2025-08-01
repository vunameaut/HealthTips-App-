package com.vhn.doan.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

/**
 * Helper để debug và kiểm tra thông báo
 */
public class NotificationDebugHelper {

    private static final String TAG = "NotificationDebugHelper";

    /**
     * Kiểm tra trạng thái thông báo
     */
    public static void checkNotificationStatus(Context context) {
        Log.d(TAG, "=== KIỂM TRA TRẠNG THÁI THÔNG BÁO ===");

        // Kiểm tra quyền thông báo
        boolean hasNotificationPermission = PermissionHelper.hasNotificationPermission(context);
        Log.d(TAG, "Có quyền thông báo: " + hasNotificationPermission);

        // Kiểm tra thông báo có được bật không
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        boolean areNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled();
        Log.d(TAG, "Thông báo được bật: " + areNotificationsEnabled);

        // Kiểm tra quyền exact alarm
        boolean hasExactAlarmPermission = ReminderPermissionHelper.hasExactAlarmPermission(context);
        Log.d(TAG, "Có quyền exact alarm: " + hasExactAlarmPermission);

        // Kiểm tra battery optimization
        boolean isBatteryOptimizationIgnored = PermissionHelper.isBatteryOptimizationIgnored(context);
        Log.d(TAG, "Battery optimization bị ignore: " + isBatteryOptimizationIgnored);

        // Kiểm tra notification channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            android.app.NotificationChannel reminderChannel = notificationManager.getNotificationChannel("reminder_channel");
            if (reminderChannel != null) {
                Log.d(TAG, "Reminder channel importance: " + reminderChannel.getImportance());
                Log.d(TAG, "Reminder channel enabled: " + reminderChannel.getImportance() != android.app.NotificationManager.IMPORTANCE_NONE);
            } else {
                Log.w(TAG, "Reminder channel không tồn tại");
            }
        }

        Log.d(TAG, "=== KẾT THÚC KIỂM TRA ===");
    }

    /**
     * Test thông báo ngay lập tức
     */
    public static void testNotification(Context context) {
        Log.d(TAG, "Test thông báo ngay lập tức");
        
        try {
            com.vhn.doan.services.NotificationService.showReminderNotification(
                context,
                "Test Thông Báo",
                "Đây là thông báo test để kiểm tra hệ thống",
                "test_id"
            );
            Log.d(TAG, "Đã gửi test notification");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi test notification", e);
        }
    }

    /**
     * Kiểm tra và hiển thị thông báo cho reminders đã bị miss
     */
    public static void showMissedReminders(Context context) {
        Log.d(TAG, "Kiểm tra reminders đã bị miss");
        
        // Gọi ReminderManager để kiểm tra
        ReminderManager.checkAndRestartMissedReminders(context);
    }
}