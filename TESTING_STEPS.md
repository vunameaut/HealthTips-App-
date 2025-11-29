# 🧪 HƯỚNG DẪN TEST BÁO CÁO - BƯỚC TIẾP THEO

## ✅ ĐÃ KIỂM TRA VÀ XÁC NHẬN

### 1. APK mới đã được build thành công
- ✅ Location: `D:\app\HealthTips-App-\app\build\intermediates\apk\debug\app-debug.apk`
- ✅ URL đã được fix: `https://healthtips-admin.vercel.app/api`
- ✅ Build thành công lúc: 2025-11-29

### 2. Web Admin đã deploy thành công
- ✅ Production URL: `https://healthtips-admin.vercel.app`
- ✅ Deployment mới nhất: `healthtips-admin-50ho1qyml`
- ✅ API endpoint: `/api/admin-notifications/user-report`

### 3. Cấu hình Firebase đúng
- ✅ API ghi vào: `admin_notifications`
- ✅ Web admin đọc từ: `admin_notifications`
- ✅ Firebase Admin SDK đã được cấu hình đúng
- ✅ Vercel environment variables đã set

---

## 📋 CÁC BƯỚC TIẾP THEO - QUAN TRỌNG!

### Bước 1: Uninstall app cũ và install app mới

**Rất quan trọng:** Bạn PHẢI uninstall app cũ và install app mới có URL đúng!

```bash
# 1. Uninstall app cũ
adb uninstall com.vhn.doan

# 2. Install app mới
adb install "D:\app\HealthTips-App-\app\build\intermediates\apk\debug\app-debug.apk"

# 3. Verify installed
adb shell pm list packages | grep com.vhn.doan
```

**Output mong đợi:**
```
Success
Success
package:com.vhn.doan
```

---

### Bước 2: Mở terminal để monitor log

**Mở terminal riêng** và chạy lệnh này để theo dõi log từ app:

```bash
adb logcat -c
adb logcat | grep -E "AdminNotificationSender|ReportIssue"
```

Giữ terminal này mở và quan sát log khi bạn gửi báo cáo.

---

### Bước 3: Test gửi báo cáo từ app

1. Mở app trên thiết bị/emulator
2. **Đăng nhập vào app** (bắt buộc!)
3. Vào: **Settings** → **Support** → **Report Issue**
4. Điền form:
   - Report Type: Chọn bất kỳ (ví dụ: "Bug")
   - Subject: "Test report"
   - Description: "Testing admin notification system"
5. Nhấn **Submit**

---

### Bước 4: Quan sát log và xác nhận

**Trong terminal log (Bước 2), bạn cần thấy các dòng sau:**

```
AdminNotificationSender: sendUserReport called - reportType: bug
AdminNotificationSender: Getting Firebase ID token for user: [user-id]
AdminNotificationSender: Got Firebase ID token successfully
AdminNotificationSender: Sending request to: https://healthtips-admin.vercel.app/api/admin-notifications/user-report
AdminNotificationSender: Request body: {"userId":"...","userName":"...","reportType":"bug",...}
AdminNotificationSender: Admin notification sent successfully
```

**Nếu thấy "sent successfully"** → Tiếp tục Bước 5

**Nếu thấy lỗi** → Chụp ảnh log và báo lại

---

### Bước 5: Kiểm tra web admin

1. Mở trình duyệt: `https://healthtips-admin.vercel.app/admin-notifications`
2. **Hard refresh:** Ctrl+Shift+R (Windows) hoặc Cmd+Shift+R (Mac)
3. Kiểm tra tab **"Chưa đọc"**
4. Hoặc filter theo **"Báo cáo từ User"**

**Nên thấy:** Báo cáo mới với nội dung "Test report"

---

## 🔍 NẾU VẪN KHÔNG THẤY BÁO CÁO

### Test A: Kiểm tra API trực tiếp

Mở file: `D:\app\HealthTips-App-\test-api.html` trong trình duyệt

1. Click nút: **"🚀 Test API (No Auth)"**
2. Xem response

**Kết quả mong đợi:**
- ✅ Status 400: "Missing required fields" → API hoạt động
- ✅ Status 201: Success → API hoạt động và ghi được vào Firebase

---

### Test B: Kiểm tra Firebase Database trực tiếp

1. Vào: `https://console.firebase.google.com/`
2. Chọn project: **healthtips** (hoặc tên project của bạn)
3. Vào: **Realtime Database**
4. Kiểm tra node: `/admin_notifications`

**Nên thấy:** Các notification mới với timestamp gần đây

---

### Test C: Check APK đã install đúng chưa

```bash
# Lấy thông tin version
adb shell dumpsys package com.vhn.doan | grep versionName

# Decompile và check URL (nếu cần)
apktool d "D:\app\HealthTips-App-\app\build\intermediates\apk\debug\app-debug.apk"
grep -r "healthtips-admin.vercel.app" app-debug/
```

**Phải thấy:** URL `healthtips-admin.vercel.app`, KHÔNG phải URL deployment cũ

---

## 🎯 CHECKLIST NHANH

Trước khi test, hãy đảm bảo:

- [ ] Đã uninstall app cũ bằng `adb uninstall com.vhn.doan`
- [ ] Đã install APK MỚI từ `app\build\intermediates\apk\debug\app-debug.apk`
- [ ] Đã đăng nhập vào app (có tài khoản Firebase Auth)
- [ ] Đã mở `adb logcat` để xem log
- [ ] App hiển thị form "Report Issue" đúng cách
- [ ] Internet connection ổn định

---

## 📊 DIAGNOSTIC INFORMATION

### APK Path:
```
D:\app\HealthTips-App-\app\build\intermediates\apk\debug\app-debug.apk
```

### API Endpoint:
```
POST https://healthtips-admin.vercel.app/api/admin-notifications/user-report
```

### Required Headers:
```
Authorization: Bearer [Firebase-ID-Token]
Content-Type: application/json
```

### Required Fields:
```json
{
  "userId": "string",
  "reportType": "string",
  "reason": "string"
}
```

### Firebase Path:
```
/admin_notifications
  /{notificationId}
    type: "USER_REPORT"
    title: "..."
    message: "..."
    data: {...}
    createdAt: timestamp
    read: false
```

---

## 🆘 NẾU GẶP LỖI

### Lỗi: "User not authenticated"
**Giải pháp:** Đăng nhập vào app trước khi gửi báo cáo

### Lỗi: "Failed to get authentication token"
**Giải pháp:**
- Kiểm tra Firebase Auth đang hoạt động
- Logout và login lại

### Lỗi: "Failed to send admin notification" (Network error)
**Giải pháp:**
- Kiểm tra internet connection
- Kiểm tra URL trong AdminNotificationSender.java
- Test API bằng `test-api.html`

### Lỗi: "Server error: 500"
**Giải pháp:**
- Kiểm tra Vercel logs: `vercel logs healthtips-admin.vercel.app`
- Kiểm tra Firebase Admin credentials
- Kiểm tra Vercel environment variables

---

## 📞 THÔNG TIN CẦN CUNG CẤP NẾU VẪN LỖI

1. **Screenshot adb logcat** khi gửi báo cáo
2. **Screenshot Firebase Database** (node /admin_notifications)
3. **Output của:**
   ```bash
   adb shell dumpsys package com.vhn.doan | grep versionName
   ```
4. **Screenshot web admin** sau khi refresh
5. **Screenshot test-api.html** sau khi test

---

**Ngày tạo:** 2025-11-29
**APK version:** Latest with correct URL
**Web deployment:** healthtips-admin-50ho1qyml (Production)
