package com.vhn.doan.presentation.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.vhn.doan.R;
import com.vhn.doan.data.ChatMessage;
import com.vhn.doan.data.repository.ChatRepositoryImpl;
import com.vhn.doan.presentation.base.BaseFragment;

import java.util.List;

/**
 * Fragment hiển thị giao diện chat với AI về sức khỏe
 */
public class ChatFragment extends BaseFragment<ChatContract.Presenter> implements ChatContract.View {

    private static final String TAG = "ChatFragment";

    // Views
    private RecyclerView rvChatMessages;
    private TextInputEditText etMessageInput;
    private MaterialButton btnSendMessage;
    private ImageButton btnClearChat;
    private View layoutLoading;
    private View layoutEmpty;
    private View layoutStatus;
    private ProgressBar progressSending;
    private TextView tvStatus;

    // Components
    private ChatAdapter chatAdapter;
    private LinearLayoutManager layoutManager;

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat;
    }

    @Override
    protected void initPresenter() {
        ChatRepositoryImpl chatRepository = new ChatRepositoryImpl();
        presenter = new ChatPresenter(chatRepository);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        setupListeners();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.attachView(this);
    }

    private void initViews(View view) {
        rvChatMessages = view.findViewById(R.id.rv_chat_messages);
        etMessageInput = view.findViewById(R.id.et_message_input);
        btnSendMessage = view.findViewById(R.id.btn_send_message);
        btnClearChat = view.findViewById(R.id.btn_clear_chat);
        layoutLoading = view.findViewById(R.id.layout_loading);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        layoutStatus = view.findViewById(R.id.layout_status);
        progressSending = view.findViewById(R.id.progress_sending);
        tvStatus = view.findViewById(R.id.tv_status);
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter();
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true); // Bắt đầu từ cuối danh sách

        rvChatMessages.setLayoutManager(layoutManager);
        rvChatMessages.setAdapter(chatAdapter);
        rvChatMessages.setHasFixedSize(false);
    }

    private void setupListeners() {
        btnSendMessage.setOnClickListener(v -> sendMessage());

        btnClearChat.setOnClickListener(v -> showClearChatDialog());

        // Gửi tin nhắn khi nhấn Enter (nếu không phải Shift+Enter)
        etMessageInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void sendMessage() {
        String message = etMessageInput.getText() != null ? etMessageInput.getText().toString().trim() : "";

        if (!TextUtils.isEmpty(message)) {
            presenter.sendMessage(message);
        } else {
            showError("Vui lòng nhập nội dung tin nhắn");
        }
    }

    private void showClearChatDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa lịch sử chat")
                .setMessage("Bạn có chắc chắn muốn xóa toàn bộ lịch sử cuộc trò chuyện không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    presenter.clearChatHistory();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Implementation của ChatContract.View

    @Override
    public void showMessages(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvChatMessages.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvChatMessages.setVisibility(View.VISIBLE);
            chatAdapter.setMessages(messages);
        }
    }

    @Override
    public void addMessage(ChatMessage message) {
        layoutEmpty.setVisibility(View.GONE);
        rvChatMessages.setVisibility(View.VISIBLE);
        chatAdapter.addMessage(message);
    }

    @Override
    public void showSendingMessage() {
        layoutStatus.setVisibility(View.VISIBLE);
        progressSending.setVisibility(View.VISIBLE);
        tvStatus.setText("Đang gửi tin nhắn...");
        btnSendMessage.setEnabled(false);
    }

    @Override
    public void hideSendingMessage() {
        btnSendMessage.setEnabled(true);
        if (!chatAdapter.isAiTyping) {
            layoutStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public void showAiTyping() {
        chatAdapter.showAiTyping();
        layoutStatus.setVisibility(View.VISIBLE);
        progressSending.setVisibility(View.VISIBLE);
        tvStatus.setText("AI đang trả lời...");
    }

    @Override
    public void hideAiTyping() {
        chatAdapter.hideAiTyping();
        layoutStatus.setVisibility(View.GONE);
    }

    @Override
    public void clearMessageInput() {
        if (etMessageInput != null) {
            etMessageInput.setText("");
        }
    }

    @Override
    public void scrollToLatestMessage() {
        if (chatAdapter.getItemCount() > 0) {
            rvChatMessages.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void showSendMessageError(String error) {
        showError("Lỗi gửi tin nhắn: " + error);
    }

    @Override
    public void showLoadMessagesError(String error) {
        showError("Lỗi tải tin nhắn: " + error);
    }

    @Override
    public void showLoadingMessages() {
        layoutLoading.setVisibility(View.VISIBLE);
        rvChatMessages.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
    }

    @Override
    public void hideLoadingMessages() {
        layoutLoading.setVisibility(View.GONE);
    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void showMessage(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showLoading() {
        showLoadingMessages();
    }

    @Override
    public void hideLoading() {
        hideLoadingMessages();
    }

    @Override
    public void onDestroyView() {
        if (presenter != null) {
            presenter.detachView();
        }
        super.onDestroyView();
    }
}
