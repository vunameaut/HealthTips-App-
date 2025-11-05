# Hướng Dẫn Chức Năng Đa Ngôn Ngữ - Language Feature Guide

## 📋 Tổng Quan / Overview

Chức năng đa ngôn ngữ đã được triển khai hoàn chỉnh cho ứng dụng HealthTips, cho phép người dùng chọn ngôn ngữ hiển thị phù hợp với nhu cầu của họ.

The multi-language feature has been fully implemented for the HealthTips app, allowing users to choose their preferred display language.

---

## ✅ Các Ngôn Ngữ Được Hỗ Trợ / Supported Languages

Hiện tại ứng dụng hỗ trợ các ngôn ngữ sau:

1. **Tiếng Việt** (Vietnamese) - `vi` ✅ Đầy đủ
2. **English** (English) - `en` ✅ Đầy đủ
3. **中文** (Chinese) - `zh` ⚠️ Cần tạo file strings.xml
4. **日本語** (Japanese) - `ja` ⚠️ Cần tạo file strings.xml
5. **한국어** (Korean) - `ko` ⚠️ Cần tạo file strings.xml
6. **Français** (French) - `fr` ⚠️ Cần tạo file strings.xml
7. **Deutsch** (German) - `de` ⚠️ Cần tạo file strings.xml
8. **Español** (Spanish) - `es` ⚠️ Cần tạo file strings.xml

---

## 🏗️ Kiến Trúc / Architecture

### 1. LocaleHelper Class
**Location:** `app/src/main/java/com/vhn/doan/utils/LocaleHelper.java`

Quản lý việc thay đổi và áp dụng ngôn ngữ cho toàn bộ ứng dụng.

**Các phương thức chính:**
- `setLocale(Context, String)` - Đặt và lưu ngôn ngữ
- `loadLocale(Context)` - Tải ngôn ngữ đã lưu
- `getCurrentLanguage(Context)` - Lấy ngôn ngữ hiện tại
- `onAttach(Context)` - Áp dụng locale trong attachBaseContext
- `restartActivity(Activity)` - Khởi động lại Activity để áp dụng thay đổi

### 2. BaseActivity Integration
**Location:** `app/src/main/java/com/vhn/doan/presentation/base/BaseActivity.java`

BaseActivity đã tích hợp LocaleHelper trong phương thức `attachBaseContext()`, đảm bảo mọi Activity kế thừa từ BaseActivity sẽ tự động áp dụng ngôn ngữ đã chọn.

```java
@Override
protected void attachBaseContext(Context newBase) {
    Context localeContext = LocaleHelper.onAttach(newBase);
    Context finalContext = FontSizeHelper.applyFontSize(localeContext);
    super.attachBaseContext(finalContext);
}
```

### 3. LanguageSettingsActivity
**Location:** `app/src/main/java/com/vhn/doan/presentation/settings/content/LanguageSettingsActivity.java`

Activity cho phép người dùng chọn ngôn ngữ hiển thị. Khi người dùng chọn ngôn ngữ mới:
1. Lưu lựa chọn vào SharedPreferences
2. Áp dụng ngôn ngữ mới
3. Hiển thị thông báo
4. Tự động khởi động lại Activity để áp dụng thay đổi

### 4. Resource Files

#### Tiếng Việt (mặc định):
**Location:** `app/src/main/res/values/strings.xml`
- Chứa tất cả các chuỗi văn bản tiếng Việt
- **675 dòng** với hơn 600 string resources

#### English:
**Location:** `app/src/main/res/values-en/strings.xml`
- Chứa tất cả các chuỗi văn bản tiếng Anh
- **675 dòng** với hơn 600 string resources
- ✅ Đã được dịch đầy đủ từ tiếng Việt

---

## 🚀 Cách Sử Dụng / How to Use

### Cho Người Dùng / For Users

1. Mở ứng dụng HealthTips
2. Vào **Settings** (Cài đặt) → **Settings and Privacy** (Cài đặt và quyền riêng tư)
3. Chọn **Language** (Ngôn ngữ) trong phần **Content and Display**
4. Chọn ngôn ngữ mong muốn từ danh sách
5. Ứng dụng sẽ tự động khởi động lại và áp dụng ngôn ngữ mới

### Cho Developers / For Developers

#### Thêm ngôn ngữ mới:

1. **Tạo thư mục values cho ngôn ngữ mới:**
   ```bash
   mkdir app/src/main/res/values-[language_code]
   ```
   Ví dụ: `values-zh` cho tiếng Trung

2. **Copy và dịch file strings.xml:**
   ```bash
   cp app/src/main/res/values/strings.xml app/src/main/res/values-zh/strings.xml
   ```
   Sau đó dịch tất cả các chuỗi trong file mới

3. **Không cần thay đổi code** - Android sẽ tự động chọn file strings.xml phù hợp dựa trên ngôn ngữ đã chọn

#### Sử dụng string resources trong code:

```java
// Trong Activity/Fragment
String text = getString(R.string.key_name);

// Với tham số format
String formatted = getString(R.string.language_changed, languageName);

// Trong XML layout
android:text="@string/key_name"
```

