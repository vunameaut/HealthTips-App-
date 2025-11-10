package com.vhn.doan.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.vhn.doan.R;
import com.vhn.doan.presentation.home.HomeActivity;

/**
 * Service để xử lý Firebase Cloud Messaging (FCM)
 * - Nhận và xử lý push notifications
 * - Lưu FCM token để gửi thông báo targeted
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "health_tips_fcm_channel";
    private static final String CHANNEL_NAME = "Thông báo HealthTips";
    private static final String CHANNEL_DESCRIPTION = "Nhận thông báo về mẹo sức khỏe mới, cập nhật từ AI";

    @Override
    public void onCreate() {
        super.onCreate();
        // Tạo notification channel khi service được khởi tạo
        createNotificationChannel();
    }

    /**
     * Được gọi khi có FCM token mới
     * Token này dùng để gửi notification cho device cụ thể
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM Token mới: " + token);

        // TODO: Gửi token này lên Firebase Database để lưu lại
        // Bạn sẽ cần token này để gửi thông báo cho user cụ thể
        sendTokenToServer(token);
    }

    /**
     * Được gọi khi nhận được push notification từ Firebase
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Log.d(TAG, "Nhận được message từ: " + message.getFrom());

        // Kiểm tra xem message có data payload không
        if (!message.getData().isEmpty()) {
            Log.d(TAG, "Message data payload: " + message.getData());
            // Xử lý data payload nếu cần
            handleDataPayload(message.getData());
        }

        // Kiểm tra xem message có notification payload không
        if (message.getNotification() != null) {
            String title = message.getNotification().getTitle();
            String body = message.getNotification().getBody();
            Log.d(TAG, "Message Notification - Title: " + title + ", Body: " + body);

            // Hiển thị notification
            showNotification(title, body);
        }
    }

    /**
     * Xử lý data payload từ FCM message
     */
    private void handleDataPayload(java.util.Map<String, String> data) {
        // Ví dụ: Xử lý các loại notification khác nhau
        String type = data.get("type");

        if (type != null) {
            switch (type) {
                case "new_health_tip":
                    // Có mẹo sức khỏe mới
                    String tipTitle = data.get("tip_title");
                    showNotification("Mẹo sức khỏe mới!", tipTitle);
                    break;

                case "ai_response":
                    // AI đã trả lời câu hỏi
                    showNotification("AI đã trả lời", "Bấm để xem câu trả lời của bạn");
                    break;

                case "daily_tip":
                    // Mẹo sức khỏe hàng ngày
                    String dailyTip = data.get("message");
                    showNotification("Mẹo sức khỏe hôm nay", dailyTip);
                    break;

                default:
                    Log.d(TAG, "Unknown notification type: " + type);
                    break;
            }
        }
    }

    /**
     * Hiển thị notification lên thanh trạng thái
     */
    private void showNotification(String title, String body) {
        // Tạo intent để mở HomeActivity khi click vào notification
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Tạo notification builder
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_reminder) // Sử dụng icon có sẵn
            .setContentTitle(title != null ? title : "HealthTips")
            .setContentText(body != null ? body : "")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(body != null ? body : ""));

        // Lấy NotificationManager và hiển thị
        NotificationManager notificationManager =
            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            // Sử dụng timestamp làm notification ID để mỗi notification là unique
            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, notificationBuilder.build());
            Log.d(TAG, "Đã hiển thị notification: " + title);
        }
    }

    /**
     * Tạo Notification Channel cho Android 8.0+ (API 26+)
     * Bắt buộc phải có để hiển thị notification trên Android 8.0+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification Channel đã được tạo: " + CHANNEL_ID);
            }
        }
    }

    /**
     * Gửi FCM token lên server để lưu trữ
     * Token này sẽ được dùng để gửi notification cho user cụ thể
     */
    private void sendTokenToServer(String token) {
        // TODO: Implement logic để lưu token lên Firebase Database
        // Ví dụ:
        // FirebaseDatabase.getInstance()
        //     .getReference("users")
        //     .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
        //     .child("fcmToken")
        //     .setValue(token);

        Log.d(TAG, "Cần implement logic lưu token lên server");
        Log.d(TAG, "Token: " + token);
    }
}
