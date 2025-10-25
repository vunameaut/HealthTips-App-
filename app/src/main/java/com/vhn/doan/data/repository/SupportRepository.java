package com.vhn.doan.data.repository;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vhn.doan.data.SupportTicket;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repository quản lý dữ liệu hỗ trợ và báo cáo
 */
@Singleton
public class SupportRepository {

    private static final String TAG = "SupportRepository";
    private static final String SUPPORT_TICKETS_PATH = "support_tickets";
    private static final String SCREENSHOTS_PATH = "support_screenshots";

    private final DatabaseReference database;
    private final StorageReference storage;
    private final FirebaseAuth auth;

    public interface OnTicketCreatedListener {
        void onSuccess(String ticketId);
        void onError(String error);
    }

    public interface OnTicketsLoadedListener {
        void onSuccess(List<SupportTicket> tickets);
        void onError(String error);
    }

    public interface OnTicketUpdatedListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnScreenshotUploadedListener {
        void onSuccess(String downloadUrl);
        void onError(String error);
    }

    @Inject
    public SupportRepository() {
        this.database = FirebaseDatabase.getInstance().getReference();
        this.storage = FirebaseStorage.getInstance().getReference();
        this.auth = FirebaseAuth.getInstance();
    }

    /**
     * Tạo ticket hỗ trợ mới
     */
    public void createTicket(SupportTicket ticket, OnTicketCreatedListener listener) {
        if (ticket == null) {
            listener.onError("Ticket không được để trống");
            return;
        }

        String ticketId = database.child(SUPPORT_TICKETS_PATH).push().getKey();
        if (ticketId == null) {
            listener.onError("Không thể tạo ticket ID");
            return;
        }

        ticket.setTicketId(ticketId);
        ticket.setCreatedAt(new Date());
        ticket.setUpdatedAt(new Date());

        database.child(SUPPORT_TICKETS_PATH)
                .child(ticketId)
                .setValue(ticket.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Ticket created successfully: " + ticketId);
                    listener.onSuccess(ticketId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating ticket", e);
                    listener.onError("Không thể tạo ticket: " + e.getMessage());
                });
    }

    /**
     * Tải danh sách ticket của người dùng hiện tại
     */
    public void getUserTickets(OnTicketsLoadedListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onError("Người dùng chưa đăng nhập");
            return;
        }

        Log.d(TAG, "Loading tickets for user: " + userId);

        Query query = database.child(SUPPORT_TICKETS_PATH)
                .orderByChild("userId")
                .equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<SupportTicket> tickets = new ArrayList<>();

                Log.d(TAG, "DataSnapshot exists: " + dataSnapshot.exists());
                Log.d(TAG, "DataSnapshot children count: " + dataSnapshot.getChildrenCount());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Log.d(TAG, "Processing ticket: " + snapshot.getKey());

                        // Parse từ Map để xử lý Enum chính xác
                        Object value = snapshot.getValue();
                        if (value instanceof java.util.Map) {
                            @SuppressWarnings("unchecked")
                            java.util.Map<String, Object> map = (java.util.Map<String, Object>) value;
                            SupportTicket ticket = SupportTicket.fromMap(map);
                            if (ticket != null) {
                                tickets.add(ticket);
                                Log.d(TAG, "Ticket added: " + ticket.getTicketId());
                            }
                        } else {
                            // Fallback - thử parse trực tiếp
                            SupportTicket ticket = snapshot.getValue(SupportTicket.class);
                            if (ticket != null) {
                                tickets.add(ticket);
                                Log.d(TAG, "Ticket added (direct): " + ticket.getTicketId());
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing ticket: " + snapshot.getKey(), e);
                    }
                }

