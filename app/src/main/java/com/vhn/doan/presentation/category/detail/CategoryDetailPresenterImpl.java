package com.vhn.doan.presentation.category.detail;

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
 * Implementation của CategoryDetailPresenter
 */
public class CategoryDetailPresenterImpl implements CategoryDetailPresenter {

    private CategoryDetailView view;
    private FirebaseManager firebaseManager;

    /**
     * Constructor nhận FirebaseManager từ DI
     * @param firebaseManager Manager quản lý giao tiếp với Firebase
     */
    public CategoryDetailPresenterImpl(FirebaseManager firebaseManager) {
        this.firebaseManager = firebaseManager;
    }

    @Override
    public void attachView(CategoryDetailView view) {
        this.view = view;
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
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        view.showLoading(false);

                        if (dataSnapshot.exists()) {
                            List<HealthTip> healthTips = new ArrayList<>();

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                HealthTip tip = snapshot.getValue(HealthTip.class);
                                if (tip != null) {
                                    tip.setId(snapshot.getKey());
                                    healthTips.add(tip);
                                }
                            }

                            if (healthTips.isEmpty()) {
                                view.showEmptyView();
                            } else {
                                view.displayHealthTips(healthTips);
                            }
                        } else {
                            view.showEmptyView();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        view.showLoading(false);
                        view.showError("Không thể tải danh sách mẹo: " + error.getMessage());
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
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Category category = dataSnapshot.getValue(Category.class);
                            if (category != null) {
                                category.setId(dataSnapshot.getKey());
                                view.setCategoryTitle(category.getName());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
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

    @Override
    public void detachView() {
        this.view = null;
    }
}
