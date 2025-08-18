package com.vhn.doan.presentation.home;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.recyclerview.widget.LinearSmoothScroller;
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
import com.vhn.doan.presentation.home.adapter.InfiniteHealthTipAdapter;
import com.vhn.doan.presentation.home.adapter.CategorySkeletonAdapter;
import com.vhn.doan.presentation.home.adapter.HealthTipSkeletonAdapter;
import com.vhn.doan.presentation.category.CategoryFragment;
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
    private RecyclerView recyclerViewRecommendedTips;
    private RecyclerView recyclerViewLatestTips;
    private RecyclerView recyclerViewMostViewedTips;
    private RecyclerView recyclerViewMostLikedTips;
    private ProgressBar progressBar;
    private LinearLayout layoutOfflineMode;
    private ImageButton buttonSearch;
    private TextView textViewSeeAllCategories;
    private TextView textViewSeeAllRecommended;
    private TextView textViewSeeAllLatestTips;
    private TextView textViewSeeAllMostViewed;
    private TextView textViewSeeAllMostLiked;

    // Adapters - Real data
    private CategoryAdapter categoryAdapter;
    private InfiniteHealthTipAdapter recommendedTipsAdapter; // Sử dụng InfiniteHealthTipAdapter cho đề xuất
    private HealthTipAdapter latestTipsAdapter;
    private HealthTipAdapter mostViewedTipsAdapter;
    private HealthTipAdapter mostLikedTipsAdapter;

    // Skeleton Adapters
    private CategorySkeletonAdapter categorySkeletonAdapter;
    private HealthTipSkeletonAdapter recommendedTipsSkeletonAdapter;
    private HealthTipSkeletonAdapter latestTipsSkeletonAdapter;
    private HealthTipSkeletonAdapter mostViewedTipsSkeletonAdapter;
    private HealthTipSkeletonAdapter mostLikedTipsSkeletonAdapter;

    // Presenter
    private HomePresenter presenter;

    // Loading state flags
    private boolean isCategoriesLoaded = false;
    private boolean isRecommendedTipsLoaded = false;
    private boolean isLatestTipsLoaded = false;
    private boolean isMostViewedTipsLoaded = false;
    private boolean isMostLikedTipsLoaded = false;

    // Auto-scroll animation cho phần đề xuất
    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;
    private int currentRecommendedPosition = 0;
    private boolean isAutoScrolling = false;
    private static final long AUTO_SCROLL_DELAY = 4000; // 4 giây
    private static final int INFINITE_SCROLL_MULTIPLIER = 1000; // Để tạo hiệu ứng vô hạn

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

        // Thiết lập Layout Managers cho RecyclerViews TRƯỚC
        setupLayoutManagers();

        // Hiển thị skeleton loading ngay sau khi có Layout Managers
        setupSkeletonLoading();

        // Khởi tạo real adapters (nhưng chưa set vào RecyclerViews)
        initializeRealAdapters();

        // Thiết lập các sự kiện click
        setupClickListeners();

        // Thiết lập touch listener cho auto-scroll animation
        setupRecommendedTouchListener();

        // Gắn presenter với view và bắt đầu tải dữ liệu
        presenter.attachView(this);
    }

    /**
     * Thiết lập skeleton loading cho tất cả RecyclerViews
     */
    private void setupSkeletonLoading() {
        // Hiển thị tất cả các tiêu đề và "Xem tất cả" ngay lập tức
        showAllTitlesAndSeeAll();

        // Thiết lập skeleton adapters
        setupSkeletonAdapters();
    }

    /**
     * Hiển thị tất cả tiêu đề và nút "Xem tất cả"
     */
    private void showAllTitlesAndSeeAll() {
        if (textViewCategoriesTitle != null) textViewCategoriesTitle.setVisibility(View.VISIBLE);
        if (textViewRecommendedTipsTitle != null) textViewRecommendedTipsTitle.setVisibility(View.VISIBLE);
        if (textViewLatestTipsTitle != null) textViewLatestTipsTitle.setVisibility(View.VISIBLE);
        if (textViewMostViewedTitle != null) textViewMostViewedTitle.setVisibility(View.VISIBLE);
        if (textViewMostLikedTitle != null) textViewMostLikedTitle.setVisibility(View.VISIBLE);

        if (textViewSeeAllCategories != null) textViewSeeAllCategories.setVisibility(View.VISIBLE);
        if (textViewSeeAllRecommended != null) textViewSeeAllRecommended.setVisibility(View.VISIBLE);
        if (textViewSeeAllLatestTips != null) textViewSeeAllLatestTips.setVisibility(View.VISIBLE);
        if (textViewSeeAllMostViewed != null) textViewSeeAllMostViewed.setVisibility(View.VISIBLE);
        if (textViewSeeAllMostLiked != null) textViewSeeAllMostLiked.setVisibility(View.VISIBLE);
    }

    /**
     * Thiết lập skeleton adapters cho tất cả RecyclerViews
     */
    private void setupSkeletonAdapters() {
        // Skeleton cho Categories (4 items)
        categorySkeletonAdapter = new CategorySkeletonAdapter(requireContext(), 4);
        recyclerViewCategories.setAdapter(categorySkeletonAdapter);

        // Skeleton cho Recommended Tips (3 items)
        recommendedTipsSkeletonAdapter = new HealthTipSkeletonAdapter(requireContext(), 3);
        recyclerViewRecommendedTips.setAdapter(recommendedTipsSkeletonAdapter);

        // Skeleton cho Latest Tips (3 items)
        latestTipsSkeletonAdapter = new HealthTipSkeletonAdapter(requireContext(), 3);
        recyclerViewLatestTips.setAdapter(latestTipsSkeletonAdapter);

        // Skeleton cho Most Viewed Tips (3 items)
        mostViewedTipsSkeletonAdapter = new HealthTipSkeletonAdapter(requireContext(), 3);
        recyclerViewMostViewedTips.setAdapter(mostViewedTipsSkeletonAdapter);

        // Skeleton cho Most Liked Tips (3 items)
        mostLikedTipsSkeletonAdapter = new HealthTipSkeletonAdapter(requireContext(), 3);
        recyclerViewMostLikedTips.setAdapter(mostLikedTipsSkeletonAdapter);
    }

    /**
     * DEPRECATED: Không còn sử dụng phương thức này
     * Thay thế bằng setupSkeletonLoading()
     */
    @Deprecated
    private void hideContentViews() {
        // Phương thức này không còn được sử dụng
        // Skeleton loading sẽ hiển thị ngay từ đầu
    }

    /**
     * DEPRECATED: Không còn sử dụng animation cascade
     * Data sẽ được thay thế trực tiếp từ skeleton sang real data
     */
    @Deprecated
    private void showContentViewsWithAnimation() {
        // Phương thức này không còn được sử dụng
        // Data được cập nhật trực tiếp qua các phương thức show*Data()
    }

    /**
     * Ánh xạ các thành phần UI từ layout
     */
    private void initViews(View view) {
        // RecyclerViews
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        recyclerViewRecommendedTips = view.findViewById(R.id.recyclerViewRecommendedTips);
        recyclerViewLatestTips = view.findViewById(R.id.recyclerViewLatestTips);
        recyclerViewMostViewedTips = view.findViewById(R.id.recyclerViewMostViewedTips);
        recyclerViewMostLikedTips = view.findViewById(R.id.recyclerViewMostLikedTips);

        // Các thành phần UI khác
        progressBar = view.findViewById(R.id.progressBar);
        layoutOfflineMode = view.findViewById(R.id.layoutOfflineMode);
        buttonSearch = view.findViewById(R.id.buttonSearch);

        // Buttons "Xem tất cả"
        textViewSeeAllCategories = view.findViewById(R.id.textViewSeeAllCategories);
        textViewSeeAllRecommended = view.findViewById(R.id.textViewSeeAllRecommended);
        textViewSeeAllLatestTips = view.findViewById(R.id.textViewSeeAllLatestTips);
        textViewSeeAllMostViewed = view.findViewById(R.id.textViewSeeAllMostViewed);
        textViewSeeAllMostLiked = view.findViewById(R.id.textViewSeeAllMostLiked);

        // Thêm các tiêu đề sections
        initSectionTitles(view);
    }

    // UI components cho tiêu đề sections
    private TextView textViewCategoriesTitle;
    private TextView textViewRecommendedTipsTitle;
    private TextView textViewLatestTipsTitle;
    private TextView textViewMostViewedTitle;
    private TextView textViewMostLikedTitle;

    /**
     * Khởi tạo các tiêu đề sections
     */
    private void initSectionTitles(View view) {
        // Tìm các TextView tiêu đề trong layout
        textViewCategoriesTitle = view.findViewById(R.id.textViewCategoriesTitle);
        textViewRecommendedTipsTitle = view.findViewById(R.id.textViewRecommendedTipsTitle);
        textViewLatestTipsTitle = view.findViewById(R.id.textViewLatestTipsTitle);
        textViewMostViewedTitle = view.findViewById(R.id.textViewMostViewedTitle);
        textViewMostLikedTitle = view.findViewById(R.id.textViewMostLikedTitle);
    }

    /**
     * Thiết lập RecyclerViews với adapters và LayoutManagers
     */
    private void setupRecyclerViews() {
        // Setup Layout Managers TRƯỚC KHI thiết lập skeleton
        setupLayoutManagers();

        // Khởi tạo real adapters (nhưng chưa set vào RecyclerViews)
        initializeRealAdapters();
    }

    /**
     * Thiết lập Layout Managers cho tất cả RecyclerViews
     */
    private void setupLayoutManagers() {
        // Setup Layout Manager cho Categories
        LinearLayoutManager categoriesLayoutManager = new LinearLayoutManager(
                requireContext(), RecyclerView.HORIZONTAL, false);
        recyclerViewCategories.setLayoutManager(categoriesLayoutManager);

        // Setup Layout Manager cho Recommended Tips
        LinearLayoutManager recommendedTipsLayoutManager = new LinearLayoutManager(
                requireContext(), RecyclerView.HORIZONTAL, false);
        recyclerViewRecommendedTips.setLayoutManager(recommendedTipsLayoutManager);

        // Setup Layout Manager cho Latest Tips
        LinearLayoutManager latestTipsLayoutManager = new LinearLayoutManager(
                requireContext(), RecyclerView.HORIZONTAL, false);
        recyclerViewLatestTips.setLayoutManager(latestTipsLayoutManager);

        // Setup Layout Manager cho Most Viewed Tips
        LinearLayoutManager mostViewedLayoutManager = new LinearLayoutManager(
                requireContext(), RecyclerView.HORIZONTAL, false);
        recyclerViewMostViewedTips.setLayoutManager(mostViewedLayoutManager);

        // Setup Layout Manager cho Most Liked Tips
        LinearLayoutManager mostLikedLayoutManager = new LinearLayoutManager(
                requireContext(), RecyclerView.HORIZONTAL, false);
        recyclerViewMostLikedTips.setLayoutManager(mostLikedLayoutManager);
    }

    /**
     * Khởi tạo các real adapters (nhưng chưa set vào RecyclerViews)
     */
    private void initializeRealAdapters() {
        // Khởi tạo Category Adapter
        categoryAdapter = new CategoryAdapter(
                requireContext(),
                new ArrayList<>(),
                new CategoryAdapter.CategoryClickListener() {
                    @Override
                    public void onCategoryClick(Category category) {
                        presenter.onCategorySelected(category);
                    }
                });

        // Khởi tạo Recommended Tips Adapter
        recommendedTipsAdapter = new InfiniteHealthTipAdapter(
                requireContext(),
                new ArrayList<>(),
                new InfiniteHealthTipAdapter.HealthTipClickListener() {
                    @Override
                    public void onHealthTipClick(HealthTip healthTip) {
                        presenter.onHealthTipSelected(healthTip);
                    }

                    @Override
                    public void onFavoriteClick(HealthTip healthTip, boolean isFavorite) {
                        handleFavoriteClick(healthTip, isFavorite);
                    }
                });

        // Khởi tạo Latest Tips Adapter
        latestTipsAdapter = new HealthTipAdapter(
                requireContext(),
                new ArrayList<>(),
                new HealthTipAdapter.HealthTipClickListener() {
                    @Override
                    public void onHealthTipClick(HealthTip healthTip) {
                        presenter.onHealthTipSelected(healthTip);
                    }

                    @Override
                    public void onFavoriteClick(HealthTip healthTip, boolean isFavorite) {
                        handleFavoriteClick(healthTip, isFavorite);
                    }
                });

        // Khởi tạo Most Viewed Tips Adapter
        mostViewedTipsAdapter = new HealthTipAdapter(
                requireContext(),
                new ArrayList<>(),
                new HealthTipAdapter.HealthTipClickListener() {
                    @Override
                    public void onHealthTipClick(HealthTip healthTip) {
                        presenter.onHealthTipSelected(healthTip);
                    }

                    @Override
                    public void onFavoriteClick(HealthTip healthTip, boolean isFavorite) {
                        handleFavoriteClick(healthTip, isFavorite);
                    }
                });

        // Khởi tạo Most Liked Tips Adapter
        mostLikedTipsAdapter = new HealthTipAdapter(
                requireContext(),
                new ArrayList<>(),
                new HealthTipAdapter.HealthTipClickListener() {
                    @Override
                    public void onHealthTipClick(HealthTip healthTip) {
                        presenter.onHealthTipSelected(healthTip);
                    }

                    @Override
                    public void onFavoriteClick(HealthTip healthTip, boolean isFavorite) {
                        handleFavoriteClick(healthTip, isFavorite);
                    }
                });
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
        textViewSeeAllCategories.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, CategoryFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
        });

        // Xem tất cả mẹo được đề xuất
        textViewSeeAllRecommended.setOnClickListener(v -> {
            startActivity(AllHealthTipsActivity.createIntent(requireContext(), AllHealthTipsActivity.MODE_RECOMMENDED));
        });

        // Xem tất cả mẹo mới nhất
        textViewSeeAllLatestTips.setOnClickListener(v -> {
            startActivity(AllHealthTipsActivity.createIntent(requireContext(), AllHealthTipsActivity.MODE_LATEST));
        });

        // Xem tất cả mẹo xem nhiều nhất
        textViewSeeAllMostViewed.setOnClickListener(v -> {
            startActivity(AllHealthTipsActivity.createIntent(requireContext(), AllHealthTipsActivity.MODE_MOST_VIEWED));
        });

        // Xem tất cả mẹo được yêu thích nhất
        textViewSeeAllMostLiked.setOnClickListener(v -> {
            startActivity(AllHealthTipsActivity.createIntent(requireContext(), AllHealthTipsActivity.MODE_MOST_LIKED));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start(); // Tải dữ liệu khi Fragment được hiển thị

        // Lắng nghe thay đổi realtime từ Firebase
        presenter.listenToCategories();
        presenter.listenToLatestHealthTips();

        // Tiếp tục auto-scroll nếu đã có dữ liệu
        if (isRecommendedTipsLoaded && recommendedTipsAdapter != null &&
            recommendedTipsAdapter.getItemCount() > 0 && !isAutoScrolling) {
            startAutoScrollForRecommended();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stop(); // Dừng lắng nghe khi Fragment không được hiển thị

        // Tạm dừng auto-scroll khi Fragment không visible
        stopAutoScrollForRecommended();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detachView(); // Tách View khỏi Presenter

        // Dọn dẹp auto-scroll resources
        stopAutoScrollForRecommended();
        if (autoScrollHandler != null) {
            autoScrollHandler.removeCallbacksAndMessages(null);
            autoScrollHandler = null;
        }
        autoScrollRunnable = null;
    }

    // Triển khai các phương thức của HomeView với skeleton loading
    @Override
    public void showCategories(List<Category> categories) {
        // Thay thế skeleton adapter bằng real adapter với data
        if (!isCategoriesLoaded) {
            recyclerViewCategories.setAdapter(categoryAdapter);
            isCategoriesLoaded = true;
        }
        categoryAdapter.updateCategories(categories);
    }

    @Override
    public void showRecommendedHealthTips(List<HealthTip> healthTips) {
        // Thay thế skeleton adapter bằng real adapter với data
        if (!isRecommendedTipsLoaded) {
            recyclerViewRecommendedTips.setAdapter(recommendedTipsAdapter);
            isRecommendedTipsLoaded = true;

            // Đặt vị trí bắt đầu ở giữa để tạo hiệu ứng vòng tròn
            recyclerViewRecommendedTips.post(() -> {
                int startPosition = recommendedTipsAdapter.getStartPosition();
                recyclerViewRecommendedTips.scrollToPosition(startPosition);
                currentRecommendedPosition = startPosition;
            });
        }
        recommendedTipsAdapter.updateHealthTips(healthTips);

        // Bắt đầu auto-scroll animation khi có dữ liệu
        startAutoScrollForRecommended();
    }

    @Override
    public void showLatestHealthTips(List<HealthTip> healthTips) {
        // Thay thế skeleton adapter bằng real adapter với data
        if (!isLatestTipsLoaded) {
            recyclerViewLatestTips.setAdapter(latestTipsAdapter);
            isLatestTipsLoaded = true;
        }
        latestTipsAdapter.updateHealthTips(healthTips);
    }

    @Override
    public void showMostViewedHealthTips(List<HealthTip> healthTips) {
        // Thay thế skeleton adapter bằng real adapter với data
        if (!isMostViewedTipsLoaded) {
            recyclerViewMostViewedTips.setAdapter(mostViewedTipsAdapter);
            isMostViewedTipsLoaded = true;
        }
        mostViewedTipsAdapter.updateHealthTips(healthTips);
    }

    @Override
    public void showMostLikedHealthTips(List<HealthTip> healthTips) {
        // Thay thế skeleton adapter bằng real adapter với data
        if (!isMostLikedTipsLoaded) {
            recyclerViewMostLikedTips.setAdapter(mostLikedTipsAdapter);
            isMostLikedTipsLoaded = true;
        }
        mostLikedTipsAdapter.updateHealthTips(healthTips);
    }

    /**
     * DEPRECATED: Không còn sử dụng animation cascade
     * Data được thay thế trực tiếp từ skeleton sang real data
     */
    @Deprecated
    private void checkAndShowContentAnimation() {
        // Không còn cần thiết với skeleton loading
    }

    /**
     * DEPRECATED: Các phương thức kiểm tra data không còn cần thiết
     */
    @Deprecated
    private boolean hasCategories() { return false; }
    @Deprecated
    private boolean hasRecommendedTips() { return false; }
    @Deprecated
    private boolean hasLatestTips() { return false; }
    @Deprecated
    private boolean hasMostViewedTips() { return false; }
    @Deprecated
    private boolean hasMostLikedTips() { return false; }

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
        // Với skeleton loading, không cần hiển thị progress bar chính
        // Skeleton đã thay thế loading indicator
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
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

    /**
     * Xử lý khi người dùng click vào nút yêu thích
     * Đồng bộ trạng thái yêu thích giữa tất cả các adapter
     */
    private void handleFavoriteClick(HealthTip healthTip, boolean isFavorite) {
        if (healthTip == null || healthTip.getId() == null) {
            showError("Không thể cập nhật trạng thái yêu thích");
            return;
        }

        // Hiển thị thông báo cho người dùng
        if (isFavorite) {
            showMessage("Đã thêm '" + healthTip.getTitle() + "' vào danh sách yêu thích");
        } else {
            showMessage("Đã xóa '" + healthTip.getTitle() + "' khỏi danh sách yêu thích");
        }

        // Đồng bộ trạng thái yêu thích giữa tất cả các adapter
        syncFavoriteStatusAcrossAdapters(healthTip.getId(), isFavorite);
    }

    /**
     * Đồng bộ trạng thái yêu thích của một health tip trên tất cả các adapter
     * @param healthTipId ID của health tip cần đồng bộ
     * @param isFavorite Trạng thái yêu thích mới
     */
    private void syncFavoriteStatusAcrossAdapters(String healthTipId, boolean isFavorite) {
        // Đồng bộ cho adapter Recommended Tips
        if (recommendedTipsAdapter != null) {
            recommendedTipsAdapter.updateFavoriteStatus(healthTipId, isFavorite);
        }

        // Đồng bộ cho adapter Latest Tips
        if (latestTipsAdapter != null) {
            latestTipsAdapter.updateFavoriteStatus(healthTipId, isFavorite);
        }

        // Đồng bộ cho adapter Most Viewed Tips
        if (mostViewedTipsAdapter != null) {
            mostViewedTipsAdapter.updateFavoriteStatus(healthTipId, isFavorite);
        }

        // Đồng bộ cho adapter Most Liked Tips
        if (mostLikedTipsAdapter != null) {
            mostLikedTipsAdapter.updateFavoriteStatus(healthTipId, isFavorite);
        }
    }

    /**
     * Navigation đến màn hình profile (nơi chứa chức năng yêu thích)
     * Được gọi khi người dùng muốn xem toàn bộ danh sách yêu thích
     */
    public void navigateToFavorites() {
        // Chuyển tab bottom navigation đến profile (nơi chứa chức năng yêu thích)
        if (getActivity() != null) {
            // Trigger bottom navigation để chuyển đến ProfileFragment
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_profile);
            }
        }
    }

    /**
     * Bắt đầu animation tự động trượt cho phần đề xuất
     */
    private void startAutoScrollForRecommended() {
        if (isAutoScrolling) {
            return; // Đã đang chạy auto-scroll
        }

        // Khởi tạo Handler nếu chưa có
        if (autoScrollHandler == null) {
            autoScrollHandler = new Handler(Looper.getMainLooper());
        }

        // Tạo Runnable cho auto-scroll
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (recommendedTipsAdapter != null &&
                    recommendedTipsAdapter.getItemCount() > 0 &&
                    recyclerViewRecommendedTips != null &&
                    isAdded()) {

                    // Tính toán vị trí tiếp theo
                    int itemCount = recommendedTipsAdapter.getItemCount();
                    currentRecommendedPosition = (currentRecommendedPosition + 1) % itemCount;

                    // Tạo smooth scroller với tốc độ vừa phải
                    RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(requireContext()) {
                        @Override
                        protected int getHorizontalSnapPreference() {
                            return LinearSmoothScroller.SNAP_TO_START;
                        }

                        @Override
                        protected float calculateSpeedPerPixel(android.util.DisplayMetrics displayMetrics) {
                            // Tốc độ vừa phải: 150ms per inch (mặc định là 25ms)
                            return 150f / displayMetrics.densityDpi;
                        }
                    };

                    // Scroll đến vị trí tiếp theo
                    smoothScroller.setTargetPosition(currentRecommendedPosition);
                    RecyclerView.LayoutManager layoutManager = recyclerViewRecommendedTips.getLayoutManager();
                    if (layoutManager != null) {
                        layoutManager.startSmoothScroll(smoothScroller);
                    }

                    // Lên lịch cho lần scroll tiếp theo
                    if (isAutoScrolling && autoScrollHandler != null) {
                        autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY);
                    }
                }
            }
        };

        // Bắt đầu auto-scroll
        isAutoScrolling = true;
        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
    }

    /**
     * Dừng animation tự động trượt
     */
    private void stopAutoScrollForRecommended() {
        isAutoScrolling = false;
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    /**
     * Thiết lập touch listener để tạm dừng auto-scroll khi người dùng tương tác
     */
    private void setupRecommendedTouchListener() {
        if (recyclerViewRecommendedTips != null) {
            recyclerViewRecommendedTips.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
                @Override
                public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull android.view.MotionEvent e) {
                    // Tạm dừng auto-scroll khi người dùng chạm vào
                    if (e.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                        stopAutoScrollForRecommended();

                        // Tiếp tục auto-scroll sau 6 giây không tương tác
                        if (autoScrollHandler != null) {
                            autoScrollHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!isAutoScrolling && isAdded()) {
                                        startAutoScrollForRecommended();
                                    }
                                }
                            }, 6000); // 6 giây
                        }
                    }
                    return false;
                }
            });
        }
    }
}
