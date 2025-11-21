# 🔥 Hướng Dẫn Sử Dụng Firebase Crashlytics

## ✅ Đã Hoàn Thành

Firebase Crashlytics đã được tích hợp thành công vào HealthTips App!

## 📋 Các Thay Đổi Đã Thực Hiện

### 1. Cấu Hình Build Files

**File: `gradle/libs.versions.toml`**
- Thêm phiên bản Crashlytics: `firebaseCrashlytics = "3.0.2"`
- Thêm plugin: `google-firebase-crashlytics`

**File: `build.gradle.kts` (Project level)**
```kotlin
plugins {
    alias(libs.plugins.google.firebase.crashlytics) apply false
}
```

**File: `app/build.gradle.kts`**
```kotlin
plugins {
    alias(libs.plugins.google.firebase.crashlytics)
}

dependencies {
    implementation("com.google.firebase:firebase-crashlytics")
}
```

### 2. Activity Test Crashlytics

**File:** `app/src/main/java/com/vhn/doan/presentation/debug/CrashlyticsTestActivity.java`

Activity này cung cấp 4 chức năng test:

1. **Test Fatal Crash** - Gây crash thực sự để kiểm tra Crashlytics
2. **Test Non-Fatal Error** - Ghi lại lỗi không làm crash app
3. **Test Custom Log** - Ghi các log tùy chỉnh
4. **Test User Info** - Thiết lập thông tin người dùng

### 3. Giao Diện (UI)

**File:** `app/src/main/res/layout/activity_crashlytics_test.xml`
- Giao diện Material Design 3 hiện đại
- 4 card tương ứng với 4 chức năng test
- Hướng dẫn sử dụng rõ ràng

### 4. Tích Hợp vào Settings

**File:** `app/src/main/java/com/vhn/doan/presentation/settings/SettingsAndPrivacyActivity.java`
- Thêm button "Test Crashlytics" trong phần Debug & Testing
- Dễ dàng truy cập từ Settings → Debug & Testing → Test Crashlytics

## 🚀 Cách Sử Dụng

### Bước 1: Mở App
1. Chạy app trên thiết bị hoặc emulator
2. Đăng nhập vào tài khoản
3. Vào **Profile** → **Settings and Privacy**

### Bước 2: Truy Cập Test Crashlytics
1. Cuộn xuống phần **🧪 Debug & Testing**
2. Nhấn vào **Test Crashlytics**

### Bước 3: Test Các Chức Năng

#### 1️⃣ Test Fatal Crash
- Nhấn nút "⚠️ Gây Crash"
- App sẽ crash sau 2 giây
- Khởi động lại app để gửi crash report lên Firebase

#### 2️⃣ Test Non-Fatal Error
- Nhấn nút "📝 Ghi Non-Fatal Error"
- Lỗi được ghi nhưng app không crash
- Xem toast thông báo thành công

#### 3️⃣ Test Custom Log
- Nhấn nút "📊 Ghi Custom Log"
- Các custom keys được ghi: button_clicked, timestamp, screen_name
- Thông tin này sẽ xuất hiện cùng crash reports

#### 4️⃣ Test User Info
- Nhấn nút "👤 Thiết lập User Info"
- Thông tin user được thiết lập: user_type, app_version, device_info
- Giúp nhận biết ai gặp lỗi khi có crash

### Bước 4: Xem Báo Cáo trên Firebase Console

