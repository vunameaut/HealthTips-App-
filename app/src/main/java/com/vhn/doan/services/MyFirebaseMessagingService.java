package com.vhn.doan.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.vhn.doan.R;
import com.vhn.doan.data.NotificationHistory;
import com.vhn.doan.data.NotificationPriority;
import com.vhn.doan.data.NotificationType;
import com.vhn.doan.data.repository.NotificationHistoryRepositoryImpl;
import com.vhn.doan.data.repository.RepositoryCallback;
import com.vhn.doan.presentation.settings.content.NotificationSettingsActivity;
import com.vhn.doan.utils.SessionManager;

import java.util.Map;

/**
 * Service để xử lý Firebase Cloud Messaging (FCM)
 * - Nhận và xử lý push notifications với Deep Linking
 * - Lưu FCM token vào Firebase Database
 * - Hỗ trợ nhiều loại notifications: comment reply, new health tip, new video, etc.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "health_tips_notifications";

    // Notification types
    public static final String TYPE_COMMENT_REPLY = "comment_reply";
    public static final String TYPE_NEW_HEALTH_TIP = "new_health_tip";
    public static final String TYPE_NEW_VIDEO = "new_video";
    public static final String TYPE_COMMENT_LIKE = "comment_like";
    public static final String TYPE_HEALTH_TIP_RECOMMENDATION = "health_tip_recommendation";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM Token: " + token);

        // Lưu token vào Firebase Database
        saveFCMTokenToDatabase(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Log.d(TAG, "Message received from: " + message.getFrom());

        // Xử lý data payload
        if (!message.getData().isEmpty()) {
            Map<String, String> data = message.getData();
            Log.d(TAG, "Message data payload: " + data);
            handleNotificationData(data);
        }
    }

    /**
     * Xử lý data payload và hiển thị notification phù hợp
     */
    private void handleNotificationData(Map<String, String> data) {
        String type = data.get("type");
        String title = data.get("title");
        String body = data.get("body");

        if (type == null || title == null || body == null) {
            Log.w(TAG, "Invalid notification data");
            return;
        }

        // ✅ CHECK: Kiểm tra notification settings trước khi hiển thị
        if (!shouldShowNotification(type)) {
            Log.d(TAG, "Notification type '" + type + "' is disabled in settings. Skipping notification.");
            // Vẫn lưu vào history nhưng không hiển thị notification
            saveNotificationToHistory(data);
            return;
        }

        Intent intent = createDeepLinkIntent(type, data);
        showNotification(title, body, intent);

        // Lưu notification vào history
        saveNotificationToHistory(data);
    }

    /**
     * ✅ NEW: Kiểm tra xem notification type này có được bật trong settings không
     */
    private boolean shouldShowNotification(String type) {
        // Map FCM notification types to NotificationSettings keys
        String settingsKey = mapTypeToSettingsKey(type);
        if (settingsKey == null) {
            // Unknown type, show by default
            return true;
        }

        // Check if this notification type is enabled
        return NotificationSettingsActivity.isNotificationEnabled(this, settingsKey);
    }

    /**
     * ✅ NEW: Map FCM notification type sang NotificationSettings key
     */
    private String mapTypeToSettingsKey(String fcmType) {
        switch (fcmType) {
            case TYPE_COMMENT_REPLY:
                return "comment_reply";
            case TYPE_NEW_HEALTH_TIP:
                return "new_health_tip";
            case TYPE_NEW_VIDEO:
                return "new_video";
            case TYPE_COMMENT_LIKE:
                return "comment_like";
            case TYPE_HEALTH_TIP_RECOMMENDATION:
                return "recommendations";
            default:
                return null;
        }
    }

    /**
     * Tạo Intent cho deep linking dựa trên notification type
     */
    private Intent createDeepLinkIntent(String type, Map<String, String> data) {
        // Import DeepLinkHandlerActivity - sẽ tạo trong bước tiếp theo
        Intent intent = new Intent(this, com.vhn.doan.presentation.deeplink.DeepLinkHandlerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Thêm notification type
        intent.putExtra("notification_type", type);

        // Thêm data tùy theo type
        switch (type) {
            case TYPE_COMMENT_REPLY:
                intent.putExtra("video_id", data.get("videoId"));
                intent.putExtra("parent_comment_id", data.get("parentCommentId"));
                intent.putExtra("reply_comment_id", data.get("replyCommentId"));
                intent.putExtra("sender_name", data.get("senderName"));
                intent.putExtra("reply_text", data.get("replyText"));
                break;

            case TYPE_NEW_HEALTH_TIP:
                intent.putExtra("health_tip_id", data.get("healthTipId"));
                intent.putExtra("category_id", data.get("categoryId"));
                break;

            case TYPE_NEW_VIDEO:
                intent.putExtra("video_id", data.get("videoId"));
                break;

            case TYPE_COMMENT_LIKE:
                intent.putExtra("video_id", data.get("videoId"));
                intent.putExtra("comment_id", data.get("commentId"));
                intent.putExtra("sender_name", data.get("senderName"));
                break;

            case TYPE_HEALTH_TIP_RECOMMENDATION:
                // DeepLinkHandlerActivity expect "tips_json" key
                String tipsJson = data.get("tips");
                intent.putExtra("tips_json", tipsJson);
                intent.putExtra("tips", tipsJson); // Backward compatibility
                intent.putExtra("tips_count", data.get("tipsCount"));
                Log.d(TAG, "Recommendation notification data: tips=" + tipsJson);
                break;
        }

        return intent;
    }

    /**
     * Hiển thị notification với PendingIntent
     * ✅ UPDATED: Tôn trọng sound và vibration settings
     */
    private void showNotification(String title, String body, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            (int) System.currentTimeMillis(),
            intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(body));

        // ✅ CHECK: Tôn trọng sound và vibration settings
        boolean soundEnabled = NotificationSettingsActivity.isSoundEnabled(this);
        boolean vibrationEnabled = NotificationSettingsActivity.isVibrationEnabled(this);

        int defaults = 0;
        if (soundEnabled) {
            defaults |= NotificationCompat.DEFAULT_SOUND;
        }
        if (vibrationEnabled) {
            defaults |= NotificationCompat.DEFAULT_VIBRATE;
        }
        // Always show lights
        defaults |= NotificationCompat.DEFAULT_LIGHTS;

        builder.setDefaults(defaults);

        Log.d(TAG, "Notification settings - Sound: " + soundEnabled + ", Vibration: " + vibrationEnabled);

        NotificationManager notificationManager =
            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Notification displayed: " + title);
        }
    }

    /**
     * Tạo Notification Channel (Required cho Android 8.0+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Thông báo HealthTips",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Nhận thông báo về bình luận, bài viết mới");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "Notification Channel created: " + CHANNEL_ID);
            }
        }
    }

    /**
     * Lưu FCM token vào Firebase Database
     */
    private void saveFCMTokenToDatabase(String token) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
            ? FirebaseAuth.getInstance().getCurrentUser().getUid()
            : null;

        if (userId != null) {
            FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("fcmToken")
                .setValue(token)
                .addOnSuccessListener(aVoid ->
                    Log.d(TAG, "FCM token saved successfully"))
                .addOnFailureListener(e ->
                    Log.e(TAG, "Failed to save FCM token", e));
        } else {
            Log.w(TAG, "User not logged in, cannot save FCM token");
        }
    }

    /**
     * Lưu notification vào NotificationHistory
     */
    private void saveNotificationToHistory(Map<String, String> data) {
        try {
            // Lấy user ID từ SessionManager hoặc Firebase Auth
            SessionManager sessionManager = new SessionManager(this);
            String userId = sessionManager.getCurrentUserId();

            if (userId == null || userId.isEmpty()) {
                // Thử lấy từ Firebase Auth nếu SessionManager không có
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                }
            }

            if (userId == null || userId.isEmpty()) {
                Log.w(TAG, "Cannot save notification to history: User not logged in");
                return;
            }

            // Tạo NotificationHistory object
            NotificationHistory notification = new NotificationHistory();
            notification.setUserId(userId);
            notification.setTitle(data.get("title"));
            notification.setBody(data.get("body"));
            notification.setImageUrl(data.get("image"));

            // Set notification type
            String typeStr = data.get("type");
            NotificationType type = NotificationType.fromValue(typeStr);
            notification.setType(type);

            // Set priority (mặc định là HIGH cho FCM)
            notification.setPriority(NotificationPriority.HIGH);

            // Create deep link
            String deepLink = createDeepLink(typeStr, data);
            notification.setDeepLink(deepLink);

            // Set target ID và type
            notification.setTargetId(getTargetId(typeStr, data));
            notification.setTargetType(getTargetType(typeStr));

            // Set extra data for comment-related notifications
            String extraData = getExtraData(typeStr, data);
            if (extraData != null) {
                notification.setExtraData(extraData);
            }

            // Lưu vào database
            NotificationHistoryRepositoryImpl repository = NotificationHistoryRepositoryImpl.getInstance(this);
            repository.saveNotification(notification, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Log.d(TAG, "Notification saved to history successfully");
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Failed to save notification to history: " + error);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error saving notification to history", e);
        }
    }

    /**
     * Tạo deep link string từ notification data
     */
    private String createDeepLink(String type, Map<String, String> data) {
        String baseUrl = "healthtips://";

        switch (type) {
            case TYPE_COMMENT_REPLY:
                return baseUrl + "video/" + data.get("videoId") + "?comment=" + data.get("replyCommentId");

            case TYPE_NEW_HEALTH_TIP:
                return baseUrl + "tip/" + data.get("healthTipId");

            case TYPE_NEW_VIDEO:
                return baseUrl + "video/" + data.get("videoId");

            case TYPE_COMMENT_LIKE:
                return baseUrl + "video/" + data.get("videoId") + "?comment=" + data.get("commentId");

            case TYPE_HEALTH_TIP_RECOMMENDATION:
                return baseUrl + "recommendations";

            default:
                return baseUrl + "home";
        }
    }

    /**
     * Lấy target ID từ notification data
     */
    private String getTargetId(String type, Map<String, String> data) {
        switch (type) {
            case TYPE_COMMENT_REPLY:
            case TYPE_COMMENT_LIKE:
            case TYPE_NEW_VIDEO:
                return data.get("videoId");

            case TYPE_NEW_HEALTH_TIP:
                return data.get("healthTipId");

            case TYPE_HEALTH_TIP_RECOMMENDATION:
                // Extract first tip ID from tips JSON
                String tipsJson = data.get("tips");
                if (tipsJson != null && !tipsJson.isEmpty()) {
                    try {
                        org.json.JSONArray tipsArray = new org.json.JSONArray(tipsJson);
                        if (tipsArray.length() > 0) {
                            org.json.JSONObject firstTip = tipsArray.getJSONObject(0);
                            return firstTip.getString("healthTipId");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing tips JSON for targetId", e);
                    }
                }
                return null;

            default:
                return null;
        }
    }

    /**
     * Lấy target type từ notification type
     */
    private String getTargetType(String type) {
        switch (type) {
            case TYPE_COMMENT_REPLY:
            case TYPE_COMMENT_LIKE:
                return "comment";

            case TYPE_NEW_VIDEO:
                return "video";

            case TYPE_NEW_HEALTH_TIP:
                return "health_tip";

            case TYPE_HEALTH_TIP_RECOMMENDATION:
                return "recommendation";

            default:
                return "unknown";
        }
    }

    /**
     * Lấy extra data JSON string từ notification data
     * Dùng để lưu thêm thông tin như comment_id cho comment notifications
     */
    private String getExtraData(String type, Map<String, String> data) {
        try {
            org.json.JSONObject extraJson = new org.json.JSONObject();

            switch (type) {
                case TYPE_COMMENT_REPLY:
                    String replyCommentId = data.get("replyCommentId");
                    String parentCommentId = data.get("parentCommentId");
                    if (replyCommentId != null) {
                        extraJson.put("comment_id", replyCommentId);
                    }
                    if (parentCommentId != null) {
                        extraJson.put("parent_comment_id", parentCommentId);
                    }
                    break;

                case TYPE_COMMENT_LIKE:
                    String commentId = data.get("commentId");
                    if (commentId != null) {
                        extraJson.put("comment_id", commentId);
                    }
                    break;

                default:
                    return null;
            }

            return extraJson.length() > 0 ? extraJson.toString() : null;
        } catch (Exception e) {
            Log.e(TAG, "Error creating extra data JSON", e);
            return null;
        }
    }
}
