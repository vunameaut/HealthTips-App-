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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.vhn.doan.R;
import com.vhn.doan.data.Reminder;
import com.vhn.doan.services.ReminderService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity hiển thị giao diện báo thức khi đến giờ nhắc nhở
 * Thiết kế như một ứng dụng báo thức thực sự
 */
public class AlarmActivity extends AppCompatActivity {

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

    private Reminder reminder;
    private ReminderService reminderService;

    private static final int AUTO_DISMISS_DELAY = 60000; // Tự động tắt sau 1 phút

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cấu hình để hiển thị trên màn hình khóa
        setupWindowFlags();

        setContentView(R.layout.activity_alarm);

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
        String reminderId = intent.getStringExtra("reminder_id");
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("message");

        // Hiển thị thông tin
        tvTitle.setText(title != null ? title : "Nhắc nhở sức khỏe");
        tvDescription.setText(description != null ? description : "Đã đến giờ thực hiện nhắc nhở");

        // Hiển thị thời gian hiện tại
        Date now = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());

        tvTime.setText(timeFormat.format(now));
        tvDate.setText(dateFormat.format(now));

        reminderService = new ReminderService(this);
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
            e.printStackTrace();
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

    private void dismissAlarm() {
        stopAlarmSound();
        stopVibration();
        if (handler != null && dismissRunnable != null) {
            handler.removeCallbacks(dismissRunnable);
        }
        finish();
    }

    private void snoozeAlarm() {
        stopAlarmSound();
        stopVibration();

        // Lên lịch báo lại sau 5 phút
        Intent intent = getIntent();
        String reminderId = intent.getStringExtra("reminder_id");

        // Tạo reminder mới cho snooze
        long snoozeTime = System.currentTimeMillis() + (5 * 60 * 1000); // 5 phút
        Reminder snoozeReminder = new Reminder();
        snoozeReminder.setId(reminderId + "_snooze_" + System.currentTimeMillis());
        snoozeReminder.setTitle(intent.getStringExtra("title"));
        snoozeReminder.setDescription(intent.getStringExtra("message"));
        snoozeReminder.setReminderTime(snoozeTime);
        snoozeReminder.setActive(true);

        if (reminderService != null) {
            reminderService.scheduleReminder(snoozeReminder);
        }

        if (handler != null && dismissRunnable != null) {
            handler.removeCallbacks(dismissRunnable);
        }

        finish();
    }

    private void stopAlarmSound() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
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
    }

    @Override
    public void onBackPressed() {
        // Không cho phép back khi đang báo thức
        // Phải bấm dismiss hoặc snooze
    }

    // Phương thức static để khởi động AlarmActivity
    public static void startAlarm(Context context, String reminderId, String title, String message) {
        Intent intent = new Intent(context, AlarmActivity.class);
        intent.putExtra("reminder_id", reminderId);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                       Intent.FLAG_ACTIVITY_CLEAR_TOP |
                       Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
}
