package com.vhn.doan.workers;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.vhn.doan.data.Reminder;
import com.vhn.doan.data.repository.ReminderRepository;
import com.vhn.doan.data.repository.ReminderRepositoryImpl;
import com.vhn.doan.presentation.reminder.AlarmActivity;
import com.vhn.doan.services.NotificationService;

/**
 * WorkManager Worker để xử lý nhắc nhở với giao diện báo thức
 * Hoạt động mạnh mẽ hơn AlarmManager trong việc xử lý Doze mode
 */
public class ReminderWorker extends Worker {

    private static final String TAG = "ReminderWorker";

    // Input data keys
    public static final String KEY_REMINDER_ID = "reminder_id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_SOUND_ID = "sound_id";
    public static final String KEY_SOUND_URI = "sound_uri";
    public static final String KEY_VIBRATE = "vibrate";
    public static final String KEY_VOLUME = "volume";
    public static final String KEY_IS_ALARM_STYLE = "is_alarm_style";

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Acquire wake lock để đảm bảo thiết bị không sleep
        PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "HealthTips:ReminderWorker"
        );

        try {
            wakeLock.acquire(30 * 1000); // 30 giây timeout

            // Lấy dữ liệu từ input
            String reminderId = getInputData().getString(KEY_REMINDER_ID);
            String title = getInputData().getString(KEY_TITLE);
            String message = getInputData().getString(KEY_MESSAGE);
            String soundId = getInputData().getString(KEY_SOUND_ID);
            String soundUri = getInputData().getString(KEY_SOUND_URI);
            boolean vibrate = getInputData().getBoolean(KEY_VIBRATE, true);
            int volume = getInputData().getInt(KEY_VOLUME, 80);
            boolean isAlarmStyle = getInputData().getBoolean(KEY_IS_ALARM_STYLE, true);

            Log.d(TAG, "🔔 WorkManager nhắc nhở được kích hoạt: " + title + " (ID: " + reminderId + ")");

            if (reminderId == null || title == null) {
                Log.e(TAG, "❌ Thiếu thông tin reminderId hoặc title");
                return Result.failure();
            }

            // Kiểm tra xem reminder còn active không trước khi hiển thị
            ReminderRepository repository = new ReminderRepositoryImpl();
            repository.getReminderById(reminderId, new ReminderRepository.RepositoryCallback<Reminder>() {
                @Override
                public void onSuccess(Reminder reminder) {
                    if (reminder != null && reminder.isActive()) {
                        // Chỉ hiển thị nếu reminder vẫn còn active
                        showReminderAlarm(reminderId, title, message);
                    } else {
                        Log.d(TAG, "⏭️ Reminder đã bị tắt hoặc không tồn tại, bỏ qua hiển thị");
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Lỗi khi kiểm tra reminder: " + error);
                    // Fallback: vẫn hiển thị alarm
                    showReminderAlarm(reminderId, title, message);
                }
            });

            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi trong ReminderWorker", e);
            return Result.failure();
        } finally {
            // Release wake lock
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    /**
     * Hiển thị giao diện alarm
     */
    private void showReminderAlarm(String reminderId, String title, String message) {
        try {
            Log.d(TAG, "🚨 Hiển thị alarm cho reminder: " + title);

            // Khởi động AlarmActivity
            AlarmActivity.startAlarm(getApplicationContext(), reminderId, title, message);

        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi hiển thị alarm", e);

            // Fallback: hiển thị notification
            try {
                NotificationService.showReminderNotification(
                    getApplicationContext(),
                    title,
                    message != null ? message : "Đã đến giờ nhắc nhở!",
                    reminderId
                );
            } catch (Exception fallbackError) {
                Log.e(TAG, "❌ Lỗi cả khi hiển thị notification fallback", fallbackError);
            }
        }
    }
}
