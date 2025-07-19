package com.vhn.doan.presentation.base;

/**
 * BaseView định nghĩa các phương thức cơ bản mà tất cả các View trong ứng dụng nên implement
 * Tuân theo kiến trúc MVP (Model-View-Presenter)
 */
public interface BaseView {
    /**
     * Hiển thị thông báo cho người dùng
     * @param message Nội dung thông báo
     */
    void showMessage(String message);

    /**
     * Hiển thị hoặc ẩn trạng thái đang tải
     * @param loading true để hiển thị loading, false để ẩn
     */
    void showLoading(boolean loading);

    /**
     * Hiển thị thông báo lỗi
     * @param errorMessage Thông báo lỗi
     */
    void showError(String errorMessage);
}
