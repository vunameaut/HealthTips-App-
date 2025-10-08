package com.vhn.doan.presentation.chat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.R;
import com.vhn.doan.data.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter cho danh sách tin nhắn chat
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ChatAdapter";
    private static final int VIEW_TYPE_USER_MESSAGE = 1;
    private static final int VIEW_TYPE_AI_MESSAGE = 2;
    private static final int VIEW_TYPE_TYPING = 3;

    private List<ChatMessage> messages;
    private boolean isAiTyping = false;
    private final SimpleDateFormat timeFormat;
    private FirebaseUser currentUser;
    private final DatabaseReference usersRef;
    private final Map<String, String> userAvatarCache;

    public ChatAdapter() {
        this.messages = new ArrayList<>();
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
        this.userAvatarCache = new HashMap<>();

        // Tải thông tin avatar cho người dùng hiện tại
        if (currentUser != null) {
            loadUserAvatar(currentUser.getUid());
        }
    }

    // Phương thức để tải avatar người dùng từ Firebase Database
    private void loadUserAvatar(String userId) {
        // Nếu đã có trong cache, không cần tải lại
        if (userAvatarCache.containsKey(userId)) {
            return;
        }

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Lấy URL avatar từ database - sử dụng photoUrl theo User model
                    String avatarUrl = snapshot.child("photoUrl").getValue(String.class);
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        userAvatarCache.put(userId, avatarUrl);
                        notifyDataSetChanged(); // Cập nhật lại adapter để hiển thị avatar mới
                        Log.d(TAG, "Đã tải avatar cho userId: " + userId + " - URL: " + avatarUrl);
                    } else {
                        Log.d(TAG, "Không tìm thấy photoUrl cho userId: " + userId);
                    }
                } else {
                    Log.d(TAG, "Không tìm thấy thông tin người dùng với userId: " + userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi khi tải avatar người dùng: " + error.getMessage());
            }
        });
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

        // Tải avatar cho tất cả người dùng trong danh sách tin nhắn
        for (ChatMessage message : messages) {
            if (message.isFromUser() && message.getUserId() != null) {
                loadUserAvatar(message.getUserId());
            }
        }

        notifyDataSetChanged();
    }

    /**
     * Thêm tin nhắn mới
     */
    public void addMessage(ChatMessage message) {
        this.messages.add(message);

        // Tải avatar nếu là tin nhắn từ người dùng
        if (message.isFromUser() && message.getUserId() != null) {
            loadUserAvatar(message.getUserId());
        }

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
        private ImageView avatarImage;

        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tv_user_message);
            timeText = itemView.findViewById(R.id.tv_user_time);
            avatarImage = itemView.findViewById(R.id.iv_user_avatar);
        }

        public void bind(ChatMessage message) {
            messageText.setText(message.getContent());
            timeText.setText(timeFormat.format(new Date(message.getTimestamp())));

            // Lấy userId từ tin nhắn hoặc từ người dùng hiện tại
            String userId = message.getUserId() != null ? message.getUserId() :
                           (currentUser != null ? currentUser.getUid() : null);

            if (userId != null) {
                // Thử lấy avatar từ cache trước
                String avatarUrl = userAvatarCache.get(userId);

                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    // Nếu có avatar trong cache, sử dụng nó
                    Log.d(TAG, "Hiển thị avatar từ cache cho userId: " + userId + " - URL: " + avatarUrl);
                    Glide.with(itemView.getContext())
                        .load(avatarUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(avatarImage);
                } else {
                    // Sử dụng avatar mặc định trước khi tải avatar thực
                    avatarImage.setImageResource(R.drawable.ic_person);

                    // Nếu chưa tải avatar, thử tải lại từ database
                    loadUserAvatar(userId);
                }
            } else {
                // Không có userId, hiển thị avatar mặc định
                avatarImage.setImageResource(R.drawable.ic_person);
            }
        }
    }

    // ViewHolder cho tin nhắn AI
    class AiMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private TextView timeText;
        private ImageView avatarImage;

        public AiMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tv_ai_message);
            timeText = itemView.findViewById(R.id.tv_ai_time);
            avatarImage = itemView.findViewById(R.id.iv_ai_avatar);
        }

        public void bind(ChatMessage message) {
            messageText.setText(message.getContent());
            timeText.setText(timeFormat.format(new Date(message.getTimestamp())));

            // Sử dụng logo của ứng dụng làm avatar cho AI
            avatarImage.setImageResource(R.mipmap.ic_logo);
        }
    }

    // ViewHolder cho typing indicator
    class TypingViewHolder extends RecyclerView.ViewHolder {
        public TypingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
