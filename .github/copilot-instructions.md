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
        * **Firebase Realtime Database:** **Đây là cơ sở dữ liệu chính được sử dụng để lưu trữ dữ liệu cấu trúc (ví dụ: thông tin mẹo sức khỏe, danh mục, hồ sơ người dùng) và quản lý dữ liệu thời gian thực.**
        * **Firebase Storage:** Lưu trữ các file (ví dụ: hình ảnh cho mẹo sức khỏe).
        * **Firebase Cloud Messaging (FCM):** Gửi thông báo đẩy cho người dùng.
        * **Cloud Firestore:** **Không phải là cơ sở dữ liệu chính cho dữ liệu cấu trúc hiện tại.** Có thể được sử dụng tùy chọn cho các tính năng đặc thù trong tương lai nếu cần.

4.  **Dependency Injection:**
    * Sử dụng **Dagger 2** cho toàn bộ hệ thống Dependency Injection.
    * Đảm bảo các module và component được cấu hình đúng đắn để cung cấp các dependencies cần thiết cho Presenters, Repositories, Services, v.v.

5.  **Giao diện người dùng (UI):**
    * Giao diện được xây dựng bằng **XML**.
    * Tuân thủ nghiêm ngặt **Material Design 3** để đảm bảo tính nhất quán và trải nghiệm người dùng hiện đại.

6.  **Kiểm tra Resources để tránh lỗi Duplicate:**
    * **Trước khi tạo hoặc đề xuất bất kỳ resource mới nào (string, color, drawable, dimen, style, etc.), Copilot PHẢI kiểm tra kỹ lưỡng các file resource hiện có trong dự án để tránh lỗi "Duplicate resources".**
    * **Quy trình kiểm tra bắt buộc:**
        * **Kiểm tra strings.xml:** Trước khi thêm string mới, phải kiểm tra tất cả các file `res/values*/strings.xml` để đảm bảo tên string chưa tồn tại.
        * **Kiểm tra colors.xml:** Trước khi thêm màu mới, phải kiểm tra tất cả các file `res/values*/colors.xml` để đảm bảo tên màu chưa tồn tại.
        * **Kiểm tra styles.xml:** Trước khi thêm style mới, phải kiểm tra file `res/values*/styles.xml` để tránh trùng lặp.
        * **Kiểm tra drawables:** Kiểm tra thư mục `res/drawable*` để tránh trùng tên file drawable.
        * **Kiểm tra dimensions:** Kiểm tra file `res/values*/dimens.xml` để tránh trùng lặp dimension.
    * **Hành động khi phát hiện trùng lặp:**
        * **Nếu resource đã tồn tại và có cùng giá trị:** Sử dụng lại resource hiện có thay vì tạo mới.
        * **Nếu resource đã tồn tại nhưng có giá trị khác:** Đặt tên mới có ý nghĩa và mô tả rõ ràng (ví dụ: `button_text_primary` thay vì `button_text` nếu đã có).
        * **Luôn ưu tiên việc tái sử dụng resource hiện có** để duy trì tính nhất quán trong thiết kế.
    * **Quy ước đặt tên resource:**
        * **String:** Sử dụng format `[component]_[purpose]_[detail]` (ví dụ: `home_button_save`, `dialog_message_error`).
        * **Color:** Sử dụng format `[theme]_[component]_[state]` (ví dụ: `primary_button_normal`, `dark_background_surface`).
        * **Drawable:** Sử dụng format `[type]_[component]_[state]` (ví dụ: `ic_home_selected`, `bg_button_pressed`).
        * **Tránh đặt tên chung chung** như `text`, `color`, `background` mà hãy cụ thể hóa mục đích sử dụng.

7.  **Quy ước đặt tên:**
    * Luôn tuân thủ quy ước đặt tên rõ ràng và nhất quán:
        * Adapter: `[Tên]Adapter.java` (ví dụ: `HealthTipAdapter.java`).
        * Presenter: `[Tên]Presenter.java` (ví dụ: `HomePresenter.java`, `LoginPresenter.java`).
        * Service: `[Tên]Service.java` (ví dụ: `ReminderService.java`, `NotificationService.java`).
        * Activity/Fragment: `[Tên]Activity.java`, `[Tên]Fragment.java`.
        * View Interface: `[Tên]Contract.java` (chứa `View` và `Presenter` interfaces lồng vào nhau) hoặc `[Tên]View.java`.
        * Model: `[Tên]Model.java` hoặc chỉ `[Tên].java` (ví dụ: `HealthTip.java`).
        * Repository: `[Tên]Repository.java`.

8.  **Kế thừa Presenter và View:**
    * Tất cả các Presenter phải kế thừa từ một lớp `BasePresenter` chung.
    * Tất cả các View interfaces (hoặc các lớp Activity/Fragment implement View interface) phải implement một interface `BaseView` chung.

