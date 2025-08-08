package com.vhn.doan.presentation.shortvideo;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho RecyclerView hiển thị danh sách video ngắn
 * Tối ưu hóa cho hiệu ứng vuốt giống TikTok/Facebook Reels
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
            videos.get(position).setLikeCount(newLikeCount);
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

    public class VideoViewHolder extends RecyclerView.ViewHolder {
        private VideoView videoView;
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

        private boolean isLiked = false;
        private boolean isVideoLoaded = false;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            setupClickListeners();
        }

        private void initViews() {
            videoView = itemView.findViewById(R.id.videoView);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
            imgPlayPause = itemView.findViewById(R.id.imgPlayPause);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtCaption = itemView.findViewById(R.id.txtCaption);
            txtViewCount = itemView.findViewById(R.id.txtViewCount);
            txtLikeCount = itemView.findViewById(R.id.txtLikeCount);
            txtUploadDate = itemView.findViewById(R.id.txtUploadDate);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnComment = itemView.findViewById(R.id.btnComment);


        }

        private void setupClickListeners() {
            // Click video để play/pause
            videoView.setOnClickListener(v -> togglePlayPause());
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
            txtViewCount.setText(formatCount(video.getViewCount()) + " lượt xem");
            txtLikeCount.setText(formatCount(video.getLikeCount()));

            // Format ngày upload
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            txtUploadDate.setText(sdf.format(new Date(video.getUploadDate())));



            // Load thumbnail
            Glide.with(itemView.getContext())
                    .load(video.getThumbnailUrl())
                    .placeholder(R.drawable.ic_video_placeholder)
                    .error(R.drawable.ic_video_error)
                    .centerCrop()
                    .into(imgThumbnail);



            // Setup video
            setupVideo(video.getVideoUrl());

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
                    updateLikeButton();
                } else if ("view_update".equals(payload)) {
                    txtViewCount.setText(formatCount(video.getViewCount()) + " lượt xem");
                } else if ("play".equals(payload)) {
                    playVideo();
                } else if ("pause".equals(payload)) {
                    pauseVideo();
                } else if ("resume".equals(payload)) {
                    resumeVideo();
                }
            }
        }

        private void setupVideo(String videoUrl) {
            if (videoUrl != null && !videoUrl.isEmpty()) {
                videoView.setVideoURI(Uri.parse(videoUrl));

                videoView.setOnPreparedListener(mediaPlayer -> {
                    isVideoLoaded = true;
                    mediaPlayer.setLooping(true);

                    // Tự động phát nếu đây là video hiện tại
                    int position = getAdapterPosition();
                    if (position == currentPlayingPosition) {
                        playVideo();
                    }
                });

                videoView.setOnErrorListener((mediaPlayer, what, extra) -> {
                    // Hiển thị thumbnail khi có lỗi
                    imgThumbnail.setVisibility(View.VISIBLE);
                    imgPlayPause.setVisibility(View.VISIBLE);
                    return true;
                });
            }
        }

        private void togglePlayPause() {
            if (videoView.isPlaying()) {
                pauseVideo();
            } else {
                playVideo();
            }
        }

        private void playVideo() {
            if (isVideoLoaded && !videoView.isPlaying()) {
                videoView.start();
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
            if (videoView.isPlaying()) {
                videoView.pause();
                imgPlayPause.setVisibility(View.VISIBLE);
            }
        }

        private void resumeVideo() {
            if (isVideoLoaded && !videoView.isPlaying()) {
                videoView.start();
                imgPlayPause.setVisibility(View.GONE);
            }
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
    }
}
