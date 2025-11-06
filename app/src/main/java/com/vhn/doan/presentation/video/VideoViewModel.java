package com.vhn.doan.presentation.video;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vhn.doan.data.local.AppDatabase;
import com.vhn.doan.data.local.entity.VideoEntity;
import com.vhn.doan.presentation.base.BaseViewModel;

import java.util.List;

/**
 * VideoViewModel - ViewModel cho VideoFragment
 * Quản lý video feed với caching và pagination
 */
public class VideoViewModel extends BaseViewModel {

    private static final String TAG = "VideoViewModel";

    // LiveData từ database
    private final LiveData<List<VideoEntity>> allVideos;
    private final LiveData<List<VideoEntity>> likedVideos;
    private final LiveData<List<VideoEntity>> trendingVideos;

    // Current video position (cho swipe navigation)
    private final MutableLiveData<Integer> currentPosition = new MutableLiveData<>(0);

    // Refresh state
    private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>(false);

    // Video playback state
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);

    public VideoViewModel(@NonNull Application application) {
        super(application);

        // Initialize LiveData từ database
        allVideos = database.videoDao().getAllVideos();
        likedVideos = database.videoDao().getLikedVideos();
        trendingVideos = database.videoDao().getTrendingVideos(20);

        Log.d(TAG, "VideoViewModel initialized");
    }

    /**
     * Refresh video feed từ server
     */
    public void refreshVideos() {
        isRefreshing.setValue(true);

        // TODO: Implement fetch từ Firebase
        // 1. Fetch videos từ Firebase
        // 2. Save vào Room database
        // 3. LiveData sẽ tự động update UI

        // Giả lập delay
        new android.os.Handler().postDelayed(() -> {
            isRefreshing.setValue(false);
            Log.d(TAG, "Videos refreshed");
        }, 1000);
    }

    /**
     * Toggle like status cho video
     */
    public void toggleLike(String videoId, boolean isLiked) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            database.videoDao().updateLikeStatus(videoId, isLiked);

            // Update like count
            VideoEntity video = database.videoDao().getVideoByIdSync(videoId);
            if (video != null) {
                long newLikeCount = isLiked ? video.getLikeCount() + 1 : video.getLikeCount() - 1;
                database.videoDao().updateLikeCount(videoId, Math.max(0, newLikeCount));
            }

            Log.d(TAG, "Updated like status: " + videoId + " = " + isLiked);
        });
    }

    /**
     * Increment view count cho video
     */
    public void incrementViewCount(String videoId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            VideoEntity video = database.videoDao().getVideoByIdSync(videoId);
            if (video != null) {
                database.videoDao().updateViewCount(videoId, video.getViewCount() + 1);
                Log.d(TAG, "Incremented view count: " + videoId);
            }
        });
    }

    /**
     * Set current video position
     */
    public void setCurrentPosition(int position) {
        currentPosition.setValue(position);
    }

    /**
     * Move to next video
     */
    public void nextVideo() {
        Integer current = currentPosition.getValue();
        if (current != null) {
            currentPosition.setValue(current + 1);
        }
    }

    /**
     * Move to previous video
     */
    public void previousVideo() {
        Integer current = currentPosition.getValue();
        if (current != null && current > 0) {
            currentPosition.setValue(current - 1);
        }
    }

    /**
     * Set video playing state
     */
    public void setPlaying(boolean playing) {
        isPlaying.setValue(playing);
    }

    /**
     * Toggle play/pause
     */
    public void togglePlayPause() {
        Boolean playing = isPlaying.getValue();
        isPlaying.setValue(playing == null || !playing);
    }

    // Getters cho LiveData
    public LiveData<List<VideoEntity>> getAllVideos() {
        return allVideos;
    }

    public LiveData<List<VideoEntity>> getLikedVideos() {
        return likedVideos;
    }

    public LiveData<List<VideoEntity>> getTrendingVideos() {
        return trendingVideos;
    }

    public LiveData<Integer> getCurrentPosition() {
        return currentPosition;
    }

    public LiveData<Boolean> getIsRefreshing() {
        return isRefreshing;
    }

    public LiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Pause video khi ViewModel bị destroy
        setPlaying(false);
        Log.d(TAG, "VideoViewModel cleared");
    }
}
