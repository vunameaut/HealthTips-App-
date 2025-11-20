package com.vhn.doan.presentation.chat;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.data.ChatMessage;
import com.vhn.doan.data.repository.ChatRepository;
import com.vhn.doan.data.repository.RepositoryCallback;

import java.util.List;

/**
 * Presenter cho Chat feature theo kiến trúc MVP
 */
public class ChatPresenter implements ChatContract.Presenter {

    private static final String TAG = "ChatPresenter";
    private static final int MAX_MESSAGE_LENGTH = 500; // Giới hạn độ dài tin nhắn tối đa

    private final ChatRepository chatRepository;
    private final FirebaseAuth firebaseAuth;
    private ChatContract.View view;

    public ChatPresenter(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void attachView(ChatContract.View view) {
        this.view = view;
        // Tự động tải tin nhắn khi attach view
        loadMessages();
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    private boolean isViewAttached() {
        return view != null;
    }

    @Override
    public void loadMessages() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (isViewAttached()) {
                view.showLoadMessagesError("Bạn cần đăng nhập để sử dụng tính năng chat");
            }
            return;
        }

        if (isViewAttached()) {
            view.showLoadingMessages();
        }

        chatRepository.getChatMessages(currentUser.getUid(), new RepositoryCallback<List<ChatMessage>>() {
            @Override
            public void onSuccess(List<ChatMessage> messages) {
                if (isViewAttached()) {
                    view.hideLoadingMessages();
                    view.showMessages(messages);

                    if (!messages.isEmpty()) {
                        view.scrollToLatestMessage();
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load messages: " + error);
                if (isViewAttached()) {
                    view.hideLoadingMessages();
                    view.showLoadMessagesError(error);
                }
            }
        });
    }

    @Override
    public void sendMessage(String content) {
        // 1. Kiểm tra rỗng
        if (content == null || content.trim().isEmpty()) {
            if (isViewAttached()) {
                view.showSendMessageError("Vui lòng nhập nội dung tin nhắn");
            }
            return;
        }

        // 2. Kiểm tra người dùng đã đăng nhập
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (isViewAttached()) {
                view.showSendMessageError("Bạn cần đăng nhập để gửi tin nhắn");
            }
            return;
        }

        // 3. Trim và làm sạch input
        String trimmedContent = content.trim();
        String sanitizedContent = sanitizeInput(trimmedContent);

        // 4. Kiểm tra sau khi làm sạch có còn nội dung không
        if (sanitizedContent.isEmpty()) {
            if (isViewAttached()) {
                view.showSendMessageError("Nội dung tin nhắn không hợp lệ");
            }
            return;
        }

        // 5. Giới hạn độ dài tin nhắn
        boolean isTruncated = false;
        if (sanitizedContent.length() > MAX_MESSAGE_LENGTH) {
            sanitizedContent = sanitizedContent.substring(0, MAX_MESSAGE_LENGTH);
            isTruncated = true;
        }

        long timestamp = System.currentTimeMillis();
        String userId = currentUser.getUid();

        // Tạo conversationId tạm thời hoặc sử dụng conversationId hiện tại
        String conversationId = view.getConversationId();
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = "temp_" + userId + "_" + timestamp; // Tạo conversationId tạm thời
            if (isViewAttached()) {
                view.setConversationId(conversationId);
            }
        }

        // Hiển thị tin nhắn của người dùng ngay lập tức
        ChatMessage userMessage = new ChatMessage(conversationId, userId, sanitizedContent, true, timestamp);
        String topic = chatRepository.extractTopic(sanitizedContent);
        userMessage.setTopic(topic);

        if (isViewAttached()) {
            view.addMessage(userMessage);
            view.clearMessageInput();
            view.scrollToLatestMessage();
            view.showSendingMessage();

            // Hiển thị cảnh báo nếu tin nhắn bị cắt
            if (isTruncated) {
                view.showMessage("Tin nhắn quá dài, đã được rút gọn xuống " + MAX_MESSAGE_LENGTH + " ký tự");
            }
        }

        // Lưu tin nhắn người dùng vào Firebase
        final String finalConversationId = conversationId;
        final String finalSanitizedContent = sanitizedContent;
        chatRepository.saveChatMessage(userMessage, new RepositoryCallback<ChatMessage>() {
            @Override
            public void onSuccess(ChatMessage savedMessage) {
                Log.d(TAG, "User message saved successfully");
                if (isViewAttached()) {
                    view.hideSendingMessage();
                    view.showAiTyping();
                }

                // Gửi tin nhắn tới AI với lịch sử cuộc trò chuyện (tối đa 10 tin nhắn gần nhất)
                chatRepository.sendMessageToAI(finalSanitizedContent, finalConversationId, 10, new RepositoryCallback<String>() {
                    @Override
                    public void onSuccess(String aiResponse) {
                        Log.d(TAG, "AI response received: " + aiResponse);

                        // Tạo tin nhắn phản hồi từ AI
                        ChatMessage aiMessage = new ChatMessage(finalConversationId, userId, aiResponse, false, System.currentTimeMillis());
                        aiMessage.setTopic(topic);

                        if (isViewAttached()) {
                            view.hideAiTyping();
                            view.addMessage(aiMessage);
                            view.scrollToLatestMessage();
                        }

                        // Lưu phản hồi AI vào Firebase
                        chatRepository.saveChatMessage(aiMessage, new RepositoryCallback<ChatMessage>() {
                            @Override
                            public void onSuccess(ChatMessage savedAiMessage) {
                                Log.d(TAG, "AI message saved successfully");
                            }

                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "Failed to save AI message: " + error);
                                // Không hiển thị lỗi cho người dùng vì tin nhắn đã được hiển thị
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Failed to get AI response: " + error);

                        if (isViewAttached()) {
                            view.hideAiTyping();

                            // Hiển thị tin nhắn lỗi từ AI
                            ChatMessage errorMessage = new ChatMessage(finalConversationId, userId,
                                "Xin lỗi, tôi không thể trả lời câu hỏi của bạn lúc này. Vui lòng thử lại sau.",
                                false, System.currentTimeMillis());
                            view.addMessage(errorMessage);
                            view.scrollToLatestMessage();
                            view.showSendMessageError("Không thể nhận phản hồi từ AI: " + error);
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to save user message: " + error);
                if (isViewAttached()) {
                    view.hideSendingMessage();
                    view.showSendMessageError("Không thể lưu tin nhắn: " + error);
                }
            }
        });
    }

    @Override
    public void clearChatHistory() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (isViewAttached()) {
                view.showLoadMessagesError("Bạn cần đăng nhập để xóa lịch sử chat");
            }
            return;
        }

        chatRepository.clearChatHistory(currentUser.getUid(), new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                Log.d(TAG, "Chat history cleared successfully");
                if (isViewAttached()) {
                    view.showMessage("Đã xóa lịch sử chat thành công");
                    // Tải lại danh sách tin nhắn (sẽ rỗng)
                    loadMessages();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to clear chat history: " + error);
                if (isViewAttached()) {
                    view.showError("Không thể xóa lịch sử chat: " + error);
                }
            }
        });
    }

    @Override
    public void refreshMessages() {
        loadMessages();
    }

    /**
     * Làm sạch input để tránh các ký tự đặc biệt gây lỗi hoặc tấn công injection
     * @param input Chuỗi cần làm sạch
     * @return Chuỗi đã được làm sạch
     */
    private String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }

        // Xóa các ký tự nguy hiểm có thể gây lỗi hoặc injection
        return input
                // Xóa các thẻ HTML nguy hiểm
                .replaceAll("<script[^>]*>.*?</script>", "")
                .replaceAll("<[^>]+>", "")
                // Xóa các ký tự đặc biệt nguy hiểm
                .replaceAll("[<>\"'&]", "")
                // Chuẩn hóa khoảng trắng (loại bỏ nhiều space liên tiếp)
                .replaceAll("\\s+", " ")
                // Loại bỏ các ký tự điều khiển không mong muốn
                .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                .trim();
    }
}
