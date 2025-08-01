package com.vhn.doan.presentation.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.vhn.doan.R;
import com.vhn.doan.presentation.auth.LoginActivity;
import com.vhn.doan.presentation.favorite.FavoriteFragment;
import com.vhn.doan.presentation.profile.ProfileFragment;
import com.vhn.doan.presentation.reminder.ReminderFragment;
import com.vhn.doan.services.AuthManager;
import com.vhn.doan.services.ReminderForegroundService;
import com.vhn.doan.receivers.BootReceiver;

/**
 * HomeActivity là màn hình chính của ứng dụng sau khi đăng nhập
 * Chứa HomeFragment và các Fragment khác thông qua BottomNavigationView
 */
public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private BottomNavigationView bottomNavigationView;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Khởi tạo AuthManager
        authManager = new AuthManager(this);

        // Kiểm tra đăng nhập
        if (!authManager.isUserLoggedIn()) {
            // Chuyển về màn hình đăng nhập nếu chưa đăng nhập
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // ✅ KHỞI ĐỘNG REMINDER SERVICE KHI APP ĐƯỢC MỞ
        startReminderServices();

        // Khởi tạo và thiết lập BottomNavigationView
        setupBottomNavigation();

        // Mặc định hiển thị HomeFragment khi khởi động
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    /**
     * Khởi động các service cần thiết cho reminder system
     */
    private void startReminderServices() {
        try {
            Log.d(TAG, "🔄 Khởi động reminder services...");

            // 1. Khởi động Foreground Service để duy trì hoạt động
            ReminderForegroundService.startService(this);
            Log.d(TAG, "✅ Đã khởi động ReminderForegroundService");

            // 2. Khôi phục lại tất cả reminder active
            BootReceiver.rescheduleAllReminders(this);
            Log.d(TAG, "✅ Đã yêu cầu khôi phục reminders");

        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi khởi động reminder services", e);
        }
    }

    /**
     * Method để xử lý onClick từ XML layout cho nút tạo reminder
     * Sửa lỗi: IllegalStateException: Could not find method onCreateReminderClick
     */
    public void onCreateReminderClick(android.view.View view) {
        // Tìm ReminderFragment hiện tại và gọi method tạo reminder
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof ReminderFragment) {
            ReminderFragment reminderFragment = (ReminderFragment) currentFragment;
            reminderFragment.onCreateReminderClick();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // ✅ ĐẢM BẢO SERVICE LUÔN CHẠY KHI APP ĐƯỢC RESUME
        ensureReminderServiceRunning();
    }

    /**
     * Đảm bảo reminder service luôn chạy
     */
    private void ensureReminderServiceRunning() {
        try {
            Log.d(TAG, "🔄 Kiểm tra và đảm bảo reminder service đang chạy...");
            
            // Khởi động service nếu chưa chạy
            ReminderForegroundService.startService(this);
            
            Log.d(TAG, "✅ Đã đảm bảo reminder service đang chạy");
        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi đảm bảo reminder service chạy", e);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    fragment = HomeFragment.newInstance();
                } else if (itemId == R.id.nav_reminders) {
                    // Kích hoạt ReminderFragment
                    fragment = ReminderFragment.newInstance();
                } else if (itemId == R.id.nav_videos) {
                    // Tạm thời vẫn dùng HomeFragment
                    // Sẽ thay thế bằng ShortVideoFragment khi phát triển
                    fragment = HomeFragment.newInstance();
                    // fragment = ShortVideoFragment.newInstance();
                } else if (itemId == R.id.nav_favorites) {
                    // Kích hoạt FavoriteFragment
                    fragment = FavoriteFragment.newInstance();
                } else if (itemId == R.id.nav_profile) {
                    // Sử dụng ProfileFragment thay vì HomeFragment
                    fragment = ProfileFragment.newInstance();
                }

                return loadFragment(fragment);
            }
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
