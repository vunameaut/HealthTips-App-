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
 * Hỗ trợ animation trượt từ dưới lên và swipe-to-dismiss
 */
public class HealthTipDetailActivity extends AppCompatActivity implements HealthTipDetailView {

    private static final String EXTRA_HEALTH_TIP_ID = "health_tip_id";

    // UI components
    private SwipeToDismissLayout swipeToDismissLayout;
    private View backgroundOverlay;
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
    private View dragHandle;

    // Presenter
    private HealthTipDetailPresenter presenter;

    // Data
    private String healthTipId;
    private boolean isLiked = false;
    private boolean isFavorite = false;

    /**
     * Tạo Intent để mở Activity này với animation trượt từ dưới lên
     */
    public static Intent createIntent(Context context, String healthTipId) {
        Intent intent = new Intent(context, HealthTipDetailActivity.class);
        intent.putExtra(EXTRA_HEALTH_TIP_ID, healthTipId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Đặt activity ở vị trí dưới màn hình ngay từ đầu để tránh nháy
        setupInitialPosition();

        setContentView(R.layout.activity_health_tip_detail);

        // Khởi tạo UI components
        initViews();
        setupSwipeToDismiss();

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

        // Thiết lập listeners
        setupListeners();

        // Bắt đầu animation trượt lên từ dưới
        startSlideUpAnimation();

        // Tải dữ liệu
        presenter.loadHealthTipDetail(healthTipId);
    }

    /**
     * Đặt vị trí ban đầu của activity ở dưới màn hình
     */
    private void setupInitialPosition() {
        // Lấy kích thước màn hình
        android.view.WindowManager windowManager = getWindowManager();
        android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;

        // Đặt activity ở vị trí dưới màn hình
        View decorView = getWindow().getDecorView();
        decorView.setTranslationY(screenHeight);
        decorView.setAlpha(1.0f);
    }

    /**
     * Bắt đầu animation trượt lên từ dưới
     */
    private void startSlideUpAnimation() {
        View decorView = getWindow().getDecorView();

        // Animation trượt lên từ dưới với hiệu ứng mượt mà
        decorView.animate()
                .translationY(0)
                .setDuration(300)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .setListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(android.animation.Animator animation) {
                        // Đảm bảo activity hiển thị trong suốt quá trình animation
                        decorView.setAlpha(1.0f);
                    }
                })
                .start();
    }

    /**
     * Khởi tạo các view
     */
    private void initViews() {
        // Khởi tạo SwipeToDismissLayout
        swipeToDismissLayout = findViewById(R.id.swipeToDismissLayout);
        dragHandle = findViewById(R.id.dragHandle);

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

        // Khởi tạo background overlay
        backgroundOverlay = findViewById(R.id.backgroundOverlay);

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
        if (textViewViewCount != null) textViewViewCount.setVisibility(View.GONE);
        if (textViewLikeCount != null) textViewLikeCount.setVisibility(View.GONE);
        if (fabFavorite != null) fabFavorite.setVisibility(View.GONE);
        if (buttonLike != null) buttonLike.setVisibility(View.GONE);
        if (buttonShare != null) buttonShare.setVisibility(View.GONE);
        if (imageViewDetail != null) imageViewDetail.setVisibility(View.GONE);

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
            textViewContent,
            buttonLike,
            buttonShare,
            fabFavorite
        };

        // Animate từng view với delay nhỏ để tạo hiệu ứng cascade
        for (int i = 0; i < viewsToAnimate.length; i++) {
            final View view = viewsToAnimate[i];
            if (view != null) {
                // Đặt vị trí ban đầu (ẩn và offset)
                view.setAlpha(0f);
                view.setTranslationY(50f);
                view.setVisibility(View.VISIBLE);

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

    /**
     * Thiết lập chức năng swipe-to-dismiss
     */
    private void setupSwipeToDismiss() {
        if (swipeToDismissLayout != null) {
            swipeToDismissLayout.setOnDismissListener(new SwipeToDismissLayout.OnDismissListener() {
                @Override
                public void onDismiss() {
                    // Đóng activity với animation
                    finishWithAnimation();
                }

                @Override
                public void onDragProgress(float progress) {
                    // Cập nhật hiệu ứng khi đang kéo
                    // Progress từ 0.0 (không kéo) đến 1.0 (kéo hoàn toàn)
                    updateDragProgress(progress);
                }
            });
        }
    }

    /**
     * Cập nhật hiệu ứng khi đang kéo - làm cho activity phía sau hiển thị rõ hơn
     */
    private void updateDragProgress(float progress) {
        // Thay đổi độ mờ của drag handle
        if (dragHandle != null) {
            dragHandle.setAlpha(1.0f - progress * 0.5f);
        }

        // Thông báo cho activity phía trước để cập nhật hiệu ứng
        // Activity phía trước sẽ được làm sáng dần khi kéo xuống
        updateBackgroundActivityVisibility(progress);
    }

    /**
     * Cập nhật hiển thị activity phía sau khi đang kéo
     */
    private void updateBackgroundActivityVisibility(float progress) {
        // Tính toán độ mờ của overlay để làm cho activity phía sau hiển thị rõ hơn
        // Khi progress = 0 (không kéo): overlay hoàn toàn mờ
        // Khi progress = 1 (kéo hoàn toàn): overlay trong suốt, activity phía sau hiển thị rõ

        // Tạo overlay effect để activity phía sau hiển thị dần
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            // Thay đổi background alpha của window để thấy activity phía sau
            float overlayAlpha = 0.3f * (1.0f - progress); // Bắt đầu từ 30% opacity, giảm dần về 0
            getWindow().setStatusBarColor(
                    android.graphics.Color.argb(
                            (int) (overlayAlpha * 255),
                            0, 0, 0
                    )
            );
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
        textViewContent.setText(healthTip.getContent());
        textViewViewCount.setText(String.valueOf(healthTip.getViewCount()));
        textViewLikeCount.setText(String.valueOf(healthTip.getLikeCount()));

        // Set category text - đây là phần bị thiếu
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
            Glide.with(this)
                    .load(healthTip.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(imageViewDetail);
        } else {
            imageViewDetail.setImageResource(R.drawable.placeholder_image);
        }

        // Cập nhật trạng thái favorite và like
        isFavorite = healthTip.isFavorite();
        isLiked = healthTip.isLiked();
        updateFavoriteFabIcon();
        updateLikeButtonText();

        // Hiển thị nội dung với animation
        showContentViewsWithAnimation();
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
        finishWithAnimation();
    }

    @Override
    public void onBackPressed() {
        finishWithAnimation();
    }

    /**
     * Đóng activity với animation trượt xuống dưới
     */
    private void finishWithAnimation() {
        super.finish();
        // Áp dụng animation trượt xuống dưới khi đóng activity
        overridePendingTransition(R.anim.restore_and_scale_up, R.anim.slide_down_to_bottom);
    }
}
