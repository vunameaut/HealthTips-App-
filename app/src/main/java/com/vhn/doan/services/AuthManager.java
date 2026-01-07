package com.vhn.doan.services;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
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
     * Đăng nhập bằng Google
     * @param account Google account từ Google Sign-In
     * @param callback Callback xử lý kết quả
     */
    public void signInWithGoogle(GoogleSignInAccount account, AuthCallback callback) {
        if (account == null) {
            Log.e("AuthManager", "Google account is null");
            callback.onResult(false, null, "Tài khoản Google không hợp lệ");
            return;
        }

        if (account.getIdToken() == null) {
            Log.e("AuthManager", "Google ID Token is null");
            callback.onResult(false, null, "Không thể lấy thông tin xác thực từ Google");
            return;
        }

        Log.d("AuthManager", "Starting Google Sign-In with account: " + account.getEmail());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("AuthManager", "Firebase authentication successful");
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            Log.d("AuthManager", "Firebase user found: " + firebaseUser.getUid());
                            // Kiểm tra xem user đã tồn tại trong database chưa
                            userRepository.getUserByUid(firebaseUser.getUid(), new UserRepository.UserCallback() {
                                @Override
                                public void onSuccess(User user) {
                                    Log.d("AuthManager", "Existing user found, updating last login");
                                    // User đã tồn tại, cập nhật thời gian đăng nhập
                                    userRepository.updateLastLogin(firebaseUser.getUid(), new UserRepository.UserOperationCallback() {
                                        @Override
                                        public void onSuccess() {
                                            Log.d("AuthManager", "Last login updated successfully");
                                            callback.onResult(true, firebaseUser.getUid(), null);
                                        }

                                        @Override
                                        public void onError(String errorMessage) {
                                            Log.w("AuthManager", "Failed to update last login: " + errorMessage);
                                            // Vẫn cho đăng nhập thành công
                                            callback.onResult(true, firebaseUser.getUid(), null);
                                        }
                                    });
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    Log.d("AuthManager", "New user, creating account");
                                    // User chưa tồn tại, tạo mới
                                    String displayName = firebaseUser.getDisplayName() != null ?
                                            firebaseUser.getDisplayName() : "User";
                                    String email = firebaseUser.getEmail() != null ?
                                            firebaseUser.getEmail() : "";

                                    User newUser = new User(firebaseUser.getUid(), email, displayName);

                                    // Lưu thông tin user mới
                                    userRepository.saveUser(newUser, new UserRepository.UserOperationCallback() {
                                        @Override
                                        public void onSuccess() {
                                            Log.d("AuthManager", "New user saved successfully");
                                            callback.onResult(true, firebaseUser.getUid(), null);
                                        }

                                        @Override
                                        public void onError(String errorMessage) {
                                            Log.e("AuthManager", "Failed to save new user: " + errorMessage);
                                            callback.onResult(false, null, "Đăng nhập thành công nhưng không thể lưu thông tin: " + errorMessage);
                                        }
                                    });
                                }
                            });
                        } else {
                            Log.e("AuthManager", "Firebase user is null after successful auth");
                            callback.onResult(false, null, "Đăng nhập thành công nhưng không tìm thấy thông tin người dùng");
                        }
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Đăng nhập Google thất bại";
                        Log.e("AuthManager", "Firebase authentication failed: " + errorMessage, task.getException());
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
        // Sử dụng fetchSignInMethodsForEmail để kiểm tra email có tồn tại không
        firebaseAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Kiểm tra danh sách phương thức đăng nhập
                        if (task.getResult() != null && task.getResult().getSignInMethods() != null
                                && !task.getResult().getSignInMethods().isEmpty()) {
                            // Email đã được đăng ký
                            callback.onResult(true, null, null);
                        } else {
                            // Email chưa được đăng ký
                            callback.onResult(false, null, "Email không tồn tại trong hệ thống");
                        }
                    } else {
                        // Có lỗi xảy ra
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Không thể kiểm tra email";
                        callback.onResult(false, null, errorMessage);
                    }
                });
    }

    /**
     * Gửi email đặt lại mật khẩu với kiểm tra chi tiết
     * @param email Email cần đặt lại mật khẩu
     * @param callback Callback xử lý kết quả
     */
    public void sendPasswordResetEmailWithVerification(String email, AuthCallback callback) {
        // Kiểm tra email có tồn tại không
        checkEmailExists(email, (isSuccess, userId, errorMessage) -> {
            if (isSuccess) {
                // Email tồn tại, tiến hành gửi email reset
                sendPasswordResetEmail(email, callback);
            } else {
                // Email không tồn tại hoặc có lỗi
                callback.onResult(false, null, errorMessage);
            }
        });
    }
}
