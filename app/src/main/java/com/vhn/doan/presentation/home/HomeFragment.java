package com.vhn.doan.presentation.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.vhn.doan.presentation.base.FragmentVisibilityListener;
import com.vhn.doan.presentation.search.SearchActivity;
import com.vhn.doan.presentation.healthtip.detail.HealthTipDetailActivity;
import com.vhn.doan.utils.Constants;
import com.vhn.doan.presentation.home.adapter.CategoryAdapter;
import com.vhn.doan.presentation.home.adapter.InfiniteHealthTipAdapter;
import com.vhn.doan.presentation.home.adapter.HealthTipAdapter;
import com.vhn.doan.presentation.home.adapter.CategorySkeletonAdapter;
import com.vhn.doan.presentation.home.adapter.HealthTipSkeletonAdapter;
import com.vhn.doan.utils.NetworkMonitor;
import com.vhn.doan.data.repository.NotificationHistoryRepositoryImpl;
import com.vhn.doan.services.CustomAnalyticsService;
import com.vhn.doan.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeFragment hiển thị trang chính của ứng dụng
 * Tuân thủ kiến trúc MVP và sử dụng HomePresenter để xử lý logic
 * Implement FragmentVisibilityListener để quản lý lifecycle
 */
public class HomeFragment extends Fragment implements HomeView, FragmentVisibilityListener {

    private static final String TAG = "HomeFragment";

    // UI components
    private RecyclerView recyclerViewCategories;
    // private RecyclerView recyclerViewRecommendedTips; // Removed - replaced by Featured Card
    private RecyclerView recyclerViewLatestTips;
    private RecyclerView recyclerViewMostViewedTips;
    private RecyclerView recyclerViewMostLikedTips;
    private ProgressBar progressBar;
    private LinearLayout layoutOfflineMode;
    private ImageButton buttonSearch;
    private TextView textViewSeeAllCategories;
    // private TextView textViewSeeAllRecommended; // Removed
    private TextView textViewSeeAllLatestTips;
    private TextView textViewSeeAllMostViewed;
    private TextView textViewSeeAllMostLiked;

    // Adapters - Real data
    private CategoryAdapter categoryAdapter;
    private InfiniteHealthTipAdapter featuredTipsAdapter; // Featured tips carousel với auto-scroll
    private HealthTipAdapter latestTipsAdapter;
    private HealthTipAdapter mostViewedTipsAdapter;
    private HealthTipAdapter mostLikedTipsAdapter;

    // Skeleton Adapters
    private CategorySkeletonAdapter categorySkeletonAdapter;
    private HealthTipSkeletonAdapter featuredTipsSkeletonAdapter; // Featured tips skeleton
    private HealthTipSkeletonAdapter latestTipsSkeletonAdapter;
    private HealthTipSkeletonAdapter mostViewedTipsSkeletonAdapter;
    private HealthTipSkeletonAdapter mostLikedTipsSkeletonAdapter;

    // Presenter
    private HomePresenter presenter;

    // Network Monitor để theo dõi trạng thái mạng
    private NetworkMonitor networkMonitor;
    private boolean wasOffline = false; // Flag để theo dõi trạng thái offline trước đó

    // Loading state flags
    private boolean isCategoriesLoaded = false;
    private boolean isFeaturedTipsLoaded = false; // Featured tips carousel
    private boolean isLatestTipsLoaded = false;
    private boolean isMostViewedTipsLoaded = false;
    private boolean isMostLikedTipsLoaded = false;

    // Auto-scroll animation cho featured tips carousel
    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;
    private int currentFeaturedPosition = 0;
    private boolean isAutoScrolling = false;
    private static final long AUTO_SCROLL_DELAY = 7000; // 7 giây - tăng thời gian đợi
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
        HealthTipRepository healthTipRepository = new HealthTipRepositoryImpl(requireContext());

        // Khởi tạo presenter
        presenter = new HomePresenter(requireContext(), categoryRepository, healthTipRepository);

        // Khởi tạo NetworkMonitor để theo dõi trạng thái mạng
        networkMonitor = NetworkMonitor.getInstance(requireContext());
        networkMonitor.startMonitoring();

        // Observe network status changes
        setupNetworkObserver();
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

