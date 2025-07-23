package com.vhn.doan.presentation.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;

/**
 * Adapter hiển thị skeleton loading cho Category items
 * Kích thước được thiết kế để khớp chính xác với real CategoryAdapter
 */
public class CategorySkeletonAdapter extends RecyclerView.Adapter<CategorySkeletonAdapter.SkeletonViewHolder> {

    private final Context context;
    private final int itemCount;

    public CategorySkeletonAdapter(Context context, int itemCount) {
        this.context = context;
        this.itemCount = itemCount;
    }

    @NonNull
    @Override
    public SkeletonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_skeleton, parent, false);
        return new SkeletonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SkeletonViewHolder holder, int position) {
        // Không cần logic đặc biệt, shimmer animation được xử lý bởi drawable
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    public static class SkeletonViewHolder extends RecyclerView.ViewHolder {

        public SkeletonViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
