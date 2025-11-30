package com.vhn.doan.presentation.support;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vhn.doan.R;
import com.vhn.doan.model.SupportMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying support chat messages
 * Shows user messages on the right, admin messages on the left
 */
public class SupportChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_ADMIN = 2;

    private List<SupportMessage> messages;
    private SimpleDateFormat timeFormat;
    private OnMessageImageClickListener imageClickListener;

    public interface OnMessageImageClickListener {
        void onImageClick(String imageUrl);
    }

    public SupportChatAdapter() {
        this.messages = new ArrayList<>();
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    public void setMessages(List<SupportMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public void setOnMessageImageClickListener(OnMessageImageClickListener listener) {
        this.imageClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        SupportMessage message = messages.get(position);
        return "user".equals(message.getSenderType()) ? VIEW_TYPE_USER : VIEW_TYPE_ADMIN;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_support_message_user, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_support_message_admin, parent, false);
            return new AdminMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SupportMessage message = messages.get(position);

        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof AdminMessageViewHolder) {
            ((AdminMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * ViewHolder for user messages (shown on right side)
     */
    class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvTime;
        ImageView ivImage;

        UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_user_message);
            tvTime = itemView.findViewById(R.id.tv_user_time);
            ivImage = itemView.findViewById(R.id.iv_user_image);
        }

        void bind(SupportMessage message) {
            // Set message text
            if (!TextUtils.isEmpty(message.getText())) {
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setText(message.getText());
            } else {
                tvMessage.setVisibility(View.GONE);
            }

            // Set time
            Date date = new Date(message.getTimestamp());
            tvTime.setText(timeFormat.format(date));

            // Set image if available
            if (!TextUtils.isEmpty(message.getImageUrl())) {
                ivImage.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(ivImage);

                ivImage.setOnClickListener(v -> {
                    if (imageClickListener != null) {
                        imageClickListener.onImageClick(message.getImageUrl());
                    }
                });
            } else {
                ivImage.setVisibility(View.GONE);
            }
        }
    }

    /**
     * ViewHolder for admin messages (shown on left side)
     */
    class AdminMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvTime;
        TextView tvSenderName;
        ImageView ivImage;

        AdminMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_admin_message);
            tvTime = itemView.findViewById(R.id.tv_admin_time);
            tvSenderName = itemView.findViewById(R.id.tv_admin_name);
            ivImage = itemView.findViewById(R.id.iv_admin_image);
        }

        void bind(SupportMessage message) {
            // Set sender name (Admin name)
            if (!TextUtils.isEmpty(message.getSenderName())) {
                tvSenderName.setVisibility(View.VISIBLE);
                tvSenderName.setText(message.getSenderName());
            } else {
                tvSenderName.setVisibility(View.GONE);
            }

            // Set message text
            if (!TextUtils.isEmpty(message.getText())) {
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setText(message.getText());
            } else {
                tvMessage.setVisibility(View.GONE);
            }

            // Set time
            Date date = new Date(message.getTimestamp());
            tvTime.setText(timeFormat.format(date));

            // Set image if available
            if (!TextUtils.isEmpty(message.getImageUrl())) {
                ivImage.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(ivImage);

                ivImage.setOnClickListener(v -> {
                    if (imageClickListener != null) {
                        imageClickListener.onImageClick(message.getImageUrl());
                    }
                });
            } else {
                ivImage.setVisibility(View.GONE);
            }
        }
    }
}
