package com.vhn.doan.presentation.base;

/**
 * Base interface cho tất cả View trong kiến trúc MVP
 * Tương thích với cách sử dụng hiện có trong dự án
 */
public interface BaseView {

    /**
     * Hiển thị loading
     */
    default void showLoading() {
        // Default implementation - có thể override trong các view con
    }

    /**
     * Ẩn loading
     */
    default void hideLoading() {
        // Default implementation - có thể override trong các view con
    }

    /**
     * Hiển thị thông báo lỗi
     */
    default void showError(String message) {
        // Default implementation - có thể override trong các view con
    }

    /**
     * Hiển thị thông báo (compatibility method)
     */
    default void showMessage(String message) {
        // Default implementation - có thể override trong các view con
    }

    /**
     * Hiển thị loading với tham số boolean (compatibility method)
     */
    default void showLoading(boolean loading) {
        if (loading) {
            showLoading();
        } else {
            hideLoading();
        }
    }
}
