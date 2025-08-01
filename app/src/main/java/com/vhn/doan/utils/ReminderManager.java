package com.vhn.doan.utils;

import android.content.Context;
import android.util.Log;

import com.vhn.doan.data.Reminder;
import com.vhn.doan.data.repository.ReminderRepository;
import com.vhn.doan.data.repository.ReminderRepositoryImpl;
import com.vhn.doan.services.ReminderService;

import java.util.List;

/**
 * Manager để quản lý việc khởi động lại reminders khi app mở
 */
public class ReminderManager {

    private static final String TAG = "ReminderManager";

    /**
     * Khởi động lại tất cả reminders đang active
     */
    public static void restartAllActiveReminders(Context context) {
        Log.d(TAG, "Khởi động lại tất cả reminders đang active");

        ReminderRepository reminderRepository = new ReminderRepositoryImpl();
        reminderRepository.getAllReminders(new ReminderRepository.RepositoryCallback<List<Reminder>>() {
            @Override
            public void onSuccess(List<Reminder> reminders) {
                if (reminders != null && !reminders.isEmpty()) {
                    int activeCount = 0;
                    for (Reminder reminder : reminders) {
                        if (reminder.isActive() && !reminder.isCompleted()) {
                            // Khởi động lại reminder
                            ReminderService reminderService = new ReminderService(context);
                            reminderService.scheduleReminder(reminder);
                            activeCount++;
                            Log.d(TAG, "Đã khởi động lại reminder: " + reminder.getTitle());
                        }
                    }
                    Log.d(TAG, "Đã khởi động lại " + activeCount + " reminders");
                } else {
                    Log.d(TAG, "Không có reminders nào để khởi động lại");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Lỗi khi lấy danh sách reminders: " + error);
            }
        });
    }

    /**
     * Kiểm tra và khởi động lại reminders đã bị miss
     */
    public static void checkAndRestartMissedReminders(Context context) {
        Log.d(TAG, "Kiểm tra và khởi động lại reminders đã bị miss");

        ReminderRepository reminderRepository = new ReminderRepositoryImpl();
        reminderRepository.getAllReminders(new ReminderRepository.RepositoryCallback<List<Reminder>>() {
            @Override
            public void onSuccess(List<Reminder> reminders) {
                if (reminders != null && !reminders.isEmpty()) {
                    int missedCount = 0;
                    long currentTime = System.currentTimeMillis();

                    for (Reminder reminder : reminders) {
                        if (reminder.isActive() && !reminder.isCompleted()) {
                            // Kiểm tra xem reminder có bị miss không
                            if (reminder.getReminderTime() != null && 
                                reminder.getReminderTime() <= currentTime &&
                                reminder.getReminderTime() > currentTime - (24 * 60 * 60 * 1000)) { // Trong 24h qua
                                
                                // Hiển thị thông báo cho reminder đã bị miss
                                com.vhn.doan.services.NotificationService.showReminderNotification(
                                    context,
                                    "[BỎ LỠ] " + reminder.getTitle(),
                                    reminder.getDescription(),
                                    reminder.getId()
                                );
                                missedCount++;
                                Log.d(TAG, "Đã hiển thị thông báo cho reminder bị miss: " + reminder.getTitle());
                            }
                        }
                    }
                    Log.d(TAG, "Đã xử lý " + missedCount + " reminders bị miss");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Lỗi khi kiểm tra reminders bị miss: " + error);
            }
        });
    }
}