package com.vhn.doan.presentation.auth;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vhn.doan.R;
import com.vhn.doan.services.AuthManager;
import com.vhn.doan.services.AuthManager.AuthCallback;

/**
 * ForgotPasswordActivity xử lý chức năng quên mật khẩu
 * Cho phép người dùng gửi yêu cầu đặt lại mật khẩu qua email
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private Button buttonResetPassword;
    private ImageButton buttonBack;
    private ProgressDialog progressDialog;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Khởi tạo AuthManager
        authManager = new AuthManager(this);

        // Khởi tạo các thành phần UI
        initializeViews();

        // Thiết lập sự kiện cho các nút
        setupClickListeners();
    }

    /**
     * Khởi tạo các thành phần UI
     */
    private void initializeViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);
        buttonBack = findViewById(R.id.buttonBack);

        // Khởi tạo ProgressDialog để hiển thị trạng thái đang tải
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);
    }

    /**
     * Thiết lập sự kiện cho các nút
     */
    private void setupClickListeners() {
        // Xử lý sự kiện gửi yêu cầu đặt lại mật khẩu
        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        // Xử lý sự kiện nút quay lại
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Khi nhấn nút back (từ phần cứng hoặc nút back tự tạo), quay về màn hình trước đó
        finish();
    }

    /**
     * Gửi yêu cầu đặt lại mật khẩu
     */
    private void resetPassword() {
        String email = editTextEmail.getText().toString().trim();

        // Kiểm tra email hợp lệ
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Vui lòng nhập email");
            editTextEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Vui lòng nhập email hợp lệ");
            editTextEmail.requestFocus();
            return;
        }

        // Hiển thị ProgressDialog
        progressDialog.show();

        // Gửi email đặt lại mật khẩu với kiểm tra email tồn tại
        authManager.sendPasswordResetEmailWithVerification(email, new AuthCallback() {
            @Override
            public void onResult(boolean isSuccess, String userId, String errorMessage) {
                // Ẩn ProgressDialog
                progressDialog.dismiss();

                if (isSuccess) {
                    // Hiển thị thông báo thành công
                    Toast.makeText(ForgotPasswordActivity.this,
                        "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư của bạn.",
                        Toast.LENGTH_LONG).show();

                    // Quay lại màn hình đăng nhập sau khi gửi email thành công
                    finish();
                } else {
                    // Xử lý các trường hợp lỗi khác nhau
                    if (errorMessage != null && errorMessage.contains("không tồn tại")) {
                        // Email không tồn tại trong hệ thống
                        Toast.makeText(ForgotPasswordActivity.this,
                            "Email này chưa đăng ký tài khoản. Vui lòng kiểm tra lại.",
                            Toast.LENGTH_LONG).show();
                    } else if (errorMessage != null && errorMessage.contains("blocked")) {
                        // Firebase chặn gửi nhiều yêu cầu trong thời gian ngắn
                        Toast.makeText(ForgotPasswordActivity.this,
                            "Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau.",
                            Toast.LENGTH_LONG).show();
                    } else if (errorMessage != null && errorMessage.contains("network")) {
                        // Lỗi mạng
                        Toast.makeText(ForgotPasswordActivity.this,
                            "Không thể kết nối tới máy chủ. Vui lòng kiểm tra kết nối mạng và thử lại.",
                            Toast.LENGTH_LONG).show();
                    } else {
                        // Các lỗi khác
                        Toast.makeText(ForgotPasswordActivity.this,
                            "Lỗi: " + (errorMessage != null ? errorMessage : "Không thể gửi email đặt lại mật khẩu"),
                            Toast.LENGTH_LONG).show();
                    }

                    // Log lỗi để debug
                    System.out.println("Lỗi đặt lại mật khẩu: " + errorMessage);
                }
            }
        });
    }
}
