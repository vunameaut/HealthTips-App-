package com.vhn.doan.presentation.settings.support;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.FAQItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity hiển thị câu hỏi thường gặp (FAQ)
 */
public class FAQActivity extends AppCompatActivity {

    private RecyclerView recyclerViewFAQ;
    private FAQAdapter faqAdapter;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private List<FAQItem> faqList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        initViews();
        setupRecyclerView();
        loadFAQData();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerViewFAQ = findViewById(R.id.recyclerViewFAQ);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void setupRecyclerView() {
        faqList = new ArrayList<>();
        faqAdapter = new FAQAdapter(faqList);
        recyclerViewFAQ.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFAQ.setAdapter(faqAdapter);
    }

    private void loadFAQData() {
        showLoading(true);

        // Dữ liệu FAQ mẫu
        faqList.clear();

        // Danh mục: Sử dụng ứng dụng
        faqList.add(new FAQItem(
            "Làm thế nào để sử dụng ứng dụng HealthTips?",
            "HealthTips rất dễ sử dụng! Sau khi đăng nhập, bạn có thể:\n\n" +
            "• Xem các mẹo sức khỏe được đề xuất trên trang chủ\n" +
            "• Duyệt qua các danh mục sức khỏe khác nhau\n" +
            "• Tìm kiếm mẹo sức khỏe theo từ khóa\n" +
            "• Lưu các mẹo yêu thích để xem lại sau\n" +
            "• Xem video ngắn về sức khỏe\n" +
            "• Sử dụng chat AI để nhận tư vấn sức khỏe",
            "Cơ bản",
            R.drawable.ic_help
        ));

        faqList.add(new FAQItem(
            "Làm thế nào để lưu mẹo yêu thích?",
            "Để lưu một mẹo sức khỏe vào danh sách yêu thích:\n\n" +
            "1. Mở mẹo sức khỏe bạn muốn lưu\n" +
            "2. Nhấn vào biểu tượng trái tim ở góc trên bên phải\n" +
            "3. Mẹo sẽ được lưu vào tab \"Yêu thích\" ở menu dưới\n\n" +
            "Bạn có thể xem lại tất cả các mẹo đã lưu bất cứ lúc nào trong tab Yêu thích.",
            "Cơ bản",
            R.drawable.ic_favorite
        ));

        // Danh mục: Nhắc nhở
        faqList.add(new FAQItem(
            "Làm thế nào để tạo nhắc nhở sức khỏe?",
            "Để tạo nhắc nhở sức khỏe:\n\n" +
            "1. Vào tab \"Nhắc nhở\" ở menu dưới\n" +
            "2. Nhấn nút \"+\" để tạo nhắc nhở mới\n" +
            "3. Nhập tiêu đề và mô tả cho nhắc nhở\n" +
            "4. Chọn thời gian và tần suất lặp lại\n" +
            "5. Tùy chỉnh âm thanh, rung và các cài đặt khác\n" +
            "6. Nhấn \"Lưu\" để hoàn tất\n\n" +
            "Ứng dụng sẽ gửi thông báo nhắc nhở vào thời gian bạn đã đặt.",
            "Nhắc nhở",
            R.drawable.ic_notifications
        ));

        faqList.add(new FAQItem(
            "Tôi không nhận được thông báo nhắc nhở?",
            "Nếu bạn không nhận được thông báo nhắc nhở, hãy kiểm tra:\n\n" +
            "• Đảm bảo quyền thông báo đã được bật cho ứng dụng\n" +
            "• Kiểm tra cài đặt \"Không làm phiền\" trên thiết bị\n" +
            "• Xác nhận nhắc nhở đã được kích hoạt (toggle màu xanh)\n" +
            "• Kiểm tra pin - một số thiết bị tắt thông báo khi tiết kiệm pin\n\n" +
            "Nếu vẫn gặp vấn đề, hãy liên hệ hỗ trợ.",
            "Nhắc nhở",
            R.drawable.ic_notifications_off
        ));

        // Danh mục: Chat AI
        faqList.add(new FAQItem(
            "Làm thế nào để sử dụng chat AI?",
            "Để sử dụng tính năng chat AI:\n\n" +
            "1. Vào tab \"Chat AI\" ở menu dưới\n" +
            "2. Nhập câu hỏi về sức khỏe vào ô chat\n" +
            "3. AI sẽ phân tích và đưa ra câu trả lời\n" +
            "4. Bạn có thể tiếp tục hội thoại để làm rõ thêm\n\n" +
            "Lưu ý: Chat AI chỉ mang tính chất tham khảo. Hãy tham khảo ý kiến bác sĩ cho vấn đề sức khỏe nghiêm trọng.",
            "Chat AI",
            R.drawable.ic_chat
        ));

        // Danh mục: Video
        faqList.add(new FAQItem(
            "Làm thế nào để xem và thích video?",
            "Để xem video ngắn về sức khỏe:\n\n" +
            "1. Vào tab \"Video ngắn\" ở menu dưới\n" +
            "2. Vuốt lên/xuống để xem các video khác nhau\n" +
            "3. Nhấn vào biểu tượng trái tim để thích video\n" +
            "4. Nhấn vào biểu tượng chia sẻ để chia sẻ video\n\n" +
            "Các video bạn đã thích sẽ được lưu trong danh sách riêng để xem lại.",
            "Video",
            R.drawable.ic_video
        ));

        // Danh mục: Tài khoản
        faqList.add(new FAQItem(
            "Làm thế nào để đổi mật khẩu?",
            "Để đổi mật khẩu tài khoản:\n\n" +
            "1. Vào tab \"Hồ sơ\"\n" +
            "2. Nhấn vào biểu tượng cài đặt\n" +
            "3. Chọn \"Tài khoản\" > \"Bảo mật\"\n" +
            "4. Chọn \"Đổi mật khẩu\"\n" +
            "5. Nhập mật khẩu hiện tại và mật khẩu mới\n" +
            "6. Nhấn \"Xác nhận\" để hoàn tất\n\n" +
            "Bạn cũng có thể sử dụng tính năng \"Quên mật khẩu\" ở màn hình đăng nhập.",
            "Tài khoản",
            R.drawable.ic_lock
        ));

        faqList.add(new FAQItem(
            "Làm thế nào để xóa tài khoản?",
            "Nếu bạn muốn xóa tài khoản:\n\n" +
            "1. Vào tab \"Hồ sơ\"\n" +
            "2. Nhấn vào biểu tượng cài đặt\n" +
            "3. Chọn \"Tài khoản\"\n" +
            "4. Kéo xuống và chọn \"Xóa tài khoản\"\n" +
            "5. Xác nhận quyết định của bạn\n\n" +
            "Lưu ý: Việc xóa tài khoản sẽ xóa vĩnh viễn tất cả dữ liệu của bạn và không thể khôi phục.",
            "Tài khoản",
            R.drawable.ic_delete
        ));

        // Danh mục: Báo cáo và Hỗ trợ
        faqList.add(new FAQItem(
            "Làm thế nào để báo cáo nội dung không phù hợp?",
            "Để báo cáo nội dung vi phạm:\n\n" +
            "1. Mở nội dung bạn muốn báo cáo\n" +
            "2. Nhấn vào biểu tượng ba chấm (⋮) ở góc trên\n" +
            "3. Chọn \"Báo cáo nội dung\"\n" +
            "4. Chọn lý do báo cáo\n" +
            "5. Thêm chi tiết nếu cần\n" +
            "6. Nhấn \"Gửi báo cáo\"\n\n" +
            "Chúng tôi sẽ xem xét và xử lý trong vòng 24-48 giờ.",
            "Hỗ trợ",
            R.drawable.ic_report
        ));

        faqList.add(new FAQItem(
            "Làm thế nào để liên hệ hỗ trợ?",
            "Bạn có thể liên hệ với đội ngũ hỗ trợ qua:\n\n" +
            "• Email: vuhoainam.dev@gmail.com\n" +
            "• Tạo ticket hỗ trợ trong ứng dụng:\n" +
            "  Cài đặt > Hỗ trợ > Tạo yêu cầu hỗ trợ\n\n" +
            "Chúng tôi sẽ phản hồi trong vòng 24 giờ làm việc.",
            "Hỗ trợ",
            R.drawable.ic_support
        ));

        // Danh mục: Khác
        faqList.add(new FAQItem(
            "Ứng dụng có miễn phí không?",
            "Có! HealthTips hoàn toàn miễn phí.\n\n" +
            "Tất cả các tính năng cơ bản đều có thể sử dụng miễn phí, bao gồm:\n" +
            "• Xem mẹo sức khỏe không giới hạn\n" +
            "• Tạo nhắc nhở sức khỏe\n" +
            "• Chat với AI\n" +
            "• Xem video ngắn\n" +
            "• Lưu mẹo yêu thích\n\n" +
            "Chúng tôi có thể giới thiệu các tính năng cao cấp trong tương lai.",
            "Khác",
            R.drawable.ic_info
        ));

        faqList.add(new FAQItem(
            "Dữ liệu của tôi có được bảo mật không?",
            "Chúng tôi rất coi trọng quyền riêng tư của bạn.\n\n" +
            "• Dữ liệu được mã hóa và lưu trữ an toàn trên Firebase\n" +
            "• Chúng tôi không chia sẻ thông tin cá nhân với bên thứ ba\n" +
            "• Bạn có quyền kiểm soát dữ liệu của mình\n" +
            "• Có thể xóa tài khoản và dữ liệu bất cứ lúc nào\n\n" +
            "Xem thêm: Cài đặt > Điều khoản và chính sách > Chính sách bảo mật",
            "Khác",
            R.drawable.ic_security
        ));

        showLoading(false);
        updateEmptyState();
        faqAdapter.notifyDataSetChanged();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewFAQ.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        if (faqList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerViewFAQ.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerViewFAQ.setVisibility(View.VISIBLE);
        }
    }
}

