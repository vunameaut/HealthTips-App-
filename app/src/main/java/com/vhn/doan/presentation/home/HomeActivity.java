package com.vhn.doan.presentation.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

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
import com.google.firebase.auth.FirebaseAuth;
import com.vhn.doan.services.AuthManager;
import com.vhn.doan.services.ReminderManager;
import com.vhn.doan.utils.AuthTokenManager;
import com.vhn.doan.utils.SyncScheduler;
import com.vhn.doan.utils.UserSessionManager;

/**
 * HomeActivity là màn hình chính của ứng dụng sau khi đăng nhập
 * Chứa HomeFragment và các Fragment khác thông qua BottomNavigationView
 */
public class HomeActivity extends BaseActivity {

    private static final String TAG = "HomeActivity";
    private static final String KEY_SELECTED_TAB = "selected_tab_id";

    private BottomNavigationView bottomNavigationView;
    private AuthManager authManager;
    private ReminderManager reminderManager;
    private FirebaseAuth.AuthStateListener authStateListener;

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

        // Thêm AuthStateListener để theo dõi thay đổi trạng thái đăng nhập
        authStateListener = AuthTokenManager.addAuthStateListener(this);
        Log.d(TAG, "AuthStateListener đã được thêm để theo dõi trạng thái đăng nhập");

        // Verify token hiện tại để đảm bảo nó vẫn hợp lệ
        AuthTokenManager.verifyCurrentToken(new AuthTokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed() {
                Log.d(TAG, "Token hiện tại hợp lệ, tiếp tục khởi động ứng dụng");
            }

            @Override
            public void onTokenRefreshFailed() {
                Log.w(TAG, "Token không hợp lệ, đăng xuất và chuyển về màn hình đăng nhập");
                AuthTokenManager.forceLogoutAndRedirectToLogin(HomeActivity.this,
                    "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
            }
        });

        // Khởi tạo ReminderManager
        reminderManager = new ReminderManager(new UserSessionManager(this));

        // Khởi động ReminderForegroundService
        reminderManager.startReminderService(this);
        Log.d(TAG, "ReminderForegroundService đã được khởi động từ HomeActivity");

        // Schedule periodic sync cho offline mode
        SyncScheduler.scheduleHealthTipSync(this);
        Log.d(TAG, "Health tip sync scheduled for offline mode");

        // Khởi tạo và thiết lập BottomNavigationView
        setupBottomNavigation();

