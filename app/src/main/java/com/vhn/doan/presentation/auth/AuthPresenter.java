package com.vhn.doan.presentation.auth;

import android.content.Context;

import com.vhn.doan.services.AuthManager;

/**
 * AuthPresenter là lớp presenter trong mô hình MVP, xử lý logic cho các hoạt động xác thực
 * Lớp này kết nối AuthView (giao diện người dùng) và AuthManager (xử lý backend)
 */
public class AuthPresenter {

    private final AuthView view;
    private final AuthManager authManager;
    private final Context context;

    /**
     * Khởi tạo AuthPresenter
     * @param view Giao diện AuthView để cập nhật UI
     * @param context Context của ứng dụng
     */
    public AuthPresenter(AuthView view, Context context) {
        this.view = view;
        this.context = context;
        this.authManager = new AuthManager(context);
    }

    /**
     * Thực hiện đăng nhập bằng email và mật khẩu
     * @param email Email người dùng
     * @param password Mật khẩu người dùng
     */
    public void login(String email, String password) {
        if (!validateLoginInput(email, password)) {
            return;
        }

        view.showLoading(true);

        authManager.loginWithEmailPassword(email, password, (isSuccess, userId, errorMessage) -> {
            view.showLoading(false);

            if (isSuccess) {
                view.onLoginSuccess(userId);
            } else {
                view.showError(errorMessage);
            }
        });
    }

    /**
     * Thực hiện đăng ký tài khoản mới
     * @param email Email người dùng
     * @param password Mật khẩu người dùng
     * @param confirmPassword Xác nhận mật khẩu
     */
    public void register(String email, String password, String confirmPassword) {
        if (!validateRegistrationInput(email, password, confirmPassword)) {
            return;
        }

        view.showLoading(true);

        authManager.registerWithEmailPassword(email, password, (isSuccess, userId, errorMessage) -> {
            view.showLoading(false);

            if (isSuccess) {
                view.onRegistrationSuccess(userId);
            } else {
                view.showError(errorMessage);
            }
        });
    }

    /**
     * Thực hiện đăng xuất người dùng hiện tại
     */
    public void logout() {
        authManager.logout();
        view.onLogoutSuccess();
    }

    /**
     * Kiểm tra người dùng đã đăng nhập hay chưa
     * @return true nếu người dùng đã đăng nhập
     */
    public boolean isUserLoggedIn() {
        return authManager.isUserLoggedIn();
    }

    /**
     * Lấy ID của người dùng hiện tại
     * @return ID người dùng hoặc null nếu chưa đăng nhập
     */
    public String getCurrentUserId() {
        return authManager.getCurrentUserId();
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu đăng nhập
     * @param email Email cần kiểm tra
     * @param password Mật khẩu cần kiểm tra
     * @return true nếu dữ liệu hợp lệ, false nếu không hợp lệ
     */
    private boolean validateLoginInput(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            view.showError("Email không được để trống");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showError("Email không hợp lệ");
            return false;
        }

        if (password == null || password.isEmpty()) {
            view.showError("Mật khẩu không được để trống");
            return false;
        }

        if (password.length() < 6) {
            view.showError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }

        return true;
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu đăng ký
     * @param email Email cần kiểm tra
     * @param password Mật khẩu cần kiểm tra
     * @param confirmPassword Xác nhận mật khẩu cần kiểm tra
     * @return true nếu dữ liệu hợp lệ, false nếu không hợp lệ
     */
    private boolean validateRegistrationInput(String email, String password, String confirmPassword) {
        if (!validateLoginInput(email, password)) {
            return false;
        }

        if (!password.equals(confirmPassword)) {
            view.showError("Xác nhận mật khẩu không khớp");
            return false;
        }

        return true;
    }
}
