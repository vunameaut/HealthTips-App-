package com.vhn.doan.presentation.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.vhn.doan.R;
import com.vhn.doan.presentation.base.BaseActivity;
import com.vhn.doan.presentation.home.HomeActivity;
import com.vhn.doan.utils.SessionManager;

/**
 * LoginActivity là màn hình đăng nhập của ứng dụng
 * Lớp này triển khai giao diện AuthView và sử dụng AuthPresenter
 * để xử lý logic đăng nhập theo mô hình MVP
 */
public class LoginActivity extends BaseActivity implements AuthView {

    private static final String TAG = "LoginActivity";

    // UI components
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private com.google.android.gms.common.SignInButton buttonGoogleSignIn;
    private TextView textViewForgotPassword;
    private TextView textViewRegister;
    private ProgressDialog progressDialog;

    // Presenter
    private AuthPresenter authPresenter;

    // Session Manager
    private SessionManager sessionManager;

    // Google Sign-In
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo SessionManager
        sessionManager = new SessionManager(this);

        // Khởi tạo AuthPresenter
        authPresenter = new AuthPresenter(this, this);

        // Khởi tạo Google Sign-In
        initGoogleSignIn();

        // Load existing session
        sessionManager.loadSession();

        // Kiểm tra người dùng đã đăng nhập chưa
        if (sessionManager.isUserLoggedIn() && sessionManager.autoCheckLoginState()) {
            // Check if account is pending deletion
            if (sessionManager.isPendingDeletion()) {
                // Show warning and continue to home
                Toast.makeText(this, "Tài khoản của bạn đang chờ xóa. Còn " +
                        sessionManager.getFormattedRemainingTime(), Toast.LENGTH_LONG).show();
            }
            // Nếu đã đăng nhập thì chuyển đến màn hình chính
            navigateToHomeActivity();
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
        buttonGoogleSignIn = findViewById(R.id.buttonGoogleSignIn);
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

        // Xử lý sự kiện đăng nhập bằng Google
        buttonGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
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
     * Chuyển đến màn hình chính (HomeActivity)
     */
    private void navigateToHomeActivity() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
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

    /**
     * Khởi tạo Google Sign-In
     */
    private void initGoogleSignIn() {
        try {
            // Cấu hình Google Sign-In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            googleSignInClient = GoogleSignIn.getClient(this, gso);

            Log.d(TAG, "Google Sign-In initialized successfully");

            // Khởi tạo ActivityResultLauncher cho Google Sign-In
            googleSignInLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Log.d(TAG, "Google Sign-In result received with code: " + result.getResultCode());
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                            handleGoogleSignInResult(task);
                        } else {
                            Log.w(TAG, "Google Sign-In was cancelled or failed with code: " + result.getResultCode());
                            showError("Đăng nhập Google đã bị hủy");
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Google Sign-In", e);
            showError("Lỗi khởi tạo Google Sign-In: " + e.getMessage());
        }
    }

    /**
     * Bắt đầu quá trình đăng nhập bằng Google
     */
    private void signInWithGoogle() {
        try {
            Log.d(TAG, "Starting Google Sign-In flow");
            // Đăng xuất tài khoản cũ trước khi đăng nhập mới (tùy chọn)
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error starting Google Sign-In", e);
            showError("Lỗi khởi động đăng nhập Google: " + e.getMessage());
        }
    }

    /**
     * Xử lý kết quả đăng nhập Google
     */
    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account == null) {
                Log.e(TAG, "Google account is null");
                showError("Không thể lấy thông tin tài khoản Google");
                return;
            }

            Log.d(TAG, "Google Sign-In successful for: " + account.getEmail());

            // Hiển thị loading
            showLoading(true);

            // Đăng nhập Firebase với Google account
            authPresenter.loginWithGoogle(account);

        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed with status code: " + e.getStatusCode(), e);
            String errorMessage = "Đăng nhập Google thất bại";

            // Cung cấp thông tin lỗi chi tiết hơn
            switch (e.getStatusCode()) {
                case 12501: // User cancelled
                    errorMessage = "Bạn đã hủy đăng nhập";
                    break;
                case 12500: // Sign in failed
                    errorMessage = "Đăng nhập thất bại. Vui lòng thử lại";
                    break;
                case 7: // Network error
                    errorMessage = "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối";
                    break;
                default:
                    errorMessage = "Lỗi đăng nhập Google (Code: " + e.getStatusCode() + ")";
            }

            showError(errorMessage);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during Google Sign-In", e);
            showError("Lỗi không xác định: " + e.getMessage());
        }
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
        // Save login state
        sessionManager.saveLoginState(userId);

        // Create new session
        sessionManager.createSession();

        showMessage("Đăng nhập thành công");
        navigateToHomeActivity();
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
