package com.vhn.doan.data.repository;

import com.vhn.doan.data.ChatMessage;
import com.vhn.doan.data.Conversation;

import java.util.List;

/**
 * Repository interface cho chức năng Chat với hỗ trợ multiple conversations
 */
public interface ChatRepository {

    /**
     * Gửi tin nhắn tới AI qua OpenRouter API
     * @param message Nội dung tin nhắn
     * @param callback Callback để nhận kết quả
     */
    void sendMessageToAI(String message, RepositoryCallback<String> callback);

    // ========== CONVERSATION MANAGEMENT ==========

    /**
     * Tạo cuộc trò chuyện mới
     * @param userId ID người dùng
     * @param firstMessage Tin nhắn đầu tiên để tạo tiêu đề
     * @param callback Callback để nhận kết quả
     */
    void createConversation(String userId, String firstMessage, RepositoryCallback<Conversation> callback);

    /**
     * Lấy danh sách cuộc trò chuyện của người dùng (có phân trang)
     * @param userId ID người dùng
     * @param limit Số lượng cuộc trò chuyện cần lấy (tối đa 8 cho lần đầu)
     * @param lastConversationTime Thời gian của cuộc trò chuyện cuối cùng (để phân trang)
     * @param callback Callback để nhận kết quả
     */
    void getConversations(String userId, int limit, Long lastConversationTime, RepositoryCallback<List<Conversation>> callback);

    /**
     * Cập nhật thông tin cuộc trò chuyện (tin nhắn cuối, số lượng tin nhắn)
     * @param conversation Cuộc trò chuyện cần cập nhật
     * @param callback Callback để nhận kết quả
     */
    void updateConversation(Conversation conversation, RepositoryCallback<Conversation> callback);

    /**
     * Xóa cuộc trò chuyện và tất cả tin nhắn trong đó
     * @param conversationId ID cuộc trò chuyện
     * @param callback Callback để nhận kết quả
     */
    void deleteConversation(String conversationId, RepositoryCallback<Boolean> callback);

    // ========== MESSAGE MANAGEMENT ==========

    /**
     * Lưu tin nhắn vào Firebase
     * @param chatMessage Tin nhắn cần lưu
     * @param callback Callback để nhận kết quả
     */
    void saveChatMessage(ChatMessage chatMessage, RepositoryCallback<ChatMessage> callback);

    /**
     * Lấy danh sách tin nhắn của một cuộc trò chuyện cụ thể
     * @param conversationId ID cuộc trò chuyện
     * @param callback Callback để nhận kết quả
     */
    void getChatMessages(String conversationId, RepositoryCallback<List<ChatMessage>> callback);

    /**
     * Xóa tất cả tin nhắn trong một cuộc trò chuyện
     * @param conversationId ID cuộc trò chuyện
     * @param callback Callback để nhận kết quả
     */
    void clearChatMessages(String conversationId, RepositoryCallback<Boolean> callback);

    /**
     * Xóa lịch sử chat của người dùng (tất cả cuộc trò chuyện)
     * @param userId ID người dùng
     * @param callback Callback để nhận kết quả
     */
    void clearChatHistory(String userId, RepositoryCallback<Boolean> callback);

    // ========== UTILITY METHODS ==========

    /**
     * Trích xuất chủ đề từ nội dung tin nhắn
     * @param content Nội dung tin nhắn
     * @return Chủ đề được trích xuất
     */
    String extractTopic(String content);

    /**
     * Kiểm tra xem còn cuộc trò chuyện nào để load không
     * @param userId ID người dùng
     * @param lastConversationTime Thời gian cuộc trò chuyện cuối cùng đã load
     * @param callback Callback để nhận kết quả
     */
    void hasMoreConversations(String userId, Long lastConversationTime, RepositoryCallback<Boolean> callback);
}
