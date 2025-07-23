package com.vhn.doan.presentation.base;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * BaseFragment chứa các phương thức chung cho tất cả Fragment trong ứng dụng
 * Implement BaseView để tuân thủ kiến trúc MVP
 */
public abstract class BaseFragment extends Fragment implements BaseView {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupListeners();
    }

    /**
     * Khởi tạo các view components
     * @param view Root view của fragment
     */
    protected abstract void initViews(View view);

    /**
     * Thiết lập các event listeners
     */
    protected abstract void setupListeners();

    // Implementation của BaseView interface
    @Override
    public void showMessage(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showLoading(boolean loading) {
        // TODO: Implement loading indicator
        // Có thể override trong các Fragment con để implement loading UI cụ thể
    }

    @Override
    public void showError(String errorMessage) {
        if (getContext() != null) {
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Convenience method để hiển thị loading
     */
    public void showLoading() {
        showLoading(true);
    }

    /**
     * Convenience method để ẩn loading
     */
    public void hideLoading() {
        showLoading(false);
    }
}
