# Báo cáo tổng quan dự án HealthTips

## 1. Tổng quan dự án

**Tên dự án:** HealthTips - Ứng dụng Mẹo Chăm Sóc Sức Khỏe Hàng Ngày

**Mục tiêu:** Xây dựng một hệ thống hoàn chỉnh bao gồm ứng dụng di động cho người dùng cuối và một trang web quản trị (admin panel) để quản lý nội dung, người dùng và các hoạt động của ứng dụng.

**Các thành phần chính:**

1.  **Ứng dụng Android (HealthTips-App):**
    *   Nền tảng cung cấp mẹo sức khỏe, video, nhắc nhở, và tương tác với chatbot AI cho người dùng.
    *   Được xây dựng bằng Java Native với kiến trúc MVP.

2.  **Trang web quản trị (healthtips-admin):**
    *   Công cụ cho phép quản trị viên quản lý toàn bộ nội dung (bài viết, video, danh mục), người dùng, xem phân tích, gửi thông báo và hỗ trợ người dùng.
    *   Được xây dựng bằng Next.js (React) và triển khai trên Vercel.

---

## 2. Phân tích ứng dụng Android

### 2.1. Công nghệ và Kiến trúc

*   **Ngôn ngữ:** Java
*   **Kiến trúc:** MVP (Model-View-Presenter)
*   **Min SDK:** 24 (Android 7.0)
*   **UI:** XML Layouts với Material Design 3, hỗ trợ Dark Mode & Light Mode.
*   **Database chính:** Firebase Realtime Database
*   **Xác thực:** Firebase Authentication
*   **Lưu trữ file:** Firebase Storage & Cloudinary
*   **Thông báo:** Firebase Cloud Messaging (FCM)
*   **Phân tích & Báo cáo lỗi:** Firebase Analytics & Crashlytics
*   **Dependency Injection:** Dagger 2
*   **Đa ngôn ngữ:** Hỗ trợ 5 ngôn ngữ (Tiếng Việt, Anh, Trung, Nhật, Hàn).

### 2.2. Cấu trúc thư mục chính

Dự án được tổ chức theo mô hình MVP, phân tách rõ ràng các thành phần:

```
app/src/main/java/com/vhn/doan/
├── data/              # Lớp dữ liệu: Models, Repositories, Firebase access
├── presentation/      # Lớp trình bày: Activities, Fragments, Presenters, Views
├── services/          # Các dịch vụ chạy nền (FCM, Reminders)
├── utils/             # Các lớp tiện ích
├── receivers/         # BroadcastReceivers (khởi động, báo thức)
└── workers/           # WorkManager cho các tác vụ nền
```

### 2.3. Mô hình dữ liệu (Data Models)

Các đối tượng dữ liệu chính bao gồm:
*   `User`: Thông tin người dùng.
*   `HealthTip`: Nội dung bài viết mẹo sức khỏe.
*   `Category`: Danh mục cho các mẹo.
*   `Video`: Thông tin các video.
*   `Reminder`: Dữ liệu nhắc nhở của người dùng.
*   `ChatMessage`: Tin nhắn trong chatbot.
*   `Favorite`, `Comment`, `SupportTicket`, v.v.

### 2.4. Tích hợp Firebase

*   **Realtime Database:** Là cơ sở dữ liệu chính, lưu trữ dữ liệu có cấu trúc như `users`, `health_tips`, `categories`, `chat-sessions`. Cấu trúc dạng cây JSON giúp đồng bộ hóa dữ liệu thời gian thực hiệu quả.
*   **Authentication:** Quản lý đăng nhập, đăng ký qua Email/Password.
*   **Storage:** Lưu trữ các file media như hình ảnh đại diện.
*   **Cloud Messaging (FCM):** Gửi thông báo đẩy (push notifications) từ admin và thông báo cục bộ cho các tính năng như nhắc nhở.
*   **Analytics & Crashlytics:** Theo dõi hành vi người dùng và báo cáo lỗi tự động.

### 2.5. Các tính năng chính

