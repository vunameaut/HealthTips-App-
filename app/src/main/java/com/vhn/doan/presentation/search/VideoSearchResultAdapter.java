package com.vhn.doan.presentation.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.utils.CloudinaryUrls;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter hiển thị kết quả tìm kiếm video với đầy đủ chức năng tương tác
 */
public class VideoSearchResultAdapter extends RecyclerView.Adapter<VideoSearchResultAdapter.VideoViewHolder> {
    private final Context mContext;
    private final List<ShortVideo> mVideos;
    private OnVideoClickListener mListener;
    private OnVideoInteractionListener mInteractionListener;

    /**
     * Interface lắng nghe sự kiện click vào video
     */
    public interface OnVideoClickListener {
        void onVideoClick(int position);
    }

    /**
     * Interface lắng nghe các tương tác khác với video (like, share, comment)
     */
    public interface OnVideoInteractionListener {
        void onLikeClicked(ShortVideo video, int position, boolean isLiked);
        void onShareClicked(ShortVideo video, int position);
        void onCommentClicked(ShortVideo video, int position);
    }

    public VideoSearchResultAdapter(Context context, List<ShortVideo> videos) {
        mContext = context;
        mVideos = videos;
    }

    public void setOnVideoClickListener(OnVideoClickListener listener) {
        mListener = listener;
    }

    public void setOnVideoInteractionListener(OnVideoInteractionListener listener) {
        mInteractionListener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_video_search_result, parent, false);
        return new VideoViewHolder(view, mListener, mInteractionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        ShortVideo video = mVideos.get(position);

        // Thiết lập dữ liệu cho ViewHolder
        holder.bind(video, position);
    }

    @Override
    public int getItemCount() {
        return mVideos.size();
    }

    /**
     * Cập nhật trạng thái like của video tại vị trí position
     */
    public void updateVideoLikeStatus(int position, boolean isLiked) {
        if (position >= 0 && position < mVideos.size()) {
            mVideos.get(position).setLiked(isLiked);
            notifyItemChanged(position, "LIKE_STATUS_CHANGED");
        }
    }

    /**
     * Hoàn tác trạng thái like UI (dùng khi có lỗi)
     */
    public void revertLikeUI(int position) {
        if (position >= 0 && position < mVideos.size()) {
            notifyItemChanged(position, "REVERT_LIKE_UI");
        }
    }

    /**
     * Format số lượt xem thành string dễ đọc
     */
    private String formatViewCount(long viewCount) {
        if (viewCount < 1000) {
            return String.valueOf(viewCount);
        } else if (viewCount < 1000000) {
            return String.format(Locale.getDefault(), "%.1fK", viewCount / 1000.0);
        } else {
            return String.format(Locale.getDefault(), "%.1fM", viewCount / 1000000.0);
        }
    }

