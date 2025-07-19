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
    public void getHealthTipById(String healthTipId, final HealthTipCallback callback) {
        healthTipsRef.child(healthTipId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HealthTip healthTip = dataSnapshot.getValue(HealthTip.class);
                if (healthTip != null) {
                    healthTip.setId(dataSnapshot.getKey());
                    List<HealthTip> healthTips = new ArrayList<>();
                    healthTips.add(healthTip);
                    callback.onSuccess(healthTips);
                } else {
                    callback.onError("Không tìm thấy mẹo sức khỏe");
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
    public void addHealthTip(final HealthTip healthTip, final HealthTipOperationCallback callback) {
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
    public void updateHealthTip(final HealthTip healthTip, final HealthTipOperationCallback callback) {
        if (healthTip.getId() == null) {
            callback.onError("ID của mẹo sức khỏe không hợp lệ");
            return;
        }

        Map<String, Object> healthTipValues = new HashMap<>();
        healthTipValues.put("title", healthTip.getTitle());
        healthTipValues.put("content", healthTip.getContent());
        healthTipValues.put("categoryId", healthTip.getCategoryId());
        healthTipValues.put("viewCount", healthTip.getViewCount());
        healthTipValues.put("likeCount", healthTip.getLikeCount());
        healthTipValues.put("imageUrl", healthTip.getImageUrl());
        healthTipValues.put("createdAt", healthTip.getCreatedAt());
        healthTipValues.put("isFavorite", healthTip.isFavorite());

        healthTipsRef.child(healthTip.getId()).updateChildren(healthTipValues)
                .addOnSuccessListener(aVoid -> callback.onSuccess(healthTip.getId()))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void deleteHealthTip(final String healthTipId, final HealthTipOperationCallback callback) {
        healthTipsRef.child(healthTipId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess(healthTipId))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void incrementViewCount(String healthTipId, final HealthTipOperationCallback callback) {
        DatabaseReference viewCountRef = healthTipsRef.child(healthTipId).child("viewCount");
        viewCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer currentCount = dataSnapshot.getValue(Integer.class);
                int newCount = (currentCount == null) ? 1 : currentCount + 1;
                viewCountRef.setValue(newCount)
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
    public void toggleLike(String healthTipId, final boolean like, final HealthTipOperationCallback callback) {
        DatabaseReference likeCountRef = healthTipsRef.child(healthTipId).child("likeCount");
        likeCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer currentCount = dataSnapshot.getValue(Integer.class);
                int newCount;

                if (currentCount == null) {
                    newCount = like ? 1 : 0;
                } else {
                    newCount = like ? currentCount + 1 : Math.max(0, currentCount - 1);
                }

                likeCountRef.setValue(newCount)
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
    public Object listenToLatestHealthTips(int limit, HealthTipCallback callback) {
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
        return listener;
    }

    @Override
    public void removeListener(Object listener) {
        if (listener instanceof ValueEventListener) {
            healthTipsRef.removeEventListener((ValueEventListener) listener);
        }
    }
}
