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

        // Cấu hình ExoPlayer với các tùy chọn tối ưu cho video
        exoPlayer = new ExoPlayer.Builder(context)
                .setSeekBackIncrementMs(5000)
                .setSeekForwardIncrementMs(15000)
                .build();

        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);

        // Cấu hình video scaling để fit PlayerView
        exoPlayer.setVideoScalingMode(com.google.android.exoplayer2.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Log.d(TAG, "Playback state changed: " + getPlaybackStateString(playbackState) + " for position: " + currentPosition);

                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                        Log.d(TAG, "⏳ Video is buffering...");
                        break;
                    case Player.STATE_READY:
                        Log.d(TAG, "✅ Video is ready to play");
                        if (listener != null && currentPosition != -1) {
                            listener.onVideoReady(currentPosition);
                        }
                        break;
                    case Player.STATE_ENDED:
                        Log.d(TAG, "🔄 Video playback ended");
                        if (listener != null && currentPosition != -1) {
                            listener.onVideoEnded(currentPosition);
                        }
                        break;
                    case Player.STATE_IDLE:
                        Log.d(TAG, "⭕ Player is idle");
                        break;
                }
            }

            @Override
            public void onPlayerError(@NonNull com.google.android.exoplayer2.PlaybackException error) {
                Log.e(TAG, "❌ Player error for position " + currentPosition + ": " + error.getMessage());
                Log.e(TAG, "Error code: " + error.errorCode);
                Log.e(TAG, "Error type: " + getErrorTypeString(error));

                if (listener != null && currentPosition != -1) {
                    listener.onVideoError(currentPosition, new Exception(error));
                }
            }

            @Override
            public void onVideoSizeChanged(com.google.android.exoplayer2.video.VideoSize videoSize) {
                Log.d(TAG, "📐 Video size changed: " + videoSize.width + "x" + videoSize.height);
                Log.d(TAG, "Video aspect ratio: " + ((float) videoSize.width / videoSize.height));

                // Đảm bảo PlayerView cập nhật aspect ratio
                if (currentPlayerView != null) {
                    currentPlayerView.setResizeMode(com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT);
                }
            }

            @Override
            public void onRenderedFirstFrame() {
                Log.d(TAG, "🎬 First frame rendered successfully!");
            }

            @Override
            public void onSurfaceSizeChanged(int width, int height) {
                Log.d(TAG, "📱 Surface size changed: " + width + "x" + height);
            }
        });
    }

    /**
     * Gắn player vào PlayerView mới và phát video tại vị trí chỉ định
     */
    public void attachToPlayerView(@NonNull PlayerView playerView, @NonNull List<ShortVideo> videos, int position) {
        Log.d(TAG, "=== Attaching to PlayerView at position: " + position + " ===");

        // Detach khỏi PlayerView cũ
        if (currentPlayerView != null) {
            currentPlayerView.setPlayer(null);
            Log.d(TAG, "Detached from previous PlayerView");
        }

        // Cấu hình PlayerView chi tiết
        currentPlayerView = playerView;

        // Cấu hình PlayerView để hiển thị video tốt nhất
        currentPlayerView.setUseController(false); // Tắt controls mặc định
        currentPlayerView.setResizeMode(com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        currentPlayerView.setShowBuffering(com.google.android.exoplayer2.ui.PlayerView.SHOW_BUFFERING_WHEN_PLAYING);

        // Đảm bảo surface được tạo đúng cách
        currentPlayerView.setKeepContentOnPlayerReset(true);

        // Gắn player vào PlayerView
        currentPlayerView.setPlayer(exoPlayer);
        Log.d(TAG, "✅ PlayerView configured and attached to ExoPlayer");

        // Cập nhật danh sách video và vị trí
        this.videos = videos;
        this.currentPosition = position;

        // Load và phát video mới
        if (position >= 0 && position < videos.size()) {
            ShortVideo video = videos.get(position);

            // Thử URL cơ bản trước để tránh vấn đề với optimization
            String videoUrl = video.getVideoUrlWithFallback();

            Log.d(TAG, "Video info - ID: " + video.getId());
            Log.d(TAG, "Video info - PublicId: " + video.getCldPublicId());
            Log.d(TAG, "Video info - Version: " + video.getCldVersion());
            Log.d(TAG, "Video info - Generated URL (fallback): " + videoUrl);

            if (videoUrl != null && !videoUrl.isEmpty()) {
                // Thêm validation cho URL
                if (!isValidVideoUrl(videoUrl)) {
                    Log.e(TAG, "INVALID VIDEO URL: " + videoUrl);
                    Log.e(TAG, "URL validation failed - trying optimized URL as last resort");

                    // Last resort: thử URL tối ưu hóa
                    String optimizedUrl = video.getOptimizedVideoUrl();
                    if (optimizedUrl != null && isValidVideoUrl(optimizedUrl)) {
                        videoUrl = optimizedUrl;
                        Log.d(TAG, "✅ Using optimized URL as fallback: " + videoUrl);
                    } else {
                        if (listener != null) {
                            listener.onVideoError(position, new Exception("All video URL attempts failed"));
                        }
                        return;
                    }
                }

                Log.d(TAG, "✅ URL validation passed - Loading video: " + videoUrl);

                // Tạo MediaItem đơn giản từ URI
                MediaItem mediaItem = MediaItem.fromUri(videoUrl);

                Log.d(TAG, "🎬 Created MediaItem from URI");

                // Load media item vào player
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.prepare();
                exoPlayer.setPlayWhenReady(true);
                Log.d(TAG, "✅ Video prepared and set to play");

                // Force refresh PlayerView
                currentPlayerView.invalidate();

            } else {
                Log.e(TAG, "❌ Video URL is null or empty for position: " + position);
                if (listener != null) {
                    listener.onVideoError(position, new Exception("Video URL is null or empty"));
                }
            }
        } else {
            Log.e(TAG, "❌ Invalid position: " + position + " (videos size: " + videos.size() + ")");
        }
    }

    /**
     * Validate video URL trước khi load
     */
    private boolean isValidVideoUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            Log.e(TAG, "URL validation failed: URL is null or empty");
            return false;
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Log.e(TAG, "URL validation failed: Not a valid HTTP/HTTPS URL: " + url);
            return false;
        }

        if (!url.contains("cloudinary.com")) {
            Log.e(TAG, "URL validation failed: Not a Cloudinary URL: " + url);
            return false;
        }

        if (!url.contains("video/upload")) {
            Log.e(TAG, "URL validation failed: Not a video URL (contains image/upload instead): " + url);
            return false;
        }

        if (!url.endsWith(".mp4")) {
            Log.e(TAG, "URL validation failed: Does not end with .mp4: " + url);
            return false;
        }

        Log.d(TAG, "✅ URL validation passed for: " + url);
        return true;
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

    /**
     * Helper method để convert playback state thành string dễ đọc
     */
    private String getPlaybackStateString(int state) {
        switch (state) {
            case Player.STATE_IDLE: return "IDLE";
            case Player.STATE_BUFFERING: return "BUFFERING";
            case Player.STATE_READY: return "READY";
            case Player.STATE_ENDED: return "ENDED";
            default: return "UNKNOWN";
        }
    }

    /**
     * Helper method để convert error type thành string dễ đọc
     */
    private String getErrorTypeString(com.google.android.exoplayer2.PlaybackException error) {
        switch (error.errorCode) {
            case com.google.android.exoplayer2.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED:
                return "NETWORK_CONNECTION_FAILED";
            case com.google.android.exoplayer2.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT:
                return "NETWORK_CONNECTION_TIMEOUT";
            case com.google.android.exoplayer2.PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED:
                return "CONTAINER_MALFORMED";
            case com.google.android.exoplayer2.PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED:
                return "MANIFEST_MALFORMED";
            case com.google.android.exoplayer2.PlaybackException.ERROR_CODE_DECODER_INIT_FAILED:
                return "DECODER_INIT_FAILED";
            case com.google.android.exoplayer2.PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED:
                return "DECODER_QUERY_FAILED";
            default:
                return "ERROR_CODE_" + error.errorCode;
        }
    }
}