        // Mặc định hiển thị HomeFragment khi khởi động
        if (savedInstanceState == null) {
            Log.d(TAG, "🆕 onCreate: NEW ACTIVITY - Initializing fragments");
            initializeFragments();
            showFragment(homeFragment);
        } else {
            // Restore fragments sau configuration change
            Log.d(TAG, "♻️ onCreate: RECREATING ACTIVITY (theme change/rotation) - Restoring fragments");
            restoreFragments(savedInstanceState);
        }
    }

    /**
     * Restore fragments sau configuration change (như screen rotation hoặc theme change)
     */
    private void restoreFragments(Bundle savedInstanceState) {
        homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("HOME");
        chatListFragment = (ChatListFragment) getSupportFragmentManager().findFragmentByTag("CHAT");
        reminderFragment = (ReminderFragment) getSupportFragmentManager().findFragmentByTag("REMINDER");
        videoFragment = (VideoFragment) getSupportFragmentManager().findFragmentByTag("VIDEO");
        profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("PROFILE");

        Log.d(TAG, "🔄 Restored fragments - Home: " + (homeFragment != null) +
                ", Chat: " + (chatListFragment != null) +
                ", Reminder: " + (reminderFragment != null) +
                ", Video: " + (videoFragment != null) +
                ", Profile: " + (profileFragment != null));

        // LẤY TAB ĐÃ LƯU TỪ SAVEDINSTANCESTATE
        int selectedItemId = savedInstanceState.getInt(KEY_SELECTED_TAB, R.id.nav_home);
        Log.d(TAG, "📌 Restored selected tab ID: " + selectedItemId);

        // Xác định currentFragment dựa trên selectedItemId đã lưu
        currentFragment = null;
        if (selectedItemId == R.id.nav_home) {
            currentFragment = homeFragment;
            Log.d(TAG, "✅ Restoring Home tab");
        } else if (selectedItemId == R.id.nav_chat) {
            currentFragment = chatListFragment;
            Log.d(TAG, "✅ Restoring Chat tab");
        } else if (selectedItemId == R.id.nav_reminders) {
            currentFragment = reminderFragment;
            Log.d(TAG, "✅ Restoring Reminder tab");
        } else if (selectedItemId == R.id.nav_videos) {
            currentFragment = videoFragment;
            Log.d(TAG, "✅ Restoring Video tab");
        } else if (selectedItemId == R.id.nav_profile) {
            currentFragment = profileFragment;
            Log.d(TAG, "✅ Restoring Profile tab");
        } else {
            currentFragment = homeFragment;
            selectedItemId = R.id.nav_home;
            Log.w(TAG, "⚠️ Unknown tab ID, defaulting to Home");
        }

        if (currentFragment == null) {
            Log.w(TAG, "⚠️ No fragment found after restore, defaulting to Home");
            currentFragment = homeFragment;
            selectedItemId = R.id.nav_home;
        }

        // CRITICAL FIX: LUÔN LUÔN hide tất cả fragments trước, sau đó chỉ show currentFragment
        // Điều này đảm bảo chỉ có 1 fragment visible sau khi recreate
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // BƯỚC 1: Hide TẤT CẢ fragments (không quan tâm trạng thái hiện tại)
        if (homeFragment != null) {
            transaction.hide(homeFragment);
            Log.d(TAG, "🔒 Hiding HomeFragment");
        }
        if (chatListFragment != null) {
            transaction.hide(chatListFragment);
            Log.d(TAG, "🔒 Hiding ChatListFragment");
        }
        if (reminderFragment != null) {
            transaction.hide(reminderFragment);
            Log.d(TAG, "🔒 Hiding ReminderFragment");
        }
        if (videoFragment != null) {
            transaction.hide(videoFragment);
            Log.d(TAG, "🔒 Hiding VideoFragment");
        }
        if (profileFragment != null) {
            transaction.hide(profileFragment);
            Log.d(TAG, "🔒 Hiding ProfileFragment");
        }

        // BƯỚC 2: Chỉ show currentFragment
        if (currentFragment != null) {
            transaction.show(currentFragment);
            Log.d(TAG, "🔓 Showing ONLY " + currentFragment.getClass().getSimpleName());
        }

        // Commit ngay lập tức
        transaction.commitNow();
        Log.d(TAG, "✅ Fragment visibility forcefully corrected - ONLY 1 visible");

        // BƯỚC 2.5: FORCE set View visibility để đảm bảo UI không bị chồng lên nhau
        // Điều này rất quan trọng vì FragmentTransaction hide/show có thể không đủ
        if (homeFragment != null && homeFragment.getView() != null) {
            homeFragment.getView().setVisibility(homeFragment == currentFragment ? View.VISIBLE : View.GONE);
            Log.d(TAG, "🎨 HomeFragment View: " + (homeFragment == currentFragment ? "VISIBLE" : "GONE"));
        }
        if (chatListFragment != null && chatListFragment.getView() != null) {
            chatListFragment.getView().setVisibility(chatListFragment == currentFragment ? View.VISIBLE : View.GONE);
            Log.d(TAG, "🎨 ChatListFragment View: " + (chatListFragment == currentFragment ? "VISIBLE" : "GONE"));
        }
        if (reminderFragment != null && reminderFragment.getView() != null) {
            reminderFragment.getView().setVisibility(reminderFragment == currentFragment ? View.VISIBLE : View.GONE);
            Log.d(TAG, "🎨 ReminderFragment View: " + (reminderFragment == currentFragment ? "VISIBLE" : "GONE"));
        }
        if (videoFragment != null && videoFragment.getView() != null) {
            videoFragment.getView().setVisibility(videoFragment == currentFragment ? View.VISIBLE : View.GONE);
            Log.d(TAG, "🎨 VideoFragment View: " + (videoFragment == currentFragment ? "VISIBLE" : "GONE"));
        }
        if (profileFragment != null && profileFragment.getView() != null) {
            profileFragment.getView().setVisibility(profileFragment == currentFragment ? View.VISIBLE : View.GONE);
            Log.d(TAG, "🎨 ProfileFragment View: " + (profileFragment == currentFragment ? "VISIBLE" : "GONE"));
        }

        // BƯỚC 3: Notify tất cả fragments về trạng thái của chúng
        if (homeFragment != null) {
            if (homeFragment == currentFragment) {
                notifyFragmentVisible(homeFragment);
            } else {
                notifyFragmentHidden(homeFragment);
            }
        }
        if (chatListFragment != null) {
            if (chatListFragment == currentFragment) {
                notifyFragmentVisible(chatListFragment);
            } else {
                notifyFragmentHidden(chatListFragment);
            }
        }
        if (reminderFragment != null) {
            if (reminderFragment == currentFragment) {
                notifyFragmentVisible(reminderFragment);
            } else {
                notifyFragmentHidden(reminderFragment);
            }
        }
        if (videoFragment != null) {
            if (videoFragment == currentFragment) {
                notifyFragmentVisible(videoFragment);
            } else {
                notifyFragmentHidden(videoFragment);
            }
        }
        if (profileFragment != null) {
            if (profileFragment == currentFragment) {
                notifyFragmentVisible(profileFragment);
            } else {
                notifyFragmentHidden(profileFragment);
            }
        }

        // Mark current fragment as shown để update flags
        markFragmentAsShown(currentFragment);

        // Post to make sure UI is ready
        final int finalSelectedId = selectedItemId;
        bottomNavigationView.post(() -> {
            bottomNavigationView.setSelectedItemId(finalSelectedId);
            Log.d(TAG, "🎯 Bottom nav synced to: " + finalSelectedId);
        });

        Log.d(TAG, "✅ Fragments restored. Current: " +
            (currentFragment != null ? currentFragment.getClass().getSimpleName() : "null"));
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Lưu tab hiện tại trước khi recreate (theme change, rotation, etc.)
        int selectedItemId = bottomNavigationView.getSelectedItemId();
        outState.putInt(KEY_SELECTED_TAB, selectedItemId);
        Log.d(TAG, "💾 Saving selected tab ID: " + selectedItemId);
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
            Log.d(TAG, "🔄 Attempting to switch from " +
                (currentFragment != null ? currentFragment.getClass().getSimpleName() : "null") +
                " to " + fragment.getClass().getSimpleName());

            // Thông báo fragment cũ bị ẩn
            notifyFragmentHidden(currentFragment);

            // Chuyển đổi fragment
            androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Chỉ hide currentFragment nếu nó không null
            if (currentFragment != null) {
                transaction.hide(currentFragment);
                Log.d(TAG, "  ➡️ Hiding: " + currentFragment.getClass().getSimpleName());
            }

            transaction.show(fragment);
            Log.d(TAG, "  ➡️ Showing: " + fragment.getClass().getSimpleName());

            // Sử dụng commitNow() để đảm bảo transaction được thực thi ngay lập tức
            transaction.commitNow();

            Fragment previousFragment = currentFragment;
            currentFragment = fragment;

            // CRITICAL: Force set View visibility để đảm bảo UI thực sự được update
            // Đây là lớp bảo vệ thêm để đảm bảo không có fragment nào bị chồng lên nhau
            if (previousFragment != null && previousFragment.getView() != null) {
                previousFragment.getView().setVisibility(android.view.View.GONE);
                Log.d(TAG, "  🎨 Previous fragment View set to GONE: " + previousFragment.getClass().getSimpleName());
            }
            if (fragment.getView() != null) {
                fragment.getView().setVisibility(android.view.View.VISIBLE);
                Log.d(TAG, "  🎨 Current fragment View set to VISIBLE: " + fragment.getClass().getSimpleName());
            }

            // Đánh dấu fragment đã được show lần đầu và thông báo
            markFragmentAsShown(fragment);
            notifyFragmentVisible(fragment);

            Log.d(TAG, "✅ Switched successfully. Current fragment: " + currentFragment.getClass().getSimpleName());
            return true;
        } else {
            Log.d(TAG, "⚠️ showFragment skipped - fragment: " +
                (fragment != null ? fragment.getClass().getSimpleName() : "null") +
                ", currentFragment: " + (currentFragment != null ? currentFragment.getClass().getSimpleName() : "null"));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove AuthStateListener để tránh memory leak
        if (authStateListener != null) {
            AuthTokenManager.removeAuthStateListener(authStateListener);
            Log.d(TAG, "AuthStateListener đã được remove");
        }
    }
}
