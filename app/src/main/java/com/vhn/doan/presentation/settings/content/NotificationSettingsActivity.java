package com.vhn.doan.presentation.settings.content;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.vhn.doan.R;

/**
 * Activity cài đặt thông báo
 */
public class NotificationSettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;

    private SwitchCompat switchAllNotifications;
    private SwitchCompat switchHealthTips;
    private SwitchCompat switchReminders;
    private SwitchCompat switchLikes;
    private SwitchCompat switchComments;
    private SwitchCompat switchFollows;
    private SwitchCompat switchUpdates;
    private SwitchCompat switchSound;
    private SwitchCompat switchVibration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        preferences = getSharedPreferences("NotificationSettings", MODE_PRIVATE);

        setupViews();
        loadSettings();
        setupListeners();
    }

    private void setupViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        switchAllNotifications = findViewById(R.id.switchAllNotifications);
        switchHealthTips = findViewById(R.id.switchHealthTips);
        switchReminders = findViewById(R.id.switchReminders);
        switchLikes = findViewById(R.id.switchLikes);
        switchComments = findViewById(R.id.switchComments);
        switchFollows = findViewById(R.id.switchFollows);
        switchUpdates = findViewById(R.id.switchUpdates);
        switchSound = findViewById(R.id.switchSound);
        switchVibration = findViewById(R.id.switchVibration);
    }

    private void loadSettings() {
        switchAllNotifications.setChecked(preferences.getBoolean("all_notifications", true));
        switchHealthTips.setChecked(preferences.getBoolean("health_tips", true));
        switchReminders.setChecked(preferences.getBoolean("reminders", true));
        switchLikes.setChecked(preferences.getBoolean("likes", true));
        switchComments.setChecked(preferences.getBoolean("comments", true));
        switchFollows.setChecked(preferences.getBoolean("follows", true));
        switchUpdates.setChecked(preferences.getBoolean("updates", true));
        switchSound.setChecked(preferences.getBoolean("sound", true));
        switchVibration.setChecked(preferences.getBoolean("vibration", true));
    }

    private void setupListeners() {
        switchAllNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("all_notifications", isChecked).apply();

            if (!isChecked) {
                switchHealthTips.setEnabled(false);
                switchReminders.setEnabled(false);
                switchLikes.setEnabled(false);
                switchComments.setEnabled(false);
                switchFollows.setEnabled(false);
                switchUpdates.setEnabled(false);
            } else {
                switchHealthTips.setEnabled(true);
                switchReminders.setEnabled(true);
                switchLikes.setEnabled(true);
                switchComments.setEnabled(true);
                switchFollows.setEnabled(true);
                switchUpdates.setEnabled(true);
            }
        });

        switchHealthTips.setOnCheckedChangeListener((buttonView, isChecked) ->
            preferences.edit().putBoolean("health_tips", isChecked).apply());

        switchReminders.setOnCheckedChangeListener((buttonView, isChecked) ->
            preferences.edit().putBoolean("reminders", isChecked).apply());

        switchLikes.setOnCheckedChangeListener((buttonView, isChecked) ->
            preferences.edit().putBoolean("likes", isChecked).apply());

        switchComments.setOnCheckedChangeListener((buttonView, isChecked) ->
            preferences.edit().putBoolean("comments", isChecked).apply());

        switchFollows.setOnCheckedChangeListener((buttonView, isChecked) ->
            preferences.edit().putBoolean("follows", isChecked).apply());

        switchUpdates.setOnCheckedChangeListener((buttonView, isChecked) ->
            preferences.edit().putBoolean("updates", isChecked).apply());

        switchSound.setOnCheckedChangeListener((buttonView, isChecked) ->
            preferences.edit().putBoolean("sound", isChecked).apply());

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) ->
            preferences.edit().putBoolean("vibration", isChecked).apply());
    }
}
