package com.vhn.doan.presentation.healthtip.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vhn.doan.R;
import com.vhn.doan.data.ContentBlock;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.repository.HealthTipRepository;
import com.vhn.doan.data.repository.HealthTipRepositoryImpl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity hiển thị chi tiết một bài viết mẹo sức khỏe
 * Tuân theo kiến trúc MVP (Model-View-Presenter)
 * Hỗ trợ hiển thị nội dung theo định dạng ContentBlock
 */
public class HealthTipDetailActivity extends AppCompatActivity implements HealthTipDetailView {

    private static final String EXTRA_HEALTH_TIP_ID = "health_tip_id";

    // UI components
    private ImageView imageViewDetail;
    private TextView textViewTitle;
    private TextView textViewCategory;
    private TextView textViewContent; // Giữ lại để tương thích ngược
    private RecyclerView recyclerViewContent; // Thêm RecyclerView cho nội dung mới
    private ContentBlockAdapter contentAdapter; // Adapter cho ContentBlock
    private TextView textViewViewCount;
    private TextView textViewLikeCount;
    private FloatingActionButton fabFavorite;
    private Button buttonLike;
    private Button buttonShare;
    private ProgressBar progressBar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private TextView textViewAuthor;
    private TextView textViewPublishedDate;
    private ChipGroup chipGroupTags;

    // Presenter
    private HealthTipDetailPresenter presenter;

    // Data
    private String healthTipId;
    private boolean isLiked = false;
    private boolean isFavorite = false;

    /**
     * Tạo Intent để mở Activity này
     */
    public static Intent createIntent(Context context, String healthTipId) {
        Intent intent = new Intent(context, HealthTipDetailActivity.class);
        intent.putExtra(EXTRA_HEALTH_TIP_ID, healthTipId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_tip_detail);

        // Khởi tạo UI components
        initViews();

        // Lấy healthTipId từ Intent
        healthTipId = getIntent().getStringExtra(EXTRA_HEALTH_TIP_ID);

        if (healthTipId == null || healthTipId.isEmpty()) {
            showError("ID bài viết không hợp lệ");
            finish();
            return;
        }

        // Khởi tạo presenter
        HealthTipRepository repository = new HealthTipRepositoryImpl();
        presenter = new HealthTipDetailPresenterImpl(repository);
        presenter.attachView(this);

        // Khởi tạo adapter cho ContentBlock
        contentAdapter = new ContentBlockAdapter();
        recyclerViewContent.setAdapter(contentAdapter);
        recyclerViewContent.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewContent.setNestedScrollingEnabled(false); // Tránh xung đột scroll

        // Thiết lập listeners
        setupListeners();

        // Tải dữ liệu
        presenter.loadHealthTipDetail(healthTipId);
    }

    /**
     * Khởi tạo các view
     */
    private void initViews() {
        // Khởi tạo toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Khởi tạo các view khác
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
        imageViewDetail = findViewById(R.id.imageViewDetail);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewCategory = findViewById(R.id.textViewCategory);
        textViewContent = findViewById(R.id.textViewContent);
        textViewViewCount = findViewById(R.id.textViewViewCount);
        textViewLikeCount = findViewById(R.id.textViewLikeCount);
        fabFavorite = findViewById(R.id.fabFavorite);
        buttonLike = findViewById(R.id.buttonLike);
        buttonShare = findViewById(R.id.buttonShare);
        progressBar = findViewById(R.id.progressBar);

        // Khởi tạo các view mới
        recyclerViewContent = findViewById(R.id.recyclerViewContent);
        textViewAuthor = findViewById(R.id.textViewAuthor);
        textViewPublishedDate = findViewById(R.id.textViewPublishedDate);
        chipGroupTags = findViewById(R.id.chipGroupTags);

        // Ẩn tất cả nội dung cho đến khi dữ liệu được tải
        hideContentViews();
    }

