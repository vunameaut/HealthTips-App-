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
 * Fragment hiển thị thông tin profile người dùng
 * Tuân thủ kiến trúc MVP pattern với Firebase Authentication
 */
public class ProfileFragment extends BaseFragment implements ProfileContract.View {

    private ProfileContract.Presenter presenter;
    private TextView profileName, profileUsername, profileBio;
    private ImageButton btnToggleTheme, btnMenu;
    private ImageView profileImage;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FirebaseAuth firebaseAuth;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        // TODO: Inject presenter through Dagger 2 trong tương lai
        // presenter = DaggerProfileComponent.create().getPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load thông tin người dùng ngay sau khi view được tạo
        loadUserProfile();

        // Thiết lập TabLayout và ViewPager2
        setupTabLayoutWithViewPager();

        // Attach presenter nếu có
        if (presenter != null) {
            presenter.attachView(this);
            presenter.loadUserProfile();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    @Override
    protected void initViews(View view) {
        // Khởi tạo các view components từ layout mới
        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileUsername = view.findViewById(R.id.profile_username);
        profileBio = view.findViewById(R.id.profile_bio);


        btnToggleTheme = view.findViewById(R.id.btn_toggle_theme);
        btnMenu = view.findViewById(R.id.btn_menu);

        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
    }

    @Override
    protected void setupListeners() {
        // Thiết lập listener cho các nút
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

        // Sử dụng BottomSheetDialog với theme bo góc
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_menu, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Khởi tạo các view trong BottomSheetDialog
        TextView txtSetting = bottomSheetView.findViewById(R.id.txt_setting);
        TextView txtLogout = bottomSheetView.findViewById(R.id.txt_logout);

        // Thiết lập sự kiện click cho các tùy chọn
        txtSetting.setOnClickListener(v -> {
            openSettings();
            bottomSheetDialog.dismiss();
        });

        txtLogout.setOnClickListener(v -> {
            showLogoutConfirmDialog();
            bottomSheetDialog.dismiss();
        });

        // Hiển thị BottomSheetDialog
        bottomSheetDialog.show();
    }

    private void openSettings() {
        // Xử lý mở màn hình cài đặt
        if (getContext() != null) {
            Toast.makeText(getContext(), "Chức năng cài đặt đang phát triển", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to settings screen
        }
    }

    /**
     * Tải thông tin profile người dùng từ Firebase
     */
    private void loadUserProfile() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Hiển thị thông tin người dùng lên giao diện
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            profileName.setText(displayName != null ? displayName : "Người dùng");
            profileUsername.setText("@" + (email != null ? email.split("@")[0] : "user"));
            profileBio.setText("Mô tả của người dùng chưa được cập nhật. Hãy thêm mô tả để mọi người biết thêm về bạn.");
        } else {
            // Nếu chưa đăng nhập, hiển thị thông báo
            profileName.setText("Chưa đăng nhập");
            profileUsername.setText("@guest");
            profileBio.setText("Vui lòng đăng nhập để xem thông tin cá nhân");
        }
    }

    public void showLogoutConfirmDialog() {
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

    @Override
    public void displayUserProfile(String userInfo) {
        // Phương thức này sẽ được presenter gọi để hiển thị thông tin người dùng
        if (profileBio != null) {
            profileBio.setText(userInfo);
        }
    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showLoading(boolean isLoading) {
        // TODO: Hiển thị hoặc ẩn loading indicator
    }

    private String buildUserInfoString(FirebaseUser user) {
        StringBuilder sb = new StringBuilder();
        if (user.getDisplayName() != null) {
            sb.append("Tên: ").append(user.getDisplayName()).append("\n");
        }
        if (user.getEmail() != null) {
            sb.append("Email: ").append(user.getEmail()).append("\n");
        }
        if (user.getPhoneNumber() != null) {
            sb.append("SĐT: ").append(user.getPhoneNumber()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void onLogoutSuccess() {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
        }

        // Chuyển về màn hình đăng nhập
        navigateToLogin();
    }
}
