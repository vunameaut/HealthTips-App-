package com.vhn.doan.presentation.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.vhn.doan.R;
import com.vhn.doan.data.User;
import com.vhn.doan.data.repository.UserRepository;
import com.vhn.doan.data.repository.UserRepositoryImpl;
import com.vhn.doan.utils.AvatarUtils;

/**
 * Activity để chỉnh sửa thông tin cá nhân
 * Tuân thủ mô hình MVP
 */
public class EditProfileActivity extends AppCompatActivity implements EditProfileContract.View {

    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final int REQUEST_PICK_IMAGE = 200;

    private EditProfileContract.Presenter presenter;
    private ImageView profileImage;
    private FloatingActionButton btnChangeAvatar;
    private TextInputEditText editDisplayName, editPhone;
    private TextView txtEmail;
    private Button btnSave;
    private ProgressBar avatarUploadProgress;
    private FrameLayout loadingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initializeViews();
        setupToolbar();
        setupListeners();
        initPresenter();

        // Tải thông tin người dùng
        presenter.loadUserProfile();
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profile_image);
        btnChangeAvatar = findViewById(R.id.btn_change_avatar);
        editDisplayName = findViewById(R.id.edit_display_name);
        editPhone = findViewById(R.id.edit_phone);
        txtEmail = findViewById(R.id.txt_email);
        btnSave = findViewById(R.id.btn_save);
        avatarUploadProgress = findViewById(R.id.avatar_upload_progress);
        loadingView = findViewById(R.id.loading_view);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupListeners() {
        btnChangeAvatar.setOnClickListener(v -> checkPermissionAndPickImage());

        btnSave.setOnClickListener(v -> {
            String displayName = editDisplayName.getText() != null ? editDisplayName.getText().toString().trim() : "";
            String phone = editPhone.getText() != null ? editPhone.getText().toString().trim() : "";

            presenter.updateUserProfile(displayName, phone);
        });
    }

    private void initPresenter() {
        UserRepository userRepository = new UserRepositoryImpl();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        presenter = new EditProfilePresenterImpl(userRepository, firebaseAuth);
        presenter.attachView(this);
        ((EditProfilePresenterImpl) presenter).setContext(this);
        presenter.start();
    }

    /**
     * Kiểm tra quyền truy cập bộ nhớ và mở gallery để chọn ảnh
     * Nếu chưa được cấp quyền, hiển thị dialog yêu cầu quyền truy cập
     */
    private void checkPermissionAndPickImage() {
        String permission;

        // Từ Android 13 (API 33) trở đi, sử dụng READ_MEDIA_IMAGES thay vì READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            // Hiển thị giải thích tại sao cần quyền truy cập
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Cần cấp quyền truy cập")
                        .setMessage("Để thay đổi ảnh đại diện, ứng dụng cần quyền truy cập vào bộ nhớ thiết bị của bạn.")
                        .setPositiveButton("Cấp quyền", (dialog, which) -> {
                            // Yêu cầu quyền
                            ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_STORAGE_PERMISSION);
                        })
                        .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
            } else {
                // Trực tiếp yêu cầu quyền nếu không cần hiển thị giải thích
                ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_STORAGE_PERMISSION);
            }
        } else {
            // Đã có quyền, mở gallery
            openGallery();
        }
    }

    /**
     * Mở gallery để chọn ảnh
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp, mở gallery
                openGallery();
            } else {
                // Quyền bị từ chối, hiển thị thông báo và hướng dẫn người dùng cấp quyền trong cài đặt
                Toast.makeText(this, "Cần quyền truy cập bộ nhớ để thay đổi avatar", Toast.LENGTH_SHORT).show();
                showSettingsPermissionDialog();
            }
        }
    }

    /**
     * Hiển thị dialog hướng dẫn người dùng vào cài đặt để cấp quyền
     */
    private void showSettingsPermissionDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cấp quyền truy cập")
                .setMessage("Để thay đổi ảnh đại diện, bạn cần cấp quyền truy cập vào bộ nhớ thiết bị trong cài đặt ứng dụng.")
                .setPositiveButton("Vào cài đặt", (dialog, which) -> {
                    // Mở màn hình cài đặt của ứng dụng
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                // Hiển thị ảnh đã chọn và cập nhật lên server
                showCroppedImage(imageUri);
                presenter.updateUserAvatar(imageUri);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    // Triển khai các phương thức của View interface

    @Override
    public void showLoading(boolean isLoading) {
        loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void displayUserProfile(User user) {
        if (user != null) {
            editDisplayName.setText(user.getDisplayName());
            editPhone.setText(user.getPhoneNumber());
            txtEmail.setText("Email: " + (user.getEmail() != null ? user.getEmail() : ""));
        }
    }

    @Override
    public void displayUserAvatar(String avatarUrl) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .into(profileImage);
        }
    }

    @Override
    public void showAvatarUploadProgress(int progress) {
        if (progress > 0 && progress < 100) {
            avatarUploadProgress.setVisibility(View.VISIBLE);
            avatarUploadProgress.setProgress(progress);
        } else if (progress == 100) {
            avatarUploadProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onProfileUpdateSuccess() {
        Toast.makeText(this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void showCroppedImage(Uri imageUri) {
        Glide.with(this)
                .load(imageUri)
                .apply(RequestOptions.circleCropTransform())
                .into(profileImage);
    }
}
