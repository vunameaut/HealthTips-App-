package com.vhn.doan.utils;

import android.content.Context;
import android.util.Log;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * DataStoreManager - Modern replacement cho SharedPreferences
 * Sử dụng Jetpack DataStore với RxJava3
 * Thread-safe và type-safe
 */
public class DataStoreManager {

    private static final String TAG = "DataStoreManager";
    private static final String DATASTORE_NAME = "healthtips_preferences";

    private static DataStoreManager instance;
    private final RxDataStore<Preferences> dataStore;

    // Preference Keys
    public static class Keys {
        // User session keys
        public static final Preferences.Key<String> USER_ID = PreferencesKeys.stringKey("user_id");
        public static final Preferences.Key<String> USER_EMAIL = PreferencesKeys.stringKey("user_email");
        public static final Preferences.Key<String> USER_NAME = PreferencesKeys.stringKey("user_name");
        public static final Preferences.Key<Boolean> IS_LOGGED_IN = PreferencesKeys.booleanKey("is_logged_in");
        public static final Preferences.Key<Long> LAST_LOGIN_TIME = PreferencesKeys.longKey("last_login_time");
        public static final Preferences.Key<Long> LAST_ACTIVE_TIME = PreferencesKeys.longKey("last_active_time");

        // App settings keys
        public static final Preferences.Key<String> THEME_MODE = PreferencesKeys.stringKey("theme_mode");
        public static final Preferences.Key<String> LANGUAGE = PreferencesKeys.stringKey("language");
        public static final Preferences.Key<Float> FONT_SIZE = PreferencesKeys.floatKey("font_size");
        public static final Preferences.Key<Boolean> SECURE_MODE = PreferencesKeys.booleanKey("secure_mode");
        public static final Preferences.Key<Boolean> NOTIFICATIONS_ENABLED = PreferencesKeys.booleanKey("notifications_enabled");

        // Cache keys
        public static final Preferences.Key<Long> LAST_CACHE_CLEANUP = PreferencesKeys.longKey("last_cache_cleanup");
        public static final Preferences.Key<Long> LAST_DATA_SYNC = PreferencesKeys.longKey("last_data_sync");

        // Account deletion keys
        public static final Preferences.Key<Boolean> ACCOUNT_PENDING_DELETION = PreferencesKeys.booleanKey("account_pending_deletion");
        public static final Preferences.Key<Long> DELETION_SCHEDULED_TIME = PreferencesKeys.longKey("deletion_scheduled_time");
    }

    private DataStoreManager(Context context) {
        this.dataStore = new RxPreferenceDataStoreBuilder(
                context.getApplicationContext(),
                DATASTORE_NAME
        ).build();
        Log.d(TAG, "DataStoreManager initialized");
    }

