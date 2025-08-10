package com.vhn.doan.presentation.shortvideo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.repository.RepositoryCallback;
import com.vhn.doan.data.repository.ShortVideoRepository;
import com.vhn.doan.data.repository.ShortVideoRepositoryImpl;
import com.vhn.doan.presentation.profile.adapter.GridShortVideoAdapter;
import com.vhn.doan.services.FirebaseManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchShortVideoActivity extends AppCompatActivity {

    private EditText editSearch;
    private ImageButton btnBack;
    private RecyclerView recyclerView;
    private GridShortVideoAdapter adapter;
    private ShortVideoRepository repository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_short_video);

        editSearch = findViewById(R.id.editSearch);
        btnBack = findViewById(R.id.buttonBack);
        recyclerView = findViewById(R.id.recyclerView);

        adapter = new GridShortVideoAdapter(this, video -> openInViewer(adapter.getCurrentData(), video));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);

        repository = new ShortVideoRepositoryImpl();

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
        // Lưu keyword vào Firebase cho người dùng hiện tại
        saveSearchKeyword("shorts", query);

        repository.searchShortVideos(query, new RepositoryCallback<List<ShortVideo>>() {
            @Override
            public void onSuccess(List<ShortVideo> result) {
                adapter.updateData(result);
                if (result == null || result.isEmpty()) {
                    Toast.makeText(SearchShortVideoActivity.this, R.string.no_results_found, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(SearchShortVideoActivity.this, R.string.search_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openInViewer(List<ShortVideo> list, ShortVideo clicked) {
        if (list == null || clicked == null) return;
        int startIndex = 0;
        for (int i = 0; i < list.size(); i++) {
            if (clicked.getId() != null && clicked.getId().equals(list.get(i).getId())) {
                startIndex = i;
                break;
            }
        }
        ShortVideoPreloadManager.getInstance().setCachedVideos(list);
        androidx.fragment.app.Fragment fragment = ShortVideoFragment.newInstance();
        android.os.Bundle args = new android.os.Bundle();
        args.putInt("start_position", startIndex);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
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
}