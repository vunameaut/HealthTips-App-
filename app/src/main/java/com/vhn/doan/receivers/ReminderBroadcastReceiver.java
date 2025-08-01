package com.vhn.doan.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vhn.doan.data.Reminder;
import com.vhn.doan.services.NotificationService;

/**
 * BroadcastReceiver để xử lý khi thời gian nhắc nhở đã đến
 */
public class ReminderBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if ("REMINDER_NOTIFICATION".equals(action)) {
            handleReminderNotification(context, intent);
        }
    }

    /**
     * Xử lý hiển thị thông báo nhắc nhở
     */
    private void handleReminderNotification(Context context, Intent intent) {
        String reminderId = intent.getStringExtra("reminder_id");
        String title = intent.getStringExtra("reminder_title");
        String description = intent.getStringExtra("reminder_description");

        if (reminderId != null && title != null) {
            // Tạo reminder object từ dữ liệu trong intent
            Reminder reminder = new Reminder();
            reminder.setId(reminderId);
            reminder.setTitle(title);
            reminder.setDescription(description != null ? description : "");

            // Hiển thị thông báo
            NotificationService notificationService = new NotificationService(context);
            notificationService.showReminderNotification(reminder);
        }
    }
}
