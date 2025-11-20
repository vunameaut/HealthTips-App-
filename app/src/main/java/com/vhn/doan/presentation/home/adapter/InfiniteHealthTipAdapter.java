package com.vhn.doan.presentation.home.adapter;

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
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.ContentBlock;
import com.vhn.doan.data.local.CacheManager;

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
    private int layoutResourceId; // Custom layout resource ID
    private static final int INFINITE_MULTIPLIER = 1000; // Tạo rất nhiều items để scroll vô hạn

    /**
     * Interface để xử lý sự kiện click
     */
    public interface HealthTipClickListener {
        void onHealthTipClick(HealthTip healthTip);
        void onFavoriteClick(HealthTip healthTip, boolean isFavorite);
    }

    /**
     * Constructor mặc định - sử dụng item_health_tip layout
     */
    public InfiniteHealthTipAdapter(Context context, List<HealthTip> healthTips, HealthTipClickListener clickListener) {
        this(context, healthTips, clickListener, R.layout.item_health_tip);
    }

    /**
     * Constructor với custom layout
     */
    public InfiniteHealthTipAdapter(Context context, List<HealthTip> healthTips, HealthTipClickListener clickListener, int layoutResourceId) {
        this.context = context;
        this.originalHealthTips = new ArrayList<>(healthTips);
        this.clickListener = clickListener;
        this.layoutResourceId = layoutResourceId;
    }

    @NonNull
    @Override
    public HealthTipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layoutResourceId, parent, false);
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

        // 🎯 CACHE NGAY KHI USER SCROLL QUA! (Recommended tips)
        CacheManager.getInstance(context).cacheHealthTipImmediately(healthTip);
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
                if (healthTip.getExcerpt() != null && !healthTip.getExcerpt().isEmpty()) {
                    // Sử dụng excerpt nếu có
                    textViewSummary.setText(healthTip.getExcerpt());
                } else {
                    // Tạo tóm tắt từ nội dung - sử dụng ContentBlocks thay vì content trực tiếp
                    List<ContentBlock> contentBlocks = healthTip.getContentBlockObjects();
                    if (contentBlocks != null && !contentBlocks.isEmpty()) {
                        // Tạo tóm tắt từ các block loại text và heading
                        StringBuilder summaryBuilder = new StringBuilder();
                        for (ContentBlock block : contentBlocks) {
                            if ("text".equals(block.getType()) || "heading".equals(block.getType())) {
                                if (block.getValue() != null && !block.getValue().isEmpty()) {
                                    summaryBuilder.append(block.getValue()).append(" ");
                                    // Nếu đã đủ dài, dừng việc thu thập nội dung
                                    if (summaryBuilder.length() > 200) {
                                        break;
                                    }
                                }
                            }
                        }

                        String summaryText = summaryBuilder.toString().trim();
                        if (!summaryText.isEmpty()) {
                            // Giới hạn độ dài tóm tắt
                            int maxLength = 150;
                            String summary = summaryText.length() > maxLength ?
                                summaryText.substring(0, maxLength).trim() + "..." :
                                summaryText;
                            textViewSummary.setText(summary);
                        } else {
                            textViewSummary.setText("Xem chi tiết...");
                        }
                    } else {
                        textViewSummary.setText("Xem chi tiết...");
                    }
                }
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
                            .centerCrop()
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

                // Cập nhật màu sắc theo trạng thái yêu thích
                buttonFavorite.setColorFilter(
                    ContextCompat.getColor(itemView.getContext(),
                    isFavorite ? R.color.favorite_active : R.color.favorite_inactive),
                    android.graphics.PorterDuff.Mode.SRC_IN);

                // Xử lý sự kiện click
                buttonFavorite.setOnClickListener(v -> {
                    if (clickListener != null) {
                        // Thêm hiệu ứng animation khi click
                        v.animate()
                            .scaleX(0.8f)
                            .scaleY(0.8f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                                clickListener.onFavoriteClick(healthTip, !isFavorite);
                            })
                            .start();
                    }
                });
            }

            // Xử lý click vào toàn bộ item với hiệu ứng ripple
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onHealthTipClick(healthTip);
                }
            });
        }
    }
}
