# 🐛 HƯỚNG DẪN DEBUG KHÔNG NHẬN ĐƯỢC BÁO CÁO

## Vấn đề
Web admin không nhận được báo cáo từ app sau khi gửi.

---

## ✅ CHECKLIST DEBUG

### 1. Kiểm tra APK đã install đúng phiên bản

**❓ Bạn đã install lại APK mới chưa?**

APK cũ vẫn có URL cũ `...4nqwzfhay...`, chỉ APK mới có URL đúng `healthtips-admin.vercel.app`

**Cách kiểm tra:**
```bash
# 1. Uninstall app cũ
adb uninstall com.vhn.doan

# 2. Install APK mới
adb install "D:\app\HealthTips-App-\app\build\intermediates\apk\debug\app-debug.apk"

# 3. Verify installed
adb shell pm list packages | grep com.vhn.doan
```

---

### 2. Test API endpoint trực tiếp

**Mở file test:**
```
D:\app\HealthTips-App-\test-api.html
```

**Click nút:** "🚀 Test API (No Auth)"

**Kết quả mong đợi:**
- ❌ Error 400: "Missing required fields" → API hoạt động nhưng cần auth
- ✅ Success hoặc Error 401/403 → API sống

---

### 3. Kiểm tra log app khi gửi báo cáo

```bash
# Xem log realtime:
adb logcat | grep -i "AdminNotificationSender\|ReportIssue"

# Hoặc filter log:
adb logcat *:E | grep -i "report"
```

**Tìm các dòng log:**
- `sendUserReport called - reportType: ...` → Request được gửi
- `Got Firebase ID token successfully` → Auth OK
- `Sending request to: https://healthtips-admin.vercel.app/api/...` → URL đúng
- `Admin notification sent successfully` → Thành công ✅
- `Failed to send admin notification` → Lỗi ❌

---

### 4. Test thủ công bằng cURL

```bash
curl -X POST https://healthtips-admin.vercel.app/api/admin-notifications/user-report \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test123",
    "userName": "Test User",
    "reportType": "spam",
    "reason": "Test reason",
    "description": "Test description"
  }'
```

**Kết quả mong đợi:**
```json
{
  "success": true,
  "notificationId": "-Nxxx...",
  "message": "Báo cáo đã được gửi đến admin"
}
```

---

### 5. Kiểm tra Firebase Database trực tiếp

**Vào Firebase Console:**
```
https://console.firebase.google.com/
```

**Chọn project** → **Realtime Database**

**Kiểm tra nodes:**
1. `/admin_notifications` → Có notification mới không?
2. `/issues` → Có issue mới không?

---

## 🔧 CÁC LỖI THƯỜNG GẶP

### Lỗi 1: App chưa install lại
**Triệu chứng:** Gửi báo cáo nhưng không thấy gì trên web admin

**Nguyên nhân:** Vẫn dùng APK cũ với URL cũ

**Giải pháp:**
```bash
adb uninstall com.vhn.doan
adb install "D:\app\HealthTips-App-\app\build\intermediates\apk\debug\app-debug.apk"
```

---

### Lỗi 2: User chưa đăng nhập
**Triệu chứng:** Toast "Vui lòng đăng nhập"

**Nguyên nhân:** Firebase Auth chưa login

**Giải pháp:**
- Đăng nhập vào app trước khi gửi báo cáo

---

### Lỗi 3: Firebase Admin credentials sai
**Triệu chứng:** API trả về 500 Internal Server Error

**Kiểm tra:**
```bash
cd "D:\hoc tap\web\healthtips-admin"
vercel env ls
```

Phải có 4 biến:
- FIREBASE_ADMIN_PROJECT_ID
- FIREBASE_ADMIN_CLIENT_EMAIL
- FIREBASE_ADMIN_PRIVATE_KEY
- FIREBASE_ADMIN_DATABASE_URL

---

### Lỗi 4: Web admin xem sai node
**Triệu chứng:** Firebase có data nhưng web không hiển thị

**Kiểm tra code web admin:**
```typescript
// File: src/pages/admin-notifications/index.tsx
const notificationsRef = ref(database, 'admin_notifications'); // ✅ Đúng
```

---

### Lỗi 5: CORS issue
**Triệu chứng:** Console log: "CORS policy blocked"

**Giải pháp:** API Next.js không cần CORS config cho API routes

---

## 📋 DEBUG FLOW HOÀN CHỈNH

```
1. Install APK mới
   ↓
2. Đăng nhập vào app
   ↓
3. Mở adb logcat (terminal riêng)
   ↓
4. Vào Settings → Support → Report Issue
   ↓
5. Điền form và Submit
   ↓
6. Xem log trong adb logcat:
   - "sendUserReport called"? → App đã gọi API ✅
   - "Got Firebase ID token"? → Auth OK ✅
   - "Sending request to: https://healthtips-admin.vercel.app"? → URL đúng ✅
   - "sent successfully"? → Thành công ✅
   ↓
7. Refresh web admin:
   https://healthtips-admin.vercel.app/admin-notifications
   ↓
8. Check tab "Chưa đọc" hoặc filter "Báo cáo từ User"
```

---

## 🧪 TEST SCRIPTS

### Test 1: Verify APK URL
```bash
# Decompile APK và check URL
apktool d app-debug.apk
grep -r "healthtips-admin" app-debug/
```

### Test 2: Live log monitoring
```bash
# Terminal 1: Monitor app logs
adb logcat | grep -E "AdminNotification|ReportIssue|error"

# Terminal 2: Use app to send report
# (Manually test on device)
```

### Test 3: Network inspection
```bash
# Proxy tool (Charles/Fiddler) to see actual HTTP request
# hoặc adb tcpdump
```

---

## ❓ KIỂM TRA NHANH

**Trả lời các câu hỏi sau:**

- [ ] Đã uninstall app cũ?
- [ ] Đã install APK MỚI từ `app\build\intermediates\apk\debug\app-debug.apk`?
- [ ] Đã đăng nhập vào app?
- [ ] Có chạy `adb logcat` xem log không?
- [ ] Test API bằng `test-api.html` thành công?
- [ ] Firebase Database có node `/admin_notifications` không?
- [ ] Web admin đã refresh sau khi gửi báo cáo?

---

## 🆘 NẾU VẪN KHÔNG ĐƯỢC

**Gửi cho tôi:**
1. Screenshot log từ `adb logcat`
2. Screenshot Firebase Database
3. Screenshot network tab trong browser DevTools (F12)
4. Output của: `adb shell dumpsys package com.vhn.doan | grep version`

---

**Ngày tạo:** 29/11/2025
**Phiên bản APK:** v2 (với URL mới)
