package com.vhn.doan.presentation.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Base Fragment cho tất cả Fragment trong ứng dụng
 * Cung cấp các phương thức chung và lifecycle management
 */
public abstract class BaseFragment extends Fragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupListeners();
    }

    /**
     * Khởi tạo các View components
     * @param view Root view của fragment
     */
    protected abstract void initViews(View view);

    /**
     * Thiết lập các event listeners
     */
    protected abstract void setupListeners();

    /**
     * Hiển thị loading state
     */
    public void showLoading() {
        // Override trong các fragment con nếu cần
    }

    /**
     * Ẩn loading state
     */
    public void hideLoading() {
        // Override trong các fragment con nếu cần
    }

    /**
     * Hiển thị thông báo lỗi
     */
    public void showError(String message) {
        // Override trong các fragment con nếu cần
    }
}
