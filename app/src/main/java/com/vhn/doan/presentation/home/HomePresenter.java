package com.vhn.doan.presentation.home;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.vhn.doan.data.Category;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.repository.CategoryRepository;
import com.vhn.doan.data.repository.HealthTipRepository;

import java.util.ArrayList;

/**
 * HomePresenter kết nối HomeView với dữ liệu từ Repository
 * Tuân theo kiến trúc MVP
 */
public class HomePresenter {

    private static final int CATEGORY_LIMIT = 10;
    private static final int HEALTH_TIP_LIMIT = 10;

    private HomeView view;
    private final Context context;
    private final CategoryRepository categoryRepository;
    private final HealthTipRepository healthTipRepository;

    // Biến để lưu trữ đối tượng listener cho firebase
    private Object categoriesListener;
    private Object latestTipsListener;

    /**
     * Constructor
     * @param context Context của ứng dụng
     * @param categoryRepository Repository cho danh mục
     * @param healthTipRepository Repository cho mẹo sức khỏe
     */
    public HomePresenter(Context context, CategoryRepository categoryRepository, HealthTipRepository healthTipRepository) {
        this.context = context;
        this.categoryRepository = categoryRepository;
        this.healthTipRepository = healthTipRepository;
    }

    /**
     * Gắn view vào presenter
     * @param view HomeView để hiển thị dữ liệu
     */
    public void attachView(HomeView view) {
        this.view = view;
    }

    /**
     * Tách view khỏi presenter để tránh memory leak
     */
    public void detachView() {
        this.view = null;
    }

    /**
     * Bắt đầu tải dữ liệu
     */
    public void start() {
        if (view != null) {
            view.showLoading(true);
            loadHomeData();
        }
    }

    /**
     * Dừng lắng nghe khi không cần thiết
     */
    public void stop() {
        if (categoriesListener != null) {
            categoryRepository.removeListener(categoriesListener);
            categoriesListener = null;
        }

        if (latestTipsListener != null) {
            healthTipRepository.removeListener(latestTipsListener);
            latestTipsListener = null;
        }
    }

