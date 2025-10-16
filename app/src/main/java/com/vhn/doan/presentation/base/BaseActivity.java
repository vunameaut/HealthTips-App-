package com.vhn.doan.presentation.base;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Lớp Activity cơ sở cho tất cả các Activity trong ứng dụng
 * Cung cấp các chức năng chung và tuân thủ kiến trúc MVP
 */
public abstract class BaseActivity extends AppCompatActivity implements BaseView {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeActivity();
    }

    /**
     * Khởi tạo các thành phần cơ bản của Activity
     */
    protected void initializeActivity() {
        // Override trong các Activity con nếu cần
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showLoading() {
        // Hiển thị loading indicator
        // Có thể implement với ProgressBar hoặc Dialog
    }

    @Override
    public void hideLoading() {
        // Ẩn loading indicator
    }

    @Override
    public void showError(String message) {
        // Hiển thị thông báo lỗi
        // Có thể sử dụng Toast, Snackbar hoặc Dialog
    }

    @Override
    public void showMessage(String message) {
        // Hiển thị thông báo thông thường
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup resources nếu cần
    }
}
