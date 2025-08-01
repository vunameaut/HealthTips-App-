package com.vhn.doan.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class để quản lý quyền ứng dụng
 */
public class PermissionHelper {

    // Mã yêu cầu quyền cho reminder
    public static final int PERMISSION_REQUEST_REMINDER = 1001;

    // Quyền cần thiết cho tính năng reminder - chỉ cần thông báo
    private static final String[] REMINDER_PERMISSIONS = {
            Manifest.permission.POST_NOTIFICATIONS
    };

    // Map để lưu trữ callback theo fragment hashCode
    private static final Map<Integer, PermissionCallback> callbackMap = new HashMap<>();

    /**
     * Interface callback cho việc xử lý kết quả quyền
     */
    public interface PermissionCallback {
        void onPermissionsGranted();
        void onPermissionsDenied(List<String> deniedPermissions);
    }

    /**
     * Kiểm tra xem tất cả quyền reminder đã được cấp chưa (bao gồm tối ưu hóa pin)
     */
    public static boolean hasReminderPermissions(Context context) {
        // Kiểm tra quyền thông báo cơ bản
        boolean hasNotificationPermission = hasBasicNotificationPermission(context);

        // Kiểm tra tối ưu hóa pin
        boolean isBatteryOptimizationIgnored = isBatteryOptimizationIgnored(context);

        return hasNotificationPermission && isBatteryOptimizationIgnored;
    }

    /**
     * Kiểm tra quyền thông báo cơ bản
     */
    public static boolean hasBasicNotificationPermission(Context context) {
        // Đối với Android 13+ mới cần kiểm tra POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true; // Không cần quyền thông báo cho Android < 13
        }

        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * ✅ THÊM: Kiểm tra xem app có bị tối ưu hóa pin không
     */
    public static boolean isBatteryOptimizationIgnored(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return true; // Android < 6.0 không có Doze Mode
    }

