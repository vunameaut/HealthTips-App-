package com.vhn.doan.presentation.reminder;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.vhn.doan.R;
import com.vhn.doan.data.Reminder;
import com.vhn.doan.data.repository.ReminderRepository;
import com.vhn.doan.data.repository.ReminderRepositoryImpl;
import com.vhn.doan.services.ReminderService;
import com.vhn.doan.utils.UserSessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Full screen Activity để tạo hoặc chỉnh sửa nhắc nhở
 * Thay thế ReminderDialog để có trải nghiệm tốt hơn
 */
public class ReminderEditorActivity extends AppCompatActivity {

    private static final String TAG = "ReminderEditor";
    public static final String EXTRA_REMINDER_ID = "reminder_id";
    public static final String EXTRA_IS_EDIT_MODE = "is_edit_mode";
    private static final int REQUEST_CODE_SOUND_PICKER = 1001;

    // UI Components - Basic Info
    private MaterialToolbar toolbar;
    private TextInputEditText etTitle;
    private TextInputEditText etDescription;
    private SwitchMaterial swActive;

    // UI Components - Time & Repeat
    private TextView tvSelectedDate;
    private TextView tvSelectedTime;
    private Button btnSelectDate;
    private Button btnSelectTime;
    private Spinner spRepeatType;

    // UI Components - Alarm Settings
    private TextView tvSelectedSound;
    private MaterialButton btnSelectSound;
    private SwitchMaterial swVibrate;
    private SwitchMaterial swAlarmStyle;
    private Slider sliderVolume;
    private TextView tvVolumeValue;
    private Spinner spSnoozeMinutes;

    // UI Components - Actions
    private MaterialButton btnSave;
    private MaterialButton btnCancel;

    // Data
    private Reminder reminder;
    private boolean isEditMode;
    private Calendar selectedDateTime;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    // Sound Selection
    private String selectedSoundId = "default_alarm";
    private String selectedSoundName = "Báo thức mặc định";
    private String selectedSoundUri;

