package com.vhn.doan.presentation.chat;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.data.ChatMessage;
import com.vhn.doan.data.Conversation;
import com.vhn.doan.data.repository.ChatRepository;
import com.vhn.doan.data.repository.RepositoryCallback;

import java.util.List;

/**
 * Presenter cho ChatDetail feature - Quản lý chi tiết một cuộc trò chuyện
 */
public class ChatDetailPresenter implements ChatDetailContract.Presenter {

    private static final String TAG = "ChatDetailPresenter";

    private final ChatRepository chatRepository;
    private final FirebaseAuth firebaseAuth;
    private ChatDetailContract.View view;

    // State management
    private String conversationId;
    private String conversationTitle;
    private Conversation currentConversation;
    private boolean isLoading = false;
    private boolean isSending = false;

    public ChatDetailPresenter(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void attachView(ChatDetailContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    private boolean isViewAttached() {
        return view != null;
    }

    @Override
    public void initialize(String conversationId, String conversationTitle) {
        this.conversationId = conversationId;
        this.conversationTitle = conversationTitle;

        if (isViewAttached()) {
            view.updateConversationTitle(conversationTitle != null ? conversationTitle : "Cuộc trò chuyện");
        }

        // Tự động tải tin nhắn khi khởi tạo
        loadMessages();
    }

    @Override
    public void loadMessages() {
        if (isLoading || conversationId == null) {
            Log.d(TAG, "Skip loading messages: isLoading=" + isLoading + ", conversationId=" + conversationId);
            return;
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (isViewAttached()) {
                view.showLoadMessagesError("Bạn cần đăng nhập để xem tin nhắn");
            }
            return;
        }

        isLoading = true;

        if (isViewAttached()) {
            view.showLoadingMessages();
            view.hideEmptyConversation();
        }

        chatRepository.getChatMessages(conversationId, new RepositoryCallback<List<ChatMessage>>() {
            @Override
            public void onSuccess(List<ChatMessage> messages) {
                isLoading = false;

                if (isViewAttached()) {
                    view.hideLoadingMessages();

                    if (messages == null || messages.isEmpty()) {
                        view.showEmptyConversation();
                    } else {
                        view.hideEmptyConversation();
                        view.showMessages(messages);
                        view.scrollToLatestMessage();
                    }
                }

                Log.d(TAG, "Loaded " + (messages != null ? messages.size() : 0) + " messages for conversation: " + conversationId);
            }

            @Override
            public void onError(String error) {
                isLoading = false;
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

        if (conversationId == null) {
            if (isViewAttached()) {
                view.showSendMessageError("Không tìm thấy cuộc trò chuyện");
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
        String userId = currentUser.getUid();
        long timestamp = System.currentTimeMillis();

        // Hiển thị tin nhắn của người dùng ngay lập tức
        ChatMessage userMessage = new ChatMessage(conversationId, userId, trimmedContent, true, timestamp);
        String topic = chatRepository.extractTopic(trimmedContent);
        userMessage.setTopic(topic);

        if (isViewAttached()) {
            view.addMessage(userMessage);
            view.clearMessageInput();
            view.scrollToLatestMessage();
            view.showSendingMessage();
            view.hideEmptyConversation();
        }

        // Lưu tin nhắn người dùng vào Firebase
        chatRepository.saveChatMessage(userMessage, new RepositoryCallback<ChatMessage>() {
            @Override
            public void onSuccess(ChatMessage savedUserMessage) {
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
                        ChatMessage aiMessage = new ChatMessage(conversationId, userId, aiResponse, false, System.currentTimeMillis());
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

                                // Cập nhật thông tin cuộc trò chuyện
                                updateConversationInfo(aiMessage);
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
                            ChatMessage errorMessage = new ChatMessage(conversationId, userId,
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
    public void refreshMessages() {
        loadMessages();
    }

    /**
     * Cập nhật thông tin cuộc trò chuyện sau khi có tin nhắn mới
     */
    private void updateConversationInfo(ChatMessage lastMessage) {
        if (currentConversation != null && lastMessage != null) {
            currentConversation.setLastMessage(lastMessage.getContent());
            currentConversation.setLastMessageTime(lastMessage.getTimestamp());
            currentConversation.setFromUser(lastMessage.isFromUser());

            // Tăng số lượng tin nhắn
            currentConversation.setMessageCount(currentConversation.getMessageCount() + 2); // User + AI message

            chatRepository.updateConversation(currentConversation, new RepositoryCallback<Conversation>() {
                @Override
                public void onSuccess(Conversation updatedConversation) {
                    Log.d(TAG, "Conversation updated successfully");
                    currentConversation = updatedConversation;
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Failed to update conversation: " + error);
                    // Không hiển thị lỗi cho người dùng vì không ảnh hưởng đến UX
                }
            });
        }
    }

    /**
     * Getter methods cho testing và debug
     */
    public String getConversationId() {
        return conversationId;
    }

    public String getConversationTitle() {
        return conversationTitle;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isSending() {
        return isSending;
    }
}
