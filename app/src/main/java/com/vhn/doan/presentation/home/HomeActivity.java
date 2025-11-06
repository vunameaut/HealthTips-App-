package com.vhn.doan.presentation.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.vhn.doan.R;
import com.vhn.doan.presentation.auth.LoginActivity;
import com.vhn.doan.presentation.base.BaseActivity;
import com.vhn.doan.presentation.base.FragmentVisibilityListener;
import com.vhn.doan.presentation.chat.ChatListFragment;
import com.vhn.doan.presentation.chat.NewChatFragment;
import com.vhn.doan.presentation.profile.ProfileFragment;
import com.vhn.doan.presentation.reminder.ReminderFragment;
import com.vhn.doan.presentation.video.VideoFragment;
import com.vhn.doan.services.AuthManager;
import com.vhn.doan.services.ReminderManager;
import com.vhn.doan.utils.UserSessionManager;

/**
 * HomeActivity là màn hình chính của ứng dụng sau khi đăng nhập
 * Chứa HomeFragment và các Fragment khác thông qua BottomNavigationView
 */
public class HomeActivity extends BaseActivity {

    private static final String TAG = "HomeActivity";
    private BottomNavigationView bottomNavigationView;
    private AuthManager authManager;
    private ReminderManager reminderManager;

    // Cache Fragments để sử dụng show/hide thay vì replace
    private HomeFragment homeFragment;
    private ChatListFragment chatListFragment;
    private ReminderFragment reminderFragment;
    private VideoFragment videoFragment;
    private ProfileFragment profileFragment;
    private Fragment currentFragment;

