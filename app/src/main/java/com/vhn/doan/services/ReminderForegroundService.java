package com.vhn.doan.services;

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
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.vhn.doan.R;
import com.vhn.doan.presentation.home.HomeActivity;

/**
 * Foreground Service để đảm bảo thông báo nhắc nhở hoạt động mạnh mẽ
 * Service này sẽ chạy trong background và không bị kill bởi hệ thống
 */
public class ReminderForegroundService extends Service {

    private static final String TAG = "ReminderForegroundService";
    private static final int FOREGROUND_NOTIFICATION_ID = 9999;
    private static final String CHANNEL_ID = "reminder_foreground_channel";

    private PowerManager.WakeLock wakeLock;
    private NotificationManager notificationManager;
    private boolean isServiceRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ReminderForegroundService created");

        // Tạo notification channel
        createNotificationChannel();

        // Acquire wake lock
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "HealthTips:ReminderForegroundService"
        );
        wakeLock.acquire();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        isServiceRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "ReminderForegroundService started with flags: " + flags + ", startId: " + startId);

        try {
            // Bắt đầu chạy foreground với delay nhỏ để tránh lỗi
            startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundNotification());

            // Xử lý intent nếu có
            if (intent != null) {
                String action = intent.getAction();
                if ("SHOW_REMINDER".equals(action)) {
                    handleShowReminder(intent);
                }
            }

            // ✅ THÊM: Tự động restart service nếu bị kill
            Log.d(TAG, "Service đang chạy với START_STICKY để tự động restart");

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

        isServiceRunning = false;

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        // ✅ THÊM: Tự động restart service nếu bị destroy không mong muốn
        if (isServiceRunning) {
            Log.d(TAG, "Service bị destroy - tự động restart...");
            Intent restartIntent = new Intent(this, ReminderForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(restartIntent);
            } else {
                startService(restartIntent);
            }
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "App task removed - service vẫn chạy trong background");
        
        // Đảm bảo service tiếp tục chạy khi app bị remove khỏi recent apps
        Intent restartServiceIntent = new Intent(getApplicationContext(), ReminderForegroundService.class);
        restartServiceIntent.setPackage(getPackageName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartServiceIntent);
        } else {
            startService(restartServiceIntent);
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
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Dịch vụ nhắc nhở sức khỏe",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Dịch vụ chạy ngầm để đảm bảo nhắc nhở hoạt động");
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
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
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build();
    }

    /**
     * Xử lý hiển thị reminder
     */
    private void handleShowReminder(Intent intent) {
        String reminderId = intent.getStringExtra("reminder_id");
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        Log.d(TAG, "Handling show reminder: " + title);

        if (title != null && message != null) {
            // Hiển thị thông báo nhắc nhở
            NotificationService.showReminderNotification(this, title, message, reminderId);
            
            // ✅ THÊM: Log để debug
            Log.d(TAG, "✅ Đã hiển thị notification cho reminder: " + title);
        }
    }

    /**
     * Static method để start service và hiển thị reminder
     */
    public static void showReminder(Context context, String reminderId, String title, String message) {
        Log.d(TAG, "🔄 Yêu cầu hiển thị reminder: " + title);
        
        Intent serviceIntent = new Intent(context, ReminderForegroundService.class);
        serviceIntent.setAction("SHOW_REMINDER");
        serviceIntent.putExtra("reminder_id", reminderId);
        serviceIntent.putExtra("title", title);
        serviceIntent.putExtra("message", message);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            Log.d(TAG, "✅ Đã gửi intent hiển thị reminder");
        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi gửi intent hiển thị reminder", e);
            // Fallback: hiển thị notification trực tiếp
            NotificationService.showReminderNotification(context, title, message, reminderId);
        }
    }

    /**
     * Static method để start service
     */
    public static void startService(Context context) {
        Log.d(TAG, "🔄 Khởi động ReminderForegroundService...");
        
        Intent serviceIntent = new Intent(context, ReminderForegroundService.class);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            Log.d(TAG, "✅ Đã khởi động ReminderForegroundService thành công");
        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi khởi động ReminderForegroundService", e);
        }
    }

    /**
     * Static method để stop service
     */
    public static void stopService(Context context) {
        Log.d(TAG, "🔄 Dừng ReminderForegroundService...");
        
        Intent serviceIntent = new Intent(context, ReminderForegroundService.class);
        context.stopService(serviceIntent);
        
        Log.d(TAG, "✅ Đã dừng ReminderForegroundService");
    }

    /**
     * Fallback khi không thể start foreground service
     */
    private void handleFallbackNotification(Intent intent) {
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
    }
}
