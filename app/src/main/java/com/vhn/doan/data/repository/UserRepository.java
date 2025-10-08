package com.vhn.doan.data.repository;

import com.vhn.doan.data.User;

/**
 * Interface UserRepository định nghĩa các phương thức để quản lý thông tin người dùng
 * trong Firebase Realtime Database
 */
public interface UserRepository {

    /**
     * Interface callback để xử lý kết quả thao tác với User
     */
    interface UserCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
    }

    /**
     * Interface callback cho các thao tác không trả về dữ liệu
     */
    interface UserOperationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    /**
     * Lưu thông tin người dùng khi đăng ký
     * @param user đối tượng User chứa thông tin người dùng
     * @param callback callback để nhận kết quả
     */
    void saveUser(User user, UserOperationCallback callback);

    /**
     * Lấy thông tin người dùng theo UID
     * @param uid UID của người dùng
     * @param callback callback để nhận kết quả
     */
    void getUserByUid(String uid, UserCallback callback);

    /**
     * Cập nhật thông tin người dùng
     * @param user đối tượng User với thông tin đã cập nhật
     * @param callback callback để nhận kết quả
     */
    void updateUser(User user, UserOperationCallback callback);

    /**
     * Cập nhật thời gian đăng nhập cuối
     * @param uid UID của người dùng
     * @param callback callback để nhận kết quả
     */
    void updateLastLogin(String uid, UserOperationCallback callback);

    /**
     * Thêm bài viết vào danh sách yêu thích của người dùng
     * @param uid UID của người dùng
     * @param healthTipId ID của bài viết
     * @param callback callback để nhận kết quả
     */
    void addFavoriteHealthTip(String uid, String healthTipId, UserOperationCallback callback);

    /**
     * Xóa bài viết khỏi danh sách yêu thích của người dùng
     * @param uid UID của người dùng
     * @param healthTipId ID của bài viết
     * @param callback callback để nhận kết quả
     */
    void removeFavoriteHealthTip(String uid, String healthTipId, UserOperationCallback callback);

    /**
     * Thêm bài viết vào danh sách đã thích của người dùng
     * @param uid UID của người dùng
     * @param healthTipId ID của bài viết
     * @param callback callback để nhận kết quả
     */
    void addLikedHealthTip(String uid, String healthTipId, UserOperationCallback callback);

    /**
     * Xóa bài viết khỏi danh sách đã thích của người dùng
     * @param uid UID của người dùng
     * @param healthTipId ID của bài viết
     * @param callback callback để nhận kết quả
     */
    void removeLikedHealthTip(String uid, String healthTipId, UserOperationCallback callback);

    /**
     * Cập nhật tùy chọn của người dùng
     * @param uid UID của người dùng
     * @param key key của tùy chọn
     * @param value giá trị của tùy chọn
     * @param callback callback để nhận kết quả
     */
    void updateUserPreference(String uid, String key, Object value, UserOperationCallback callback);

    /**
     * Cập nhật ảnh đại diện của người dùng
     * @param uid UID của người dùng
     * @param photoUrl URL của ảnh đại diện mới
     * @param callback callback để nhận kết quả
     */
    void updateUserAvatar(String uid, String photoUrl, UserOperationCallback callback);

    /**
     * Cập nhật thông tin cá nhân của người dùng
     * @param uid UID của người dùng
     * @param displayName Tên hiển thị mới
     * @param bio Giới thiệu bản thân mới
     * @param phoneNumber Số điện thoại mới
     * @param callback callback để nhận kết quả
     */
    void updateUserProfile(String uid, String displayName, String bio, String phoneNumber, UserOperationCallback callback);
}
