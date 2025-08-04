package com.vhn.doan.presentation.chat;

import com.vhn.doan.data.Conversation;
import com.vhn.doan.presentation.base.BaseView;

import java.util.List;

/**
 * Contract cho ChatList feature - Hiển thị danh sách các cuộc trò chuyện
 */
public interface ChatListContract {

    interface View extends BaseView {
        /**
         * Hiển thị danh sách cuộc trò chuyện
         */
        void showConversations(List<Conversation> conversations);

        /**
         * Thêm danh sách cuộc trò chuyện mới vào cuối (load more)
         */
        void addMoreConversations(List<Conversation> conversations);

        /**
         * Hiển thị trạng thái đang tải danh sách cuộc trò chuyện
         */
        void showLoadingConversations();

        /**
         * Ẩn trạng thái đang tải danh sách cuộc trò chuyện
         */
        void hideLoadingConversations();

        /**
         * Hiển thị trạng thái đang tải thêm cuộc trò chuyện
         */
        void showLoadingMore();

        /**
         * Ẩn trạng thái đang tải thêm cuộc trò chuyện
         */
        void hideLoadingMore();

        /**
         * Hiển thị/ẩn nút "Xem thêm"
         */
        void showLoadMoreButton(boolean show);

        /**
         * Hiển thị trạng thái danh sách trống
         */
        void showEmptyState();

        /**
         * Ẩn trạng thái danh sách trống
         */
        void hideEmptyState();

        /**
         * Hiển thị thông báo lỗi khi tải danh sách cuộc trò chuyện
         */
        void showLoadConversationsError(String error);

        /**
         * Hiển thị thông báo lỗi khi tải thêm cuộc trò chuyện
         */
        void showLoadMoreError(String error);

        /**
         * Điều hướng đến màn hình chat chi tiết
         */
        void navigateToChatDetail(String conversationId, String conversationTitle);

        /**
         * Điều hướng đến màn hình tạo cuộc trò chuyện mới
         */
        void navigateToNewChat();

        /**
         * Hiển thị thông báo thành công
         */
        void showMessage(String message);
    }

    interface Presenter {
        /**
         * Gắn view vào presenter
         */
        void attachView(View view);

        /**
         * Gỡ bỏ view khỏi presenter
         */
        void detachView();

        /**
         * Tải danh sách cuộc trò chuyện (lần đầu)
         */
        void loadConversations();

        /**
         * Tải thêm cuộc trò chuyện (phân trang)
         */
        void loadMoreConversations();

        /**
         * Làm mới danh sách cuộc trò chuyện
         */
        void refreshConversations();

        /**
         * Xử lý khi người dùng nhấn vào một cuộc trò chuyện
         */
        void onConversationClicked(Conversation conversation);

        /**
         * Xử lý khi người dùng muốn tạo cuộc trò chuyện mới
         */
        void onNewChatClicked();

        /**
         * Xử lý khi người dùng nhấn "Xem thêm"
         */
        void onLoadMoreClicked();

        // ========== CONTEXT MENU ACTIONS ==========

        /**
         * Đổi tên cuộc trò chuyện
         */
        void renameConversation(Conversation conversation, String newName);

        /**
         * Bật/tắt ghim cuộc trò chuyện
         */
        void togglePinConversation(Conversation conversation);

        /**
         * Bật/tắt thông báo cuộc trò chuyện
         */
        void toggleMuteConversation(Conversation conversation);

        /**
         * Xóa cuộc trò chuyện
         */
        void deleteConversation(Conversation conversation);

        /**
         * Đánh dấu cuộc trò chuyện là đã đọc
         */
        void markConversationAsRead(Conversation conversation);
    }
}
