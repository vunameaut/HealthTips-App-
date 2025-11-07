package com.vhn.doan.presentation.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.R;
import com.vhn.doan.presentation.auth.LoginActivity;
import com.vhn.doan.presentation.base.BaseFragment;
import com.vhn.doan.presentation.settings.SettingsAndPrivacyActivity;

/**
 * Fragment đơn giản cho profile người dùng
 * Phiên bản đơn giản theo thiết kế giống TikTok
 */
public class SimpleProfileFragment extends BaseFragment {

    private TextView profileName, profileUsername;
    private SwitchCompat switchDarkMode;
    private ImageButton btnMenu;
    private ImageView profileImage;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFirebase();
        loadUserInfo();
        setupTabLayoutWithViewPager();
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void initViews(View view) {
        // Khởi tạo các view components từ layout mới
        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileUsername = view.findViewById(R.id.profile_username);

        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        btnMenu = view.findViewById(R.id.btn_menu);

        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);

        // Load và set trạng thái switch dựa trên theme hiện tại
        updateSwitchBasedOnCurrentTheme();
    }

    @Override
    protected void setupListeners() {
        // Thiết lập listener cho switch dark mode với animation mượt mà
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Chỉ xử lý khi user thực sự thay đổi, không phải khi set programmatically
            if (!buttonView.isPressed()) {
                return;
            }

            // Animation mượt mà khi chuyển đổi
            buttonView.animate()
                    .scaleX(0.92f)
                    .scaleY(0.92f)
                    .setDuration(100)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .withEndAction(() -> {
                        buttonView.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(120)
                                .setInterpolator(new android.view.animation.OvershootInterpolator())
                                .start();
                    })
                    .start();

            // Thêm hiệu ứng alpha cho transition
            buttonView.animate()
                    .alpha(0.85f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        buttonView.animate()
                                .alpha(1.0f)
                                .setDuration(100)
                                .start();
                    })
                    .start();

            toggleDarkMode(isChecked);
        });

        btnMenu.setOnClickListener(v -> showMenuOptions());
    }

    private void setupTabLayoutWithViewPager() {
        // Khởi tạo adapter cho ViewPager
        ProfileViewPagerAdapter adapter = new ProfileViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Liên kết TabLayout với ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Bài viết yêu thích");
                    break;
                case 1:
                    tab.setText("Video đã like");
                    break;
            }
        }).attach();
    }

    private long lastToggleTime = 0;
    private static final long TOGGLE_DEBOUNCE_MS = 500; // 500ms debounce

    private void toggleDarkMode(boolean isDarkMode) {
        // Debounce để tránh click liên tục
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastToggleTime < TOGGLE_DEBOUNCE_MS) {
            return;
        }
        lastToggleTime = currentTime;

        if (getContext() == null) return;

        try {
            // Lấy SharedPreferences
            android.content.SharedPreferences prefs = getContext().getSharedPreferences("DisplaySettings", android.content.Context.MODE_PRIVATE);

            String newTheme;
            int nightMode;
            String message;

            // Set theme dựa trên switch state
            if (isDarkMode) {
                // Bật chế độ tối
                newTheme = "dark";
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                message = "Đã chuyển sang chế độ tối";
            } else {
                // Tắt chế độ tối (chuyển sang sáng)
                newTheme = "light";
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                message = "Đã chuyển sang chế độ sáng";
            }

            // Lưu vào SharedPreferences
            prefs.edit().putString("theme_mode", newTheme).apply();
            prefs.edit().putBoolean("dark_mode", isDarkMode).apply();

            // Apply theme với animation mượt mà
            if (getActivity() != null && getView() != null) {
                final View rootView = getActivity().findViewById(android.R.id.content);
                final String toastMessage = message; // Capture message

                // Fade out nhẹ - chỉ xuống 0.85 thay vì 0.3
                rootView.animate()
                    .alpha(0.85f)
                    .setDuration(150)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .withEndAction(() -> {
                        // Apply theme
                        AppCompatDelegate.setDefaultNightMode(nightMode);

                        // Đợi một chút để theme được set, sau đó recreate Activity
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                                // Recreate Activity để áp dụng theme
                                getActivity().recreate();

                                // Hiển thị thông báo
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), toastMessage, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, 150);
                    })
                    .start();
            }

        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Cập nhật trạng thái switch dựa trên theme hiện tại của hệ thống
     */
    private void updateSwitchBasedOnCurrentTheme() {
        if (getContext() == null || switchDarkMode == null) return;

        try {
            // Detect theme hiện tại từ system resources
            int currentNightMode = getResources().getConfiguration().uiMode &
                                   android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            boolean isDarkMode = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;

            // Set switch state mà không trigger listener
            switchDarkMode.setChecked(isDarkMode);
        } catch (Exception e) {
            // Ignore errors
        }
    }

    private void showMenuOptions() {
        if (getContext() == null) return;

        // Sử dụng BottomSheetDialog thay vì AlertDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_menu, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Khởi tạo các view trong BottomSheetDialog
        TextView txtSetting = bottomSheetView.findViewById(R.id.txt_setting);
        TextView txtLogout = bottomSheetView.findViewById(R.id.txt_logout);

        // Thiết lập sự kiện click cho các tùy chọn
        txtSetting.setOnClickListener(v -> {
            // Mở màn hình Settings và Privacy
            Intent intent = new Intent(getContext(), SettingsAndPrivacyActivity.class);
            startActivity(intent);
            bottomSheetDialog.dismiss();
        });

        txtLogout.setOnClickListener(v -> {
            showLogoutConfirmDialog();
            bottomSheetDialog.dismiss();
        });

        // Hiển thị BottomSheetDialog
        bottomSheetDialog.show();
    }

    private void loadUserInfo() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Hiển thị thông tin người dùng
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            profileName.setText(displayName != null ? displayName : "Người dùng");
            profileUsername.setText("@" + (email != null ? email.split("@")[0] : "user"));
        } else {
            profileName.setText("Chưa đăng nhập");
            profileUsername.setText("@guest");
        }
    }

    private void showLogoutConfirmDialog() {
        if (getContext() != null) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Xác nhận đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout())
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    private void performLogout() {
        try {
            // Hiển thị loading
            showLoading(true);

            // Đăng xuất Firebase
            firebaseAuth.signOut();

            // Ẩn loading
            showLoading(false);

            // Hiển thị thông báo thành công
            if (getContext() != null) {
                Toast.makeText(getContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
            }

            // Chuyển về màn hình đăng nhập
            navigateToLogin();

        } catch (Exception e) {
            showLoading(false);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi đăng xuất: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToLogin() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    public void showLoading(boolean isLoading) {
        if (getView() != null) {
            View loadingView = getView().findViewById(R.id.loadingLayout);
            if (loadingView != null) {
                loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        }
    }
}
