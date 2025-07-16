package com.vhn.doan.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vhn.doan.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * FirebaseManager là lớp quản lý các dịch vụ Firebase như Auth, Firestore, Realtime DB và Storage
 * Cung cấp các phương thức truy cập và thao tác với dữ liệu Firebase
 */
public class FirebaseManager {

    // Các instance Firebase
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private final FirebaseDatabase realtimeDB;
    private final FirebaseStorage storage;

    /**
     * Constructor mặc định của FirebaseManager
     * Khởi tạo các instance Firebase
     */
    public FirebaseManager() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.realtimeDB = FirebaseDatabase.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    /**
     * Lấy instance FirebaseAuth
     * @return Instance FirebaseAuth
     */
    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    /**
     * Lấy instance FirebaseFirestore
     * @return Instance FirebaseFirestore
     */
    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    /**
     * Lấy instance FirebaseDatabase (Realtime Database)
     * @return Instance FirebaseDatabase
     */
    public FirebaseDatabase getRealtimeDB() {
        return realtimeDB;
    }

    /**
     * Lấy instance FirebaseStorage
     * @return Instance FirebaseStorage
     */
    public FirebaseStorage getStorage() {
        return storage;
    }

    /**
     * Lấy reference đến thư mục gốc của Storage
     * @return StorageReference tới thư mục gốc
     */
    public StorageReference getRootStorageReference() {
        return storage.getReference();
    }

    /**
     * Lấy reference đến thư mục lưu trữ ảnh người dùng
     * @param userId ID của người dùng
     * @return StorageReference tới thư mục ảnh người dùng
     */
    public StorageReference getUserImagesReference(String userId) {
        return storage.getReference().child(Constants.USERS_COLLECTION).child(userId).child(Constants.PROFILE_IMAGES_PATH);
    }

    /**
     * Tạo document người dùng mới trong Firestore
     * @param userId ID của người dùng
     * @param email Email của người dùng
     */
    public void createUserDocument(String userId, String email) {
        DocumentReference userDocRef = firestore.collection(Constants.USERS_COLLECTION).document(userId);

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("createdAt", System.currentTimeMillis());

        userDocRef.set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Document tạo thành công
                })
                .addOnFailureListener(e -> {
                    // Lỗi khi tạo document
                });
    }

    /**
     * Cập nhật thông tin người dùng trong Firestore
     * @param userId ID của người dùng
     * @param userData Map chứa dữ liệu cần cập nhật
     */
    public void updateUserData(String userId, Map<String, Object> userData) {
        DocumentReference userDocRef = firestore.collection(Constants.USERS_COLLECTION).document(userId);

        userDocRef.update(userData)
                .addOnSuccessListener(aVoid -> {
                    // Document cập nhật thành công
                })
                .addOnFailureListener(e -> {
                    // Lỗi khi cập nhật document
                });
    }

    /**
     * Lấy ID của người dùng hiện tại
     * @return ID người dùng hoặc null nếu chưa đăng nhập
     */
    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }
}
