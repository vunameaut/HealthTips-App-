package com.vhn.doan.presentation.about;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.vhn.doan.R;

/**
 * Activity hiển thị các tài liệu pháp lý (Terms, Privacy, Community Guidelines)
 * Nội dung được lấy cảm hứng từ TikTok và điều chỉnh cho HealthTips
 */
public class LegalDocumentActivity extends AppCompatActivity {

    public static final String EXTRA_DOCUMENT_TYPE = "document_type";
    public static final String TYPE_TERMS = "terms";
    public static final String TYPE_PRIVACY = "privacy";
    public static final String TYPE_COMMUNITY = "community";

    private TextView contentText;
    private TextView lastUpdatedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal_document);

        contentText = findViewById(R.id.contentText);
        lastUpdatedText = findViewById(R.id.lastUpdatedText);

        String documentType = getIntent().getStringExtra(EXTRA_DOCUMENT_TYPE);

        setupToolbar(documentType);
        loadDocument(documentType);
    }

    private void setupToolbar(String documentType) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            String title = "";
            switch (documentType != null ? documentType : "") {
                case TYPE_TERMS:
                    title = getString(R.string.terms_of_service);
                    break;
                case TYPE_PRIVACY:
                    title = getString(R.string.privacy_policy);
                    break;
                case TYPE_COMMUNITY:
                    title = getString(R.string.community_guidelines);
                    break;
            }
            getSupportActionBar().setTitle(title);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadDocument(String documentType) {
        String content = "";
        String lastUpdated = "Cập nhật lần cuối: 25 tháng 10, 2024";

        switch (documentType != null ? documentType : "") {
            case TYPE_TERMS:
                content = getTermsOfServiceContent();
                break;
            case TYPE_PRIVACY:
                content = getPrivacyPolicyContent();
                break;
            case TYPE_COMMUNITY:
                content = getCommunityGuidelinesContent();
                break;
        }

        lastUpdatedText.setText(lastUpdated);
        contentText.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
    }

    private String getTermsOfServiceContent() {
        return "<h2>Điều khoản dịch vụ</h2>" +

                "<h3>1. Chấp nhận điều khoản</h3>" +
                "<p>Bằng việc truy cập và sử dụng HealthTips (\"Ứng dụng\"), bạn đồng ý tuân thủ và bị ràng buộc bởi các Điều khoản dịch vụ này. Nếu bạn không đồng ý với bất kỳ phần nào của các điều khoản này, vui lòng không sử dụng Ứng dụng.</p>" +

                "<h3>2. Sử dụng dịch vụ</h3>" +
                "<p>HealthTips cung cấp nền tảng để người dùng truy cập các mẹo sức khỏe, video ngắn, tính năng chat AI và các dịch vụ liên quan đến sức khỏe. Bạn đồng ý sử dụng Ứng dụng một cách hợp pháp và phù hợp với các điều khoản này.</p>" +

                "<h3>3. Tài khoản người dùng</h3>" +
                "<p>Khi tạo tài khoản, bạn phải cung cấp thông tin chính xác và cập nhật. Bạn có trách nhiệm duy trì bảo mật tài khoản và mật khẩu của mình. Bạn đồng ý chấp nhận trách nhiệm cho tất cả các hoạt động diễn ra dưới tài khoản của bạn.</p>" +

                "<h3>4. Nội dung người dùng</h3>" +
                "<p>Bạn có trách nhiệm về nội dung mà bạn đăng tải hoặc chia sẻ trên HealthTips. Bạn không được đăng tải nội dung vi phạm pháp luật, xúc phạm, gây hiểu lầm hoặc vi phạm quyền của người khác.</p>" +

                "<h3>5. Quyền sở hữu trí tuệ</h3>" +
                "<p>Tất cả nội dung, tính năng và chức năng của Ứng dụng (bao gồm nhưng không giới hạn ở văn bản, đồ họa, logo, biểu tượng, hình ảnh và phần mềm) đều thuộc sở hữu của HealthTips hoặc người cấp phép của chúng tôi và được bảo vệ bởi luật bản quyền.</p>" +

                "<h3>6. Hành vi bị cấm</h3>" +
                "<p>Khi sử dụng HealthTips, bạn không được:</p>" +
                "<ul>" +
                "<li>Vi phạm bất kỳ luật hoặc quy định hiện hành nào</li>" +
                "<li>Đăng tải nội dung gây hiểu lầm về sức khỏe hoặc y tế</li>" +
                "<li>Quấy rối, lạm dụng hoặc gây hại cho người dùng khác</li>" +
                "<li>Spam hoặc gửi thông điệp không mong muốn</li>" +
                "<li>Cố gắng truy cập trái phép vào hệ thống</li>" +
                "<li>Sử dụng Ứng dụng cho mục đích thương mại không được phép</li>" +
                "</ul>" +

                "<h3>7. Thông tin y tế</h3>" +
                "<p><strong>Tuyên bố từ chối trách nhiệm y tế:</strong> Nội dung trên HealthTips chỉ mang tính chất tham khảo và không thay thế cho lời khuyên y tế chuyên nghiệp. Luôn tham khảo ý kiến bác sĩ hoặc chuyên gia y tế có trình độ trước khi thực hiện bất kỳ thay đổi nào về sức khỏe.</p>" +

                "<h3>8. Chấm dứt</h3>" +
                "<p>Chúng tôi có quyền chấm dứt hoặc đình chỉ quyền truy cập của bạn vào Ứng dụng ngay lập tức, không cần thông báo trước, nếu bạn vi phạm các Điều khoản dịch vụ này.</p>" +

                "<h3>9. Giới hạn trách nhiệm</h3>" +
                "<p>HealthTips và các đối tác của chúng tôi sẽ không chịu trách nhiệm về bất kỳ thiệt hại trực tiếp, gián tiếp, ngẫu nhiên, đặc biệt hoặc do hậu quả nào phát sinh từ việc sử dụng hoặc không thể sử dụng Ứng dụng.</p>" +

                "<h3>10. Thay đổi điều khoản</h3>" +
                "<p>Chúng tôi có quyền sửa đổi các điều khoản này bất cứ lúc nào. Chúng tôi sẽ thông báo cho bạn về những thay đổi quan trọng thông qua Ứng dụng hoặc email. Việc tiếp tục sử dụng Ứng dụng sau khi có thay đổi đồng nghĩa với việc bạn chấp nhận các điều khoản mới.</p>" +

                "<h3>11. Liên hệ</h3>" +
                "<p>Nếu bạn có bất kỳ câu hỏi nào về Điều khoản dịch vụ này, vui lòng liên hệ với chúng tôi qua email: vuhoainam.dev@gmail.com</p>";
    }

    private String getPrivacyPolicyContent() {
        return "<h2>Chính sách bảo mật</h2>" +

                "<h3>1. Giới thiệu</h3>" +
                "<p>HealthTips (\"chúng tôi\" hoặc \"chúng ta\") cam kết bảo vệ quyền riêng tư của bạn. Chính sách bảo mật này giải thích cách chúng tôi thu thập, sử dụng, chia sẻ và bảo vệ thông tin cá nhân của bạn khi bạn sử dụng ứng dụng HealthTips.</p>" +

                "<h3>2. Thông tin chúng tôi thu thập</h3>" +
                "<p><strong>Thông tin bạn cung cấp:</strong></p>" +
                "<ul>" +
                "<li>Thông tin tài khoản (tên, email, mật khẩu)</li>" +
                "<li>Thông tin hồ sơ (ảnh đại diện, tiểu sử)</li>" +
                "<li>Nội dung bạn tạo (bình luận, yêu thích, lịch sử tìm kiếm)</li>" +
                "<li>Thông tin liên hệ khi bạn gửi yêu cầu hỗ trợ</li>" +
                "</ul>" +

                "<p><strong>Thông tin tự động thu thập:</strong></p>" +
                "<ul>" +
                "<li>Thông tin thiết bị (kiểu máy, hệ điều hành, ID thiết bị)</li>" +
                "<li>Dữ liệu sử dụng (tính năng được sử dụng, thời gian sử dụng)</li>" +
                "<li>Dữ liệu vị trí (nếu bạn cho phép)</li>" +
                "<li>Cookies và công nghệ theo dõi tương tự</li>" +
                "</ul>" +

                "<h3>3. Cách chúng tôi sử dụng thông tin</h3>" +
                "<p>Chúng tôi sử dụng thông tin của bạn để:</p>" +
                "<ul>" +
                "<li>Cung cấp, duy trì và cải thiện dịch vụ của chúng tôi</li>" +
                "<li>Cá nhân hóa trải nghiệm người dùng</li>" +
                "<li>Gửi thông báo và cập nhật quan trọng</li>" +
                "<li>Phản hồi yêu cầu hỗ trợ của bạn</li>" +
                "<li>Ngăn chặn gian lận và lạm dụng</li>" +
                "<li>Tuân thủ nghĩa vụ pháp lý</li>" +
                "</ul>" +

                "<h3>4. Chia sẻ thông tin</h3>" +
                "<p>Chúng tôi không bán thông tin cá nhân của bạn. Chúng tôi có thể chia sẻ thông tin với:</p>" +
                "<ul>" +
                "<li><strong>Nhà cung cấp dịch vụ:</strong> Firebase (Google) cho lưu trữ và xác thực</li>" +
                "<li><strong>Khi có yêu cầu pháp lý:</strong> Nếu luật pháp yêu cầu</li>" +
                "<li><strong>Với sự đồng ý của bạn:</strong> Trong các trường hợp khác với sự cho phép của bạn</li>" +
                "</ul>" +

                "<h3>5. Bảo mật dữ liệu</h3>" +
                "<p>Chúng tôi thực hiện các biện pháp bảo mật kỹ thuật và tổ chức phù hợp để bảo vệ thông tin của bạn khỏi truy cập, tiết lộ, thay đổi hoặc phá hủy trái phép. Tuy nhiên, không có phương thức truyền tải qua Internet hoặc lưu trữ điện tử nào là an toàn 100%.</p>" +

                "<h3>6. Quyền của bạn</h3>" +
                "<p>Bạn có quyền:</p>" +
                "<ul>" +
                "<li>Truy cập và xem thông tin cá nhân của bạn</li>" +
                "<li>Chỉnh sửa hoặc cập nhật thông tin</li>" +
                "<li>Xóa tài khoản và dữ liệu của bạn</li>" +
                "<li>Từ chối nhận thông báo marketing</li>" +
                "<li>Yêu cầu sao chép dữ liệu của bạn</li>" +
                "</ul>" +

                "<h3>7. Lưu trữ dữ liệu</h3>" +
                "<p>Chúng tôi lưu trữ thông tin của bạn cho đến khi bạn xóa tài khoản hoặc yêu cầu xóa dữ liệu. Một số thông tin có thể được giữ lại lâu hơn nếu cần thiết cho mục đích pháp lý hoặc kinh doanh hợp pháp.</p>" +

                "<h3>8. Quyền riêng tư của trẻ em</h3>" +
                "<p>HealthTips không nhắm đến trẻ em dưới 13 tuổi. Chúng tôi không cố ý thu thập thông tin cá nhân từ trẻ em dưới 13 tuổi. Nếu bạn là phụ huynh và phát hiện con bạn đã cung cấp thông tin cho chúng tôi, vui lòng liên hệ để chúng tôi có thể xóa thông tin đó.</p>" +

                "<h3>9. Thay đổi chính sách</h3>" +
                "<p>Chúng tôi có thể cập nhật Chính sách bảo mật này theo thời gian. Chúng tôi sẽ thông báo cho bạn về những thay đổi quan trọng thông qua Ứng dụng hoặc các phương tiện khác.</p>" +

                "<h3>10. Liên hệ</h3>" +
                "<p>Nếu bạn có câu hỏi về Chính sách bảo mật này, vui lòng liên hệ:<br>" +
                "Email: support@healthtips.com</p>";
    }

    private String getCommunityGuidelinesContent() {
        return "<h2>Nguyên tắc cộng đồng</h2>" +

                "<h3>1. Tầm nhìn của chúng tôi</h3>" +
                "<p>HealthTips cam kết xây dựng một cộng đồng an toàn, hỗ trợ và tích cực nơi mọi người có thể chia sẻ và học hỏi về sức khỏe. Các nguyên tắc này giúp đảm bảo trải nghiệm tích cực cho tất cả người dùng.</p>" +

                "<h3>2. An toàn và Phúc lợi</h3>" +
                "<p><strong>Thông tin sức khỏe chính xác:</strong></p>" +
                "<ul>" +
                "<li>Chỉ chia sẻ thông tin sức khỏe có nguồn gốc đáng tin cậy</li>" +
                "<li>Không đưa ra lời khuyên y tế không có cơ sở khoa học</li>" +
                "<li>Luôn khuyến khích tham khảo ý kiến chuyên gia y tế</li>" +
                "<li>Không quảng cáo thuốc hoặc phương pháp điều trị chưa được kiểm chứng</li>" +
                "</ul>" +

                "<p><strong>Bảo vệ sức khỏe tinh thần:</strong></p>" +
                "<ul>" +
                "<li>Không chia sẻ nội dung có thể gây hại cho sức khỏe tinh thần</li>" +
                "<li>Không khuyến khích hành vi tự làm hại bản thân</li>" +
                "<li>Hỗ trợ và khuyến khích người khác tìm kiếm sự giúp đỡ chuyên nghiệp khi cần</li>" +
                "</ul>" +

                "<h3>3. Tôn trọng và Lịch sự</h3>" +
                "<p>Chúng tôi kỳ vọng tất cả thành viên đối xử với nhau một cách tôn trọng:</p>" +
                "<ul>" +
                "<li>Không bắt nạt, quấy rối hoặc đe dọa người khác</li>" +
                "<li>Không phân biệt đối xử dựa trên chủng tộc, giới tính, tôn giáo, v.v.</li>" +
                "<li>Không đăng tải nội dung xúc phạm hoặc gây thù ghét</li>" +
                "<li>Tôn trọng quan điểm và trải nghiệm khác nhau</li>" +
                "</ul>" +

                "<h3>4. Nội dung phù hợp</h3>" +
                "<p><strong>Nội dung bị cấm:</strong></p>" +
                "<ul>" +
                "<li>Nội dung khiêu dâm hoặc tình dục</li>" +
                "<li>Bạo lực hoặc hình ảnh gây sốc</li>" +
                "<li>Spam hoặc nội dung quảng cáo không mong muốn</li>" +
                "<li>Thông tin giả mạo hoặc gây hiểu lầm</li>" +
                "<li>Vi phạm bản quyền hoặc sở hữu trí tuệ</li>" +
                "</ul>" +

                "<h3>5. Quyền riêng tư</h3>" +
                "<ul>" +
                "<li>Không chia sẻ thông tin cá nhân của người khác mà không có sự đồng ý</li>" +
                "<li>Tôn trọng quyền riêng tư trong các cuộc thảo luận về sức khỏe</li>" +
                "<li>Không đăng tải hình ảnh hoặc thông tin y tế của người khác</li>" +
                "</ul>" +

                "<h3>6. Tính xác thực</h3>" +
                "<ul>" +
                "<li>Đừng mạo danh người khác hoặc tổ chức</li>" +
                "<li>Đừng tạo tài khoản giả mạo</li>" +
                "<li>Cung cấp thông tin trung thực về bản thân</li>" +
                "</ul>" +

                "<h3>7. Hoạt động hợp pháp</h3>" +
                "<ul>" +
                "<li>Không sử dụng HealthTips cho hoạt động bất hợp pháp</li>" +
                "<li>Tuân thủ tất cả luật và quy định hiện hành</li>" +
                "<li>Không quảng bá các sản phẩm hoặc dịch vụ bất hợp pháp</li>" +
                "</ul>" +

                "<h3>8. Báo cáo vi phạm</h3>" +
                "<p>Nếu bạn thấy nội dung hoặc hành vi vi phạm các nguyên tắc này, vui lòng báo cáo:</p>" +
                "<ul>" +
                "<li>Sử dụng tính năng \"Báo cáo\" trong ứng dụng</li>" +
                "<li>Liên hệ đội ngũ hỗ trợ qua email: vuhoainam.dev@gmail.com</li>" +
                "<li>Cung cấp chi tiết cụ thể để chúng tôi có thể xem xét</li>" +
                "</ul>" +

                "<h3>9. Hậu quả vi phạm</h3>" +
                "<p>Vi phạm các nguyên tắc này có thể dẫn đến:</p>" +
                "<ul>" +
                "<li>Cảnh báo</li>" +
                "<li>Xóa nội dung</li>" +
                "<li>Hạn chế tính năng</li>" +
                "<li>Đình chỉ tạm thời</li>" +
                "<li>Cấm vĩnh viễn khỏi nền tảng</li>" +
                "</ul>" +

                "<h3>10. Đóng góp tích cực</h3>" +
                "<p>Chúng tôi khuyến khích bạn:</p>" +
                "<ul>" +
                "<li>Chia sẻ kinh nghiệm và kiến thức hữu ích</li>" +
                "<li>Hỗ trợ và động viên người khác</li>" +
                "<li>Tham gia các cuộc thảo luận mang tính xây dựng</li>" +
                "<li>Đóng góp vào việc xây dựng cộng đồng tích cực</li>" +
                "</ul>" +

                "<p><strong>Cảm ơn bạn đã giúp HealthTips trở thành một cộng đồng an toàn và hữu ích cho tất cả mọi người!</strong></p>";
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

