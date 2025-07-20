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
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.services.FirebaseManager;
import com.vhn.doan.utils.Constants;

import java.util.List;

/**
 * Activity hiển thị danh sách các mẹo sức khỏe theo danh mục
 */
public class CategoryDetailActivity extends AppCompatActivity implements CategoryDetailView, HealthTipAdapter.OnHealthTipClickListener {

    private CategoryDetailPresenter presenter;
    private HealthTipAdapter adapter;

    private RecyclerView recyclerViewHealthTips;
    private ProgressBar progressBarLoading;
    private LinearLayout layoutEmpty;
    private TextView textViewError;
    private TextView textViewCategoryTitle;

    private String categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);

        // Lấy categoryId từ intent
        categoryId = getIntent().getStringExtra(Constants.INTENT_CATEGORY_ID);
        if (categoryId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin danh mục", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo views
        initViews();

        // Khởi tạo presenter
        FirebaseManager firebaseManager = FirebaseManager.getInstance();
        presenter = new CategoryDetailPresenterImpl(firebaseManager);
        presenter.attachView(this);

        // Load dữ liệu
        presenter.loadCategoryDetails(categoryId);
        presenter.loadHealthTipsByCategory(categoryId);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbarCategoryDetail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        textViewCategoryTitle = findViewById(R.id.textViewCategoryTitle);
        recyclerViewHealthTips = findViewById(R.id.recyclerViewHealthTips);
        progressBarLoading = findViewById(R.id.progressBarLoading);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        textViewError = findViewById(R.id.textViewError);

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> onBackPressed());

        // Khởi tạo RecyclerView và Adapter
        recyclerViewHealthTips.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HealthTipAdapter(this, this);
        recyclerViewHealthTips.setAdapter(adapter);
    }

    @Override
    public void displayHealthTips(List<HealthTip> healthTips) {
        recyclerViewHealthTips.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        textViewError.setVisibility(View.GONE);

        adapter.setHealthTips(healthTips);
    }

    @Override
    public void showLoading(boolean loading) {
        progressBarLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showEmptyView() {
        recyclerViewHealthTips.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        textViewError.setVisibility(View.GONE);
    }

    @Override
    public void showError(String message) {
        recyclerViewHealthTips.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
        textViewError.setVisibility(View.VISIBLE);
        textViewError.setText(message);
    }

    @Override
    public void setCategoryTitle(String title) {
        textViewCategoryTitle.setText(title);
    }

    @Override
    public void navigateToHealthTipDetails(String healthTipId) {
        // Gọi đến phương thức đã tồn tại để tái sử dụng code
        navigateToHealthTipDetail(healthTipId);
    }

    @Override
    public void onHealthTipClick(String healthTipId) {
        navigateToHealthTipDetail(healthTipId);
    }

    /**
     * Điều hướng đến màn hình chi tiết mẹo sức khỏe
     * @param healthTipId ID của mẹo sức khỏe
     */
    public void navigateToHealthTipDetail(String healthTipId) {
        // Khi tạo HealthTipDetailActivity, sẽ điều hướng đến đó
        // TODO: Thay thế bằng Intent đến HealthTipDetailActivity khi tạo xong
        Toast.makeText(this, "Đã chọn mẹo sức khỏe: " + healthTipId, Toast.LENGTH_SHORT).show();

        // Mẫu code cho điều hướng đến HealthTipDetailActivity trong tương lai
        /*
        Intent intent = new Intent(this, HealthTipDetailActivity.class);
        intent.putExtra(Constants.INTENT_HEALTH_TIP_ID, healthTipId);
        startActivity(intent);
        */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }
}
