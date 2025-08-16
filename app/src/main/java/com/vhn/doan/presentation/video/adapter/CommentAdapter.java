package com.vhn.doan.presentation.video.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.VideoComment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho danh sách bình luận video
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<VideoComment> comments = new ArrayList<>();
    private OnCommentInteractionListener listener;

    public interface OnCommentInteractionListener {
        void onLikeComment(VideoComment comment, int position);
        void onReplyComment(VideoComment comment, int position);
        void onShowReplies(VideoComment comment, int position);
    }

    public void setOnCommentInteractionListener(OnCommentInteractionListener listener) {
        this.listener = listener;
    }

    public void updateComments(List<VideoComment> newComments) {
        comments.clear();
        if (newComments != null) {
            comments.addAll(newComments);
        }
        notifyDataSetChanged();
    }

    public void addComment(VideoComment comment) {
        if (comment != null) {
            comments.add(0, comment); // Thêm vào đầu danh sách
            notifyItemInserted(0);
        }
    }

    public void updateComment(VideoComment comment, int position) {
        if (position >= 0 && position < comments.size() && comment != null) {
            comments.set(position, comment);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        VideoComment comment = comments.get(position);
        holder.bind(comment, position);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {

        private final ImageView userAvatar;
        private final TextView userName;
        private final TextView commentTime;
        private final TextView commentText;
        private final LinearLayout likeButton;
        private final ImageView likeIcon;
        private final TextView likeCount;
        private final TextView replyButton;
        private final TextView replyCount;
        private final RecyclerView repliesRecyclerView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            userAvatar = itemView.findViewById(R.id.iv_user_avatar);
            userName = itemView.findViewById(R.id.tv_user_name);
            commentTime = itemView.findViewById(R.id.tv_comment_time);
            commentText = itemView.findViewById(R.id.tv_comment_text);
            likeButton = itemView.findViewById(R.id.btn_like_comment);
            likeIcon = itemView.findViewById(R.id.iv_like_comment);
            likeCount = itemView.findViewById(R.id.tv_like_count);
            replyButton = itemView.findViewById(R.id.btn_reply_comment);
            replyCount = itemView.findViewById(R.id.tv_reply_count);
            repliesRecyclerView = itemView.findViewById(R.id.rv_replies);

            // Set up click listeners
            likeButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onLikeComment(comments.get(position), position);
                }
            });

            replyButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onReplyComment(comments.get(position), position);
                }
            });

            replyCount.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onShowReplies(comments.get(position), position);
                }
            });
        }

        public void bind(VideoComment comment, int position) {
            // Set user name (có thể lấy từ User ID trong tương lai)
            userName.setText("Người dùng " + comment.getUserId().substring(0, Math.min(6, comment.getUserId().length())));

            // Set comment text
            commentText.setText(comment.getText());

            // Set time
            commentTime.setText(formatCommentTime(comment.getCreatedAt()));

            // Set like count
            likeCount.setText(String.valueOf(comment.getLikeCount()));

            // Set reply count
            if (comment.getReplyCount() > 0) {
                replyCount.setVisibility(View.VISIBLE);
                replyCount.setText("Xem " + comment.getReplyCount() + " câu trả lời");
            } else {
                replyCount.setVisibility(View.GONE);
            }

            // TODO: Set like status based on current user
            updateLikeIcon(false); // Default to not liked
        }

        public void updateLikeIcon(boolean isLiked) {
            if (isLiked) {
                likeIcon.setImageResource(R.drawable.ic_heart_filled);
                likeIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.like_color_active));
            } else {
                likeIcon.setImageResource(R.drawable.ic_heart_outline);
                likeIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.like_color_inactive));
            }
        }

        private String formatCommentTime(Object timestamp) {
            if (timestamp == null) return "Vừa xong";

            long time;
            if (timestamp instanceof Long) {
                time = (Long) timestamp;
            } else {
                return "Vừa xong";
            }

            long now = System.currentTimeMillis();
            long diff = now - time;

            // Tính toán thời gian
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) {
                return days + " ngày trước";
            } else if (hours > 0) {
                return hours + " giờ trước";
            } else if (minutes > 0) {
                return minutes + " phút trước";
            } else {
                return "Vừa xong";
            }
        }
    }
}
