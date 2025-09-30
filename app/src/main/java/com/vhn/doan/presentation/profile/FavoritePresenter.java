package com.vhn.doan.presentation.profile;

import android.content.Context;

import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.repository.FavoriteRepository;
import com.vhn.doan.data.repository.HealthTipRepository;
import com.vhn.doan.presentation.base.BasePresenter;
import com.vhn.doan.utils.UserSessionManager;

import java.util.List;

/**
 * Presenter cho màn hình danh sách bài viết yêu thích
 * Xử lý logic nghiệp vụ và giao tiếp giữa View và Repository
 */
public class FavoritePresenter extends BasePresenter<FavoriteView> {

    private final Context context;
    private final FavoriteRepository favoriteRepository;
    private final HealthTipRepository healthTipRepository;
    private final UserSessionManager userSessionManager;

    public FavoritePresenter(Context context,
                           FavoriteRepository favoriteRepository,
                           HealthTipRepository healthTipRepository) {
        this.context = context;
        this.favoriteRepository = favoriteRepository;
        this.healthTipRepository = healthTipRepository;
        this.userSessionManager = new UserSessionManager(context);
    }

    /**
     * Khởi động presenter và tải dữ liệu ban đầu
     */
    public void start() {
        loadFavoriteHealthTips();
    }

    /**
     * Tải danh sách bài viết yêu thích
     */
    public void loadFavoriteHealthTips() {
        if (getView() != null) {
            getView().showLoading();
        }

        String userId = userSessionManager.getCurrentUserId();
        if (userId == null) {
            if (getView() != null) {
                getView().hideLoading();
                getView().showError("Vui lòng đăng nhập để xem danh sách yêu thích");
            }
            return;
        }

        favoriteRepository.getFavoriteHealthTips(userId, new FavoriteRepository.FavoriteListCallback() {
            @Override
            public void onSuccess(List<HealthTip> favoriteHealthTips) {
                if (getView() != null) {
                    getView().hideLoading();
                    if (favoriteHealthTips != null && !favoriteHealthTips.isEmpty()) {
                        getView().displayFavoriteHealthTips(favoriteHealthTips);
                    } else {
                        getView().showEmptyState();
                    }
                }
            }

            @Override
            public void onError(String error) {
                if (getView() != null) {
                    getView().hideLoading();
                    getView().showError("Lỗi khi tải danh sách yêu thích: " + error);
                }
            }
        });
    }

    /**
     * Refresh lại danh sách yêu thích
     */
    public void refreshFavorites() {
        loadFavoriteHealthTips();
    }

    /**
     * Xử lý khi người dùng click vào một bài viết yêu thích
     */
    public void onHealthTipClicked(HealthTip healthTip) {
        if (getView() != null && healthTip != null) {
            getView().navigateToHealthTipDetail(healthTip.getId());
        }
    }

    /**
     * Xử lý khi người dùng xóa bài viết khỏi danh sách yêu thích
     */
    public void onRemoveFavorite(String healthTipId) {
        String userId = userSessionManager.getCurrentUserId();
        if (userId == null || healthTipId == null) {
            if (getView() != null) {
                getView().showError("Không thể thực hiện thao tác");
            }
            return;
        }

        favoriteRepository.removeFromFavorites(userId, healthTipId, new FavoriteRepository.FavoriteActionCallback() {
            @Override
            public void onSuccess() {
                if (getView() != null) {
                    getView().showMessage("Đã xóa khỏi danh sách yêu thích");
                    // Refresh lại danh sách
                    loadFavoriteHealthTips();
                }
            }

            @Override
            public void onError(String error) {
                if (getView() != null) {
                    getView().showError("Lỗi khi xóa khỏi yêu thích: " + error);
                }
            }
        });
    }

    /**
     * Lấy số lượng bài viết yêu thích
     */
    public void getFavoriteCount() {
        String userId = userSessionManager.getCurrentUserId();
        if (userId == null) {
            return;
        }

        favoriteRepository.getFavoriteCount(userId, new FavoriteRepository.FavoriteCountCallback() {
            @Override
            public void onSuccess(int count) {
                if (getView() != null) {
                    getView().updateFavoriteCount(count);
                }
            }

            @Override
            public void onError(String error) {
                // Không cần hiển thị lỗi cho count
            }
        });
    }

    /**
     * Xóa tất cả yêu thích
     */
    public void clearAllFavorites() {
        String userId = userSessionManager.getCurrentUserId();
        if (userId == null) {
            if (getView() != null) {
                getView().showError("Vui lòng đăng nhập");
            }
            return;
        }

        if (getView() != null) {
            getView().showLoading();
        }

        favoriteRepository.clearAllFavorites(userId, new FavoriteRepository.FavoriteActionCallback() {
            @Override
            public void onSuccess() {
                if (getView() != null) {
                    getView().hideLoading();
                    getView().showMessage("Đã xóa tất cả yêu thích");
                    getView().showEmptyState();
                }
            }

            @Override
            public void onError(String error) {
                if (getView() != null) {
                    getView().hideLoading();
                    getView().showError("Lỗi khi xóa tất cả yêu thích: " + error);
                }
            }
        });
    }

    /**
     * Xử lý khi người dùng chọn một bài viết yêu thích (tên phương thức cũ)
     */
    public void onHealthTipSelected(HealthTip healthTip) {
        onHealthTipClicked(healthTip);
    }

    /**
     * Xử lý khi người dùng xóa bài viết khỏi danh sách yêu thích (tên phương thức cũ)
     */
    public void removeFromFavorites(HealthTip healthTip) {
        if (healthTip != null) {
            onRemoveFavorite(healthTip.getId());
        }
    }
}
