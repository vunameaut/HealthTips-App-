# ✅ SỬA LỖI FULL SCREEN ALARM NOTIFICATION

## 📋 Tóm tắt vấn đề

Ứng dụng không hiển thị full screen alarm notification (báo thức toàn màn hình) vì **thiếu kiểm tra quyền USE_FULL_SCREEN_INTENT** cho Android 14+ (API 34+).

## 🔧 Các thay đổi đã thực hiện

### 1. ✨ Cập nhật `ReminderPermissionChecker.java`
**File:** `app/src/main/java/com/vhn/doan/utils/ReminderPermissionChecker.java`

**Thêm mới:**
- ✅ Kiểm tra quyền `POST_NOTIFICATIONS` (Android 13+)
- ✅ Request quyền `POST_NOTIFICATIONS` với dialog giải thích
- ✅ Method `checkRequiredPermissions()` để check chỉ các quyền bắt buộc
- ✅ Method `getMissingPermissions()` để liệt kê quyền còn thiếu

**Thứ tự kiểm tra quyền (quan trọng nhất trước):**
1. `POST_NOTIFICATIONS` (Android 13+) - **BẮT BUỘC**
2. `SCHEDULE_EXACT_ALARM` (Android 12+) - **BẮT BUỘC**
3. `USE_FULL_SCREEN_INTENT` (Android 14+) - **BẮT BUỘC** cho full screen alarm
4. Battery Optimization - **KHUYẾN NGHỊ**

---

### 2. 🎯 Cập nhật `ReminderPermissionHelper.java`
**File:** `app/src/main/java/com/vhn/doan/utils/ReminderPermissionHelper.java`

**QUAN TRỌNG - ĐÂY LÀ FIX CHÍNH:**
- ✅ **Thêm check quyền `USE_FULL_SCREEN_INTENT`** (Android 14+)
- ✅ **Thêm method `hasFullScreenIntentPermission()`**
- ✅ **Thêm method `requestFullScreenIntentPermission()`**
- ✅ **Thêm dialog giải thích quyền full screen intent**
- ✅ Cập nhật callback interface với `onFullScreenIntentDenied()`
- ✅ Thêm logging chi tiết cho từng bước kiểm tra quyền

**Thứ tự kiểm tra trong flow:**
```java
1. POST_NOTIFICATIONS      → Nếu thiếu: hiện dialog và request
2. SCHEDULE_EXACT_ALARM    → Nếu thiếu: hiện dialog và request
3. USE_FULL_SCREEN_INTENT  → Nếu thiếu: hiện dialog và request (⭐ MỚI)
4. Battery Optimization    → Nếu thiếu: hiện dialog và request (khuyến nghị)
```

---

### 3. 🔔 Cập nhật `NotificationService.java`
**File:** `app/src/main/java/com/vhn/doan/services/NotificationService.java`

**Cải tiến:**
- ✅ Thêm check `POST_NOTIFICATIONS` permission ở đầu method `showFullScreenAlarmNotification()`
- ✅ Cải thiện logging với thông báo rõ ràng hơn khi thiếu quyền
- ✅ Hướng dẫn cụ thể cho người dùng khi thiếu quyền:
  - Notification permission: `Settings > Apps > Notifications`
  - Full screen intent: `Settings > Apps > Special app access > Alarms & reminders`

**Log messages mới:**
```
❌ KHÔNG CÓ QUYỀN GỬI THÔNG BÁO!
   User cần bật quyền notification trong Settings > Apps > Notifications
   App sẽ KHÔNG thể hiển thị bất kỳ notification nào!

⚠️ KHÔNG CÓ QUYỀN full screen intent!
   User cần bật trong Settings > Apps > Special app access > Alarms & reminders
   Fallback: Sẽ hiển thị HIGH PRIORITY notification thay thế

✅ Đã POST alarm notification (ID: xxx)
   - Full screen: true/false
   - Title: [title]
   - ReminderId: [id]
```

---

