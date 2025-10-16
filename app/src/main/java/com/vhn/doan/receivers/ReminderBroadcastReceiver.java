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

    private void handleReminderTrigger(Context context, Intent intent) {
        try {
            String reminderId = intent.getStringExtra("reminder_id");
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");
            String soundId = intent.getStringExtra("sound_id");
            String soundUri = intent.getStringExtra("sound_uri");
            boolean vibrate = intent.getBooleanExtra("vibrate", true);
            int volume = intent.getIntExtra("volume", 80);
            boolean isAlarmStyle = intent.getBooleanExtra("is_alarm_style", true);

            if (reminderId == null || title == null) {
                return;
            }

            // Khởi động AlarmActivity thay vì hiển thị notification
            if (isAlarmStyle) {
                AlarmActivity.startAlarm(context, reminderId, title, message);
            } else {
                // Fallback: hiển thị notification nếu không dùng alarm style
                // (có thể implement sau nếu cần)
                AlarmActivity.startAlarm(context, reminderId, title, message);
            }

            // Cập nhật trạng thái reminder trong database
            updateReminderStatus(context, reminderId);

            // Lên lịch lặp lại nếu cần
            scheduleNextRepeat(context, reminderId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSystemReboot(Context context) {
        try {
            // Khôi phục các reminder đã được lên lịch sau khi reboot
            ReminderRepository repository = new ReminderRepositoryImpl();
            // Implementation sẽ được thêm vào ReminderRepository để lấy active reminders
            // và lên lịch lại chúng
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateReminderStatus(Context context, String reminderId) {
        try {
            ReminderRepository repository = new ReminderRepositoryImpl();
            // Cập nhật lastNotified time
            // Implementation sẽ được thêm vào ReminderRepository
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scheduleNextRepeat(Context context, String reminderId) {
        try {
            ReminderRepository repository = new ReminderRepositoryImpl();
            // Lấy thông tin reminder và lên lịch lần tiếp theo nếu có repeat
            // Implementation sẽ được thêm vào ReminderRepository
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
