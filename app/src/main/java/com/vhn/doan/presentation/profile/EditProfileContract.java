package com.vhn.doan.presentation.profile;

import android.net.Uri;

import com.vhn.doan.data.User;
import com.vhn.doan.presentation.base.BasePresenter;
import com.vhn.doan.presentation.base.BaseView;

/**
 * Contract cho màn hình chỉnh sửa thông tin cá nhân
 */
public interface EditProfileContract {

    interface View extends BaseView {
        /**
         * Hiển thị thông tin người dùng vào các trường dữ liệu
         * @param user thông tin người dùng
         */
        void displayUserProfile(User user);

        /**
         * Hiển thị avatar người dùng
         * @param avatarUrl URL của avatar
         */
        void displayUserAvatar(String avatarUrl);

        /**
         * Hiển thị tiến trình tải avatar lên
         * @param progress Tiến trình (0-100)
         */
        void showAvatarUploadProgress(int progress);

        /**
         * Thông báo kết quả cập nhật thành công
         */
        void onProfileUpdateSuccess();

        /**
         * Hiển thị dialog thay đổi avatar với URI của ảnh đã chọn
         * @param imageUri URI của ảnh đã chọn
         */
        void showCroppedImage(Uri imageUri);
    }

    abstract class Presenter extends BasePresenter<View> {
        /**
         * Tải thông tin người dùng
         */
        public abstract void loadUserProfile();

        /**
         * Cập nhật avatar người dùng
         * @param imageUri URI của ảnh đã chọn
         */
        public abstract void updateUserAvatar(Uri imageUri);

        /**
         * Cập nhật thông tin cá nhân người dùng
         * @param displayName Tên hiển thị
         * @param phoneNumber Số điện thoại
         */
        public abstract void updateUserProfile(String displayName, String phoneNumber);
    }
}
