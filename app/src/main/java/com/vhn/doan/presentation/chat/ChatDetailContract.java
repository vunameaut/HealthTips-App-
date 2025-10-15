package com.vhn.doan.presentation.chat;

import com.vhn.doan.data.ChatMessage;
import com.vhn.doan.presentation.base.BaseView;

import java.util.List;

/**
 * Contract cho ChatDetail feature - Hiển thị và quản lý chi tiết một cuộc trò chuyện
 */
public interface ChatDetailContract {

    interface View extends BaseView {
        /**
         * Hiển thị danh sách tin nhắn của cuộc trò chuyện
         */
        void showMessages(List<ChatMessage> messages);

        /**
         * Thêm tin nhắn mới vào cuộc trò chuyện
         */
        void addMessage(ChatMessage message);

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
         * Cuộn xuống tin nhắn mới nhất
         */
        void scrollToLatestMessage();

        /**
         * Hiển thị trạng thái đang tải tin nhắn
         */
        void showLoadingMessages();

        /**
         * Ẩn trạng thái đang tải tin nhắn
         */
        void hideLoadingMessages();

        /**
         * Hiển thị trạng thái cuộc trò chuyện trống
         */
        void showEmptyConversation();

        /**
         * Ẩn trạng thái cuộc trò chuyện trống
         */
        void hideEmptyConversation();

        /**
         * Hiển thị thông báo lỗi khi gửi tin nhắn thất bại
         */
        void showSendMessageError(String error);

        /**
         * Hiển thị thông báo lỗi khi tải tin nhắn thất bại
         */
        void showLoadMessagesError(String error);

        /**
         * Cập nhật tiêu đề cuộc trò chuyện
         */
        void updateConversationTitle(String title);

        /**
         * Hiển thị thông báo thành công
         */
        void showMessage(String message);

        /**
         * Vô hiệu hóa ô nhập liệu và nút gửi
         */
        void disableInput();

        /**
         * Kích hoạt lại ô nhập liệu và nút gửi
         */
        void enableInput();
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
         * Khởi tạo với ID cuộc trò chuyện
         */
        void initialize(String conversationId, String conversationTitle);

        /**
         * Tải danh sách tin nhắn của cuộc trò chuyện
         */
        void loadMessages();

        /**
         * Gửi tin nhắn mới trong cuộc trò chuyện
         */
        void sendMessage(String content);

        /**
         * Làm mới danh sách tin nhắn
         */
        void refreshMessages();
    }
}
