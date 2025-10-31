package com.vhn.doan.presentation.settings.content;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.vhn.doan.R;

/**
 * Activity hiển thị chi tiết điều khoản và chính sách
 */
public class TermsPolicyDetailActivity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_policy_detail);

        setupViews();
        loadContent();
    }

    private void setupViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        tvTitle = findViewById(R.id.tvTitle);
        tvContent = findViewById(R.id.tvContent);
    }

    private void loadContent() {
        String typeString = getIntent().getStringExtra("type");
        String title = getIntent().getStringExtra("title");

        tvTitle.setText(title);

        if (typeString != null) {
            TermsPolicyActivity.TermsPolicyType type =
                TermsPolicyActivity.TermsPolicyType.valueOf(typeString);

            String content = getContentForType(type);
            tvContent.setText(content);
        }
    }

    private String getContentForType(TermsPolicyActivity.TermsPolicyType type) {
        switch (type) {
            case TERMS_OF_SERVICE:
                return getTermsOfService();
            case PRIVACY_POLICY:
                return getPrivacyPolicy();
            case COMMUNITY_GUIDELINES:
                return getCommunityGuidelines();
            case COPYRIGHT_POLICY:
                return getCopyrightPolicy();
            default:
                return "";
        }
    }

    private String getTermsOfService() {
        return "ĐIỀU KHOẢN DỊCH VỤ\n\n" +
                "Cập nhật lần cuối: " + getCurrentDate() + "\n\n" +

                "1. CHẤP NHẬN ĐIỀU KHOẢN\n\n" +
                "Bằng việc truy cập và sử dụng ứng dụng HealthTips, bạn đồng ý tuân thủ và bị ràng buộc bởi các điều khoản và điều kiện sau đây. Nếu bạn không đồng ý với bất kỳ phần nào của các điều khoản này, bạn không được phép sử dụng dịch vụ của chúng tôi.\n\n" +

                "2. MÔ TẢ DỊCH VỤ\n\n" +
                "HealthTips là một ứng dụng cung cấp thông tin và mẹo về sức khỏe. Chúng tôi cung cấp nội dung giáo dục và thông tin chăm sóc sức khỏe, nhưng không thay thế cho lời khuyên y tế chuyên nghiệp.\n\n" +

                "3. TÀI KHOẢN NGƯỜI DÙNG\n\n" +
                "• Bạn phải từ 13 tuổi trở lên để sử dụng dịch vụ này\n" +
                "• Bạn chịu trách nhiệm duy trì tính bảo mật của tài khoản\n" +
                "• Bạn chịu trách nhiệm về tất cả hoạt động diễn ra dưới tài khoản của mình\n" +
                "• Bạn phải cung cấp thông tin chính xác và cập nhật\n\n" +

                "4. HÀNH VI NGƯỜI DÙNG\n\n" +
                "Bạn đồng ý KHÔNG:\n" +
                "• Sử dụng dịch vụ cho bất kỳ mục đích bất hợp pháp nào\n" +
                "• Đăng nội dung xúc phạm, quấy rối, hoặc vi phạm quyền của người khác\n" +
                "• Cố gắng truy cập trái phép vào hệ thống\n" +
                "• Sử dụng bot, script hoặc công cụ tự động\n" +
                "• Thu thập thông tin người dùng khác\n\n" +

                "5. NỘI DUNG\n\n" +
                "• Tất cả nội dung chỉ mang tính chất tham khảo\n" +
                "• Không thay thế cho tư vấn y tế chuyên nghiệp\n" +
                "• Chúng tôi không chịu trách nhiệm về kết quả từ việc sử dụng thông tin\n" +
                "• Nội dung có thể thay đổi mà không cần thông báo\n\n" +

                "6. QUYỀN SỞ HỮU TRÍ TUỆ\n\n" +
                "Tất cả nội dung, tính năng và chức năng của ứng dụng là tài sản của HealthTips và được bảo vệ bởi luật bản quyền quốc tế.\n\n" +

                "7. CHẤM DỨT\n\n" +
                "Chúng tôi có quyền tạm ngưng hoặc chấm dứt quyền truy cập của bạn vào dịch vụ ngay lập tức, mà không cần thông báo trước, nếu bạn vi phạm các điều khoản này.\n\n" +

                "8. TỪ CHỐI BẢO ĐẢM\n\n" +
                "Dịch vụ được cung cấp \"như hiện có\" và \"như có sẵn\" mà không có bất kỳ bảo đảm nào, dù rõ ràng hay ngụ ý.\n\n" +

                "9. GIỚI HẠN TRÁCH NHIỆM\n\n" +
                "Trong mọi trường hợp, HealthTips sẽ không chịu trách nhiệm về bất kỳ thiệt hại trực tiếp, gián tiếp, ngẫu nhiên, đặc biệt hoặc do hậu quả nào phát sinh từ việc sử dụng hoặc không thể sử dụng dịch vụ.\n\n" +

                "10. THAY ĐỔI ĐIỀU KHOẢN\n\n" +
                "Chúng tôi có quyền sửa đổi các điều khoản này bất cứ lúc nào. Việc tiếp tục sử dụng dịch vụ sau những thay đổi đó đồng nghĩa với việc bạn chấp nhận các điều khoản mới.\n\n" +

                "11. LUẬT ĐIỀU CHỈNH\n\n" +
                "Các điều khoản này được điều chỉnh bởi luật pháp Việt Nam.\n\n" +

                "12. LIÊN HỆ\n\n" +
                "Nếu bạn có bất kỳ câu hỏi nào về các Điều khoản này, vui lòng liên hệ chúng tôi qua email: vuhoainam.dev@gmail.com";
    }

    private String getPrivacyPolicy() {
        return "CHÍNH SÁCH BẢO MẬT\n\n" +
                "Cập nhật lần cuối: " + getCurrentDate() + "\n\n" +

                "1. GIỚI THIỆU\n\n" +
                "HealthTips (\"chúng tôi\", \"của chúng tôi\") cam kết bảo vệ quyền riêng tư của bạn. Chính sách bảo mật này giải thích cách chúng tôi thu thập, sử dụng, tiết lộ và bảo vệ thông tin của bạn khi sử dụng ứng dụng di động của chúng tôi.\n\n" +

                "2. THÔNG TIN CHÚNG TÔI THU THẬP\n\n" +
                "2.1. Thông tin bạn cung cấp:\n" +
                "• Tên và địa chỉ email khi đăng ký\n" +
                "• Ảnh đại diện (tùy chọn)\n" +
                "• Thông tin hồ sơ cá nhân\n" +
                "• Nội dung bạn tạo hoặc chia sẻ\n" +
                "• Thông tin liên hệ khi bạn gửi yêu cầu hỗ trợ\n\n" +

                "2.2. Thông tin tự động thu thập:\n" +
                "• Loại thiết bị và hệ điều hành\n" +
                "• Địa chỉ IP\n" +
                "• Dữ liệu sử dụng ứng dụng\n" +
                "• Thông tin về sự cố và lỗi\n" +
                "• Cookies và công nghệ tương tự\n\n" +

                "3. CÁCH CHÚNG TÔI SỬ DỤNG THÔNG TIN\n\n" +
                "Chúng tôi sử dụng thông tin để:\n" +
                "• Cung cấp và duy trì dịch vụ\n" +
                "• Cá nhân hóa trải nghiệm người dùng\n" +
                "• Cải thiện ứng dụng và phát triển tính năng mới\n" +
                "• Gửi thông báo và cập nhật quan trọng\n" +
                "• Phân tích xu hướng và hoạt động người dùng\n" +
                "• Phát hiện và ngăn chặn gian lận\n" +
                "• Tuân thủ các nghĩa vụ pháp lý\n\n" +

                "4. CHIA SẺ THÔNG TIN\n\n" +
                "Chúng tôi KHÔNG bán thông tin cá nhân của bạn. Chúng tôi chỉ chia sẻ thông tin trong các trường hợp:\n" +
                "• Với nhà cung cấp dịch vụ đáng tin cậy (Firebase, Analytics)\n" +
                "• Khi được yêu cầu bởi pháp luật\n" +
                "• Để bảo vệ quyền và an toàn của chúng tôi và người khác\n" +
                "• Với sự đồng ý của bạn\n\n" +

                "5. BẢO MẬT DỮ LIỆU\n\n" +
                "Chúng tôi thực hiện các biện pháp bảo mật kỹ thuật và tổ chức phù hợp:\n" +
                "• Mã hóa dữ liệu trong quá trình truyền tải (SSL/TLS)\n" +
                "• Mã hóa dữ liệu lưu trữ\n" +
                "• Kiểm soát truy cập nghiêm ngặt\n" +
                "• Giám sát và kiểm tra bảo mật thường xuyên\n\n" +

                "6. QUYỀN CỦA BẠN\n\n" +
                "Bạn có quyền:\n" +
                "• Truy cập và xem thông tin cá nhân của bạn\n" +
                "• Chỉnh sửa hoặc cập nhật thông tin\n" +
                "• Xóa tài khoản và dữ liệu\n" +
                "• Từ chối nhận thông báo marketing\n" +
                "• Khiếu nại với cơ quan có thẩm quyền\n\n" +

                "7. LƯU TRỮ DỮ LIỆU\n\n" +
                "Chúng tôi chỉ lưu trữ thông tin cá nhân của bạn trong thời gian cần thiết để thực hiện các mục đích đã nêu hoặc theo yêu cầu của pháp luật.\n\n" +

                "8. DỊCH VỤ BÊN THỨ BA\n\n" +
                "Ứng dụng sử dụng các dịch vụ bên thứ ba:\n" +
                "• Firebase (Google) - Xác thực và lưu trữ dữ liệu\n" +
                "• Google Analytics - Phân tích sử dụng\n\n" +
                "Các dịch vụ này có chính sách bảo mật riêng.\n\n" +

                "9. TRẺ EM\n\n" +
                "Dịch vụ không dành cho trẻ em dưới 13 tuổi. Chúng tôi không cố ý thu thập thông tin từ trẻ em dưới 13 tuổi.\n\n" +

                "10. THAY ĐỔI CHÍNH SÁCH\n\n" +
                "Chúng tôi có thể cập nhật Chính sách bảo mật này theo thời gian. Chúng tôi sẽ thông báo cho bạn về bất kỳ thay đổi nào bằng cách đăng chính sách mới trong ứng dụng.\n\n" +

                "11. LIÊN HỆ\n\n" +
                "Nếu bạn có câu hỏi về Chính sách bảo mật này, vui lòng liên hệ:\n" +
                "Email: vuhoainam.dev@gmail.com";
    }

    private String getCommunityGuidelines() {
        return "NGUYÊN TẮC CỘNG ĐỒNG\n\n" +
                "Cập nhật lần cuối: " + getCurrentDate() + "\n\n" +

                "1. MỤC ĐÍCH\n\n" +
                "Nguyên tắc cộng đồng này nhằm tạo ra một môi trường an toàn, tôn trọng và hỗ trợ lẫn nhau cho tất cả người dùng HealthTips.\n\n" +

                "2. HÀNH VI ĐƯỢC KHUYẾN KHÍCH\n\n" +
                "• Chia sẻ thông tin sức khỏe chính xác và hữu ích\n" +
                "• Tôn trọng quan điểm và trải nghiệm của người khác\n" +
                "• Hỗ trợ và động viên các thành viên khác\n" +
                "• Sử dụng ngôn ngữ lịch sự và tích cực\n" +
                "• Báo cáo nội dung vi phạm khi phát hiện\n\n" +

                "3. NỘI DUNG CẤM\n\n" +
                "3.1. Nội dung bạo lực và nguy hiểm:\n" +
                "• Khuyến khích tự tử hoặc tự gây thương tích\n" +
                "• Nội dung bạo lực đồ họa\n" +
                "• Hoạt động nguy hiểm hoặc bất hợp pháp\n\n" +

                "3.2. Nội dung thù ghét:\n" +
                "• Phân biệt chủng tộc, dân tộc\n" +
                "• Kỳ thị tôn giáo\n" +
                "• Phân biệt giới tính hoặc xu hướng tính dục\n" +
                "• Kỳ thị người khuyết tật\n\n" +

                "3.3. Quấy rối:\n" +
                "• Bắt nạt hoặc đe dọa\n" +
                "• Quấy rối tình dục\n" +
                "• Doxxing (công khai thông tin cá nhân)\n" +
                "• Spam hoặc tin nhắn rác\n\n" +

                "3.4. Thông tin sai lệch:\n" +
                "• Thông tin y tế sai lệch nguy hiểm\n" +
                "• Khuyến cáo điều trị không an toàn\n" +
                "• Quảng cáo thuốc hoặc liệu pháp chưa được chứng minh\n\n" +

                "4. QUYỀN RIÊNG TƯ\n\n" +
                "• KHÔNG chia sẻ thông tin cá nhân của người khác\n" +
                "• Tôn trọng quyền riêng tư và bảo mật\n" +
                "• Không yêu cầu hoặc chia sẻ thông tin nhạy cảm\n\n" +

                "5. SPAM VÀ LỪA ĐẢO\n\n" +
                "• Không spam nội dung lặp lại\n" +
                "• Không quảng cáo sản phẩm/dịch vụ không liên quan\n" +
                "• Không lừa đảo hoặc giả mạo\n" +
                "• Không sử dụng bot hoặc tài khoản giả\n\n" +

                "6. BẢN QUYỀN\n\n" +
                "• Chỉ đăng nội dung bạn có quyền chia sẻ\n" +
                "• Ghi nguồn khi trích dẫn\n" +
                "• Tôn trọng quyền sở hữu trí tuệ\n\n" +

                "7. BÁO CÁO VI PHẠM\n\n" +
                "Nếu bạn thấy nội dung vi phạm:\n" +
                "• Sử dụng tính năng báo cáo trong ứng dụng\n" +
                "• Cung cấp thông tin cụ thể về vi phạm\n" +
                "• Không tấn công hoặc quấy rối người vi phạm\n\n" +

                "8. XỬ LÝ VI PHẠM\n\n" +
                "Tùy mức độ vi phạm, chúng tôi có thể:\n" +
                "• Cảnh báo\n" +
                "• Xóa nội dung vi phạm\n" +
                "• Tạm khóa tài khoản\n" +
                "• Khóa vĩnh viễn tài khoản\n" +
                "• Báo cáo cho cơ quan chức năng\n\n" +

                "9. KHIẾU NẠI\n\n" +
                "Nếu bạn tin rằng nội dung của bạn bị xóa nhầm:\n" +
                "• Liên hệ qua email: vuhoainam.dev@gmail.com\n" +
                "• Cung cấp thông tin chi tiết về trường hợp\n" +
                "• Chúng tôi sẽ xem xét trong vòng 7 ngày làm việc\n\n" +

                "10. CAM KẾT CỦA CHÚNG TÔI\n\n" +
                "Chúng tôi cam kết:\n" +
                "• Xem xét tất cả báo cáo một cách công bằng\n" +
                "• Bảo vệ quyền riêng tư của người báo cáo\n" +
                "• Cập nhật nguyên tắc khi cần thiết\n" +
                "• Lắng nghe phản hồi từ cộng đồng";
    }

    private String getCopyrightPolicy() {
        return "CHÍNH SÁCH BẢN QUYỀN\n\n" +
                "Cập nhật lần cuối: " + getCurrentDate() + "\n\n" +

                "1. GIỚI THIỆU\n\n" +
                "HealthTips tôn trọng quyền sở hữu trí tuệ của người khác và yêu cầu người dùng của chúng tôi cũng làm như vậy. Chính sách này mô tả cách chúng tôi xử lý các khiếu nại về vi phạm bản quyền.\n\n" +

                "2. QUYỀN SỞ HỮU NỘI DUNG\n\n" +
                "2.1. Nội dung của HealthTips:\n" +
                "• Tất cả nội dung gốc (văn bản, hình ảnh, thiết kế, logo) thuộc sở hữu của HealthTips\n" +
                "• Được bảo vệ bởi luật bản quyền Việt Nam và quốc tế\n" +
                "• Không được sao chép, phân phối mà không có sự cho phép\n\n" +

                "2.2. Nội dung người dùng:\n" +
                "• Bạn giữ quyền sở hữu nội dung bạn đăng tải\n" +
                "• Bạn cấp cho HealthTips giấy phép sử dụng, hiển thị và phân phối nội dung\n" +
                "• Giấy phép này là không độc quyền và miễn phí\n\n" +

                "3. DMCA VÀ THÔNG BÁO VI PHẠM\n\n" +
                "Nếu bạn tin rằng nội dung trên HealthTips vi phạm bản quyền của bạn, vui lòng gửi thông báo bao gồm:\n\n" +

                "• Chữ ký điện tử hoặc vật lý của chủ sở hữu bản quyền\n" +
                "• Mô tả tác phẩm được bảo vệ bản quyền\n" +
                "• Mô tả nội dung vi phạm và vị trí của nó\n" +
                "• Thông tin liên hệ của bạn (địa chỉ, số điện thoại, email)\n" +
                "• Tuyên bố rằng bạn tin tưởng rằng việc sử dụng là không được phép\n" +
                "• Tuyên bố rằng thông tin trong thông báo là chính xác\n\n" +

                "Gửi đến: vuhoainam.dev@gmail.com\n\n" +

                "4. PHẢN HỒI VI PHẠM\n\n" +
                "Khi nhận được thông báo hợp lệ, chúng tôi sẽ:\n" +
                "• Xóa hoặc vô hiệu hóa quyền truy cập vào nội dung vi phạm\n" +
                "• Thông báo cho người đăng nội dung\n" +
                "• Chấm dứt tài khoản của người vi phạm tái diễn\n\n" +

                "5. THÔNG BÁO PHẢN ĐỐI\n\n" +
                "Nếu bạn tin rằng nội dung của bạn bị xóa nhầm, bạn có thể gửi thông báo phản đối bao gồm:\n\n" +
                "• Chữ ký vật lý hoặc điện tử của bạn\n" +
                "• Mô tả nội dung đã bị xóa\n" +
                "• Tuyên bố dưới hình phạt khai man rằng nội dung bị xóa do nhầm lẫn\n" +
                "• Tên, địa chỉ, số điện thoại và email của bạn\n\n" +

                "6. SỬ DỤNG HỢP LÝ\n\n" +
                "Một số sử dụng có thể được coi là \"sử dụng hợp lý\" theo luật bản quyền:\n" +
                "• Trích dẫn cho mục đích giáo dục\n" +
                "• Phê bình hoặc bình luận\n" +
                "• Báo cáo tin tức\n" +
                "• Nghiên cứu\n\n" +

                "7. GIẤY PHÉP SỬ DỤNG\n\n" +
                "Khi đăng nội dung lên HealthTips, bạn cấp cho chúng tôi:\n" +
                "• Giấy phép toàn cầu, không độc quyền, miễn phí\n" +
                "• Quyền sử dụng, sao chép, sửa đổi, phân phối nội dung\n" +
                "• Quyền cấp phép lại cho người dùng khác xem nội dung\n\n" +

                "8. NỘI DUNG BÊN THỨ BA\n\n" +
                "• Chúng tôi không chịu trách nhiệm về nội dung của bên thứ ba\n" +
                "• Người dùng chịu trách nhiệm đảm bảo họ có quyền đăng nội dung\n" +
                "• Vi phạm có thể dẫn đến chấm dứt tài khoản\n\n" +

                "9. TRADEMARK\n\n" +
                "• Logo và tên \"HealthTips\" là nhãn hiệu của chúng tôi\n" +
                "• Không được sử dụng mà không có sự cho phép bằng văn bản\n" +
                "• Vi phạm có thể bị truy cứu trách nhiệm pháp lý\n\n" +

                "10. VI PHẠM LẶP LẠI\n\n" +
                "Chúng tôi sẽ chấm dứt tài khoản của người dùng:\n" +
                "• Vi phạm bản quyền nhiều lần\n" +
                "• Bỏ qua cảnh báo về vi phạm\n" +
                "• Cố ý đăng nội dung vi phạm\n\n" +

                "11. LIÊN HỆ\n\n" +
                "Mọi câu hỏi về chính sách bản quyền, vui lòng liên hệ:\n\n" +
                "Email: vuhoainam.dev@gmail.com\n" +
                "Chủ đề: Copyright Notice - HealthTips\n\n" +

                "12. THAY ĐỔI CHÍNH SÁCH\n\n" +
                "Chúng tôi có thể cập nhật chính sách này theo thời gian. Những thay đổi sẽ có hiệu lực ngay khi được đăng trong ứng dụng.";
    }

    private String getCurrentDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }
}

