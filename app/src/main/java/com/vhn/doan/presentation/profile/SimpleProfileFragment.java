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
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.R;
import com.vhn.doan.presentation.auth.LoginActivity;
import com.vhn.doan.presentation.base.BaseFragment;

/**
 * Fragment đơn giản cho profile người dùng
 * Phiên bản đơn giản theo thiết kế giống TikTok
 */
public class SimpleProfileFragment extends BaseFragment {

    private TextView profileName, profileUsername, profileBio;
    private ImageButton btnToggleTheme, btnMenu;
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

        btnToggleTheme = view.findViewById(R.id.btn_toggle_theme);
        btnMenu = view.findViewById(R.id.btn_menu);

        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
    }

    @Override
    protected void setupListeners() {
        btnToggleTheme.setOnClickListener(v -> toggleDarkMode());
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

    private void toggleDarkMode() {
        int currentNightMode = AppCompatDelegate.getDefaultNightMode();
        if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            // Đang ở chế độ tối, chuyển sang chế độ sáng
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Đã chuyển sang chế độ sáng", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Đang ở chế độ sáng, chuyển sang chế độ tối
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Đã chuyển sang chế độ tối", Toast.LENGTH_SHORT).show();
            }
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
            Toast.makeText(getContext(), "Chức năng cài đặt đang phát triển", Toast.LENGTH_SHORT).show();
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
            profileBio.setText("Mô tả của người dùng chưa được cập nhật. Hãy thêm mô tả để mọi người biết thêm về bạn.");
        } else {
            profileName.setText("Chưa đăng nhập");
            profileUsername.setText("@guest");
            profileBio.setText("Vui lòng đăng nhập để xem thông tin cá nhân");
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
        // TODO: Hiển thị hoặc ẩn loading indicator
    }
}
