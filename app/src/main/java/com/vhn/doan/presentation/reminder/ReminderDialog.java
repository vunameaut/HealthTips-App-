package com.vhn.doan.presentation.reminder;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.vhn.doan.R;
import com.vhn.doan.data.Reminder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Dialog để tạo hoặc chỉnh sửa nhắc nhở với tính năng báo thức và chọn âm thanh
 */
public class ReminderDialog {

    private static final int REQUEST_SOUND_SELECTION = 1001;

    private Context context;
    private OnReminderDialogListener listener;
    private AlertDialog dialog;
    private Reminder reminder;
    private boolean isEditMode;

    // UI Components
    private EditText etTitle;
    private EditText etDescription;
    private TextView tvSelectedDate;
    private TextView tvSelectedTime;
    private Spinner spRepeatType;
    private SwitchMaterial swActive;
    private Button btnSelectDate;
    private Button btnSelectTime;
    private Button btnSave;
    private Button btnCancel;

    // New UI Components for Alarm Features
    private TextView tvSelectedSound;
    private Button btnSelectSound;
    private SwitchMaterial swVibrate;
    private SwitchMaterial swAlarmStyle;
    private Slider sliderVolume;
    private TextView tvVolumeValue;
    private Spinner spSnoozeMinutes;

    // Date and Time
    private Calendar selectedDateTime;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    // Sound Selection
    private String selectedSoundId = "default_alarm";
    private String selectedSoundName = "Báo thức mặc định";
    private String selectedSoundUri;

    public interface OnReminderDialogListener {
        void onReminderSaved(Reminder reminder);
        void onReminderDeleted(String reminderId);
    }

    public ReminderDialog(Context context, OnReminderDialogListener listener) {
        this.context = context;
        this.listener = listener;
        this.selectedDateTime = Calendar.getInstance();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    public void showCreateDialog() {
        this.isEditMode = false;
        this.reminder = new Reminder();
        showDialog();
    }

    public void showEditDialog(Reminder reminder) {
        this.isEditMode = true;
        this.reminder = reminder;
        if (reminder.getReminderTime() != null) {
            selectedDateTime.setTimeInMillis(reminder.getReminderTime());
        }
        // Load sound settings
        if (reminder.getSoundId() != null) {
            selectedSoundId = reminder.getSoundId();
            selectedSoundName = reminder.getSoundName();
            selectedSoundUri = reminder.getSoundUri();
        }
        showDialog();
    }

    private void showDialog() {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_reminder_enhanced, null);
        initializeViews(dialogView);
        setupViews();
        populateFields();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        builder.setCancelable(true);

        dialog = builder.create();
        dialog.show();
    }

    private void initializeViews(View view) {
        // Existing views
        etTitle = view.findViewById(R.id.et_reminder_title);
        etDescription = view.findViewById(R.id.et_reminder_description);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        tvSelectedTime = view.findViewById(R.id.tv_selected_time);
        spRepeatType = view.findViewById(R.id.sp_repeat_type);
        swActive = view.findViewById(R.id.sw_reminder_active);
        btnSelectDate = view.findViewById(R.id.btn_select_date);
        btnSelectTime = view.findViewById(R.id.btn_select_time);
        btnSave = view.findViewById(R.id.btn_save_reminder);
        btnCancel = view.findViewById(R.id.btn_cancel_reminder);

        // New views for alarm features
        tvSelectedSound = view.findViewById(R.id.tv_selected_sound);
        btnSelectSound = view.findViewById(R.id.btn_select_sound);
        swVibrate = view.findViewById(R.id.sw_vibrate);
        swAlarmStyle = view.findViewById(R.id.sw_alarm_style);
        sliderVolume = view.findViewById(R.id.slider_volume);
        tvVolumeValue = view.findViewById(R.id.tv_volume_value);
        spSnoozeMinutes = view.findViewById(R.id.sp_snooze_minutes);
    }

    private void setupViews() {
        // Setup repeat type spinner
        ArrayAdapter<CharSequence> repeatAdapter = ArrayAdapter.createFromResource(
            context, R.array.repeat_types, android.R.layout.simple_spinner_item);
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRepeatType.setAdapter(repeatAdapter);

        // Setup snooze minutes spinner
        ArrayAdapter<CharSequence> snoozeAdapter = ArrayAdapter.createFromResource(
            context, R.array.snooze_options, android.R.layout.simple_spinner_item);
        snoozeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSnoozeMinutes.setAdapter(snoozeAdapter);

        // Setup volume slider
        sliderVolume.setValueFrom(0f);
        sliderVolume.setValueTo(100f);
        sliderVolume.setValue(80f);
        sliderVolume.addOnChangeListener((slider, value, fromUser) -> {
            tvVolumeValue.setText((int)value + "%");
        });

        // Setup click listeners
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectTime.setOnClickListener(v -> showTimePicker());
        btnSelectSound.setOnClickListener(v -> showSoundSelection());
        btnSave.setOnClickListener(v -> saveReminder());
        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void populateFields() {
        if (reminder != null) {
            etTitle.setText(reminder.getTitle());
            etDescription.setText(reminder.getDescription());
            spRepeatType.setSelection(reminder.getRepeatType());
            swActive.setChecked(reminder.isActive());

            // Populate alarm settings
            swVibrate.setChecked(reminder.isVibrate());
            swAlarmStyle.setChecked(reminder.isAlarmStyle());
            sliderVolume.setValue(reminder.getVolume());
            tvVolumeValue.setText(reminder.getVolume() + "%");

            // Set snooze minutes selection
            int snoozeMinutes = reminder.getSnoozeMinutes();
            int snoozePosition = getSnoozePosition(snoozeMinutes);
            spSnoozeMinutes.setSelection(snoozePosition);

            // Update sound selection
            updateSoundSelection();
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
            context,
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
            context,
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

    private void showSoundSelection() {
        Intent intent = new Intent(context, SoundSelectionActivity.class);
        intent.putExtra(SoundSelectionActivity.EXTRA_CURRENT_SOUND_ID, selectedSoundId);

        // Start activity for result if context is Activity
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).startActivityForResult(intent, REQUEST_SOUND_SELECTION);
        }
    }

    public void onSoundSelected(String soundId, String soundName, String soundUri) {
        selectedSoundId = soundId;
        selectedSoundName = soundName;
        selectedSoundUri = soundUri;
        updateSoundSelection();
    }

    private void updateSoundSelection() {
        if (selectedSoundName != null) {
            tvSelectedSound.setText(selectedSoundName);
        } else {
            tvSelectedSound.setText(context.getString(R.string.sound_default));
        }
    }

    private void updateDateTimeDisplay() {
        tvSelectedDate.setText(dateFormat.format(selectedDateTime.getTime()));
        tvSelectedTime.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void saveReminder() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập tiêu đề nhắc nhở", Toast.LENGTH_SHORT).show();
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

        reminder.touch(); // Update timestamp

        if (listener != null) {
            listener.onReminderSaved(reminder);
        }

        dialog.dismiss();
    }
}
