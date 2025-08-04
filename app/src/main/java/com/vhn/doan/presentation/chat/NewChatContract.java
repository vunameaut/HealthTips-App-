package com.vhn.doan.presentation.chat;

import com.vhn.doan.data.ChatMessage;
import com.vhn.doan.data.Conversation;
import com.vhn.doan.presentation.base.BaseView;

import java.util.List;

/**
 * Contract cho NewChat feature - Tạo cuộc trò chuyện mới
 */
public interface NewChatContract {

    interface View extends BaseView {
        /**
         * Hiển thị trạng thái đang tạo cuộc trò chuyện
         */
        void showCreatingConversation();

        /**
         * Ẩn trạng thái đang tạo cuộc trò chuyện
         */
        void hideCreatingConversation();

        /**
         * Hiển thị trạng thái đang gửi tin nhắn
         */
        void showSendingMessage();

        /**
         * Ẩn trạng thái đang gửi tin nhắn
         */
        void hideSendingMessage();

        /**
         * Hiển thị trạng thái AI đang trả lời
         */
        void showAiTyping();

        /**
         * Ẩn trạng thái AI đang trả lời
         */
        void hideAiTyping();

        /**
         * Xóa nội dung trong ô nhập tin nhắn
         */
        void clearMessageInput();

        /**
         * Hiển thị thông báo lỗi khi tạo cuộc trò chuyện thất bại
         */
        void showCreateConversationError(String error);

        /**
         * Hiển thị thông báo lỗi khi gửi tin nhắn thất bại
         */
        void showSendMessageError(String error);

        /**
         * Điều hướng đến màn hình chat detail với cuộc trò chuyện đã tạo
         */
        void navigateToChatDetail(String conversationId, String conversationTitle);

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
         * Tạo cuộc trò chuyện mới và gửi tin nhắn đầu tiên
         * @param firstMessage Tin nhắn đầu tiên
         */
        void createConversationAndSendMessage(String firstMessage);
    }
}
