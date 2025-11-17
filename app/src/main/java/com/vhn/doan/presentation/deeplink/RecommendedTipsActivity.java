package com.vhn.doan.presentation.deeplink;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.repository.HealthTipRepositoryImpl;
import com.vhn.doan.data.repository.HealthTipRepository;
import com.vhn.doan.presentation.home.adapter.HealthTipAdapter;
import com.vhn.doan.presentation.healthtip.detail.HealthTipDetailActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity hiển thị danh sách bài viết được đề xuất từ notification
 */
public class RecommendedTipsActivity extends AppCompatActivity
        implements HealthTipAdapter.HealthTipClickListener {

    private static final String TAG = "RecommendedTipsActivity";

    private RecyclerView recyclerView;
    private HealthTipAdapter adapter;
    private HealthTipRepository healthTipRepository;
    private List<String> tipIds = new ArrayList<>();
    private List<HealthTip> healthTips = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");

        setContentView(R.layout.activity_recommended_tips);
        Log.d(TAG, "Layout set");

        setupActionBar();
        Log.d(TAG, "ActionBar setup complete");

        setupRecyclerView();
        Log.d(TAG, "RecyclerView setup complete");

        parseIntentData();
        Log.d(TAG, "Intent data parsed, tipIds count: " + tipIds.size());

        loadRecommendedTips();
        Log.d(TAG, "loadRecommendedTips called");
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Bài viết đề xuất");
        }
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.rv_recommended_tips);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        healthTipRepository = new HealthTipRepositoryImpl();
        adapter = new HealthTipAdapter(this, healthTips, this);

        recyclerView.setAdapter(adapter);
    }

    private void parseIntentData() {
        String tipsJson = getIntent().getStringExtra("tips_json");
        Log.d(TAG, "parseIntentData - tips_json: " + tipsJson);

        if (tipsJson != null) {
            try {
                JSONArray tipsArray = new JSONArray(tipsJson);
                Log.d(TAG, "JSON array length: " + tipsArray.length());

                for (int i = 0; i < tipsArray.length(); i++) {
                    JSONObject tipObj = tipsArray.getJSONObject(i);
                    String tipId = tipObj.getString("healthTipId");
                    tipIds.add(tipId);
                    Log.d(TAG, "Added tip ID: " + tipId);
                }

                Log.d(TAG, "Parsed " + tipIds.size() + " tip IDs from notification");
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing tips JSON", e);
                Log.e(TAG, "JSON content: " + tipsJson);
                showError("Lỗi xử lý dữ liệu");
            }
        } else {
            Log.w(TAG, "tips_json is null!");
        }
    }

    private void loadRecommendedTips() {
        Log.d(TAG, "loadRecommendedTips - tipIds.size: " + tipIds.size());

        if (tipIds.isEmpty()) {
            Log.w(TAG, "No tips to load!");
            showError("Không có bài viết nào");
            return;
        }

        // Load từng tip từ Firebase
        int[] loadedCount = {0};

        for (String tipId : tipIds) {
            Log.d(TAG, "Loading tip: " + tipId);
            healthTipRepository.getHealthTipDetail(tipId, new HealthTipRepository.SingleHealthTipCallback() {
                @Override
                public void onSuccess(HealthTip healthTip) {
                    healthTips.add(healthTip);
                    loadedCount[0]++;

                    // Khi đã load hết tất cả tips
                    if (loadedCount[0] == tipIds.size()) {
                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "Error loading tip: " + errorMessage);
                    loadedCount[0]++;

                    if (loadedCount[0] == tipIds.size()) {
                        runOnUiThread(() -> {
                            if (!healthTips.isEmpty()) {
                                adapter.notifyDataSetChanged();
                            } else {
                                showError("Không thể tải bài viết");
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void onHealthTipClick(HealthTip healthTip) {
        Intent intent = new Intent(this, HealthTipDetailActivity.class);
        intent.putExtra("health_tip_id", healthTip.getId());
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(HealthTip healthTip, boolean isFavorite) {
        // Không cần xử lý favorite trong màn hình này
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
