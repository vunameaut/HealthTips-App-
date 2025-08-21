package com.vhn.doan.presentation.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
 * Adapter hiển thị kết quả tìm kiếm video
 */
public class VideoSearchResultAdapter extends RecyclerView.Adapter<VideoSearchResultAdapter.VideoViewHolder> {
    private final Context mContext;
    private final List<ShortVideo> mVideos;
    private OnVideoClickListener mListener;

    /**
     * Interface lắng nghe sự kiện click vào video
     */
    public interface OnVideoClickListener {
        void onVideoClick(int position);
    }

    public VideoSearchResultAdapter(Context context, List<ShortVideo> videos) {
        mContext = context;
        mVideos = videos;
    }

    public void setOnVideoClickListener(OnVideoClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_video_search_result, parent, false);
        return new VideoViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        ShortVideo video = mVideos.get(position);

        // Thiết lập dữ liệu cho ViewHolder
        holder.tvVideoTitle.setText(video.getTitle());
        holder.tvVideoCaption.setText(video.getCaption());

        // Hiển thị số lượt xem
        String viewCount = formatViewCount(video.getViewCount());
        holder.tvViewCount.setText(viewCount);

        // Hiển thị thời gian đăng
        String uploadDate = formatUploadDate(video.getUploadDate());
        holder.tvUploadDate.setText(uploadDate);

        // Tải thumbnail của video
        if (video.getThumbnailUrl() != null && !video.getThumbnailUrl().isEmpty()) {
            // Nếu có thumbnail URL trực tiếp
            Glide.with(mContext)
                    .load(video.getThumbnailUrl())
                    .placeholder(R.drawable.ic_video_placeholder)
                    .error(R.drawable.ic_video_error)
                    .centerCrop()
                    .into(holder.ivVideoThumbnail);
        } else if (video.getCldPublicId() != null && !video.getCldPublicId().isEmpty()) {
            // Nếu có Cloudinary public ID, tạo URL thumbnail từ đó
            String thumbnailUrl = CloudinaryUrls.poster(video.getCldPublicId(), video.getCldVersion());
            Glide.with(mContext)
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.ic_video_placeholder)
                    .error(R.drawable.ic_video_error)
                    .centerCrop()
                    .into(holder.ivVideoThumbnail);
        } else {
            // Hiển thị placeholder nếu không có ảnh
            holder.ivVideoThumbnail.setImageResource(R.drawable.ic_video_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return mVideos != null ? mVideos.size() : 0;
    }

    /**
     * Format số lượt xem thành chuỗi đẹp hơn (vd: 1.2K, 4.5M)
     * @param viewCount Số lượt xem
     * @return Chuỗi đã format
     */
    private String formatViewCount(long viewCount) {
        if (viewCount < 1000) {
            return viewCount + " " + mContext.getString(R.string.views);
        } else if (viewCount < 1000000) {
            float k = viewCount / 1000f;
            return String.format(Locale.getDefault(), "%.1fK %s", k, mContext.getString(R.string.views));
        } else {
            float m = viewCount / 1000000f;
            return String.format(Locale.getDefault(), "%.1fM %s", m, mContext.getString(R.string.views));
        }
    }

    /**
     * Format thời gian đăng thành chuỗi relative time (vd: 2 ngày trước)
     * @param uploadTimestamp Timestamp thời gian đăng
     * @return Chuỗi đã format
     */
    private String formatUploadDate(long uploadTimestamp) {
        long currentTime = System.currentTimeMillis();
        long diffTime = currentTime - uploadTimestamp;

        long seconds = diffTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long months = days / 30;
        long years = days / 365;

        if (years > 0) {
            return years + " năm trước";
        } else if (months > 0) {
            return months + " tháng trước";
        } else if (days > 0) {
            return days + " ngày trước";
        } else if (hours > 0) {
            return hours + " giờ trước";
        } else if (minutes > 0) {
            return minutes + " phút trước";
        } else {
            return "Vừa xong";
        }
    }

    /**
     * ViewHolder cho item video
     */
    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivVideoThumbnail;
        ImageView ivPlayButton;
        TextView tvVideoTitle;
        TextView tvVideoCaption;
        TextView tvViewCount;
        TextView tvUploadDate;

        public VideoViewHolder(@NonNull View itemView, final OnVideoClickListener listener) {
            super(itemView);

            ivVideoThumbnail = itemView.findViewById(R.id.iv_video_thumbnail);
            ivPlayButton = itemView.findViewById(R.id.iv_play_button);
            tvVideoTitle = itemView.findViewById(R.id.tv_video_title);
            tvVideoCaption = itemView.findViewById(R.id.tv_video_caption);
            tvViewCount = itemView.findViewById(R.id.tv_view_count);
            tvUploadDate = itemView.findViewById(R.id.tv_upload_date);

            // Thiết lập sự kiện click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onVideoClick(position);
                    }
                }
            });
        }
    }
}
