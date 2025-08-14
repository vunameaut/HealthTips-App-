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

import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho RecyclerView hiển thị danh sách video ngắn
 * Sử dụng SingleExoPlayerManager để quản lý 1 ExoPlayer duy nhất cho toàn bộ feed
 * Giải quyết vấn đề "video #2 phải lướt qua rồi lướt lại mới play"
 */
public class ShortVideoAdapter extends RecyclerView.Adapter<ShortVideoAdapter.VideoViewHolder> {

    private final List<ShortVideo> videos;
    private final VideoInteractionListener listener;
    private int currentPlayingPosition = -1;
    private final List<VideoViewHolder> activeHolders = new ArrayList<>();

    // Sử dụng SingleExoPlayerManager thay vì ExoPlayerPreloadManager
    private SingleExoPlayerManager playerManager;
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
        // PlayerManager sẽ được khởi tạo khi onCreateViewHolder được gọi
    }

    // Constructor mới cho LikedVideoPlayerFragment (không cần listener)
    public ShortVideoAdapter(android.content.Context context, List<ShortVideo> videos) {
        this.videos = videos;
        this.mainHandler = new Handler(Looper.getMainLooper());
        // Khởi tạo player manager
        this.playerManager = new SingleExoPlayerManager(context);
        setupPlayerManagerListener();
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

    private void setupPlayerManagerListener() {
        if (playerManager != null) {
            playerManager.setVideoPlayerListener(new SingleExoPlayerManager.VideoPlayerListener() {
                @Override
                public void onVideoReady(int position) {
                    android.util.Log.d("ShortVideoAdapter", "Video ready at position: " + position);
                    if (listener != null) {
                        listener.onVideoPlayerReady();
                    }
                    // Gọi callback view count
                    if (position >= 0 && position < videos.size() && listener != null) {
                        ShortVideo video = videos.get(position);
                        listener.onVideoViewed(position, video.getId());
                    }
                }

                @Override
                public void onVideoEnded(int position) {
                    android.util.Log.d("ShortVideoAdapter", "Video ended at position: " + position);
                    // Video đã kết thúc, có thể tự động chuyển sang video tiếp theo hoặc loop
                }

                @Override
                public void onVideoError(int position, Exception error) {
                    android.util.Log.e("ShortVideoAdapter", "Video error at position " + position + ": " + error.getMessage());
                    // Xử lý lỗi video - có thể hiển thị placeholder hoặc skip video
                }
            });
        }
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_short_video_simple, parent, false);

        // Khởi tạo player manager nếu chưa có
        if (playerManager == null) {
            playerManager = new SingleExoPlayerManager(parent.getContext());
            setupPlayerManagerListener();
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
        // Dừng video hiện tại trước khi update
        if (playerManager != null) {
            playerManager.detachFromPlayerView();
        }
        currentPlayingPosition = -1;

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
        android.util.Log.d("ShortVideoAdapter", "Playing video at position: " + position);

        if (position < 0 || position >= videos.size()) {
            android.util.Log.w("ShortVideoAdapter", "Invalid position: " + position);
            return;
        }

        // Nếu đang phát video khác, detach trước
        if (currentPlayingPosition != -1 && currentPlayingPosition != position) {
            if (playerManager != null) {
                playerManager.detachFromPlayerView();
            }
            // Notify item cũ để ẩn player view
            safeNotifyItemChanged(currentPlayingPosition, "pause");
        }

        currentPlayingPosition = position;

        // Tìm ViewHolder cho position này
        VideoViewHolder targetHolder = findViewHolderForPosition(position);
        if (targetHolder != null && targetHolder.playerView != null) {
            // Attach player manager vào PlayerView của ViewHolder
            if (playerManager != null) {
                playerManager.attachToPlayerView(targetHolder.playerView, videos, position);
            }
            // Notify để hiển thị player view
            safeNotifyItemChanged(position, "play");
        } else {
            android.util.Log.w("ShortVideoAdapter", "ViewHolder not found for position: " + position);
            // Nếu không tìm thấy ViewHolder, chỉ update position và notify
            safeNotifyItemChanged(position, "play");
        }
    }

    private VideoViewHolder findViewHolderForPosition(int position) {
        for (VideoViewHolder holder : activeHolders) {
            if (holder.getAdapterPosition() == position) {
                return holder;
            }
        }
        return null;
    }

    public void pauseVideoAt(int position) {
        if (position >= 0 && position < getItemCount()) {
            if (playerManager != null && currentPlayingPosition == position) {
                playerManager.pause();
            }
            safeNotifyItemChanged(position, "pause");
        }
    }

    public void pauseCurrentVideo() {
        if (currentPlayingPosition != -1) {
            if (playerManager != null) {
                playerManager.pause();
            }
            safeNotifyItemChanged(currentPlayingPosition, "pause");
        }
    }

    public void resumeCurrentVideo() {
        if (currentPlayingPosition != -1) {
            if (playerManager != null) {
                playerManager.play();
            }
            safeNotifyItemChanged(currentPlayingPosition, "resume");
        }
    }

    public void pauseAllVideos() {
        if (playerManager != null) {
            playerManager.detachFromPlayerView();
        }
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

        // Release player manager resources
        if (playerManager != null) {
            playerManager.release();
            playerManager = null;
        }
    }

    // Thêm các method còn thiếu cho LikedVideoPlayerFragment
    public void updateData(List<ShortVideo> newVideos) {
        // Release player manager trước khi update data
        if (playerManager != null) {
            playerManager.detachFromPlayerView();
        }
        currentPlayingPosition = -1;

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
     * Method để trigger khi người dùng scroll đến vị trí mới
     * Gọi từ Fragment/Activity khi detect scroll
     */
    public void onScrollToPosition(int position) {
        if (position >= 0 && position < videos.size()) {
            android.util.Log.d("ShortVideoAdapter", "Scrolled to position: " + position);
            // Tự động phát video tại vị trí mới
            playVideoAt(position);
        }
    }

    /**
     * Lấy thông tin debug về player manager
     */
    public String getPlayerDebugInfo() {
        if (playerManager == null) {
            return "Player Manager: null";
        }
        return playerManager.getDebugInfo();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VideoViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (!activeHolders.contains(holder)) {
            activeHolders.add(holder);
        }

        // Nếu đây là ViewHolder cho vị trí hiện tại đang phát, attach player
        int position = holder.getAdapterPosition();
        if (position == currentPlayingPosition && playerManager != null && holder.playerView != null) {
            playerManager.attachToPlayerView(holder.playerView, videos, position);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull VideoViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.releaseResources();
        activeHolders.remove(holder);

        // Nếu đây là ViewHolder cho vị trí hiện tại đang phát, detach player
        int position = holder.getAdapterPosition();
        if (position == currentPlayingPosition && playerManager != null) {
            playerManager.detachFromPlayerView();
        }
    }

    @Override
    public void onViewRecycled(@NonNull VideoViewHolder holder) {
        super.onViewRecycled(holder);
        holder.releaseResources();
        activeHolders.remove(holder);
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {
        private PlayerView playerView;
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
            android.util.Log.d("VideoViewHolder", "Binding video at position: " + position + " - " + video.getTitle());

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

            // QUAN TRỌNG: Reset UI state trước khi setup video
            resetPlayerViewState();

            // Setup video - Sử dụng URL được tối ưu cho mobile
            String optimizedVideoUrl = video.getOptimizedVideoUrl();
            android.util.Log.d("ShortVideoAdapter", "Loading video at position " + position + " - Original: " + video.getVideoUrl());
            android.util.Log.d("ShortVideoAdapter", "Loading video at position " + position + " - Optimized: " + optimizedVideoUrl);
            setupVideo(optimizedVideoUrl);
        }

        /**
         * Reset trạng thái UI trước khi setup video mới
         */
        private void resetPlayerViewState() {
            android.util.Log.d("VideoViewHolder", "Resetting player view state");

            // Reset các trạng thái
            isVideoLoaded = false;

            // Ẩn các element UI
            imgThumbnail.setVisibility(View.GONE);
            imgPlayPause.setVisibility(View.GONE);

            // Ẩn PlayerView và hiển thị loading
            playerView.setVisibility(View.INVISIBLE);
            showLoadingIndicator(true);
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

            // Nếu đang phát video khác, detach trước
            if (currentPlayingPosition != -1) {
                if (playerManager != null) {
                    playerManager.detachFromPlayerView();
                }
                // Notify item cũ để ẩn player view
                safeNotifyItemChanged(currentPlayingPosition, "pause");
            }

            currentPlayingPosition = getAdapterPosition();

            // Tìm ViewHolder cho position này
            VideoViewHolder targetHolder = findViewHolderForPosition(currentPlayingPosition);
            if (targetHolder != null && targetHolder.playerView != null) {
                // Attach player manager vào PlayerView của ViewHolder
                if (playerManager != null) {
                    playerManager.attachToPlayerView(targetHolder.playerView, videos, currentPlayingPosition);
                }
                // Notify để hiển thị player view
                safeNotifyItemChanged(currentPlayingPosition, "play");
            } else {
                android.util.Log.w("ShortVideoAdapter", "ViewHolder not found for position: " + currentPlayingPosition);
                // Nếu không tìm thấy ViewHolder, chỉ update position và notify
                safeNotifyItemChanged(currentPlayingPosition, "play");
            }
        }

        private void createNewPlayer(String videoUrl) {
            int position = getAdapterPosition();
            android.util.Log.d("VideoViewHolder", "=== Creating new player for position: " + position + " ===");
            android.util.Log.d("VideoViewHolder", "Video URL: " + videoUrl);

            try {
                // Kiểm tra URL trước khi tạo player
                if (videoUrl == null || videoUrl.isEmpty()) {
                    android.util.Log.e("VideoViewHolder", "FATAL: Video URL is null/empty for position " + position);
                    showErrorMessage("URL video không hợp lệ cho video " + (position + 1));
                    return;
                }

                // Kiểm tra định dạng URL
                if (!videoUrl.startsWith("http://") && !videoUrl.startsWith("https://") && !videoUrl.startsWith("content://")) {
                    android.util.Log.e("VideoViewHolder", "FATAL: Invalid URL format for position " + position + ": " + videoUrl);
                    showErrorMessage("Định dạng URL không hợp lệ cho video " + (position + 1));
                    return;
                }

                // Chỉ cần gọi attachToPlayerView với position đúng
                if (playerManager != null) {
                    playerManager.attachToPlayerView(playerView, videos, position);
                }

                android.util.Log.d("VideoViewHolder", "Player attached successfully for position " + position);

            } catch (Exception e) {
                android.util.Log.e("VideoViewHolder", "CRITICAL ERROR creating player for position " + position + ": " + e.getMessage());
                e.printStackTrace();
                showErrorMessage("Lỗi nghiêm trọng khi tạo video player cho video " + (position + 1) + ": " + e.getMessage());

                // Fallback: Thử tạo lại player với delay
                itemView.postDelayed(() -> {
                    android.util.Log.d("VideoViewHolder", "Attempting fallback player creation for position " + position);
                    retryCreatePlayer(videoUrl, position);
                }, 1000);
            }
        }

        /**
         * Phương thức fallback để thử tạo lại player khi có lỗi
         */
        private void retryCreatePlayer(String videoUrl, int position) {
            try {
                android.util.Log.d("VideoViewHolder", "Retry attempt for position " + position);

                // Release player cũ nếu có
                if (playerManager != null) {
                    playerManager.detachFromPlayerView();
                }

                // Đơn giản hóa tạo player
                if (playerManager != null) {
                    playerManager.attachToPlayerView(playerView, videos, position);
                }

                android.util.Log.d("VideoViewHolder", "Retry successful for position " + position);

            } catch (Exception retryError) {
                android.util.Log.e("VideoViewHolder", "Retry also failed for position " + position + ": " + retryError.getMessage());
                showErrorMessage("Không thể phát video " + (position + 1) + " sau khi thử lại");

                // Hiển thị thông báo lỗi cho user
                showVideoErrorState(position);
            }
        }

        /**
         * Hiển thị trạng thái lỗi cho video
         */
        private void showVideoErrorState(int position) {
            showLoadingIndicator(false);
            playerView.setVisibility(View.GONE);

            // Hiển thị message lỗi persistent
            TextView errorMessage = itemView.findViewById(R.id.errorMessage);
            if (errorMessage != null) {
                errorMessage.setText("Video " + (position + 1) + " không thể phát. Vui lòng thử lại sau.");
                errorMessage.setVisibility(View.VISIBLE);
                errorMessage.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.black));
                errorMessage.setTextColor(itemView.getContext().getResources().getColor(android.R.color.white));
                errorMessage.setPadding(32, 32, 32, 32);
            }
        }

        private void togglePlayPause() {
            if (playerManager != null && playerManager.isPlaying()) {
                playerManager.pause();
                imgPlayPause.setVisibility(View.VISIBLE);
            } else {
                playerManager.play();
                imgPlayPause.setVisibility(View.GONE);
                imgThumbnail.setVisibility(View.GONE);
            }
        }

        private void playVideo() {
            if (playerManager != null) {
                playerManager.play();
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
            if (playerManager != null && playerManager.isPlaying()) {
                playerManager.pause();
                imgPlayPause.setVisibility(View.VISIBLE);
            }
        }

        private void resumeVideo() {
            if (playerManager != null && !playerManager.isPlaying()) {
                playerManager.play();
                imgPlayPause.setVisibility(View.GONE);
            }
        }

        public void releaseResources() {
            if (playerManager != null) {
                playerManager.detachFromPlayerView();
            }
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

            if (playerManager != null && playerManager.isPlaying()) {
                try {
                    playerManager.pause();
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
