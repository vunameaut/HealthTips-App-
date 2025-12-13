package com.vhn.doan.data.repository;

import com.vhn.doan.data.model.Report;
import com.vhn.doan.data.model.ReportMessage;

import java.util.List;

/**
 * Interface Repository cho Report (báo cáo từ người dùng)
 * Hỗ trợ CRUD operations và chat messages
 */
public interface ReportRepository {

    /**
     * Callback interface cho các async operations
     */
    interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    // ==================== REPORT OPERATIONS ====================

    /**
     * Tạo report mới
     * @param report Report cần tạo
     * @param callback Callback trả về reportId khi thành công
     */
    void createReport(Report report, Callback<String> callback);

    /**
     * Lấy report theo ID
     * @param reportId ID của report
     * @param callback Callback trả về Report
     */
    void getReportById(String reportId, Callback<Report> callback);

    /**
     * Lấy tất cả reports của một user
     * @param userId ID của user
     * @param callback Callback trả về danh sách Reports
     */
    void getReportsByUserId(String userId, Callback<List<Report>> callback);

    /**
     * Lấy tất cả reports (cho admin)
     * @param callback Callback trả về danh sách Reports
     */
    void getAllReports(Callback<List<Report>> callback);

    /**
     * Lấy reports theo status
     * @param status Trạng thái cần lọc
     * @param callback Callback trả về danh sách Reports
     */
    void getReportsByStatus(String status, Callback<List<Report>> callback);

    /**
     * Cập nhật trạng thái report
     * @param reportId ID của report
     * @param status Trạng thái mới
     * @param callback Callback khi hoàn thành
     */
    void updateReportStatus(String reportId, String status, Callback<Void> callback);

    /**
     * Đóng report (không cho phép chat nữa)
     * @param reportId ID của report
     * @param callback Callback khi hoàn thành
     */
    void closeReport(String reportId, Callback<Void> callback);

    // ==================== MESSAGE OPERATIONS ====================

    /**
     * Gửi tin nhắn trong report
     * @param reportId ID của report
     * @param message Tin nhắn cần gửi
     * @param callback Callback trả về messageId khi thành công
     */
    void sendMessage(String reportId, ReportMessage message, Callback<String> callback);

    /**
     * Lấy tất cả tin nhắn của report
     * @param reportId ID của report
     * @param callback Callback trả về danh sách Messages
     */
    void getMessages(String reportId, Callback<List<ReportMessage>> callback);

    /**
     * Đánh dấu tin nhắn đã đọc
     * @param reportId ID của report
     * @param messageId ID của tin nhắn
     * @param callback Callback khi hoàn thành
     */
    void markMessageAsRead(String reportId, String messageId, Callback<Void> callback);

    /**
     * Đánh dấu tất cả tin nhắn của report đã đọc
     * @param reportId ID của report
     * @param readerType Loại người đọc ("user" hoặc "admin")
     * @param callback Callback khi hoàn thành
     */
    void markAllMessagesAsRead(String reportId, String readerType, Callback<Void> callback);

    // ==================== REALTIME LISTENERS ====================

    /**
     * Interface cho realtime listener
     */
    interface ReportListener {
        void onReportUpdated(Report report);
        void onError(String error);
    }

    interface MessagesListener {
        void onMessagesUpdated(List<ReportMessage> messages);
        void onNewMessage(ReportMessage message);
        void onError(String error);
    }

    /**
     * Lắng nghe thay đổi của report realtime
     * @param reportId ID của report
     * @param listener Listener nhận updates
     */
    void addReportListener(String reportId, ReportListener listener);

    /**
     * Lắng nghe tin nhắn mới realtime
     * @param reportId ID của report
     * @param listener Listener nhận updates
     */
    void addMessagesListener(String reportId, MessagesListener listener);

    /**
     * Lắng nghe danh sách reports của user realtime
     * @param userId ID của user
     * @param listener Listener nhận updates
     */
    void addUserReportsListener(String userId, Callback<List<Report>> listener);

    /**
     * Hủy tất cả listeners
     */
    void removeAllListeners();
}

