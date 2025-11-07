package com.vhn.doan.presentation.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Observer;

import com.google.android.material.snackbar.Snackbar;
import com.vhn.doan.R;
import com.vhn.doan.utils.FontSizeHelper;
import com.vhn.doan.utils.LocaleHelper;
import com.vhn.doan.utils.NetworkMonitor;
import com.vhn.doan.utils.SessionManager;

/**
 * Lớp Activity cơ sở cho tất cả các Activity trong ứng dụng
 * Cung cấp các chức năng chung và tuân thủ kiến trúc MVP
 */
public abstract class BaseActivity extends AppCompatActivity implements BaseView {

    protected SessionManager sessionManager;
    protected NetworkMonitor networkMonitor;
    private Snackbar networkSnackbar;
    private boolean wasConnected = true;

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

        // Khởi tạo NetworkMonitor
        networkMonitor = NetworkMonitor.getInstance(this);
        setupNetworkMonitoring();

        initializeActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật thời gian hoạt động cuối cùng
        if (sessionManager != null && sessionManager.isUserLoggedIn()) {
            sessionManager.updateLastActive();
        }

        // Bắt đầu theo dõi network
        if (networkMonitor != null) {
            networkMonitor.startMonitoring();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Ẩn snackbar khi pause
        if (networkSnackbar != null && networkSnackbar.isShown()) {
            networkSnackbar.dismiss();
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

    /**
     * Thiết lập theo dõi kết nối mạng
     */
    private void setupNetworkMonitoring() {
        networkMonitor.getConnectionStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isConnected) {
                handleNetworkChange(isConnected);
            }
        });
    }

    /**
     * Xử lý thay đổi kết nối mạng
     */
    private void handleNetworkChange(boolean isConnected) {
        if (!isConnected && wasConnected) {
            // Mất kết nối
            showNoInternetSnackbar();
            onNetworkDisconnected();
        } else if (isConnected && !wasConnected) {
            // Khôi phục kết nối
            hideNoInternetSnackbar();
            onNetworkConnected();
        }
        wasConnected = isConnected;
    }

    /**
     * Hiển thị Snackbar thông báo mất kết nối
     */
    private void showNoInternetSnackbar() {
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            networkSnackbar = Snackbar.make(
                    rootView,
                    getString(R.string.no_internet_connection),
                    Snackbar.LENGTH_INDEFINITE
            );

            // Tùy chỉnh màu sắc
            View snackbarView = networkSnackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#D32F2F")); // Màu đỏ
            networkSnackbar.setTextColor(Color.WHITE);

            // Thêm action để retry
            networkSnackbar.setAction(getString(R.string.retry), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (networkMonitor.isConnectedNow()) {
                        hideNoInternetSnackbar();
                        onNetworkConnected();
                    }
                }
            });
            networkSnackbar.setActionTextColor(Color.YELLOW);

            networkSnackbar.show();
        }
    }

    /**
     * Ẩn Snackbar thông báo mất kết nối
     */
    private void hideNoInternetSnackbar() {
        if (networkSnackbar != null && networkSnackbar.isShown()) {
            networkSnackbar.dismiss();
            networkSnackbar = null;

            // Hiển thị thông báo ngắn khi đã kết nối lại
            View rootView = findViewById(android.R.id.content);
            if (rootView != null) {
                Snackbar reconnectedSnackbar = Snackbar.make(
                        rootView,
                        getString(R.string.internet_connected),
                        Snackbar.LENGTH_SHORT
                );
                View snackbarView = reconnectedSnackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#388E3C")); // Màu xanh
                reconnectedSnackbar.setTextColor(Color.WHITE);
                reconnectedSnackbar.show();
            }
        }
    }

    /**
     * Callback khi mất kết nối mạng - override nếu cần xử lý riêng
     */
    protected void onNetworkDisconnected() {
        // Override trong Activity con nếu cần
    }

    /**
     * Callback khi kết nối lại mạng - override nếu cần xử lý riêng
     */
    protected void onNetworkConnected() {
        // Override trong Activity con nếu cần
    }

    /**
     * Kiểm tra kết nối mạng hiện tại
     */
    protected boolean isNetworkConnected() {
        return networkMonitor != null && networkMonitor.isConnectedNow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cleanup Snackbar
        if (networkSnackbar != null) {
            networkSnackbar.dismiss();
            networkSnackbar = null;
        }

        // Cleanup resources nếu cần
    }
}
