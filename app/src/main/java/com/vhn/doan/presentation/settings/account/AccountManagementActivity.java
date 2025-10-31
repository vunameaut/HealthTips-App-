package com.vhn.doan.presentation.settings.account;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.R;
import com.vhn.doan.presentation.auth.LoginActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity quản lý tài khoản
 */
public class AccountManagementActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvEmail;
    private TextView tvDisplayName;
    private TextView tvCreatedDate;
    private LinearLayout btnEditProfile;
    private LinearLayout btnChangePassword;
    private LinearLayout btnTwoFactorAuth;
    private SwitchMaterial switchTwoFactor;
    private LinearLayout btnLogout;
    private LinearLayout btnDeleteAccount;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        setupViews();
        loadUserData();
        setupListeners();
    }

    private void setupViews() {
        toolbar = findViewById(R.id.toolbar);
        tvEmail = findViewById(R.id.tvEmail);
        tvDisplayName = findViewById(R.id.tvDisplayName);
        tvCreatedDate = findViewById(R.id.tvCreatedDate);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnTwoFactorAuth = findViewById(R.id.btnTwoFactorAuth);
        switchTwoFactor = findViewById(R.id.switchTwoFactor);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        // Setup toolbar
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadUserData() {
        if (currentUser != null) {
            // Load email
            String email = currentUser.getEmail();
            if (email != null && !email.isEmpty()) {
                tvEmail.setText(email);
            }

            // Load display name
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvDisplayName.setText(displayName);
            } else {
                tvDisplayName.setText(getString(R.string.account_display_name_placeholder));
            }

            // Load creation date
            long creationTimestamp = currentUser.getMetadata() != null
                ? currentUser.getMetadata().getCreationTimestamp()
                : System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvCreatedDate.setText(sdf.format(new Date(creationTimestamp)));
        }
    }

    private void setupListeners() {
        // Edit Profile
        btnEditProfile.setOnClickListener(v -> {
            // TODO: Navigate to Edit Profile Activity
            Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Change Password
        btnChangePassword.setOnClickListener(v -> {
            showChangePasswordDialog();
        });

        // Two Factor Auth
        btnTwoFactorAuth.setOnClickListener(v -> {
            switchTwoFactor.setChecked(!switchTwoFactor.isChecked());
        });

        switchTwoFactor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showTwoFactorSetupDialog();
            } else {
                // Disable 2FA
                Toast.makeText(this, "Đã tắt xác thực hai yếu tố", Toast.LENGTH_SHORT).show();
            }
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            showLogoutConfirmDialog();
        });

        // Delete Account
        btnDeleteAccount.setOnClickListener(v -> {
            showDeleteAccountConfirmDialog();
        });
    }

    private void showChangePasswordDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Đổi mật khẩu")
            .setMessage("Bạn sẽ nhận được email hướng dẫn đặt lại mật khẩu")
            .setPositiveButton("Gửi email", (dialog, which) -> {
                if (currentUser != null && currentUser.getEmail() != null) {
                    mAuth.sendPasswordResetEmail(currentUser.getEmail())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Đã gửi email đặt lại mật khẩu", Toast.LENGTH_LONG).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showTwoFactorSetupDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Xác thực hai yếu tố")
            .setMessage("Chức năng xác thực hai yếu tố sẽ được phát triển trong phiên bản tiếp theo")
            .setPositiveButton("OK", (dialog, which) -> {
                switchTwoFactor.setChecked(false);
            })
            .show();
    }

    private void showLogoutConfirmDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc chắn muốn đăng xuất?")
            .setPositiveButton("Đăng xuất", (dialog, which) -> {
                performLogout();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showDeleteAccountConfirmDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Xóa tài khoản")
            .setMessage("⚠️ CẢNH BÁO: Hành động này không thể hoàn tác!\n\nTất cả dữ liệu của bạn sẽ bị xóa vĩnh viễn, bao gồm:\n• Hồ sơ cá nhân\n• Bài viết yêu thích\n• Lịch sử hoạt động\n• Nhắc nhở sức khỏe\n\nBạn có chắc chắn muốn tiếp tục?")
            .setPositiveButton("Xóa tài khoản", (dialog, which) -> {
                showFinalDeleteConfirmation();
            })
            .setNegativeButton("Hủy", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    private void showFinalDeleteConfirmation() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Xác nhận lần cuối")
            .setMessage("Nhập 'XOA' để xác nhận xóa tài khoản")
            .setView(R.layout.dialog_reminder) // Temporary, should create proper input dialog
            .setPositiveButton("Xác nhận", (dialog, which) -> {
                performDeleteAccount();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void performLogout() {
        mAuth.signOut();
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();

        // Navigate to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void performDeleteAccount() {
        if (currentUser != null) {
            currentUser.delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tài khoản đã được xóa", Toast.LENGTH_LONG).show();

                    // Navigate to login screen
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage() + "\nVui lòng đăng nhập lại để xóa tài khoản", Toast.LENGTH_LONG).show();
                });
        }
    }
}
