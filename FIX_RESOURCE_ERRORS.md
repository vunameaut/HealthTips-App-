# Hướng Dẫn Khắc Phục Lỗi Resource Not Found

## Vấn đề

Sau khi tạo các resource mới (drawables, colors, strings), Android Studio có thể chưa sync và báo lỗi:
```
error: resource drawable/bg_input_field not found
error: resource color/text_hint not found
error: resource color/primary_green not found
```

## Giải pháp

### Cách 1: Sync Project (Nhanh nhất)

1. Trong Android Studio, chọn **File** > **Sync Project with Gradle Files**
2. Hoặc nhấn tổ hợp phím: **Ctrl + Shift + O** (Windows/Linux) hoặc **Cmd + Shift + O** (Mac)

### Cách 2: Clean và Rebuild

1. Chọn **Build** > **Clean Project**
2. Đợi quá trình clean hoàn tất
3. Chọn **Build** > **Rebuild Project**

### Cách 3: Invalidate Caches (Nếu vẫn lỗi)

1. Chọn **File** > **Invalidate Caches / Restart**
2. Chọn **Invalidate and Restart**
3. Đợi Android Studio khởi động lại
4. Sau khi khởi động lại, chọn **Build** > **Clean Project**
5. Sau đó chọn **Build** > **Rebuild Project**

### Cách 4: Sử dụng Script (Command Line)

Chạy file `build_and_clean.bat` trong thư mục gốc của project:

```bash
cd d:\app\HealthTips-App-
build_and_clean.bat
```

## Danh Sách Resources Đã Tạo

### Drawables:
- ✅ `bg_input_field.xml` - Background cho input fields
- ✅ `bg_gradient_button.xml` - Background gradient cho buttons  
- ✅ `ic_privacy.xml` - Icon shield cho Privacy Policy
- ✅ `ic_community.xml` - Icon nhóm người cho Community Guidelines
- ✅ `ic_copyright.xml` - Icon © cho Copyright Policy

### Colors (Light Mode - colors.xml):
- ✅ `text_hint` = #9E9E9E
- ✅ `primary_green` = #4CAF50
- ✅ `primary_green_light` = #81C784
- ✅ `primary_green_dark` = #2E7D32

### Colors (Dark Mode - colors-night.xml):
- ✅ `text_hint` = #757575
- ✅ `primary_green` = #66BB6A
- ✅ `primary_green_light` = #81C784
- ✅ `primary_green_dark` = #4CAF50

### Strings:
- ✅ `subject` = Tiêu đề
- ✅ `enter_subject` = Nhập tiêu đề vấn đề
- ✅ `enter_description` = Mô tả chi tiết vấn đề bạn gặp phải...
- ✅ `device_info` = Thông tin thiết bị
- ✅ `icon` = Biểu tượng
- ✅ `navigate` = Điều hướng

## Kiểm Tra Resources

Sau khi sync, bạn có thể kiểm tra xem resources đã được nhận diện chưa bằng cách:

1. Mở file Java/XML có sử dụng resource
2. Nhấn **Ctrl + Click** (Windows/Linux) hoặc **Cmd + Click** (Mac) vào resource (ví dụ: `R.drawable.bg_input_field`)
3. Nếu resource được nhận diện, Android Studio sẽ nhảy đến file định nghĩa resource đó

## Lưu Ý

- Các lỗi "Cannot resolve symbol" trong Android Studio **KHÔNG ẢNH HƯỞNG** đến việc build nếu resources đã được tạo đúng
- Chỉ cần **sync project** là đủ để Android Studio nhận diện resources mới
- Nếu build thành công nhưng IDE vẫn báo lỗi, hãy restart Android Studio

## Liên Hệ

Nếu vẫn gặp vấn đề, vui lòng kiểm tra:
1. File resources có tồn tại trong thư mục đúng không
2. Cú pháp XML có đúng không
3. Tên resource có trùng với resource khác không

