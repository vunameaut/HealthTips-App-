package com.vhn.doan.presentation.category;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.Category;
import com.vhn.doan.data.repository.CategoryRepository;
import com.vhn.doan.data.repository.CategoryRepositoryImpl;
import com.vhn.doan.presentation.category.detail.CategoryDetailListActivity;
import com.vhn.doan.utils.Constants;

import java.util.List;

/**
 * Activity hiển thị tất cả các chủ đề sức khỏe
 */
public class AllCategoriesActivity extends AppCompatActivity implements CategoryView, CategoryAdapter.OnCategoryClickListener {

    // Views
    private RecyclerView recyclerViewCategories;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private TextView textViewError;
    private ImageButton buttonBack;

    // Adapter và Presenter
    private CategoryAdapter adapter;
    private CategoryPresenter presenter;

    /**
     * Tạo Intent để mở Activity này
     */
    public static Intent createIntent(Context context) {
        return new Intent(context, AllCategoriesActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_categories);

        // Khởi tạo các views
        initViews();

        // Khởi tạo adapter
        setupRecyclerView();

        // Khởi tạo presenter
        initPresenter();

        // Tải dữ liệu
        loadData();
    }

    /**
     * Khởi tạo các views
     */
    private void initViews() {
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        textViewError = findViewById(R.id.textViewError);
        buttonBack = findViewById(R.id.buttonBack);

        // Xử lý nút back
        if (buttonBack != null) {
            buttonBack.setOnClickListener(v -> finish());
        }
    }

    /**
     * Thiết lập RecyclerView và Adapter
     */
    private void setupRecyclerView() {
        // Sử dụng GridLayoutManager với 2 cột
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerViewCategories.setLayoutManager(layoutManager);

        // Khởi tạo adapter và gắn vào recyclerView
        adapter = new CategoryAdapter(this, this);
        recyclerViewCategories.setAdapter(adapter);
    }

    /**
     * Khởi tạo presenter
     */
    private void initPresenter() {
        CategoryRepository categoryRepository = new CategoryRepositoryImpl();
        presenter = new CategoryPresenter(this, categoryRepository);
    }

    /**
     * Tải dữ liệu danh mục
     */
    private void loadData() {
        presenter.loadCategories();
        presenter.startListeningToCategories();
    }

    @Override
    public void displayCategories(List<Category> categories) {
        recyclerViewCategories.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        textViewError.setVisibility(View.GONE);

        adapter.setCategories(categories);
    }

    @Override
    public void showEmptyView() {
        recyclerViewCategories.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        textViewError.setVisibility(View.GONE);
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showError(String message) {
        recyclerViewCategories.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
        textViewError.setVisibility(View.VISIBLE);
        textViewError.setText(message);
    }

    @Override
    public void navigateToCategoryDetail(Category category) {
        if (category != null && category.getId() != null) {
            Intent intent = new Intent(this, CategoryDetailListActivity.class);
            intent.putExtra(Constants.INTENT_CATEGORY_ID, category.getId());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Không thể mở chi tiết danh mục", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCategoryClick(Category category) {
        presenter.onCategorySelected(category);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.onDestroy();
        }
    }
}

