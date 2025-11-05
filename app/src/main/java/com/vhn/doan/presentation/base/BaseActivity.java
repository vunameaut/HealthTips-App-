package com.vhn.doan.presentation.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.vhn.doan.utils.FontSizeHelper;
import com.vhn.doan.utils.LocaleHelper;
import com.vhn.doan.utils.SessionManager;

/**
 * Lớp Activity cơ sở cho tất cả các Activity trong ứng dụng
 * Cung cấp các chức năng chung và tuân thủ kiến trúc MVP
 */
public abstract class BaseActivity extends AppCompatActivity implements BaseView {

    protected SessionManager sessionManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        // Áp dụng locale (ngôn ngữ)
        Context localeContext = LocaleHelper.onAttach(newBase);

        // Áp dụng font size
        Context finalContext = FontSizeHelper.applyFontSize(localeContext);

        super.attachBaseContext(finalContext);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Áp dụng theme mode trước khi super.onCreate()
        applyThemeMode();

        // Áp dụng secure mode (chặn screenshot) nếu được bật
        applySecureMode();

        super.onCreate(savedInstanceState);

        // Khởi tạo SessionManager
        sessionManager = new SessionManager(this);

        initializeActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật thời gian hoạt động cuối cùng
        if (sessionManager != null && sessionManager.isUserLoggedIn()) {
            sessionManager.updateLastActive();
        }
    }

    /**
     * Áp dụng theme mode từ SharedPreferences
     */
    private void applyThemeMode() {
        SharedPreferences prefs = getSharedPreferences("DisplaySettings", MODE_PRIVATE);
        String themeMode = prefs.getString("theme_mode", "system");

        int nightMode;
        switch (themeMode) {
            case "light":
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case "dark":
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case "system":
            default:
                nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }

        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    /**
     * Áp dụng secure mode - chặn screenshot và screen recording
     */
    private void applySecureMode() {
        SharedPreferences securityPrefs = getSharedPreferences("SecuritySettings", MODE_PRIVATE);
        boolean secureModeEnabled = securityPrefs.getBoolean("secure_mode_enabled", false);

        if (secureModeEnabled) {
            // Chặn screenshot và screen recording
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
            );
        } else {
            // Cho phép screenshot và screen recording
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
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
