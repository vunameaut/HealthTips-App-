package com.vhn.doan.presentation.settings.content;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.R;
import com.vhn.doan.services.NotificationService;

/**
 * ✅ REFACTORED: Activity cài đặt thông báo - Phù hợp với app hiện tại
 */
public class NotificationSettingsActivity extends AppCompatActivity {

    // Notification preference keys - Match với NotificationType
    private static final String KEY_ALL_NOTIFICATIONS = "all_notifications";
    private static final String KEY_NEW_HEALTH_TIP = "new_health_tip";
    private static final String KEY_NEW_VIDEO = "new_video";
    private static final String KEY_RECOMMENDATIONS = "recommendations";
    private static final String KEY_COMMENT_REPLY = "comment_reply";
    private static final String KEY_COMMENT_LIKE = "comment_like";
    private static final String KEY_REMINDERS = "reminders";
    private static final String KEY_SYSTEM_UPDATES = "system_updates";
    private static final String KEY_SOUND = "sound";
    private static final String KEY_VIBRATION = "vibration";

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;
    private DatabaseReference userSettingsRef;
    private NotificationService notificationService;

    // Master switch
    private SwitchCompat switchAllNotifications;

    // Content notifications (Health Tips & Videos)
    private SwitchCompat switchNewHealthTips;
    private SwitchCompat switchNewVideos;
    private SwitchCompat switchRecommendations;

    // Social notifications (Comments)
    private SwitchCompat switchCommentReply;
    private SwitchCompat switchCommentLike;

    // Reminder notifications
    private SwitchCompat switchReminders;

    // System notifications
    private SwitchCompat switchSystemUpdates;

    // Sound & Vibration
    private SwitchCompat switchSound;
    private SwitchCompat switchVibration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        preferences = getSharedPreferences("NotificationSettings", MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        notificationService = new NotificationService(this);

        // Initialize Firebase reference
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userSettingsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("notification_settings");
        }

