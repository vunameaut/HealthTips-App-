package com.vhn.doan.presentation.search;

import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.SearchHistory;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.repository.RepositoryCallback;
import com.vhn.doan.data.repository.SearchRepository;
import com.vhn.doan.utils.FirebaseAuthHelper;

import java.util.List;

/**
 * Presenter cho chức năng tìm kiếm, theo kiến trúc MVP
 */
public class SearchPresenter implements SearchContract.Presenter {
    private SearchContract.View mView;
    private final SearchRepository mSearchRepository;
    private final FirebaseAuthHelper mAuthHelper;
    private static final int SEARCH_HISTORY_LIMIT = 10;
    private static final int MAX_SEARCH_LENGTH = 100; // Giới hạn độ dài query tìm kiếm

    public SearchPresenter(SearchRepository searchRepository, FirebaseAuthHelper authHelper) {
        mSearchRepository = searchRepository;
        mAuthHelper = authHelper;
    }

    @Override
    public void attachView(SearchContract.View view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void loadSearchHistory() {
        String userId = mAuthHelper.getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            if (mView != null) {
                mView.showSearchHistory(List.of()); // Không có lịch sử nếu chưa đăng nhập
            }
            return;
        }

        mSearchRepository.getSearchHistory(userId, SEARCH_HISTORY_LIMIT, new RepositoryCallback<List<SearchHistory>>() {
            @Override
            public void onSuccess(List<SearchHistory> data) {
                if (mView != null) {
                    mView.showSearchHistory(data);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (mView != null) {
                    mView.showError("Không thể tải lịch sử tìm kiếm: " + errorMessage);
                    mView.showSearchHistory(List.of());
                }
            }
        });
    }

    @Override
    public void search(String keyword) {
        // 1. Kiểm tra rỗng
        if (keyword == null || keyword.trim().isEmpty()) {
            if (mView != null) {
                mView.showError("Vui lòng nhập từ khóa tìm kiếm");
            }
            return;
        }

        // 2. Trim khoảng trắng
        final String trimmedKeyword = keyword.trim();

        // 3. Kiểm tra độ dài query
        if (trimmedKeyword.length() > MAX_SEARCH_LENGTH) {
            if (mView != null) {
                mView.showError("Từ khóa tìm kiếm quá dài (tối đa " + MAX_SEARCH_LENGTH + " ký tự)");
            }
            return;
        }

        // 4. Hiển thị loading
        if (mView != null) {
            mView.showLoading(true);
        }

        // Lưu từ khóa tìm kiếm vào lịch sử
        String userId = mAuthHelper.getCurrentUserId();
        if (userId != null && !userId.isEmpty()) {
            mSearchRepository.saveSearchHistory(trimmedKeyword, userId, new RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    // Không tải lại lịch sử tìm kiếm sau khi lưu thành công
                    // Việc này gây ra hiện tượng ViewFlipper bị chuyển về màn hình lịch sử
                }

                @Override
                public void onError(String errorMessage) {
                    if (mView != null) {
                        mView.showError("Không thể lưu lịch sử tìm kiếm: " + errorMessage);
                    }
                }
            });
        }

        // Tìm kiếm bài viết
        mSearchRepository.searchHealthTips(trimmedKeyword, new RepositoryCallback<List<HealthTip>>() {
            @Override
            public void onSuccess(List<HealthTip> healthTips) {
                if (mView != null) {
                    mView.showHealthTipResults(healthTips);

                    // Nếu cả hai loại kết quả đều trống, hiển thị thông báo không có kết quả
                    if (healthTips.isEmpty()) {
                        checkAndShowNoResults();
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (mView != null) {
                    mView.showError("Lỗi khi tìm kiếm bài viết: " + errorMessage);
                    mView.showHealthTipResults(List.of());
                    checkAndShowNoResults();
                }
            }
        });

        // Tìm kiếm video
        mSearchRepository.searchVideos(trimmedKeyword, new RepositoryCallback<List<ShortVideo>>() {
            @Override
            public void onSuccess(List<ShortVideo> videos) {
                if (mView != null) {
                    mView.showVideoResults(videos);
                    mView.showLoading(false);

                    // Nếu cả hai loại kết quả đều trống, hiển thị thông báo không có kết quả
                    if (videos.isEmpty()) {
                        checkAndShowNoResults();
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (mView != null) {
                    mView.showError("Lỗi khi tìm kiếm video: " + errorMessage);
                    mView.showVideoResults(List.of());
                    mView.showLoading(false);
                    checkAndShowNoResults();
                }
            }
        });
    }

    private void checkAndShowNoResults() {
        // Phương thức này sẽ kiểm tra nếu cả hai danh sách kết quả đều rỗng
        // và hiển thị thông báo "Không có kết quả"
        if (mView != null) {
            mView.showNoResults();
        }
    }

    @Override
    public void deleteSearchHistory(String searchHistoryId) {
        String userId = mAuthHelper.getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            return;
        }

        mSearchRepository.deleteSearchHistory(searchHistoryId, userId, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                loadSearchHistory(); // Tải lại lịch sử sau khi xóa
            }

            @Override
            public void onError(String errorMessage) {
                if (mView != null) {
                    mView.showError("Không thể xóa lịch sử tìm kiếm: " + errorMessage);
                }
            }
        });
    }

    @Override
    public void clearAllSearchHistory() {
        String userId = mAuthHelper.getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            return;
        }

        mSearchRepository.clearAllSearchHistory(userId, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                loadSearchHistory(); // Tải lại lịch sử sau khi xóa tất cả
            }

            @Override
            public void onError(String errorMessage) {
                if (mView != null) {
                    mView.showError("Không thể xóa lịch sử tìm kiếm: " + errorMessage);
                }
            }
        });
    }
}
