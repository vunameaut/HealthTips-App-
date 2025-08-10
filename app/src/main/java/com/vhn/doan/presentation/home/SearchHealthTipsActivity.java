package com.vhn.doan.presentation.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.repository.HealthTipRepository;
import com.vhn.doan.data.repository.HealthTipRepositoryImpl;
import com.vhn.doan.presentation.home.adapter.HealthTipAdapter;
import com.vhn.doan.services.FirebaseManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchHealthTipsActivity extends AppCompatActivity implements HealthTipAdapter.HealthTipClickListener {

    private EditText editSearch;
    private ImageButton btnBack;
    private RecyclerView recyclerView;
    private HealthTipAdapter adapter;
    private HealthTipRepository repository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_health_tips);

        editSearch = findViewById(R.id.editSearch);
        btnBack = findViewById(R.id.buttonBack);
        recyclerView = findViewById(R.id.recyclerView);

        adapter = new HealthTipAdapter(this, new java.util.ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        repository = new HealthTipRepositoryImpl();

        btnBack.setOnClickListener(v -> onBackPressed());
        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(editSearch.getText().toString().trim());
                return true;
            }
            return false;
        });
    }

    private void performSearch(String query) {
        if (TextUtils.isEmpty(query)) {
            Toast.makeText(this, R.string.enter_search_keyword, Toast.LENGTH_SHORT).show();
            return;
        }
        saveSearchKeyword("health_tips", query);

        repository.searchHealthTips(query, new HealthTipRepository.HealthTipCallback() {
            @Override
            public void onSuccess(List<HealthTip> healthTips) {
                if (healthTips == null || healthTips.isEmpty()) {
                    Toast.makeText(SearchHealthTipsActivity.this, R.string.no_results_found, Toast.LENGTH_SHORT).show();
                }
                adapter.updateHealthTips(healthTips);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(SearchHealthTipsActivity.this, R.string.search_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveSearchKeyword(String type, String keyword) {
        try {
            String userId = FirebaseManager.getInstance().getCurrentUserId();
            if (userId == null) return;
            com.google.firebase.database.DatabaseReference ref = FirebaseManager.getInstance()
                    .getDatabaseReference()
                    .child("user_searches")
                    .child(userId)
                    .child(type)
                    .push();
            Map<String, Object> data = new HashMap<>();
            data.put("keyword", keyword);
            data.put("timestamp", System.currentTimeMillis());
            ref.setValue(data);
        } catch (Exception ignored) {}
    }

    @Override
    public void onHealthTipClick(HealthTip healthTip) {
        if (healthTip != null && healthTip.getId() != null) {
            startActivity(com.vhn.doan.presentation.healthtip.detail.HealthTipDetailActivity.createIntent(this, healthTip.getId()));
        }
    }

    @Override
    public void onFavoriteClick(HealthTip healthTip, boolean isFavorite) {
        // No-op in search screen
    }
}