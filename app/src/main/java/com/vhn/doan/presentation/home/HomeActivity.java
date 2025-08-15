package com.vhn.doan.presentation.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.vhn.doan.R;
import com.vhn.doan.presentation.auth.LoginActivity;
import com.vhn.doan.presentation.chat.ChatListFragment;
import com.vhn.doan.presentation.profile.ProfileFragment;
import com.vhn.doan.presentation.reminder.ReminderFragment;
import com.vhn.doan.services.AuthManager;
import com.vhn.doan.services.ReminderManager;
import com.vhn.doan.utils.UserSessionManager;

/**
 * HomeActivity là màn hình chính của ứng dụng sau khi đăng nhập
 * Chứa HomeFragment và các Fragment khác thông qua BottomNavigationView
 */
public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private BottomNavigationView bottomNavigationView;
    private AuthManager authManager;
    private ReminderManager reminderManager;

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

        // Khởi tạo ReminderManager
        reminderManager = new ReminderManager(new UserSessionManager(this));

        // Khởi động ReminderForegroundService
        reminderManager.startReminderService(this);
        Log.d(TAG, "ReminderForegroundService đã được khởi động từ HomeActivity");

        // Khởi tạo và thiết lập BottomNavigationView
        setupBottomNavigation();

        // Mặc định hiển thị HomeFragment khi khởi động
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Khởi động lại ReminderForegroundService khi activity được resume
        if (reminderManager != null) {
            // Khởi động lại service để đảm bảo nó đang chạy
            reminderManager.startReminderService(this);

            // Khởi động lại tất cả reminders đang active
            reminderManager.restartAllReminders(this);

            // Kiểm tra và hiển thị reminders đã bị miss
            reminderManager.checkAndShowMissedReminders(this);

            Log.d(TAG, "Đã khởi động lại và kiểm tra reminders trong onResume");
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

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    fragment = HomeFragment.newInstance();
                } else if (itemId == R.id.nav_chat) {
                    // Kích hoạt ChatListFragment - Danh sách cuộc trò chuyện với AI
                    fragment = ChatListFragment.newInstance();
                } else if (itemId == R.id.nav_reminders) {
                    // Kích hoạt ReminderFragment
                    fragment = ReminderFragment.newInstance();
                } else if (itemId == R.id.nav_videos) {
                    // Chức năng video sẽ được triển khai lại
                    // Tạm thời giữ nguyên HomeFragment
                    fragment = HomeFragment.newInstance();
                } else if (itemId == R.id.nav_profile) {
                    // Sử dụng ProfileFragment - chức năng yêu thích đã được tích hợp vào đây
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