    /**
     * Lấy danh sách quyền chưa được cấp (bao gồm tối ưu hóa pin)
     */
    public static List<String> getMissingReminderPermissions(Context context) {
        List<String> missingPermissions = new ArrayList<>();

        // Kiểm tra quyền thông báo
        if (!hasBasicNotificationPermission(context)) {
            missingPermissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        // Kiểm tra tối ưu hóa pin
        if (!isBatteryOptimizationIgnored(context)) {
            missingPermissions.add("BATTERY_OPTIMIZATION");
        }

        return missingPermissions;
    }

    /**
     * Hiển thị dialog giải thích tại sao cần quyền và yêu cầu cấp quyền (cập nhật)
     */
    public static void showPermissionExplanationDialog(Fragment fragment,
                                                      PermissionCallback callback) {
        if (fragment.getContext() == null) {
            return;
        }

        List<String> missingPermissions = getMissingReminderPermissions(fragment.getContext());
        if (missingPermissions.isEmpty()) {
            callback.onPermissionsGranted();
            return;
        }

        String message = buildPermissionMessage(missingPermissions);

        new AlertDialog.Builder(fragment.getContext())
                .setTitle("Cần cấp quyền")
                .setMessage(message)
                .setPositiveButton("Cấp quyền", (dialog, which) -> {
                    dialog.dismiss();
                    requestPermissions(fragment, missingPermissions, callback);
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                    callback.onPermissionsDenied(missingPermissions);
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Yêu cầu cấp quyền (cập nhật để xử lý tối ưu hóa pin)
     */
    private static void requestPermissions(Fragment fragment,
                                         List<String> permissions,
                                         PermissionCallback callback) {
        // Lưu callback vào map với key là hashCode của fragment
        int fragmentKey = fragment.hashCode();
        callbackMap.put(fragmentKey, callback);

        // Kiểm tra xem có cần yêu cầu tối ưu hóa pin không
        boolean needsBatteryOptimization = permissions.contains("BATTERY_OPTIMIZATION");
        boolean needsNotificationPermission = permissions.contains(Manifest.permission.POST_NOTIFICATIONS);

        if (needsBatteryOptimization) {
            // Ưu tiên xử lý tối ưu hóa pin trước
            requestIgnoreBatteryOptimization(fragment, callback);
        } else if (needsNotificationPermission) {
            // Chỉ yêu cầu quyền thông báo
            String[] permissionArray = {Manifest.permission.POST_NOTIFICATIONS};
            fragment.requestPermissions(permissionArray, PERMISSION_REQUEST_REMINDER);
        } else {
            // Không có quyền nào cần yêu cầu
            callback.onPermissionsGranted();
        }
    }

    /**
     * ✅ THÊM: Yêu cầu tắt tối ưu hóa pin
     */
    private static void requestIgnoreBatteryOptimization(Fragment fragment, PermissionCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                String packageName = fragment.requireContext().getPackageName();
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(android.net.Uri.parse("package:" + packageName));
                fragment.startActivity(intent);

                // Hiển thị thông báo hướng dẫn
                new AlertDialog.Builder(fragment.getContext())
                    .setTitle("Tối ưu hóa pin")
                    .setMessage("Vui lòng chọn \"Cho phép\" để đảm bảo nhắc nhở hoạt động khi app ở chế độ nền.\n\nSau khi cài đặt xong, vui lòng quay lại app.")
                    .setPositiveButton("Đã hiểu", (dialog, which) -> {
                        // Kiểm tra lại sau khi người dùng quay lại
                        checkPermissionsAfterBatteryOptimization(fragment, callback);
                    })
                    .setCancelable(false)
                    .show();

            } catch (Exception e) {
                android.util.Log.e("PermissionHelper", "Lỗi khi mở cài đặt tối ưu hóa pin", e);
                callback.onPermissionsDenied(java.util.Arrays.asList("BATTERY_OPTIMIZATION"));
            }
        } else {
            callback.onPermissionsGranted();
        }
    }

    /**
     * ✅ THÊM: Kiểm tra quyền sau khi cài đặt tối ưu hóa pin
     */
    private static void checkPermissionsAfterBatteryOptimization(Fragment fragment, PermissionCallback callback) {
        if (fragment.getContext() == null) return;

        // Kiểm tra lại tất cả quyền
        List<String> stillMissingPermissions = getMissingReminderPermissions(fragment.getContext());

        if (stillMissingPermissions.isEmpty()) {
            callback.onPermissionsGranted();
        } else if (stillMissingPermissions.contains(Manifest.permission.POST_NOTIFICATIONS)) {
            // Vẫn cần quyền thông báo
            String[] permissionArray = {Manifest.permission.POST_NOTIFICATIONS};
            fragment.requestPermissions(permissionArray, PERMISSION_REQUEST_REMINDER);
        } else {
            callback.onPermissionsDenied(stillMissingPermissions);
        }
    }

    /**
     * Tạo thông điệp giải thích về quyền cần thiết (cập nhật)
     */
    private static String buildPermissionMessage(List<String> missingPermissions) {
        StringBuilder message = new StringBuilder();
        message.append("Ứng dụng cần các quyền sau để nhắc nhở hoạt động ổn định:\n\n");

        for (String permission : missingPermissions) {
            switch (permission) {
                case Manifest.permission.POST_NOTIFICATIONS:
                    message.append("• Thông báo: Để hiển thị nhắc nhở đúng thời gian\n");
                    break;
                case "BATTERY_OPTIMIZATION":
                    message.append("• Tắt tối ưu hóa pin: Để nhắc nhở hoạt động khi app ở chế độ nền\n");
                    break;
            }
        }

        message.append("\nBạn có muốn cấp quyền để sử dụng đầy đủ tính năng không?");
        return message.toString();
    }

    /**
     * Xử lý kết quả yêu cầu quyền
     */
    public static void handlePermissionResult(Fragment fragment,
                                            int requestCode,
                                            String[] permissions,
                                            int[] grantResults) {
        if (requestCode != PERMISSION_REQUEST_REMINDER) {
            return;
        }

        // Lấy callback từ map
        int fragmentKey = fragment.hashCode();
        PermissionCallback callback = callbackMap.get(fragmentKey);
        if (callback == null) {
            return;
        }

        List<String> deniedPermissions = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permissions[i]);
            }
        }

        if (deniedPermissions.isEmpty()) {
            callback.onPermissionsGranted();
        } else {
            callback.onPermissionsDenied(deniedPermissions);
        }

        // Xóa callback khỏi map
        callbackMap.remove(fragmentKey);
    }

    /**
     * Kiểm tra xem có nên hiển thị giải thích quyền hay không
     */
    public static boolean shouldShowRequestPermissionRationale(Fragment fragment, String permission) {
        return fragment.shouldShowRequestPermissionRationale(permission);
    }

    /**
     * Kiểm tra xem có quyền thông báo hay không
     */
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Android < 13 không cần quyền thông báo runtime
    }

    /**
     * Yêu cầu quyền thông báo
     */
    public static void requestNotificationPermission(Fragment fragment, NotificationPermissionCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Lưu callback
            int fragmentKey = fragment.hashCode();
            notificationCallbackMap.put(fragmentKey, callback);

            // Yêu cầu quyền
            fragment.requestPermissions(
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                PERMISSION_REQUEST_REMINDER
            );
        } else {
            // Android < 13 tự động có quyền
            callback.onResult(true);
        }
    }

    /**
     * Interface callback cho quyền thông báo
     */
    public interface NotificationPermissionCallback {
        void onResult(boolean granted);
    }

    // Map để lưu notification permission callbacks
    private static final Map<Integer, NotificationPermissionCallback> notificationCallbackMap = new HashMap<>();
}
