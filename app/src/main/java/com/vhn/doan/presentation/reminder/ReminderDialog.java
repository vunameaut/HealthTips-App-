package com.vhn.doan.presentation.reminder;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.vhn.doan.R;
import com.vhn.doan.data.Reminder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Dialog để tạo hoặc chỉnh sửa nhắc nhở
 */
public class ReminderDialog {

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
    private Switch swActive;
    private Button btnSelectDate;
    private Button btnSelectTime;
    private Button btnSave;
    private Button btnCancel;

    // Date and Time
    private Calendar selectedDateTime;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    // Repeat types
    private String[] repeatTypes = {
        "Không lặp",
        "Hàng ngày",
        "Hàng tuần",
        "Hàng tháng"
    };

    public interface OnReminderDialogListener {
        void onReminderSaved(Reminder reminder);
        void onReminderCanceled();
    }

    public ReminderDialog(Context context, OnReminderDialogListener listener) {
        this.context = context;
        this.listener = listener;
        this.isEditMode = false;
        this.selectedDateTime = Calendar.getInstance();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Đặt thời gian mặc định là 1 giờ sau thời điểm hiện tại
        selectedDateTime.add(Calendar.HOUR_OF_DAY, 1);
    }

    public ReminderDialog(Context context, Reminder reminder, OnReminderDialogListener listener) {
        this(context, listener);
        this.reminder = reminder;
        this.isEditMode = true;

        if (reminder.getReminderTime() != null) {
            selectedDateTime.setTime(reminder.getReminderTime());
        }
    }

    public void show() {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_reminder, null);

        initViews(dialogView);
        setupSpinner();
        setupClickListeners();
        populateData();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        builder.setTitle(isEditMode ? "Chỉnh sửa nhắc nhở" : "Tạo nhắc nhở mới");

        dialog = builder.create();
        dialog.show();
    }

    private void initViews(View view) {
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

        // Hiển thị thời gian đã chọn
        updateDateTimeDisplay();
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            context,
            android.R.layout.simple_spinner_item,
            repeatTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRepeatType.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveReminder());
        btnCancel.setOnClickListener(v -> cancelDialog());
    }

    private void populateData() {
        if (isEditMode && reminder != null) {
            etTitle.setText(reminder.getTitle());
            etDescription.setText(reminder.getDescription());
            spRepeatType.setSelection(reminder.getRepeatType());
            swActive.setChecked(reminder.isActive());
        } else {
            // Giá trị mặc định cho reminder mới
            swActive.setChecked(true);
            spRepeatType.setSelection(0); // Không lặp
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

        // Không cho phép chọn ngày trong quá khứ
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
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

    private void updateDateTimeDisplay() {
        tvSelectedDate.setText(dateFormat.format(selectedDateTime.getTime()));
        tvSelectedTime.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void saveReminder() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Validation
        if (title.isEmpty()) {
            etTitle.setError("Vui lòng nhập tiêu đề nhắc nhở");
            etTitle.requestFocus();
            return;
        }

        // Kiểm tra thời gian không được trong quá khứ
        if (selectedDateTime.getTimeInMillis() <= System.currentTimeMillis()) {
            Toast.makeText(context, "Thời gian nhắc nhở phải sau thời điểm hiện tại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo hoặc cập nhật reminder
        if (reminder == null) {
            reminder = new Reminder();
        }

        reminder.setTitle(title);
        reminder.setDescription(description);
        reminder.setReminderTime(selectedDateTime.getTime());
        reminder.setRepeatType(spRepeatType.getSelectedItemPosition());
        reminder.setActive(swActive.isChecked());

        if (listener != null) {
            listener.onReminderSaved(reminder);
        }

        dialog.dismiss();
    }

    private void cancelDialog() {
        if (listener != null) {
            listener.onReminderCanceled();
        }
        dialog.dismiss();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
