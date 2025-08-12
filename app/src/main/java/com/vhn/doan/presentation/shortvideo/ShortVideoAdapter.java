package com.vhn.doan.presentation.shortvideo;

import android.animation.ObjectAnimator;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho RecyclerView hiển thị danh sách video ngắn
 * Tối ưu hóa cho hiệu ứng vuốt giống TikTok/Facebook Reels
 * Sử dụng ExoPlayer để hiển thị video đúng tỷ lệ mà không bị crop
 */
public class ShortVideoAdapter extends RecyclerView.Adapter<ShortVideoAdapter.VideoViewHolder> {

    private final List<ShortVideo> videos;
    private final VideoInteractionListener listener;
    private int currentPlayingPosition = -1;
    private final List<VideoViewHolder> activeHolders = new ArrayList<>();

    // Thêm ExoPlayerPreloadManager và Handler
    private ExoPlayerPreloadManager preloadManager;
    private Handler mainHandler;
    private RecyclerView attachedRecyclerView;

    public interface VideoInteractionListener {
        void onVideoLiked(int position, String videoId, boolean isCurrentlyLiked);
        void onVideoShared(int position, String videoId);
        void onVideoCommented(int position, String videoId);
        void onVideoViewed(int position, String videoId);
        void onVideoProfileClicked(int position, String userId);
        void onVideoPlayerReady(); // Callback khi video player sẵn sàng
    }

    public ShortVideoAdapter(List<ShortVideo> videos, VideoInteractionListener listener) {
        this.videos = videos;
        this.listener = listener;
        this.mainHandler = new Handler(Looper.getMainLooper());
        // Context sẽ được set khi onCreateViewHolder được gọi
    }

    // Constructor mới cho LikedVideoPlayerFragment (không cần listener)
    public ShortVideoAdapter(android.content.Context context, List<ShortVideo> videos) {
        this.videos = videos;
        this.mainHandler = new Handler(Looper.getMainLooper());
        // Khởi tạo preload manager
        this.preloadManager = new ExoPlayerPreloadManager(context);
        this.listener = new VideoInteractionListener() {
            @Override
            public void onVideoLiked(int position, String videoId, boolean isCurrentlyLiked) {
                // Default implementation - có thể để trống hoặc hiển thị Toast
            }

            @Override
            public void onVideoShared(int position, String videoId) {
                // Default implementation
            }

            @Override
            public void onVideoCommented(int position, String videoId) {
                // Default implementation
            }

            @Override
            public void onVideoViewed(int position, String videoId) {
                // Default implementation
            }

            @Override
            public void onVideoProfileClicked(int position, String userId) {
                // Default implementation
            }

            @Override
            public void onVideoPlayerReady() {
                // Default implementation
            }
        };
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_short_video_simple, parent, false);