    /**
     * Ẩn tất cả các view nội dung để tránh hiện tượng nháy
     */
    private void hideContentViews() {
        // Ẩn các thành phần chính
        if (textViewTitle != null) textViewTitle.setVisibility(View.GONE);
        if (textViewCategory != null) textViewCategory.setVisibility(View.GONE);
        if (textViewContent != null) textViewContent.setVisibility(View.GONE);
        if (recyclerViewContent != null) recyclerViewContent.setVisibility(View.GONE);
        if (textViewViewCount != null) textViewViewCount.setVisibility(View.GONE);
        if (textViewLikeCount != null) textViewLikeCount.setVisibility(View.GONE);
        if (fabFavorite != null) fabFavorite.setVisibility(View.GONE);
        if (buttonLike != null) buttonLike.setVisibility(View.GONE);
        if (buttonShare != null) buttonShare.setVisibility(View.GONE);
        if (imageViewDetail != null) imageViewDetail.setVisibility(View.GONE);
        if (textViewAuthor != null) textViewAuthor.setVisibility(View.GONE);
        if (textViewPublishedDate != null) textViewPublishedDate.setVisibility(View.GONE);
        if (chipGroupTags != null) chipGroupTags.setVisibility(View.GONE);

        // Hiển thị loading
        showLoading(true);
    }

