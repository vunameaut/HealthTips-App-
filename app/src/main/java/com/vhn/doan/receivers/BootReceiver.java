package com.vhn.doan.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vhn.doan.data.Reminder;
import com.vhn.doan.data.repository.ReminderRepository;
import com.vhn.doan.data.repository.ReminderRepositoryImpl;
import com.vhn.doan.services.ReminderService;
import com.vhn.doan.utils.UserSessionManager;

import java.util.List;

/**
 * BroadcastReceiver để khởi động lại các reminder sau khi thiết bị boot
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) ||
            Intent.ACTION_PACKAGE_REPLACED.equals(action)) {

            rescheduleAllReminders(context);
        }
    }

    /**
     * Lên lịch lại tất cả các reminder đang hoạt động
     */
    private void rescheduleAllReminders(Context context) {
        UserSessionManager userSessionManager = new UserSessionManager(context);
        String userId = userSessionManager.getCurrentUserId();

        if (userId == null || userId.isEmpty()) {
            return; // Không có user đăng nhập
        }

        ReminderRepository reminderRepository = new ReminderRepositoryImpl();
        ReminderService reminderService = new ReminderService(context);

        // Lấy tất cả reminder đang hoạt động của user
        reminderRepository.getActiveReminders(userId, new ReminderRepository.RepositoryCallback<List<Reminder>>() {
            @Override
            public void onSuccess(List<Reminder> reminders) {
                // Lên lịch lại tất cả reminder
                reminderService.rescheduleAllActiveReminders(reminders);
            }

            @Override
            public void onError(String error) {
                // Log lỗi nhưng không làm gì thêm
                android.util.Log.e("BootReceiver", "Lỗi khi lấy reminder: " + error);
            }
        });
    }
}
