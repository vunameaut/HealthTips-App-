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

            String reminderId = getInputData().getString(KEY_REMINDER_ID);
            String title = getInputData().getString(KEY_TITLE);
            String message = getInputData().getString(KEY_MESSAGE);
            boolean isAlarmStyle = getInputData().getBoolean(KEY_IS_ALARM_STYLE, true);

            if (reminderId == null || title == null) {
                Log.e(TAG, "Thiếu thông tin reminder ID hoặc title");
                return Result.failure();
            }

            Log.d(TAG, "Xử lý reminder: " + reminderId + " - " + title);

            // Khởi động AlarmActivity với các thông tin đã truyền
            if (isAlarmStyle) {
                AlarmActivity.startAlarm(getApplicationContext(), reminderId, title, message);
            } else {
                // Fallback: sử dụng notification service nếu không dùng alarm style
                showFallbackNotification(reminderId, title, message);
            }

            // Cập nhật trạng thái reminder
            updateReminderStatus(reminderId);

            // Lên lịch lặp lại nếu cần
            scheduleNextRepeat(reminderId);

            Log.d(TAG, "Hoàn thành xử lý reminder: " + reminderId);
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong quá trình xử lý reminder", e);
            return Result.failure();
        } finally {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    private void showFallbackNotification(String reminderId, String title, String message) {
        try {
            NotificationService notificationService = new NotificationService(getApplicationContext());

            // Tạo intent để mở ứng dụng khi tap notification
            Intent intent = new Intent(getApplicationContext(), AlarmActivity.class);
            intent.putExtra(AlarmActivity.EXTRA_REMINDER_ID, reminderId);
            intent.putExtra(AlarmActivity.EXTRA_TITLE, title);
            intent.putExtra(AlarmActivity.EXTRA_MESSAGE, message);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Gửi notification với high priority để có thể hiển thị ngay cả khi màn hình tắt
            notificationService.showReminderNotification(
                reminderId.hashCode(), // Sử dụng hash code làm notification ID
                title,
                message != null ? message : "Đã đến giờ thực hiện!",
                intent
            );

            Log.d(TAG, "Đã hiển thị fallback notification cho: " + reminderId);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi hiển thị fallback notification", e);
        }
    }

    private void updateReminderStatus(String reminderId) {
        try {
            ReminderRepository repository = new ReminderRepositoryImpl();

            // Lấy thông tin reminder hiện tại
            repository.getReminderById(reminderId, new ReminderRepository.RepositoryCallback<Reminder>() {
                @Override
                public void onSuccess(Reminder reminder) {
                    if (reminder != null) {
                        // Cập nhật thời gian thông báo cuối cùng
                        reminder.setLastNotified(System.currentTimeMillis());

                        // Nếu là reminder không lặp lại, đánh dấu là completed
                        if (reminder.getRepeatType() == Reminder.RepeatType.NO_REPEAT) {
                            reminder.setCompleted(true);
                            reminder.setActive(false);
                        }

                        // Cập nhật vào database
                        repository.updateReminder(reminder, new ReminderRepository.RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                Log.d(TAG, "Cập nhật trạng thái reminder thành công: " + reminderId);
                            }

                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "Lỗi cập nhật reminder status: " + error);
                            }
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Lỗi lấy thông tin reminder: " + error);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong updateReminderStatus", e);
        }
    }

    private void scheduleNextRepeat(String reminderId) {
        try {
            ReminderRepository repository = new ReminderRepositoryImpl();

            repository.getReminderById(reminderId, new ReminderRepository.RepositoryCallback<Reminder>() {
                @Override
                public void onSuccess(Reminder reminder) {
                    if (reminder != null && reminder.isActive() && reminder.getRepeatType() != Reminder.RepeatType.NO_REPEAT) {
                        // Tính toán thời gian lặp lại tiếp theo
                        long nextReminderTime = calculateNextReminderTime(reminder);

                        if (nextReminderTime > System.currentTimeMillis()) {
                            // Cập nhật thời gian nhắc nhở mới
                            reminder.setReminderTime(nextReminderTime);

                            repository.updateReminder(reminder, new ReminderRepository.RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    // Lên lịch WorkManager cho lần nhắc nhở tiếp theo
                                    scheduleReminderWork(reminder);
                                    Log.d(TAG, "Đã lên lịch reminder tiếp theo cho: " + reminderId);
                                }

                                @Override
                                public void onError(String error) {
                                    Log.e(TAG, "Lỗi cập nhật thời gian lặp lại: " + error);
                                }
                            });
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Lỗi lấy thông tin reminder để lặp lại: " + error);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong scheduleNextRepeat", e);
        }
    }

    /**
     * Tính toán thời gian nhắc nhở tiếp theo dựa trên loại lặp lại
     */
    private long calculateNextReminderTime(Reminder reminder) {
        long currentTime = reminder.getReminderTime();
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);

        switch (reminder.getRepeatType()) {
            case Reminder.RepeatType.DAILY:
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
                break;
            case Reminder.RepeatType.WEEKLY:
                calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1);
                break;
            case Reminder.RepeatType.MONTHLY:
                calendar.add(java.util.Calendar.MONTH, 1);
                break;
            default:
                return 0; // Không lặp lại
        }

        return calendar.getTimeInMillis();
    }

    /**
     * Lên lịch WorkManager cho reminder tiếp theo
     */
    private void scheduleReminderWork(Reminder reminder) {
        try {
            androidx.work.Data inputData = new androidx.work.Data.Builder()
                .putString(KEY_REMINDER_ID, reminder.getId())
                .putString(KEY_TITLE, reminder.getTitle())
                .putString(KEY_MESSAGE, reminder.getDescription())
                .putString(KEY_SOUND_ID, reminder.getSoundId())
                .putString(KEY_SOUND_URI, reminder.getSoundUri())
                .putBoolean(KEY_VIBRATE, reminder.isVibrate())
                .putInt(KEY_VOLUME, reminder.getVolume())
                .putBoolean(KEY_IS_ALARM_STYLE, reminder.isAlarmStyle())
                .build();

            long delayMillis = reminder.getReminderTime() - System.currentTimeMillis();

            androidx.work.OneTimeWorkRequest reminderWork =
                new androidx.work.OneTimeWorkRequest.Builder(ReminderWorker.class)
                    .setInitialDelay(delayMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag("reminder_" + reminder.getId())
                    .build();

            androidx.work.WorkManager.getInstance(getApplicationContext())
                .enqueueUniqueWork(
                    "reminder_" + reminder.getId(),
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    reminderWork
                );

        } catch (Exception e) {
            Log.e(TAG, "Lỗi lên lịch reminder tiếp theo: " + e.getMessage(), e);
        }
    }
}

