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
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.utils.CloudinaryUrls;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

/**
 * Short video feed (TikTok-like) với quản lý ExoPlayer được tối ưu
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private final List<ShortVideo> videos = new ArrayList<>();
    private OnVideoInteractionListener listener;

    // Chỉ cho 1 video phát tại 1 thời điểm
    private int currentPlayingPosition = -1;
    private ExoPlayer currentPlayer;

    // Đơn giản hóa: chỉ cache vị trí cho video hiện tại để resume khi pause/play
    private final Map<String, Long> sessionPositions = new HashMap<>();

    public interface OnVideoInteractionListener {
        void onVideoClick(ShortVideo video, int position);
        void onLikeClick(ShortVideo video, int position);
        void onShareClick(ShortVideo video, int position);
        void onCommentClick(ShortVideo video, int position);
        void onVideoVisible(int position);
        void onVideoInvisible(int position);
    }

    public void setOnVideoInteractionListener(OnVideoInteractionListener listener) {
        this.listener = listener;
    }

    public void updateVideos(List<ShortVideo> newVideos) {
        releaseAllPlayers();
        videos.clear();
        if (newVideos != null) videos.addAll(newVideos);
        currentPlayingPosition = -1;
        sessionPositions.clear();
        notifyDataSetChanged();
    }

    public void updateVideo(ShortVideo video, int position) {
        if (position >= 0 && position < videos.size()) {
            videos.set(position, video);
            notifyItemChanged(position, "payload_metadata");
        }
    }

    /**
     * Cập nhật trạng thái phát cho video tại position
     */
    public void setCurrentPlayingPosition(int position) {
        if (currentPlayingPosition == position) return;

        // Pause video hiện tại
        pauseAllVideos();

        currentPlayingPosition = position;
        if (position >= 0) {
            notifyItemChanged(position);
        }

        // Thông báo cho listener (nếu có)
        if (listener != null && position >= 0) {
            listener.onVideoVisible(position);
        }
    }

    /**
     * Pause tất cả video - được gọi khi scroll
     */
    public void pauseAllVideos() {
        if (currentPlayingPosition >= 0) {
            notifyItemChanged(currentPlayingPosition, "payload_pause");
        }
        currentPlayingPosition = -1;
    }

    /**
     * Resume video tại position
     */
    public void resumeVideoAt(int position) {
        setCurrentPlayingPosition(position);
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_short_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
            return;
        }

        // Chỉ cập nhật play/pause khi có payload để tránh rebind media không cần thiết
        for (Object payload : payloads) {
            if ("payload_pause".equals(payload)) {
                holder.updatePlayState(false);
            } else if ("payload_play".equals(payload)) {
                holder.updatePlayState(true);
            } else if ("payload_metadata".equals(payload)) {
                holder.bindMetadata(videos.get(position));
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        holder.bind(videos.get(position), position);
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
     * Release tất cả players khi không cần thiết
     */
    private void releaseAllPlayers() {
        if (currentPlayer != null) {
            currentPlayer.release();
            currentPlayer = null;
        }
        currentPlayingPosition = -1;
        sessionPositions.clear();
    }

    // ====================== ViewHolder ======================

    public class VideoViewHolder extends RecyclerView.ViewHolder {

        private PlayerView playerView;
        private ImageView posterImageView;
        private ImageView playPauseOverlay;
        private View loadingView;

        private TextView titleTextView, captionTextView, viewCountTextView, likeCountTextView, uploadDateTextView;
        private LinearLayout likeButton, shareButton, commentButton;

        private ExoPlayer player;
        private String currentVideoId = "";
        private boolean isPlayerReady = false;
        private boolean isCurrentlyPlaying = false;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            setupClicks();
        }

        private void initViews() {
            playerView = itemView.findViewById(R.id.player_view);
            posterImageView = itemView.findViewById(R.id.iv_poster);
            playPauseOverlay = itemView.findViewById(R.id.play_pause_overlay);
            loadingView = itemView.findViewById(R.id.loading_view);

            titleTextView = itemView.findViewById(R.id.tv_title);
            captionTextView = itemView.findViewById(R.id.tv_caption);
            viewCountTextView = itemView.findViewById(R.id.tv_view_count);
            uploadDateTextView = itemView.findViewById(R.id.tv_upload_date);
            likeCountTextView = itemView.findViewById(R.id.tv_like_count);

            likeButton = itemView.findViewById(R.id.btn_like);
            shareButton = itemView.findViewById(R.id.btn_share);
            commentButton = itemView.findViewById(R.id.btn_comment);
        }

        private void setupClicks() {
            View tapArea = itemView.findViewById(R.id.video_tap_area);
            if (tapArea != null) tapArea.setOnClickListener(v -> togglePlayPause());
            playerView.setOnClickListener(v -> togglePlayPause());

            likeButton.setOnClickListener(v -> {
                int p = getBindingAdapterPosition();
                if (p != RecyclerView.NO_POSITION && listener != null) {
                    listener.onLikeClick(videos.get(p), p);
                }
            });
            shareButton.setOnClickListener(v -> {
                int p = getBindingAdapterPosition();
                if (p != RecyclerView.NO_POSITION && listener != null) {
                    listener.onShareClick(videos.get(p), p);
                }
            });
            commentButton.setOnClickListener(v -> {
                int p = getBindingAdapterPosition();
                if (p != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCommentClick(videos.get(p), p);
                }
            });
        }

        private ExoPlayer createPlayer(Context ctx) {
            ExoPlayer newPlayer = new ExoPlayer.Builder(ctx)
                    .setSeekBackIncrementMs(5000)
                    .setSeekForwardIncrementMs(10000)
                    .build();

            newPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);

            // Listener để theo dõi trạng thái
            newPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    switch (state) {
                        case Player.STATE_BUFFERING:
                            showLoading(true);
                            isPlayerReady = false;
                            break;
                        case Player.STATE_READY:
                            showLoading(false);
                            isPlayerReady = true;
                            // Chỉ seek nếu có vị trí đã lưu và đang trong cùng session
                            Long savedPosition = sessionPositions.get(currentVideoId);
                            if (savedPosition != null && savedPosition > 1000) { // Chỉ seek nếu > 1 giây
                                newPlayer.seekTo(savedPosition);
                                sessionPositions.remove(currentVideoId); // Xóa sau khi đã sử dụng
                            }
                            break;
                        case Player.STATE_ENDED:
                            // Reset video về đầu khi kết thúc
                            sessionPositions.put(currentVideoId, 0L);
                            isCurrentlyPlaying = false;
                            showPlayOverlay(true);
                            break;
                        case Player.STATE_IDLE:
                            isPlayerReady = false;
                            break;
                    }
                }

                @Override
                public void onPlayerError(@NonNull PlaybackException error) {
                    showLoading(false);
                    isPlayerReady = false;
                    // Có thể hiển thị thông báo lỗi hoặc retry
                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    isCurrentlyPlaying = isPlaying;
                    showPlayOverlay(!isPlaying);
                }
            });

            return newPlayer;
        }

        void bind(ShortVideo video, int position) {
            bindMetadata(video);

            String videoId = video.getId();
            if (videoId == null || videoId.isEmpty()) {
                return; // Skip nếu không có ID
            }

            currentVideoId = videoId;
            String url = getVideoUrl(video);

            // Tạo player mới cho mỗi video để tránh xung đột
            if (player != null) {
                player.release();
            }

            player = createPlayer(itemView.getContext());
            playerView.setPlayer(player);
            playerView.setUseController(false);
            playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER);

            // Load media - luôn bắt đầu từ đầu khi bind mới
            MediaItem item = MediaItem.fromUri(Uri.parse(url));
            player.setMediaItem(item);
            player.prepare();
            isPlayerReady = false;

            // Cập nhật trạng thái play/pause
            updatePlayState(position == currentPlayingPosition);
        }

        void bindMetadata(ShortVideo video) {
            titleTextView.setText(video.getTitle());

            // Caption + See more
            if (video.getCaption() == null || video.getCaption().trim().isEmpty()) {
                captionTextView.setVisibility(View.GONE);
                TextView seeMore = itemView.findViewById(R.id.tv_see_more);
                if (seeMore != null) seeMore.setVisibility(View.GONE);
            } else {
                captionTextView.setVisibility(View.VISIBLE);
                captionTextView.setText(video.getCaption());
                captionTextView.setMaxLines(2);
                captionTextView.post(() -> {
                    TextView seeMore = itemView.findViewById(R.id.tv_see_more);
                    if (seeMore != null) {
                        if (captionTextView.getLineCount() > 2) {
                            seeMore.setVisibility(View.VISIBLE);
                            seeMore.setText(R.string.see_more);
                            seeMore.setOnClickListener(v -> {
                                if (captionTextView.getMaxLines() == Integer.MAX_VALUE) {
                                    captionTextView.setMaxLines(2);
                                    seeMore.setText(R.string.see_more);
                                } else {
                                    captionTextView.setMaxLines(Integer.MAX_VALUE);
                                    seeMore.setText(R.string.see_less);
                                }
                            });
                        } else {
                            seeMore.setVisibility(View.GONE);
                        }
                    }
                });
            }

            viewCountTextView.setText(formatCount(video.getViewCount()) + " lượt xem");
            likeCountTextView.setText(formatCount(video.getLikeCount()));
            uploadDateTextView.setText(formatDate(video.getUploadDate()));
        }

        void updatePlayState(boolean shouldPlay) {
            if (player == null) return;

            if (shouldPlay) {
                // Pause player cũ nếu có
                if (currentPlayer != null && currentPlayer != player) {
                    currentPlayer.setPlayWhenReady(false);
                }

                currentPlayer = player;
                player.setPlayWhenReady(true);
                showPlayOverlay(false);
            } else {
                // Lưu vị trí hiện tại để resume khi tap play lại
                if (player != null && isPlayerReady) {
                    long currentPos = player.getCurrentPosition();
                    if (currentPos > 0) {
                        sessionPositions.put(currentVideoId, currentPos);
                    }
                }
                player.setPlayWhenReady(false);
                showPlayOverlay(true);
            }
        }

        private void togglePlayPause() {
            int p = getBindingAdapterPosition();
            if (p == RecyclerView.NO_POSITION) return;

            if (p == currentPlayingPosition) {
                // Đang phát -> pause
                setCurrentPlayingPosition(-1);
            } else {
                // Chưa phát -> play
                setCurrentPlayingPosition(p);
            }
        }

        public void releasePlayer() {
            if (player != null) {
                // Lưu vị trí trước khi release
                if (isPlayerReady && !currentVideoId.isEmpty()) {
                    long currentPos = player.getCurrentPosition();
                    if (currentPos > 0) {
                        sessionPositions.put(currentVideoId, currentPos);
                    }
                }

                player.release();
                player = null;
            }

            playerView.setPlayer(null);
            currentVideoId = "";
            isPlayerReady = false;
            isCurrentlyPlaying = false;
        }

        private void showLoading(boolean show) {
            if (loadingView != null) {
                loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }

        private void showPlayOverlay(boolean show) {
            if (playPauseOverlay != null) {
                playPauseOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }


        private String formatCount(long count) {
            if (count < 1000) return String.valueOf(count);
            if (count < 1_000_000) return String.format(Locale.getDefault(), "%.1fK", count / 1000f);
            return String.format(Locale.getDefault(), "%.1fM", count / 1_000_000f);
        }

        private String formatDate(long timestamp) {
            if (timestamp == 0) return "";
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(date);
        }
    }

    private String getVideoUrl(ShortVideo v) {
        if (v.getCldPublicId() != null && !v.getCldPublicId().isEmpty()) {
            return CloudinaryUrls.mp4(v.getCldPublicId(), v.getCldVersion());
        }
        return "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
    }
}