    /**
     * Tải dữ liệu cho trang chủ
     * NOTE: KHÔNG check network ở đây nữa - để Repository tự xử lý offline/online
     */
    private void loadHomeData() {
        // Kiểm tra offline mode để hiển thị indicator (optional)
        if (!isNetworkAvailable() && view != null) {
            view.showOfflineMode();
        }

        // Luôn gọi repository - repository sẽ tự xử lý offline/online
        // Tải danh mục hoạt động
        categoryRepository.getCategoriesByActiveStatus(true, new CategoryRepository.CategoryCallback() {
            @Override
            public void onSuccess(java.util.List<Category> categories) {
                if (view != null) {
                    view.showCategories(categories);
                }
            }

            @Override
            public void onSingleCategoryLoaded(Category category) {
                // Không sử dụng trong trường hợp này
            }

            @Override
            public void onError(String errorMessage) {
                if (view != null) {
                    view.showError("Không thể tải danh mục: " + errorMessage);
                }
            }
        });

        // Tải mẹo mới nhất
        healthTipRepository.getLatestHealthTips(HEALTH_TIP_LIMIT, new HealthTipRepository.HealthTipCallback() {
            @Override
            public void onSuccess(java.util.List<HealthTip> healthTips) {
                if (view != null) {
                    view.showLatestHealthTips(healthTips);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (view != null) {
                    view.showError("Không thể tải mẹo mới nhất: " + errorMessage);
                }
            }
        });

        // Tải mẹo xem nhiều nhất
        healthTipRepository.getMostViewedHealthTips(HEALTH_TIP_LIMIT, new HealthTipRepository.HealthTipCallback() {
            @Override
            public void onSuccess(java.util.List<HealthTip> healthTips) {
                if (view != null) {
                    view.showMostViewedHealthTips(healthTips);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (view != null) {
                    view.showError("Không thể tải mẹo xem nhiều: " + errorMessage);
                }
            }
        });

        // Tải mẹo đề xuất cho người dùng (chỉ 10 bài phù hợp cho hôm nay)
        healthTipRepository.getTodayRecommendedHealthTips(HEALTH_TIP_LIMIT, new HealthTipRepository.HealthTipCallback() {
            @Override
            public void onSuccess(java.util.List<HealthTip> healthTips) {
                if (view != null) {
                    view.showRecommendedHealthTips(healthTips);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (view != null) {
                    view.showError("Không thể tải mẹo đề xuất: " + errorMessage);
                }
            }
        });

        // Tải mẹo được thích nhiều nhất
        healthTipRepository.getMostLikedHealthTips(HEALTH_TIP_LIMIT, new HealthTipRepository.HealthTipCallback() {
            @Override
            public void onSuccess(java.util.List<HealthTip> healthTips) {
                if (view != null) {
                    view.showMostLikedHealthTips(healthTips);
                    view.showLoading(false); // Tắt loading sau khi tất cả dữ liệu đã được tải
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (view != null) {
                    view.showError("Không thể tải mẹo được thích nhiều: " + errorMessage);
                    view.showLoading(false);
                }
            }
        });
    }

    /**
     * Lắng nghe thay đổi từ danh mục
     */
    public void listenToCategories() {
        if (categoriesListener == null) {
            categoriesListener = categoryRepository.listenToCategories(new CategoryRepository.CategoryCallback() {
                @Override
                public void onSuccess(java.util.List<Category> categories) {
                    if (view != null) {
                        view.showCategories(categories);
                    }
                }

                @Override
                public void onSingleCategoryLoaded(Category category) {
                    // Không sử dụng trong trường hợp này
                }

                @Override
                public void onError(String errorMessage) {
                    if (view != null) {
                        view.showError("Lỗi theo dõi danh mục: " + errorMessage);
                    }
                }
            });
        }
    }

    /**
     * Lắng nghe thay đổi từ các mẹo sức khỏe mới nhất
     */
    public void listenToLatestHealthTips() {
        if (latestTipsListener == null) {
            latestTipsListener = healthTipRepository.listenToLatestHealthTips(HEALTH_TIP_LIMIT, new HealthTipRepository.HealthTipCallback() {
                @Override
                public void onSuccess(java.util.List<HealthTip> healthTips) {
                    if (view != null) {
                        view.showLatestHealthTips(healthTips);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    if (view != null) {
                        view.showError("Lỗi theo dõi mẹo mới nhất: " + errorMessage);
                    }
                }
            });
        }
    }

    /**
     * Xử lý sự kiện khi người dùng chọn một danh mục
     * @param category Danh mục được chọn
     */
    public void onCategorySelected(Category category) {
        if (view != null) {
            view.navigateToCategoryDetail(category);
        }
    }

    /**
     * Xử lý sự kiện khi người dùng chọn một mẹo sức khỏe
     * @param healthTip Mẹo sức khỏe được chọn
     */
    public void onHealthTipSelected(HealthTip healthTip) {
        if (view != null) {
            // 🎯 FIX: NAVIGATE NGAY LẬP TỨC - không đợi updateViewCount
            // Điều này đảm bảo offline mode hoạt động mượt mà
            view.navigateToHealthTipDetail(healthTip);

            // Tăng lượt xem trong background (không block UI)
            // Nếu offline, sẽ fail nhưng không ảnh hưởng đến UX
            healthTipRepository.updateViewCount(healthTip.getId(), new HealthTipRepository.HealthTipOperationCallback() {
                @Override
                public void onSuccess() {
                    // Silent success - không cần thông báo
                }

                @Override
                public void onError(String errorMessage) {
                    // Silent fail - không cần thông báo (có thể do offline)
                    android.util.Log.d("HomePresenter", "Failed to update view count (might be offline): " + errorMessage);
                }
            });
        }
    }

    /**
     * Xử lý tìm kiếm mẹo sức khỏe
     * @param query Từ khóa tìm kiếm
     */
    public void searchHealthTips(String query) {
        if (view != null) {
            view.navigateToSearch();
        }
    }

    /**
     * Kiểm tra kết nối mạng
     * @return true nếu có kết nối mạng
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
