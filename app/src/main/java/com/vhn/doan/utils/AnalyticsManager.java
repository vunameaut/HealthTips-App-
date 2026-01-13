package com.vhn.doan.utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.vhn.doan.services.CustomAnalyticsService;

/**
 * Singleton class để quản lý Firebase Analytics & Custom Analytics
 * - Firebase Analytics: Cho Firebase Console insights
 * - Custom Analytics: Cho Admin Panel (Realtime Database)
 */
public class AnalyticsManager {

    private static final String TAG = "AnalyticsManager";
    private static AnalyticsManager instance;
    private FirebaseAnalytics firebaseAnalytics;
    private CustomAnalyticsService customAnalytics;

    // Analytics Event Names
    public static final String EVENT_VIEW_HEALTH_TIP = "view_health_tip";
    public static final String EVENT_SEARCH = "search";
    public static final String EVENT_AI_CHAT_MESSAGE = "ai_chat_message";
    public static final String EVENT_REMINDER_CREATED = "reminder_created";
    public static final String EVENT_VIDEO_VIEW = "video_view";
    public static final String EVENT_VIDEO_LIKE = "video_like";
    public static final String EVENT_VIDEO_SHARE = "video_share";
    public static final String EVENT_TIP_FAVORITE = "tip_favorite";
    public static final String EVENT_TIP_UNFAVORITE = "tip_unfavorite";
    public static final String EVENT_TIP_LIKE = "tip_like";
    public static final String EVENT_TIP_UNLIKE = "tip_unlike";
    public static final String EVENT_TIP_SHARE = "tip_share";

    // Analytics Parameter Names
    public static final String PARAM_TIP_ID = "tip_id";
    public static final String PARAM_TIP_TITLE = "tip_title";
    public static final String PARAM_TIP_CATEGORY = "tip_category";
    public static final String PARAM_SEARCH_TERM = FirebaseAnalytics.Param.SEARCH_TERM;
    public static final String PARAM_CONVERSATION_ID = "conversation_id";
    public static final String PARAM_MESSAGE_LENGTH = "message_length";
    public static final String PARAM_VIDEO_ID = "video_id";
    public static final String PARAM_VIDEO_TITLE = "video_title";
    public static final String PARAM_VIDEO_POSITION = "video_position";
    public static final String PARAM_REMINDER_TYPE = "reminder_type";
    public static final String PARAM_SEARCH_TYPE = "search_type"; // "healthtip" or "video"

