package com.vhn.doan.services;

import static com.vhn.doan.data.Reminder.RepeatType.DAILY;
import static com.vhn.doan.data.Reminder.RepeatType.MONTHLY;
import static com.vhn.doan.data.Reminder.RepeatType.WEEKLY;
import static com.vhn.doan.data.Reminder.RepeatType.NO_REPEAT;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.vhn.doan.data.Reminder;
import com.vhn.doan.presentation.reminder.AlarmActivity;
import com.vhn.doan.receivers.ReminderBroadcastReceiver;
import com.vhn.doan.workers.ReminderWorker;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Service để quản lý và lên lịch nhắc nhở với giao diện báo thức
 * Sử dụng cả AlarmManager và WorkManager để đảm bảo hoạt động trong mọi tình huống
 */
public class ReminderService {

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
     * Lên lịch nhắc nhở với giao diện báo thức
     */
    public void scheduleReminder(Reminder reminder) {
        if (reminder == null || reminder.getReminderTime() == null || !reminder.isActive()) {
            return;
        }

        long reminderTimeMillis = reminder.getReminderTime();
        long currentTime = System.currentTimeMillis();

        if (reminderTimeMillis <= currentTime) {
            return;
        }

        // Schedule với cả hai phương pháp để đảm bảo độ tin cậy
        scheduleWithAlarmManager(reminder, reminderTimeMillis);
        scheduleWithWorkManager(reminder, reminderTimeMillis - currentTime);
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
            intent.putExtra("sound_id", reminder.getSoundId());
            intent.putExtra("sound_uri", reminder.getSoundUri());
            intent.putExtra("vibrate", reminder.isVibrate());
            intent.putExtra("volume", reminder.getVolume());
            intent.putExtra("is_alarm_style", reminder.isAlarmStyle());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Sử dụng setAlarmClock để có độ ưu tiên cao nhất
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Tạo intent để mở AlarmActivity khi click notification từ alarm clock
                Intent showIntent = new Intent(context, AlarmActivity.class);
                showIntent.putExtra("reminder_id", reminder.getId());
                showIntent.putExtra("title", reminder.getTitle());
                showIntent.putExtra("message", reminder.getDescription());
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
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                );
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
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
                .putString(ReminderWorker.KEY_SOUND_ID, reminder.getSoundId())
                .putString(ReminderWorker.KEY_SOUND_URI, reminder.getSoundUri())
                .putBoolean(ReminderWorker.KEY_VIBRATE, reminder.isVibrate())
                .putInt(ReminderWorker.KEY_VOLUME, reminder.getVolume())
                .putBoolean(ReminderWorker.KEY_IS_ALARM_STYLE, reminder.isAlarmStyle())
                .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("reminder_" + reminder.getId())
                .build();

            WorkManager.getInstance(context).enqueue(workRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hủy nhắc nhở đã được lên lịch
     */
    public void cancelReminder(String reminderId) {
        if (reminderId == null || reminderId.isEmpty()) {
            return;
        }

        try {
            // Hủy AlarmManager
            Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
            intent.setAction(ACTION_REMINDER_TRIGGER);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }

            // Hủy WorkManager
            WorkManager.getInstance(context).cancelAllWorkByTag("reminder_" + reminderId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Lên lịch lặp lại cho reminder (nếu có)
     */
    public void scheduleNextRepeat(Reminder reminder) {
        if (reminder == null || reminder.getRepeatType() == Reminder.RepeatType.NO_REPEAT) {
            return;
        }

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(reminder.getReminderTime());

            // Tính toán thời gian lặp lại tiếp theo
            switch (reminder.getRepeatType()) {
                case DAILY:
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    break;
                case WEEKLY:
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case MONTHLY:
                    calendar.add(Calendar.MONTH, 1);
                    break;
                default:
                    return;
            }

            // Cập nhật thời gian mới cho reminder
            reminder.setReminderTime(calendar.getTimeInMillis());

            // Lên lịch với thời gian mới
            scheduleReminder(reminder);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Kiểm tra xem có quyền lên lịch alarm chính xác không
     */
    public boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    /**
     * Lên lịch nhắc nhở ngay lập tức (cho testing)
     */
    public void scheduleImmediateReminder(Reminder reminder) {
        if (reminder == null) {
            return;
        }

        // Lên lịch sau 5 giây để có thời gian chuẩn bị
        long reminderTime = System.currentTimeMillis() + 5000;
        reminder.setReminderTime(reminderTime);
        scheduleReminder(reminder);
    }

    /**
     * Lấy thời gian nhắc nhở tiếp theo cho reminder lặp lại
     */
    public long getNextReminderTime(Reminder reminder) {
        if (reminder == null || reminder.getRepeatType() == Reminder.RepeatType.NO_REPEAT) {
            return 0;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(reminder.getReminderTime());

        switch (reminder.getRepeatType()) {
            case DAILY:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case WEEKLY:
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case MONTHLY:
                calendar.add(Calendar.MONTH, 1);
                break;
            default:
                return 0;
        }

        return calendar.getTimeInMillis();
    }

    /**
     * Giải phóng tài nguyên
     */
    public void release() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}
