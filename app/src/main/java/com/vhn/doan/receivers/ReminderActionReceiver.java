package com.vhn.doan.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.vhn.doan.data.repository.ReminderRepository;
import com.vhn.doan.data.repository.ReminderRepositoryImpl;
import com.vhn.doan.services.NotificationService;

/**
 * BroadcastReceiver để xử lý các action từ notification nhắc nhở
 */
public class ReminderActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String reminderId = intent.getStringExtra("reminder_id");

        if ("MARK_COMPLETE".equals(action) && reminderId != null) {
            markReminderComplete(context, reminderId);
        }
    }

    /**
     * Đánh dấu nhắc nhở đã hoàn thành
     */
    private void markReminderComplete(Context context, String reminderId) {
        ReminderRepository reminderRepository = new ReminderRepositoryImpl();

        // Tắt nhắc nhở
        reminderRepository.toggleReminder(reminderId, false, new ReminderRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Hủy notification
                NotificationService notificationService = new NotificationService(context);
                notificationService.cancelReminderNotification(reminderId);

                // Hiển thị thông báo thành công
                Toast.makeText(context, "Đã đánh dấu nhắc nhở hoàn thành", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(context, "Lỗi khi cập nhật nhắc nhở: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
