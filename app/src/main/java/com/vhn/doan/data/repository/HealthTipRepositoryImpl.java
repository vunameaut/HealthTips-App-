package com.vhn.doan.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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
import com.vhn.doan.data.local.AppDatabase;
import com.vhn.doan.data.local.dao.HealthTipDao;
import com.vhn.doan.data.local.entity.HealthTipEntity;
import com.vhn.doan.utils.AuthTokenManager;
import com.vhn.doan.utils.Constants;
import com.vhn.doan.utils.NetworkUtils;
import com.vhn.doan.utils.VercelApiHelper;

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
 * Triển khai HealthTipRepository sử dụng Firebase Realtime Database + Room Cache
 * Chiến lược Offline-First: Hiển thị cache trước, sau đó sync từ server
 */
public class HealthTipRepositoryImpl implements HealthTipRepository {

    private static final String TAG = "HealthTipRepoImpl";

    private final FirebaseDatabase database;
    private final DatabaseReference healthTipsRef;
    private final DatabaseReference categoriesRef;
    private final HealthTipDao healthTipDao;
    private final AppDatabase appDatabase;
    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Map<Object, ValueEventListener> activeListeners = new HashMap<>();

    /**
     * Constructor với Context để khởi tạo Room Database
     */
    public HealthTipRepositoryImpl(Context context) {
        this.context = context.getApplicationContext();
        database = FirebaseDatabase.getInstance();
        healthTipsRef = database.getReference(Constants.HEALTH_TIPS_REF);
        categoriesRef = database.getReference(Constants.CATEGORIES_REF);

        // Khởi tạo Room Database
        appDatabase = AppDatabase.getInstance(this.context);
        healthTipDao = appDatabase.healthTipDao();

        Log.d(TAG, "HealthTipRepositoryImpl initialized with offline support");
    }

    /**
     * Constructor mặc định (để tương thích ngược)
     * @deprecated Sử dụng constructor với Context thay thế
     */
    @Deprecated
    public HealthTipRepositoryImpl() {
        this.context = null;
        database = FirebaseDatabase.getInstance();
        healthTipsRef = database.getReference(Constants.HEALTH_TIPS_REF);
        categoriesRef = database.getReference(Constants.CATEGORIES_REF);
        appDatabase = null;
        healthTipDao = null;

        Log.w(TAG, "HealthTipRepositoryImpl initialized WITHOUT offline support (deprecated constructor)");
    }

