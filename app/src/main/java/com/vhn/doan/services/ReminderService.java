package com.vhn.doan.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.vhn.doan.data.Reminder;
import com.vhn.doan.receivers.ReminderBroadcastReceiver;

import java.util.Date;

/**
 * Service để quản lý và lên lịch thông báo nhắc nhở
 */
public class ReminderService {

    private Context context;
    private AlarmManager alarmManager;

    public ReminderService(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * Lên lịch thông báo nhắc nhở
     */
    public void scheduleReminder(Reminder reminder) {
        if (reminder == null || reminder.getReminderTime() == null || !reminder.isActive()) {
            return;
        }

        // Không lên lịch cho thời gian trong quá khứ
        if (reminder.getReminderTime().getTime() <= System.currentTimeMillis()) {
            return;
        }

        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
        intent.putExtra("reminder_id", reminder.getId());
        intent.putExtra("reminder_title", reminder.getTitle());
        intent.putExtra("reminder_description", reminder.getDescription());
        intent.setAction("REMINDER_NOTIFICATION");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.getId().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Sử dụng setExactAndAllowWhileIdle cho Android 6.0+
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.getReminderTime().getTime(),
                    pendingIntent
                );
            } else {
                // Sử dụng setExact cho Android cũ hơn
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminder.getReminderTime().getTime(),
                    pendingIntent
                );
            }

            // Lên lịch cho lần tiếp theo nếu là reminder lặp lại
            scheduleNextRepeat(reminder);

        } catch (SecurityException e) {
            // Xử lý trường hợp không có quyền SCHEDULE_EXACT_ALARM (Android 12+)
            e.printStackTrace();
        }
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
            nextReminder.setReminderTime(nextTime);
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
     * Kiểm tra xem có quyền lên lịch exact alarm hay không (Android 12+)
     */
    public boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }
}
