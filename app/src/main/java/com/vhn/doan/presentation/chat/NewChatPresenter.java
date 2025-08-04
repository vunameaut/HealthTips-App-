package com.vhn.doan.presentation.chat;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.data.ChatMessage;
import com.vhn.doan.data.Conversation;
import com.vhn.doan.data.repository.ChatRepository;
import com.vhn.doan.data.repository.RepositoryCallback;

/**
 * Presenter cho NewChat feature - Tạo cuộc trò chuyện mới
 */
public class NewChatPresenter implements NewChatContract.Presenter {

    private static final String TAG = "NewChatPresenter";

    private final ChatRepository chatRepository;
    private final FirebaseAuth firebaseAuth;
    private NewChatContract.View view;

    public NewChatPresenter(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void attachView(NewChatContract.View view) {
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
    public void createConversationAndSendMessage(String firstMessage) {
        if (firstMessage == null || firstMessage.trim().isEmpty()) {
            if (isViewAttached()) {
                view.showSendMessageError("Vui lòng nhập nội dung tin nhắn");
            }
            return;
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (isViewAttached()) {
                view.showCreateConversationError("Bạn cần đăng nhập để tạo cuộc trò chuyện");
            }
            return;
        }

        String trimmedMessage = firstMessage.trim();
        String userId = currentUser.getUid();

        if (isViewAttached()) {
            view.showCreatingConversation();
            view.clearMessageInput();
        }

        // Bước 1: Tạo cuộc trò chuyện mới
        chatRepository.createConversation(userId, trimmedMessage, new RepositoryCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                Log.d(TAG, "Conversation created successfully: " + conversation.getId());

                if (isViewAttached()) {
                    view.hideCreatingConversation();
                    view.showSendingMessage();
                }

                // Bước 2: Lưu tin nhắn đầu tiên của người dùng
                long timestamp = System.currentTimeMillis();
                ChatMessage userMessage = new ChatMessage(conversation.getId(), userId, trimmedMessage, true, timestamp);
                String topic = chatRepository.extractTopic(trimmedMessage);
                userMessage.setTopic(topic);

                chatRepository.saveChatMessage(userMessage, new RepositoryCallback<ChatMessage>() {
                    @Override
                    public void onSuccess(ChatMessage savedUserMessage) {
                        Log.d(TAG, "User message saved successfully");

                        if (isViewAttached()) {
                            view.hideSendingMessage();
                            view.showAiTyping();
                        }

                        // Bước 3: Gửi tin nhắn tới AI
                        chatRepository.sendMessageToAI(trimmedMessage, new RepositoryCallback<String>() {
                            @Override
                            public void onSuccess(String aiResponse) {
                                Log.d(TAG, "AI response received: " + aiResponse);

                                // Bước 4: Lưu phản hồi AI
                                ChatMessage aiMessage = new ChatMessage(conversation.getId(), userId, aiResponse, false, System.currentTimeMillis());
                                aiMessage.setTopic(topic);

                                chatRepository.saveChatMessage(aiMessage, new RepositoryCallback<ChatMessage>() {
                                    @Override
                                    public void onSuccess(ChatMessage savedAiMessage) {
                                        Log.d(TAG, "AI message saved successfully");

                                        // Bước 5: Cập nhật thông tin cuộc trò chuyện
                                        conversation.setLastMessage(aiResponse);
                                        conversation.setLastMessageTime(savedAiMessage.getTimestamp());
                                        conversation.setFromUser(false);
                                        conversation.setMessageCount(2); // User message + AI message

                                        chatRepository.updateConversation(conversation, new RepositoryCallback<Conversation>() {
                                            @Override
                                            public void onSuccess(Conversation updatedConversation) {
                                                Log.d(TAG, "Conversation updated successfully");

                                                if (isViewAttached()) {
                                                    view.hideAiTyping();
                                                    view.showMessage("Cuộc trò chuyện đã được tạo thành công!");

                                                    // Chuyển đến màn hình chat detail
                                                    view.navigateToChatDetail(updatedConversation.getId(), updatedConversation.getTitle());
                                                }
                                            }

                                            @Override
                                            public void onError(String error) {
                                                Log.e(TAG, "Failed to update conversation: " + error);

                                                if (isViewAttached()) {
                                                    view.hideAiTyping();
                                                    // Vẫn chuyển đến chat detail dù cập nhật conversation lỗi
                                                    view.navigateToChatDetail(conversation.getId(), conversation.getTitle());
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(String error) {
                                        Log.e(TAG, "Failed to save AI message: " + error);

                                        if (isViewAttached()) {
                                            view.hideAiTyping();
                                            view.showSendMessageError("Không thể lưu phản hồi AI: " + error);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "Failed to get AI response: " + error);

                                if (isViewAttached()) {
                                    view.hideAiTyping();
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
            public void onError(String error) {
                Log.e(TAG, "Failed to create conversation: " + error);

                if (isViewAttached()) {
                    view.hideCreatingConversation();
                    view.showCreateConversationError("Không thể tạo cuộc trò chuyện: " + error);
                }
            }
        });
    }
}
