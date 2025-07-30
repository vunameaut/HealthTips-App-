package com.vhn.doan.presentation.base;

/**
 * Base class cho tất cả Presenter trong kiến trúc MVP
 * Hỗ trợ generic type cho View
 */
public abstract class BasePresenter<V extends BaseView> {

    protected V view;

    /**
     * Gắn view vào presenter
     */
    public void attachView(V view) {
        this.view = view;
    }

    /**
     * Gỡ bỏ view khỏi presenter
     */
    public void detachView() {
        this.view = null;
    }

    /**
     * Kiểm tra xem view có được gắn vào hay không
     */
    public boolean isViewAttached() {
        return view != null;
    }

    /**
     * Lấy view hiện tại
     */
    public V getView() {
        return view;
    }

    /**
     * Phương thức khởi tạo - override trong các presenter con
     */
    public abstract void start();
}
