package com.vhn.doan.presentation.video.adapter;

import android.content.Context;
import android.graphics.Color;
import android.media.audiofx.LoudnessEnhancer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.local.VideoCacheManager;
import com.vhn.doan.utils.CloudinaryUrls;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter hiển thị feed short-video kiểu TikTok:
 * - 1 player chính cho item đang hiển thị
 * - Preload player (mute) cho các item lân cận để tránh "nháy đen"
 * - Khi lướt đi rồi quay lại: luôn REPLAY từ đầu
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    // ================== Cấu hình hành vi ==================
    private static final boolean REPLAY_ON_REVISIT = true; // lướt quay lại -> phát từ đầu
    private static final int PRELOAD_AHEAD = 2;            // số item preload trước/sau
    private static final int MAX_CAPTION_LENGTH = 100;     // Độ dài tối đa caption trước khi cắt
    private static final float DEFAULT_VOLUME = 0.5f;      // Volume mặc định 50%
    private static final int LOUDNESS_GAIN = 800;          // +8dB loudness boost (800 = 8.0dB)
    private static final float FAST_FORWARD_SPEED = 2.0f;  // Tốc độ phát nhanh x2 khi nhấn giữ
    private static final float REWIND_SPEED = 0.5f;        // Tốc độ phát chậm x0.5 khi nhấn giữ lùi
    private static final int LOAD_MORE_THRESHOLD = 3;      // Số video còn lại để trigger load more

    // ================== Dữ liệu / listener ==================
    private final List<ShortVideo> videos = new ArrayList<>();
    private OnVideoInteractionListener listener;

    // Map để theo dõi trạng thái like của từng video
    private final Map<Integer, Boolean> likeStatusMap = new HashMap<>();

    // Auto-scroll state
    private boolean autoScrollEnabled = false;

    // Audio enhancement
    private LoudnessEnhancer loudnessEnhancer;

    public interface OnVideoInteractionListener {
        void onVideoClick(ShortVideo video, int position);
        void onLikeClick(ShortVideo video, int position);
        void onShareClick(ShortVideo video, int position);
        void onCommentClick(ShortVideo video, int position);
        void onVideoVisible(int position);
        void onVideoInvisible(int position);
        void onVideoEnded(int position); // Callback khi video kết thúc
        void onLoadMore(); // Callback để load thêm video
        void onMenuClick(ShortVideo video, int position); // Callback khi click menu
    }

    public void setOnVideoInteractionListener(OnVideoInteractionListener listener) {
        this.listener = listener;
    }

    /**
     * Bật/tắt chế độ auto-scroll
     */
    public void setAutoScrollEnabled(boolean enabled) {
        this.autoScrollEnabled = enabled;
        // Cập nhật repeat mode của player hiện tại
        if (currentPlayer != null) {
            currentPlayer.setRepeatMode(enabled ? Player.REPEAT_MODE_OFF : Player.REPEAT_MODE_ONE);
        }
    }

    /**
     * Lấy trạng thái auto-scroll hiện tại
     */
    public boolean isAutoScrollEnabled() {
        return autoScrollEnabled;
    }

    /**
     * Lấy trạng thái hiển thị UI hiện tại
     * Trả về true nếu UI đang hiển thị, false nếu đang ẩn
     */
    public boolean isUIVisible() {
        // Kiểm tra trạng thái UI từ ViewHolder hiện tại nếu có
        if (currentPlayingPosition != RecyclerView.NO_POSITION && currentRecyclerView != null) {
            RecyclerView.ViewHolder holder = currentRecyclerView.findViewHolderForAdapterPosition(currentPlayingPosition);
            if (holder instanceof VideoViewHolder) {
                return ((VideoViewHolder) holder).isUIVisible;
            }
        }
        // Mặc định là hiển thị UI
        return true;
    }

    /**
     * Thiết lập hiển thị/ẩn UI cho tất cả video
     * @param visible true để hiển thị UI, false để ẩn UI
     */
    public void setUIVisibility(boolean visible) {
        // Cập nhật UI cho ViewHolder hiện tại
        if (currentPlayingPosition != RecyclerView.NO_POSITION && currentRecyclerView != null) {
            RecyclerView.ViewHolder holder = currentRecyclerView.findViewHolderForAdapterPosition(currentPlayingPosition);
            if (holder instanceof VideoViewHolder) {
                VideoViewHolder viewHolder = (VideoViewHolder) holder;
                viewHolder.setUIVisibility(visible);
            }
        }
    }

    public void updateVideos(List<ShortVideo> newVideos) {
        // 🎯 QUAN TRỌNG: Clear tất cả PlayerView trước khi release players
        // Điều này đảm bảo không có frame cache nào được giữ lại
        if (currentRecyclerView != null) {
            for (int i = 0; i < currentRecyclerView.getChildCount(); i++) {
                android.view.View child = currentRecyclerView.getChildAt(i);
                RecyclerView.ViewHolder holder = currentRecyclerView.getChildViewHolder(child);
                if (holder instanceof VideoViewHolder) {
                    VideoViewHolder vh = (VideoViewHolder) holder;
                    vh.playerView.setPlayer(null);
                }
            }
        }

        releaseAllPlayers();
        videos.clear();
        // Không xóa likeStatusMap nữa để giữ lại trạng thái like
        // likeStatusMap.clear();
        if (newVideos != null) videos.addAll(newVideos);
        currentPlayingPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    public void updateVideo(ShortVideo video, int position) {
        if (position >= 0 && position < videos.size()) {
            videos.set(position, video);
            notifyItemChanged(position, "payload_metadata");
        }
    }

    /**
     * Bí danh của updateVideoLikeStatus() để tương thích với code đang sử dụng
     * @param position Vị trí của video cần cập nhật trạng thái
     * @param isLiked Trạng thái like mới (true: đã like, false: chưa like)
     */
    public void updateLikeStatus(int position, boolean isLiked) {
        updateVideoLikeStatus(position, isLiked);
    }

    /**
     * Cập nhật trạng thái like cho video tại vị trí cụ thể
     */
    public void updateVideoLikeStatus(int position, boolean isLiked) {
        if (position >= 0 && position < videos.size()) {
            likeStatusMap.put(position, isLiked);

            // Tìm ViewHolder và cập nhật UI
            RecyclerView recyclerView = getCurrentRecyclerView();
            if (recyclerView != null) {
                VideoViewHolder holder = (VideoViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                if (holder != null) {
                    holder.updateLikeIcon(isLiked);
                    holder.confirmLikeOperation(isLiked);
                }
            }
        }
    }

    /**
     * Revert UI cho video khi like operation thất bại
     */
    public void revertLikeUI(int position) {
        if (position >= 0 && position < videos.size()) {
            RecyclerView recyclerView = getCurrentRecyclerView();
            if (recyclerView != null) {
                VideoViewHolder holder = (VideoViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                if (holder != null) {
                    holder.revertLikeUI(videos.get(position));
                }
            }
        }
    }

    // Helper để get current RecyclerView
    private RecyclerView currentRecyclerView;

    public void setRecyclerView(RecyclerView recyclerView) {
        this.currentRecyclerView = recyclerView;
    }

    private RecyclerView getCurrentRecyclerView() {
        return currentRecyclerView;
    }

    // ================== Player chính + preload ==================
    private ExoPlayer currentPlayer;                       // chỉ 1 player đang phát
    private int currentPlayingPosition = RecyclerView.NO_POSITION;
    private final Map<Integer, ExoPlayer> preloadedPlayers = new HashMap<>(); // đã prepare, mute
    private VideoViewHolder activeHolder;                  // holder đang gắn player
    private Context appContext;

    // 🎯 VIDEO CACHE để hỗ trợ offline playback
    private VideoCacheManager videoCacheManager;
    private CacheDataSource.Factory cacheDataSourceFactory;

    private void ensureCurrentPlayer(Context context) {
        if (currentPlayer != null) return;
        appContext = context.getApplicationContext();

        // 🎯 Khởi tạo Video Cache Manager
        if (videoCacheManager == null) {
            videoCacheManager = VideoCacheManager.getInstance(appContext);

            // Tạo CacheDataSourceFactory để ExoPlayer tự động cache
            DataSource.Factory upstreamFactory = new DefaultDataSource.Factory(
                appContext,
                new DefaultHttpDataSource.Factory()
                    .setUserAgent("HealthTipsApp/1.0")
                    .setConnectTimeoutMs(30000)
                    .setReadTimeoutMs(30000)
            );

            cacheDataSourceFactory = new CacheDataSource.Factory()
                .setCache(videoCacheManager.getCache())
                .setUpstreamDataSourceFactory(upstreamFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR); // Nếu cache lỗi thì fallback online
        }

        // Tạo ExoPlayer với cache support
        currentPlayer = new ExoPlayer.Builder(appContext)
            .setMediaSourceFactory(new DefaultMediaSourceFactory(cacheDataSourceFactory))
            .build();

        // Set repeat mode dựa trên auto-scroll
        currentPlayer.setRepeatMode(autoScrollEnabled ? Player.REPEAT_MODE_OFF : Player.REPEAT_MODE_ONE);
        currentPlayer.setPlayWhenReady(true);

        // Thiết lập volume mặc định 50%
        currentPlayer.setVolume(DEFAULT_VOLUME);

        // Khởi tạo LoudnessEnhancer để tăng âm lượng
        try {
            if (loudnessEnhancer != null) {
                loudnessEnhancer.release();
            }
            loudnessEnhancer = new LoudnessEnhancer(currentPlayer.getAudioSessionId());
            loudnessEnhancer.setTargetGain(LOUDNESS_GAIN); // +8dB
            loudnessEnhancer.setEnabled(true);
            Log.d("VideoAdapter", "✅ LoudnessEnhancer initialized with +8dB gain");
        } catch (Exception e) {
            Log.e("VideoAdapter", "❌ Failed to initialize LoudnessEnhancer", e);
        }

        attachMainPlayerListener();

        Log.d("VideoAdapter", "✅ Current player created with cache support and audio enhancement");
    }

    private void attachMainPlayerListener() {
        currentPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (activeHolder == null) return;
                switch (state) {
                    case Player.STATE_BUFFERING:
                        activeHolder.showLoading(true);
                        activeHolder.showPoster(true);
                        break;
                    case Player.STATE_READY:
                        activeHolder.showLoading(false);
                        activeHolder.showPoster(false);
                        break;
                    case Player.STATE_ENDED:
                        // Xử lý khi video kết thúc
                        if (autoScrollEnabled && listener != null) {
                            // Auto-scroll: chuyển sang video tiếp theo
                            listener.onVideoEnded(currentPlayingPosition);
                        } else {
                            // Manual mode: phát lại video hiện tại
                            currentPlayer.seekTo(0);
                            currentPlayer.play();
                        }

                        // Kiểm tra xem có cần load thêm video không
                        if (currentPlayingPosition >= getItemCount() - LOAD_MORE_THRESHOLD) {
                            if (listener != null) {
                                listener.onLoadMore();
                            }
                        }
                        break;
                    case Player.STATE_IDLE:
                    default:
                        break;
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                if (activeHolder != null) {
                    activeHolder.showLoading(false);
                    activeHolder.showPoster(true);
                }
            }
        });
    }

    private ExoPlayer createPreloadPlayer(String url) {
        Context ctx = appContext != null ? appContext : lastKnownContext;

        // 🎯 Đảm bảo cache factory đã được khởi tạo
        if (cacheDataSourceFactory == null && ctx != null) {
            videoCacheManager = VideoCacheManager.getInstance(ctx);

            DataSource.Factory upstreamFactory = new DefaultDataSource.Factory(
                ctx,
                new DefaultHttpDataSource.Factory()
                    .setUserAgent("HealthTipsApp/1.0")
                    .setConnectTimeoutMs(30000)
                    .setReadTimeoutMs(30000)
            );

            cacheDataSourceFactory = new CacheDataSource.Factory()
                .setCache(videoCacheManager.getCache())
                .setUpstreamDataSourceFactory(upstreamFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
        }

        // Tạo preload player với cache support
        ExoPlayer p = new ExoPlayer.Builder(ctx)
            .setMediaSourceFactory(new DefaultMediaSourceFactory(cacheDataSourceFactory))
            .build();

        p.setRepeatMode(Player.REPEAT_MODE_ONE);
        p.setPlayWhenReady(false);      // preload -> không phát
        p.setVolume(0f);                // luôn mute trong preload
        p.setMediaItem(MediaItem.fromUri(Uri.parse(url)));
        p.prepare();                    // sẵn sàng - ExoPlayer sẽ tự cache!

        Log.d("VideoAdapter", "📦 Preload player created with cache for: " + url);
        return p;
    }

    private Context lastKnownContext; // fallback nếu appContext chưa set

    // ================== RecyclerView ==================
    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        lastKnownContext = parent.getContext();
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
        for (Object p : payloads) {
            if ("payload_metadata".equals(p)) {
                holder.bindMetadata(videos.get(position));
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        ShortVideo video = videos.get(position);

        // Null safety check
        if (video == null) {
            android.util.Log.e("VideoAdapter", "Video at position " + position + " is null");
            return;
        }

        holder.bindMetadata(video);

        // 🎯 Reset UI về trạng thái hiển thị mặc định khi bind video mới
        // Đảm bảo mỗi video mới đều hiển thị UI, không bị ảnh hưởng bởi trạng thái ẩn UI của video trước
        holder.setUIVisibility(true);

        // 🎯 QUAN TRỌNG: Clear player trước để tránh hiển thị frame cũ
        holder.playerView.setPlayer(null);

        // Cấu hình PlayerView anti-flicker
        holder.playerView.setUseController(false);
        holder.playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING);
        // 🎯 TẮT keepContentOnPlayerReset để tránh giữ lại video cache cũ
        holder.playerView.setKeepContentOnPlayerReset(false);
        holder.playerView.setShutterBackgroundColor(Color.TRANSPARENT);

        // Nếu là item đang phát -> gắn player chính
        if (position == currentPlayingPosition && currentPlayer != null) {
            activeHolder = holder;
            // 🎯 Đặt player SAU KHI đã clear để tránh flash
            holder.playerView.setPlayer(currentPlayer);
            // đồng bộ UI theo state hiện tại
            int state = currentPlayer.getPlaybackState();
            holder.showLoading(state == Player.STATE_BUFFERING);
            holder.showPoster(state != Player.STATE_READY);

            // Chủ động yêu cầu kiểm tra trạng thái like khi video hiển thị
            if (listener != null) {
                listener.onVideoVisible(position);
            }
        } else {
            // PlayerView đã được clear ở trên rồi
            holder.showLoading(false);
            // Khi không phải item đang phát thì để poster hiển thị sẵn
            holder.showPoster(true);
        }

        // Preload xung quanh
        preloadAround(position);
    }

    @Override
    public void onViewRecycled(@NonNull VideoViewHolder holder) {
        // ngắt liên kết player với holder cũ
        if (holder == activeHolder) {
            holder.playerView.setPlayer(null);
            activeHolder = null;
        } else {
            holder.playerView.setPlayer(null);
        }
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    // ================== API điều khiển phát ==================

    /**
     * Gọi từ Fragment khi item này trở thành visible.
     */
    public void playVideoAt(int position, @NonNull RecyclerView recyclerView) {
        if (position < 0 || position >= getItemCount()) return;

        try {
            ensureCurrentPlayer(recyclerView.getContext());
            String url = getVideoUrl(videos.get(position));

            // Null/empty url check
            if (url == null || url.isEmpty()) {
                android.util.Log.e("VideoAdapter", "Video URL is null or empty at position " + position);
                return;
            }

            // Thông báo ngay lập tức khi video trở nên visible để cập nhật trạng thái like
            if (listener != null) {
                listener.onVideoVisible(position);
            }


            // 1) Nếu đã có player preload cho vị trí này -> handover sang player chính
            // (Note: Giờ không còn preload cho video đang phát, nhưng giữ logic để xử lý edge cases)
            ExoPlayer pre = preloadedPlayers.remove(position);
            if (pre != null) {
                android.util.Log.d("VideoAdapter", "🔄 Handover preload player to current for position " + position);

                // Dừng hẳn preload player
                pre.pause();
                pre.setPlayWhenReady(false);

                // Giải phóng player chính cũ
                if (currentPlayer != null) {
                    try { currentPlayer.release(); } catch (Exception ignore) {}
                }

                // Handover sang player chính
                currentPlayer = pre;
                attachMainPlayerListener();

                // Seek về 0 và play
                currentPlayer.seekTo(0);
                currentPlayer.setVolume(1f);
                currentPlayer.setPlayWhenReady(true);

                android.util.Log.d("VideoAdapter", "✅ Handover complete - playing from start");

            } else {
                // 2) Dùng player chính hiện tại:
                // - Nếu media giống nhau -> (nếu config REPLAY) thì seek về 0
                // - Nếu khác -> set media mới + prepare
                boolean same = false;
                if (currentPlayer.getMediaItemCount() > 0) {
                    MediaItem cur = currentPlayer.getCurrentMediaItem();
                    if (cur != null && cur.localConfiguration != null && cur.localConfiguration.uri != null) {
                        same = url.equals(cur.localConfiguration.uri.toString());
                    }
                }
                if (same) {
                    if (REPLAY_ON_REVISIT) currentPlayer.seekTo(0);
                    currentPlayer.setPlayWhenReady(true);
                } else {
                    // 🎯 QUAN TRỌNG: Clear surface trước khi load video mới
                    // Detach player từ tất cả views để xóa frame cache
                    if (activeHolder != null) {
                        activeHolder.playerView.setPlayer(null);
                    }

                    currentPlayer.stop();
                    currentPlayer.clearMediaItems();
                    currentPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(url)), /*resetPosition=*/true);
                    currentPlayer.prepare();
                    currentPlayer.setPlayWhenReady(true);

                    android.util.Log.d("VideoAdapter", "🔄 Loading new video: " + url);
                }
            }

            // 3) Cập nhật UI holder mới
            int previous = currentPlayingPosition;
            currentPlayingPosition = position;

            if (previous != RecyclerView.NO_POSITION) notifyItemChanged(previous);
            notifyItemChanged(position);

            // 4) Gọi preload vòng quanh
            preloadAround(position);
        } catch (Exception e) {
            android.util.Log.e("VideoAdapter", "Error playing video at position " + position, e);
        }
    }

    /**
     * G�����i khi cần tạm dừng (ví dụ Fragment onPause hoặc khi user chạm tạm dừng).
     */
    public void pauseAllVideos() {
        if (currentPlayer != null) {
            currentPlayer.setPlayWhenReady(false);
        }
        int previous = currentPlayingPosition;
        currentPlayingPosition = RecyclerView.NO_POSITION;
        if (previous != RecyclerView.NO_POSITION) notifyItemChanged(previous);
    }

    /**
     * Dọn dẹp toàn bộ players (Fragment onDestroyView/onDestroy).
     */
    public void releaseAllPlayers() {
        if (currentPlayer != null) {
            try {
                // 🎯 QUAN TRỌNG: Stop và clear media items trước khi release
                currentPlayer.stop();
                currentPlayer.clearMediaItems();
                currentPlayer.release();
            } catch (Exception ignore) {}
            currentPlayer = null;
        }
        if (loudnessEnhancer != null) {
            try { loudnessEnhancer.release(); } catch (Exception ignore) {}
            loudnessEnhancer = null;
        }
        for (ExoPlayer p : preloadedPlayers.values()) {
            try { p.release(); } catch (Exception ignore) {}
        }
        preloadedPlayers.clear();
        activeHolder = null;
        currentPlayingPosition = RecyclerView.NO_POSITION;
    }

    // ================== Preload ==================
    private void preloadAround(int anchorPosition) {
        if (appContext == null) appContext = lastKnownContext;
        if (appContext == null) return;

        // dọn những player preload quá xa hoặc chính là video đang phát
        preloadedPlayers.entrySet().removeIf(e -> {
            int pos = e.getKey();
            // 🎯 Xóa player preload nếu quá xa HOẶC là video đang phát
            if (Math.abs(pos - anchorPosition) > PRELOAD_AHEAD || pos == anchorPosition) {
                try { e.getValue().release(); } catch (Exception ignore) {}
                return true;
            }
            return false;
        });

        // preload phía trước và phía sau - NHƯNG KHÔNG preload cho chính anchorPosition
        for (int i = 1; i <= PRELOAD_AHEAD; i++) {
            int next = anchorPosition + i;
            // 🎯 Chỉ preload nếu KHÔNG phải video đang phát
            if (next < getItemCount() && next != anchorPosition && !preloadedPlayers.containsKey(next)) {
                try {
                    String url = getVideoUrl(videos.get(next));
                    if (url != null && !url.isEmpty()) {
                        preloadedPlayers.put(next, createPreloadPlayer(url));
                    }
                } catch (Exception e) {
                    android.util.Log.e("VideoAdapter", "Error preloading video at position " + next, e);
                }
            }
            int prev = anchorPosition - i;
            // 🎯 Chỉ preload nếu KHÔNG phải video đang phát
            if (prev >= 0 && prev != anchorPosition && !preloadedPlayers.containsKey(prev)) {
                try {
                    String url = getVideoUrl(videos.get(prev));
                    if (url != null && !url.isEmpty()) {
                        preloadedPlayers.put(prev, createPreloadPlayer(url));
                    }
                } catch (Exception e) {
                    android.util.Log.e("VideoAdapter", "Error preloading video at position " + prev, e);
                }
            }
        }
    }

    // ================== ViewHolder ==================
    public class VideoViewHolder extends RecyclerView.ViewHolder {

        // Video
        private final PlayerView playerView;
        private final ImageView posterImageView;
        private final ImageView playPauseOverlay;
        private final View loadingView;

        // Metadata
        private final TextView titleTextView;
        private final TextView captionTextView;
        private final TextView viewCountTextView;
        private final TextView likeCountTextView;
        private final TextView uploadDateTextView;

        // Actions
        private final LinearLayout likeButton, shareButton, commentButton;
        private final ImageView likeIcon;
        private final ImageView menuButton;

        // Double-tap animation
        private final ImageView doubleTapHeart;
        private GestureDetector gestureDetector;

        // Seek indicators
        private final TextView fastForwardIndicator;
        private final TextView rewindIndicator;
        private boolean isSeeking = false;

        // UI visibility toggle (TikTok style)
        private final LinearLayout layoutVideoInfo;
        private final LinearLayout layoutActionButtons;
        private final ImageView btnShowUI;
        private boolean isUIVisible = true;

        // State tracking cho optimistic UI
        private boolean isLikedLocally = false;
        private boolean isLikeOperationPending = false;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);

            // Video
            playerView = itemView.findViewById(R.id.player_view);
            posterImageView = itemView.findViewById(R.id.iv_poster);
            playPauseOverlay = itemView.findViewById(R.id.play_pause_overlay);
            loadingView = itemView.findViewById(R.id.loading_view);

            // Metadata
            titleTextView = itemView.findViewById(R.id.tv_title);
            captionTextView = itemView.findViewById(R.id.tv_caption);
            viewCountTextView = itemView.findViewById(R.id.tv_view_count);
            uploadDateTextView = itemView.findViewById(R.id.tv_upload_date);
            likeCountTextView = itemView.findViewById(R.id.tv_like_count);

            // Actions
            likeButton = itemView.findViewById(R.id.btn_like);
            shareButton = itemView.findViewById(R.id.btn_share);
            commentButton = itemView.findViewById(R.id.btn_comment);
            likeIcon = itemView.findViewById(R.id.iv_like_icon);
            doubleTapHeart = itemView.findViewById(R.id.iv_double_tap_heart);
            menuButton = itemView.findViewById(R.id.btn_menu);

            // Seek indicators
            fastForwardIndicator = itemView.findViewById(R.id.tv_fast_forward);
            rewindIndicator = itemView.findViewById(R.id.tv_rewind);

            // UI containers for visibility toggle
            layoutVideoInfo = itemView.findViewById(R.id.layout_video_info);
            layoutActionButtons = itemView.findViewById(R.id.layout_action_buttons);
            btnShowUI = itemView.findViewById(R.id.btn_show_ui);

            setupDoubleTapGesture();
            setupSeekGestures();
            setupShowUIButton();

            // Buttons
            likeButton.setOnClickListener(v -> {
                int p = getBindingAdapterPosition();
                if (p != RecyclerView.NO_POSITION && !isLikeOperationPending) {
                    handleLikeClick(videos.get(p), p);
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

            // Menu button
            if (menuButton != null) {
                menuButton.setOnClickListener(v -> {
                    int p = getBindingAdapterPosition();
                    if (p != RecyclerView.NO_POSITION && listener != null) {
                        listener.onMenuClick(videos.get(p), p);
                    }
                });
            }
        }

        private void setupDoubleTapGesture() {
            gestureDetector = new GestureDetector(itemView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    // Single tap = toggle play/pause (thay vì toggle UI để tránh conflict)
                    // UI visibility giờ được điều khiển qua dialog options
                    togglePlayPause();
                    return true;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    // Double tap = like với animation
                    int p = getBindingAdapterPosition();
                    if (p != RecyclerView.NO_POSITION && !isLikeOperationPending) {
                        handleDoubleTapLike(videos.get(p), p, e.getX(), e.getY());
                    }
                    return true;
                }

                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }
            });
        }

        /**
         * Toggle visibility của UI elements (caption, icons) như TikTok
         */
        private void toggleUIVisibility() {
            isUIVisible = !isUIVisible;

            // Animate fade in/out
            if (layoutVideoInfo != null) {
                if (isUIVisible) {
                    layoutVideoInfo.setAlpha(0f);
                    layoutVideoInfo.setVisibility(View.VISIBLE);
                    layoutVideoInfo.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start();
                } else {
                    layoutVideoInfo.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> layoutVideoInfo.setVisibility(View.GONE))
                        .start();
                }
            }

            if (layoutActionButtons != null) {
                if (isUIVisible) {
                    layoutActionButtons.setAlpha(0f);
                    layoutActionButtons.setVisibility(View.VISIBLE);
                    layoutActionButtons.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start();
                } else {
                    layoutActionButtons.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> layoutActionButtons.setVisibility(View.GONE))
                        .start();
                }
            }
        }

        /**
         * Thiết lập hiển thị/ẩn UI từ bên ngoài (từ dialog options)
         * @param visible true để hiển thị UI, false để ẩn UI
         */
        void setUIVisibility(boolean visible) {
            isUIVisible = visible;

            // Animate fade in/out cho layoutVideoInfo
            if (layoutVideoInfo != null) {
                if (isUIVisible) {
                    layoutVideoInfo.setAlpha(0f);
                    layoutVideoInfo.setVisibility(View.VISIBLE);
                    layoutVideoInfo.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start();
                } else {
                    layoutVideoInfo.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> layoutVideoInfo.setVisibility(View.GONE))
                        .start();
                }
            }

            // Animate fade in/out cho layoutActionButtons
            if (layoutActionButtons != null) {
                if (isUIVisible) {
                    layoutActionButtons.setAlpha(0f);
                    layoutActionButtons.setVisibility(View.VISIBLE);
                    layoutActionButtons.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start();
                } else {
                    layoutActionButtons.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> layoutActionButtons.setVisibility(View.GONE))
                        .start();
                }
            }

            // Hiển thị/ẩn nút Show UI (ngược lại với UI visibility)
            if (btnShowUI != null) {
                if (isUIVisible) {
                    // UI đang hiển thị -> ẩn nút Show UI
                    btnShowUI.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> btnShowUI.setVisibility(View.GONE))
                        .start();
                } else {
                    // UI đang ẩn -> hiển thị nút Show UI
                    btnShowUI.setAlpha(0f);
                    btnShowUI.setVisibility(View.VISIBLE);
                    btnShowUI.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start();
                }
            }
        }

        /**
         * Setup nút Show UI - hiển thị khi UI bị ẩn (TikTok style)
         */
        private void setupShowUIButton() {
            if (btnShowUI != null) {
                btnShowUI.setOnClickListener(v -> {
                    // Khi click nút Show UI -> hiển thị lại UI
                    setUIVisibility(true);
                });
            }
        }

        /**
         * Setup seek gestures for fast forward and rewind
         * Nhấn giữ cạnh phải: tua nhanh x2
         * Nhấn giữ cạnh trái: tua lùi x2
         */
        private void setupSeekGestures() {
            View videoTapArea = itemView.findViewById(R.id.video_tap_area);
            View touchView = videoTapArea != null ? videoTapArea : playerView;

            touchView.setOnTouchListener(new View.OnTouchListener() {
                private float initialX = 0;
                private boolean isLongPressHandled = false;
                private final Handler handler = new Handler(Looper.getMainLooper());
                private Runnable longPressRunnable;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Cho gesture detector xử lý trước (single tap, double tap)
                    boolean gestureHandled = gestureDetector.onTouchEvent(event);

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = event.getX();
                            isLongPressHandled = false;

                            // Setup long press cho dialog (nhấn giữ vào giữa màn hình)
                            float screenWidth = v.getWidth();
                            float centerZoneStart = screenWidth * 0.3f;
                            float centerZoneEnd = screenWidth * 0.7f;

                            if (initialX >= centerZoneStart && initialX <= centerZoneEnd) {
                                // Nhấn giữ vào giữa -> hiển thị menu dialog
                                longPressRunnable = () -> {
                                    isLongPressHandled = true;
                                    int p = getBindingAdapterPosition();
                                    if (p != RecyclerView.NO_POSITION && listener != null) {
                                        listener.onMenuClick(videos.get(p), p);
                                    }
                                };
                                handler.postDelayed(longPressRunnable, 500); // 500ms để kích hoạt long press
                            } else {
                                // Nhấn giữ hai cạnh -> tua video
                                longPressRunnable = () -> {
                                    if (currentPlayer != null && getBindingAdapterPosition() == currentPlayingPosition) {
                                        isLongPressHandled = true;
                                        boolean isRightSide = initialX > screenWidth / 2;
                                        startSeeking(isRightSide);
                                    }
                                };
                                handler.postDelayed(longPressRunnable, 200); // 200ms để bắt đầu tua
                            }
                            break;

                        case MotionEvent.ACTION_MOVE:
                            // Nếu di chuyển quá xa khỏi vị trí ban đầu, hủy long press
                            if (Math.abs(event.getX() - initialX) > 50) {
                                if (longPressRunnable != null) {
                                    handler.removeCallbacks(longPressRunnable);
                                }
                            }
                            break;

                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            if (longPressRunnable != null) {
                                handler.removeCallbacks(longPressRunnable);
                            }
                            if (isSeeking) {
                                stopSeeking();
                            }
                            // Nếu long press đã xử lý, không cho gesture detector xử lý nữa
                            return isLongPressHandled || gestureHandled;
                    }

                    return gestureHandled || isLongPressHandled;
                }
            });
        }

        /**
         * Bắt đầu tăng tốc độ phát video x2
         */
        private void startSeeking(boolean forward) {
            if (currentPlayer == null || isSeeking) return;

            isSeeking = true;

            // Hiển thị indicator
            if (forward) {
                if (fastForwardIndicator != null) {
                    fastForwardIndicator.setVisibility(View.VISIBLE);
                }
            } else {
                if (rewindIndicator != null) {
                    rewindIndicator.setVisibility(View.VISIBLE);
                }
            }

            // Tăng tốc độ phát lên x2 cho fast forward, giảm xuống x0.5 cho rewind
            float playbackSpeed = forward ? FAST_FORWARD_SPEED : REWIND_SPEED;
            currentPlayer.setPlaybackSpeed(playbackSpeed);
        }

        /**
         * Dừng tua video - đặt lại tốc độ phát về bình thường
         */
        private void stopSeeking() {
            isSeeking = false;

            // Đặt lại tốc độ phát về bình thường (1.0x)
            if (currentPlayer != null) {
                currentPlayer.setPlaybackSpeed(1.0f);
            }

            // Ẩn indicators
            if (fastForwardIndicator != null) {
                fastForwardIndicator.setVisibility(View.GONE);
            }
            if (rewindIndicator != null) {
                rewindIndicator.setVisibility(View.GONE);
            }
        }

        private void handleLikeClick(ShortVideo video, int position) {
            if (listener != null) {
                // Optimistic UI update
                updateLikeUIOptimistically(!isLikedLocally, video);

                // Call listener
                listener.onLikeClick(video, position);
            }
        }

        private void handleDoubleTapLike(ShortVideo video, int position, float x, float y) {
            // Nếu chưa like thì like, nếu đã like thì không làm gì
            if (!isLikedLocally && listener != null) {
                // Show heart animation
                showDoubleTapHeartAnimation(x, y);

                // Optimistic UI update
                updateLikeUIOptimistically(true, video);

                // Call listener
                listener.onLikeClick(video, position);
            }
        }

        private void showDoubleTapHeartAnimation(float x, float y) {
            if (doubleTapHeart == null) return;

            // Position heart at tap location
            doubleTapHeart.setX(x - doubleTapHeart.getWidth() / 2f);
            doubleTapHeart.setY(y - doubleTapHeart.getHeight() / 2f);

            doubleTapHeart.setVisibility(View.VISIBLE);
            doubleTapHeart.setScaleX(0f);
            doubleTapHeart.setScaleY(0f);
            doubleTapHeart.setAlpha(1f);

            // Animation: scale up và fade out
            doubleTapHeart.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .alpha(0f)
                .setDuration(800)
                .withEndAction(() -> doubleTapHeart.setVisibility(View.GONE))
                .start();
        }

        private void updateLikeUIOptimistically(boolean liked, ShortVideo video) {
            isLikedLocally = liked;
            isLikeOperationPending = true;

            // Update icon
            updateLikeIcon(liked);

            // Update count optimistically
            long currentCount = video.getLikeCount();
            long newCount = liked ? currentCount + 1 : Math.max(0, currentCount - 1);
            likeCountTextView.setText(formatCount(newCount));
        }

        public void updateLikeIcon(boolean liked) {
            if (likeIcon != null) {
                likeIcon.setImageResource(liked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
                likeIcon.setColorFilter(liked ?
                    ContextCompat.getColor(itemView.getContext(), R.color.like_color_active) :
                    ContextCompat.getColor(itemView.getContext(), R.color.like_color_inactive));
            }
        }

        public void revertLikeUI(ShortVideo video) {
            // Revert optimistic changes nếu operation thất bại
            isLikeOperationPending = false;

            // Revert icon
            updateLikeIcon(isLikedLocally);

            // Revert count
            likeCountTextView.setText(formatCount(video.getLikeCount()));
        }

        public void confirmLikeOperation(boolean newLikedState) {
            isLikedLocally = newLikedState;
            isLikeOperationPending = false;
        }

        void bindMetadata(ShortVideo video) {
            if (video == null) {
                android.util.Log.e("VideoAdapter", "bindMetadata called with null video");
                return;
            }

            titleTextView.setText(video.getTitle() != null ? video.getTitle() : "");

            // Caption + "Xem thêm"
            TextView seeMore = itemView.findViewById(R.id.tv_see_more);
            if (video.getCaption() == null || video.getCaption().trim().isEmpty()) {
                captionTextView.setVisibility(View.GONE);
                if (seeMore != null) seeMore.setVisibility(View.GONE);
            } else {
                String caption = video.getCaption().trim();
                captionTextView.setVisibility(View.VISIBLE);

                if (caption.length() > MAX_CAPTION_LENGTH) {
                    String shortCaption = caption.substring(0, MAX_CAPTION_LENGTH) + "...";

                    // Đặt về trạng thái thu gọn ban đầu
                    captionTextView.setMaxLines(2);
                    captionTextView.setText(shortCaption);

                    if (seeMore != null) {
                        seeMore.setVisibility(View.VISIBLE);
                        seeMore.setText("Xem thêm");
                        seeMore.setOnClickListener(v -> {
                            // Mở rộng caption - hiển thị toàn bộ
                            captionTextView.setMaxLines(Integer.MAX_VALUE);
                            captionTextView.setText(caption);
                            seeMore.setText("Thu gọn");

                            seeMore.setOnClickListener(v2 -> {
                                // Thu gọn caption
                                captionTextView.setMaxLines(2);
                                captionTextView.setText(shortCaption);
                                seeMore.setText("Xem thêm");

                                seeMore.setOnClickListener(v3 -> {
                                    // Mở rộng lại
                                    captionTextView.setMaxLines(Integer.MAX_VALUE);
                                    captionTextView.setText(caption);
                                    seeMore.setText("Thu gọn");
                                });
                            });
                        });
                    }
                } else {
                    captionTextView.setMaxLines(Integer.MAX_VALUE);
                    captionTextView.setText(caption);
                    if (seeMore != null) seeMore.setVisibility(View.GONE);
                }
            }

            // Counts
            viewCountTextView.setText(formatCount(video.getViewCount()) + " " + itemView.getContext().getString(R.string.views_count));
            likeCountTextView.setText(formatCount(video.getLikeCount()));

            // Upload date
            uploadDateTextView.setText(formatUploadDate(video.getUploadDate()));

            // Cập nhật trạng thái like từ Map thay vì mặc định
            int position = getBindingAdapterPosition();
            Boolean isLikedFromMap = likeStatusMap.get(position);
            if (isLikedFromMap != null) {
                isLikedLocally = isLikedFromMap;
                updateLikeIcon(isLikedFromMap);
            } else {
                // Mặc định chưa like nếu chưa có thông tin
                isLikedLocally = false;
                updateLikeIcon(false);
            }
            isLikeOperationPending = false;
        }

        private void togglePlayPause() {
            if (getBindingAdapterPosition() != currentPlayingPosition || currentPlayer == null) return;
            if (currentPlayer.isPlaying()) {
                currentPlayer.pause();
                showPlayOverlay(true);
            } else {
                // Nếu đã ENDED -> phát lại từ đầu
                if (currentPlayer.getPlaybackState() == Player.STATE_ENDED) {
                    currentPlayer.seekTo(0);
                }
                currentPlayer.play();
                showPlayOverlay(false);
            }
        }

        void showLoading(boolean show) {
            if (loadingView != null) loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        void showPoster(boolean show) {
            if (posterImageView != null) posterImageView.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        void showPlayOverlay(boolean show) {
            if (playPauseOverlay != null) playPauseOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        private String formatCount(long count) {
            if (count < 1000) return String.valueOf(count);
            if (count < 1_000_000) return String.format(Locale.getDefault(), "%.1fK", count / 1000f);
            return String.format(Locale.getDefault(), "%.1fM", count / 1_000_000f);
        }

        private String formatDate(long timestamp) {
            if (timestamp <= 0) return "";
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }

        private String formatUploadDate(long timestamp) {
            if (timestamp <= 0) return "";

            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            // Tính toán thời gian
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            long weeks = days / 7;
            long months = days / 30;
            long years = days / 365;

            if (years > 0) {
                return years + " " + itemView.getContext().getString(R.string.years_ago);
            } else if (months > 0) {
                return months + " " + itemView.getContext().getString(R.string.months_ago);
            } else if (weeks > 0) {
                return weeks + " " + itemView.getContext().getString(R.string.weeks_ago);
            } else if (days > 0) {
                return days + " " + itemView.getContext().getString(R.string.days_ago);
            } else if (hours > 0) {
                return hours + " " + itemView.getContext().getString(R.string.hours_ago);
            } else if (minutes > 0) {
                return minutes + " " + itemView.getContext().getString(R.string.minutes_ago);
            } else {
                return itemView.getContext().getString(R.string.just_now);
            }
        }
    }

    // ================== Helpers ==================
    private String getVideoUrl(ShortVideo v) {
        // 🎯 Sử dụng getVideoUrl() từ ShortVideo - nó đã có logic ưu tiên đúng
        String url = v.getVideoUrl();

        if (url != null && !url.isEmpty()) {
            Log.d("VideoAdapter", "📹 Video URL: " + url);
            return url;
        }

        // Fallback cuối cùng nếu không có URL nào
        Log.w("VideoAdapter", "⚠️ No video URL available for video " + v.getId() +
              " - cldPublicId: " + v.getCldPublicId() + ", cached videoUrl: " + v.getVideoUrl());
        return "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
    }

    /**
     * Lấy danh sách videos hiện tại
     * @return Danh sách videos
     */
    public List<ShortVideo> getVideos() {
        return videos;
    }

    /**
     * Cập nhật tốc độ phát video
     * @param speed Tốc độ phát (0.25f - 2.0f)
     */
    public void setPlaybackSpeed(float speed) {
        if (currentPlayer != null) {
            currentPlayer.setPlaybackSpeed(speed);
        }
    }
}
