package com.vhn.doan.presentation.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.vhn.doan.data.User;
import com.vhn.doan.data.repository.UserRepository;
import com.vhn.doan.data.repository.UserRepositoryImpl;
import com.vhn.doan.presentation.auth.LoginActivity;
import com.vhn.doan.presentation.settings.SettingsAndPrivacyActivity;
import com.vhn.doan.presentation.base.BaseFragment;

/**
 * Fragment hiển thị thông tin profile người dùng
 * Tuân thủ kiến trúc MVP pattern với Firebase Authentication
 */
public class ProfileFragment extends BaseFragment implements ProfileContract.View {

    private ProfileContract.Presenter presenter;
    private TextView profileName, profileUsername;
    private ImageButton btnToggleTheme, btnMenu;
    private ImageView profileImage;
    private ProgressBar avatarUploadProgress;
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

        // Khởi tạo presenter với các dependency cần thiết
        UserRepository userRepository = new UserRepositoryImpl();
        presenter = new ProfilePresenterImpl(userRepository, firebaseAuth);
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
            // Thiết lập context cho presenter để có thể tải ảnh lên Cloudinary
            if (presenter instanceof ProfilePresenterImpl) {
                ((ProfilePresenterImpl) presenter).setContext(getContext());
            }
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
        avatarUploadProgress = view.findViewById(R.id.avatar_upload_progress);

        btnToggleTheme = view.findViewById(R.id.btn_toggle_theme);
        btnMenu = view.findViewById(R.id.btn_menu);

        // Thiết lập sự kiện click cho avatar để hiển thị dialog tùy chọn
        profileImage.setOnClickListener(v -> showAvatarOptionsDialog());

