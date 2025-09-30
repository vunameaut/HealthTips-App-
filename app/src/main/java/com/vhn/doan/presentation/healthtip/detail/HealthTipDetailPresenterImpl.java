package com.vhn.doan.presentation.healthtip.detail;

import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.repository.FavoriteRepository;
import com.vhn.doan.data.repository.HealthTipRepository;
import com.vhn.doan.presentation.base.BaseView;
import com.vhn.doan.utils.UserSessionManager;

/**
 * Lớp HealthTipDetailPresenterImpl triển khai HealthTipDetailPresenter
 * xử lý logic cho màn hình chi tiết bài viết sức khỏe
 * Tuân theo kiến trúc MVP (Model-View-Presenter)
 */
public class HealthTipDetailPresenterImpl implements HealthTipDetailPresenter {

    private final HealthTipRepository healthTipRepository;
    private final FavoriteRepository favoriteRepository;
    private final UserSessionManager userSessionManager;
    private HealthTipDetailView view;
    private HealthTip currentHealthTip;

    /**
     * Constructor
     * @param healthTipRepository Repository để truy cập dữ liệu health tip
     * @param favoriteRepository Repository để quản lý yêu thích
     * @param userSessionManager Manager để quản lý session người dùng
     */
    public HealthTipDetailPresenterImpl(HealthTipRepository healthTipRepository,
                                       FavoriteRepository favoriteRepository,
                                       UserSessionManager userSessionManager) {
        this.healthTipRepository = healthTipRepository;
        this.favoriteRepository = favoriteRepository;
        this.userSessionManager = userSessionManager;
    }

