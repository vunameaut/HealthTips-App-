Tuyệt vời! Để tích hợp phong cách màu sắc và giao diện mà bạn mong muốn vào bộ hướng dẫn, tôi sẽ thêm một mục mới là "9. Phong cách và Giao diện người dùng" ngay dưới mục "5. Giao diện người dùng (UI)" hiện có. Điều này sẽ giúp Copilot hiểu rõ hơn về các nguyên tắc thiết kế màu sắc mà bạn muốn áp dụng.

Đây là các instructions đã cập nhật:

```markdown
# Instructions cho GitHub Copilot - Dự án HealthTips App (Android)

Dự án này là ứng dụng mẹo sức khỏe trên Android, được phát triển bằng Java và tuân thủ kiến trúc MVP (Model-View-Presenter).

## Hướng dẫn chung:
0.  **Ngôn ngữ phản hồi:**
    * **Luôn viết phản hồi / mô tả bằng tiếng Việt.**
1.  **Ngôn ngữ và Kiến trúc:**
    * Sử dụng **Java** làm ngôn ngữ lập trình chính.
    * Tuân thủ chặt chẽ kiến trúc **MVP (Model-View-Presenter)** cho tất cả các màn hình và luồng logic.

2.  **Cấu trúc thư mục:**
    * Tổ chức code theo các thư mục sau:
        * `data/`: Chứa các lớp liên quan đến truy cập và quản lý dữ liệu (models, repositories, data sources, etc.).
        * `presentation/`: Chứa các lớp UI (Activities, Fragments), các Presenters và View interfaces.
        * `services/`: Chứa các Service (ví dụ: background services, reminder services).
        * `di/`: Chứa các module và component của Dagger 2 cho Dependency Injection.
        * `receivers/`: Chứa các Broadcast Receivers.
        * `utils/`: Chứa các lớp tiện ích chung, helpers, constants.

3.  **Tích hợp Firebase:**
    * Firebase đã được tích hợp đầy đủ và sẽ được sử dụng cho các chức năng sau:
        * **Firebase Authentication:** Quản lý đăng nhập/đăng ký người dùng.
        * **Cloud Firestore:** Lưu trữ dữ liệu cấu trúc (ví dụ: thông tin mẹo sức khỏe, hồ sơ người dùng).
        * **Firebase Storage:** Lưu trữ các file (ví dụ: hình ảnh cho mẹo sức khỏe).
        * **Firebase Realtime Database:** Có thể được sử dụng cho các tính năng thời gian thực đơn giản hơn nếu cần, nhưng ưu tiên Firestore.
        * **Firebase Cloud Messaging (FCM):** Gửi thông báo đẩy cho người dùng.

4.  **Dependency Injection:**
    * Sử dụng **Dagger 2** cho toàn bộ hệ thống Dependency Injection.
    * Đảm bảo các module và component được cấu hình đúng đắn để cung cấp các dependencies cần thiết cho Presenters, Repositories, Services, v.v.

5.  **Giao diện người dùng (UI):**
    * Giao diện được xây dựng bằng **XML**.
    * Tuân thủ nghiêm ngặt **Material Design 3** để đảm bảo tính nhất quán và trải nghiệm người dùng hiện đại.

**9. Phong cách và Giao diện người dùng:**
    * **Gam màu chủ đạo:**
        * **Nền (Background):** Sử dụng các tông màu đen xám đậm (#1A1A1D hoặc #212124) để tạo cảm giác hiện đại và sang trọng.
        * **Chữ (Text):** Trắng tinh khiết (#FFFFFF) hoặc trắng xám nhạt (#F0F0F0) để đảm bảo độ tương phản cao và dễ đọc trên nền tối.
    * **Màu sắc các nút (Buttons):**
        * **Nút chính (Primary Buttons - ví dụ: "Tập phim"):** Sử dụng gradient chuyển màu từ xanh lá cây đậm (#4CAF50 hoặc #2E7D32) sang vàng chanh (#C0CA33 hoặc xanh lá cây nhạt hơn #8BC34A). Chữ trên nút phải là màu trắng (#FFFFFF).
        * **Nút phụ/Hành động khác (Secondary Buttons - ví dụ: "Xem phim"):** Sử dụng gradient chuyển màu từ cam tươi (#FF9800 hoặc #F57C00) sang đỏ cam (#FF5722 hoặc đỏ gạch #D32F2F). Chữ trên nút nên là màu trắng (#FFFFFF) hoặc đen (#000000) tùy theo độ tương phản.
    * **Các thành phần UI khác:**
        * **Đường viền/phân cách:** Nên là màu xám nhạt (#424242) hoặc xám đậm hơn một chút so với nền để tạo sự phân tách tinh tế.
        * **Biểu tượng (Icons):** Sử dụng màu trắng (#FFFFFF) hoặc các màu tương đồng với gam màu của các nút để duy trì sự nhất quán.
    * **Hiệu ứng:** Ưu tiên sử dụng gradient cho các nút để tạo chiều sâu và điểm nhấn.

6.  **Quy ước đặt tên:**
    * Luôn tuân thủ quy ước đặt tên rõ ràng và nhất quán:
        * Adapter: `[Tên]Adapter.java` (ví dụ: `HealthTipAdapter.java`).
        * Presenter: `[Tên]Presenter.java` (ví dụ: `HomePresenter.java`, `LoginPresenter.java`).
        * Service: `[Tên]Service.java` (ví dụ: `ReminderService.java`, `NotificationService.java`).
        * Activity/Fragment: `[Tên]Activity.java`, `[Tên]Fragment.java`.
        * View Interface: `[Tên]Contract.java` (chứa `View` và `Presenter` interfaces lồng vào nhau) hoặc `[Tên]View.java`.
        * Model: `[Tên]Model.java` hoặc chỉ `[Tên].java` (ví dụ: `HealthTip.java`).
        * Repository: `[Tên]Repository.java`.

7.  **Kế thừa Presenter và View:**
    * Tất cả các Presenter phải kế thừa từ một lớp `BasePresenter` chung.
    * Tất cả các View interfaces (hoặc các lớp Activity/Fragment implement View interface) phải implement một interface `BaseView` chung.

8.  **Tuân thủ thiết kế:**
    * Luôn sinh code tuân thủ chặt chẽ **file thiết kế phân tích đã được cung cấp**, bao gồm cả việc chia module và chức năng đã định rõ.


## Những điều cần lưu ý khi sinh code:

* **Tính mô đun:** Tập trung vào việc tạo ra các thành phần nhỏ, có trách nhiệm duy nhất.
* **Tính khả dụng lại:** Viết code có thể tái sử dụng.
* **Xử lý lỗi:** Bao gồm các cơ chế xử lý lỗi phù hợp (ví dụ: try-catch, kiểm tra null).
* **Phản hồi UI:** Đảm bảo có phản hồi phù hợp cho người dùng khi thực hiện các thao tác (ví dụ: loading states, thông báo lỗi/thành công).
* **Bảo mật:** Lưu ý các vấn đề bảo mật cơ bản khi làm việc với dữ liệu người dùng và Firebase.
* **Hiệu suất:** Cân nhắc hiệu suất, đặc biệt là khi làm việc với dữ liệu lớn hoặc các thao tác UI phức tạp.
```
