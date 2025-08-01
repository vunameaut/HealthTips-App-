# Sửa Lỗi Thông Báo Nhắc Nhở

## Vấn Đề
App chỉ hiển thị thông báo nhắc nhở khi mở lại app, không hiển thị thông báo real-time.

## Nguyên Nhân
1. `ReminderForegroundService` không được khởi động tự động khi app mở
2. Không có cơ chế khởi động lại reminders khi app mở
3. Không kiểm tra reminders đã bị miss

## Giải Pháp Đã Áp Dụng

### 1. Cập Nhật HomeActivity
- Thêm khởi động `ReminderForegroundService` trong `onCreate()`
- Thêm khởi động lại service trong `onResume()`
- Thêm khởi động lại tất cả reminders đang active
- Thêm kiểm tra reminders đã bị miss

### 2. Cải Thiện ReminderForegroundService
- Thêm wake lock để đảm bảo service không bị kill
- Cải thiện error handling
- Đảm bảo service chạy liên tục

### 3. Cải Thiện ReminderBroadcastReceiver
- Hiển thị thông báo ngay lập tức khi nhận broadcast
- Thêm fallback để đảm bảo thông báo luôn hiển thị

### 4. Tạo ReminderManager
- Quản lý việc khởi động lại tất cả reminders
- Kiểm tra và hiển thị reminders đã bị miss
- Đảm bảo reminders hoạt động khi app mở

### 5. Tạo NotificationDebugHelper
- Debug và kiểm tra trạng thái thông báo
- Test thông báo ngay lập tức
- Kiểm tra các quyền cần thiết

## Các File Đã Thay Đổi

### Chính:
- `app/src/main/java/com/vhn/doan/presentation/home/HomeActivity.java`
- `app/src/main/java/com/vhn/doan/services/ReminderForegroundService.java`
- `app/src/main/java/com/vhn/doan/receivers/ReminderBroadcastReceiver.java`
- `app/src/main/java/com/vhn/doan/services/NotificationService.java`

### Mới:
- `app/src/main/java/com/vhn/doan/utils/ReminderManager.java`
- `app/src/main/java/com/vhn/doan/utils/NotificationDebugHelper.java`

## Cách Hoạt Động

1. **Khi app mở:**
   - Khởi động `ReminderForegroundService`
   - Khởi động lại tất cả reminders đang active
   - Kiểm tra và hiển thị reminders đã bị miss
   - Debug trạng thái thông báo

2. **Khi reminder trigger:**
   - `ReminderBroadcastReceiver` nhận broadcast
   - Hiển thị thông báo ngay lập tức
   - Sử dụng `ReminderForegroundService` để đảm bảo thông báo hiển thị
   - Cập nhật trạng thái reminder

3. **Khi app resume:**
   - Đảm bảo service vẫn hoạt động
   - Kiểm tra lại trạng thái

## Kiểm Tra

Để kiểm tra xem thông báo có hoạt động không:

1. Mở app và tạo một reminder
2. Đóng app hoàn toàn
3. Đợi đến thời gian reminder
4. Thông báo sẽ hiển thị ngay cả khi app đang đóng

## Debug

Sử dụng `NotificationDebugHelper.checkNotificationStatus(context)` để kiểm tra:
- Quyền thông báo
- Trạng thái thông báo
- Quyền exact alarm
- Battery optimization
- Notification channels

## Lưu Ý

- Cần cấp quyền thông báo và exact alarm
- Có thể cần tắt battery optimization cho app
- Service sẽ chạy liên tục trong background
- Thông báo sẽ hiển thị ngay cả khi app đang đóng