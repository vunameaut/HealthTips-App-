package com.vhn.doan.presentation.settings.account;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.vhn.doan.R;

/**
 * Activity đổi mật khẩu
 */
public class ChangePasswordActivity extends AppCompatActivity implements ChangePasswordContract.View {

    private MaterialToolbar toolbar;
    private TextInputLayout tilCurrentPassword;
    private TextInputLayout tilNewPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etCurrentPassword;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnChangePassword;
    private ProgressBar progressBar;

    private ChangePasswordPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        initViews();
        setupPresenter();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tilCurrentPassword = findViewById(R.id.tilCurrentPassword);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        progressBar = findViewById(R.id.progressBar);

        // Setup toolbar
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupPresenter() {
        presenter = new ChangePasswordPresenter();
        presenter.attachView(this);
    }

    private void setupListeners() {
        btnChangePassword.setOnClickListener(v -> {
            clearErrors();
            String currentPassword = etCurrentPassword.getText() != null
                    ? etCurrentPassword.getText().toString().trim()
                    : "";
            String newPassword = etNewPassword.getText() != null
                    ? etNewPassword.getText().toString().trim()
                    : "";
            String confirmPassword = etConfirmPassword.getText() != null
                    ? etConfirmPassword.getText().toString().trim()
                    : "";

            presenter.validatePasswords(currentPassword, newPassword, confirmPassword);
        });
    }

    private void clearErrors() {
        tilCurrentPassword.setError(null);
        tilNewPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    @Override
    public void showPasswordChangedSuccess() {
        Toast.makeText(this, "Đã đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showPasswordChangeError(String error) {
        Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showInvalidCurrentPassword() {
        tilCurrentPassword.setError("Mật khẩu hiện tại không đúng");
        etCurrentPassword.requestFocus();
    }

    @Override
    public void showWeakNewPassword() {
        tilNewPassword.setError("Mật khẩu mới phải có ít nhất 6 ký tự");
        etNewPassword.requestFocus();
    }

    @Override
    public void showPasswordMismatch() {
        tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
        etConfirmPassword.requestFocus();
    }

    @Override
    public void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnChangePassword.setEnabled(!isLoading);
        etCurrentPassword.setEnabled(!isLoading);
        etNewPassword.setEnabled(!isLoading);
        etConfirmPassword.setEnabled(!isLoading);
    }

    @Override
    public void navigateBack() {
        finish();
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.detachView();
        }
        super.onDestroy();
    }
}