1. Mở [Firebase Console](https://console.firebase.google.com/)
2. Chọn project HealthTips App
3. Vào **Release & Monitor** → **Crashlytics**
4. Xem các crash reports và non-fatal errors

**Lưu ý:**
- Crash reports có thể mất **vài phút** để xuất hiện
- Với Debug build, crash được gửi **ngay lập tức**
- Cần **khởi động lại app** sau khi crash để gửi báo cáo

## 📊 Thông Tin Crashlytics Thu Thập

### Tự Động Thu Thập
- Stack trace của crash
- Device model và OS version
- App version và build number
- Thời gian crash xảy ra
- Trạng thái memory và battery

### Custom Data (Từ Code)
```java
FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

// Thiết lập User ID
crashlytics.setUserId("user_123");

// Ghi custom keys
crashlytics.setCustomKey("screen_name", "HomeActivity");
crashlytics.setCustomKey("user_type", "premium");

// Ghi custom log
crashlytics.log("User clicked button X");

// Ghi non-fatal exception
try {
    // Code có thể lỗi
} catch (Exception e) {
    crashlytics.recordException(e);
}
```

## 🎯 Sử Dụng Crashlytics Trong Code

### 1. Trong Application Class
```java
// Bật Crashlytics collection
FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
```

### 2. Trong Activity/Fragment
```java
// Thiết lập user identifier khi login
FirebaseAuth.getInstance().getCurrentUser().addOnCompleteListener(task -> {
    if (task.isSuccessful() && task.getResult() != null) {
        String userId = task.getResult().getUid();
        FirebaseCrashlytics.getInstance().setUserId(userId);
    }
});
```

### 3. Trong Repository/Data Layer
```java
// Ghi lại lỗi network
public void fetchData() {
    apiService.getData()
        .addOnFailureListener(e -> {
            FirebaseCrashlytics.getInstance().log("Failed to fetch data from API");
            FirebaseCrashlytics.getInstance().setCustomKey("api_endpoint", "/data");
            FirebaseCrashlytics.getInstance().recordException(e);
        });
}
```

### 4. Trong try-catch Blocks
```java
try {
    // Thao tác có thể lỗi
    riskyOperation();
} catch (Exception e) {
    // Ghi lại lỗi nhưng không crash app
    FirebaseCrashlytics.getInstance().recordException(e);
    // Xử lý lỗi
    showErrorMessage();
}
```

## 🔐 Bảo Mật và Privacy

### Tắt Crashlytics Cho User Cụ Thể
```java
// Cho phép user opt-out
FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false);
```

### Không Ghi Thông Tin Nhạy Cảm
```java
// ❌ KHÔNG NÊN
crashlytics.setCustomKey("password", userPassword);
crashlytics.setCustomKey("credit_card", cardNumber);

// ✅ NÊN
crashlytics.setCustomKey("user_type", "premium");
crashlytics.setCustomKey("subscription_status", "active");
```

## 📈 Best Practices

1. **Thiết lập User ID ngay sau khi login**
   ```java
   crashlytics.setUserId(firebaseUser.getUid());
   ```

2. **Ghi custom keys cho ngữ cảnh quan trọng**
   ```java
   crashlytics.setCustomKey("current_screen", "CheckoutActivity");
   crashlytics.setCustomKey("payment_method", "credit_card");
   ```

3. **Sử dụng log để hiểu flow trước khi crash**
   ```java
   crashlytics.log("Step 1: User entered checkout");
   crashlytics.log("Step 2: Validating payment info");
   crashlytics.log("Step 3: Processing payment");
   ```

4. **Ghi lại non-fatal errors quan trọng**
   ```java
   // Lỗi không crash app nhưng cần theo dõi
   if (!isDataValid) {
       crashlytics.recordException(new InvalidDataException("Data validation failed"));
   }
   ```

5. **Clean up sensitive data trong ProGuard**
   - File `proguard-rules.pro` đã được cấu hình để keep Crashlytics classes
   - Đảm bảo không ghi thông tin nhạy cảm vào logs

## 🐛 Troubleshooting

### Crash Reports Không Xuất Hiện?
1. Kiểm tra internet connection
2. Đảm bảo đã khởi động lại app sau khi crash
3. Kiểm tra Firebase Console có nhận project không
4. Với release build, có thể mất đến 24h để reports xuất hiện

### Testing Trên Emulator
```bash
# Xóa crash reports cache
adb shell run-as com.vhn.doan rm -rf /data/data/com.vhn.doan/files/.com.google.firebase.crashlytics

# Restart app
adb shell am force-stop com.vhn.doan
adb shell am start -n com.vhn.doan/.presentation.auth.LoginActivity
```

### Force Send Reports
```java
// Trong debug mode
FirebaseCrashlytics.getInstance().sendUnsentReports();
```

## 📚 Tài Liệu Tham Khảo

- [Firebase Crashlytics Documentation](https://firebase.google.com/docs/crashlytics)
- [Get Started with Crashlytics](https://firebase.google.com/docs/crashlytics/get-started?platform=android)
- [Customize Crash Reports](https://firebase.google.com/docs/crashlytics/customize-crash-reports)

## ✨ Kết Luận

Firebase Crashlytics đã sẵn sàng sử dụng! Bạn có thể:
- ✅ Test ngay trong app qua Settings → Debug & Testing
- ✅ Xem crash reports real-time trên Firebase Console
- ✅ Tích hợp vào code để track errors và crashes
- ✅ Improve app stability dựa trên crash analytics

Happy debugging! 🚀

