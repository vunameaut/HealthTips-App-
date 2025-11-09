package com.vhn.doan.presentation.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.repository.HealthTipRepository;
import com.vhn.doan.data.repository.HealthTipRepositoryImpl;
import com.vhn.doan.presentation.home.adapter.HealthTipAdapter;

import java.util.List;

/**
 * Activity hiển thị danh sách mẹo sức khỏe theo các chế độ: mới nhất, xem nhiều nhất hoặc ưa thích nhất.
 */
public class AllHealthTipsActivity extends AppCompatActivity implements HealthTipAdapter.HealthTipClickListener {

    public static final String EXTRA_MODE = "mode";
    public static final String MODE_LATEST = "latest";
    public static final String MODE_MOST_VIEWED = "most_viewed";
    public static final String MODE_MOST_LIKED = "most_liked";
    public static final String MODE_RECOMMENDED = "recommended";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyLayout;
    private HealthTipAdapter adapter;
    private HealthTipRepository repository;
    private androidx.appcompat.widget.Toolbar toolbar;

    public static Intent createIntent(Context context, String mode) {
        Intent intent = new Intent(context, AllHealthTipsActivity.class);
        intent.putExtra(EXTRA_MODE, mode);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_health_tips);

        // Thiết lập toolbar
        setupToolbar();

        recyclerView = findViewById(R.id.recyclerViewHealthTips);
        progressBar = findViewById(R.id.progressBarLoading);
        emptyLayout = findViewById(R.id.layoutEmpty);

        // Thiết lập RecyclerView
        adapter = new HealthTipAdapter(this, new java.util.ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        repository = new HealthTipRepositoryImpl(this);

        // Lấy mode từ Intent và thiết lập tiêu đề
        String mode = getIntent().getStringExtra(EXTRA_MODE);
        setupTitle(mode);

        // Tải dữ liệu
        loadData(mode);
    }

    /**
     * Thiết lập toolbar với nút quay lại
     */
    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hiển thị nút quay lại
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    /**
     * Thiết lập tiêu đề động dựa trên chế độ hiển thị
     */
    private void setupTitle(String mode) {
        String title = getString(R.string.app_name); // Mặc định

        if (mode != null) {
            switch (mode) {
                case MODE_RECOMMENDED:
                    title = getString(R.string.all_recommended_tips);
                    break;
                case MODE_LATEST:
                    title = getString(R.string.all_latest_tips);
                    break;
                case MODE_MOST_VIEWED:
                    title = getString(R.string.all_most_viewed_tips);
                    break;
                case MODE_MOST_LIKED:
                    title = getString(R.string.all_most_liked_tips);
                    break;
            }
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    /**
     * Xử lý sự kiện khi người dùng nhấn nút quay lại
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Xử lý nút back của thiết bị
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Thêm animation trượt khi quay lại
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void loadData(String mode) {
        progressBar.setVisibility(android.view.View.VISIBLE);
        HealthTipRepository.HealthTipCallback callback = new HealthTipRepository.HealthTipCallback() {
            @Override
            public void onSuccess(List<HealthTip> healthTips) {
                progressBar.setVisibility(android.view.View.GONE);
                if (healthTips == null || healthTips.isEmpty()) {
                    emptyLayout.setVisibility(android.view.View.VISIBLE);
                } else {
                    emptyLayout.setVisibility(android.view.View.GONE);
                    adapter.updateHealthTips(healthTips);
                }
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(android.view.View.GONE);
                emptyLayout.setVisibility(android.view.View.VISIBLE);
                // Hiển thị thông báo lỗi
                android.widget.Toast.makeText(AllHealthTipsActivity.this,
                    "Lỗi khi tải dữ liệu: " + errorMessage,
                    android.widget.Toast.LENGTH_SHORT).show();
            }
        };

        if (mode != null) {
            switch (mode) {
                case MODE_RECOMMENDED:
                    // Chỉ hiển thị 10 bài viết đề xuất phù hợp cho hôm nay
                    repository.getTodayRecommendedHealthTips(10, callback);
                    break;
                case MODE_LATEST:
                    repository.getLatestHealthTips(50, callback);
                    break;
                case MODE_MOST_VIEWED:
                    repository.getMostViewedHealthTips(50, callback);
                    break;
                case MODE_MOST_LIKED:
                    repository.getMostLikedHealthTips(50, callback);
                    break;
                default:
                    repository.getAllHealthTips(callback);
                    break;
            }
        } else {
            repository.getAllHealthTips(callback);
        }
    }

    @Override
    public void onHealthTipClick(HealthTip healthTip) {
        if (healthTip != null && healthTip.getId() != null) {
            startActivity(com.vhn.doan.presentation.healthtip.detail.HealthTipDetailActivity.createIntent(this, healthTip.getId()));
        }
    }

    @Override
    public void onFavoriteClick(HealthTip healthTip, boolean isFavorite) {
        // Chưa hỗ trợ thay đổi tại đây
    }
}
