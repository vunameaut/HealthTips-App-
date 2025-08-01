package com.vhn.doan.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import com.vhn.doan.data.Reminder;
import com.vhn.doan.receivers.ReminderBroadcastReceiver;

import java.util.Calendar;
import java.util.Date;

/**
 * Service để quản lý và lên lịch thông báo nhắc nhở
 */
public class ReminderService {

    private static final String TAG = "ReminderService";
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
     * Lên lịch thông báo nhắc nhở với logging chi tiết
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

        long reminderTime = reminder.getReminderTime();
        long currentTime = System.currentTimeMillis();

        Log.d(TAG, "Reminder Time: " + new Date(reminderTime));
        Log.d(TAG, "Current Time: " + new Date(currentTime));
        Log.d(TAG, "Time difference: " + (reminderTime - currentTime) + "ms");

        // Kiểm tra quyền exact alarm trước khi schedule
        if (!canScheduleExactAlarms()) {
            Log.e(TAG, "Không có quyền SCHEDULE_EXACT_ALARM!");
            return;
        }

        // Nếu là reminder lặp lại và thời gian đã qua, tính thời gian tiếp theo
        if (reminderTime <= currentTime && reminder.getRepeatType() != Reminder.RepeatType.NO_REPEAT) {
            Log.d(TAG, "Reminder đã qua và có lặp lại - tính thời gian tiếp theo");
            Date nextTime = calculateNextReminderTime(reminder, currentTime);
            if (nextTime != null) {
                reminderTime = nextTime.getTime();
                reminder.setReminderTime(reminderTime);
                Log.d(TAG, "Thời gian tiếp theo: " + nextTime);
            } else {
                Log.w(TAG, "Không thể tính thời gian tiếp theo");
                return;
            }
        } else if (reminderTime <= currentTime) {
            Log.d(TAG, "Reminder không lặp lại và đã qua - tự động tắt");
            disableExpiredReminder(reminder);
            return;
        }

        // Tạo intent với tất cả thông tin cần thiết
        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
        intent.putExtra("reminder_id", reminder.getId());
        intent.putExtra("reminder_title", reminder.getTitle());
        intent.putExtra("reminder_description", reminder.getDescription());
        intent.putExtra("reminder_repeat_type", reminder.getRepeatType());
        intent.setAction("REMINDER_NOTIFICATION");

        // Tạo unique request code để tránh conflict
        int requestCode = reminder.getId().hashCode();
        Log.d(TAG, "Request Code: " + requestCode);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            // Acquire wake lock trước khi schedule
            if (!wakeLock.isHeld()) {
                wakeLock.acquire(10000); // 10 seconds timeout
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG, "Sử dụng setExactAndAllowWhileIdle cho Android 6.0+");
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                );
            } else {
                Log.d(TAG, "Sử dụng setExact cho Android cũ");
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                );
            }

            Log.d(TAG, "✅ ĐÃ LÊN LỊCH THÀNH CÔNG!");
            Log.d(TAG, "Reminder: " + reminder.getTitle());
            Log.d(TAG, "Thời gian: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(reminderTime)));
            Log.d(TAG, "=== KẾT THÚC SCHEDULE REMINDER ===");

        } catch (SecurityException e) {
            Log.e(TAG, "❌ LỖI QUYỀN: Không thể lên lịch exact alarm", e);

            // Thử fallback với setWindow nếu không có quyền exact
            try {
                Log.d(TAG, "Thử fallback với setWindow...");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setWindow(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        60000, // 1 minute window
                        pendingIntent
                    );
                    Log.d(TAG, "Đã schedule với setWindow thành công");
                }
            } catch (Exception fallbackError) {
                Log.e(TAG, "Fallback cũng thất bại", fallbackError);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ LỖI KHÔNG XÁC ĐỊNH khi schedule reminder", e);
        } finally {
            // Release wake lock
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    /**
     * Tính toán thời gian nhắc nhở tiếp theo cho reminder lặp lại
     */
    private Date calculateNextReminderTime(Reminder reminder, long currentTime) {
        Date reminderDate = new Date(reminder.getReminderTime());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(reminderDate);

        // Tìm thời gian tiếp theo sau current time
        while (calendar.getTimeInMillis() <= currentTime) {
            switch (reminder.getRepeatType()) {
                case Reminder.RepeatType.DAILY:
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    break;
                case Reminder.RepeatType.WEEKLY:
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case Reminder.RepeatType.MONTHLY:
                    calendar.add(Calendar.MONTH, 1);
                    break;
                default:
                    return null;
            }
        }

        return calendar.getTime();
    }

    /**
     * Tự động tắt reminder đã hết hạn (không lặp lại)
     */
    private void disableExpiredReminder(Reminder reminder) {
        // TODO: Cập nhật trong database để tắt reminder
        android.util.Log.d("ReminderService", "Reminder đã hết hạn và bị tắt: " + reminder.getTitle());
    }

    /**
     * Hủy lịch thông báo nhắc nhở
     */
    public void cancelReminder(String reminderId) {
        if (reminderId == null) return;

        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
        intent.setAction("REMINDER_NOTIFICATION");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    /**
     * Cập nhật lịch nhắc nhở
     */
    public void updateReminder(Reminder reminder) {
        // Hủy lịch cũ
        cancelReminder(reminder.getId());

        // Lên lịch mới nếu reminder vẫn còn hoạt động
        if (reminder.isActive()) {
            scheduleReminder(reminder);
        }
    }

    /**
     * Lên lịch cho lần lặp lại tiếp theo
     */
    private void scheduleNextRepeat(Reminder reminder) {
        if (reminder.getRepeatType() == Reminder.RepeatType.NO_REPEAT) {
            return;
        }

        Date nextTime = reminder.getNextReminderTime();
        if (nextTime != null && nextTime.getTime() > System.currentTimeMillis()) {
            // Tạo reminder mới cho lần tiếp theo
            Reminder nextReminder = new Reminder();
            nextReminder.setId(reminder.getId() + "_next");
            nextReminder.setUserId(reminder.getUserId());
            nextReminder.setTitle(reminder.getTitle());
            nextReminder.setDescription(reminder.getDescription());
            nextReminder.setReminderTime(nextTime.getTime());
            nextReminder.setRepeatType(reminder.getRepeatType());
            nextReminder.setActive(reminder.isActive());
            nextReminder.setHealthTipId(reminder.getHealthTipId());

            scheduleReminder(nextReminder);
        }
    }

    /**
     * Lên lịch lại tất cả nhắc nhở hoạt động (thường dùng sau khi restart app)
     */
    public void rescheduleAllActiveReminders(java.util.List<Reminder> reminders) {
        if (reminders == null) return;

        for (Reminder reminder : reminders) {
            if (reminder.isActive() && reminder.getReminderTime() != null) {
                scheduleReminder(reminder);
            }
        }
    }

    /**
     * Kiểm tra xem có quyền lên lịch exact alarm hay không với logging
     */
    public boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            boolean canSchedule = alarmManager.canScheduleExactAlarms();
            Log.d(TAG, "Can schedule exact alarms: " + canSchedule);
            return canSchedule;
        }
        Log.d(TAG, "Android version < 31, exact alarms allowed by default");
        return true;
    }

    /**
     * Release wake lock khi service bị destroy
     */
    public void release() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}
