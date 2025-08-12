package com.vhn.doan.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Helper class để quản lý các chức năng Firebase Authentication
 */
public class FirebaseAuthHelper {

    private static FirebaseAuth firebaseAuth;

    /**
     * Khởi tạo FirebaseAuth instance
     */
    private static FirebaseAuth getFirebaseAuth() {
        if (firebaseAuth == null) {
            firebaseAuth = FirebaseAuth.getInstance();
        }
        return firebaseAuth;
    }

    /**
     * Lấy user ID của người dùng hiện tại
     * @return User ID nếu đã đăng nhập, null nếu chưa đăng nhập
     */
    public static String getCurrentUserId() {
        FirebaseUser currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    /**
     * Lấy thông tin người dùng hiện tại
     * @return FirebaseUser nếu đã đăng nhập, null nếu chưa đăng nhập
     */
    public static FirebaseUser getCurrentUser() {
        return getFirebaseAuth().getCurrentUser();
    }

    /**
     * Kiểm tra xem người dùng đã đăng nhập hay chưa
     * @return true nếu đã đăng nhập, false nếu chưa
     */
    public static boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    /**
     * Lấy email của người dùng hiện tại
     * @return Email nếu đã đăng nhập, null nếu chưa đăng nhập hoặc không có email
     */
    public static String getCurrentUserEmail() {
        FirebaseUser currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getEmail() : null;
    }

    /**
     * Lấy tên hiển thị của người dùng hiện tại
     * @return Tên hiển thị nếu có, null nếu không có
     */
    public static String getCurrentUserDisplayName() {
        FirebaseUser currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getDisplayName() : null;
    }

    /**
     * Đăng xuất người dùng hiện tại
     */
    public static void signOut() {
        getFirebaseAuth().signOut();
    }

    /**
     * Lấy Firebase Auth instance
     * @return FirebaseAuth instance
     */
    public static FirebaseAuth getAuthInstance() {
        return getFirebaseAuth();
    }

    /**
     * Kiểm tra xem email của người dùng đã được xác minh chưa
     * @return true nếu email đã xác minh, false nếu chưa hoặc chưa đăng nhập
     */
    public static boolean isEmailVerified() {
        FirebaseUser currentUser = getCurrentUser();
        return currentUser != null && currentUser.isEmailVerified();
    }

    /**
     * Lấy URL ảnh đại diện của người dùng hiện tại
     * @return URL ảnh đại diện nếu có, null nếu không có
     */
    public static String getCurrentUserPhotoUrl() {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser != null && currentUser.getPhotoUrl() != null) {
            return currentUser.getPhotoUrl().toString();
        }
        return null;
    }

    /**
     * Callback interface cho các thao tác authentication
     */
    public interface AuthCallback {
        void onSuccess();
        void onError(String error);
    }

    /**
     * Callback interface cho việc lấy thông tin user
     */
    public interface UserInfoCallback {
        void onSuccess(FirebaseUser user);
        void onError(String error);
    }
}
