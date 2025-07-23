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
 * Fragment đơn giản cho test chức năng đăng xuất
 */
public class SimpleProfileFragment extends BaseFragment {

    private TextView tvUserInfo;
    private MaterialButton btnLogout;
    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFirebase();
        loadUserInfo();
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void initViews(View view) {
        tvUserInfo = view.findViewById(R.id.tvUserInfo);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    @Override
    protected void setupListeners() {
        btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());
    }

    private void loadUserInfo() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userInfo = "Tên: " + (currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Chưa có tên") +
                            "\nEmail: " + currentUser.getEmail();
            tvUserInfo.setText(userInfo);
        } else {
            tvUserInfo.setText("Chưa đăng nhập");
        }
    }

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

    private void performLogout() {
        try {
            // Hiển thị loading
            showLoading(true);

            // Đăng xuất Firebase
            firebaseAuth.signOut();

            // Ẩn loading
            showLoading(false);

            // Hiển thị thông báo thành công
            if (getContext() != null) {
                Toast.makeText(getContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
            }

            // Chuyển về màn hình đăng nhập
            navigateToLogin();

        } catch (Exception e) {
            showLoading(false);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi đăng xuất: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

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
}
