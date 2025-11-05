package com.vhn.doan.presentation.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.vhn.doan.R;
import com.vhn.doan.presentation.base.BaseActivity;
import com.vhn.doan.presentation.home.HomeFragment;

/**
 * RegisterActivity là màn hình đăng ký của ứng dụng
 * Lớp này triển khai giao diện AuthView và sử dụng AuthPresenter
 * để xử lý logic đăng ký theo mô hình MVP
 */
public class RegisterActivity extends BaseActivity implements AuthView {

    // UI components
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonRegister;
    private ImageButton buttonBack;
    private ProgressDialog progressDialog;

    // Presenter
    private AuthPresenter authPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonBack = findViewById(R.id.buttonBack);

        // Khởi tạo ProgressDialog để hiển thị trạng thái đang tải
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);
    }

    /**
     * Thiết lập sự kiện click cho các nút
     */
    private void setupClickListeners() {
        // Xử lý sự kiện đăng ký
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy email, password và confirmPassword từ EditText
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String confirmPassword = editTextConfirmPassword.getText().toString().trim();

                // Gọi phương thức register từ presenter
                authPresenter.register(email, password, confirmPassword);
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
        super.onBackPressed();
        finish();
    }

    /**
     * Chuyển đến màn hình chính
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, HomeFragment.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
        // Không cần triển khai trong màn hình đăng ký
    }

    @Override
    public void onRegistrationSuccess(String userId) {
        showMessage("Đăng ký thành công");
        navigateToMainActivity();
    }

    @Override
    public void onLogoutSuccess() {
        // Không cần triển khai trong màn hình đăng ký
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
