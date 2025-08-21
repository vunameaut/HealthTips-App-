package com.vhn.doan.data.repository;

import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.SearchHistory;
import com.vhn.doan.data.ShortVideo;

import java.util.List;

/**
 * Interface định nghĩa các thao tác liên quan đến tìm kiếm
 */
public interface SearchRepository {
    /**
     * Tìm kiếm bài viết (HealthTip) theo từ khóa
     * @param keyword Từ khóa tìm kiếm
     * @param callback Callback nhận kết quả tìm kiếm
     */
    void searchHealthTips(String keyword, RepositoryCallback<List<HealthTip>> callback);

    /**
     * Tìm kiếm video theo từ khóa
     * @param keyword Từ khóa tìm kiếm
     * @param callback Callback nhận kết quả tìm kiếm
     */
    void searchVideos(String keyword, RepositoryCallback<List<ShortVideo>> callback);

    /**
     * Lưu lịch sử tìm kiếm
     * @param keyword Từ khóa đã tìm kiếm
     * @param userId ID người dùng
     * @param callback Callback báo kết quả lưu
     */
    void saveSearchHistory(String keyword, String userId, RepositoryCallback<Boolean> callback);

    /**
     * Lấy danh sách lịch sử tìm kiếm của người dùng
     * @param userId ID người dùng
     * @param limit Giới hạn số lượng kết quả trả về
     * @param callback Callback nhận danh sách lịch sử tìm kiếm
     */
    void getSearchHistory(String userId, int limit, RepositoryCallback<List<SearchHistory>> callback);

    /**
     * Xóa một lịch sử tìm kiếm
     * @param searchHistoryId ID của lịch sử tìm kiếm cần xóa
     * @param userId ID người dùng
     * @param callback Callback báo kết quả xóa
     */
    void deleteSearchHistory(String searchHistoryId, String userId, RepositoryCallback<Boolean> callback);

    /**
     * Xóa tất cả lịch sử tìm kiếm của người dùng
     * @param userId ID người dùng
     * @param callback Callback báo kết quả xóa
     */
    void clearAllSearchHistory(String userId, RepositoryCallback<Boolean> callback);

    /**
     * Lưu từ khóa vào danh sách từ khóa đề xuất cho người dùng
     * @param keyword Từ khóa cần lưu
     * @param userId ID người dùng
     */
    void saveKeywordForSuggestion(String keyword, String userId);
}
