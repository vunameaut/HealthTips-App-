package com.vhn.doan.presentation.search;

import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.SearchHistory;
import com.vhn.doan.data.ShortVideo;

import java.util.List;

/**
 * Contract định nghĩa interface View và Presenter cho chức năng tìm kiếm
 */
public interface SearchContract {

    interface View {
        /**
         * Hiển thị danh sách lịch sử tìm kiếm
         * @param searchHistories Danh sách lịch sử tìm kiếm
         */
        void showSearchHistory(List<SearchHistory> searchHistories);

        /**
         * Hiển thị danh sách kết quả bài viết
         * @param healthTips Danh sách bài viết
         */
        void showHealthTipResults(List<HealthTip> healthTips);

        /**
         * Hiển thị danh sách kết quả video
         * @param videos Danh sách video
         */
        void showVideoResults(List<ShortVideo> videos);

        /**
         * Hiển thị thông báo lỗi
         * @param message Nội dung lỗi
         */
        void showError(String message);

        /**
         * Hiển thị trạng thái đang tải
         * @param isLoading true nếu đang tải, false nếu đã tải xong
         */
        void showLoading(boolean isLoading);

        /**
         * Hiển thị thông báo không có kết quả tìm kiếm
         */
        void showNoResults();
    }

    interface Presenter {
        /**
         * Khởi tạo Presenter
         * @param view View liên kết với Presenter này
         */
        void attachView(View view);

        /**
         * Hủy liên kết với View khi không cần thiết nữa
         */
        void detachView();

        /**
         * Lấy danh sách lịch sử tìm kiếm
         */
        void loadSearchHistory();

        /**
         * Tìm kiếm với từ khóa
         * @param keyword Từ khóa tìm kiếm
         */
        void search(String keyword);

        /**
         * Xóa một mục trong lịch sử tìm kiếm
         * @param searchHistoryId ID của lịch sử tìm kiếm cần xóa
         */
        void deleteSearchHistory(String searchHistoryId);

        /**
         * Xóa tất cả lịch sử tìm kiếm
         */
        void clearAllSearchHistory();
    }
}
