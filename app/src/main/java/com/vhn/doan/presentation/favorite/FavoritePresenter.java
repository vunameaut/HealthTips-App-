package com.vhn.doan.presentation.favorite;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.repository.FavoriteRepository;
import com.vhn.doan.data.repository.HealthTipRepository;
import com.vhn.doan.presentation.base.BasePresenter;

import java.util.List;

/**
 * Presenter cho màn hình yêu thích
 * Xử lý logic nghiệp vụ và giao tiếp giữa View và Model
 */
public class FavoritePresenter extends BasePresenter<FavoriteView> {

    private final Context context;
    private final FavoriteRepository favoriteRepository;
    private final HealthTipRepository healthTipRepository;
    private final FirebaseAuth firebaseAuth;

    public FavoritePresenter(Context context,
                           FavoriteRepository favoriteRepository,
                           HealthTipRepository healthTipRepository) {
        this.context = context;
        this.favoriteRepository = favoriteRepository;
        this.healthTipRepository = healthTipRepository;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Lấy view hiện tại
     */
    protected FavoriteView getView() {
        return view;
    }

    /**
     * Tải danh sách mẹo sức khỏe yêu thích
     */
    public void loadFavoriteHealthTips() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (isViewAttached()) {
                getView().showError("Vui lòng đăng nhập để xem danh sách yêu thích");
            }
            return;
        }

        if (isViewAttached()) {
            getView().showLoading(true);
        }

        favoriteRepository.getFavoriteHealthTipIds(currentUser.getUid(), new FavoriteRepository.FavoriteListCallback() {
            @Override
            public void onSuccess(List<HealthTip> favoriteHealthTips) {
                if (isViewAttached()) {
                    getView().showLoading(false);

                    if (favoriteHealthTips.isEmpty()) {
                        getView().showEmptyFavorites();
                    } else {
                        getView().showFavoriteHealthTips(favoriteHealthTips);
                    }
                }
            }

            @Override
            public void onError(String error) {
                if (isViewAttached()) {
                    getView().showLoading(false);
                    getView().showError("Không thể tải danh sách yêu thích: " + error);
                }
            }
        });
    }

    /**
     * Xử lý khi người dùng chọn một mẹo sức khỏe
     */
    public void onHealthTipSelected(HealthTip healthTip) {
        if (isViewAttached()) {
            getView().navigateToHealthTipDetail(healthTip);
        }
    }

    /**
     * Xóa mẹo sức khỏe khỏi danh sách yêu thích
     */
    public void removeFromFavorites(HealthTip healthTip) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (isViewAttached()) {
                getView().showError("Vui lòng đăng nhập để thực hiện thao tác này");
            }
            return;
        }

        favoriteRepository.removeFromFavorites(currentUser.getUid(), healthTip.getId(),
            new FavoriteRepository.FavoriteActionCallback() {
                @Override
                public void onSuccess() {
                    if (isViewAttached()) {
                        getView().showRemovedFromFavorites(healthTip.getTitle());
                        // Tải lại danh sách sau khi xóa
                        loadFavoriteHealthTips();
                    }
                }

                @Override
                public void onError(String error) {
                    if (isViewAttached()) {
                        getView().showError("Không thể xóa khỏi yêu thích: " + error);
                    }
                }
            });
    }

    /**
     * Refresh danh sách yêu thích
     */
    public void refreshFavorites() {
        loadFavoriteHealthTips();
    }

    @Override
    public void start() {
        loadFavoriteHealthTips();
    }
}