        // Thiết lập touch listener cho auto-scroll animation - REMOVED
        // setupRecommendedTouchListener();

        // Gắn presenter với view và bắt đầu tải dữ liệu
        presenter.attachView(this);

        // Load số lượng thông báo chưa đọc
        loadUnreadNotificationCount();
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
     * Hiển thị tất cả nút "Xem tất cả"
     * (Section titles không còn cần thiết vì đã được tích hợp trong layout headers)
     */
    private void showAllTitlesAndSeeAll() {
        if (textViewSeeAllCategories != null) textViewSeeAllCategories.setVisibility(View.VISIBLE);
        // if (textViewSeeAllRecommended != null) textViewSeeAllRecommended.setVisibility(View.VISIBLE); // Removed
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

        // Skeleton cho Featured Tips (3 items) - Carousel
        featuredTipsSkeletonAdapter = new HealthTipSkeletonAdapter(requireContext(), 3);
        recyclerViewFeaturedTips.setAdapter(featuredTipsSkeletonAdapter);

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

    // UI components cho tính năng mới
    private TextView textViewGreeting;
    private View searchBarCard;
    private ImageButton buttonNotification;
    private TextView badgeNotificationCount; // Badge cho số lượng thông báo chưa đọc
    private RecyclerView recyclerViewFeaturedTips; // Featured tips carousel

    /**
     * Ánh xạ các thành phần UI từ layout
     */
    private void initViews(View view) {
        // RecyclerViews
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        recyclerViewFeaturedTips = view.findViewById(R.id.recyclerViewFeaturedTips); // Featured carousel
        recyclerViewLatestTips = view.findViewById(R.id.recyclerViewLatestTips);
        recyclerViewMostViewedTips = view.findViewById(R.id.recyclerViewMostViewedTips);
        recyclerViewMostLikedTips = view.findViewById(R.id.recyclerViewMostLikedTips);

        // Các thành phần UI khác
        progressBar = view.findViewById(R.id.progressBar);
        layoutOfflineMode = view.findViewById(R.id.layoutOfflineMode);
        buttonSearch = view.findViewById(R.id.buttonSearch);

        // Các thành phần UI mới
        textViewGreeting = view.findViewById(R.id.textViewGreeting);
        searchBarCard = view.findViewById(R.id.searchBarCard);
        buttonNotification = view.findViewById(R.id.buttonNotification);
        badgeNotificationCount = view.findViewById(R.id.badgeNotificationCount);

        // Buttons "Xem tất cả"
        textViewSeeAllCategories = view.findViewById(R.id.textViewSeeAllCategories);
        textViewSeeAllLatestTips = view.findViewById(R.id.textViewSeeAllLatestTips);
        textViewSeeAllMostViewed = view.findViewById(R.id.textViewSeeAllMostViewed);
        textViewSeeAllMostLiked = view.findViewById(R.id.textViewSeeAllMostLiked);

        // Setup greeting text theo thời gian
        setupGreeting();
    }

    /**
     * Setup greeting text dựa trên thời gian trong ngày
     * Lấy tên người dùng từ Firebase Realtime Database
     */
    private void setupGreeting() {
        if (textViewGreeting == null) return;

        // Hiển thị greeting mặc định trước
        updateGreetingText("Bạn");

        // Lấy user info từ Firebase Realtime Database
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            com.vhn.doan.data.repository.UserRepository userRepository = new com.vhn.doan.data.repository.UserRepositoryImpl();
            userRepository.getUserByUid(currentUser.getUid(), new com.vhn.doan.data.repository.UserRepository.UserCallback() {
                @Override
                public void onSuccess(com.vhn.doan.data.User user) {
                    if (!isAdded() || textViewGreeting == null) return;

                    String userName = "Bạn"; // Default
                    if (user != null) {
                        String displayName = user.getDisplayName();
                        if (displayName != null && !displayName.isEmpty()) {
                            userName = displayName;
                        } else {
                            // Fallback to email
                            String email = user.getEmail();
                            if (email != null && !email.isEmpty()) {
                                userName = email.split("@")[0];
                            }
                        }
                    }
                    updateGreetingText(userName);
                }

                @Override
                public void onError(String errorMessage) {
                    // Giữ nguyên greeting mặc định nếu có lỗi
                    Log.w(TAG, "Cannot load user for greeting: " + errorMessage);
                }
            });
        }
    }

