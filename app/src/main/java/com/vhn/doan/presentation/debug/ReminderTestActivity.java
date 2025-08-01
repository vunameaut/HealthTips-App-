package com.vhn.doan.presentation.debug;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import com.vhn.doan.R;
import com.vhn.doan.data.Reminder;
import com.vhn.doan.services.NotificationService;
import com.vhn.doan.services.ReminderService;

import java.util.Calendar;

/**
 * Activity để test và debug hệ thống thông báo nhắc nhở
 */
public class ReminderTestActivity extends Activity {

    private static final String TAG = "ReminderTestActivity";

    private TextView tvStatus;
    private Button btnTestNotification;
    private Button btnTest1MinReminder;
    private Button btnCheckPermissions;
    private Button btnRequestPermissions;

    private NotificationService notificationService;
    private ReminderService reminderService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_test);

        initServices();
        initViews();
        setupClickListeners();
        checkSystemStatus();
    }

    private void initServices() {
        notificationService = new NotificationService(this);
        reminderService = new ReminderService(this);
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tv_status);
        btnTestNotification = findViewById(R.id.btn_test_notification);
        btnTest1MinReminder = findViewById(R.id.btn_test_1min_reminder);
        btnCheckPermissions = findViewById(R.id.btn_check_permissions);
        btnRequestPermissions = findViewById(R.id.btn_request_permissions);
    }

    private void setupClickListeners() {
        btnTestNotification.setOnClickListener(v -> testImmediateNotification());
        btnTest1MinReminder.setOnClickListener(v -> test1MinuteReminder());
        btnCheckPermissions.setOnClickListener(v -> checkSystemStatus());
        btnRequestPermissions.setOnClickListener(v -> requestNecessaryPermissions());
    }

    /**
     * Test thông báo ngay lập tức
     */
    private void testImmediateNotification() {
        Log.d(TAG, "Testing immediate notification...");

        Reminder testReminder = new Reminder();
        testReminder.setId("test_immediate");
        testReminder.setTitle("Test Thông Báo Ngay");
        testReminder.setDescription("Đây là thông báo test để kiểm tra hệ thống");
        testReminder.setActive(true);

        notificationService.showReminderNotification(testReminder);

        Toast.makeText(this, "Đã gửi thông báo test!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Immediate notification sent");
    }

    /**
     * Test reminder 1 phút từ bây giờ
     */
    private void test1MinuteReminder() {
        Log.d(TAG, "Creating 1-minute test reminder...");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 1);

        Reminder testReminder = new Reminder();
        testReminder.setId("test_1min_" + System.currentTimeMillis());
        testReminder.setTitle("Test Reminder 1 Phút");
        testReminder.setDescription("Reminder này sẽ kích hoạt sau 1 phút");
        testReminder.setReminderTime(calendar.getTimeInMillis());
        testReminder.setRepeatType(Reminder.RepeatType.NO_REPEAT);
        testReminder.setActive(true);

        reminderService.scheduleReminder(testReminder);

        String message = "Đã lên lịch reminder cho " +
            new java.text.SimpleDateFormat("HH:mm:ss").format(calendar.getTime());
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        Log.d(TAG, "1-minute reminder scheduled for: " + calendar.getTime());

        updateStatus();
    }

    /**
     * Kiểm tra trạng thái hệ thống
     */
    private void checkSystemStatus() {
        StringBuilder status = new StringBuilder();

        // 1. Kiểm tra quyền thông báo
        boolean notificationEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled();
        status.append("🔔 Quyền thông báo: ").append(notificationEnabled ? "✅ Có" : "❌ Không").append("\n\n");

        // 2. Kiểm tra quyền exact alarm (Android 12+)
        boolean exactAlarmPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            exactAlarmPermission = alarmManager.canScheduleExactAlarms();
        }
        status.append("⏰ Quyền Exact Alarm: ").append(exactAlarmPermission ? "✅ Có" : "❌ Không").append("\n\n");

        // 3. Kiểm tra Battery Optimization
        boolean batteryOptimized = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            android.os.PowerManager pm = (android.os.PowerManager) getSystemService(Context.POWER_SERVICE);
            batteryOptimized = !pm.isIgnoringBatteryOptimizations(packageName);
        }
        status.append("🔋 Battery Optimization: ").append(batteryOptimized ? "❌ Bật (Cần tắt)" : "✅ Tắt").append("\n\n");

        // 4. Thông tin phiên bản Android
        status.append("📱 Android Version: ").append(Build.VERSION.RELEASE)
               .append(" (API ").append(Build.VERSION.SDK_INT).append(")\n\n");

        // 5. Gợi ý
        if (!notificationEnabled || !exactAlarmPermission || batteryOptimized) {
            status.append("⚠️ CẦN THỰC HIỆN:\n");
            if (!notificationEnabled) {
                status.append("• Bật quyền thông báo\n");
            }
            if (!exactAlarmPermission) {
                status.append("• Bật quyền Exact Alarm\n");
            }
            if (batteryOptimized) {
                status.append("• Tắt Battery Optimization\n");
            }
        } else {
            status.append("✅ Tất cả quyền đã được cấp!");
        }

        tvStatus.setText(status.toString());

        Log.d(TAG, "System status checked: " + status.toString());
    }

    /**
     * Yêu cầu các quyền cần thiết
     */
    private void requestNecessaryPermissions() {
        Log.d(TAG, "Requesting necessary permissions...");

        // 1. Mở settings thông báo
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
            Toast.makeText(this, "Vui lòng bật quyền thông báo", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. Yêu cầu quyền Exact Alarm (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                Toast.makeText(this, "Vui lòng bật quyền Exact Alarm", Toast.LENGTH_LONG).show();
                return;
            }
        }

        // 3. Yêu cầu tắt Battery Optimization
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            android.os.PowerManager pm = (android.os.PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
                Toast.makeText(this, "Vui lòng tắt Battery Optimization cho app", Toast.LENGTH_LONG).show();
                return;
            }
        }

        Toast.makeText(this, "Tất cả quyền đã được cấp!", Toast.LENGTH_SHORT).show();
    }

    private void updateStatus() {
        checkSystemStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSystemStatus();
    }
}