        setupViews();
        checkNotificationPermission();
        loadSettings();
        setupListeners();
    }

    private void setupViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Master switch
        switchAllNotifications = findViewById(R.id.switchAllNotifications);

        // Content notifications
        switchNewHealthTips = findViewById(R.id.switchNewHealthTips);
        switchNewVideos = findViewById(R.id.switchNewVideos);
        switchRecommendations = findViewById(R.id.switchRecommendations);

        // Social notifications
        switchCommentReply = findViewById(R.id.switchCommentReply);
        switchCommentLike = findViewById(R.id.switchCommentLike);

        // Reminder notifications
        switchReminders = findViewById(R.id.switchReminders);

        // System notifications
        switchSystemUpdates = findViewById(R.id.switchSystemUpdates);

        // Sound & Vibration
        switchSound = findViewById(R.id.switchSound);
        switchVibration = findViewById(R.id.switchVibration);

        // System settings button - removed as layout doesn't have this button
        // Users can access system notification settings from Android's system settings
    }

    /**
     * Kiểm tra quyền notification và hiển thị warning nếu bị tắt
     */
    private void checkNotificationPermission() {
        if (!notificationService.areNotificationsEnabled()) {
            Toast.makeText(this, "⚠️ Thông báo bị tắt. Vui lòng bật trong cài đặt hệ thống.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadSettings() {
        if (userSettingsRef != null) {
            // Load from Firebase
            userSettingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Load from Firebase
                        applySettings(snapshot);
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
    }

    /**
     * ✅ REFACTORED: Apply settings với keys mới
     */
    private void applySettings(DataSnapshot snapshot) {
        boolean allNotifications = getBoolean(snapshot, KEY_ALL_NOTIFICATIONS, true);
        boolean newHealthTips = getBoolean(snapshot, KEY_NEW_HEALTH_TIP, true);
        boolean newVideos = getBoolean(snapshot, KEY_NEW_VIDEO, true);
        boolean recommendations = getBoolean(snapshot, KEY_RECOMMENDATIONS, true);
        boolean commentReply = getBoolean(snapshot, KEY_COMMENT_REPLY, true);
        boolean commentLike = getBoolean(snapshot, KEY_COMMENT_LIKE, true);
        boolean reminders = getBoolean(snapshot, KEY_REMINDERS, true);
        boolean systemUpdates = getBoolean(snapshot, KEY_SYSTEM_UPDATES, true);
        boolean sound = getBoolean(snapshot, KEY_SOUND, true);
        boolean vibration = getBoolean(snapshot, KEY_VIBRATION, true);

        switchAllNotifications.setChecked(allNotifications);
        switchNewHealthTips.setChecked(newHealthTips);
        switchNewVideos.setChecked(newVideos);
        switchRecommendations.setChecked(recommendations);
        switchCommentReply.setChecked(commentReply);
        switchCommentLike.setChecked(commentLike);
        switchReminders.setChecked(reminders);
        switchSystemUpdates.setChecked(systemUpdates);
        switchSound.setChecked(sound);
        switchVibration.setChecked(vibration);

        // Cache to local
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_ALL_NOTIFICATIONS, allNotifications);
        editor.putBoolean(KEY_NEW_HEALTH_TIP, newHealthTips);
        editor.putBoolean(KEY_NEW_VIDEO, newVideos);
        editor.putBoolean(KEY_RECOMMENDATIONS, recommendations);
        editor.putBoolean(KEY_COMMENT_REPLY, commentReply);
        editor.putBoolean(KEY_COMMENT_LIKE, commentLike);
        editor.putBoolean(KEY_REMINDERS, reminders);
        editor.putBoolean(KEY_SYSTEM_UPDATES, systemUpdates);
        editor.putBoolean(KEY_SOUND, sound);
        editor.putBoolean(KEY_VIBRATION, vibration);
        editor.apply();

        // Update UI state
        updateNotificationSwitchesState(allNotifications);
    }

    private boolean getBoolean(DataSnapshot snapshot, String key, boolean defaultValue) {
        Boolean value = snapshot.child(key).getValue(Boolean.class);
        return value != null ? value : defaultValue;
    }

    /**
     * ✅ REFACTORED: Load từ local với keys mới
     */
    private void loadFromLocalPreferences() {
        boolean allNotifications = preferences.getBoolean(KEY_ALL_NOTIFICATIONS, true);

        switchAllNotifications.setChecked(allNotifications);
        switchNewHealthTips.setChecked(preferences.getBoolean(KEY_NEW_HEALTH_TIP, true));
        switchNewVideos.setChecked(preferences.getBoolean(KEY_NEW_VIDEO, true));
        switchRecommendations.setChecked(preferences.getBoolean(KEY_RECOMMENDATIONS, true));
        switchCommentReply.setChecked(preferences.getBoolean(KEY_COMMENT_REPLY, true));
        switchCommentLike.setChecked(preferences.getBoolean(KEY_COMMENT_LIKE, true));
        switchReminders.setChecked(preferences.getBoolean(KEY_REMINDERS, true));
        switchSystemUpdates.setChecked(preferences.getBoolean(KEY_SYSTEM_UPDATES, true));
        switchSound.setChecked(preferences.getBoolean(KEY_SOUND, true));
        switchVibration.setChecked(preferences.getBoolean(KEY_VIBRATION, true));

        updateNotificationSwitchesState(allNotifications);
    }

    /**
     * Update enabled state của các switches dựa trên master switch
     */
    private void updateNotificationSwitchesState(boolean enabled) {
        switchNewHealthTips.setEnabled(enabled);
        switchNewVideos.setEnabled(enabled);
        switchRecommendations.setEnabled(enabled);
        switchCommentReply.setEnabled(enabled);
        switchCommentLike.setEnabled(enabled);
        switchReminders.setEnabled(enabled);
        switchSystemUpdates.setEnabled(enabled);
    }

    private void saveSetting(String key, boolean value) {
        // Save to local
        preferences.edit().putBoolean(key, value).apply();

        // Sync to Firebase
        if (userSettingsRef != null) {
            userSettingsRef.child(key).setValue(value);
        }
    }

    /**
     * ✅ REFACTORED: Setup listeners với keys mới và feedback
     */
    private void setupListeners() {
        // Master switch - Tắt/bật tất cả notifications
        switchAllNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_ALL_NOTIFICATIONS, isChecked);
            updateNotificationSwitchesState(isChecked);

            if (isChecked) {
                Toast.makeText(this, "✅ Đã bật tất cả thông báo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đã tắt tất cả thông báo", Toast.LENGTH_SHORT).show();
            }
        });

        // Content notifications
        switchNewHealthTips.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_NEW_HEALTH_TIP, isChecked);
            showFeedback("Mẹo sức khỏe mới", isChecked);
        });

        switchNewVideos.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_NEW_VIDEO, isChecked);
            showFeedback("Video mới", isChecked);
        });

        switchRecommendations.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_RECOMMENDATIONS, isChecked);
            showFeedback("Đề xuất", isChecked);
        });

        // Social notifications
        switchCommentReply.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_COMMENT_REPLY, isChecked);
            showFeedback("Trả lời bình luận", isChecked);
        });

        switchCommentLike.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_COMMENT_LIKE, isChecked);
            showFeedback("Lượt thích bình luận", isChecked);
        });

        // Reminder notifications
        switchReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_REMINDERS, isChecked);
            showFeedback("Nhắc nhở", isChecked);
        });

        // System notifications
        switchSystemUpdates.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_SYSTEM_UPDATES, isChecked);
            showFeedback("Cập nhật hệ thống", isChecked);
        });

        // Sound & Vibration
        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_SOUND, isChecked);
            showFeedback("Âm thanh", isChecked);
        });

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_VIBRATION, isChecked);
            showFeedback("Rung", isChecked);
        });
    }

    /**
     * Hiển thị feedback khi toggle setting
     */
    private void showFeedback(String settingName, boolean enabled) {
        String message = enabled ?
            "✅ Đã bật: " + settingName :
            "Đã tắt: " + settingName;
        // Không show toast cho từng setting để tránh spam
        // Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * ✅ NEW: Public method để check notification setting
     * Sử dụng bởi NotificationService và ReminderService
     */
    public static boolean isNotificationEnabled(android.content.Context context, String notificationType) {
        SharedPreferences prefs = context.getSharedPreferences("NotificationSettings", android.content.Context.MODE_PRIVATE);

        // Check master switch first
        if (!prefs.getBoolean(KEY_ALL_NOTIFICATIONS, true)) {
            return false;
        }

        // Check specific notification type
        return prefs.getBoolean(notificationType, true);
    }

    /**
     * ✅ NEW: Check if sound is enabled
     */
    public static boolean isSoundEnabled(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences("NotificationSettings", android.content.Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_SOUND, true);
    }

    /**
     * ✅ NEW: Check if vibration is enabled
     */
    public static boolean isVibrationEnabled(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences("NotificationSettings", android.content.Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_VIBRATION, true);
    }
}

