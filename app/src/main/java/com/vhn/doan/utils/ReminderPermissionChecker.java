package com.vhn.doan.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

/**
 * Helper class để kiểm tra và request các permissions cần thiết cho Reminder
 * - Post Notifications Permission (Android 13+)
 * - Schedule Exact Alarm Permission (Android 12+)
 * - Full Screen Intent Permission (Android 14+)
 * - Battery Optimization Exemption (Android 6+)
 */
public class ReminderPermissionChecker {

    private static final String TAG = "ReminderPermissionChecker";
    public static final int REQUEST_POST_NOTIFICATIONS = 1001;
    public static final int REQUEST_EXACT_ALARM = 1002;
    public static final int REQUEST_FULL_SCREEN_INTENT = 1003;
    public static final int REQUEST_BATTERY_OPTIMIZATION = 1004;

    /**
     * Kiểm tra quyền POST_NOTIFICATIONS (Android 13+)
     * @return true nếu có quyền, false nếu không
     */
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;

            // Double check với NotificationManagerCompat
            boolean areEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled();

            Log.d(TAG, "POST_NOTIFICATIONS permission: " + hasPermission + ", Enabled: " + areEnabled);
            return hasPermission && areEnabled;
        }
        // Trước Android 13 không cần permission này
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    /**
     * Kiểm tra xem app có bị battery optimization không
     * @return true nếu đã được exempted, false nếu vẫn bị optimize
     */
    public static boolean isBatteryOptimizationDisabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            String packageName = context.getPackageName();
            boolean isIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName);
            Log.d(TAG, "Battery optimization ignored: " + isIgnoring);
            return isIgnoring;
        }
        return true; // Trước Android M không có battery optimization
    }

    /**
     * Kiểm tra xem có quyền schedule exact alarm không (Android 12+)
     * @return true nếu có quyền, false nếu không
     */
    public static boolean canScheduleExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            boolean canSchedule = alarmManager.canScheduleExactAlarms();
            Log.d(TAG, "Can schedule exact alarms: " + canSchedule);
            return canSchedule;
        }
        return true; // Trước Android 12 không cần permission này
    }

    /**
     * Kiểm tra xem có quyền hiển thị full screen intent không (Android 11+)
     * @return true nếu có quyền, false nếu không
     */
    public static boolean canUseFullScreenIntent(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
            // Android 14+ requires explicit permission check
            NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
            boolean canUse = notificationManager.canUseFullScreenIntent();
            Log.d(TAG, "Can use full screen intent (API 34+): " + canUse);
            return canUse;
        }
        // Trước Android 14, permission được grant tự động nếu có trong manifest
        return true;
    }

    /**
     * Kiểm tra tất cả permissions cần thiết
     * @return true nếu tất cả permissions đã được granted
     */
    public static boolean checkAllPermissions(Context context) {
        boolean notificationOk = hasNotificationPermission(context);
        boolean alarmOk = canScheduleExactAlarms(context);
        boolean fullScreenOk = canUseFullScreenIntent(context);
        boolean batteryOk = isBatteryOptimizationDisabled(context);

        Log.d(TAG, "All permissions check - Notification: " + notificationOk +
            ", Alarm: " + alarmOk + ", FullScreen: " + fullScreenOk +
            ", Battery: " + batteryOk);

        return notificationOk && alarmOk && fullScreenOk && batteryOk;
    }

    /**
     * Kiểm tra các permissions BẮT BUỘC (không bao gồm battery optimization)
     * @return true nếu có đủ permissions bắt buộc
     */
    public static boolean checkRequiredPermissions(Context context) {
        boolean notificationOk = hasNotificationPermission(context);
        boolean alarmOk = canScheduleExactAlarms(context);
        boolean fullScreenOk = canUseFullScreenIntent(context);

        Log.d(TAG, "Required permissions check - Notification: " + notificationOk +
            ", Alarm: " + alarmOk + ", FullScreen: " + fullScreenOk);

        return notificationOk && alarmOk && fullScreenOk;
    }

    /**
     * Request POST_NOTIFICATIONS permission (Android 13+)
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static void requestNotificationPermission(Activity activity) {
        if (hasNotificationPermission(activity)) {
            Log.d(TAG, "Notification permission already granted");
            return;
        }

        // Check if should show rationale
        if (ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.POST_NOTIFICATIONS
        )) {
            // Show explanation dialog first
            new AlertDialog.Builder(activity)
                .setTitle("Cho phép gửi thông báo")
                .setMessage("Để nhận thông báo nhắc nhở về sức khỏe, ứng dụng cần quyền gửi thông báo.\n\n" +
                        "Vui lòng cho phép trong bước tiếp theo.")
                .setPositiveButton("Tiếp tục", (dialog, which) -> {
                    ActivityCompat.requestPermissions(
                        activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_POST_NOTIFICATIONS
                    );
                })
                .setNegativeButton("Bỏ qua", (dialog, which) -> {
                    Log.d(TAG, "User declined notification permission explanation");
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
        } else {
            // Request directly
            ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                REQUEST_POST_NOTIFICATIONS
            );
        }
    }

    /**
     * Request battery optimization exemption với dialog giải thích
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestBatteryOptimizationExemption(Activity activity) {
        if (isBatteryOptimizationDisabled(activity)) {
            Log.d(TAG, "Battery optimization already disabled");
            return;
        }

        new AlertDialog.Builder(activity)
            .setTitle("Cho phép nhắc nhở hoạt động")
            .setMessage("Để nhận thông báo nhắc nhở đúng giờ khi app đóng, vui lòng tắt tối ưu hóa pin cho ứng dụng.\n\n" +
                    "Điều này đảm bảo bạn không bỏ lỡ các nhắc nhở quan trọng về sức khỏe.")
            .setPositiveButton("Cài đặt", (dialog, which) -> {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivity(intent);
                    Log.d(TAG, "Opened battery optimization settings");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to open battery optimization settings", e);
                    // Fallback: mở settings tổng quát
                    openBatteryOptimizationSettings(activity);
                }
            })
            .setNegativeButton("Bỏ qua", (dialog, which) -> {
                Log.d(TAG, "User skipped battery optimization exemption");
                dialog.dismiss();
            })
            .setCancelable(false)
            .show();
    }

    /**
     * Request exact alarm permission với dialog giải thích (Android 12+)
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    public static void requestExactAlarmPermission(Activity activity) {
        if (canScheduleExactAlarms(activity)) {
            Log.d(TAG, "Exact alarm permission already granted");
            return;
        }

        new AlertDialog.Builder(activity)
            .setTitle("Cho phép đặt báo thức chính xác")
            .setMessage("Để nhận thông báo nhắc nhở chính xác đúng giờ, ứng dụng cần quyền đặt báo thức.\n\n" +
                    "Vui lòng bật quyền 'Alarms & reminders' trong cài đặt.")
            .setPositiveButton("Cài đặt", (dialog, which) -> {
                try {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivity(intent);
                    Log.d(TAG, "Opened exact alarm settings");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to open exact alarm settings", e);
                    // Fallback: mở app settings
                    openAppSettings(activity);
                }
            })
            .setNegativeButton("Bỏ qua", (dialog, which) -> {
                Log.d(TAG, "User skipped exact alarm permission");
                dialog.dismiss();
            })
            .setCancelable(false)
            .show();
    }

    /**
     * Request full screen intent permission (Android 14+/API 34+)
     */
    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    public static void requestFullScreenIntentPermission(Activity activity) {
        if (canUseFullScreenIntent(activity)) {
            Log.d(TAG, "Full screen intent permission already granted");
            return;
        }

        new AlertDialog.Builder(activity)
            .setTitle("Cho phép hiển thị báo thức toàn màn hình")
            .setMessage("Để nhận báo thức nhắc nhở toàn màn hình (giống đồng hồ báo thức), " +
                    "ứng dụng cần quyền hiển thị toàn màn hình.\n\n" +
                    "Vui lòng bật quyền 'Display over other apps' hoặc 'Full screen notifications' trong cài đặt.")
            .setPositiveButton("Cài đặt", (dialog, which) -> {
                try {
                    // Mở settings để user grant permission
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT);
                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivity(intent);
                    Log.d(TAG, "Opened full screen intent settings");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to open full screen intent settings", e);
                    // Fallback: mở app settings
                    openAppSettings(activity);
                }
            })
            .setNegativeButton("Bỏ qua", (dialog, which) -> {
                Log.d(TAG, "User skipped full screen intent permission");
                dialog.dismiss();
            })
            .setCancelable(false)
            .show();
    }

    /**
     * Mở settings battery optimization (fallback)
     */
    private static void openBatteryOptimizationSettings(Activity activity) {
        try {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            activity.startActivity(intent);
            Log.d(TAG, "Opened battery optimization list settings");
        } catch (Exception e) {
            Log.e(TAG, "Failed to open battery optimization list settings", e);
            openAppSettings(activity);
        }
    }

    /**
     * Mở app settings tổng quát
     */
    private static void openAppSettings(Activity activity) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
            Log.d(TAG, "Opened app settings");
        } catch (Exception e) {
            Log.e(TAG, "Failed to open app settings", e);
        }
    }

    /**
     * Request tất cả permissions cần thiết với sequence
     * Gọi method này khi user tạo reminder đầu tiên hoặc từ settings
     *
     * Thứ tự ưu tiên (quan trọng nhất trước):
     * 1. POST_NOTIFICATIONS (Android 13+) - BẮT BUỘC
     * 2. SCHEDULE_EXACT_ALARM (Android 12+) - BẮT BUỘC
     * 3. FULL_SCREEN_INTENT (Android 14+) - BẮT BUỘC cho alarm style
     * 4. Battery Optimization - KHUYẾN NGHỊ (không bắt buộc)
     */
    public static void requestAllNecessaryPermissions(Activity activity) {
        Log.d(TAG, "Requesting all necessary permissions");

        // 1. Android 13+ (API 33+): Request POST_NOTIFICATIONS trước tiên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission(activity)) {
                requestNotificationPermission(activity);
                return; // Request từng cái một, tránh overwhelm user
            }
        }

        // 2. Android 12+ (API 31+): Request exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!canScheduleExactAlarms(activity)) {
                requestExactAlarmPermission(activity);
                return;
            }
        }

        // 3. Android 14+ (API 34+): Request full screen intent permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (!canUseFullScreenIntent(activity)) {
                requestFullScreenIntentPermission(activity);
                return;
            }
        }

        // 4. Android 6+ (API 23+): Request battery optimization exemption (optional)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isBatteryOptimizationDisabled(activity)) {
                requestBatteryOptimizationExemption(activity);
                return;
            }
        }

        Log.d(TAG, "All necessary permissions already granted");
    }

    /**
     * Hiển thị dialog cảnh báo nếu thiếu permissions
     * @return true nếu có đủ permissions, false nếu thiếu và đã show dialog
     */
    public static boolean checkAndRequestPermissionsIfNeeded(Activity activity) {
        if (checkAllPermissions(activity)) {
            return true;
        }

        // Nếu thiếu permissions, request chúng
        requestAllNecessaryPermissions(activity);
        return false;
    }

    /**
     * Tạo message giải thích tại sao cần permissions
     */
    public static String getPermissionExplanationMessage(Context context) {
        StringBuilder message = new StringBuilder();
        message.append("Để đảm bảo nhận thông báo nhắc nhở đúng giờ, ứng dụng cần:\n\n");

        int missingCount = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission(context)) {
            message.append("• Quyền gửi thông báo\n");
            missingCount++;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms(context)) {
            message.append("• Quyền đặt báo thức chính xác\n");
            missingCount++;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && !canUseFullScreenIntent(context)) {
            message.append("• Quyền hiển thị báo thức toàn màn hình\n");
            missingCount++;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isBatteryOptimizationDisabled(context)) {
            message.append("• Tắt tối ưu hóa pin (khuyến nghị)\n");
            missingCount++;
        }

        if (missingCount == 0) {
            return "Tất cả quyền cần thiết đã được cấp.";
        }

        message.append("\nNhững quyền này giúp bạn không bỏ lỡ các nhắc nhở quan trọng về sức khỏe.");

        return message.toString();
    }

    /**
     * Tạo danh sách các quyền còn thiếu
     */
    public static java.util.List<String> getMissingPermissions(Context context) {
        java.util.List<String> missing = new java.util.ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission(context)) {
            missing.add("POST_NOTIFICATIONS");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms(context)) {
            missing.add("SCHEDULE_EXACT_ALARM");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && !canUseFullScreenIntent(context)) {
            missing.add("USE_FULL_SCREEN_INTENT");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isBatteryOptimizationDisabled(context)) {
            missing.add("BATTERY_OPTIMIZATION");
        }

        return missing;
    }
}
