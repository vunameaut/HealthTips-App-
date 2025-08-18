package com.vhn.doan.presentation.home.adapter;

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
 * Adapter cho RecyclerView hiển thị danh sách mẹo sức khỏe với khả năng scroll vô hạn
 * Tạo hiệu ứng vòng tròn bằng cách nhân đôi số lượng items
 */
public class InfiniteHealthTipAdapter extends RecyclerView.Adapter<InfiniteHealthTipAdapter.HealthTipViewHolder> {

    private Context context;
    private List<HealthTip> originalHealthTips; // Danh sách gốc
    private HealthTipClickListener clickListener;
    private static final int INFINITE_MULTIPLIER = 1000; // Tạo rất nhiều items để scroll vô hạn

    /**
     * Interface để xử lý sự kiện click
     */
    public interface HealthTipClickListener {
        void onHealthTipClick(HealthTip healthTip);
        void onFavoriteClick(HealthTip healthTip, boolean isFavorite);
    }

    /**
     * Constructor
     */
    public InfiniteHealthTipAdapter(Context context, List<HealthTip> healthTips, HealthTipClickListener clickListener) {
        this.context = context;
        this.originalHealthTips = new ArrayList<>(healthTips);
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public HealthTipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_health_tip, parent, false);
        return new HealthTipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HealthTipViewHolder holder, int position) {
        if (originalHealthTips.isEmpty()) {
            return;
        }

        // Tính toán vị trí thực tế trong danh sách gốc
        int realPosition = position % originalHealthTips.size();
        HealthTip healthTip = originalHealthTips.get(realPosition);

        holder.bind(healthTip, clickListener);
    }

    @Override
    public int getItemCount() {
        if (originalHealthTips.isEmpty()) {
            return 0;
        }
        // Trả về số lượng rất lớn để tạo hiệu ứng vô hạn
        return originalHealthTips.size() * INFINITE_MULTIPLIER;
    }

    /**
     * Cập nhật danh sách mẹo sức khỏe
     */
    public void updateHealthTips(List<HealthTip> newHealthTips) {
        this.originalHealthTips = new ArrayList<>(newHealthTips);
        notifyDataSetChanged();
    }

    /**
     * Lấy vị trí bắt đầu ở giữa để tạo hiệu ứng vô hạn
     */
    public int getStartPosition() {
        if (originalHealthTips.isEmpty()) {
            return 0;
        }
        // Bắt đầu ở giữa danh sách vô hạn
        return (INFINITE_MULTIPLIER / 2) * originalHealthTips.size();
    }

    /**
     * Cập nhật trạng thái yêu thích của một health tip
     */
    public void updateFavoriteStatus(String healthTipId, boolean isFavorite) {
        for (HealthTip tip : originalHealthTips) {
            if (tip.getId() != null && tip.getId().equals(healthTipId)) {
                tip.setFavorite(isFavorite);
                break;
            }
        }
        notifyDataSetChanged();
    }

    /**
     * ViewHolder cho mỗi item mẹo sức khỏe
     */
    static class HealthTipViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewThumbnail;
        private TextView textViewTitle;
        private TextView textViewSummary;
        private TextView textViewViewCount;
        private TextView textViewLikeCount;
        private TextView textViewCreatedAt;
        private ImageView buttonFavorite;

        public HealthTipViewHolder(@NonNull View itemView) {
            super(itemView);
            // Sử dụng các ID chính xác từ layout item_health_tip.xml
            imageViewThumbnail = itemView.findViewById(R.id.imageViewHealthTip);
            textViewTitle = itemView.findViewById(R.id.textViewHealthTipTitle);
            textViewSummary = itemView.findViewById(R.id.textViewHealthTipSummary);
            textViewViewCount = itemView.findViewById(R.id.textViewViewCount);
            textViewLikeCount = itemView.findViewById(R.id.textViewLikeCount);
            textViewCreatedAt = itemView.findViewById(R.id.textViewDate);
            buttonFavorite = itemView.findViewById(R.id.imageViewFavorite);
        }

        public void bind(HealthTip healthTip, HealthTipClickListener clickListener) {
            // Hiển thị tiêu đề
            if (textViewTitle != null) {
                textViewTitle.setText(healthTip.getTitle() != null ? healthTip.getTitle() : "Mẹo sức khỏe");
            }

            // Hiển thị tóm tắt nội dung
            if (textViewSummary != null) {
                textViewSummary.setText(healthTip.getSummary());
            }

            // Hiển thị số lượt xem
            if (textViewViewCount != null) {
                int viewCount = healthTip.getViewCount() != null ? healthTip.getViewCount() : 0;
                textViewViewCount.setText(String.valueOf(viewCount));
            }

            // Hiển thị số lượt thích
            if (textViewLikeCount != null) {
                int likeCount = healthTip.getLikeCount() != null ? healthTip.getLikeCount() : 0;
                textViewLikeCount.setText(String.valueOf(likeCount));
            }

            // Hiển thị thời gian tạo
            if (textViewCreatedAt != null) {
                long createdAt = healthTip.getCreatedAt();
                if (createdAt > 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String dateString = sdf.format(new Date(createdAt));
                    textViewCreatedAt.setText(dateString);
                } else {
                    textViewCreatedAt.setText("");
                }
            }

            // Hiển thị hình ảnh thumbnail
            if (imageViewThumbnail != null) {
                if (healthTip.getImageUrl() != null && !healthTip.getImageUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(healthTip.getImageUrl())
                            .placeholder(R.drawable.ic_placeholder_image)
                            .error(R.drawable.ic_error_image)
                            .into(imageViewThumbnail);
                } else {
                    imageViewThumbnail.setImageResource(R.drawable.ic_placeholder_image);
                }
            }

            // Xử lý nút yêu thích
            if (buttonFavorite != null) {
                // Cập nhật trạng thái hiển thị
                boolean isFavorite = healthTip.isFavorite();
                buttonFavorite.setImageResource(isFavorite ?
                    R.drawable.ic_favorite : R.drawable.ic_favorite_border);

                // Xử lý sự kiện click
                buttonFavorite.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onFavoriteClick(healthTip, !isFavorite);
                    }
                });
            }

            // Xử lý click vào toàn bộ item
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onHealthTipClick(healthTip);
                }
            });
        }
    }
}
