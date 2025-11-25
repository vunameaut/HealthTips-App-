package com.vhn.doan.services;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.data.Reminder;
import com.vhn.doan.utils.Constants;
import com.vhn.doan.utils.UserSessionManager;

import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Class quản lý các reminder trong ứng dụng
 * - Khởi động ReminderForegroundService
 * - Khởi động lại reminders đang hoạt động
 * - Kiểm tra và hiển thị reminders bị miss
 */
@Singleton
public class ReminderManager {
    private static final String TAG = "ReminderManager";
    private final UserSessionManager userSessionManager;

    // Flag để disable missed reminder notifications nếu người dùng không muốn
    private static final boolean ENABLE_MISSED_REMINDER_NOTIFICATIONS = false;

    // Flag để disable foreground service (tránh thông báo trống)
    // Sử dụng AlarmManager trực tiếp thay vì foreground service
    private static final boolean ENABLE_FOREGROUND_SERVICE = false;

    @Inject
    public ReminderManager(UserSessionManager userSessionManager) {
        this.userSessionManager = userSessionManager;
    }

    /**
     * Khởi động ReminderForegroundService để đảm bảo reminders hoạt động
     * DISABLED: Tắt foreground service để tránh thông báo trống
     */
    public void startReminderService(Context context) {
        if (!ENABLE_FOREGROUND_SERVICE) {
            Log.d(TAG, "ReminderForegroundService đã bị vô hiệu hóa để tránh thông báo trống");
            return;
        }

        try {
            Intent serviceIntent = new Intent(context, ReminderForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            Log.d(TAG, "ReminderForegroundService đã được khởi động");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi khởi động ReminderForegroundService: " + e.getMessage());
        }
    }

    /**
     * Khởi động lại tất cả các reminder đang hoạt động
     * Được gọi khi app mở hoặc resume để đảm bảo reminders luôn được kích hoạt
     */
    public void restartAllReminders(Context context) {
        String userId = userSessionManager.getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Không thể khởi động lại reminders: User không đăng nhập");
            return;
        }

        DatabaseReference remindersRef = FirebaseDatabase.getInstance()
                .getReference(Constants.REMINDERS_NODE)
                .child(userId);

        remindersRef.orderByChild("isActive").equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot reminderSnapshot : dataSnapshot.getChildren()) {
                            Reminder reminder = reminderSnapshot.getValue(Reminder.class);
                            if (reminder != null && reminder.isActive()) {
                                // Đặt lại reminder trong ReminderService
                                Intent intent = new Intent(context, ReminderService.class);
                                intent.setAction(Constants.ACTION_SET_REMINDER);
                                intent.putExtra(Constants.EXTRA_REMINDER, reminder);
                                context.startService(intent);

                                Log.d(TAG, "Khởi động lại reminder: " + reminder.getTitle());
                            }
                        }
                        Log.d(TAG, "Đã khởi động lại tất cả reminders đang hoạt động");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Lỗi khi khởi động lại reminders: " + databaseError.getMessage());
                    }
                });
    }

    /**
     * Kiểm tra và hiển thị các reminders đã bị miss
     * Được gọi khi app mở để thông báo cho người dùng về các reminders đã bỏ lỡ
     * DISABLED: Chức năng này đã bị tắt để tránh thông báo test
     */
    public void checkAndShowMissedReminders(Context context) {
        if (!ENABLE_MISSED_REMINDER_NOTIFICATIONS) {
            Log.d(TAG, "Missed reminder notifications đã bị vô hiệu hóa");
            return;
        }

        String userId = userSessionManager.getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Không thể kiểm tra reminders bị miss: User không đăng nhập");
            return;
        }

        // Lấy thời gian hiện tại để so sánh với thời gian của reminder
        final long currentTime = Calendar.getInstance().getTimeInMillis();
        // Lấy thời gian từ 24 giờ trước để chỉ kiểm tra reminders gần đây
        final long oneDayAgo = currentTime - (24 * 60 * 60 * 1000);

        Query missedRemindersQuery = FirebaseDatabase.getInstance()
                .getReference(Constants.REMINDERS_NODE)
                .child(userId)
                .orderByChild("reminderTime")
                .startAt(oneDayAgo)
                .endAt(currentTime);

        missedRemindersQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot reminderSnapshot : dataSnapshot.getChildren()) {
                    Reminder reminder = reminderSnapshot.getValue(Reminder.class);
                    if (reminder != null && reminder.isActive() &&
                            reminder.getReminderTime() < currentTime &&
                            reminder.getReminderTime() > oneDayAgo) {

                        // Hiển thị thông báo cho reminder bị miss
                        Intent intent = new Intent(context, ReminderService.class);
                        intent.setAction(Constants.ACTION_SHOW_MISSED_REMINDER);
                        intent.putExtra(Constants.EXTRA_REMINDER, reminder);
                        context.startService(intent);

                        Log.d(TAG, "Phát hiện reminder bị miss: " + reminder.getTitle());
                    }
                }
                Log.d(TAG, "Đã kiểm tra xong các reminders bị miss");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Lỗi khi kiểm tra reminders bị miss: " + databaseError.getMessage());
            }
        });
    }
}
