package com.vhn.doan.data.repository;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query;
import com.vhn.doan.data.Reminder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Triển khai ReminderRepository sử dụng Firebase Realtime Database
 */
@Singleton
public class ReminderRepositoryImpl implements ReminderRepository {

    private static final String REMINDERS_NODE = "reminders";
    private static final String USER_REMINDERS_NODE = "user_reminders";

    private final DatabaseReference database;
    private final DatabaseReference remindersRef;
    private final DatabaseReference userRemindersRef;

    @Inject
    public ReminderRepositoryImpl() {
        this.database = FirebaseDatabase.getInstance().getReference();
        this.remindersRef = database.child(REMINDERS_NODE);
        this.userRemindersRef = database.child(USER_REMINDERS_NODE);
    }

    @Override
    public void addReminder(Reminder reminder, RepositoryCallback<String> callback) {
        try {
            if (reminder.getId() == null || reminder.getId().isEmpty()) {
                reminder.setId(remindersRef.push().getKey());
            }

            reminder.setUpdatedAt(System.currentTimeMillis());

            Map<String, Object> reminderValues = reminder.toFirebaseMap();
            Map<String, Object> childUpdates = new HashMap<>();

            // Lưu vào node reminders
            childUpdates.put("/" + REMINDERS_NODE + "/" + reminder.getId(), reminderValues);

            // Lưu vào node user_reminders để query nhanh
            childUpdates.put("/" + USER_REMINDERS_NODE + "/" + reminder.getUserId() + "/" + reminder.getId(), true);

            database.updateChildren(childUpdates)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(reminder.getId()))
                    .addOnFailureListener(e -> callback.onError("Lỗi khi thêm nhắc nhở: " + e.getMessage()));

        } catch (Exception e) {
            callback.onError("Lỗi khi thêm nhắc nhở: " + e.getMessage());
        }
    }

    @Override
    public void updateReminder(Reminder reminder, RepositoryCallback<Void> callback) {
        try {
            if (reminder.getId() == null || reminder.getId().isEmpty()) {
                callback.onError("ID nhắc nhở không hợp lệ");
                return;
            }

            reminder.setUpdatedAt(System.currentTimeMillis());
            Map<String, Object> reminderValues = reminder.toFirebaseMap();

            remindersRef.child(reminder.getId())
                    .updateChildren(reminderValues)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                    .addOnFailureListener(e -> callback.onError("Lỗi khi cập nhật nhắc nhở: " + e.getMessage()));

        } catch (Exception e) {
            callback.onError("Lỗi khi cập nhật nhắc nhở: " + e.getMessage());
        }
    }

    @Override
    public void deleteReminder(String reminderId, RepositoryCallback<Void> callback) {
        try {
            if (reminderId == null || reminderId.isEmpty()) {
                callback.onError("ID nhắc nhở không hợp lệ");
                return;
            }

            // Lấy thông tin reminder trước khi xóa để có userId
            remindersRef.child(reminderId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Reminder reminder = dataSnapshot.getValue(Reminder.class);
                        if (reminder != null) {
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/" + REMINDERS_NODE + "/" + reminderId, null);
                            childUpdates.put("/" + USER_REMINDERS_NODE + "/" + reminder.getUserId() + "/" + reminderId, null);

                            database.updateChildren(childUpdates)
                                    .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                                    .addOnFailureListener(e -> callback.onError("Lỗi khi xóa nhắc nhở: " + e.getMessage()));
                        } else {
                            callback.onError("Không tìm thấy nhắc nhở");
                        }
                    } else {
                        callback.onError("Không tìm thấy nhắc nhở");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    callback.onError("Lỗi khi xóa nhắc nhở: " + databaseError.getMessage());
                }
            });

        } catch (Exception e) {
            callback.onError("Lỗi khi xóa nhắc nhở: " + e.getMessage());
        }
    }

    @Override
    public void getUserReminders(String userId, RepositoryCallback<List<Reminder>> callback) {
        try {
            if (userId == null || userId.isEmpty()) {
                callback.onError("ID người dùng không hợp lệ");
                return;
            }

            userRemindersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<String> reminderIds = new ArrayList<>();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        reminderIds.add(child.getKey());
                    }

                    if (reminderIds.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    // Lấy chi tiết từng reminder
                    fetchRemindersByIds(reminderIds, callback);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    callback.onError("Lỗi khi lấy danh sách nhắc nhở: " + databaseError.getMessage());
                }
            });

        } catch (Exception e) {
            callback.onError("Lỗi khi lấy danh sách nhắc nhở: " + e.getMessage());
        }
    }

    @Override
    public void getReminderById(String reminderId, RepositoryCallback<Reminder> callback) {
        try {
            if (reminderId == null || reminderId.isEmpty()) {
                callback.onError("ID nhắc nhở không hợp lệ");
                return;
            }

            remindersRef.child(reminderId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Reminder reminder = dataSnapshot.getValue(Reminder.class);
                        if (reminder != null) {
                            callback.onSuccess(reminder);
                        } else {
                            callback.onError("Không thể chuyển đổi dữ liệu nhắc nhở");
                        }
                    } else {
                        callback.onError("Không tìm thấy nhắc nhở");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    callback.onError("Lỗi khi lấy nhắc nhở: " + databaseError.getMessage());
                }
            });

        } catch (Exception e) {
            callback.onError("Lỗi khi lấy nhắc nhở: " + e.getMessage());
        }
    }

    @Override
    public void getActiveReminders(String userId, RepositoryCallback<List<Reminder>> callback) {
        getUserReminders(userId, new RepositoryCallback<List<Reminder>>() {
            @Override
            public void onSuccess(List<Reminder> reminders) {
                List<Reminder> activeReminders = new ArrayList<>();
                for (Reminder reminder : reminders) {
                    if (reminder.isActive()) {
                        activeReminders.add(reminder);
                    }
                }
                callback.onSuccess(activeReminders);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    @Override
    public void toggleReminder(String reminderId, boolean isActive, RepositoryCallback<Void> callback) {
        try {
            if (reminderId == null || reminderId.isEmpty()) {
                callback.onError("ID nhắc nhở không hợp lệ");
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("active", isActive);
            updates.put("updatedAt", System.currentTimeMillis());

            remindersRef.child(reminderId)
                    .updateChildren(updates)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                    .addOnFailureListener(e -> callback.onError("Lỗi khi cập nhật trạng thái nhắc nhở: " + e.getMessage()));

        } catch (Exception e) {
            callback.onError("Lỗi khi cập nhật trạng thái nhắc nhở: " + e.getMessage());
        }
    }

    @Override
    public void getRemindersByHealthTip(String healthTipId, RepositoryCallback<List<Reminder>> callback) {
        try {
            if (healthTipId == null || healthTipId.isEmpty()) {
                callback.onError("ID mẹo sức khỏe không hợp lệ");
                return;
            }

            Query query = remindersRef.orderByChild("healthTipId").equalTo(healthTipId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Reminder> reminders = new ArrayList<>();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Reminder reminder = child.getValue(Reminder.class);
                        if (reminder != null) {
                            reminders.add(reminder);
                        }
                    }
                    callback.onSuccess(reminders);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    callback.onError("Lỗi khi lấy nhắc nhở theo mẹo sức khỏe: " + databaseError.getMessage());
                }
            });

        } catch (Exception e) {
            callback.onError("Lỗi khi lấy nhắc nhở theo mẹo sức khỏe: " + e.getMessage());
        }
    }

    /**
     * Helper method để lấy chi tiết nhiều reminder theo IDs
     */
    private void fetchRemindersByIds(List<String> reminderIds, RepositoryCallback<List<Reminder>> callback) {
        List<Reminder> reminders = new ArrayList<>();
        final int[] pendingRequests = {reminderIds.size()};

        for (String reminderId : reminderIds) {
            remindersRef.child(reminderId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Reminder reminder = dataSnapshot.getValue(Reminder.class);
                        if (reminder != null) {
                            reminders.add(reminder);
                        }
                    }

                    pendingRequests[0]--;
                    if (pendingRequests[0] == 0) {
                        // Sắp xếp theo thời gian tạo (mới nhất trước)
                        reminders.sort((r1, r2) -> Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));
                        callback.onSuccess(reminders);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    pendingRequests[0]--;
                    if (pendingRequests[0] == 0) {
                        callback.onError("Lỗi khi lấy chi tiết nhắc nhở: " + databaseError.getMessage());
                    }
                }
            });
        }
    }
}
