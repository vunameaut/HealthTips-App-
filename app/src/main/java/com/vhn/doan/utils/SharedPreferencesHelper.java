package com.vhn.doan.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper class để quản lý SharedPreferences
 */
public class SharedPreferencesHelper {

    private static final String PREF_NAME = "HealthTipsPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_COUNTRY = "user_country";

    private final SharedPreferences sharedPreferences;

    public SharedPreferencesHelper(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Lưu thông tin user ID
     */
    public void setCurrentUserId(String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    /**
     * Lấy user ID hiện tại
     */
    public String getCurrentUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    /**
     * Lưu email người dùng
     */
    public void setUserEmail(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    /**
     * Lấy email người dùng
     */
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Lưu tên người dùng
     */
    public void setUserName(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    /**
     * Lấy tên người dùng
     */
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    /**
     * Đặt trạng thái đăng nhập
     */
    public void setLoggedIn(boolean isLoggedIn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    /**
     * Kiểm tra trạng thái đăng nhập
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Xóa tất cả dữ liệu (khi logout)
     */
    public void clearAll() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    // Static methods for convenience (used by VideoFragment and other components)

    /**
     * Static method để lấy user ID
     */
    public static String getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Static method để lấy user country
     */
    public static String getUserCountry(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_COUNTRY, "VN"); // Default to Vietnam
    }

    /**
     * Static method để set user country
     */
    public static void setUserCountry(Context context, String country) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_COUNTRY, country);
        editor.apply();
    }

    /**
     * Lưu user country
     */
    public void setUserCountry(String country) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_COUNTRY, country);
        editor.apply();
    }

    /**
     * Lấy user country
     */
    public String getUserCountry() {
        return sharedPreferences.getString(KEY_USER_COUNTRY, "VN");
    }
}
