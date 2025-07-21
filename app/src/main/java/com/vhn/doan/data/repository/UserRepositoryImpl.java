package com.vhn.doan.data.repository;

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
}
