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
import com.bumptech.glide.request.RequestOptions;
import com.vhn.doan.R;
import com.vhn.doan.data.Category;

import java.util.List;

/**
 * Adapter cho RecyclerView hiển thị danh sách các danh mục
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

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

        // Thiết lập tên danh mục
        holder.textViewCategoryName.setText(category.getName());

        // Tải hình ảnh danh mục sử dụng Glide
        if (category.getIconUrl() != null && !category.getIconUrl().isEmpty()) {
            Glide.with(context)
                    .load(category.getIconUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_launcher_foreground) // Placeholder mặc định
                            .error(R.drawable.ic_launcher_foreground)) // Ảnh hiển thị khi lỗi
                    .into(holder.imageViewCategoryIcon);
        } else {
            // Hiển thị icon mặc định nếu không có url
            holder.imageViewCategoryIcon.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Xử lý sự kiện click vào card
        holder.cardViewCategory.setOnClickListener(v -> {
            if (clickListener != null) {
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