### 4. 📱 Cập nhật `ReminderFragment.java`
**File:** `app/src/main/java/com/vhn/doan/presentation/reminder/ReminderFragment.java`

**Thêm callback:**
```java
@Override
public void onFullScreenIntentDenied() {
    Log.w(TAG, "⚠️ Quyền full screen intent bị từ chối");
    Toast.makeText(requireContext(),
        "App sẽ dùng thông báo ưu tiên cao thay vì báo thức toàn màn hình",
        Toast.LENGTH_LONG).show();
    // Vẫn tiếp tục kiểm tra quyền khác
    checkReminderPermissions();
}
```

---

## 🎯 Cách hoạt động mới

### Khi người dùng mở ReminderFragment:

1. **Auto-check permissions** được gọi trong `onViewCreated()`:
   ```
   checkReminderPermissions() → ReminderPermissionHelper.checkAndRequestAllPermissions()
   ```

2. **Flow kiểm tra tuần tự** (từng quyền một để không overwhelm user):
   ```
   Check POST_NOTIFICATIONS
   ↓ (nếu có)
   Check SCHEDULE_EXACT_ALARM
   ↓ (nếu có)
   Check USE_FULL_SCREEN_INTENT  ← ⭐ MỚI, QUAN TRỌNG
   ↓ (nếu có)
   Check Battery Optimization (optional)
   ↓ (nếu có hoặc skip)
   ✅ ALL PERMISSIONS GRANTED → Start ReminderService
   ```

3. **Mỗi quyền thiếu sẽ hiện dialog giải thích:**
   - Dialog có 2 nút: "Cài đặt" (mở Settings) và "Bỏ qua" (skip)
   - Nếu user bỏ qua quyền optional (battery), vẫn tiếp tục
   - Nếu user bỏ qua quyền bắt buộc, hiện warning và không schedule reminder

---

## 📱 Hướng dẫn cho người dùng

### Khi app yêu cầu quyền "Full Screen Intent" (Android 14+):

**Bước 1:** Nhấn nút "Cài đặt" trong dialog

**Bước 2:** Trong màn hình Settings sẽ thấy:
- **Tiếng Anh:** "Alarms & reminders" hoặc "Display over other apps"
- **Tiếng Việt:** "Báo thức và lời nhắc" hoặc "Hiển thị trên các ứng dụng khác"

**Bước 3:** Bật toggle **ON** cho app HealthTips

**Bước 4:** Quay lại app, app sẽ tự động kiểm tra lại quyền

### Nếu không cấp quyền Full Screen Intent:

- ⚠️ App vẫn hoạt động bình thường
- ⚠️ Nhưng sẽ hiển thị **High Priority Notification** thay vì **Full Screen Alarm**
- ⚠️ User phải mở notification tray để thấy thông báo (không tự động hiện toàn màn hình)

---

## 🧪 Cách test

### Test trên Android 14+ (API 34+):

1. **Uninstall app cũ** (để reset permissions):
   ```bash
   adb uninstall com.vhn.doan
   ```

2. **Install app mới:**
   ```bash
   ./gradlew installDebug
   ```

3. **Mở app và vào tab Reminder**

4. **Quan sát dialogs xuất hiện tuần tự:**
   - ✅ Dialog 1: Quyền thông báo (POST_NOTIFICATIONS)
   - ✅ Dialog 2: Quyền báo thức chính xác (SCHEDULE_EXACT_ALARM)
   - ✅ Dialog 3: **Quyền hiển thị báo thức toàn màn hình (USE_FULL_SCREEN_INTENT)** ← MỚI
   - ✅ Dialog 4: Tối ưu hóa pin (Battery Optimization)

5. **Tạo một reminder test** (ví dụ: sau 2 phút)

6. **Chờ và quan sát:**
   - **Có quyền full screen:** AlarmActivity hiện toàn màn hình, có âm thanh + rung
   - **Không có quyền:** Notification bar hiện thông báo ưu tiên cao

### Kiểm tra logs:

```bash
adb logcat | grep -E "ReminderPermissionHelper|NotificationService|ReminderFragment"
```