9.  **Tuân thủ thiết kế:**
    * Luôn sinh code tuân thủ chặt chẽ **file thiết kế phân tích đã được cung cấp**, bao gồm cả việc chia module và chức năng đã định rõ.

10. **Phong cách và Giao diện người dùng:**
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
        * **Màu sắc các nút (Buttons):
            * **Nút chính (Primary Buttons):** Sử dụng các tông màu xanh dương (ví dụ: `#2196F3` hoặc `#1976D2`) hoặc xanh lá cây (ví dụ: `#4CAF50`) tương tự như logo `cu_night.png`. Chữ trên nút phải là màu trắng (`#FFFFFF`).
            * **Nút phụ/Hành động khác (Secondary Buttons):** Sử dụng màu sắc tương phản nhưng vẫn hài hòa, ví dụ: màu xám đậm (`#616161`) hoặc cam nhạt (`#FFB74D`). Chữ trên nút nên là màu trắng (`#FFFFFF`) hoặc đen (`#000000`).
        * **Các thành phần UI khác:**
            * **Đường viền/phân cách:** Nên là màu xám nhạt (`#BDBDBD`) hoặc xanh nhạt (`#81D4FA`).
            * **Biểu tượng (Icons):** Màu đen (`#000000`), xanh lam đậm (`#1976D2`), hoặc các màu xanh từ logo `cu_night.png` để phù hợp với nền sáng.
            * **Điểm nhấn/Highlight:** Các màu cam/vàng từ bóng đèn trên logo `cu_night.png` (ví dụ: `#FFC107` hoặc `#FFEB3B`) có thể được sử dụng để làm nổi bật các yếu tố quan trọng hoặc trạng thái.
    * **Hiệu ứng chung:** Ưu tiên sử dụng gradient cho các nút để tạo chiều sâu và điểm nhấn trong cả hai chế độ. Đảm bảo tính nhất quán về hình dạng, khoảng cách và kiểu chữ giữa hai chế độ để trải nghiệm người dùng không bị gián đoạn.

11. **Tránh trùng lặp File và Chức năng:**
    * **Trước khi đề xuất hoặc tạo bất kỳ file mới nào (Activity, Fragment, Presenter, Model, Service, v.v.) hoặc triển khai một chức năng mới, Copilot phải kiểm tra kỹ lưỡng toàn bộ cấu trúc dự án hiện có.**
    * **Nếu phát hiện đã tồn tại một file hoặc một phần code thực hiện chức năng tương tự hoặc cùng tên trong dự án (dựa trên tên file, quy ước đặt tên, hoặc logic đã có), Copilot phải thông báo và KHÔNG tạo ra bản sao.**
    * **Thay vào đó, Copilot sẽ:**
        * **Đề xuất tích hợp hoặc mở rộng chức năng hiện có** vào file hoặc module đã tồn tại.
        * **Chỉ tạo file mới khi không có bất kỳ file hoặc chức năng tương tự nào tồn tại** và nó thực sự cần thiết cho một module hoặc tính năng mới hoàn toàn.
        * **Khi tiếp tục một chức năng đã được bắt đầu ở bước trước, Copilot phải tiếp tục làm việc trên các file đã được tạo ra cho chức năng đó, không tạo lại chúng ở thư mục khác.**
    * **Luôn ưu tiên việc tái sử dụng và mở rộng code hiện có.**

12. **Chi tiết Phân tích Dự án:**
    * **Để có cái nhìn toàn diện và chi tiết về cấu trúc dự án, các thành phần công nghệ, mô hình dữ liệu, quy tắc bảo mật, và lộ trình phát triển, Copilot HÃY THAM KHẢO file `Project_Analysis_Details.md` được cung cấp trong cùng thư mục dự án.**
    * **File này chứa thông tin chi tiết về:**
        * Tổng quan dự án (SDK, Architecture, Database, Authentication, v.v.).
        * Cấu trúc thư mục chi tiết.
        * Định nghĩa các Data Models (User, HealthTip, Category, Reminder, ChatMessage, Enums).
        * Cấu hình Firebase (FirebaseManager, Firestore Collections Structure).
        * Chi tiết triển khai ki��n trúc MVP (Base Classes, Repository Pattern).
        * Triển khai đa ngôn ngữ (LocaleHelper, cấu trúc resource, ví dụ strings.xml).
        * Các Activity và Fragment chính.
        * Hệ thống thông báo (FCMService, ReminderService).
        * Hệ thống Chat Bot.
        * Permissions và cấu hình AndroidManifest.xml.
        * Cấu hình Build (build.gradle).
        * Class Application.
        * Cấu hình Dependency Injection với Dagger 2.
        * Chiến lược kiểm thử (Unit Tests, Presenter Tests).
        * Ưu tiên triển khai tính năng theo từng Phase.
        * Các quy tắc bảo mật Firebase và ProGuard.
        * Monitoring & Analytics (Firebase Analytics Events).