    // Repository
    private ReminderRepository reminderRepository;
    private ReminderService reminderService;
    private UserSessionManager userSessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_editor);

        // Initialize
        reminderRepository = new ReminderRepositoryImpl();
        reminderService = new ReminderService(this);
        userSessionManager = new UserSessionManager(this);
        selectedDateTime = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        initializeViews();
        setupToolbar();
        loadReminderData();
        setupViews();
        populateFields();
    }

    private void initializeViews() {
        // Toolbar
        toolbar = findViewById(R.id.toolbar_reminder_editor);

        // Basic Info
        etTitle = findViewById(R.id.et_reminder_title);
        etDescription = findViewById(R.id.et_reminder_description);
        swActive = findViewById(R.id.sw_reminder_active);

        // Time & Repeat
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        tvSelectedTime = findViewById(R.id.tv_selected_time);
        btnSelectDate = findViewById(R.id.btn_select_date);
        btnSelectTime = findViewById(R.id.btn_select_time);
        spRepeatType = findViewById(R.id.sp_repeat_type);

        // Alarm Settings
        tvSelectedSound = findViewById(R.id.tv_selected_sound);
        btnSelectSound = findViewById(R.id.btn_select_sound);
        swVibrate = findViewById(R.id.sw_vibrate);
        swAlarmStyle = findViewById(R.id.sw_alarm_style);
        sliderVolume = findViewById(R.id.slider_volume);
        tvVolumeValue = findViewById(R.id.tv_volume_value);
        spSnoozeMinutes = findViewById(R.id.sp_snooze_minutes);

        // Actions
        btnSave = findViewById(R.id.btn_save_reminder);
        btnCancel = findViewById(R.id.btn_cancel_reminder);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void loadReminderData() {
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra(EXTRA_IS_EDIT_MODE, false);
        String reminderId = intent.getStringExtra(EXTRA_REMINDER_ID);

        if (isEditMode && reminderId != null) {
            // Load reminder from database
            toolbar.setTitle("Chỉnh sửa nhắc nhở");
            loadReminderFromDatabase(reminderId);
        } else {
            // Create new reminder
            toolbar.setTitle("Tạo nhắc nhở mới");
            reminder = new Reminder();
            reminder.setUserId(userSessionManager.getCurrentUserId());
            reminder.setActive(true);
            reminder.setVibrate(true);
            reminder.setAlarmStyle(true);
            reminder.setVolume(80);
            reminder.setSnoozeMinutes(5);
            reminder.setRepeatType(Reminder.RepeatType.NO_REPEAT);
        }
    }

    private void loadReminderFromDatabase(String reminderId) {
        reminderRepository.getReminderById(reminderId, new ReminderRepository.RepositoryCallback<Reminder>() {
            @Override
            public void onSuccess(Reminder loadedReminder) {
                if (loadedReminder != null) {
                    reminder = loadedReminder;
                    if (reminder.getReminderTime() != null) {
                        selectedDateTime.setTimeInMillis(reminder.getReminderTime());
                    }
                    if (reminder.getSoundId() != null) {
                        selectedSoundId = reminder.getSoundId();
                        selectedSoundName = reminder.getSoundName();
                        selectedSoundUri = reminder.getSoundUri();
                    }
                    runOnUiThread(() -> populateFields());
                } else {
                    showError("Không tìm thấy nhắc nhở");
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                showError("Lỗi tải dữ liệu: " + error);
                finish();
            }
        });
    }

    private void setupViews() {
        // Setup repeat type spinner
        ArrayAdapter<CharSequence> repeatAdapter = ArrayAdapter.createFromResource(
                this, R.array.repeat_types, android.R.layout.simple_spinner_item);
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRepeatType.setAdapter(repeatAdapter);

        // Setup snooze minutes spinner
        ArrayAdapter<CharSequence> snoozeAdapter = ArrayAdapter.createFromResource(
                this, R.array.snooze_options, android.R.layout.simple_spinner_item);
        snoozeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSnoozeMinutes.setAdapter(snoozeAdapter);

        // Setup volume slider
        sliderVolume.setValueFrom(0f);
        sliderVolume.setValueTo(100f);
        sliderVolume.setValue(80f);
        sliderVolume.addOnChangeListener((slider, value, fromUser) -> {
            tvVolumeValue.setText((int) value + "%");
        });

        // Setup click listeners
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectTime.setOnClickListener(v -> showTimePicker());
        btnSelectSound.setOnClickListener(v -> showSoundPicker());
        btnSave.setOnClickListener(v -> saveReminder());
        btnCancel.setOnClickListener(v -> finish());

        // Setup alarm style switch listener để hiển thị/ẩn cài đặt âm thanh
        swAlarmStyle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateAlarmSettingsVisibility(isChecked);
        });
    }

    private void updateAlarmSettingsVisibility(boolean isAlarmStyle) {
        // Hiển thị các cài đặt âm thanh chi tiết khi chọn alarm style
        int visibility = isAlarmStyle ? View.VISIBLE : View.GONE;
        findViewById(R.id.layout_sound_settings).setVisibility(visibility);
        findViewById(R.id.layout_volume_settings).setVisibility(visibility);
        findViewById(R.id.layout_snooze_settings).setVisibility(visibility);
        findViewById(R.id.layout_vibrate_settings).setVisibility(visibility);
    }

    private void populateFields() {
        if (reminder != null) {
            etTitle.setText(reminder.getTitle());
            etDescription.setText(reminder.getDescription());
            swActive.setChecked(reminder.isActive());

            // Repeat type
            spRepeatType.setSelection(reminder.getRepeatType());

            // Alarm settings
            swVibrate.setChecked(reminder.isVibrate());
            swAlarmStyle.setChecked(reminder.isAlarmStyle());
            sliderVolume.setValue(reminder.getVolume());
            tvVolumeValue.setText(reminder.getVolume() + "%");

            // Snooze minutes
            int snoozePosition = getSnoozePosition(reminder.getSnoozeMinutes());
            spSnoozeMinutes.setSelection(snoozePosition);

            // Update sound selection
            updateSoundDisplay();

            // Update alarm settings visibility
            updateAlarmSettingsVisibility(reminder.isAlarmStyle());
        }

        updateDateTimeDisplay();
    }

    private int getSnoozePosition(int minutes) {
        switch (minutes) {
            case 5: return 0;
            case 10: return 1;
            case 15: return 2;
            case 30: return 3;
            default: return 0;
        }
    }

    private int getSnoozeMinutesFromPosition(int position) {
        switch (position) {
            case 0: return 5;
            case 1: return 10;
            case 2: return 15;
            case 3: return 30;
            default: return 5;
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    selectedDateTime.set(Calendar.SECOND, 0);
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void showSoundPicker() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Chọn âm thanh nhắc nhở");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                selectedSoundUri != null ? Uri.parse(selectedSoundUri) : null);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);

        startActivityForResult(intent, REQUEST_CODE_SOUND_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SOUND_PICKER && resultCode == RESULT_OK) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                selectedSoundUri = uri.toString();
                Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
                selectedSoundName = ringtone.getTitle(this);
                selectedSoundId = uri.getLastPathSegment();
                updateSoundDisplay();
            }
        }
    }

    private void updateSoundDisplay() {
        if (selectedSoundName != null && !selectedSoundName.isEmpty()) {
            tvSelectedSound.setText(selectedSoundName);
        } else {
            tvSelectedSound.setText("Báo thức mặc định");
        }
    }

    private void updateDateTimeDisplay() {
        tvSelectedDate.setText(dateFormat.format(selectedDateTime.getTime()));
        tvSelectedTime.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void saveReminder() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        // Validation
        if (title.isEmpty()) {
            etTitle.setError("Vui lòng nhập tiêu đề");
            etTitle.requestFocus();
            return;
        }

        // Check if time is in the past
        if (selectedDateTime.getTimeInMillis() < System.currentTimeMillis()) {
            showError("Vui lòng chọn thời gian trong tương lai");
            return;
        }

        // Update reminder object
        reminder.setTitle(title);
        reminder.setDescription(description);
        reminder.setReminderTime(selectedDateTime.getTimeInMillis());
        reminder.setRepeatType(spRepeatType.getSelectedItemPosition());
        reminder.setActive(swActive.isChecked());

        // Update alarm settings
        reminder.setSoundId(selectedSoundId);
        reminder.setSoundName(selectedSoundName);
        reminder.setSoundUri(selectedSoundUri);
        reminder.setVibrate(swVibrate.isChecked());
        reminder.setAlarmStyle(swAlarmStyle.isChecked());
        reminder.setVolume((int) sliderVolume.getValue());
        reminder.setSnoozeMinutes(getSnoozeMinutesFromPosition(spSnoozeMinutes.getSelectedItemPosition()));

        if (!isEditMode) {
            // Generate unique ID for new reminder
            String newId = "reminder_" + userSessionManager.getCurrentUserId() + "_" + System.currentTimeMillis();
            reminder.setId(newId);
            reminder.setCreatedAt(System.currentTimeMillis());
        }
        reminder.setUpdatedAt(System.currentTimeMillis());

        // Save to database
        saveToDatabase();
    }

    private void saveToDatabase() {
        if (isEditMode) {
            reminderRepository.updateReminder(reminder, new ReminderRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // Schedule reminder
                    if (reminder.isActive()) {
                        reminderService.scheduleReminder(reminder);
                    } else {
                        reminderService.cancelReminder(reminder.getId());
                    }

                    showSuccess("Đã cập nhật nhắc nhở");
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onError(String error) {
                    showError("Lỗi cập nhật: " + error);
                }
            });
        } else {
            reminderRepository.addReminder(reminder, new ReminderRepository.RepositoryCallback<String>() {
                @Override
                public void onSuccess(String reminderId) {
                    // Schedule reminder
                    if (reminder.isActive()) {
                        reminderService.scheduleReminder(reminder);
                    }

                    showSuccess("Đã tạo nhắc nhở");
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onError(String error) {
                    showError("Lỗi tạo nhắc nhở: " + error);
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Hiển thị dialog xác nhận nếu có thay đổi
        new AlertDialog.Builder(this)
                .setTitle("Thoát không lưu?")
                .setMessage("Bạn có muốn thoát mà không lưu thay đổi?")
                .setPositiveButton("Thoát", (dialog, which) -> finish())
                .setNegativeButton("Tiếp tục chỉnh sửa", null)
                .show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
