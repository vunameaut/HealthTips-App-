package com.vhn.doan.utils;

/**
 * Constants.java - Lớp chứa các hằng số được sử dụng trong ứng dụng
 *
 * File này chứa các hằng số quan trọng như:
 * - Key Firebase
 * - Tên các collection trong database
 * - Đường dẫn cho Storage
 * - Routes cho điều hướng
 * - Các giá trị dùng chung khác
 */
public class Constants {

    // Firebase API Keys và Configuration
    public static final String FIREBASE_WEB_API_KEY = "AIzaSyCxJ5fASsMWruG9D00QGip3JW3Q0HvZp7g";

    // Firebase Collection Paths
    public static final String USERS_COLLECTION = "users";
    public static final String POSTS_COLLECTION = "posts";
    public static final String MESSAGES_COLLECTION = "messages";
    public static final String COMMENTS_COLLECTION = "comments";

    // Firebase Storage Paths
    public static final String PROFILE_IMAGES_PATH = "profile_images";
    public static final String POST_IMAGES_PATH = "post_images";

    // Shared Preferences
    public static final String PREFS_NAME = "app_preferences";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";

    // Request Codes
    public static final int RC_SIGN_IN = 100;
    public static final int RC_IMAGE_PICK = 101;

    // Intent extras
    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_POST_ID = "extra_post_id";

    // Validation constants
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_USERNAME_LENGTH = 30;

    // Network timeout values (in milliseconds)
    public static final long DEFAULT_TIMEOUT = 30000; // 30 seconds

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;

    // Notification channels
    public static final String CHANNEL_GENERAL = "general_channel";
    public static final String CHANNEL_MESSAGES = "messages_channel";

    // Routes for Navigation
    public static final String ROUTE_HOME = "home";
    public static final String ROUTE_PROFILE = "profile";
    public static final String ROUTE_MESSAGES = "messages";
    public static final String ROUTE_NOTIFICATIONS = "notifications";
    public static final String ROUTE_SETTINGS = "settings";
}
