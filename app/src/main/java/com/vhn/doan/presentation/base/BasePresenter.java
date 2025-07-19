package com.vhn.doan.presentation.base;

/**
 * BasePresenter định nghĩa các phương thức cơ bản mà tất cả các Presenter trong ứng dụng nên thừa kế
 * Tuân theo kiến trúc MVP (Model-View-Presenter)
 *
 * @param <V> Loại View mà Presenter này liên kết với
 */
public abstract class BasePresenter<V extends BaseView> {
    protected V view;

    /**
     * Gắn View với Presenter
     * @param view View cần gắn
     */
    public void attachView(V view) {
        this.view = view;
    }

    /**
     * Tách View khỏi Presenter để tránh memory leak
     */
    public void detachView() {
        this.view = null;
    }

    /**
     * Kiểm tra xem View có đang được gắn không
     * @return true nếu View đang được gắn, false nếu không
     */
    public boolean isViewAttached() {
        return view != null;
    }

    /**
     * Phương thức được gọi khi Presenter bắt đầu hoạt động
     * Nên ghi đè phương thức này để thực hiện các tác vụ khởi tạo
     */
    public void start() {
        // Có thể ghi đè trong các lớp con
    }

    /**
     * Phương thức được gọi khi Presenter kết thúc hoạt động
     * Nên ghi đè phương thức này để dọn dẹp tài nguyên
     */
    public void stop() {
        // Có thể ghi đè trong các lớp con
    }
}
