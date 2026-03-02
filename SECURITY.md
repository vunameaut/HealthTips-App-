# Hướng dẫn Bảo mật API Keys

## Tại sao cần bảo mật API Keys?

API keys là thông tin nhạy cảm cho phép truy cập vào các dịch vụ như Google Gemini, OpenAI, Firebase, v.v. Nếu bị lộ:

- ⚠️ Người khác có thể sử dụng API key của bạn
- 💰 Bạn có thể bị tính phí cho việc sử dụng không phải của mình
- 🔓 Dữ liệu của bạn có thể bị truy cập trái phép
- 🚫 API key có thể bị vô hiệu hóa bởi nhà cung cấp dịch vụ

## Cơ chế bảo mật trong dự án

### 1. File `local.properties`

**Chức năng:**
- Lưu trữ API keys và thông tin nhạy cảm LOCAL trên máy developer
- Không bao giờ được commit lên Git

**Vị trí:** Thư mục gốc của project (`/local.properties`)

**Trạng thái:**
- ✅ Đã được thêm vào `.gitignore`
- ✅ Git sẽ bỏ qua file này khi commit
- ✅ Mỗi developer có file `local.properties` riêng

### 2. File `local.properties.example`

**Chức năng:**
- File mẫu hướng dẫn cấu hình
- KHÔNG chứa API keys thật
- CÓ THỂ commit lên Git an toàn

**Cách sử dụng:**
```bash
# Copy file mẫu
cp local.properties.example local.properties

# Sau đó điền API keys thật vào local.properties
```

### 3. Build Configuration (`build.gradle.kts`)

**Cơ chế:**
```kotlin
// Đọc API key từ local.properties
val localProperties = Properties()
localProperties.load(FileInputStream(rootProject.file("local.properties")))

val geminiApiKey = localProperties.getProperty("gemini.api.key", "")

// Inject vào BuildConfig
buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
```

**Kết quả:**
- API key được compile vào file `BuildConfig.java`
- Source code KHÔNG chứa API key trực tiếp
- File `BuildConfig.java` nằm trong `/build` (đã bỏ qua bởi Git)

### 4. Sử dụng trong Code

**ĐÚNG ✅:**
```java
// Sử dụng BuildConfig
private static String getGeminiApiKey() {
    try {
        java.lang.reflect.Field field = BuildConfig.class.getDeclaredField("GEMINI_API_KEY");
        return field.get(null).toString();
    } catch (Exception e) {
        Log.e(TAG, "API key chưa được cấu hình");
        return "";
    }
}
```

**SAI ❌:**
```java
// KHÔNG BAO GIỜ làm như này!
private static final String API_KEY = "AIzaSyCE7CLwXyr3BMg5tyh2r6AtDi0RA5wI5ic";
```

## Checklist Bảo mật

Trước khi commit code:

- [ ] Kiểm tra KHÔNG có API key trong source code (`.java`, `.kt`, `.xml`)
- [ ] File `local.properties` có trong `.gitignore`
- [ ] Chỉ commit file `local.properties.example` (không có key thật)
- [ ] API keys được load từ `BuildConfig`
- [ ] Run `git status` để đảm bảo `local.properties` không được track

```bash
# Kiểm tra file nào sẽ được commit
git status

# Nếu thấy local.properties trong danh sách -> NGUY HIỂM!
# Cần xóa khỏi staging:
git reset HEAD local.properties
```

## Quy trình cho Team Members

### Developer mới tham gia project:

1. **Clone repository:**
   ```bash
   git clone https://github.com/your-username/HealthTips-App-.git
   cd HealthTips-App-
   ```

2. **Copy file mẫu:**
   ```bash
   cp local.properties.example local.properties
   ```

3. **Lấy API keys:**
   - Google Gemini: https://makersuite.google.com/app/apikey
   - Firebase: Lấy từ Firebase Console
   - Cloudinary: Lấy từ Cloudinary Dashboard

4. **Cập nhật `local.properties`:**
   ```properties
   gemini.api.key=AIzaSy_YOUR_ACTUAL_KEY_HERE
   ```

5. **Rebuild project:**
   - Build → Rebuild Project trong Android Studio

6. **KHÔNG BAO GIỜ commit `local.properties`**

### Khi cần thay đổi API configuration:

1. ✅ Cập nhật `local.properties.example` (file mẫu)
2. ✅ Cập nhật `build.gradle.kts` (build configuration)
3. ✅ Thông báo cho team members cập nhật `local.properties` của họ
4. ❌ KHÔNG commit `local.properties` chứa key thật

## Khôi phục khi bị lộ API Key

Nếu không may commit nhầm API key:

1. **Vô hiệu hóa API key ngay lập tức:**
   - Google Gemini: https://makersuite.google.com/app/apikey
   - Xóa hoặc regenerate key bị lộ

2. **Tạo API key mới:**
   - Tạo key mới từ console
   - Cập nhật vào `local.properties`

3. **Xóa key khỏi Git history:**
   ```bash
   # Cảnh báo: Làm việc này sẽ rewrite history!
   git filter-branch --force --index-filter \
     "git rm --cached --ignore-unmatch local.properties" \
     --prune-empty --tag-name-filter cat -- --all
   
   # Force push (cần thông báo team)
   git push origin --force --all
   ```

4. **Thông báo team:**
   - Báo cho tất cả members biết
   - Yêu cầu họ pull lại code

## Best Practices

1. **Định kỳ rotate API keys** (3-6 tháng một lần)
2. **Sử dụng API key có quyền hạn tối thiểu** (principle of least privilege)
3. **Monitor usage** để phát hiện truy cập bất thường
4. **Set usage limits** trên các API platforms
5. **Review code** trước khi merge PR để đảm bảo không có sensitive data

## Các file cần bảo mật

```
local.properties          ❌ KHÔNG commit (chứa API keys thật)
local.properties.example  ✅ CÓ THỂ commit (chỉ là template)
google-services.json      ⚠️  Có thể commit (public info) nhưng nên cẩn thận
build/                    ✅ Đã trong .gitignore
.gradle/                  ✅ Đã trong .gitignore
```

## Tài nguyên tham khảo

- [Firebase Security Best Practices](https://firebase.google.com/docs/rules)
- [Android Security Best Practices](https://developer.android.com/topic/security/best-practices)
- [OWASP Mobile Security](https://owasp.org/www-project-mobile-security/)

---

**Nhớ:** Bảo mật là trách nhiệm của mọi người trong team! 🔐