    /**
     * Hiển thị các view nội dung với animation mượt mà
     */
    private void showContentViewsWithAnimation() {
        // Ẩn loading trước
        showLoading(false);

        // Tạo danh sách các view cần animate
        View[] viewsToAnimate = {
            imageViewDetail,
            textViewTitle,
            textViewCategory,
            textViewViewCount,
            textViewLikeCount,
            recyclerViewContent, // Sử dụng RecyclerView thay vì TextView
            buttonLike,
            buttonShare,
            fabFavorite
        };

        // Animate từng view với delay nhỏ để tạo hiệu ứng cascade
        for (int i = 0; i < viewsToAnimate.length; i++) {
            final View view = viewsToAnimate[i];
            if (view != null && view.getVisibility() == View.VISIBLE) {
                // Đặt vị trí ban đầu (ẩn và offset)
                view.setAlpha(0f);
                view.setTranslationY(50f);

                // Animate với delay
                view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setStartDelay(i * 50) // Delay 50ms cho mỗi view
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void displayHealthTipDetails(HealthTip healthTip) {
        // Hiển thị thông tin chi tiết
        textViewTitle.setText(healthTip.getTitle());
        textViewViewCount.setText(String.valueOf(healthTip.getViewCount()));
        textViewLikeCount.setText(String.valueOf(healthTip.getLikeCount()));

        // Set category text
        if (healthTip.getCategoryName() != null && !healthTip.getCategoryName().isEmpty()) {
            textViewCategory.setText(healthTip.getCategoryName());
            textViewCategory.setVisibility(View.VISIBLE);
        } else {
            textViewCategory.setText("Chưa phân loại");
            textViewCategory.setVisibility(View.VISIBLE);
        }

        // Set title cho toolbar
        collapsingToolbarLayout.setTitle(healthTip.getTitle());

        // Tải hình ảnh
        if (healthTip.getImageUrl() != null && !healthTip.getImageUrl().isEmpty()) {
            imageViewDetail.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(healthTip.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(imageViewDetail);
        } else {
            imageViewDetail.setVisibility(View.GONE);
        }

        // Hiển thị nội dung
        displayContent(healthTip);

        // Hiển thị thông tin tác giả nếu có
        if (healthTip.getAuthor() != null && !healthTip.getAuthor().isEmpty()) {
            textViewAuthor.setText("Tác giả: " + healthTip.getAuthor());
            textViewAuthor.setVisibility(View.VISIBLE);
        } else {
            textViewAuthor.setVisibility(View.GONE);
        }

        // Hiển thị ngày xuất bản nếu có
        if (healthTip.getPublishedAt() != null && healthTip.getPublishedAt() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = sdf.format(new Date(healthTip.getPublishedAt()));
            textViewPublishedDate.setText("Ngày xuất bản: " + formattedDate);
            textViewPublishedDate.setVisibility(View.VISIBLE);
        } else {
            textViewPublishedDate.setVisibility(View.GONE);
        }

        // Hiển thị tags nếu có
        displayTags(healthTip.getTags());

        // Cập nhật trạng thái favorite và like
        isFavorite = healthTip.isFavorite();
        isLiked = healthTip.isLiked();
        updateFavoriteFabIcon();
        updateLikeButtonText();

        // Hiển thị nội dung với animation
        showContentViewsWithAnimation();
    }

    /**
     * Hiển thị nội dung bài viết dựa trên định dạng
     * Hỗ trợ cả định dạng cũ (String) và mới (ContentBlock)
     */
    private void displayContent(HealthTip healthTip) {
        List<ContentBlock> contentBlocks = healthTip.getContentBlockObjects();

        if (contentBlocks != null && !contentBlocks.isEmpty()) {
            // Sử dụng định dạng mới (ContentBlock)
            contentAdapter.setContentBlocks(contentBlocks);
            recyclerViewContent.setVisibility(View.VISIBLE);
            textViewContent.setVisibility(View.GONE);
        } else {
            // Sử dụng định dạng cũ (String)
            String content = healthTip.getContent();
            if (content != null && !content.isEmpty()) {
                textViewContent.setText(content);
                textViewContent.setVisibility(View.VISIBLE);
                recyclerViewContent.setVisibility(View.GONE);
            } else {
                textViewContent.setText("Nội dung đang được cập nhật");
                textViewContent.setVisibility(View.VISIBLE);
                recyclerViewContent.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Hiển thị tags dưới dạng Chip trong ChipGroup
     */
    private void displayTags(List<String> tags) {
        if (tags != null && !tags.isEmpty()) {
            chipGroupTags.removeAllViews();

            for (String tag : tags) {
                Chip chip = new Chip(this);
                chip.setText(tag);
                chip.setClickable(true);
                chip.setCheckable(false);
                chip.setChipBackgroundColorResource(R.color.chip_background);
                chip.setTextColor(getResources().getColor(R.color.chip_text));

                chip.setOnClickListener(v -> {
                    // Xử lý khi click vào tag (ví dụ: mở màn hình search với tag này)
                    showMessage("Tìm kiếm bài viết với tag: " + tag);
                });

                chipGroupTags.addView(chip);
            }

            chipGroupTags.setVisibility(View.VISIBLE);
        } else {
            chipGroupTags.setVisibility(View.GONE);
        }
    }

    /**
     * Thiết lập các sự kiện
     */
    private void setupListeners() {
        if (fabFavorite != null) {
            fabFavorite.setOnClickListener(v -> {
                if (presenter != null) {
                    presenter.onFavoriteClick(healthTipId);
                }
            });
        }

        if (buttonLike != null) {
            buttonLike.setOnClickListener(v -> {
                if (presenter != null) {
                    presenter.onLikeClick(healthTipId);
                }
            });
        }

        if (buttonShare != null) {
            buttonShare.setOnClickListener(v -> {
                if (presenter != null) {
                    presenter.onShareClick(healthTipId);
                }
            });
        }
    }

    /**
     * Cập nhật icon của FAB yêu thích
     */
    private void updateFavoriteFabIcon() {
        if (fabFavorite != null) {
            if (isFavorite) {
                fabFavorite.setImageResource(R.drawable.ic_favorite);
                fabFavorite.setContentDescription(getString(R.string.remove_from_favorite));
            } else {
                fabFavorite.setImageResource(R.drawable.ic_favorite_border);
                fabFavorite.setContentDescription(getString(R.string.add_to_favorite));
            }
        }
    }

    /**
     * Cập nhật text của nút thích
     */
    private void updateLikeButtonText() {
        if (buttonLike != null) {
            buttonLike.setText(isLiked ? R.string.unlike : R.string.like);
        }
    }

    @Override
    public void updateFavoriteStatus(boolean isFavorite) {
        if (isFavorite) {
            fabFavorite.setImageResource(R.drawable.ic_favorite);
        } else {
            fabFavorite.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    @Override
    public void updateLikeStatus(boolean isLiked) {
        // Thay đổi text của button thay vì image vì buttonLike là Button không phải ImageButton
        if (isLiked) {
            buttonLike.setText("❤️ Đã thích");
            buttonLike.setTextColor(getResources().getColor(R.color.primary_button_start));
        } else {
            buttonLike.setText("🤍 Thích");
            buttonLike.setTextColor(getResources().getColor(R.color.text_secondary));
        }
    }

    @Override
    public void updateLikeCount(int likeCount) {
        textViewLikeCount.setText(String.valueOf(likeCount));
    }

    @Override
    public void updateViewCount(int viewCount) {
        textViewViewCount.setText(String.valueOf(viewCount));
    }

    @Override
    public void shareContent(String content) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_tip)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    @Override
    public void finish() {
        super.finish();
        // Không còn animation trượt xuống dưới nữa
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Không còn animation trượt xuống dưới nữa
    }
}
