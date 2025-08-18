package com.vhn.doan.presentation.video;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.vhn.doan.R;
import com.vhn.doan.data.VideoComment;
import com.vhn.doan.data.repository.FirebaseVideoRepositoryImpl;
import com.vhn.doan.data.repository.VideoRepository;
import com.vhn.doan.presentation.video.adapter.CommentAdapter;
import com.vhn.doan.utils.SharedPreferencesHelper;

import java.util.List;

/**
 * Bottom Sheet Fragment hiển thị danh sách bình luận của video
 * Được cấu hình để mở rộng hoàn toàn ngay từ đầu
 */
public class CommentBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_VIDEO_ID = "video_id";

    private String videoId;
    private String currentUserId;

    // Views
    private RecyclerView commentsRecyclerView;
    private LinearLayout emptyCommentsLayout;
    private EditText commentInput;
    private ImageView sendButton;
    private ImageView closeButton;

    // Adapter và Repository
    private CommentAdapter commentAdapter;
    private VideoRepository videoRepository;

    public static CommentBottomSheetFragment newInstance(String videoId) {
        CommentBottomSheetFragment fragment = new CommentBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_ID, videoId);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);

                // Thiết lập chiều cao mở rộng là 85% màn hình
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.85);
                bottomSheet.setLayoutParams(layoutParams);

                // Mở rộng bottom sheet ngay từ đầu
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                // Cho phép kéo để đóng như TikTok
                behavior.setHideable(true);
                behavior.setSkipCollapsed(false);

                // Thiết lập peek height thấp để có thể đóng được
                behavior.setPeekHeight(0);

                // Thiết lập callback để xử lý trạng thái
                behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        // Cho phép đóng khi kéo xuống
                        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                            dismiss();
                        }
                        // Nếu đang collapsed và người dùng kéo tiếp, cho phép hide
                        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                            // Tự động chuyển về hidden sau một khoảng thời gian ngắn nếu người dùng không tương tác
                            bottomSheet.postDelayed(() -> {
                                if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                                    behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                                }
                            }, 300);
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                        // Thêm hiệu ứng fade out khi kéo xuống
                        if (slideOffset < 0.5f) {
                            bottomSheet.setAlpha(slideOffset * 2f);
                        } else {
                            bottomSheet.setAlpha(1f);
                        }
                    }
                });
            }
        });

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            videoId = getArguments().getString(ARG_VIDEO_ID);
        }

        currentUserId = SharedPreferencesHelper.getUserId(getContext());
        videoRepository = new FirebaseVideoRepositoryImpl();
        commentAdapter = new CommentAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_comments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadComments();
    }

    private void initViews(View view) {
        commentsRecyclerView = view.findViewById(R.id.rv_comments);
        emptyCommentsLayout = view.findViewById(R.id.layout_empty_comments);
        commentInput = view.findViewById(R.id.et_comment_input);
        sendButton = view.findViewById(R.id.iv_send_comment);
        closeButton = view.findViewById(R.id.iv_close_comments);
    }

    private void setupRecyclerView() {
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentsRecyclerView.setAdapter(commentAdapter);

        commentAdapter.setOnCommentInteractionListener(new CommentAdapter.OnCommentInteractionListener() {
            @Override
            public void onLikeComment(VideoComment comment, int position) {
                handleLikeComment(comment, position);
            }

            @Override
            public void onReplyComment(VideoComment comment, int position) {
                handleReplyComment(comment);
            }

            @Override
            public void onShowReplies(VideoComment comment, int position) {
                handleShowReplies(comment);
            }

            @Override
            public void onLikeReply(VideoComment reply, String parentCommentId) {
                handleLikeReply(reply, parentCommentId);
            }

            @Override
            public void onShowMoreReplies(String parentCommentId) {
                handleShowMoreReplies(parentCommentId);
            }
        });
    }

    private void setupListeners() {
        // Close button
        closeButton.setOnClickListener(v -> dismiss());

        // Send button
        sendButton.setOnClickListener(v -> sendComment());

        // Enable/disable send button based on input
        commentInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setEnabled(!s.toString().trim().isEmpty());
                sendButton.setAlpha(s.toString().trim().isEmpty() ? 0.5f : 1.0f);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Initial state
        sendButton.setEnabled(false);
        sendButton.setAlpha(0.5f);
    }

    private void loadComments() {
        if (videoId == null || videoId.isEmpty()) {
            showError("Video ID không hợp lệ");
            return;
        }

        videoRepository.getVideoComments(videoId, new VideoRepository.CommentsCallback() {
            @Override
            public void onSuccess(List<VideoComment> comments) {
                if (getActivity() == null || !isAdded()) return;

                if (comments.isEmpty()) {
                    showEmptyState();
                } else {
                    showComments(comments);
                    // Load replies cho từng comment
                    loadRepliesForComments(comments);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() == null || !isAdded()) return;
                showError("Không thể tải bình luận: " + errorMessage);
            }
        });
    }

    /**
     * Load replies cho tất cả comments
     */
    private void loadRepliesForComments(List<VideoComment> comments) {
        for (VideoComment comment : comments) {
            if (comment.getReplyCount() > 0) {
                loadRepliesForComment(comment);
            }
        }
    }

    /**
     * Load replies cho một comment cụ thể
     */
    private void loadRepliesForComment(VideoComment comment) {
        videoRepository.getCommentReplies(videoId, comment.getId(), new VideoRepository.CommentsCallback() {
            @Override
            public void onSuccess(List<VideoComment> replies) {
                if (getActivity() == null || !isAdded()) return;

                if (!replies.isEmpty()) {
                    // Cập nhật replies trong adapter
                    commentAdapter.updateReplies(comment.getId(), replies);

                    // Đảm bảo comment được mở rộng để hiển thị replies
                    commentAdapter.forceExpandReplies(comment.getId());

                    android.util.Log.d("CommentBottomSheet", "Loaded " + replies.size() +
                        " replies for comment " + comment.getId());
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Không hiển thị lỗi cho việc load replies để tránh spam
                android.util.Log.w("CommentBottomSheet", "Không thể load replies cho comment " + comment.getId() + ": " + errorMessage);
            }
        });
    }

    private void sendComment() {
        String commentText = commentInput.getText().toString().trim();
        if (commentText.isEmpty()) {
            return;
        }

        if (currentUserId == null || currentUserId.isEmpty()) {
            showError("Vui lòng đăng nhập để bình luận");
            return;
        }

        // Disable input while sending
        sendButton.setEnabled(false);
        commentInput.setEnabled(false);

        videoRepository.addComment(videoId, currentUserId, commentText, null,
            new VideoRepository.CommentCallback() {
                @Override
                public void onSuccess(VideoComment comment) {
                    if (getActivity() == null || !isAdded()) return;

                    // Clear input and re-enable
                    commentInput.setText("");
                    commentInput.setEnabled(true);
                    sendButton.setEnabled(false);
                    sendButton.setAlpha(0.5f);

                    // Add comment to list
                    commentAdapter.addComment(comment);

                    // Hide empty state if visible
                    if (emptyCommentsLayout.getVisibility() == View.VISIBLE) {
                        emptyCommentsLayout.setVisibility(View.GONE);
                        commentsRecyclerView.setVisibility(View.VISIBLE);
                    }

                    // Scroll to top to show new comment
                    commentsRecyclerView.scrollToPosition(0);

                    showSuccess("Đã thêm bình luận");
                }

                @Override
                public void onError(String errorMessage) {
                    if (getActivity() == null || !isAdded()) return;

                    // Re-enable input
                    commentInput.setEnabled(true);
                    sendButton.setEnabled(!commentInput.getText().toString().trim().isEmpty());
                    sendButton.setAlpha(!commentInput.getText().toString().trim().isEmpty() ? 1.0f : 0.5f);

                    showError("Không thể gửi bình luận: " + errorMessage);
                }
            });
    }

    private void handleLikeComment(VideoComment comment, int position) {
        if (currentUserId == null || currentUserId.isEmpty()) {
            showError("Vui lòng đăng nhập để thích bình luận");
            return;
        }

        // Check current like status first
        videoRepository.isCommentLiked(videoId, comment.getId(), currentUserId,
            new VideoRepository.BooleanCallback() {
                @Override
                public void onSuccess(boolean isLiked) {
                    if (getActivity() == null || !isAdded()) return;

                    if (isLiked) {
                        // Unlike comment
                        videoRepository.unlikeComment(videoId, comment.getId(), currentUserId,
                            new VideoRepository.BooleanCallback() {
                                @Override
                                public void onSuccess(boolean result) {
                                    if (getActivity() == null || !isAdded()) return;

                                    comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
                                    commentAdapter.updateComment(comment, position);
                                    showSuccess("Đã bỏ thích bình luận");
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    if (getActivity() == null || !isAdded()) return;
                                    showError("Không thể bỏ thích: " + errorMessage);
                                }
                            });
                    } else {
                        // Like comment
                        videoRepository.likeComment(videoId, comment.getId(), currentUserId,
                            new VideoRepository.BooleanCallback() {
                                @Override
                                public void onSuccess(boolean result) {
                                    if (getActivity() == null || !isAdded()) return;

                                    comment.setLikeCount(comment.getLikeCount() + 1);
                                    commentAdapter.updateComment(comment, position);
                                    showSuccess("Đã thích bình luận");
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    if (getActivity() == null || !isAdded()) return;
                                    showError("Không thể thích: " + errorMessage);
                                }
                            });
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    if (getActivity() == null || !isAdded()) return;
                    showError("Không thể kiểm tra trạng thái thích: " + errorMessage);
                }
            });
    }

    private void handleReplyComment(VideoComment comment) {
        ReplyBottomSheetFragment replyFragment = ReplyBottomSheetFragment.newInstance(videoId, comment);
        replyFragment.setOnReplyAddedListener(new ReplyBottomSheetFragment.OnReplyAddedListener() {
            @Override
            public void onReplyAdded(VideoComment reply, String parentCommentId) {
                // Thêm reply vào adapter
                commentAdapter.addReply(reply, parentCommentId);

                // Tự động mở rộng replies để hiển thị reply mới
                commentAdapter.toggleRepliesVisibility(parentCommentId);

                showSuccess("Đã thêm câu trả lời");
            }
        });

        replyFragment.show(getChildFragmentManager(), "ReplyBottomSheet");
    }

    private void handleShowReplies(VideoComment comment) {
        // Tải replies từ Firebase nếu chưa có
        videoRepository.getCommentReplies(videoId, comment.getId(), new VideoRepository.CommentsCallback() {
            @Override
            public void onSuccess(List<VideoComment> replies) {
                if (getActivity() == null || !isAdded()) return;

                // Cập nhật replies trong adapter
                commentAdapter.updateReplies(comment.getId(), replies);
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() == null || !isAdded()) return;
                showError("Không thể tải câu trả lời: " + errorMessage);
            }
        });
    }

    private void handleLikeReply(VideoComment reply, String parentCommentId) {
        if (currentUserId == null || currentUserId.isEmpty()) {
            showError("Vui lòng đăng nhập để thích câu trả lời");
            return;
        }

        // Kiểm tra trạng thái like hiện tại của reply
        videoRepository.isCommentLiked(videoId, reply.getId(), currentUserId,
            new VideoRepository.BooleanCallback() {
                @Override
                public void onSuccess(boolean isLiked) {
                    if (getActivity() == null || !isAdded()) return;

                    if (isLiked) {
                        // Unlike reply
                        videoRepository.unlikeComment(videoId, reply.getId(), currentUserId,
                            new VideoRepository.BooleanCallback() {
                                @Override
                                public void onSuccess(boolean result) {
                                    if (getActivity() == null || !isAdded()) return;

                                    reply.setLikeCount(Math.max(0, reply.getLikeCount() - 1));
                                    // Cập nhật lại replies list
                                    refreshReplies(parentCommentId);
                                    showSuccess("Đã bỏ thích câu trả lời");
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    if (getActivity() == null || !isAdded()) return;
                                    showError("Không thể bỏ thích: " + errorMessage);
                                }
                            });
                    } else {
                        // Like reply
                        videoRepository.likeComment(videoId, reply.getId(), currentUserId,
                            new VideoRepository.BooleanCallback() {
                                @Override
                                public void onSuccess(boolean result) {
                                    if (getActivity() == null || !isAdded()) return;

                                    reply.setLikeCount(reply.getLikeCount() + 1);
                                    // Cập nhật lại replies list
                                    refreshReplies(parentCommentId);
                                    showSuccess("Đã thích câu trả lời");
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    if (getActivity() == null || !isAdded()) return;
                                    showError("Không thể thích: " + errorMessage);
                                }
                            });
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    if (getActivity() == null || !isAdded()) return;
                    showError("Không thể kiểm tra trạng thái thích: " + errorMessage);
                }
            });
    }

    private void refreshReplies(String parentCommentId) {
        // Tải lại replies để cập nhật số liệu chính xác
        videoRepository.getCommentReplies(videoId, parentCommentId, new VideoRepository.CommentsCallback() {
            @Override
            public void onSuccess(List<VideoComment> replies) {
                if (getActivity() == null || !isAdded()) return;
                commentAdapter.updateReplies(parentCommentId, replies);
            }

            @Override
            public void onError(String errorMessage) {
                // Bỏ qua lỗi refresh
            }
        });
    }

    /**
     * Xử lý khi người dùng nhấn "Xem thêm replies"
     */
    private void handleShowMoreReplies(String parentCommentId) {
        // Toggle trạng thái hiển thị tất cả replies
        commentAdapter.toggleShowAllReplies(parentCommentId);
    }

    private void showComments(List<VideoComment> comments) {
        emptyCommentsLayout.setVisibility(View.GONE);
        commentsRecyclerView.setVisibility(View.VISIBLE);
        commentAdapter.updateComments(comments);
    }

    private void showEmptyState() {
        commentsRecyclerView.setVisibility(View.GONE);
        emptyCommentsLayout.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showSuccess(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cleanup repository listeners if any
        if (videoRepository != null) {
            videoRepository.cleanup();
        }
    }
}
