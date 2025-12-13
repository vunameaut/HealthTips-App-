package com.vhn.doan.presentation.report.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.vhn.doan.R;
import com.vhn.doan.data.model.Report;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Adapter cho danh sách Reports
 * Sử dụng ListAdapter với DiffUtil để tối ưu hiệu năng
 */
public class ReportAdapter extends ListAdapter<Report, ReportAdapter.ReportViewHolder> {

    private final OnReportClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnReportClickListener {
        void onReportClick(Report report);
    }

    public ReportAdapter(OnReportClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Report> DIFF_CALLBACK = new DiffUtil.ItemCallback<Report>() {
        @Override
        public boolean areItemsTheSame(@NonNull Report oldItem, @NonNull Report newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Report oldItem, @NonNull Report newItem) {
            return oldItem.getStatus().equals(newItem.getStatus()) &&
                   oldItem.getUpdatedAt() == newItem.getUpdatedAt() &&
                   oldItem.getLastMessageAt() == newItem.getLastMessageAt();
        }
    };

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ReportViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardView;
        private final TextView tvReportType;
        private final TextView tvStatus;
        private final TextView tvContent;
        private final LinearLayout layoutImageIndicator;
        private final ImageView ivMessageIcon;
        private final TextView tvLastMessage;
        private final TextView tvUnreadBadge;
        private final TextView tvTime;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvReportType = itemView.findViewById(R.id.tvReportType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvContent = itemView.findViewById(R.id.tvContent);
            layoutImageIndicator = itemView.findViewById(R.id.layoutImageIndicator);
            ivMessageIcon = itemView.findViewById(R.id.ivMessageIcon);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvUnreadBadge = itemView.findViewById(R.id.tvUnreadBadge);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(Report report) {
            // Report Type
            tvReportType.setText(report.getTitleDisplayName());
            setReportTypeBackground(report.getTitle());

            // Status
            tvStatus.setText(report.getStatusDisplayName());
            tvStatus.setTextColor(report.getStatusColor());
            tvStatus.setBackgroundTintList(ColorStateList.valueOf(
                    adjustAlpha(report.getStatusColor(), 0.15f)
            ));

            // Content
            tvContent.setText(report.getContent());

            // Image indicator
            layoutImageIndicator.setVisibility(report.hasImage() ? View.VISIBLE : View.GONE);

            // Last message
            if (report.getLastMessagePreview() != null && !report.getLastMessagePreview().isEmpty()) {
                tvLastMessage.setText(report.getLastMessagePreview());
                tvLastMessage.setVisibility(View.VISIBLE);
                ivMessageIcon.setVisibility(View.VISIBLE);
            } else {
                tvLastMessage.setText(R.string.no_messages_yet);
                tvLastMessage.setVisibility(View.VISIBLE);
                ivMessageIcon.setVisibility(View.VISIBLE);
            }

            // Time
            tvTime.setText(getTimeAgo(report.getLastMessageAt() > 0 ? 
                    report.getLastMessageAt() : report.getCreatedAt()));

            // Unread badge - TODO: implement unread count logic
            tvUnreadBadge.setVisibility(View.GONE);

            // Click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReportClick(report);
                }
            });

            // Highlight if report is closed
            if (Report.STATUS_CLOSED.equals(report.getStatus())) {
                cardView.setAlpha(0.7f);
            } else {
                cardView.setAlpha(1.0f);
            }
        }

        private void setReportTypeBackground(String type) {
            int color;
            switch (type) {
                case Report.TYPE_BUG:
                    color = Color.parseColor("#F44336"); // Red
                    break;
                case Report.TYPE_FEEDBACK:
                    color = Color.parseColor("#2196F3"); // Blue
                    break;
                case Report.TYPE_QUESTION:
                    color = Color.parseColor("#9C27B0"); // Purple
                    break;
                default:
                    color = Color.parseColor("#607D8B"); // Blue Grey
                    break;
            }
            tvReportType.setBackgroundTintList(ColorStateList.valueOf(color));
        }

        private String getTimeAgo(long timestamp) {
            if (timestamp == 0) {
                return "";
            }

            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            if (diff < TimeUnit.MINUTES.toMillis(1)) {
                return "Vừa xong";
            } else if (diff < TimeUnit.HOURS.toMillis(1)) {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                return minutes + " phút trước";
            } else if (diff < TimeUnit.DAYS.toMillis(1)) {
                long hours = TimeUnit.MILLISECONDS.toHours(diff);
                return hours + " giờ trước";
            } else if (diff < TimeUnit.DAYS.toMillis(7)) {
                long days = TimeUnit.MILLISECONDS.toDays(diff);
                return days + " ngày trước";
            } else {
                return dateFormat.format(new Date(timestamp));
            }
        }

        private int adjustAlpha(int color, float factor) {
            int alpha = Math.round(Color.alpha(color) * factor);
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            return Color.argb(Math.min(255, Math.max(0, (int)(255 * factor))), red, green, blue);
        }
    }
}

