package com.vhn.doan.presentation.reminder;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.vhn.doan.R;
import com.vhn.doan.data.Reminder;
import com.vhn.doan.data.repository.ReminderRepository;
import com.vhn.doan.data.repository.ReminderRepositoryImpl;
import com.vhn.doan.services.ReminderService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity hiển thị giao diện báo thức khi đến giờ nhắc nhở
 * Thiết kế như một ứng dụng báo thức thực sự
 */
public class AlarmActivity extends AppCompatActivity {

    private static final String TAG = "AlarmActivity";

    // Constants cho Intent extras
    public static final String EXTRA_REMINDER_ID = "reminder_id";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_MESSAGE = "message";

    private TextView tvTitle, tvDescription, tvTime, tvDate;
    private Button btnDismiss, btnSnooze;
    private ImageView ivAlarmIcon;

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private Handler handler;
    private Runnable dismissRunnable;

    private String reminderId;
    private String title;
    private String message;
    private ReminderService reminderService;
    private ReminderRepository reminderRepository;

    private static final int AUTO_DISMISS_DELAY = 60000; // Tự động tắt sau 1 phút

    /**
     * Static method để khởi động AlarmActivity từ các component khác
     */
    public static void startAlarm(Context context, String reminderId, String title, String message) {
        try {
            Log.d(TAG, "🚨 Khởi động AlarmActivity: " + title);

            Intent intent = new Intent(context, AlarmActivity.class);
            intent.putExtra(EXTRA_REMINDER_ID, reminderId);
            intent.putExtra(EXTRA_TITLE, title);
            intent.putExtra(EXTRA_MESSAGE, message);

            // Đảm bảo Activity có thể khởi động từ background
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                           Intent.FLAG_ACTIVITY_CLEAR_TOP |
                           Intent.FLAG_ACTIVITY_SINGLE_TOP);

            context.startActivity(intent);

        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi khởi động AlarmActivity", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cấu hình để hiển thị trên màn hình khóa
        setupWindowFlags();

        setContentView(R.layout.activity_alarm);

        // Khởi tạo repository
        reminderRepository = new ReminderRepositoryImpl();
        reminderService = new ReminderService(this);

        initializeViews();
        setupReminder();
        setupButtons();
        startAlarmSound();
        startVibration();
        setupAutoDismiss();
    }

    private void setupWindowFlags() {
        // Hiển thị trên màn hình khóa và đánh thức thiết bị
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }

        // Đảm bảo activity hiển thị đầy màn hình
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void initializeViews() {
        tvTitle = findViewById(R.id.tv_alarm_title);
        tvDescription = findViewById(R.id.tv_alarm_description);
        tvTime = findViewById(R.id.tv_alarm_time);
        tvDate = findViewById(R.id.tv_alarm_date);
        btnDismiss = findViewById(R.id.btn_dismiss);
        btnSnooze = findViewById(R.id.btn_snooze);
        ivAlarmIcon = findViewById(R.id.iv_alarm_icon);
    }

    private void setupReminder() {
        // Lấy thông tin reminder từ Intent
        Intent intent = getIntent();
        reminderId = intent.getStringExtra(EXTRA_REMINDER_ID);
        title = intent.getStringExtra(EXTRA_TITLE);
        message = intent.getStringExtra(EXTRA_MESSAGE);

        Log.d(TAG, "📱 AlarmActivity nhận dữ liệu: " + title + " (ID: " + reminderId + ")");

        // Hiển thị thông tin
        tvTitle.setText(title != null ? title : getString(R.string.reminder_title_default));
        tvDescription.setText(message != null ? message : getString(R.string.reminder_message_default));

        // Hiển thị thời gian hiện tại
        Date now = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());

