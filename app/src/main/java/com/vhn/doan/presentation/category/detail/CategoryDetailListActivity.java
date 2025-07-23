package com.vhn.doan.presentation.category.detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.Category;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.presentation.healthtip.detail.HealthTipDetailActivity;
import com.vhn.doan.services.FirebaseManager;
import com.vhn.doan.utils.Constants;

import java.util.List;

/**
 * Activity hiển thị danh sách các mẹo sức khỏe theo danh mục
 */
public class CategoryDetailListActivity extends AppCompatActivity implements CategoryDetailListView, HealthTipAdapter.HealthTipClickListener {

    private CategoryDetailListPresenter presenter;
    private HealthTipAdapter adapter;

    private RecyclerView recyclerViewHealthTips;
    private ProgressBar progressBarLoading;
    private LinearLayout layoutEmpty;
    private TextView textViewError;
    private TextView textViewCategoryTitle;

    private String categoryId;
    private String selectedHealthTipId; // Thêm biến để lưu ID của mẹo sức khỏe cần cuộn đến

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail_list);

        // Lấy categoryId từ intent
        categoryId = getIntent().getStringExtra(Constants.INTENT_CATEGORY_ID);
        selectedHealthTipId = getIntent().getStringExtra(Constants.INTENT_HEALTH_TIP_ID);

        if (categoryId == null) {
            finish();
            return;
        }

        // Khởi tạo views
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupPresenter();

        // Load dữ liệu
        presenter.loadCategoryDetails(categoryId);
        presenter.loadHealthTipsByCategory(categoryId);
    }

    private void initViews() {
        recyclerViewHealthTips = findViewById(R.id.recyclerViewHealthTips);
        progressBarLoading = findViewById(R.id.progressBarLoading);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        textViewError = findViewById(R.id.textViewError);
        textViewCategoryTitle = findViewById(R.id.textViewCategoryTitle);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new HealthTipAdapter(this, this);
        recyclerViewHealthTips.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHealthTips.setAdapter(adapter);
    }

    private void setupPresenter() {
        FirebaseManager firebaseManager = new FirebaseManager();
        presenter = new CategoryDetailListPresenterImpl(firebaseManager);
        presenter.attachView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    // Implementation của CategoryDetailListView interface
    @Override
    public void displayHealthTips(List<HealthTip> healthTips) {
        if (healthTips != null && !healthTips.isEmpty()) {
            adapter.updateHealthTips(healthTips);
            recyclerViewHealthTips.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);

            // Cuộn đến mẹo sức khỏe được chọn nếu có
            if (selectedHealthTipId != null) {
                scrollToHealthTip(selectedHealthTipId);
            }
        } else {
            showEmptyState();
        }
    }

    @Override
    public void displayCategoryDetails(Category category) {
        if (category != null && textViewCategoryTitle != null) {
            textViewCategoryTitle.setText(category.getName());

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(category.getName());
            }
        }
    }

    @Override
    public void showEmptyState() {
        recyclerViewHealthTips.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        progressBarLoading.setVisibility(View.GONE);
    }

    @Override
    public void showLoading(boolean loading) {
        if (loading) {
            progressBarLoading.setVisibility(View.VISIBLE);
            recyclerViewHealthTips.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.GONE);
            if (textViewError != null) {
                textViewError.setVisibility(View.GONE);
            }
        } else {
            progressBarLoading.setVisibility(View.GONE);
        }
    }

    @Override
    public void showEmptyView() {
        showEmptyState(); // Delegate to showEmptyState for consistency
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showError(String message) {
        progressBarLoading.setVisibility(View.GONE);
        recyclerViewHealthTips.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        if (textViewError != null) {
            textViewError.setText(message);
            textViewError.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void setCategoryTitle(String title) {
        if (textViewCategoryTitle != null) {
            textViewCategoryTitle.setText(title);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void navigateToHealthTipDetails(String healthTipId) {
        Intent intent = new Intent(this, HealthTipDetailActivity.class);
        intent.putExtra(Constants.INTENT_HEALTH_TIP_ID, healthTipId);
        startActivity(intent);
    }

    // Implementation của HealthTipAdapter.HealthTipClickListener
    @Override
    public void onHealthTipClick(HealthTip healthTip) {
        if (presenter != null && healthTip != null) {
            presenter.onHealthTipSelected(healthTip.getId());
        }
    }

    @Override
    public void onFavoriteClick(HealthTip healthTip, boolean isFavorite) {
        // Xử lý sự kiện khi người dùng click vào nút yêu thích
        if (isFavorite) {
            showMessage("Đã thêm vào danh sách yêu thích");
        } else {
            showMessage("Đã xóa khỏi danh sách yêu thích");
        }
    }

    /**
     * Cuộn đến mẹo sức khỏe được chỉ định
     */
    private void scrollToHealthTip(String healthTipId) {
        if (adapter != null && healthTipId != null) {
            int position = findHealthTipPosition(healthTipId);
            if (position != -1) {
                recyclerViewHealthTips.smoothScrollToPosition(position);
            }
        }
    }

    /**
     * Tìm vị trí của health tip theo ID
     */
    private int findHealthTipPosition(String healthTipId) {
        if (adapter != null && adapter.getHealthTipsList() != null) {
            List<HealthTip> healthTips = adapter.getHealthTipsList();
            for (int i = 0; i < healthTips.size(); i++) {
                HealthTip healthTip = healthTips.get(i);
                if (healthTip != null && healthTipId.equals(healthTip.getId())) {
                    return i;
                }
            }
        }
        return -1;
    }
}
