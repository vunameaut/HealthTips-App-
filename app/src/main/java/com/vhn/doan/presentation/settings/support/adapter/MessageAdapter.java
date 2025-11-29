package com.vhn.doan.presentation.settings.support.adapter;

import android.content.Context;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_ADMIN = 2;

    private Context context;
    private List<SupportMessage> messages;
    private SimpleDateFormat timeFormat;

    public MessageAdapter(Context context, List<SupportMessage> messages) {
        this.context = context;
        this.messages = messages;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @Override
    public int getItemViewType(int position) {
        SupportMessage message = messages.get(position);
        if ("user".equals(message.getSenderType())) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_ADMIN;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_user, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_admin, parent, false);
            return new AdminMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SupportMessage message = messages.get(position);

        if (holder instanceof UserMessageViewHolder) {
            bindUserMessage((UserMessageViewHolder) holder, message);
        } else if (holder instanceof AdminMessageViewHolder) {
            bindAdminMessage((AdminMessageViewHolder) holder, message);
        }
    }

    private void bindUserMessage(UserMessageViewHolder holder, SupportMessage message) {
        // Set message text
        if (message.getText() != null && !message.getText().isEmpty()) {
            holder.messageText.setText(message.getText());
            holder.messageText.setVisibility(View.VISIBLE);
        } else {
            holder.messageText.setVisibility(View.GONE);
        }

        // Set message image if available
        if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
            holder.messageImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(message.getImageUrl())
                    .centerCrop()
                    .into(holder.messageImage);
        } else {
            holder.messageImage.setVisibility(View.GONE);
        }

        // Set timestamp
        holder.messageTime.setText(timeFormat.format(new Date(message.getTimestamp())));
    }

    private void bindAdminMessage(AdminMessageViewHolder holder, SupportMessage message) {
        // Set message text
        if (message.getText() != null && !message.getText().isEmpty()) {
            holder.messageText.setText(message.getText());
            holder.messageText.setVisibility(View.VISIBLE);
        } else {
            holder.messageText.setVisibility(View.GONE);
        }

        // Set message image if available
        if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
            holder.messageImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(message.getImageUrl())
                    .centerCrop()
                    .into(holder.messageImage);
        } else {
            holder.messageImage.setVisibility(View.GONE);
        }

        // Set timestamp
        holder.messageTime.setText(timeFormat.format(new Date(message.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder for user messages
    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView messageImage;
        TextView messageTime;

        UserMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageImage = itemView.findViewById(R.id.messageImage);
            messageTime = itemView.findViewById(R.id.messageTime);
        }
    }

    // ViewHolder for admin messages
    static class AdminMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView messageImage;
        TextView messageTime;

        AdminMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageImage = itemView.findViewById(R.id.messageImage);
            messageTime = itemView.findViewById(R.id.messageTime);
        }
    }

    public void updateMessages(List<SupportMessage> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }
}
