package com.vhn.doan.presentation.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.vhn.doan.presentation.home.HomeActivity;

import java.util.List;

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
    private TextView tvQuestion1;
    private TextView tvQuestion2;
    private TextView tvQuestion3;
    private View layoutStatus;
    private ProgressBar progressStatus;
    private TextView tvStatus;

    // Components
    private NewChatContract.Presenter presenter;

    // Suggested questions
    private String question1 = "Làm thế nào để tăng cường sức đề kháng?";
    private String question2 = "Chế độ ăn nào tốt cho tim mạch?";
    private String question3 = "Cách cải thiện chất lượng giấc ngủ?";

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

        // Ẩn bottom navigation khi hiển thị tạo chat mới
        hideBottomNavigation();

        // Tải câu hỏi gợi ý dựa trên dữ liệu người dùng
        presenter.loadSuggestedQuestions();
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btn_back);
        etMessageInput = view.findViewById(R.id.et_message_input);
        btnSendMessage = view.findViewById(R.id.btn_send_message);
        cardQuestion1 = view.findViewById(R.id.card_question_1);
        cardQuestion2 = view.findViewById(R.id.card_question_2);
        cardQuestion3 = view.findViewById(R.id.card_question_3);
        tvQuestion1 = view.findViewById(R.id.tv_question_1);
        tvQuestion2 = view.findViewById(R.id.tv_question_2);
        tvQuestion3 = view.findViewById(R.id.tv_question_3);
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

        cardQuestion1.setOnClickListener(v -> sendSuggestedQuestion(question1));
        cardQuestion2.setOnClickListener(v -> sendSuggestedQuestion(question2));
        cardQuestion3.setOnClickListener(v -> sendSuggestedQuestion(question3));

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
        // Ẩn bàn phím và disable input khi đang tạo cuộc trò chuyện
        hideKeyboard();
        etMessageInput.setEnabled(false);
    }

    @Override
    public void hideCreatingConversation() {
        // Không ẩn layoutStatus vì có thể chuyển sang trạng thái khác
        btnSendMessage.setEnabled(true);
        setQuestionCardsEnabled(true);
        // Enable lại input khi hoàn thành
        etMessageInput.setEnabled(true);
    }

    @Override
    public void showSendingMessage() {
        layoutStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("Đang gửi tin nhắn...");
        btnSendMessage.setEnabled(false);
        setQuestionCardsEnabled(false);
        // Ẩn bàn phím và disable input khi đang gửi tin nhắn
        hideKeyboard();
        etMessageInput.setEnabled(false);
    }

    @Override
    public void hideSendingMessage() {
        // Không ẩn layoutStatus vì có thể chuyển sang trạng thái khác
        btnSendMessage.setEnabled(true);
        setQuestionCardsEnabled(true);
        // Enable lại input khi hoàn thành
        etMessageInput.setEnabled(true);
    }

    @Override
    public void showAiTyping() {
        layoutStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("AI đang trả lời...");
        btnSendMessage.setEnabled(false);
        setQuestionCardsEnabled(false);
        // Ẩn bàn phím và disable input khi AI đang trả lời - QUAN TRỌNG NHẤT
        hideKeyboard();
        etMessageInput.setEnabled(false);
    }

    @Override
    public void hideAiTyping() {
        layoutStatus.setVisibility(View.GONE);
        btnSendMessage.setEnabled(true);
        setQuestionCardsEnabled(true);
        // Enable lại input khi AI hoàn thành trả lời
        etMessageInput.setEnabled(true);
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
        // Enable lại input khi có lỗi
        etMessageInput.setEnabled(true);
    }

    @Override
    public void showSendMessageError(String error) {
        showError("Lỗi gửi tin nhắn: " + error);
        layoutStatus.setVisibility(View.GONE);
        btnSendMessage.setEnabled(true);
        setQuestionCardsEnabled(true);
        // Enable lại input khi có lỗi
        etMessageInput.setEnabled(true);
    }

    @Override
    public void navigateToChatDetail(String conversationId, String conversationTitle) {
        if (getActivity() != null) {
            ChatDetailFragment chatDetailFragment = ChatDetailFragment.newInstance(conversationId, conversationTitle);

            // Thay thế NewChatFragment bằng ChatDetailFragment và KHÔNG thêm vào backstack
            // Điều này đảm bảo khi ấn Back từ ChatDetail sẽ quay về màn hình trước NewChatFragment (thường là ChatListFragment)
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, chatDetailFragment)
                    // Không thêm addToBackStack(null) để tránh quay lại NewChatFragment
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

        // Hiển thị lại bottom navigation khi thoát
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

    @Override
    public void showSuggestedQuestions(List<String> questions) {
        if (questions != null && questions.size() >= 3) {
            question1 = questions.get(0);
            question2 = questions.get(1);
            question3 = questions.get(2);

            tvQuestion1.setText(question1);
            tvQuestion2.setText(question2);
            tvQuestion3.setText(question3);
        }
    }

    @Override
    public void updateSuggestedQuestions(List<String> suggestedQuestions) {
        if (suggestedQuestions != null && suggestedQuestions.size() >= 3) {
            // Thêm biểu tượng emoji phù hợp cho mỗi câu hỏi
            question1 = addEmojiToQuestion(suggestedQuestions.get(0), "💪");
            question2 = addEmojiToQuestion(suggestedQuestions.get(1), "🥗");
            question3 = addEmojiToQuestion(suggestedQuestions.get(2), "😴");

            // Cập nhật nội dung các TextView
            if (tvQuestion1 != null) tvQuestion1.setText(question1);
            if (tvQuestion2 != null) tvQuestion2.setText(question2);
            if (tvQuestion3 != null) tvQuestion3.setText(question3);
        }
    }

    /**
     * Thêm emoji vào đầu câu hỏi nếu chưa có
     */
    private String addEmojiToQuestion(String question, String defaultEmoji) {
        // Kiểm tra xem câu hỏi đã có emoji chưa
        boolean hasEmoji = question.length() > 0 &&
                           Character.isHighSurrogate(question.charAt(0)) ||
                           (question.codePointAt(0) >= 0x2600);

        if (hasEmoji) {
            return question;
        } else {
            return defaultEmoji + " " + question;
        }
    }

    @Override
    public void showLoadingSuggestedQuestions() {
        // Hiển thị trạng thái đang tải bằng cách làm mờ các card câu hỏi
        setQuestionCardsEnabled(false);
    }

    @Override
    public void hideLoadingSuggestedQuestions() {
        // Ẩn trạng thái đang tải bằng cách làm rõ các card câu hỏi
        setQuestionCardsEnabled(true);
    }

    /**
     * Triển khai phương thức từ NewChatContract.View
     * để lấy SharedPreferences lưu trữ lịch sử câu hỏi gợi ý
     */
    @Override
    public SharedPreferences getSharedPreferences() {
        if (getContext() != null) {
            return getContext().getSharedPreferences("chat_preferences", android.content.Context.MODE_PRIVATE);
        }
        return null;
    }

    /**
     * Ẩn bàn phím ảo
     */
    private void hideKeyboard() {
        if (getActivity() != null) {
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}
