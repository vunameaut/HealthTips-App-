package com.vhn.doan.presentation.video.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.ui.PlayerView;
import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.utils.CloudinaryUrls;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho RecyclerView hiển thị danh sách Short Videos
 * Tối ưu cho vertical scrolling với video player
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<ShortVideo> videos;
    private OnVideoInteractionListener listener;
    private int currentPlayingPosition = -1;
    private ExoPlayer currentPlayer; // Track current player globally

    /**
     * Interface để xử lý các tương tác với video
     */
    public interface OnVideoInteractionListener {
        void onVideoClick(ShortVideo video, int position);
        void onLikeClick(ShortVideo video, int position);
        void onShareClick(ShortVideo video, int position);
        void onCommentClick(ShortVideo video, int position);
        void onVideoVisible(int position);
        void onVideoInvisible(int position);
    }

    /**
     * Constructor
     */
    public VideoAdapter() {
        this.videos = new ArrayList<>();
    }

    /**
     * Set listener cho video interactions
     */
    public void setOnVideoInteractionListener(OnVideoInteractionListener listener) {
        this.listener = listener;
    }

    /**
     * Cập nhật danh sách video
     */
    public void updateVideos(List<ShortVideo> newVideos) {
        // Release current player to prevent memory leaks
        releaseCurrentPlayer();

        this.videos.clear();
        if (newVideos != null) {
            this.videos.addAll(newVideos);
        }
        currentPlayingPosition = -1;
        notifyDataSetChanged();
    }

    /**
     * Cập nhật thông tin một video cụ thể
     */
    public void updateVideo(ShortVideo video, int position) {
        if (position >= 0 && position < videos.size()) {
            videos.set(position, video);
            notifyItemChanged(position);
        }
    }

    /**
     * Set video đang phát
     */
    public void setCurrentPlayingPosition(int position) {
        int oldPosition = currentPlayingPosition;
        currentPlayingPosition = position;

        // Pause old video
        if (oldPosition != -1 && oldPosition != position) {
            notifyItemChanged(oldPosition);
        }

        // Play new video
        if (position != -1) {
            notifyItemChanged(position);
        }
    }

    /**
     * Pause tất cả video
     */
    public void pauseAllVideos() {
        if (currentPlayer != null) {
            currentPlayer.setPlayWhenReady(false);
        }
        currentPlayingPosition = -1;
    }

    /**
     * Resume video tại position
     */
    public void resumeVideoAt(int position) {
        setCurrentPlayingPosition(position);
    }

    /**
     * Release current player để tránh memory leak
     */
    private void releaseCurrentPlayer() {
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer.release();
            currentPlayer = null;
        }
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_short_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        ShortVideo video = videos.get(position);
        holder.bind(video, position);
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    @Override
    public void onViewRecycled(@NonNull VideoViewHolder holder) {
        super.onViewRecycled(holder);
        holder.releasePlayer();
    }

    /**
     * ViewHolder cho video item
     */
    public class VideoViewHolder extends RecyclerView.ViewHolder {

        private PlayerView playerView;
        private ImageView posterImageView;
        private TextView titleTextView;
        private TextView captionTextView;
        private TextView viewCountTextView;
        private TextView likeCountTextView;
        private TextView uploadDateTextView;
        private LinearLayout likeButton;         // Changed from ImageView to LinearLayout
        private LinearLayout shareButton;       // Changed from ImageView to LinearLayout
        private LinearLayout commentButton;     // Changed from ImageView to LinearLayout
        private View playPauseOverlay;
        private View loadingView;

        private ExoPlayer player;
        private boolean isPlayerInitialized = false;
        private String currentVideoUrl = "";

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            setupClickListeners();
        }

        private void initViews() {
            // Video player và overlay views
            playerView = itemView.findViewById(R.id.player_view);
            posterImageView = itemView.findViewById(R.id.iv_poster);
            playPauseOverlay = itemView.findViewById(R.id.play_pause_overlay);
            loadingView = itemView.findViewById(R.id.loading_view);

            // Text views cho metadata
            titleTextView = itemView.findViewById(R.id.tv_title);
            captionTextView = itemView.findViewById(R.id.tv_caption);
            viewCountTextView = itemView.findViewById(R.id.tv_view_count);
            uploadDateTextView = itemView.findViewById(R.id.tv_upload_date);
            likeCountTextView = itemView.findViewById(R.id.tv_like_count);

            // Button layouts
            likeButton = itemView.findViewById(R.id.btn_like);
            shareButton = itemView.findViewById(R.id.btn_share);
            commentButton = itemView.findViewById(R.id.btn_comment);
        }

        private void setupClickListeners() {
            // Click toàn bộ video để play/pause
            playerView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onVideoClick(videos.get(position), position);
                }
            });

            // Like button
            likeButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onLikeClick(videos.get(position), position);
                }
            });

            // Share button
            shareButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onShareClick(videos.get(position), position);
                }
            });

            // Comment button
            commentButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCommentClick(videos.get(position), position);
                }
            });
        }

        private void initPlayer(Context context) {
            if (isPlayerInitialized) {
                return;
            }

            // Khởi tạo ExoPlayer với cấu hình tối ưu
            player = new ExoPlayer.Builder(context)
                    .setSeekBackIncrementMs(5000)
                    .setSeekForwardIncrementMs(10000)
                    .build();

            // Set player to PlayerView
            playerView.setPlayer(player);

            // Ẩn controls để có trải nghiệm như TikTok
            playerView.setUseController(false);
            playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER);

            // Add player listener ONLY ONCE
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    switch (playbackState) {
                        case Player.STATE_IDLE:
                            android.util.Log.d("VideoPlayer", "State: IDLE");
                            break;
                        case Player.STATE_BUFFERING:
                            android.util.Log.d("VideoPlayer", "State: BUFFERING");
                            showLoading(true);
                            break;
                        case Player.STATE_READY:
                            android.util.Log.d("VideoPlayer", "State: READY");
                            showLoading(false);
                            break;
                        case Player.STATE_ENDED:
                            android.util.Log.d("VideoPlayer", "State: ENDED");
                            // Loop video
                            if (player != null) {
                                player.seekTo(0);
                                player.setPlayWhenReady(true);
                            }
                            break;
                    }
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    android.util.Log.e("VideoPlayer", "Player error: " + error.getMessage());
                    showLoading(false);
                }
            });

            isPlayerInitialized = true;
        }

        public void bind(ShortVideo video, int position) {
            // Set basic info
            titleTextView.setText(video.getTitle());
            captionTextView.setText(video.getCaption());

            // Format và hiển thị view count
            viewCountTextView.setText(formatCount(video.getViewCount()) + " lượt xem");

            // Format và hiển thị like count
            likeCountTextView.setText(formatCount(video.getLikeCount()));

            // Format và hiển thị upload date
            uploadDateTextView.setText(formatUploadDate(video.getUploadDate()));

            // Set video URLs sử dụng CloudinaryUrls hoặc test URL
            String videoUrl;
            if (video.getCldPublicId() != null && !video.getCldPublicId().isEmpty()) {
                videoUrl = CloudinaryUrls.mp4(video.getCldPublicId(), video.getCldVersion());
            } else {
                // Test URL tạm thời để debug
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
                android.util.Log.w("VideoPlayer", "Sử dụng test URL vì thiếu cldPublicId cho video: " + video.getTitle());
            }

            // Debug log
            android.util.Log.d("VideoPlayer", "Binding video at position " + position + " with URL: " + videoUrl);

            // Only setup video if URL changed to prevent reload loop
            if (!videoUrl.equals(currentVideoUrl)) {
                currentVideoUrl = videoUrl;
                setupVideoView(videoUrl, position);
            } else {
                // Just update play state without reloading video
                updatePlayState(position == currentPlayingPosition);
            }
        }

        private void setupVideoView(String videoUrl, int position) {
            try {
                // Initialize player if not done yet
                if (!isPlayerInitialized) {
                    initPlayer(itemView.getContext());
                }

                // Only proceed if player is initialized
                if (player == null) {
                    android.util.Log.e("VideoPlayer", "Player is null, cannot setup video");
                    return;
                }

                // Prevent reloading same video
                if (videoUrl.equals(currentVideoUrl) && player.getMediaItemCount() > 0) {
                    updatePlayState(position == currentPlayingPosition);
                    return;
                }

                // Clear previous media item
                player.stop();
                player.clearMediaItems();

                // Tạo MediaItem từ URL
                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
                player.setMediaItem(mediaItem);

                // Prepare player
                player.prepare();

                // Update current video URL
                currentVideoUrl = videoUrl;

                // Set play state
                updatePlayState(position == currentPlayingPosition);

                android.util.Log.d("VideoPlayer", "Video setup completed for position: " + position);

            } catch (Exception e) {
                android.util.Log.e("VideoPlayer", "Error setting up video: " + e.getMessage());
            }
        }

        private void updatePlayState(boolean shouldPlay) {
            if (player == null) return;

            if (shouldPlay) {
                // Set as current player globally
                if (currentPlayer != null && currentPlayer != player) {
                    currentPlayer.setPlayWhenReady(false);
                }
                currentPlayer = player;
                player.setPlayWhenReady(true);
                showPlayPauseOverlay(false);
            } else {
                player.setPlayWhenReady(false);
                showPlayPauseOverlay(true);
            }
        }

        private void showPlayPauseOverlay(boolean show) {
            if (playPauseOverlay != null) {
                playPauseOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }

        private void showLoading(boolean show) {
            if (loadingView != null) {
                loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }

        public void releasePlayer() {
            if (player != null) {
                player.stop();
                player.release();
                player = null;
                isPlayerInitialized = false;
                currentVideoUrl = "";
            }
        }

        // Helper methods
        private String formatCount(long count) {
            if (count < 1000) {
                return String.valueOf(count);
            } else if (count < 1000000) {
                return String.format(Locale.getDefault(), "%.1fK", count / 1000.0);
            } else {
                return String.format(Locale.getDefault(), "%.1fM", count / 1000000.0);
            }
        }

        private String formatUploadDate(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}
