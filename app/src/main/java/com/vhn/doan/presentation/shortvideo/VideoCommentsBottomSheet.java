package com.vhn.doan.presentation.shortvideo;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.vhn.doan.R;
import com.vhn.doan.data.VideoComment;
import com.vhn.doan.data.repository.RepositoryCallback;
import com.vhn.doan.data.repository.ShortVideoRepository;
import com.vhn.doan.data.repository.ShortVideoRepositoryImpl;
import com.vhn.doan.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Bottom sheet hiển thị danh sách bình luận giống TikTok: mỗi bình luận có avatar, tên, nội dung,
 * thời gian, nút thích, số lượt thích và nút trả lời.
 */
public class VideoCommentsBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_VIDEO_ID = "video_id";

    private String videoId;
    private RecyclerView recyclerView;
    private TextView txtNoComments;
    private EditText editComment;
    private ImageView btnSend;
    private CommentsAdapter adapter;
    private ShortVideoRepository repository;
    private SharedPreferencesHelper prefs;
    private String replyPath = null;

    public static VideoCommentsBottomSheet newInstance(String videoId) {
        VideoCommentsBottomSheet sheet = new VideoCommentsBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_ID, videoId);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_video_comments, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewComments);
        txtNoComments = view.findViewById(R.id.txtNoComments);
        editComment = view.findViewById(R.id.editComment);
        btnSend = view.findViewById(R.id.btnSendComment);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getArguments() != null) {
            videoId = getArguments().getString(ARG_VIDEO_ID);
        }

        repository = new ShortVideoRepositoryImpl();
        prefs = new SharedPreferencesHelper(requireContext());
        String currentUserId = prefs.getCurrentUserId();

        adapter = new CommentsAdapter(new ArrayList<>(), videoId, repository, currentUserId,
                (path, userName) -> {
                    replyPath = path;
                    editComment.setHint(getString(R.string.reply_to, userName));
                });
        recyclerView.setAdapter(adapter);

        loadComments();

        btnSend.setOnClickListener(v -> {
            String text = editComment.getText().toString().trim();
            if (!text.isEmpty()) {
                VideoComment comment = new VideoComment(currentUserId, text, System.currentTimeMillis());
                comment.setUserName(prefs.getUserName());
                if (replyPath == null) {
                    repository.addComment(videoId, comment, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            editComment.setText("");
                            editComment.setHint(getString(R.string.enter_comment));
                            adapter.addComment(comment);
                            updateEmptyState();
                        }

                        @Override
                        public void onError(String error) {
                            // Ignore for now
                        }
                    });
                } else {
                    repository.addReply(videoId, replyPath, comment, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            editComment.setText("");
                            editComment.setHint(getString(R.string.enter_comment));
                            adapter.addReply(replyPath, comment);
                            replyPath = null;
                            updateEmptyState();
                        }

                        @Override
                        public void onError(String error) {
                            // Ignore for now
                        }
                    });
                }
            }
        });

        return view;
    }

    private void loadComments() {
        repository.getComments(videoId, new RepositoryCallback<List<VideoComment>>() {
            @Override
            public void onSuccess(List<VideoComment> comments) {
                adapter.setComments(comments);
                updateEmptyState();
            }

            @Override
            public void onError(String error) {
                updateEmptyState();
            }
        });
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            txtNoComments.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            txtNoComments.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /** Adapter hiển thị danh sách bình luận và phản hồi */
    private static class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
        private final List<CommentItem> items;
        private final String videoId;
        private final ShortVideoRepository repository;
        private final String currentUserId;
        private final OnReplyClickListener replyClickListener;

        CommentsAdapter(List<CommentItem> items, String videoId, ShortVideoRepository repository,
                        String currentUserId, OnReplyClickListener replyClickListener) {
            this.items = items;
            this.videoId = videoId;
            this.repository = repository;
            this.currentUserId = currentUserId;
            this.replyClickListener = replyClickListener;
        }

        void setComments(List<VideoComment> comments) {
            items.clear();
            for (VideoComment c : comments) {
                items.add(new CommentItem(c, c.getId(), false, null));
                for (VideoComment r : c.getReplies()) {
                    items.add(new CommentItem(r, c.getId() + "/replies/" + r.getId(), true, c.getId()));
                }
            }
            notifyDataSetChanged();
        }

        void addComment(VideoComment comment) {
            items.add(0, new CommentItem(comment, comment.getId(), false, null));
            notifyItemInserted(0);
        }

        void addReply(String parentPath, VideoComment reply) {
            int insertPos = items.size();
            for (int i = 0; i < items.size(); i++) {
                CommentItem item = items.get(i);
                if (!item.isReply && item.path.equals(parentPath)) {
                    insertPos = i + 1;
                    while (insertPos < items.size() && items.get(insertPos).isReply && items.get(insertPos).parentId.equals(parentPath)) {
                        insertPos++;
                    }
                    break;
                }
            }
            items.add(insertPos, new CommentItem(reply, parentPath + "/replies/" + reply.getId(), true, parentPath));
            notifyItemInserted(insertPos);
        }

        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_video_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
            CommentItem item = items.get(position);
            VideoComment comment = item.comment;

            holder.txtUser.setText(comment.getUserName() == null ? comment.getUserId() : comment.getUserName());
            holder.txtComment.setText(comment.getComment());
            CharSequence time = DateUtils.getRelativeTimeSpanString(comment.getTimestamp(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
            holder.txtTime.setText(time);
            holder.txtLikeCount.setText(String.valueOf(comment.getLikeCount()));
            holder.btnLike.setImageResource(comment.isLikedBy(currentUserId) ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            holder.btnLike.setColorFilter(comment.isLikedBy(currentUserId) ? holder.itemView.getResources().getColor(R.color.color_like) : holder.itemView.getResources().getColor(R.color.white));

            // Load avatar
            Glide.with(holder.itemView.getContext())
                    .load(comment.getAvatarUrl())
                    .placeholder(R.drawable.ic_user_placeholder)
                    .circleCrop()
                    .into(holder.imgAvatar);

            // Indent replies
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
            if (item.isReply) {
                params.leftMargin = (int) (holder.itemView.getResources().getDisplayMetrics().density * 40);
            } else {
                params.leftMargin = 0;
            }
            holder.itemView.setLayoutParams(params);

            holder.btnLike.setOnClickListener(v -> {
                boolean newLike = !comment.isLikedBy(currentUserId);
                repository.likeComment(videoId, item.path, currentUserId, newLike, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (newLike) {
                            comment.getLikes().put(currentUserId, true);
                        } else {
                            comment.getLikes().remove(currentUserId);
                        }
                        notifyItemChanged(holder.getAdapterPosition());
                    }

                    @Override
                    public void onError(String error) {
                        // ignore
                    }
                });
            });

            holder.btnReply.setOnClickListener(v -> {
                if (replyClickListener != null) {
                    String parentPath = item.isReply ? item.parentId : item.path;
                    replyClickListener.onReplyClicked(parentPath, holder.txtUser.getText().toString());
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class CommentViewHolder extends RecyclerView.ViewHolder {
            ImageView imgAvatar;
            TextView txtUser;
            TextView txtComment;
            TextView txtTime;
            ImageView btnLike;
            TextView txtLikeCount;
            TextView btnReply;

            CommentViewHolder(@NonNull View itemView) {
                super(itemView);
                imgAvatar = itemView.findViewById(R.id.imgCommentAvatar);
                txtUser = itemView.findViewById(R.id.txtCommentUser);
                txtComment = itemView.findViewById(R.id.txtCommentText);
                txtTime = itemView.findViewById(R.id.txtCommentTime);
                btnLike = itemView.findViewById(R.id.btnCommentLike);
                txtLikeCount = itemView.findViewById(R.id.txtCommentLikeCount);
                btnReply = itemView.findViewById(R.id.btnCommentReply);
            }
        }

        private static class CommentItem {
            final VideoComment comment;
            final String path; // path under /comments
            final boolean isReply;
            final String parentId;

            CommentItem(VideoComment comment, String path, boolean isReply, String parentId) {
                this.comment = comment;
                this.path = path;
                this.isReply = isReply;
                this.parentId = parentId;
            }
        }

        interface OnReplyClickListener {
            void onReplyClicked(String path, String userName);
        }
    }
}

