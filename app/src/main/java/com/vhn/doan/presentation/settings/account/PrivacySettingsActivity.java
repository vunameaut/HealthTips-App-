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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.R;

import androidx.annotation.NonNull;

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
    private FirebaseAuth mAuth;
    private DatabaseReference userSettingsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_settings);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase reference
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userSettingsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("privacy_settings");
        }

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
        if (userSettingsRef != null) {
            // Load from Firebase
            userSettingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Load from Firebase
                        boolean publicProfile = snapshot.child(KEY_PUBLIC_PROFILE).getValue(Boolean.class) != null
                            ? snapshot.child(KEY_PUBLIC_PROFILE).getValue(Boolean.class)
                            : true;
                        boolean showEmail = snapshot.child(KEY_SHOW_EMAIL).getValue(Boolean.class) != null
                            ? snapshot.child(KEY_SHOW_EMAIL).getValue(Boolean.class)
                            : false;
                        boolean showActivity = snapshot.child(KEY_SHOW_ACTIVITY).getValue(Boolean.class) != null
                            ? snapshot.child(KEY_SHOW_ACTIVITY).getValue(Boolean.class)
                            : true;
                        boolean showLikedPosts = snapshot.child(KEY_SHOW_LIKED_POSTS).getValue(Boolean.class) != null
                            ? snapshot.child(KEY_SHOW_LIKED_POSTS).getValue(Boolean.class)
                            : true;

                        switchPublicProfile.setChecked(publicProfile);
                        switchShowEmail.setChecked(showEmail);
                        switchShowActivity.setChecked(showActivity);
                        switchShowLikedPosts.setChecked(showLikedPosts);

                        // Also save to local SharedPreferences as cache
                        preferences.edit()
                            .putBoolean(KEY_PUBLIC_PROFILE, publicProfile)
                            .putBoolean(KEY_SHOW_EMAIL, showEmail)
                            .putBoolean(KEY_SHOW_ACTIVITY, showActivity)
                            .putBoolean(KEY_SHOW_LIKED_POSTS, showLikedPosts)
                            .apply();
                    } else {
                        // No Firebase data, load from SharedPreferences
                        loadFromLocalPreferences();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // On error, fallback to local preferences
                    loadFromLocalPreferences();
                }
            });
        } else {
            // No user logged in, load from local preferences
            loadFromLocalPreferences();
        }

        // ✅ IMPLEMENTED: Load blocked users count from Firebase
        loadBlockedUsersCount();
    }

    private void loadFromLocalPreferences() {
        switchPublicProfile.setChecked(preferences.getBoolean(KEY_PUBLIC_PROFILE, true));
        switchShowEmail.setChecked(preferences.getBoolean(KEY_SHOW_EMAIL, false));
        switchShowActivity.setChecked(preferences.getBoolean(KEY_SHOW_ACTIVITY, true));
        switchShowLikedPosts.setChecked(preferences.getBoolean(KEY_SHOW_LIKED_POSTS, true));
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
            // ✅ IMPLEMENTED: Navigate to blocked users list
            Intent intent = new Intent(this, BlockedUsersActivity.class);
            startActivity(intent);
        });
    }

    private void savePrivacySetting(String key, boolean value) {
        // Save to local SharedPreferences
        preferences.edit().putBoolean(key, value).apply();

        // Sync with Firebase
        if (userSettingsRef != null) {
            userSettingsRef.child(key).setValue(value)
                .addOnFailureListener(e -> {
                    // Silent fail, data is still in local preferences
                    android.util.Log.e("PrivacySettings", "Failed to sync to Firebase: " + e.getMessage());
                });
        }
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
        // ✅ IMPLEMENTED: Clear search history from local database and Firebase

        // 1. Clear local SharedPreferences
        SharedPreferences searchPrefs = getSharedPreferences("SearchHistory", MODE_PRIVATE);
        searchPrefs.edit().clear().apply();

        // 2. Clear from Firebase if user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference searchHistoryRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("search_history");

            searchHistoryRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✅ Đã xóa lịch sử tìm kiếm (local và cloud)", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Đã xóa local. Lỗi xóa cloud: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        } else {
            Toast.makeText(this, "Đã xóa lịch sử tìm kiếm (local)", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearViewHistory() {
        // ✅ IMPLEMENTED: Clear view history from local database and Firebase

        // 1. Clear local SharedPreferences
        SharedPreferences viewPrefs = getSharedPreferences("ViewHistory", MODE_PRIVATE);
        viewPrefs.edit().clear().apply();

        // 2. Clear from Firebase if user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference viewHistoryRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("view_history");

            viewHistoryRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✅ Đã xóa lịch sử xem (local và cloud)", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Đã xóa local. Lỗi xóa cloud: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        } else {
            Toast.makeText(this, "Đã xóa lịch sử xem (local)", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ✅ IMPLEMENTED: Export user data to JSON file
     */
    private void requestDataDownload() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để tải dữ liệu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress dialog
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Đang chuẩn bị dữ liệu...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Collect all user data from Firebase
        DatabaseReference userDataRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(currentUser.getUid());

        userDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    // Build JSON data
                    org.json.JSONObject jsonData = new org.json.JSONObject();

                    // User profile
                    org.json.JSONObject profile = new org.json.JSONObject();
                    profile.put("email", currentUser.getEmail());
                    profile.put("displayName", currentUser.getDisplayName());
                    profile.put("uid", currentUser.getUid());
                    profile.put("createdAt", currentUser.getMetadata() != null
                            ? currentUser.getMetadata().getCreationTimestamp() : 0);
                    jsonData.put("profile", profile);

                    // Privacy settings
                    if (dataSnapshot.child("privacy_settings").exists()) {
                        org.json.JSONObject privacySettings = new org.json.JSONObject();
                        DataSnapshot privacySnap = dataSnapshot.child("privacy_settings");
                        for (DataSnapshot child : privacySnap.getChildren()) {
                            privacySettings.put(child.getKey(), child.getValue());
                        }
                        jsonData.put("privacy_settings", privacySettings);
                    }

                    // Notification settings
                    if (dataSnapshot.child("notification_settings").exists()) {
                        org.json.JSONObject notifSettings = new org.json.JSONObject();
                        DataSnapshot notifSnap = dataSnapshot.child("notification_settings");
                        for (DataSnapshot child : notifSnap.getChildren()) {
                            notifSettings.put(child.getKey(), child.getValue());
                        }
                        jsonData.put("notification_settings", notifSettings);
                    }

                    // Blocked users
                    if (dataSnapshot.child("blocked_users").exists()) {
                        org.json.JSONArray blockedUsers = new org.json.JSONArray();
                        DataSnapshot blockedSnap = dataSnapshot.child("blocked_users");
                        for (DataSnapshot child : blockedSnap.getChildren()) {
                            org.json.JSONObject user = new org.json.JSONObject();
                            user.put("userId", child.getKey());
                            if (child.child("displayName").exists()) {
                                user.put("displayName", child.child("displayName").getValue());
                            }
                            if (child.child("blockedAt").exists()) {
                                user.put("blockedAt", child.child("blockedAt").getValue());
                            }
                            blockedUsers.put(user);
                        }
                        jsonData.put("blocked_users", blockedUsers);
                    }

                    // Export timestamp
                    jsonData.put("exported_at", System.currentTimeMillis());
                    jsonData.put("exported_date", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            java.util.Locale.getDefault()).format(new java.util.Date()));

                    // Save to file
                    saveDataToFile(jsonData.toString(2), progressDialog);

                } catch (org.json.JSONException e) {
                    progressDialog.dismiss();
                    Toast.makeText(PrivacySettingsActivity.this,
                            "Lỗi xử lý dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(PrivacySettingsActivity.this,
                        "Lỗi tải dữ liệu: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveDataToFile(String jsonData, android.app.ProgressDialog progressDialog) {
        try {
            // Create file in Downloads directory
            java.io.File downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS);

            String fileName = "HealthTips_MyData_" +
                    new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                            .format(new java.util.Date()) + ".json";

            java.io.File file = new java.io.File(downloadsDir, fileName);

            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(jsonData);
            writer.close();

            progressDialog.dismiss();

            // Show success message with file path
            new MaterialAlertDialogBuilder(this)
                .setTitle("✅ Tải xuống thành công")
                .setMessage("Dữ liệu của bạn đã được lưu tại:\n\n" +
                        file.getAbsolutePath() + "\n\n" +
                        "Bạn có thể tìm file trong thư mục Downloads.")
                .setPositiveButton("Mở thư mục", (dialog, which) -> {
                    // Open Downloads folder
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(android.net.Uri.fromFile(downloadsDir), "resource/folder");
                    if (intent.resolveActivityInfo(getPackageManager(), 0) != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Không thể mở thư mục Downloads", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();

        } catch (java.io.IOException e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Lỗi lưu file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * ✅ IMPLEMENTED: Load blocked users count from Firebase
     */
    private void loadBlockedUsersCount() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference blockedUsersRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("blocked_users");

            blockedUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int count = (int) dataSnapshot.getChildrenCount();
                    updateBlockedUsersCount(count);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // On error, default to 0
                    updateBlockedUsersCount(0);
                    android.util.Log.e("PrivacySettings", "Error loading blocked users: " + databaseError.getMessage());
                }
            });
        } else {
            // No user logged in
            updateBlockedUsersCount(0);
        }
    }

    private void updateBlockedUsersCount(int count) {
        String countText = count == 0 ? "0 người dùng" : count + " người dùng";
        tvBlockedCount.setText(countText);
    }
}
