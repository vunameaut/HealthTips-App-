package com.vhn.doan.presentation.settings.account;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.vhn.doan.R;

/**
 * Activity cài đặt bảo mật
 */
public class SecuritySettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SecuritySettings";
    private static final String KEY_TWO_FACTOR = "two_factor_enabled";
    private static final String KEY_FINGERPRINT = "fingerprint_enabled";
    private static final String KEY_AUTO_LOGOUT = "auto_logout_enabled";
    private static final String KEY_ENCRYPT_DATA = "encrypt_data_enabled";
    private static final String KEY_SECURE_MODE = "secure_mode_enabled";
    private static final String KEY_SUSPICIOUS_ALERT = "suspicious_alert_enabled";

    private MaterialToolbar toolbar;
    private LinearLayout btnChangePassword;
    private LinearLayout btnTwoFactor;
    private SwitchMaterial switchTwoFactor;
    private SwitchMaterial switchFingerprint;
    private LinearLayout btnActiveSessions;
    private TextView tvSessionCount;
    private SwitchMaterial switchAutoLogout;
    private SwitchMaterial switchEncryptData;
    private SwitchMaterial switchSecureMode;
    private LinearLayout btnLoginHistory;
    private SwitchMaterial switchSuspiciousAlert;

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_settings);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();

        setupViews();
        loadSettings();
        setupListeners();
    }

    private void setupViews() {
        toolbar = findViewById(R.id.toolbar);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnTwoFactor = findViewById(R.id.btnTwoFactor);
        switchTwoFactor = findViewById(R.id.switchTwoFactor);
        switchFingerprint = findViewById(R.id.switchFingerprint);
        btnActiveSessions = findViewById(R.id.btnActiveSessions);
        tvSessionCount = findViewById(R.id.tvSessionCount);
        switchAutoLogout = findViewById(R.id.switchAutoLogout);
        switchEncryptData = findViewById(R.id.switchEncryptData);
        switchSecureMode = findViewById(R.id.switchSecureMode);
        btnLoginHistory = findViewById(R.id.btnLoginHistory);
        switchSuspiciousAlert = findViewById(R.id.switchSuspiciousAlert);

        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void loadSettings() {
        switchTwoFactor.setChecked(preferences.getBoolean(KEY_TWO_FACTOR, false));
        switchFingerprint.setChecked(preferences.getBoolean(KEY_FINGERPRINT, false));
        switchAutoLogout.setChecked(preferences.getBoolean(KEY_AUTO_LOGOUT, false));
        switchEncryptData.setChecked(preferences.getBoolean(KEY_ENCRYPT_DATA, true));
        switchSecureMode.setChecked(preferences.getBoolean(KEY_SECURE_MODE, false));
        switchSuspiciousAlert.setChecked(preferences.getBoolean(KEY_SUSPICIOUS_ALERT, true));

        // Mock session count
        tvSessionCount.setText("1 thiết bị");
    }

    private void setupListeners() {
        // Đổi mật khẩu
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Xác thực hai yếu tố
        btnTwoFactor.setOnClickListener(v -> switchTwoFactor.setChecked(!switchTwoFactor.isChecked()));
        
        switchTwoFactor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_TWO_FACTOR, isChecked);
            if (isChecked) {
                showTwoFactorSetupDialog();
            } else {
                Toast.makeText(this, "Đã tắt xác thực hai yếu tố", Toast.LENGTH_SHORT).show();
            }
        });

        // Vân tay
        switchFingerprint.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_FINGERPRINT, isChecked);
            String message = isChecked ? "Đã bật đăng nhập bằng vân tay" : "Đã tắt đăng nhập bằng vân tay";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        // Phiên đang hoạt động
        btnActiveSessions.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Tự động đăng xuất
        switchAutoLogout.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_AUTO_LOGOUT, isChecked);
            String message = isChecked ? "Sẽ tự động đăng xuất sau 15 phút không hoạt động" : "Đã tắt tự động đăng xuất";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        // Mã hóa dữ liệu
        switchEncryptData.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_ENCRYPT_DATA, isChecked);
            String message = isChecked ? "Đã bật mã hóa dữ liệu" : "Đã tắt mã hóa dữ liệu";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        // Chế độ bảo mật
        switchSecureMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_SECURE_MODE, isChecked);
            String message = isChecked ? "Đã bật chế độ bảo mật (chặn chụp màn hình)" : "Đã tắt chế độ bảo mật";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        // Lịch sử đăng nhập
        btnLoginHistory.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Cảnh báo hoạt động đáng ngờ
        switchSuspiciousAlert.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_SUSPICIOUS_ALERT, isChecked);
            String message = isChecked ? "Bạn sẽ nhận thông báo về hoạt động đáng ngờ" : "Đã tắt cảnh báo hoạt động đáng ngờ";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    private void saveSetting(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    private void showChangePasswordDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Đổi mật khẩu")
            .setMessage("Bạn sẽ nhận được email hướng dẫn đặt lại mật khẩu")
            .setPositiveButton("Gửi email", (dialog, which) -> {
                if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
                    mAuth.sendPasswordResetEmail(mAuth.getCurrentUser().getEmail())
                        .addOnSuccessListener(aVoid -> 
                            Toast.makeText(this, "Đã gửi email đặt lại mật khẩu", Toast.LENGTH_LONG).show())
                        .addOnFailureListener(e -> 
                            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showTwoFactorSetupDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Xác thực hai yếu tố")
            .setMessage("Chức năng xác thực hai yếu tố sẽ được phát triển trong phiên bản tiếp theo.\n\nBạn sẽ cần:\n• Số điện thoại xác thực\n• Ứng dụng xác thực (Google Authenticator)\n• Mã dự phòng")
            .setPositiveButton("OK", (dialog, which) -> switchTwoFactor.setChecked(false))
            .show();
    }
}
