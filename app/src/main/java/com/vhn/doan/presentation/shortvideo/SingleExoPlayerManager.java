package com.vhn.doan.presentation.shortvideo;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.vhn.doan.data.ShortVideo;

import java.util.List;

/**
 * Manager quản lý 1 ExoPlayer duy nhất cho toàn bộ feed video ngắn
 * Giải quyết vấn đề "video #2 phải lướt qua rồi lướt lại mới play"
 */
public class SingleExoPlayerManager {
    private static final String TAG = "SingleExoPlayerManager";

    private ExoPlayer exoPlayer;
    private PlayerView currentPlayerView;
    private int currentPosition = -1;
    private List<ShortVideo> videos;
    private VideoPlayerListener listener;

    public interface VideoPlayerListener {
        void onVideoReady(int position);
        void onVideoEnded(int position);
        void onVideoError(int position, Exception error);
    }

    public SingleExoPlayerManager(@NonNull Context context) {
        initializePlayer(context);
    }

    private void initializePlayer(@NonNull Context context) {
        if (exoPlayer != null) {
            exoPlayer.release();
        }

        exoPlayer = new ExoPlayer.Builder(context).build();
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Log.d(TAG, "Playback state changed: " + playbackState + " for position: " + currentPosition);

                switch (playbackState) {
                    case Player.STATE_READY:
                        if (listener != null && currentPosition != -1) {
                            listener.onVideoReady(currentPosition);
                        }
                        break;
                    case Player.STATE_ENDED:
                        if (listener != null && currentPosition != -1) {
                            listener.onVideoEnded(currentPosition);
                        }
                        break;
                }
            }

            @Override
            public void onPlayerError(@NonNull com.google.android.exoplayer2.PlaybackException error) {
                Log.e(TAG, "Player error for position " + currentPosition + ": " + error.getMessage());
                if (listener != null && currentPosition != -1) {
                    listener.onVideoError(currentPosition, new Exception(error));
                }
            }
        });
    }

    /**
     * Gắn player vào PlayerView mới và phát video tại vị trí chỉ định
     */
    public void attachToPlayerView(@NonNull PlayerView playerView, @NonNull List<ShortVideo> videos, int position) {
        Log.d(TAG, "Attaching to PlayerView at position: " + position);

        // Detach khỏi PlayerView cũ
        if (currentPlayerView != null) {
            currentPlayerView.setPlayer(null);
        }

        // Gắn vào PlayerView mới
        currentPlayerView = playerView;
        currentPlayerView.setPlayer(exoPlayer);

        // Cập nhật danh sách video và vị trí
        this.videos = videos;
        this.currentPosition = position;

        // Load và phát video mới
        if (position >= 0 && position < videos.size()) {
            ShortVideo video = videos.get(position);
            String videoUrl = video.getOptimizedVideoUrl();

            if (videoUrl != null && !videoUrl.isEmpty()) {
                Log.d(TAG, "Loading video URL: " + videoUrl);
                MediaItem mediaItem = MediaItem.fromUri(videoUrl);
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.prepare();
                exoPlayer.setPlayWhenReady(true);
            } else {
                Log.e(TAG, "Video URL is null or empty for position: " + position);
                if (listener != null) {
                    listener.onVideoError(position, new Exception("Video URL is null or empty"));
                }
            }
        }
    }

    /**
     * Detach player khỏi PlayerView hiện tại
     */
    public void detachFromPlayerView() {
        Log.d(TAG, "Detaching from PlayerView");

        if (currentPlayerView != null) {
            currentPlayerView.setPlayer(null);
            currentPlayerView = null;
        }

        exoPlayer.pause();
        currentPosition = -1;
    }

    /**
     * Phát video
     */
    public void play() {
        if (exoPlayer != null) {
            Log.d(TAG, "Playing video at position: " + currentPosition);
            exoPlayer.setPlayWhenReady(true);
        }
    }

    /**
     * Tạm dừng video
     */
    public void pause() {
        if (exoPlayer != null) {
            Log.d(TAG, "Pausing video at position: " + currentPosition);
            exoPlayer.setPlayWhenReady(false);
        }
    }

    /**
     * Kiểm tra xem có đang phát video không
     */
    public boolean isPlaying() {
        return exoPlayer != null && exoPlayer.getPlayWhenReady() && exoPlayer.getPlaybackState() == Player.STATE_READY;
    }

    /**
     * Lấy vị trí hiện tại đang phát
     */
    public int getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Thiết lập listener
     */
    public void setVideoPlayerListener(@Nullable VideoPlayerListener listener) {
        this.listener = listener;
    }

    /**
     * Giải phóng tài nguyên
     */
    public void release() {
        Log.d(TAG, "Releasing ExoPlayer resources");

        if (currentPlayerView != null) {
            currentPlayerView.setPlayer(null);
            currentPlayerView = null;
        }

        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }

        currentPosition = -1;
        videos = null;
        listener = null;
    }

    /**
     * Lấy thông tin debug
     */
    public String getDebugInfo() {
        if (exoPlayer == null) {
            return "ExoPlayer: null";
        }

        return String.format("ExoPlayer - Position: %d, Playing: %b, State: %d",
                currentPosition,
                exoPlayer.getPlayWhenReady(),
                exoPlayer.getPlaybackState());
    }
}