13. **Hướng dẫn theo ngữ cảnh Chức năng:**
    * **Copilot PHẢI chủ động đọc và phân tích file `Project_Analysis_Details.md` dựa trên chức năng hoặc nhiệm vụ mà người dùng đang thực hiện.**
    * **Khi người dùng yêu cầu thực hiện một tác vụ hoặc đang làm việc trong một file cụ thể, Copilot cần:**
        * **Xác định chức năng liên quan:** Ví dụ, nếu người dùng đang chỉnh sửa `HomeFragment.java` hoặc yêu cầu "tải dữ liệu trang chủ", Copilot phải hiểu rằng đây là một phần của "Home Fragment với MVP" và "Phase 1 (MVP)".
        * **Truy xuất thông tin liên quan:** Tìm kiếm các phần trong `Project_Analysis_Details.md` mô tả chi tiết về chức năng đó, bao gồm:
            * **Mô hình dữ liệu (Models):** Các Model liên quan (ví dụ: `HealthTip`, `Category`).
            * **Giao diện (Views):** Giao diện View tương ứng (ví dụ: `HomeView`).
            * **Presenter:** Presenter liên quan (ví dụ: `HomePresenter`).
            * **Repository:** Các Repository được sử dụng (ví dụ: `HealthTipRepository`, `CategoryRepository`).
            * **Cấu trúc Firebase:** Các Collection hoặc cấu trúc dữ liệu Firebase liên quan.
            * **Quy ước đặt tên:** Tên file và thư mục chính xác theo quy ước.
            * **Ưu tiên triển khai (Implementation Priority):** Xác định giai đoạn hiện tại của chức năng để gợi ý các bước tiếp theo phù hợp với lộ trình dự án.
        * **Đề xuất và Hỗ trợ thông minh:** Dựa trên thông tin đã truy xuất, Copilot sẽ đưa ra các gợi ý code, giải thích, hoặc các bước tiếp theo một cách chính xác và phù hợp với thiết kế tổng thể của dự án.
        * **Nếu không rõ ngữ cảnh:** Nếu Copilot không thể xác định rõ chức năng hiện tại hoặc các thông tin liên quan trong `Project_Analysis_Details.md`, nó sẽ hỏi người dùng để làm rõ.

---

## Những điều cần lưu ý khi sinh code:

* **Tính mô đun:** Tập trung vào việc tạo ra các thành phần nhỏ, có trách nhiệm duy nhất.
* **Tính khả dụng lại:** Viết code có thể tái sử dụng.
* **Xử lý lỗi:** Bao gồm các cơ chế xử lý lỗi phù hợp (ví dụ: try-catch, kiểm tra null).
* **Phản hồi UI:** Đảm bảo có phản hồi phù hợp cho người dùng khi thực hiện các thao tác (ví dụ: loading states, thông báo lỗi/thành công).
* **Bảo mật:** Lưu ý các vấn đề bảo mật cơ bản khi làm việc với dữ liệu người dùng và Firebase.
* **Hiệu suất:** Cân nhắc hiệu suất, đặc biệt là khi làm việc với dữ liệu lớn hoặc các thao tác UI phức tạp.
* **Kiểm tra Resources:** Luôn kiểm tra sự tồn tại của resources trước khi tạo mới để tránh lỗi duplicate.

---

**Lưu ý quan trọng cho việc phát triển (Từ Project_Analysis_Details.md):**

* Luôn sử dụng Java best practices và tuân thủ coding conventions.
* Implement proper error handling cho tất cả Firebase operations.
* Test trên nhiều thiết bị và API levels khác nhau.
* Optimize cho performance - đặc biệt là loading times và memory usage.
* Follow Material Design guidelines cho UI/UX consistency.
* Implement proper lifecycle management cho Activities và Fragments.
* Use proper logging với different levels (DEBUG, INFO, WARNING, ERROR).
* Secure sensitive data và không hardcode API keys trong source code.
* Implement offline capabilities cho user experience tốt hơn.
* Regular backup Firebase data và có disaster recovery plan.

**Đối với đa ngôn ngữ (Từ Project_Analysis_Details.md):**

* Luôn sử dụng string resources thay vì hardcode text.
* Test UI layout với các ngôn ngữ có text dài (German) và ngắn (Chinese).
* Implement proper RTL support cho các ngôn ngữ như Arabic (nếu cần).
* Use appropriate fonts cho các ngôn ngữ khác nhau.
* **Kiểm tra tất cả file strings.xml** (bao gồm cả các thư mục values-xx cho đa ngôn ngữ) trước khi thêm string mới để tránh duplicate resources.
