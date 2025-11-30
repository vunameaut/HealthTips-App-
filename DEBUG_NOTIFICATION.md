# Debug Notification Issues

## Vấn đề hiện tại
- Notification từ hệ thống không mở được chat
- Notification từ lịch sử app không mở được chat
- Admin chưa nhận được thông báo khi user nhắn tin

## Cách test và debug

### 1. Cài đặt APK mới
```bash
adb install -r HealthTips-DEBUG.apk
```

### 2. Xem Android logs để debug
```bash
# Mở terminal và chạy lệnh này để xem logs
adb logcat | findstr "TicketChat|DeepLink|FirebaseMessaging"
```

### 3. Test notification từ hệ thống

**Bước 1:** Mở app, đảm bảo đã đăng nhập

**Bước 2:** Gửi test notification từ admin (hoặc đợi admin reply)

**Bước 3:** Khi nhận được notification:
- Không mở app
- Tap vào notification từ notification tray
- Xem logs để thấy:
  ```
  DeepLinkHandlerActivity: Notification type: SUPPORT_REPLY
  DeepLinkHandlerActivity: Opening support chat for ticket: [ticketId]
  TicketChatActivity: Received ticket ID: [ticketId]
  ```

**Bước 4:** Nếu không thấy logs hoặc app không mở:
- Check logs xem có error gì
- Check xem notification data có đúng format không

### 4. Test notification từ lịch sử app

**Bước 1:** Mở app → Vào "Lịch sử thông báo"

**Bước 2:** Tap vào notification "Admin đã trả lời..."

**Bước 3:** Xem logs:
  ```
  NotificationHistoryActivity: Navigate to content - Type: SUPPORT_REPLY, TargetId: [ticketId]
  TicketChatActivity: Received ticket ID: [ticketId]
  ```

### 5. Test user gửi tin nhắn → admin nhận thông báo

**Bước 1:** Mở support ticket chat, gửi tin nhắn

**Bước 2:** Check logs để xem API call:
  ```
  TicketChatActivity: Admin notification sent successfully: {"success":true,...}
  ```

  HOẶC nếu lỗi:
  ```
  TicketChatActivity: Admin notification failed with code 500: {...}
  ```

**Bước 3:** Check web admin:
- Mở https://healthtips-admin-fxbnt4896-projects.vercel.app
- Xem notification bell (🔔) ở góc phải trên
- Nếu có notification, sẽ thấy badge đỏ với số lượng
- Click vào bell → xem danh sách notifications
- Tìm notification loại "USER_FEEDBACK"

## Những gì đã implement

### Mobile App (Android)
✅ MyFirebaseMessagingService - xử lý FCM notifications
✅ DeepLinkHandlerActivity - routing notifications đến đúng Activity
✅ TicketChatActivity - chat interface với admin
✅ NotificationHistoryActivity - xử lý navigation từ lịch sử
✅ NotificationType enum - thêm SUPPORT_REPLY type
✅ API call để notify admin - gọi web admin API khi user gửi tin nhắn

### Web Admin
✅ Admin notifications page - hiển thị thông báo real-time
✅ USER_FEEDBACK notification type handler
✅ Notification bell với badge count
✅ API endpoint: /api/support/send-message-notification
✅ Firebase listener tự động update notifications

## Kiểm tra nếu vẫn không hoạt động

### Notification không mở được

1. **Check notification type trong logs:**
   ```
   MyFirebaseMessagingService: Message data payload: {type=SUPPORT_REPLY, ticketId=xxx, ...}
   ```

2. **Check DeepLinkHandlerActivity có nhận được notification type không:**
   ```
   DeepLinkHandlerActivity: Notification type: SUPPORT_REPLY
   ```

3. **Check ticketId có được truyền đúng không:**
   ```
   DeepLinkHandlerActivity: Opening support chat for ticket: xxx
   ```

4. **Check TicketChatActivity có nhận được ticketId không:**
   ```
   TicketChatActivity: Received ticket ID: xxx
   ```

### Admin không nhận notification

1. **Check API response trong Android logs:**
   ```
   TicketChatActivity: Admin notification sent successfully: {...}
   ```

   Nếu thấy error code khác 200:
   ```
   TicketChatActivity: Admin notification failed with code XXX: {...}
   ```

2. **Check Firebase Database:**
   - Mở Firebase Console
   - Vào Realtime Database
   - Check path: `admin_notifications`
   - Xem có notifications mới với type `USER_FEEDBACK` không

3. **Check web admin console:**
   - Mở browser DevTools (F12)
   - Vào Console tab
   - Xem có errors không

## Thông tin kỹ thuật

### Notification Data Format (FCM)
```json
{
  "type": "SUPPORT_REPLY",
  "ticketId": "xxx",
  "title": "Admin đã trả lời yêu cầu hỗ trợ",
  "body": "Message content..."
}
```

### Deep Link Format
```
healthtips://support/{ticketId}
```

### Firebase Paths
- Messages: `support_tickets/{ticketId}/messages`
- Admin notifications: `admin_notifications/{notificationId}`
- User notifications: `user_notifications/{userId}/{notificationId}`

## Liên hệ
Nếu vấn đề vẫn chưa được giải quyết, hãy cung cấp:
1. Android logs (từ adb logcat)
2. Screenshot của notification
3. Screenshot của lỗi (nếu có)
