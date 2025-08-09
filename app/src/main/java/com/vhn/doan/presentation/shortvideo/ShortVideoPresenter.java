package com.vhn.doan.presentation.shortvideo;

import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.repository.RepositoryCallback;
import com.vhn.doan.data.repository.ShortVideoRepository;
import com.vhn.doan.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Presenter cho chức năng video ngắn
 * Xử lý logic nghiệp vụ và tương tác với Repository
 */
public class ShortVideoPresenter implements ShortVideoContract.Presenter {

    private ShortVideoContract.View view;
    private final ShortVideoRepository repository;
    private final SharedPreferencesHelper preferencesHelper;
    private final List<ShortVideo> currentVideos;
    private String currentUserId;
    private String currentFilter = null; // null = recommended, "trending", hoặc categoryId

    private static final int VIDEO_LIMIT = 20;

    public ShortVideoPresenter(ShortVideoRepository repository, SharedPreferencesHelper preferencesHelper) {
        this.repository = repository;
        this.preferencesHelper = preferencesHelper;
        this.currentVideos = new ArrayList<>();
    }

    @Override
    public void attachView(ShortVideoContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void start() {
        currentUserId = preferencesHelper.getCurrentUserId();
        loadRecommendedVideos();
    }

    @Override
    public void loadRecommendedVideos() {
        if (view != null) {
            view.showLoading();
        }

        currentFilter = null;

        if (currentUserId == null || currentUserId.isEmpty()) {
            // Người dùng chưa đăng nhập -> hiển thị video trending
            loadTrendingVideos();
            return;
        }

        repository.getRecommendedVideos(currentUserId, VIDEO_LIMIT, new RepositoryCallback<List<ShortVideo>>() {
            @Override
            public void onSuccess(List<ShortVideo> videos) {
                if (view != null) {
                    view.hideLoading();

                    currentVideos.clear();
                    currentVideos.addAll(videos);

                    if (videos.isEmpty()) {
                        view.showEmptyState();
                    } else {
                        view.showVideos(videos);
                    }
                }
            }

            @Override
            public void onError(String error) {
                if (view != null) {
                    view.hideLoading();
                    view.showError("Không thể tải video: " + error);
                }
            }
        });
    }

    @Override
    public void loadVideosByCategory(String categoryId) {
        if (view != null) {
            view.showLoading();
        }

        currentFilter = categoryId;

        repository.getVideosByCategory(categoryId, VIDEO_LIMIT, new RepositoryCallback<List<ShortVideo>>() {
            @Override
            public void onSuccess(List<ShortVideo> videos) {
                if (view != null) {
                    view.hideLoading();

                    currentVideos.clear();
                    currentVideos.addAll(videos);

                    if (videos.isEmpty()) {
                        view.showEmptyState();
                    } else {
                        view.showVideos(videos);
                    }
                }
            }

            @Override
            public void onError(String error) {
                if (view != null) {
                    view.hideLoading();
                    view.showError("Không thể tải video theo danh mục: " + error);
                }
            }
        });
    }

    @Override
    public void loadTrendingVideos() {
        if (view != null) {
            view.showLoading();
        }

        currentFilter = "trending";

        repository.getTrendingVideos(VIDEO_LIMIT, new RepositoryCallback<List<ShortVideo>>() {
            @Override
            public void onSuccess(List<ShortVideo> videos) {
                if (view != null) {
                    view.hideLoading();

                    currentVideos.clear();
                    currentVideos.addAll(videos);

                    if (videos.isEmpty()) {
                        view.showEmptyState();
                    } else {
                        view.showVideos(videos);
                    }
                }
            }

            @Override
            public void onError(String error) {
                if (view != null) {
                    view.hideLoading();
                    view.showError("Không thể tải video trending: " + error);
                }
            }
        });
    }

    @Override
    public void onVideoLiked(int position, String videoId, boolean isCurrentlyLiked) {
        if (position < 0 || position >= currentVideos.size()) {
            return;
        }

        // Cập nhật UI ngay lập tức để UX mượt mà
        ShortVideo video = currentVideos.get(position);
        boolean newLikedState = !isCurrentlyLiked;
        int newLikeCount = newLikedState ? video.getLikeCount() + 1 : Math.max(0, video.getLikeCount() - 1);

        // Cập nhật dữ liệu local
        video.setLikeCount(newLikeCount);
        video.setLikedByCurrentUser(newLikedState);

        if (view != null) {
            view.updateVideoLike(position, newLikedState, newLikeCount);
        }

        // Cập nhật trên server
        repository.updateLikeCount(videoId, newLikedState, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Thành công - UI đã được cập nhật trước đó
            }

            @Override
            public void onError(String error) {
                // Rollback nếu có lỗi
                boolean originalState = !newLikedState;
                int originalCount = originalState ? video.getLikeCount() + 1 : Math.max(0, video.getLikeCount() - 1);
                video.setLikeCount(originalCount);
                video.setLikedByCurrentUser(originalState);

                if (view != null) {
                    view.updateVideoLike(position, originalState, originalCount);
                    view.showError("Không thể cập nhật like");
                }
            }
        });
    }

    @Override
    public void onVideoViewed(int position, String videoId) {
        if (position < 0 || position >= currentVideos.size()) {
            return;
        }

        ShortVideo video = currentVideos.get(position);

        repository.incrementViewCount(videoId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Cập nhật view count local
                int newViewCount = video.getViewCount() + 1;
                video.setViewCount(newViewCount);

                if (view != null) {
                    view.updateVideoView(position, newViewCount);
                }
            }

            @Override
            public void onError(String error) {
                // Không cần hiển thị lỗi cho view count vì không quan trọng lắm
                // Log lỗi nếu cần
            }
        });
    }

    @Override
    public void refreshData() {
        // Tải lại dữ liệu dựa trên filter hiện tại
        if (currentFilter == null) {
            loadRecommendedVideos();
        } else if ("trending".equals(currentFilter)) {
            loadTrendingVideos();
        } else {
            loadVideosByCategory(currentFilter);
        }
    }

    @Override
    public void loadMoreVideos() {
        // TODO: Implement pagination nếu cần
        // Hiện tại chưa implement vì sẽ phức tạp với logic recommendation
        if (view != null) {
            view.showSuccess("Đã tải hết video");
        }
    }

    @Override
    public void onVideoShared(String videoId) {
        // TODO: Implement logic share video
        // Có thể track analytics hoặc tăng share count
        if (view != null) {
            view.showSuccess("Video đã được chia sẻ");
        }
    }

    @Override
    public void filterVideosByTag(String tag) {
        if (currentVideos.isEmpty()) {
            return;
        }

        List<ShortVideo> filteredVideos = new ArrayList<>();
        for (ShortVideo video : currentVideos) {
            if (video.getTags() != null && Boolean.TRUE.equals(video.getTags().get(tag))) {
                filteredVideos.add(video);
            }
        }

        if (view != null) {
            if (filteredVideos.isEmpty()) {
                view.showEmptyState();
            } else {
                view.showVideos(filteredVideos);
            }
        }
    }

    @Override
    public void addComment(String videoId, String commentText) {
        if (currentUserId == null || commentText == null || commentText.trim().isEmpty()) {
            if (view != null) {
                view.showError("Không thể gửi bình luận");
            }
            return;
        }

        com.vhn.doan.data.VideoComment comment = new com.vhn.doan.data.VideoComment(
                currentUserId,
                commentText,
                System.currentTimeMillis()
        );

        repository.addComment(videoId, comment, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (view != null) {
                    view.showSuccess("Đã gửi bình luận");
                }
            }

            @Override
            public void onError(String error) {
                if (view != null) {
                    view.showError("Không thể gửi bình luận");
                }
            }
        });
    }

    /**
     * Lấy video hiện tại tại vị trí cụ thể
     */
    public ShortVideo getVideoAt(int position) {
        if (position >= 0 && position < currentVideos.size()) {
            return currentVideos.get(position);
        }
        return null;
    }

    /**
     * Lấy tổng số video hiện tại
     */
    public int getVideoCount() {
        return currentVideos.size();
    }
}
