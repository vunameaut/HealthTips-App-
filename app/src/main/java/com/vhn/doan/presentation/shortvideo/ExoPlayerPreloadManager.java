package com.vhn.doan.presentation.shortvideo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.vhn.doan.data.ShortVideo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phiên bản đơn giản hóa của ExoPlayerPreloadManager
 * Tập trung vào việc tạo player ổn định trước
 */
public class ExoPlayerPreloadManager {
    private static final String TAG = "ExoPlayerPreloadManager";
    private static final int PRELOAD_AHEAD = 1; // Giảm xuống 1 để ổn định hơn

    private final Context context;
    private final Map<Integer, ExoPlayer> preloadedPlayers = new HashMap<>();
    private final Handler mainHandler;
    private ExoPlayer currentPlayer;
    private int currentPosition = -1;
    private boolean isEnabled = true; // Flag để tắt preload nếu cần

    public ExoPlayerPreloadManager(Context context) {
        this.context = context.getApplicationContext();
        this.mainHandler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "ExoPlayerPreloadManager initialized");
    }

    /**
     * Thiết lập video hiện tại - phiên bản đơn giản
     */
    public ExoPlayer setupCurrentVideo(List<ShortVideo> videos, int position) {
        Log.d(TAG, "Setting up current video at position: " + position);

        if (!isEnabled || position < 0 || position >= videos.size()) {
            Log.w(TAG, "Preload disabled or invalid position: " + position);
            return null;
        }

        try {
            // Kiểm tra xem có player preload không
            if (preloadedPlayers.containsKey(position)) {
                Log.d(TAG, "Using preloaded player for position: " + position);
                currentPlayer = preloadedPlayers.remove(position);
                currentPosition = position;

                // Trigger preload background sau khi setup xong
                schedulePreload(videos, position);
                return currentPlayer;
            }

            // Nếu không có preload, tạo player mới ngay lập tức
            Log.d(TAG, "Creating new player for position: " + position);
            releaseCurrentPlayer();

            currentPlayer = createPlayerSync(videos.get(position).getOptimizedVideoUrl());
            currentPosition = position;

            // Trigger preload background
            schedulePreload(videos, position);
            return currentPlayer;

        } catch (Exception e) {
            Log.e(TAG, "Error in setupCurrentVideo: " + e.getMessage());
            isEnabled = false; // Tắt preload nếu có lỗi
            return null;
        }
    }

    /**
     * Tạo player đồng bộ để tránh delay
     */
    private ExoPlayer createPlayerSync(String videoUrl) {
        try {
            if (videoUrl == null || videoUrl.isEmpty()) {
                Log.e(TAG, "Invalid video URL");
                return null;
            }

            ExoPlayer player = new ExoPlayer.Builder(context).build();
            MediaItem item = MediaItem.fromUri(videoUrl);
            player.setMediaItem(item);
            player.setRepeatMode(Player.REPEAT_MODE_ONE);
            player.prepare();

            Log.d(TAG, "Created sync player for URL: " + videoUrl);
            return player;
        } catch (Exception e) {
            Log.e(TAG, "Error creating sync player: " + e.getMessage());
            return null;
        }
    }

    /**
     * Tạo preload cho các video xung quanh vị trí hiện tại
     */
    private void schedulePreloadInternal(List<ShortVideo> videos, int currentPos) {
        if (!isEnabled) return;

        mainHandler.post(() -> {
            try {
                // Preload video tiếp theo
                int nextPos = currentPos + 1;
                if (nextPos < videos.size() && !preloadedPlayers.containsKey(nextPos)) {
                    createPreloadPlayer(videos.get(nextPos).getOptimizedVideoUrl(), nextPos);
                }

                // Preload video trước đó (nếu cần)
                int prevPos = currentPos - 1;
                if (prevPos >= 0 && !preloadedPlayers.containsKey(prevPos)) {
                    createPreloadPlayer(videos.get(prevPos).getOptimizedVideoUrl(), prevPos);
                }

                // Cleanup các player cũ không cần thiết
                cleanupOldPlayers(currentPos);
            } catch (Exception e) {
                Log.e(TAG, "Error in preload scheduling: " + e.getMessage());
            }
        });
    }

    /**
     * Public method để ShortVideoAdapter có thể gọi
     */
    public void schedulePreload(List<ShortVideo> videos, int currentPos) {
        schedulePreloadInternal(videos, currentPos);
    }

    private void createPreloadPlayer(String videoUrl, int position) {
        try {
            if (videoUrl == null || videoUrl.isEmpty()) {
                Log.w(TAG, "Skipping preload for position " + position + " - invalid URL");
                return;
            }

            ExoPlayer player = new ExoPlayer.Builder(context).build();
            MediaItem item = MediaItem.fromUri(videoUrl);
            player.setMediaItem(item);
            player.setRepeatMode(Player.REPEAT_MODE_ONE);
            player.prepare();

            preloadedPlayers.put(position, player);
            Log.d(TAG, "Preloaded player for position: " + position);
        } catch (Exception e) {
            Log.e(TAG, "Error creating preload player for position " + position + ": " + e.getMessage());
        }
    }

    private void cleanupOldPlayers(int currentPos) {
        // Remove players that are too far from current position
        preloadedPlayers.entrySet().removeIf(entry -> {
            int pos = entry.getKey();
            if (Math.abs(pos - currentPos) > PRELOAD_AHEAD) {
                try {
                    entry.getValue().release();
                    Log.d(TAG, "Released old preload player at position: " + pos);
                } catch (Exception e) {
                    Log.w(TAG, "Error releasing old player: " + e.getMessage());
                }
                return true;
            }
            return false;
        });
    }

    /**
     * Release player hiện tại
     */
    private void releaseCurrentPlayer() {
        if (currentPlayer != null) {
            try {
                currentPlayer.release();
                Log.d(TAG, "Released current player");
            } catch (Exception e) {
                Log.w(TAG, "Error releasing current player: " + e.getMessage());
            }
            currentPlayer = null;
        }
        currentPosition = -1;
    }

    /**
     * Release tất cả resources
     */
    public void releaseAllResources() {
        Log.d(TAG, "Releasing all resources");

        releaseCurrentPlayer();

        for (Map.Entry<Integer, ExoPlayer> entry : preloadedPlayers.entrySet()) {
            try {
                entry.getValue().release();
            } catch (Exception e) {
                Log.w(TAG, "Error releasing preload player: " + e.getMessage());
            }
        }
        preloadedPlayers.clear();

        isEnabled = true; // Reset enable flag
    }

    /**
     * Lấy player hiện tại
     */
    public ExoPlayer getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Lấy vị trí hiện tại
     */
    public int getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Kiểm tra xem có player preload cho vị trí này không
     */
    public boolean hasPreloadedPlayer(int position) {
        return preloadedPlayers.containsKey(position);
    }

    /**
     * Lấy số lượng player đã preload
     */
    public int getPreloadedCount() {
        return preloadedPlayers.size();
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        if (!enabled) {
            releaseAllResources();
        }
    }
}
