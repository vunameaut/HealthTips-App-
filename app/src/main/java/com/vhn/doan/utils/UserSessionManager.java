package com.vhn.doan.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Quản lý phiên đăng nhập của người dùng
 */
public class UserSessionManager {

    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private FirebaseAuth firebaseAuth;

    public UserSessionManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Lưu thông tin phiên đăng nhập
     */
    public void createLoginSession(String userId, String email, String name) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Kiểm tra trạng thái đăng nhập
     */
    public boolean isLoggedIn() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return currentUser != null && sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Lấy ID người dùng hiện tại
     */
    public String getCurrentUserId() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        }
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    /**
     * Lấy email người dùng hiện tại
     */
    public String getCurrentUserEmail() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getEmail();
        }
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Lấy tên người dùng hiện tại
     */
    public String getCurrentUserName() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getDisplayName();
        }
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    /**
     * Xóa phiên đăng nhập
     */
    public void logoutUser() {
        editor.clear();
        editor.apply();
        firebaseAuth.signOut();
    }

    /**
     * Cập nhật thông tin người dùng
     */
    public void updateUserInfo(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }
}
