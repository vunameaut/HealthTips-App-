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

9.  **Phong cách và Giao diện người dùng:**
    * **Ứng dụng sẽ hỗ trợ hai chế độ giao diện:** Chế độ tối (Dark Mode) và Chế độ sáng (Light Mode).
    * **Chế độ tối (Dark Mode - Lấy cảm hứng từ `cu_black.png`):**
        * **Gam màu chủ đạo:**
            * **Nền (Background):** Các tông màu đen xám đậm (ví dụ: `#1A1A1D` hoặc `#212124`) để tạo cảm giác hiện đại, sang trọng và dịu mắt khi sử dụng trong điều kiện thiếu sáng.
            * **Chữ (Text):** Trắng tinh khiết (`#FFFFFF`) hoặc trắng xám nhạt (`#F0F0F0`) để đảm bảo độ tương phản cao và dễ đọc trên nền tối.
        * **Màu sắc các nút (Buttons):**
            * **Nút chính (Primary Buttons):** Sử dụng gradient chuyển màu từ xanh lá cây đậm (ví dụ: `#4CAF50` hoặc `#2E7D32`) sang vàng chanh (`#C0CA33` hoặc xanh lá cây nhạt hơn `#8BC34A`). Chữ trên nút phải là màu trắng (`#FFFFFF`).
            * **Nút phụ/Hành động khác (Secondary Buttons):** Sử dụng gradient chuyển màu từ cam tươi (`#FF9800` hoặc `#F57C00`) sang đỏ cam (`#FF5722` hoặc đỏ gạch `#D32F2F`). Chữ trên nút nên là màu trắng (`#FFFFFF`).
        * **Các thành phần UI khác:**
            * **Đường viền/phân cách:** Nên là màu xám nhạt (`#424242`) hoặc xám đậm hơn một chút so với nền để tạo sự phân tách tinh tế.
            * **Biểu tượng (Icons):** Màu trắng (`#FFFFFF`) hoặc các màu tương đồng với gam màu của các nút để duy trì sự nhất quán.
            * **Điểm nhấn/Highlight:** Có thể sử dụng các màu xanh dương sáng từ logo `cu_black.png` (ví dụ: `#00BFFF` hoặc `#1E90FF`) cho các yếu tố tương tác nhỏ, đường viền hoặc trạng thái được chọn.
    * **Chế độ sáng (Light Mode - Lấy cảm hứng từ `cu_night.png`):**
        * **Gam màu chủ đạo:**
            * **Nền (Background):** Các tông màu xanh dương nhạt hoặc trắng sáng (ví dụ: `#E0F2F7` hoặc `#FFFFFF`), tạo cảm giác tươi mới và dễ chịu.
            * **Chữ (Text):** Xám đậm (ví dụ: `#212124` hoặc `#424242`) hoặc đen (`#000000`) để đảm bảo độ tương phản trên nền sáng.
        * **Màu sắc các nút (Buttons):**
            * **Nút chính (Primary Buttons):** Sử dụng các tông màu xanh dương (ví dụ: `#2196F3` hoặc `#1976D2`) hoặc xanh lá cây (ví dụ: `#4CAF50`) tương tự như logo `cu_night.png`. Chữ trên nút phải là màu trắng (`#FFFFFF`).
            * **Nút phụ/Hành động khác (Secondary Buttons):** Sử dụng màu sắc tương phản nhưng vẫn hài hòa, ví dụ: màu xám đậm (`#616161`) hoặc cam nhạt (`#FFB74D`). Chữ trên nút nên là màu trắng (`#FFFFFF`) hoặc đen (`#000000`).
        * **Các thành phần UI khác:**
            * **Đường viền/phân cách:** Nên là màu xám nhạt (`#BDBDBD`) hoặc xanh nhạt (`#81D4FA`).
            * **Biểu tượng (Icons):** Màu đen (`#000000`), xanh lam đậm (`#1976D2`), hoặc các màu xanh từ logo `cu_night.png` để phù hợp với nền sáng.
            * **Điểm nhấn/Highlight:** Các màu cam/vàng từ bóng đèn trên logo `cu_night.png` (ví dụ: `#FFC107` hoặc `#FFEB3B`) có thể được sử dụng để làm nổi bật các yếu tố quan trọng hoặc trạng thái.
    * **Hiệu ứng chung:** Ưu tiên sử dụng gradient cho các nút để tạo chiều sâu và điểm nhấn trong cả hai chế độ. Đảm bảo tính nhất quán về hình dạng, khoảng cách và kiểu chữ giữa hai chế độ để trải nghiệm người dùng không bị gián đoạn.

