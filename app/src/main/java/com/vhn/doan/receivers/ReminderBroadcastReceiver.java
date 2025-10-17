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
import com.vhn.doan.services.ReminderService;

/**
 * BroadcastReceiver để xử lý khi thời gian nhắc nhở đã đến
 * - Khởi động AlarmActivity thay vì hiển thị notification
 * - Cập nhật trạng thái reminder
 */
public class ReminderBroadcastReceiver extends BroadcastReceiver {

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

            String action = intent.getAction();
            if (action == null) {
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
            e.printStackTrace();
        } finally {
            // Release wake lock
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    /**
     * Xử lý khi nhắc nhở được kích hoạt
     */
    private void handleReminderTrigger(Context context, Intent intent) {
        String reminderId = intent.getStringExtra("reminder_id");
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        if (reminderId == null || title == null) {
            return;
        }

        // Kiểm tra reminder còn active không
        ReminderRepository repository = new ReminderRepositoryImpl();
        repository.getReminderById(reminderId, new ReminderRepository.RepositoryCallback<Reminder>() {
            @Override
            public void onSuccess(Reminder reminder) {
                if (reminder != null && reminder.isActive()) {
                    // Khởi động AlarmActivity
                    AlarmActivity.startAlarm(context, reminderId, title, message);

                    // Lên lịch lặp lại nếu cần
                    if (reminder.getRepeatType() != Reminder.RepeatType.NO_REPEAT) {
                        ReminderService reminderService = new ReminderService(context);
                        reminderService.scheduleNextRepeat(reminder);
                    }
                }
            }

            @Override
            public void onError(String error) {
                // Fallback: vẫn hiển thị alarm
                AlarmActivity.startAlarm(context, reminderId, title, message);
            }
        });
    }

    /**
     * Xử lý khi hệ thống khởi động lại
     */
    private void handleSystemReboot(Context context) {
        // Lên lịch lại tất cả các reminder đang active
        ReminderRepository repository = new ReminderRepositoryImpl();
        // Tạm thời bỏ qua việc lấy userId - cần cải thiện trong tương lai
        // Có thể lưu userId vào SharedPreferences hoặc cơ chế khác
    }
}
