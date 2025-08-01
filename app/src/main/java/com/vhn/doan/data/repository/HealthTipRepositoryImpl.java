package com.vhn.doan.data.repository;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.Category;
import com.vhn.doan.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Triển khai HealthTipRepository sử dụng Firebase Realtime Database
 */
public class HealthTipRepositoryImpl implements HealthTipRepository {

    private final FirebaseDatabase database;
    private final DatabaseReference healthTipsRef;
    private final DatabaseReference categoriesRef;
    private Map<Object, ValueEventListener> activeListeners = new HashMap<>();

    /**
     * Constructor mặc định
     */
    public HealthTipRepositoryImpl() {
        database = FirebaseDatabase.getInstance();
        healthTipsRef = database.getReference(Constants.HEALTH_TIPS_REF);
        categoriesRef = database.getReference(Constants.CATEGORIES_REF);
    }

    /**
     * Helper method để lấy category name từ category ID
     */
    private void loadCategoryNameForHealthTip(HealthTip healthTip, final Runnable onComplete) {
        if (healthTip.getCategoryId() == null || healthTip.getCategoryId().isEmpty()) {
            healthTip.setCategoryName("Chưa phân loại");
            if (onComplete != null) onComplete.run();
            return;
        }

        categoriesRef.child(healthTip.getCategoryId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Category category = dataSnapshot.getValue(Category.class);
                if (category != null && category.getName() != null && !category.getName().isEmpty()) {
                    healthTip.setCategoryName(category.getName());
                } else {
                    healthTip.setCategoryName("Chưa phân loại");
                }
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                healthTip.setCategoryName("Chưa phân loại");
                if (onComplete != null) onComplete.run();
            }
        });
    }

    /**
     * Helper method để load category names cho danh sách health tips
     */
    private void loadCategoryNamesForHealthTips(List<HealthTip> healthTips, final HealthTipCallback callback) {
        if (healthTips == null || healthTips.isEmpty()) {
            callback.onSuccess(healthTips);
            return;
        }

        final int[] completedCount = {0};
        final int totalCount = healthTips.size();

        for (HealthTip healthTip : healthTips) {
            loadCategoryNameForHealthTip(healthTip, new Runnable() {
                @Override
                public void run() {
                    completedCount[0]++;
                    if (completedCount[0] >= totalCount) {
                        callback.onSuccess(healthTips);
                    }
                }
            });
        }
    }

    /**
     * Helper method để load category name cho single health tip
     */
    private void loadCategoryNameForSingleHealthTip(HealthTip healthTip, final SingleHealthTipCallback callback) {
        loadCategoryNameForHealthTip(healthTip, new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(healthTip);
            }
        });
    }

    @Override
    public void getAllHealthTips(final HealthTipCallback callback) {
        healthTipsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<HealthTip> healthTips = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HealthTip healthTip = snapshot.getValue(HealthTip.class);
                    if (healthTip != null) {
                        // Đảm bảo ID được set từ key của Firebase
                        String healthTipId = snapshot.getKey();
                        healthTip.setId(healthTipId);

                        // Validate và set default values nếu cần
                        if (healthTip.getTitle() == null || healthTip.getTitle().trim().isEmpty()) {
                            healthTip.setTitle("Mẹo sức khỏe không tên");
                        }
                        if (healthTip.getContent() == null || healthTip.getContent().trim().isEmpty()) {
                            healthTip.setContent("Nội dung đang được cập nhật");
                        }
                        if (healthTip.getCreatedAt() <= 0) {
                            healthTip.setCreatedAt(System.currentTimeMillis());
                        }
                        if (healthTip.getViewCount() < 0) {
                            healthTip.setViewCount(0);
                        }
                        if (healthTip.getLikeCount() < 0) {
                            healthTip.setLikeCount(0);
                        }

                        healthTips.add(healthTip);
                    }
                }
                // Load category names cho tất cả health tips
                loadCategoryNamesForHealthTips(healthTips, callback);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    @Override
    public void getHealthTipDetail(String tipId, final SingleHealthTipCallback callback) {
        if (tipId == null || tipId.trim().isEmpty()) {
            callback.onError("ID mẹo sức khỏe không hợp lệ");
            return;
        }

        healthTipsRef.child(tipId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HealthTip healthTip = dataSnapshot.getValue(HealthTip.class);
                if (healthTip != null) {
                    // Đảm bảo ID được set chính xác
                    healthTip.setId(dataSnapshot.getKey());

                    // Validate dữ liệu
                    if (healthTip.getTitle() == null || healthTip.getTitle().trim().isEmpty()) {
                        healthTip.setTitle("Mẹo sức khỏe không tên");
                    }
                    if (healthTip.getContent() == null || healthTip.getContent().trim().isEmpty()) {
                        healthTip.setContent("Nội dung đang được cập nhật");
                    }
                    if (healthTip.getViewCount() < 0) {
                        healthTip.setViewCount(0);
                    }
                    if (healthTip.getLikeCount() < 0) {
                        healthTip.setLikeCount(0);
                    }

                    // Load category name
                    loadCategoryNameForSingleHealthTip(healthTip, callback);
                } else {
                    callback.onError("Không tìm thấy mẹo sức khỏe với ID: " + tipId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    @Override
    public void getHealthTipsByCategory(String categoryId, final HealthTipCallback callback) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            callback.onError("ID danh mục không hợp lệ");
            return;
        }

        Query query = healthTipsRef.orderByChild("categoryId").equalTo(categoryId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<HealthTip> healthTips = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HealthTip healthTip = snapshot.getValue(HealthTip.class);
                    if (healthTip != null) {
                        // Đảm bảo ID được set từ key
                        healthTip.setId(snapshot.getKey());

                        // Validate dữ liệu
                        if (healthTip.getViewCount() < 0) {
                            healthTip.setViewCount(0);
                        }
                        if (healthTip.getLikeCount() < 0) {
                            healthTip.setLikeCount(0);
                        }

                        healthTips.add(healthTip);
                    }
                }
                // Load category names
                loadCategoryNamesForHealthTips(healthTips, callback);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }


    @Override
    public void getLatestHealthTips(int limit, final HealthTipCallback callback) {
        Query query = healthTipsRef.orderByChild("createdAt").limitToLast(limit);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<HealthTip> healthTips = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HealthTip healthTip = snapshot.getValue(HealthTip.class);
                    if (healthTip != null) {
                        healthTip.setId(snapshot.getKey());
                        healthTips.add(healthTip);
                    }
                }
                // Đảo ngược danh sách để các mục mới nhất hiển thị trước
                List<HealthTip> reversedList = new ArrayList<>();
                for (int i = healthTips.size() - 1; i >= 0; i--) {
                    reversedList.add(healthTips.get(i));
                }
                // Load category names trước khi trả về callback
                loadCategoryNamesForHealthTips(reversedList, callback);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    @Override
    public void getMostViewedHealthTips(int limit, final HealthTipCallback callback) {
        Query query = healthTipsRef.orderByChild("viewCount").limitToLast(limit);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<HealthTip> healthTips = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HealthTip healthTip = snapshot.getValue(HealthTip.class);
                    if (healthTip != null) {
                        healthTip.setId(snapshot.getKey());
                        healthTips.add(healthTip);
                    }
                }
                // Đảo ngược danh sách để các mục có số lượt xem nhiều nhất hiển thị trước
                List<HealthTip> reversedList = new ArrayList<>();
                for (int i = healthTips.size() - 1; i >= 0; i--) {
                    reversedList.add(healthTips.get(i));
                }
                // Load category names trước khi trả về callback
                loadCategoryNamesForHealthTips(reversedList, callback);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    @Override
    public void getMostLikedHealthTips(int limit, final HealthTipCallback callback) {
        Query query = healthTipsRef.orderByChild("likeCount").limitToLast(limit);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<HealthTip> healthTips = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HealthTip healthTip = snapshot.getValue(HealthTip.class);
                    if (healthTip != null) {
                        healthTip.setId(snapshot.getKey());
                        healthTips.add(healthTip);
                    }
                }
                // Đảo ngược danh sách để các mục có số lượt thích nhiều nhất hiển thị trước
                List<HealthTip> reversedList = new ArrayList<>();
                for (int i = healthTips.size() - 1; i >= 0; i--) {
                    reversedList.add(healthTips.get(i));
                }
                // Load category names trước khi trả về callback
                loadCategoryNamesForHealthTips(reversedList, callback);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    @Override
    public void searchHealthTips(String query, HealthTipCallback callback) {
        if (query == null || query.trim().isEmpty()) {
            callback.onError("Từ khóa tìm kiếm không hợp lệ");
            return;
        }

        healthTipsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<HealthTip> searchResults = new ArrayList<>();
                String searchQuery = query.toLowerCase().trim();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HealthTip healthTip = snapshot.getValue(HealthTip.class);
                    if (healthTip != null) {
                        healthTip.setId(snapshot.getKey());

                        // Tìm kiếm trong tiêu đề và nội dung
                        boolean titleMatch = healthTip.getTitle() != null &&
                                           healthTip.getTitle().toLowerCase().contains(searchQuery);
                        boolean contentMatch = healthTip.getContent() != null &&
                                             healthTip.getContent().toLowerCase().contains(searchQuery);

                        if (titleMatch || contentMatch) {
                            searchResults.add(healthTip);
                        }
                    }
                }
                // Load category names cho search results
                loadCategoryNamesForHealthTips(searchResults, callback);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Lỗi khi tìm kiếm: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void addHealthTip(HealthTip healthTip, final HealthTipOperationCallback callback) {
        String key = healthTipsRef.push().getKey();
        if (key != null) {
            healthTip.setId(key);
            healthTipsRef.child(key).setValue(healthTip)
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        } else {
            callback.onError("Không thể tạo ID cho mẹo sức khỏe mới");
        }
    }

    @Override
    public void updateLikeStatus(String tipId, boolean isLiked, HealthTipOperationCallback callback) {
        if (tipId == null || tipId.isEmpty()) {
            callback.onError("ID bài viết không hợp lệ");
            return;
        }

        DatabaseReference tipRef = healthTipsRef.child(tipId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("liked", isLiked);

        tipRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Lỗi khi cập nhật trạng thái thích: " + e.getMessage()));
    }

    @Override
    public Object listenToLatestHealthTips(int limit, HealthTipCallback callback) {
        Query query = healthTipsRef.orderByChild("timestamp").limitToLast(limit);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<HealthTip> healthTips = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HealthTip healthTip = snapshot.getValue(HealthTip.class);
                    if (healthTip != null) {
                        healthTip.setId(snapshot.getKey());
                        healthTips.add(healthTip);
                    }
                }
                // Load category names cho listen results
                loadCategoryNamesForHealthTips(healthTips, callback);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Lỗi khi lắng nghe dữ liệu: " + databaseError.getMessage());
            }
        };

        query.addValueEventListener(listener);
        activeListeners.put(listener, listener);
        return listener;
    }

    @Override
    public void removeListener(Object listener) {
        if (listener instanceof ValueEventListener) {
            ValueEventListener valueEventListener = (ValueEventListener) listener;
            if (activeListeners.containsKey(listener)) {
                healthTipsRef.removeEventListener(valueEventListener);
                activeListeners.remove(listener);
            }
        }
    }

    @Override
    public void updateFavoriteStatus(String tipId, boolean isFavorite, HealthTipOperationCallback callback) {
        if (tipId == null || tipId.isEmpty()) {
            callback.onError("ID bài viết không hợp lệ");
            return;
        }

        DatabaseReference tipRef = healthTipsRef.child(tipId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("favorite", isFavorite);

        tipRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Lỗi khi cập nhật trạng thái yêu thích: " + e.getMessage()));
    }

    @Override
    public void updateViewCount(String tipId, HealthTipOperationCallback callback) {
        if (tipId == null || tipId.isEmpty()) {
            callback.onError("ID bài viết không hợp lệ");
            return;
        }

        DatabaseReference tipRef = healthTipsRef.child(tipId).child("viewCount");
        tipRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int currentCount = 0;
                if (dataSnapshot.exists()) {
                    currentCount = dataSnapshot.getValue(Integer.class);
                }
                tipRef.setValue(currentCount + 1)
                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                        .addOnFailureListener(e -> callback.onError("Lỗi khi cập nhật số lượt xem: " + e.getMessage()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Lỗi khi đọc số lượt xem: " + databaseError.getMessage());
            }
        });
    }


    @Override
    public void getFavoriteHealthTips(String userId, final HealthTipCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onError("ID người dùng không hợp lệ");
            return;
        }

        // Tìm các bài viết mà người dùng đã đánh dấu yêu thích
        Query query = healthTipsRef.orderByChild("favoriteUsers/" + userId).equalTo(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<HealthTip> favoriteHealthTips = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HealthTip healthTip = snapshot.getValue(HealthTip.class);
                    if (healthTip != null) {
                        healthTip.setId(snapshot.getKey());
                        favoriteHealthTips.add(healthTip);
                    }
                }
                // Load category names trước khi trả về callback
                loadCategoryNamesForHealthTips(favoriteHealthTips, callback);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Lỗi khi tải danh sách yêu thích: " + databaseError.getMessage());
            }
        });
    }
}
