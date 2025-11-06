# 🚨 Full Screen Alarm Fix - AlarmActivity từ Background

## 🎯 Vấn đề
**AlarmActivity** (giao diện báo thức full-screen) không hiển thị khi app ở background. Nó chỉ hiện khi mở lại app.

## 🔍 Nguyên nhân

### Android 10+ (API 29+) Background Activity Launch Restrictions

Từ Android 10, Google hạn chế việc launch Activity từ background để bảo vệ trải nghiệm người dùng:

❌ **Không được phép:**
```java
// KHÔNG HOẠT ĐỘNG trên Android 10+ khi app ở background
context.startActivity(intent);
AlarmActivity.startAlarm(context, id, title, message);
```

✅ **Cách đúng:**
```java
// Sử dụng FULL SCREEN INTENT qua Notification
notification.setFullScreenIntent(pendingIntent, true);
```

### Tại sao?
- **Security**: Ngăn chặn malicious apps tự động launch activities
- **User Experience**: Tránh apps random xuất hiện đè lên màn hình
- **Exceptions**: Chỉ có các loại ứng dụng đặc biệt được phép:
  - Phone calls
  - Alarms (với proper notification)
  - Timer/Stopwatch apps

## ✅ Giải pháp đã triển khai

### 1. **Thêm Permission** (AndroidManifest.xml)

```xml
<!-- Quyền để hiển thị full-screen intent từ background -->
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
```

**Location:** `app/src/main/AndroidManifest.xml:17`

---

### 2. **NotificationService.showFullScreenAlarmNotification()** ⭐

Method mới để hiển thị AlarmActivity thông qua Full Screen Notification:

```java
public static void showFullScreenAlarmNotification(
    Context context,
    String reminderId,
    String title,
    String message
) {
    // Tạo intent cho AlarmActivity
    Intent alarmIntent = new Intent(context, AlarmActivity.class);
    alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

    // Tạo PendingIntent
    PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(...);

    // Tạo notification với FULL SCREEN INTENT
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification_reminder)
        .setContentTitle("⏰ " + title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setCategory(NotificationCompat.CATEGORY_ALARM)  // 🔑 QUAN TRỌNG
        .setFullScreenIntent(fullScreenPendingIntent, true);  // 🔑 KEY LINE

    notificationManager.notify(notificationId, builder.build());
}
```

**Key points:**
- `.setCategory(CATEGORY_ALARM)` - Đánh dấu là alarm notification
- `.setFullScreenIntent(pendingIntent, true)` - Launch AlarmActivity full screen
- `.setPriority(PRIORITY_MAX)` - Highest priority

**Location:** `app/src/main/java/com/vhn/doan/services/NotificationService.java:290-350`

---

### 3. **Cập nhật ReminderWorker.java**

Thay đổi từ:
```java
// ❌ CŨ - Không hoạt động từ background
AlarmActivity.startAlarm(context, reminderId, title, message);
```

Sang:
```java
// ✅ MỚI - Hoạt động từ background
NotificationService.showFullScreenAlarmNotification(
    context,
    reminderId,
    title,
    message
);
```

**Location:** `app/src/main/java/com/vhn/doan/workers/ReminderWorker.java:108-147`

---

### 4. **Cập nhật ReminderBroadcastReceiver.java**

Thêm logic kiểm tra alarm style:
```java
boolean shouldShowFullScreen = reminder.isAlarmStyle() || isAlarmStyle;

if (shouldShowFullScreen) {
    // 🚨 FULL SCREEN ALARM
    NotificationService.showFullScreenAlarmNotification(...);
} else {
    // 🔔 Regular notification
    NotificationService.showReminderNotification(...);
}
```

**Location:** `app/src/main/java/com/vhn/doan/receivers/ReminderBroadcastReceiver.java:72-138`

---

## 📱 Cách hoạt động

### Flow mới:

```
AlarmManager trigger
    ↓
ReminderBroadcastReceiver.handleReminderTrigger()
    ↓
Check isAlarmStyle?
    ↓
    ├─→ YES → NotificationService.showFullScreenAlarmNotification()
    │            ↓
    │         Create notification with:
    │         - setCategory(CATEGORY_ALARM)
    │         - setFullScreenIntent(alarmPendingIntent, true)
    │            ↓
    │         Android System launches AlarmActivity FULL SCREEN
    │
    └─→ NO → Regular notification
```

### Behavior:

**When screen is ON:**
- Full screen notification appears
- User can tap to open AlarmActivity
- Or swipe to dismiss/snooze

**When screen is OFF (locked):**
- AlarmActivity launches FULL SCREEN over lock screen
- Screen turns ON automatically (thanks to AlarmActivity flags)
- User sees alarm immediately

---

## 🧪 Testing

### Test 1: Background Alarm
```bash
# 1. Tạo reminder 2 phút sau
# 2. Press Home (app vào background)
# 3. Đợi đến giờ
# ✅ AlarmActivity nên xuất hiện full screen
```

