# 📱 HỆ THỐNG THÔNG BÁO HEALTHTIPS APP# 📱 HỆ THỐNG THÔNG BÁO HEALTHTIPS APP



> **Giải pháp đơn giản với Localhost Server**  > **Giải pháp đơn giản với Localhost Server**  

> Không cần deploy lên cloud, không cần lo về môi trường biến phức tạp!> Không cần deploy lên cloud, không cần lo về môi trường biến phức tạp!



------



## 📋 MỤC LỤC## 📋 MỤC LỤC

1. [Tổng quan hệ thống](#1-tổng-quan-hệ-thống)1. [Tổng quan hệ thống](#1-tổng-quan-hệ-thống)

2. [Cách hoạt động](#2-cách-hoạt-động)2. [Cách hoạt động](#2-cách-hoạt-động)

3. [Cài đặt Server Localhost](#3-cài-đặt-server-localhost)3. [Cài đặt Server Localhost](#3-cài-đặt-server-localhost)

4. [Tích hợp vào Android App](#4-tích-hợp-vào-android-app)4. [Tích hợp vào Android App](#4-tích-hợp-vào-android-app)

5. [Sử dụng hàng ngày](#5-sử-dụng-hàng-ngày)5. [Sử dụng hàng ngày](#5-sử-dụng-hàng-ngày)

6. [Khắc phục sự cố](#6-khắc-phục-sự-cố)6. [Khắc phục sự cố](#6-khắc-phục-sự-cố)



------



## 1. TỔNG QUAN HỆ THỐNG## 1. TỔNG QUAN



### 🎯 Mục tiêu### 1.1. Mục tiêu

Xây dựng hệ thống thông báo HealthTips App với 2 chức năng chính:Xây dựng hệ thống thông báo thông minh cho phép:

- ✅ Người dùng nhận thông báo khi có reply comment (Real-time)

1. **Tự động gửi thông báo khi có reply comment** ⚡- ✅ **Admin gửi thông báo bài viết quan trọng từ Admin Web riêng**

   - User A reply comment của User B- ✅ Hệ thống tự động đề xuất 1-2 bài viết theo sở thích user (18:00 hàng ngày)

   - Hệ thống tự động gửi notification cho User B- ✅ Khi click vào thông báo, app tự động điều hướng đến đúng nội dung

   - User B click thông báo → Mở app đến đúng comment đó- ✅ Trải nghiệm mượt mà tương tự TikTok



2. **Admin gửi thông báo từ Web Dashboard** 📢### 1.2. Các thành phần hệ thống

   - Admin vào trang web quản trị

   - Chọn loại thông báo (Broadcast, Recommendation, Custom)#### 🔷 **Android App (Repository này)**

   - Click gửi → Tất cả users nhận được- MyFirebaseMessagingService - Nhận và xử lý notifications

- DeepLinkHandlerActivity - Routing notifications

### 🏗️ Kiến trúc- NotificationPreferencesActivity - Cài đặt thông báo

- RecommendedTipsActivity - Hiển thị bài đề xuất

```

┌─────────────────────────────────────────────────────────────┐#### 🔷 **Admin Web (Trang riêng biệt - KHÔNG trong app)**

│                   ANDROID APP                               │- Giao diện web để admin đăng bài viết

│                                                             │- Form với checkbox "Gửi thông báo đến người dùng"

│  • User reply comment → Gọi API                             │- Code JavaScript gọi Vercel API

│  • Nhận FCM notification                                    │- Quản lý bài viết, categories

│  • Deep linking đến màn hình tương ứng                      │

└──────────────────┬──────────────────────────────────────────┘#### 🔷 **Backend - Vercel Serverless Functions**

                   │- **Repository:** https://github.com/vunameaut/healthtips-notifications-backend

                   ▼- **Production URL:** https://healthtips-notify.vercel.app

┌─────────────────────────────────────────────────────────────┐- **4 API Endpoints:**

│            LOCALHOST SERVER (Máy tính của bạn)              │  - `/api/send-comment-reply` - Gửi thông báo khi có comment mới

│                                                             │  - `/api/send-new-health-tip` - Gửi thông báo broadcast từ Admin

│  • URL: http://192.168.x.x:3000                             │  - `/api/queue-recommendation` - Thêm mẹo vào hàng đợi gợi ý

│  • Chạy khi cần: npm start                                  │  - `/api/send-daily-recommendations` - Gửi gợi ý hàng ngày (Cron: 18:00)

│  • Tắt khi không dùng                                       │

│                                                             │#### 🔷 **Firebase**

│  📂 4 API Endpoints:                                        │- Cloud Messaging (FCM) - Gửi notifications

│  ├─ POST /api/send-comment-reply                            │- Realtime Database - Lưu trữ data

│  ├─ POST /api/send-new-health-tip                           │- Authentication - Phân quyền admin

│  ├─ POST /api/send-recommendation                           │

│  └─ POST /api/send-custom-notification                      │### 1.3. Công nghệ sử dụng

└──────────────────┬──────────────────────────────────────────┘- **Firebase Cloud Messaging (FCM)**: Gửi push notifications

                   │- **Firebase Realtime Database**: Lưu trữ FCM tokens, preferences, queue

                   ▼- **Vercel Serverless Functions**: Backend API xử lý logic gửi notification

┌─────────────────────────────────────────────────────────────┐- **Cron-job.org**: Tự động chạy daily recommendations lúc 18:00

│              FIREBASE CLOUD MESSAGING                       │- **Admin Web (HTML/JavaScript/React/Vue)**: Trang web quản trị riêng

│                                                             │- **Deep Linking (Android)**: Điều hướng đến màn hình cụ thể trong app

│  • Server gửi FCM message                                   │- **Intent Extras**: Truyền dữ liệu giữa các Activity

│  • Firebase chuyển đến thiết bị Android                     │

└─────────────────────────────────────────────────────────────┘### 1.4. Yêu cầu kỹ thuật

                   │- Android API 26+ (Android 8.0+)

                   ▼- Firebase SDK đã được tích hợp

┌─────────────────────────────────────────────────────────────┐- Quyền POST_NOTIFICATIONS (Android 13+)

│                  WEB ADMIN (Vercel)                         │- Internet permission để gọi Vercel API

│                                                             │

│  • URL: https://admin-healthytip.vercel.app                 │---

│  • Gọi API server localhost khi gửi thông báo               │

│  • (Chỉ hoạt động khi server đang chạy)                     │## 2. KIẾN TRÚC HỆ THỐNG

└─────────────────────────────────────────────────────────────┘

```### 2.1. Sơ đồ tổng quan



### ✨ Ưu điểm giải pháp này```

┌──────────────────────────────────────────────────────────────────┐

✅ **Đơn giản**: Không cần deploy lên cloud  │                      ADMIN WEB (Riêng biệt)                      │

✅ **Miễn phí**: Không tốn tiền hosting  │  - Đăng bài viết mới                                             │

✅ **Dễ debug**: Chạy local, xem log trực tiếp  │  - Tick checkbox "Gửi thông báo"                                 │

✅ **Linh hoạt**: Chỉ chạy khi cần  │  - Gọi Vercel API: /api/send-new-health-tip                      │

✅ **Phù hợp**: Lý tưởng cho dự án sinh viên└────────────────────┬─────────────────────────────────────────────┘

                     │

### 📦 Công nghệ sử dụng                     ▼

┌──────────────────────────────────────────────────────────────────┐

- **Backend**: Node.js + Express.js│            VERCEL SERVERLESS FUNCTIONS                           │

- **Firebase**: Cloud Messaging (FCM) + Realtime Database│  URL: https://healthtips-notify.vercel.app                       │

- **Android**: Java + MVP Pattern│                                                                  │

- **Web Admin**: HTML/CSS/JavaScript (đã có sẵn trên Vercel)│  - /api/send-comment-reply (Gọi từ Android khi có comment)      │

│  - /api/send-new-health-tip (Gọi từ Admin Web)                  │

---│  - /api/queue-recommendation (Gọi từ Android khi tạo tip)       │

│  - /api/send-daily-recommendations (Cron Job 18:00)             │

## 2. CÁCH HOẠT ĐỘNG└────────────────────┬─────────────────────────────────────────────┘

                     │

### 🔄 Quy trình 1: Tự động thông báo Reply Comment                     ▼

┌──────────────────────────────────────────────────────────────────┐

```│              FIREBASE CLOUD MESSAGING (FCM)                      │

1. User A mở HealthTips App│  - Nhận data payload từ Vercel API                               │

   ↓│  - Phân phối đến thiết bị người dùng                             │

2. User A reply comment của User B└────────────────────┬─────────────────────────────────────────────┘

   ↓                     │

3. App gọi API: POST http://192.168.x.x:3000/api/send-comment-reply                     ▼

   Body: {┌──────────────────────────────────────────────────────────────────┐

     "healthTipId": "tip123",│         MyFirebaseMessagingService (APP CLIENT)                  │

     "commentContent": "Cảm ơn bạn đã chia sẻ!",│  - onMessageReceived(): Nhận FCM message                         │

     "recipientUserId": "userB_id"│  - handleDataPayload(): Xử lý data                               │

   }│  - showNotification(): Hiển thị thông báo                        │

   ↓└────────────────────┬─────────────────────────────────────────────┘

4. Server nhận request                     │

   ↓                     ▼

5. Server lấy FCM token của User B từ Firebase Database┌──────────────────────────────────────────────────────────────────┐

   ↓│              NOTIFICATION TAP (USER ACTION)                      │

6. Server gửi FCM message đến thiết bị User B│  - User nhấn vào notification                                   │

   ↓│  - PendingIntent được trigger                                   │

7. User B nhận notification: "User A đã trả lời bình luận của bạn"└────────────────────┬─────────────────────────────────────────────┘

   ↓                     │

8. User B click notification → App mở → Scroll đến comment đó                     ▼

```┌──────────────────────────────────────────────────────────────────┐

│              DEEP LINK HANDLER ACTIVITY                          │

### 📢 Quy trình 2: Admin gửi thông báo thủ công│  - Nhận Intent extras                                       │

│  - Parse notification type                                  │

```│  - Điều hướng đến màn hình đích                             │

1. Admin mở Web Admin: https://admin-healthytip.vercel.app└─────────────────────────────────────────────────────────────┘

   ↓                     │

2. Admin chọn loại thông báo:        ┌────────────┴────────────┐

   • Broadcast: Gửi cho tất cả users        ▼                         ▼

   • Recommendation: Đề xuất bài viết cho user cụ thể┌──────────────┐        ┌──────────────────┐

   • Custom: Thông báo tùy chỉnh│ VideoActivity│        │HealthTipDetail   │

   ↓│ + Comment    │        │   Activity       │

3. Admin điền nội dung và click "Gửi thông báo"│   Section    │        └──────────────────┘

   ↓└──────────────┘

4. Web Admin gọi API: POST http://192.168.x.x:3000/api/send-new-health-tip```

   ↓

5. Server nhận request### 2.2. Các thành phần chính

   ↓

6. Server lấy danh sách FCM tokens từ Firebase#### A. MyFirebaseMessagingService

   ↓**File:** `app/src/main/java/com/vhn/doan/services/MyFirebaseMessagingService.java`

7. Server gửi FCM messages đến tất cả thiết bị

   ↓**Trách nhiệm:**

8. Users nhận notification trên điện thoại- Nhận push notifications từ FCM

```- Xử lý data payload

- Hiển thị notification với PendingIntent phù hợp

### 🔑 Bảo mật API- Lưu FCM token vào Firebase Database



Tất cả API đều yêu cầu **API Secret Key** trong header:#### B. DeepLinkHandlerActivity (CẦN TẠO MỚI)

**Mục đích:** Activity trung gian để xử lý deep linking

```http

POST /api/send-comment-reply**Trách nhiệm:**

Headers:- Nhận Intent từ notification tap

  x-api-key: 3dc3c0be040e54ac5594a1e5eda411ace5eb137ee22c83a8f8cfa96092bf769d- Parse notification type và data

```- Điều hướng đến Activity/Fragment tương ứng

- Đóng chính nó sau khi điều hướng

Nếu thiếu key hoặc sai key → Server trả về lỗi **401 Unauthorized**.

#### C. Firebase Database Structure

---```

users/

## 3. CÀI ĐẶT SERVER LOCALHOST  {userId}/

    fcmToken: "fcm_token_string"

### 📥 Bước 1: Tải code về    

notifications/

Server đã được chuẩn bị sẵn trên GitHub:  {userId}/

    {notificationId}/

```bash      type: "comment_reply" | "new_post" | "new_video"

# Clone repository      videoId: "video123"

git clone https://github.com/vunameaut/healthtips-backend-server.git      commentId: "comment456"

      healthTipId: "tip789"

# Di chuyển vào thư mục      timestamp: 1699999999

cd healthtips-backend-server      read: false

      senderId: "user_abc"

# Cài đặt dependencies      senderName: "Nguyễn Văn A"

npm install      message: "đã trả lời bình luận của bạn"

``````



### 🔧 Bước 2: Cấu hình môi trường---



**File `.env` đã được chuẩn bị sẵn** với đầy đủ thông tin Firebase.## 3. CÁC LOẠI THÔNG BÁO



Bạn **KHÔNG CẦN** thay đổi gì, chỉ cần đảm bảo file `.env` tồn tại trong thư mục.### 📊 Tổng quan 3 loại notification:



### ▶️ Bước 3: Chạy server| Loại | Kích hoạt | Đối tượng | Thời điểm | Mục đích |

|------|-----------|-----------|-----------|----------|

```bash| **Comment Reply** | User reply comment | User bị reply | Ngay lập tức | Tương tác real-time |

npm start| **Admin Broadcast** | Admin tick checkbox | Tất cả users | Ngay lập tức | Thông báo quan trọng |

```| **Smart Recommendations** | Tự động | Theo sở thích user | 18:00 hàng ngày | Đề xuất cá nhân hóa |



**Output khi thành công:**---



```### 3.1. Comment Reply Notification

🚀 HealthTips Notification Server Starting...

**Kịch bản:**

✅ Firebase initialized successfully1. User A comment video của User B

   Project: reminderwater-846942. User C reply vào comment của User A

   Database: https://reminderwater-84694-default-rtdb...3. User A nhận notification

4. User A click notification → Mở video + scroll đến comment được reply

🔒 Security: API_SECRET_KEY protection enabled

**Data payload:**

📍 Server running on:```json

   Local:   http://localhost:3000{

   Network: http://192.168.1.100:3000  "type": "comment_reply",

  "videoId": "video123",

📋 Available endpoints:  "parentCommentId": "comment_of_user_a",

   GET  /api/health                    - Health check  "replyCommentId": "new_reply_comment",

   GET  /api/test-firebase             - Test Firebase connection  "senderId": "user_c_id",

   POST /api/send-comment-reply        - Send reply notification  "senderName": "User C",

   POST /api/send-new-health-tip       - Broadcast notification  "senderAvatar": "https://...",

   POST /api/send-recommendation       - Personal recommendation  "videoTitle": "Mẹo sức khỏe ABC",

   POST /api/send-custom-notification  - Custom message  "replyText": "Cảm ơn bạn đã chia sẻ!",

```  "timestamp": 1699999999

}

### 📱 Bước 4: Lấy địa chỉ IP của máy```



**Windows:**### 3.2. New Health Tip Notification (Admin Control)



```bash**⚠️ Admin tự quyết định có gửi notification hay không:**

ipconfig

```**Khi đăng bài viết, Admin có 2 lựa chọn:**

- ✅ **Gửi thông báo đến tất cả người dùng** (Checkbox trong form đăng bài)

Tìm dòng **IPv4 Address** (ví dụ: `192.168.1.100`)- ❌ **Không gửi thông báo** (Đăng bình thường)



**macOS/Linux:****Kịch bản:**

1. Admin đăng bài viết sức khỏe mới

```bash2. Admin tick checkbox: "Gửi thông báo đến người dùng"

ifconfig | grep "inet "3. Khi submit, app gọi Cloud Function để gửi notification

```4. Users nhận notification → Click → Mở bài viết



### ✅ Bước 5: Test server**Data payload:**

```json

Mở trình duyệt, truy cập:{

  "type": "new_health_tip",

```  "healthTipId": "tip789",

http://localhost:3000/api/health  "title": "10 mẹo giữ sức khỏe mùa đông",

```  "categoryId": "category_123",

  "categoryName": "Dinh dưỡng",

Hoặc từ điện thoại (cùng WiFi):  "imageUrl": "https://...",

  "timestamp": 1699999999,

```  "sendNotification": true,  // Admin chọn

http://192.168.1.100:3000/api/health  "broadcastType": "admin"   // Phân biệt với recommendation

```}

```

**Response thành công:**

### 3.3. Smart Health Tip Recommendations

```json

{**🎯 Hệ thống đề xuất thông minh:**

  "status": "OK",

  "message": "HealthTips Notification Server is running",**Chiến lược:**

  "timestamp": "2025-11-11T10:30:45.123Z",- ✅ **Tự động chạy mỗi ngày lúc 18:00**

  "firebase": "connected"- ✅ **Chọn 1-2 bài viết mới phù hợp sở thích của mỗi user**

}- ✅ **Dựa trên categories user quan tâm** (user preferences)

```- ✅ **Không gửi nếu không có bài mới phù hợp**



---**Kịch bản:**

1. **10:00 sáng:** Admin đăng 5 bài viết mới (không tick gửi notification)

## 4. TÍCH HỢP VÀO ANDROID APP   - Bài 1: Dinh dưỡng

   - Bài 2: Thể dục

### 🔧 Bước 1: Thêm API Helper Class   - Bài 3: Sức khỏe tinh thần

   - Bài 4: Dinh dưỡng

Tạo file **`VercelApiHelper.java`** trong package `utils`:   - Bài 5: Yoga



```java2. **18:00 chiều:** Scheduled Function tự động chạy

package com.vhn.doan.utils;   - User A (thích Dinh dưỡng + Thể dục) → Nhận 2 bài: Bài 1 + Bài 2

   - User B (thích Yoga) → Nhận 1 bài: Bài 5

import okhttp3.*;   - User C (thích Làm đẹp) → Không nhận gì (không có bài phù hợp)

import org.json.JSONObject;

import java.io.IOException;3. **User click notification** → Mở bài viết được đề xuất



public class VercelApiHelper {**Data payload:**

    ```json

    // ⚠️ THAY ĐỔI IP NÀY THÀNH IP MÁY CỦA BẠN{

    private static final String BASE_URL = "http://192.168.1.100:3000";  "type": "health_tip_recommendation",

      "recommendationType": "daily_picks",

    private static final String API_KEY = "3dc3c0be040e54ac5594a1e5eda411ace5eb137ee22c83a8f8cfa96092bf769d";  "tips": [

        {

    private static final OkHttpClient client = new OkHttpClient();      "healthTipId": "tip789",

          "title": "10 mẹo giữ sức khỏe mùa đông",

    /**      "categoryId": "category_123",

     * Gửi thông báo khi có reply comment      "categoryName": "Dinh dưỡng"

     */    },

    public static void sendCommentReplyNotification(    {

        String healthTipId,      "healthTipId": "tip790",

        String commentContent,      "title": "Bài tập buổi sáng hiệu quả",

        String recipientUserId,      "categoryId": "category_456",

        Callback callback      "categoryName": "Thể dục"

    ) {    }

        try {  ],

            JSONObject json = new JSONObject();  "tipsCount": 2,

            json.put("healthTipId", healthTipId);  "timestamp": 1699999999,

            json.put("commentContent", commentContent);  "title": "📚 2 bài viết mới dành cho bạn",

            json.put("recipientUserId", recipientUserId);  "body": "10 mẹo giữ sức khỏe mùa đông và 1 bài viết khác"

            }

            RequestBody body = RequestBody.create(```

                json.toString(),

                MediaType.parse("application/json")### 3.3. New Video Notification

            );

            **Kịch bản:**

            Request request = new Request.Builder()1. Admin đăng video mới

                .url(BASE_URL + "/api/send-comment-reply")2. Users quan tâm nhận notification

                .post(body)3. User click notification → Mở VideoActivity với video cụ thể

                .addHeader("x-api-key", API_KEY)

                .addHeader("Content-Type", "application/json")**Data payload:**

                .build();```json

            {

            client.newCall(request).enqueue(callback);  "type": "new_video",

              "videoId": "video456",

        } catch (Exception e) {  "title": "Bài tập yoga buổi sáng",

            e.printStackTrace();  "thumbnailUrl": "https://...",

        }  "duration": 300,

    }  "timestamp": 1699999999

}}

``````



### 📲 Bước 2: Gọi API khi User Reply Comment### 3.4. Comment Like Notification



Trong Activity/Fragment xử lý reply comment:**Kịch bản:**

1. User A comment video

```java2. User B like comment của User A

// Khi user nhấn nút "Gửi reply"3. User A nhận notification

btnSendReply.setOnClickListener(v -> {

    String replyContent = edtReply.getText().toString();**Data payload:**

    ```json

    // 1. Lưu reply vào Firebase Database (code hiện tại){

    saveReplyToFirebase(replyContent);  "type": "comment_like",

      "videoId": "video123",

    // 2. GỬI THÔNG BÁO (MỚI THÊM)  "commentId": "comment_of_user_a",

    VercelApiHelper.sendCommentReplyNotification(  "senderId": "user_b_id",

        healthTipId,          // ID của bài viết  "senderName": "User B",

        replyContent,         // Nội dung reply  "timestamp": 1699999999

        originalCommenterId,  // ID của người bị reply}

        new Callback() {```

            @Override

            public void onResponse(Call call, Response response) {---

                // Thông báo đã được gửi thành công

                Log.d("Notification", "Sent successfully");## 4. CẤU TRÚC DATA PAYLOAD

            }

            ### 4.1. Notification Message Format

            @Override

            public void onFailure(Call call, IOException e) {FCM hỗ trợ 2 loại payload:

                // Lỗi khi gửi thông báo (nhưng reply vẫn được lưu)- **Notification payload**: Tự động hiển thị bởi hệ thống (Limited control)

                Log.e("Notification", "Failed: " + e.getMessage());- **Data payload**: Full control, xử lý trong app

            }

        }**➡️ Chúng ta sử dụng Data payload để có full control**

    );

});### 4.2. Common Fields (Tất cả notifications)

```

```json

### 🔔 Bước 3: Xử lý khi nhận Notification{

  "type": "comment_reply | new_health_tip | new_video | comment_like",

**MyFirebaseMessagingService** đã được triển khai để nhận FCM messages.  "notification_id": "unique_id",

  "timestamp": 1699999999,

Khi user click notification, app sẽ mở và điều hướng đến đúng màn hình.  "title": "Tiêu đề notification",

  "body": "Nội dung notification",

---  "icon": "ic_notification_icon",

  "sound": "default",

## 5. SỬ DỤNG HÀNG NGÀY  "priority": "high",

  "click_action": "OPEN_DEEP_LINK"

### 💡 Kịch bản 1: Test chức năng Reply Comment}

```

```

1️⃣ Bật server trên máy tính:### 4.3. Type-Specific Fields

   cd D:\app\healthtips-backend-server

   npm start#### Comment Reply:

```json

2️⃣ Đảm bảo điện thoại và máy tính cùng WiFi{

  "videoId": "string",

3️⃣ Mở HealthTips App trên điện thoại  "parentCommentId": "string",

  "replyCommentId": "string",

4️⃣ Reply một comment bất kỳ  "senderId": "string",

  "senderName": "string",

5️⃣ Người bị reply sẽ nhận notification ngay lập tức  "senderAvatar": "url",

  "replyText": "string"

6️⃣ Click notification → App mở đến đúng comment đó}

``````



### 📢 Kịch bản 2: Admin gửi thông báo#### New Health Tip:

```json

```{

1️⃣ Bật server trên máy tính:  "healthTipId": "string",

   npm start  "categoryId": "string",

  "categoryName": "string",

2️⃣ Mở Web Admin:  "imageUrl": "url"

   https://admin-healthytip.vercel.app}

```

3️⃣ Đăng nhập với tài khoản admin

#### New Video:

4️⃣ Vào mục "Gửi thông báo"```json

{

5️⃣ Chọn loại thông báo và điền nội dung  "videoId": "string",

  "thumbnailUrl": "url",

6️⃣ Click "Gửi" → Tất cả users nhận được  "duration": number

}

7️⃣ Sau khi gửi xong, có thể tắt server```

```

---

### 🛑 Khi nào cần chạy server?

## 5. TRIỂN KHAI CHI TIẾT

✅ **CẦN chạy:**

- Khi muốn test chức năng reply comment### 5.1. Bước 1: Cập nhật MyFirebaseMessagingService

- Khi admin cần gửi thông báo từ web

**File:** `MyFirebaseMessagingService.java`

❌ **KHÔNG CẦN chạy:**

- Khi chỉ sử dụng app bình thường```java

- Khi không ai reply commentpackage com.vhn.doan.services;

- Khi admin không gửi thông báo

import android.app.NotificationChannel;

---import android.app.NotificationManager;

import android.app.PendingIntent;

## 6. KHẮC PHỤC SỰ CỐimport android.content.Intent;

import android.os.Build;

### ❌ Lỗi 1: "Cannot connect to server"import android.util.Log;



**Nguyên nhân:** Server chưa chạy hoặc IP saiimport androidx.annotation.NonNull;

import androidx.core.app.NotificationCompat;

**Giải pháp:**

1. Kiểm tra server đang chạy: `npm start`import com.google.firebase.auth.FirebaseAuth;

2. Kiểm tra IP máy: `ipconfig` (Windows) hoặc `ifconfig` (Mac/Linux)import com.google.firebase.database.FirebaseDatabase;

3. Cập nhật IP trong `VercelApiHelper.java`:import com.google.firebase.messaging.FirebaseMessagingService;

   ```javaimport com.google.firebase.messaging.RemoteMessage;

   private static final String BASE_URL = "http://192.168.X.X:3000";import com.vhn.doan.R;

   ```

4. Rebuild app Androidimport java.util.Map;



### ❌ Lỗi 2: "401 Unauthorized"public class MyFirebaseMessagingService extends FirebaseMessagingService {



**Nguyên nhân:** API key sai hoặc thiếu    private static final String TAG = "FCMService";

    private static final String CHANNEL_ID = "health_tips_notifications";

**Giải pháp:**    

1. Kiểm tra API_KEY trong `VercelApiHelper.java`    // Notification types

2. Phải đúng: `3dc3c0be040e54ac5594a1e5eda411ace5eb137ee22c83a8f8cfa96092bf769d`    public static final String TYPE_COMMENT_REPLY = "comment_reply";

    public static final String TYPE_NEW_HEALTH_TIP = "new_health_tip";

### ❌ Lỗi 3: "Không nhận được notification"    public static final String TYPE_NEW_VIDEO = "new_video";

    public static final String TYPE_COMMENT_LIKE = "comment_like";

**Nguyên nhân:** FCM token chưa được lưu hoặc Firebase chưa cấu hình đúng

    @Override

**Giải pháp:**    public void onCreate() {

1. Kiểm tra FCM token đã được lưu vào Firebase Database:        super.onCreate();

   ```        createNotificationChannel();

   Firebase Console → Realtime Database → users/{userId}/fcmToken    }

   ```

2. Kiểm tra file `google-services.json` trong app    @Override

3. Test Firebase connection:    public void onNewToken(@NonNull String token) {

   ```        super.onNewToken(token);

   http://192.168.X.X:3000/api/test-firebase        Log.d(TAG, "New FCM Token: " + token);

   ```        

        // Lưu token vào Firebase Database

### ❌ Lỗi 4: "Web Admin không gọi được localhost"        saveFCMTokenToDatabase(token);

    }

**Nguyên nhân:** Web Admin chạy trên Vercel (cloud), không thể truy cập máy cá nhân

    @Override

**Giải pháp 2 phương án:**    public void onMessageReceived(@NonNull RemoteMessage message) {

        super.onMessageReceived(message);

**Phương án A - Dùng Ngrok (Khuyến nghị):**        

```bash        Log.d(TAG, "Message received from: " + message.getFrom());

# 1. Tải Ngrok: https://ngrok.com/download

        // Xử lý data payload

# 2. Chạy server localhost        if (!message.getData().isEmpty()) {

npm start            Map<String, String> data = message.getData();

            handleNotificationData(data);

# 3. Chạy ngrok trong terminal mới        }

ngrok http 3000    }



# 4. Ngrok sẽ tạo URL công khai:    /**

https://abc123.ngrok-free.app     * Xử lý data payload và hiển thị notification phù hợp

     */

# 5. Cập nhật URL này trong Web Admin    private void handleNotificationData(Map<String, String> data) {

```        String type = data.get("type");

        String title = data.get("title");

**Phương án B - Chạy Web Admin ở localhost:**        String body = data.get("body");

```bash        

# Clone Web Admin về máy        if (type == null || title == null || body == null) {

git clone https://github.com/vunameaut/admin-healthytip.git            Log.w(TAG, "Invalid notification data");

            return;

# Mở file index.html trực tiếp        }

# Hoặc chạy local server:

npx serve .        Intent intent = createDeepLinkIntent(type, data);

```        showNotification(title, body, intent);

    }

### 📞 Liên hệ hỗ trợ

    /**

Nếu gặp vấn đề không giải quyết được, vui lòng:     * Tạo Intent cho deep linking dựa trên notification type

1. Kiểm tra log server: Terminal nơi chạy `npm start`     */

2. Kiểm tra Logcat Android Studio    private Intent createDeepLinkIntent(String type, Map<String, String> data) {

3. Kiểm tra Firebase Console → Realtime Database        Intent intent = new Intent(this, DeepLinkHandlerActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

---        

        // Thêm notification type

## 📚 TÀI LIỆU THAM KHẢO        intent.putExtra("notification_type", type);

        

### 📂 Code Repository        // Thêm data tùy theo type

        switch (type) {

- **Backend Server:** https://github.com/vunameaut/healthtips-backend-server            case TYPE_COMMENT_REPLY:

- **Android App:** https://github.com/vunameaut/HealthTips-App-                intent.putExtra("video_id", data.get("videoId"));

- **Web Admin:** https://admin-healthytip.vercel.app                intent.putExtra("parent_comment_id", data.get("parentCommentId"));

                intent.putExtra("reply_comment_id", data.get("replyCommentId"));

### 📖 Tài liệu kỹ thuật                intent.putExtra("sender_name", data.get("senderName"));

                intent.putExtra("reply_text", data.get("replyText"));

- **Firebase Cloud Messaging:** https://firebase.google.com/docs/cloud-messaging                break;

- **Express.js Documentation:** https://expressjs.com/                

- **OkHttp Android:** https://square.github.io/okhttp/            case TYPE_NEW_HEALTH_TIP:

                intent.putExtra("health_tip_id", data.get("healthTipId"));

---                intent.putExtra("category_id", data.get("categoryId"));

                break;

## ✅ CHECKLIST TRIỂN KHAI                

            case TYPE_NEW_VIDEO:

- [ ] Clone backend server về máy                intent.putExtra("video_id", data.get("videoId"));

- [ ] Cài đặt Node.js và npm                break;

- [ ] Chạy `npm install` trong thư mục server                

- [ ] Chạy `npm start` để test server            case TYPE_COMMENT_LIKE:

- [ ] Kiểm tra IP máy bằng `ipconfig`                intent.putExtra("video_id", data.get("videoId"));

- [ ] Tạo `VercelApiHelper.java` trong Android app                intent.putExtra("comment_id", data.get("commentId"));

- [ ] Cập nhật BASE_URL với IP máy                intent.putExtra("sender_name", data.get("senderName"));

- [ ] Thêm code gọi API khi reply comment                break;

- [ ] Test chức năng reply và nhận notification        }

- [ ] (Tùy chọn) Cài đặt Ngrok nếu cần access từ xa        

- [ ] Cập nhật Web Admin để gọi localhost hoặc ngrok URL        return intent;

    }

---

    /**

**🎉 Chúc bạn triển khai thành công!**     * Hiển thị notification với PendingIntent

     */

_Hệ thống localhost đơn giản, dễ debug, phù hợp cho dự án sinh viên._    private void showNotification(String title, String body, Intent intent) {

        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            (int) System.currentTimeMillis(),
            intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(body));

        NotificationManager notificationManager = 
            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, builder.build());
        }
    }

    /**
     * Tạo Notification Channel (Required cho Android 8.0+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Thông báo HealthTips",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Nhận thông báo về bình luận, bài viết mới");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Lưu FCM token vào Firebase Database
     */
    private void saveFCMTokenToDatabase(String token) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null 
            ? FirebaseAuth.getInstance().getCurrentUser().getUid() 
            : null;
        
        if (userId != null) {
            FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("fcmToken")
                .setValue(token)
                .addOnSuccessListener(aVoid -> 
                    Log.d(TAG, "FCM token saved successfully"))
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Failed to save FCM token", e));
        }
    }
}
```

### 5.2. Bước 2: Tạo DeepLinkHandlerActivity

**File:** `DeepLinkHandlerActivity.java`
**Location:** `app/src/main/java/com/vhn/doan/presentation/deeplink/`

```java
package com.vhn.doan.presentation.deeplink;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.vhn.doan.presentation.healthtip.detail.HealthTipDetailActivity;
import com.vhn.doan.presentation.video.SingleVideoPlayerActivity;
import com.vhn.doan.services.MyFirebaseMessagingService;

/**
 * Activity trung gian để xử lý deep linking từ notifications
 * Activity này sẽ parse notification data và điều hướng đến màn hình phù hợp
 */
public class DeepLinkHandlerActivity extends AppCompatActivity {

    private static final String TAG = "DeepLinkHandler";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Không cần setContentView vì đây là transparent activity
        
        Intent receivedIntent = getIntent();
        if (receivedIntent != null) {
            handleDeepLink(receivedIntent);
        } else {
            finish();
        }
    }

    /**
     * Xử lý deep link dựa trên notification type
     */
    private void handleDeepLink(Intent intent) {
        String notificationType = intent.getStringExtra("notification_type");
        
        if (notificationType == null) {
            Log.w(TAG, "No notification type found");
            finish();
            return;
        }

        Log.d(TAG, "Handling deep link for type: " + notificationType);

        switch (notificationType) {
            case MyFirebaseMessagingService.TYPE_COMMENT_REPLY:
                handleCommentReplyNotification(intent);
                break;
                
            case MyFirebaseMessagingService.TYPE_NEW_HEALTH_TIP:
                handleNewHealthTipNotification(intent);
                break;
                
            case MyFirebaseMessagingService.TYPE_NEW_VIDEO:
                handleNewVideoNotification(intent);
                break;
                
            case MyFirebaseMessagingService.TYPE_COMMENT_LIKE:
                handleCommentLikeNotification(intent);
                break;
                
            default:
                Log.w(TAG, "Unknown notification type: " + notificationType);
                finish();
        }
    }

    /**
     * Xử lý thông báo reply comment
     * Mở video và scroll đến comment được reply
     */
    private void handleCommentReplyNotification(Intent sourceIntent) {
        String videoId = sourceIntent.getStringExtra("video_id");
        String parentCommentId = sourceIntent.getStringExtra("parent_comment_id");
        String replyCommentId = sourceIntent.getStringExtra("reply_comment_id");
        
        if (videoId == null) {
            Log.w(TAG, "Missing video_id for comment reply");
            finish();
            return;
        }

        // Tạo Intent để mở SingleVideoPlayerActivity
        Intent videoIntent = new Intent(this, SingleVideoPlayerActivity.class);
        videoIntent.putExtra("video_id", videoId);
        videoIntent.putExtra("open_comments", true); // Flag để tự động mở comments
        videoIntent.putExtra("scroll_to_comment", parentCommentId); // Scroll đến comment
        videoIntent.putExtra("highlight_reply", replyCommentId); // Highlight reply mới
        videoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        startActivity(videoIntent);
        finish();
    }

    /**
     * Xử lý thông báo bài viết sức khỏe mới
     */
    private void handleNewHealthTipNotification(Intent sourceIntent) {
        String healthTipId = sourceIntent.getStringExtra("health_tip_id");
        
        if (healthTipId == null) {
            Log.w(TAG, "Missing health_tip_id");
            finish();
            return;
        }

        // Tạo Intent để mở HealthTipDetailActivity
        Intent detailIntent = new Intent(this, HealthTipDetailActivity.class);
        detailIntent.putExtra("health_tip_id", healthTipId);
        detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        startActivity(detailIntent);
        finish();
    }

    /**
     * Xử lý thông báo recommendations (1-2 bài được đề xuất)
     */
    private void handleHealthTipRecommendation(Intent sourceIntent) {
        String tipsJson = sourceIntent.getStringExtra("tips");
        
        if (tipsJson == null) {
            Log.w(TAG, "Missing tips data");
            finish();
            return;
        }

        try {
            // Parse JSON array của tips
            JSONArray tipsArray = new JSONArray(tipsJson);
            
            if (tipsArray.length() == 1) {
                // Nếu chỉ 1 bài → Mở luôn detail
                JSONObject tip = tipsArray.getJSONObject(0);
                String tipId = tip.getString("tipId");
                
                Intent detailIntent = new Intent(this, HealthTipDetailActivity.class);
                detailIntent.putExtra("health_tip_id", tipId);
                detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                
                startActivity(detailIntent);
            } else {
                // Nếu 2 bài → Mở danh sách recommendations
                Intent listIntent = new Intent(this, RecommendedTipsActivity.class);
                listIntent.putExtra("tips_json", tipsJson);
                listIntent.putExtra("title", "Bài viết đề xuất cho bạn");
                listIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                
                startActivity(listIntent);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing tips JSON", e);
        }
        
        finish();
    }

    /**
     * Xử lý thông báo video mới
     */
    private void handleNewVideoNotification(Intent sourceIntent) {
        String videoId = sourceIntent.getStringExtra("video_id");
        
        if (videoId == null) {
            Log.w(TAG, "Missing video_id");
            finish();
            return;
        }

        // Tạo Intent để mở SingleVideoPlayerActivity
        Intent videoIntent = new Intent(this, SingleVideoPlayerActivity.class);
        videoIntent.putExtra("video_id", videoId);
        videoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        startActivity(videoIntent);
        finish();
    }

    /**
     * Xử lý thông báo like comment
     */
    private void handleCommentLikeNotification(Intent sourceIntent) {
        String videoId = sourceIntent.getStringExtra("video_id");
        String commentId = sourceIntent.getStringExtra("comment_id");
        
        if (videoId == null || commentId == null) {
            Log.w(TAG, "Missing data for comment like notification");
            finish();
            return;
        }

        // Mở video và highlight comment được like
        Intent videoIntent = new Intent(this, SingleVideoPlayerActivity.class);
        videoIntent.putExtra("video_id", videoId);
        videoIntent.putExtra("open_comments", true);
        videoIntent.putExtra("scroll_to_comment", commentId);
        videoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        startActivity(videoIntent);
        finish();
    }
}
```

### 5.3. Bước 3: Cập nhật AndroidManifest.xml

```xml
<!-- Thêm DeepLinkHandlerActivity -->
<activity
    android:name=".presentation.deeplink.DeepLinkHandlerActivity"
    android:exported="true"
    android:theme="@android:style/Theme.Translucent.NoTitleBar"
    android:launchMode="singleTask"
    android:taskAffinity="">
    <!-- Theme transparent để user không thấy activity này -->
</activity>

<!-- Đảm bảo MyFirebaseMessagingService đã được đăng ký -->
<service
    android:name=".services.MyFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

### 5.4. Bước 4: Cập nhật SingleVideoPlayerActivity

**Thêm logic để xử lý Intent extras:**

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_single_video_player);
    
    // ... existing code ...
    
    // Xử lý deep link từ notification
    handleDeepLinkExtras();
}

/**
 * Xử lý extras từ deep link notification
 */
private void handleDeepLinkExtras() {
    Intent intent = getIntent();
    
    // Kiểm tra xem có yêu cầu mở comments không
    boolean shouldOpenComments = intent.getBooleanExtra("open_comments", false);
    String scrollToCommentId = intent.getStringExtra("scroll_to_comment");
    String highlightReplyId = intent.getStringExtra("highlight_reply");
    
    if (shouldOpenComments) {
        // Delay một chút để video load xong
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            openCommentsWithScroll(scrollToCommentId, highlightReplyId);
        }, 500);
    }
}

/**
 * Mở comments bottom sheet và scroll đến comment cụ thể
 */
private void openCommentsWithScroll(String commentId, String highlightReplyId) {
    CommentBottomSheetFragment commentSheet = 
        CommentBottomSheetFragment.newInstance(currentVideoId);
    
    // Truyền thông tin scroll và highlight
    Bundle args = new Bundle();
    if (commentId != null) {
        args.putString("scroll_to_comment_id", commentId);
    }
    if (highlightReplyId != null) {
        args.putString("highlight_reply_id", highlightReplyId);
    }
    commentSheet.setArguments(args);
    
    commentSheet.show(getSupportFragmentManager(), "CommentBottomSheet");
}
```

### 5.5. Bước 5: Cập nhật CommentBottomSheetFragment

**Thêm logic scroll và highlight:**

```java
@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    
    // ... existing code ...
    
    // Xử lý scroll từ deep link
    handleScrollToComment();
}

/**
 * Scroll đến comment cụ thể từ notification
 */
private void handleScrollToComment() {
    Bundle args = getArguments();
    if (args == null) return;
    
    String scrollToCommentId = args.getString("scroll_to_comment_id");
    String highlightReplyId = args.getString("highlight_reply_id");
    
    if (scrollToCommentId != null) {
        // Delay để đảm bảo RecyclerView đã load xong
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            scrollToCommentAndExpand(scrollToCommentId, highlightReplyId);
        }, 300);
    }
}

/**
 * Scroll đến comment và expand replies nếu cần
 */
private void scrollToCommentAndExpand(String commentId, String highlightReplyId) {
    // Tìm position của comment
    int position = findCommentPosition(commentId);
    
    if (position != -1) {
        // Scroll đến comment
        commentsRecyclerView.smoothScrollToPosition(position);
        
        // Nếu có reply cần highlight, expand replies
        if (highlightReplyId != null) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                commentAdapter.expandReplies(commentId);
                commentAdapter.highlightReply(highlightReplyId);
            }, 500);
        }
    }
}

/**
 * Tìm position của comment trong adapter
 */
private int findCommentPosition(String commentId) {
    for (int i = 0; i < commentAdapter.getItemCount(); i++) {
        VideoComment comment = commentAdapter.getCommentAt(i);
        if (comment != null && comment.getId().equals(commentId)) {
            return i;
        }
    }
    return -1;
}
```

### 5.6. Bước 6: Server-side Code (Cloud Functions)

## ⚡ QUAN TRỌNG: Firebase Database Triggers - KHÔNG CẦN WEBHOOK!

**Firebase Realtime Database Triggers hoạt động như sau:**
1. **Tự động lắng nghe** mọi thay đổi trong database
2. **Trigger Cloud Function** ngay lập tức khi có data mới
3. **Không cần API endpoint** hay webhook bên ngoài
4. **Real-time** - Độ trễ chỉ vài trăm milliseconds

**Ví dụ:** Khi User A reply comment của User B:
```
User A gửi reply → Firebase Realtime Database nhận data mới
    ↓ (Tự động trigger - KHÔNG CẦN CODE THÊM)
Cloud Function onCreate() được gọi NGAY LẬP TỨC
    ↓
Gửi FCM notification đến User B
    ↓
User B nhận notification trong < 1 giây
```

**✅ Ưu điểm:**
- Hoàn toàn tự động, không cần setup webhook
- Real-time, độ trễ thấp
- Scalable, Firebase tự động scale
- Miễn phí cho Spark plan (giới hạn 125K invocations/tháng)
- Có retry mechanism tự động

---

**Gửi notification khi có reply comment:**

```javascript
// Cloud Function để gửi notification khi có reply
// ⚡ Trigger TỰ ĐỘNG khi có comment mới được tạo
exports.sendCommentReplyNotification = functions.database
    .ref('/videos/{videoId}/comments/{commentId}')
    .onCreate(async (snapshot, context) => {
        const reply = snapshot.val();
        const videoId = context.params.videoId;
        const commentId = context.params.commentId;
        
        // Kiểm tra xem đây có phải là reply không
        if (!reply.parentId) {
            return null; // Không phải reply, bỏ qua
        }
        
        // Lấy thông tin parent comment để biết ai cần nhận notification
        const parentCommentSnapshot = await admin.database()
            .ref(`/videos/${videoId}/comments/${reply.parentId}`)
            .once('value');
        
        const parentComment = parentCommentSnapshot.val();
        if (!parentComment) {
            return null;
        }
        
        const recipientUserId = parentComment.userId; // Người nhận notification
        const senderUserId = reply.userId; // Người gửi reply
        
        // Không gửi notification cho chính mình
        if (recipientUserId === senderUserId) {
            return null;
        }
        
        // Lấy FCM token của recipient
        const userSnapshot = await admin.database()
            .ref(`/users/${recipientUserId}`)
            .once('value');
        
        const user = userSnapshot.val();
        if (!user || !user.fcmToken) {
            console.log('User has no FCM token');
            return null;
        }
        
        // Lấy thông tin người gửi
        const senderSnapshot = await admin.database()
            .ref(`/users/${senderUserId}`)
            .once('value');
        
        const sender = senderSnapshot.val();
        const senderName = sender?.displayName || 'Người dùng';
        
        // Tạo notification payload
        const payload = {
            data: {
                type: 'comment_reply',
                videoId: videoId,
                parentCommentId: reply.parentId,
                replyCommentId: commentId,
                senderId: senderUserId,
                senderName: senderName,
                senderAvatar: sender?.photoURL || '',
                replyText: reply.text,
                timestamp: Date.now().toString(),
                title: 'Trả lời bình luận',
                body: `${senderName} đã trả lời bình luận của bạn: "${reply.text}"`
            }
        };
        
        // Gửi notification
        try {
            await admin.messaging().sendToDevice(user.fcmToken, payload, {
                priority: 'high',
                timeToLive: 60 * 60 * 24 // 24 hours
            });
            
            // Lưu notification vào database để tracking
            await admin.database()
                .ref(`/notifications/${recipientUserId}`)
                .push({
                    ...payload.data,
                    read: false,
                    createdAt: admin.database.ServerValue.TIMESTAMP
                });
            
            console.log('Comment reply notification sent successfully');
        } catch (error) {
            console.error('Error sending notification:', error);
        }
        
        return null;
    });

// ==================== NEW HEALTH TIP NOTIFICATION (ADMIN CONTROL) ====================

/**
 * Gửi notification bài viết mới (khi Admin chọn)
 * Call function này từ Admin app/web khi đăng bài
 */
exports.sendNewHealthTipNotification = functions.https.onCall(async (data, context) => {
    // Kiểm tra quyền admin
    if (!context.auth || !context.auth.token.admin) {
        throw new functions.https.HttpsError(
            'permission-denied',
            'Chỉ admin mới có thể gửi notification'
        );
    }
    
    const { healthTipId, title, categoryId, imageUrl } = data;
    
    if (!healthTipId || !title) {
        throw new functions.https.HttpsError(
            'invalid-argument',
            'Missing required fields'
        );
    }
    
    console.log('Admin triggered notification for health tip:', healthTipId);
    
    try {
        // Lấy tất cả users có bật notification
        const usersSnapshot = await admin.database()
            .ref('/users')
            .once('value');
        
        const users = usersSnapshot.val();
        const tokens = [];
        
        if (users) {
            Object.keys(users).forEach(userId => {
                const user = users[userId];
                // Kiểm tra preferences
                if (user.fcmToken && 
                    user.notification_preferences?.new_posts !== false) {
                    tokens.push(user.fcmToken);
                }
            });
        }
        
        if (tokens.length === 0) {
            console.log('No users to notify');
            return { success: true, sentCount: 0 };
        }
        
        const payload = {
            data: {
                type: 'new_health_tip',
                healthTipId: healthTipId,
                categoryId: categoryId || '',
                imageUrl: imageUrl || '',
                timestamp: Date.now().toString(),
                broadcastType: 'admin',
                title: '📢 Bài viết mới',
                body: title
            }
        };
        
        // Gửi notification
        const response = await admin.messaging().sendToDevice(tokens, payload, {
            priority: 'high',
            timeToLive: 60 * 60 * 24
        });
        
        console.log('Notification sent to', tokens.length, 'users');
        console.log('Success count:', response.successCount);
        console.log('Failure count:', response.failureCount);
        
        return { 
            success: true, 
            sentCount: response.successCount,
            failureCount: response.failureCount
        };
        
    } catch (error) {
        console.error('Error sending notification:', error);
        throw new functions.https.HttpsError('internal', error.message);
    }
});

// ==================== SMART RECOMMENDATIONS ====================

/**
 * Lưu bài viết mới vào recommendation queue
 */
exports.queueHealthTipForRecommendation = functions.database
    .ref('/health_tips/{tipId}')
    .onCreate(async (snapshot, context) => {
        const healthTip = snapshot.val();
        const tipId = context.params.tipId;
        
        console.log('New health tip created:', tipId);
        
        // Lưu vào recommendation queue
        const today = new Date().toISOString().split('T')[0];
        
        await admin.database()
            .ref(`/recommendation_queue/${today}/${tipId}`)
            .set({
                tipId: tipId,
                title: healthTip.title,
                categoryId: healthTip.categoryId,
                categoryName: healthTip.categoryName,
                imageUrl: healthTip.imageUrl || '',
                timestamp: admin.database.ServerValue.TIMESTAMP
            });
        
        console.log('Health tip queued for recommendations');
        return null;
    });

/**
 * Scheduled function chạy mỗi ngày lúc 18:00
 * Gửi 1-2 bài viết đề xuất cho mỗi user dựa trên preferences
 */
exports.sendDailyRecommendations = functions.pubsub
    .schedule('0 18 * * *')
    .timeZone('Asia/Ho_Chi_Minh')
    .onRun(async (context) => {
        const today = new Date().toISOString().split('T')[0];
        
        console.log('Running daily recommendations for:', today);
        
        try {
            // Lấy queue bài viết mới
            const queueSnapshot = await admin.database()
                .ref(`/recommendation_queue/${today}`)
                .once('value');
            
            const newTips = queueSnapshot.val();
            
            if (!newTips) {
                console.log('No new tips today for recommendations');
                return null;
            }
            
            console.log('Found', Object.keys(newTips).length, 'new tips');
            
            // Nhóm tips theo category
            const tipsByCategory = {};
            Object.keys(newTips).forEach(tipId => {
                const tip = newTips[tipId];
                const categoryId = tip.categoryId;
                
                if (!tipsByCategory[categoryId]) {
                    tipsByCategory[categoryId] = [];
                }
                tipsByCategory[categoryId].push({
                    tipId: tipId,
                    ...tip
                });
            });
            
            // Lấy tất cả users
            const usersSnapshot = await admin.database()
                .ref('/users')
                .once('value');
            
            const users = usersSnapshot.val();
            if (!users) {
                console.log('No users found');
                return null;
            }
            
            // Gửi recommendations cho từng user
            const promises = Object.keys(users).map(async userId => {
                const user = users[userId];
                
                // Kiểm tra user có bật recommendations không
                if (!user.fcmToken || 
                    user.notification_preferences?.recommendations === false) {
                    return null;
                }
                
                // Lấy categories user quan tâm
                const userCategories = [];
                const prefs = user.notification_preferences || {};
                
                Object.keys(prefs).forEach(key => {
                    if (key.startsWith('category_') && prefs[key] === true) {
                        const categoryId = key.replace('category_', '');
                        userCategories.push(categoryId);
                    }
                });
                
                if (userCategories.length === 0) {
                    console.log('User', userId, 'has no category preferences');
                    return null;
                }
                
                // Chọn 1-2 bài viết phù hợp
                const recommendedTips = [];
                
                for (const categoryId of userCategories) {
                    if (tipsByCategory[categoryId] && tipsByCategory[categoryId].length > 0) {
                        // Lấy bài viết đầu tiên trong category (có thể random sau)
                        recommendedTips.push(tipsByCategory[categoryId][0]);
                        
                        if (recommendedTips.length >= 2) {
                            break; // Tối đa 2 bài
                        }
                    }
                }
                
                if (recommendedTips.length === 0) {
                    console.log('No matching tips for user', userId);
                    return null;
                }
                
                // Tạo notification payload
                const firstTip = recommendedTips[0];
                const tipsCount = recommendedTips.length;
                
                const payload = {
                    data: {
                        type: 'health_tip_recommendation',
                        recommendationType: 'daily_picks',
                        tips: JSON.stringify(recommendedTips),
                        tipsCount: tipsCount.toString(),
                        timestamp: Date.now().toString(),
                        title: `📚 ${tipsCount} bài viết mới dành cho bạn`,
                        body: `${firstTip.title}${tipsCount > 1 ? ' và ' + (tipsCount - 1) + ' bài viết khác' : ''}`
                    }
                };
                
                // Gửi notification
                try {
                    await admin.messaging().sendToDevice(user.fcmToken, payload, {
                        priority: 'high',
                        timeToLive: 60 * 60 * 24
                    });
                    
                    console.log('Recommendation sent to user', userId, '-', tipsCount, 'tips');
                } catch (error) {
                    console.error('Error sending to user', userId, ':', error);
                }
                
                return null;
            });
            
            await Promise.all(promises);
            
            // Xóa queue sau khi gửi
            await admin.database()
                .ref(`/recommendation_queue/${today}`)
                .remove();
            
            console.log('Daily recommendations completed');
            
        } catch (error) {
            console.error('Error sending daily recommendations:', error);
        }
        
        return null;
    });

```

---

## 6. FIREBASE DATABASE TRIGGERS - GIẢI PHÁP TỰ ĐỘNG

### 6.1. Firebase Database Triggers là gì?

**Firebase Realtime Database Triggers** là cơ chế **tự động kích hoạt Cloud Functions** khi có thay đổi trong database.

#### ❌ KHÔNG CẦN WEBHOOK vì:

**Webhook truyền thống:**
```
Client → API Server → Webhook → Xử lý
(Cần setup endpoint, authentication, monitoring)
```

**Firebase Database Triggers:**
```
Client → Firebase Database → Cloud Function TỰ ĐỘNG chạy
(Không cần setup gì thêm!)
```

#### ✅ Ưu điểm so với Webhook:

| Tiêu chí | Webhook | Firebase Triggers |
|----------|---------|-------------------|
| **Setup** | Cần API endpoint | Không cần |
| **Authentication** | Phải tự implement | Firebase tự handle |
| **Scaling** | Phải tự manage | Auto-scale |
| **Retry logic** | Phải tự code | Built-in |
| **Real-time** | Phụ thuộc polling | < 1 giây |
| **Cost** | Server 24/7 | Pay-per-use |
| **Monitoring** | Phải setup | Firebase Console |

### 6.2. Cách Firebase Triggers hoạt động

```javascript
// Khi User A tạo reply comment:
const replyRef = firebase.database().ref('videos/video123/comments').push();
replyRef.set({
    userId: 'userA',
    text: 'Great tip!',
    parentId: 'comment456', // Comment của User B
    createdAt: Date.now()
});

// ⚡ NGAY LẬP TỨC (< 1 giây):
// Firebase Database nhận thấy có data mới
// → Tự động trigger Cloud Function
// → Cloud Function gửi notification đến User B
// → User B nhận notification

// KHÔNG CẦN:
// - API call từ client để trigger
// - Polling để check data mới
// - Webhook endpoint
// - Background service
```

### 6.3. Các loại Triggers

#### A. onCreate Trigger
Chạy khi có **data mới** được tạo:
```javascript
// Khi có comment reply mới
exports.onCommentReply = functions.database
    .ref('/videos/{videoId}/comments/{commentId}')
    .onCreate((snapshot, context) => {
        // Tự động chạy khi có comment mới
        const newComment = snapshot.val();
        // Gửi notification...
    });
```

#### B. onUpdate Trigger
Chạy khi data được **cập nhật**:
```javascript
// Khi comment được edit
exports.onCommentEdit = functions.database
    .ref('/videos/{videoId}/comments/{commentId}')
    .onUpdate((change, context) => {
        const before = change.before.val();
        const after = change.after.val();
        // Xử lý update...
    });
```

#### C. onDelete Trigger
Chạy khi data bị **xóa**:
```javascript
// Khi comment bị xóa
exports.onCommentDelete = functions.database
    .ref('/videos/{videoId}/comments/{commentId}')
    .onDelete((snapshot, context) => {
        const deletedComment = snapshot.val();
        // Cleanup...
    });
```

#### D. onWrite Trigger
Chạy khi có **bất kỳ thay đổi nào** (create, update, delete):
```javascript
exports.onCommentChange = functions.database
    .ref('/videos/{videoId}/comments/{commentId}')
    .onWrite((change, context) => {
        // Xử lý mọi thay đổi
    });
```

### 6.4. Scheduled Functions (Thay thế Cron Jobs)

```javascript
// Ví dụ: Scheduled function cho weekly highlights
exports.sendWeeklyHighlights = functions.pubsub
    .schedule('0 20 * * 0') // Chủ Nhật lúc 20:00
    .timeZone('Asia/Ho_Chi_Minh')
    .onRun(async (context) => {
        // Gửi bài viết hot trong tuần
    });
```

### 6.5. User Notification Preferences

**Cấu trúc Firebase Database cho preferences:**

```json
{
  "users": {
    "user123": {
      "fcmToken": "fcm_token_string",
      "notification_preferences": {
        "comment_replies": true,
        "comment_likes": false,
        "new_videos": true,
        "new_posts": true,
        "category_dinh_duong": true,
        "category_the_duc": true,
        "category_suc_khoe_tinh_than": false,
        "quiet_hours_enabled": true,
        "quiet_hours_start": "22:00",
        "quiet_hours_end": "07:00"
      }
    }
  }
}
```

**Cloud Function kiểm tra preferences trước khi gửi:**

```javascript
async function shouldSendNotification(userId, notificationType) {
    const prefsSnapshot = await admin.database()
        .ref(`/users/${userId}/notification_preferences/${notificationType}`)
        .once('value');
    
    const enabled = prefsSnapshot.val();
    
    if (enabled !== true) {
        console.log(`User ${userId} has disabled ${notificationType}`);
        return false;
    }
    
    // Kiểm tra quiet hours
    const quietHoursSnapshot = await admin.database()
        .ref(`/users/${userId}/notification_preferences`)
        .once('value');
    
    const prefs = quietHoursSnapshot.val();
    
    if (prefs.quiet_hours_enabled) {
        const now = new Date();
        const currentHour = now.getHours();
        const startHour = parseInt(prefs.quiet_hours_start.split(':')[0]);
        const endHour = parseInt(prefs.quiet_hours_end.split(':')[0]);
        
        // Kiểm tra xem có trong quiet hours không
        if (currentHour >= startHour || currentHour < endHour) {
            console.log(`User ${userId} is in quiet hours`);
            return false;
        }
    }
    
    return true;
}

// Sử dụng trong Cloud Function
exports.sendCommentReplyNotification = functions.database
    .ref('/videos/{videoId}/comments/{commentId}')
    .onCreate(async (snapshot, context) => {
        const reply = snapshot.val();
        
        // ... lấy thông tin recipient ...
        
        // Kiểm tra preferences trước khi gửi
        const shouldSend = await shouldSendNotification(
            recipientUserId, 
            'comment_replies'
        );
        
        if (!shouldSend) {
            console.log('Skipping notification due to user preferences');
            return null;
        }
        
        // Gửi notification...
    });
```

### 6.6. Notification Batching và Rate Limiting

**Tránh spam notifications cho cùng 1 user:**

```javascript
const NOTIFICATION_COOLDOWN = 5 * 60 * 1000; // 5 phút

async function canSendNotification(userId, notificationType) {
    const lastNotifRef = admin.database()
        .ref(`/users/${userId}/last_notification/${notificationType}`);
    
    const snapshot = await lastNotifRef.once('value');
    const lastTimestamp = snapshot.val();
    
    if (lastTimestamp) {
        const timeSinceLastNotif = Date.now() - lastTimestamp;
        
        if (timeSinceLastNotif < NOTIFICATION_COOLDOWN) {
            console.log(`Rate limit: Last notification sent ${timeSinceLastNotif}ms ago`);
            return false;
        }
    }
    
    // Update last notification time
    await lastNotifRef.set(Date.now());
    
    return true;
}
```

**Batch multiple replies thành 1 notification:**

```javascript
// Nếu User B reply 3 lần comment của User A trong 5 phút
// → Chỉ gửi 1 notification: "User B và 2 người khác đã trả lời bình luận"

const BATCH_WINDOW = 5 * 60 * 1000; // 5 phút

exports.batchCommentReplies = functions.database
    .ref('/videos/{videoId}/comments/{commentId}')
    .onCreate(async (snapshot, context) => {
        const reply = snapshot.val();
        
        if (!reply.parentId) return null;
        
        const parentComment = /* ... get parent ... */;
        const recipientUserId = parentComment.userId;
        
        // Kiểm tra xem có replies khác trong batch window không
        const batchRef = admin.database()
            .ref(`/notification_batch/${recipientUserId}/comment_replies`);
        
        const batchSnapshot = await batchRef.once('value');
        const batch = batchSnapshot.val() || {};
        
        // Thêm reply vào batch
        batch[context.params.commentId] = {
            senderId: reply.userId,
            timestamp: Date.now()
        };
        
        await batchRef.set(batch);
        
        // Schedule function để gửi batch sau 5 phút
        // (hoặc gửi ngay nếu đã hết batch window)
        
        return null;
    });
```

---

## 7. LUỒNG XỬ LÝ

### 7.1. Luồng Reply Comment (Với Firebase Triggers)

```
[User A] Comment vào video
       ↓
[User B] Reply vào comment của User A
       ↓
[Cloud Function] Trigger onCreate cho reply
       ↓
[Cloud Function] Lấy thông tin:
  - Parent comment → userId của User A
  - FCM token của User A
  - Thông tin User B (người reply)
       ↓
[Cloud Function] Gửi FCM message với data payload
       ↓
[FCM] Gửi đến device của User A
       ↓
[MyFirebaseMessagingService] onMessageReceived()
  - Parse data payload
  - Tạo notification với PendingIntent
       ↓
[User A] Click vào notification
       ↓
[DeepLinkHandlerActivity] onCreate()
  - Parse notification type = "comment_reply"
  - Extract: videoId, parentCommentId, replyCommentId
  - Tạo Intent cho SingleVideoPlayerActivity
       ↓
[SingleVideoPlayerActivity] onCreate()
  - Load video
  - Phát hiện flag "open_comments" = true
  - Delay 500ms để video load
  - Gọi openCommentsWithScroll()
       ↓
[CommentBottomSheetFragment] show()
  - Load comments từ Firebase
  - Nhận args: scroll_to_comment_id, highlight_reply_id
  - Delay 300ms để RecyclerView load
  - Gọi scrollToCommentAndExpand()
       ↓
[CommentAdapter] 
  - Scroll đến parent comment
  - Expand replies section
  - Highlight reply mới với animation
       ↓
[User A] Thấy reply và có thể tương tác
```

### 7.2. Luồng New Health Tip (Daily Digest)

```
[Admin] Đăng bài viết mới vào Firebase
       ↓
[Cloud Function] Trigger onCreate cho health_tips
       ↓
[Cloud Function] 
  - Lấy danh sách tất cả users
  - Filter users có FCM token
  - Tạo payload với healthTipId
       ↓
[FCM] Gửi đến tất cả devices
       ↓
[MyFirebaseMessagingService] onMessageReceived()
  - Parse data: type = "new_health_tip"
  - Hiển thị notification
       ↓
[User] Click notification
       ↓
[DeepLinkHandlerActivity]
  - Parse healthTipId
  - Tạo Intent cho HealthTipDetailActivity
       ↓
[HealthTipDetailActivity]
  - Load và hiển thị bài viết
```

---

## 8. RECOMMENDED TIPS ACTIVITY (Hiển thị danh sách đề xuất)

### 8.1. Tạo RecommendedTipsActivity

**Mục đích:** Hiển thị danh sách 2 bài viết được đề xuất khi user click notification recommendations.

**File:** `RecommendedTipsActivity.java`
**Location:** `app/src/main/java/com/vhn/doan/presentation/healthtip/recommended/`

```java
package com.vhn.doan.presentation.healthtip.recommended;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.presentation.healthtip.detail.HealthTipDetailActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RecommendedTipsActivity extends AppCompatActivity {

    private static final String TAG = "RecommendedTips";
    private RecyclerView recyclerView;
    private RecommendedTipsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommended_tips);
        
        setupToolbar();
        setupRecyclerView();
        loadRecommendations();
    }
    
    private void setupToolbar() {
        String title = getIntent().getStringExtra("title");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title != null ? title : "Bài viết đề xuất");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_recommended_tips);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new RecommendedTipsAdapter(tip -> {
            // Click vào tip → Mở detail
            Intent intent = new Intent(this, HealthTipDetailActivity.class);
            intent.putExtra("health_tip_id", tip.getTipId());
            startActivity(intent);
        });
        
        recyclerView.setAdapter(adapter);
    }
    
    private void loadRecommendations() {
        String tipsJson = getIntent().getStringExtra("tips_json");
        
        if (tipsJson == null) {
            Log.w(TAG, "No tips data found");
            finish();
            return;
        }
        
        try {
            JSONArray tipsArray = new JSONArray(tipsJson);
            List<RecommendedTip> tips = new ArrayList<>();
            
            for (int i = 0; i < tipsArray.length(); i++) {
                JSONObject tipObj = tipsArray.getJSONObject(i);
                
                RecommendedTip tip = new RecommendedTip(
                    tipObj.getString("tipId"),
                    tipObj.getString("title"),
                    tipObj.optString("categoryName", ""),
                    tipObj.optString("imageUrl", "")
                );
                
                tips.add(tip);
            }
            
            adapter.setTips(tips);
            
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing tips JSON", e);
            finish();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
```

**Layout:** `activity_recommended_tips.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        
        <androidx.appcompat.widget.Toolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            app:title="Bài viết đề xuất"/>
    </com.google.android.material.appbar.AppBarLayout>

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp"
        android:text="📚 Dành riêng cho bạn"
        android:textSize="18sp"
        android:textStyle="bold"/>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recycler_recommended_tips"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:padding="8dp"/>

</LinearLayout>
```

**Model:** `RecommendedTip.java`

```java
package com.vhn.doan.presentation.healthtip.recommended;

public class RecommendedTip {
    private String tipId;
    private String title;
    private String categoryName;
    private String imageUrl;

    public RecommendedTip(String tipId, String title, String categoryName, String imageUrl) {
        this.tipId = tipId;
        this.title = title;
        this.categoryName = categoryName;
        this.imageUrl = imageUrl;
    }

    public String getTipId() { return tipId; }
    public String getTitle() { return title; }
    public String getCategoryName() { return categoryName; }
    public String getImageUrl() { return imageUrl; }
}
```

**Adapter:** `RecommendedTipsAdapter.java`

```java
package com.vhn.doan.presentation.healthtip.recommended;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.vhn.doan.R;
import java.util.ArrayList;
import java.util.List;

public class RecommendedTipsAdapter extends RecyclerView.Adapter<RecommendedTipsAdapter.ViewHolder> {

    private List<RecommendedTip> tips = new ArrayList<>();
    private OnTipClickListener listener;

    public interface OnTipClickListener {
        void onTipClick(RecommendedTip tip);
    }

    public RecommendedTipsAdapter(OnTipClickListener listener) {
        this.listener = listener;
    }

    public void setTips(List<RecommendedTip> tips) {
        this.tips = tips;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_recommended_tip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecommendedTip tip = tips.get(position);
        
        holder.tvTitle.setText(tip.getTitle());
        holder.tvCategory.setText(tip.getCategoryName());
        
        if (tip.getImageUrl() != null && !tip.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(tip.getImageUrl())
                .placeholder(R.drawable.placeholder_health_tip)
                .into(holder.ivThumbnail);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTipClick(tip);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tips.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle;
        TextView tvCategory;

        ViewHolder(View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_tip_thumbnail);
            tvTitle = itemView.findViewById(R.id.tv_tip_title);
            tvCategory = itemView.findViewById(R.id.tv_tip_category);
        }
    }
}
```

**Item Layout:** `item_recommended_tip.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="12dp">

        <ImageView
            android:id="@+id/iv_tip_thumbnail"
            android:layout_width="80dp"
            android:layout_height="80dp"
            android:scaleType="centerCrop"
            android:contentDescription="Health tip thumbnail"/>

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginStart="12dp"
            android:orientation="vertical">

            <TextView
                android:id="@+id/tv_tip_title"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="10 mẹo giữ sức khỏe mùa đông"
                android:textSize="16sp"
                android:textStyle="bold"
                android:maxLines="2"
                android:ellipsize="end"/>

            <TextView
                android:id="@+id/tv_tip_category"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="4dp"
                android:text="Dinh dưỡng"
                android:textSize="14sp"
                android:textColor="@color/primary"
                android:drawableStart="@drawable/ic_category"
                android:drawablePadding="4dp"/>

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:text="📚 Đề xuất cho bạn"
                android:textSize="12sp"
                android:textColor="@color/text_secondary"/>

        </LinearLayout>

    </LinearLayout>

</com.google.android.material.card.MaterialCardView>
```

**Thêm vào AndroidManifest.xml:**

```xml
<activity
    android:name=".presentation.healthtip.recommended.RecommendedTipsActivity"
    android:label="Bài viết đề xuất"
    android:theme="@style/Theme.HealthTips"/>
```

---

## 9. NOTIFICATION PREFERENCES UI

### 8.1. Tạo NotificationPreferencesActivity

**Layout cho preferences:**

```xml
<!-- activity_notification_preferences.xml -->
<ScrollView>
    <LinearLayout>
        <!-- Comment Notifications -->
        <TextView 
            android:text="Bình luận"
            style="@style/PreferenceCategory"/>
        
        <SwitchCompat
            android:id="@+id/switch_comment_replies"
            android:text="Trả lời bình luận"
            android:checked="true"/>
        
        <SwitchCompat
            android:id="@+id/switch_comment_likes"
            android:text="Lượt thích bình luận"
            android:checked="true"/>
        
        <!-- Content Notifications -->
        <TextView 
            android:text="Nội dung"
            style="@style/PreferenceCategory"/>
        
        <SwitchCompat
            android:id="@+id/switch_new_posts"
            android:text="Bài viết mới từ Admin"
            android:checked="true"/>
        
        <TextView
            android:text="Nhận thông báo khi Admin đăng bài viết quan trọng"
            android:textSize="12sp"/>
        
        <SwitchCompat
            android:id="@+id/switch_recommendations"
            android:text="Đề xuất hàng ngày (18:00)"
            android:checked="true"/>
        
        <TextView
            android:text="Nhận 1-2 bài viết đề xuất mỗi ngày theo sở thích"
            android:textSize="12sp"/>
        
        <!-- Category Preferences -->
        <TextView 
            android:text="Chủ đề quan tâm"
            style="@style/PreferenceCategory"/>
        
        <SwitchCompat
            android:id="@+id/switch_category_nutrition"
            android:text="Dinh dưỡng"
            android:checked="true"/>
        
        <SwitchCompat
            android:id="@+id/switch_category_exercise"
            android:text="Thể dục"
            android:checked="true"/>
        
        <!-- Quiet Hours -->
        <TextView 
            android:text="Giờ im lặng"
            style="@style/PreferenceCategory"/>
        
        <SwitchCompat
            android:id="@+id/switch_quiet_hours"
            android:text="Bật giờ im lặng"
            android:checked="false"/>
        
        <LinearLayout android:id="@+id/layout_quiet_hours">
            <TextView android:text="Từ:"/>
            <Button 
                android:id="@+id/btn_quiet_start"
                android:text="22:00"/>
            
            <TextView android:text="Đến:"/>
            <Button 
                android:id="@+id/btn_quiet_end"
                android:text="07:00"/>
        </LinearLayout>
    </LinearLayout>
</ScrollView>
```

**Java code:**

```java
public class NotificationPreferencesActivity extends AppCompatActivity {
    
    private String userId;
    private DatabaseReference prefsRef;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_preferences);
        
        userId = SharedPreferencesHelper.getUserId(this);
        prefsRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("notification_preferences");
        
        loadPreferences();
        setupListeners();
    }
    
    private void loadPreferences() {
        prefsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Load và set trạng thái các switches
                Boolean commentReplies = snapshot.child("comment_replies")
                    .getValue(Boolean.class);
                switchCommentReplies.setChecked(
                    commentReplies != null ? commentReplies : true
                );
                
                Boolean newPosts = snapshot.child("new_posts")
                    .getValue(Boolean.class);
                switchNewPosts.setChecked(
                    newPosts != null ? newPosts : true
                );
                
                Boolean recommendations = snapshot.child("recommendations")
                    .getValue(Boolean.class);
                switchRecommendations.setChecked(
                    recommendations != null ? recommendations : true
                );
                
                // Load category preferences
                Boolean categoryDinhDuong = snapshot.child("category_dinh_duong")
                    .getValue(Boolean.class);
                switchCategoryNutrition.setChecked(
                    categoryDinhDuong != null ? categoryDinhDuong : true
                );
                
                // Load các preferences khác...
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(this, "Lỗi tải preferences", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setupListeners() {
        switchCommentReplies.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsRef.child("comment_replies").setValue(isChecked);
        });
        
        switchNewPosts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsRef.child("new_posts").setValue(isChecked);
        });
        
        switchRecommendations.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsRef.child("recommendations").setValue(isChecked);
        });
        
        switchCategoryNutrition.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsRef.child("category_dinh_duong").setValue(isChecked);
        });
        
        // Setup các listeners khác...
    }
}
```

---

## 9. TESTING & DEBUGGING

### 9.1. Test Firebase Triggers Local

**Cài đặt Firebase Emulator Suite:**

```bash
npm install -g firebase-tools
firebase login
firebase init emulators

# Chọn:
# - Functions
# - Realtime Database
```

**Test Cloud Functions local:**

```bash
# Terminal 1: Start emulators
firebase emulators:start

# Terminal 2: Trigger function bằng cách thêm data
curl -X PUT http://localhost:9000/.../videos/test123/comments/comment456 \
  -d '{"userId":"userA","text":"Test reply","parentId":"comment123"}'

# Xem log trong Terminal 1
```

### 9.2. Test bằng Firebase Console

**Bước 1:** Lấy FCM Token
```java
// Trong MainActivity hoặc bất kỳ Activity nào
FirebaseMessaging.getInstance().getToken()
    .addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
            String token = task.getResult();
            Log.d("FCM_TOKEN", token);
            // Copy token này
        }
    });
```

**Bước 2:** Gửi test notification từ Firebase Console
1. Vào Firebase Console → Cloud Messaging
2. Click "Send your first message"
3. Nhập notification text
4. Click "Send test message"
5. Paste FCM token
6. Click "Test"

**Bước 3:** Gửi với custom data
Sử dụng Firebase Composer hoặc REST API:

```bash
curl -X POST https://fcm.googleapis.com/fcm/send \
  -H "Authorization: key=YOUR_SERVER_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "to": "FCM_TOKEN_HERE",
    "data": {
      "type": "comment_reply",
      "videoId": "test_video_123",
      "parentCommentId": "comment_456",
      "replyCommentId": "reply_789",
      "senderName": "Test User",
      "replyText": "This is a test reply",
      "title": "Trả lời bình luận",
      "body": "Test User đã trả lời bình luận của bạn"
    }
  }'
```

### 9.3. Debug Checklist

#### Notification không hiển thị:
- ✅ Kiểm tra quyền POST_NOTIFICATIONS (Android 13+)
- ✅ Kiểm tra Notification Channel đã được tạo
- ✅ Kiểm tra app không bị force stop
- ✅ Kiểm tra log trong MyFirebaseMessagingService

#### Deep link không hoạt động:
- ✅ Kiểm tra DeepLinkHandlerActivity trong AndroidManifest
- ✅ Kiểm tra Intent extras có được truyền đúng không
- ✅ Kiểm tra log trong DeepLinkHandlerActivity
- ✅ Kiểm tra Activity đích có tồn tại không

#### Comment không scroll đến đúng vị trí:
- ✅ Kiểm tra commentId có chính xác không
- ✅ Kiểm tra delay timing (có thể cần tăng delay)
- ✅ Kiểm tra RecyclerView đã load xong chưa
- ✅ Kiểm tra method findCommentPosition()

#### Cloud Function không chạy:
- ✅ Kiểm tra Functions đã deploy chưa: `firebase deploy --only functions`
- ✅ Xem logs: `firebase functions:log`
- ✅ Kiểm tra billing account (Functions cần Blaze plan cho production)
- ✅ Verify database path trong trigger code

#### Notification không gửi được:
- ✅ Kiểm tra FCM token còn valid không
- ✅ Verify user preferences trong database
- ✅ Check rate limiting / cooldown
- ✅ Xem Cloud Function logs để tìm lỗi

### 9.4. Logging Strategy

```java
// Thêm các log points quan trọng

// MyFirebaseMessagingService
Log.d(TAG, "=== FCM Message Received ===");
Log.d(TAG, "Type: " + type);
Log.d(TAG, "Data: " + data.toString());

// DeepLinkHandlerActivity
Log.d(TAG, "=== Deep Link Handler ===");
Log.d(TAG, "Notification type: " + notificationType);
Log.d(TAG, "Extras: " + intent.getExtras());

// SingleVideoPlayerActivity
Log.d(TAG, "=== Handling Deep Link ===");
Log.d(TAG, "Should open comments: " + shouldOpenComments);
Log.d(TAG, "Scroll to comment: " + scrollToCommentId);

// CommentBottomSheetFragment
Log.d(TAG, "=== Scroll to Comment ===");
Log.d(TAG, "Target comment ID: " + scrollToCommentId);
Log.d(TAG, "Found at position: " + position);
```

// Cloud Functions logging
exports.sendCommentReplyNotification = functions.database
    .ref('/videos/{videoId}/comments/{commentId}')
    .onCreate(async (snapshot, context) => {
        console.log('=== Comment Reply Trigger ===');
        console.log('Video ID:', context.params.videoId);
        console.log('Comment ID:', context.params.commentId);
        console.log('Data:', snapshot.val());
        
        const reply = snapshot.val();
        
        if (!reply.parentId) {
            console.log('Not a reply, skipping');
            return null;
        }
        
        console.log('Parent comment ID:', reply.parentId);
        
        // ... rest of code ...
        
        console.log('Notification sent successfully to:', recipientUserId);
        return null;
    });

---

## 10. BEST PRACTICES

### 10.1. Performance

#### Optimize Notification Delivery
```java
// Giới hạn số lượng notifications hiển thị
private static final int MAX_NOTIFICATIONS = 5;
private static int notificationCount = 0;

private void showNotification(...) {
    if (notificationCount >= MAX_NOTIFICATIONS) {
        // Group notifications hoặc create summary
        createSummaryNotification();
    } else {
        // Show individual notification
        notificationCount++;
    }
}
```

#### Batch Notifications
```java
// Group multiple notifications
NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
    .setGroupSummary(true)
    .setGroup("comment_replies")
    .setContentTitle("3 câu trả lời mới")
    .setContentText("Bạn có 3 câu trả lời mới cho bình luận");
```

### 10.2. User Experience

#### Smart Notification Timing
```java
// Không gửi notification vào ban đêm
private boolean shouldSendNotification() {
    Calendar calendar = Calendar.getInstance();
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    
    // Không gửi từ 22h đến 7h sáng
    return hour >= 7 && hour < 22;
}
```

#### Notification Preferences
```java
// Cho phép user tùy chỉnh notifications
SharedPreferences prefs = getSharedPreferences("notifications", MODE_PRIVATE);

boolean enableCommentReplies = prefs.getBoolean("enable_comment_replies", true);
boolean enableNewPosts = prefs.getBoolean("enable_new_posts", true);
boolean enableVideoUpdates = prefs.getBoolean("enable_video_updates", true);
```

### 10.3. Security

#### Validate Data Payload
```java
private boolean isValidPayload(Map<String, String> data) {
    // Kiểm tra các trường bắt buộc
    if (!data.containsKey("type") || 
        !data.containsKey("title") || 
        !data.containsKey("body")) {
        return false;
    }
    
    // Kiểm tra type hợp lệ
    String type = data.get("type");
    return Arrays.asList(
        TYPE_COMMENT_REPLY, 
        TYPE_NEW_HEALTH_TIP, 
        TYPE_NEW_VIDEO,
        TYPE_COMMENT_LIKE
    ).contains(type);
}
```

#### Prevent Deep Link Injection
```java
// Sanitize IDs trước khi sử dụng
private String sanitizeId(String id) {
    if (id == null) return null;
    
    // Chỉ cho phép alphanumeric và underscore
    return id.replaceAll("[^a-zA-Z0-9_-]", "");
}
```

### 10.4. Error Handling

```java
private void handleDeepLink(Intent intent) {
    try {
        String type = intent.getStringExtra("notification_type");
        
        if (type == null) {
            logError("Missing notification type");
            showErrorToUser("Không thể mở nội dung");
            finish();
            return;
        }
        
        // Process deep link
        
    } catch (Exception e) {
        Log.e(TAG, "Error handling deep link", e);
        
        // Fallback: Mở HomeActivity
        Intent homeIntent = new Intent(this, HomeActivity.class);
        startActivity(homeIntent);
        finish();
    }
}
```

### 10.5. Cost Optimization (Firebase Functions)

```javascript
// ❌ KHÔNG NÊN: Query tất cả users mỗi lần
const allUsers = await admin.database().ref('/users').once('value');

// ✅ NÊN: Index và query có điều kiện
const activeUsers = await admin.database()
    .ref('/users')
    .orderByChild('notification_preferences/new_posts')
    .equalTo(true)
    .once('value');

// ❌ KHÔNG NÊN: Gọi nhiều lần cho từng user
for (const userId of userIds) {
    const user = await getUser(userId); // N queries!
}

// ✅ NÊN: Batch read
const usersSnapshot = await admin.database()
    .ref('/users')
    .once('value');
const users = usersSnapshot.val();
```

**Firebase Pricing Tips:**
- Realtime Database: Tính theo GB stored + GB downloaded
- Cloud Functions: 
  - Spark (Free): 125K invocations/month, 40K GB-seconds
  - Blaze: $0.40 per million invocations
- Cloud Messaging: Miễn phí

**Tối ưu hóa:**
- Cache FCM tokens trong memory
- Batch notifications thay vì gửi từng cái
- Sử dụng database indexes
- Cleanup old notification data

### 10.6. Analytics

```java
// Track notification interactions
private void logNotificationClick(String type, String targetId) {
    Bundle params = new Bundle();
    params.putString("notification_type", type);
    params.putString("target_id", targetId);
    params.putLong("timestamp", System.currentTimeMillis());
    
    FirebaseAnalytics.getInstance(this)
        .logEvent("notification_clicked", params);
}
```

---

## 11. SETUP CHO DEEP LINKING NOTIFICATIONS

### 11.1. Cấu trúc Database cần thêm

**Thêm vào Firebase Realtime Database hiện tại:**
   ```json
   {
     "users": {
       "user_id": {
         "fcmToken": "string",
         "notification_preferences": {}
       }
     },
     "videos": {
       "video_id": {
         "comments": {
           "comment_id": {
             "userId": "string",
             "text": "string",
             "parentId": "string",
             "createdAt": timestamp
           }
         }
       }
     },
     "health_tips": {
       "tip_id": {
         "title": "string",
         "categoryId": "string"
       }
     },
     "notifications": {
       "user_id": {
         "notification_id": {
           "type": "string",
           "read": false,
           "createdAt": timestamp
         }
       }
     }
   }
   ```

3. **Setup Database Rules (Tạm thời - Development):**
   ```json
   {
     "rules": {
       ".read": "auth != null",
       ".write": "auth != null"
     }
   }
   ```

4. **Setup Database Indexes:**
   - Tab "Rules" → "Indexes"
   - Thêm index cho query hiệu quả:
   ```json
   {
     "rules": {
       "users": {
         ".indexOn": ["fcmToken"]
       },
       "videos": {
         "$videoId": {
           "comments": {
             ".indexOn": ["parentId", "createdAt"]
           }
         }
       }
     }
   }
   ```

#### C. Enable Authentication

1. **Enable Auth Methods:**
   - Menu → "Build" → "Authentication"
   - Tab "Sign-in method"
   - Enable:
     - ✅ Email/Password
     - ✅ Google (optional)

### 11.3. Bước 3: Setup Cloud Functions

#### A. Khởi tạo Cloud Functions (nếu chưa có)

```bash
# Nếu chưa có folder functions
firebase init functions

# Chọn JavaScript và install dependencies
```

#### B. Thêm Cloud Functions code

**Thêm vào file: `functions/index.js`**

```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Initialize Firebase Admin (nếu chưa có)
// admin.initializeApp();

// ==================== COMMENT REPLY NOTIFICATION ====================

/**
 * Trigger khi có comment reply mới
 * Tự động gửi notification đến người được reply
 */
exports.sendCommentReplyNotification = functions.database
    .ref('/videos/{videoId}/comments/{commentId}')
    .onCreate(async (snapshot, context) => {
        const reply = snapshot.val();
        const videoId = context.params.videoId;
        const commentId = context.params.commentId;
        
        console.log('New comment created:', commentId);
        
        // Kiểm tra xem có phải reply không
        if (!reply.parentId) {
            console.log('Not a reply, skipping');
            return null;
        }
        
        console.log('This is a reply to:', reply.parentId);
        
        try {
            // Lấy parent comment
            const parentSnapshot = await admin.database()
                .ref(`/videos/${videoId}/comments/${reply.parentId}`)
                .once('value');
            
            const parentComment = parentSnapshot.val();
            if (!parentComment) {
                console.log('Parent comment not found');
                return null;
            }
            
            const recipientUserId = parentComment.userId;
            const senderUserId = reply.userId;
            
            // Không gửi notification cho chính mình
            if (recipientUserId === senderUserId) {
                console.log('Self-reply, skipping notification');
                return null;
            }
            
            // Kiểm tra user preferences
            const prefsSnapshot = await admin.database()
                .ref(`/users/${recipientUserId}/notification_preferences/comment_replies`)
                .once('value');
            
            if (prefsSnapshot.val() === false) {
                console.log('User disabled comment reply notifications');
                return null;
            }
            
            // Lấy FCM token
            const userSnapshot = await admin.database()
                .ref(`/users/${recipientUserId}`)
                .once('value');
            
            const user = userSnapshot.val();
            if (!user || !user.fcmToken) {
                console.log('User has no FCM token');
                return null;
            }
            
            // Lấy thông tin sender
            const senderSnapshot = await admin.database()
                .ref(`/users/${senderUserId}`)
                .once('value');
            
            const sender = senderSnapshot.val();
            const senderName = sender?.displayName || 'Người dùng';
            
            // Tạo notification payload
            const payload = {
                data: {
                    type: 'comment_reply',
                    videoId: videoId,
                    parentCommentId: reply.parentId,
                    replyCommentId: commentId,
                    senderId: senderUserId,
                    senderName: senderName,
                    replyText: reply.text,
                    timestamp: Date.now().toString(),
                    title: 'Trả lời bình luận',
                    body: `${senderName} đã trả lời: "${reply.text}"`
                }
            };
            
            // Gửi notification
            await admin.messaging().sendToDevice(user.fcmToken, payload, {
                priority: 'high',
                timeToLive: 60 * 60 * 24
            });
            
            console.log('Notification sent successfully to:', recipientUserId);
            
            // Lưu notification history
            await admin.database()
                .ref(`/notifications/${recipientUserId}`)
                .push({
                    ...payload.data,
                    read: false,
                    createdAt: admin.database.ServerValue.TIMESTAMP
                });
            
        } catch (error) {
            console.error('Error sending notification:', error);
        }
        
        return null;
    });

// ==================== NEW HEALTH TIP NOTIFICATION (ADMIN CONTROL) ====================

/**
 * Gửi notification bài viết mới (khi Admin chọn)
 * Call function này từ Admin app/web khi đăng bài
 */
exports.sendNewHealthTipNotification = functions.https.onCall(async (data, context) => {
    // Kiểm tra quyền admin
    if (!context.auth || !context.auth.token.admin) {
        throw new functions.https.HttpsError(
            'permission-denied',
            'Chỉ admin mới có thể gửi notification'
        );
    }
    
    const { healthTipId, title, categoryId, imageUrl } = data;
    
    if (!healthTipId || !title) {
        throw new functions.https.HttpsError(
            'invalid-argument',
            'Missing required fields'
        );
    }
    
    console.log('Admin triggered notification for health tip:', healthTipId);
    
    try {
        // Lấy tất cả users có bật notification
        const usersSnapshot = await admin.database()
            .ref('/users')
            .once('value');
        
        const users = usersSnapshot.val();
        const tokens = [];
        
        if (users) {
            Object.keys(users).forEach(userId => {
                const user = users[userId];
                // Kiểm tra preferences
                if (user.fcmToken && 
                    user.notification_preferences?.new_posts !== false) {
                    tokens.push(user.fcmToken);
                }
            });
        }
        
        if (tokens.length === 0) {
            console.log('No users to notify');
            return { success: true, sentCount: 0 };
        }
        
        const payload = {
            data: {
                type: 'new_health_tip',
                healthTipId: healthTipId,
                categoryId: categoryId || '',
                imageUrl: imageUrl || '',
                timestamp: Date.now().toString(),
                title: '📢 Bài viết mới',
                body: title
            }
        };
        
        // Gửi notification
        const response = await admin.messaging().sendToDevice(tokens, payload, {
            priority: 'high',
            timeToLive: 60 * 60 * 24
        });
        
        console.log('Notification sent to', tokens.length, 'users');
        console.log('Success count:', response.successCount);
        console.log('Failure count:', response.failureCount);
        
        return { 
            success: true, 
            sentCount: response.successCount,
            failureCount: response.failureCount
        };
        
    } catch (error) {
        console.error('Error sending notification:', error);
        throw new functions.https.HttpsError('internal', error.message);
    }
});
```

#### C. Setup Admin Claims (Để phân quyền)

**Thêm custom claims cho admin user:**

```javascript
// Chạy một lần để set admin claims
const admin = require('firebase-admin');
admin.initializeApp();

async function setAdminClaim(email) {
    const user = await admin.auth().getUserByEmail(email);
    await admin.auth().setCustomUserClaims(user.uid, { admin: true });
    console.log(`Admin claim set for ${email}`);
}

// Chạy với email của admin
setAdminClaim('admin@healthtips.com');
```

#### D. Deploy Cloud Functions

```bash
# Deploy tất cả functions
firebase deploy --only functions:sendCommentReplyNotification,sendNewHealthTipNotification,queueHealthTipForRecommendation,sendDailyRecommendations

# Xem logs
firebase functions:log --follow
```

### 11.3. Test Cloud Functions

#### A. Test Comment Reply Notification

**1. Tạo comment reply trong app:**
   - User A comment vào video
   - User B reply comment của User A
   - User A sẽ nhận notification

**2. Hoặc thêm manual trong Firebase Console:**
   - Realtime Database → videos/{videoId}/comments
   - Add child với data:
   ```json
   {
     "userId": "userB_id",
     "text": "Test reply",
     "parentId": "comment_id_cua_userA",
     "createdAt": 1699999999000
   }
   ```

**3. Xem logs:**
   ```bash
   firebase functions:log --follow
   ```

**4. Kiểm tra:**
   - User A device nhận notification
   - Click notification → Mở video + scroll đến comment

#### B. Test New Health Tip Notification (Admin Control)

**1. Trong Admin Web, thêm code gọi Cloud Function:**

**Option 1: Sử dụng Firebase SDK (JavaScript):**

```javascript
// Trong Admin Web (JavaScript/React/Vue/etc.)
import { getFunctions, httpsCallable } from 'firebase/functions';

const functions = getFunctions();
const sendNotification = httpsCallable(functions, 'sendNewHealthTipNotification');

// Khi Admin đăng bài và tick checkbox "Gửi thông báo"
async function publishPostWithNotification(postData) {
    try {
        const result = await sendNotification({
            healthTipId: postData.id,
            title: postData.title,
            categoryId: postData.categoryId,
            imageUrl: postData.imageUrl
        });
        
        console.log('Notification sent to', result.data.sentCount, 'users');
        alert(`Đã gửi thông báo đến ${result.data.sentCount} người dùng`);
    } catch (error) {
        console.error('Error sending notification:', error);
        alert('Lỗi gửi thông báo: ' + error.message);
    }
}
```

**Option 2: Sử dụng REST API:**

```javascript
// Trong Admin Web - Gọi bằng fetch/axios
async function sendNotificationViaREST(postData, adminToken) {
    const url = `https://us-central1-YOUR_PROJECT_ID.cloudfunctions.net/sendNewHealthTipNotification`;
    
    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${adminToken}`
        },
        body: JSON.stringify({
            data: {
                healthTipId: postData.id,
                title: postData.title,
                categoryId: postData.categoryId,
                imageUrl: postData.imageUrl
            }
        })
    });
    
    const result = await response.json();
    console.log('Sent to', result.result.sentCount, 'users');
}
```

**Option 3: HTML Form Example (Simple Admin Web):**

```html
<!-- admin-create-post.html -->
<!DOCTYPE html>
<html>
<head>
    <title>Admin - Đăng bài viết</title>
    <script src="https://www.gstatic.com/firebasejs/10.0.0/firebase-app-compat.js"></script>
    <script src="https://www.gstatic.com/firebasejs/10.0.0/firebase-functions-compat.js"></script>
</head>
<body>
    <h1>Đăng bài viết mới</h1>
    
    <form id="postForm">
        <label>Tiêu đề:</label>
        <input type="text" id="title" required><br>
        
        <label>Category ID:</label>
        <input type="text" id="categoryId" required><br>
        
        <label>Image URL:</label>
        <input type="text" id="imageUrl"><br>
        
        <label>
            <input type="checkbox" id="sendNotification">
            Gửi thông báo đến tất cả người dùng
        </label><br>
        
        <button type="submit">Đăng bài</button>
    </form>
    
    <div id="result"></div>
    
    <script>
        // Initialize Firebase
        const firebaseConfig = {
            // Your config here
        };
        firebase.initializeApp(firebaseConfig);
        
        document.getElementById('postForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const title = document.getElementById('title').value;
            const categoryId = document.getElementById('categoryId').value;
            const imageUrl = document.getElementById('imageUrl').value;
            const sendNotif = document.getElementById('sendNotification').checked;
            
            // 1. Lưu bài viết vào database
            const newTipRef = firebase.database().ref('health_tips').push();
            await newTipRef.set({
                title: title,
                categoryId: categoryId,
                categoryName: getCategoryName(categoryId),
                imageUrl: imageUrl,
                createdAt: firebase.database.ServerValue.TIMESTAMP
            });
            
            const newTipId = newTipRef.key;
            
            // 2. Nếu tick checkbox → Gửi notification
            if (sendNotif) {
                const sendNotification = firebase.functions().httpsCallable('sendNewHealthTipNotification');
                
                try {
                    const result = await sendNotification({
                        healthTipId: newTipId,
                        title: title,
                        categoryId: categoryId,
                        imageUrl: imageUrl
                    });
                    
                    document.getElementById('result').innerHTML = 
                        `✅ Đã đăng bài và gửi thông báo đến ${result.data.sentCount} người dùng`;
                } catch (error) {
                    document.getElementById('result').innerHTML = 
                        `⚠️ Đã đăng bài nhưng gửi thông báo thất bại: ${error.message}`;
                }
            } else {
                document.getElementById('result').innerHTML = 
                    `✅ Đã đăng bài (không gửi thông báo)`;
            }
        });
        
        function getCategoryName(categoryId) {
            const categories = {
                'dinh_duong': 'Dinh dưỡng',
                'the_duc': 'Thể dục',
                'yoga': 'Yoga'
            };
            return categories[categoryId] || categoryId;
        }
    </script>
</body>
</html>
```

**2. Test trong Firebase Console:**
   - Functions → sendNewHealthTipNotification
   - Test với data:
   ```json
   {
     "healthTipId": "tip123",
     "title": "Test notification",
     "categoryId": "cat1"
   }
   ```

**3. Kiểm tra:**
   - Users nhận notification
   - Check logs để xem sent count

#### C. Test Smart Recommendations

**1. Setup user preferences:**

```java
// Trong app, user chọn categories quan tâm
DatabaseReference prefsRef = FirebaseDatabase.getInstance()
    .getReference("users")
    .child(userId)
    .child("notification_preferences");

Map<String, Object> prefs = new HashMap<>();
prefs.put("recommendations", true);
prefs.put("category_dinh_duong", true);  // User thích Dinh dưỡng
prefs.put("category_the_duc", true);     // User thích Thể dục
prefs.put("category_yoga", false);       // User không thích Yoga

prefsRef.updateChildren(prefs);
```

**2. Thêm bài viết test:**

```javascript
// Trong Firebase Console → Realtime Database
// Thêm vào /health_tips/
{
  "tip001": {
    "title": "Bài viết về Dinh dưỡng",
    "categoryId": "dinh_duong",
    "categoryName": "Dinh dưỡng"
  },
  "tip002": {
    "title": "Bài tập thể dục buổi sáng",
    "categoryId": "the_duc",
    "categoryName": "Thể dục"
  },
  "tip003": {
    "title": "Yoga cơ bản",
    "categoryId": "yoga",
    "categoryName": "Yoga"
  }
}
```

**3. Test scheduled function manually:**

```bash
# Gọi function manually (không cần đợi 18:00)
firebase functions:shell
> sendDailyRecommendations()
```

**4. Kiểm tra kết quả:**
   - User sẽ nhận 2 bài: tip001 (Dinh dưỡng) + tip002 (Thể dục)
   - KHÔNG nhận tip003 (Yoga) vì user không quan tâm
   - Check notification data có đúng format không

### 11.4. Lưu FCM Token vào Database

**Cập nhật trong app khi user login:**

```java
// Sau khi login thành công
FirebaseMessaging.getInstance().getToken()
    .addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
            String token = task.getResult();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            
            // Lưu token vào database
            FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("fcmToken")
                .setValue(token);
        }
    });
```

### 11.5. Setup Notification Preferences (Optional)

**Default preferences khi user mới đăng ký:**

```java
String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
DatabaseReference prefsRef = FirebaseDatabase.getInstance()
    .getReference("users")
    .child(userId)
    .child("notification_preferences");

Map<String, Object> defaultPrefs = new HashMap<>();
defaultPrefs.put("comment_replies", true);
defaultPrefs.put("comment_likes", true);
defaultPrefs.put("new_posts", true);          // Nhận thông báo bài viết mới từ Admin
defaultPrefs.put("recommendations", true);     // Nhận đề xuất hàng ngày
defaultPrefs.put("quiet_hours_enabled", false);

// Thêm categories mặc định (user có thể tùy chỉnh sau)
defaultPrefs.put("category_dinh_duong", true);
defaultPrefs.put("category_the_duc", true);
defaultPrefs.put("category_yoga", false);

prefsRef.setValue(defaultPrefs);
```

### 11.6. Admin Web - UI Example

**⚠️ LƯU Ý:** Admin là trang web riêng biệt, KHÔNG phải là phần của app Android này.

**Ví dụ UI cho Admin Web (HTML + Firebase SDK):**

Tham khảo code HTML đầy đủ trong **Section 11.3.B** (Test New Health Tip Notification).

**Các bước:**
1. ✅ Admin mở trang web quản trị
2. ✅ Điền form: Title, Category, Image URL
3. ✅ Tick checkbox: "Gửi thông báo đến tất cả người dùng"
4. ✅ Click "Đăng bài"
5. ✅ Code tự động:
   - Lưu bài viết vào Firebase Realtime Database
   - Nếu checkbox checked → Gọi Cloud Function `sendNewHealthTipNotification`
   - Hiển thị kết quả: "Đã gửi thông báo đến X người dùng"

**Screenshot flow:**
```
[Admin Web Form]
┌─────────────────────────────────────┐
│ Tiêu đề: [10 mẹo giữ sức khỏe...]  │
│ Category: [Dinh dưỡng ▼]            │
│ Image URL: [https://...]            │
│                                     │
│ ☑ Gửi thông báo đến người dùng      │
│                                     │
│        [Đăng bài]                   │
└─────────────────────────────────────┘
         ↓ (click)
┌─────────────────────────────────────┐
│ ✅ Đã đăng bài và gửi thông báo     │
│    đến 1,234 người dùng             │
└─────────────────────────────────────┘
```

---

## 12. KẾT LUẬN

### 12.1. Tóm tắt

Hệ thống Deep Linking với FCM Notifications đã được thiết kế để:
- ✅ Gửi thông báo thông minh cho người dùng
- ✅ Điều hướng chính xác đến nội dung cụ thể
- ✅ Tạo trải nghiệm liền mạch giống TikTok
- ✅ Hỗ trợ nhiều loại notification khác nhau
- ✅ Dễ dàng mở rộng cho các loại notification mới
- ✅ **Tự động hoàn toàn với Firebase Triggers - KHÔNG CẦN WEBHOOK**
- ✅ **Admin Web riêng - gửi notification từ trang quản trị**
- ✅ **Smart Recommendations - đề xuất cá nhân hóa cho từng user**
- ✅ **User preferences để tùy chỉnh thông báo và sở thích**

### 12.2. Roadmap tiếp theo

#### Phase 1: Core Implementation ✅
- [x] MyFirebaseMessagingService (Android App)
- [x] DeepLinkHandlerActivity (Android App)
- [x] Comment Reply Notifications (Real-time)
- [x] Firebase Database Triggers setup
- [x] Admin Web Control for New Posts (Trang web riêng)
- [x] Smart Daily Recommendations (18:00)
- [x] Category-based Personalization
- [x] Notification Preferences UI
- [x] Rate limiting

#### Phase 2: Enhancement 🚧
- [ ] Rich notifications với hình ảnh
- [ ] In-app notification center
- [ ] Weekly highlights
- [ ] ML-based recommendation scoring (thay vì first match)
- [ ] A/B testing cho notification content
- [ ] Notification sound customization
- [ ] Recommendation analytics (click-through rate)

#### Phase 3: Advanced Features 📋
- [ ] Smart notification timing (ML-based - gửi lúc user hay mở app)
- [ ] AI-powered content similarity matching
- [ ] Collaborative filtering recommendations
- [ ] Cross-device notification sync
- [ ] Interactive notifications (quick reply)
- [ ] Push notification A/B testing
- [ ] Notification engagement scoring
- [ ] User behavior tracking cho recommendations tốt hơn

### 12.4. Setup Checklist

**Để triển khai đầy đủ, cần:**

1. ✅ **Android App:**
   - [ ] MyFirebaseMessagingService (đã có)
   - [ ] DeepLinkHandlerActivity (tạo mới)
   - [ ] RecommendedTipsActivity (tạo mới - hiển thị đề xuất)
   - [ ] Cập nhật SingleVideoPlayerActivity
   - [ ] Cập nhật CommentBottomSheetFragment
   - [ ] NotificationPreferencesActivity (tạo mới)
   - [ ] Cập nhật AndroidManifest.xml

2. ✅ **Admin Web (Trang riêng biệt - KHÔNG phải app Android):**
   - [ ] Tạo trang web admin (HTML/React/Vue/Angular)
   - [ ] Form đăng bài viết
   - [ ] Tích hợp Firebase SDK (JavaScript)
   - [ ] Checkbox "Gửi thông báo đến người dùng"
   - [ ] Code gọi Cloud Function `sendNewHealthTipNotification`
   - [ ] Setup Firebase Authentication cho admin
   - [ ] Giao diện quản lý bài viết

3. ✅ **Firebase Cloud Functions:**
   - [ ] Setup Cloud Functions project
   - [ ] Deploy `sendCommentReplyNotification` (onCreate trigger)
   - [ ] Deploy `sendNewHealthTipNotification` (HTTPS Callable - Admin web gọi)
   - [ ] Deploy `queueHealthTipForRecommendation` (onCreate trigger)
   - [ ] Deploy `sendDailyRecommendations` (Scheduled 18:00)
   - [ ] Setup admin custom claims
   - [ ] Setup Firebase Database indexes
   - [ ] Configure Firebase Authentication

4. ✅ **Firebase Console:**
   - [ ] Enable Cloud Messaging
   - [ ] Enable Realtime Database
   - [ ] Enable Cloud Functions (Blaze plan - cần cho scheduled functions)
   - [ ] Setup database rules
   - [ ] Configure indexes
   - [ ] Add admin user email

5. ✅ **Testing:**
   - [ ] Test FCM tokens trong Android app
   - [ ] Test deep linking trong Android app
   - [ ] Test preferences UI trong Android app
   - [ ] **Test admin web đăng bài (không gửi notification)**
   - [ ] **Test admin web đăng bài + gửi notification**
   - [ ] Test daily recommendations (manual trigger)
   - [ ] Test category matching logic

### 12.5. Resources

**Firebase Documentation:**
- [FCM Documentation](https://firebase.google.com/docs/cloud-messaging)
- [Cloud Functions](https://firebase.google.com/docs/functions)
- [Notification Best Practices](https://firebase.google.com/docs/cloud-messaging/android/send-multiple)

**Android Documentation:**
- [Notification Guide](https://developer.android.com/develop/ui/views/notifications)
- [Deep Links](https://developer.android.com/training/app-links/deep-linking)
- [PendingIntent](https://developer.android.com/reference/android/app/PendingIntent)

---

## 13. TÍCH HỢP VERCEL BACKEND API

### 13.1. Tổng quan

Thay vì sử dụng Firebase Cloud Functions (yêu cầu Blaze Plan với billing), hệ thống đã được triển khai sử dụng **Vercel Serverless Functions** - hoàn toàn miễn phí.

**Backend Repository:**
- 📦 **GitHub:** https://github.com/vunameaut/healthtips-notifications-backend
- 🌐 **Production URL:** https://healthtips-notify.vercel.app
- ✅ **Status:** Đã deploy thành công
- 🔄 **Cron Job:** Đã setup tại Cron-job.org (chạy daily 18:00)

### 13.2. Các API Endpoints

#### 📌 Base URL
```
https://healthtips-notify.vercel.app
```

#### 📌 Endpoint 1: Send Comment Reply Notification
**URL:** `POST /api/send-comment-reply`

**Mô tả:** Gửi thông báo khi có bình luận mới

**Khi nào gọi:** Sau khi user tạo comment thành công trong Android app

**Request Body:**
```json
{
  "healthTipId": "tip123",
  "commentId": "comment456",
  "commentUserId": "user789",
  "commentContent": "Mẹo hay quá!",
  "healthTipTitle": "Uống nước mỗi ngày",
  "healthTipAuthorId": "user111"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "messageId": "projects/reminderwater-84694/messages/123456",
  "message": "Notification sent successfully"
}
```

**Response Error (401/500):**
```json
{
  "success": false,
  "error": "Failed to send notification",
  "details": "Error message"
}
```

---

#### 📌 Endpoint 2: Send New Health Tip Notification
**URL:** `POST /api/send-new-health-tip`

**Mô tả:** Gửi thông báo broadcast khi Admin đăng bài mới

**Khi nào gọi:** Từ Admin Web khi tick checkbox "Gửi thông báo"

**Request Body:**
```json
{
  "healthTipId": "tip789",
  "title": "10 mẹo giữ sức khỏe mùa đông",
  "category": "nutrition",
  "authorId": "admin_user_id"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "successCount": 45,
  "failureCount": 2,
  "totalTargets": 47,
  "message": "Notifications sent"
}
```

---

#### 📌 Endpoint 3: Queue Recommendation
**URL:** `POST /api/queue-recommendation`

**Mô tả:** Thêm mẹo sức khỏe vào hàng đợi gợi ý

**Khi nào gọi:** Sau khi tạo health tip mới thành công

**Request Body:**
```json
{
  "healthTipId": "tip123",
  "title": "Uống đủ nước mỗi ngày",
  "category": "nutrition"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "message": "Added to recommendation queue",
  "healthTipId": "tip123",
  "queuedAt": 1699999999000
}
```

---

#### 📌 Endpoint 4: Send Daily Recommendations
**URL:** `POST /api/send-daily-recommendations`

**Mô tả:** Gửi gợi ý cá nhân hóa hàng ngày

**⚠️ Endpoint này chỉ được gọi bởi Cron Job (đã setup)**

**Authorization:** Requires `Bearer hehehe` header

**Response Success (200):**
```json
{
  "success": true,
  "message": "Daily recommendations sent",
  "sentCount": 32,
  "failedCount": 1,
  "totalRecommendations": 5,
  "timestamp": "2025-11-11T11:00:00.000Z"
}
```

---

### 13.3. Code tích hợp Android

#### A. Thêm dependencies (build.gradle)

```gradle
dependencies {
    // Volley for HTTP requests
    implementation 'com.android.volley:volley:1.2.1'
    
    // Gson for JSON parsing
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // Existing Firebase dependencies
    implementation 'com.google.firebase:firebase-messaging:23.4.0'
    implementation 'com.google.firebase:firebase-database:20.3.0'
}
```

#### B. Tạo API Helper Class

```java
package com.healthtips.utils;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class VercelApiHelper {
    
    private static final String BASE_URL = "https://healthtips-notify.vercel.app";
    private static VercelApiHelper instance;
    private RequestQueue requestQueue;
    private Context context;
    
    private VercelApiHelper(Context context) {
        this.context = context.getApplicationContext();
        this.requestQueue = Volley.newRequestQueue(this.context);
    }
    
    public static synchronized VercelApiHelper getInstance(Context context) {
        if (instance == null) {
            instance = new VercelApiHelper(context);
        }
        return instance;
    }
    
    /**
     * Gửi thông báo comment reply
     */
    public void sendCommentReplyNotification(
            String healthTipId,
            String commentId,
            String commentUserId,
            String commentContent,
            String healthTipTitle,
            String healthTipAuthorId,
            ApiCallback callback) {
        
        try {
            String url = BASE_URL + "/api/send-comment-reply";
            
            JSONObject json = new JSONObject();
            json.put("healthTipId", healthTipId);
            json.put("commentId", commentId);
            json.put("commentUserId", commentUserId);
            json.put("commentContent", commentContent);
            json.put("healthTipTitle", healthTipTitle);
            json.put("healthTipAuthorId", healthTipAuthorId);
            
            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, json,
                response -> callback.onSuccess(response),
                error -> callback.onError(error.toString())
            );
            
            requestQueue.add(request);
            
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
    
    /**
     * Queue health tip for recommendation
     */
    public void queueHealthTipForRecommendation(
            String healthTipId,
            String title,
            String category,
            ApiCallback callback) {
        
        try {
            String url = BASE_URL + "/api/queue-recommendation";
            
            JSONObject json = new JSONObject();
            json.put("healthTipId", healthTipId);
            json.put("title", title);
            json.put("category", category);
            
            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, json,
                response -> callback.onSuccess(response),
                error -> callback.onError(error.toString())
            );
            
            requestQueue.add(request);
            
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
    
    /**
     * Gửi thông báo broadcast (Admin only)
     */
    public void sendNewHealthTipNotification(
            String healthTipId,
            String title,
            String category,
            String authorId,
            ApiCallback callback) {
        
        try {
            String url = BASE_URL + "/api/send-new-health-tip";
            
            JSONObject json = new JSONObject();
            json.put("healthTipId", healthTipId);
            json.put("title", title);
            json.put("category", category);
            json.put("authorId", authorId);
            
            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, json,
                response -> callback.onSuccess(response),
                error -> callback.onError(error.toString())
            );
            
            requestQueue.add(request);
            
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
    
    /**
     * Callback interface
     */
    public interface ApiCallback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }
}
```

#### C. Sử dụng trong Comment Activity

```java
// Sau khi user tạo comment thành công
private void onCommentCreated(Comment newComment) {
    // Lưu comment vào Firebase
    DatabaseReference commentRef = FirebaseDatabase.getInstance()
        .getReference("comments")
        .child(healthTipId)
        .push();
    
    commentRef.setValue(newComment).addOnSuccessListener(aVoid -> {
        // Thành công → Gọi API gửi notification
        VercelApiHelper.getInstance(this).sendCommentReplyNotification(
            healthTipId,
            commentRef.getKey(),
            currentUserId,
            newComment.getContent(),
            healthTipTitle,
            healthTipAuthorId,
            new VercelApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.d("Comment", "Notification sent successfully");
                }
                
                @Override
                public void onError(String error) {
                    Log.e("Comment", "Failed to send notification: " + error);
                    // Không block user, chỉ log lỗi
                }
            }
        );
    });
}
```

#### D. Sử dụng khi tạo Health Tip mới

```java
// Sau khi admin/user tạo health tip thành công
private void onHealthTipCreated(HealthTip newTip) {
    // Lưu vào Firebase
    DatabaseReference tipRef = FirebaseDatabase.getInstance()
        .getReference("healthTips")
        .push();
    
    tipRef.setValue(newTip).addOnSuccessListener(aVoid -> {
        String tipId = tipRef.getKey();
        
        // Tự động thêm vào queue recommendations
        VercelApiHelper.getInstance(this).queueHealthTipForRecommendation(
            tipId,
            newTip.getTitle(),
            newTip.getCategory(),
            new VercelApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.d("HealthTip", "Added to recommendation queue");
                }
                
                @Override
                public void onError(String error) {
                    Log.e("HealthTip", "Failed to queue: " + error);
                }
            }
        );
    });
}
```

#### E. Admin Web - Gửi broadcast notification

```java
// Trong Admin Activity/Fragment
private void publishHealthTipWithNotification(HealthTip tip, boolean sendNotification) {
    // Lưu tip vào Firebase
    DatabaseReference tipRef = FirebaseDatabase.getInstance()
        .getReference("healthTips")
        .push();
    
    tipRef.setValue(tip).addOnSuccessListener(aVoid -> {
        String tipId = tipRef.getKey();
        
        if (sendNotification) {
            // Admin chọn gửi thông báo
            VercelApiHelper.getInstance(this).sendNewHealthTipNotification(
                tipId,
                tip.getTitle(),
                tip.getCategory(),
                currentAdminUserId,
                new VercelApiHelper.ApiCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            int sentCount = response.getInt("successCount");
                            Toast.makeText(AdminActivity.this, 
                                "Đã gửi thông báo tới " + sentCount + " người dùng", 
                                Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e("Admin", "Parse response error", e);
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        Toast.makeText(AdminActivity.this, 
                            "Lỗi gửi thông báo: " + error, 
                            Toast.LENGTH_SHORT).show();
                    }
                }
            );
        } else {
            Toast.makeText(this, "Đã đăng bài viết", Toast.LENGTH_SHORT).show();
        }
    });
}
```

---

### 13.4. Monitoring & Logs

#### Xem logs trên Vercel:
1. Vào https://vercel.com/dashboard
2. Click project **healthtips-notify**
3. Tab **Deployments** → Click deployment mới nhất
4. Tab **Functions** → Chọn function
5. Tab **Logs** → Xem real-time logs

#### Xem lịch sử Cron Job:
1. Vào https://cron-job.org/
2. Click vào cronjob **"HealthTips Daily Recommendations"**
3. Xem **Execution history**
4. Kiểm tra status (200 = success)

---

### 13.5. Troubleshooting

#### Lỗi 401 Unauthorized (Daily Recommendations):
- **Nguyên nhân:** Cron job không gửi đúng Authorization header
- **Giải pháp:** Kiểm tra header `Authorization: Bearer hehehe` trong Cron-job.org

#### Lỗi 405 Method Not Allowed:
- **Nguyên nhân:** Đang dùng GET thay vì POST
- **Giải pháp:** Đảm bảo request method là POST

#### Không nhận được notification:
- **Kiểm tra:**
  1. User có FCM token chưa?
  2. User có bật notifications trong preferences chưa?
  3. Category có khớp với preferences không?
  4. Xem logs trên Vercel để debug

---

### 13.6. Checklist triển khai

- [x] ✅ Vercel backend đã deploy thành công
- [x] ✅ Cron job đã setup (18:00 daily)
- [x] ✅ Firebase credentials đã cấu hình
- [ ] ⬜ Tích hợp `VercelApiHelper` vào Android app
- [ ] ⬜ Gọi API khi tạo comment mới
- [ ] ⬜ Gọi API khi tạo health tip mới
- [ ] ⬜ Implement Admin Web broadcast feature
- [ ] ⬜ Test tất cả endpoints
- [ ] ⬜ Test cron job chạy đúng giờ

---

**Người lập:** AI Assistant  
**Ngày:** 11/11/2025  
**Phiên bản:** 2.0  
**Trạng thái:** Đã hoàn thành
