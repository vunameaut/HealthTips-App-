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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.vhn.doan.R;
import com.vhn.doan.data.repository.ChatRepositoryImpl;

/**
 * Fragment để tạo cuộc trò chuyện mới với AI
 */
public class NewChatFragment extends Fragment implements NewChatContract.View {

    private static final String TAG = "NewChatFragment";

    // Views
    private ImageButton btnBack;
    private TextInputEditText etMessageInput;
    private MaterialButton btnSendMessage;
    private MaterialCardView cardQuestion1;
    private MaterialCardView cardQuestion2;
    private MaterialCardView cardQuestion3;
    private View layoutStatus;
    private ProgressBar progressStatus;
    private TextView tvStatus;

    // Components
    private NewChatContract.Presenter presenter;

    // Suggested questions
    private static final String QUESTION_1 = "Làm thế nào để tăng cường sức đề kháng?";
    private static final String QUESTION_2 = "Chế độ ăn nào tốt cho tim mạch?";
    private static final String QUESTION_3 = "Cách cải thiện chất lượng giấc ngủ?";

    public static NewChatFragment newInstance() {
        return new NewChatFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupListeners();
        initPresenter();
        presenter.attachView(this);
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btn_back);
        etMessageInput = view.findViewById(R.id.et_message_input);
        btnSendMessage = view.findViewById(R.id.btn_send_message);
        cardQuestion1 = view.findViewById(R.id.card_question_1);
        cardQuestion2 = view.findViewById(R.id.card_question_2);
        cardQuestion3 = view.findViewById(R.id.card_question_3);
        layoutStatus = view.findViewById(R.id.layout_status);
        progressStatus = view.findViewById(R.id.progress_status);
        tvStatus = view.findViewById(R.id.tv_status);
    }

    private void initPresenter() {
        ChatRepositoryImpl chatRepository = new ChatRepositoryImpl();
        presenter = new NewChatPresenter(chatRepository);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        btnSendMessage.setOnClickListener(v -> sendMessage());

        cardQuestion1.setOnClickListener(v -> sendSuggestedQuestion(QUESTION_1));
        cardQuestion2.setOnClickListener(v -> sendSuggestedQuestion(QUESTION_2));
        cardQuestion3.setOnClickListener(v -> sendSuggestedQuestion(QUESTION_3));

        etMessageInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void sendMessage() {
        String message = etMessageInput.getText() != null ? etMessageInput.getText().toString().trim() : "";

        if (!TextUtils.isEmpty(message)) {
            if (presenter != null) {
                presenter.createConversationAndSendMessage(message);
            }
        } else {
            showError("Vui lòng nhập nội dung tin nhắn");
        }
    }

    private void sendSuggestedQuestion(String question) {
        etMessageInput.setText(question);
        if (presenter != null) {
            presenter.createConversationAndSendMessage(question);
        }
    }

    // Implementation của NewChatContract.View

    @Override
    public void showCreatingConversation() {
        layoutStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("Đang tạo cuộc trò chuyện...");
        btnSendMessage.setEnabled(false);
        setQuestionCardsEnabled(false);
    }

    @Override
    public void hideCreatingConversation() {
        // Không ẩn layoutStatus vì có thể chuyển sang trạng thái khác
        btnSendMessage.setEnabled(true);
        setQuestionCardsEnabled(true);
    }

    @Override
    public void showSendingMessage() {
        layoutStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("Đang gửi tin nhắn...");
        btnSendMessage.setEnabled(false);
        setQuestionCardsEnabled(false);
    }

    @Override
    public void hideSendingMessage() {
        // Không ẩn layoutStatus vì có thể chuyển sang trạng thái khác
        btnSendMessage.setEnabled(true);
        setQuestionCardsEnabled(true);
    }

    @Override
    public void showAiTyping() {
        layoutStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("AI đang trả lời...");
        btnSendMessage.setEnabled(false);
        setQuestionCardsEnabled(false);
    }

    @Override
    public void hideAiTyping() {
        layoutStatus.setVisibility(View.GONE);
        btnSendMessage.setEnabled(true);
        setQuestionCardsEnabled(true);
    }

    @Override
    public void clearMessageInput() {
        if (etMessageInput != null) {
            etMessageInput.setText("");
        }
    }

    @Override
    public void showCreateConversationError(String error) {
        showError("Lỗi tạo cuộc trò chuyện: " + error);
        layoutStatus.setVisibility(View.GONE);
        btnSendMessage.setEnabled(true);
        setQuestionCardsEnabled(true);
    }

    @Override
    public void showSendMessageError(String error) {
        showError("Lỗi gửi tin nhắn: " + error);
        layoutStatus.setVisibility(View.GONE);
        btnSendMessage.setEnabled(true);
        setQuestionCardsEnabled(true);
    }

    @Override
    public void navigateToChatDetail(String conversationId, String conversationTitle) {
        if (getActivity() != null) {
            ChatDetailFragment chatDetailFragment = ChatDetailFragment.newInstance(conversationId, conversationTitle);

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, chatDetailFragment)
                    .addToBackStack(null)
                    .commit();
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
        showCreatingConversation();
    }

    @Override
    public void hideLoading() {
        hideCreatingConversation();
    }

    /**
     * Enable/disable question cards
     */
    private void setQuestionCardsEnabled(boolean enabled) {
        cardQuestion1.setEnabled(enabled);
        cardQuestion2.setEnabled(enabled);
        cardQuestion3.setEnabled(enabled);

        float alpha = enabled ? 1.0f : 0.6f;
        cardQuestion1.setAlpha(alpha);
        cardQuestion2.setAlpha(alpha);
        cardQuestion3.setAlpha(alpha);
    }

    @Override
    public void onDestroyView() {
        if (presenter != null) {
            presenter.detachView();
        }
        super.onDestroyView();
    }
}
