package com.vhn.doan.presentation.base;

/**
 * BasePresenter interface định nghĩa các phương thức cơ bản cho tất cả Presenter
 * Tuân theo kiến trúc MVP (Model-View-Presenter)
 */
public interface BasePresenterInterface {

    /**
     * Gắn View với Presenter
     * @param view View cần gắn
     */
    void attachView(BaseView view);

    /**
     * Tách View khỏi Presenter để tránh memory leak
     */
    void detachView();

    /**
     * Phương thức được gọi khi Presenter bắt đầu hoạt động
     */
    void start();

    /**
     * Phương thức được gọi khi Presenter kết thúc hoạt động
     */
    void stop();
}
