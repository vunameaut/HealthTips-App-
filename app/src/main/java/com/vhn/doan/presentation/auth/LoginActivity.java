package com.vhn.doan.presentation.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vhn.doan.MainActivity;
import com.vhn.doan.R;

/**
 * LoginActivity là màn hình đăng nhập của ứng dụng
 * Lớp này triển khai giao diện AuthView và sử dụng AuthPresenter
 * để xử lý logic đăng nhập theo mô hình MVP
 */
public class LoginActivity extends AppCompatActivity implements AuthView {

    // UI components
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewForgotPassword;
    private TextView textViewRegister;
    private ProgressDialog progressDialog;

    // Presenter
    private AuthPresenter authPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo AuthPresenter
        authPresenter = new AuthPresenter(this, this);

        // Kiểm tra người dùng đã đăng nhập chưa
        if (authPresenter.isUserLoggedIn()) {
            // Nếu đã đăng nhập thì chuyển đến màn hình chính
            navigateToMainActivity();
            return;
        }

        // Ánh xạ các thành phần UI
        initializeViews();

        // Thiết lập sự kiện cho các nút
        setupClickListeners();
    }

    /**
     * Khởi tạo các thành phần UI
     */
    private void initializeViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        textViewRegister = findViewById(R.id.textViewRegister);

        // Khởi tạo ProgressDialog để hiển thị trạng thái đang tải
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);
    }

    /**
     * Thiết lập sự kiện click cho các nút
     */
    private void setupClickListeners() {
        // Xử lý sự kiện đăng nhập
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy email và password từ EditText
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Gọi phương thức login từ presenter
                authPresenter.login(email, password);
            }
        });

        // Xử lý sự kiện chuyển đến màn hình quên mật khẩu
        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển đến màn hình quên mật khẩu, không cần nhận kết quả
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        // Xử lý sự kiện chuyển đến màn hình đăng ký
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển đến màn hình đăng ký, không cần nhận kết quả
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Chuyển đến màn hình chính
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Chuyển đến màn hình quên mật khẩu
     */
    private void navigateToForgotPasswordActivity() {
        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    /**
     * Chuyển đến màn hình đăng ký
     */
    private void navigateToRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    // Triển khai các phương thức của giao diện AuthView

    @Override
    public void showLoading(boolean loading) {
        if (loading) {
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }

    @Override
    public void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLoginSuccess(String userId) {
        showMessage("Đăng nhập thành công");
        navigateToMainActivity();
    }

    @Override
    public void onRegistrationSuccess(String userId) {
        // Không cần triển khai trong màn hình đăng nhập
    }

    @Override
    public void onLogoutSuccess() {
        // Không cần triển khai trong màn hình đăng nhập
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
