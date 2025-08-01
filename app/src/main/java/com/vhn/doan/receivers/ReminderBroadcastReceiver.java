package com.vhn.doan.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.vhn.doan.data.Reminder;
import com.vhn.doan.services.NotificationService;
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
            wakeLock.acquire(30000); // 30 seconds timeout

            String action = intent.getAction();
            Log.d(TAG, "=== NHẬN ĐƯỢC BROADCAST ===");
            Log.d(TAG, "Action: " + action);
            Log.d(TAG, "Time: " + new java.util.Date());

            if ("REMINDER_NOTIFICATION".equals(action)) {
                handleReminderNotification(context, intent);
            } else {
                Log.w(TAG, "Action không được nhận diện: " + action);
            }

        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong onReceive", e);
        } finally {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    /**
     * Xử lý hiển thị thông báo nhắc nhở với logging chi tiết
     */
    private void handleReminderNotification(Context context, Intent intent) {
        String reminderId = intent.getStringExtra("reminder_id");
        String title = intent.getStringExtra("reminder_title");
        String description = intent.getStringExtra("reminder_description");
        int repeatType = intent.getIntExtra("reminder_repeat_type", Reminder.RepeatType.NO_REPEAT);

        Log.d(TAG, "=== XỬ LÝ REMINDER NOTIFICATION ===");
        Log.d(TAG, "Reminder ID: " + reminderId);
        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "Description: " + description);
        Log.d(TAG, "Repeat Type: " + repeatType);

        if (reminderId == null || title == null) {
            Log.e(TAG, "❌ Thiếu thông tin reminder - bỏ qua");
            return;
        }

        try {
            // Tạo reminder object từ dữ liệu trong intent
            Reminder reminder = new Reminder();
            reminder.setId(reminderId);
            reminder.setTitle(title);
            reminder.setDescription(description != null ? description : "");
            reminder.setRepeatType(repeatType);
            reminder.setReminderTime(System.currentTimeMillis());
            reminder.setActive(true);

            Log.d(TAG, "Đang hiển thị thông báo...");

            // Hiển thị thông báo
            NotificationService notificationService = new NotificationService(context);
            notificationService.showReminderNotification(reminder);

            Log.d(TAG, "✅ Đã hiển thị thông báo thành công!");

            // Nếu là reminder lặp lại, lên lịch cho lần tiếp theo
            if (repeatType != Reminder.RepeatType.NO_REPEAT) {
                Log.d(TAG, "Reminder có lặp lại - lên lịch lần tiếp theo...");
                scheduleNextRepeatReminder(context, reminder);
            } else {
                Log.d(TAG, "Reminder không lặp lại - hoàn thành");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi xử lý reminder notification", e);
        }

        Log.d(TAG, "=== KẾT THÚC XỬ LÝ REMINDER ===");
    }

    /**
     * Lên lịch cho lần lặp lại tiếp theo với logging chi tiết
     */
    private void scheduleNextRepeatReminder(Context context, Reminder currentReminder) {
        try {
            Log.d(TAG, "=== LÊN LỊCH LẦN TIẾP THEO ===");

            // Tính thời gian tiếp theo
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            switch (currentReminder.getRepeatType()) {
                case Reminder.RepeatType.DAILY:
                    calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
                    Log.d(TAG, "Thêm 1 ngày");
                    break;
                case Reminder.RepeatType.WEEKLY:
                    calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1);
                    Log.d(TAG, "Thêm 1 tuần");
                    break;
                case Reminder.RepeatType.MONTHLY:
                    calendar.add(java.util.Calendar.MONTH, 1);
                    Log.d(TAG, "Thêm 1 tháng");
                    break;
                default:
                    Log.d(TAG, "Không lặp lại - bỏ qua");
                    return;
            }

            // Tạo reminder cho lần tiếp theo
            Reminder nextReminder = new Reminder();
            nextReminder.setId(currentReminder.getId());
            nextReminder.setTitle(currentReminder.getTitle());
            nextReminder.setDescription(currentReminder.getDescription());
            nextReminder.setRepeatType(currentReminder.getRepeatType());
            nextReminder.setReminderTime(calendar.getTimeInMillis());
            nextReminder.setActive(true);

            Log.d(TAG, "Thời gian lần tiếp theo: " + calendar.getTime());

            // Lên lịch cho lần tiếp theo
            ReminderService reminderService = new ReminderService(context);
            reminderService.scheduleReminder(nextReminder);

            Log.d(TAG, "✅ Đã lên lịch lần tiếp theo thành công!");
            Log.d(TAG, "Reminder: " + nextReminder.getTitle());
            Log.d(TAG, "Vào: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(calendar.getTime()));

        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi lên lịch lần tiếp theo", e);
        }
    }
}
