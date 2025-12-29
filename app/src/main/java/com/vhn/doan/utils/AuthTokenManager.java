package com.vhn.doan.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseError;
import com.vhn.doan.presentation.auth.LoginActivity;

/**
 * AuthTokenManager - Quản lý token xác thực và xử lý các trường hợp token bị invalidate
 * Giải quyết vấn đề: Khi đổi mật khẩu ở nền tảng khác, token cũ sẽ không hợp lệ
 */
public class AuthTokenManager {

    private static final String TAG = "AuthTokenManager";
    private static FirebaseAuth firebaseAuth;

    // Callback interface để thông báo kết quả refresh token
    public interface TokenRefreshCallback {
        void onTokenRefreshed();
        void onTokenRefreshFailed();
    }

    /**
     * Khởi tạo FirebaseAuth instance
     */
    private static FirebaseAuth getFirebaseAuth() {
        if (firebaseAuth == null) {
            firebaseAuth = FirebaseAuth.getInstance();
        }
        return firebaseAuth;
    }

    /**
     * Kiểm tra xem lỗi có phải là lỗi PERMISSION_DENIED hay không
     * @param databaseError DatabaseError từ Firebase
     * @return true nếu là lỗi permission
     */
    public static boolean isPermissionDeniedError(DatabaseError databaseError) {
        if (databaseError == null) return false;

        return databaseError.getCode() == DatabaseError.PERMISSION_DENIED ||
               databaseError.getMessage().toLowerCase().contains("permission") ||
               databaseError.getMessage().toLowerCase().contains("unauthorized");
    }

    /**
     * Refresh token của người dùng hiện tại
     * @param callback Callback để thông báo kết quả
     */
    public static void refreshUserToken(final TokenRefreshCallback callback) {
        FirebaseUser currentUser = getFirebaseAuth().getCurrentUser();

        if (currentUser == null) {
            Log.w(TAG, "Không thể refresh token: Người dùng chưa đăng nhập");
            if (callback != null) {
                callback.onTokenRefreshFailed();
            }
            return;
        }

        Log.d(TAG, "Đang refresh token cho user: " + currentUser.getUid());

        // Force refresh token
        currentUser.getIdToken(true)
            .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult().getToken();
                        Log.d(TAG, "Token refresh thành công");
                        if (callback != null) {
                            callback.onTokenRefreshed();
                        }
                    } else {
                        Log.e(TAG, "Token refresh thất bại", task.getException());
                        if (callback != null) {
                            callback.onTokenRefreshFailed();
                        }
                    }
                }
            });
    }

    /**
     * Reload thông tin người dùng hiện tại từ Firebase
     * @param callback Callback để thông báo kết quả
     */
    public static void reloadCurrentUser(final TokenRefreshCallback callback) {
        FirebaseUser currentUser = getFirebaseAuth().getCurrentUser();

        if (currentUser == null) {
            Log.w(TAG, "Không thể reload user: Người dùng chưa đăng nhập");
            if (callback != null) {
                callback.onTokenRefreshFailed();
            }
            return;
        }

        Log.d(TAG, "Đang reload thông tin user: " + currentUser.getUid());

        currentUser.reload()
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User reload thành công");
                        // Sau khi reload, refresh token
                        refreshUserToken(callback);
                    } else {
                        Log.e(TAG, "User reload thất bại", task.getException());
                        if (callback != null) {
                            callback.onTokenRefreshFailed();
                        }
                    }
                }
            });
    }

    /**
     * Xử lý lỗi PERMISSION_DENIED bằng cách thử refresh token
     * Nếu không thành công, logout user và chuyển về màn hình đăng nhập
     * @param context Context của Activity/Fragment
     * @param databaseError DatabaseError từ Firebase
     */
    public static void handlePermissionDeniedError(final Context context, DatabaseError databaseError) {
        if (!isPermissionDeniedError(databaseError)) {
            return;
        }

        Log.w(TAG, "Phát hiện lỗi PERMISSION_DENIED. Đang thử refresh token...");

        reloadCurrentUser(new TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed() {
                Log.d(TAG, "Token đã được refresh thành công. Vui lòng thử lại.");
                if (context != null) {
                    Toast.makeText(context,
                        "Phiên đăng nhập đã được làm mới. Vui lòng thử lại.",
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTokenRefreshFailed() {
                Log.e(TAG, "Không thể refresh token. Đăng xuất user...");
                forceLogoutAndRedirectToLogin(context,
                    "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
            }
        });
    }

    /**
     * Buộc đăng xuất user và chuyển về màn hình đăng nhập
     * @param context Context của Activity/Fragment
     * @param message Thông báo hiển thị cho user
     */
    public static void forceLogoutAndRedirectToLogin(Context context, String message) {
        if (context == null) return;

        Log.w(TAG, "Buộc đăng xuất user và chuyển về màn hình đăng nhập");

        // Đăng xuất khỏi Firebase
        getFirebaseAuth().signOut();

        // Xóa session local
        SessionManager sessionManager = new SessionManager(context);
        sessionManager.clearSession();

        // Hiển thị thông báo
        if (message != null && !message.isEmpty()) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }

        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        // Finish activity hiện tại nếu là Activity
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    /**
     * Kiểm tra và verify token hiện tại
     * @param callback Callback để thông báo kết quả
     */
    public static void verifyCurrentToken(final TokenRefreshCallback callback) {
        FirebaseUser currentUser = getFirebaseAuth().getCurrentUser();

        if (currentUser == null) {
            Log.w(TAG, "Không có user để verify token");
            if (callback != null) {
                callback.onTokenRefreshFailed();
            }
            return;
        }

        currentUser.getIdToken(false)
            .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "Token hiện tại vẫn hợp lệ");
                        if (callback != null) {
                            callback.onTokenRefreshed();
                        }
                    } else {
                        Log.w(TAG, "Token hiện tại không hợp lệ, thử refresh...");
                        refreshUserToken(callback);
                    }
                }
            });
    }

    /**
     * Thêm AuthStateListener để theo dõi trạng thái đăng nhập
     * Nếu user bị logout từ nơi khác, sẽ tự động chuyển về màn hình đăng nhập
     * @param context Context của Activity
     * @return AuthStateListener đã được thêm (cần lưu để remove sau này)
     */
    public static FirebaseAuth.AuthStateListener addAuthStateListener(final Context context) {
        final FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Log.w(TAG, "User đã bị logout. Chuyển về màn hình đăng nhập.");

                    // Chỉ redirect nếu không phải là LoginActivity
                    if (context != null && !(context instanceof LoginActivity)) {
                        forceLogoutAndRedirectToLogin(context,
                            "Phiên đăng nhập đã kết thúc. Vui lòng đăng nhập lại.");
                    }
                } else {
                    Log.d(TAG, "User đang đăng nhập: " + user.getUid());
                }
            }
        };

        getFirebaseAuth().addAuthStateListener(authStateListener);
        return authStateListener;
    }

    /**
     * Remove AuthStateListener
     * @param authStateListener Listener cần remove
     */
    public static void removeAuthStateListener(FirebaseAuth.AuthStateListener authStateListener) {
        if (authStateListener != null) {
            getFirebaseAuth().removeAuthStateListener(authStateListener);
        }
    }
}

