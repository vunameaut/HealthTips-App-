package com.vhn.doan.presentation.profile;

import android.net.Uri;

import com.vhn.doan.data.User;
import com.vhn.doan.presentation.base.BasePresenter;
import com.vhn.doan.presentation.base.BaseView;

/**
 * Contract interface cho Profile Module
 * Định nghĩa các phương thức giao tiếp giữa View và Presenter
 */
public interface ProfileContract {

    /**
     * Callback khi lấy avatar URL
     */
    interface AvatarCallback {
        void onAvatarLoaded(String avatarUrl);
    }

    interface View extends BaseView {
        /**
         * Hiển thị thông tin profile người dùng
         * @param user Đối tượng User chứa thông tin người dùng
         */
        void displayUserProfile(User user);

        /**
         * Hiển thị avatar người dùng
         * @param avatarUrl URL của avatar
         * @param isDefault true nếu là avatar mặc định
         */
        void displayUserAvatar(String avatarUrl, boolean isDefault);

        /**
         * Xử lý khi đăng xuất thành công
         */
        void onLogoutSuccess();

        /**
         * Hiển thị tiến trình tải avatar lên
         * @param progress Tiến trình tải lên (0-100)
         */
        void showAvatarUploadProgress(int progress);

        /**
         * Hiển thị kết quả cập nhật thông tin cá nhân
         * @param success true nếu cập nhật thành công
         * @param message Thông báo kết quả
         */
        void showProfileUpdateResult(boolean success, String message);
    }

    abstract class Presenter extends BasePresenter<View> {
        /**
         * Tải thông tin profile người dùng
         */
        public abstract void loadUserProfile();

        /**
         * Xử lý đăng xuất
         */
        public abstract void logout();

        /**
         * Refresh thông tin người dùng
         */
        public abstract void refreshUserProfile();

        /**
         * Cập nhật avatar người dùng
         * @param imageUri Uri của ảnh được chọn
         */
        public abstract void updateUserAvatar(Uri imageUri);

        /**
         * Cập nhật thông tin cá nhân của người dùng
         * @param displayName Tên hiển thị mới
         * @param bio Giới thiệu bản thân mới
         * @param phoneNumber Số điện thoại mới
         */
        public abstract void updateUserProfile(String displayName, String bio, String phoneNumber);

        /**
         * Xem avatar hiện tại ở kích thước lớn hơn
         */
        public abstract void viewFullAvatar();

        /**
         * Lấy URL avatar hiện tại của người dùng
         * @param callback Callback để trả về URL avatar
         */
        public abstract void getCurrentUserAvatar(AvatarCallback callback);
    }
}
