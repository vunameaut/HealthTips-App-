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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

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
    public void getRecommendedHealthTips(int limit, final HealthTipCallback callback) {
        // Logic đề xuất: Lấy ngẫu nhiên các bài viết từ nhiều danh mục khác nhau
        // Kết hợp từ các bài viết mới, được xem nhiều và được thích nhiều
        healthTipsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<HealthTip> allHealthTips = new ArrayList<>();

                // Lấy tất cả bài viết
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HealthTip healthTip = snapshot.getValue(HealthTip.class);
                    if (healthTip != null) {
                        healthTip.setId(snapshot.getKey());
                        allHealthTips.add(healthTip);
                    }
                }

                if (allHealthTips.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                // Thuật toán đề xuất đơn giản:
                // 1. Ưu tiên các bài viết có điểm số cao (dựa trên lượt xem + lượt thích)
                // 2. Đảm bảo đa dạng danh mục
                // 3. Trộn ngẫu nhiên để tạo sự mới mẻ

                List<HealthTip> recommendedTips = new ArrayList<>();

                // Tính điểm và sắp xếp
                for (HealthTip tip : allHealthTips) {
                    int viewCount = tip.getViewCount() != null ? tip.getViewCount() : 0;
                    int likeCount = tip.getLikeCount() != null ? tip.getLikeCount() : 0;
                    // Điểm = lượt xem + (lượt thích * 2) để ưu tiên bài được thích
                    tip.setRecommendationScore(viewCount + (likeCount * 2));
                }

                // Sắp xếp theo điểm đề xuất giảm dần
                allHealthTips.sort((tip1, tip2) -> {
                    int score1 = tip1.getRecommendationScore() != null ? tip1.getRecommendationScore() : 0;
                    int score2 = tip2.getRecommendationScore() != null ? tip2.getRecommendationScore() : 0;
                    return Integer.compare(score2, score1);
                });

                // Lấy các bài viết top và đảm bảo đa dạng danh mục
                Map<String, Integer> categoryCount = new HashMap<>();
                int maxPerCategory = Math.max(1, limit / 3); // Tối đa limit/3 bài viết per danh mục

                for (HealthTip tip : allHealthTips) {
                    if (recommendedTips.size() >= limit) break;

                    String categoryId = tip.getCategoryId() != null ? tip.getCategoryId() : "unknown";
                    int currentCount = categoryCount.getOrDefault(categoryId, 0);

                    // Thêm bài viết nếu chưa đạt giới hạn danh mục hoặc vẫn còn slot
                    if (currentCount < maxPerCategory || recommendedTips.size() < limit - 2) {
                        recommendedTips.add(tip);
                        categoryCount.put(categoryId, currentCount + 1);
                    }
                }

                // Nếu chưa đủ số lượng, thêm các bài viết còn lại
                if (recommendedTips.size() < limit) {
                    for (HealthTip tip : allHealthTips) {
                        if (recommendedTips.size() >= limit) break;
                        if (!recommendedTips.contains(tip)) {
                            recommendedTips.add(tip);
                        }
                    }
                }

                // Giới hạn số lượng kết quả
                if (recommendedTips.size() > limit) {
                    recommendedTips = recommendedTips.subList(0, limit);
                }

                // Load category names trước khi trả về callback
                loadCategoryNamesForHealthTips(recommendedTips, callback);
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

    @Override
    public void getDailyRecommendedHealthTips(String date, int limit, final HealthTipCallback callback) {
        if (date == null || date.isEmpty()) {
            callback.onError("Ngày không hợp lệ");
            return;
        }

        // Lấy tất cả bài viết trước, sau đó áp dụng thuật toán đề xuất dựa trên ngày
        healthTipsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<HealthTip> allHealthTips = new ArrayList<>();

                // Lấy tất cả bài viết
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HealthTip healthTip = snapshot.getValue(HealthTip.class);
                    if (healthTip != null) {
                        healthTip.setId(snapshot.getKey());
                        allHealthTips.add(healthTip);
                    }
                }

                if (allHealthTips.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                // Tạo seed từ ngày để đảm bảo tính nhất quán
                // Cùng một ngày sẽ luôn có cùng một bộ bài viết được đề xuất
                long seed = date.hashCode();
                Random random = new Random(seed);

                // Tạo danh sách đề xuất dựa trên thuật toán
                List<HealthTip> dailyRecommended = generateDailyRecommendations(allHealthTips, limit, random);

                // Load category names trước khi trả về callback
                loadCategoryNamesForHealthTips(dailyRecommended, callback);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Lỗi khi tải dữ liệu: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void getTodayRecommendedHealthTips(int limit, HealthTipCallback callback) {
        // Lấy ngày hiện tại theo định dạng yyyy-MM-dd
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        // Gọi phương thức getDailyRecommendedHealthTips với ngày hôm nay
        getDailyRecommendedHealthTips(today, limit, callback);
    }

    /**
     * Tạo danh sách bài viết đề xuất cho một ngày cụ thể
     * Sử dụng thuật toán seed để đảm bảo tính nhất quán
     */
    private List<HealthTip> generateDailyRecommendations(List<HealthTip> allHealthTips, int limit, Random random) {
        if (allHealthTips.isEmpty()) {
            return new ArrayList<>();
        }

        // Tạo bản sao để không ảnh hưởng đến danh sách gốc
        List<HealthTip> availableTips = new ArrayList<>(allHealthTips);
        List<HealthTip> recommendedTips = new ArrayList<>();

        // Thuật toán đề xuất:
        // 1. Chia bài viết thành các nhóm theo điểm số (cao, trung bình, thấp)
        // 2. Chọn ngẫu nhiên từ mỗi nhóm để đảm bảo đa dạng
        // 3. Ưu tiên bài viết có điểm cao nhưng vẫn có sự ngẫu nhiên

        // Tính điểm cho từng bài viết
        for (HealthTip tip : availableTips) {
            int viewCount = tip.getViewCount() != null ? tip.getViewCount() : 0;
            int likeCount = tip.getLikeCount() != null ? tip.getLikeCount() : 0;
            long ageInDays = (System.currentTimeMillis() - tip.getCreatedAt()) / (1000 * 60 * 60 * 24);

            // Điểm = (lượt xem + lượt thích * 2) / (tuổi bài viết + 1)
            // Điều này ưu tiên bài viết mới và có tương tác cao
            double score = (viewCount + likeCount * 2.0) / (ageInDays + 1);
            tip.setRecommendationScore((int) (score * 100)); // Nhân 100 để dễ so sánh
        }

        // Sắp xếp theo điểm
        availableTips.sort((tip1, tip2) -> {
            int score1 = tip1.getRecommendationScore() != null ? tip1.getRecommendationScore() : 0;
            int score2 = tip2.getRecommendationScore() != null ? tip2.getRecommendationScore() : 0;
            return Integer.compare(score2, score1);
        });

        // Chia thành 3 nhóm: Top 30%, Middle 40%, Bottom 30%
        int totalCount = availableTips.size();
        int topCount = Math.max(1, (int) (totalCount * 0.3));
        int middleCount = Math.max(1, (int) (totalCount * 0.4));

        List<HealthTip> topTips = availableTips.subList(0, Math.min(topCount, totalCount));
        List<HealthTip> middleTips = availableTips.subList(Math.min(topCount, totalCount),
                Math.min(topCount + middleCount, totalCount));
        List<HealthTip> bottomTips = availableTips.subList(Math.min(topCount + middleCount, totalCount), totalCount);

        // Chọn bài viết từ mỗi nhóm với tỷ lệ: 50% top, 30% middle, 20% bottom
        int topLimit = Math.max(1, (int) (limit * 0.5));
        int middleLimit = Math.max(1, (int) (limit * 0.3));
        int bottomLimit = limit - topLimit - middleLimit;

        // Thêm bài viết từ nhóm top
        addRandomTipsFromGroup(topTips, topLimit, recommendedTips, random);

        // Thêm bài viết từ nhóm middle
        addRandomTipsFromGroup(middleTips, middleLimit, recommendedTips, random);

        // Thêm bài viết từ nhóm bottom
        addRandomTipsFromGroup(bottomTips, bottomLimit, recommendedTips, random);

        // Nếu chưa đủ số lượng, thêm ngẫu nhiên từ các bài viết còn lại
        while (recommendedTips.size() < limit && recommendedTips.size() < totalCount) {
            for (HealthTip tip : availableTips) {
                if (recommendedTips.size() >= limit) break;
                if (!recommendedTips.contains(tip)) {
                    recommendedTips.add(tip);
                }
            }
        }

        // Trộn ngẫu nhiên danh sách cuối cùng để tạo sự đa dạng trong hiển thị
        Collections.shuffle(recommendedTips, random);

        return recommendedTips;
    }

    /**
     * Thêm ngẫu nhiên các bài viết từ một nhóm vào danh sách đề xuất
     */
    private void addRandomTipsFromGroup(List<HealthTip> sourceTips, int maxCount,
                                       List<HealthTip> targetTips, Random random) {
        if (sourceTips.isEmpty() || maxCount <= 0) return;

        List<HealthTip> availableTips = new ArrayList<>();
        for (HealthTip tip : sourceTips) {
            if (!targetTips.contains(tip)) {
                availableTips.add(tip);
            }
        }

        Collections.shuffle(availableTips, random);

        int addCount = Math.min(maxCount, availableTips.size());
        for (int i = 0; i < addCount; i++) {
            targetTips.add(availableTips.get(i));
        }
    }
}
