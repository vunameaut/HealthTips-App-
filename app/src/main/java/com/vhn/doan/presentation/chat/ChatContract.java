package com.vhn.doan.presentation.chat;

import com.vhn.doan.data.ChatMessage;
import com.vhn.doan.presentation.base.BasePresenter;
import com.vhn.doan.presentation.base.BaseView;

import java.util.List;

/**
 * Contract interface cho Chat feature theo kiến trúc MVP
 */
public interface ChatContract {

    interface View extends BaseView<Presenter> {
        /**
         * Hiển thị danh sách tin nhắn chat
         */
        void showMessages(List<ChatMessage> messages);

        /**
         * Thêm tin nhắn mới vào danh sách
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
         * Hiển thị thông báo lỗi khi gửi tin nhắn thất bại
         */
        void showSendMessageError(String error);

        /**
         * Hiển thị thông báo lỗi khi tải tin nhắn thất bại
         */
        void showLoadMessagesError(String error);

        /**
         * Hiển thị trạng thái đang tải tin nhắn
         */
        void showLoadingMessages();

        /**
         * Ẩn trạng thái đang tải tin nhắn
         */
        void hideLoadingMessages();
    }

    interface Presenter extends BasePresenter<View> {
        /**
         * Tải danh sách tin nhắn từ Firebase
         */
        void loadMessages();

        /**
         * Gửi tin nhắn mới
         * @param content Nội dung tin nhắn
         */
        void sendMessage(String content);

        /**
         * Xóa lịch sử chat
         */
        void clearChatHistory();

        /**
         * Làm mới danh sách tin nhắn
         */
        void refreshMessages();
    }
}
