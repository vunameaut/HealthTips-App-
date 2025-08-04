package com.vhn.doan.presentation.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho danh sách tin nhắn chat
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER_MESSAGE = 1;
    private static final int VIEW_TYPE_AI_MESSAGE = 2;
    private static final int VIEW_TYPE_TYPING = 3;

    private List<ChatMessage> messages;
    private boolean isAiTyping = false;
    private final SimpleDateFormat timeFormat;

    public ChatAdapter() {
        this.messages = new ArrayList<>();
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @Override
    public int getItemViewType(int position) {
        if (isAiTyping && position == getItemCount() - 1) {
            return VIEW_TYPE_TYPING;
        }

        ChatMessage message = messages.get(position);
        return message.isFromUser() ? VIEW_TYPE_USER_MESSAGE : VIEW_TYPE_AI_MESSAGE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_USER_MESSAGE:
                View userView = inflater.inflate(R.layout.item_chat_user_message, parent, false);
                return new UserMessageViewHolder(userView);

            case VIEW_TYPE_AI_MESSAGE:
                View aiView = inflater.inflate(R.layout.item_chat_ai_message, parent, false);
                return new AiMessageViewHolder(aiView);

            case VIEW_TYPE_TYPING:
                View typingView = inflater.inflate(R.layout.item_chat_typing, parent, false);
                return new TypingViewHolder(typingView);

            default:
                throw new IllegalArgumentException("Unknown view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TypingViewHolder) {
            // Không cần bind data cho typing indicator
            return;
        }

        ChatMessage message = messages.get(position);

        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof AiMessageViewHolder) {
            ((AiMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        int count = messages.size();
        if (isAiTyping) {
            count++;
        }
        return count;
    }

    /**
     * Cập nhật danh sách tin nhắn
     */
    public void setMessages(List<ChatMessage> messages) {
        this.messages.clear();
        this.messages.addAll(messages);
        notifyDataSetChanged();
    }

    /**
     * Thêm tin nhắn mới
     */
    public void addMessage(ChatMessage message) {
        this.messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    /**
     * Hiển thị trạng thái AI đang gõ
     */
    public void showAiTyping() {
        if (!isAiTyping) {
            isAiTyping = true;
            notifyItemInserted(getItemCount() - 1);
        }
    }

    /**
     * Ẩn trạng thái AI đang gõ
     */
    public void hideAiTyping() {
        if (isAiTyping) {
            isAiTyping = false;
            notifyItemRemoved(getItemCount());
        }
    }

    /**
     * Kiểm tra AI có đang gõ không
     */
    public boolean isAiTyping() {
        return isAiTyping;
    }

    /**
     * Xóa tất cả tin nhắn
     */
    public void clearMessages() {
        int size = messages.size();
        messages.clear();
        notifyItemRangeRemoved(0, size);
    }

    // ViewHolder cho tin nhắn người dùng
    class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private TextView timeText;

        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tv_user_message);
            timeText = itemView.findViewById(R.id.tv_user_time);
        }

        public void bind(ChatMessage message) {
            messageText.setText(message.getContent());
            timeText.setText(timeFormat.format(new Date(message.getTimestamp())));
        }
    }

    // ViewHolder cho tin nhắn AI
    class AiMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private TextView timeText;

        public AiMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tv_ai_message);
            timeText = itemView.findViewById(R.id.tv_ai_time);
        }

        public void bind(ChatMessage message) {
            messageText.setText(message.getContent());
            timeText.setText(timeFormat.format(new Date(message.getTimestamp())));
        }
    }

    // ViewHolder cho typing indicator
    class TypingViewHolder extends RecyclerView.ViewHolder {
        public TypingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
