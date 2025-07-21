package com.vhn.doan.presentation.healthtip.detail;

import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.repository.HealthTipRepository;
import com.vhn.doan.presentation.base.BasePresenter;

/**
 * Lớp HealthTipDetailPresenterImpl triển khai HealthTipDetailPresenter
 * xử lý logic cho màn hình chi tiết bài viết sức khỏe
 * Tuân theo kiến trúc MVP (Model-View-Presenter)
 */
public class HealthTipDetailPresenterImpl extends BasePresenter<HealthTipDetailView> implements HealthTipDetailPresenter {

    private final HealthTipRepository healthTipRepository;
    private HealthTip currentHealthTip;

    /**
     * Constructor
     * @param view View để hiển thị dữ liệu
     * @param healthTipRepository Repository để truy cập dữ liệu
     */
    public HealthTipDetailPresenterImpl(HealthTipDetailView view, HealthTipRepository healthTipRepository) {
        this.healthTipRepository = healthTipRepository;
        attachView(view);
    }

    @Override
    public void start() {
        // Không có hành động khởi tạo cần thiết ở đây
        // Việc tải dữ liệu sẽ được thực hiện trong loadHealthTipDetails()
    }

    @Override
    public void loadHealthTipDetails(String healthTipId) {
        if (healthTipId == null || healthTipId.isEmpty()) {
            view.showError("ID bài viết không hợp lệ");
            return;
        }

        view.showLoading(true);

        healthTipRepository.getHealthTipById(healthTipId, new HealthTipRepository.SingleHealthTipCallback() {
            @Override
            public void onSuccess(HealthTip healthTip) {
                if (isViewAttached()) {
                    view.showLoading(false);
                    if (healthTip != null) {
                        currentHealthTip = healthTip;
                        view.displayHealthTipDetails(healthTip);
                        incrementViewCount(healthTipId);
                    } else {
                        view.showError("Không tìm thấy bài viết");
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (isViewAttached()) {
                    view.showLoading(false);
                    view.showError(errorMessage);
                }
            }
        });
    }

    @Override
    public void toggleFavoriteStatus(String healthTipId, boolean isFavorite) {
        if (healthTipId == null || healthTipId.isEmpty()) {
            view.showError("ID bài viết không hợp lệ");
            return;
        }

        healthTipRepository.updateFavoriteStatus(healthTipId, isFavorite, new HealthTipRepository.HealthTipOperationCallback() {
            @Override
            public void onSuccess(String healthTipId) {
                view.updateFavoriteStatus(isFavorite);
                if (isFavorite) {
                    view.showMessage("Đã thêm vào danh sách yêu thích");
                } else {
                    view.showMessage("Đã xóa khỏi danh sách yêu thích");
                }
            }

            @Override
            public void onError(String errorMessage) {
                view.showError(errorMessage);
            }
        });
    }

    @Override
    public void incrementViewCount(String healthTipId) {
        if (healthTipId == null || healthTipId.isEmpty()) {
            return; // Lỗi tĩnh, không cần thông báo
        }

        healthTipRepository.incrementViewCount(healthTipId, new HealthTipRepository.HealthTipOperationCallback() {
            @Override
            public void onSuccess(String healthTipId) {
                if (currentHealthTip != null) {
                    currentHealthTip.setViewCount(currentHealthTip.getViewCount() + 1);
                    view.updateViewCount(currentHealthTip.getViewCount());
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Không hiển thị lỗi cho người dùng khi cập nhật số lượt xem thất bại
                // Đây là thao tác ngầm
            }
        });
    }

    @Override
    public void toggleLike(String healthTipId, boolean isLiked) {
        if (healthTipId == null || healthTipId.isEmpty()) {
            view.showError("ID bài viết không hợp lệ");
            return;
        }

        healthTipRepository.updateLikeStatus(healthTipId, isLiked, new HealthTipRepository.HealthTipOperationCallback() {
            @Override
            public void onSuccess(String healthTipId) {
                if (currentHealthTip != null) {
                    int newLikeCount = isLiked ?
                            currentHealthTip.getLikeCount() + 1 :
                            Math.max(0, currentHealthTip.getLikeCount() - 1);

                    currentHealthTip.setLikeCount(newLikeCount);
                    view.updateLikeCount(newLikeCount);

                    if (isLiked) {
                        view.showMessage("Bạn đã thích bài viết này");
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                view.showError(errorMessage);
            }
        });
    }
}
