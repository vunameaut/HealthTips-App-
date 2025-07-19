package com.vhn.doan.presentation.home.adapter;

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
import com.vhn.doan.R;
import com.vhn.doan.data.HealthTip;

import java.util.List;

/**
 * Adapter để hiển thị danh sách mẹo sức khỏe trong RecyclerView
 * Tuân theo mô hình MVP
 */
public class HealthTipAdapter extends RecyclerView.Adapter<HealthTipAdapter.HealthTipViewHolder> {

    private final List<HealthTip> healthTips;
    private final HealthTipClickListener listener;
    private final Context context;

    /**
     * Interface cho sự kiện click vào mẹo sức khỏe
     */
    public interface HealthTipClickListener {
        void onHealthTipClick(HealthTip healthTip);
    }

    /**
     * Constructor
     * @param context Context
     * @param healthTips Danh sách mẹo sức khỏe cần hiển thị
     * @param listener Listener xử lý sự kiện click
     */
    public HealthTipAdapter(Context context, List<HealthTip> healthTips, HealthTipClickListener listener) {
        this.context = context;
        this.healthTips = healthTips;
        this.listener = listener;
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
        holder.bind(healthTip, listener);
    }

    @Override
    public int getItemCount() {
        return healthTips != null ? healthTips.size() : 0;
    }

    /**
     * Cập nhật danh sách mẹo sức khỏe
     * @param newHealthTips Danh sách mẹo sức khỏe mới
     */
    public void updateHealthTips(List<HealthTip> newHealthTips) {
        healthTips.clear();
        healthTips.addAll(newHealthTips);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder cho item mẹo sức khỏe
     */
    public static class HealthTipViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewThumbnail;
        private final TextView textViewTitle;
        private final TextView textViewShortDesc;
        private final TextView textViewViewCount;
        private final TextView textViewLikeCount;
        private final CardView cardViewHealthTip;

        public HealthTipViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewThumbnail = itemView.findViewById(R.id.imageViewThumbnail);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewShortDesc = itemView.findViewById(R.id.textViewShortDesc);
            textViewViewCount = itemView.findViewById(R.id.textViewViewCount);
            textViewLikeCount = itemView.findViewById(R.id.textViewLikeCount);
            cardViewHealthTip = itemView.findViewById(R.id.cardViewHealthTip);
        }

        public void bind(final HealthTip healthTip, final HealthTipClickListener listener) {
            // Đặt tiêu đề
            textViewTitle.setText(healthTip.getTitle());

            // Đặt mô tả ngắn - lấy 50 ký tự đầu tiên của nội dung
            String shortDesc = healthTip.getContent();
            if (shortDesc != null && shortDesc.length() > 50) {
                shortDesc = shortDesc.substring(0, 50) + "...";
            }
            textViewShortDesc.setText(shortDesc);

            // Hiển thị số lượt xem và số lượt thích
            textViewViewCount.setText(String.valueOf(healthTip.getViewCount()));
            textViewLikeCount.setText(String.valueOf(healthTip.getLikeCount()));

            // Tải hình ảnh thumbnail bằng Glide
            if (healthTip.getImageUrl() != null && !healthTip.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(healthTip.getImageUrl())
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(imageViewThumbnail);
            } else {
                // Nếu không có hình ảnh, hiển thị hình ảnh mặc định
                imageViewThumbnail.setImageResource(R.drawable.placeholder_image);
            }

            // Xử lý sự kiện click
            cardViewHealthTip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onHealthTipClick(healthTip);
                    }
                }
            });
        }
    }
}
