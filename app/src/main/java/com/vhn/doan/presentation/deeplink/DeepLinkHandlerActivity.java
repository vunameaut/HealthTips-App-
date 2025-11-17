package com.vhn.doan.presentation.deeplink;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.vhn.doan.presentation.healthtip.detail.HealthTipDetailActivity;
import com.vhn.doan.presentation.video.SingleVideoPlayerActivity;
import com.vhn.doan.services.MyFirebaseMessagingService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activity trung gian để xử lý deep linking từ notifications
 * Activity này sẽ parse notification data và điều hướng đến màn hình phù hợp
 */
public class DeepLinkHandlerActivity extends AppCompatActivity {

    private static final String TAG = "DeepLinkHandler";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Không cần setContentView vì đây là transparent activity

        Intent receivedIntent = getIntent();
        if (receivedIntent != null) {
            handleDeepLink(receivedIntent);
        } else {
            finish();
        }
    }

    /**
     * Xử lý deep link dựa trên notification type
     */
    private void handleDeepLink(Intent intent) {
        String notificationType = intent.getStringExtra("notification_type");

        if (notificationType == null) {
            Log.w(TAG, "No notification type found");
            finish();
            return;
        }

        Log.d(TAG, "Handling deep link for type: " + notificationType);

        switch (notificationType) {
            case MyFirebaseMessagingService.TYPE_COMMENT_REPLY:
                handleCommentReplyNotification(intent);
                break;

            case MyFirebaseMessagingService.TYPE_NEW_HEALTH_TIP:
                handleNewHealthTipNotification(intent);
                break;

            case MyFirebaseMessagingService.TYPE_NEW_VIDEO:
                handleNewVideoNotification(intent);
                break;

            case MyFirebaseMessagingService.TYPE_COMMENT_LIKE:
                handleCommentLikeNotification(intent);
                break;

            case MyFirebaseMessagingService.TYPE_HEALTH_TIP_RECOMMENDATION:
                handleHealthTipRecommendation(intent);
                break;

            default:
                Log.w(TAG, "Unknown notification type: " + notificationType);
                finish();
        }
    }

    /**
     * Xử lý thông báo reply comment
     * Mở video và scroll đến comment được reply
     */
    private void handleCommentReplyNotification(Intent sourceIntent) {
        String videoId = sourceIntent.getStringExtra("video_id");
        String parentCommentId = sourceIntent.getStringExtra("parent_comment_id");
        String replyCommentId = sourceIntent.getStringExtra("reply_comment_id");

        if (videoId == null) {
            Log.w(TAG, "Missing video_id for comment reply");
            finish();
            return;
        }

        // Tạo Intent để mở SingleVideoPlayerActivity
        Intent videoIntent = new Intent(this, SingleVideoPlayerActivity.class);
        videoIntent.putExtra("video_id", videoId);
        videoIntent.putExtra("open_comments", true); // Flag để tự động mở comments
        videoIntent.putExtra("scroll_to_comment", parentCommentId); // Scroll đến comment
        videoIntent.putExtra("highlight_reply", replyCommentId); // Highlight reply mới
        videoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(videoIntent);
        finish();
    }

    /**
     * Xử lý thông báo bài viết sức khỏe mới
     */
    private void handleNewHealthTipNotification(Intent sourceIntent) {
        String healthTipId = sourceIntent.getStringExtra("health_tip_id");

        if (healthTipId == null) {
            Log.w(TAG, "Missing health_tip_id");
            finish();
            return;
        }

        // Tạo Intent để mở HealthTipDetailActivity
        Intent detailIntent = new Intent(this, HealthTipDetailActivity.class);
        detailIntent.putExtra("health_tip_id", healthTipId);
        detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(detailIntent);
        finish();
    }

    /**
     * Xử lý thông báo recommendations
     * Luôn mở bài viết đầu tiên (hoặc bài có score cao nhất)
     */
    private void handleHealthTipRecommendation(Intent sourceIntent) {
        String tipsJson = sourceIntent.getStringExtra("tips");

        Log.d(TAG, "handleHealthTipRecommendation called");
        Log.d(TAG, "tips from intent: " + tipsJson);

        if (tipsJson == null) {
            Log.w(TAG, "Missing tips data");
            finish();
            return;
        }

        try {
            // Parse JSON array của tips
            JSONArray tipsArray = new JSONArray(tipsJson);
            Log.d(TAG, "Parsed JSON array with " + tipsArray.length() + " tips");

            if (tipsArray.length() == 0) {
                Log.w(TAG, "Empty tips array");
                finish();
                return;
            }

            // Luôn mở bài đầu tiên (bài có score cao nhất từ recommendation engine)
            JSONObject firstTip = tipsArray.getJSONObject(0);
            String tipId = firstTip.getString("healthTipId");

            Log.d(TAG, "Opening detail for recommended tip: " + tipId);

            Intent detailIntent = new Intent(this, HealthTipDetailActivity.class);
            detailIntent.putExtra("health_tip_id", tipId);
            detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(detailIntent);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing tips JSON", e);
            Log.e(TAG, "JSON content was: " + tipsJson);
        }

        finish();
    }

    /**
     * Xử lý thông báo video mới
     */
    private void handleNewVideoNotification(Intent sourceIntent) {
        String videoId = sourceIntent.getStringExtra("video_id");

        if (videoId == null) {
            Log.w(TAG, "Missing video_id");
            finish();
            return;
        }

        // Tạo Intent để mở SingleVideoPlayerActivity
        Intent videoIntent = new Intent(this, SingleVideoPlayerActivity.class);
        videoIntent.putExtra("video_id", videoId);
        videoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(videoIntent);
        finish();
    }

    /**
     * Xử lý thông báo like comment
     */
    private void handleCommentLikeNotification(Intent sourceIntent) {
        String videoId = sourceIntent.getStringExtra("video_id");
        String commentId = sourceIntent.getStringExtra("comment_id");

        if (videoId == null || commentId == null) {
            Log.w(TAG, "Missing data for comment like notification");
            finish();
            return;
        }

        // Mở video và highlight comment được like
        Intent videoIntent = new Intent(this, SingleVideoPlayerActivity.class);
        videoIntent.putExtra("video_id", videoId);
        videoIntent.putExtra("open_comments", true);
        videoIntent.putExtra("scroll_to_comment", commentId);
        videoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(videoIntent);
        finish();
    }
}