    /**
     * Format thời gian đăng thành string dễ đọc
     */
    private String formatUploadDate(long uploadDateTimestamp) {
        if (uploadDateTimestamp == 0) {
            return "";
        }

        java.util.Date uploadDate = new java.util.Date(uploadDateTimestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return formatter.format(uploadDate);
    }

    /**
     * ViewHolder cho item video trong search results
     */
    static class VideoViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivVideoThumbnail;
        private final TextView tvVideoTitle;
        private final TextView tvVideoCaption;
        private final TextView tvViewCount;
        private final TextView tvUploadDate;
        private final ImageView ivLikeButton;
        private final TextView tvLikeCount;
        private final ImageView ivCommentButton;
        private final TextView tvCommentCount;
        private final ImageView ivShareButton;

        private final OnVideoClickListener clickListener;
        private final OnVideoInteractionListener interactionListener;

        public VideoViewHolder(@NonNull View itemView, OnVideoClickListener clickListener,
                              OnVideoInteractionListener interactionListener) {
            super(itemView);

            this.clickListener = clickListener;
            this.interactionListener = interactionListener;

            // Khởi tạo views
            ivVideoThumbnail = itemView.findViewById(R.id.iv_video_thumbnail);
            tvVideoTitle = itemView.findViewById(R.id.tv_video_title);
            tvVideoCaption = itemView.findViewById(R.id.tv_video_caption);
            tvViewCount = itemView.findViewById(R.id.tv_view_count);
            tvUploadDate = itemView.findViewById(R.id.tv_upload_date);
            ivLikeButton = itemView.findViewById(R.id.iv_like_button);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            ivCommentButton = itemView.findViewById(R.id.iv_comment_button);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count);
            ivShareButton = itemView.findViewById(R.id.iv_share_button);

            // Thiết lập click listener cho toàn bộ item
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onVideoClick(getAdapterPosition());
                }
            });
        }

        public void bind(ShortVideo video, int position) {
            // Thiết lập dữ liệu cơ bản
            tvVideoTitle.setText(video.getTitle());
            tvVideoCaption.setText(video.getCaption());

            // Hiển thị số lượt xem
            String viewCount = formatViewCount(video.getViewCount());
            tvViewCount.setText(viewCount + " lượt xem");

            // Hiển thị thời gian đăng
            String uploadDate = formatUploadDate(video.getUploadDate());
            tvUploadDate.setText(uploadDate);

            // Thiết lập trạng thái like
            updateLikeStatus(video.isLiked());
            tvLikeCount.setText(String.valueOf(video.getLikeCount()));

            // Thiết lập số lượng comment
            tvCommentCount.setText(String.valueOf(video.getCommentCount()));

            // Tải thumbnail của video
            loadVideoThumbnail(video);

            // Thiết lập click listeners cho các nút tương tác
            setupInteractionListeners(video, position);
        }

        private void loadVideoThumbnail(ShortVideo video) {
            Context context = itemView.getContext();

            if (video.getThumbnailUrl() != null && !video.getThumbnailUrl().isEmpty()) {
                // Nếu có thumbnail URL trực tiếp
                Glide.with(context)
                        .load(video.getThumbnailUrl())
                        .placeholder(R.drawable.ic_video_placeholder)
                        .error(R.drawable.ic_video_error)
                        .centerCrop()
                        .into(ivVideoThumbnail);
            } else if (video.getCldPublicId() != null && !video.getCldPublicId().isEmpty()) {
                // Nếu có Cloudinary public ID, tạo URL thumbnail từ đó
                String thumbnailUrl = CloudinaryUrls.poster(video.getCldPublicId(), video.getCldVersion());
                Glide.with(context)
                        .load(thumbnailUrl)
                        .placeholder(R.drawable.ic_video_placeholder)
                        .error(R.drawable.ic_video_error)
                        .centerCrop()
                        .into(ivVideoThumbnail);
            } else {
                // Hiển thị placeholder nếu không có thumbnail
                ivVideoThumbnail.setImageResource(R.drawable.ic_video_placeholder);
            }
        }

        private void setupInteractionListeners(ShortVideo video, int position) {
            // Like button
            ivLikeButton.setOnClickListener(v -> {
                if (interactionListener != null) {
                    boolean currentLikeStatus = video.isLiked();
                    // Cập nhật UI ngay lập tức để phản hồi nhanh
                    updateLikeStatus(!currentLikeStatus);
                    // Gọi listener
                    interactionListener.onLikeClicked(video, position, !currentLikeStatus);
                }
            });

            // Comment button
            ivCommentButton.setOnClickListener(v -> {
                if (interactionListener != null) {
                    interactionListener.onCommentClicked(video, position);
                }
            });

            // Share button
            ivShareButton.setOnClickListener(v -> {
                if (interactionListener != null) {
                    interactionListener.onShareClicked(video, position);
                }
            });
        }

        private void updateLikeStatus(boolean isLiked) {
            Context context = itemView.getContext();
            if (isLiked) {
                ivLikeButton.setImageResource(R.drawable.ic_heart_filled);
                ivLikeButton.setColorFilter(ContextCompat.getColor(context, R.color.like_active_color));
            } else {
                ivLikeButton.setImageResource(R.drawable.ic_heart_outline);
                ivLikeButton.setColorFilter(ContextCompat.getColor(context, R.color.like_inactive_color));
            }
        }

        private String formatViewCount(long viewCount) {
            if (viewCount < 1000) {
                return String.valueOf(viewCount);
            } else if (viewCount < 1000000) {
                return String.format(Locale.getDefault(), "%.1fK", viewCount / 1000.0);
            } else {
                return String.format(Locale.getDefault(), "%.1fM", viewCount / 1000000.0);
            }
        }

        private String formatUploadDate(long uploadDateTimestamp) {
            if (uploadDateTimestamp == 0) {
                return "";
            }

            java.util.Date uploadDate = new java.util.Date(uploadDateTimestamp);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return formatter.format(uploadDate);
        }
    }
}
