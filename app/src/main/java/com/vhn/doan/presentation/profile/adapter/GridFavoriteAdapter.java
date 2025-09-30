package com.vhn.doan.presentation.profile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.vhn.doan.R;
import com.vhn.doan.data.HealthTip;

import java.util.List;

/**
 * Adapter để hiển thị danh sách mẹo sức khỏe yêu thích dưới dạng lưới
 */
public class GridFavoriteAdapter extends RecyclerView.Adapter<GridFavoriteAdapter.GridViewHolder> {

    private final Context context;
    private List<HealthTip> healthTips;
    private final OnFavoriteItemClickListener listener;

    /**
     * Interface cho các sự kiện click trên item
     */
    public interface OnFavoriteItemClickListener {
        void onItemClick(HealthTip healthTip);
        void onRemoveFavorite(HealthTip healthTip);
    }

    public GridFavoriteAdapter(Context context, List<HealthTip> healthTips, OnFavoriteItemClickListener listener) {
        this.context = context;
        this.healthTips = healthTips;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grid_favorite, parent, false);
        return new GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {
        HealthTip healthTip = healthTips.get(position);
        holder.bind(healthTip, listener);
    }

    @Override
    public int getItemCount() {
        return healthTips != null ? healthTips.size() : 0;
    }

    /**
     * Cập nhật danh sách yêu thích
     */
    public void updateFavoriteList(List<HealthTip> newHealthTips) {
        this.healthTips = newHealthTips;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder cho từng item trong grid
     */
    public static class GridViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final ImageView imageView;
        private final TextView titleTextView;
        private final ImageView removeButton;

        public GridViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            imageView = itemView.findViewById(R.id.imageView);
            titleTextView = itemView.findViewById(R.id.textViewTitle);
            removeButton = itemView.findViewById(R.id.imageViewRemove);
        }

        public void bind(HealthTip healthTip, OnFavoriteItemClickListener listener) {
            // Thiết lập tiêu đề
            if (titleTextView != null) {
                titleTextView.setText(healthTip.getTitle());
            }

            // Tải hình ảnh
            if (imageView != null) {
                String imageUrl = healthTip.getImageUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    RequestOptions requestOptions = new RequestOptions()
                            .transform(new CenterCrop(), new RoundedCorners(16));

                    Glide.with(itemView.getContext())
                            .load(imageUrl)
                            .apply(requestOptions)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .into(imageView);
                } else {
                    imageView.setImageResource(R.drawable.placeholder_image);
                }
            }

            // Thiết lập sự kiện click cho item
            if (cardView != null) {
                cardView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(healthTip);
                    }
                });
            }

            // Thiết lập sự kiện click cho nút xóa
            if (removeButton != null) {
                removeButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRemoveFavorite(healthTip);
                    }
                });
            }
        }
    }
}
