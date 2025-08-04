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
        if (content == null || content.trim().isEmpty()) {
            if (isViewAttached()) {
                view.showSendMessageError("Vui lòng nhập nội dung tin nhắn");
            }
            return;
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (isViewAttached()) {
                view.showSendMessageError("Bạn cần đăng nhập để gửi tin nhắn");
            }
            return;
        }

        String trimmedContent = content.trim();
        long timestamp = System.currentTimeMillis();
        String userId = currentUser.getUid();

        // Hiển thị tin nhắn của người dùng ngay lập tức
        ChatMessage userMessage = new ChatMessage(userId, trimmedContent, true, timestamp);
        String topic = chatRepository.extractTopic(trimmedContent);
        userMessage.setTopic(topic);

        if (isViewAttached()) {
            view.addMessage(userMessage);
            view.clearMessageInput();
            view.scrollToLatestMessage();
            view.showSendingMessage();
        }

        // Lưu tin nhắn người dùng vào Firebase
        chatRepository.saveChatMessage(userMessage, new RepositoryCallback<ChatMessage>() {
            @Override
            public void onSuccess(ChatMessage savedMessage) {
                Log.d(TAG, "User message saved successfully");
                if (isViewAttached()) {
                    view.hideSendingMessage();
                    view.showAiTyping();
                }

                // Gửi tin nhắn tới AI
                chatRepository.sendMessageToAI(trimmedContent, new RepositoryCallback<String>() {
                    @Override
                    public void onSuccess(String aiResponse) {
                        Log.d(TAG, "AI response received: " + aiResponse);

                        // Tạo tin nhắn phản hồi từ AI
                        ChatMessage aiMessage = new ChatMessage(userId, aiResponse, false, System.currentTimeMillis());
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
                            ChatMessage errorMessage = new ChatMessage(userId,
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
}
