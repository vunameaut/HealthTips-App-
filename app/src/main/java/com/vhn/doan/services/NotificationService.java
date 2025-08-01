package com.vhn.doan.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.vhn.doan.R;
import com.vhn.doan.data.Reminder;
import com.vhn.doan.presentation.home.HomeActivity;
import com.vhn.doan.receivers.ReminderActionReceiver;

/**
 * Service để xử lý thông báo nhắc nhở
 */
public class NotificationService {

    private static final String REMINDER_CHANNEL_ID = "reminder_channel";
    private static final String REMINDER_CHANNEL_NAME = "Nhắc nhở sức khỏe";
    private static final String REMINDER_CHANNEL_DESCRIPTION = "Thông báo nhắc nhở về sức khỏe";
    private static final int REMINDER_NOTIFICATION_ID = 1001;

    private Context context;
    private NotificationManager notificationManager;

    public NotificationService(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    /**
     * Tạo notification channel cho Android 8.0+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                REMINDER_CHANNEL_ID,
                REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(REMINDER_CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{1000, 1000, 1000, 1000, 1000});
            channel.setLightColor(android.graphics.Color.BLUE);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Hiển thị thông báo nhắc nhở
     */
    public void showReminderNotification(Reminder reminder) {
        if (reminder == null) return;

        // Tạo intent để mở app khi click notification
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("open_reminders", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Tạo notification với âm thanh và rung
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_reminder)
            .setContentTitle("🔔 Nhắc nhở: " + reminder.getTitle())
            .setContentText(reminder.getDescription())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setOngoing(false)
            .setContentIntent(pendingIntent)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(reminder.getDescription())
                .setBigContentTitle("🔔 Nhắc nhở: " + reminder.getTitle()))
            .addAction(
                R.drawable.ic_check,
                "✓ Hoàn thành",
                createMarkCompleteIntent(reminder)
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_REMINDER);

        // Hiển thị notification
        try {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            if (notificationManagerCompat.areNotificationsEnabled()) {
                int notificationId = REMINDER_NOTIFICATION_ID + reminder.getId().hashCode();
                notificationManagerCompat.notify(notificationId, builder.build());

                android.util.Log.d("NotificationService", "Đã hiển thị thông báo cho reminder: " +
                    reminder.getTitle() + " với ID: " + notificationId);
            } else {
                android.util.Log.w("NotificationService", "Thông báo bị tắt bởi người dùng");
            }
        } catch (SecurityException e) {
            android.util.Log.e("NotificationService", "Không có quyền hiển thị thông báo", e);
        }
    }

    /**
     * Tạo PendingIntent để đánh dấu hoàn thành nhắc nhở
     */
    private PendingIntent createMarkCompleteIntent(Reminder reminder) {
        Intent intent = new Intent(context, ReminderActionReceiver.class);
        intent.setAction("MARK_COMPLETE");
        intent.putExtra("reminder_id", reminder.getId());

        return PendingIntent.getBroadcast(
            context,
            reminder.getId().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    /**
     * Hủy thông báo nhắc nhở
     */
    public void cancelReminderNotification(String reminderId) {
        if (reminderId != null) {
            notificationManager.cancel(REMINDER_NOTIFICATION_ID + reminderId.hashCode());
        }
    }

    /**
     * Hủy tất cả thông báo nhắc nhở
     */
    public void cancelAllReminderNotifications() {
        notificationManager.cancelAll();
    }

    /**
     * Kiểm tra xem notification có được bật hay không
     */
    public boolean areNotificationsEnabled() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        return notificationManagerCompat.areNotificationsEnabled();
    }

    /**
     * Hiển thị thông báo nhắc nhở với các tham số riêng lẻ (static method)
     */
    public static void showReminderNotification(Context context, String title, String message, String reminderId) {
        try {
            NotificationService service = new NotificationService(context);

            // Tạo một Reminder object tạm thời để sử dụng method hiện tại
            Reminder tempReminder = new Reminder();
            tempReminder.setId(reminderId);
            tempReminder.setTitle(title);
            tempReminder.setDescription(message);

            service.showReminderNotification(tempReminder);
            
            android.util.Log.d("NotificationService", "Đã hiển thị thông báo: " + title);
        } catch (Exception e) {
            android.util.Log.e("NotificationService", "Lỗi khi hiển thị thông báo", e);
        }
    }
}
