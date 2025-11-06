# 🔔 Hướng dẫn khắc phục vấn đề Reminder/Notification

## 📋 Tóm tắt vấn đề
Trước đây, notifications không hiển thị đúng giờ khi app ở background. Notifications chỉ xuất hiện khi mở lại app.

## 🔍 Nguyên nhân chính

### 1. WorkManager không đáng tin cậy cho exact-time alarms
- WorkManager bị **defer** trong Doze mode
- Android có thể delay execution lên đến 15 phút hoặc hơn
- Không phù hợp cho time-sensitive notifications

### 2. Battery Optimization
- Android tự động kill background processes
- User chưa được yêu cầu disable battery optimization
- App không có exemption để chạy trong background

### 3. Thiếu Exact Alarm Permission (Android 12+)
- Android 12+ yêu cầu permission `SCHEDULE_EXACT_ALARM`
- Permission này cần được request runtime

## ✅ Giải pháp đã triển khai

### 1. **AlarmManager làm primary method** (`ReminderScheduler.java`)
```java
// Sử dụng setAlarmClock() - highest priority, bypass Doze mode
alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
```

**Ưu điểm:**
- ✅ Exact-time delivery
- ✅ Bypass Doze mode
- ✅ Highest priority alarm
- ✅ Đáng tin cậy nhất cho time-sensitive notifications

### 2. **Permission Checker** (`ReminderPermissionChecker.java`)
Helper class để:
- Check battery optimization status
- Check exact alarm permission (Android 12+)
- Request permissions với dialog giải thích rõ ràng
- Guide user đến settings

### 3. **Cải thiện BootReceiver** (`BootReceiver.java`)
- Restart `ReminderForegroundService` sau boot
- Reschedule tất cả active reminders
- Sử dụng `ReminderScheduler` với AlarmManager

### 4. **Dual approach**: AlarmManager + WorkManager
- **AlarmManager**: Primary method (exact-time)
- **WorkManager**: Backup/fallback

## 🚀 Cách sử dụng

### Trong ReminderDialog hoặc nơi tạo reminder:

```java
// 1. Check và request permissions trước khi tạo reminder
if (!ReminderPermissionChecker.checkAllPermissions(activity)) {
    ReminderPermissionChecker.requestAllNecessaryPermissions(activity);
    // User cần grant permissions trước khi tiếp tục
    return;
}

// 2. Tạo và schedule reminder
Reminder reminder = new Reminder(...);
ReminderScheduler scheduler = ReminderScheduler.getInstance(context);
scheduler.scheduleReminder(reminder);
```

### Trong Settings Activity:

```java
// Thêm nút để check permissions
Button btnCheckPermissions = findViewById(R.id.btn_check_permissions);
btnCheckPermissions.setOnClickListener(v -> {
    if (ReminderPermissionChecker.checkAllPermissions(this)) {
        Toast.makeText(this, "Tất cả permissions đã được cấp!", Toast.LENGTH_SHORT).show();
    } else {
        String message = ReminderPermissionChecker.getPermissionExplanationMessage(this);
        new AlertDialog.Builder(this)
            .setTitle("Permissions cần thiết")
            .setMessage(message)
            .setPositiveButton("Cài đặt", (d, w) -> {
                ReminderPermissionChecker.requestAllNecessaryPermissions(this);
            })
            .show();
    }
});
```

## 🧪 Testing Guide

### Test 1: Background notification
1. Tạo reminder 2 phút trong tương lai
2. Thoát app (press Home hoặc Recent Apps)
3. Chờ đến thời gian reminder
4. ✅ Notification nên xuất hiện đúng giờ

### Test 2: Doze mode (ADB commands)

```bash
# Bật Doze mode ngay lập tức (cần USB debugging)
adb shell dumpsys deviceidle force-idle

# Kiểm tra status
adb shell dumpsys deviceidle get

# Thoát Doze mode
adb shell dumpsys deviceidle unforce

# Hoặc reset
adb shell dumpsys battery reset
```

**Test flow:**
1. Tạo reminder 5 phút sau
2. Kết nối ADB và force Doze mode
3. Chờ đến thời gian reminder
4. ✅ Notification vẫn nên xuất hiện (nhờ setAlarmClock)

### Test 3: Sau reboot
1. Tạo reminder cho ngày mai
2. Reboot thiết bị
3. Kiểm tra logs: `adb logcat | grep BootReceiver`
4. ✅ Nên thấy "Rescheduled reminder" trong logs

