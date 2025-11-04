package com.vhn.doan.presentation.settings.content;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

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

/**
 * Activity cài đặt thông báo
 */
public class NotificationSettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;
    private DatabaseReference userSettingsRef;

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
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase reference
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userSettingsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("notification_settings");
        }

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

    private void applySettings(DataSnapshot snapshot) {
        boolean allNotifications = getBoolean(snapshot, "all_notifications", true);
        boolean healthTips = getBoolean(snapshot, "health_tips", true);
        boolean reminders = getBoolean(snapshot, "reminders", true);
        boolean likes = getBoolean(snapshot, "likes", true);
        boolean comments = getBoolean(snapshot, "comments", true);
        boolean follows = getBoolean(snapshot, "follows", true);
        boolean updates = getBoolean(snapshot, "updates", true);
        boolean sound = getBoolean(snapshot, "sound", true);
        boolean vibration = getBoolean(snapshot, "vibration", true);

        switchAllNotifications.setChecked(allNotifications);
        switchHealthTips.setChecked(healthTips);
        switchReminders.setChecked(reminders);
        switchLikes.setChecked(likes);
        switchComments.setChecked(comments);
        switchFollows.setChecked(follows);
        switchUpdates.setChecked(updates);
        switchSound.setChecked(sound);
        switchVibration.setChecked(vibration);

        // Cache to local
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("all_notifications", allNotifications);
        editor.putBoolean("health_tips", healthTips);
        editor.putBoolean("reminders", reminders);
        editor.putBoolean("likes", likes);
        editor.putBoolean("comments", comments);
        editor.putBoolean("follows", follows);
        editor.putBoolean("updates", updates);
        editor.putBoolean("sound", sound);
        editor.putBoolean("vibration", vibration);
        editor.apply();
    }

    private boolean getBoolean(DataSnapshot snapshot, String key, boolean defaultValue) {
        Boolean value = snapshot.child(key).getValue(Boolean.class);
        return value != null ? value : defaultValue;
    }

    private void loadFromLocalPreferences() {
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

    private void saveSetting(String key, boolean value) {
        // Save to local
        preferences.edit().putBoolean(key, value).apply();

        // Sync to Firebase
        if (userSettingsRef != null) {
            userSettingsRef.child(key).setValue(value);
        }
    }

    private void setupListeners() {
        switchAllNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting("all_notifications", isChecked);

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
            saveSetting("health_tips", isChecked));

        switchReminders.setOnCheckedChangeListener((buttonView, isChecked) ->
            saveSetting("reminders", isChecked));

        switchLikes.setOnCheckedChangeListener((buttonView, isChecked) ->
            saveSetting("likes", isChecked));

        switchComments.setOnCheckedChangeListener((buttonView, isChecked) ->
            saveSetting("comments", isChecked));

        switchFollows.setOnCheckedChangeListener((buttonView, isChecked) ->
            saveSetting("follows", isChecked));

        switchUpdates.setOnCheckedChangeListener((buttonView, isChecked) ->
            saveSetting("updates", isChecked));

        switchSound.setOnCheckedChangeListener((buttonView, isChecked) ->
            saveSetting("sound", isChecked));

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) ->
            saveSetting("vibration", isChecked));
    }
}
