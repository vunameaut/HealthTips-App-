package com.vhn.doan.presentation.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.vhn.doan.R;
import com.vhn.doan.data.Category;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.repository.CategoryRepository;
import com.vhn.doan.data.repository.CategoryRepositoryImpl;
import com.vhn.doan.data.repository.HealthTipRepository;
import com.vhn.doan.data.repository.HealthTipRepositoryImpl;
import com.vhn.doan.presentation.home.adapter.CategoryAdapter;
import com.vhn.doan.presentation.home.adapter.HealthTipAdapter;
import com.vhn.doan.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeFragment hiển thị trang chính của ứng dụng
 * Tuân thủ kiến trúc MVP và sử dụng HomePresenter để xử lý logic
 */
public class HomeFragment extends Fragment implements HomeView {

    // UI components
    private RecyclerView recyclerViewCategories;
    private RecyclerView recyclerViewLatestTips;
    private RecyclerView recyclerViewMostViewedTips;
    private RecyclerView recyclerViewMostLikedTips;
    private ProgressBar progressBar;
    private LinearLayout layoutOfflineMode;
    private ImageButton buttonSearch;
    private TextView textViewSeeAllCategories;
    private TextView textViewSeeAllLatestTips;
    private TextView textViewSeeAllMostViewed;
    private TextView textViewSeeAllMostLiked;

    // Adapters
    private CategoryAdapter categoryAdapter;
    private HealthTipAdapter latestTipsAdapter;
    private HealthTipAdapter mostViewedTipsAdapter;
    private HealthTipAdapter mostLikedTipsAdapter;

    // Presenter
    private HomePresenter presenter;

    public HomeFragment() {
        // Constructor mặc định
    }

    /**
     * Phương thức factory để tạo instance mới của fragment này
     */
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo repositories
        CategoryRepository categoryRepository = new CategoryRepositoryImpl();
        HealthTipRepository healthTipRepository = new HealthTipRepositoryImpl();

        // Khởi tạo presenter
        presenter = new HomePresenter(requireContext(), categoryRepository, healthTipRepository);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout cho fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ các thành phần UI
        initViews(view);

        // Thiết lập RecyclerViews
        setupRecyclerViews();

        // Thiết lập các sự kiện click
        setupClickListeners();

