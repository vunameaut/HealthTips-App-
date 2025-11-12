package com.vhn.doan.presentation.video.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.VideoComment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter cho danh sách bình luận video với hỗ trợ replies
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private static final int MAX_REPLIES_SHOW = 3; // Số lượng replies tối đa hiển thị ban đầu

    private final List<VideoComment> comments = new ArrayList<>();
    private final Map<String, List<VideoComment>> repliesMap = new HashMap<>();
    private final Map<String, Boolean> expandedComments = new HashMap<>();
    private final Map<String, Boolean> showAllReplies = new HashMap<>(); // Theo dõi trạng thái hiển thị tất cả replies
    private OnCommentInteractionListener listener;

    public interface OnCommentInteractionListener {
        void onLikeComment(VideoComment comment, int position);
        void onReplyComment(VideoComment comment, int position);
        void onShowReplies(VideoComment comment, int position);
        void onLikeReply(VideoComment reply, String parentCommentId);
        void onShowMoreReplies(String parentCommentId); // Thêm callback để hiển thị thêm replies
    }

    public void setOnCommentInteractionListener(OnCommentInteractionListener listener) {
        this.listener = listener;
    }

    public void updateComments(List<VideoComment> newComments) {
        comments.clear();
        repliesMap.clear();
        expandedComments.clear();
        showAllReplies.clear();
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

    public void addReply(VideoComment reply, String parentCommentId) {
        if (reply != null && parentCommentId != null) {
            List<VideoComment> replies = repliesMap.get(parentCommentId);
            if (replies == null) {
                replies = new ArrayList<>();
                repliesMap.put(parentCommentId, replies);
            }
            replies.add(reply);

            // Cập nhật reply count của comment cha
            for (int i = 0; i < comments.size(); i++) {
                VideoComment comment = comments.get(i);
                if (comment.getId().equals(parentCommentId)) {
                    comment.setReplyCount(comment.getReplyCount() + 1);
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    public void updateReplies(String commentId, List<VideoComment> replies) {
        repliesMap.put(commentId, new ArrayList<>(replies));

        // Tìm vị trí comment và cập nhật
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).getId().equals(commentId)) {
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void toggleRepliesVisibility(String commentId) {
        Boolean isExpanded = expandedComments.get(commentId);
        expandedComments.put(commentId, isExpanded == null ? true : !isExpanded);

        // Tìm vị trí comment và cập nhật
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).getId().equals(commentId)) {
                notifyItemChanged(i);
                break;
            }
        }
    }

    /**
     * Toggle trạng thái hiển thị tất cả replies cho một comment
     */
    public void toggleShowAllReplies(String commentId) {
        Boolean showAll = showAllReplies.get(commentId);
        showAllReplies.put(commentId, showAll == null ? true : !showAll);

        // Tìm và cập nhật comment
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).getId().equals(commentId)) {
                notifyItemChanged(i);
                break;
            }
        }
    }

    /**
     * Bắt buộc mở rộng replies cho một comment (không toggle)
     */
    public void forceExpandReplies(String commentId) {
        expandedComments.put(commentId, true);

        // Tìm và cập nhật comment
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).getId().equals(commentId)) {
                notifyItemChanged(i);
                break;
            }
        }

        android.util.Log.d("CommentAdapter", "Force expanding replies for comment: " + commentId);
    }

    /**
     * Mở rộng replies cho một comment (alias cho forceExpandReplies)
     */
    public void expandReplies(String commentId) {
        forceExpandReplies(commentId);
    }

    /**
     * Lấy comment tại position cụ thể
     */
    public VideoComment getCommentAt(int position) {
        if (position >= 0 && position < comments.size()) {
            return comments.get(position);
        }
        return null;
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

        private ReplyAdapter replyAdapter;

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

            // Setup replies RecyclerView
            setupRepliesRecyclerView();

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
                    VideoComment comment = comments.get(position);
                    toggleRepliesVisibility(comment.getId());
                    listener.onShowReplies(comment, position);
                }
            });
        }

        private void setupRepliesRecyclerView() {
            repliesRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            replyAdapter = new ReplyAdapter();
            repliesRecyclerView.setAdapter(replyAdapter);

            replyAdapter.setOnReplyInteractionListener(new ReplyAdapter.OnReplyInteractionListener() {
                @Override
                public void onLikeReply(VideoComment reply, int position) {
                    int commentPosition = getBindingAdapterPosition();
                    if (commentPosition != RecyclerView.NO_POSITION && listener != null) {
                        VideoComment comment = comments.get(commentPosition);
                        listener.onLikeReply(reply, comment.getId());
                    }
                }

                @Override
                public void onShowMoreReplies(String parentCommentId) {
                    if (listener != null) {
                        listener.onShowMoreReplies(parentCommentId);
                    }
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

            // Set reply count và visibility
            if (comment.getReplyCount() > 0) {
                replyCount.setVisibility(View.VISIBLE);
                Boolean isExpanded = expandedComments.get(comment.getId());
                if (isExpanded != null && isExpanded) {
                    replyCount.setText("Ẩn câu trả lời");
                } else {
                    replyCount.setText("Xem " + comment.getReplyCount() + " câu trả lời");
                }
            } else {
                replyCount.setVisibility(View.GONE);
            }

            // Setup replies - Đây là phần quan trọng!
            setupReplies(comment);
        }

        private void setupReplies(VideoComment comment) {
            Boolean isExpanded = expandedComments.get(comment.getId());
            List<VideoComment> replies = repliesMap.get(comment.getId());

            if (isExpanded != null && isExpanded && replies != null && !replies.isEmpty()) {
                repliesRecyclerView.setVisibility(View.VISIBLE);

                // Kiểm tra xem có cần hiển thị giới hạn replies không
                Boolean showAll = showAllReplies.get(comment.getId());
                List<VideoComment> repliesToShow;

                if (replies.size() > MAX_REPLIES_SHOW && (showAll == null || !showAll)) {
                    // Hiển thị chỉ một số replies đầu tiên
                    repliesToShow = replies.subList(0, MAX_REPLIES_SHOW);
                    replyAdapter.updateReplies(repliesToShow, replies.size() - MAX_REPLIES_SHOW, comment.getId());
                } else {
                    // Hiển thị tất cả replies
                    repliesToShow = replies;
                    replyAdapter.updateReplies(repliesToShow, 0, comment.getId());
                }

                // Debug log để kiểm tra
                android.util.Log.d("CommentAdapter", "Setting up replies for comment " + comment.getId() +
                        ": total=" + replies.size() + ", showing=" + repliesToShow.size());
            } else {
                repliesRecyclerView.setVisibility(View.GONE);
                android.util.Log.d("CommentAdapter", "Hiding replies for comment " + comment.getId() +
                        ": expanded=" + isExpanded + ", replies=" + (replies != null ? replies.size() : "null"));
            }
        }

        private String formatCommentTime(Object createdAt) {
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
    }
}
