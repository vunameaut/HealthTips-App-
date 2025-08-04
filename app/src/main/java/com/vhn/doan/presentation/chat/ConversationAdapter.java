package com.vhn.doan.presentation.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.Conversation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho danh sách các cuộc trò chuyện chat
 */
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private List<Conversation> conversations;
    private OnConversationClickListener onConversationClickListener;
    private final SimpleDateFormat timeFormat;
    private final SimpleDateFormat dateFormat;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ConversationAdapter() {
        this.conversations = new ArrayList<>();
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    public void setOnConversationClickListener(OnConversationClickListener listener) {
        this.onConversationClickListener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        holder.bind(conversation);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    /**
     * Cập nhật danh sách cuộc trò chuyện (reset toàn bộ)
     */
    public void setConversations(List<Conversation> conversations) {
        this.conversations.clear();
        if (conversations != null) {
            this.conversations.addAll(conversations);
        }
        notifyDataSetChanged();
    }

    /**
     * Thêm danh sách cuộc trò chuyện mới vào cuối (cho load more)
     */
    public void addConversations(List<Conversation> conversations) {
        if (conversations != null && !conversations.isEmpty()) {
            int startPosition = this.conversations.size();
            this.conversations.addAll(conversations);
            notifyItemRangeInserted(startPosition, conversations.size());
        }
    }

    /**
     * Xóa tất cả cuộc trò chuyện
     */
    public void clearConversations() {
        int size = conversations.size();
        conversations.clear();
        notifyItemRangeRemoved(0, size);
    }

    /**
     * Cập nhật một cuộc trò chuyện cụ thể
     */
    public void updateConversation(Conversation updatedConversation) {
        for (int i = 0; i < conversations.size(); i++) {
            if (conversations.get(i).getId().equals(updatedConversation.getId())) {
                conversations.set(i, updatedConversation);
                notifyItemChanged(i);
                break;
            }
        }
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        private TextView titleText;
        private TextView lastMessageText;
        private TextView timeText;
        private TextView messageCountText;
        private View unreadIndicator;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.tv_conversation_title);
            lastMessageText = itemView.findViewById(R.id.tv_last_message);
            timeText = itemView.findViewById(R.id.tv_conversation_time);
            messageCountText = itemView.findViewById(R.id.tv_message_count);
            unreadIndicator = itemView.findViewById(R.id.view_unread_indicator);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onConversationClickListener != null) {
                    onConversationClickListener.onConversationClick(conversations.get(position));
                }
            });
        }

        public void bind(Conversation conversation) {
            // Tiêu đề cuộc trò chuyện
            titleText.setText(conversation.getTitle() != null ? conversation.getTitle() : "Cuộc trò chuyện");

            // Tin nhắn cuối cùng
            if (conversation.getLastMessage() != null && !conversation.getLastMessage().isEmpty()) {
                String lastMessage = conversation.getLastMessage();
                // Giới hạn độ dài tin nhắn hiển thị
                if (lastMessage.length() > 50) {
                    lastMessage = lastMessage.substring(0, 47) + "...";
                }

                // Thêm prefix để biết tin nhắn từ ai
                String prefix = conversation.isFromUser() ? "Bạn: " : "AI: ";
                lastMessageText.setText(prefix + lastMessage);
                lastMessageText.setVisibility(View.VISIBLE);
            } else {
                lastMessageText.setText("Chưa có tin nhắn");
                lastMessageText.setVisibility(View.VISIBLE);
            }

            // Thời gian
            String timeStr = formatTime(conversation.getLastMessageTime());
            timeText.setText(timeStr);

            // Số lượng tin nhắn
            int count = conversation.getMessageCount();
            if (count > 0) {
                messageCountText.setText(String.valueOf(count));
                messageCountText.setVisibility(View.VISIBLE);
            } else {
                messageCountText.setVisibility(View.GONE);
            }

            // Unread indicator (tạm thời ẩn, có thể implement sau)
            unreadIndicator.setVisibility(View.GONE);
        }

        /**
         * Format thời gian hiển thị
         */
        private String formatTime(long timestamp) {
            if (timestamp <= 0) {
                return "";
            }

            try {
                Date messageDate = new Date(timestamp);
                Date currentDate = new Date();

                // Tính số ngày chênh lệch
                long diffInMillies = currentDate.getTime() - messageDate.getTime();
                long diffInDays = diffInMillies / (24 * 60 * 60 * 1000);

                if (diffInDays == 0) {
                    // Hôm nay - hiển thị giờ
                    return timeFormat.format(messageDate);
                } else if (diffInDays == 1) {
                    // Hôm qua
                    return "Hôm qua";
                } else if (diffInDays < 7) {
                    // Trong tuần - hiển thị số ngày
                    return diffInDays + " ngày trước";
                } else {
                    // Lâu hơn - hiển thị ngày tháng
                    return dateFormat.format(messageDate);
                }
            } catch (Exception e) {
                return "";
            }
        }
    }
}
