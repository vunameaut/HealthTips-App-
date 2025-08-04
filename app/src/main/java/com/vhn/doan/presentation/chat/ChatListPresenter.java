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
        if (isViewAttached()) {
            view.navigateToNewChat();
        }
    }

    @Override
    public void onLoadMoreClicked() {
        if (!isLoadingMore && hasMoreConversations) {
            loadMoreConversations();
        }
    }

    // ========== CONTEXT MENU ACTIONS IMPLEMENTATION ==========

    @Override
    public void renameConversation(Conversation conversation, String newName) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (isViewAttached()) {
                view.showError("Bạn cần đăng nhập để đổi tên cuộc trò chuyện");
            }
            return;
        }

        if (newName == null || newName.trim().isEmpty()) {
            if (isViewAttached()) {
                view.showError("Tên cuộc trò chuyện không được để trống");
            }
            return;
        }

        // Cập nhật tên cuộc trò chuyện
        conversation.setTitle(newName.trim());

        chatRepository.updateConversation(conversation, new RepositoryCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation updatedConversation) {
                Log.d(TAG, "Conversation renamed successfully: " + updatedConversation.getTitle());

                if (isViewAttached()) {
                    view.showMessage("Đã đổi tên cuộc trò chuyện");
                    // Cập nhật trong danh sách local
                    updateConversationInList(updatedConversation);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to rename conversation: " + error);
                if (isViewAttached()) {
                    view.showError("Không thể đổi tên cuộc trò chuyện: " + error);
                }
            }
        });
    }

    @Override
    public void togglePinConversation(Conversation conversation) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (isViewAttached()) {
                view.showError("Bạn cần đăng nhập để ghim cuộc trò chuyện");
            }
            return;
        }

        // Nếu đang ghim và hiện tại chưa được ghim
        if (!conversation.isPinned()) {
            // Kiểm tra số lượng cuộc trò chuyện đã được ghim
            long pinnedCount = conversationsList.stream().filter(Conversation::isPinned).count();
            if (pinnedCount >= 5) {
                if (isViewAttached()) {
                    view.showError("Bạn chỉ có thể ghim tối đa 5 cuộc trò chuyện");
                }
                return;
            }
        }

        // Toggle trạng thái pin
        boolean newPinState = !conversation.isPinned();
        conversation.setPinned(newPinState);

        chatRepository.updateConversation(conversation, new RepositoryCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation updatedConversation) {
                String message = newPinState ? "Đã ghim cuộc trò chuyện" : "Đã bỏ ghim cuộc trò chuyện";
                Log.d(TAG, message + ": " + updatedConversation.getTitle());

                if (isViewAttached()) {
                    view.showMessage(message);
                    // Cập nhật trong danh sách local
                    updateConversationInList(updatedConversation);
                    // Sắp xếp lại danh sách để đưa cuộc trò chuyện được ghim lên đầu
                    sortConversationsWithPinnedFirst();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to toggle pin conversation: " + error);
                // Revert lại trạng thái cũ
                conversation.setPinned(!newPinState);
                if (isViewAttached()) {
                    view.showError("Không thể cập nhật trạng thái ghim: " + error);
                }
            }
        });
    }

    @Override
    public void toggleMuteConversation(Conversation conversation) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (isViewAttached()) {
                view.showError("Bạn cần đăng nhập để tắt/bật thông báo");
            }
            return;
        }

        // Toggle trạng thái mute
        boolean newMuteState = !conversation.isMuted();
        conversation.setMuted(newMuteState);

        chatRepository.updateConversation(conversation, new RepositoryCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation updatedConversation) {
                String message = newMuteState ? "Đã tắt thông báo" : "Đã bật thông báo";
                Log.d(TAG, message + " cho cuộc trò chuyện: " + updatedConversation.getTitle());

                if (isViewAttached()) {
                    view.showMessage(message);
                    // Cập nhật trong danh sách local
                    updateConversationInList(updatedConversation);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to toggle mute conversation: " + error);
                // Revert lại trạng thái cũ
                conversation.setMuted(!newMuteState);
                if (isViewAttached()) {
                    view.showError("Không thể cập nhật trạng thái thông báo: " + error);
                }
            }
        });
    }

    @Override
    public void deleteConversation(Conversation conversation) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (isViewAttached()) {
                view.showError("Bạn cần đăng nhập để xóa cuộc trò chuyện");
            }
            return;
        }

        if (conversation.getId() == null) {
            if (isViewAttached()) {
                view.showError("Thông tin cuộc trò chuyện không hợp lệ");
            }
            return;
        }

        chatRepository.deleteConversation(conversation.getId(), new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                Log.d(TAG, "Conversation deleted successfully: " + conversation.getTitle());

                if (isViewAttached()) {
                    view.showMessage("Đã xóa cuộc trò chuyện");
                    // Xóa khỏi danh sách local
                    removeConversationFromList(conversation);

                    // Nếu danh sách trống, hiển thị empty state
                    if (conversationsList.isEmpty()) {
                        view.showEmptyState();
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to delete conversation: " + error);
                if (isViewAttached()) {
                    view.showError("Không thể xóa cuộc trò chuyện: " + error);
                }
            }
        });
    }

    @Override
    public void markConversationAsRead(Conversation conversation) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (isViewAttached()) {
                view.showError("Bạn cần đăng nhập để đánh dấu đã đọc");
            }
            return;
        }

        // Đánh dấu đã đọc
        conversation.setRead(true);

        chatRepository.updateConversation(conversation, new RepositoryCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation updatedConversation) {
                Log.d(TAG, "Conversation marked as read: " + updatedConversation.getTitle());

                if (isViewAttached()) {
                    view.showMessage("Đã đánh dấu là đã đọc");
                    // Cập nhật trong danh sách local
                    updateConversationInList(updatedConversation);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to mark conversation as read: " + error);
                // Revert lại trạng thái cũ
                conversation.setRead(false);
                if (isViewAttached()) {
                    view.showError("Không thể đánh dấu đã đọc: " + error);
                }
            }
        });
    }

    // ========== HELPER METHODS ==========

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

        chatRepository.hasMoreConversations(currentUser.getUid(), lastConversationTime, new RepositoryCallback<Boolean>() {
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
                Log.e(TAG, "Error checking more conversations: " + error);
                // Trong trường hợp lỗi, giả định không còn cuộc trò chuyện nào
                hasMoreConversations = false;
                if (isViewAttached()) {
                    view.showLoadMoreButton(false);
                }
            }
        });
    }

    /**
     * Cập nhật một cuộc trò chuyện trong danh sách local
     */
    private void updateConversationInList(Conversation updatedConversation) {
        for (int i = 0; i < conversationsList.size(); i++) {
            if (conversationsList.get(i).getId().equals(updatedConversation.getId())) {
                conversationsList.set(i, updatedConversation);
                if (isViewAttached()) {
                    view.showConversations(new ArrayList<>(conversationsList));
                }
                break;
            }
        }
    }

    /**
     * Xóa một cuộc trò chuyện khỏi danh sách local
     */
    private void removeConversationFromList(Conversation conversation) {
        conversationsList.removeIf(c -> c.getId().equals(conversation.getId()));
        if (isViewAttached()) {
            view.showConversations(new ArrayList<>(conversationsList));
        }
    }

    /**
     * Sắp xếp danh sách cuộc trò chuyện với các cuộc trò chuyện được ghim lên đầu
     */
    private void sortConversationsWithPinnedFirst() {
        conversationsList.sort((c1, c2) -> {
            if (c1.isPinned() && !c2.isPinned()) {
                return -1;
            } else if (!c1.isPinned() && c2.isPinned()) {
                return 1;
            } else {
                return 0;
            }
        });

        if (isViewAttached()) {
            view.showConversations(new ArrayList<>(conversationsList));
        }
    }
}
