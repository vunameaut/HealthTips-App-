package com.vhn.doan.data.repository;

import com.vhn.doan.data.ShortVideo;
import java.util.List;

/**
 * Repository interface để quản lý dữ liệu video ngắn
 */
public interface ShortVideoRepository {

    /**
     * Lấy danh sách video được đề xuất cho người dùng
     * @param userId ID của người dùng
     * @param limit Số lượng video tối đa cần lấy
     * @param callback Callback để xử lý kết quả
     */
    void getRecommendedVideos(String userId, int limit, RepositoryCallback<List<ShortVideo>> callback);

    /**
     * Lấy danh sách video theo categoryId
     * @param categoryId ID của danh mục
     * @param limit Số lượng video tối đa cần lấy
     * @param callback Callback để xử lý kết quả
     */
    void getVideosByCategory(String categoryId, int limit, RepositoryCallback<List<ShortVideo>> callback);

    /**
     * Lấy danh sách video trending
     * @param limit Số lượng video tối đa cần lấy
     * @param callback Callback để xử lý kết quả
     */
    void getTrendingVideos(int limit, RepositoryCallback<List<ShortVideo>> callback);

    /**
     * Lấy danh sách video mà người dùng đã thích
     * @param userId ID của người dùng
     * @param limit Số lượng video tối đa cần lấy
     * @param callback Callback xử lý kết quả
     */
    void getLikedVideos(String userId, int limit, RepositoryCallback<List<ShortVideo>> callback);

    /**
     * Tăng view count cho video
     * @param videoId ID của video
     * @param callback Callback để xử lý kết quả
     */
    void incrementViewCount(String videoId, RepositoryCallback<Void> callback);

    /**
     * Tăng/giảm like count cho video
     * @param videoId ID của video
     * @param isLiked true nếu like, false nếu unlike
     * @param callback Callback để xử lý kết quả
     */
    void updateLikeCount(String videoId, boolean isLiked, RepositoryCallback<Void> callback);

    /**
     * Thêm bình luận cho video
     * @param videoId ID của video
     * @param comment Đối tượng bình luận
     * @param callback Callback xử lý kết quả
     */
    void addComment(String videoId, com.vhn.doan.data.VideoComment comment, RepositoryCallback<Void> callback);

    /**
     * Lấy danh sách bình luận của video
     * @param videoId ID của video
     * @param callback Callback xử lý kết quả
     */
    void getComments(String videoId, RepositoryCallback<java.util.List<com.vhn.doan.data.VideoComment>> callback);

    /**
     * Thích hoặc bỏ thích một bình luận hoặc phản hồi
     * @param videoId ID video chứa bình luận
     * @param commentPath Đường dẫn tới bình luận (ví dụ: commentId hoặc commentId/replies/replyId)
     * @param userId ID người dùng thực hiện
     * @param like true nếu thích, false nếu bỏ thích
     * @param callback Callback xử lý kết quả
     */
    void likeComment(String videoId, String commentPath, String userId, boolean like, RepositoryCallback<Void> callback);

    /**
     * Thêm phản hồi cho một bình luận
     * @param videoId ID video chứa bình luận
     * @param commentId ID của bình luận cha
     * @param reply Đối tượng bình luận phản hồi
     * @param callback Callback xử lý kết quả
     */
    void addReply(String videoId, String commentId, com.vhn.doan.data.VideoComment reply, RepositoryCallback<Void> callback);

    /**
     * Lấy sở thích người dùng từ preferences và user_topics
     * @param userId ID của người dùng
     * @param callback Callback để xử lý kết quả
     */
    void getUserPreferences(String userId, RepositoryCallback<java.util.Map<String, Float>> callback);
}