    /**
     * Helper method để xử lý DatabaseError và kiểm tra PERMISSION_DENIED
     * @param databaseError Lỗi từ Firebase
     * @param errorMessage Thông báo lỗi mặc định
     * @return Thông báo lỗi đã được xử lý
     */
    private String handleDatabaseError(DatabaseError databaseError, String errorMessage) {
        if (databaseError == null) {
            return errorMessage;
        }

        Log.e(TAG, "DatabaseError: " + databaseError.getMessage() + " (Code: " + databaseError.getCode() + ")");

        // Kiểm tra nếu là lỗi PERMISSION_DENIED
        if (AuthTokenManager.isPermissionDeniedError(databaseError)) {
            Log.w(TAG, "Phát hiện lỗi PERMISSION_DENIED - Token có thể đã bị invalidate");

            // Xử lý lỗi PERMISSION_DENIED
            if (context != null) {
                AuthTokenManager.handlePermissionDeniedError(context, databaseError);
            }

            return "Phiên đăng nhập đã hết hạn. Đang làm mới...";
        }

        return errorMessage + ": " + databaseError.getMessage();
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
     * Sau khi load xong, lưu vào Room cache
     * ⚡ OPTIMIZED: Load tất cả categories một lần thay vì N+1 queries
     */
    private void loadCategoryNamesForHealthTips(List<HealthTip> healthTips, final HealthTipCallback callback) {
        if (healthTips == null || healthTips.isEmpty()) {
            callback.onSuccess(healthTips);
            return;
        }

        // ⚡ OPTIMIZATION: Load tất cả categories một lần
        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Tạo HashMap để O(1) lookup
                HashMap<String, String> categoryMap = new HashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Category category = snapshot.getValue(Category.class);
                    if (category != null && category.getId() != null) {
                        categoryMap.put(category.getId(), category.getName());
                    }
                }

                // Gán category names từ HashMap
                for (HealthTip healthTip : healthTips) {
                    if (healthTip.getCategoryId() != null) {
                        String categoryName = categoryMap.get(healthTip.getCategoryId());
                        healthTip.setCategoryName(categoryName != null ? categoryName : "Chưa phân loại");
                    } else {
                        healthTip.setCategoryName("Chưa phân loại");
                    }
                }

                // Lưu vào cache và trả về
                saveToCache(healthTips);
                callback.onSuccess(healthTips);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMsg = handleDatabaseError(databaseError, "Error loading categories");
                Log.e(TAG, errorMsg);
                // Vẫn trả về health tips nhưng không có category names
                callback.onSuccess(healthTips);
            }
        });
    }

    /**
     * Lưu danh sách HealthTips vào Room cache
     */
    private void saveToCache(List<HealthTip> healthTips) {
        if (healthTipDao != null && healthTips != null && !healthTips.isEmpty()) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                List<HealthTipEntity> entities = new ArrayList<>();
                for (HealthTip tip : healthTips) {
                    entities.add(HealthTipEntity.fromHealthTip(tip));
                }
                healthTipDao.insertAll(entities);
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
        Log.d(TAG, "getAllHealthTips called");

        // Kiểm tra network
        boolean isOnline = context != null && NetworkUtils.isNetworkAvailable(context);
        Log.d(TAG, "Network status: " + (isOnline ? "ONLINE" : "OFFLINE"));

        // OFFLINE-FIRST STRATEGY:
        // 1. Luôn load từ cache trước (nếu có offline support)
        if (healthTipDao != null) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    // ⚡ OPTIMIZED: Limit 100 items để giảm memory usage và tăng tốc độ
                    List<HealthTipEntity> cachedEntities = healthTipDao.getAllHealthTipsSyncLimited(100);
                    Log.d(TAG, "Cache loaded (limited): " + (cachedEntities != null ? cachedEntities.size() : 0) + " items");

                    if (cachedEntities != null && !cachedEntities.isEmpty()) {
                        // Chuyển đổi Entity sang Model
                        List<HealthTip> cachedTips = new ArrayList<>();
                        for (HealthTipEntity entity : cachedEntities) {
                            cachedTips.add(entity.toHealthTip());
                        }

                        // Trả về cache trên main thread
                        mainHandler.post(() -> {
                            Log.d(TAG, "Returning " + cachedTips.size() + " cached items to UI");
                            callback.onSuccess(cachedTips);
                        });

                        // Nếu offline, dừng ở đây
                        if (!isOnline) {
                            Log.d(TAG, "Offline mode - using cache only");
                            return;
                        }
                    } else {
                        Log.d(TAG, "No cache available");

                        // 🎯 FIX: Nếu không có cache và offline, trả về empty list
                        // Điều này cho phép UI hiển thị empty state thay vì error
                        if (!isOnline) {
                            mainHandler.post(() -> {
                                Log.d(TAG, "📭 Offline with no cache - returning empty list");
                                callback.onSuccess(new ArrayList<>());
                            });
                            return;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading cache: " + e.getMessage(), e);
                }
            });
        } else {
            Log.w(TAG, "Offline support not available (healthTipDao is null)");
        }

        // 2. Nếu online, fetch từ Firebase với LIMIT
        if (isOnline) {
            Log.d(TAG, "Fetching from Firebase with limit...");
            // ⚡ OPTIMIZED: Limit to 100 latest items thay vì load tất cả
            Query limitedQuery = healthTipsRef.orderByChild("createdAt").limitToLast(100);

            limitedQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "Firebase onDataChange (limited): " + dataSnapshot.getChildrenCount() + " items");
                    List<HealthTip> healthTips = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        try {
                            HealthTip healthTip = snapshot.getValue(HealthTip.class);
                            if (healthTip != null) {
                                // Đảm bảo ID được set từ key của Firebase
                                String healthTipId = snapshot.getKey();
                                healthTip.setId(healthTipId);

                            // Validate và set default values nếu cần
                            if (healthTip.getTitle() == null || healthTip.getTitle().trim().isEmpty()) {
                                healthTip.setTitle("Mẹo sức khỏe không tên");
                            }

                            // Kiểm tra nội dung và đảm bảo tương thích ngược
                            if (healthTip.getContent() == null) {
                                // Xử lý trường hợp không có nội dung
                                healthTip.setContent("Nội dung đang được cập nhật");
                            } else if (healthTip.getContent().trim().isEmpty()) {
                                // Nếu nội dung trống
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
                    } catch (Exception e) {
                        // Xử lý lỗi chuyển đổi
                        try {
                            // Truy xuất dữ liệu thủ công từ snapshot để xử lý định dạng mới
                            String id = snapshot.getKey();
                            String title = snapshot.child("title").getValue(String.class);

                            // Xử lý nội dung
                            String content = snapshot.child("content").getValue(String.class);

                            // Xử lý contentBlocks nếu có
                            List<Map<String, Object>> contentBlocksData = null;
                            DataSnapshot contentBlocksSnapshot = snapshot.child("contentBlocks");
                            if (contentBlocksSnapshot.exists()) {
                                contentBlocksData = new ArrayList<>();
                                for (DataSnapshot blockSnapshot : contentBlocksSnapshot.getChildren()) {
                                    Map<String, Object> blockMap = (Map<String, Object>) blockSnapshot.getValue();
                                    if (blockMap != null) {
                                        contentBlocksData.add(blockMap);
                                    }
                                }
                            }

                            String categoryId = snapshot.child("categoryId").getValue(String.class);
                            Integer viewCount = snapshot.child("viewCount").getValue(Integer.class);
                            Integer likeCount = snapshot.child("likeCount").getValue(Integer.class);
                            String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                            Long createdAt = snapshot.child("createdAt").getValue(Long.class);

                            // Tạo đối tượng HealthTip mới
                            HealthTip healthTip = new HealthTip();
                            healthTip.setId(id);
                            healthTip.setTitle(title != null ? title : "Mẹo sức khỏe không tên");
                            healthTip.setContent(content != null ? content : "");
                            if (contentBlocksData != null) {
                                healthTip.setContentBlocks(contentBlocksData);
                            }
                            healthTip.setCategoryId(categoryId);
                            healthTip.setViewCount(viewCount != null ? viewCount : 0);
                            healthTip.setLikeCount(likeCount != null ? likeCount : 0);
                            healthTip.setImageUrl(imageUrl);
                            healthTip.setCreatedAt(createdAt != null ? createdAt : System.currentTimeMillis());

                            // Thêm các trường bổ sung
                            healthTip.setExcerpt(snapshot.child("excerpt").getValue(String.class));
                            healthTip.setStatus(snapshot.child("status").getValue(String.class));
                            healthTip.setAuthor(snapshot.child("author").getValue(String.class));
                            healthTip.setPublishedAt(snapshot.child("publishedAt").getValue(Long.class));
                            healthTip.setUpdatedAt(snapshot.child("updatedAt").getValue(Long.class));

                            // Xử lý tags nếu có
                            DataSnapshot tagsSnapshot = snapshot.child("tags");
                            if (tagsSnapshot.exists()) {
                                List<String> tags = new ArrayList<>();
                                for (DataSnapshot tagSnapshot : tagsSnapshot.getChildren()) {
                                    String tag = tagSnapshot.getValue(String.class);
                                    if (tag != null) {
                                        tags.add(tag);
                                    }
                                }
                                healthTip.setTags(tags);
                            }

                            healthTips.add(healthTip);
                        } catch (Exception innerEx) {
                            // Bỏ qua bài viết này nếu không thể xử lý
                            System.out.println("Không thể xử lý bài viết: " + snapshot.getKey() + " - Lỗi: " + innerEx.getMessage());
                        }
                    }
                }
                    // Load category names cho tất cả health tips
                    loadCategoryNamesForHealthTips(healthTips, callback);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    String errorMsg = handleDatabaseError(databaseError, "Firebase error");
                    Log.e(TAG, errorMsg);
                    // Nếu có cache thì không báo lỗi (vì đã trả về cache rồi)
                    // Chỉ báo lỗi nếu không có cache
                    if (healthTipDao == null) {
                        callback.onError(errorMsg);
                    }
                }
            });
        }
    }

    @Override
    public void getHealthTipDetail(String tipId, final SingleHealthTipCallback callback) {
        if (tipId == null || tipId.trim().isEmpty()) {
            callback.onError("ID mẹo sức khỏe không hợp lệ");
            return;
        }

        Log.d(TAG, "getHealthTipDetail called for ID: " + tipId);

        // Kiểm tra network
        boolean isOnline = context != null && NetworkUtils.isNetworkAvailable(context);
        Log.d(TAG, "Network status: " + (isOnline ? "ONLINE" : "OFFLINE"));

        // 🎯 FIX CRITICAL BUG: Sử dụng flag để tránh callback được gọi nhiều lần
        final boolean[] callbackCalled = {false};

        // 1. Load từ cache trước
        if (healthTipDao != null) {
            Log.d(TAG, "✓ healthTipDao EXISTS for detail, starting executor...");
            AppDatabase.databaseWriteExecutor.execute(() -> {
                Log.d(TAG, "✓ EXECUTOR STARTED for detail: " + tipId);
                try {
                    HealthTipEntity cachedEntity = healthTipDao.getHealthTipByIdSync(tipId);
                    Log.d(TAG, "✓ Detail cache: " + (cachedEntity != null ? "FOUND" : "NOT FOUND") + " for ID: " + tipId);

                    if (cachedEntity != null) {
                        HealthTip cachedTip = cachedEntity.toHealthTip();
                        mainHandler.post(() -> {
                            Log.d(TAG, "✅ Returning cached detail for: " + tipId);
                            callback.onSuccess(cachedTip);
                            callbackCalled[0] = true; // 🎯 Đánh dấu đã callback
                        });

                        if (!isOnline) {
                            Log.d(TAG, "📵 Offline mode - using detail cache only");
                            return;
                        }
                    } else if (!isOnline) {
                        mainHandler.post(() -> {
                            if (!callbackCalled[0]) { // 🎯 Chỉ callback nếu chưa được gọi
                                callback.onError("Không có kết nối mạng và chưa có dữ liệu offline");
                                callbackCalled[0] = true;
                            }
                        });
                        return;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "✗ ERROR in detail executor: " + e.getMessage(), e);
                    e.printStackTrace();
                }
            });
        } else {
            Log.e(TAG, "✗ CRITICAL: healthTipDao is NULL for detail!");
        }

        // 2. Nếu online, fetch từ Firebase để update cache
        if (isOnline) {
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

                        // Load category name và lưu cache
                        loadCategoryNameForSingleHealthTip(healthTip, new SingleHealthTipCallback() {
                            @Override
                            public void onSuccess(HealthTip tip) {
                                // Lưu vào cache
                                saveSingleToCache(tip);

                                // 🎯 FIX: Chỉ callback nếu chưa trả về cache
                                // Nếu đã có cache, không cần callback nữa (tránh UI bị flash)
                                if (!callbackCalled[0]) {
                                    Log.d(TAG, "📡 Returning Firebase detail (no cache): " + tipId);
                                    callback.onSuccess(tip);
                                    callbackCalled[0] = true;
                                } else {
                                    Log.d(TAG, "💾 Firebase data cached silently (already showed cache): " + tipId);
                                }
                            }

                            @Override
                            public void onError(String errorMessage) {
                                // 🎯 FIX: Chỉ callback error nếu chưa có data từ cache
                                if (!callbackCalled[0]) {
                                    callback.onError(errorMessage);
                                    callbackCalled[0] = true;
                                }
                            }
                        });
                    } else {
                        // 🎯 FIX: Chỉ callback error nếu chưa có data từ cache
                        if (!callbackCalled[0]) {
                            callback.onError("Không tìm thấy mẹo sức khỏe với ID: " + tipId);
                            callbackCalled[0] = true;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    String errorMsg = handleDatabaseError(databaseError, "Firebase error in getHealthTipDetail");
                    Log.e(TAG, errorMsg);

                    // 🎯 FIX CRITICAL: KHÔNG callback error nếu đã có cache
                    // Đây là bug chính - Firebase error ghi đè cache result!
                    if (!callbackCalled[0]) {
                        // Chỉ báo lỗi nếu thực sự không có offline support
                        if (healthTipDao == null) {
                            callback.onError(errorMsg);
                            callbackCalled[0] = true;
                        }
                        // Ngược lại: im lặng, vì cache đã hoặc sẽ được load
                        Log.d(TAG, "🔇 Firebase error silenced (cache exists or loading)");
                    }
                }
            });
        }
    }

    /**
     * Lưu một HealthTip vào cache
     */
    private void saveSingleToCache(HealthTip healthTip) {
        if (healthTipDao != null && healthTip != null) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                healthTipDao.insert(HealthTipEntity.fromHealthTip(healthTip));
                Log.d(TAG, "Saved single tip to cache: " + healthTip.getId());
            });
        }
    }

    @Override
    public void getHealthTipsByCategory(String categoryId, final HealthTipCallback callback) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            callback.onError("ID danh mục không hợp lệ");
            return;
        }

        Log.d(TAG, "getHealthTipsByCategory called for category: " + categoryId);

        boolean isOnline = context != null && NetworkUtils.isNetworkAvailable(context);
        Log.d(TAG, "Network status: " + (isOnline ? "ONLINE" : "OFFLINE"));

        // 1. Load từ cache trước
        if (healthTipDao != null) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    List<HealthTipEntity> cachedEntities = healthTipDao.getHealthTipsByCategorySync(categoryId);
                    Log.d(TAG, "Category cache loaded: " + (cachedEntities != null ? cachedEntities.size() : 0) + " items");

                    if (cachedEntities != null && !cachedEntities.isEmpty()) {
                        List<HealthTip> cachedTips = new ArrayList<>();
                        for (HealthTipEntity entity : cachedEntities) {
                            cachedTips.add(entity.toHealthTip());
                        }

                        mainHandler.post(() -> {
                            Log.d(TAG, "Returning " + cachedTips.size() + " category cached items to UI");
                            callback.onSuccess(cachedTips);
                        });

                        if (!isOnline) {
                            Log.d(TAG, "Offline mode - using category cache only");
                            return;
                        }
                    } else if (!isOnline) {
                        // 🎯 FIX: Trả về empty list thay vì error
                        mainHandler.post(() -> {
                            Log.d(TAG, "📭 Offline with no category cache - returning empty list");
                            callback.onSuccess(new ArrayList<>());
                        });
                        return;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading category cache: " + e.getMessage(), e);
                }
            });
        }

        // 2. Nếu online, fetch từ Firebase
        if (isOnline) {
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
                    String errorMsg = handleDatabaseError(databaseError, "Firebase error in getHealthTipsByCategory");
                    Log.e(TAG, errorMsg);
                    if (healthTipDao == null) {
                        callback.onError(errorMsg);
                    }
                }
            });
        }
    }


    @Override
    public void getLatestHealthTips(int limit, final HealthTipCallback callback) {
        Log.d(TAG, "getLatestHealthTips called, limit=" + limit);

        // Kiểm tra network
        boolean isOnline = context != null && NetworkUtils.isNetworkAvailable(context);
        Log.d(TAG, "Network status: " + (isOnline ? "ONLINE" : "OFFLINE"));

        // 1. Load từ cache trước
        if (healthTipDao != null) {
            Log.d(TAG, "✓ healthTipDao EXISTS, starting executor...");
            AppDatabase.databaseWriteExecutor.execute(() -> {
                Log.d(TAG, "✓ EXECUTOR STARTED for latest tips");
                try {
                    List<HealthTipEntity> cachedEntities = healthTipDao.getLatestHealthTipsSync(limit);
                    Log.d(TAG, "✓ Latest cache loaded: " + (cachedEntities != null ? cachedEntities.size() : 0) + " items");

                    if (cachedEntities != null && !cachedEntities.isEmpty()) {
                        List<HealthTip> cachedTips = new ArrayList<>();
                        for (HealthTipEntity entity : cachedEntities) {
                            cachedTips.add(entity.toHealthTip());
                        }

                        mainHandler.post(() -> {
                            Log.d(TAG, "Returning " + cachedTips.size() + " latest cached items to UI");
                            callback.onSuccess(cachedTips);
                        });

                        if (!isOnline) {
                            Log.d(TAG, "Offline mode - using latest cache only");
                            return;
                        }
                    } else if (!isOnline) {
                        // 🎯 FIX: Trả về empty list thay vì error
                        mainHandler.post(() -> {
                            Log.d(TAG, "📭 Offline with no latest cache - returning empty list");
                            callback.onSuccess(new ArrayList<>());
                        });
                        return;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "✗ ERROR in latest tips executor: " + e.getMessage(), e);
                    e.printStackTrace();
                }
            });
        } else {
            Log.e(TAG, "✗ CRITICAL: healthTipDao is NULL for latest tips!");
        }

        // 2. Nếu online, fetch từ Firebase
        if (isOnline) {
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
                    String errorMsg = handleDatabaseError(databaseError, "Firebase error in getLatestHealthTips");
                    Log.e(TAG, errorMsg);
                    if (healthTipDao == null) {
                        callback.onError(errorMsg);
                    }
                }
            });
        }
    }

    @Override
    public void getMostViewedHealthTips(int limit, final HealthTipCallback callback) {
        Log.d(TAG, "getMostViewedHealthTips called, limit=" + limit);

        boolean isOnline = context != null && NetworkUtils.isNetworkAvailable(context);

        // 1. Load từ cache trước
        if (healthTipDao != null) {
            Log.d(TAG, "✓ healthTipDao EXISTS for most viewed, starting executor...");
            AppDatabase.databaseWriteExecutor.execute(() -> {
                Log.d(TAG, "✓ EXECUTOR STARTED for most viewed tips");
                try {
                    List<HealthTipEntity> cachedEntities = healthTipDao.getMostViewedHealthTipsSync(limit);
                    Log.d(TAG, "✓ Most viewed cache loaded: " + (cachedEntities != null ? cachedEntities.size() : 0) + " items");
                    if (cachedEntities != null && !cachedEntities.isEmpty()) {
                        List<HealthTip> cachedTips = new ArrayList<>();
                        for (HealthTipEntity entity : cachedEntities) {
                            cachedTips.add(entity.toHealthTip());
                        }
                        mainHandler.post(() -> callback.onSuccess(cachedTips));

                        if (!isOnline) return;
                    } else if (!isOnline) {
                        // 🎯 FIX: Trả về empty list thay vì error
                        mainHandler.post(() -> {
                            Log.d(TAG, "📭 Offline with no most viewed cache - returning empty list");
                            callback.onSuccess(new ArrayList<>());
                        });
                        return;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "✗ ERROR in most viewed executor: " + e.getMessage(), e);
                    e.printStackTrace();
                }
            });
        } else {
            Log.e(TAG, "✗ CRITICAL: healthTipDao is NULL for most viewed tips!");
        }

        // 2. Nếu online, fetch từ Firebase
        if (isOnline) {
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
                    String errorMsg = handleDatabaseError(databaseError, "Firebase error in getMostViewedHealthTips");
                    Log.e(TAG, errorMsg);
                    if (healthTipDao == null) {
                        callback.onError(errorMsg);
                    }
                }
            });
        }
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
                String errorMsg = handleDatabaseError(databaseError, "Firebase error in getMostLikedHealthTips");
                callback.onError(errorMsg);
            }
        });
    }

    @Override
    public void getRecommendedHealthTips(int limit, final HealthTipCallback callback) {
        // Logic đề xuất: Lấy ngẫu nhiên các bài viết từ nhiều danh mục khác nhau
        // Kết hợp từ các bài viết mới, được xem nhiều và được thích nhiều

        // ⚡ OPTIMIZED: Limit to 200 latest items để tính recommendation score
        // thay vì load tất cả (có thể hàng nghìn items)
        Query recommendQuery = healthTipsRef.orderByChild("createdAt").limitToLast(200);

        recommendQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<HealthTip> allHealthTips = new ArrayList<>();

                // Lấy subset để tính recommendation
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
                String errorMsg = handleDatabaseError(databaseError, "Firebase error in getRecommendedHealthTips");
                callback.onError(errorMsg);
            }
        });
    }

    @Override
    public void getPersonalizedRecommendations(String userId, int limit, final HealthTipCallback callback) {
        if (userId == null || userId.trim().isEmpty()) {
            Log.w(TAG, "UserId is null, falling back to generic recommendations");
            getRecommendedHealthTips(limit, callback);
            return;
        }

        Log.d(TAG, "Getting personalized recommendations for user: " + userId);

        // Gọi API recommendation từ backend
        VercelApiHelper.getInstance(context).getPersonalizedRecommendations(
            userId,
            limit,
            "hybrid", // Sử dụng hybrid algorithm (content + collaborative + trending)
            new VercelApiHelper.ApiCallback() {
                @Override
                public void onSuccess(org.json.JSONObject response) {
                    try {
                        // Parse response
                        org.json.JSONArray recommendationsArray = response.getJSONArray("recommendations");
                        Log.d(TAG, "Received " + recommendationsArray.length() + " personalized recommendations");

                        List<String> tipIds = new ArrayList<>();
                        for (int i = 0; i < recommendationsArray.length(); i++) {
                            org.json.JSONObject rec = recommendationsArray.getJSONObject(i);
                            String healthTipId = rec.getString("healthTipId");
                            tipIds.add(healthTipId);
                        }

                        // Load chi tiết các tips từ Firebase
                        loadHealthTipsByIds(tipIds, callback);
                    } catch (org.json.JSONException e) {
                        Log.e(TAG, "Error parsing recommendations response", e);
                        // Fallback to generic recommendations
                        mainHandler.post(() -> getRecommendedHealthTips(limit, callback));
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error getting personalized recommendations: " + error);
                    // Fallback to generic recommendations
                    mainHandler.post(() -> getRecommendedHealthTips(limit, callback));
                }
            }
        );
    }

    /**
     * Load chi tiết health tips theo danh sách IDs
     */
    private void loadHealthTipsByIds(List<String> tipIds, final HealthTipCallback callback) {
        if (tipIds.isEmpty()) {
            mainHandler.post(() -> callback.onSuccess(new ArrayList<>()));
            return;
        }

        List<HealthTip> loadedTips = new ArrayList<>();
        final int[] loadedCount = {0};

        for (String tipId : tipIds) {
            healthTipsRef.child(tipId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    HealthTip tip = snapshot.getValue(HealthTip.class);
                    if (tip != null) {
                        tip.setId(snapshot.getKey());
                        loadedTips.add(tip);
                    }

                    loadedCount[0]++;
                    if (loadedCount[0] == tipIds.size()) {
                        // Đã load xong tất cả, load category names và trả về
                        loadCategoryNamesForHealthTips(loadedTips, callback);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e(TAG, "Error loading tip: " + tipId + " - " + error.getMessage());
                    loadedCount[0]++;
                    if (loadedCount[0] == tipIds.size()) {
                        loadCategoryNamesForHealthTips(loadedTips, callback);
                    }
                }
            });
        }
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
                String errorMsg = handleDatabaseError(databaseError, "Lỗi khi tìm kiếm");
                callback.onError(errorMsg);
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
                String errorMsg = handleDatabaseError(databaseError, "Lỗi khi lắng nghe dữ liệu");
                callback.onError(errorMsg);
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

        // 🎯 FIX: Kiểm tra network trước - nếu offline thì callback success luôn
        // Không cần báo lỗi vì đây chỉ là analytics, không ảnh hưởng UX
        boolean isOnline = context != null && NetworkUtils.isNetworkAvailable(context);
        if (!isOnline) {
            Log.d(TAG, "Offline mode - skipping view count update for: " + tipId);
            callback.onSuccess(); // Silent success - không block user
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
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to update view count: " + e.getMessage());
                            callback.onSuccess(); // 🎯 FIX: Callback success thay vì error
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMsg = handleDatabaseError(databaseError, "View count update cancelled");
                Log.e(TAG, errorMsg);
                callback.onSuccess(); // 🎯 FIX: Callback success thay vì error
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
                String errorMsg = handleDatabaseError(databaseError, "Lỗi khi tải danh sách yêu thích");
                callback.onError(errorMsg);
            }
        });
    }

    @Override
    public void getDailyRecommendedHealthTips(String date, int limit, final HealthTipCallback callback) {
        if (date == null || date.isEmpty()) {
            callback.onError("Ngày không hợp lệ");
            return;
        }

        Log.d(TAG, "getDailyRecommendedHealthTips called with limit: " + limit);

        // Kiểm tra network
        boolean isOnline = context != null && NetworkUtils.isNetworkAvailable(context);
        Log.d(TAG, "Network status for recommended: " + (isOnline ? "ONLINE" : "OFFLINE"));

        // 1. Load từ cache trước (offline-first)
        if (healthTipDao != null) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    // Lấy recommended tips từ cache (sorted by recommendation_score)
                    List<HealthTipEntity> cachedEntities = healthTipDao.getLatestHealthTipsSync(limit);
                    Log.d(TAG, "Recommended cache: " + (cachedEntities != null ? cachedEntities.size() : 0) + " items");

                    if (cachedEntities != null && !cachedEntities.isEmpty()) {
                        List<HealthTip> cachedTips = new ArrayList<>();
                        for (HealthTipEntity entity : cachedEntities) {
                            cachedTips.add(entity.toHealthTip());
                        }
                        mainHandler.post(() -> {
                            Log.d(TAG, "Returning " + cachedTips.size() + " recommended tips from cache");
                            callback.onSuccess(cachedTips);
                        });

                        if (!isOnline) {
                            Log.d(TAG, "Offline mode - using recommended cache only");
                            return;
                        }
                    } else {
                        Log.d(TAG, "No recommended cache available");

                        // 🎯 FIX: Trả về empty list thay vì error khi offline không có cache
                        if (!isOnline) {
                            mainHandler.post(() -> {
                                Log.d(TAG, "📭 Offline with no recommended cache - returning empty list");
                                callback.onSuccess(new ArrayList<>());
                            });
                            return;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading recommended cache: " + e.getMessage(), e);
                }
            });
        }

        // 2. Nếu online, fetch từ Firebase
        if (!isOnline) {
            return; // Đã xử lý offline ở trên
        }

        Log.d(TAG, "Fetching recommended tips from Firebase...");
        // ⚡ OPTIMIZED: Lấy 200 bài viết mới nhất thay vì tất cả
        Query recommendQuery = healthTipsRef.orderByChild("createdAt").limitToLast(200);

        recommendQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<HealthTip> allHealthTips = new ArrayList<>();

                // Lấy subset để tính recommendation
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
                String errorMsg = handleDatabaseError(databaseError, "Lỗi khi tải dữ liệu");
                callback.onError(errorMsg);
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
