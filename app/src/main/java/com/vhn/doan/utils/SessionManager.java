package com.vhn.doan.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Manager class for handling user sessions, login history, and account deletion
 */
public class SessionManager {

    private static final String PREFS_NAME = "SessionPrefs";
    private static final String KEY_SESSION_ID = "session_id";
    private static final String KEY_DEVICE_NAME = "device_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_PENDING_DELETION = "pending_deletion";
    private static final String KEY_DELETION_TIMESTAMP = "deletion_timestamp";
    private static final long DELETION_GRACE_PERIOD = TimeUnit.DAYS.toMillis(3); // 3 days in milliseconds

    private final Context context;
    private final SharedPreferences prefs;
    private final FirebaseAuth mAuth;
    private final DatabaseReference sessionsRef;
    private final DatabaseReference usersRef;
    private String currentSessionId;

    public SessionManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.mAuth = FirebaseAuth.getInstance();
        this.sessionsRef = FirebaseDatabase.getInstance().getReference("user_sessions");
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    /**
     * Save login state when user logs in
     */
    public void saveLoginState(String userId) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_ID, userId)
                .apply();
    }

    /**
     * Check if user is logged in
     */
    public boolean isUserLoggedIn() {
        FirebaseUser user = mAuth.getCurrentUser();
        boolean localState = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        return user != null && localState;
    }

    /**
     * Get current user ID
     */
    public String getCurrentUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Auto-check login state on app startup
     * Returns true if user should stay logged in, false if should logout
     */
    public boolean autoCheckLoginState() {
        FirebaseUser user = mAuth.getCurrentUser();

        // If Firebase Auth has no user, clear local state
        if (user == null) {
            clearSession();
            return false;
        }

        // If user is pending deletion, check if grace period has expired
        if (isPendingDeletion()) {
            long deletionTime = prefs.getLong(KEY_DELETION_TIMESTAMP, 0);
            long currentTime = System.currentTimeMillis();

            if (currentTime >= deletionTime) {
                // Grace period expired, proceed with deletion
                return false; // Will trigger logout and deletion
            }
        }

        return true;
    }

    /**
     * Create and save a new session when user logs in
     */
    public void createSession() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            currentSessionId = generateSessionId();

            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("sessionId", currentSessionId);
            sessionData.put("deviceName", getDeviceName());
            sessionData.put("deviceModel", Build.MODEL);
            sessionData.put("deviceBrand", Build.BRAND);
            sessionData.put("androidVersion", Build.VERSION.RELEASE);
            sessionData.put("loginTime", System.currentTimeMillis());
            sessionData.put("lastActiveTime", System.currentTimeMillis());
            sessionData.put("ipAddress", "N/A"); // Can be obtained from server
            sessionData.put("location", "N/A"); // Can be obtained from location API

            // Save to Firebase
            sessionsRef.child(userId).child(currentSessionId).setValue(sessionData);

            // Save locally
            prefs.edit()
                    .putString(KEY_SESSION_ID, currentSessionId)
                    .putString(KEY_DEVICE_NAME, getDeviceName())
                    .apply();
        }
    }

    /**
     * Update last active time for current session
     */
    public void updateLastActive() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && currentSessionId != null) {
            String userId = user.getUid();
            sessionsRef.child(userId).child(currentSessionId)
                    .child("lastActiveTime")
                    .setValue(System.currentTimeMillis());
        }
    }

    /**
     * Get all active sessions for current user
     */
    public void getActiveSessions(SessionCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            sessionsRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<SessionInfo> sessions = new ArrayList<>();
                    for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                        SessionInfo session = sessionSnapshot.getValue(SessionInfo.class);
                        if (session != null) {
                            session.sessionId = sessionSnapshot.getKey();
                            session.isCurrentDevice = session.sessionId.equals(currentSessionId);
                            sessions.add(session);
                        }
                    }
                    callback.onSessionsLoaded(sessions);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
        } else {
            callback.onError("User not logged in");
        }
    }

    /**
     * Logout from a specific session
     */
    public void logoutSession(String sessionId, LogoutCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            sessionsRef.child(userId).child(sessionId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        if (sessionId.equals(currentSessionId)) {
                            // If logging out current session, sign out
                            mAuth.signOut();
                        }
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        }
    }

    /**
     * Logout from all other sessions except current
     */
    public void logoutAllOtherSessions(LogoutCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            getActiveSessions(new SessionCallback() {
                @Override
                public void onSessionsLoaded(List<SessionInfo> sessions) {
                    int count = 0;
                    for (SessionInfo session : sessions) {
                        if (!session.isCurrentDevice) {
                            sessionsRef.child(userId).child(session.sessionId).removeValue();
                            count++;
                        }
                    }
                    callback.onSuccess();
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
        }
    }

    /**
     * Clear current session on logout
     */
    public void clearSession() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && currentSessionId != null) {
            String userId = user.getUid();
            sessionsRef.child(userId).child(currentSessionId).removeValue();
        }
        prefs.edit().clear().apply();
        currentSessionId = null;
    }

    // ==================== ACCOUNT DELETION MANAGEMENT ====================

    /**
     * Request account deletion with 3-day grace period
     */
    public void requestAccountDeletion(DeletionCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }

        String userId = user.getUid();
        long deletionTimestamp = System.currentTimeMillis() + DELETION_GRACE_PERIOD;

        // Save pending deletion state locally
        prefs.edit()
                .putBoolean(KEY_PENDING_DELETION, true)
                .putLong(KEY_DELETION_TIMESTAMP, deletionTimestamp)
                .apply();

        // Save to Firebase
        Map<String, Object> deletionData = new HashMap<>();
        deletionData.put("pending_deletion", true);
        deletionData.put("deletion_timestamp", deletionTimestamp);
        deletionData.put("requested_at", System.currentTimeMillis());

        usersRef.child(userId).child("account_status").setValue(deletionData)
                .addOnSuccessListener(aVoid -> callback.onSuccess(deletionTimestamp))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Cancel account deletion request
     */
    public void cancelAccountDeletion(DeletionCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }

        String userId = user.getUid();

        // Clear local state
        prefs.edit()
                .putBoolean(KEY_PENDING_DELETION, false)
                .putLong(KEY_DELETION_TIMESTAMP, 0)
                .apply();

        // Clear from Firebase
        usersRef.child(userId).child("account_status").removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess(0))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Check if account is pending deletion
     */
    public boolean isPendingDeletion() {
        return prefs.getBoolean(KEY_PENDING_DELETION, false);
    }

    /**
     * Get deletion timestamp
     */
    public long getDeletionTimestamp() {
        return prefs.getLong(KEY_DELETION_TIMESTAMP, 0);
    }

    /**
     * Get remaining time until deletion (in milliseconds)
     */
    public long getRemainingTimeUntilDeletion() {
        if (!isPendingDeletion()) {
            return 0;
        }

        long deletionTime = getDeletionTimestamp();
        long currentTime = System.currentTimeMillis();
        long remaining = deletionTime - currentTime;

        return Math.max(0, remaining);
    }

    /**
     * Get formatted remaining time until deletion
     */
    public String getFormattedRemainingTime() {
        long remainingMs = getRemainingTimeUntilDeletion();
        if (remainingMs == 0) {
            return "Đã hết hạn";
        }

        long days = TimeUnit.MILLISECONDS.toDays(remainingMs);
        long hours = TimeUnit.MILLISECONDS.toHours(remainingMs) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMs) % 60;

        if (days > 0) {
            return days + " ngày " + hours + " giờ";
        } else if (hours > 0) {
            return hours + " giờ " + minutes + " phút";
        } else {
            return minutes + " phút";
        }
    }

    /**
     * Perform account deletion (should be called after grace period)
     */
    public void performAccountDeletion(DeletionCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }

        String userId = user.getUid();

        // Delete all user data from Firebase Database
        usersRef.child(userId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Delete all sessions
                    sessionsRef.child(userId).removeValue();

                    // Delete Firebase Auth account
                    user.delete()
                            .addOnSuccessListener(aVoid2 -> {
                                // Clear local session
                                clearSession();
                                callback.onSuccess(0);
                            })
                            .addOnFailureListener(e -> callback.onError("Failed to delete auth account: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError("Failed to delete user data: " + e.getMessage()));
    }

    /**
     * Check and perform deletion if grace period has expired
     */
    public void checkAndPerformScheduledDeletion(DeletionCallback callback) {
        if (!isPendingDeletion()) {
            callback.onError("No pending deletion");
            return;
        }

        long remainingTime = getRemainingTimeUntilDeletion();
        if (remainingTime <= 0) {
            // Grace period expired, perform deletion
            performAccountDeletion(callback);
        } else {
            callback.onError("Grace period has not expired yet");
        }
    }

    /**
     * Load existing session ID
     */
    public void loadSession() {
        currentSessionId = prefs.getString(KEY_SESSION_ID, null);
    }

    /**
     * Generate unique session ID
     */
    private String generateSessionId() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
                + "_" + System.currentTimeMillis();
    }

    /**
     * Get device name
     */
    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        char first = str.charAt(0);
        if (Character.isUpperCase(first)) {
            return str;
        } else {
            return Character.toUpperCase(first) + str.substring(1);
        }
    }

    /**
     * Session info class
     */
    public static class SessionInfo {
        public String sessionId;
        public String deviceName;
        public String deviceModel;
        public String deviceBrand;
        public String androidVersion;
        public long loginTime;
        public long lastActiveTime;
        public String ipAddress;
        public String location;
        public boolean isCurrentDevice;

        public SessionInfo() {
            // Required for Firebase
        }

        public String getFormattedLoginTime() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(new Date(loginTime));
        }

        public String getFormattedLastActive() {
            long diff = System.currentTimeMillis() - lastActiveTime;
            long minutes = diff / (1000 * 60);
            long hours = diff / (1000 * 60 * 60);
            long days = diff / (1000 * 60 * 60 * 24);

            if (minutes < 60) {
                return minutes + " phút trước";
            } else if (hours < 24) {
                return hours + " giờ trước";
            } else {
                return days + " ngày trước";
            }
        }
    }

    /**
     * Callback interfaces
     */
    public interface SessionCallback {
        void onSessionsLoaded(List<SessionInfo> sessions);
        void onError(String error);
    }

    public interface LogoutCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface DeletionCallback {
        void onSuccess(long timestamp);
        void onError(String error);
    }
}
