package com.vhn.doan.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.fragment.app.Fragment;

/**
 * Helper class để quản lý các quyền đặc biệt cần thiết cho hệ thống nhắc nhở
 */
public class ReminderPermissionHelper {

    private static final String TAG = "ReminderPermissionHelper";

    /**
     * Kiểm tra và yêu cầu tất cả quyền cần thiết cho reminder
     */
    public static void checkAndRequestAllPermissions(Fragment fragment, ReminderPermissionCallback callback) {
        Context context = fragment.getContext();
        if (context == null) {
            callback.onError("Context is null");
            return;
        }

        // Kiểm tra quyền thông báo
        if (!PermissionHelper.hasNotificationPermission(context)) {
            showPermissionDialog(fragment, "Quyền thông báo",
                "Ứng dụng cần quyền hiển thị thông báo để nhắc nhở bạn về sức khỏe.",
                () -> PermissionHelper.requestNotificationPermission(fragment, callback::onNotificationPermissionResult));
            return;
        }

        // Kiểm tra quyền exact alarm
        if (!hasExactAlarmPermission(context)) {
            showPermissionDialog(fragment, "Quyền báo thức chính xác",
                "Ứng dụng cần quyền đặt báo thức chính xác để thông báo đúng giờ.",
                () -> requestExactAlarmPermission(fragment));
            return;
        }

        // Kiểm tra tối ưu hóa pin
        if (!isBatteryOptimizationDisabled(context)) {
            showBatteryOptimizationDialog(fragment, callback);
            return;
        }

        // Tất cả quyền đã được cấp
        callback.onAllPermissionsGranted();
    }

    /**
     * Kiểm tra quyền exact alarm
     */
    public static boolean hasExactAlarmPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    /**
     * Yêu cầu quyền exact alarm
     */
    public static void requestExactAlarmPermission(Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + fragment.requireContext().getPackageName()));
            fragment.startActivity(intent);
        }
    }

    /**
     * Kiểm tra xem battery optimization có bị tắt không
     */
    public static boolean isBatteryOptimizationDisabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return true;
    }

    /**
     * Hiển thị dialog yêu cầu tắt battery optimization
     */
    private static void showBatteryOptimizationDialog(Fragment fragment, ReminderPermissionCallback callback) {
        new AlertDialog.Builder(fragment.requireContext())
            .setTitle("Tối ưu hóa pin")
            .setMessage("Để đảm bảo nhắc nhở hoạt động ngay cả khi app bị tắt, vui lòng tắt tối ưu hóa pin cho ứng dụng này.\n\n" +
                       "Điều này sẽ không ảnh hưởng đáng kể đến thời lượng pin của bạn.")
            .setPositiveButton("Cài đặt", (dialog, which) -> {
                requestBatteryOptimizationDisable(fragment);
                dialog.dismiss();
            })
            .setNegativeButton("Bỏ qua", (dialog, which) -> {
                dialog.dismiss();
                callback.onBatteryOptimizationDenied();
            })
            .setCancelable(false)
            .show();
    }

    /**
     * Yêu cầu tắt battery optimization
     */
    public static void requestBatteryOptimizationDisable(Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + fragment.requireContext().getPackageName()));
                fragment.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to open battery optimization settings", e);
                // Fallback to general battery optimization settings
                Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                fragment.startActivity(intent);
            }
        }
    }

    /**
     * Hiển thị dialog thông báo quyền
     */
    private static void showPermissionDialog(Fragment fragment, String title, String message, Runnable onAccept) {
        new AlertDialog.Builder(fragment.requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cấp quyền", (dialog, which) -> {
                dialog.dismiss();
                onAccept.run();
            })
            .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
            .setCancelable(false)
            .show();
    }

    /**
     * Khởi động foreground service để duy trì hoạt động
     * DISABLED: Tắt để tránh thông báo trống
     */
    public static void startReminderService(Context context) {
        // DISABLED: Foreground service bị tắt để tránh notification trống
        // AlarmManager sẽ xử lý reminders trực tiếp
        Log.d(TAG, "Reminder foreground service đã bị vô hiệu hóa để tránh notification trống");


    }

    /**
     * Interface callback cho permission results
     */
    public interface ReminderPermissionCallback {
        void onAllPermissionsGranted();
        void onNotificationPermissionResult(boolean granted);
        void onBatteryOptimizationDenied();
        void onError(String error);
    }
}
