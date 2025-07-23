package com.vhn.doan.presentation.category.detail;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.data.Category;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.services.FirebaseManager;
import com.vhn.doan.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation của CategoryDetailListPresenter
 */
public class CategoryDetailListPresenterImpl implements CategoryDetailListPresenter {

    private CategoryDetailListView view;
    private FirebaseManager firebaseManager;

    /**
     * Constructor nhận FirebaseManager từ DI
     * @param firebaseManager Manager quản lý giao tiếp với Firebase
     */
    public CategoryDetailListPresenterImpl(FirebaseManager firebaseManager) {
        this.firebaseManager = firebaseManager;
    }

    @Override
    public void attachView(CategoryDetailListView view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void loadHealthTipsByCategory(String categoryId) {
        if (view == null) return;

        view.showLoading(true);

        firebaseManager.getDatabaseReference()
                .child(Constants.HEALTH_TIPS_REF)
                .orderByChild("categoryId")
                .equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Null check quan trọng trong callback - sử dụng local reference để tránh race condition
                        CategoryDetailListView currentView = view;
                        if (currentView == null) return;

                        currentView.showLoading(false);

                        if (dataSnapshot.exists()) {
                            List<HealthTip> healthTips = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                HealthTip healthTip = snapshot.getValue(HealthTip.class);
                                if (healthTip != null) {
                                    healthTip.setId(snapshot.getKey());
                                    healthTips.add(healthTip);
                                }
                            }

                            // Double check view vẫn còn tồn tại trước khi cập nhật UI
                            if (view != null) {
                                view.displayHealthTips(healthTips);
                            }
                        } else {
                            // Double check view vẫn còn tồn tại trước khi hiển thị empty state
                            if (view != null) {
                                view.showEmptyState();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Null check quan trọng trong callback onCancelled
                        CategoryDetailListView currentView = view;
                        if (currentView == null) return;

                        currentView.showLoading(false);
                        currentView.showError("Lỗi khi tải dữ liệu: " + databaseError.getMessage());
                    }
                });
    }

    @Override
    public void loadCategoryDetails(String categoryId) {
        if (view == null) return;

        firebaseManager.getDatabaseReference()
                .child(Constants.CATEGORIES_REF)
                .child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Null check quan trọng trong callback
                        if (view == null) return;

                        if (dataSnapshot.exists()) {
                            Category category = dataSnapshot.getValue(Category.class);
                            if (category != null) {
                                category.setId(dataSnapshot.getKey());
                                view.displayCategoryDetails(category);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Null check quan trọng trong callback
                        if (view == null) return;

                        view.showError("Không thể tải thông tin danh mục: " + error.getMessage());
                    }
                });
    }

    @Override
    public void onHealthTipSelected(String healthTipId) {
        if (view == null) return;

        // Chuyển đến màn hình chi tiết mẹo sức khỏe
        view.navigateToHealthTipDetails(healthTipId);
    }
}
