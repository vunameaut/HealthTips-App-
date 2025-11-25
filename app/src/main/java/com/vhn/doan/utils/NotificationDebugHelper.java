package com.vhn.doan.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Random;

/**
 * Helper class để debug và kiểm tra trạng thái thông báo
 * - Kiểm tra quyền thông báo
 * - DISABLED: Test notification features (người dùng không muốn thông báo test)
 * - Kiểm tra thiết lập các kênh thông báo
 */
public class NotificationDebugHelper {
    private static final String TAG = "NotificationDebugHelper";
    private static final String DEBUG_CHANNEL_ID = "debug_channel";
    private static final String DEBUG_CHANNEL_NAME = "Debug Notifications";

    // Flag để disable tất cả test notifications
    private static final boolean ENABLE_TEST_NOTIFICATIONS = false;

    /**
     * Kiểm tra xem ứng dụng có quyền hiển thị thông báo không
     *
     * @param context Context của ứng dụng
     * @return true nếu có quyền, false nếu không
     */
    public static boolean checkNotificationPermission(Context context) {
        if (context == null) return false;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        boolean isEnabled = notificationManager.areNotificationsEnabled();

        Log.d(TAG, "Quyền thông báo: " + (isEnabled ? "ĐÃ CẤP" : "CHƯA CẤP"));

        return isEnabled;
    }

    /**
     * Mở cài đặt thông báo của ứng dụng
     *
     * @param context Context của ứng dụng
     */
    public static void openNotificationSettings(Context context) {
        if (context == null) return;

        try {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            } else {
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("app_package", context.getPackageName());
                intent.putExtra("app_uid", context.getApplicationInfo().uid);
            }
            context.startActivity(intent);
            Log.d(TAG, "Mở cài đặt thông báo");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi mở cài đặt thông báo: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra và tạo kênh thông báo debug nếu cần
     *
     * @param context Context của ứng dụng
     */
    private static void ensureDebugChannelExists(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel channel = notificationManager.getNotificationChannel(DEBUG_CHANNEL_ID);
            if (channel == null) {
                channel = new NotificationChannel(
                        DEBUG_CHANNEL_ID,
                        DEBUG_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Kênh dùng để test thông báo");
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Đã tạo kênh thông báo debug");
            }
        }
    }

    /**
     * Test thông báo ngay lập tức
     * DISABLED: Chức năng test đã bị vô hiệu hóa theo yêu cầu người dùng
     *
     * @param context Context của ứng dụng
     * @return false - test notification disabled
     */
    public static boolean testNotification(Context context) {
        if (!ENABLE_TEST_NOTIFICATIONS) {
            Log.d(TAG, "Test notification đã bị vô hiệu hóa");
            return false;
        }

        // Code cũ được giữ lại nhưng không chạy
        if (context == null) return false;

        if (!checkNotificationPermission(context)) {
            Log.e(TAG, "Test notification thất bại: Không có quyền hiển thị thông báo");
            return false;
        }

        try {
            ensureDebugChannelExists(context);

            int notificationId = new Random().nextInt(1000);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DEBUG_CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Test Notification")
                    .setContentText("Đây là thông báo test từ NotificationDebugHelper")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());

            Log.d(TAG, "Đã hiển thị thông báo test với ID: " + notificationId);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hiển thị thông báo test: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra các kênh thông báo hiện tại của ứng dụng
     *
     * @param context Context của ứng dụng
     * @return Mảng các kênh thông báo hoặc null nếu không có hoặc SDK < Oreo
     */
    public static String[] checkNotificationChannels(Context context) {
        if (context == null) return null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                if (notificationManager.getNotificationChannels().size() == 0) {
                    Log.d(TAG, "Không có kênh thông báo nào được tạo");
                    return new String[]{"Không có kênh thông báo nào"};
                }

                String[] channelIds = new String[notificationManager.getNotificationChannels().size()];
                for (int i = 0; i < channelIds.length; i++) {
                    NotificationChannel channel = notificationManager.getNotificationChannels().get(i);
                    channelIds[i] = channel.getId() + " (" + channel.getName() + ")";
                    Log.d(TAG, "Kênh thông báo: " + channelIds[i]);
                }

                return channelIds;
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi kiểm tra kênh thông báo: " + e.getMessage());
                return null;
            }
        } else {
            Log.d(TAG, "Notification Channels không được hỗ trợ trên Android " + Build.VERSION.SDK_INT);
            return new String[]{"Notification Channels chỉ được hỗ trợ từ Android 8.0 trở lên"};
        }
    }
}
