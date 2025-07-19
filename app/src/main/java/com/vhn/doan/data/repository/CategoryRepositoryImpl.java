package com.vhn.doan.data.repository;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.data.Category;
import com.vhn.doan.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Triển khai CategoryRepository sử dụng Firebase Realtime Database
 */
public class CategoryRepositoryImpl implements CategoryRepository {

    private final FirebaseDatabase database;
    private final DatabaseReference categoryRef;

    /**
     * Constructor mặc định
     */
    public CategoryRepositoryImpl() {
        database = FirebaseDatabase.getInstance();
        categoryRef = database.getReference(Constants.CATEGORIES_REF);
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
                        category.setId(snapshot.getKey());
                        categories.add(category);
                    }
                }
                callback.onSuccess(categories);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    @Override
    public void getCategoryById(String categoryId, final CategoryCallback callback) {
        categoryRef.child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Category category = dataSnapshot.getValue(Category.class);
                if (category != null) {
                    category.setId(dataSnapshot.getKey());
                    callback.onSingleCategoryLoaded(category);
                } else {
                    callback.onError("Không tìm thấy danh mục");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
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
                callback.onError(databaseError.getMessage());
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
                callback.onError(databaseError.getMessage());
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