        tvTime.setText(timeFormat.format(now));
        tvDate.setText(dateFormat.format(now));
    }

    private void setupButtons() {
        btnDismiss.setOnClickListener(v -> dismissAlarm());
        btnSnooze.setOnClickListener(v -> snoozeAlarm());
    }

    private void startAlarmSound() {
        try {
            // Lấy URI âm thanh từ reminder hoặc sử dụng mặc định
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (soundUri == null) {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, soundUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();

            // Thiết lập âm lượng
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            int volume = (int) (maxVolume * 0.8); // 80% âm lượng
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0);

        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi phát âm thanh báo thức", e);
        }
    }

    private void startVibration() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            // Tạo pattern rung: rung 1s, nghỉ 0.5s, lặp lại
            long[] pattern = {0, 1000, 500};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createWaveform(pattern, 0));
            } else {
                vibrator.vibrate(pattern, 0);
            }
        }
    }

    private void setupAutoDismiss() {
        handler = new Handler();
        dismissRunnable = this::dismissAlarm;
        handler.postDelayed(dismissRunnable, AUTO_DISMISS_DELAY);
    }

    /**
     * FIXED: Tắt thực sự nhắc nhở trong database khi ấn nút "Tắt"
     */
    private void dismissAlarm() {
        Log.d(TAG, "✅ Người dùng ấn Tắt - Bắt đầu tắt nhắc nhở: " + reminderId);

        stopAlarmSound();
        stopVibration();

        if (handler != null && dismissRunnable != null) {
            handler.removeCallbacks(dismissRunnable);
        }

        // QUAN TRỌNG: Tắt nhắc nhở trong database
        if (reminderId != null && reminderRepository != null) {
            disableReminderInDatabase();
        } else {
            Log.w(TAG, "⚠️ Không thể tắt nhắc nhở - thiếu reminderId hoặc repository");
            finish();
        }
    }

    /**
     * Tắt nhắc nhở trong database và gửi broadcast cập nhật UI
     */
    private void disableReminderInDatabase() {
        Log.d(TAG, "🔄 Bắt đầu tắt nhắc nhở trong database: " + reminderId);

        reminderRepository.getReminderById(reminderId, new ReminderRepository.RepositoryCallback<Reminder>() {
            @Override
            public void onSuccess(Reminder reminder) {
                if (reminder != null) {
                    Log.d(TAG, "📋 Tìm thấy reminder: " + reminder.getTitle() + " - Current active: " + reminder.isActive());

                    // Tắt nhắc nhở
                    reminder.setActive(false);
                    reminder.setUpdatedAt(System.currentTimeMillis());

                    // Cập nhật trong database
                    reminderRepository.updateReminder(reminder, new ReminderRepository.RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Log.d(TAG, "✅ Đã tắt nhắc nhở thành công trong database");

                            // Hủy scheduling
                            if (reminderService != null) {
                                reminderService.cancelReminder(reminderId);
                                Log.d(TAG, "✅ Đã hủy scheduling alarm");
                            }

                            // Gửi broadcast để cập nhật UI danh sách nhắc nhở
                            sendReminderStatusBroadcast(reminder, "dismissed_by_user");

                            // Gửi thêm broadcast REFRESH để force reload toàn bộ list
                            Intent refreshIntent = new Intent("REMINDER_LIST_REFRESH");
                            refreshIntent.putExtra("reminder_id", reminderId);
                            refreshIntent.putExtra("refresh_reason", "reminder_dismissed");
                            sendBroadcast(refreshIntent);
                            Log.d(TAG, "📤 Đã gửi broadcast REMINDER_LIST_REFRESH");

                            // Delay lớn hơn để đảm bảo database update và broadcast được xử lý
                            // INCREASED: Tăng delay để Firebase kịp sync dữ liệu
                            new Handler().postDelayed(() -> {
                                finish();
                            }, 1000); // 1000ms delay - đủ thời gian cho Firebase sync
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "❌ Lỗi khi cập nhật reminder trong database: " + error);
                            // Vẫn đóng activity dù có lỗi
                            finish();
                        }
                    });
                } else {
                    Log.w(TAG, "⚠️ Không tìm thấy reminder để tắt");
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Lỗi khi lấy thông tin reminder: " + error);
                // Vẫn đóng activity dù có lỗi
                finish();
            }
        });
    }

    /**
     * Gửi broadcast để thông báo thay đổi trạng thái reminder
     */
    private void sendReminderStatusBroadcast(Reminder reminder, String reason) {
        try {
            Intent intent = new Intent("REMINDER_STATUS_CHANGED");
            intent.putExtra("reminder_id", reminder.getId());
            intent.putExtra("reminder_title", reminder.getTitle());
            intent.putExtra("is_active", reminder.isActive());
            intent.putExtra("reason", reason);

            sendBroadcast(intent);
            Log.d(TAG, "📡 Đã gửi broadcast REMINDER_STATUS_CHANGED: " + reason);

            // Gửi thêm broadcast force refresh UI
            Intent refreshIntent = new Intent("REMINDER_LIST_REFRESH");
            refreshIntent.putExtra("refresh_reason", "reminder_dismissed");
            sendBroadcast(refreshIntent);
            Log.d(TAG, "📡 Đã gửi broadcast REMINDER_LIST_REFRESH");

        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi gửi broadcast: " + e.getMessage());
        }
    }

    private void snoozeAlarm() {
        Log.d(TAG, "⏰ Người dùng ấn Báo lại - Hiển thị dialog chọn thời gian");

        stopAlarmSound();
        stopVibration();

        // Hiển thị dialog để người dùng chọn thời gian snooze
        showSnoozeDialog();
    }

    /**
     * Hiển thị dialog để chọn thời gian báo lại
     */
    private void showSnoozeDialog() {
        String[] snoozeOptions = {
            "5 phút",
            "10 phút",
            "15 phút",
            "30 phút"
        };

        int[] snoozeMinutes = {5, 10, 15, 30};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Báo lại sau bao lâu?")
                .setItems(snoozeOptions, (dialog, which) -> {
                    int selectedMinutes = snoozeMinutes[which];
                    scheduleSnooze(selectedMinutes);
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    // Người dùng hủy, quay lại alarm
                    startAlarmSound();
                    startVibration();
                })
                .setOnCancelListener(dialog -> {
                    // Người dùng nhấn back, quay lại alarm
                    startAlarmSound();
                    startVibration();
                })
                .show();
    }

    /**
     * Lên lịch snooze với thời gian đã chọn
     * FIXED: Xóa reminder hiện tại và tạo reminder mới cho snooze
     */
    private void scheduleSnooze(int minutes) {
        Log.d(TAG, "⏰ Lên lịch báo lại sau " + minutes + " phút");

        if (reminderId == null) {
            Log.w(TAG, "⚠️ reminderId is null, cannot snooze");
            finish();
            return;
        }

        // Tải reminder từ database để có đầy đủ thông tin
        reminderRepository.getReminderById(reminderId, new ReminderRepository.RepositoryCallback<Reminder>() {
            @Override
            public void onSuccess(Reminder originalReminder) {
                if (originalReminder != null) {
                    long snoozeTime = System.currentTimeMillis() + (minutes * 60 * 1000);

                    // Tạo reminder mới cho snooze và LƯU VÀO DATABASE
                    Reminder snoozeReminder = new Reminder();
                    // Generate unique ID for snooze reminder
                    String snoozeId = "snooze_" + originalReminder.getId() + "_" + System.currentTimeMillis();
                    snoozeReminder.setId(snoozeId);
                    snoozeReminder.setUserId(originalReminder.getUserId());
                    snoozeReminder.setTitle(originalReminder.getTitle() + " (Báo lại)");
                    snoozeReminder.setDescription(originalReminder.getDescription());
                    snoozeReminder.setReminderTime(snoozeTime);
                    snoozeReminder.setActive(true);
                    snoozeReminder.setRepeatType(Reminder.RepeatType.NO_REPEAT);

                    // Copy alarm settings
                    snoozeReminder.setSoundId(originalReminder.getSoundId());
                    snoozeReminder.setSoundName(originalReminder.getSoundName());
                    snoozeReminder.setSoundUri(originalReminder.getSoundUri());
                    snoozeReminder.setVibrate(originalReminder.isVibrate());
                    snoozeReminder.setAlarmStyle(originalReminder.isAlarmStyle());
                    snoozeReminder.setVolume(originalReminder.getVolume());
                    snoozeReminder.setSnoozeMinutes(originalReminder.getSnoozeMinutes());

                    snoozeReminder.setCreatedAt(System.currentTimeMillis());
                    snoozeReminder.setUpdatedAt(System.currentTimeMillis());

                    // BƯỚC 1: XÓA REMINDER GỐC TRƯỚC
                    Log.d(TAG, "🗑️ Xóa reminder gốc: " + originalReminder.getTitle());
                    reminderRepository.deleteReminder(originalReminder.getId(), new ReminderRepository.RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Log.d(TAG, "✅ Đã xóa reminder gốc");

                            // Hủy alarm của reminder gốc
                            if (reminderService != null) {
                                reminderService.cancelReminder(originalReminder.getId());
                                Log.d(TAG, "✅ Đã hủy alarm của reminder gốc");
                            }

                            // BƯỚC 2: TẠO VÀ LƯU SNOOZE REMINDER
                            reminderRepository.addReminder(snoozeReminder, new ReminderRepository.RepositoryCallback<String>() {
                                @Override
                                public void onSuccess(String newReminderId) {
                                    // Schedule alarm cho snooze
                                    if (reminderService != null) {
                                        reminderService.scheduleReminder(snoozeReminder);
                                        Log.d(TAG, "✅ Đã lưu và lên lịch snooze sau " + minutes + " phút với ID: " + newReminderId);

                                        // Gửi broadcast để refresh UI
                                        Intent refreshIntent = new Intent("REMINDER_LIST_REFRESH");
                                        refreshIntent.putExtra("reminder_id", originalReminder.getId());
                                        refreshIntent.putExtra("refresh_reason", "reminder_snoozed");
                                        sendBroadcast(refreshIntent);
                                        Log.d(TAG, "📤 Đã gửi broadcast REMINDER_LIST_REFRESH");

                                        // Hiển thị thông báo
                                        runOnUiThread(() -> {
                                            android.widget.Toast.makeText(AlarmActivity.this,
                                                    "Sẽ nhắc lại sau " + minutes + " phút",
                                                    android.widget.Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                    finish();
                                }

                                @Override
                                public void onError(String error) {
                                    Log.e(TAG, "❌ Lỗi khi lưu snooze reminder: " + error);
                                    // Fallback: schedule without saving to database
                                    if (reminderService != null) {
                                        reminderService.scheduleReminder(snoozeReminder);
                                    }
                                    finish();
                                }
                            });
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "❌ Lỗi khi xóa reminder gốc: " + error);
                            // Nếu không xóa được reminder gốc, vẫn tạo snooze reminder
                            // (ít nhất người dùng vẫn được nhắc lại)
                            reminderRepository.addReminder(snoozeReminder, new ReminderRepository.RepositoryCallback<String>() {
                                @Override
                                public void onSuccess(String reminderId) {
                                    if (reminderService != null) {
                                        reminderService.scheduleReminder(snoozeReminder);
                                        Log.d(TAG, "⚠️ Đã tạo snooze reminder dù không xóa được reminder gốc");
                                    }
                                    finish();
                                }

                                @Override
                                public void onError(String error2) {
                                    Log.e(TAG, "❌ Lỗi khi lưu snooze reminder sau khi xóa thất bại: " + error2);
                                    finish();
                                }
                            });
                        }
                    });
                } else {
                    Log.w(TAG, "⚠️ Không tìm thấy reminder gốc");
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Lỗi khi tải reminder: " + error);
                finish();
            }
        });
    }

    private void stopAlarmSound() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi dừng âm thanh", e);
        }
    }

    private void stopVibration() {
        try {
            if (vibrator != null) {
                vibrator.cancel();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi dừng rung", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAlarmSound();
        stopVibration();

        if (handler != null && dismissRunnable != null) {
            handler.removeCallbacks(dismissRunnable);
        }

        Log.d(TAG, "🔚 AlarmActivity đã được destroy");
    }

    @Override
    public void onBackPressed() {
        // Ngăn người dùng thoát bằng nút back - phải bấm dismiss hoặc snooze
        Log.d(TAG, "🔙 Người dùng ấn back - Không cho phép thoát");
    }
}
