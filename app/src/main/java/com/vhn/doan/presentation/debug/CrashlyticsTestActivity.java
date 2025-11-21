package com.vhn.doan.presentation.debug;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.vhn.doan.R;

/**
 * Activity để test Firebase Crashlytics
 * Màn hình này giúp kiểm tra xem Crashlytics có hoạt động đúng không
 */
public class CrashlyticsTestActivity extends AppCompatActivity {

    private FirebaseCrashlytics crashlytics;
    private Button btnTestCrash;
    private Button btnTestNonFatalError;
    private Button btnTestCustomLog;
    private Button btnTestUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crashlytics_test);

        // Khởi tạo Crashlytics
        crashlytics = FirebaseCrashlytics.getInstance();

        // Thiết lập toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Test Crashlytics");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Khởi tạo views
        initViews();

        // Thiết lập các sự kiện click
        setupClickListeners();
    }

    private void initViews() {
        btnTestCrash = findViewById(R.id.btnTestCrash);
        btnTestNonFatalError = findViewById(R.id.btnTestNonFatalError);
        btnTestCustomLog = findViewById(R.id.btnTestCustomLog);
        btnTestUserInfo = findViewById(R.id.btnTestUserInfo);
    }

    private void setupClickListeners() {
        // Test 1: Gây crash để test Crashlytics
        btnTestCrash.setOnClickListener(v -> {
            Toast.makeText(this, "App sẽ crash sau 2 giây...", Toast.LENGTH_SHORT).show();

            // Delay 2 giây để người dùng thấy Toast
            v.postDelayed(() -> {
                throw new RuntimeException("Test Crash từ Crashlytics Test Activity");
            }, 2000);
        });

        // Test 2: Ghi lại lỗi không gây crash (Non-fatal error)
        btnTestNonFatalError.setOnClickListener(v -> {
            try {
                // Tạo một exception giả lập
                throw new Exception("Test Non-Fatal Error - Lỗi này không làm crash app");
            } catch (Exception e) {
                // Ghi lại exception vào Crashlytics
                crashlytics.recordException(e);
                Toast.makeText(this, "Đã ghi lại Non-Fatal Error vào Crashlytics", Toast.LENGTH_LONG).show();
            }
        });

        // Test 3: Ghi custom log
        btnTestCustomLog.setOnClickListener(v -> {
            // Ghi các custom log keys
            crashlytics.log("Custom log: Người dùng nhấn nút Test Custom Log");
            crashlytics.setCustomKey("button_clicked", "test_custom_log");
            crashlytics.setCustomKey("timestamp", System.currentTimeMillis());
            crashlytics.setCustomKey("screen_name", "CrashlyticsTestActivity");

            Toast.makeText(this, "Đã ghi Custom Log vào Crashlytics", Toast.LENGTH_LONG).show();
        });

        // Test 4: Thiết lập thông tin user
        btnTestUserInfo.setOnClickListener(v -> {
            // Thiết lập User ID (trong thực tế, đây sẽ là Firebase User ID)
            crashlytics.setUserId("test_user_123");

            // Thiết lập các custom keys cho user
            crashlytics.setCustomKey("user_type", "test_user");
            crashlytics.setCustomKey("app_version", "1.0");
            crashlytics.setCustomKey("device_info", android.os.Build.MODEL);

            Toast.makeText(this, "Đã thiết lập thông tin User vào Crashlytics", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