        // Thiết lập sự kiện cho các nút khác
        btnToggleTheme.setOnClickListener(v -> toggleDarkMode());
        btnMenu.setOnClickListener(v -> showMenuOptions());

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
                    tab.setText(R.string.favorite_articles_tab);
                    break;
                case 1:
                    tab.setText(R.string.video_liked_tab);
                    break;
            }
        }).attach();
    }

    private long lastToggleTime = 0;
    private static final long TOGGLE_DEBOUNCE_MS = 1500; // 1.5 giây debounce

    private void toggleDarkMode() {
        // Debounce để tránh click liên tục gây crash
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastToggleTime < TOGGLE_DEBOUNCE_MS) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Vui lòng đợi...", Toast.LENGTH_SHORT).show();
            }
            return; // Bỏ qua nếu click quá nhanh
        }
        lastToggleTime = currentTime;

        if (getContext() == null) return;

        try {
            // Lấy SharedPreferences
            android.content.SharedPreferences prefs = getContext().getSharedPreferences("DisplaySettings", android.content.Context.MODE_PRIVATE);

            // Detect theme hiện tại từ system resources
            int currentNightMode = getResources().getConfiguration().uiMode &
                                   android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            boolean isDarkMode = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;

            String newTheme;
            int nightMode;
            String message;

            // Toggle: Nếu đang dark → light, nếu đang light → dark
            if (isDarkMode) {
                // Đang ở chế độ tối, chuyển sang sáng
                newTheme = "light";
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                message = "Đã chuyển sang chế độ sáng";
            } else {
                // Đang ở chế độ sáng, chuyển sang tối
                newTheme = "dark";
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                message = "Đã chuyển sang chế độ tối";
            }

            // Lưu vào SharedPreferences
            prefs.edit().putString("theme_mode", newTheme).apply();

            // Hiển thị thông báo trước khi recreate
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

            // Delay một chút rồi mới recreate để tránh crash
            if (getActivity() != null) {
                final android.app.Activity activity = getActivity();
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (activity != null && !activity.isFinishing()) {
                        try {
                            // Apply fade animation khi recreate
                            activity.recreate();
                            activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        } catch (Exception e) {
                            // Fallback: chỉ apply theme không recreate
                            AppCompatDelegate.setDefaultNightMode(nightMode);
                        }
                    }
                }, 300); // Delay 300ms
            }

        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        LinearLayout layoutSettings = bottomSheetView.findViewById(R.id.layout_settings);
        LinearLayout layoutLogout = bottomSheetView.findViewById(R.id.layout_logout);

        // Thiết lập sự kiện click cho các tùy chọn
        layoutSettings.setOnClickListener(v -> {
            openSettings();
            bottomSheetDialog.dismiss();
        });

        layoutLogout.setOnClickListener(v -> {
            showLogoutConfirmDialog();
            bottomSheetDialog.dismiss();
        });

        // Hiển thị BottomSheetDialog
        bottomSheetDialog.show();
    }

    private void openSettings() {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), SettingsAndPrivacyActivity.class);
            startActivity(intent);
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
        } else {
            // Nếu chưa đăng nhập, hiển thị thông báo
            profileName.setText("Chưa đăng nhập");
            profileUsername.setText("@guest");
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
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showLoading(boolean isLoading) {
        if (getView() != null) {
            View loadingView = getView().findViewById(R.id.loadingLayout);
            if (loadingView != null) {
                loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        }
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

    /**
     * Hiển thị dialog với các tùy chọn cho avatar
     */
    private void showAvatarOptionsDialog() {
        if (getContext() == null) return;

        // Sử dụng BottomSheetDialog hiển thị các tùy chọn
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.dialog_avatar_options, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Khởi tạo các view trong dialog
        LinearLayout viewAvatarOption = bottomSheetView.findViewById(R.id.view_avatar_option);
        LinearLayout changeAvatarOption = bottomSheetView.findViewById(R.id.change_avatar_option);
        LinearLayout editProfileOption = bottomSheetView.findViewById(R.id.edit_profile_option);

        // Thiết lập sự kiện click cho các tùy chọn
        viewAvatarOption.setOnClickListener(v -> {
            viewFullAvatar();
            bottomSheetDialog.dismiss();
        });

        changeAvatarOption.setOnClickListener(v -> {
            openImagePicker();
            bottomSheetDialog.dismiss();
        });

        editProfileOption.setOnClickListener(v -> {
            openEditProfile();
            bottomSheetDialog.dismiss();
        });

        // Hiển thị dialog
        bottomSheetDialog.show();
    }

    /**
     * Mở màn hình xem avatar kích thước đầy đủ
     */
    private void viewFullAvatar() {
        if (getContext() == null) return;

        // Sử dụng presenter để lấy thông tin avatar hiện tại
        if (presenter != null) {
            presenter.getCurrentUserAvatar(new ProfileContract.AvatarCallback() {
                @Override
                public void onAvatarLoaded(String avatarUrl) {
                    if (getContext() == null) return;

                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        // Tạo một dialog hiển thị ảnh avatar kích thước lớn
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        ImageView imageView = new ImageView(getContext());

                        // Load avatar sử dụng Glide với force no-cache
                        com.bumptech.glide.Glide.with(ProfileFragment.this)
                                .load(avatarUrl)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                                .placeholder(R.drawable.default_avatar)
                                .error(R.drawable.default_avatar)
                                .into(imageView);

                        builder.setView(imageView);
                        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        Toast.makeText(getContext(), "Không có ảnh đại diện để hiển thị", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // Fallback nếu presenter chưa được khởi tạo
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null && currentUser.getPhotoUrl() != null) {
                // Tạo một dialog hiển thị ảnh avatar kích thước lớn
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                ImageView imageView = new ImageView(getContext());

                // Load avatar sử dụng Glide
                com.bumptech.glide.Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(imageView);

                builder.setView(imageView);
                builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                Toast.makeText(getContext(), "Không có ảnh đại diện để hiển thị", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Mở màn hình chọn ảnh từ thiết bị
     */
    private void openImagePicker() {
        if (getContext() == null) return;

        // Kiểm tra quyền truy cập bộ nhớ
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.READ_MEDIA_IMAGES) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Yêu cầu quyền truy cập READ_MEDIA_IMAGES cho Android 13+
                requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
                return;
            }
        } else {
            if (androidx.core.content.ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Yêu cầu quyền truy cập READ_EXTERNAL_STORAGE cho các phiên bản Android cũ hơn
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
                return;
            }
        }

        // Đã có quyền, mở trực tiếp gallery để chọn ảnh
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final int REQUEST_PICK_IMAGE = 200;

    /**
     * Mở màn hình chỉnh sửa thông tin cá nhân
     */
    private void openEditProfile() {
        if (getContext() == null) return;

        Intent intent = new Intent(getContext(), EditProfileActivity.class);
        startActivityForResult(intent, REQUEST_EDIT_PROFILE);
    }

    // Định nghĩa mã yêu cầu cho startActivityForResult
    private static final int REQUEST_EDIT_PROFILE = 1001;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_PROFILE && resultCode == getActivity().RESULT_OK) {
            // Nếu thông tin đã được cập nhật, làm mới dữ liệu
            if (presenter != null) {
                presenter.refreshUserProfile();
            } else {
                loadUserProfile();
            }
        } else if (requestCode == REQUEST_PICK_IMAGE && resultCode == getActivity().RESULT_OK && data != null) {
            // Xử lý kết quả chọn ảnh từ gallery
            android.net.Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Sử dụng presenter để cập nhật avatar mới
                if (presenter != null) {
                    presenter.updateUserAvatar(selectedImageUri);
                }
            }
        }
    }

    @Override
    public void displayUserAvatar(String avatarUrl, boolean isDefault) {
        if (getContext() == null || profileImage == null) return;

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.default_avatar);
        }
    }

    @Override
    public void displayUserProfile(User user) {
        if (user == null) return;

        profileName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Người dùng");
        profileUsername.setText("@" + (user.getEmail() != null ? user.getEmail().split("@")[0] : "user"));

        // Hiển thị avatar
        displayUserAvatar(user.getPhotoUrl(), false);
    }

    @Override
    public void showAvatarUploadProgress(int progress) {
        if (getContext() == null || avatarUploadProgress == null) return;

        if (progress > 0 && progress < 100) {
            // Hiển thị ProgressBar khi đang tải
            avatarUploadProgress.setVisibility(View.VISIBLE);
        } else {
            // Ẩn ProgressBar khi tải xong hoặc lỗi
            avatarUploadProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public void showProfileUpdateResult(boolean success, String message) {
        if (getContext() == null) return;

        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật lại thông tin profile mỗi khi fragment được hiển thị lại
        if (presenter != null) {
            presenter.refreshUserProfile();
        } else {
            loadUserProfile();
        }
    }
}
