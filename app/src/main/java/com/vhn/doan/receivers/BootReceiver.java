package com.vhn.doan.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.vhn.doan.data.Reminder;
import com.vhn.doan.data.repository.ReminderRepository;
import com.vhn.doan.data.repository.ReminderRepositoryImpl;
import com.vhn.doan.services.ReminderService;
import com.vhn.doan.utils.UserSessionManager;

import java.util.List;

/**
 * BootReceiver để khởi động lại tất cả reminder active sau khi khởi động lại máy
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "BootReceiver triggered with action: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) ||
            Intent.ACTION_PACKAGE_REPLACED.equals(action)) {

            Log.d(TAG, "Device booted or app updated - rescheduling all active reminders");
            rescheduleAllActiveReminders(context);
        }
    }

    private void rescheduleAllActiveReminders(Context context) {
        // Lấy user session
        UserSessionManager userSessionManager = new UserSessionManager(context);
        String userId = userSessionManager.getCurrentUserId();

        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "No user logged in - skipping reminder rescheduling");
            return;
        }

        // Lấy tất cả reminder active và reschedule
        ReminderRepository reminderRepository = new ReminderRepositoryImpl();
        reminderRepository.getActiveReminders(userId, new ReminderRepository.RepositoryCallback<List<Reminder>>() {
            @Override
            public void onSuccess(List<Reminder> reminders) {
                Log.d(TAG, "Found " + reminders.size() + " active reminders to reschedule");

                ReminderService reminderService = new ReminderService(context);
                int rescheduledCount = 0;

                for (Reminder reminder : reminders) {
                    if (reminder.isActive() && reminder.getReminderTime() != null) {
                        // Chỉ reschedule những reminder trong tương lai
                        if (reminder.getReminderTime() > System.currentTimeMillis()) {
                            reminderService.scheduleReminder(reminder);
                            rescheduledCount++;
                            Log.d(TAG, "Rescheduled reminder: " + reminder.getTitle());
                        }
                    }
                }

                Log.d(TAG, "Successfully rescheduled " + rescheduledCount + " reminders");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load active reminders: " + error);
            }
        });
    }

    /**
     * Public static method để có thể gọi từ các class khác
     */
    public static void rescheduleAllReminders(Context context) {
        BootReceiver receiver = new BootReceiver();
        receiver.rescheduleAllActiveReminders(context);
    }
}
