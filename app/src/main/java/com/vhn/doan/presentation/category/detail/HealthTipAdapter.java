package com.vhn.doan.presentation.category.detail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.R;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.repository.FavoriteRepository;
import com.vhn.doan.data.repository.FavoriteRepositoryImpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Adapter hiển thị danh sách mẹo sức khỏe trong RecyclerView
 * Đã được cập nhật để hỗ trợ chức năng yêu thích
 */
public class HealthTipAdapter extends RecyclerView.Adapter<HealthTipAdapter.HealthTipViewHolder> {

    private List<HealthTip> healthTips = new ArrayList<>();
    private final Context context;
    private final HealthTipClickListener listener;
    private final FavoriteRepository favoriteRepository;
    private final FirebaseAuth firebaseAuth;
    private final Set<String> favoriteHealthTipIds; // Cache để theo dõi trạng thái yêu thích

    /**
     * Interface để xử lý sự kiện khi nhấp vào mẹo sức khỏe
     * Đã thống nhất với home adapter
     */
    public interface HealthTipClickListener {
        void onHealthTipClick(HealthTip healthTip);
        void onFavoriteClick(HealthTip healthTip, boolean isFavorite);
    }

    /**
     * Constructor nhận context và listener
     * @param context Context của activity hoặc fragment
     * @param listener Listener xử lý sự kiện click
     */
    public HealthTipAdapter(Context context, HealthTipClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.favoriteRepository = new FavoriteRepositoryImpl(context);
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.favoriteHealthTipIds = new HashSet<>();

        // Load danh sách yêu thích khi khởi tạo adapter
        loadUserFavorites();
    }

    /**
     * Constructor với FavoriteRepository tùy chỉnh (cho testing)
     */
    public HealthTipAdapter(Context context, HealthTipClickListener listener, FavoriteRepository favoriteRepository) {
        this.context = context;
        this.listener = listener;
        this.favoriteRepository = favoriteRepository;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.favoriteHealthTipIds = new HashSet<>();

        loadUserFavorites();
    }

    /**
     * Cập nhật dữ liệu mới cho adapter
     * @param healthTips Danh sách mẹo sức khỏe mới
     */
    public void updateHealthTips(List<HealthTip> healthTips) {
        this.healthTips.clear();
        if (healthTips != null) {
            this.healthTips.addAll(healthTips);
        }
        notifyDataSetChanged();
        // Reload favorites sau khi cập nhật danh sách
        loadUserFavorites();
    }

    /**
     * Lấy danh sách health tips hiện tại
     * @return Danh sách health tips
     */
    public List<HealthTip> getHealthTipsList() {
        return new ArrayList<>(healthTips);
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

    @NonNull
    @Override
    public HealthTipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_health_tip, parent, false);
        return new HealthTipViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HealthTipViewHolder holder, int position) {
        HealthTip healthTip = healthTips.get(position);
        boolean isFavorite = favoriteHealthTipIds.contains(healthTip.getId());
        holder.bind(healthTip, listener, isFavorite, this);
    }

    @Override
    public int getItemCount() {
        return healthTips.size();
    }

    /**
     * ViewHolder cho từng item trong RecyclerView
     */
    public static class HealthTipViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView titleTextView;
        private final TextView summaryTextView;
        private final TextView dateTextView;
        private final TextView viewCountTextView;
        private final TextView likeCountTextView;
        private final ImageView favoriteIcon;
        private final SimpleDateFormat dateFormat;

        public HealthTipViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewHealthTip);
            titleTextView = itemView.findViewById(R.id.textViewHealthTipTitle);
            summaryTextView = itemView.findViewById(R.id.textViewHealthTipSummary);
            dateTextView = itemView.findViewById(R.id.textViewDate);
            viewCountTextView = itemView.findViewById(R.id.textViewViewCount);
            likeCountTextView = itemView.findViewById(R.id.textViewLikeCount);
            favoriteIcon = itemView.findViewById(R.id.imageViewFavorite);
            dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        }

        public void bind(HealthTip healthTip, HealthTipClickListener listener, boolean isFavorite, HealthTipAdapter adapter) {
            // Thiết lập tiêu đề - kiểm tra null
            String title = healthTip.getTitle();
            titleTextView.setText(title != null ? title : "Không có tiêu đề");

            // Thiết lập tóm tắt - ưu tiên sử dụng excerpt, nếu không có thì dùng content
            String summary = healthTip.getExcerpt();

            // Nếu không có excerpt, fallback về content
            if (summary == null || summary.isEmpty()) {
                summary = healthTip.getContent();
                // Cắt nội dung nếu quá dài
                if (summary != null && summary.length() > 100) {
                    summary = summary.substring(0, 100) + "...";
                }
            }

            // Hiển thị tóm tắt hoặc placeholder
            summaryTextView.setText(summary != null && !summary.isEmpty() ? summary : "Không có mô tả");

            // Thiết lập ngày - kiểm tra createdAt > 0 thay vì != null vì là primitive long
            if (healthTip.getCreatedAt() > 0) {
                dateTextView.setText(dateFormat.format(new Date(healthTip.getCreatedAt())));
            } else {
                dateTextView.setText(dateFormat.format(new Date()));
            }

            // Thiết lập số lượt xem và thích
            viewCountTextView.setText(String.valueOf(healthTip.getViewCount()));
            likeCountTextView.setText(String.valueOf(healthTip.getLikeCount()));

            // Tải hình ảnh với Glide
            String imageUrl = healthTip.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                android.util.Log.d("CategoryHealthTipAdapter", "Loading image: " + imageUrl);

                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.placeholder_image);
            }

            // Cập nhật icon yêu thích
            adapter.updateFavoriteIcon(favoriteIcon, isFavorite);

            // Thiết lập sự kiện click cho item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHealthTipClick(healthTip);
                }
            });

            // Thiết lập sự kiện click cho nút yêu thích
            favoriteIcon.setOnClickListener(v -> adapter.toggleFavorite(healthTip, favoriteIcon));
        }
    }
}