        // Khởi tạo preload manager nếu chưa có
        if (preloadManager == null) {
            preloadManager = new ExoPlayerPreloadManager(parent.getContext());
        }

        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        ShortVideo video = videos.get(position);
        holder.bind(video, position);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            ShortVideo video = videos.get(position);
            holder.bind(video, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public void updateVideos(List<ShortVideo> newVideos) {
        this.videos.clear();
       this.videos.addAll(newVideos);
       notifyDataSetChanged();
    }

    public void addVideos(List<ShortVideo> newVideos) {
        if (newVideos == null || newVideos.isEmpty()) {
            return;
        }
        int start = this.videos.size();
        this.videos.addAll(newVideos);
        notifyItemRangeInserted(start, newVideos.size());
    }

    /**
     * Method an toàn để notify item changed, tránh conflict với RecyclerView scroll
     */
    private void safeNotifyItemChanged(int position, Object payload) {
        if (attachedRecyclerView != null && attachedRecyclerView.isComputingLayout()) {
            // Delay notify nếu RecyclerView đang computing layout
            mainHandler.post(() -> safeNotifyItemChanged(position, payload));
            return;
        }

        try {
            if (payload != null) {
                notifyItemChanged(position, payload);
            } else {
                notifyItemChanged(position);
            }
        } catch (Exception e) {
            android.util.Log.w("ShortVideoAdapter", "Error notifying item changed: " + e.getMessage());
            // Retry sau delay nhỏ
            mainHandler.postDelayed(() -> {
                try {
                    if (payload != null) {
                        notifyItemChanged(position, payload);
                    } else {
                        notifyItemChanged(position);
                    }
                } catch (Exception retryError) {
                    android.util.Log.e("ShortVideoAdapter", "Retry notify failed: " + retryError.getMessage());
                }
            }, 50);
        }
    }

    public void updateVideoLike(int position, boolean isLiked, int newLikeCount) {
        if (position >= 0 && position < videos.size()) {
            ShortVideo video = videos.get(position);
            video.setLikeCount(newLikeCount);
            video.setLikedByCurrentUser(isLiked);
            safeNotifyItemChanged(position, "like_update");
        }
    }

    public void updateVideoView(int position, int newViewCount) {
        if (position >= 0 && position < videos.size()) {
            videos.get(position).setViewCount(newViewCount);
            safeNotifyItemChanged(position, "view_update");
        }
    }

    public void playVideoAt(int position) {
        if (currentPlayingPosition != -1 && currentPlayingPosition != position) {
            safeNotifyItemChanged(currentPlayingPosition, "pause");
        }
        currentPlayingPosition = position;
        safeNotifyItemChanged(position, "play");
    }

    public void pauseVideoAt(int position) {
        if (position >= 0 && position < getItemCount()) {
            safeNotifyItemChanged(position, "pause");
        }
    }

    public void pauseCurrentVideo() {
        if (currentPlayingPosition != -1) {
            safeNotifyItemChanged(currentPlayingPosition, "pause");
        }
    }

    public void resumeCurrentVideo() {
        if (currentPlayingPosition != -1) {
            safeNotifyItemChanged(currentPlayingPosition, "resume");
        }
    }

    public void pauseAllVideos() {
        for (int i = 0; i < getItemCount(); i++) {
            safeNotifyItemChanged(i, "pause");
        }
        currentPlayingPosition = -1;
    }

    public void hideAllVideoViews() {
        for (int i = 0; i < getItemCount(); i++) {
            safeNotifyItemChanged(i, "hide_video");
        }
    }

    public void showAllVideoViews() {
        for (int i = 0; i < getItemCount(); i++) {
            safeNotifyItemChanged(i, "show_video");
        }
    }

    public void releaseAllResources() {
        for (VideoViewHolder holder : new ArrayList<>(activeHolders)) {
            holder.releaseResources();
        }
        activeHolders.clear();
        currentPlayingPosition = -1;

        // Release preload manager resources
        if (preloadManager != null) {
            preloadManager.releaseAllResources();
        }
    }

    // Thêm các method còn thiếu cho LikedVideoPlayerFragment
    public void updateData(List<ShortVideo> newVideos) {
        // Release preload manager trước khi update data
        if (preloadManager != null) {
            preloadManager.releaseAllResources();
        }

        this.videos.clear();
        if (newVideos != null) {
            this.videos.addAll(newVideos);
        }
        notifyDataSetChanged();
    }

    public void releasePlayer() {
        releaseAllResources();
    }

    /**
     * Method để trigger preload khi người dùng scroll đến vị trí mới
     * Gọi từ Fragment/Activity khi detect scroll
     */
    public void onScrollToPosition(int position) {
        if (preloadManager != null && position >= 0 && position < videos.size()) {
            android.util.Log.d("ShortVideoAdapter", "Scrolled to position: " + position + ", triggering preload");
            // Setup current video và preload xung quanh
            preloadManager.setupCurrentVideo(videos, position);
        }
    }

    /**
     * Lấy thông tin debug về preload manager
     */
    public String getPreloadDebugInfo() {
        if (preloadManager == null) {
            return "Preload Manager: null";
        }
        return String.format("Preload Manager - Current: %d, Preloaded: %d",
                preloadManager.getCurrentPosition(),
                preloadManager.getPreloadedCount());
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VideoViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (!activeHolders.contains(holder)) {
            activeHolders.add(holder);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull VideoViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.releaseResources();
        activeHolders.remove(holder);
    }

    @Override
    public void onViewRecycled(@NonNull VideoViewHolder holder) {
        super.onViewRecycled(holder);
        holder.releaseResources();
        activeHolders.remove(holder);
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {
        private PlayerView playerView;
        private ExoPlayer exoPlayer;
        private ImageView imgThumbnail;
        private ImageView imgPlayPause;
        private TextView txtTitle;
        private TextView txtCaption;
        private TextView txtViewCount;
        private TextView txtLikeCount;
        private TextView txtUploadDate;
        private ImageView btnLike;
        private ImageView btnShare;
        private ImageView btnComment;
        private TextView btnSeeMore;
        private TextView txtHashtags;
        private FrameLayout rootLayout;
        private GestureDetector gestureDetector;

        private boolean isLiked = false;
        private boolean isVideoLoaded = false;
        private boolean isCaptionExpanded = false;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            setupGestureDetector();
            setupClickListeners();
        }

        private void initViews() {
            playerView = itemView.findViewById(R.id.playerView);
            if (playerView != null) {
                playerView.setUseController(false);
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            }
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
            imgPlayPause = itemView.findViewById(R.id.imgPlayPause);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtCaption = itemView.findViewById(R.id.txtCaption);
            txtViewCount = itemView.findViewById(R.id.txtViewCount);
            txtLikeCount = itemView.findViewById(R.id.txtLikeCount);
            txtUploadDate = itemView.findViewById(R.id.txtUploadDate);
            btnSeeMore = itemView.findViewById(R.id.btnSeeMore);
            txtHashtags = itemView.findViewById(R.id.txtHashtags);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnComment = itemView.findViewById(R.id.btnComment);
            rootLayout = (FrameLayout) itemView;

            // Log để debug
            android.util.Log.d("VideoViewHolder", "Views initialized - PlayerView: " + (playerView != null));
        }

        private void setupGestureDetector() {
            gestureDetector = new GestureDetector(itemView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    // Cho phép nhận các sự kiện tiếp theo
                    return true;
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    togglePlayPause();
                    return true;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    showHeartAnimation(e.getX(), e.getY());
                    if (!isLiked && listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            ShortVideo video = videos.get(position);
                            listener.onVideoLiked(position, video.getId(), isLiked);
                        }
                    }
                    return true;
                }
            });
            playerView.setOnTouchListener((v, event) -> {
                // Sinh tim liên tục khi người dùng chạm/di chuyển trên màn hình
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        showHeartAnimation(event.getX(), event.getY());
                        break;
                }
                return gestureDetector.onTouchEvent(event);
            });
        }



        private void setupClickListeners() {
            // Click video play/pause handled by gesture detector
            imgPlayPause.setOnClickListener(v -> togglePlayPause());

            // Click like button
            btnLike.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        ShortVideo video = videos.get(position);
                        listener.onVideoLiked(position, video.getId(), isLiked);
                        // Cập nhật trạng thái local ngay để tránh tăng tim liên tục
                        isLiked = !isLiked;
                        updateLikeButton();
                    }
                }
            });

            // Click share button
            btnShare.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        ShortVideo video = videos.get(position);
                        listener.onVideoShared(position, video.getId());
                    }
                }
            });

            // Click comment button
            btnComment.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        ShortVideo video = videos.get(position);
                        listener.onVideoCommented(position, video.getId());
                    }
                }
            });

            // Loại bỏ click listeners cho user info vì không còn tồn tại
            // imgUserAvatar và txtUsername đã bị ẩn hoàn toàn
        }

        public void bind(ShortVideo video, int position) {
            // Hiển thị thông tin video
            txtTitle.setText(video.getTitle());
            txtCaption.setText(video.getCaption());
            btnSeeMore.setVisibility(View.GONE);
            isCaptionExpanded = false;
            txtCaption.post(() -> {
                if (txtCaption.getLineCount() > 2) {
                    txtCaption.setMaxLines(2);
                    btnSeeMore.setVisibility(View.VISIBLE);
                } else {
                    txtCaption.setMaxLines(Integer.MAX_VALUE);
                    btnSeeMore.setVisibility(View.GONE);
                }
            });
            btnSeeMore.setOnClickListener(v -> {
                txtCaption.setMaxLines(Integer.MAX_VALUE);
                btnSeeMore.setVisibility(View.GONE);
                isCaptionExpanded = true;
            });
            txtCaption.setOnClickListener(v -> {
                if (isCaptionExpanded) {
                    txtCaption.setMaxLines(2);
                    btnSeeMore.setVisibility(View.VISIBLE);
                    isCaptionExpanded = false;
                }
            });

            if (video.getTags() != null && !video.getTags().isEmpty()) {
                StringBuilder tagsLine = new StringBuilder();
                for (String tag : video.getTags().keySet()) {
                    tagsLine.append("#").append(tag).append(" ");
                }
                txtHashtags.setText(tagsLine.toString().trim());
                txtHashtags.setVisibility(View.VISIBLE);
            } else {
                txtHashtags.setVisibility(View.GONE);
            }
            txtViewCount.setText(formatCount(video.getViewCount()) + " lượt xem");
            txtLikeCount.setText(formatCount(video.getLikeCount()));
            isLiked = video.isLikedByCurrentUser();
            updateLikeButton();

            // Format ngày upload
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            txtUploadDate.setText(sdf.format(new Date(video.getUploadDate())));

            // Ẩn thumbnail để video phát ngay khi hiển thị
            imgThumbnail.setVisibility(View.GONE);
            playerView.setVisibility(View.INVISIBLE);

            // Setup video - Sử dụng URL được tối ưu cho mobile
            String optimizedVideoUrl = video.getOptimizedVideoUrl();
            android.util.Log.d("ShortVideoAdapter", "Loading video - Original: " + video.getVideoUrl());
            android.util.Log.d("ShortVideoAdapter", "Loading video - Optimized: " + optimizedVideoUrl);
            setupVideo(optimizedVideoUrl);

            // Reset trạng thái
            isVideoLoaded = false;
            imgPlayPause.setVisibility(View.GONE);
        }

        public void bind(ShortVideo video, int position, List<Object> payloads) {
            if (payloads.isEmpty()) {
                bind(video, position);
                return;
            }

            for (Object payload : payloads) {
                if ("like_update".equals(payload)) {
                    txtLikeCount.setText(formatCount(video.getLikeCount()));
                    isLiked = video.isLikedByCurrentUser();
                    updateLikeButton();
                } else if ("view_update".equals(payload)) {
                    txtViewCount.setText(formatCount(video.getViewCount()) + " lượt xem");
                } else if ("play".equals(payload)) {
                    showVideoView();
                    playVideo();
                } else if ("pause".equals(payload)) {
                    pauseVideo();
                } else if ("resume".equals(payload)) {
                    showVideoView();
                    resumeVideo();
                } else if ("hide_video".equals(payload)) {
                    hideVideoView();
                } else if ("show_video".equals(payload)) {
                    showVideoView();
                }
            }
        }

        private void setupVideo(String videoUrl) {
            android.util.Log.d("VideoViewHolder", "setupVideo called with URL: " + videoUrl);

            if (videoUrl == null || videoUrl.isEmpty()) {
                android.util.Log.e("VideoViewHolder", "Video URL is null or empty");
                showErrorMessage("URL video không hợp lệ");
                return;
            }

            releasePlayer();

            try {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    android.util.Log.w("VideoViewHolder", "Invalid position, creating new player");
                    createNewPlayer(videoUrl);
                    return;
                }

                // Luôn tạo player mới để đảm bảo ổn định
                android.util.Log.d("VideoViewHolder", "Creating new player for position: " + position);
                createNewPlayer(videoUrl);

                // Chỉ thử preload manager như một optimization, không bắt buộc
                if (preloadManager != null) {
                    try {
                        preloadManager.schedulePreload(videos, position);
                    } catch (Exception e) {
                        android.util.Log.w("VideoViewHolder", "Preload scheduling failed: " + e.getMessage());
                    }
                }

            } catch (Exception e) {
                android.util.Log.e("VideoViewHolder", "Error setting up video: " + e.getMessage());
                showErrorMessage("Lỗi khi thiết lập video: " + e.getMessage());
            }
        }

        private void createNewPlayer(String videoUrl) {
            try {
                exoPlayer = new ExoPlayer.Builder(itemView.getContext()).build();
                playerView.setPlayer(exoPlayer);

                MediaItem item = MediaItem.fromUri(videoUrl);
                exoPlayer.setMediaItem(item);
                exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);

                setupPlayerListener();
                exoPlayer.prepare();
                android.util.Log.d("VideoViewHolder", "New ExoPlayer created and prepared");

            } catch (Exception e) {
                android.util.Log.e("VideoViewHolder", "Error creating new player: " + e.getMessage());
                showErrorMessage("Lỗi khi tạo video player: " + e.getMessage());
            }
        }

        private void setupPlayerListener() {
            if (exoPlayer == null) return;

            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    android.util.Log.d("VideoViewHolder", "Playback state changed: " + state);

                    switch (state) {
                        case Player.STATE_BUFFERING:
                            android.util.Log.d("VideoViewHolder", "Video buffering...");
                            showLoadingIndicator(true);
                            break;

                        case Player.STATE_READY:
                            android.util.Log.d("VideoViewHolder", "Video ready to play");
                            isVideoLoaded = true;
                            showLoadingIndicator(false);
                            playerView.setVisibility(View.VISIBLE);

                            if (listener != null) {
                                listener.onVideoPlayerReady();
                            }

                            // Auto play nếu đây là video hiện tại
                            int position = getAdapterPosition();
                            if (position == currentPlayingPosition || position == 0) {
                                playVideo();
                            }
                            break;

                        case Player.STATE_ENDED:
                            android.util.Log.d("VideoViewHolder", "Video ended");
                            exoPlayer.seekTo(0);
                            exoPlayer.play();
                            break;

                        case Player.STATE_IDLE:
                            android.util.Log.d("VideoViewHolder", "Player idle");
                            break;
                    }
                }

                @Override
                public void onPlayerError(com.google.android.exoplayer2.PlaybackException error) {
                    android.util.Log.e("VideoViewHolder", "Player error: " + error.getMessage());
                    showErrorMessage("Không thể phát video: " + error.getMessage());
                    showLoadingIndicator(false);
                }
            });
        }

        private void togglePlayPause() {
            if (exoPlayer != null && exoPlayer.isPlaying()) {
                pauseVideo();
            } else {
                playVideo();
            }
        }

        private void playVideo() {
            if (exoPlayer != null) {
                exoPlayer.play();
                imgPlayPause.setVisibility(View.GONE);
                imgThumbnail.setVisibility(View.GONE);
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        ShortVideo video = videos.get(position);
                        listener.onVideoViewed(position, video.getId());
                    }
                }
            }
        }

        private void pauseVideo() {
            if (exoPlayer != null && exoPlayer.isPlaying()) {
                exoPlayer.pause();
                imgPlayPause.setVisibility(View.VISIBLE);
            }
        }

        private void resumeVideo() {
            if (exoPlayer != null && !exoPlayer.isPlaying()) {
                exoPlayer.play();
                imgPlayPause.setVisibility(View.GONE);
            }
        }

        private void releasePlayer() {
            if (exoPlayer != null) {
                exoPlayer.release();
                exoPlayer = null;
                isVideoLoaded = false;
            }
        }

        public void releaseResources() {
            releasePlayer();
            isVideoLoaded = false;
            if (imgThumbnail != null) {
                imgThumbnail.setVisibility(View.GONE);
            }
            if (imgPlayPause != null) {
                imgPlayPause.setVisibility(View.GONE);
            }
            if (playerView != null) {
                playerView.setVisibility(View.INVISIBLE);
            }
        }

        private void showHeartAnimation(float x, float y) {
            if (rootLayout == null) return;
            ImageView heart = new ImageView(itemView.getContext());
            heart.setImageResource(R.drawable.ic_heart_filled);
            heart.setColorFilter(itemView.getContext().getResources().getColor(R.color.color_like));
            // Tăng kích thước trái tim gấp ~15 lần để dễ nhìn hơn
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(300, 300);
            params.leftMargin = (int) x - 150;
            params.topMargin = (int) y - 300;
            rootLayout.addView(heart, params);

            heart.setScaleX(0f);
            heart.setScaleY(0f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(heart, View.SCALE_X, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(heart, View.SCALE_Y, 1f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(heart, View.ALPHA, 0f);
            ObjectAnimator translate = ObjectAnimator.ofFloat(heart, View.TRANSLATION_Y, -300f);
            scaleX.setDuration(600);
            scaleY.setDuration(600);
            alpha.setDuration(600);
            translate.setDuration(600);
            scaleX.start();
            scaleY.start();
            translate.start();
            alpha.start();
            alpha.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    rootLayout.removeView(heart);
                }
            });
        }

        private void updateLikeButton() {
            if (isLiked) {
                btnLike.setImageResource(R.drawable.ic_heart_filled);
                btnLike.setColorFilter(itemView.getContext().getResources().getColor(R.color.color_like));
            } else {
                btnLike.setImageResource(R.drawable.ic_heart_outline);
                btnLike.setColorFilter(itemView.getContext().getResources().getColor(R.color.white));
            }
        }

        private String formatCount(int count) {
            if (count < 1000) {
                return String.valueOf(count);
            } else if (count < 1000000) {
                return String.format(Locale.getDefault(), "%.1fK", count / 1000.0);
            } else {
                return String.format(Locale.getDefault(), "%.1fM", count / 1000000.0);
            }
        }

        private void hideVideoView() {
            if (playerView != null) {
                playerView.setVisibility(View.INVISIBLE);
            }
            if (imgThumbnail != null) {
                imgThumbnail.setVisibility(View.GONE);
            }
            if (imgPlayPause != null) {
                imgPlayPause.setVisibility(View.GONE);
            }

            if (exoPlayer != null && exoPlayer.isPlaying()) {
                try {
                    exoPlayer.pause();
                } catch (Exception ignored) {
                }
            }
        }

        private void showVideoView() {
            if (playerView != null) {
                playerView.setVisibility(View.VISIBLE);
            }
        }

        private void showLoadingIndicator(boolean show) {
            View loadingProgressBar = itemView.findViewById(R.id.loadingProgressBar);
            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }

        private void showErrorMessage(String message) {
            TextView errorMessage = itemView.findViewById(R.id.errorMessage);
            if (errorMessage != null) {
                errorMessage.setText(message);
                errorMessage.setVisibility(View.VISIBLE);

                // Ẩn error message sau 5 giây
                errorMessage.postDelayed(() -> {
                    if (errorMessage != null) {
                        errorMessage.setVisibility(View.GONE);
                    }
                }, 5000);
            }
            android.util.Log.e("VideoViewHolder", message);
        }
    }

    /**
     * Set RecyclerView reference để kiểm tra trạng thái layout
     * Gọi method này trong Fragment/Activity sau khi khởi tạo adapter
     */
    public void setRecyclerView(RecyclerView recyclerView) {
        this.attachedRecyclerView = recyclerView;
    }
}
