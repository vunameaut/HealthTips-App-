package com.vhn.doan.presentation.debug;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.vhn.doan.R;
import com.vhn.doan.utils.VercelApiHelper;

import org.json.JSONObject;

/**
 * Activity để test các loại notifications
 */
public class TestNotificationActivity extends AppCompatActivity {

    private TextView tvToken;
    private EditText etVideoId, etCommentId, etHealthTipId;
    private Button btnGetToken;
    private Button btnTestCommentReply;
    private Button btnTestNewHealthTip;
    private Button btnTestRecommendation;

    private VercelApiHelper vercelApiHelper;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_notification);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Test Notifications");
        }

        initViews();
        setupListeners();

        vercelApiHelper = VercelApiHelper.getInstance(this);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
            ? FirebaseAuth.getInstance().getCurrentUser().getUid()
            : "test_user";
    }

    private void initViews() {
        tvToken = findViewById(R.id.tv_fcm_token);
        etVideoId = findViewById(R.id.et_video_id);
        etCommentId = findViewById(R.id.et_comment_id);
        etHealthTipId = findViewById(R.id.et_health_tip_id);

        btnGetToken = findViewById(R.id.btn_get_fcm_token);
        btnTestCommentReply = findViewById(R.id.btn_test_comment_reply);
        btnTestNewHealthTip = findViewById(R.id.btn_test_new_health_tip);
        btnTestRecommendation = findViewById(R.id.btn_test_recommendation);
    }

    private void setupListeners() {
        btnGetToken.setOnClickListener(v -> getFCMToken());
        btnTestCommentReply.setOnClickListener(v -> testCommentReplyNotification());
        btnTestNewHealthTip.setOnClickListener(v -> testNewHealthTipNotification());
        btnTestRecommendation.setOnClickListener(v -> testRecommendationNotification());
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken()
            .addOnSuccessListener(token -> {
                tvToken.setText("FCM Token:\n" + token);
                Toast.makeText(this, "Token loaded! Copy để test", Toast.LENGTH_LONG).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi lấy token: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    /**
     * Test Comment Reply Notification
     */
    private void testCommentReplyNotification() {
        String videoId = etVideoId.getText().toString().trim();
        String commentId = etCommentId.getText().toString().trim();

        if (videoId.isEmpty()) {
            Toast.makeText(this, "Nhập Video ID", Toast.LENGTH_SHORT).show();
            return;
        }
        if (commentId.isEmpty()) {
            Toast.makeText(this, "Nhập Comment ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đang gửi test notification...", Toast.LENGTH_SHORT).show();

        vercelApiHelper.sendCommentReplyNotification(
            videoId,
            commentId,
            "reply_" + System.currentTimeMillis(),
            currentUserId,
            new VercelApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    runOnUiThread(() -> {
                        Toast.makeText(TestNotificationActivity.this,
                            "✅ Đã gửi Comment Reply notification!",
                            Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(TestNotificationActivity.this,
                            "❌ Lỗi: " + error,
                            Toast.LENGTH_LONG).show();
                    });
                }
            }
        );
    }

    /**
     * Test New Health Tip Notification
     */
    private void testNewHealthTipNotification() {
        String healthTipId = etHealthTipId.getText().toString().trim();

        if (healthTipId.isEmpty()) {
            Toast.makeText(this, "Nhập Health Tip ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đang gửi test notification...", Toast.LENGTH_SHORT).show();

        vercelApiHelper.sendNewHealthTipNotification(
            healthTipId,
            "Test: Mẹo sức khỏe mới",
            "Dinh dưỡng",
            currentUserId,
            new VercelApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    runOnUiThread(() -> {
                        Toast.makeText(TestNotificationActivity.this,
                            "✅ Đã gửi New Health Tip notification!",
                            Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(TestNotificationActivity.this,
                            "❌ Lỗi: " + error,
                            Toast.LENGTH_LONG).show();
                    });
                }
            }
        );
    }

    /**
     * Test Recommendation Notification
     */
    private void testRecommendationNotification() {
        String healthTipId = etHealthTipId.getText().toString().trim();

        if (healthTipId.isEmpty()) {
            Toast.makeText(this, "Nhập Health Tip ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đang thêm vào recommendation queue...", Toast.LENGTH_SHORT).show();

        vercelApiHelper.queueHealthTipRecommendation(
            healthTipId,
            "category_123",
            "Test: Bài viết đề xuất",
            new VercelApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    runOnUiThread(() -> {
                        Toast.makeText(TestNotificationActivity.this,
                            "✅ Đã thêm vào recommendation queue!",
                            Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(TestNotificationActivity.this,
                            "❌ Lỗi: " + error,
                            Toast.LENGTH_LONG).show();
                    });
                }
            }
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
