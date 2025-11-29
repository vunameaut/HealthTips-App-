# Debug Instructions - Report Issue từ Video/Article

## 🔧 Thay đổi đã thực hiện

### 1. Đã thêm Authentication token
- ✅ Thêm Firebase ID token vào tất cả requests
- ✅ Header: `Authorization: Bearer {firebase_id_token}`

### 2. Đã thêm Debug Logging
- ✅ Log khi `reportContent()` được gọi
- ✅ Log khi lấy Firebase ID token
- ✅ Log request URL và body
- ✅ Log response code và error details

## 📱 Cách test

### Bước 1: Cài APK mới
```bash
# APK file location
D:\app\HealthTips-App-\app\build\outputs\apk\debug\app-debug.apk

# Cài vào device/emulator
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Bước 2: Mở Logcat để xem logs
```bash
# Xem tất cả logs từ AdminNotificationSender
adb logcat | findstr "AdminNotificationSender"

# Hoặc filter theo tag
adb logcat AdminNotificationSender:D *:S
```

### Bước 3: Test Report từ Video
1. Mở app → Vào tab Videos
2. Chọn 1 video → Click nút Report (3 chấm)
3. Chọn lý do báo cáo
4. **Quan sát Logcat** để xem các log sau:
   ```
   D/AdminNotificationSender: reportContent called - contentId: xxx, contentType: video, reportType: spam
   D/AdminNotificationSender: sendUserReport called - reportType: spam
   D/AdminNotificationSender: Getting Firebase ID token for user: xxx
   D/AdminNotificationSender: Got Firebase ID token successfully
   D/AdminNotificationSender: Sending request to: https://...
   D/AdminNotificationSender: Request body: {...}
   D/AdminNotificationSender: Admin notification sent successfully
   ```

### Bước 4: Test Report từ Article
1. Mở app → Vào Home
2. Click vào 1 bài viết để xem chi tiết
3. Click nút menu (3 chấm) → Chọn "Report"
4. Chọn lý do báo cáo
5. **Quan sát Logcat** tương tự bước 3

## 🐛 Các lỗi có thể gặp

### Lỗi 1: "User not authenticated"
**Log:**
```
E/AdminNotificationSender: User not authenticated
```
**Nguyên nhân:** User chưa đăng nhập
**Giải pháp:** Đăng nhập lại vào app

### Lỗi 2: "Failed to get ID token"
**Log:**
```
E/AdminNotificationSender: Failed to get ID token
```
**Nguyên nhân:**
- Không có internet
- Firebase Auth session hết hạn
**Giải pháp:**
- Check internet connection
- Logout và login lại

### Lỗi 3: "Admin notification failed: 401"
**Log:**
```
E/AdminNotificationSender: Admin notification failed: 401 - ...
```
**Nguyên nhân:**
- Token không hợp lệ
- Server không nhận dạng token
**Giải pháp:**
- Kiểm tra server có validate Firebase token đúng không
- Check server logs để xem lỗi cụ thể

### Lỗi 4: "Admin notification failed: 500"
**Log:**
```
E/AdminNotificationSender: Admin notification failed: 500 - ...
```
**Nguyên nhân:** Lỗi server
**Giải pháo:** Check server logs

### Lỗi 5: Network error
**Log:**
```
E/AdminNotificationSender: Failed to send admin notification: java.io.IOException...
```
**Nguyên nhân:**
- Không có internet
- Server không trả lời
**Giải pháp:**
- Check internet connection
- Ping server URL để test

## ✅ Kết quả mong đợi

Khi report thành công, bạn sẽ thấy:

**Logcat:**
```
D/AdminNotificationSender: reportContent called - contentId: abc123, contentType: video, reportType: spam
D/AdminNotificationSender: sendUserReport called - reportType: spam
D/AdminNotificationSender: Getting Firebase ID token for user: xyz456
D/AdminNotificationSender: Got Firebase ID token successfully
D/AdminNotificationSender: Sending request to: https://healthtips-admin-4nqwzfhay-vunams-projects-d3582d4f.vercel.app/api/admin-notifications/user-report
D/AdminNotificationSender: Request body: {"userId":"xyz456","userName":"User Name","reportType":"spam",...}
D/AdminNotificationSender: Admin notification sent successfully
```

**App UI:**
- Toast message: "Đã gửi báo cáo"
- Loading indicator biến mất

**Web Admin:**
- Notification mới xuất hiện với badge count tăng lên
- Chi tiết báo cáo hiển thị đầy đủ

## 📋 Checklist

Test tất cả các trường hợp sau:

- [ ] Report video từ VideoFragment
- [ ] Report video từ SingleVideoPlayerFragment
- [ ] Report video từ LikedVideosPlayerFragment
- [ ] Report article từ HealthTipDetailActivity
- [ ] Report từ ReportIssueActivity (đã test - OK ✅)

## 🔍 Next Steps

Nếu vẫn gặp lỗi:

1. **Copy full Logcat output** và gửi cho tôi
2. Screenshot lỗi trên app
3. Cho biết bạn đang test:
   - Report từ video hay article?
   - Device/Emulator gì?
   - Android version?
   - User có đăng nhập không?

---

**Updated:** 2025-11-28
**Build:** app-debug.apk (with debug logging)