    @Override
    public void attachView(BaseView view) {
        if (view instanceof HealthTipDetailView) {
            this.view = (HealthTipDetailView) view;
        }
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void start() {
        // Khởi tạo presenter
    }

    @Override
    public void stop() {
        // Dọn dẹp tài nguyên
        detachView();
    }

    @Override
    public void loadHealthTipDetail(String tipId) {
        if (tipId == null || tipId.isEmpty()) {
            if (isViewAttached()) {
                view.showError("ID bài viết không hợp lệ");
            }
            return;
        }

        if (isViewAttached()) {
            view.showLoading(true);
        }

        healthTipRepository.getHealthTipDetail(tipId, new HealthTipRepository.SingleHealthTipCallback() {
            @Override
            public void onSuccess(HealthTip healthTip) {
                if (isViewAttached()) {
                    view.showLoading(false);
                    if (healthTip != null) {
                        currentHealthTip = healthTip;
                        view.displayHealthTipDetails(healthTip);

                        // Kiểm tra trạng thái yêu thích sau khi load bài viết
                        checkFavoriteStatus(tipId);

                        // Cập nhật view count
                        updateViewCount(tipId);
                    } else {
                        view.showError("Không tìm thấy bài viết");
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (isViewAttached()) {
                    view.showLoading(false);
                    view.showError("Lỗi khi tải chi tiết bài viết: " + errorMessage);
                }
            }
        });
    }

    @Override
    public void onFavoriteClick(String tipId) {
        if (currentHealthTip == null || tipId == null) {
            if (isViewAttached()) {
                view.showError("Không thể thực hiện thao tác yêu thích");
            }
            return;
        }

        String userId = userSessionManager.getCurrentUserId();
        if (userId == null) {
            if (isViewAttached()) {
                view.showError("Vui lòng đăng nhập để sử dụng chức năng yêu thích");
            }
            return;
        }

        boolean currentFavoriteStatus = currentHealthTip.isFavorite();
        boolean newFavoriteStatus = !currentFavoriteStatus;

        if (newFavoriteStatus) {
            // Thêm vào yêu thích
            favoriteRepository.addToFavorites(userId, tipId, new FavoriteRepository.FavoriteActionCallback() {
                @Override
                public void onSuccess() {
                    if (isViewAttached() && currentHealthTip != null) {
                        currentHealthTip.setFavorite(true);
                        view.updateFavoriteStatus(true);
                        view.showMessage("Đã thêm vào yêu thích");
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    if (isViewAttached()) {
                        view.showError("Lỗi khi thêm vào yêu thích: " + errorMessage);
                    }
                }
            });
        } else {
            // Xóa khỏi yêu thích
            favoriteRepository.removeFromFavorites(userId, tipId, new FavoriteRepository.FavoriteActionCallback() {
                @Override
                public void onSuccess() {
                    if (isViewAttached() && currentHealthTip != null) {
                        currentHealthTip.setFavorite(false);
                        view.updateFavoriteStatus(false);
                        view.showMessage("Đã xóa khỏi yêu thích");
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    if (isViewAttached()) {
                        view.showError("Lỗi khi xóa khỏi yêu thích: " + errorMessage);
                    }
                }
            });
        }
    }

    @Override
    public void onLikeClick(String tipId) {
        if (currentHealthTip == null || tipId == null) {
            if (isViewAttached()) {
                view.showError("Không thể thực hiện thao tác thích");
            }
            return;
        }

        boolean newLikeStatus = !currentHealthTip.isLiked();
        int currentLikeCount = currentHealthTip.getLikeCount();
        int newLikeCount = newLikeStatus ? currentLikeCount + 1 : Math.max(0, currentLikeCount - 1);

        // Cập nhật UI ngay lập tức để có trải nghiệm tốt
        currentHealthTip.setLiked(newLikeStatus);
        currentHealthTip.setLikeCount(newLikeCount);
        if (isViewAttached()) {
            view.updateLikeStatus(newLikeStatus);
            view.updateLikeCount(newLikeCount);
        }

        healthTipRepository.updateLikeStatus(tipId, newLikeStatus, new HealthTipRepository.HealthTipOperationCallback() {
            @Override
            public void onSuccess() {
                if (isViewAttached()) {
                    view.showMessage(newLikeStatus ? "Đã thích bài viết" : "Đã bỏ thích bài viết");
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Revert lại trạng thái cũ nếu có lỗi
                if (currentHealthTip != null) {
                    currentHealthTip.setLiked(!newLikeStatus);
                    currentHealthTip.setLikeCount(currentLikeCount);
                    if (isViewAttached()) {
                        view.updateLikeStatus(!newLikeStatus);
                        view.updateLikeCount(currentLikeCount);
                        view.showError("Lỗi khi cập nhật trạng thái thích: " + errorMessage);
                    }
                }
            }
        });
    }

    @Override
    public void onShareClick(String tipId) {
        if (currentHealthTip == null) {
            if (isViewAttached()) {
                view.showError("Không có nội dung để chia sẻ");
            }
            return;
        }

        String shareText = "Chia sẻ mẹo sức khỏe: " + currentHealthTip.getTitle() + "\n\n" +
                          currentHealthTip.getContent() + "\n\nTải app để xem thêm nhiều mẹo sức khỏe hữu ích!";

        if (isViewAttached()) {
            view.shareContent(shareText);
        }
    }

    @Override
    public void updateViewCount(String tipId) {
        if (tipId == null || tipId.isEmpty()) {
            return;
        }

        healthTipRepository.updateViewCount(tipId, new HealthTipRepository.HealthTipOperationCallback() {
            @Override
            public void onSuccess() {
                // Cập nhật thành công view count
                if (currentHealthTip != null) {
                    int newViewCount = currentHealthTip.getViewCount() + 1;
                    currentHealthTip.setViewCount(newViewCount);
                    if (isViewAttached()) {
                        view.updateViewCount(newViewCount);
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Lỗi cập nhật view count không cần thông báo cho user
            }
        });
    }

    /**
     * Kiểm tra trạng thái yêu thích của bài viết hiện tại
     */
    private void checkFavoriteStatus(String tipId) {
        String userId = userSessionManager.getCurrentUserId();
        if (userId == null) {
            return;
        }

        favoriteRepository.checkFavoriteStatus(userId, tipId, new FavoriteRepository.FavoriteStatusCallback() {
            @Override
            public void onResult(boolean isFavorite) {
                if (currentHealthTip != null) {
                    currentHealthTip.setFavorite(isFavorite);
                    if (isViewAttached()) {
                        view.updateFavoriteStatus(isFavorite);
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Lỗi kiểm tra trạng thái yêu thích không cần thông báo
            }
        });
    }

    /**
     * Kiểm tra xem view có đang được gắn không
     */
    private boolean isViewAttached() {
        return view != null;
    }
}
