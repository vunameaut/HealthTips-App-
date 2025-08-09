package com.vhn.doan.presentation.profile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách video ngắn dưới dạng lưới.
 * Được sử dụng trong LikedVideosFragment.
 */
public class GridShortVideoAdapter extends RecyclerView.Adapter<GridShortVideoAdapter.VideoViewHolder> {

    private final Context context;
    private final List<ShortVideo> videos = new ArrayList<>();
    private final OnVideoClickListener listener;

    /**
     * Interface callback khi người dùng click vào video.
     */
    public interface OnVideoClickListener {
        void onVideoClick(ShortVideo video);
    }

    public GridShortVideoAdapter(Context context, OnVideoClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grid_short_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        ShortVideo video = videos.get(position);
        holder.bind(video, listener);
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    /**
     * Cập nhật danh sách video.
     */
    public void updateData(List<ShortVideo> newVideos) {
        videos.clear();
        if (newVideos != null) {
            videos.addAll(newVideos);
        }
        notifyDataSetChanged();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final ImageView imageView;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            imageView = itemView.findViewById(R.id.imageView);
        }

        void bind(final ShortVideo video, final OnVideoClickListener listener) {
            if (video.getThumbnailUrl() != null && !video.getThumbnailUrl().isEmpty()) {
                RequestOptions options = new RequestOptions()
                        .transforms(new CenterCrop(), new RoundedCorners(8));
                Glide.with(itemView.getContext())
                        .load(video.getThumbnailUrl())
                        .apply(options)
                        .placeholder(R.drawable.placeholder_image)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.placeholder_image);
            }

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVideoClick(video);
                }
            });
        }
    }
}
