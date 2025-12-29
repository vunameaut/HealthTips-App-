package com.vhn.doan.data.repository;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.data.Category;
import com.vhn.doan.utils.AuthTokenManager;
import com.vhn.doan.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Triển khai CategoryRepository sử dụng Firebase Realtime Database
 */
public class CategoryRepositoryImpl implements CategoryRepository {

    private static final String TAG = "CategoryRepoImpl";
    private final FirebaseDatabase database;
    private final DatabaseReference categoryRef;
    private final Context context;

    /**
     * Constructor với Context
     */
    public CategoryRepositoryImpl(Context context) {
        this.context = context != null ? context.getApplicationContext() : null;
        database = FirebaseDatabase.getInstance();
        categoryRef = database.getReference(Constants.CATEGORIES_REF);
    }

    /**
     * Constructor mặc định (để tương thích ngược)
     * @deprecated Sử dụng constructor với Context thay thế
     */
    @Deprecated
    public CategoryRepositoryImpl() {
        this.context = null;
        database = FirebaseDatabase.getInstance();
        categoryRef = database.getReference(Constants.CATEGORIES_REF);
    }

    /**
     * Helper method để xử lý DatabaseError và kiểm tra PERMISSION_DENIED
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

    @Override
    public void getAllCategories(final CategoryCallback callback) {
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Category> categories = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Category category = snapshot.getValue(Category.class);
                    if (category != null) {
                        // Đảm bảo ID được set từ key của Firebase
                        String categoryId = snapshot.getKey();
                        category.setId(categoryId);

                        // Kiểm tra và đảm bảo dữ liệu hợp lệ
                        if (category.getName() == null || category.getName().trim().isEmpty()) {
                            category.setName("Danh mục không tên");
                        }
                        if (category.getDescription() == null) {
                            category.setDescription("");
                        }
                        if (category.getCreatedAt() <= 0) {
                            category.setCreatedAt(System.currentTimeMillis());
                        }

                        categories.add(category);
                    }
                }
                callback.onSuccess(categories);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMsg = handleDatabaseError(databaseError, "Lỗi khi tải danh mục");
                callback.onError(errorMsg);
            }
        });
    }

    @Override
    public void getCategoryById(String categoryId, final CategoryCallback callback) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            callback.onError("ID danh mục không hợp lệ");
            return;
        }

        categoryRef.child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Category category = dataSnapshot.getValue(Category.class);
                if (category != null) {
                    // Đảm bảo ID được set chính xác
                    category.setId(dataSnapshot.getKey());

                    // Validate dữ liệu
                    if (category.getName() == null || category.getName().trim().isEmpty()) {
                        category.setName("Danh mục không tên");
                    }
                    if (category.getDescription() == null) {
                        category.setDescription("");
                    }

                    callback.onSingleCategoryLoaded(category);
                } else {
                    callback.onError("Không tìm thấy danh mục với ID: " + categoryId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMsg = handleDatabaseError(databaseError, "Lỗi khi tải danh mục");
                callback.onError(errorMsg);
            }
        });
    }

    @Override
    public void getCategoriesByActiveStatus(boolean isActive, final CategoryCallback callback) {
        Query query = categoryRef.orderByChild("active").equalTo(isActive);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Category> categories = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Category category = snapshot.getValue(Category.class);
                    if (category != null) {
                        category.setId(snapshot.getKey());
                        categories.add(category);
                    }
                }
                callback.onSuccess(categories);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMsg = handleDatabaseError(databaseError, "Lỗi khi tải danh mục theo trạng thái");
                callback.onError(errorMsg);
            }
        });
    }

    @Override
    public void addCategory(final Category category, final CategoryOperationCallback callback) {
        String key = categoryRef.push().getKey();
        if (key != null) {
            category.setId(key);
            categoryRef.child(key).setValue(category)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(key))
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        } else {
            callback.onError("Không thể tạo ID cho danh mục mới");
        }
    }

    @Override
    public void updateCategory(final Category category, final CategoryOperationCallback callback) {
        if (category.getId() == null) {
            callback.onError("ID của danh mục không hợp lệ");
            return;
        }

        Map<String, Object> categoryValues = new HashMap<>();
        categoryValues.put("name", category.getName());
        categoryValues.put("description", category.getDescription());
        categoryValues.put("iconUrl", category.getIconUrl());
        categoryValues.put("active", category.isActive());

        categoryRef.child(category.getId()).updateChildren(categoryValues)
                .addOnSuccessListener(aVoid -> callback.onSuccess(category.getId()))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void deleteCategory(final String categoryId, final CategoryOperationCallback callback) {
        categoryRef.child(categoryId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess(categoryId))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void updateCategoryActiveStatus(String categoryId, boolean isActive, final CategoryOperationCallback callback) {
        categoryRef.child(categoryId).child("active").setValue(isActive)
                .addOnSuccessListener(aVoid -> callback.onSuccess(categoryId))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void updateCategoryOrder(String categoryId, int order, CategoryOperationCallback callback) {
        categoryRef.child(categoryId).child("order").setValue(order)
                .addOnSuccessListener(aVoid -> callback.onSuccess(categoryId))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public Object listenToCategories(final CategoryCallback callback) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Category> categories = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Category category = snapshot.getValue(Category.class);
                    if (category != null) {
                        category.setId(snapshot.getKey());
                        categories.add(category);
                    }
                }
                callback.onSuccess(categories);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMsg = handleDatabaseError(databaseError, "Lỗi khi lắng nghe danh mục");
                callback.onError(errorMsg);
            }
        };

        categoryRef.addValueEventListener(listener);
        return listener;
    }

    @Override
    public void removeListener(Object listener) {
        if (listener instanceof ValueEventListener) {
            categoryRef.removeEventListener((ValueEventListener) listener);
        }
    }
}
