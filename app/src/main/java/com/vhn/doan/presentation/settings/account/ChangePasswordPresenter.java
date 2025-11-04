package com.vhn.doan.presentation.settings.account;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Presenter cho màn hình đổi mật khẩu
 */
public class ChangePasswordPresenter implements ChangePasswordContract.Presenter {

    private static final String TAG = "ChangePasswordPresenter";
    private static final int MIN_PASSWORD_LENGTH = 6;

    private ChangePasswordContract.View view;
    private final FirebaseAuth firebaseAuth;

    public ChangePasswordPresenter() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void attachView(ChangePasswordContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void validatePasswords(String currentPassword, String newPassword, String confirmPassword) {
        // Validate current password
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            if (view != null) {
                view.showInvalidCurrentPassword();
            }
            return;
        }

        // Validate new password
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            if (view != null) {
                view.showWeakNewPassword();
            }
            return;
        }

        // Validate password match
        if (!newPassword.equals(confirmPassword)) {
            if (view != null) {
                view.showPasswordMismatch();
            }
            return;
        }

        // All validations passed, proceed to change password
        changePassword(currentPassword, newPassword, confirmPassword);
    }

    @Override
    public void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        if (view != null) {
            view.showLoading(true);
        }

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            if (view != null) {
                view.showLoading(false);
                view.showPasswordChangeError("Không tìm thấy thông tin người dùng");
            }
            return;
        }

        // Re-authenticate user before changing password (Firebase requirement)
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential)
            .addOnCompleteListener(reauthTask -> {
                if (reauthTask.isSuccessful()) {
                    // Re-authentication successful, now update password
                    user.updatePassword(newPassword)
                        .addOnCompleteListener(updateTask -> {
                            if (view != null) {
                                view.showLoading(false);
                            }

                            if (updateTask.isSuccessful()) {
                                Log.d(TAG, "Password updated successfully");
                                if (view != null) {
                                    view.showPasswordChangedSuccess();
                                    view.navigateBack();
                                }
                            } else {
                                Log.e(TAG, "Password update failed", updateTask.getException());
                                String errorMessage = updateTask.getException() != null
                                    ? updateTask.getException().getMessage()
                                    : "Không thể cập nhật mật khẩu";
                                if (view != null) {
                                    view.showPasswordChangeError(errorMessage);
                                }
                            }
                        });
                } else {
                    // Re-authentication failed (current password is wrong)
                    Log.e(TAG, "Re-authentication failed", reauthTask.getException());
                    if (view != null) {
                        view.showLoading(false);
                        view.showInvalidCurrentPassword();
                    }
                }
            });
    }
}
