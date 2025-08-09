package com.vhn.doan.presentation.shortvideo;

import android.animation.ObjectAnimator;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.services.CloudinaryVideoHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho RecyclerView hiển thị danh sách video ngắn
 * Tối ưu hóa cho hiệu ứng vuốt giống TikTok/Facebook Reels
 * Sử dụng TextureView để loại bỏ letterbox và hiển thị video full màn hình
 */
public class ShortVideoAdapter extends RecyclerView.Adapter<ShortVideoAdapter.VideoViewHolder> {

    private final List<ShortVideo> videos;
    private final VideoInteractionListener listener;
    private int currentPlayingPosition = -1;

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

    public void updateVideoLike(int position, boolean isLiked, int newLikeCount) {
        if (position >= 0 && position < videos.size()) {
            ShortVideo video = videos.get(position);
            video.setLikeCount(newLikeCount);
            video.setLikedByCurrentUser(isLiked);
            notifyItemChanged(position, "like_update");
        }
    }

    public void updateVideoView(int position, int newViewCount) {
        if (position >= 0 && position < videos.size()) {
            videos.get(position).setViewCount(newViewCount);
            notifyItemChanged(position, "view_update");
        }
    }

    public void playVideoAt(int position) {
        if (currentPlayingPosition != -1 && currentPlayingPosition != position) {
            notifyItemChanged(currentPlayingPosition, "pause");
        }
        currentPlayingPosition = position;
        notifyItemChanged(position, "play");
    }

    public void pauseVideoAt(int position) {
        if (position >= 0 && position < getItemCount()) {
            notifyItemChanged(position, "pause");
        }
    }

    public void pauseCurrentVideo() {
        if (currentPlayingPosition != -1) {
            notifyItemChanged(currentPlayingPosition, "pause");
        }
    }

    public void resumeCurrentVideo() {
        if (currentPlayingPosition != -1) {
            notifyItemChanged(currentPlayingPosition, "resume");
        }
    }

    public void pauseAllVideos() {
        for (int i = 0; i < getItemCount(); i++) {
            notifyItemChanged(i, "pause");
        }
        currentPlayingPosition = -1;
    }

    public void hideAllVideoViews() {
        for (int i = 0; i < getItemCount(); i++) {
            notifyItemChanged(i, "hide_video");
        }
    }

    public void showAllVideoViews() {
        for (int i = 0; i < getItemCount(); i++) {
            notifyItemChanged(i, "show_video");
        }
    }

