package com.vhn.doan.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.vhn.doan.R;
import com.vhn.doan.presentation.home.HomeActivity;
import com.vhn.doan.receivers.ReminderBroadcastReceiver;

/**
 * Foreground Service để đảm bảo thông báo nhắc nhở hoạt động mạnh mẽ
 * Service này sẽ chạy trong background và không bị kill bởi hệ thống
 */
public class ReminderForegroundService extends Service {

    private static final String TAG = "ReminderForegroundService";
    private static final int FOREGROUND_NOTIFICATION_ID = 9999;
    private static final String CHANNEL_ID = "reminder_foreground_channel";
    // Thêm thời gian khởi động lại service nếu bị kill
    private static final int RESTART_SERVICE_DELAY_MS = 5000; // 5 giây

    private PowerManager.WakeLock wakeLock;
    private NotificationManager notificationManager;
    private boolean isServiceStarted = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ReminderForegroundService created");

        // Tạo notification channel
        createNotificationChannel();

        try {
            // Acquire wake lock để giữ CPU hoạt động ngay cả khi màn hình tắt
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "HealthTips:ReminderForegroundService"
            );
            // Timeout sau 1 giờ nếu không được release thủ công
            wakeLock.acquire(60 * 60 * 1000L);
            Log.d(TAG, "Wake lock acquired");
        } catch (Exception e) {
            Log.e(TAG, "Error acquiring wake lock", e);
        }

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "ReminderForegroundService started");

        try {
            // Bắt đầu chạy foreground với delay nhỏ để tránh lỗi
            Notification notification = createForegroundNotification();
            startForeground(FOREGROUND_NOTIFICATION_ID, notification);
            isServiceStarted = true;

            // Xử lý intent nếu có
            if (intent != null) {
                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        case "SHOW_REMINDER":
                            handleShowReminder(intent);
                            break;
                        case "RESTART_SERVICE":
                            Log.d(TAG, "Service được khởi động lại sau khi bị kill");
                            break;
                    }
                }
            }

            // Đặt lịch khởi động lại service nếu bị kill
            scheduleServiceRestart();

        } catch (Exception e) {
            Log.e(TAG, "Error starting foreground service", e);
            // Nếu không thể start foreground, fallback về normal notification
            handleFallbackNotification(intent);
            stopSelf();
            return START_NOT_STICKY;
        }

        // Return STICKY để service tự động restart nếu bị kill
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ReminderForegroundService destroyed");

        // Release wake lock nếu đang giữ
        if (wakeLock != null && wakeLock.isHeld()) {
            try {
                wakeLock.release();
                Log.d(TAG, "Wake lock released");
            } catch (Exception e) {
                Log.e(TAG, "Error releasing wake lock", e);
            }
        }

        // Nếu service đang chạy bình thường (không phải do lỗi), thì khởi động lại nó
        if (isServiceStarted) {
            Log.d(TAG, "Service bị destroy không mong muốn, sẽ khởi động lại");
            Intent restartIntent = new Intent(getApplicationContext(), ReminderForegroundService.class);
            restartIntent.setAction("RESTART_SERVICE");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(restartIntent);
            } else {
                startService(restartIntent);
            }
        }
    }

    /**
     * Đặt lịch khởi động lại service nếu bị kill
     */
    private void scheduleServiceRestart() {
        // Tạo intent cho AlarmManager để khởi động lại service
        Intent restartServiceIntent = new Intent(getApplicationContext(), ReminderBroadcastReceiver.class);
        restartServiceIntent.setAction("ACTION_RESTART_REMINDER_SERVICE");

        PendingIntent restartServicePendingIntent = PendingIntent.getBroadcast(
            getApplicationContext(),
            1,
            restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Đặt AlarmManager để khởi động lại service sau một khoảng thời gian nếu bị kill
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + RESTART_SERVICE_DELAY_MS,
                restartServicePendingIntent
            );
            Log.d(TAG, "Đã đặt lịch khởi động lại service nếu bị kill");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Tạo notification channel cho foreground service
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Dịch vụ nhắc nhở sức khỏe",
                    NotificationManager.IMPORTANCE_LOW
                );
                channel.setDescription("Dịch vụ chạy ngầm để đảm bảo nhắc nhở hoạt động");
                channel.setShowBadge(false);

                NotificationManager manager = getSystemService(NotificationManager.class);
                if (manager != null) {
                    manager.createNotificationChannel(channel);
                    Log.d(TAG, "Notification channel created: " + CHANNEL_ID);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating notification channel", e);
            }
        }
    }

    /**
     * Tạo notification cho foreground service
     */
    private Notification createForegroundNotification() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("open_reminders", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Dịch vụ nhắc nhở đang hoạt động")
            .setContentText("Đảm bảo bạn nhận được thông báo đúng giờ")
            .setSmallIcon(R.drawable.ic_notification_reminder)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .build();
    }

    /**
     * Xử lý hiển thị reminder
     */
    private void handleShowReminder(Intent intent) {
        try {
            String reminderId = intent.getStringExtra("reminder_id");
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");

            Log.d(TAG, "Handling show reminder: " + title);

            if (title != null && message != null) {
                // Hiển thị thông báo nhắc nhở
                NotificationService.showReminderNotification(this, title, message, reminderId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling show reminder", e);
        }
    }

    /**
     * Static method để start service và hiển thị reminder
     */
    public static void showReminder(Context context, String reminderId, String title, String message) {
        try {
            Intent serviceIntent = new Intent(context, ReminderForegroundService.class);
            serviceIntent.setAction("SHOW_REMINDER");
            serviceIntent.putExtra("reminder_id", reminderId);
            serviceIntent.putExtra("title", title);
            serviceIntent.putExtra("message", message);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            Log.d(TAG, "Show reminder request sent to service: " + title);
        } catch (Exception e) {
            Log.e(TAG, "Error showing reminder through service", e);
            // Fallback trực tiếp nếu không thể start service
            NotificationService.showReminderNotification(context, title, message, reminderId);
        }
    }

    /**
     * Static method để start service
     */
    public static void startService(Context context) {
        try {
            Intent serviceIntent = new Intent(context, ReminderForegroundService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            Log.d(TAG, "Service start request sent");
        } catch (Exception e) {
            Log.e(TAG, "Error starting service", e);
        }
    }

    /**
     * Static method để stop service
     */
    public static void stopService(Context context) {
        try {
            Intent serviceIntent = new Intent(context, ReminderForegroundService.class);
            context.stopService(serviceIntent);
            Log.d(TAG, "Service stop request sent");
        } catch (Exception e) {
            Log.e(TAG, "Error stopping service", e);
        }
    }

    /**
     * Fallback khi không thể start foreground service
     */
    private void handleFallbackNotification(Intent intent) {
        try {
            if (intent != null && "SHOW_REMINDER".equals(intent.getAction())) {
                String reminderId = intent.getStringExtra("reminder_id");
                String title = intent.getStringExtra("title");
                String message = intent.getStringExtra("message");

                if (title != null && message != null) {
                    // Hiển thị thông báo trực tiếp mà không cần foreground service
                    NotificationService.showReminderNotification(this, title, message, reminderId);
                    Log.d(TAG, "Showed notification via fallback method");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in fallback notification", e);
        }
    }
}