    public static synchronized DataStoreManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataStoreManager(context);
        }
        return instance;
    }

    /**
     * Save string value
     */
    public Single<Preferences> putString(Preferences.Key<String> key, String value) {
        return dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(key, value);
            return Single.just(mutablePreferences);
        }).doOnSuccess(prefs -> Log.d(TAG, "Saved string: " + key.getName() + " = " + value));
    }

    /**
     * Get string value
     */
    public Flowable<String> getString(Preferences.Key<String> key, String defaultValue) {
        return dataStore.data().map(prefs -> prefs.get(key) != null ? prefs.get(key) : defaultValue);
    }

    /**
     * Save boolean value
     */
    public Single<Preferences> putBoolean(Preferences.Key<Boolean> key, boolean value) {
        return dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(key, value);
            return Single.just(mutablePreferences);
        }).doOnSuccess(prefs -> Log.d(TAG, "Saved boolean: " + key.getName() + " = " + value));
    }

    /**
     * Get boolean value
     */
    public Flowable<Boolean> getBoolean(Preferences.Key<Boolean> key, boolean defaultValue) {
        return dataStore.data().map(prefs -> prefs.get(key) != null ? prefs.get(key) : defaultValue);
    }

    /**
     * Save long value
     */
    public Single<Preferences> putLong(Preferences.Key<Long> key, long value) {
        return dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(key, value);
            return Single.just(mutablePreferences);
        }).doOnSuccess(prefs -> Log.d(TAG, "Saved long: " + key.getName() + " = " + value));
    }

    /**
     * Get long value
     */
    public Flowable<Long> getLong(Preferences.Key<Long> key, long defaultValue) {
        return dataStore.data().map(prefs -> prefs.get(key) != null ? prefs.get(key) : defaultValue);
    }

    /**
     * Save float value
     */
    public Single<Preferences> putFloat(Preferences.Key<Float> key, float value) {
        return dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(key, value);
            return Single.just(mutablePreferences);
        }).doOnSuccess(prefs -> Log.d(TAG, "Saved float: " + key.getName() + " = " + value));
    }

    /**
     * Get float value
     */
    public Flowable<Float> getFloat(Preferences.Key<Float> key, float defaultValue) {
        return dataStore.data().map(prefs -> prefs.get(key) != null ? prefs.get(key) : defaultValue);
    }

    /**
     * Remove a key
     */
    public Single<Preferences> remove(Preferences.Key<?> key) {
        return dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.remove(key);
            return Single.just(mutablePreferences);
        }).doOnSuccess(prefs -> Log.d(TAG, "Removed key: " + key.getName()));
    }

    /**
     * Clear all data
     */
    public Single<Preferences> clear() {
        return dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.clear();
            return Single.just(mutablePreferences);
        }).doOnSuccess(prefs -> Log.d(TAG, "Cleared all preferences"));
    }

    /**
     * Save user login session
     */
    public Single<Preferences> saveUserSession(String userId, String email, String name) {
        return dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(Keys.USER_ID, userId);
            mutablePreferences.set(Keys.USER_EMAIL, email);
            mutablePreferences.set(Keys.USER_NAME, name);
            mutablePreferences.set(Keys.IS_LOGGED_IN, true);
            mutablePreferences.set(Keys.LAST_LOGIN_TIME, System.currentTimeMillis());
            mutablePreferences.set(Keys.LAST_ACTIVE_TIME, System.currentTimeMillis());
            return Single.just(mutablePreferences);
        }).doOnSuccess(prefs -> Log.d(TAG, "User session saved: " + email));
    }

    /**
     * Clear user session (logout)
     */
    public Single<Preferences> clearUserSession() {
        return dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.remove(Keys.USER_ID);
            mutablePreferences.remove(Keys.USER_EMAIL);
            mutablePreferences.remove(Keys.USER_NAME);
            mutablePreferences.set(Keys.IS_LOGGED_IN, false);
            return Single.just(mutablePreferences);
        }).doOnSuccess(prefs -> Log.d(TAG, "User session cleared"));
    }

    /**
     * Update last active time
     */
    public Single<Preferences> updateLastActive() {
        return putLong(Keys.LAST_ACTIVE_TIME, System.currentTimeMillis());
    }

    /**
     * Check if user is logged in (synchronous)
     */
    public boolean isUserLoggedIn() {
        try {
            return dataStore.data()
                    .map(prefs -> {
                        Boolean isLoggedIn = prefs.get(Keys.IS_LOGGED_IN);
                        return isLoggedIn != null && isLoggedIn;
                    })
                    .blockingFirst(false);
        } catch (Exception e) {
            Log.e(TAG, "Error checking login status", e);
            return false;
        }
    }

    /**
     * Get current user ID (synchronous)
     */
    public String getUserId() {
        try {
            return dataStore.data()
                    .map(prefs -> prefs.get(Keys.USER_ID))
                    .blockingFirst(null);
        } catch (Exception e) {
            Log.e(TAG, "Error getting user ID", e);
            return null;
        }
    }
}
