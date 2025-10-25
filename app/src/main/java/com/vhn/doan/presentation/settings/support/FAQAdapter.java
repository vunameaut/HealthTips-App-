package com.vhn.doan.presentation.settings.support;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.FAQItem;

import java.util.List;

/**
 * Adapter cho RecyclerView hiển thị danh sách FAQ
 */
public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.FAQViewHolder> {

    private final List<FAQItem> faqList;

    public FAQAdapter(List<FAQItem> faqList) {
        this.faqList = faqList;
    }

    @NonNull
    @Override
    public FAQViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_faq, parent, false);
        return new FAQViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FAQViewHolder holder, int position) {
        FAQItem faqItem = faqList.get(position);
        holder.bind(faqItem);
    }

    @Override
    public int getItemCount() {
        return faqList.size();
    }

    static class FAQViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final ImageView ivIcon;
        private final TextView tvQuestion;
        private final TextView tvAnswer;
        private final ImageView ivExpand;

        public FAQViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvAnswer = itemView.findViewById(R.id.tvAnswer);
            ivExpand = itemView.findViewById(R.id.ivExpand);
        }

        public void bind(FAQItem faqItem) {
            tvQuestion.setText(faqItem.getQuestion());
            tvAnswer.setText(faqItem.getAnswer());

            // Set icon nếu có
            if (faqItem.getIconResId() != 0) {
                ivIcon.setImageResource(faqItem.getIconResId());
                ivIcon.setVisibility(View.VISIBLE);
            } else {
                ivIcon.setVisibility(View.GONE);
            }

            // Xử lý trạng thái mở rộng/thu gọn
            updateExpandState(faqItem.isExpanded());

            // Click để mở rộng/thu gọn
            cardView.setOnClickListener(v -> {
                faqItem.setExpanded(!faqItem.isExpanded());
                updateExpandState(faqItem.isExpanded());
            });
        }

        private void updateExpandState(boolean isExpanded) {
            if (isExpanded) {
                tvAnswer.setVisibility(View.VISIBLE);
                ivExpand.setRotation(180);
            } else {
                tvAnswer.setVisibility(View.GONE);
                ivExpand.setRotation(0);
            }
        }
    }
}

