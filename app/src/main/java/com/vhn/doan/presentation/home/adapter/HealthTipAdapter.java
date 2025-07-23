package com.vhn.doan.presentation.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.R;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.repository.FavoriteRepository;
import com.vhn.doan.data.repository.FavoriteRepositoryImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter để hiển thị danh sách mẹo sức khỏe trong RecyclerView
 * Tuân theo mô hình MVP và hỗ trợ chức năng yêu thích
 */
public class HealthTipAdapter extends RecyclerView.Adapter<HealthTipAdapter.HealthTipViewHolder> {

    private final List<HealthTip> healthTips;
    private final HealthTipClickListener listener;
    private final Context context;
    private final FavoriteRepository favoriteRepository;
    private final FirebaseAuth firebaseAuth;
    private final Set<String> favoriteHealthTipIds; // Cache để theo dõi trạng thái yêu thích

    /**
     * Interface cho sự kiện click vào mẹo sức khỏe
     */
    public interface HealthTipClickListener {
        void onHealthTipClick(HealthTip healthTip);
        void onFavoriteClick(HealthTip healthTip, boolean isFavorite); // Callback cho sự kiện yêu thích
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
        this.favoriteRepository = new FavoriteRepositoryImpl();
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.favoriteHealthTipIds = new HashSet<>();

        // Load danh sách yêu thích khi khởi tạo adapter
        loadUserFavorites();
    }

    /**
     * Constructor với FavoriteRepository tùy chỉnh (cho testing)
     */
    public HealthTipAdapter(Context context, List<HealthTip> healthTips, HealthTipClickListener listener,
                           FavoriteRepository favoriteRepository) {
        this.context = context;
        this.healthTips = healthTips;
        this.listener = listener;
        this.favoriteRepository = favoriteRepository;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.favoriteHealthTipIds = new HashSet<>();

        loadUserFavorites();
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
        boolean isFavorite = favoriteHealthTipIds.contains(healthTip.getId());
        holder.bind(healthTip, listener, isFavorite, this);
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
        // Reload favorites sau khi cập nhật danh sách
        loadUserFavorites();
    }

    /**
     * Load danh sách yêu thích của người dùng hiện tại
     */
    private void loadUserFavorites() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        favoriteRepository.getFavoriteHealthTipIds(currentUser.getUid(), new FavoriteRepository.FavoriteListCallback() {
            @Override
            public void onSuccess(List<HealthTip> favoriteHealthTips) {
                favoriteHealthTipIds.clear();
                for (HealthTip healthTip : favoriteHealthTips) {
                    favoriteHealthTipIds.add(healthTip.getId());
                }
                notifyDataSetChanged(); // Cập nhật UI
            }

            @Override
            public void onError(String error) {
                // Log error nhưng không hiển thị Toast để tránh spam
                android.util.Log.e("HealthTipAdapter", "Lỗi khi tải danh sách yêu thích: " + error);
            }
        });
    }

    /**
     * Toggle trạng thái yêu thích cho một mẹo sức khỏe
     */
    public void toggleFavorite(HealthTip healthTip, ImageView favoriteIcon) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Vui lòng đăng nhập để sử dụng chức năng yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String healthTipId = healthTip.getId();
        boolean isCurrentlyFavorite = favoriteHealthTipIds.contains(healthTipId);

        // Cập nhật UI ngay lập tức cho UX tốt hơn
        updateFavoriteIcon(favoriteIcon, !isCurrentlyFavorite);

        if (isCurrentlyFavorite) {
            // Xóa khỏi yêu thích
            favoriteRepository.removeFromFavorites(userId, healthTipId, new FavoriteRepository.FavoriteActionCallback() {
                @Override
                public void onSuccess() {
                    favoriteHealthTipIds.remove(healthTipId);
                    if (listener != null) {
                        listener.onFavoriteClick(healthTip, false);
                    }
                }

                @Override
                public void onError(String error) {
                    // Revert UI changes on error
                    updateFavoriteIcon(favoriteIcon, isCurrentlyFavorite);
                    Toast.makeText(context, "Lỗi khi xóa khỏi yêu thích: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Thêm vào yêu thích
            favoriteRepository.addToFavorites(userId, healthTipId, new FavoriteRepository.FavoriteActionCallback() {
                @Override
                public void onSuccess() {
                    favoriteHealthTipIds.add(healthTipId);
                    if (listener != null) {
                        listener.onFavoriteClick(healthTip, true);
                    }
                }

                @Override
                public void onError(String error) {
                    // Revert UI changes on error
                    updateFavoriteIcon(favoriteIcon, isCurrentlyFavorite);
                    Toast.makeText(context, "Lỗi khi thêm vào yêu thích: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Cập nhật icon yêu thích
     */
    private void updateFavoriteIcon(ImageView favoriteIcon, boolean isFavorite) {
        if (isFavorite) {
            favoriteIcon.setImageResource(R.drawable.ic_favorite_filled);
            favoriteIcon.setColorFilter(context.getResources().getColor(R.color.favorite_color, null));
        } else {
            favoriteIcon.setImageResource(R.drawable.ic_favorite_outline);
            favoriteIcon.setColorFilter(context.getResources().getColor(R.color.icon_color, null));
        }
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
        private final ImageView imageViewFavorite;

        public HealthTipViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewThumbnail = itemView.findViewById(R.id.imageViewHealthTip);
            textViewTitle = itemView.findViewById(R.id.textViewHealthTipTitle);
            textViewShortDesc = itemView.findViewById(R.id.textViewHealthTipSummary);
            textViewViewCount = itemView.findViewById(R.id.textViewViewCount);
            textViewLikeCount = itemView.findViewById(R.id.textViewLikeCount);
            cardViewHealthTip = (CardView) itemView;
            imageViewFavorite = itemView.findViewById(R.id.imageViewFavorite);
        }

        public void bind(final HealthTip healthTip, final HealthTipClickListener listener,
                        boolean isFavorite, final HealthTipAdapter adapter) {
            // Đặt tiêu đề - kiểm tra null
            String title = healthTip.getTitle();
            textViewTitle.setText(title != null ? title : "Không có tiêu đề");

            // Đặt mô tả ngắn - lấy 50 ký tự đầu tiên của nội dung
            String shortDesc = healthTip.getContent();
            if (shortDesc != null && !shortDesc.isEmpty()) {
                if (shortDesc.length() > 50) {
                    shortDesc = shortDesc.substring(0, 50) + "...";
                }
                textViewShortDesc.setText(shortDesc);
            } else {
                textViewShortDesc.setText("Không có nội dung");
            }

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
                imageViewThumbnail.setImageResource(R.drawable.placeholder_image);
            }

            // Cập nhật icon yêu thích
            adapter.updateFavoriteIcon(imageViewFavorite, isFavorite);

            // Xử lý sự kiện click vào card
            cardViewHealthTip.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHealthTipClick(healthTip);
                }
            });

            // Xử lý sự kiện click vào nút yêu thích
            imageViewFavorite.setOnClickListener(v -> adapter.toggleFavorite(healthTip, imageViewFavorite));
        }
    }
}
