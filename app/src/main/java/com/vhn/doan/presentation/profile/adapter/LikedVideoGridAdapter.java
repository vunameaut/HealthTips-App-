package com.vhn.doan.presentation.profile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách video đã like dạng lưới 3 cột
 * Giao diện tương tự TikTok với tỷ lệ video dọc
 */
public class LikedVideoGridAdapter extends RecyclerView.Adapter<LikedVideoGridAdapter.VideoGridViewHolder> {

    private final List<ShortVideo> likedVideos = new ArrayList<>();
    private final Context context;
    private OnVideoClickListener onVideoClickListener;

    public interface OnVideoClickListener {
        void onVideoClick(int position, ShortVideo video);
    }

    public LikedVideoGridAdapter(Context context) {
        this.context = context;
    }

    public void setOnVideoClickListener(OnVideoClickListener listener) {
        this.onVideoClickListener = listener;
    }

    public void updateVideos(List<ShortVideo> videos) {
        android.util.Log.d("LikedVideoGridAdapter", "updateVideos được gọi với " + (videos != null ? videos.size() : 0) + " videos");

        this.likedVideos.clear();
        if (videos != null) {
            this.likedVideos.addAll(videos);
            android.util.Log.d("LikedVideoGridAdapter", "Đã thêm " + videos.size() + " videos vào adapter");
            for (int i = 0; i < videos.size(); i++) {
                ShortVideo video = videos.get(i);
                android.util.Log.d("LikedVideoGridAdapter", "Video " + i + ": " + video.getTitle() + " (ID: " + video.getId() + ")");
            }
        }

        android.util.Log.d("LikedVideoGridAdapter", "Gọi notifyDataSetChanged(), total items: " + getItemCount());
        notifyDataSetChanged();
    }

    public void removeVideo(int position) {
        if (position >= 0 && position < likedVideos.size()) {
            likedVideos.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, likedVideos.size());
        }
    }

    @NonNull
    @Override
    public VideoGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.util.Log.d("LikedVideoGridAdapter", "onCreateViewHolder được gọi, viewType: " + viewType);
        View view = LayoutInflater.from(context).inflate(R.layout.item_liked_video_grid, parent, false);
        android.util.Log.d("LikedVideoGridAdapter", "Layout inflated thành công: " + (view != null));
        return new VideoGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoGridViewHolder holder, int position) {
        android.util.Log.d("LikedVideoGridAdapter", "onBindViewHolder được gọi cho position: " + position);
        if (position < likedVideos.size()) {
            ShortVideo video = likedVideos.get(position);
            android.util.Log.d("LikedVideoGridAdapter", "Binding video: " + video.getTitle() + " tại position " + position);
            holder.bind(video, position);
        } else {
            android.util.Log.e("LikedVideoGridAdapter", "Position " + position + " out of bounds, size: " + likedVideos.size());
        }
    }

    @Override
    public int getItemCount() {
        int count = likedVideos.size();
        android.util.Log.d("LikedVideoGridAdapter", "getItemCount: " + count);
        return count;
    }

    class VideoGridViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailImageView;
        private final TextView durationTextView;
        private final TextView viewCountTextView;

        public VideoGridViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.img_video_thumbnail);
            durationTextView = itemView.findViewById(R.id.txt_video_duration);
            viewCountTextView = itemView.findViewById(R.id.txt_view_count);

            // Set click listener
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onVideoClickListener != null) {
                    onVideoClickListener.onVideoClick(position, likedVideos.get(position));
                }
            });
        }

        public void bind(ShortVideo video, int position) {
            android.util.Log.d("VideoGridViewHolder", "bind được gọi cho position " + position + " với video: " + video.getTitle());

            // Kiểm tra các view có tồn tại không
            android.util.Log.d("VideoGridViewHolder", "View states - thumbnail: " + (thumbnailImageView != null) +
                ", duration: " + (durationTextView != null) + ", viewCount: " + (viewCountTextView != null));

            // Load thumbnail với Glide
            if (video.getThumbnailUrl() != null && !video.getThumbnailUrl().isEmpty()) {
                android.util.Log.d("VideoGridViewHolder", "Loading thumbnail: " + video.getThumbnailUrl());
                Glide.with(context)
                        .load(video.getThumbnailUrl())
                        .transform(new CenterCrop(), new RoundedCorners(12))
                        .placeholder(R.drawable.placeholder_video_thumbnail)
                        .error(R.drawable.placeholder_video_thumbnail)
                        .into(thumbnailImageView);
            } else {
                android.util.Log.d("VideoGridViewHolder", "Không có thumbnail URL, sử dụng placeholder");
                // Fallback placeholder
                thumbnailImageView.setImageResource(R.drawable.placeholder_video_thumbnail);
            }

            // Hiển thị thời lượng video
            if (video.getDuration() > 0) {
                String duration = formatDuration(video.getDuration());
                android.util.Log.d("VideoGridViewHolder", "Setting duration: " + duration);
                durationTextView.setText(duration);
                durationTextView.setVisibility(View.VISIBLE);
            } else {
                android.util.Log.d("VideoGridViewHolder", "Duration = 0, ẩn duration text");
                durationTextView.setVisibility(View.GONE);
            }

            // Hiển thị số lượt xem
            if (video.getViewCount() > 0) {
                String viewCount = formatViewCount(video.getViewCount());
                android.util.Log.d("VideoGridViewHolder", "Setting view count: " + viewCount);
                viewCountTextView.setText(viewCount);
                viewCountTextView.setVisibility(View.VISIBLE);
            } else {
                android.util.Log.d("VideoGridViewHolder", "ViewCount = 0, ẩn view count text");
                viewCountTextView.setVisibility(View.GONE);
            }

            android.util.Log.d("VideoGridViewHolder", "Hoàn thành bind cho position " + position);
        }
    }

    /**
     * Format thời lượng video thành định dạng MM:SS
     */
    private String formatDuration(long durationInSeconds) {
        long minutes = durationInSeconds / 60;
        long seconds = durationInSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Format số lượt xem thành định dạng ngắn gọn
     */
    private String formatViewCount(long viewCount) {
        if (viewCount >= 1000000) {
            return String.format("%.1fM", viewCount / 1000000.0);
        } else if (viewCount >= 1000) {
            return String.format("%.1fK", viewCount / 1000.0);
        } else {
            return String.valueOf(viewCount);
        }
    }
}
