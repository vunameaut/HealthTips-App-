package com.vhn.doan.data.repository;

import com.vhn.doan.data.ChatMessage;

import java.util.List;

/**
 * Repository interface cho chức năng Chat
 */
public interface ChatRepository {

    /**
     * Gửi tin nhắn tới AI qua OpenRouter API
     * @param message Nội dung tin nhắn
     * @param callback Callback để nhận kết quả
     */
    void sendMessageToAI(String message, RepositoryCallback<String> callback);

    /**
     * Lưu tin nhắn vào Firebase
     * @param chatMessage Tin nhắn cần lưu
     * @param callback Callback để nhận kết quả
     */
    void saveChatMessage(ChatMessage chatMessage, RepositoryCallback<ChatMessage> callback);

    /**
     * Lấy danh sách tin nhắn của người dùng từ Firebase
     * @param userId ID người dùng
     * @param callback Callback để nhận kết quả
     */
    void getChatMessages(String userId, RepositoryCallback<List<ChatMessage>> callback);

    /**
     * Xóa lịch sử chat của người dùng
     * @param userId ID người dùng
     * @param callback Callback để nhận kết quả
     */
    void clearChatHistory(String userId, RepositoryCallback<Boolean> callback);

    /**
     * Trích xuất chủ đề từ nội dung tin nhắn
     * @param content Nội dung tin nhắn
     * @return Chủ đề được trích xuất
     */
    String extractTopic(String content);
}
