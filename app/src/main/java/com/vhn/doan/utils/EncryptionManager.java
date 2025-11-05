package com.vhn.doan.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Manager class for handling data encryption using EncryptedSharedPreferences
 */
public class EncryptionManager {

    private static final String TAG = "EncryptionManager";
    private static final String ENCRYPTED_PREFS_NAME = "encrypted_app_prefs";
    private static final String SECURITY_SETTINGS_PREFS = "SecuritySettings";
    private static final String KEY_ENCRYPT_DATA = "encrypt_data_enabled";

    private final Context context;
    private SharedPreferences encryptedPrefs;
    private SharedPreferences securitySettings;
    private boolean encryptionEnabled;

    public EncryptionManager(Context context) {
        this.context = context;
        this.securitySettings = context.getSharedPreferences(SECURITY_SETTINGS_PREFS, Context.MODE_PRIVATE);
        this.encryptionEnabled = securitySettings.getBoolean(KEY_ENCRYPT_DATA, true);

        if (encryptionEnabled) {
            initializeEncryptedPreferences();
        }
    }

    /**
     * Initialize EncryptedSharedPreferences
     */
    private void initializeEncryptedPreferences() {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    ENCRYPTED_PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            Log.d(TAG, "EncryptedSharedPreferences initialized successfully");
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Failed to initialize EncryptedSharedPreferences", e);
            // Fallback to regular SharedPreferences
            encryptedPrefs = context.getSharedPreferences("fallback_prefs", Context.MODE_PRIVATE);
        }
    }

    /**
     * Enable or disable encryption
     */
    public void setEncryptionEnabled(boolean enabled) {
        this.encryptionEnabled = enabled;
        securitySettings.edit().putBoolean(KEY_ENCRYPT_DATA, enabled).apply();

        if (enabled && encryptedPrefs == null) {
            initializeEncryptedPreferences();
        }

        Log.d(TAG, "Encryption " + (enabled ? "enabled" : "disabled"));
    }

    /**
     * Check if encryption is enabled
     */
    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }

    /**
     * Save encrypted string data
     */
    public void saveSecureString(String key, String value) {
        if (encryptionEnabled && encryptedPrefs != null) {
            encryptedPrefs.edit().putString(key, value).apply();
            Log.d(TAG, "Encrypted data saved for key: " + key);
        } else {
            Log.w(TAG, "Encryption disabled, data not saved securely");
        }
    }

    /**
     * Get encrypted string data
     */
    public String getSecureString(String key, String defaultValue) {
        if (encryptionEnabled && encryptedPrefs != null) {
            return encryptedPrefs.getString(key, defaultValue);
        } else {
            Log.w(TAG, "Encryption disabled, returning default value");
            return defaultValue;
        }
    }

    /**
     * Save encrypted boolean data
     */
    public void saveSecureBoolean(String key, boolean value) {
        if (encryptionEnabled && encryptedPrefs != null) {
            encryptedPrefs.edit().putBoolean(key, value).apply();
            Log.d(TAG, "Encrypted boolean saved for key: " + key);
        }
    }

    /**
     * Get encrypted boolean data
     */
    public boolean getSecureBoolean(String key, boolean defaultValue) {
        if (encryptionEnabled && encryptedPrefs != null) {
            return encryptedPrefs.getBoolean(key, defaultValue);
        } else {
            return defaultValue;
        }
    }

    /**
     * Save encrypted int data
     */
    public void saveSecureInt(String key, int value) {
        if (encryptionEnabled && encryptedPrefs != null) {
            encryptedPrefs.edit().putInt(key, value).apply();
            Log.d(TAG, "Encrypted int saved for key: " + key);
        }
    }

    /**
     * Get encrypted int data
     */
    public int getSecureInt(String key, int defaultValue) {
        if (encryptionEnabled && encryptedPrefs != null) {
            return encryptedPrefs.getInt(key, defaultValue);
        } else {
            return defaultValue;
        }
    }

    /**
     * Save encrypted long data
     */
    public void saveSecureLong(String key, long value) {
        if (encryptionEnabled && encryptedPrefs != null) {
            encryptedPrefs.edit().putLong(key, value).apply();
            Log.d(TAG, "Encrypted long saved for key: " + key);
        }
    }

    /**
     * Get encrypted long data
     */
    public long getSecureLong(String key, long defaultValue) {
        if (encryptionEnabled && encryptedPrefs != null) {
            return encryptedPrefs.getInt(key, (int) defaultValue);
        } else {
            return defaultValue;
        }
    }

    /**
     * Remove encrypted data
     */
    public void removeSecureData(String key) {
        if (encryptedPrefs != null) {
            encryptedPrefs.edit().remove(key).apply();
            Log.d(TAG, "Removed encrypted data for key: " + key);
        }
    }

    /**
     * Clear all encrypted data
     */
    public void clearAllSecureData() {
        if (encryptedPrefs != null) {
            encryptedPrefs.edit().clear().apply();
            Log.d(TAG, "Cleared all encrypted data");
        }
    }

    /**
     * Check if key exists in encrypted storage
     */
    public boolean containsSecureKey(String key) {
        if (encryptedPrefs != null) {
            return encryptedPrefs.contains(key);
        }
        return false;
    }

    /**
     * Migrate data from regular SharedPreferences to encrypted storage
     */
    public void migrateToEncryptedStorage(String prefsName) {
        if (!encryptionEnabled || encryptedPrefs == null) {
            Log.w(TAG, "Cannot migrate: encryption is disabled");
            return;
        }

        SharedPreferences oldPrefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);

        // Copy all data to encrypted preferences
        for (String key : oldPrefs.getAll().keySet()) {
            Object value = oldPrefs.getAll().get(key);

            if (value instanceof String) {
                saveSecureString(key, (String) value);
            } else if (value instanceof Boolean) {
                saveSecureBoolean(key, (Boolean) value);
            } else if (value instanceof Integer) {
                saveSecureInt(key, (Integer) value);
            } else if (value instanceof Long) {
                saveSecureLong(key, (Long) value);
            }
        }

        Log.d(TAG, "Migrated data from " + prefsName + " to encrypted storage");
    }
}
