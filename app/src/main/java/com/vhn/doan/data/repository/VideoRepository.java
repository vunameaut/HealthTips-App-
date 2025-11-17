package com.vhn.doan.data.repository;

import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.VideoComment;

import java.util.List;

/**
 * Interface VideoRepository định nghĩa các phương thức để truy cập và quản lý dữ liệu video ngắn
 * Tuân theo nguyên tắc thiết kế repository trong kiến trúc MVP
 */
public interface VideoRepository {

    /**
     * Interface callback để nhận kết quả từ các thao tác async
     */
    interface VideoCallback {
        /**
         * Được gọi khi thao tác thành công
         * @param videos danh sách các video ngắn
         */
        void onSuccess(List<ShortVideo> videos);

        /**
         * Được gọi khi thao tác thất bại
         * @param errorMessage thông báo lỗi
         */
        void onError(String errorMessage);
    }

    /**
     * Interface callback cho single video
     */
    interface SingleVideoCallback {
        void onSuccess(ShortVideo video);
        void onError(String errorMessage);
    }

    /**
     * Interface callback cho comments
     */
    interface CommentsCallback {
        void onSuccess(List<VideoComment> comments);
        void onError(String errorMessage);
    }

    /**
     * Interface callback cho các thao tác boolean (like/unlike)
     */
    interface BooleanCallback {
        void onSuccess(boolean result);
        void onError(String errorMessage);
    }

    /**
     * Interface callback cho comment operations
     */
    interface CommentCallback {
        void onSuccess(VideoComment comment);
        void onError(String errorMessage);
    }

    // ==================== VIDEO OPERATIONS ====================

    /**
     * Lấy feed video cho người dùng dựa trên preferences và trending
     * @param userId ID của người dùng để lấy preferences
     * @param country Quốc gia để lấy trending videos
     * @param callback Callback để nhận kết quả
     */
    void getFeed(String userId, String country, VideoCallback callback);

    /**
     * Lấy feed video đồng bộ (blocking)
     * @param userId ID của người dùng để lấy preferences
     * @param country Quốc gia để lấy trending videos
     * @return Danh sách video đã được sắp xếp
     * @deprecated Khuyên dùng phương thức async với callback
     */
    @Deprecated
    List<ShortVideo> getFeed(String userId, String country);

    /**
     * Lấy thông tin chi tiết của một video
     * @param videoId ID của video
     * @param callback Callback để nhận kết quả
     */
    void getVideoById(String videoId, SingleVideoCallback callback);

    /**
     * Tăng view count cho video
     * @param videoId ID của video
     * @param callback Callback để nhận kết quả
     */
    void incrementViewCount(String videoId, BooleanCallback callback);

    // ==================== LIKE OPERATIONS ====================

    /**
     * Like một video
     * @param videoId ID của video
     * @param userId ID của người dùng
     * @param callback Callback để nhận kết quả
     */
    void likeVideo(String videoId, String userId, BooleanCallback callback);

    /**
     * Unlike một video
     * @param videoId ID của video
     * @param userId ID của người dùng
     * @param callback Callback để nhận kết quả
     */
    void unlikeVideo(String videoId, String userId, BooleanCallback callback);

    /**
     * Kiểm tra user đã like video hay chưa
     * @param videoId ID của video
     * @param userId ID của người dùng
     * @param callback Callback để nhận kết quả
     */
    void isVideoLiked(String videoId, String userId, BooleanCallback callback);

    /**
     * Lấy danh sách video đã like của user
     * @param userId ID của người dùng
     * @param callback Callback để nhận kết quả
     */
    void getLikedVideos(String userId, VideoCallback callback);

    /**
     * Lắng nghe realtime thay đổi like status của video
     * @param videoId ID của video
     * @param userId ID của người dùng
     * @param callback Callback để nhận cập nhật
     */
    void listenToVideoLikeStatus(String videoId, String userId, BooleanCallback callback);

    // ==================== COMMENT OPERATIONS ====================

    /**
     * Lấy danh sách comment gốc của video (parentId = null)
     * @param videoId ID của video
     * @param callback Callback để nhận kết quả
     */
    void getVideoComments(String videoId, CommentsCallback callback);

    /**
     * Lấy danh sách reply của một comment
     * @param videoId ID của video
     * @param parentCommentId ID của comment cha
     * @param callback Callback để nhận kết quả
     */
    void getCommentReplies(String videoId, String parentCommentId, CommentsCallback callback);

    /**
     * Thêm comment mới vào video
     * @param videoId ID của video
     * @param userId ID của người dùng
     * @param text Nội dung comment
     * @param parentId ID của comment cha (null nếu là comment gốc)
     * @param callback Callback để nhận kết quả
     */
    void addComment(String videoId, String userId, String text, String parentId, CommentCallback callback);

    /**
     * Like một comment
     * @param videoId ID của video
     * @param commentId ID của comment
     * @param userId ID của người dùng
     * @param callback Callback để nhận kết quả
     */
    void likeComment(String videoId, String commentId, String userId, BooleanCallback callback);

    /**
     * Unlike một comment
     * @param videoId ID của video
     * @param commentId ID của comment
     * @param userId ID của người dùng
     * @param callback Callback để nhận kết quả
     */
    void unlikeComment(String videoId, String commentId, String userId, BooleanCallback callback);

    /**
     * Kiểm tra user đã like comment hay chưa
     * @param videoId ID của video
     * @param commentId ID của comment
     * @param userId ID của người dùng
     * @param callback Callback để nhận kết quả
     */
    void isCommentLiked(String videoId, String commentId, String userId, BooleanCallback callback);

    /**
     * Lắng nghe realtime thay đổi của comments
     * @param videoId ID của video
     * @param callback Callback để nhận cập nhật
     */
    void listenToVideoComments(String videoId, CommentsCallback callback);

    /**
     * Lắng nghe realtime thay đổi của replies
     * @param videoId ID của video
     * @param parentCommentId ID của comment cha
     * @param callback Callback để nhận cập nhật
     */
    void listenToCommentReplies(String videoId, String parentCommentId, CommentsCallback callback);

    // ==================== PERSONALIZATION & TRACKING ====================

    /**
     * 🎯 Track video view for TikTok-style personalization
     * Lưu video vào danh sách đã xem để không hiển thị lại
     * @param videoId ID của video
     * @param userId ID của người dùng
     */
    void trackVideoView(String videoId, String userId);

    /**
     * 🎯 Track user interaction for learning algorithm
     * Lưu tương tác để cải thiện đề xuất
     * @param videoId ID của video
     * @param userId ID của người dùng
     * @param interactionType Loại tương tác: "view", "like", "comment", "share"
     * @param watchTimeMs Thời gian xem (milliseconds)
     */
    void trackVideoInteraction(String videoId, String userId, String interactionType, long watchTimeMs);

    // ==================== CLEANUP OPERATIONS ====================

    /**
     * Dọn dẹp các listener ��ể tránh memory leak
     */
    void cleanup();
}
