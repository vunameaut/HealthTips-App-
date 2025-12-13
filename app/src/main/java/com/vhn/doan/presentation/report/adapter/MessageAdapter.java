package com.vhn.doan.presentation.report.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.vhn.doan.R;
import com.vhn.doan.data.model.ReportMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Adapter cho danh sách tin nhắn trong Report Chat
 * Phân biệt tin nhắn từ user (phải) và admin (trái)
 */
public class MessageAdapter extends ListAdapter<ReportMessage, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_ADMIN = 2;

    private final OnImageClickListener imageClickListener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public interface OnImageClickListener {
        void onImageClick(String imageUrl);
    }

    public MessageAdapter(OnImageClickListener listener) {
        super(DIFF_CALLBACK);
        this.imageClickListener = listener;
    }

    private static final DiffUtil.ItemCallback<ReportMessage> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<ReportMessage>() {
            @Override
            public boolean areItemsTheSame(@NonNull ReportMessage oldItem, @NonNull ReportMessage newItem) {
                return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull ReportMessage oldItem, @NonNull ReportMessage newItem) {
                return oldItem.getTimestamp() == newItem.getTimestamp() &&
                       oldItem.isRead() == newItem.isRead();
            }
        };

    @Override
    public int getItemViewType(int position) {
        ReportMessage message = getItem(position);
        return message.isFromUser() ? VIEW_TYPE_USER : VIEW_TYPE_ADMIN;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_message_user, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_admin, parent, false);
            return new AdminMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ReportMessage message = getItem(position);
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof AdminMessageViewHolder) {
            ((AdminMessageViewHolder) holder).bind(message);
        }
    }

    /**
     * ViewHolder cho tin nhắn từ User (hiển thị bên phải)
     */
    class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardImage;
        private final ImageView ivMessageImage;
        private final TextView tvMessageText;
        private final TextView tvTime;

        UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.cardImage);
            ivMessageImage = itemView.findViewById(R.id.ivMessageImage);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        void bind(ReportMessage message) {
            // Text
            if (message.getText() != null && !message.getText().isEmpty()) {
                tvMessageText.setVisibility(View.VISIBLE);
                tvMessageText.setText(message.getText());
            } else {
                tvMessageText.setVisibility(View.GONE);
            }

            // Image
            if (message.hasImage()) {
                cardImage.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(message.getImageUrl())
                        .centerCrop()
                        .into(ivMessageImage);

                cardImage.setOnClickListener(v -> {
                    if (imageClickListener != null) {
                        imageClickListener.onImageClick(message.getImageUrl());
                    }
                });
            } else {
                cardImage.setVisibility(View.GONE);
            }

            // Time
            tvTime.setText(formatTime(message.getTimestamp()));
        }
    }

    /**
     * ViewHolder cho tin nhắn từ Admin (hiển thị bên trái)
     */
    class AdminMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSenderName;
        private final MaterialCardView cardImage;
        private final ImageView ivMessageImage;
        private final TextView tvMessageText;
        private final TextView tvTime;

        AdminMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            cardImage = itemView.findViewById(R.id.cardImage);
            ivMessageImage = itemView.findViewById(R.id.ivMessageImage);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        void bind(ReportMessage message) {
            // Sender name
            tvSenderName.setText(message.getSenderName() != null ? 
                    message.getSenderName() : "Admin");

            // Text
            if (message.getText() != null && !message.getText().isEmpty()) {
                tvMessageText.setVisibility(View.VISIBLE);
                tvMessageText.setText(message.getText());
            } else {
                tvMessageText.setVisibility(View.GONE);
            }

            // Image
            if (message.hasImage()) {
                cardImage.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(message.getImageUrl())
                        .centerCrop()
                        .into(ivMessageImage);

                cardImage.setOnClickListener(v -> {
                    if (imageClickListener != null) {
                        imageClickListener.onImageClick(message.getImageUrl());
                    }
                });
            } else {
                cardImage.setVisibility(View.GONE);
            }

            // Time
            tvTime.setText(formatTime(message.getTimestamp()));
        }
    }

    private String formatTime(long timestamp) {
        if (timestamp == 0) return "";

        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        // Nếu trong ngày hôm nay, chỉ hiển thị giờ
        if (diff < 24 * 60 * 60 * 1000) {
            return timeFormat.format(new Date(timestamp));
        } else {
            return dateFormat.format(new Date(timestamp));
        }
    }
}