**Expected logs khi thiếu quyền:**
```
D/ReminderPermissionHelper: 🔍 Bắt đầu kiểm tra tất cả permissions cho reminder
D/ReminderPermissionHelper: ✅ Có quyền POST_NOTIFICATIONS
D/ReminderPermissionHelper: ✅ Có quyền SCHEDULE_EXACT_ALARM
W/ReminderPermissionHelper: ❌ Thiếu quyền USE_FULL_SCREEN_INTENT (Android 14+)
```

**Expected logs khi có đủ quyền:**
```
D/ReminderPermissionHelper: ✅✅✅ TẤT CẢ QUYỀN CẦN THIẾT ĐÃ ĐƯỢC CẤP
D/NotificationService: ✅ Full screen intent ENABLED - Sẽ hiển thị full screen alarm
D/NotificationService: ✅ Đã POST alarm notification
```

---

## 🚀 Kết quả

### ✅ Trước khi fix:
- ❌ App không check quyền USE_FULL_SCREEN_INTENT
- ❌ Full screen alarm không hiện (silent fail)
- ❌ Không có thông báo lỗi rõ ràng trong logs
- ❌ User không biết phải cấp quyền gì

### ✅ Sau khi fix:
- ✅ **App check đầy đủ tất cả quyền cần thiết**
- ✅ **Full screen alarm hoạt động trên Android 14+**
- ✅ **Logging chi tiết, dễ debug**
- ✅ **Dialog hướng dẫn user cấp quyền từng bước**
- ✅ **Graceful fallback** nếu user không cấp quyền

---

## 📝 Ghi chú kỹ thuật

### Android Permission Levels:

| Permission | API Level | Type | Required? |
|-----------|-----------|------|-----------|
| POST_NOTIFICATIONS | 33+ (Android 13) | Runtime | ✅ BẮT BUỘC |
| SCHEDULE_EXACT_ALARM | 31+ (Android 12) | Special | ✅ BẮT BUỘC |
| USE_FULL_SCREEN_INTENT | 34+ (Android 14) | Special | ✅ BẮT BUỘC (cho alarm style) |
| BATTERY_OPTIMIZATION | 23+ (Android 6) | Special | ⚠️ KHUYẾN NGHỊ |

### Settings Intent Actions:

```java
// POST_NOTIFICATIONS (Android 13+)
ActivityCompat.requestPermissions(activity,
    new String[]{Manifest.permission.POST_NOTIFICATIONS},
    REQUEST_CODE);

// SCHEDULE_EXACT_ALARM (Android 12+)
Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
intent.setData(Uri.parse("package:" + packageName));
startActivity(intent);

// USE_FULL_SCREEN_INTENT (Android 14+)
Intent intent = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT);
intent.setData(Uri.parse("package:" + packageName));
startActivity(intent);

// BATTERY_OPTIMIZATION (Android 6+)
Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
intent.setData(Uri.parse("package:" + packageName));
startActivity(intent);
```

---

## 🔗 Files đã thay đổi

1. ✅ `app/src/main/java/com/vhn/doan/utils/ReminderPermissionChecker.java`
2. ✅ `app/src/main/java/com/vhn/doan/utils/ReminderPermissionHelper.java` ← **QUAN TRỌNG NHẤT**
3. ✅ `app/src/main/java/com/vhn/doan/services/NotificationService.java`
4. ✅ `app/src/main/java/com/vhn/doan/presentation/reminder/ReminderFragment.java`

---

## ✅ Checklist

- [x] Thêm check quyền USE_FULL_SCREEN_INTENT
- [x] Thêm request quyền USE_FULL_SCREEN_INTENT
- [x] Thêm dialog giải thích cho user
- [x] Cải thiện logging để dễ debug
- [x] Thêm callback xử lý khi user từ chối quyền
- [x] Build thành công không có lỗi
- [x] Tạo file hướng dẫn chi tiết

---

**Ngày cập nhật:** 2025-11-06
**Người thực hiện:** Claude Code
**Status:** ✅ HOÀN THÀNH
