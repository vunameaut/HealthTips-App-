package com.vhn.doan.presentation.settings.account;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.vhn.doan.R;
import com.vhn.doan.presentation.base.BaseActivity;
import com.vhn.doan.utils.SessionManager;

import java.util.List;

/**
 * Activity quản lý Quyền và Bảo mật
 * Gộp chung các tính năng bảo mật và quyền truy cập
 */
public class SecurityAndPermissionsActivity extends BaseActivity {

    private static final String PREFS_NAME = "SecuritySettings";
    private static final String KEY_AUTO_LOGOUT = "auto_logout_enabled";
    private static final String KEY_ENCRYPT_DATA = "encrypt_data_enabled";
    private static final String KEY_SECURE_MODE = "secure_mode_enabled";
    private static final String KEY_SUSPICIOUS_ALERT = "suspicious_alert_enabled";

    // Security Views
    private MaterialToolbar toolbar;
    private LinearLayout btnActiveSessions;
    private TextView tvSessionCount;
    private SwitchMaterial switchAutoLogout;
    private SwitchMaterial switchEncryptData;
    private SwitchMaterial switchSecureMode;
    private LinearLayout btnLoginHistory;
    private SwitchMaterial switchSuspiciousAlert;

    // Permissions Views
    private LinearLayout btnStoragePermission;
    private LinearLayout btnCameraPermission;
    private LinearLayout btnNotificationPermission;
    private LinearLayout btnLocationPermission;
    private LinearLayout btnContactsPermission;
    private LinearLayout btnMicrophonePermission;
    private LinearLayout btnOpenSystemSettings;
    private LinearLayout btnResetPermissions;

