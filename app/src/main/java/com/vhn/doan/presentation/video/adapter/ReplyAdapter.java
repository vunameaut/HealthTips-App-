package com.vhn.doan.presentation.video.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.VideoComment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho danh sách reply bình luận
 */
public class ReplyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_REPLY = 0;
    private static final int TYPE_SHOW_MORE = 1;

    private final List<VideoComment> replies = new ArrayList<>();
    private OnReplyInteractionListener listener;
    private int hiddenRepliesCount = 0;
    private String parentCommentId;

    public interface OnReplyInteractionListener {
        void onLikeReply(VideoComment reply, int position);
        void onShowMoreReplies(String parentCommentId); // Thêm callback để hiển thị thêm replies
    }

    public void setOnReplyInteractionListener(OnReplyInteractionListener listener) {
        this.listener = listener;
    }

    public void updateReplies(List<VideoComment> newReplies) {
        updateReplies(newReplies, 0, null);
    }

    public void updateReplies(List<VideoComment> newReplies, int hiddenCount, String parentId) {
        replies.clear();
        this.hiddenRepliesCount = hiddenCount;
        this.parentCommentId = parentId;
        if (newReplies != null) {
            replies.addAll(newReplies);
        }
        notifyDataSetChanged();
    }

    public void addReply(VideoComment reply) {
        if (reply != null) {
            replies.add(reply); // Thêm vào cuối danh sách (replies theo thứ tự thời gian cũ -> mới)
            notifyItemInserted(replies.size() - 1);
        }
    }

    public int getViewType(int position) {
        if (position == replies.size() && hiddenRepliesCount > 0) {
            return TYPE_SHOW_MORE;
        }
        return TYPE_REPLY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SHOW_MORE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_show_more_replies, parent, false);
            return new ShowMoreViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reply, parent, false);
            return new ReplyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ReplyViewHolder) {
            VideoComment reply = replies.get(position);
            ((ReplyViewHolder) holder).bind(reply, position);
        } else if (holder instanceof ShowMoreViewHolder) {
            ((ShowMoreViewHolder) holder).bind(hiddenRepliesCount);
        }
    }

    @Override
    public int getItemCount() {
        return replies.size() + (hiddenRepliesCount > 0 ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        return getViewType(position);
    }

    public class ReplyViewHolder extends RecyclerView.ViewHolder {

        private final ImageView userAvatar;
        private final TextView userName;
        private final TextView replyTime;
        private final TextView replyText;
        private final LinearLayout likeButton;
        private final ImageView likeIcon;
        private final TextView likeCount;

        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);

            userAvatar = itemView.findViewById(R.id.iv_reply_user_avatar);
            userName = itemView.findViewById(R.id.tv_reply_user_name);
            replyTime = itemView.findViewById(R.id.tv_reply_time);
            replyText = itemView.findViewById(R.id.tv_reply_text);
            likeButton = itemView.findViewById(R.id.btn_like_reply);
            likeIcon = itemView.findViewById(R.id.iv_like_reply);
            likeCount = itemView.findViewById(R.id.tv_reply_like_count);

            // Set up click listeners
            likeButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onLikeReply(replies.get(position), position);
                }
            });
        }

        public void bind(VideoComment reply, int position) {
            // Set user name (có thể lấy từ User ID trong tương lai)
            userName.setText("Người dùng " + reply.getUserId().substring(0, Math.min(6, reply.getUserId().length())));

            // Set reply text
            replyText.setText(reply.getText());

            // Set time
            replyTime.setText(formatReplyTime(reply.getCreatedAt()));

            // Set like count
            likeCount.setText(String.valueOf(reply.getLikeCount()));

            // Update like status (sẽ được implement sau khi có thông tin user hiện tại)
            // updateLikeStatus(reply.isLikedByCurrentUser());
        }

        private String formatReplyTime(Object createdAt) {
            if (createdAt instanceof Long) {
                long timestamp = (Long) createdAt;
                long currentTime = System.currentTimeMillis();
                long diff = currentTime - timestamp;

                if (diff < 60000) { // < 1 minute
                    return "Vừa xong";
                } else if (diff < 3600000) { // < 1 hour
                    return (diff / 60000) + " phút trước";
                } else if (diff < 86400000) { // < 1 day
                    return (diff / 3600000) + " giờ trước";
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
                    return sdf.format(new Date(timestamp));
                }
            }
            return "";
        }

        private void updateLikeStatus(boolean isLiked) {
            if (isLiked) {
                likeIcon.setImageResource(R.drawable.ic_heart_filled);
                likeIcon.setColorFilter(itemView.getContext().getColor(R.color.red_heart));
            } else {
                likeIcon.setImageResource(R.drawable.ic_heart_outline);
                likeIcon.clearColorFilter();
            }
        }
    }

    public class ShowMoreViewHolder extends RecyclerView.ViewHolder {

        private final TextView showMoreText;

        public ShowMoreViewHolder(@NonNull View itemView) {
            super(itemView);

            showMoreText = itemView.findViewById(R.id.tv_show_more_replies);

            // Set up click listener
            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION || listener == null) {
                    return;
                }
                listener.onShowMoreReplies(parentCommentId);
            });
        }

        public void bind(int hiddenCount) {
            if (hiddenCount > 0) {
                showMoreText.setText("Xem thêm " + hiddenCount + " câu trả lời");
            } else {
                showMoreText.setText("");
            }
        }
    }
}
