# 🚨 QUICK FIX: Full Screen Alarm Not Showing

## ⚠️ Vấn đề
Full screen alarm **vẫn không hiển thị** khi thoát app ra ngoài.

## 🔍 Nguyên nhân chính

### Android 14+ (API 34+) yêu cầu MANUAL permission grant

Từ Android 14, Google yêu cầu user **manually grant permission** trong Settings để app có thể hiển thị full screen notifications từ background.

Permission này **KHÔNG thể request tự động** qua dialog - user PHẢI vào Settings để enable.

---

## ✅ GIẢI PHÁP - Bật permission manually

### Bước 1: Vào Settings

Có 2 cách:

#### Cách 1: Qua App Info
```
Settings (Cài đặt)
  → Apps (Ứng dụng)
    → HealthTips (app của bạn)
      → Tap vào app name
```

#### Cách 2: Qua Special App Access (Nhanh hơn)
```
Settings (Cài đặt)
  → Apps (Ứng dụng)
    → Special app access (Quyền truy cập đặc biệt)
      → Display over other apps (Hiển thị trên các ứng dụng khác)
        → Tìm "HealthTips"
          → Toggle ON
```

### Bước 2: Enable permission

Tùy Android version, tên có thể khác:
- **"Display over other apps"**
- **"Full screen notifications"**
- **"Display pop-up windows while running in background"**
- **"Alarms & reminders"** (một số thiết bị)

**→ BẬT TOGGLE LÊN** ✅

### Bước 3: Test lại

1. Tạo reminder 2 phút sau
2. Thoát app (press Home)
3. Chờ đến giờ
4. ✅ AlarmActivity nên hiện full screen

---

## 🧪 Check xem có permission chưa

### Method 1: Qua code

Thêm code này vào app để check:

```java
import com.vhn.doan.utils.ReminderPermissionChecker;

// Check permission
boolean canShowFullScreen = ReminderPermissionChecker.canUseFullScreenIntent(context);

if (!canShowFullScreen) {
    Log.w("TEST", "⚠️ KHÔNG CÓ PERMISSION full screen intent!");
    // Request permission
    ReminderPermissionChecker.requestFullScreenIntentPermission(activity);
} else {
    Log.d("TEST", "✅ CÓ permission full screen intent");
}
```

### Method 2: Qua ADB

```bash
# Check permission status
adb shell dumpsys notification | grep "canUseFullScreenIntent"

# Expected output if GRANTED:
# canUseFullScreenIntent: true

# If DENIED:
# canUseFullScreenIntent: false
```

### Method 3: Check logs khi trigger alarm

```bash
adb logcat | grep "NotificationService"

# Nếu KHÔNG có permission, sẽ thấy:
# ⚠️ KHÔNG CÓ PERMISSION full screen intent!
# User cần enable trong Settings > Apps > Special app access

# Nếu CÓ permission, sẽ thấy:
# ✅ Full screen intent enabled
# ✅ Đã hiển thị alarm notification
```

---

## 🔧 Code changes đã implement

### 1. **ReminderPermissionChecker.java** - Added permission check

```java
// Check full screen intent permission (Android 14+)
public static boolean canUseFullScreenIntent(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        return nm.canUseFullScreenIntent();
    }
    return true;
}

// Request permission
@RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
public static void requestFullScreenIntentPermission(Activity activity) {
    // Show dialog explaining why and open settings
    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT);
    intent.setData(Uri.parse("package:" + activity.getPackageName()));
    activity.startActivity(intent);
}
```

### 2. **NotificationService.java** - Runtime check before using full screen

```java
public static void showFullScreenAlarmNotification(...) {
    // ⭐ CHECK permission trước
    boolean canUseFullScreen = true;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        canUseFullScreen = nm.canUseFullScreenIntent();
    }

    // CHỈ dùng full screen nếu có permission
    if (canUseFullScreen) {
        builder.setFullScreenIntent(fullScreenPendingIntent, true);
    } else {
        // Fallback: High priority notification
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
    }
}
```

---

## 📱 Test scenario

### Test 1: Với permission (Android 14+)

```bash
# 1. Grant permission manually trong Settings (như hướng dẫn trên)

# 2. Check permission qua code/ADB
# → Should return TRUE

# 3. Tạo reminder và test
# → Full screen alarm nên hiện ✅
```

### Test 2: Không có permission (Android 14+)

```bash
# 1. Chưa grant permission

# 2. Check permission qua code/ADB
# → Should return FALSE

# 3. Tạo reminder và test
# → Chỉ thấy notification thông thường (không full screen) ⚠️

# 4. Check logs sẽ thấy:
# ⚠️ KHÔNG CÓ PERMISSION full screen intent!
```

### Test 3: Android 13 và thấp hơn

```bash
# Android 13- không cần permission này
# Full screen alarm nên hoạt động bình thường ✅
```

---

## 🎯 Integration vào UI

### Option 1: Request khi tạo reminder đầu tiên

```java
// Trong ReminderDialog hoặc nơi tạo reminder
if (!ReminderPermissionChecker.checkAllPermissions(activity)) {
    ReminderPermissionChecker.requestAllNecessaryPermissions(activity);
    // Sẽ show dialog sequence request từng permission
}
```

