package com.vhn.doan.presentation.healthtip.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vhn.doan.R;
import com.vhn.doan.data.ContentBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity test để hiển thị giao diện mới với thứ tự:
 * Tiêu đề → Phân loại → Tác giả → Nổi bật → Nội dung (text, image, caption xen kẽ)
 */
public class HealthTipDetailTestActivity extends AppCompatActivity {

    // UI Components theo thứ tự hiển thị
    private TextView textViewTitle;
    private TextView textViewCategory;
    private TextView textViewAuthor;
    private TextView textViewPublishedDate;
    private TextView textViewFeatured;
    private RecyclerView recyclerViewContent;
    private TextView textViewViewCount;
    private TextView textViewLikeCount;
    private Button buttonLike;
    private Button buttonShare;
    private FloatingActionButton fabFavorite;
    private ProgressBar progressBar;

    // Adapter
    private NewContentBlockAdapter contentAdapter;

    public static Intent createIntent(Context context) {
        return new Intent(context, HealthTipDetailTestActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_tip_detail_new);

        initViews();
        setupContent();
        setupListeners();
    }

    private void initViews() {
        // Khởi tạo toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Khởi tạo các view theo thứ tự
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewCategory = findViewById(R.id.textViewCategory);
        textViewAuthor = findViewById(R.id.textViewAuthor);
        textViewPublishedDate = findViewById(R.id.textViewPublishedDate);
        textViewFeatured = findViewById(R.id.textViewFeatured);
        recyclerViewContent = findViewById(R.id.recyclerViewContent);
        textViewViewCount = findViewById(R.id.textViewViewCount);
        textViewLikeCount = findViewById(R.id.textViewLikeCount);
        buttonLike = findViewById(R.id.buttonLike);
        buttonShare = findViewById(R.id.buttonShare);
        fabFavorite = findViewById(R.id.fabFavorite);
        progressBar = findViewById(R.id.progressBar);

        // Khởi tạo adapter
        contentAdapter = new NewContentBlockAdapter();
        recyclerViewContent.setAdapter(contentAdapter);
        recyclerViewContent.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewContent.setNestedScrollingEnabled(false);
    }

    private void setupContent() {
        // 1. TIÊU ĐỀ
        textViewTitle.setText("Bí quyết ăn uống cân bằng cho một cơ thể khỏe mạnh");

        // 2. PHÂN LOẠI
        textViewCategory.setText("Dinh dưỡng");

        // 3. TÁC GIẢ
        textViewAuthor.setText("Dr. Nguyen Van A");
        textViewPublishedDate.setText("Ngày đăng: 30/09/2025");

        // 4. NỔI BẬT (hiển thị để test)
        textViewFeatured.setVisibility(View.VISIBLE);

        // 5. NỘI DUNG CHÍNH - Tạo dữ liệu test với thứ tự xen kẽ
        List<ContentBlock> testContent = createTestContent();
        contentAdapter.setContentBlocks(testContent);

        // 6. THỐNG KÊ
        textViewViewCount.setText("1,234 lượt xem");
        textViewLikeCount.setText("156 lượt thích");

        // Ẩn loading
        progressBar.setVisibility(View.GONE);
    }

    private List<ContentBlock> createTestContent() {
        List<ContentBlock> contentBlocks = new ArrayList<>();

        // Đoạn văn mở đầu
        contentBlocks.add(new ContentBlock(
                "text_1",
                "text",
                "Chế độ ăn uống cân bằng là nền tảng quan trọng nhất cho một cơ thể khỏe mạnh. Việc lựa chọn thực phẩm phù hợp không chỉ giúp cung cấp năng lượng mà còn tăng cường sức đề kháng tự nhiên.",
                null
        ));

        // Tiêu đề phụ đầu tiên
        contentBlocks.add(new ContentBlock(
                "heading_1",
                "heading",
                "Tại sao cần có chế độ ăn cân bằng?",
                null
        ));

        // Đoạn văn giải thích
        contentBlocks.add(new ContentBlock(
                "text_2",
                "text",
                "Cơ thể con người cần đầy đủ các nhóm chất dinh dưỡng: carbohydrate, protein, lipid, vitamin và khoáng chất để hoạt động hiệu quả. Thiếu hụt bất kỳ thành phần nào cũng có thể dẫn đến các vấn đề sức khỏe nghiêm trọng.",
                null
        ));

        // Hình ảnh minh họa đầu tiên
        contentBlocks.add(new ContentBlock(
                "image_1",
                "image",
                "https://res.cloudinary.com/dazo6ypwt/image/upload/v1757926161/healthy_tip_image/editor-images/2025/09/post1_img1.jpg",
                null
        ));

        // Chú thích cho hình ảnh đầu tiên
        contentBlocks.add(new ContentBlock(
                "caption_1",
                "caption",
                "Bữa ăn cân bằng với đầy đủ các nhóm thực phẩm cần thiết",
                null
        ));

        // Tiêu đề phụ thứ hai
        contentBlocks.add(new ContentBlock(
                "heading_2",
                "heading",
                "Các nguyên tắc cơ bản",
                null
        ));

        // Đoạn trích dẫn
        contentBlocks.add(new ContentBlock(
                "quote_1",
                "quote",
                "\"Hãy để thực phẩm trở thành thuốc của bạn, và thuốc trở thành thực phẩm của bạn\" - Hippocrates",
                null
        ));

        // Đoạn văn tiếp theo
        contentBlocks.add(new ContentBlock(
                "text_3",
                "text",
                "Ưu tiên các thực phẩm tự nhiên như rau xanh, trái cây tươi, ngũ cốc nguyên hạt và protein từ nguồn chất lượng cao. Hạn chế thực phẩm chế biến sẵn, đồ ăn nhanh và đồ uống có đường.",
                null
        ));

        // Hình ảnh minh họa thứ hai
        contentBlocks.add(new ContentBlock(
                "image_2",
                "image",
                "https://res.cloudinary.com/dazo6ypwt/image/upload/v1757926161/healthy_tip_image/editor-images/2025/09/post1_img2.jpg",
                null
        ));

        // Chú thích cho hình ảnh thứ hai
        contentBlocks.add(new ContentBlock(
                "caption_2",
                "caption",
                "Rau xanh và trái cây tươi - nguồn vitamin và khoáng chất tự nhiên",
                null
        ));

        // Đoạn kết luận
        contentBlocks.add(new ContentBlock(
                "text_4",
                "text",
                "Việc duy trì chế độ ăn uống cân bằng không chỉ giúp cải thiện sức khỏe tổng thể mà còn tăng cường năng lượng, cải thiện tâm trạng và nâng cao chất lượng cuộc sống. Hãy bắt đầu từ những thay đổi nhỏ và duy trì lâu dài.",
                null
        ));

        return contentBlocks;
    }

    private void setupListeners() {
        buttonLike.setOnClickListener(v -> {
            // Test logic cho nút Like
            buttonLike.setText("Đã thích ❤️");
        });

        buttonShare.setOnClickListener(v -> {
            // Test logic cho nút Share
            // Có thể mở share dialog
        });

        fabFavorite.setOnClickListener(v -> {
            // Test logic cho nút Favorite
            fabFavorite.setImageResource(R.drawable.ic_favorite_filled);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
