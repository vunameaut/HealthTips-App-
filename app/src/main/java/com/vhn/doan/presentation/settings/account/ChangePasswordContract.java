package com.vhn.doan.presentation.settings.account;

import com.vhn.doan.presentation.base.BaseView;

/**
 * Contract cho màn hình đổi mật khẩu
 */
public interface ChangePasswordContract {

    interface View extends BaseView {
        void showPasswordChangedSuccess();
        void showPasswordChangeError(String error);
        void showInvalidCurrentPassword();
        void showWeakNewPassword();
        void showPasswordMismatch();
        void showLoading(boolean isLoading);
        void navigateBack();
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void changePassword(String currentPassword, String newPassword, String confirmPassword);
        void validatePasswords(String currentPassword, String newPassword, String confirmPassword);
    }
}
