package com.vhn.doan.presentation.auth;

/**
 * AuthView là giao diện định nghĩa các phương thức hiển thị UI cho việc xác thực người dùng
 * Giao diện này thuộc thành phần View trong mẫu MVP (Model-View-Presenter)
 */
public interface AuthView {

    /**
     * Hiển thị trạng thái đang tải khi xử lý quá trình xác thực
     * @param loading true nếu đang tải, false nếu kết thúc tải
     */
    void showLoading(boolean loading);

    /**
     * Hiển thị thông báo lỗi khi xác thực thất bại
     * @param errorMessage Nội dung thông báo lỗi
     */
    void showError(String errorMessage);

    /**
     * Được gọi khi đăng nhập thành công
     * @param userId ID của người dùng đã đăng nhập
     */
    void onLoginSuccess(String userId);

    /**
     * Được gọi khi đăng ký thành công
     * @param userId ID của người dùng mới đăng ký
     */
    void onRegistrationSuccess(String userId);

    /**
     * Được gọi khi đăng xuất thành công
     */
    void onLogoutSuccess();

    /**
     * Hiển thị thông báo để thông tin cho người dùng
     * @param message Nội dung thông báo
     */
    void showMessage(String message);
}
