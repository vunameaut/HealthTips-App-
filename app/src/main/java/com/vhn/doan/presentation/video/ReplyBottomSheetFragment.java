package com.vhn.doan.presentation.video;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.vhn.doan.R;
import com.vhn.doan.data.VideoComment;
import com.vhn.doan.data.repository.FirebaseVideoRepositoryImpl;
import com.vhn.doan.data.repository.VideoRepository;
import com.vhn.doan.utils.SharedPreferencesHelper;
import com.vhn.doan.utils.VercelApiHelper;

/**
 * Bottom Sheet Fragment để trả lời bình luận
 */
public class ReplyBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_VIDEO_ID = "video_id";
    private static final String ARG_PARENT_COMMENT_ID = "parent_comment_id";
    private static final String ARG_PARENT_COMMENT_TEXT = "parent_comment_text";
    private static final String ARG_PARENT_USER_NAME = "parent_user_name";

    private String videoId;
    private String parentCommentId;
    private String parentCommentText;
    private String parentUserName;
    private String currentUserId;

    // Views
    private TextView parentCommentInfo;
    private EditText replyInput;
    private ImageView sendButton;
    private ImageView closeButton;

    // Repository
    private VideoRepository videoRepository;

    // Callback interface
    public interface OnReplyAddedListener {
        void onReplyAdded(VideoComment reply, String parentCommentId);
    }

    private OnReplyAddedListener replyAddedListener;

    public static ReplyBottomSheetFragment newInstance(String videoId, VideoComment parentComment) {
        ReplyBottomSheetFragment fragment = new ReplyBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_ID, videoId);
        args.putString(ARG_PARENT_COMMENT_ID, parentComment.getId());
        args.putString(ARG_PARENT_COMMENT_TEXT, parentComment.getText());
        // Tạm thời dùng fallback, sẽ fetch real name khi hiển thị
        args.putString(ARG_PARENT_USER_NAME, "Người dùng " + parentComment.getUserId().substring(0, Math.min(6, parentComment.getUserId().length())));
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnReplyAddedListener(OnReplyAddedListener listener) {
        this.replyAddedListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        // Thiết lập để bottom sheet không bị keyboard che khuất
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);

                // Thiết lập chiều cao cho reply bottom sheet (nhỏ hơn comments)
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                bottomSheet.setLayoutParams(layoutParams);

                // Mở rộng ngay từ đầu
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);

                // Thiết lập peek height động dựa trên nội dung
                behavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);

                // Cho phép bottom sheet điều chỉnh khi keyboard hiện
                behavior.setFitToContents(true);
            }
        });

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            videoId = getArguments().getString(ARG_VIDEO_ID);
            parentCommentId = getArguments().getString(ARG_PARENT_COMMENT_ID);
            parentCommentText = getArguments().getString(ARG_PARENT_COMMENT_TEXT);
            parentUserName = getArguments().getString(ARG_PARENT_USER_NAME);
        }

        currentUserId = SharedPreferencesHelper.getUserId(getContext());
        videoRepository = new FirebaseVideoRepositoryImpl();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_reply, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
        setupParentCommentInfo();
    }

    private void initViews(View view) {
        parentCommentInfo = view.findViewById(R.id.tv_parent_comment_info);
        replyInput = view.findViewById(R.id.et_reply_input);
        sendButton = view.findViewById(R.id.iv_send_reply);
        closeButton = view.findViewById(R.id.iv_close_reply);
    }

    private void setupListeners() {
        // Close button
        closeButton.setOnClickListener(v -> dismiss());

        // Send button
        sendButton.setOnClickListener(v -> sendReply());

        // Enable/disable send button based on input
        replyInput.addTextChangedListener(new TextWatcher() {
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

        // Xử lý sự kiện nhấn "Send" trên bàn phím
        replyInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                if (!replyInput.getText().toString().trim().isEmpty()) {
                    sendReply();
                    return true;
                }
            }
            return false;
        });

        // Initial state
        sendButton.setEnabled(false);
        sendButton.setAlpha(0.5f);

        // Auto focus input và hiển thị keyboard
        replyInput.requestFocus();
        replyInput.post(() -> {
            InputMethodManager imm =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(replyInput, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void setupParentCommentInfo() {
        if (parentUserName != null && parentCommentText != null) {
            String displayText = parentCommentText.length() > 50
                ? parentCommentText.substring(0, 50) + "..."
                : parentCommentText;
            parentCommentInfo.setText("Trả lời " + parentUserName + ": " + displayText);
        }
    }

    private void sendReply() {
        String replyText = replyInput.getText().toString().trim();
        if (replyText.isEmpty()) {
            return;
        }

        if (currentUserId == null || currentUserId.isEmpty()) {
            showError("Vui lòng đăng nhập để trả lời bình luận");
            return;
        }

        // Disable input while sending
        sendButton.setEnabled(false);
        replyInput.setEnabled(false);

        videoRepository.addComment(videoId, currentUserId, replyText, parentCommentId,
            new VideoRepository.CommentCallback() {
                @Override
                public void onSuccess(VideoComment reply) {
                    if (getActivity() == null || !isAdded()) return;

                    // Notify listener about new reply
                    if (replyAddedListener != null) {
                        replyAddedListener.onReplyAdded(reply, parentCommentId);
                    }

                    // Gửi thông báo đến người được reply
                    VercelApiHelper.getInstance(getContext())
                        .sendCommentReplyNotification(
                            videoId,
                            parentCommentId,
                            reply.getId(),
                            currentUserId,
                            new VercelApiHelper.ApiCallback() {
                                @Override
                                public void onSuccess(org.json.JSONObject response) {
                                    Log.d("ReplyBottomSheet", "Notification sent successfully: " + response.toString());
                                }

                                @Override
                                public void onError(String error) {
                                    Log.e("ReplyBottomSheet", "Failed to send notification: " + error);
                                    // Không hiển thị lỗi cho user vì reply đã thành công
                                }
                            }
                        );

                    showSuccess("Đã gửi câu trả lời");
                    dismiss();
                }

                @Override
                public void onError(String errorMessage) {
                    if (getActivity() == null || !isAdded()) return;

                    // Re-enable input
                    replyInput.setEnabled(true);
                    sendButton.setEnabled(!replyInput.getText().toString().trim().isEmpty());
                    sendButton.setAlpha(!replyInput.getText().toString().trim().isEmpty() ? 1.0f : 0.5f);

                    showError("Không thể gửi câu trả lời: " + errorMessage);
                }
            });
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
        if (videoRepository != null) {
            videoRepository.cleanup();
        }
    }
}
