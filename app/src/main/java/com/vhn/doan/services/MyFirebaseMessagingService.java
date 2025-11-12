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

        Intent intent = createDeepLinkIntent(type, data);
        showNotification(title, body, intent);
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
                intent.putExtra("tips", data.get("tips"));
                intent.putExtra("tips_count", data.get("tipsCount"));
                break;
        }

        return intent;
    }

    /**
     * Hiển thị notification với PendingIntent
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
}
