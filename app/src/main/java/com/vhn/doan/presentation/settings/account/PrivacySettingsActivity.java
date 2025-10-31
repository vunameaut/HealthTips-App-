package com.vhn.doan.presentation.settings.account;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.vhn.doan.R;

/**
 * Activity cài đặt quyền riêng tư
 */
public class PrivacySettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PrivacySettings";
    private static final String KEY_PUBLIC_PROFILE = "public_profile";
    private static final String KEY_SHOW_EMAIL = "show_email";
    private static final String KEY_SHOW_ACTIVITY = "show_activity";
    private static final String KEY_SHOW_LIKED_POSTS = "show_liked_posts";

    private MaterialToolbar toolbar;
    private SwitchMaterial switchPublicProfile;
    private SwitchMaterial switchShowEmail;
    private SwitchMaterial switchShowActivity;
    private SwitchMaterial switchShowLikedPosts;
    private LinearLayout btnClearSearchHistory;
    private LinearLayout btnClearViewHistory;
    private LinearLayout btnDownloadData;
    private LinearLayout btnBlockedUsers;
    private TextView tvBlockedCount;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_settings);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        setupViews();
        loadPrivacySettings();
        setupListeners();
    }

    private void setupViews() {
        toolbar = findViewById(R.id.toolbar);
        switchPublicProfile = findViewById(R.id.switchPublicProfile);
        switchShowEmail = findViewById(R.id.switchShowEmail);
        switchShowActivity = findViewById(R.id.switchShowActivity);
        switchShowLikedPosts = findViewById(R.id.switchShowLikedPosts);
        btnClearSearchHistory = findViewById(R.id.btnClearSearchHistory);
        btnClearViewHistory = findViewById(R.id.btnClearViewHistory);
        btnDownloadData = findViewById(R.id.btnDownloadData);
        btnBlockedUsers = findViewById(R.id.btnBlockedUsers);
        tvBlockedCount = findViewById(R.id.tvBlockedCount);

        // Setup toolbar
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadPrivacySettings() {
        // Load saved settings
        switchPublicProfile.setChecked(preferences.getBoolean(KEY_PUBLIC_PROFILE, true));
        switchShowEmail.setChecked(preferences.getBoolean(KEY_SHOW_EMAIL, false));
        switchShowActivity.setChecked(preferences.getBoolean(KEY_SHOW_ACTIVITY, true));
        switchShowLikedPosts.setChecked(preferences.getBoolean(KEY_SHOW_LIKED_POSTS, true));

        // Load blocked users count (mock data for now)
        updateBlockedUsersCount(0);
    }

    private void setupListeners() {
        // Privacy switches
        switchPublicProfile.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePrivacySetting(KEY_PUBLIC_PROFILE, isChecked);
            String message = isChecked ? "Hồ sơ của bạn giờ là công khai" : "Hồ sơ của bạn giờ là riêng tư";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        switchShowEmail.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePrivacySetting(KEY_SHOW_EMAIL, isChecked);
            String message = isChecked ? "Email của bạn sẽ hiển thị với người khác" : "Email của bạn đã được ẩn";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        switchShowActivity.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePrivacySetting(KEY_SHOW_ACTIVITY, isChecked);
            String message = isChecked ? "Hoạt động của bạn sẽ hiển thị" : "Hoạt động của bạn đã được ẩn";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        switchShowLikedPosts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePrivacySetting(KEY_SHOW_LIKED_POSTS, isChecked);
            String message = isChecked ? "Bài viết yêu thích sẽ hiển thị" : "Bài viết yêu thích đã được ẩn";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        // Data management buttons
        btnClearSearchHistory.setOnClickListener(v -> {
            showClearSearchHistoryDialog();
        });

        btnClearViewHistory.setOnClickListener(v -> {
            showClearViewHistoryDialog();
        });

        btnDownloadData.setOnClickListener(v -> {
            showDownloadDataDialog();
        });

        btnBlockedUsers.setOnClickListener(v -> {
            // TODO: Navigate to blocked users list
            Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void savePrivacySetting(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
        // TODO: Sync with Firebase/Firestore
    }

    private void showClearSearchHistoryDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Xóa lịch sử tìm kiếm")
            .setMessage("Bạn có chắc chắn muốn xóa toàn bộ lịch sử tìm kiếm? Hành động này không thể hoàn tác.")
            .setPositiveButton("Xóa", (dialog, which) -> {
                clearSearchHistory();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showClearViewHistoryDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Xóa lịch sử xem")
            .setMessage("Bạn có chắc chắn muốn xóa toàn bộ lịch sử các bài viết đã xem? Hành động này không thể hoàn tác.")
            .setPositiveButton("Xóa", (dialog, which) -> {
                clearViewHistory();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showDownloadDataDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Tải dữ liệu của bạn")
            .setMessage("Chúng tôi sẽ chuẩn bị một bản sao dữ liệu của bạn và gửi link tải xuống qua email trong vòng 24-48 giờ.\n\nDữ liệu bao gồm:\n• Thông tin hồ sơ\n• Bài viết yêu thích\n• Lịch sử hoạt động\n• Nhắc nhở sức khỏe")
            .setPositiveButton("Yêu cầu tải xuống", (dialog, which) -> {
                requestDataDownload();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void clearSearchHistory() {
        // TODO: Clear search history from local database and Firebase
        SharedPreferences searchPrefs = getSharedPreferences("SearchHistory", MODE_PRIVATE);
        searchPrefs.edit().clear().apply();

        Toast.makeText(this, "Đã xóa lịch sử tìm kiếm", Toast.LENGTH_SHORT).show();
    }

    private void clearViewHistory() {
        // TODO: Clear view history from local database and Firebase
        SharedPreferences viewPrefs = getSharedPreferences("ViewHistory", MODE_PRIVATE);
        viewPrefs.edit().clear().apply();

        Toast.makeText(this, "Đã xóa lịch sử xem", Toast.LENGTH_SHORT).show();
    }

    private void requestDataDownload() {
        // TODO: Request data download from server
        Toast.makeText(this, "Yêu cầu đã được gửi. Bạn sẽ nhận được email trong vòng 24-48 giờ.", Toast.LENGTH_LONG).show();
    }

    private void updateBlockedUsersCount(int count) {
        String countText = count == 0 ? "0 người dùng" : count + " người dùng";
        tvBlockedCount.setText(countText);
    }
}
