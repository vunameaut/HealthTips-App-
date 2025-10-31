package com.vhn.doan.presentation.settings.account;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vhn.doan.R;

/**
 * Activity quản lý quyền ứng dụng
 */
public class PermissionsSettingsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_settings);

        setupViews();
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

        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void updatePermissionStatus() {
        // Storage
        updatePermissionStatusView(tvStorageStatus, isStoragePermissionGranted());

        // Camera
        updatePermissionStatusView(tvCameraStatus,
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);

        // Notification
        updatePermissionStatusView(tvNotificationStatus, true); // Notifications are always "granted" in terms of showing

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

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void updatePermissionStatusView(TextView textView, boolean isGranted) {
        if (isGranted) {
            textView.setText(R.string.permission_status_granted);
            textView.setTextColor(ContextCompat.getColor(this, R.color.primary_green));
        } else {
            textView.setText(R.string.permission_status_denied);
            textView.setTextColor(ContextCompat.getColor(this, R.color.error_color));
        }
    }

    private void setupListeners() {
        btnStoragePermission.setOnClickListener(v -> showPermissionExplanation(
            "Quyền truy cập bộ nhớ",
            "Ứng dụng cần quyền này để:\n• Lưu trữ ảnh đại diện\n• Lưu dữ liệu offline\n• Xuất dữ liệu sức khỏe",
            "storage"
        ));

        btnCameraPermission.setOnClickListener(v -> showPermissionExplanation(
            "Quyền truy cập camera",
            "Ứng dụng cần quyền này để:\n• Chụp ảnh đại diện\n• Đính kèm ảnh trong báo cáo vấn đề",
            "camera"
        ));

        btnNotificationPermission.setOnClickListener(v -> showPermissionExplanation(
            "Quyền gửi thông báo",
            "Ứng dụng cần quyền này để:\n• Nhắc nhở sức khỏe\n• Thông báo nội dung mới\n• Cảnh báo quan trọng",
            "notification"
        ));

        btnLocationPermission.setOnClickListener(v -> showPermissionExplanation(
            "Quyền truy cập vị trí",
            "Ứng dụng cần quyền này để:\n• Cung cấp nội dung phù hợp với khu vực\n• Gợi ý phòng khám gần bạn",
            "location"
        ));

        btnContactsPermission.setOnClickListener(v -> showPermissionExplanation(
            "Quyền truy cập danh bạ",
            "Ứng dụng cần quyền này để:\n• Chia sẻ mẹo sức khỏe với bạn bè\n• Mời bạn bè tham gia",
            "contacts"
        ));

        btnMicrophonePermission.setOnClickListener(v -> showPermissionExplanation(
            "Quyền truy cập microphone",
            "Ứng dụng cần quyền này để:\n• Tìm kiếm bằng giọng nói\n• Ghi chú sức khỏe bằng giọng nói",
            "microphone"
        ));

        btnOpenSystemSettings.setOnClickListener(v -> openAppSettings());

        btnResetPermissions.setOnClickListener(v -> showResetPermissionsDialog());
    }

    private void showPermissionExplanation(String title, String message, String permissionType) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Mở cài đặt", (dialog, which) -> openAppSettings())
            .setNegativeButton("Đóng", null)
            .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void showResetPermissionsDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Đặt lại quyền")
            .setMessage("Bạn có chắc chắn muốn đặt lại tất cả quyền?\n\nỨng dụng sẽ yêu cầu lại các quyền cần thiết khi bạn sử dụng các tính năng liên quan.")
            .setPositiveButton("Đặt lại", (dialog, which) -> {
                Toast.makeText(this, "Vui lòng vào cài đặt hệ thống để thu hồi quyền", Toast.LENGTH_LONG).show();
                openAppSettings();
            })
            .setNegativeButton("Hủy", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }
}