    /**
     * Update greeting text dựa trên thời gian và tên user
     */
    private void updateGreetingText(String userName) {
        if (textViewGreeting == null) return;

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int hourOfDay = calendar.get(java.util.Calendar.HOUR_OF_DAY);

        String greetingText;
        if (hourOfDay >= 5 && hourOfDay < 12) {
            greetingText = "Chào buổi sáng, " + userName + "!";
        } else if (hourOfDay >= 12 && hourOfDay < 18) {
            greetingText = "Chào buổi chiều, " + userName + "!";
        } else if (hourOfDay >= 18 && hourOfDay < 22) {
            greetingText = "Chào buổi tối, " + userName + "!";
        } else {
            greetingText = "Chào bạn, " + userName + "!";
        }

        textViewGreeting.setText(greetingText);
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

        // Setup Layout Manager cho Featured Tips (Carousel)
        LinearLayoutManager featuredTipsLayoutManager = new LinearLayoutManager(
                requireContext(), RecyclerView.HORIZONTAL, false);
        recyclerViewFeaturedTips.setLayoutManager(featuredTipsLayoutManager);

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

        // Khởi tạo Featured Tips Adapter - Carousel với auto-scroll
        featuredTipsAdapter = new InfiniteHealthTipAdapter(
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
                },
                R.layout.item_featured_tip); // Sử dụng featured tip layout

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
        // Nút tìm kiếm header
        if (buttonSearch != null) {
            buttonSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Chuyển đến SearchActivity
                    Intent intent = new Intent(requireContext(), SearchActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Thanh tìm kiếm pill-shaped
        if (searchBarCard != null) {
            searchBarCard.setOnClickListener(v -> {
                // Chuyển đến SearchActivity
                Intent intent = new Intent(requireContext(), SearchActivity.class);
                startActivity(intent);
            });
        }

        // Nút thông báo
        if (buttonNotification != null) {
            buttonNotification.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), com.vhn.doan.presentation.notification.NotificationHistoryActivity.class);
                startActivity(intent);
            });
        }

        // Featured Tips carousel - touch listener để pause/resume auto-scroll
        setupFeaturedTouchListener();

        // Xem tất cả danh mục - mở Activity mới thay vì Fragment
        if (textViewSeeAllCategories != null) {
            textViewSeeAllCategories.setOnClickListener(v -> {
                if (getActivity() == null || !isAdded()) return;
                startActivity(com.vhn.doan.presentation.category.AllCategoriesActivity.createIntent(requireContext()));
            });
        }

        // Xem tất cả mẹo được đề xuất - REMOVED
        // if (textViewSeeAllRecommended != null) {
        //     textViewSeeAllRecommended.setOnClickListener(v -> {
        //         startActivity(AllHealthTipsActivity.createIntent(requireContext(), AllHealthTipsActivity.MODE_RECOMMENDED));
        //     });
        // }

        // Xem tất cả mẹo mới nhất
        if (textViewSeeAllLatestTips != null) {
            textViewSeeAllLatestTips.setOnClickListener(v -> {
                startActivity(AllHealthTipsActivity.createIntent(requireContext(), AllHealthTipsActivity.MODE_LATEST));
            });
        }

        // Xem tất cả mẹo xem nhiều nhất
        if (textViewSeeAllMostViewed != null) {
            textViewSeeAllMostViewed.setOnClickListener(v -> {
                startActivity(AllHealthTipsActivity.createIntent(requireContext(), AllHealthTipsActivity.MODE_MOST_VIEWED));
            });
        }

        // Xem tất cả mẹo được yêu thích nhất
        if (textViewSeeAllMostLiked != null) {
            textViewSeeAllMostLiked.setOnClickListener(v -> {
                startActivity(AllHealthTipsActivity.createIntent(requireContext(), AllHealthTipsActivity.MODE_MOST_LIKED));
            });
        }
    }

    // Featured Health Tip để hiển thị trong Featured Card
    private HealthTip featuredHealthTip;

    @Override
    public void onResume() {
        super.onResume();

        // Track page view
        if (getContext() != null) {
            CustomAnalyticsService.getInstance(getContext()).trackPageView("home");
        }

        // ✅ Reload greeting để cập nhật tên user nếu đã đổi
        setupGreeting();

        // Force reload nếu categories chưa được load (xảy ra khi recreate do theme change)
        if (!isCategoriesLoaded) {
            // KHÔNG gọi setupSkeletonLoading() ở đây vì đã được gọi trong onCreateView()
            // Chỉ cần start presenter để load data
            presenter.start(); // Tải dữ liệu khi Fragment được hiển thị
        }

        // Lắng nghe thay đổi realtime từ Firebase
        presenter.listenToCategories();
        presenter.listenToLatestHealthTips();

        // Tiếp tục auto-scroll cho Featured Tips nếu đã có dữ liệu
        if (isFeaturedTipsLoaded && featuredTipsAdapter != null &&
            featuredTipsAdapter.getItemCount() > 0 && !isAutoScrolling) {
            startAutoScrollForFeatured();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            // ✅ Reload greeting khi fragment được show lại
            setupGreeting();

            // Fragment được hiển thị lại (khi back từ CategoryFragment)
            // Đảm bảo RecyclerView categories visible
            if (recyclerViewCategories != null) {
                recyclerViewCategories.setVisibility(View.VISIBLE);
                // Force refresh adapter để hiển thị lại items
                if (categoryAdapter != null) {
                    categoryAdapter.notifyDataSetChanged();
                }
            }

            // Refresh dữ liệu để đảm bảo cập nhật mới nhất
            if (presenter != null) {
                presenter.start();
                presenter.listenToCategories();
                presenter.listenToLatestHealthTips();
            }

            // Tiếp tục auto-scroll cho Featured Tips nếu có
            if (isFeaturedTipsLoaded && featuredTipsAdapter != null &&
                featuredTipsAdapter.getItemCount() > 0 && !isAutoScrolling) {
                startAutoScrollForFeatured();
            }
        } else {
            // Fragment bị ẩn - dừng auto-scroll
            stopAutoScrollForFeatured();

            // Dừng các listener để tránh memory leak
            if (presenter != null) {
                presenter.stop();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stop(); // Dừng lắng nghe khi Fragment không được hiển thị

        // Tạm dừng auto-scroll cho Featured Tips
        stopAutoScrollForFeatured();

        // Reset loading flags để force reload khi resume (fix theme change issue)
        isCategoriesLoaded = false;
        isFeaturedTipsLoaded = false; // Featured carousel
        isLatestTipsLoaded = false;
        isMostViewedTipsLoaded = false;
        isMostLikedTipsLoaded = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detachView(); // Tách View khỏi Presenter

        // Dọn dẹp auto-scroll resources
        stopAutoScrollForFeatured();
        if (autoScrollHandler != null) {
            autoScrollHandler.removeCallbacksAndMessages(null);
            autoScrollHandler = null;
        }
        autoScrollRunnable = null;
    }

    // Triển khai các phương thức của HomeView với skeleton loading
    @Override
    public void showCategories(List<Category> categories) {
        // Kiểm tra null để tránh crash khi recreate
        if (categoryAdapter == null || recyclerViewCategories == null) {
            Log.w(TAG, "showCategories: adapter or recyclerView is null, skipping");
            return;
        }

        Log.d(TAG, "showCategories called with " + (categories != null ? categories.size() : 0) + " categories");
        Log.d(TAG, "Current adapter: " + recyclerViewCategories.getAdapter());
        Log.d(TAG, "isCategoriesLoaded: " + isCategoriesLoaded);

        // Thay thế skeleton adapter bằng real adapter với data
        if (!isCategoriesLoaded) {
            Log.d(TAG, "First time loading - setting real adapter");
            // Set adapter NGAY LẬP TỨC (không dùng post)
            recyclerViewCategories.setAdapter(categoryAdapter);
            isCategoriesLoaded = true;
        }

        // Luôn update data (dù lần đầu hay lần sau)
        Log.d(TAG, "Updating categories data");
        categoryAdapter.updateCategories(categories);
    }

    @Override
    public void showRecommendedHealthTips(List<HealthTip> healthTips) {
        // Kiểm tra null để tránh crash
        if (featuredTipsAdapter == null || recyclerViewFeaturedTips == null) {
            Log.w(TAG, "showRecommendedHealthTips: adapter or recyclerView is null, skipping");
            return;
        }

        Log.d(TAG, "showRecommendedHealthTips called with " + (healthTips != null ? healthTips.size() : 0) + " tips");
        Log.d(TAG, "isFeaturedTipsLoaded: " + isFeaturedTipsLoaded);

        // Setup Featured Tips Carousel với auto-scroll
        if (!isFeaturedTipsLoaded) {
            Log.d(TAG, "First time loading - setting featured tips adapter");
            // Set adapter NGAY LẬP TỨC
            recyclerViewFeaturedTips.setAdapter(featuredTipsAdapter);
            isFeaturedTipsLoaded = true;

            // Post scroll position và auto-scroll (cần đợi layout)
            recyclerViewFeaturedTips.post(() -> {
                if (recyclerViewFeaturedTips != null && featuredTipsAdapter != null) {
                    int startPosition = featuredTipsAdapter.getStartPosition();
                    recyclerViewFeaturedTips.scrollToPosition(startPosition);
                    currentFeaturedPosition = startPosition;
                    startAutoScrollForFeatured();
                }
            });
        }

        // Luôn update data
        Log.d(TAG, "Updating featured tips data");
        featuredTipsAdapter.updateHealthTips(healthTips);
    }

    @Override
    public void showLatestHealthTips(List<HealthTip> healthTips) {
        Log.d(TAG, "showLatestHealthTips called with " + (healthTips != null ? healthTips.size() : 0) + " items");
        // Thay thế skeleton adapter bằng real adapter với data
        if (!isLatestTipsLoaded) {
            Log.d(TAG, "Setting latest tips adapter for first time");
            recyclerViewLatestTips.setAdapter(latestTipsAdapter);
            isLatestTipsLoaded = true;
        }
        latestTipsAdapter.updateHealthTips(healthTips);
        Log.d(TAG, "Latest tips adapter updated");
    }

    @Override
    public void showMostViewedHealthTips(List<HealthTip> healthTips) {
        Log.d(TAG, "showMostViewedHealthTips called with " + (healthTips != null ? healthTips.size() : 0) + " items");
        // Thay thế skeleton adapter bằng real adapter với data
        if (!isMostViewedTipsLoaded) {
            Log.d(TAG, "Setting most viewed tips adapter for first time");
            recyclerViewMostViewedTips.setAdapter(mostViewedTipsAdapter);
            isMostViewedTipsLoaded = true;
        }
        mostViewedTipsAdapter.updateHealthTips(healthTips);
        Log.d(TAG, "Most viewed tips adapter updated");
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
     * Thiết lập observer cho network status
     */
    private void setupNetworkObserver() {
        if (networkMonitor != null) {
            networkMonitor.getConnectionStatus().observe(this, isConnected -> {
                if (isConnected != null) {
                    Log.d(TAG, "🌐 Network status changed: " + (isConnected ? "ONLINE" : "OFFLINE"));

                    if (isConnected) {
                        // Có mạng trở lại
                        hideOfflineMode();

                        // Nếu trước đó đang offline, hiển thị thông báo đã có mạng
                        if (wasOffline) {
                            showMessage("✅ Đã kết nối lại mạng");
                            wasOffline = false;

                            // Reload dữ liệu để sync với server
                            if (presenter != null) {
                                presenter.start();
                            }
                        }
                    } else {
                        // Mất mạng
                        showOfflineMode();
                        wasOffline = true;
                        showMessage("⚠️ Đang ở chế độ ngoại tuyến");
                    }
                }
            });
        }
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
        if (layoutOfflineMode != null) {
            layoutOfflineMode.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Ẩn thông báo offline mode
     */
    public void hideOfflineMode() {
        if (layoutOfflineMode != null) {
            layoutOfflineMode.setVisibility(View.GONE);
        }
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
        // Đồng bộ cho adapter Recommended Tips - REMOVED
        // if (recommendedTipsAdapter != null) {
        //     recommendedTipsAdapter.updateFavoriteStatus(healthTipId, isFavorite);
        // }

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

    // ============================================================
    // AUTO-SCROLL METHODS - Featured Tips Carousel
    // ============================================================

    /**
     * Bắt đầu animation tự động trượt cho Featured Tips carousel
     */
    private void startAutoScrollForFeatured() {
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
                if (featuredTipsAdapter != null &&
                    featuredTipsAdapter.getItemCount() > 0 &&
                    recyclerViewFeaturedTips != null &&
                    isAdded()) {

                    // Tính toán vị trí tiếp theo
                    int itemCount = featuredTipsAdapter.getItemCount();
                    currentFeaturedPosition = (currentFeaturedPosition + 1) % itemCount;

                    // Tạo smooth scroller
                    RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(requireContext()) {
                        @Override
                        protected int getHorizontalSnapPreference() {
                            return LinearSmoothScroller.SNAP_TO_START;
                        }

                        @Override
                        protected float calculateSpeedPerPixel(android.util.DisplayMetrics displayMetrics) {
                            return 150f / displayMetrics.densityDpi;
                        }
                    };

                    // Scroll đến vị trí tiếp theo
                    smoothScroller.setTargetPosition(currentFeaturedPosition);
                    RecyclerView.LayoutManager layoutManager = recyclerViewFeaturedTips.getLayoutManager();
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
    private void stopAutoScrollForFeatured() {
        isAutoScrolling = false;
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    /**
     * Load và hiển thị số lượng thông báo chưa đọc trên badge
     */
    private void loadUnreadNotificationCount() {
        try {
            SessionManager sessionManager = new SessionManager(requireContext());
            String userId = sessionManager.getCurrentUserId();

            if (userId == null || userId.isEmpty()) {
                // Không có user ID, ẩn badge
                if (badgeNotificationCount != null) {
                    badgeNotificationCount.setVisibility(View.GONE);
                }
                return;
            }

            NotificationHistoryRepositoryImpl repository =
                NotificationHistoryRepositoryImpl.getInstance(requireContext());

            repository.getUnreadCount(userId).observe(getViewLifecycleOwner(), count -> {
                if (badgeNotificationCount != null && count != null) {
                    if (count > 0) {
                        badgeNotificationCount.setVisibility(View.VISIBLE);
                        // Hiển thị số, tối đa 99+
                        if (count > 99) {
                            badgeNotificationCount.setText("99+");
                        } else {
                            badgeNotificationCount.setText(String.valueOf(count));
                        }
                    } else {
                        badgeNotificationCount.setVisibility(View.GONE);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error loading unread notification count", e);
            if (badgeNotificationCount != null) {
                badgeNotificationCount.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Thiết lập touch listener để tạm dừng auto-scroll khi người dùng tương tác
     */
    private void setupFeaturedTouchListener() {
        if (recyclerViewFeaturedTips != null) {
            recyclerViewFeaturedTips.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
                @Override
                public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull android.view.MotionEvent e) {
                    // Tạm dừng auto-scroll khi người dùng chạm vào
                    if (e.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                        stopAutoScrollForFeatured();

                        // Tiếp tục auto-scroll sau 6 giây không tương tác
                        if (autoScrollHandler != null) {
                            autoScrollHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!isAutoScrolling && isAdded()) {
                                        startAutoScrollForFeatured();
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

    // ============================================================
    // FragmentVisibilityListener Implementation
    // ============================================================

    /**
     * Được gọi khi fragment được hiển thị (visible to user)
     */
    @Override
    public void onFragmentVisible() {
        // HomeFragment luôn load dữ liệu ngay khi được tạo
        // Tiếp tục auto-scroll cho Featured Tips khi fragment visible
        if (isFeaturedTipsLoaded && featuredTipsAdapter != null &&
            featuredTipsAdapter.getItemCount() > 0 && !isAutoScrolling) {
            startAutoScrollForFeatured();
        }
    }

    /**
     * Được gọi khi fragment bị ẩn (hidden from user)
     */
    @Override
    public void onFragmentHidden() {
        // Dừng auto-scroll khi fragment bị ẩn
        stopAutoScrollForFeatured();
    }
}
