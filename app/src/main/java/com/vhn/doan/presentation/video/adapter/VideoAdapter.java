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
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Short video feed (TikTok-like) với quản lý vị trí thông minh - chỉ lưu vị trí tạm thời trong session
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private final List<ShortVideo> videos = new ArrayList<>();
    private OnVideoInteractionListener listener;

    // Chỉ cho 1 video phát tại 1 thời điểm
    private int currentPlayingPosition = -1;
    private ExoPlayer currentPlayer;

    // Lưu vị trí phát tạm thời cho session hiện tại (chỉ cho video đang active hoặc gần đó)
    private final Map<String, Long> temporaryPositions = new HashMap<>();

    // Cache player instances để tránh tạo lại liên tục
    private final Map<String, ExoPlayer> playerCache = new HashMap<>();
    private static final int MAX_CACHED_PLAYERS = 3; // Giới hạn số lượng player cache

    // Quản lý vị trí video được viewed (để reset về đầu khi lướt lại)
    private final Set<String> viewedVideos = new HashSet<>();
    private final Map<String, Long> lastViewTime = new HashMap<>();

    // Cấu hình auto-cleanup
    private static final long SESSION_TIMEOUT = 5 * 60 * 1000; // 5 phút
    private static final int MAX_POSITION_CACHE = 10; // Tối đa 10 video lưu vị trí tạm thời

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
        notifyDataSetChanged();
    }

    public void updateVideo(ShortVideo video, int position) {
        if (position >= 0 && position < videos.size()) {
            videos.set(position, video);
            notifyItemChanged(position, "payload_metadata");
        }
    }

    public void setCurrentPlayingPosition(int position) {
        int old = currentPlayingPosition;
        currentPlayingPosition = position;

        // Pause video cũ
        if (old != -1 && old != position) {
            notifyItemChanged(old, "payload_pause");
        }

        // Play video mới
        if (position != -1) {
            notifyItemChanged(position, "payload_play");
        }
    }

    public void pauseAllVideos() {
        if (currentPlayer != null) {
            // Lưu vị trí hiện tại trước khi pause (để resume khi tap lại trên cùng item)
            if (currentPlayingPosition != -1 && currentPlayingPosition < videos.size()) {
                String videoId = videos.get(currentPlayingPosition).getId();
                if (videoId != null) {
                    temporaryPositions.put(videoId, currentPlayer.getCurrentPosition());
                }
            }
            currentPlayer.setPlayWhenReady(false);
        }
        currentPlayingPosition = -1;
    }

    private void releaseAllPlayers() {
        // Lưu vị trí của player hiện tại trước khi release
        if (currentPlayer != null && currentPlayingPosition != -1 && currentPlayingPosition < videos.size()) {
            String videoId = videos.get(currentPlayingPosition).getId();
            if (videoId != null) {
                temporaryPositions.put(videoId, currentPlayer.getCurrentPosition());
            }
        }

        // Release tất cả cached players
        for (ExoPlayer player : playerCache.values()) {
            if (player != null) {
                player.release();
            }
        }
        playerCache.clear();
        currentPlayer = null;
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
        holder.saveCurrentPosition(); // lưu vị trí trước khi recycle (để resume nếu user vẫn ở cùng item)
        holder.releasePlayer();
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

        private ExoPlayer getOrCreatePlayer(Context ctx, String videoId) {
            // Kiểm tra cache trước
            ExoPlayer cachedPlayer = playerCache.get(videoId);
            if (cachedPlayer != null) {
                return cachedPlayer;
            }

            // Tạo player mới
            ExoPlayer newPlayer = new ExoPlayer.Builder(ctx)
                    .setSeekBackIncrementMs(5000)
                    .setSeekForwardIncrementMs(10000)
                    .build();

            newPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);

            // Listener để theo dõi trạng thái
            newPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_BUFFERING) {
                        showLoading(true);
                        isPlayerReady = false;
                    } else if (state == Player.STATE_READY) {
                        showLoading(false);
                        isPlayerReady = true;

                        // Seek về vị trí đã lưu nếu có (trừ khi đã bị reset về 0 trong bind)
                        Long savedPosition = temporaryPositions.get(videoId);
                        if (savedPosition != null && savedPosition > 0) {
                            newPlayer.seekTo(savedPosition);
                        }
                    } else if (state == Player.STATE_ENDED) {
                        // Reset về đầu video khi kết thúc
                        temporaryPositions.put(videoId, 0L);
                    }
                }

                @Override
                public void onPlayerError(@NonNull PlaybackException error) {
                    showLoading(false);
                    isPlayerReady = false;
                }
            });

            // Cache player (giới hạn số lượng)
            if (playerCache.size() >= MAX_CACHED_PLAYERS) {
                // Remove oldest player
                String oldestKey = playerCache.keySet().iterator().next();
                ExoPlayer oldPlayer = playerCache.remove(oldestKey);
                if (oldPlayer != null && oldPlayer != currentPlayer) {
                    oldPlayer.release();
                }
            }

            playerCache.put(videoId, newPlayer);
            return newPlayer;
        }

        void bind(ShortVideo video, int position) {
            bindMetadata(video);

            String videoId = video.getId();
            if (videoId == null || videoId.isEmpty()) {
                return; // Skip nếu không có ID
            }

            // ---------- QUY TẮC REPLAY KHI QUAY LẠI ITEM ----------
            // Mỗi lần ViewHolder được bind (tức là quay lại item), buộc phát lại từ đầu
            temporaryPositions.put(videoId, 0L);
            // ------------------------------------------------------

            // Lưu vị trí cũ của item trước đó nếu có
            saveCurrentPosition();

            currentVideoId = videoId;
            String url = getVideoUrl(video);

            // Lấy hoặc tạo player cho video này
            player = getOrCreatePlayer(itemView.getContext(), videoId);
            playerView.setPlayer(player);
            playerView.setUseController(false);
            playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER);

            // Kiểm tra xem đã load media chưa
            if (player.getCurrentMediaItem() == null ||
                    !url.equals(getCurrentMediaUrl(player))) {

                // Load media mới
                MediaItem item = MediaItem.fromUri(Uri.parse(url));
                player.setMediaItem(item);
                player.prepare();
                isPlayerReady = false;
            } else {
                // Nếu media giống nhau, seek về 0 theo quy tắc replay
                player.seekTo(0);
            }

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

                // Khi play lại item vừa quay về, theo luật replay (đã set về 0 ở bind)
                // Khi tap pause/play trên cùng item thì không re-bind, nên vẫn resume vị trí hiện tại
                if (isPlayerReady) {
                    player.setPlayWhenReady(true);
                    showPlayOverlay(false);
                } else {
                    player.setPlayWhenReady(true);
                    showLoading(true);
                }
            } else {
                // Lưu vị trí hiện tại để RESUME khi tap trên cùng item (không qua re-bind)
                saveCurrentPosition();
                player.setPlayWhenReady(false);
                showPlayOverlay(true);
            }
        }

        private void togglePlayPause() {
            int p = getBindingAdapterPosition();
            if (p == RecyclerView.NO_POSITION) return;

            if (p == currentPlayingPosition) {
                // Đang phát -> pause (sẽ lưu vị trí để resume nếu tap lại ngay)
                setCurrentPlayingPosition(-1);
            } else {
                // Chưa phát -> play (nếu là re-bind do quay lại, bind() đã reset về 0)
                setCurrentPlayingPosition(p);
            }
        }

        void saveCurrentPosition() {
            if (player != null && !currentVideoId.isEmpty()) {
                long pos = player.getCurrentPosition();
                temporaryPositions.put(currentVideoId, pos);
            }
        }

        public void releasePlayer() {
            saveCurrentPosition();

            // Không release player ngay, để lại trong cache
            if (player != null) {
                player.setPlayWhenReady(false);
                playerView.setPlayer(null);
                player = null;
            }

            currentVideoId = "";
            isPlayerReady = false;
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

        private String getCurrentMediaUrl(ExoPlayer player) {
            if (player == null || player.getCurrentMediaItem() == null) return null;
            MediaItem mi = player.getCurrentMediaItem();
            if (mi.localConfiguration == null || mi.localConfiguration.uri == null) return null;
            return mi.localConfiguration.uri.toString();
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

    /**
     * Resume video tại position (dùng khi quay lại màn hình và muốn phát auto)
     */
    public void resumeVideoAt(int position) {
        setCurrentPlayingPosition(position);
    }

    /**
     * Preload video ở vị trí gần đó để cải thiện trải nghiệm
     */
    public void preloadVideoAt(int position, Context context) {
        if (position >= 0 && position < videos.size() && context != null) {
            ShortVideo video = videos.get(position);
            String videoId = video.getId();
            if (videoId != null && !playerCache.containsKey(videoId)) {
                // Tạo player và prepare sẵn
                ExoPlayer preloadPlayer = createPlayer(context, videoId);
                String url = getVideoUrl(video);
                MediaItem item = MediaItem.fromUri(Uri.parse(url));
                preloadPlayer.setMediaItem(item);
                preloadPlayer.prepare();
                // Không play, chỉ prepare
            }
        }
    }

    private ExoPlayer createPlayer(Context context, String videoId) {
        // Kiểm tra cache trước
        ExoPlayer cachedPlayer = playerCache.get(videoId);
        if (cachedPlayer != null) {
            return cachedPlayer;
        }

        // Tạo player mới
        ExoPlayer newPlayer = new ExoPlayer.Builder(context)
                .setSeekBackIncrementMs(5000)
                .setSeekForwardIncrementMs(10000)
                .build();

        newPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);

        // Cache player (giới hạn số lượng)
        if (playerCache.size() >= MAX_CACHED_PLAYERS) {
            // Remove oldest player
            String oldestKey = playerCache.keySet().iterator().next();
            ExoPlayer oldPlayer = playerCache.remove(oldestKey);
            if (oldPlayer != null && oldPlayer != currentPlayer) {
                oldPlayer.release();
            }
        }

        playerCache.put(videoId, newPlayer);
        return newPlayer;
    }

    /**
     * Optimize memory bằng cách release player của các video xa hiện tại
     */
    public void optimizeMemory(int currentPosition) {
        // Clean up các player quá xa khỏi vị trí hiện tại
        List<String> keysToRemove = new ArrayList<>();

        for (int i = 0; i < videos.size(); i++) {
            if (Math.abs(i - currentPosition) > MAX_CACHED_PLAYERS) {
                String videoId = videos.get(i).getId();
                if (videoId != null && playerCache.containsKey(videoId)) {
                    keysToRemove.add(videoId);
                }
            }
        }

        for (String key : keysToRemove) {
            ExoPlayer player = playerCache.remove(key);
            if (player != null && player != currentPlayer) {
                player.release();
            }
        }
    }

    /**
     * Release tất cả resources khi không cần thiết
     */
    public void onDestroy() {
        releaseAllPlayers();
        temporaryPositions.clear();
    }

    /**
     * Tự động dọn dẹp dữ liệu tạm thời để tiết kiệm memory
     */
    public void autoCleanupTemporaryData() {
        long currentTime = System.currentTimeMillis();

        // Dọn dẹp vị trí tạm thời của các video đã xem lâu
        List<String> expiredPositions = new ArrayList<>();
        for (Map.Entry<String, Long> entry : lastViewTime.entrySet()) {
            if (currentTime - entry.getValue() > SESSION_TIMEOUT) {
                expiredPositions.add(entry.getKey());
            }
        }

        for (String videoId : expiredPositions) {
            temporaryPositions.remove(videoId);
            lastViewTime.remove(videoId);
            viewedVideos.remove(videoId);
        }

        // Giới hạn số lượng vị trí được cache
        if (temporaryPositions.size() > MAX_POSITION_CACHE) {
            // Xóa các video cũ nhất
            List<Map.Entry<String, Long>> sortedEntries = new ArrayList<>();
            for (Map.Entry<String, Long> entry : lastViewTime.entrySet()) {
                sortedEntries.add(entry);
            }

            // Sắp xếp theo thời gian xem (cũ nhất trước)
            sortedEntries.sort(Map.Entry.comparingByValue());

            // Xóa các entry cũ nhất
            int toRemove = temporaryPositions.size() - MAX_POSITION_CACHE;
            for (int i = 0; i < toRemove && i < sortedEntries.size(); i++) {
                String videoId = sortedEntries.get(i).getKey();
                temporaryPositions.remove(videoId);
                lastViewTime.remove(videoId);
                viewedVideos.remove(videoId);
            }
        }

        android.util.Log.d("VideoAdapter", "Auto cleanup completed. Cached positions: " +
                          temporaryPositions.size() + "/" + MAX_POSITION_CACHE);
    }

    /**
     * Xóa toàn bộ dữ liệu tạm thời (có thể gọi khi user thoát app)
     */
    public void clearAllTemporaryData() {
        temporaryPositions.clear();
        viewedVideos.clear();
        lastViewTime.clear();
        android.util.Log.d("VideoAdapter", "All temporary data cleared");
    }

    /**
     * Lấy thông tin về memory usage hiện tại
     */
    public String getMemoryUsageInfo() {
        return String.format(Locale.getDefault(),
                "Cached Players: %d/%d | Temporary Positions: %d/%d | Viewed Videos: %d",
                playerCache.size(), MAX_CACHED_PLAYERS,
                temporaryPositions.size(), MAX_POSITION_CACHE,
                viewedVideos.size());
    }
}
