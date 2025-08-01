package com.vhn.doan.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.vhn.doan.services.NotificationService;

/**
 * BroadcastReceiver để xử lý các hành động từ thông báo nhắc nhở
 */
public class ReminderActionReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderActionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received action: " + action);

        if ("MARK_COMPLETE".equals(action)) {
            handleMarkComplete(context, intent);
        }
    }

    /**
     * Xử lý khi người dùng đánh dấu hoàn thành nhắc nhở
     */
    private void handleMarkComplete(Context context, Intent intent) {
        String reminderId = intent.getStringExtra("reminder_id");

        if (reminderId != null) {
            // Hủy thông báo
            NotificationService notificationService = new NotificationService(context);
            notificationService.cancelReminderNotification(reminderId);

            // Hiển thị thông báo Toast
            Toast.makeText(context, "Đã đánh dấu nhắc nhở hoàn thành", Toast.LENGTH_SHORT).show();

            Log.d(TAG, "Đã đánh dấu hoàn thành reminder: " + reminderId);

            // TODO: Có thể cập nhật trạng thái trong database nếu cần
        }
    }
}
