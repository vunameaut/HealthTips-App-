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
            thumbnailImageView = itemView.findViewById(R.id.imageViewThumbnail);
            durationTextView = itemView.findViewById(R.id.textViewDuration);
            viewCountTextView = itemView.findViewById(R.id.textViewViewCount);
        }

        public void bind(ShortVideo video, int position) {
            android.util.Log.d("LikedVideoGridAdapter", "Bắt đầu bind cho video: " + video.getId());

            // Hiển thị thumbnail
            loadThumbnail(video);

            // Hiển thị duration nếu có
            if (durationTextView != null && video.getDuration() != null && video.getDuration() > 0) {
                String durationText = formatDuration(video.getDuration());
                durationTextView.setText(durationText);
                durationTextView.setVisibility(View.VISIBLE);
            } else if (durationTextView != null) {
                durationTextView.setVisibility(View.GONE);
            }

            // Hiển thị view count
            if (viewCountTextView != null) {
                String viewCountText = formatViewCount(video.getViewCount());
                viewCountTextView.setText(viewCountText);
                viewCountTextView.setVisibility(View.VISIBLE);
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (onVideoClickListener != null) {
                    android.util.Log.d("LikedVideoGridAdapter", "Video clicked at position: " + position);
                    onVideoClickListener.onVideoClick(position, video);
                }
            });
        }

        private void loadThumbnail(ShortVideo video) {
            if (thumbnailImageView == null) {
                android.util.Log.w("LikedVideoGridAdapter", "thumbnailImageView is null");
                return;
            }

            String thumbnailUrl = getThumbnailUrl(video);
            android.util.Log.d("LikedVideoGridAdapter", "Loading thumbnail URL: " + thumbnailUrl + " cho video: " + video.getId());

            if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                Glide.with(context)
                        .load(thumbnailUrl)
                        .apply(new com.bumptech.glide.request.RequestOptions()
                                .transform(new CenterCrop(), new RoundedCorners(12))
                                .placeholder(R.drawable.video_placeholder)
                                .error(R.drawable.video_error_placeholder))
                        .into(thumbnailImageView);
                android.util.Log.d("LikedVideoGridAdapter", "Glide load thumbnail thành công cho video: " + video.getId());
            } else {
                android.util.Log.w("LikedVideoGridAdapter", "Thumbnail URL null hoặc empty cho video: " + video.getId());
                thumbnailImageView.setImageResource(R.drawable.video_placeholder);
            }
        }

        private String getThumbnailUrl(ShortVideo video) {
            // Thử các cách lấy thumbnail URL theo thứ tự ưu tiên

            // 1. Từ thumbnailUrl field
            String thumbnailUrl = video.getThumbnailUrl();
            if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                android.util.Log.d("LikedVideoGridAdapter", "Sử dụng thumbnailUrl field: " + thumbnailUrl);
                return thumbnailUrl;
            }

            // 2. Từ thumb field (có thể là URL ngắn gọn)
            String thumb = video.getThumb();
            if (thumb != null && !thumb.isEmpty()) {
                android.util.Log.d("LikedVideoGridAdapter", "Sử dụng thumb field: " + thumb);
                return thumb;
            }

            // 3. Tạo thumbnail từ Cloudinary nếu có cldPublicId
            String cldPublicId = video.getCldPublicId();
            if (cldPublicId != null && !cldPublicId.isEmpty()) {
                String generatedThumbnail = video.getThumbnailUrlFromCloudinary();
                android.util.Log.d("LikedVideoGridAdapter", "Tạo thumbnail từ Cloudinary: " + generatedThumbnail);
                return generatedThumbnail;
            }

            android.util.Log.w("LikedVideoGridAdapter", "Không tìm thấy thumbnail cho video: " + video.getId());
            return null;
        }

        private String formatDuration(Long duration) {
            if (duration == null || duration <= 0) {
                return "";
            }

            long seconds = duration;
            long minutes = seconds / 60;
            seconds = seconds % 60;

            if (minutes > 0) {
                return String.format("%d:%02d", minutes, seconds);
            } else {
                return String.format("0:%02d", seconds);
            }
        }

        private String formatViewCount(Long viewCount) {
            if (viewCount == null || viewCount <= 0) {
                return "0";
            }

            if (viewCount < 1000) {
                return String.valueOf(viewCount);
            } else if (viewCount < 1000000) {
                return String.format("%.1fK", viewCount / 1000.0);
            } else {
                return String.format("%.1fM", viewCount / 1000000.0);
            }
        }
    }
}
