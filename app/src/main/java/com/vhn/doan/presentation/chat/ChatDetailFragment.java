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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.vhn.doan.R;
import com.vhn.doan.data.ChatMessage;
import com.vhn.doan.data.repository.ChatRepositoryImpl;
import com.vhn.doan.presentation.home.HomeActivity;
import com.vhn.doan.utils.AnalyticsManager;

import java.util.List;

/**
 * Fragment hiển thị chi tiết một cuộc trò chuyện cụ thể với AI
 */
public class ChatDetailFragment extends Fragment implements ChatDetailContract.View {

    private static final String TAG = "ChatDetailFragment";
    private static final String ARG_CONVERSATION_ID = "conversation_id";
    private static final String ARG_CONVERSATION_TITLE = "conversation_title";

    // Views
    private ImageButton btnBack;
    private ImageButton btnRefresh;
    private TextView tvConversationTitle;
    private RecyclerView rvChatMessages;
    private TextInputEditText etMessageInput;
    private FloatingActionButton btnSendMessage;
    private View layoutLoading;
    private View layoutEmpty;
    private View layoutStatus;
    private ProgressBar progressStatus;
    private TextView tvStatus;

    // Components
    private ChatAdapter chatAdapter;
    private LinearLayoutManager layoutManager;
    private ChatDetailContract.Presenter presenter;
    private AnalyticsManager analyticsManager;

    // Arguments
    private String conversationId;
    private String conversationTitle;

    public static ChatDetailFragment newInstance(String conversationId, String conversationTitle) {
        ChatDetailFragment fragment = new ChatDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CONVERSATION_ID, conversationId);
        args.putString(ARG_CONVERSATION_TITLE, conversationTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            conversationId = getArguments().getString(ARG_CONVERSATION_ID);
            conversationTitle = getArguments().getString(ARG_CONVERSATION_TITLE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        setupListeners();

        // Khởi tạo Analytics Manager
        if (getContext() != null) {
            analyticsManager = AnalyticsManager.getInstance(getContext());
        }

        initPresenter();
        presenter.attachView(this);
        presenter.initialize(conversationId, conversationTitle);

        // Ẩn bottom navigation khi hiển thị chi tiết chat
        hideBottomNavigation();
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btn_back);
        btnRefresh = view.findViewById(R.id.btn_refresh);
        tvConversationTitle = view.findViewById(R.id.tv_conversation_title);
        rvChatMessages = view.findViewById(R.id.rv_chat_messages);
        etMessageInput = view.findViewById(R.id.et_message_input);
        btnSendMessage = view.findViewById(R.id.btn_send_message);
        layoutLoading = view.findViewById(R.id.layout_loading);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        layoutStatus = view.findViewById(R.id.layout_status);
        progressStatus = view.findViewById(R.id.progress_status);
        tvStatus = view.findViewById(R.id.tv_status);
    }

    private void initPresenter() {
        ChatRepositoryImpl chatRepository = new ChatRepositoryImpl();
        presenter = new ChatDetailPresenter(chatRepository, getContext());
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
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        btnRefresh.setOnClickListener(v -> {
            if (presenter != null) {
                presenter.refreshMessages();
            }
        });

        btnSendMessage.setOnClickListener(v -> sendMessage());

        // Gửi tin nhắn khi nhấn Enter
        etMessageInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void sendMessage() {
        String message = etMessageInput.getText() != null ? etMessageInput.getText().toString().trim() : "";

        if (!TextUtils.isEmpty(message)) {
            // 📊 Log Analytics Event: Gửi tin nhắn chat AI
            if (analyticsManager != null && conversationId != null) {
                analyticsManager.logAiChatMessage(conversationId, message.length());
            }

            if (presenter != null) {
                presenter.sendMessage(message);
            }
        } else {
            showError("Vui lòng nhập nội dung tin nhắn");
        }
    }

    // Implementation của ChatDetailContract.View

    @Override
    public void showMessages(List<ChatMessage> messages) {
        chatAdapter.setMessages(messages);
    }

    @Override
    public void addMessage(ChatMessage message) {
        chatAdapter.addMessage(message);
    }

    @Override
    public void showSendingMessage() {
        layoutStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("Đang gửi tin nhắn...");
        btnSendMessage.setEnabled(false);
    }

    @Override
    public void hideSendingMessage() {
        btnSendMessage.setEnabled(true);
        if (!chatAdapter.isAiTyping()) {
            layoutStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public void showAiTyping() {
        chatAdapter.showAiTyping();
        layoutStatus.setVisibility(View.VISIBLE);
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
    public void showLoadingMessages() {
        layoutLoading.setVisibility(View.VISIBLE);
        rvChatMessages.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
    }

    @Override
    public void hideLoadingMessages() {
        layoutLoading.setVisibility(View.GONE);
        rvChatMessages.setVisibility(View.VISIBLE);
    }

    @Override
    public void showEmptyConversation() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvChatMessages.setVisibility(View.GONE);
    }

    @Override
    public void hideEmptyConversation() {
        layoutEmpty.setVisibility(View.GONE);
        rvChatMessages.setVisibility(View.VISIBLE);
    }

    @Override
    public void showSendMessageError(String error) {
        showError("Lỗi gửi tin nhắn: " + error);
        layoutStatus.setVisibility(View.GONE);
        btnSendMessage.setEnabled(true);
    }

    @Override
    public void showLoadMessagesError(String error) {
        showError("Lỗi tải tin nhắn: " + error);
    }

    @Override
    public void updateConversationTitle(String title) {
        if (tvConversationTitle != null) {
            tvConversationTitle.setText(title);
        }
    }

    @Override
    public void showMessage(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
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
    public void disableInput() {
        etMessageInput.setEnabled(false);
        btnSendMessage.setEnabled(false);
        hideKeyboard();
    }

    @Override
    public void enableInput() {
        etMessageInput.setEnabled(true);
        btnSendMessage.setEnabled(true);
    }

    private void hideKeyboard() {
        if (getActivity() != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            View view = getActivity().getCurrentFocus();
            if (view == null) {
                view = new View(getActivity());
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroyView() {
        if (presenter != null) {
            presenter.detachView();
        }

        // Hiển thị lại bottom navigation khi thoát khỏi chat detail
        showBottomNavigation();

        super.onDestroyView();
    }

    // Phương thức ẩn bottom navigation
    private void hideBottomNavigation() {
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).hideBottomNavigation();
        }
    }

    // Phương thức hiển thị bottom navigation
    private void showBottomNavigation() {
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).showBottomNavigation();
        }
    }
}