        // Gắn presenter với view và bắt đầu tải dữ liệu
        presenter.attachView(this);
    }

    /**
     * Ánh xạ các thành phần UI từ layout
     */
    private void initViews(View view) {
        // RecyclerViews
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        recyclerViewLatestTips = view.findViewById(R.id.recyclerViewLatestTips);
        recyclerViewMostViewedTips = view.findViewById(R.id.recyclerViewMostViewedTips);
        recyclerViewMostLikedTips = view.findViewById(R.id.recyclerViewMostLikedTips);

        // Các thành phần UI khác
        progressBar = view.findViewById(R.id.progressBar);
        layoutOfflineMode = view.findViewById(R.id.layoutOfflineMode);
        buttonSearch = view.findViewById(R.id.buttonSearch);

        // Buttons "Xem tất cả"
        textViewSeeAllCategories = view.findViewById(R.id.textViewSeeAllCategories);
        textViewSeeAllLatestTips = view.findViewById(R.id.textViewSeeAllLatestTips);
        textViewSeeAllMostViewed = view.findViewById(R.id.textViewSeeAllMostViewed);
        textViewSeeAllMostLiked = view.findViewById(R.id.textViewSeeAllMostLiked);
    }

    /**
     * Thiết lập RecyclerViews với adapters và LayoutManagers
     */
    private void setupRecyclerViews() {
        // Setup cho RecyclerView danh mục
        LinearLayoutManager categoriesLayoutManager = new LinearLayoutManager(
                requireContext(), RecyclerView.HORIZONTAL, false);
        recyclerViewCategories.setLayoutManager(categoriesLayoutManager);
        categoryAdapter = new CategoryAdapter(
                requireContext(),
                new ArrayList<>(),
                new CategoryAdapter.CategoryClickListener() {
                    @Override
                    public void onCategoryClick(Category category) {
                        presenter.onCategorySelected(category);
                    }
                });
        recyclerViewCategories.setAdapter(categoryAdapter);

        // Setup cho RecyclerView mẹo sức khỏe mới nhất
        LinearLayoutManager latestTipsLayoutManager = new LinearLayoutManager(
                requireContext(), RecyclerView.HORIZONTAL, false);
        recyclerViewLatestTips.setLayoutManager(latestTipsLayoutManager);
        latestTipsAdapter = new HealthTipAdapter(
                requireContext(),
                new ArrayList<>(),
                new HealthTipAdapter.HealthTipClickListener() {
                    @Override
                    public void onHealthTipClick(HealthTip healthTip) {
                        presenter.onHealthTipSelected(healthTip);
                    }
                });
        recyclerViewLatestTips.setAdapter(latestTipsAdapter);

        // Setup cho RecyclerView mẹo sức khỏe xem nhiều nhất
        LinearLayoutManager mostViewedLayoutManager = new LinearLayoutManager(
                requireContext(), RecyclerView.HORIZONTAL, false);
        recyclerViewMostViewedTips.setLayoutManager(mostViewedLayoutManager);
        mostViewedTipsAdapter = new HealthTipAdapter(
                requireContext(),
                new ArrayList<>(),
                new HealthTipAdapter.HealthTipClickListener() {
                    @Override
                    public void onHealthTipClick(HealthTip healthTip) {
                        presenter.onHealthTipSelected(healthTip);
                    }
                });
        recyclerViewMostViewedTips.setAdapter(mostViewedTipsAdapter);

        // Setup cho RecyclerView mẹo sức khỏe được yêu thích nhất
        LinearLayoutManager mostLikedLayoutManager = new LinearLayoutManager(
                requireContext(), RecyclerView.HORIZONTAL, false);
        recyclerViewMostLikedTips.setLayoutManager(mostLikedLayoutManager);
        mostLikedTipsAdapter = new HealthTipAdapter(
                requireContext(),
                new ArrayList<>(),
                new HealthTipAdapter.HealthTipClickListener() {
                    @Override
                    public void onHealthTipClick(HealthTip healthTip) {
                        presenter.onHealthTipSelected(healthTip);
                    }
                });
        recyclerViewMostLikedTips.setAdapter(mostLikedTipsAdapter);
    }

    /**
     * Thiết lập các sự kiện click
     */
    private void setupClickListeners() {
        // Nút tìm kiếm
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.searchHealthTips("");
            }
        });

        // Xem tất cả danh mục
        textViewSeeAllCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage("Xem tất cả danh mục");
                // Chức năng sẽ được triển khai sau
            }
        });

        // Xem tất cả mẹo mới nhất
        textViewSeeAllLatestTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage("Xem tất cả mẹo mới nhất");
                // Chức năng sẽ được triển khai sau
            }
        });

        // Xem tất cả mẹo xem nhiều nhất
        textViewSeeAllMostViewed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage("Xem tất cả mẹo xem nhiều nhất");
                // Chức năng sẽ được triển khai sau
            }
        });

        // Xem tất cả mẹo được yêu thích nhất
        textViewSeeAllMostLiked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage("Xem tất cả mẹo được yêu thích nhất");
                // Chức năng sẽ được triển khai sau
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start(); // Tải dữ liệu khi Fragment được hiển thị

        // Lắng nghe thay đổi realtime từ Firebase
        presenter.listenToCategories();
        presenter.listenToLatestHealthTips();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stop(); // Dừng lắng nghe khi Fragment không được hiển thị
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detachView(); // Tách View khỏi Presenter
    }

    // Triển khai các phương thức của HomeView

    @Override
    public void showCategories(List<Category> categories) {
        categoryAdapter.updateCategories(categories);
    }

    @Override
    public void showLatestHealthTips(List<HealthTip> healthTips) {
        latestTipsAdapter.updateHealthTips(healthTips);
    }

    @Override
    public void showMostViewedHealthTips(List<HealthTip> healthTips) {
        mostViewedTipsAdapter.updateHealthTips(healthTips);
    }

    @Override
    public void showMostLikedHealthTips(List<HealthTip> healthTips) {
        mostLikedTipsAdapter.updateHealthTips(healthTips);
    }

    @Override
    public void showOfflineMode() {
        layoutOfflineMode.setVisibility(View.VISIBLE);
    }

    @Override
    public void navigateToCategoryDetail(Category category) {
        if (category != null && category.getId() != null) {
            // Tạo Intent để chuyển đến CategoryDetailListActivity
            android.content.Intent intent = new android.content.Intent(requireContext(), com.vhn.doan.presentation.category.detail.CategoryDetailListActivity.class);

            // Truyền ID của danh mục
            intent.putExtra(Constants.CATEGORY_ID_KEY, category.getId());

            // Khởi chạy Activity mới
            startActivity(intent);
        } else {
            showError("Không thể mở chi tiết danh mục do thiếu thông tin");
        }
    }

    @Override
    public void navigateToHealthTipDetail(HealthTip healthTip) {
        if (healthTip != null && healthTip.getId() != null) {
            // Tạo Intent để chuyển đến HealthTipDetailActivity
            android.content.Intent intent = com.vhn.doan.presentation.healthtip.detail.HealthTipDetailActivity.createIntent(
                    requireContext(),
                    healthTip.getId()
            );

            // Khởi chạy Activity mới
            startActivity(intent);
        } else {
            showError("Không thể mở chi tiết mẹo sức khỏe do thiếu thông tin");
        }
    }

    @Override
    public void navigateToSearch() {
        // Triển khai chức năng chuyển đến trang tìm kiếm
        showMessage("Chuyển đến trang tìm kiếm");
        // Sẽ triển khai sau khi có SearchActivity/Fragment
    }

    @Override
    public void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showMessage(String message) {
        if (isAdded() && getView() != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showError(String errorMessage) {
        if (isAdded() && getView() != null) {
            Snackbar.make(getView(), errorMessage, Snackbar.LENGTH_LONG).show();
        }
    }
}