    // Flag để theo dõi fragment đã được hiển thị thực sự chưa
    private boolean isHomeFragmentEverShown = false;
    private boolean isChatFragmentEverShown = false;
    private boolean isReminderFragmentEverShown = false;
    private boolean isVideoFragmentEverShown = false;
    private boolean isProfileFragmentEverShown = false;

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
            initializeFragments();
            showFragment(homeFragment);
        } else {
            // Restore fragments sau configuration change
            restoreFragments();
        }
    }

    /**
     * Restore fragments sau configuration change (như screen rotation)
     */
    private void restoreFragments() {
        homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("HOME");
        chatListFragment = (ChatListFragment) getSupportFragmentManager().findFragmentByTag("CHAT");
        reminderFragment = (ReminderFragment) getSupportFragmentManager().findFragmentByTag("REMINDER");
        videoFragment = (VideoFragment) getSupportFragmentManager().findFragmentByTag("VIDEO");
        profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("PROFILE");

        // Tìm fragment hiện tại đang visible
        if (homeFragment != null && homeFragment.isVisible()) {
            currentFragment = homeFragment;
        } else if (chatListFragment != null && chatListFragment.isVisible()) {
            currentFragment = chatListFragment;
        } else if (reminderFragment != null && reminderFragment.isVisible()) {
            currentFragment = reminderFragment;
        } else if (videoFragment != null && videoFragment.isVisible()) {
            currentFragment = videoFragment;
        } else if (profileFragment != null && profileFragment.isVisible()) {
            currentFragment = profileFragment;
        }

        Log.d(TAG, "Fragments restored after configuration change");
    }

    /**
     * Khởi tạo tất cả fragments một lần duy nhất
     * Chỉ HomeFragment được hiển thị và load dữ liệu ban đầu
     * Các fragment khác được add nhưng ẩn, chỉ load dữ liệu khi được show lần đầu
     */
    private void initializeFragments() {
        homeFragment = HomeFragment.newInstance();
        chatListFragment = ChatListFragment.newInstance();
        reminderFragment = ReminderFragment.newInstance();
        videoFragment = VideoFragment.newInstance();
        profileFragment = ProfileFragment.newInstance();

        // Thêm tất cả fragments vào container và hide tất cả trừ home
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, homeFragment, "HOME")
                .add(R.id.fragment_container, chatListFragment, "CHAT")
                .add(R.id.fragment_container, reminderFragment, "REMINDER")
                .add(R.id.fragment_container, videoFragment, "VIDEO")
                .add(R.id.fragment_container, profileFragment, "PROFILE")
                .hide(chatListFragment)
                .hide(reminderFragment)
                .hide(videoFragment)
                .hide(profileFragment)
                .commit();

        currentFragment = homeFragment;

        // Đánh dấu HomeFragment đã được show
        isHomeFragmentEverShown = true;
        notifyFragmentVisible(homeFragment);

        Log.d(TAG, "✅ All fragments initialized. Only HomeFragment is visible and active.");
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
                    fragment = homeFragment;
                } else if (itemId == R.id.nav_chat) {
                    fragment = chatListFragment;
                } else if (itemId == R.id.nav_reminders) {
                    fragment = reminderFragment;
                } else if (itemId == R.id.nav_videos) {
                    fragment = videoFragment;
                } else if (itemId == R.id.nav_profile) {
                    fragment = profileFragment;
                }

                return showFragment(fragment);
            }
        });
    }

    /**
     * Hiển thị fragment sử dụng show/hide pattern
     * Giữ nguyên trạng thái của fragment khi chuyển đổi
     * Thông báo cho fragment khi được show/hide
     */
    private boolean showFragment(Fragment fragment) {
        if (fragment != null && fragment != currentFragment) {
            // Thông báo fragment cũ bị ẩn
            notifyFragmentHidden(currentFragment);

            // Chuyển đổi fragment
            getSupportFragmentManager().beginTransaction()
                    .hide(currentFragment)
                    .show(fragment)
                    .commit();

            Fragment previousFragment = currentFragment;
            currentFragment = fragment;

            // Đánh dấu fragment đã được show lần đầu và thông báo
            markFragmentAsShown(fragment);
            notifyFragmentVisible(fragment);

            Log.d(TAG, "🔄 Switched from " + previousFragment.getClass().getSimpleName() +
                      " to " + fragment.getClass().getSimpleName());
            return true;
        }
        return false;
    }

    /**
     * Đánh dấu fragment đã được show lần đầu
     */
    private void markFragmentAsShown(Fragment fragment) {
        if (fragment == homeFragment && !isHomeFragmentEverShown) {
            isHomeFragmentEverShown = true;
            Log.d(TAG, "📍 HomeFragment shown for the first time");
        } else if (fragment == chatListFragment && !isChatFragmentEverShown) {
            isChatFragmentEverShown = true;
            Log.d(TAG, "📍 ChatFragment shown for the first time");
        } else if (fragment == reminderFragment && !isReminderFragmentEverShown) {
            isReminderFragmentEverShown = true;
            Log.d(TAG, "📍 ReminderFragment shown for the first time");
        } else if (fragment == videoFragment && !isVideoFragmentEverShown) {
            isVideoFragmentEverShown = true;
            Log.d(TAG, "📍 VideoFragment shown for the first time - NOW it can start loading");
        } else if (fragment == profileFragment && !isProfileFragmentEverShown) {
            isProfileFragmentEverShown = true;
            Log.d(TAG, "📍 ProfileFragment shown for the first time");
        }
    }

    /**
     * Thông báo cho fragment khi được hiển thị
     */
    private void notifyFragmentVisible(Fragment fragment) {
        if (fragment instanceof FragmentVisibilityListener) {
            ((FragmentVisibilityListener) fragment).onFragmentVisible();
            Log.d(TAG, "🔔 Notified " + fragment.getClass().getSimpleName() + " onFragmentVisible");
        }
    }

    /**
     * Thông báo cho fragment khi bị ẩn
     */
    private void notifyFragmentHidden(Fragment fragment) {
        if (fragment instanceof FragmentVisibilityListener) {
            ((FragmentVisibilityListener) fragment).onFragmentHidden();
            Log.d(TAG, "🔕 Notified " + fragment.getClass().getSimpleName() + " onFragmentHidden");
        }
    }

    // Phương thức để hiển thị bottom navigation
    public void showBottomNavigation() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(android.view.View.VISIBLE);
        }
    }

    // Phương thức để ẩn bottom navigation
    public void hideBottomNavigation() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(android.view.View.GONE);
        }
    }

    // Kiểm tra nếu fragment được thêm vào là một trong các fragment chính, hiển thị bottom nav
    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);

        // Nếu fragment là NewChatFragment, đảm bảo bottom navigation vẫn bị ẩn
        if (fragment instanceof NewChatFragment) {
            hideBottomNavigation();
        }
        // Hiển thị bottom navigation cho các fragment chính
        else if (fragment instanceof HomeFragment ||
            fragment instanceof ChatListFragment ||
            fragment instanceof ReminderFragment ||
            fragment instanceof VideoFragment ||
            fragment instanceof ProfileFragment) {
            showBottomNavigation();
        }
    }
}