                Log.d(TAG, "Total tickets loaded: " + tickets.size());
                listener.onSuccess(tickets);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading tickets", databaseError.toException());
                listener.onError("Không thể tải danh sách ticket: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Tải tất cả ticket (dành cho admin)
     */
    public void getAllTickets(OnTicketsLoadedListener listener) {
        database.child(SUPPORT_TICKETS_PATH)
                .orderByChild("createdAt")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<SupportTicket> tickets = new ArrayList<>();

                        Log.d(TAG, "Loading all tickets - DataSnapshot exists: " + dataSnapshot.exists());
                        Log.d(TAG, "DataSnapshot children count: " + dataSnapshot.getChildrenCount());

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            try {
                                Log.d(TAG, "Processing ticket: " + snapshot.getKey());

                                // Parse từ Map để xử lý Enum chính xác
                                Object value = snapshot.getValue();
                                if (value instanceof java.util.Map) {
                                    @SuppressWarnings("unchecked")
                                    java.util.Map<String, Object> map = (java.util.Map<String, Object>) value;
                                    SupportTicket ticket = SupportTicket.fromMap(map);
                                    if (ticket != null) {
                                        tickets.add(ticket);
                                    }
                                } else {
                                    // Fallback - thử parse trực tiếp
                                    SupportTicket ticket = snapshot.getValue(SupportTicket.class);
                                    if (ticket != null) {
                                        tickets.add(ticket);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing ticket: " + snapshot.getKey(), e);
                            }
                        }

                        Log.d(TAG, "Total tickets loaded: " + tickets.size());
                        listener.onSuccess(tickets);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Error loading all tickets", databaseError.toException());
                        listener.onError("Không thể tải danh sách ticket: " + databaseError.getMessage());
                    }
                });
    }

    /**
     * Cập nhật trạng thái ticket
     */
    public void updateTicketStatus(String ticketId, SupportTicket.TicketStatus status,
                                   OnTicketUpdatedListener listener) {
        if (ticketId == null || ticketId.isEmpty()) {
            listener.onError("Ticket ID không hợp lệ");
            return;
        }

        DatabaseReference ticketRef = database.child(SUPPORT_TICKETS_PATH).child(ticketId);
        ticketRef.child("status").setValue(status.getValue())
                .addOnSuccessListener(aVoid -> {
                    ticketRef.child("updatedAt").setValue(new Date());
                    if (status == SupportTicket.TicketStatus.RESOLVED ||
                        status == SupportTicket.TicketStatus.CLOSED) {
                        ticketRef.child("resolvedAt").setValue(new Date());
                    }
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating ticket status", e);
                    listener.onError("Không thể cập nhật trạng thái: " + e.getMessage());
                });
    }

    /**
     * Thêm phản hồi từ admin
     */
    public void addAdminResponse(String ticketId, String adminId, String response,
                                OnTicketUpdatedListener listener) {
        if (ticketId == null || ticketId.isEmpty()) {
            listener.onError("Ticket ID không hợp lệ");
            return;
        }

        DatabaseReference ticketRef = database.child(SUPPORT_TICKETS_PATH).child(ticketId);
        ticketRef.child("adminResponse").setValue(response)
                .addOnSuccessListener(aVoid -> {
                    ticketRef.child("adminId").setValue(adminId);
                    ticketRef.child("updatedAt").setValue(new Date());
                    ticketRef.child("status").setValue(SupportTicket.TicketStatus.IN_PROGRESS.getValue());
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding admin response", e);
                    listener.onError("Không thể thêm phản hồi: " + e.getMessage());
                });
    }

    /**
     * Upload ảnh chụp màn hình
     */
    public void uploadScreenshot(Uri imageUri, OnScreenshotUploadedListener listener) {
        if (imageUri == null) {
            listener.onError("Không có ảnh để tải lên");
            return;
        }

        String fileName = "screenshot_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference screenshotRef = storage.child(SCREENSHOTS_PATH).child(fileName);

        screenshotRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    screenshotRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                Log.d(TAG, "Screenshot uploaded: " + uri.toString());
                                listener.onSuccess(uri.toString());
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error getting download URL", e);
                                listener.onError("Không thể lấy URL ảnh: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error uploading screenshot", e);
                    listener.onError("Không thể tải ảnh lên: " + e.getMessage());
                });
    }

    /**
     * Xóa ticket
     */
    public void deleteTicket(String ticketId, OnTicketUpdatedListener listener) {
        if (ticketId == null || ticketId.isEmpty()) {
            listener.onError("Ticket ID không hợp lệ");
            return;
        }

        database.child(SUPPORT_TICKETS_PATH)
                .child(ticketId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Ticket deleted: " + ticketId);
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting ticket", e);
                    listener.onError("Không thể xóa ticket: " + e.getMessage());
                });
    }

    /**
     * Lấy user ID hiện tại
     */
    private String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }
}