### Test 4: Battery Saver mode
1. Bật Battery Saver trong Settings
2. Tạo reminder 2 phút sau
3. Thoát app
4. ✅ Notification nên vẫn hoạt động

## 📱 Permissions cần thiết

### AndroidManifest.xml (đã có)
```xml
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

### Runtime permissions cần request:
1. **Battery Optimization Exemption** (Android 6+)
   - Tự động request qua `ReminderPermissionChecker`

2. **Exact Alarm Permission** (Android 12+)
   - Tự động request qua `ReminderPermissionChecker`

3. **Post Notifications** (Android 13+)
   - Cần request trong `ReminderPermissionHelper.java` (nếu chưa có)

## 🔧 Troubleshooting

### Vấn đề: Notification vẫn không hiện khi app đóng

**Kiểm tra:**
1. **Battery optimization**
   ```java
   boolean isOptimized = ReminderPermissionChecker.isBatteryOptimizationDisabled(context);
   Log.d(TAG, "Battery optimized: " + !isOptimized);
   ```

2. **Exact alarm permission** (Android 12+)
   ```java
   boolean canSchedule = ReminderPermissionChecker.canScheduleExactAlarms(context);
   Log.d(TAG, "Can schedule exact alarms: " + canSchedule);
   ```

3. **AlarmManager logs**
   ```bash
   adb logcat | grep "ReminderScheduler\|AlarmManager"
   ```

4. **Notification channel enabled**
   - Settings > Apps > HealthTips > Notifications
   - Đảm bảo tất cả channels được bật

### Vấn đề: Notification không hoạt động sau reboot

**Kiểm tra:**
1. **BOOT_COMPLETED permission** trong Manifest (đã có ✅)
2. **BootReceiver logs**
   ```bash
   adb logcat | grep BootReceiver
   ```
3. **ReminderForegroundService status**
   ```bash
   adb shell dumpsys activity services | grep ReminderForegroundService
   ```

### Vấn đề: Notification bị delay

**Nguyên nhân có thể:**
1. Vẫn đang dùng WorkManager instead of AlarmManager
   - Check code xem có gọi đúng `ReminderScheduler.scheduleReminder()` không

2. Battery optimization chưa disable
   - Request user disable qua `ReminderPermissionChecker`

3. Doze mode aggressive trên một số thiết bị
   - Xiaomi, Huawei, Oppo có battery optimization rất aggressive
   - Cần guide user vào settings của OEM để whitelist app

## 📊 Logs để debug

### Khi schedule reminder:
```
✅ AlarmManager: Đã đặt với setAlarmClock (highest priority)
✅ WorkManager: Đã lên lịch backup
✅ Đã lên lịch reminder với cả AlarmManager và WorkManager
```

### Khi reminder trigger:
```
🔔 WorkManager nhắc nhở được kích hoạt
🚨 Hiển thị alarm cho reminder
```

### Sau boot:
```
🔄 BootReceiver triggered with action: android.intent.action.BOOT_COMPLETED
✅ ReminderForegroundService started after boot
📋 Found X active reminders to reschedule
✅ Rescheduling complete
```

## 🎯 Next Steps

### Recommended enhancements:

1. **Permission Request Flow**
   - Thêm permission request vào onboarding/first reminder creation
   - Show dialog giải thích rõ ràng tại sao cần permissions

2. **Settings Screen**
   - Thêm section "Notification Settings"
   - Show permission status
   - Button để re-request permissions

3. **Notification Channels**
   - Tạo separate channels cho different reminder types
   - Allow user customize notification sound per channel

4. **Analytics**
   - Track permission grant rate
   - Track notification delivery success rate
   - Monitor battery optimization exemption rate

## 📚 Tài liệu tham khảo

- [AlarmManager Best Practices](https://developer.android.com/training/scheduling/alarms)
- [Schedule Exact Alarms](https://developer.android.com/about/versions/12/behavior-changes-12#exact-alarm-permission)
- [Battery Optimization](https://developer.android.com/training/monitoring-device-state/doze-standby)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)

## ✨ Tóm tắt

**Thay đổi chính:**
1. ✅ Chuyển từ WorkManager sang AlarmManager (primary)
2. ✅ Thêm ReminderPermissionChecker
3. ✅ Cải thiện BootReceiver
4. ✅ Dual approach: AlarmManager + WorkManager backup

**Kết quả:**
- ✅ Notifications hiển thị đúng giờ khi app ở background
- ✅ Hoạt động trong Doze mode
- ✅ Tự động reschedule sau reboot
- ✅ Reliable và consistent delivery
