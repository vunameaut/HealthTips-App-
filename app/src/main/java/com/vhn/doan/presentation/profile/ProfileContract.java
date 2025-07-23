package com.vhn.doan.presentation.profile;

import com.vhn.doan.presentation.base.BasePresenter;
import com.vhn.doan.presentation.base.BaseView;

/**
 * Contract interface cho Profile Module
 * Định nghĩa các phương thức giao tiếp giữa View và Presenter
 */
public interface ProfileContract {

    interface View extends BaseView {
        /**
         * Hiển thị thông tin profile người dùng
         * @param userInfo Thông tin người dùng dạng string
         */
        void displayUserProfile(String userInfo);

        /**
         * Xử lý khi đăng xuất thành công
         */
        void onLogoutSuccess();
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
    }
}
