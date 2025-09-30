package com.vhn.doan.data.repository;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Triển khai FavoriteRepository sử dụng Firebase Realtime Database
 * Lưu dữ liệu theo cấu trúc: users/{uid}/favorites/{tipId}
 */
public class FavoriteRepositoryImpl implements FavoriteRepository {

    private final FirebaseDatabase database;
    private final HealthTipRepository healthTipRepository;

    /**
     * Constructor mặc định
     */
    public FavoriteRepositoryImpl() {
        database = FirebaseDatabase.getInstance();
        healthTipRepository = new HealthTipRepositoryImpl();
    }

    @Override
    public void addToFavorites(String userId, String healthTipId, FavoriteActionCallback callback) {
        if (userId == null || userId.trim().isEmpty() ||
            healthTipId == null || healthTipId.trim().isEmpty()) {
            callback.onError("Thông tin không hợp lệ");
            return;
        }

        // Tạo favorite entry theo cấu trúc dữ liệu test
        Map<String, Object> favoriteData = new HashMap<>();
        favoriteData.put("healthTipId", healthTipId);
        favoriteData.put("userId", userId);
        favoriteData.put("addedAt", System.currentTimeMillis());

        DatabaseReference favoriteRef = database.getReference(Constants.FAVORITES_REF)
                .child(userId)
                .child(healthTipId);

        favoriteRef.setValue(favoriteData)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Lỗi khi thêm vào yêu thích: " + e.getMessage()));
    }

    @Override
    public void removeFromFavorites(String userId, String healthTipId, FavoriteActionCallback callback) {
        if (userId == null || userId.trim().isEmpty() ||
            healthTipId == null || healthTipId.trim().isEmpty()) {
            callback.onError("Thông tin không hợp lệ");
            return;
        }

        DatabaseReference favoriteRef = database.getReference(Constants.FAVORITES_REF)
                .child(userId)
                .child(healthTipId);

        favoriteRef.removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Lỗi khi xóa khỏi yêu thích: " + e.getMessage()));
    }

    @Override
    public void clearAllFavorites(String userId, FavoriteActionCallback callback) {
        if (userId == null || userId.trim().isEmpty()) {
            callback.onError("ID người dùng không hợp lệ");
            return;
        }

        DatabaseReference userFavoritesRef = database.getReference(Constants.FAVORITES_REF)
                .child(userId);

        userFavoritesRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                String errorMessage = task.getException() != null ?
                    task.getException().getMessage() : "Lỗi không xác định";
                callback.onError("Lỗi khi xóa toàn bộ danh sách yêu thích: " + errorMessage);
            }
        });
    }

    @Override
    public void getFavoriteHealthTips(String userId, FavoriteListCallback callback) {
        if (userId == null || userId.trim().isEmpty()) {
            callback.onError("ID người dùng không hợp lệ");
            return;
        }

        DatabaseReference userFavoritesRef = database.getReference(Constants.FAVORITES_REF)
                .child(userId);

        userFavoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> favoriteIds = new ArrayList<>();

                // Lấy danh sách ID các bài viết yêu thích
                for (DataSnapshot favoriteSnapshot : dataSnapshot.getChildren()) {
                    String healthTipId = favoriteSnapshot.getKey();
                    if (healthTipId != null) {
                        favoriteIds.add(healthTipId);
                    }
                }

                if (favoriteIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                // Lấy chi tiết từng bài viết yêu thích
                List<HealthTip> favoriteHealthTips = new ArrayList<>();
                final int[] completedRequests = {0};

                for (String healthTipId : favoriteIds) {
                    healthTipRepository.getHealthTipDetail(healthTipId, new HealthTipRepository.SingleHealthTipCallback() {
                        @Override
                        public void onSuccess(HealthTip healthTip) {
                            if (healthTip != null) {
                                healthTip.setFavorite(true); // Đánh dấu là yêu thích
                                favoriteHealthTips.add(healthTip);
                            }
                            completedRequests[0]++;

                            if (completedRequests[0] == favoriteIds.size()) {
                                callback.onSuccess(favoriteHealthTips);
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            completedRequests[0]++;

                            if (completedRequests[0] == favoriteIds.size()) {
                                callback.onSuccess(favoriteHealthTips);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError("Lỗi khi lấy danh sách yêu thích: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void checkFavoriteStatus(String userId, String healthTipId, FavoriteCheckCallback callback) {
        if (userId == null || userId.trim().isEmpty() ||
            healthTipId == null || healthTipId.trim().isEmpty()) {
            callback.onError("Thông tin không hợp lệ");
            return;
        }

        DatabaseReference favoriteRef = database.getReference(Constants.FAVORITES_REF)
                .child(userId)
                .child(healthTipId);

        favoriteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isFavorite = dataSnapshot.exists();
                callback.onResult(isFavorite);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError("Lỗi khi kiểm tra trạng thái yêu thích: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void checkFavoriteStatus(String userId, String healthTipId, FavoriteStatusCallback callback) {
        if (userId == null || userId.trim().isEmpty() ||
            healthTipId == null || healthTipId.trim().isEmpty()) {
            callback.onError("Thông tin không hợp lệ");
            return;
        }

        DatabaseReference favoriteRef = database.getReference(Constants.FAVORITES_REF)
                .child(userId)
                .child(healthTipId);

        favoriteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isFavorite = dataSnapshot.exists();
                callback.onResult(isFavorite);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError("Lỗi khi kiểm tra trạng thái yêu thích: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void getFavoriteCount(String userId, FavoriteCountCallback callback) {
        if (userId == null || userId.trim().isEmpty()) {
            callback.onError("ID người dùng không hợp lệ");
            return;
        }

        DatabaseReference userFavoritesRef = database.getReference(Constants.FAVORITES_REF)
                .child(userId);

        userFavoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                callback.onSuccess(count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError("Lỗi khi đếm số lượng yêu thích: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void getFavoriteHealthTipIds(String userId, FavoriteListCallback callback) {
        // Sử dụng lại logic của getFavoriteHealthTips
        getFavoriteHealthTips(userId, callback);
    }
}