10. **Tránh trùng lặp File và Chức năng:**
    * **Trước khi đề xuất hoặc tạo bất kỳ file mới nào (Activity, Fragment, Presenter, Model, Service, v.v.) hoặc triển khai một chức năng mới, Copilot phải kiểm tra kỹ lưỡng toàn bộ cấu trúc dự án hiện có.**
    * **Nếu phát hiện đã tồn tại một file hoặc một phần code thực hiện chức năng tương tự hoặc cùng tên trong dự án (dựa trên tên file, quy ước đặt tên, hoặc logic đã có), Copilot phải thông báo và KHÔNG tạo ra bản sao.**
    * **Thay vào đó, Copilot sẽ:**
        * **Đề xuất tích hợp hoặc mở rộng chức năng hiện có** vào file hoặc module đã tồn tại.
        * **Chỉ tạo file mới khi không có bất kỳ file hoặc chức năng tương tự nào tồn tại** và nó thực sự cần thiết cho một module hoặc tính năng mới hoàn toàn.
        * **Khi tiếp tục một chức năng đã được bắt đầu ở bước trước, Copilot phải tiếp tục làm việc trên các file đã được tạo ra cho chức năng đó, không tạo lại chúng ở thư mục khác.**
    * **Luôn ưu tiên việc tái sử dụng và mở rộng code hiện có.**

11. **Quản lý tài nguyên:**
    * **Tập trung tài nguyên vào các file chung:**
        * **Strings:** Tất cả chuỗi văn bản phải được đặt trong file `strings.xml` chung, không tạo file strings riêng cho từng tính năng.
        * **Colors:** Tất cả định nghĩa màu sắc phải được đặt trong file `colors.xml` chung, không tạo file colors riêng biệt.
        * **Styles/Themes:** Tất cả styles và themes phải được đặt trong file `styles.xml` hoặc `themes.xml` chung, không tạo file styles riêng.
        * **Dimensions:** Tất cả kích thước phải được đặt trong file `dimens.xml` chung.
    * **Đặt tên tài nguyên:**
        * Đặt tên theo cấu trúc `[feature]_[type]_[description]` (ví dụ: `category_title`, `home_description`, `auth_button_text`)
        * Đảm bảo tên mô tả đúng mục đích sử dụng và dễ hiểu
    * **Tránh trùng lặp tài nguyên:** Kiểm tra kỹ trước khi thêm tài nguyên mới, tái sử dụng tài nguyên hiện có nếu phù hợp.
    * **Tài nguyên hình ảnh:**
        * Vector Drawables (XML) được ưu tiên hơn bitmap cho biểu tượng và đồ họa đơn giản
        * Bitmap (PNG, JPEG) chỉ sử dụng cho hình ảnh phức tạp không thể biểu diễn bằng vector

---

## Những điều cần lưu ý khi sinh code:

* **Tính mô đun:** Tập trung vào việc tạo ra các thành phần nhỏ, có trách nhiệm duy nhất.
* **Tính khả dụng lại:** Viết code có thể tái sử dụng.
* **Xử lý lỗi:** Bao gồm các cơ chế xử lý lỗi phù hợp (ví dụ: try-catch, kiểm tra null).
* **Phản hồi UI:** Đảm bảo có phản hồi phù hợp cho người dùng khi thực hiện các thao tác (ví dụ: loading states, thông báo lỗi/thành công).
* **Bảo mật:** Lưu ý các vấn đề bảo mật cơ bản khi làm việc với dữ liệu người dùng và Firebase.
* **Hiệu suất:** Cân nhắc hiệu suất, đặc biệt là khi làm việc với dữ liệu lớn hoặc các thao tác UI phức tạp.