### Option 2: Add vào Settings screen

```java
// Trong NotificationSettingsActivity
Button btnFullScreenPermission = findViewById(R.id.btn_fullscreen_permission);

if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
    btnFullScreenPermission.setVisibility(View.VISIBLE);

    boolean hasPermission = ReminderPermissionChecker.canUseFullScreenIntent(this);
    btnFullScreenPermission.setText(hasPermission ?
        "✅ Full screen alarm: Enabled" :
        "⚠️ Full screen alarm: Disabled - Tap to enable");

    btnFullScreenPermission.setOnClickListener(v -> {
        if (!hasPermission) {
            ReminderPermissionChecker.requestFullScreenIntentPermission(this);
        }
    });
} else {
    // Android 13- không cần button này
    btnFullScreenPermission.setVisibility(View.GONE);
}
```

### Option 3: Show warning banner

```java
// Trong ReminderFragment
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
    if (!ReminderPermissionChecker.canUseFullScreenIntent(getContext())) {
        // Show warning banner
        warningBanner.setVisibility(View.VISIBLE);
        warningBanner.setText("⚠️ Full screen alarm disabled. Tap to enable.");
        warningBanner.setOnClickListener(v -> {
            ReminderPermissionChecker.requestFullScreenIntentPermission(getActivity());
        });
    }
}
```

---

## ⚠️ Important Notes

### 1. **Permission chỉ áp dụng Android 14+**

- Android 13 và thấp hơn: Permission tự động granted nếu có trong manifest ✅
- Android 14+: Phải manual grant trong Settings ⚠️

### 2. **Không thể request qua dialog**

Unlike other permissions (camera, location, etc.), full screen intent permission **KHÔNG thể request qua runtime dialog**.

User **PHẢI** vào Settings manually.

### 3. **Check permission trước khi schedule**

Best practice: Check permission khi user tạo reminder, không phải lúc trigger.

```java
// GOOD ✅
if (!ReminderPermissionChecker.canUseFullScreenIntent(context)) {
    // Show dialog guide user to settings
    ReminderPermissionChecker.requestFullScreenIntentPermission(activity);
    return; // Don't create reminder yet
}
// Create reminder...

// BAD ❌
// Create reminder without checking
// → User sẽ không biết tại sao alarm không hiện
```

### 4. **Fallback gracefully**

Code hiện tại đã có fallback:
- Có permission → Full screen alarm ✅
- Không có permission → High priority notification ⚠️

User vẫn nhận được notification, chỉ không full screen.

---

## 📋 Checklist để fix

- [x] Thêm `USE_FULL_SCREEN_INTENT` permission vào Manifest
- [x] Implement `canUseFullScreenIntent()` check
- [x] Implement `requestFullScreenIntentPermission()`
- [x] Update `NotificationService` với runtime check
- [x] Add fallback nếu không có permission
- [ ] **TODO: Integrate vào UI** (ReminderDialog/Settings)
- [ ] **TODO: Test trên Android 14+ device**

---

## 🔍 Debugging

### Check 1: Permission trong Manifest
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
```
✅ Đã có

### Check 2: Runtime check
```bash
adb logcat | grep "canUseFullScreenIntent"

# Nên thấy:
# Can use full screen intent: true/false
```

### Check 3: Notification trigger
```bash
adb logcat | grep "NotificationService"

# Nếu CÓ permission:
# ✅ Full screen intent enabled

# Nếu KHÔNG có:
# ⚠️ KHÔNG CÓ PERMISSION full screen intent!
# ⚠️ Fallback: High priority notification
```

### Check 4: Manual verification
```
1. Vào Settings > Apps > HealthTips > Special app access
2. Tìm "Display over other apps" hoặc "Full screen notifications"
3. Verify toggle là ON ✅
```

---

## ✨ Summary

**Root cause:** Android 14+ requires manual permission grant for full screen intents

**Solution implemented:**
1. ✅ Added permission check (`canUseFullScreenIntent()`)
2. ✅ Added request method (`requestFullScreenIntentPermission()`)
3. ✅ Updated NotificationService with runtime check
4. ✅ Added graceful fallback

**What user needs to do:**
1. Go to Settings > Apps > HealthTips > Special app access
2. Enable "Display over other apps" or "Full screen notifications"
3. Test alarm again

**Next steps:**
1. Build và install app mới
2. Grant permission manually trong Settings
3. Test alarm từ background
4. (Optional) Add UI để guide user grant permission

---

## 📞 Support

Nếu vẫn không hoạt động sau khi grant permission:

1. Check logs: `adb logcat | grep "NotificationService\|ReminderPermissionChecker"`
2. Verify permission: Use code snippet trên
3. Try reboot device
4. Check battery optimization cũng đã disable
5. Check DND mode settings

**Expected logs khi hoạt động:**
```
🚨 showFullScreenAlarmNotification: [Title]
✅ Full screen intent enabled
✅ Đã hiển thị alarm notification
📱 AlarmActivity nhận dữ liệu: [Title]
```
