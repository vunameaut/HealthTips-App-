package com.vhn.doan.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.data.model.Report;
import com.vhn.doan.data.model.ReportMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation của ReportRepository sử dụng Firebase Realtime Database
 */
public class ReportRepositoryImpl implements ReportRepository {

    private static final String TAG = "ReportRepository";
    private static final String REPORTS_NODE = "reports";
    private static final String MESSAGES_NODE = "messages";

    private final DatabaseReference reportsRef;
    private final List<ValueEventListener> activeListeners = new ArrayList<>();
    private final List<ChildEventListener> activeChildListeners = new ArrayList<>();
    private final Map<String, DatabaseReference> listenerRefs = new HashMap<>();

    public ReportRepositoryImpl() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        reportsRef = database.getReference(REPORTS_NODE);
    }

    // ==================== REPORT OPERATIONS ====================

    @Override
    public void createReport(Report report, Callback<String> callback) {
        try {
            // Tạo key mới cho report
            String reportId = reportsRef.push().getKey();
            if (reportId == null) {
                callback.onError("Không thể tạo ID cho report");
                return;
            }

            report.setId(reportId);
            report.setCreatedAt(System.currentTimeMillis());
            report.setUpdatedAt(System.currentTimeMillis());

            // Lưu report vào Firebase
            reportsRef.child(reportId).setValue(report.toMap())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Report created successfully: " + reportId);
                        callback.onSuccess(reportId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to create report", e);
                        callback.onError("Không thể tạo report: " + e.getMessage());
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error creating report", e);
            callback.onError("Lỗi: " + e.getMessage());
        }
    }

    @Override
    public void getReportById(String reportId, Callback<Report> callback) {
        reportsRef.child(reportId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Report report = snapshot.getValue(Report.class);
                    if (report != null) {
                        report.setId(snapshot.getKey());
                        callback.onSuccess(report);
                    } else {
                        callback.onError("Không thể parse report data");
                    }
                } else {
                    callback.onError("Report không tồn tại");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to get report", error.toException());
                callback.onError(error.getMessage());
            }
        });
    }

    @Override
    public void getReportsByUserId(String userId, Callback<List<Report>> callback) {
        Query query = reportsRef.orderByChild("userId").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Report> reports = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Report report = child.getValue(Report.class);
                    if (report != null) {
                        report.setId(child.getKey());
                        reports.add(report);
                    }
                }
                // Sắp xếp theo thời gian mới nhất
                Collections.sort(reports, (r1, r2) -> 
                    Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));
                callback.onSuccess(reports);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to get user reports", error.toException());
                callback.onError(error.getMessage());
            }
        });
    }

    @Override
    public void getAllReports(Callback<List<Report>> callback) {
        reportsRef.orderByChild("createdAt").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Report> reports = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Report report = child.getValue(Report.class);
                    if (report != null) {
                        report.setId(child.getKey());
                        reports.add(report);
                    }
                }
                // Sắp xếp theo thời gian mới nhất
                Collections.sort(reports, (r1, r2) -> 
                    Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));
                callback.onSuccess(reports);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    @Override
    public void getReportsByStatus(String status, Callback<List<Report>> callback) {
        Query query = reportsRef.orderByChild("status").equalTo(status);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Report> reports = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Report report = child.getValue(Report.class);
                    if (report != null) {
                        report.setId(child.getKey());
                        reports.add(report);
                    }
                }
                Collections.sort(reports, (r1, r2) -> 
                    Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));
                callback.onSuccess(reports);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    @Override
    public void updateReportStatus(String reportId, String status, Callback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("updatedAt", System.currentTimeMillis());

        reportsRef.child(reportId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Report status updated: " + reportId + " -> " + status);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update report status", e);
                    callback.onError(e.getMessage());
                });
    }

    @Override
    public void closeReport(String reportId, Callback<Void> callback) {
        updateReportStatus(reportId, Report.STATUS_CLOSED, callback);
    }

    // ==================== MESSAGE OPERATIONS ====================

    @Override
    public void sendMessage(String reportId, ReportMessage message, Callback<String> callback) {
        try {
            DatabaseReference messagesRef = reportsRef.child(reportId).child(MESSAGES_NODE);
            String messageId = messagesRef.push().getKey();
            if (messageId == null) {
                callback.onError("Không thể tạo ID cho tin nhắn");
                return;
            }

            message.setId(messageId);
            message.setTimestamp(System.currentTimeMillis());

            // Lưu tin nhắn
            messagesRef.child(messageId).setValue(message.toMap())
                    .addOnSuccessListener(aVoid -> {
                        // Cập nhật lastMessageAt và lastMessagePreview trong report
                        Map<String, Object> reportUpdates = new HashMap<>();
                        reportUpdates.put("lastMessageAt", message.getTimestamp());
                        reportUpdates.put("lastMessagePreview", message.getPreviewText(50));
                        reportUpdates.put("updatedAt", System.currentTimeMillis());

                        // Nếu admin reply lần đầu, đổi status sang in_progress
                        if (message.isFromAdmin()) {
                            reportUpdates.put("status", Report.STATUS_IN_PROGRESS);
                        }

                        reportsRef.child(reportId).updateChildren(reportUpdates);

                        Log.d(TAG, "Message sent successfully: " + messageId);
                        callback.onSuccess(messageId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to send message", e);
                        callback.onError("Không thể gửi tin nhắn: " + e.getMessage());
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
            callback.onError("Lỗi: " + e.getMessage());
        }
    }

    @Override
    public void getMessages(String reportId, Callback<List<ReportMessage>> callback) {
        DatabaseReference messagesRef = reportsRef.child(reportId).child(MESSAGES_NODE);
        messagesRef.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ReportMessage> messages = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    ReportMessage message = child.getValue(ReportMessage.class);
                    if (message != null) {
                        message.setId(child.getKey());
                        messages.add(message);
                    }
                }
                callback.onSuccess(messages);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    @Override
    public void markMessageAsRead(String reportId, String messageId, Callback<Void> callback) {
        reportsRef.child(reportId).child(MESSAGES_NODE).child(messageId)
                .child("read").setValue(true)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void markAllMessagesAsRead(String reportId, String readerType, Callback<Void> callback) {
        DatabaseReference messagesRef = reportsRef.child(reportId).child(MESSAGES_NODE);
        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    ReportMessage message = child.getValue(ReportMessage.class);
                    if (message != null && !message.isRead()) {
                        // User đọc tin nhắn từ admin và ngược lại
                        boolean shouldMark = (readerType.equals("user") && message.isFromAdmin()) ||
                                           (readerType.equals("admin") && message.isFromUser());
                        if (shouldMark) {
                            child.getRef().child("read").setValue(true);
                        }
                    }
                }
                callback.onSuccess(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    // ==================== REALTIME LISTENERS ====================

    @Override
    public void addReportListener(String reportId, ReportListener listener) {
        DatabaseReference ref = reportsRef.child(reportId);
        ValueEventListener valueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Report report = snapshot.getValue(Report.class);
                    if (report != null) {
                        report.setId(snapshot.getKey());
                        listener.onReportUpdated(report);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        };
        ref.addValueEventListener(valueListener);
        activeListeners.add(valueListener);
        listenerRefs.put("report_" + reportId, ref);
    }

    @Override
    public void addMessagesListener(String reportId, MessagesListener listener) {
        DatabaseReference messagesRef = reportsRef.child(reportId).child(MESSAGES_NODE);
        
        // Listener cho danh sách tin nhắn
        ValueEventListener valueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ReportMessage> messages = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    ReportMessage message = child.getValue(ReportMessage.class);
                    if (message != null) {
                        message.setId(child.getKey());
                        messages.add(message);
                    }
                }
                listener.onMessagesUpdated(messages);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        };
        messagesRef.orderByChild("timestamp").addValueEventListener(valueListener);
        activeListeners.add(valueListener);
        listenerRefs.put("messages_" + reportId, messagesRef);

        // Listener cho tin nhắn mới
        ChildEventListener childListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                ReportMessage message = snapshot.getValue(ReportMessage.class);
                if (message != null) {
                    message.setId(snapshot.getKey());
                    listener.onNewMessage(message);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        };
        messagesRef.addChildEventListener(childListener);
        activeChildListeners.add(childListener);
    }

    @Override
    public void addUserReportsListener(String userId, Callback<List<Report>> listener) {
        Query query = reportsRef.orderByChild("userId").equalTo(userId);
        ValueEventListener valueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Report> reports = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Report report = child.getValue(Report.class);
                    if (report != null) {
                        report.setId(child.getKey());
                        reports.add(report);
                    }
                }
                Collections.sort(reports, (r1, r2) -> 
                    Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));
                listener.onSuccess(reports);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        };
        query.addValueEventListener(valueListener);
        activeListeners.add(valueListener);
    }

    @Override
    public void removeAllListeners() {
        for (Map.Entry<String, DatabaseReference> entry : listenerRefs.entrySet()) {
            DatabaseReference ref = entry.getValue();
            for (ValueEventListener listener : activeListeners) {
                try {
                    ref.removeEventListener(listener);
                } catch (Exception ignored) {}
            }
            for (ChildEventListener listener : activeChildListeners) {
                try {
                    ref.removeEventListener(listener);
                } catch (Exception ignored) {}
            }
        }
        activeListeners.clear();
        activeChildListeners.clear();
        listenerRefs.clear();
        Log.d(TAG, "All listeners removed");
    }
}

