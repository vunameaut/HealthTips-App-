package com.vhn.doan.presentation.category;

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
import com.bumptech.glide.request.RequestOptions;
import com.vhn.doan.R;
import com.vhn.doan.data.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách các danh mục trong RecyclerView
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private List<Category> categories;
    private final OnCategoryClickListener listener;

    /**
     * Interface để lắng nghe sự kiện click vào danh mục
     */
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    /**
     * Constructor khởi tạo adapter với context và listener
     */
    public CategoryAdapter(Context context, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Cập nhật danh sách danh mục
     * @param categories danh sách danh mục mới
     */
    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    /**
     * ViewHolder chứa references đến các view trong item_category
     */
    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardViewCategory;
        private final ImageView imageViewCategoryIcon;
        private final TextView textViewCategoryName;
        private final TextView textViewArticleCount;
        private final TextView badgeTrending;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewCategory = itemView.findViewById(R.id.cardViewCategory);
            imageViewCategoryIcon = itemView.findViewById(R.id.imageViewCategoryIcon);
            textViewCategoryName = itemView.findViewById(R.id.textViewCategoryName);
            textViewArticleCount = itemView.findViewById(R.id.textViewArticleCount);
            badgeTrending = itemView.findViewById(R.id.badgeTrending);

            cardViewCategory.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCategoryClick(categories.get(position));
                }
            });
        }

        /**
         * Cập nhật UI với dữ liệu của category
         * @param category đối tượng Category chứa dữ liệu
         */
        void bind(Category category) {
            textViewCategoryName.setText(category.getName());

            // Hiển thị HOT badge nếu có nhiều bài viết (> 10)
            int tipCount = category.getTipCount();
            if (tipCount > 10) {
                badgeTrending.setVisibility(View.VISIBLE);
            } else {
                badgeTrending.setVisibility(View.GONE);
            }

            // Tải hình ảnh danh mục bằng Glide
            if (category.getIconUrl() != null && !category.getIconUrl().isEmpty()) {
                Glide.with(context)
                        .load(category.getIconUrl())
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.placeholder_image)
                                .error(R.drawable.error_image))
                        .into(imageViewCategoryIcon);
            } else {
                // Sử dụng ảnh mặc định nếu không có iconUrl
                imageViewCategoryIcon.setImageResource(R.drawable.placeholder_image);
            }
        }
    }
}
