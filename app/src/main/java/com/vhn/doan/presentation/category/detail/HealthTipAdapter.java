package com.vhn.doan.presentation.category.detail;

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
 * Adapter hiển thị danh sách mẹo sức khỏe trong RecyclerView
 */
public class HealthTipAdapter extends RecyclerView.Adapter<HealthTipAdapter.HealthTipViewHolder> {

    private List<HealthTip> healthTips = new ArrayList<>();
    private final Context context;
    private final OnHealthTipClickListener listener;

    /**
     * Interface để xử lý sự kiện khi nhấp vào mẹo sức khỏe
     */
    public interface OnHealthTipClickListener {
        void onHealthTipClick(String healthTipId);
    }

    /**
     * Constructor nhận context và listener
     * @param context Context của activity hoặc fragment
     * @param listener Listener xử lý sự kiện click
     */
    public HealthTipAdapter(Context context, OnHealthTipClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /**
     * Cập nhật dữ liệu mới cho adapter
     * @param healthTips Danh sách mẹo sức khỏe mới
     */
    public void setHealthTips(List<HealthTip> healthTips) {
        this.healthTips = healthTips;
        notifyDataSetChanged();
    }

    public List<HealthTip> getHealthTips() {
        return healthTips;
    }

    @NonNull
    @Override
    public HealthTipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_health_tip, parent, false);
        return new HealthTipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HealthTipViewHolder holder, int position) {
        HealthTip healthTip = healthTips.get(position);
        holder.bind(healthTip);
    }

    @Override
    public int getItemCount() {
        return healthTips.size();
    }

    /**
     * ViewHolder cho mỗi item trong danh sách
     */
    class HealthTipViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTitle;
        private final TextView textViewContent;
        private final TextView textViewViews;
        private final TextView textViewLikes;
        private final TextView textViewDate;
        private final ImageView imageViewThumbnail;
        private final ImageView imageViewFavorite;

        public HealthTipViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewHealthTipTitle);
            textViewContent = itemView.findViewById(R.id.textViewHealthTipSummary);
            textViewViews = itemView.findViewById(R.id.textViewViewCount);
            textViewLikes = itemView.findViewById(R.id.textViewLikeCount);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            imageViewThumbnail = itemView.findViewById(R.id.imageViewHealthTip);
            imageViewFavorite = itemView.findViewById(R.id.imageViewFavorite);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onHealthTipClick(healthTips.get(position).getId());
                }
            });
        }

        /**
         * Hiển thị thông tin từ HealthTip lên giao diện
         * @param healthTip Đối tượng HealthTip cần hiển thị
         */
        public void bind(HealthTip healthTip) {
            textViewTitle.setText(healthTip.getTitle());

            // Hiển thị tóm tắt nội dung (100 ký tự đầu tiên)
            String summary = healthTip.getContent();
            if (summary.length() > 100) {
                summary = summary.substring(0, 97) + "...";
            }
            textViewContent.setText(summary);

            textViewViews.setText(String.valueOf(healthTip.getViewCount()));
            textViewLikes.setText(String.valueOf(healthTip.getLikeCount()));

            // Định dạng ngày tạo
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = sdf.format(new Date(healthTip.getCreatedAt()));
            textViewDate.setText(formattedDate);

            // Hiển thị icon yêu thích
            imageViewFavorite.setImageResource(
                    healthTip.isFavorite() ?
                    R.drawable.ic_favorite_filled :
                    R.drawable.ic_favorite_border);

            // Tải ảnh thumbnail nếu có
            if (healthTip.getImageUrl() != null && !healthTip.getImageUrl().isEmpty()) {
                Glide.with(context)
                    .load(healthTip.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(imageViewThumbnail);
                imageViewThumbnail.setVisibility(View.VISIBLE);
            } else {
                imageViewThumbnail.setVisibility(View.GONE);
            }
        }
    }
}
