# 📋 HƯỚNG DẪN CHI TIẾT THỰC HIỆN BÁO CÁO ĐÁNH GIÁ APP HEALTHTIPS

Chào bạn,

Đây là tài liệu hướng dẫn chi tiết các bước thực hiện dựa trên "BÁO CÁO ĐÁNH GIÁ APP HEALTHTIPS". Tài liệu này sẽ tập trung giải thích "Tại sao?" và hướng dẫn "Làm thế nào?" cho từng hạng mục được đánh dấu là THIẾU, CHƯA ĐẦY ĐỦ, hoặc CẦN CẢI THIỆN.

---

## PHẦN 1: HƯỚNG DẪN CÁC THỦ TỤC BẮT BUỘC

Đây là những mục **sống còn** để có thể phát hành ứng dụng lên Google Play. Bạn phải hoàn thành tất cả.

### 1. Firebase Crashlytics ❌ THIẾU

* **Giải thích (Tại sao):** Khi người dùng đã cài app, bạn không thể xem được lỗi (logcat) trên máy của họ. Crashlytics là công cụ duy nhất giúp bạn tự động nhận báo cáo chi tiết khi ứng dụng bị sập (crash), bao gồm lỗi ở dòng code nào, trên thiết bị gì, phiên bản Android nào. Không có nó, bạn sẽ "mù" hoàn toàn về các lỗi xảy ra trong thực tế.
* **Hướng dẫn chi tiết (Làm thế nào):**
    1.  **Trên Firebase Console:**
        * Mở dự án Firebase của bạn.
        * Trong menu bên trái, tìm **Release & Monitor** -> **Crashlytics**.
        * Nhấn **Enable Crashlytics**.
    2.  **Trong file `build.gradle.kts` (cấp Project):**
        ```kotlin
        plugins {
            // ...
            id("com.google.firebase.crashlytics") version "X.X.X" apply false 
        }
        ```
        (Thay `X.X.X` bằng phiên bản mới nhất).
    3.  **Trong file `build.gradle.kts` (cấp App):**
        ```kotlin
        plugins {
            // ...
            id("com.google.firebase.crashlytics")
        }

        dependencies {
            // Thư viện Crashlytics và Analytics (cần thiết cho Crashlytics)
            implementation("com.google.firebase:firebase-crashlytics")
            implementation("com.google.firebase:firebase-analytics")
        }
        ```
    4.  **Thử nghiệm:** Để chắc chắn nó hoạt động, hãy tạo một nút bấm tạm thời và thêm code này vào:
        ```java
        // Gây crash để thử nghiệm
        throw new RuntimeException("Test Crash"); 
        ```
        Chạy app (bản build debug), nhấn nút đó. Tắt app và mở lại (để nó gửi báo cáo). Sau vài phút, bạn sẽ thấy báo cáo "Test Crash" xuất hiện trên dashboard Crashlytics.

### 2. Privacy Policy URL ⚠️ CHƯA ĐẦY ĐỦ

