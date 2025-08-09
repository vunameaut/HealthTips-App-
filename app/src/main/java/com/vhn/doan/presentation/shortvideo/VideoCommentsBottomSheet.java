package com.vhn.doan.presentation.shortvideo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.VideoComment;
import com.vhn.doan.data.repository.RepositoryCallback;
import com.vhn.doan.data.repository.ShortVideoRepository;
import com.vhn.doan.data.repository.ShortVideoRepositoryImpl;
import com.vhn.doan.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Bottom sheet hiển thị danh sách bình luận và input giống TikTok
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
        adapter = new CommentsAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        repository = new ShortVideoRepositoryImpl();
        prefs = new SharedPreferencesHelper(requireContext());
        if (getArguments() != null) {
            videoId = getArguments().getString(ARG_VIDEO_ID);
        }

        loadComments();

        btnSend.setOnClickListener(v -> {
            String text = editComment.getText().toString().trim();
            if (!text.isEmpty()) {
                VideoComment comment = new VideoComment(prefs.getCurrentUserId(), text, System.currentTimeMillis());
                repository.addComment(videoId, comment, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        editComment.setText("");
                        adapter.addComment(comment);
                        updateEmptyState();
                    }

                    @Override
                    public void onError(String error) {
                        // Có thể hiển thị Toast nếu cần
                    }
                });
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

    private static class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
        private final List<VideoComment> comments;

        CommentsAdapter(List<VideoComment> comments) {
            this.comments = comments;
        }

        void setComments(List<VideoComment> newComments) {
            comments.clear();
            comments.addAll(newComments);
            notifyDataSetChanged();
        }

        void addComment(VideoComment comment) {
            comments.add(0, comment);
            notifyItemInserted(0);
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
            VideoComment comment = comments.get(position);
            holder.txtUser.setText(comment.getUserId());
            holder.txtComment.setText(comment.getComment());
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        static class CommentViewHolder extends RecyclerView.ViewHolder {
            TextView txtUser;
            TextView txtComment;

            CommentViewHolder(@NonNull View itemView) {
                super(itemView);
                txtUser = itemView.findViewById(R.id.txtCommentUser);
                txtComment = itemView.findViewById(R.id.txtCommentText);
            }
        }
    }
}
