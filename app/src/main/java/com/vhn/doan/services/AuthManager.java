package com.vhn.doan.services;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.data.User;
import com.vhn.doan.data.repository.UserRepository;
import com.vhn.doan.data.repository.UserRepositoryImpl;

/**
 * AuthManager là lớp quản lý xác thực người dùng sử dụng FirebaseAuth
 * Cung cấp các phương thức để đăng nhập, đăng ký, đăng xuất và kiểm tra trạng thái đăng nhập
 * Đã được cập nhật để lưu thông tin người dùng vào Firebase Realtime Database
 */
public class AuthManager {

    // Interface để callback kết quả xác thực
    public interface AuthCallback {
        void onResult(boolean isSuccess, String userId, String errorMessage);
    }

    private final Context context;
    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;

    /**
     * Constructor của AuthManager
     * @param context Context ứng dụng
     */
    public AuthManager(Context context) {
        this.context = context;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.userRepository = new UserRepositoryImpl();
    }

    /**
     * Đăng nhập bằng email và mật khẩu
     * @param email Email người dùng
     * @param password Mật khẩu người dùng
     * @param callback Callback xử lý kết quả
     */
    public void loginWithEmailPassword(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Cập nhật thời gian đăng nhập cuối
                            userRepository.updateLastLogin(firebaseUser.getUid(), new UserRepository.UserOperationCallback() {
                                @Override
                                public void onSuccess() {
                                    callback.onResult(true, firebaseUser.getUid(), null);
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    // Vẫn cho đăng nhập thành công dù không cập nhật được thời gian
                                    callback.onResult(true, firebaseUser.getUid(), null);
                                }
                            });
                        } else {
                            callback.onResult(false, null, "Đăng nhập thành công nhưng không tìm thấy thông tin người dùng");
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Đăng nhập thất bại";
                        callback.onResult(false, null, errorMessage);
                    }
                });
    }

    /**
     * Đăng ký tài khoản mới bằng email và mật khẩu
     * @param email Email người dùng
     * @param password Mật khẩu người dùng
     * @param displayName Tên hiển thị của người dùng
     * @param callback Callback xử lý kết quả
     */
    public void registerWithEmailPassword(String email, String password, String displayName, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Tạo đối tượng User để lưu vào Firebase Realtime Database
                            User user = new User(firebaseUser.getUid(), email, displayName);

                            // Lưu thông tin người dùng vào Firebase Realtime Database
                            userRepository.saveUser(user, new UserRepository.UserOperationCallback() {
                                @Override
                                public void onSuccess() {
                                    callback.onResult(true, firebaseUser.getUid(), null);
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    // Đăng ký Firebase Auth thành công nhưng lưu thông tin thất bại
                                    callback.onResult(false, null, "Đăng ký thành công nhưng không thể lưu thông tin người dùng: " + errorMessage);
                                }
                            });
                        } else {
                            callback.onResult(false, null, "Đăng ký thành công nhưng không tìm thấy thông tin người dùng");
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Đăng ký thất bại";
                        callback.onResult(false, null, errorMessage);
                    }
                });
    }

    /**
     * Đăng xuất người dùng hiện tại
     */
    public void logout() {
        firebaseAuth.signOut();
    }

    /**
     * Kiểm tra người dùng đã đăng nhập hay chưa
     * @return true nếu đã đăng nhập, false nếu chưa
     */
    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Lấy ID của người dùng hiện tại
     * @return ID người dùng hoặc null nếu chưa đăng nhập
     */
    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Lấy email của người dùng hiện tại
     * @return Email người dùng hoặc null nếu chưa đăng nhập
     */
    public String getCurrentUserEmail() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    /**
     * Gửi email đặt lại mật khẩu
     * @param email Email cần đặt lại mật khẩu
     * @param callback Callback xử lý kết quả
     */
    public void sendPasswordResetEmail(String email, AuthCallback callback) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResult(true, null, null);
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Gửi email đặt lại mật khẩu thất bại";
                        callback.onResult(false, null, errorMessage);
                    }
                });
    }

    /**
     * Kiểm tra email có tồn tại trong hệ thống không
     * @param email Email cần kiểm tra
     * @param callback Callback xử lý kết quả
     */
    public void checkEmailExists(String email, AuthCallback callback) {
        // Thêm log để debug
        System.out.println("Bắt đầu kiểm tra email: " + email);

        // Phương pháp đáng tin cậy hơn là trực tiếp gửi yêu cầu reset mật khẩu
        // Firebase sẽ tự động kiểm tra và trả về lỗi nếu email không tồn tại
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Nếu Firebase chấp nhận yêu cầu, email chắc chắn đã được đăng ký
                        System.out.println("Email tồn tại (gửi reset thành công): " + email);
                        callback.onResult(true, null, null);
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Gửi email đặt lại mật khẩu thất bại";

                        // Kiểm tra nếu lỗi là "không tìm thấy người dùng"
                        if (errorMessage != null &&
                           (errorMessage.contains("no user record") ||
                            errorMessage.contains("không tìm thấy người dùng") ||
                            errorMessage.contains("user-not-found"))) {
                            System.out.println("Email không tồn tại: " + email);
                            callback.onResult(false, null, "Email không tồn tại trong hệ thống");
                        } else {
                            // Lỗi khác không liên quan đến việc email không tồn tại
                            System.out.println("Lỗi kiểm tra email: " + errorMessage);
                            // Gọi callback với thất bại nhưng giữ nguyên thông báo lỗi gốc
                            callback.onResult(false, null, errorMessage);
                        }
                    }
                });
    }

    /**
     * Gửi email đặt lại mật khẩu với kiểm tra chi tiết
     * @param email Email cần đặt lại mật khẩu
     * @param callback Callback xử lý kết quả
     */
    public void sendPasswordResetEmailWithVerification(String email, AuthCallback callback) {
        // Sử dụng phương thức kiểm tra email đã được cải tiến
        checkEmailExists(email, (isSuccess, userId, errorMessage) -> {
            // Không cần gửi lại email nếu đã gửi trong quá trình checkEmailExists
            if (isSuccess) {
                // Email tồn tại và yêu cầu đặt lại mật khẩu đã được gửi
                callback.onResult(true, null, null);
            } else {
                // Email không tồn tại hoặc có lỗi khác
                callback.onResult(false, null, errorMessage);
            }
        });
    }
}
