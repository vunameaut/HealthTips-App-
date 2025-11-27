package com.vhn.doan.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.data.User;
import com.vhn.doan.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Triển khai UserRepository sử dụng Firebase Realtime Database
 * Lưu thông tin người dùng theo cấu trúc users/{uid}/
 */
public class UserRepositoryImpl implements UserRepository {

    private final FirebaseDatabase database;
    private final DatabaseReference usersRef;

    /**
     * Constructor mặc định
     */
    public UserRepositoryImpl() {
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference(Constants.USERS_REF);
    }

    @Override
    public void saveUser(User user, UserOperationCallback callback) {
        if (user == null || user.getUid() == null || user.getUid().isEmpty()) {
            callback.onError("Thông tin người dùng không hợp lệ");
            return;
        }

        usersRef.child(user.getUid()).setValue(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Lỗi khi lưu thông tin người dùng: " + e.getMessage()));
    }

    @Override
    public void getUserByUid(String uid, UserCallback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onError("UID không hợp lệ");
            return;
        }

        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    callback.onSuccess(user);
                } else {
                    callback.onError("Không tìm thấy thông tin người dùng");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Lỗi khi tải thông tin người dùng: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void updateUser(User user, UserOperationCallback callback) {
        if (user == null || user.getUid() == null || user.getUid().isEmpty()) {
            callback.onError("Thông tin người dùng không hợp lệ");
            return;
        }

        usersRef.child(user.getUid()).setValue(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Lỗi khi cập nhật thông tin người dùng: " + e.getMessage()));
    }

    @Override
    public void updateLastLogin(String uid, UserOperationCallback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onError("UID không hợp lệ");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("lastLoginAt", System.currentTimeMillis());

        usersRef.child(uid).updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Lỗi khi cập nhật thời gian đăng nhập: " + e.getMessage()));
    }

    @Override
    public void addFavoriteHealthTip(String uid, String healthTipId, UserOperationCallback callback) {
        if (uid == null || uid.isEmpty() || healthTipId == null || healthTipId.isEmpty()) {
            callback.onError("Thông tin không hợp lệ");
            return;
        }

        usersRef.child(uid).child("favoriteHealthTips").child(healthTipId).setValue(true)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Lỗi khi thêm vào danh sách yêu thích: " + e.getMessage()));
    }

    @Override
    public void removeFavoriteHealthTip(String uid, String healthTipId, UserOperationCallback callback) {
        if (uid == null || uid.isEmpty() || healthTipId == null || healthTipId.isEmpty()) {
            callback.onError("Thông tin không hợp lệ");
            return;
        }

        usersRef.child(uid).child("favoriteHealthTips").child(healthTipId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Lỗi khi xóa khỏi danh sách yêu thích: " + e.getMessage()));
    }

    @Override
    public void addLikedHealthTip(String uid, String healthTipId, UserOperationCallback callback) {
        if (uid == null || uid.isEmpty() || healthTipId == null || healthTipId.isEmpty()) {
            callback.onError("Thông tin không hợp lệ");
            return;
        }

        usersRef.child(uid).child("likedHealthTips").child(healthTipId).setValue(true)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Lỗi khi thêm vào danh sách đã thích: " + e.getMessage()));
    }

    @Override
    public void removeLikedHealthTip(String uid, String healthTipId, UserOperationCallback callback) {
        if (uid == null || uid.isEmpty() || healthTipId == null || healthTipId.isEmpty()) {
            callback.onError("Thông tin không hợp lệ");
            return;
        }

        usersRef.child(uid).child("likedHealthTips").child(healthTipId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Lỗi khi xóa khỏi danh sách đã thích: " + e.getMessage()));
    }

    @Override
    public void updateUserPreference(String uid, String key, Object value, UserOperationCallback callback) {
        if (uid == null || uid.isEmpty() || key == null || key.isEmpty()) {
            callback.onError("Thông tin không hợp lệ");
            return;
        }

        usersRef.child(uid).child("preferences").child(key).setValue(value)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Lỗi khi cập nhật tùy chọn: " + e.getMessage()));
    }

    /**
     * Cập nhật ảnh avatar của người dùng
     * @param uid UID của người dùng
     * @param photoUrl URL của avatar mới
     * @param callback callback để nhận kết quả
     */
    @Override
    public void updateUserAvatar(String uid, String photoUrl, UserOperationCallback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onError("UID không hợp lệ");
            return;
        }

        if (photoUrl == null || photoUrl.isEmpty()) {
            callback.onError("URL ảnh không hợp lệ");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("photoUrl", photoUrl);

        usersRef.child(uid).updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Lỗi khi cập nhật ảnh đại diện: " + e.getMessage()));
    }

    /**
     * Cập nhật thông tin cá nhân của người dùng
     * ✅ UPDATED: Cũng cập nhật Firebase Authentication displayName
     * @param uid UID của người dùng
     * @param displayName Tên hiển thị mới
     * @param bio Giới thiệu bản thân mới
     * @param phoneNumber Số điện thoại mới
     * @param callback callback để nhận kết quả
     */
    @Override
    public void updateUserProfile(String uid, String displayName, String bio, String phoneNumber, UserOperationCallback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onError("UID không hợp lệ");
            return;
        }

        Map<String, Object> updates = new HashMap<>();

        if (displayName != null && !displayName.isEmpty()) {
            updates.put("displayName", displayName);
        }

        if (bio != null) {
            updates.put("bio", bio);
        }

        if (phoneNumber != null) {
            updates.put("phoneNumber", phoneNumber);
        }

        if (updates.isEmpty()) {
            callback.onError("Không có thông tin nào cần cập nhật");
            return;
        }

        // ✅ Step 1: Update Firebase Database
        usersRef.child(uid).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    // ✅ Step 2: Update Firebase Authentication displayName nếu có
                    if (displayName != null && !displayName.isEmpty()) {
                        updateFirebaseAuthDisplayName(displayName, callback);
                    } else {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> callback.onError("Lỗi khi cập nhật thông tin cá nhân: " + e.getMessage()));
    }

    /**
     * ✅ NEW: Update Firebase Authentication displayName
     */
    private void updateFirebaseAuthDisplayName(String displayName, UserOperationCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();

            currentUser.updateProfile(profileUpdates)
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> {
                        // Database đã update thành công, chỉ Auth update thất bại
                        // Vẫn coi là thành công nhưng log error
                        android.util.Log.e("UserRepositoryImpl", "Failed to update Firebase Auth displayName: " + e.getMessage());
                        callback.onSuccess(); // Still return success vì Database đã update
                    });
        } else {
            // Không có current user, nhưng database đã update thành công
            callback.onSuccess();
        }
    }
}
