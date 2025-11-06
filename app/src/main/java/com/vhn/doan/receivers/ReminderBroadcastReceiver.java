package com.vhn.doan.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import com.vhn.doan.R;
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
     * CHIẾN LƯỢC MỚI: Hiển thị notification với fullScreenIntent + launch activity
     */
    private void showFullScreenAlarm(Context context, Reminder reminder) {
        try {
            Log.d(TAG, "🚨 Hiển thị alarm cho: " + reminder.getTitle());

            // Tạo intent cho AlarmActivity
            Intent alarmIntent = new Intent(context, AlarmActivity.class);
            alarmIntent.putExtra(AlarmActivity.EXTRA_REMINDER_ID, reminder.getId());
            alarmIntent.putExtra(AlarmActivity.EXTRA_TITLE, reminder.getTitle());
            alarmIntent.putExtra(AlarmActivity.EXTRA_MESSAGE, reminder.getDescription());
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP);

            android.app.PendingIntent fullScreenPendingIntent = android.app.PendingIntent.getActivity(
                context,
                reminder.getId().hashCode(),
                alarmIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
            );

            // BƯỚC 1: Tạo HIGH priority notification channel
            createHighPriorityAlarmChannel(context);

            // BƯỚC 2: Tạo notification với fullScreenIntent
            androidx.core.app.NotificationCompat.Builder builder =
                new androidx.core.app.NotificationCompat.Builder(context, "alarm_channel_urgent")
                    .setSmallIcon(R.drawable.ic_notification_reminder)
                    .setContentTitle("⏰ NHẮC NHỞ: " + reminder.getTitle())
                    .setContentText(reminder.getDescription())
                    .setStyle(new androidx.core.app.NotificationCompat.BigTextStyle()
                        .bigText(reminder.getDescription()))
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX)
                    .setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentIntent(fullScreenPendingIntent)
                    .setFullScreenIntent(fullScreenPendingIntent, true) // QUAN TRỌNG: fullScreenIntent
                    // ÂM THANH + RUNG
                    .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
                    .setVibrate(new long[]{0, 1000, 500, 1000, 500, 1000});

            // BƯỚC 3: Hiển thị notification
            android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                int notificationId = 9000 + reminder.getId().hashCode();
                notificationManager.notify(notificationId, builder.build());
                Log.d(TAG, "✅ Notification hiển thị với fullScreenIntent, ID: " + notificationId);
            }

            // BƯỚC 4: Thử launch activity (sẽ work nếu app ở foreground)
            try {
                context.startActivity(alarmIntent);
                Log.d(TAG, "✅ AlarmActivity launched");
            } catch (Exception activityException) {
                Log.w(TAG, "⚠️ Không thể launch activity từ background (expected behavior), notification sẽ handle việc này");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi hiển thị alarm", e);
            // Final fallback: notification thường
            NotificationService notificationService = new NotificationService(context);
            notificationService.showReminderNotification(reminder);
        }
    }

    /**
     * Tạo notification channel với độ ưu tiên CAO NHẤT cho alarm
     */
    private void createHighPriorityAlarmChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                "alarm_channel_urgent",
                "Báo thức khẩn cấp",
                android.app.NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo báo thức toàn màn hình");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            channel.setBypassDnd(true); // Bypass Do Not Disturb
            channel.setSound(
                android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM),
                new android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            );

            android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Hiển thị notification thường với âm thanh và rung
     * Sử dụng khi user không chọn alarm style
     * CHIẾN LƯỢC: Giống showFullScreenAlarm nhưng không có overlay
     */
    private void showNotificationWithSound(Context context, Reminder reminder) {
        try {
            Log.d(TAG, "🔔 Hiển thị notification thường với âm thanh");

            // Tạo intent cho AlarmActivity (khi user tap notification)
            Intent alarmIntent = new Intent(context, AlarmActivity.class);
            alarmIntent.putExtra(AlarmActivity.EXTRA_REMINDER_ID, reminder.getId());
            alarmIntent.putExtra(AlarmActivity.EXTRA_TITLE, reminder.getTitle());
            alarmIntent.putExtra(AlarmActivity.EXTRA_MESSAGE, reminder.getDescription());
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP);

            android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                context,
                reminder.getId().hashCode(),
                alarmIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
            );

            // Tạo notification channel với HIGH priority
            createHighPriorityNotificationChannel(context);

            // Tạo notification với HIGH priority
            androidx.core.app.NotificationCompat.Builder builder =
                new androidx.core.app.NotificationCompat.Builder(context, "reminder_channel_high")
                    .setSmallIcon(R.drawable.ic_notification_reminder)
                    .setContentTitle("🔔 Nhắc nhở: " + reminder.getTitle())
                    .setContentText(reminder.getDescription())
                    .setStyle(new androidx.core.app.NotificationCompat.BigTextStyle()
                        .bigText(reminder.getDescription()))
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                    .setCategory(androidx.core.app.NotificationCompat.CATEGORY_REMINDER)
                    .setAutoCancel(true) // Tự động dismiss khi tap
                    .setOngoing(false) // Có thể swipe away
                    .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentIntent(pendingIntent)
                    // ÂM THANH + RUNG
                    .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
                    .setVibrate(new long[]{0, 500, 250, 500}); // Rung nhẹ hơn alarm

            // Hiển thị notification
            android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                int notificationId = 8000 + reminder.getId().hashCode();
                notificationManager.notify(notificationId, builder.build());
                Log.d(TAG, "✅ Notification thường hiển thị, ID: " + notificationId);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi hiển thị notification", e);
        }
    }

    /**
     * Tạo notification channel cho notification thường (không phải alarm)
     */
    private void createHighPriorityNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                "reminder_channel_high",
                "Nhắc nhở sức khỏe",
                android.app.NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo nhắc nhở sức khỏe");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            channel.setSound(
                android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION),
                new android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            );

            android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
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
