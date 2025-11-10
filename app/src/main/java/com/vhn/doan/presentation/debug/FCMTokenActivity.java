package com.vhn.doan.presentation.debug;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.vhn.doan.R;

/**
 * Activity để hiển thị và copy FCM Registration Token
 * Dùng để test push notifications
 */
public class FCMTokenActivity extends AppCompatActivity {

    private static final String TAG = "FCMTokenActivity";

    private TextView tvToken;
    private TextView tvStatus;
    private Button btnCopyToken;
    private Button btnRefreshToken;
    private ProgressBar progressBar;

    private String currentToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fcm_token);

        // Khởi tạo views
        initViews();

        // Lấy FCM token
        getFCMToken();
    }

    private void initViews() {
        tvToken = findViewById(R.id.tv_token);
        tvStatus = findViewById(R.id.tv_status);
        btnCopyToken = findViewById(R.id.btn_copy_token);
        btnRefreshToken = findViewById(R.id.btn_refresh_token);
        progressBar = findViewById(R.id.progress_bar);

        // Set up button listeners
        btnCopyToken.setOnClickListener(v -> copyTokenToClipboard());
        btnRefreshToken.setOnClickListener(v -> getFCMToken());

        // Disable copy button initially
        btnCopyToken.setEnabled(false);
    }

    /**
     * Lấy FCM Registration Token từ Firebase
     */
    private void getFCMToken() {
        showLoading(true);
        tvStatus.setText(R.string.fcm_status_loading);
        tvToken.setText("");
        btnCopyToken.setEnabled(false);

        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                showLoading(false);

                if (!task.isSuccessful()) {
                    Log.w(TAG, "Lấy FCM token thất bại", task.getException());
                    tvStatus.setText(R.string.fcm_status_error);
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));

                    if (task.getException() != null) {
                        tvToken.setText(getString(R.string.fcm_error_details, task.getException().getMessage()));
                    }
                    return;
                }

                // Lấy token thành công
                currentToken = task.getResult();
                Log.d(TAG, "FCM Token: " + currentToken);

                tvStatus.setText(R.string.fcm_status_success);
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
                tvToken.setText(currentToken);
                btnCopyToken.setEnabled(true);

                Toast.makeText(FCMTokenActivity.this,
                    R.string.fcm_toast_ready,
                    Toast.LENGTH_LONG).show();
            });
    }

    /**
     * Copy token vào clipboard
     */
    private void copyTokenToClipboard() {
        if (currentToken != null && !currentToken.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("FCM Token", currentToken);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this,
                R.string.fcm_toast_copied,
                Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.fcm_toast_no_token, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Hiển thị/ẩn loading indicator
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRefreshToken.setEnabled(!show);
    }
}

