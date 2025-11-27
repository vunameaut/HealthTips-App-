package com.vhn.doan.presentation.home.adapter;

import android.content.Context;
import android.util.Log;
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
import com.vhn.doan.data.local.CacheManager;
import com.vhn.doan.data.repository.FavoriteRepository;
import com.vhn.doan.data.repository.FavoriteRepositoryImpl;

import java.util.ArrayList;
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
        this.favoriteRepository = new FavoriteRepositoryImpl(context);
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

        // 🎯 CACHE NGAY KHI USER SCROLL QUA! (Giống TikTok/Facebook)
        // Cache passive - user không cần làm gì, chỉ cần nhìn thấy item
        CacheManager.getInstance(context).cacheHealthTipImmediately(healthTip);
    }

    @Override
    public int getItemCount() {
        return healthTips != null ? healthTips.size() : 0;
    }

    /**
     * Cập nhật danh sách mẹo sức khỏe với DiffUtil (OPTIMIZED)
     * Chỉ update items thay đổi thay vì rebind tất cả
     * @param newHealthTips Danh sách mẹo sức khỏe mới
     */
    public void updateHealthTips(List<HealthTip> newHealthTips) {
        // ⚡ OPTIMIZED: Sử dụng DiffUtil thay vì notifyDataSetChanged()
        final List<HealthTip> oldList = new ArrayList<>(this.healthTips);

        androidx.recyclerview.widget.DiffUtil.DiffResult diffResult =
            androidx.recyclerview.widget.DiffUtil.calculateDiff(new androidx.recyclerview.widget.DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return oldList.size();
                }

                @Override
                public int getNewListSize() {
                    return newHealthTips.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    // So sánh ID để xác định cùng một item
                    HealthTip oldItem = oldList.get(oldItemPosition);
                    HealthTip newItem = newHealthTips.get(newItemPosition);
                    return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    // So sánh nội dung để xác định có thay đổi không
                    HealthTip oldItem = oldList.get(oldItemPosition);
                    HealthTip newItem = newHealthTips.get(newItemPosition);

                    // So sánh các fields quan trọng
                    boolean sameTitle = (oldItem.getTitle() == null && newItem.getTitle() == null) ||
                            (oldItem.getTitle() != null && oldItem.getTitle().equals(newItem.getTitle()));

                    boolean sameLikeCount = oldItem.getLikeCount() == newItem.getLikeCount();
                    boolean sameViewCount = oldItem.getViewCount() == newItem.getViewCount();

                    return sameTitle && sameLikeCount && sameViewCount;
                }
            });

        // Update list và dispatch changes
        this.healthTips.clear();
        this.healthTips.addAll(newHealthTips);
        diffResult.dispatchUpdatesTo(this);

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
     * ⚡ OPTIMIZED: Update favorites từ shared data thay vì load riêng
     * Dùng method này để share favorites giữa nhiều adapters
     */
    public void updateFavoritesFromShared(java.util.Set<String> sharedFavoriteIds) {
        if (sharedFavoriteIds != null) {
            favoriteHealthTipIds.clear();
            favoriteHealthTipIds.addAll(sharedFavoriteIds);
            notifyDataSetChanged();
        }
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
     * Cập nhật trạng thái yêu thích cho một health tip cụ thể
     * Method này được gọi từ HomeFragment để đồng bộ trạng thái giữa các adapter
     * @param healthTipId ID của health tip cần cập nhật
     * @param isFavorite Trạng thái yêu thích mới
     */
    public void updateFavoriteStatus(String healthTipId, boolean isFavorite) {
        if (healthTipId == null) return;

        // Cập nhật cache trạng thái yêu thích
        if (isFavorite) {
            favoriteHealthTipIds.add(healthTipId);
        } else {
            favoriteHealthTipIds.remove(healthTipId);
        }

        // Tìm và cập nhật UI cho item có ID tương ứng
        for (int i = 0; i < healthTips.size(); i++) {
            HealthTip healthTip = healthTips.get(i);
            if (healthTip != null && healthTipId.equals(healthTip.getId())) {
                // Chỉ cập nhật item cụ thể thay vì toàn bộ danh sách
                notifyItemChanged(i);
                break;
            }
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

            // Đặt mô tả ngắn - ưu tiên sử dụng excerpt, nếu không có thì dùng content
            String shortDesc = healthTip.getExcerpt();

            // Nếu không có excerpt, fallback về content
            if (shortDesc == null || shortDesc.isEmpty()) {
                shortDesc = healthTip.getContent();
                // Cắt nội dung nếu quá dài
                if (shortDesc != null && shortDesc.length() > 100) {
                    shortDesc = shortDesc.substring(0, 100) + "...";
                }
            }

            // Hiển thị mô tả hoặc placeholder
            if (shortDesc != null && !shortDesc.isEmpty()) {
                textViewShortDesc.setText(shortDesc);
            } else {
                textViewShortDesc.setText("Không có mô tả");
            }

            // Hiển thị số lượt xem và số lượt thích
            textViewViewCount.setText(String.valueOf(healthTip.getViewCount()));
            textViewLikeCount.setText(String.valueOf(healthTip.getLikeCount()));

            // Tải hình ảnh bằng Glide
            String imageUrl = healthTip.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                android.util.Log.d("HealthTipAdapter", "Loading image: " + imageUrl);

                // ⚡ OPTIMIZED: Glide with disk cache, priority, and thumbnail
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.RESOURCE) // Cache decoded images
                        .priority(com.bumptech.glide.Priority.HIGH) // High priority for visible items
                        .thumbnail(0.1f) // Load 10% thumbnail first for faster display
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
