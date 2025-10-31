package com.vhn.doan.presentation.settings.content;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;

import java.util.List;

/**
 * Adapter cho danh sách điều khoản và chính sách
 */
public class TermsPolicyAdapter extends RecyclerView.Adapter<TermsPolicyAdapter.ViewHolder> {

    private Context context;
    private List<TermsPolicyActivity.TermsPolicyItem> items;

    public TermsPolicyAdapter(Context context, List<TermsPolicyActivity.TermsPolicyItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_terms_policy, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TermsPolicyActivity.TermsPolicyItem item = items.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.ivIcon.setImageResource(item.getIconRes());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TermsPolicyDetailActivity.class);
            intent.putExtra("type", item.getType().name());
            intent.putExtra("title", item.getTitle());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        ImageView ivArrow;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            ivArrow = itemView.findViewById(R.id.ivArrow);
        }
    }
}

