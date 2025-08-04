package com.vhn.doan.presentation.chat;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.data.Conversation;
import com.vhn.doan.data.repository.ChatRepository;
import com.vhn.doan.data.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Presenter cho ChatList feature - Quản lý danh sách cuộc trò chuyện
 */
public class ChatListPresenter implements ChatListContract.Presenter {

    private static final String TAG = "ChatListPresenter";
    private static final int DEFAULT_CONVERSATIONS_LIMIT = 8;
    private static final int LOAD_MORE_CONVERSATIONS_LIMIT = 3;

    private final ChatRepository chatRepository;
    private final FirebaseAuth firebaseAuth;
    private ChatListContract.View view;

    // State management
    private List<Conversation> conversationsList;
    private Long lastConversationTime;
    private boolean isLoading = false;
    private boolean isLoadingMore = false;
    private boolean hasMoreConversations = true;

    public ChatListPresenter(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.conversationsList = new ArrayList<>();
    }

    @Override
    public void attachView(ChatListContract.View view) {
        this.view = view;
        // Tự động tải danh sách cuộc trò chuyện khi attach view
        loadConversations();
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    private boolean isViewAttached() {
        return view != null;
    }

    @Override
    public void loadConversations() {
        if (isLoading) {
            Log.d(TAG, "Already loading conversations, skip");
            return;
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (isViewAttached()) {
                view.showLoadConversationsError("Bạn cần đăng nhập để xem danh sách cuộc trò chuyện");
            }
            return;
        }

        isLoading = true;
        lastConversationTime = null; // Reset phân trang
        hasMoreConversations = true;

        if (isViewAttached()) {
            view.showLoadingConversations();
            view.hideEmptyState();
        }

        chatRepository.getConversations(currentUser.getUid(), DEFAULT_CONVERSATIONS_LIMIT, null,
            new RepositoryCallback<List<Conversation>>() {
                @Override
                public void onSuccess(List<Conversation> conversations) {
                    isLoading = false;

                    if (isViewAttached()) {
                        view.hideLoadingConversations();

                        if (conversations == null || conversations.isEmpty()) {
                            view.showEmptyState();
                            view.showLoadMoreButton(false);
                        } else {
                            view.hideEmptyState();
                            view.showConversations(conversations);

                            // Cập nhật state cho phân trang
                            conversationsList.clear();
                            conversationsList.addAll(conversations);

                            if (!conversations.isEmpty()) {
                                lastConversationTime = conversations.get(conversations.size() - 1).getLastMessageTime();
                            }

                            // Kiểm tra xem có thêm cuộc trò chuyện để load không
                            checkHasMoreConversations();
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    isLoading = false;
                    Log.e(TAG, "Failed to load conversations: " + error);

                    if (isViewAttached()) {
                        view.hideLoadingConversations();
                        view.showLoadConversationsError(error);
                    }
                }
            });
    }

    @Override
    public void loadMoreConversations() {
        if (isLoadingMore || !hasMoreConversations) {
            Log.d(TAG, "Skip load more: isLoadingMore=" + isLoadingMore + ", hasMore=" + hasMoreConversations);
            return;
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (isViewAttached()) {
                view.showLoadMoreError("Bạn cần đăng nhập để tải thêm cuộc trò chuyện");
            }
            return;
        }

        isLoadingMore = true;

        if (isViewAttached()) {
            view.showLoadingMore();
        }

        chatRepository.getConversations(currentUser.getUid(), LOAD_MORE_CONVERSATIONS_LIMIT, lastConversationTime,
            new RepositoryCallback<List<Conversation>>() {
                @Override
                public void onSuccess(List<Conversation> conversations) {
                    isLoadingMore = false;

                    if (isViewAttached()) {
                        view.hideLoadingMore();

                        if (conversations != null && !conversations.isEmpty()) {
                            view.addMoreConversations(conversations);

                            // Cập nhật state
                            conversationsList.addAll(conversations);
                            lastConversationTime = conversations.get(conversations.size() - 1).getLastMessageTime();

                            // Kiểm tra xem còn thêm cuộc trò chuyện không
                            checkHasMoreConversations();
                        } else {
                            // Không còn cuộc trò chuyện nào để tải
                            hasMoreConversations = false;
                            view.showLoadMoreButton(false);
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    isLoadingMore = false;
                    Log.e(TAG, "Failed to load more conversations: " + error);

                    if (isViewAttached()) {
                        view.hideLoadingMore();
                        view.showLoadMoreError(error);
                    }
                }
            });
    }

    @Override
    public void refreshConversations() {
        // Reset tất cả state và tải lại từ đầu
        conversationsList.clear();
        lastConversationTime = null;
        hasMoreConversations = true;
        isLoading = false;
        isLoadingMore = false;

        loadConversations();
    }

    @Override
    public void onConversationClicked(Conversation conversation) {
        if (conversation == null || conversation.getId() == null) {
            if (isViewAttached()) {
                view.showError("Thông tin cuộc trò chuyện không hợp lệ");
            }
            return;
        }

        Log.d(TAG, "Conversation clicked: " + conversation.getId() + " - " + conversation.getTitle());

        if (isViewAttached()) {
            view.navigateToChatDetail(conversation.getId(), conversation.getTitle());
        }
    }

    @Override
    public void onNewChatClicked() {
        Log.d(TAG, "New chat clicked");

        if (isViewAttached()) {
            view.navigateToNewChat();
        }
    }

    @Override
    public void onLoadMoreClicked() {
        Log.d(TAG, "Load more clicked");
        loadMoreConversations();
    }

    /**
     * Kiểm tra xem còn cuộc trò chuyện nào để load không
     */
    private void checkHasMoreConversations() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null || lastConversationTime == null) {
            hasMoreConversations = false;
            if (isViewAttached()) {
                view.showLoadMoreButton(false);
            }
            return;
        }

        chatRepository.hasMoreConversations(currentUser.getUid(), lastConversationTime,
            new RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean hasMore) {
                    hasMoreConversations = hasMore;

                    if (isViewAttached()) {
                        view.showLoadMoreButton(hasMore);
                    }

                    Log.d(TAG, "Has more conversations: " + hasMore);
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Failed to check more conversations: " + error);
                    // Trong trường hợp lỗi, giả sử không còn cuộc trò chuyện nào
                    hasMoreConversations = false;

                    if (isViewAttached()) {
                        view.showLoadMoreButton(false);
                    }
                }
            });
    }

    /**
     * Getter cho testing hoặc debug
     */
    public List<Conversation> getConversationsList() {
        return new ArrayList<>(conversationsList);
    }

    public boolean isHasMoreConversations() {
        return hasMoreConversations;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isLoadingMore() {
        return isLoadingMore;
    }
}
