package com.vhn.doan.presentation.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.R;
import com.vhn.doan.presentation.auth.LoginActivity;
import com.vhn.doan.presentation.base.BaseFragment;

/**
 * Fragment hiển thị thông tin profile người dùng
 * Tuân thủ kiến trúc MVP pattern với Firebase Authentication
 */
public class ProfileFragment extends BaseFragment implements ProfileContract.View {

    private ProfileContract.Presenter presenter;
    private TextView tvUserInfo;
    private MaterialButton btnLogout;
    private FirebaseAuth firebaseAuth;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        // TODO: Inject presenter through Dagger 2 trong tương lai
        // presenter = DaggerProfileComponent.create().getPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load thông tin người dùng ngay sau khi view được tạo
        loadUserProfile();

        // Attach presenter nếu có
        if (presenter != null) {
            presenter.attachView(this);
            presenter.loadUserProfile();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    @Override
    protected void initViews(View view) {
        // Khởi tạo các view components từ layout
        tvUserInfo = view.findViewById(R.id.tvUserInfo);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    @Override
    protected void setupListeners() {
        // Thiết lập listener cho nút đăng xuất
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());
        }
    }

    /**
     * Tải thông tin profile người dùng từ Firebase
     */
    private void loadUserProfile() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userInfo = buildUserInfoString(currentUser);
            displayUserProfile(userInfo);
        } else {
            displayUserProfile("Chưa đăng nhập");
        }
    }

    /**
     * Xây dựng chuỗi thông tin người dùng
     */
    private String buildUserInfoString(FirebaseUser user) {
        StringBuilder userInfo = new StringBuilder();

        // Tên người dùng
        String displayName = user.getDisplayName();
        userInfo.append("Tên: ").append(displayName != null ? displayName : "Chưa có tên").append("\n");

        // Email
        String email = user.getEmail();
        userInfo.append("Email: ").append(email != null ? email : "Chưa có email").append("\n");

        // UID (có thể hữu ích cho debug)
        userInfo.append("ID: ").append(user.getUid().substring(0, 8)).append("...");

        return userInfo.toString();
    }

    /**
     * Hiển thị dialog xác nhận đăng xuất
     */
    private void showLogoutConfirmDialog() {
        if (getContext() != null) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Xác nhận đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout())
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    /**
     * Thực hiện đăng xuất
     */
    private void performLogout() {
        try {
            showLoading(true);

            // Đăng xuất Firebase
            firebaseAuth.signOut();

            showLoading(false);
            showMessage("Đăng xuất thành công");

            // Gọi callback khi đăng xuất thành công
            onLogoutSuccess();

        } catch (Exception e) {
            showLoading(false);
            showError("Lỗi đăng xuất: " + e.getMessage());
        }
    }

    // Implementation của ProfileContract.View interface
    @Override
    public void displayUserProfile(String userInfo) {
        if (tvUserInfo != null) {
            tvUserInfo.setText(userInfo);
        }
    }

    @Override
    public void onLogoutSuccess() {
        // Chuyển về màn hình đăng nhập
        navigateToLogin();
    }

    /**
     * Chuyển hướng về màn hình đăng nhập
     */
    private void navigateToLogin() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void showLoading(boolean loading) {
        // Implement loading indicator với UI feedback
        if (btnLogout != null) {
            btnLogout.setEnabled(!loading);
            btnLogout.setText(loading ? "Đang đăng xuất..." : "Đăng xuất");
        }
    }

    // Convenience methods cho loading
    public void showLoading() {
        showLoading(true);
    }

    public void hideLoading() {
        showLoading(false);
    }

    // Setter cho Dependency Injection
    public void setPresenter(ProfileContract.Presenter presenter) {
        this.presenter = presenter;
    }

    /**
     * Refresh thông tin người dùng
     */
    public void refreshUserProfile() {
        loadUserProfile();
        if (presenter != null) {
            presenter.refreshUserProfile();
        }
    }
}
