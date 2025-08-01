package com.vhn.doan.utils;

/**
 * Class lưu trữ các hằng số dùng chung trong ứng dụng
 */
public class Constants {

    // Các key cho Firebase Authentication
    public static final String FIREBASE_AUTH_KEY = "AIzaSyA9TsK_lVXViYuZ_LjHj4MvM1LAPJxNJqQ"; // Key mẫu, cần thay bằng key thật

    // Đường dẫn tham chiếu Firebase Realtime Database
    public static final String USERS_REF = "users";
    public static final String CATEGORIES_REF = "categories";
    public static final String HEALTH_TIPS_REF = "health_tips";
    public static final String FAVORITES_REF = "favorites"; // Thêm path cho favorites theo cấu trúc test data
    public static final String USER_FAVORITES_REF = "user_favorites";
    public static final String USER_HISTORY_REF = "user_history";
    public static final String REMINDERS_NODE = "reminders"; // Node lưu trữ các reminder

    // Đường dẫn cho Firebase Firestore và Storage
    public static final String USERS_PATH = "users";
    public static final String PROFILE_IMAGES_PATH = "profile_images";
    public static final String USERS_COLLECTION = "users";

    // Các key cho SharedPreferences
    public static final String PREF_NAME = "HealthTipsPrefs";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_THEME_MODE = "theme_mode";

    // Các key cho Intent
    public static final String INTENT_CATEGORY_ID = "category_id";
    public static final String INTENT_HEALTH_TIP_ID = "health_tip_id";
    public static final String INTENT_SEARCH_QUERY = "search_query";
    public static final String CATEGORY_ID_KEY = "category_id"; // Key cho Bundle trong Fragment

    // Số lượng hiển thị mặc định
    public static final int DEFAULT_LIMIT = 10;

    // Các tham số giao diện
    public static final boolean DARK_MODE_DEFAULT = true;

    // Các thông báo
    public static final String ERROR_NETWORK = "Không có kết nối internet";
    public static final String ERROR_LOGIN_FAILED = "Đăng nhập thất bại";
    public static final String ERROR_REGISTRATION_FAILED = "Đăng ký thất bại";
    public static final String SUCCESS_LOGIN = "Đăng nhập thành công";
    public static final String SUCCESS_REGISTRATION = "Đăng ký thành công";
    public static final String SUCCESS_PASSWORD_RESET = "Đã gửi email đặt lại mật khẩu";

    // Các màu sắc gradient cho nút (được định nghĩa trong XML)
    public static final String PRIMARY_BUTTON_GRADIENT = "primary_button_gradient";
    public static final String SECONDARY_BUTTON_GRADIENT = "secondary_button_gradient";

    // Các action cho ReminderService
    public static final String ACTION_SET_REMINDER = "com.vhn.doan.action.SET_REMINDER";
    public static final String ACTION_SHOW_MISSED_REMINDER = "com.vhn.doan.action.SHOW_MISSED_REMINDER";
    public static final String ACTION_CANCEL_REMINDER = "com.vhn.doan.action.CANCEL_REMINDER";
    public static final String ACTION_REMINDER_TRIGGER = "com.vhn.doan.action.REMINDER_TRIGGER";

    // Các key cho Intent extras
    public static final String EXTRA_REMINDER = "com.vhn.doan.extra.REMINDER";
    public static final String EXTRA_REMINDER_ID = "com.vhn.doan.extra.REMINDER_ID";
    public static final String EXTRA_REMINDER_TITLE = "com.vhn.doan.extra.REMINDER_TITLE";
    public static final String EXTRA_REMINDER_MESSAGE = "com.vhn.doan.extra.REMINDER_MESSAGE";
}