    public void releaseAllResources() {
        // Release tất cả MediaPlayer instances để tránh memory leak
        for (int i = 0; i < getItemCount(); i++) {
            notifyItemChanged(i, "release_resources");
        }
        currentPlayingPosition = -1;
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder implements TextureView.SurfaceTextureListener {
        private TextureView textureView;
        private MediaPlayer mediaPlayer;
        private Surface surface;
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
        private boolean isTextureAvailable = false;
        private String pendingVideoUrl = null;
        private boolean isCaptionExpanded = false;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            setupGestureDetector();
            setupClickListeners();
            setupTextureView();
        }

        private void initViews() {
            textureView = itemView.findViewById(R.id.textureView);
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
        }

        private void setupTextureView() {
            textureView.setSurfaceTextureListener(this);
        }

        private void setupGestureDetector() {
            gestureDetector = new GestureDetector(itemView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
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

            textureView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
        }

        // Implement TextureView.SurfaceTextureListener
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            surface = new Surface(surfaceTexture);
            isTextureAvailable = true;

            // Nếu có video đang chờ được load, load ngay
            if (pendingVideoUrl != null) {
                setupVideo(pendingVideoUrl);
                pendingVideoUrl = null;
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            adjustVideoSize();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            releaseMediaPlayer();
            if (surface != null) {
                surface.release();
                surface = null;
            }
            isTextureAvailable = false;
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            // Không cần xử lý gì
        }

        private void adjustVideoSize() {
            if (mediaPlayer == null || textureView == null) return;

            try {
                int videoWidth = mediaPlayer.getVideoWidth();
                int videoHeight = mediaPlayer.getVideoHeight();
                int viewWidth = textureView.getWidth();
                int viewHeight = textureView.getHeight();

                if (videoWidth == 0 || videoHeight == 0 || viewWidth == 0 || viewHeight == 0) {
                    return;
                }

                // Tính toán tỷ lệ khung hình
                float videoAspectRatio = (float) videoWidth / videoHeight;
                float viewAspectRatio = (float) viewWidth / viewHeight;

                float scaleX = 1.0f;
                float scaleY = 1.0f;

                // Logic mới: Scale để fill màn hình NHƯNG ưu tiên giữ nguyên nội dung
                if (videoAspectRatio > viewAspectRatio) {
                    // Video rộng hơn -> scale theo width, có thể có letterbox nhỏ trên/dưới
                    scaleX = (float) viewWidth / videoWidth;
                    scaleY = scaleX;
                } else {
                    // Video cao hơn -> scale theo height, có thể có letterbox nhỏ trái/phải
                    scaleY = (float) viewHeight / videoHeight;
                    scaleX = scaleY;
                }

                // Tăng scale một chút để loại bỏ letterbox nhưng không crop quá nhiều
                float adjustmentFactor = 1.05f; // Tăng 5% để loại bỏ letterbox
                scaleX *= adjustmentFactor;
                scaleY *= adjustmentFactor;

                // Đảm bảo không scale quá mức
                float maxScale = 1.2f; // Giới hạn scale tối đa
                scaleX = Math.min(scaleX, maxScale);
                scaleY = Math.min(scaleY, maxScale);

                // Tính toán kích thước sau scale
                float scaledWidth = videoWidth * scaleX;
                float scaledHeight = videoHeight * scaleY;

                // Tính translation để center video
                float translationX = (viewWidth - scaledWidth) / 2f;
                float translationY = (viewHeight - scaledHeight) / 2f;

                // Tạo Matrix
                Matrix matrix = new Matrix();
                matrix.reset();

                // Áp dụng scale từ center
                matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f);

                // Thêm translation để center hoàn hảo
                matrix.postTranslate(translationX, translationY);

                textureView.setTransform(matrix);

                android.util.Log.d("VideoAdjust", String.format(
                    "Video: %dx%d (ratio:%.2f), View: %dx%d (ratio:%.2f), Scale: %.2fx%.2f, Scaled: %.1fx%.1f, Translation: %.1f,%.1f",
                    videoWidth, videoHeight, videoAspectRatio,
                    viewWidth, viewHeight, viewAspectRatio,
                    scaleX, scaleY, scaledWidth, scaledHeight, translationX, translationY));

            } catch (Exception e) {
                android.util.Log.e("VideoAdjust", "Error adjusting video size", e);
            }
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

            // Load thumbnail - Sử dụng Cloudinary thumbnail nếu có
            String thumbnailUrl = video.getThumbnailUrl();
            if (video.isCloudinaryVideo() && (thumbnailUrl == null || thumbnailUrl.isEmpty())) {
                // Tự động tạo thumbnail từ video URL nếu chưa có
                thumbnailUrl = CloudinaryVideoHelper.getThumbnailFromVideoUrl(video.getVideoUrl());
            }

            Glide.with(itemView.getContext())
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.ic_video_placeholder)
                    .error(R.drawable.ic_video_error)
                    .centerCrop()
                    .into(imgThumbnail);

            // Setup video - Sử dụng URL được tối ưu cho mobile
            String optimizedVideoUrl = video.getOptimizedVideoUrl();
            android.util.Log.d("ShortVideoAdapter", "Loading video - Original: " + video.getVideoUrl());
            android.util.Log.d("ShortVideoAdapter", "Loading video - Optimized: " + optimizedVideoUrl);
            setupVideo(optimizedVideoUrl);

            // Reset trạng thái
            isVideoLoaded = false;
            imgPlayPause.setVisibility(View.VISIBLE);
            imgThumbnail.setVisibility(View.VISIBLE);
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
                } else if ("release_resources".equals(payload)) {
                    releaseAllPlayerResources();
                }
            }
        }

        private void setupVideo(String videoUrl) {
            if (videoUrl == null || videoUrl.isEmpty()) {
                return;
            }

            // Nếu TextureView chưa sẵn sàng, lưu URL để setup sau
            if (!isTextureAvailable || surface == null) {
                pendingVideoUrl = videoUrl;
                return;
            }

            try {
                // Giải phóng MediaPlayer cũ nếu có
                releaseMediaPlayer();

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(itemView.getContext(), Uri.parse(videoUrl));

                // Quan trọng: Set surface TRƯỚC khi prepare
                mediaPlayer.setSurface(surface);

                mediaPlayer.setOnPreparedListener(mp -> {
                    isVideoLoaded = true;
                    mp.setLooping(true);

                    // Điều chỉnh kích thước video sau khi prepared
                    adjustVideoSize();

                    // Tự động phát nếu đây là video hiện tại
                    int position = getAdapterPosition();
                    if (position == currentPlayingPosition) {
                        playVideo();
                    }

                    // Gọi callback thông báo video player đã sẵn sàng
                    if (listener != null) {
                        listener.onVideoPlayerReady();
                    }
                });

                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    // Hiển thị thumbnail khi có lỗi
                    imgThumbnail.setVisibility(View.VISIBLE);
                    imgPlayPause.setVisibility(View.VISIBLE);
                    return true;
                });

                mediaPlayer.setOnVideoSizeChangedListener((mp, width, height) -> {
                    // Điều chỉnh lại kích thước khi video size thay đổi
                    adjustVideoSize();
                });

                mediaPlayer.prepareAsync();

            } catch (IOException e) {
                e.printStackTrace();
                // Hiển thị thumbnail khi có lỗi
                imgThumbnail.setVisibility(View.VISIBLE);
                imgPlayPause.setVisibility(View.VISIBLE);
            }
        }

        private void togglePlayPause() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                pauseVideo();
            } else {
                playVideo();
            }
        }

        private void playVideo() {
            if (isVideoLoaded && mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                imgPlayPause.setVisibility(View.GONE);
                imgThumbnail.setVisibility(View.GONE);

                // Thông báo về việc xem video
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
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                imgPlayPause.setVisibility(View.VISIBLE);
            }
        }

        private void resumeVideo() {
            if (isVideoLoaded && mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                imgPlayPause.setVisibility(View.GONE);
            }
        }

        private void releaseMediaPlayer() {
            if (mediaPlayer != null) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.release();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mediaPlayer = null;
                    isVideoLoaded = false;
                }
            }
        }

        private void releaseAllPlayerResources() {
            // Release MediaPlayer hoàn toàn
            releaseMediaPlayer();

            // Reset tất cả trạng thái
            isVideoLoaded = false;
            isTextureAvailable = false;
            pendingVideoUrl = null;

            // Hiện thumbnail và ẩn TextureView
            if (imgThumbnail != null) {
                imgThumbnail.setVisibility(View.VISIBLE);
            }
            if (imgPlayPause != null) {
                imgPlayPause.setVisibility(View.VISIBLE);
            }
            if (textureView != null) {
                textureView.setVisibility(View.GONE);
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
            // Ẩn TextureView ngay lập tức và hiện thumbnail để tránh nháy
            if (textureView != null) {
                textureView.setVisibility(View.INVISIBLE); // Dùng INVISIBLE thay vì GONE để tránh layout shift
            }
            if (imgThumbnail != null) {
                imgThumbnail.setVisibility(View.VISIBLE);
            }
            if (imgPlayPause != null) {
                imgPlayPause.setVisibility(View.VISIBLE);
            }

            // Dừng video nếu đang phát
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                try {
                    mediaPlayer.pause();
                } catch (Exception e) {
                    // Ignore exception khi pause
                }
            }
        }

        private void showVideoView() {
            // Chỉ hiện TextureView khi thực sự cần thiết
            if (textureView != null && isVideoLoaded) {
                textureView.setVisibility(View.VISIBLE);
            }
        }
    }
}
