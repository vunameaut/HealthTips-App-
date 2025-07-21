package com.vhn.doan.presentation.healthtip.detail;

import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.repository.HealthTipRepository;
import com.vhn.doan.presentation.base.BaseView;

/**
 * Lớp HealthTipDetailPresenterImpl triển khai HealthTipDetailPresenter
 * xử lý logic cho màn hình chi tiết bài viết sức khỏe
 * Tuân theo kiến trúc MVP (Model-View-Presenter)
 */
public class HealthTipDetailPresenterImpl implements HealthTipDetailPresenter {

    private final HealthTipRepository healthTipRepository;
    private HealthTipDetailView view;
    private HealthTip currentHealthTip;

    /**
     * Constructor
     * @param healthTipRepository Repository để truy cập dữ liệu
     */
    public HealthTipDetailPresenterImpl(HealthTipRepository healthTipRepository) {
        this.healthTipRepository = healthTipRepository;
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
            if (view != null) {
                view.showError("ID bài viết không hợp lệ");
            }
            return;
        }

        if (view != null) {
            view.showLoading(true);
        }

        healthTipRepository.getHealthTipDetail(tipId, new HealthTipRepository.SingleHealthTipCallback() {
            @Override
            public void onSuccess(HealthTip healthTip) {
                if (view != null) {
                    view.showLoading(false);
                    if (healthTip != null) {
                        currentHealthTip = healthTip;
                        view.displayHealthTipDetails(healthTip);
                        updateViewCount(tipId);
                    } else {
                        view.showError("Không tìm thấy bài viết");
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (view != null) {
                    view.showLoading(false);
                    view.showError("Lỗi khi tải chi tiết bài viết: " + errorMessage);
                }
            }
        });
    }

    @Override
    public void onFavoriteClick(String tipId) {
        if (currentHealthTip == null || tipId == null) {
            if (view != null) {
                view.showError("Không thể thực hiện thao tác yêu thích");
            }
            return;
        }

        boolean newFavoriteStatus = !currentHealthTip.isFavorite();

        healthTipRepository.updateFavoriteStatus(tipId, newFavoriteStatus, new HealthTipRepository.HealthTipOperationCallback() {
            @Override
            public void onSuccess() {
                if (view != null && currentHealthTip != null) {
                    currentHealthTip.setFavorite(newFavoriteStatus);
                    view.updateFavoriteStatus(newFavoriteStatus);
                    view.showMessage(newFavoriteStatus ? "Đã thêm vào yêu thích" : "Đã bỏ khỏi yêu thích");
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (view != null) {
                    view.showError("Lỗi khi cập nhật trạng thái yêu thích: " + errorMessage);
                }
            }
        });
    }

    @Override
    public void onLikeClick(String tipId) {
        if (currentHealthTip == null || tipId == null) {
            if (view != null) {
                view.showError("Không thể thực hiện thao tác thích");
            }
            return;
        }

        boolean newLikeStatus = !currentHealthTip.isLiked();

        healthTipRepository.updateLikeStatus(tipId, newLikeStatus, new HealthTipRepository.HealthTipOperationCallback() {
            @Override
            public void onSuccess() {
                if (view != null && currentHealthTip != null) {
                    currentHealthTip.setLiked(newLikeStatus);
                    view.updateLikeStatus(newLikeStatus);
                    view.showMessage(newLikeStatus ? "Đã thích bài viết" : "Đã bỏ thích bài viết");
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (view != null) {
                    view.showError("Lỗi khi cập nhật trạng thái thích: " + errorMessage);
                }
            }
        });
    }

    @Override
    public void onShareClick(String tipId) {
        if (currentHealthTip == null) {
            if (view != null) {
                view.showError("Không có nội dung để chia sẻ");
            }
            return;
        }

        String shareText = "Chia sẻ mẹo sức khỏe: " + currentHealthTip.getTitle() + "\n\n" +
                          currentHealthTip.getContent() + "\n\nTải app để xem thêm nhiều mẹo sức khỏe hữu ích!";

        if (view != null) {
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
                // Cập nhật thành công, không cần thông báo
                if (currentHealthTip != null) {
                    currentHealthTip.setViewCount(currentHealthTip.getViewCount() + 1);
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Lỗi cập nhật view count không cần thông báo cho user
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
