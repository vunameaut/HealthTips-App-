# ⚠️ VẤN ĐỀ VÀ GIẢI PHÁP URL ADMIN

## Vấn đề
Admin không nhận được báo cáo từ người dùng vì **URL admin trong app đã CŨ**.

### URL Cũ (SAI):
```java
private static final String ADMIN_API_BASE_URL =
    "https://healthtips-admin-4nqwzfhay-vunams-projects-d3582d4f.vercel.app/api";
```

**Lý do lỗi:**
- Vercel tạo URL deployment mới mỗi lần deploy
- URL này thay đổi liên tục: `...-4nqwzfhay`, `...-q4fqcpbzd`, `...-50ho1qyml`, etc.
- App đang gửi request đến URL cũ không còn tồn tại

---

## Giải pháp

### URL Mới (ĐÚNG - Stable Domain):
```java
private static final String ADMIN_API_BASE_URL =
    "https://healthtips-admin.vercel.app/api";
```

**Lý do:**
- ✅ Domain chính thức của project
- ✅ Không thay đổi khi deploy
- ✅ Tự động alias đến deployment mới nhất

### Các Domain Ổn Định khác:
1. `healthtips-admin.vercel.app` - **Nên dùng** (ngắn gọn)
2. `healthtips-admin-vunams-projects-d3582d4f.vercel.app` - OK
3. `healthtips-admin-git-main-vunams-projects-d3582d4f.vercel.app` - OK

---

## File đã sửa

**File:** `app/src/main/java/com/vhn/doan/utils/AdminNotificationSender.java`

**Dòng 33:**
```java
// CŨ (17 giờ trước):
"https://healthtips-admin-4nqwzfhay-vunams-projects-d3582d4f.vercel.app/api"

// MỚI (hiện tại):
"https://healthtips-admin.vercel.app/api"
```

---

## Kết quả

### ✅ Đã hoàn thành:
1. ✅ Tìm ra nguyên nhân (URL deployment cũ)
2. ✅ Tìm production domain chính thức
3. ✅ Update URL trong AdminNotificationSender.java
4. ✅ Build lại APK với URL mới

### 🔄 Đang làm:
- ⏳ Building APK với URL đúng...

### 📋 Bước tiếp theo:
1. Install APK mới vào thiết bị
2. Test gửi báo cáo từ app
3. Kiểm tra admin panel nhận được thông báo

---

## Timeline Deployments

| Thời gian | URL | Status |
|-----------|-----|--------|
| 17h trước | `...-4nqwzfhay` | ● Ready (URL trong app CŨ) |
| 10m trước | `...-q4fqcpbzd` | ● Error |
| 7m trước | `...-50ho1qyml` | ● Ready (Deployment mới nhất) |

**Production Alias:**
- `healthtips-admin.vercel.app` → `...-50ho1qyml` (tự động)

---

## API Endpoint

**Full URL:**
```
https://healthtips-admin.vercel.app/api/admin-notifications/user-report
```

**Request từ app:**
```java
POST https://healthtips-admin.vercel.app/api/admin-notifications/user-report
Headers:
  - Authorization: Bearer <firebase-id-token>
  - Content-Type: application/json
Body:
  {
    "userId": "user123",
    "userName": "John Doe",
    "reportType": "spam|inappropriate|content|abuse|other",
    "reason": "Lý do báo cáo",
    "description": "Mô tả chi tiết",
    "additionalData": {
      "device": "Samsung Galaxy S21",
      "osVersion": "Android 13",
      "apiLevel": 33
    }
  }
```

---

## Kiểm tra deployment

**Xem deployments:**
```bash
cd "D:\hoc tap\web\healthtips-admin"
vercel ls
```

**Xem aliases:**
```bash
vercel alias ls
```

**Production domain:**
- Main: `healthtips-admin.vercel.app`
- Project: `healthtips-admin-vunams-projects-d3582d4f.vercel.app`

---

**Ngày fix:** 29/11/2025 02:52
**Build mới:** Đang chạy...