    private AnalyticsManager(Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context.getApplicationContext());
        customAnalytics = CustomAnalyticsService.getInstance(context.getApplicationContext());
        Log.d(TAG, "AnalyticsManager initialized with Firebase & Custom Analytics");
    }

    /**
     * Lấy instance của AnalyticsManager (Singleton)
     */
    public static synchronized AnalyticsManager getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new AnalyticsManager(context);
        }
        return instance;
    }

    /**
     * Log sự kiện xem chi tiết health tip
     * Được gọi trong HealthTipDetailActivity
     */
    public void logViewHealthTip(@NonNull String tipId, @NonNull String tipTitle, @Nullable String category) {
        // Firebase Analytics
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_TIP_ID, tipId);
        bundle.putString(PARAM_TIP_TITLE, tipTitle);
        if (category != null && !category.isEmpty()) {
            bundle.putString(PARAM_TIP_CATEGORY, category);
        }
        firebaseAnalytics.logEvent(EVENT_VIEW_HEALTH_TIP, bundle);
        
        // Custom Analytics cho Admin Panel
        customAnalytics.trackContentView(tipId, tipTitle, category);
        
        Log.d(TAG, "Logged: view_health_tip - " + tipTitle);
    }

    /**
     * Log sự kiện tìm kiếm
     * Được gọi trong SearchActivity
     */
    public void logSearch(@NonNull String searchTerm, @Nullable String searchType) {
        // Firebase Analytics
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_SEARCH_TERM, searchTerm);
        if (searchType != null) {
            bundle.putString(PARAM_SEARCH_TYPE, searchType);
        }
        firebaseAnalytics.logEvent(EVENT_SEARCH, bundle);
        
        // Custom Analytics cho Admin Panel
        customAnalytics.trackSearch(searchTerm, searchType);
        
        Log.d(TAG, "Logged: search - " + searchTerm);
    }

    /**
     * Log sự kiện chat AI
     * Được gọi trong ChatDetailFragment khi gửi tin nhắn
     */
    public void logAiChatMessage(@NonNull String conversationId, int messageLength) {
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_CONVERSATION_ID, conversationId);
        bundle.putInt(PARAM_MESSAGE_LENGTH, messageLength);

        firebaseAnalytics.logEvent(EVENT_AI_CHAT_MESSAGE, bundle);
        Log.d(TAG, "Logged: ai_chat_message - conversation: " + conversationId);
    }

    /**
     * Log sự kiện tạo nhắc nhở
     * Được gọi trong ReminderEditorActivity
     */
    public void logReminderCreated(@Nullable String reminderType) {
        // Firebase Analytics
        Bundle bundle = new Bundle();
        if (reminderType != null) {
            bundle.putString(PARAM_REMINDER_TYPE, reminderType);
        }
        firebaseAnalytics.logEvent(EVENT_REMINDER_CREATED, bundle);
        
        // Custom Analytics cho Admin Panel
        customAnalytics.trackReminderSet("reminder_" + System.currentTimeMillis(), reminderType);
        
        Log.d(TAG, "Logged: reminder_created");
    }

    /**
     * Log sự kiện xem video
     * Được gọi trong VideoFragment khi video được phát
     */
    public void logVideoView(@NonNull String videoId, @NonNull String videoTitle, int position) {
        // Firebase Analytics
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_VIDEO_ID, videoId);
        bundle.putString(PARAM_VIDEO_TITLE, videoTitle);
        bundle.putInt(PARAM_VIDEO_POSITION, position);
        firebaseAnalytics.logEvent(EVENT_VIDEO_VIEW, bundle);
        
        // Custom Analytics cho Admin Panel
        customAnalytics.trackVideoView(videoId, videoTitle);
        
        Log.d(TAG, "Logged: video_view - " + videoTitle);
    }

    /**
     * Log sự kiện like video
     */
    public void logVideoLike(@NonNull String videoId, @NonNull String videoTitle) {
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_VIDEO_ID, videoId);
        bundle.putString(PARAM_VIDEO_TITLE, videoTitle);

        firebaseAnalytics.logEvent(EVENT_VIDEO_LIKE, bundle);
        Log.d(TAG, "Logged: video_like - " + videoTitle);
    }

    /**
     * Log sự kiện share video
     */
    public void logVideoShare(@NonNull String videoId, @NonNull String videoTitle) {
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_VIDEO_ID, videoId);
        bundle.putString(PARAM_VIDEO_TITLE, videoTitle);

        firebaseAnalytics.logEvent(EVENT_VIDEO_SHARE, bundle);
        Log.d(TAG, "Logged: video_share - " + videoTitle);
    }

    /**
     * Log sự kiện thêm tip vào yêu thích
     */
    public void logTipFavorite(@NonNull String tipId, @NonNull String tipTitle) {
        // Firebase Analytics
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_TIP_ID, tipId);
        bundle.putString(PARAM_TIP_TITLE, tipTitle);
        firebaseAnalytics.logEvent(EVENT_TIP_FAVORITE, bundle);
        
        // Custom Analytics cho Admin Panel
        customAnalytics.trackFavoriteAdd(tipId, tipTitle);
        
        Log.d(TAG, "Logged: tip_favorite - " + tipTitle);
    }

    /**
     * Log sự kiện bỏ tip khỏi yêu thích
     */
    public void logTipUnfavorite(@NonNull String tipId, @NonNull String tipTitle) {
        // Firebase Analytics
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_TIP_ID, tipId);
        bundle.putString(PARAM_TIP_TITLE, tipTitle);
        firebaseAnalytics.logEvent(EVENT_TIP_UNFAVORITE, bundle);
        
        // Custom Analytics cho Admin Panel
        customAnalytics.trackFavoriteRemove(tipId, tipTitle);
        
        Log.d(TAG, "Logged: tip_unfavorite - " + tipTitle);
    }

    /**
     * Log sự kiện like tip
     */
    public void logTipLike(@NonNull String tipId, @NonNull String tipTitle) {
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_TIP_ID, tipId);
        bundle.putString(PARAM_TIP_TITLE, tipTitle);

        firebaseAnalytics.logEvent(EVENT_TIP_LIKE, bundle);
        Log.d(TAG, "Logged: tip_like - " + tipTitle);
    }

    /**
     * Log sự kiện unlike tip
     */
    public void logTipUnlike(@NonNull String tipId, @NonNull String tipTitle) {
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_TIP_ID, tipId);
        bundle.putString(PARAM_TIP_TITLE, tipTitle);

        firebaseAnalytics.logEvent(EVENT_TIP_UNLIKE, bundle);
        Log.d(TAG, "Logged: tip_unlike - " + tipTitle);
    }

    /**
     * Log sự kiện share tip
     */
    public void logTipShare(@NonNull String tipId, @NonNull String tipTitle) {
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_TIP_ID, tipId);
        bundle.putString(PARAM_TIP_TITLE, tipTitle);

        firebaseAnalytics.logEvent(EVENT_TIP_SHARE, bundle);
        Log.d(TAG, "Logged: tip_share - " + tipTitle);
    }

    /**
     * Log custom event với bundle tùy chỉnh
     */
    public void logCustomEvent(@NonNull String eventName, @Nullable Bundle params) {
        firebaseAnalytics.logEvent(eventName, params);
        Log.d(TAG, "Logged custom event: " + eventName);
    }

    /**
     * Set user ID cho analytics
     */
    public void setUserId(@Nullable String userId) {
        firebaseAnalytics.setUserId(userId);
        Log.d(TAG, "Set user ID: " + userId);
    }

    /**
     * Set user property
     */
    public void setUserProperty(@NonNull String name, @Nullable String value) {
        firebaseAnalytics.setUserProperty(name, value);
        Log.d(TAG, "Set user property: " + name + " = " + value);
    }
}