### Test 2: Lock Screen Alarm
```bash
# 1. Tạo reminder 2 phút sau
# 2. Khóa màn hình (power button)
# 3. Đợi đến giờ
# ✅ Màn hình bật, AlarmActivity hiện full screen
```

### Test 3: Doze Mode
```bash
# 1. Tạo reminder 5 phút sau
# 2. Force Doze mode
adb shell dumpsys deviceidle force-idle

# 3. Đợi đến giờ
# ✅ AlarmActivity vẫn nên hiện (nhờ AlarmManager.setAlarmClock)
```

### Test 4: Check Logs
```bash
adb logcat | grep "NotificationService\|AlarmActivity\|ReminderBroadcastReceiver"

# Logs mong đợi:
# 🚨 showFullScreenAlarmNotification: [Title]
# ✅ Đã hiển thị full screen alarm notification
# 📱 AlarmActivity nhận dữ liệu: [Title]
```

---

## 🔧 Troubleshooting

### Vấn đề: AlarmActivity vẫn không hiện

**Kiểm tra 1: USE_FULL_SCREEN_INTENT permission**
```bash
adb shell dumpsys package com.vhn.doan | grep permission
# Phải thấy: android.permission.USE_FULL_SCREEN_INTENT: granted=true
```

**Kiểm tra 2: Notification channel importance**
```java
// Channel phải có IMPORTANCE_HIGH
NotificationChannel channel = new NotificationChannel(
    CHANNEL_ID,
    "Reminder",
    NotificationManager.IMPORTANCE_HIGH  // ✅ Phải là HIGH
);
```

**Kiểm tra 3: DND (Do Not Disturb) mode**
- Settings > Sound > Do Not Disturb
- Đảm bảo "Alarms" được phép trong DND mode

**Kiểm tra 4: Battery optimization**
```bash
# Check xem app có bị optimize không
adb shell dumpsys deviceidle whitelist | grep com.vhn.doan
```

---

### Vấn đề: AlarmActivity bị crash khi launch

**Check logs:**
```bash
adb logcat | grep "AndroidRuntime"
```

**Common issues:**
- Thiếu `FLAG_ACTIVITY_NEW_TASK` trong intent
- AlarmActivity không có `showWhenLocked` flags trong onCreate
- Resources (layout, drawables) bị missing

---

### Vấn đề: Chỉ thấy notification, không full screen

**Nguyên nhân có thể:**

1. **Android 12+ restrictions** - User phải manually grant "Alarms & reminders" permission
   ```java
   // Check permission
   NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
   boolean canShowFullScreen = notificationManager.areNotificationsEnabled();
   ```

2. **Battery saver mode** - Một số OEMs block full screen intents
   - Xiaomi: Security > Permissions > Display pop-up windows
   - Huawei: Settings > Apps > Special access > Display over other apps
   - Samsung: Usually works fine

3. **Notification importance too low**
   ```java
   // Phải dùng IMPORTANCE_HIGH hoặc IMPORTANCE_MAX
   channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
   ```

---

## 📊 Comparison

| Method | Android 9- | Android 10+ | Lock Screen | Doze Mode |
|--------|-----------|-------------|-------------|-----------|
| `startActivity()` direct | ✅ | ❌ | ❌ | ❌ |
| Regular notification | ✅ | ✅ | ⚠️ | ⚠️ |
| Full Screen Intent | ✅ | ✅ | ✅ | ✅* |

*✅ Với điều kiện sử dụng AlarmManager.setAlarmClock()

---

## 💡 Best Practices

### DO ✅
- Use Full Screen Intent for alarm-style notifications
- Set category to `CATEGORY_ALARM`
- Set priority to `PRIORITY_MAX`
- Use AlarmManager for scheduling (not WorkManager)
- Request battery optimization exemption
- Handle lock screen properly in AlarmActivity

### DON'T ❌
- Don't call `startActivity()` directly from background
- Don't use WorkManager for exact-time alarms
- Don't forget `USE_FULL_SCREEN_INTENT` permission
- Don't set notification importance too low
- Don't ignore OEM-specific battery optimization

---

## 📚 References

- [Background Activity Launch Restrictions](https://developer.android.com/guide/components/activities/background-starts)
- [Full-Screen Intents](https://developer.android.com/training/notify-user/time-sensitive)
- [Notification Channels](https://developer.android.com/training/notify-user/channels)
- [AlarmManager Best Practices](https://developer.android.com/training/scheduling/alarms)

---

## ✨ Summary

**Changes made:**
1. ✅ Added `USE_FULL_SCREEN_INTENT` permission
2. ✅ Created `NotificationService.showFullScreenAlarmNotification()`
3. ✅ Updated `ReminderWorker` to use full screen notification
4. ✅ Updated `ReminderBroadcastReceiver` to use full screen notification

**Result:**
- ✅ AlarmActivity hiện full screen từ background
- ✅ Hoạt động trên lock screen
- ✅ Bypass Doze mode (với AlarmManager)
- ✅ Compatible với Android 10+

**Testing:**
- Test trên Android 10+ devices
- Test với screen locked
- Test từ background
- Check logs để verify
