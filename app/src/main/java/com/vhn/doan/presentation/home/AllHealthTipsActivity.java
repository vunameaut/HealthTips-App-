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

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyLayout;
    private HealthTipAdapter adapter;
    private HealthTipRepository repository;

    public static Intent createIntent(Context context, String mode) {
        Intent intent = new Intent(context, AllHealthTipsActivity.class);
        intent.putExtra(EXTRA_MODE, mode);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_health_tips);

        recyclerView = findViewById(R.id.recyclerViewHealthTips);
        progressBar = findViewById(R.id.progressBarLoading);
        emptyLayout = findViewById(R.id.layoutEmpty);

        adapter = new HealthTipAdapter(this, new java.util.ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        repository = new HealthTipRepositoryImpl();

        loadData(getIntent().getStringExtra(EXTRA_MODE));
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
            }
        };

        if (MODE_MOST_VIEWED.equals(mode)) {
            repository.getMostViewedHealthTips(50, callback);
        } else if (MODE_MOST_LIKED.equals(mode)) {
            repository.getMostLikedHealthTips(50, callback);
        } else {
            repository.getLatestHealthTips(50, callback);
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
