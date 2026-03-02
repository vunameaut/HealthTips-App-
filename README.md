"# HealthTips App - Ứng dụng Mẹo Sức Khỏe

Ứng dụng Android cung cấp các mẹo sức khỏe hữu ích và trợ lý AI sức khỏe thông minh.

## 🔧 Cài đặt và Cấu hình

### Yêu cầu hệ thống
- Android Studio (phiên bản mới nhất)
- JDK 11 trở lên
- Android SDK API 26 trở lên

### Cấu hình API Keys (Quan trọng!)

Để bảo mật và tránh API keys bị public lên GitHub, dự án sử dụng file `local.properties` để lưu trữ các thông tin nhạy cảm.

#### Bước 1: Tạo file `local.properties`

File `local.properties` đã có sẵn trong thư mục gốc của dự án. Nếu chưa có, tạo file mới với nội dung:

```properties
# Android SDK location
sdk.dir=YOUR_ANDROID_SDK_PATH

# Google Gemini API Configuration (Bắt buộc cho tính năng Chat AI)
# Lấy API key tại: https://makersuite.google.com/app/apikey
gemini.api.key=YOUR_GEMINI_API_KEY_HERE

# OpenAI API Configuration (Tùy chọn - backup)
openai.api.key=YOUR_OPENAI_API_KEY_HERE

# Cloudinary API Configuration (Tùy chọn)
cloudinary.api.key=YOUR_CLOUDINARY_API_KEY
cloudinary.api.secret=YOUR_CLOUDINARY_API_SECRET

# Firebase Auth Configuration (Tùy chọn)
firebase.auth.key=YOUR_FIREBASE_AUTH_KEY
```

#### Bước 2: Lấy Google Gemini API Key

1. Truy cập: https://makersuite.google.com/app/apikey
2. Đăng nhập bằng tài khoản Google
3. Tạo API key mới
4. Copy API key và dán vào `local.properties`:
   ```properties
   gemini.api.key=AIzaSyXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   ```

#### Bước 3: Rebuild Project

Sau khi thêm API key vào `local.properties`:

1. Chọn **Build** → **Rebuild Project** trong Android Studio
2. Đợi quá trình build hoàn tất
3. Run ứng dụng

### ⚠️ Lưu ý Bảo mật

- ✅ File `local.properties` đã được thêm vào `.gitignore` - sẽ KHÔNG bị push lên GitHub
- ✅ API keys được load từ `BuildConfig` - không hardcode trong source code
- ✅ File `local.properties.example` được cung cấp để hướng dẫn - có thể commit file này
- ❌ **KHÔNG BAO GIỜ** commit file `local.properties` chứa API key thật lên GitHub
- ❌ **KHÔNG BAO GIỜ** hardcode API key trực tiếp vào code

## 🚀 Chạy ứng dụng

1. Clone repository:
   ```bash
   git clone https://github.com/your-username/HealthTips-App-.git
   cd HealthTips-App-
   ```

2. Cấu hình `local.properties` (xem hướng dẫn ở trên)

3. Mở project trong Android Studio

4. Rebuild project:
   - **Build** → **Rebuild Project**

5. Run ứng dụng:
   - Chọn device/emulator
   - Click **Run** (hoặc Shift+F10)

## 📱 Tính năng

- ✅ Xem các mẹo sức khỏe theo danh mục
- ✅ Trợ lý AI sức khỏe thông minh (Google Gemini)
- ✅ Lưu mẹo yêu thích
- ✅ Nhắc nhở sức khỏe
- ✅ Theo dõi lịch sử chat
- ✅ Hỗ trợ đa ngôn ngữ
- ✅ Dark mode / Light mode

## 🛠️ Công nghệ sử dụng

- **Ngôn ngữ**: Java
- **Kiến trúc**: MVP (Model-View-Presenter)
- **Database**: Firebase Realtime Database
- **Authentication**: Firebase Authentication
- **AI API**: Google Gemini API
- **Storage**: Firebase Storage
- **Notifications**: Firebase Cloud Messaging (FCM)
- **Dependency Injection**: Dagger 2
- **UI**: Material Design 3

## 📝 Cấu trúc dự án

```
app/src/main/java/com/vhn/doan/
├── data/           # Models, Repositories, Data Sources
├── presentation/   # Activities, Fragments, Presenters
├── services/       # Background Services
├── di/             # Dependency Injection (Dagger 2)
├── receivers/      # Broadcast Receivers
└── utils/          # Utilities, Helpers, Constants
```

## 🔐 Bảo mật

Dự án tuân thủ các nguyên tắc bảo mật:

1. **API Keys**: Lưu trong `local.properties`, không commit lên Git
2. **BuildConfig**: API keys được inject vào BuildConfig khi build
3. **ProGuard**: Minify và obfuscate code cho bản release
4. **Firebase Security Rules**: Kiểm soát truy cập dữ liệu

## 📄 License

[Thêm thông tin license của bạn ở đây]

## 👥 Đóng góp

Mọi đóng góp đều được chào đón! Vui lòng:

1. Fork repository
2. Tạo branch mới (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request

**Lưu ý khi đóng góp:**
- KHÔNG commit file `local.properties` chứa API keys
- Sử dụng file `local.properties.example` để hướng dẫn
- Tuân thủ coding conventions trong `.github/copilot-instructions.md`

## 📞 Liên hệ

[Thêm thông tin liên hệ của bạn]

---

**Made with ❤️ for Health & Wellness**
" 
