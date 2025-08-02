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
                        getView().showFavoriteHealthTips(favoriteHealthTips);
                    } else {
                        getView().showEmptyFavorites();
                    }
                }
            }

            @Override
            public void onError(String error) {
                if (getView() != null) {
                    getView().hideLoading();
                    getView().showError("Không thể tải danh sách yêu thích: " + error);
                }
            }
        });
    }

    /**
     * Làm mới danh sách bài viết yêu thích
     */
    public void refreshFavorites() {
        loadFavoriteHealthTips();
    }

    /**
     * Xử lý khi người dùng chọn một bài viết
     * @param healthTip bài viết được chọn
     */
    public void onHealthTipSelected(HealthTip healthTip) {
        if (getView() != null && healthTip != null) {
            getView().navigateToHealthTipDetail(healthTip);
        }
    }

    /**
     * Xóa bài viết khỏi danh sách yêu thích
     * @param healthTip bài viết cần xóa
     */
    public void removeFromFavorites(HealthTip healthTip) {
        if (healthTip == null) return;

        String userId = userSessionManager.getCurrentUserId();
        if (userId == null) {
            if (getView() != null) {
                getView().showRemoveFavoriteError("Vui lòng đăng nhập để thực hiện thao tác này");
            }
            return;
        }

        favoriteRepository.removeFromFavorites(userId, healthTip.getId(), new FavoriteRepository.FavoriteActionCallback() {
            @Override
            public void onSuccess() {
                if (getView() != null) {
                    getView().showRemoveFavoriteSuccess("Đã xóa khỏi danh sách yêu thích");
                    // Tải lại danh sách sau khi xóa
                    loadFavoriteHealthTips();
                }
            }

            @Override
            public void onError(String error) {
                if (getView() != null) {
                    getView().showRemoveFavoriteError("Không thể xóa khỏi danh sách yêu thích: " + error);
                }
            }
        });
    }
}