*   **Xác thực người dùng:** Đăng ký, đăng nhập, quên mật khẩu.
*   **Trang chủ:** Hiển thị danh mục và các mẹo phổ biến.
*   **Quản lý nội dung:** Xem chi tiết bài viết, video. Hỗ trợ hiển thị nội dung đa dạng (rich content).
*   **Tương tác:** Thích, chia sẻ, bình luận video.
*   **Cá nhân hóa:** Quản lý thông tin cá nhân, avatar, danh sách yêu thích, lịch sử xem.
*   **Nhắc nhở:** Tạo, sửa, xóa lịch nhắc nhở (uống nước, tập thể dục) với thông báo đẩy.
*   **Tìm kiếm:** Tìm kiếm toàn cục trong các mẹo và video.
*   **Chatbot:** Giao diện chat với AI để nhận gợi ý về sức khỏe.
*   **Hỗ trợ:** Gửi ticket và chat trực tiếp với admin.
*   **Cài đặt:** Thay đổi ngôn ngữ, giao diện (Sáng/Tối), cài đặt thông báo.

---

## 3. Phân tích Trang web Admin (healthtips-admin)

### 3.1. Công nghệ và Triển khai

*   **Framework:** Next.js (React)
*   **Ngôn ngữ:** TypeScript
*   **UI:** Material-UI (MUI)
*   **Backend & Database:** Tương tác trực tiếp với Firebase (Realtime Database, Auth).
*   **Lưu trữ Media:** Tích hợp Cloudinary để upload và quản lý video, hình ảnh.
*   **Triển khai:** Tự động triển khai qua Vercel.
*   **URL Production:** [https://healthtips-admin-fxbnt4896-vunams-projects-d3582d4f.vercel.app](https://healthtips-admin-fxbnt4896-vunams-projects-d3582d4f.vercel.app)

### 3.2. Các tính năng chính

*   **Dashboard:** Bảng điều khiển tổng quan với các số liệu thống kê (người dùng, bài viết, video) được biểu diễn qua biểu đồ.
*   **Quản lý nội dung (CRUD):**
    *   Quản lý bài viết (Health Tips) với trình soạn thảo văn bản đa dạng (rich text editor).
    *   Quản lý video, tích hợp upload lên Cloudinary.
    *   Quản lý danh mục, tags, và collections (nhóm bài viết).
*   **Quản lý người dùng:** Xem danh sách, thông tin chi tiết, phân quyền (RBAC), và quản lý trạng thái tài khoản.
*   **Hệ thống thông báo:** Soạn và gửi thông báo đẩy (FCM) đến tất cả hoặc một nhóm người dùng cụ thể.
*   **Hệ thống hỗ trợ:** Xem và trả lời các ticket hỗ trợ từ người dùng qua giao diện chat thời gian thực.
*   **Phân tích & Báo cáo:** Xem báo cáo về hoạt động của người dùng, hiệu suất nội dung và các chỉ số KPI.
*   **AI Features:** Công cụ tạo gợi ý cá nhân hóa cho người dùng.

---

## 4. Luồng dữ liệu và Bảo mật

### 4.1. Luồng dữ liệu

1.  **Admin -> App:**
    *   Admin tạo/cập nhật nội dung (bài viết, video) trên **Web Admin**.
    *   Dữ liệu được lưu vào **Firebase Realtime Database** và **Cloudinary**.
    *   **Ứng dụng Android** lắng nghe thay đổi từ Realtime Database và hiển thị nội dung mới cho người dùng.
2.  **User -> App -> Admin:**
    *   Người dùng tương tác trên **Ứng dụng Android** (ví dụ: tạo tài khoản, gửi ticket hỗ trợ).
    *   Dữ liệu được ghi vào **Firebase Realtime Database**.
    *   **Web Admin** đọc dữ liệu này và hiển thị cho quản trị viên (ví dụ: ticket mới, người dùng mới).

### 4.2. Bảo mật

*   **Firebase Security Rules:** Cả Realtime Database và Storage đều được cấu hình luật bảo mật chi tiết.
    *   Người dùng chỉ có thể đọc/ghi dữ liệu của chính mình (ví dụ: thông tin cá nhân, nhắc nhở).
    *   Nội dung công khai (bài viết, danh mục) chỉ cho phép đọc.
    *   Chỉ những người dùng có cờ `isAdmin == true` mới có quyền ghi vào các mục nội dung.
*   **ProGuard (Android):** Mã nguồn được làm rối khi build bản release để chống dịch ngược.
*   **Bảo mật phía Web Admin:**
    *   Các route được bảo vệ, yêu cầu đăng nhập với quyền admin.
    *   Sử dụng biến môi trường (.env) để lưu các khóa API và thông tin nhạy cảm.
