package com.vhn.doan.presentation.healthtip.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vhn.doan.R;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.repository.HealthTipRepository;
import com.vhn.doan.data.repository.HealthTipRepositoryImpl;

/**
 * Activity hiển thị chi tiết một bài viết mẹo sức khỏe
 * Tuân theo kiến trúc MVP (Model-View-Presenter)
 */
public class HealthTipDetailActivity extends AppCompatActivity implements HealthTipDetailView {

    private static final String EXTRA_HEALTH_TIP_ID = "health_tip_id";

    // UI components
    private ImageView imageViewDetail;
    private TextView textViewTitle;
    private TextView textViewCategory;
    private TextView textViewContent;
    private TextView textViewViewCount;
    private TextView textViewLikeCount;
    private FloatingActionButton fabFavorite;
    private Button buttonLike;
    private Button buttonShare;
    private ProgressBar progressBar;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    // Presenter
    private HealthTipDetailPresenter presenter;

    // Data
    private String healthTipId;
    private boolean isLiked = false;
    private boolean isFavorite = false;

    /**
     * Tạo Intent để mở Activity này
     * @param context Context nguồn
     * @param healthTipId ID của mẹo sức khỏe cần xem chi tiết
     * @return Intent để mở HealthTipDetailActivity
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
        presenter = new HealthTipDetailPresenterImpl(this, repository);

        // Thiết lập listeners
        setupListeners();

        // Tải dữ liệu
        presenter.loadHealthTipDetails(healthTipId);
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

        // Khởi tạo các view
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
    }

    /**
     * Thiết lập các sự kiện
     */
    private void setupListeners() {
        fabFavorite.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            presenter.toggleFavoriteStatus(healthTipId, isFavorite);
            updateFavoriteFabIcon();
        });

        buttonLike.setOnClickListener(v -> {
            isLiked = !isLiked;
            presenter.toggleLike(healthTipId, isLiked);
            updateLikeButtonText();
        });

        buttonShare.setOnClickListener(v -> shareHealthTip());
    }

    /**
     * Cập nhật icon của FAB yêu thích
     */
    private void updateFavoriteFabIcon() {
        if (isFavorite) {
            fabFavorite.setImageResource(R.drawable.ic_favorite);
            fabFavorite.setContentDescription(getString(R.string.remove_from_favorite));
        } else {
            fabFavorite.setImageResource(R.drawable.ic_favorite_border);
            fabFavorite.setContentDescription(getString(R.string.add_to_favorite));
        }
    }

    /**
     * Cập nhật text của nút thích
     */
    private void updateLikeButtonText() {
        buttonLike.setText(isLiked ? R.string.unlike : R.string.like);
    }

    /**
     * Chia sẻ thông tin mẹo sức khỏe
     */
    private void shareHealthTip() {
        String shareText = textViewTitle.getText() + "\n\n" + textViewContent.getText();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, textViewTitle.getText());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_tip)));
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
        textViewContent.setText(healthTip.getContent());
        textViewViewCount.setText(String.valueOf(healthTip.getViewCount()));
        textViewLikeCount.setText(String.valueOf(healthTip.getLikeCount()));

        // Hiển thị hình ảnh
        if (healthTip.getImageUrl() != null && !healthTip.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(healthTip.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(imageViewDetail);
        }

        // Cập nhật trạng thái yêu thích và thích
        isFavorite = healthTip.isFavorite();
        updateFavoriteStatus(isFavorite);

        // Đặt tiêu đề cho CollapsingToolbarLayout
        collapsingToolbarLayout.setTitle(healthTip.getTitle());
    }

    @Override
    public void updateFavoriteStatus(boolean isFavorite) {
        this.isFavorite = isFavorite;
        updateFavoriteFabIcon();
    }

    @Override
    public void updateLikeCount(int likeCount) {
        textViewLikeCount.setText(String.valueOf(likeCount));
    }

    @Override
    public void updateViewCount(int viewCount) {
        textViewViewCount.setText(String.valueOf(viewCount));
    }
}
