package com.vhn.doan.presentation.settings.account;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.R;
import com.vhn.doan.presentation.auth.LoginActivity;
import com.vhn.doan.presentation.base.BaseActivity;
import com.vhn.doan.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity quản lý tài khoản
 */
public class AccountManagementActivity extends BaseActivity {

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
    private com.google.android.material.card.MaterialCardView pendingDeletionContainer;
    private TextView tvPendingDeletionStatus;
    private LinearLayout btnCancelDeletion;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        sessionManager = new SessionManager(this);

        setupViews();
        loadUserData();
        checkPendingDeletion();
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

        // Pending deletion views
        pendingDeletionContainer = findViewById(R.id.pendingDeletionContainer);
        tvPendingDeletionStatus = findViewById(R.id.tvPendingDeletionStatus);
        btnCancelDeletion = findViewById(R.id.btnCancelDeletion);

        // Setup toolbar
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void checkPendingDeletion() {
        if (sessionManager.isPendingDeletion()) {
            // Show pending deletion warning
            if (pendingDeletionContainer != null) {
                pendingDeletionContainer.setVisibility(android.view.View.VISIBLE);
            }

            String remainingTime = sessionManager.getFormattedRemainingTime();
            String message = "⚠️ Tài khoản của bạn sẽ bị xóa sau: " + remainingTime;

            if (tvPendingDeletionStatus != null) {
                tvPendingDeletionStatus.setText(message);
            } else {
                // Fallback: Show in toast
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }

            // Check if grace period has expired
            if (sessionManager.getRemainingTimeUntilDeletion() <= 0) {
                // Automatically perform deletion
                performAutomaticDeletion();
            }
        } else {
            if (pendingDeletionContainer != null) {
                pendingDeletionContainer.setVisibility(android.view.View.GONE);
            }
        }
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
            Intent intent = new Intent(this, com.vhn.doan.presentation.profile.EditProfileActivity.class);
            startActivity(intent);
        });

        // Change Password
        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
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

        // Cancel Deletion
        if (btnCancelDeletion != null) {
            btnCancelDeletion.setOnClickListener(v -> {
                showCancelDeletionDialog();
            });
        }
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
            .setTitle("Yêu cầu xóa tài khoản")
            .setMessage("⚠️ CẢNH BÁO: Tài khoản của bạn sẽ được đặt vào trạng thái chờ xóa trong 3 ngày.\n\n" +
                    "Trong thời gian này:\n" +
                    "• Bạn vẫn có thể sử dụng ứng dụng bình thường\n" +
                    "• Bạn có thể hủy yêu cầu xóa bất cứ lúc nào\n" +
                    "• Sau 3 ngày, tài khoản sẽ bị xóa vĩnh viễn\n\n" +
                    "Dữ liệu bị xóa bao gồm:\n" +
                    "• Hồ sơ cá nhân\n" +
                    "• Bài viết yêu thích\n" +
                    "• Lịch sử hoạt động\n" +
                    "• Nhắc nhở sức khỏe\n\n" +
                    "ℹ️ Nếu bạn muốn xóa tài khoản ngay lập tức, vui lòng liên hệ với chúng tôi qua email hỗ trợ: support@healthtips.com")
            .setPositiveButton("Tiếp tục", (dialog, which) -> {
                requestPendingDeletion();
            })
            .setNegativeButton("Hủy", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    private void requestPendingDeletion() {
        sessionManager.requestAccountDeletion(new SessionManager.DeletionCallback() {
            @Override
            public void onSuccess(long timestamp) {
                String remainingTime = sessionManager.getFormattedRemainingTime();
                Toast.makeText(AccountManagementActivity.this,
                        "Yêu cầu xóa tài khoản đã được ghi nhận. Tài khoản sẽ bị xóa sau: " + remainingTime,
                        Toast.LENGTH_LONG).show();

                // Refresh UI to show pending deletion status
                checkPendingDeletion();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AccountManagementActivity.this,
                        "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showCancelDeletionDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Hủy yêu cầu xóa tài khoản")
            .setMessage("Bạn có chắc chắn muốn hủy yêu cầu xóa tài khoản?\n\n" +
                    "Tài khoản của bạn sẽ được giữ lại và tiếp tục hoạt động bình thường.")
            .setPositiveButton("Hủy yêu cầu xóa", (dialog, which) -> {
                performCancelDeletion();
            })
            .setNegativeButton("Quay lại", null)
            .show();
    }

    private void performCancelDeletion() {
        sessionManager.cancelAccountDeletion(new SessionManager.DeletionCallback() {
            @Override
            public void onSuccess(long timestamp) {
                Toast.makeText(AccountManagementActivity.this,
                        "Đã hủy yêu cầu xóa tài khoản. Tài khoản của bạn sẽ được giữ lại.",
                        Toast.LENGTH_LONG).show();

                // Refresh UI to hide pending deletion status
                checkPendingDeletion();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AccountManagementActivity.this,
                        "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performAutomaticDeletion() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Thời gian chờ đã hết")
            .setMessage("Thời gian chờ 3 ngày đã hết. Tài khoản của bạn sẽ bị xóa vĩnh viễn ngay bây giờ.")
            .setPositiveButton("Xóa tài khoản", (dialog, which) -> {
                performDeleteAccount();
            })
            .setCancelable(false)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    private void performLogout() {
        // Clear session first
        sessionManager.clearSession();

        // Then sign out from Firebase
        mAuth.signOut();

        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();

        // Navigate to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void performDeleteAccount() {
        sessionManager.performAccountDeletion(new SessionManager.DeletionCallback() {
            @Override
            public void onSuccess(long timestamp) {
                Toast.makeText(AccountManagementActivity.this,
                        "Tài khoản đã được xóa vĩnh viễn", Toast.LENGTH_LONG).show();

                // Navigate to login screen
                Intent intent = new Intent(AccountManagementActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AccountManagementActivity.this,
                        "Lỗi khi xóa tài khoản: " + error +
                        "\n\nVui lòng đăng nhập lại hoặc liên hệ hỗ trợ: support@healthtips.com",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