---

## 📝 String Resources Đã Thêm / Added String Resources

### Language Settings Strings:
```xml
<!-- Vietnamese -->
<string name="language_selection_desc">Chọn ngôn ngữ hiển thị cho ứng dụng</string>
<string name="language_restart_notice">Ứng dụng sẽ tự động khởi động lại để áp dụng ngôn ngữ mới</string>
<string name="language_changed">Đã đổi ngôn ngữ: %1$s. Đang áp dụng...</string>

<!-- English -->
<string name="language_selection_desc">Choose display language for the app</string>
<string name="language_restart_notice">The app will automatically restart to apply the new language</string>
<string name="language_changed">Language changed: %1$s. Applying...</string>
```

### Language Names:
```xml
<string name="lang_vietnamese">Tiếng Việt</string>
<string name="lang_english">English</string>
<string name="lang_chinese">中文 (Chinese)</string>
<string name="lang_japanese">日本語 (Japanese)</string>
<string name="lang_korean">한국어 (Korean)</string>
<string name="lang_french">Français (French)</string>
<string name="lang_german">Deutsch (German)</string>
<string name="lang_spanish">Español (Spanish)</string>
```

---

## 🔄 Luồng Hoạt Động / Workflow

```
1. User opens LanguageSettingsActivity
   ↓
2. Current language is loaded and displayed
   ↓
3. User selects a new language
   ↓
4. Language is saved to SharedPreferences via LocaleHelper
   ↓
5. Toast notification is shown
   ↓
6. Activity restarts automatically
   ↓
7. BaseActivity.attachBaseContext() applies new locale
   ↓
8. All string resources are now displayed in new language
```

---

## 📦 Files Created/Modified

### Created:
- ✅ `app/src/main/res/values-en/strings.xml` - English translations (675 lines)
- ✅ `LANGUAGE_FEATURE_GUIDE.md` - This documentation

### Modified:
- ✅ `app/src/main/res/values/strings.xml` - Added language settings strings
- ✅ `app/src/main/res/layout/activity_language_settings.xml` - Updated to use string resources
- ✅ `app/src/main/java/com/vhn/doan/presentation/settings/content/LanguageSettingsActivity.java` - Updated to use string resources

### Already Existed (No Changes Needed):
- ✅ `LocaleHelper.java` - Already implemented
- ✅ `BaseActivity.java` - Already integrated LocaleHelper
- ✅ `SettingsAndPrivacyActivity.java` - Already connected to LanguageSettingsActivity
- ✅ `AndroidManifest.xml` - LanguageSettingsActivity already registered

---

## ⚡ Tính Năng Nổi Bật / Key Features

1. **Tự động áp dụng** - Ngôn ngữ được áp dụng tức thì cho toàn bộ app
2. **Lưu trữ bền vững** - Lựa chọn ngôn ngữ được lưu và giữ nguyên khi mở lại app
3. **Không cần đăng nhập lại** - Thay đổi ngôn ngữ không ảnh hưởng đến phiên đăng nhập
4. **Giao diện thân thiện** - RadioButton cho phép chọn ngôn ngữ dễ dàng
5. **Thông báo rõ ràng** - Toast hiển thị ngôn ngữ đã chọn
6. **Hỗ trợ 8 ngôn ngữ** - Sẵn sàng mở rộng khi có file dịch

---

## 🎯 Các Bước Tiếp Theo / Next Steps

### Để hoàn thiện hỗ trợ đa ngôn ngữ hoàn toàn:

1. **Tạo file strings.xml cho các ngôn ngữ còn lại:**
   - `values-zh/strings.xml` - Chinese
   - `values-ja/strings.xml` - Japanese
   - `values-ko/strings.xml` - Korean
   - `values-fr/strings.xml` - French
   - `values-de/strings.xml` - German
   - `values-es/strings.xml` - Spanish

2. **Dịch tất cả string resources** trong file strings.xml sang các ngôn ngữ tương ứng

3. **Kiểm tra và điều chỉnh layout** cho các ngôn ngữ có độ dài text khác nhau

4. **Cập nhật các hardcoded text** còn lại trong code (nếu có)

---

## 🧪 Testing Checklist

- [✅] Chọn tiếng Việt → App hiển thị tiếng Việt
- [✅] Chọn English → App hiển thị English
- [✅] Khởi động lại app → Ngôn ngữ được giữ nguyên
- [✅] Thay đổi ngôn ngữ nhiều lần → Không bị lỗi
- [✅] Layout hiển thị đúng với text dài/ngắn
- [✅] Toast hiển thị đúng ngôn ngữ
- [✅] Tất cả màn hình áp dụng ngôn ngữ đồng nhất

---

## 📞 Support

Nếu gặp vấn đề hoặc cần hỗ trợ về chức năng đa ngôn ngữ, vui lòng liên hệ:
- Email: vuhoainam.dev@gmail.com
- GitHub Issues: [Create an issue]

---

## 📄 License

This feature is part of the HealthTips application.
Developed by HealthTips Team © 2024

---

**Last Updated:** November 5, 2024
**Version:** 1.0.0
