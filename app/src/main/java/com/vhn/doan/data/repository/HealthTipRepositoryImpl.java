package com.vhn.doan.data.repository;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.data.HealthTip;
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
    private Map<Object, ValueEventListener> activeListeners = new HashMap<>();

    /**
     * Constructor mặc định
     */
    public HealthTipRepositoryImpl() {
        database = FirebaseDatabase.getInstance();
        healthTipsRef = database.getReference(Constants.HEALTH_TIPS_REF);
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
                        healthTip.setId(snapshot.getKey());
                        healthTips.add(healthTip);
                    }
                }
                callback.onSuccess(healthTips);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    @Override
    public void getHealthTipsByCategory(String categoryId, final HealthTipCallback callback) {
        Query query = healthTipsRef.orderByChild("categoryId").equalTo(categoryId);
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
                callback.onSuccess(healthTips);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    @Override
    public void getHealthTipById(String healthTipId, final SingleHealthTipCallback callback) {
        if (healthTipId == null || healthTipId.isEmpty()) {
            callback.onError("ID bài viết không hợp lệ");
            return;
        }

        healthTipsRef.child(healthTipId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HealthTip healthTip = dataSnapshot.getValue(HealthTip.class);
                if (healthTip != null) {
                    healthTip.setId(dataSnapshot.getKey());
                    callback.onSuccess(healthTip);
                } else {
                    callback.onError("Không tìm thấy bài viết");
                }
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
                callback.onSuccess(reversedList);
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
                callback.onSuccess(reversedList);
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
                callback.onSuccess(reversedList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    @Override
    public void searchHealthTips(String query, final HealthTipCallback callback) {
        // Firebase Realtime Database không hỗ trợ tìm kiếm text đầy đủ
        // Chúng ta sẽ lấy tất cả và lọc theo tên
        healthTipsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<HealthTip> searchResults = new ArrayList<>();
                String lowerQuery = query.toLowerCase();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HealthTip healthTip = snapshot.getValue(HealthTip.class);
                    if (healthTip != null) {
                        healthTip.setId(snapshot.getKey());

                        // Kiểm tra nếu tiêu đề hoặc nội dung chứa từ khóa tìm kiếm
                        if ((healthTip.getTitle() != null && healthTip.getTitle().toLowerCase().contains(lowerQuery)) ||
                            (healthTip.getContent() != null && healthTip.getContent().toLowerCase().contains(lowerQuery))) {
                            searchResults.add(healthTip);
                        }
                    }
                }

                callback.onSuccess(searchResults);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    @Override
    public void addHealthTip(HealthTip healthTip, final HealthTipOperationCallback callback) {
        String key = healthTipsRef.push().getKey();
        if (key != null) {
            healthTip.setId(key);
            healthTipsRef.child(key).setValue(healthTip)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(key))
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        } else {
            callback.onError("Không thể tạo ID cho mẹo sức khỏe mới");
        }
    }

    @Override
    public void updateFavoriteStatus(String healthTipId, boolean isFavorite, final HealthTipOperationCallback callback) {
        if (healthTipId == null || healthTipId.isEmpty()) {
            callback.onError("ID bài viết không hợp lệ");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("isFavorite", isFavorite);

        healthTipsRef.child(healthTipId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(healthTipId))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void incrementViewCount(String healthTipId, final HealthTipOperationCallback callback) {
        if (healthTipId == null || healthTipId.isEmpty()) {
            callback.onError("ID bài viết không hợp lệ");
            return;
        }

        DatabaseReference healthTipRef = healthTipsRef.child(healthTipId).child("viewCount");

        healthTipRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer currentViews = dataSnapshot.getValue(Integer.class);
                int newViewCount = (currentViews != null) ? currentViews + 1 : 1;

                healthTipRef.setValue(newViewCount)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(healthTipId))
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    @Override
    public void updateLikeStatus(String healthTipId, boolean isLiked, final HealthTipOperationCallback callback) {
        if (healthTipId == null || healthTipId.isEmpty()) {
            callback.onError("ID bài viết không hợp lệ");
            return;
        }

        DatabaseReference healthTipRef = healthTipsRef.child(healthTipId).child("likeCount");

        healthTipRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer currentLikes = dataSnapshot.getValue(Integer.class);
                int likesCount = (currentLikes != null) ? currentLikes : 0;

                int newLikeCount;
                if (isLiked) {
                    newLikeCount = likesCount + 1;
                } else {
                    newLikeCount = Math.max(0, likesCount - 1);
                }

                healthTipRef.setValue(newLikeCount)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(healthTipId))
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    /**
     * Đăng ký lắng nghe các thay đổi từ Firebase cho dữ liệu mẹo sức khỏe mới nhất
     * @param limit Số lượng mẹo muốn lấy
     * @param callback Callback để nhận kết quả
     * @return Object định danh của listener để có thể hủy đăng ký sau này
     */
    @Override
    public Object listenToLatestHealthTips(int limit, final HealthTipCallback callback) {
        Query query = healthTipsRef.orderByChild("createdAt").limitToLast(limit);
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
                // Đảo ngược danh sách để các mục mới nhất hiển thị trước
                List<HealthTip> reversedList = new ArrayList<>();
                for (int i = healthTips.size() - 1; i >= 0; i--) {
                    reversedList.add(healthTips.get(i));
                }
                callback.onSuccess(reversedList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        };

        query.addValueEventListener(listener);
        Object listenerId = new Object();
        activeListeners.put(listenerId, listener);
        return listenerId;
    }

    /**
     * Hủy đăng ký lắng nghe thay đổi từ Firebase
     * @param listener Đối tượng listener cần hủy, nhận được từ phương thức đăng ký tương ứng
     */
    @Override
    public void removeListener(Object listener) {
        ValueEventListener valueEventListener = activeListeners.get(listener);
        if (valueEventListener != null) {
            healthTipsRef.removeEventListener(valueEventListener);
            activeListeners.remove(listener);
        }
    }
}
