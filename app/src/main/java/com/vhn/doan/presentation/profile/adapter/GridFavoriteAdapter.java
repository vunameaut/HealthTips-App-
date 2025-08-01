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
        return healthTips.size();
    }

    /**
     * Cập nhật danh sách dữ liệu
     * @param newData danh sách mẹo sức khỏe mới
     */
    public void updateData(List<HealthTip> newData) {
        this.healthTips = newData;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder cho grid item
     */
    static class GridViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final ImageView imageView;
        private final TextView titleView;
        private final ImageView btnRemoveFavorite;

        public GridViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            imageView = itemView.findViewById(R.id.imageView);
            titleView = itemView.findViewById(R.id.titleView);
            btnRemoveFavorite = itemView.findViewById(R.id.btnRemoveFavorite);
        }

        void bind(final HealthTip healthTip, final OnFavoriteItemClickListener listener) {
            // Thiết lập tiêu đề
            titleView.setText(healthTip.getTitle());

            // Tải hình ảnh nếu có
            if (healthTip.getImageUrl() != null && !healthTip.getImageUrl().isEmpty()) {
                RequestOptions requestOptions = new RequestOptions()
                        .transforms(new CenterCrop(), new RoundedCorners(8));

                Glide.with(itemView.getContext())
                        .load(healthTip.getImageUrl())
                        .apply(requestOptions)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(imageView);
            } else {
                // Nếu không có hình, hiển thị placeholder
                imageView.setImageResource(R.drawable.placeholder_image);
            }

            // Thiết lập sự kiện click
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(healthTip);
                }
            });

            btnRemoveFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveFavorite(healthTip);
                }
            });
        }
    }
}
