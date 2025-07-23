package com.vhn.doan.presentation.favorite.adapter;

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
import com.vhn.doan.data.HealthTip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho danh sách mẹo sức khỏe yêu thích
 * Hiển thị các mẹo sức khỏe với tùy chọn xóa khỏi yêu thích
 */
public class FavoriteHealthTipAdapter extends RecyclerView.Adapter<FavoriteHealthTipAdapter.FavoriteHealthTipViewHolder> {

    private List<HealthTip> favoriteHealthTips = new ArrayList<>();
    private final Context context;
    private final FavoriteHealthTipClickListener listener;

    /**
     * Interface để xử lý sự kiện click
     */
    public interface FavoriteHealthTipClickListener {
        void onHealthTipClick(HealthTip healthTip);
        void onRemoveFromFavorites(HealthTip healthTip);
    }

    public FavoriteHealthTipAdapter(Context context, List<HealthTip> favoriteHealthTips, FavoriteHealthTipClickListener listener) {
        this.context = context;
        this.favoriteHealthTips = new ArrayList<>(favoriteHealthTips);
        this.listener = listener;
    }

    /**
     * Cập nhật danh sách mẹo sức khỏe yêu thích
     */
    public void updateFavoriteHealthTips(List<HealthTip> favoriteHealthTips) {
        this.favoriteHealthTips.clear();
        if (favoriteHealthTips != null) {
            this.favoriteHealthTips.addAll(favoriteHealthTips);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoriteHealthTipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_favorite_health_tip, parent, false);
        return new FavoriteHealthTipViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteHealthTipViewHolder holder, int position) {
        HealthTip healthTip = favoriteHealthTips.get(position);
        holder.bind(healthTip, listener);
    }

    @Override
    public int getItemCount() {
        return favoriteHealthTips.size();
    }

    /**
     * ViewHolder cho từng item trong danh sách yêu thích
     */
    public static class FavoriteHealthTipViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewHealthTip;
        private final TextView textViewTitle;
        private final TextView textViewContent;
        private final TextView textViewDate;
        private final TextView textViewViewCount;
        private final TextView textViewLikeCount;
        private final ImageView imageViewRemoveFavorite;
        private final SimpleDateFormat dateFormat;

        public FavoriteHealthTipViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewHealthTip = itemView.findViewById(R.id.imageViewHealthTip);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewContent = itemView.findViewById(R.id.textViewContent);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewViewCount = itemView.findViewById(R.id.textViewViewCount);
            textViewLikeCount = itemView.findViewById(R.id.textViewLikeCount);
            imageViewRemoveFavorite = itemView.findViewById(R.id.imageViewRemoveFavorite);

            dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        }

        public void bind(HealthTip healthTip, FavoriteHealthTipClickListener listener) {
            // Kiểm tra null cho healthTip
            if (healthTip == null) {
                return;
            }

            // Thiết lập tiêu đề - kiểm tra null
            String title = healthTip.getTitle();
            textViewTitle.setText(title != null && !title.trim().isEmpty() ? title : "Không có tiêu đề");

            // Thiết lập nội dung (sử dụng getSummary() thay vì getContent trực tiếp)
            String summary = healthTip.getSummary();
            textViewContent.setText(summary != null && !summary.trim().isEmpty() ? summary : "Không có nội dung");

            // Thiết lập ngày tạo - kiểm tra giá trị hợp lệ
            long createdAt = healthTip.getCreatedAt();
            if (createdAt > 0) {
                textViewDate.setText(dateFormat.format(new Date(createdAt)));
            } else {
                textViewDate.setText(dateFormat.format(new Date()));
            }

            // Thiết lập số lượt xem và thích với kiểm tra âm
            int viewCount = Math.max(0, healthTip.getViewCount());
            int likeCount = Math.max(0, healthTip.getLikeCount());
            textViewViewCount.setText(String.valueOf(viewCount));
            textViewLikeCount.setText(String.valueOf(likeCount));

            // Tải hình ảnh với Glide - cải thiện xử lý null và empty
            String imageUrl = healthTip.getImageUrl();
            if (imageUrl != null && !imageUrl.trim().isEmpty() &&
                (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith("file://"))) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(imageViewHealthTip);
            } else {
                // Đặt ảnh mặc định nếu không có URL hợp lệ
                imageViewHealthTip.setImageResource(R.drawable.placeholder_image);
            }

            // Thiết lập icon xóa khỏi yêu thích
            imageViewRemoveFavorite.setImageResource(R.drawable.ic_favorite_filled);
            try {
                imageViewRemoveFavorite.setColorFilter(
                    itemView.getContext().getResources().getColor(R.color.favorite_color, null)
                );
            } catch (Exception e) {
                // Fallback nếu màu không tồn tại
                imageViewRemoveFavorite.setColorFilter(
                    itemView.getContext().getResources().getColor(android.R.color.holo_red_light, null)
                );
            }

            // Thiết lập sự kiện click cho item
            itemView.setOnClickListener(v -> {
                if (listener != null && healthTip.isValid()) {
                    listener.onHealthTipClick(healthTip);
                }
            });

            // Thiết lập sự kiện click cho nút xóa yêu thích
            imageViewRemoveFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveFromFavorites(healthTip);
                }
            });
        }
    }
}
