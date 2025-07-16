package com.vhn.doan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.vhn.doan.presentation.auth.LoginActivity;
import com.vhn.doan.services.AuthManager;

public class MainActivity extends AppCompatActivity {

    private Button buttonLogout;
    private TextView textViewWelcome;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo AuthManager
        authManager = new AuthManager(this);

        // Kiểm tra người dùng đã đăng nhập chưa
        if (!authManager.isUserLoggedIn()) {
            navigateToLogin();
            return;
        }

        // Khởi tạo các thành phần UI
        initializeViews();
    }

    /**
     * Khởi tạo các thành phần UI và thiết lập sự kiện
     */
    private void initializeViews() {
        buttonLogout = findViewById(R.id.buttonLogout);
        textViewWelcome = findViewById(R.id.textViewWelcome);

        // Hiển thị email người dùng
        String userEmail = authManager.getCurrentUserEmail();
        if (userEmail != null) {
            textViewWelcome.setText("Chào mừng, " + userEmail);
        }

        // Xử lý sự kiện đăng xuất
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    /**
     * Thực hiện đăng xuất người dùng
     */
    private void logout() {
        // Đăng xuất khỏi Firebase
        authManager.logout();

        // Hiển thị thông báo
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();

        // Chuyển về màn hình đăng nhập
        navigateToLogin();
    }

    /**
     * Chuyển đến màn hình đăng nhập
     */
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}