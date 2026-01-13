package com.vhn.doan.services;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Service quản lý Custom Analytics - đẩy dữ liệu lên Realtime Database
 * để Admin Panel có thể đọc và hiển thị analytics
 */
public class CustomAnalyticsService {

    private static final String TAG = "CustomAnalyticsService";
    private static CustomAnalyticsService instance;
    private final DatabaseReference analyticsRef;
    private final String userAgent;

    // Event types - khớp với admin panel
    public static final String EVENT_TYPE_PAGE_VIEW = "page_view";
    public static final String EVENT_TYPE_USER_LOGIN = "user_login";
    public static final String EVENT_TYPE_CONTENT_VIEW = "content_view";
    public static final String EVENT_TYPE_VIDEO_VIEW = "video_view";
    public static final String EVENT_TYPE_SEARCH = "search";
    public static final String EVENT_TYPE_FAVORITE_ADD = "favorite_add";
    public static final String EVENT_TYPE_FAVORITE_REMOVE = "favorite_remove";
    public static final String EVENT_TYPE_REMINDER_SET = "reminder_set";
    public static final String EVENT_TYPE_NOTIFICATION_OPEN = "notification_open";

    private CustomAnalyticsService(Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        analyticsRef = database.getReference("analytics");
        
        // Get user agent for device tracking
        userAgent = getUserAgent(context);
        
        Log.d(TAG, "CustomAnalyticsService initialized");
    }

    /**
     * Get singleton instance
     */
    public static synchronized CustomAnalyticsService getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new CustomAnalyticsService(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Track một sự kiện analytics
     * 
     * @param type Loại sự kiện (EVENT_TYPE_*)
     * @param data Dữ liệu bổ sung (có thể null)
     */
    public void track(@NonNull String type, @Nullable Map<String, Object> data) {
        try {
            // Lấy user ID hiện tại
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String userId = currentUser != null ? currentUser.getUid() : "anonymous";

            // Tạo event object
            Map<String, Object> event = new HashMap<>();
            event.put("type", type);
            event.put("userId", userId);
            event.put("timestamp", System.currentTimeMillis());
            event.put("userAgent", userAgent);
            
            if (data != null && !data.isEmpty()) {
                event.put("data", data);
            }

            // Push lên Firebase Realtime Database
            analyticsRef.push().setValue(event)
                    .addOnSuccessListener(aVoid -> 
                        Log.d(TAG, "Analytics tracked: " + type))
                    .addOnFailureListener(e -> 
                        Log.e(TAG, "Failed to track analytics: " + type, e));

        } catch (Exception e) {
            Log.e(TAG, "Error tracking analytics", e);
        }
    }

    /**
     * Track page view
     */
    public void trackPageView(@NonNull String pageName) {
        Map<String, Object> data = new HashMap<>();
        data.put("page", pageName);
        track(EVENT_TYPE_PAGE_VIEW, data);
    }

    /**
     * Track user login
     */
    public void trackUserLogin() {
        track(EVENT_TYPE_USER_LOGIN, null);
    }

    /**
     * Track content view (health tip)
     */
    public void trackContentView(@NonNull String contentId, @NonNull String contentTitle, @Nullable String categoryId) {
        Map<String, Object> data = new HashMap<>();
        data.put("contentId", contentId);
        data.put("contentTitle", contentTitle);
        if (categoryId != null) {
            data.put("categoryId", categoryId);
        }
        track(EVENT_TYPE_CONTENT_VIEW, data);
    }

    /**
     * Track video view
     */
    public void trackVideoView(@NonNull String videoId, @NonNull String videoTitle) {
        Map<String, Object> data = new HashMap<>();
        data.put("videoId", videoId);
        data.put("videoTitle", videoTitle);
        track(EVENT_TYPE_VIDEO_VIEW, data);
    }

    /**
     * Track search
     */
    public void trackSearch(@NonNull String searchQuery, @Nullable String searchType) {
        Map<String, Object> data = new HashMap<>();
        data.put("query", searchQuery);
        if (searchType != null) {
            data.put("searchType", searchType);
        }
        track(EVENT_TYPE_SEARCH, data);
    }

    /**
     * Track favorite add
     */
    public void trackFavoriteAdd(@NonNull String contentId, @NonNull String contentTitle) {
        Map<String, Object> data = new HashMap<>();
        data.put("contentId", contentId);
        data.put("contentTitle", contentTitle);
        track(EVENT_TYPE_FAVORITE_ADD, data);
    }

    /**
     * Track favorite remove
     */
    public void trackFavoriteRemove(@NonNull String contentId, @NonNull String contentTitle) {
        Map<String, Object> data = new HashMap<>();
        data.put("contentId", contentId);
        data.put("contentTitle", contentTitle);
        track(EVENT_TYPE_FAVORITE_REMOVE, data);
    }

    /**
     * Track reminder set
     */
    public void trackReminderSet(@NonNull String reminderId, @Nullable String reminderType) {
        Map<String, Object> data = new HashMap<>();
        data.put("reminderId", reminderId);
        if (reminderType != null) {
            data.put("reminderType", reminderType);
        }
        track(EVENT_TYPE_REMINDER_SET, data);
    }

    /**
     * Track notification open
     */
    public void trackNotificationOpen(@NonNull String notificationId, @Nullable String notificationType) {
        Map<String, Object> data = new HashMap<>();
        data.put("notificationId", notificationId);
        if (notificationType != null) {
            data.put("notificationType", notificationType);
        }
        track(EVENT_TYPE_NOTIFICATION_OPEN, data);
    }

    /**
     * Get user agent string for device identification
     */
    private String getUserAgent(Context context) {
        try {
            // Build user agent similar to browser
            String deviceInfo = Build.MANUFACTURER + " " + Build.MODEL;
            String osVersion = "Android " + Build.VERSION.RELEASE;
            
            return "HealthTipsApp/1.0 (" + deviceInfo + "; " + osVersion + ")";
        } catch (Exception e) {
            return "HealthTipsApp/1.0 (Android)";
        }
    }
}