    private TextView tvStorageStatus;
    private TextView tvCameraStatus;
    private TextView tvNotificationStatus;
    private TextView tvLocationStatus;
    private TextView tvContactsStatus;
    private TextView tvMicrophoneStatus;

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;
    private com.vhn.doan.utils.EncryptionManager encryptionManager;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_and_permissions);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        encryptionManager = new com.vhn.doan.utils.EncryptionManager(this);
        sessionManager = new SessionManager(this);

        setupViews();
        loadSecuritySettings();
        updatePermissionStatus();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionStatus();
    }

    private void setupViews() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Security Views
        btnActiveSessions = findViewById(R.id.btnActiveSessions);
        tvSessionCount = findViewById(R.id.tvSessionCount);
        switchAutoLogout = findViewById(R.id.switchAutoLogout);
        switchEncryptData = findViewById(R.id.switchEncryptData);
        switchSecureMode = findViewById(R.id.switchSecureMode);
        btnLoginHistory = findViewById(R.id.btnLoginHistory);
        switchSuspiciousAlert = findViewById(R.id.switchSuspiciousAlert);

        // Permissions Views
        btnStoragePermission = findViewById(R.id.btnStoragePermission);
        btnCameraPermission = findViewById(R.id.btnCameraPermission);
        btnNotificationPermission = findViewById(R.id.btnNotificationPermission);
        btnLocationPermission = findViewById(R.id.btnLocationPermission);
        btnContactsPermission = findViewById(R.id.btnContactsPermission);
        btnMicrophonePermission = findViewById(R.id.btnMicrophonePermission);
        btnOpenSystemSettings = findViewById(R.id.btnOpenSystemSettings);
        btnResetPermissions = findViewById(R.id.btnResetPermissions);

        tvStorageStatus = findViewById(R.id.tvStorageStatus);
        tvCameraStatus = findViewById(R.id.tvCameraStatus);
        tvNotificationStatus = findViewById(R.id.tvNotificationStatus);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);
        tvContactsStatus = findViewById(R.id.tvContactsStatus);
        tvMicrophoneStatus = findViewById(R.id.tvMicrophoneStatus);
    }

    private void loadSecuritySettings() {
        switchAutoLogout.setChecked(preferences.getBoolean(KEY_AUTO_LOGOUT, false));
        switchEncryptData.setChecked(preferences.getBoolean(KEY_ENCRYPT_DATA, true));
        switchSecureMode.setChecked(preferences.getBoolean(KEY_SECURE_MODE, false));
        switchSuspiciousAlert.setChecked(preferences.getBoolean(KEY_SUSPICIOUS_ALERT, true));

        // Load actual session count
        loadSessionCount();
    }

    private void loadSessionCount() {
        sessionManager.getActiveSessions(new SessionManager.SessionCallback() {
            @Override
            public void onSessionsLoaded(List<SessionManager.SessionInfo> sessions) {
                int count = sessions.size();
                if (count == 0) {
                    tvSessionCount.setText("Không có phiên nào");
                } else if (count == 1) {
                    tvSessionCount.setText("1 thiết bị");
                } else {
                    tvSessionCount.setText(count + " thiết bị");
                }
            }

            @Override
            public void onError(String error) {
                tvSessionCount.setText("Không thể tải");
            }
        });
    }

    private void updatePermissionStatus() {
        // Storage
        updatePermissionStatusView(tvStorageStatus, isStoragePermissionGranted());

        // Camera
        updatePermissionStatusView(tvCameraStatus,
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);

        // Notification
        updatePermissionStatusView(tvNotificationStatus, true);

        // Location
        updatePermissionStatusView(tvLocationStatus,
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

        // Contacts
        updatePermissionStatusView(tvContactsStatus,
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);

        // Microphone
        updatePermissionStatusView(tvMicrophoneStatus,
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
    }

    private void setupListeners() {
        // Security Listeners
        setupSecurityListeners();

        // Permissions Listeners
        setupPermissionsListeners();
    }

    private void setupSecurityListeners() {

        // Phiên đăng nhập
        btnActiveSessions.setOnClickListener(v -> showActiveSessionsDialog());

        // Tự động đăng xuất
        switchAutoLogout.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_AUTO_LOGOUT, isChecked);
            if (isChecked) {
                showAutoLogoutTimeDialog();
            }
        });

        // Mã hóa dữ liệu
        switchEncryptData.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_ENCRYPT_DATA, isChecked);

            // Enable/disable encryption via EncryptionManager
            encryptionManager.setEncryptionEnabled(isChecked);

            if (isChecked) {
                // Show migration dialog
                showEncryptionMigrationDialog();
            } else {
                Toast.makeText(this, "Đã tắt mã hóa dữ liệu. Dữ liệu mới sẽ không được mã hóa.",
                        Toast.LENGTH_LONG).show();
            }
        });

        // Chế độ bảo mật
        switchSecureMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_SECURE_MODE, isChecked);

            if (isChecked) {
                // Apply FLAG_SECURE immediately
                getWindow().setFlags(
                        android.view.WindowManager.LayoutParams.FLAG_SECURE,
                        android.view.WindowManager.LayoutParams.FLAG_SECURE
                );
                Toast.makeText(this, "✅ Đã bật chế độ bảo mật cao\n📵 Đã chặn chụp màn hình và quay màn hình",
                        Toast.LENGTH_LONG).show();
            } else {
                // Remove FLAG_SECURE immediately
                getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);
                Toast.makeText(this, "Đã tắt chế độ bảo mật cao",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Lịch sử đăng nhập
        btnLoginHistory.setOnClickListener(v -> showLoginHistoryDialog());

        // Cảnh báo đăng nhập
        switchSuspiciousAlert.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_SUSPICIOUS_ALERT, isChecked);
            Toast.makeText(this, isChecked ? "Đã bật cảnh báo đăng nhập lạ" : "Đã tắt cảnh báo đăng nhập lạ",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void setupPermissionsListeners() {
        // Storage Permission
        btnStoragePermission.setOnClickListener(v -> requestStoragePermission());

        // Camera Permission
        btnCameraPermission.setOnClickListener(v -> requestCameraPermission());

        // Notification Permission
        btnNotificationPermission.setOnClickListener(v -> openNotificationSettings());

        // Location Permission
        btnLocationPermission.setOnClickListener(v -> requestLocationPermission());

        // Contacts Permission
        btnContactsPermission.setOnClickListener(v -> requestContactsPermission());

        // Microphone Permission
        btnMicrophonePermission.setOnClickListener(v -> requestMicrophonePermission());

        // Open System Settings
        btnOpenSystemSettings.setOnClickListener(v -> openAppSystemSettings());

        // Reset Permissions
        btnResetPermissions.setOnClickListener(v -> showResetPermissionsDialog());
    }

    // Security Methods
    private void showChangePasswordDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Đổi mật khẩu")
                .setMessage("Bạn sẽ được chuyển đến màn hình đổi mật khẩu. Tiếp tục?")
                .setMessage("Chọn phương thức xác thực:")
                .setPositiveButton("SMS", (dialog, which) -> {
                    Toast.makeText(this, "Đang thiết lập xác thực qua SMS...", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Email", (dialog, which) -> {
                    Toast.makeText(this, "Đang thiết lập xác thực qua Email...", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Hủy", null)
                .show();
    }

    private void showActiveSessionsDialog() {
        // Show loading dialog first
        android.app.ProgressDialog loadingDialog = new android.app.ProgressDialog(this);
        loadingDialog.setMessage("Đang tải phiên hoạt động...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        // Load actual data from SessionManager
        sessionManager.getActiveSessions(new SessionManager.SessionCallback() {
            @Override
            public void onSessionsLoaded(List<SessionManager.SessionInfo> sessions) {
                loadingDialog.dismiss();

                if (sessions.isEmpty()) {
                    new MaterialAlertDialogBuilder(SecurityAndPermissionsActivity.this)
                            .setTitle("Phiên đăng nhập đang hoạt động")
                            .setMessage("Không có phiên đăng nhập nào")
                            .setPositiveButton("Đóng", null)
                            .show();
                    return;
                }

                // Update session count in UI
                tvSessionCount.setText(sessions.size() + " thiết bị");

                // Build message from actual sessions
                StringBuilder message = new StringBuilder();
                int otherSessionsCount = 0;

                for (int i = 0; i < sessions.size(); i++) {
                    SessionManager.SessionInfo session = sessions.get(i);
                    message.append("• ").append(session.deviceName);

                    if (session.isCurrentDevice) {
                        message.append(" (Thiết bị này)");
                    } else {
                        otherSessionsCount++;
                    }

                    message.append("\n  Đăng nhập: ").append(session.getFormattedLoginTime())
                            .append("\n  Hoạt động: ").append(session.getFormattedLastActive())
                            .append("\n  Android ").append(session.androidVersion);

                    if (session.location != null && !session.location.equals("N/A")) {
                        message.append("\n  Vị trí: ").append(session.location);
                    }

                    if (i < sessions.size() - 1) {
                        message.append("\n\n");
                    }
                }

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(SecurityAndPermissionsActivity.this)
                        .setTitle("Phiên đăng nhập đang hoạt động")
                        .setMessage(message.toString())
                        .setPositiveButton("Đóng", null);

                // Only show "Logout all others" if there are other sessions
                if (otherSessionsCount > 0) {
                    builder.setNegativeButton("Đăng xuất " + otherSessionsCount + " thiết bị khác", (dialog, which) -> {
                        sessionManager.logoutAllOtherSessions(new SessionManager.LogoutCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(SecurityAndPermissionsActivity.this,
                                        "Đã đăng xuất khỏi tất cả thiết bị khác", Toast.LENGTH_SHORT).show();
                                // Refresh session count
                                tvSessionCount.setText("1 thiết bị");
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(SecurityAndPermissionsActivity.this,
                                        "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }

                builder.show();
            }

            @Override
            public void onError(String error) {
                loadingDialog.dismiss();
                Toast.makeText(SecurityAndPermissionsActivity.this,
                        "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAutoLogoutTimeDialog() {
        String[] options = {"5 phút", "15 phút", "30 phút", "1 giờ", "Không bao giờ"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Thời gian tự động đăng xuất")
                .setItems(options, (dialog, which) -> {
                    Toast.makeText(this, "Đã đặt: " + options[which], Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showLoginHistoryDialog() {
        // Show loading dialog first
        android.app.ProgressDialog loadingDialog = new android.app.ProgressDialog(this);
        loadingDialog.setMessage("Đang tải lịch sử đăng nhập...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        // Load actual data from SessionManager
        sessionManager.getActiveSessions(new SessionManager.SessionCallback() {
            @Override
            public void onSessionsLoaded(List<SessionManager.SessionInfo> sessions) {
                loadingDialog.dismiss();

                if (sessions.isEmpty()) {
                    new MaterialAlertDialogBuilder(SecurityAndPermissionsActivity.this)
                            .setTitle("Lịch sử đăng nhập")
                            .setMessage("Không có lịch sử đăng nhập")
                            .setPositiveButton("Đóng", null)
                            .show();
                    return;
                }

                // Build message from actual sessions
                StringBuilder message = new StringBuilder();
                for (int i = 0; i < sessions.size(); i++) {
                    SessionManager.SessionInfo session = sessions.get(i);
                    message.append("• ").append(session.getFormattedLoginTime());

                    if (session.isCurrentDevice) {
                        message.append(" (Thiết bị này)");
                    }

                    message.append("\n  ")
                            .append(session.deviceName)
                            .append("\n  ")
                            .append("Android ").append(session.androidVersion)
                            .append("\n  Hoạt động lần cuối: ").append(session.getFormattedLastActive());

                    if (session.location != null && !session.location.equals("N/A")) {
                        message.append("\n  Vị trí: ").append(session.location);
                    }

                    if (i < sessions.size() - 1) {
                        message.append("\n\n");
                    }
                }

                new MaterialAlertDialogBuilder(SecurityAndPermissionsActivity.this)
                        .setTitle("Lịch sử đăng nhập")
                        .setMessage(message.toString())
                        .setPositiveButton("Đóng", null)
                        .show();
            }

            @Override
            public void onError(String error) {
                loadingDialog.dismiss();
                Toast.makeText(SecurityAndPermissionsActivity.this,
                        "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEncryptionMigrationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Mã hóa dữ liệu")
                .setMessage("✅ Đã bật mã hóa dữ liệu!\n\n" +
                        "Tất cả dữ liệu nhạy cảm mới sẽ được mã hóa bằng AES-256.\n\n" +
                        "Bạn có muốn di chuyển dữ liệu hiện có sang kho lưu trữ được mã hóa không?\n\n" +
                        "⚠️ Khuyến nghị: Nên di chuyển để bảo vệ tối đa dữ liệu của bạn.")
                .setPositiveButton("Di chuyển ngay", (dialog, which) -> {
                    // Migrate existing data
                    encryptionManager.migrateToEncryptedStorage("SessionPrefs");
                    Toast.makeText(this, "Đã di chuyển dữ liệu sang kho được mã hóa", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Để sau", (dialog, which) -> {
                    Toast.makeText(this, "Chỉ dữ liệu mới sẽ được mã hóa", Toast.LENGTH_SHORT).show();
                })
                .setIcon(android.R.drawable.ic_lock_lock)
                .show();
    }

    // Permission Methods
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        } else {
            // Android 10 and below
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    100);
        }
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        } else {
            Toast.makeText(this, "Quyền camera đã được cấp", Toast.LENGTH_SHORT).show();
        }
    }

    private void openNotificationSettings() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
        }
        startActivity(intent);
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    102);
        } else {
            Toast.makeText(this, "Quyền vị trí đã được cấp", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestContactsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 103);
        } else {
            Toast.makeText(this, "Quyền danh bạ đã được cấp", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 104);
        } else {
            Toast.makeText(this, "Quyền micro đã được cấp", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAppSystemSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void showResetPermissionsDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Đặt lại quyền")
                .setMessage("Bạn có muốn đặt lại tất cả quyền về mặc định? Điều này sẽ yêu cầu bạn cấp quyền lại khi cần.")
                .setPositiveButton("Đặt lại", (dialog, which) -> {
                    openAppSystemSettings();
                    Toast.makeText(this, "Vui lòng xóa quyền trong cài đặt hệ thống", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Helper Methods
    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return android.os.Environment.isExternalStorageManager();
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void updatePermissionStatusView(TextView textView, boolean granted) {
        if (granted) {
            textView.setText(R.string.permission_status_granted);
            textView.setTextColor(ContextCompat.getColor(this, R.color.success_color));
        } else {
            textView.setText(R.string.permission_status_denied);
            textView.setTextColor(ContextCompat.getColor(this, R.color.error_color));
        }
    }

    private void saveSetting(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        updatePermissionStatus();

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Quyền đã được cấp", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Quyền bị từ chối", Toast.LENGTH_SHORT).show();
        }
    }
}

