package com.vhn.doan.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.vhn.doan.data.Reminder;
import com.vhn.doan.receivers.ReminderBroadcastReceiver;
import com.vhn.doan.workers.ReminderWorker;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Service để quản lý và lên lịch thông báo nhắc nhở
 * Sử dụng cả AlarmManager và WorkManager để đảm bảo hoạt động trong mọi tình huống
 */
public class ReminderService {

    private static final String TAG = "ReminderService";
    public static final String ACTION_REMINDER_TRIGGER = "com.vhn.doan.REMINDER_TRIGGER";

    private Context context;
    private AlarmManager alarmManager;
    private PowerManager.WakeLock wakeLock;

    public ReminderService(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Tạo wake lock để đảm bảo thiết bị không sleep khi xử lý alarm
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HealthTips:ReminderWakeLock");
    }

    /**
     * Lên lịch thông báo nhắc nhở với cả AlarmManager và WorkManager
     */
    public void scheduleReminder(Reminder reminder) {
        Log.d(TAG, "=== BẮT ĐẦU SCHEDULE REMINDER ===");
        Log.d(TAG, "Reminder ID: " + reminder.getId());
        Log.d(TAG, "Title: " + reminder.getTitle());
        Log.d(TAG, "Description: " + reminder.getDescription());
        Log.d(TAG, "Is Active: " + reminder.isActive());
        Log.d(TAG, "Repeat Type: " + reminder.getRepeatType());

        if (reminder == null || reminder.getReminderTime() == null || !reminder.isActive()) {
            Log.w(TAG, "Reminder không hợp lệ - bỏ qua scheduling");
            return;
        }

        long reminderTimeMillis = reminder.getReminderTime(); // getReminderTime() đã trả về Long
        long currentTime = System.currentTimeMillis();

        Log.d(TAG, "Current time: " + new Date(currentTime));
        Log.d(TAG, "Reminder time: " + new Date(reminderTimeMillis));

        if (reminderTimeMillis <= currentTime) {
            Log.w(TAG, "Thời gian nhắc nhở đã qua - bỏ qua scheduling");
            return;
        }

        // Schedule với cả hai phương pháp để đảm bảo độ tin cậy
        scheduleWithAlarmManager(reminder, reminderTimeMillis);
        scheduleWithWorkManager(reminder, reminderTimeMillis - currentTime);

        Log.d(TAG, "=== KẾT THÚC SCHEDULE REMINDER ===");
    }

    /**
     * Schedule với AlarmManager (cho instant delivery)
     */
    private void scheduleWithAlarmManager(Reminder reminder, long reminderTimeMillis) {
        try {
            Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
            intent.setAction(ACTION_REMINDER_TRIGGER);
            intent.putExtra("reminder_id", reminder.getId());
            intent.putExtra("title", reminder.getTitle());
            intent.putExtra("message", reminder.getDescription());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Sử dụng setAlarmClock để có độ ưu tiên cao nhất (bỏ qua Doze mode hoàn toàn)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Tạo intent để mở app khi click notification từ alarm clock
                Intent showIntent = new Intent(context, com.vhn.doan.presentation.home.HomeActivity.class);
                showIntent.putExtra("open_reminders", true);
                PendingIntent showPendingIntent = PendingIntent.getActivity(
                    context,
                    reminder.getId().hashCode() + 1000,
                    showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(
                    reminderTimeMillis,
                    showPendingIntent
                );

                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
                Log.d(TAG, "AlarmManager: Đã schedule với setAlarmClock (cao nhất priority)");
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                );
                Log.d(TAG, "AlarmManager: Đã schedule với setExactAndAllowWhileIdle");
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                );
                Log.d(TAG, "AlarmManager: Đã schedule với setExact");
            }

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi schedule AlarmManager", e);
        }
    }

    /**
     * Schedule với WorkManager (backup cho reliability)
     */
    private void scheduleWithWorkManager(Reminder reminder, long delayMillis) {
        try {
            Data inputData = new Data.Builder()
                .putString(ReminderWorker.KEY_REMINDER_ID, reminder.getId())
                .putString(ReminderWorker.KEY_TITLE, reminder.getTitle())
                .putString(ReminderWorker.KEY_MESSAGE, reminder.getDescription())
                .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("reminder_" + reminder.getId())
                .build();

            WorkManager.getInstance(context).enqueue(workRequest);
            Log.d(TAG, "WorkManager: Đã schedule work request với delay " + delayMillis + "ms");

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi schedule WorkManager", e);
        }
    }

    /**
     * Hủy thông báo nhắc nhở
     */
    public static void cancelReminder(Context context, String reminderId) {
        Log.d(TAG, "Hủy reminder: " + reminderId);

        try {
            // Hủy AlarmManager
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
            intent.setAction(ACTION_REMINDER_TRIGGER);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "Đã hủy AlarmManager cho reminder: " + reminderId);

            // Hủy WorkManager
            WorkManager.getInstance(context).cancelAllWorkByTag("reminder_" + reminderId);
            Log.d(TAG, "Đã hủy WorkManager cho reminder: " + reminderId);

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hủy reminder", e);
        }
    }

    /**
     * Test thông báo ngay lập tức
     */
    public void testNotification(Reminder reminder) {
        Log.d(TAG, "Test thông báo cho reminder: " + reminder.getTitle());

        // Hiển thị thông báo ngay
        NotificationService.showReminderNotification(
            context,
            reminder.getTitle(),
            reminder.getDescription(),
            reminder.getId()
        );

        // Schedule một thông báo test sau 5 giây
        scheduleTestNotification(reminder, 5000);
    }

    /**
     * Schedule test notification sau một khoảng thời gian ngắn
     */
    private void scheduleTestNotification(Reminder reminder, long delayMillis) {
        try {
            long triggerTime = System.currentTimeMillis() + delayMillis;

            // Test với AlarmManager
            Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
            intent.setAction(ACTION_REMINDER_TRIGGER);
            intent.putExtra("reminder_id", reminder.getId());
            intent.putExtra("title", "[TEST] " + reminder.getTitle());
            intent.putExtra("message", "[TEST] " + reminder.getDescription());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                ("test_" + reminder.getId()).hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                );
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                );
            }

            Log.d(TAG, "Đã schedule test notification sau " + delayMillis + "ms");

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi schedule test notification", e);
        }
    }
}
