package com.vhn.doan.presentation.home.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.vhn.doan.R;
import com.vhn.doan.data.Category;

import java.util.List;

/**
 * Adapter cho RecyclerView hiển thị danh sách các danh mục
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private static final String TAG = "CategoryAdapter";

    private final Context context;
    private List<Category> categories;
    private final CategoryClickListener clickListener;

    /**
     * Interface để xử lý sự kiện click vào một danh mục
     */
    public interface CategoryClickListener {
        void onCategoryClick(Category category);
    }

    /**
     * Constructor của adapter
     * @param context Context của ứng dụng
     * @param categories Danh sách các danh mục
     * @param clickListener Listener xử lý sự kiện click
     */
    public CategoryAdapter(Context context, List<Category> categories, CategoryClickListener clickListener) {
        this.context = context;
        this.categories = categories;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);

        // Log thông tin danh mục cơ bản
        Log.d(TAG, "Binding Category: " + category.getName());

        // Thiết lập tên danh mục
        holder.textViewCategoryName.setText(category.getName());

        // Ưu tiên sử dụng iconUrl, fallback sang imageUrl
        String imageUrl = category.getIconUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = category.getImageUrl();
        }

        // Tải hình ảnh danh mục sử dụng Glide
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_category_default)
                            .error(R.drawable.ic_category_default)
                            .centerCrop()) // Hiển thị hình vuông
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "Failed to load image for category: " + category.getName());
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            Log.d(TAG, "Successfully loaded image for category: " + category.getName());
                            return false;
                        }
                    })
                    .into(holder.imageViewCategoryIcon);
        } else {
            Log.w(TAG, "No image URL found for category: " + category.getName() + " - using default icon");
            holder.imageViewCategoryIcon.setImageResource(R.drawable.ic_category_default);
        }

        // Xử lý sự kiện click vào card
        holder.cardViewCategory.setOnClickListener(v -> {
            if (clickListener != null) {
                Log.d(TAG, "Category clicked: " + category.getName());
                clickListener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    /**
     * Cập nhật dữ liệu mới cho adapter
     * @param newCategories Danh sách danh mục mới
     */
    public void updateCategories(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder cho item của RecyclerView
     */
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        CardView cardViewCategory;
        ImageView imageViewCategoryIcon;
        TextView textViewCategoryName;

        /**
         * Constructor cho ViewHolder
         * @param itemView View của item
         */
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewCategory = itemView.findViewById(R.id.cardViewCategory);
            imageViewCategoryIcon = itemView.findViewById(R.id.imageViewCategoryIcon);
            textViewCategoryName = itemView.findViewById(R.id.textViewCategoryName);
        }
    }
}
