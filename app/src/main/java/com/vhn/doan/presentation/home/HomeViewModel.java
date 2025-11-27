package com.vhn.doan.presentation.home;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.vhn.doan.data.local.AppDatabase;
import com.vhn.doan.data.local.entity.CategoryEntity;
import com.vhn.doan.data.local.entity.HealthTipEntity;
import com.vhn.doan.presentation.base.BaseViewModel;

import java.util.List;

/**
 * HomeViewModel - ViewModel cho HomeFragment
 * Quản lý data cho màn hình Home với caching
 */
public class HomeViewModel extends BaseViewModel {

    private static final String TAG = "HomeViewModel";

    // LiveData từ database
    private final LiveData<List<CategoryEntity>> categories;
    private final LiveData<List<HealthTipEntity>> recommendedHealthTips;
    private final LiveData<List<HealthTipEntity>> latestHealthTips;
    private final LiveData<List<HealthTipEntity>> mostViewedHealthTips;
    private final LiveData<List<HealthTipEntity>> mostLikedHealthTips;

    // MediatorLiveData để combine multiple sources
    private final MediatorLiveData<Boolean> hasData = new MediatorLiveData<>();

    // MutableLiveData cho refresh state
    private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>(false);

    public HomeViewModel(@NonNull Application application) {
        super(application);

        // Initialize LiveData từ database với LIMIT để tối ưu performance
        categories = database.categoryDao().getAllCategories();
        recommendedHealthTips = database.healthTipDao().getRecommendedHealthTips(10);
        latestHealthTips = database.healthTipDao().getAllHealthTipsLimited(50); // ⚡ OPTIMIZED: Limit 50 items
        mostViewedHealthTips = database.healthTipDao().getMostViewedHealthTips(10);
        mostLikedHealthTips = database.healthTipDao().getMostLikedHealthTips(10);

        // Setup MediatorLiveData để check nếu có data
        setupHasDataObserver();

        Log.d(TAG, "HomeViewModel initialized with optimized queries");
    }

    /**
     * Setup observer để check nếu có data trong cache
     */
    private void setupHasDataObserver() {
        hasData.addSource(categories, categoriesList -> {
            hasData.setValue(categoriesList != null && !categoriesList.isEmpty());
        });
    }

    /**
     * Refresh data từ server
     * Note: Cần implement logic fetch từ Firebase và save vào Room
     */
    public void refreshData() {
        isRefreshing.setValue(true);

        // TODO: Implement fetch từ Firebase
        // 1. Fetch categories từ Firebase
        // 2. Fetch health tips từ Firebase
        // 3. Save vào Room database
        // 4. LiveData sẽ tự động update UI

        // Giả lập delay
        new android.os.Handler().postDelayed(() -> {
            isRefreshing.setValue(false);
            Log.d(TAG, "Data refreshed");
        }, 1000);
    }

    /**
     * Toggle favorite status cho health tip
     */
    public void toggleFavorite(String healthTipId, boolean isFavorite) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            database.healthTipDao().updateFavoriteStatus(healthTipId, isFavorite);
            Log.d(TAG, "Updated favorite status: " + healthTipId + " = " + isFavorite);
        });
    }

    /**
     * Toggle like status cho health tip
     */
    public void toggleLike(String healthTipId, boolean isLiked) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            database.healthTipDao().updateLikeStatus(healthTipId, isLiked);
            Log.d(TAG, "Updated like status: " + healthTipId + " = " + isLiked);
        });
    }

    /**
     * Increment view count cho health tip
     */
    public void incrementViewCount(String healthTipId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            HealthTipEntity tip = database.healthTipDao().getHealthTipByIdSync(healthTipId);
            if (tip != null) {
                database.healthTipDao().updateViewCount(healthTipId, tip.getViewCount() + 1);
                Log.d(TAG, "Incremented view count: " + healthTipId);
            }
        });
    }

    // Getters cho LiveData
    public LiveData<List<CategoryEntity>> getCategories() {
        return categories;
    }

    public LiveData<List<HealthTipEntity>> getRecommendedHealthTips() {
        return recommendedHealthTips;
    }

    public LiveData<List<HealthTipEntity>> getLatestHealthTips() {
        return latestHealthTips;
    }

    public LiveData<List<HealthTipEntity>> getMostViewedHealthTips() {
        return mostViewedHealthTips;
    }

    public LiveData<List<HealthTipEntity>> getMostLikedHealthTips() {
        return mostLikedHealthTips;
    }

    public LiveData<Boolean> getHasData() {
        return hasData;
    }

    public LiveData<Boolean> getIsRefreshing() {
        return isRefreshing;
    }
}