* **Giải thích:** Google Play yêu cầu bạn phải cung cấp một đường link (URL) công khai, nơi người dùng có thể đọc Chính sách bảo mật. Chính sách này giải thích bạn thu thập dữ liệu gì (email, tên, lịch sử chat) và dùng vào việc gì.
* **Hướng dẫn chi tiết:**
    1.  **Viết nội dung:**
        * Sử dụng "Template Privacy Policy" trong báo cáo.
        * Viết rõ: Bạn thu thập **Tên, email, avatar** (qua Firebase Auth), **Lịch sử chat** (lưu trên Firebase, gửi cho OpenAI), **Tips yêu thích, Nhắc nhở**.
        * Mục đích: **Cá nhân hóa, AI chat, Nhắc nhở**.
        * Chia sẻ với bên thứ ba: **Firebase (Google)** để lưu trữ và **OpenAI** để xử lý chat.
    2.  **Hosting (Lưu trữ) file:** Bạn cần một URL công khai.
        * **Cách dễ nhất (Firebase Hosting):**
            1.  Cài Firebase CLI (nếu chưa có): `npm install -g firebase-tools`
            2.  Trong thư mục dự án: `firebase login`
            3.  `firebase init hosting` (Chọn dự án Firebase của bạn).
            4.  Nó sẽ tạo thư mục `public` với file `index.html`.
            5.  Copy toàn bộ nội dung Privacy Policy của bạn vào file `index.html` đó.
            6.  Chạy lệnh: `firebase deploy --only hosting`
            7.  Firebase sẽ cho bạn một URL (ví dụ: `your-app-name.web.app`).
        * **Cách khác:** Dùng [GitHub Pages](https://pages.github.com/) (miễn phí).
    3.  **Trên Google Play Console:**
        * Vào ứng dụng của bạn -> **Nội dung ứng dụng** (App content).
        * Tìm mục **Chính sách bảo mật** (Privacy Policy).
        * Dán URL bạn vừa tạo vào và lưu lại.

### 3. Data Safety Form ⚠️ CẦN CHUẨN BỊ

* **Giải thích:** Đây là một biểu mẫu (form) bắt buộc trên Play Console. Bạn phải khai báo *trung thực* những loại dữ liệu bạn thu thập và chia sẻ. Thông tin này sẽ hiển thị công khai trên trang tải app.
* **Hướng dẫn chi tiết:**
    1.  Vào Play Console -> **Nội dung ứng dụng** -> **An toàn dữ liệu** (Data Safety).
    2.  Bắt đầu điền form. Bạn sẽ cần khai báo dựa trên báo cáo:
        * **Thu thập dữ liệu?** -> Có.
        * **Loại dữ liệu (Data types):**
            * Thông tin cá nhân: Tên, Địa chỉ email.
            * Thông tin sức khỏe: Lịch sử chat AI, tips yêu thích, nhắc nhở.
            * Hoạt động trong ứng dụng: Lịch sử tìm kiếm, tips đã xem.
            * Mã nhận dạng thiết bị: (Nếu bạn dùng Push Notification, bạn thu thập Notification tokens).
        * **Sử dụng dữ liệu (Data usage):**
            * Với mỗi loại dữ liệu trên, check vào các mục đích: Chức năng ứng dụng, Cá nhân hóa, Phân tích (Analytics).
        * **Chia sẻ dữ liệu (Data sharing):**
            * Khai báo "Có" chia sẻ với bên thứ ba.
            * **Bên thứ ba:** `Firebase/Google` (cho hạ tầng), `OpenAI` (cho AI chat).
        * **Bảo mật:**
            * Check **"Dữ liệu được mã hóa khi truyền tải"** (vì bạn dùng HTTPS).
            * Check **"Người dùng có thể yêu cầu xóa dữ liệu"**.

### 4. App Signing ⚠️ CẦN CHUẨN BỊ

* **Giải thích:** Đây là "chữ ký số" của bạn. Nó đảm bảo rằng chỉ bạn mới có thể phát hành bản cập nhật cho app. Nếu mất "chữ ký" (keystore), bạn sẽ **không bao giờ** có thể cập nhật ứng dụng của mình nữa.
* **Hướng dẫn chi tiết:**
    1.  **Cách khuyến nghị (Play App Signing):**
        * Google sẽ quản lý "chữ ký" (app signing key) cho bạn. Bạn chỉ cần tạo một "chữ ký tải lên" (upload key). Đây là cách an toàn nhất.
    2.  **Tạo Upload Keystore (Bạn vẫn phải làm bước này):**
        * Mở Terminal (hoặc Command Prompt trên Windows).
        * Chạy lệnh trong báo cáo:
            ```bash
            keytool -genkey -v -keystore healthtips-release.keystore \
              -alias healthtips -keyalg RSA -keysize 2048 -validity 10000 
            ```
           
        * Nó sẽ hỏi bạn 2 loại mật khẩu (store password và key password) và các thông tin (Tên, Tổ chức...). Ghi nhớ kỹ mật khẩu này.
        * **CỰC KỲ QUAN TRỌNG:** Sao lưu file `healthtips-release.keystore` này ở một nơi an toàn (Google Drive, USB...).
    3.  **Cấu hình Build Gradle (Cách an toàn):**
        * **Đừng** hard-code mật khẩu vào `build.gradle.kts` như trong báo cáo.
        * Tạo file `keystore.properties` ở thư mục gốc dự án (cùng cấp với `build.gradle.kts`).
        * Thêm file `keystore.properties` vào `.gitignore` để không push lên Git.
        * Nội dung file `keystore.properties`:
            ```properties
            storeFile=../healthtips-release.keystore 
            storePassword=your_password
            keyAlias=healthtips
            keyPassword=your_password
            ```
        * Trong `app/build.gradle.kts`, đọc các giá trị này:
            ```kotlin
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            val keystoreProperties = java.util.Properties()
            if (keystorePropertiesFile.exists()) {
                keystoreProperties.load(java.io.FileInputStream(keystorePropertiesFile))
            }

            android {
                signingConfigs {
                    release {
                        storeFile = file(keystoreProperties.getProperty("storeFile"))
                        storePassword = keystoreProperties.getProperty("storePassword")
                        keyAlias = keystoreProperties.getProperty("keyAlias")
                        keyPassword = keystoreProperties.getProperty("keyPassword")
                    }
                }
                buildTypes {
                    release {
                        signingConfig = signingConfigs.release
                        // ...
                    }
                }
            }
            ```
    4.  **Build file AAB:** Trong Android Studio, chọn **Build** -> **Generate Signed Bundle / APK...** -> **Android App Bundle** -> chọn `release` và dùng keystore bạn vừa tạo.

### 5. Screenshots & Store Listing ❌ THIẾU

* **Giải thích:** Đây là "bộ mặt" của ứng dụng trên cửa hàng. Người dùng quyết định tải hay không dựa vào hình ảnh và mô tả. Google Play yêu cầu tối thiểu 2 ảnh chụp màn hình.
* **Hướng dẫn chi tiết:**
    1.  **Screenshots:**
        * Chạy ứng dụng trên Emulator hoặc máy thật.
        * **Mẹo:** Bật "Chế độ demo" (Demo mode) trên Android để thanh trạng thái (status bar) luôn sạch đẹp (10:00, pin 100%, full wifi/sóng).
        * Chụp các màn hình đẹp nhất theo gợi ý: Home, Chi tiết tip, Chat AI, Nhắc nhở, Video, Profile.
    2.  **Feature Graphic:**
        * Tạo một ảnh 1024x500. Dùng Canva, Figma...
        * Ảnh này thường là logo, tên app và một hình nền đẹp. Nó sẽ xuất hiện ở đầu trang.
    3.  **Descriptions:**
        * **Short (80 ký tự):** Một câu mô tả súc tích. Ví dụ: "Trợ lý sức khỏe AI, mẹo vặt hàng ngày và nhắc nhở tiện lợi."
        * **Full (4000 ký tự):** Mô tả chi tiết các tính năng:
            * Giới thiệu chung
            * Tính năng nổi bật (Chat AI, Tips)
            * Danh sách tính năng (Nhắc nhở, Video...)
            * Cam kết bảo mật.
    4.  **Upload:** Tải tất cả lên Play Console trong mục **Danh sách cửa hàng chính** (Main store listing).

---

## PHẦN 2: HƯỚNG DẪN BỔ SUNG TÍNH NĂNG (MỨC ĐỘ CAO)

Đây là các tính năng quan trọng nên có để tăng trải nghiệm và giữ chân người dùng.

### 1. Push Notifications ❌

* **Giải thích:** Dùng Firebase Cloud Messaging (FCM) để gửi thông báo đẩy đến người dùng, ngay cả khi họ không mở app. Rất quan trọng để "kéo" người dùng trở lại (ví dụ: "Có Mẹo sức khỏe mới cho bạn!", "Tip của ngày", "AI đã trả lời bạn").
* **Hướng dẫn chi tiết:**
    1.  **Thêm thư viện:**
        ```kotlin
        // app/build.gradle.kts
        implementation("com.google.firebase:firebase-messaging") 
        ```
       
    2.  **Tạo Service:** Tạo một class mới kế thừa từ `FirebaseMessagingService`.
        ```java
        public class MyFirebaseMessagingService extends FirebaseMessagingService {

            @Override
            public void onNewToken(@NonNull String token) {
                super.onNewToken(token);
                // Gửi token này lên server (Firebase Database) để lưu lại
                // Bạn cần token này để gửi thông báo cho từng người dùng cụ thể
                Log.d("FCM", "New token: " + token);
            }

            @Override
            public void onMessageReceived(@NonNull RemoteMessage message) {
                super.onMessageReceived(message);
                // Xử lý khi nhận được thông báo
                // Hiển thị notification lên thanh trạng thái
                if (message.getNotification() != null) {
                    String title = message.getNotification().getTitle();
                    String body = message.getNotification().getBody();
                    showNotification(title, body);
                }
            }

            private void showNotification(String title, String body) {
                // Code tạo và hiển thị Notification (NotificationCompat.Builder)
            }
        }
        ```
    3.  **Đăng ký Service trong `AndroidManifest.xml`:**
        ```xml
        <service
            android:name=".MyFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
        ```
    4.  **Thử nghiệm:** Vào Firebase Console -> Engage -> Messaging. Tạo một chiến dịch (campaign) mới và gửi thông báo thử nghiệm.

### 2. Analytics Tracking ⚠️ CHƯA ĐẦY ĐỦ

* **Giải thích:** Bạn đã cài `firebase-analytics`, nhưng chưa ghi lại các sự kiện (events). Bạn cần biết người dùng đang làm gì trong app: Họ xem tip nào nhiều nhất? Họ có dùng tìm kiếm không? Họ chat AI bao nhiêu?.
* **Hướng dẫn chi tiết:**
    1.  Khởi tạo `FirebaseAnalytics` trong `Activity` hoặc `Application`:
        ```java
        private FirebaseAnalytics mFirebaseAnalytics;
        // ...
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        ```
    2.  **Ghi lại sự kiện (Log events):**
        * **Khi xem chi tiết tip:** (Trong `TipDetailActivity.java`)
            ```java
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, tipId);
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, tipTitle);
            mFirebaseAnalytics.logEvent("view_health_tip", bundle); //
            ```
        * **Khi tìm kiếm:** (Trong `SearchActivity.java`)
            ```java
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, query);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle); //
            ```
        * **Khi chat AI:** (Trong `ChatActivity.java` khi gửi tin)
            ```java
            Bundle bundle = new Bundle();
            bundle.putString("conversation_id", conversationId);
            mFirebaseAnalytics.logEvent("ai_chat_message", bundle); //
            ```
        * **Khi tạo nhắc nhở:** (Trong `ReminderActivity.java`)
            ```java
            mFirebaseAnalytics.logEvent("reminder_created", null); 
            ```
    3.  Sau khi log, vào Firebase Console -> Analytics -> **Events** để xem thống kê.

### 3. Offline Mode ❌

* **Giải thích:** Hiện tại, nếu không có mạng, app sẽ không hiển thị gì. Chế độ offline cho phép người dùng xem lại các health tips, danh sách yêu thích đã xem trước đó. Điều này cải thiện trải nghiệm người dùng rõ rệt.
* **Hướng dẫn chi tiết (Phức tạp, cần kiến trúc):**
    1.  **Thêm thư viện Room (Local Database):**
        ```kotlin
        // app/build.gradle.kts
        implementation("androidx.room:room-runtime:2.6.1")
        kapt("androidx.room:room-compiler:2.6.1") // (hoặc ksp)
        ```
    2.  **Tạo cấu trúc Room:**
        * `@Entity`: Tạo một class `HealthTipEntity` (giống model, nhưng có `@Entity`).
        * `@Dao`: (Data Access Object) Tạo interface `HealthTipDao` với các hàm `insertAll`, `getTips`, `getTipById`.
        * `@Database`: Tạo class `AppDatabase` kế thừa `RoomDatabase`.
    3.  **Tích hợp (Repository Pattern):**
        * Tạo một `HealthTipRepository`.
        * Khi app cần dữ liệu (ví dụ: vào Home), nó sẽ gọi `repository.getTips()`.
        * **Chiến lược:** `Repository` sẽ:
            1.  Lấy dữ liệu từ **Room** (local cache) và hiển thị *ngay lập tức*.
            2.  Đồng thời, gọi API (Firebase) để lấy dữ liệu mới.
            3.  Khi có dữ liệu mới, lưu vào **Room**.
            4.  **Room** (nếu dùng LiveData/Flow) sẽ tự động cập nhật UI.
    4.  **Sync (WorkManager):**
        * Dùng WorkManager để tạo một tác vụ (worker) chạy nền định kỳ (ví dụ: mỗi 6 tiếng) để tự động tải tips mới và lưu vào Room, ngay cả khi người dùng không mở app.

### 4. Rate & Review ❌

* **Giải thích:** Yêu cầu người dùng đánh giá app ngay bên trong ứng dụng, thay vì bắt họ vào Play Store. Tăng cơ hội nhận được đánh giá tốt.
* **Hướng dẫn chi tiết:**
    1.  **Thêm thư viện:**
        ```kotlin
        // app/build.gradle.kts
        implementation("com.google.android.play:review:2.0.1") 
        ```
       
    2.  **Kích hoạt luồng đánh giá:**
        * Tìm thời điểm thích hợp (ví dụ: sau khi người dùng đọc 5 tips, hoặc dùng app 3 ngày).
        * Gọi code sau:
            ```java
            ReviewManager manager = ReviewManagerFactory.create(this);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Đã lấy được ReviewInfo
                    ReviewInfo reviewInfo = task.getResult();
                    Task<Void> flow = manager.launchReviewFlow(this, reviewInfo);
                    flow.addOnCompleteListener(task2 -> {
                        // Luồng đánh giá đã hoàn tất (dù người dùng có review hay không)
                    });
                } else {
                    // Có lỗi, không hiển thị
                }
            });
            ```
        * **Lưu ý:** Google sẽ quyết định có hiển thị popup hay không (để tránh spam), bạn không thể ép buộc.

### 5. Share Functionality ⚠️ CHƯA ĐẦY ĐỦ

* **Giải thích:** Tính năng chia sẻ giúp người dùng lan truyền nội dung của bạn. Báo cáo nói rằng tính năng này chưa đầy đủ. Bạn cần cho phép share health tips và videos.
* **Hướng dẫn chi tiết:**
    1.  **Chia sẻ văn bản (Health Tip):**
        * Trong `TipDetailActivity`, thêm một nút Share.
        * Khi nhấn nút:
            ```java
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Xem mẹo sức khỏe này: " + tipTitle + "\n\n" + tipContent);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, "Chia sẻ mẹo này qua");
            startActivity(shareIntent);
            ```
    2.  **Deep Linking (Nâng cao):**
        * Để khi người khác nhấn vào link, nó sẽ mở đúng app và đúng bài tip đó.
        * Trong `AndroidManifest.xml`, thêm `intent-filter` vào `TipDetailActivity`:
            ```xml
            <activity android:name=".TipDetailActivity">
                <intent-filter>
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data android:scheme="https"
                          android:host="[www.your-app-domain.com](https://www.your-app-domain.com)"
                          android:pathPattern="/tips/.*" />
                </intent-filter>
            </activity>
            ```
        * Khi share, bạn sẽ share link (ví dụ: `https://www.your-app-domain.com/tips/tip-id-123`).

---

## PHẦN 3: HƯỚNG DẪN CẢI THIỆN BẢO MẬT & COMPLIANCE

Đây là các bước để làm ứng dụng an toàn hơn, bảo vệ người dùng và chính bạn.

### 1. ProGuard Configuration ⚠️

* **Giải thích:** ProGuard (hoặc R8) dùng để "làm rối" (obfuscate) và "thu nhỏ" (shrink) code. Nó làm cho file APK nhỏ hơn và khiến hacker khó khăn hơn khi dịch ngược code. Cấu hình sai có thể làm crash app (ví dụ: nó xóa mất class model).
* **Hướng dẫn chi tiết:**
    1.  Trong `app/build.gradle.kts`, đảm bảo bạn đã bật:
        ```kotlin
        buildTypes {
            release {
                isMinifyEnabled = true // Bật ProGuard/R8
                isShrinkResources = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro" //
                )
            }
        }
        ```
    2.  Trong file `app/proguard-rules.pro`, thêm các luật `keep` cho những class không được đổi tên:
        ```proguard
        # Giữ lại các class Model (dùng cho Firebase/Gson)
        -keep class com.vhn.doan.models.** { *; }

        # Giữ lại các class liên quan đến Firebase
        -keep class com.google.firebase.** { *; }

        # Giữ lại các class liên quan đến OpenAI API
        -keep class com.openai.** { *; }

        # Giữ lại các class ViewBinding (nếu dùng)
        -keep class * implements androidx.viewbinding.ViewBinding { *; }
        ```
    3.  **Quan trọng:** Sau khi bật, hãy build bản `release` và **test kỹ** mọi tính năng.

### 2. SSL Pinning ❌

* **Giải thích:** Đây là một kỹ thuật bảo mật nâng cao. Thay vì tin tưởng mọi chứng chỉ SSL, app của bạn chỉ tin tưởng một chứng chỉ SSL (certificate) *cụ thể* mà bạn chỉ định (ví dụ: của `api.openai.com`). Điều này giúp chống lại các cuộc tấn công Man-in-the-Middle (MITM).
* **Hướng dẫn chi tiết (Dùng với OkHttp/Retrofit):**
    ```java
    String hostname = "api.openai.com";
    CertificatePinner certificatePinner = new CertificatePinner.Builder()
        .add(hostname, "sha256/YOUR_CERTIFICATE_PUBLIC_KEY_HASH")
        .build();

    OkHttpClient okHttpClient = new OkHttpClient.Builder()
        .certificatePinner(certificatePinner)
        .build();

    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("[https://api.openai.com/](https://api.openai.com/)")
        .client(okHttpClient)
        .build();
    ```
    * **Cảnh báo:** Đây là kỹ thuật phức tạp. Nếu chứng chỉ SSL trên server thay đổi (và bạn không cập nhật hash trong app), **app sẽ không thể kết nối API nữa**.

### 3. Input Validation ✅ ĐÃ HOÀN THÀNH

* **Giải thích:** Không bao giờ tin tưởng dữ liệu người dùng nhập vào (trong form đăng ký, chat, tìm kiếm). Phải kiểm tra (validate) để tránh lỗi và các cuộc tấn công (ví dụ: SQL Injection, XSS).
* **Trạng thái:** ✅ **ĐÃ TRIỂN KHAI ĐẦY ĐỦ**
* **Đã thực hiện:**
    * **Form Đăng nhập/Đăng ký:** ✅
        * Kiểm tra `TextUtils.isEmpty(email)` trước khi xử lý.
        * Dùng `Patterns.EMAIL_ADDRESS.matcher(email).matches()` để kiểm tra email hợp lệ.
        * Kiểm tra độ dài mật khẩu `password.length() < 6`.
    * **Chat AI:** ✅
        * Làm sạch input với method `sanitizeInput()`.
        * Xóa các ký tự đặc biệt nguy hiểm (HTML tags, script tags, ký tự điều khiển).
        * Giới hạn độ dài tin nhắn `MAX_MESSAGE_LENGTH = 500` ký tự.
        * Chuẩn hóa khoảng trắng.
    * **Tìm kiếm:** ✅
        * Trim khoảng trắng `query.trim()`.
        * Kiểm tra rỗng.
        * Giới hạn độ dài query `MAX_SEARCH_LENGTH = 100` ký tự.

### 4. Rate Limiting ❌

* **Giải thích:** Ngăn chặn người dùng (hoặc bot) lạm dụng tính năng, đặc biệt là Chat AI (vì nó tốn tiền của bạn). Ví dụ: giới hạn 1 user chỉ được chat 50 tin nhắn/ngày.
* **Hướng dẫn chi tiết:**
    * **Cách tốt nhất (Server-side):**
        1.  Tạo một Firebase Function (Cloud Function) làm "proxy" (trung gian).
        2.  App gọi Firebase Function -> Function gọi OpenAI.
        3.  Trong Function, bạn kiểm tra (dùng Firebase Database) xem user này đã gọi bao nhiêu lần trong 24h. Nếu vượt quá, trả về lỗi "Bạn đã hết lượt".
    * **Cách đơn giản (Client-side - Kém an toàn):**
        1.  Dùng `SharedPreferences` để lưu lại số lần chat và thời gian.
        2.  `int chatCount = prefs.getInt("chat_count", 0);`
        3.  `long lastChatTime = prefs.getLong("last_chat_time", 0);`
        4.  Trước khi gửi, kiểm tra: Nếu `chatCount > 50` và `System.currentTimeMillis() - lastChatTime < 24_HOURS`, thì báo lỗi.
        5.  (Người dùng có thể xóa data app để reset, nên đây chỉ là giải pháp tạm thời).

### 5. Biometric Authentication ❌

* **Giải thích:** Cho phép người dùng dùng vân tay hoặc khuôn mặt để bảo vệ các khu vực nhạy cảm (ví dụ: xem lịch sử chat, đổi mật khẩu).
* **Hướng dẫn chi tiết:**
    1.  **Thêm thư viện:**
        ```kotlin
        // app/build.gradle.kts
        implementation("androidx.biometric:biometric:1.2.0-alpha05")
        ```
    2.  **Kiểm tra và hiển thị:**
        ```java
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt;
        BiometricPrompt.PromptInfo promptInfo;

        // 1. Kiểm tra xem có hỗ trợ không
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {

            // 2. Tạo callback
            biometricPrompt = new BiometricPrompt(YourActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    // THÀNH CÔNG! Cho phép người dùng vào
                }

                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    // Lỗi (ví dụ: nhấn "Cancel")
                }
            });

            // 3. Cấu hình popup
            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Xác thực vân tay")
                    .setSubtitle("Dùng vân tay để truy cập")
                    .setNegativeButtonText("Hủy")
                    .build();

            // 4. Hiển thị
            biometricPrompt.authenticate(promptInfo);

        } else {
            // Thiết bị không hỗ trợ vân tay/khuôn mặt
        }
        ```

### 6. API Key Security ⚠️

* **Giải thích:** Bạn **TUYỆT ĐỐI KHÔNG** được để OpenAI API Key trong code Java/Kotlin, kể cả trong `BuildConfig`. Hacker có thể dịch ngược file APK và lấy cắp key, khiến bạn tốn hàng ngàn đô la.
* **Hướng dẫn chi tiết:**
    * **Cách 1 (Tốt hơn): Cất trong NDK (C++).**
        1.  Cài NDK trong Android Studio.
        2.  Tạo file `native-lib.cpp`.
        3.  Lưu key trong file C++ đó (khó bị dịch ngược hơn).
        4.  Dùng JNI để gọi từ code Java.
    * **Cách 2 (Tốt nhất - Khuyến nghị): Dùng Backend Proxy.**
        1.  **Không bao giờ** đặt key trong app.
        2.  Tạo một **Firebase Function** (hoặc server riêng).
        3.  Lưu OpenAI API Key trong **biến môi trường** (environment variable) của Firebase Function đó.
        4.  App của bạn gọi Firebase Function (đã được xác thực bằng Firebase Auth).
        5.  Firebase Function nhận yêu cầu, *thêm API key bí mật vào*, rồi gọi OpenAI.
        6.  Kết quả từ OpenAI được trả về Function, rồi trả về app.
        7.  Bằng cách này, API key của bạn không bao li_giu_toc_do rời khỏi server.

---

Hy vọng tài liệu hướng dẫn chi tiết này sẽ giúp bạn hoàn thiện ứng dụng HealthTips. Chúc bạn sớm publish app thành công!