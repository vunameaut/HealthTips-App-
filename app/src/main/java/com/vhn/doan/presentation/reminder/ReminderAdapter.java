package com.vhn.doan.presentation.reminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.vhn.doan.R;
import com.vhn.doan.data.Reminder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho RecyclerView hiển thị danh sách nhắc nhở với cải thiện null safety và crash prevention
 */
public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<Reminder> reminders;
    private OnReminderItemClickListener listener;
    private SimpleDateFormat dateTimeFormat;

    public interface OnReminderItemClickListener {
        void onReminderClick(Reminder reminder);
        void onToggleClick(Reminder reminder);
        void onDeleteClick(Reminder reminder);
    }

    public ReminderAdapter(List<Reminder> reminders, OnReminderItemClickListener listener) {
        this.reminders = reminders != null ? reminders : new ArrayList<>();
        this.listener = listener;
        this.dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        if (position < 0 || position >= reminders.size()) {
            android.util.Log.e("ReminderAdapter", "Invalid position: " + position + ", size: " + reminders.size());
            return;
        }

        Reminder reminder = reminders.get(position);
        if (reminder != null) {
            holder.bind(reminder);
        } else {
            android.util.Log.e("ReminderAdapter", "Reminder is null at position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return reminders != null ? reminders.size() : 0;
    }

    public void updateReminders(List<Reminder> newReminders) {
        try {
            if (this.reminders == null) {
                this.reminders = new ArrayList<>();
            }

            this.reminders.clear();
            if (newReminders != null) {
                this.reminders.addAll(newReminders);
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            android.util.Log.e("ReminderAdapter", "Error updating reminders: " + e.getMessage());
        }
    }

    public void addReminder(Reminder reminder) {
        try {
            if (reminder != null && reminders != null) {
                reminders.add(0, reminder);
                notifyItemInserted(0);
            }
        } catch (Exception e) {
            android.util.Log.e("ReminderAdapter", "Error adding reminder: " + e.getMessage());
        }
    }

    public void updateReminder(Reminder updatedReminder) {
        try {
            if (updatedReminder == null || reminders == null) return;

            for (int i = 0; i < reminders.size(); i++) {
                Reminder existing = reminders.get(i);
                if (existing != null && existing.getId() != null &&
                    existing.getId().equals(updatedReminder.getId())) {
                    reminders.set(i, updatedReminder);
                    notifyItemChanged(i);
                    break;
                }
            }
        } catch (Exception e) {
            android.util.Log.e("ReminderAdapter", "Error updating reminder: " + e.getMessage());
        }
    }

    public void removeReminder(Reminder reminder) {
        try {
            if (reminder == null || reminders == null) return;

            for (int i = 0; i < reminders.size(); i++) {
                Reminder existing = reminders.get(i);
                if (existing != null && existing.getId() != null &&
                    existing.getId().equals(reminder.getId())) {
                    reminders.remove(i);
                    notifyItemRemoved(i);
                    break;
                }
            }
        } catch (Exception e) {
            android.util.Log.e("ReminderAdapter", "Error removing reminder: " + e.getMessage());
        }
    }

    public class ReminderViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTitle;
        private TextView tvDescription;
        private TextView tvDateTime;
        private TextView tvRepeatType;
        private SwitchMaterial swActive;
        private MaterialButton btnDelete;
        private View statusIndicator;
        private ImageView ivReminderIcon;
        private View iconContainer;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            setupClickListeners();
        }

        private void initViews() {
            try {
                tvTitle = itemView.findViewById(R.id.tv_reminder_title);
                tvDescription = itemView.findViewById(R.id.tv_reminder_description);
                tvDateTime = itemView.findViewById(R.id.tv_reminder_datetime);
                tvRepeatType = itemView.findViewById(R.id.tv_repeat_type);
                swActive = itemView.findViewById(R.id.sw_reminder_active);
                btnDelete = itemView.findViewById(R.id.btn_delete_reminder);
                statusIndicator = itemView.findViewById(R.id.view_status_indicator);
                ivReminderIcon = itemView.findViewById(R.id.iv_reminder_icon);
                iconContainer = itemView.findViewById(R.id.icon_container);
            } catch (Exception e) {
                android.util.Log.e("ReminderAdapter", "Error initializing views: " + e.getMessage());
            }
        }

        private void setupClickListeners() {
            try {
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null &&
                        position < reminders.size()) {
                        Reminder reminder = reminders.get(position);
                        if (reminder != null) {
                            listener.onReminderClick(reminder);
                        }
                    }
                });

                if (swActive != null) {
                    swActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION && listener != null &&
                            position < reminders.size()) {
                            Reminder reminder = reminders.get(position);
                            if (reminder != null) {
                                listener.onToggleClick(reminder);
                            }
                        }
                    });
                }

                if (btnDelete != null) {
                    btnDelete.setOnClickListener(v -> {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION && listener != null &&
                            position < reminders.size()) {
                            Reminder reminder = reminders.get(position);
                            if (reminder != null) {
                                listener.onDeleteClick(reminder);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("ReminderAdapter", "Error setting up click listeners: " + e.getMessage());
            }
        }

        public void bind(Reminder reminder) {
            if (reminder == null) {
                android.util.Log.w("ReminderAdapter", "Attempted to bind null reminder");
                return;
            }

            try {
                Context context = itemView.getContext();

                // Set title
                if (tvTitle != null) {
                    tvTitle.setText(reminder.getTitle() != null ? reminder.getTitle() : "Không có tiêu đề");
                }

                // Set description
                if (tvDescription != null) {
                    String description = reminder.getDescription();
                    if (description != null && !description.trim().isEmpty()) {
                        tvDescription.setText(description);
                        tvDescription.setVisibility(View.VISIBLE);
                    } else {
                        tvDescription.setVisibility(View.GONE);
                    }
                }

                // Set date time
                if (tvDateTime != null) {
                    try {
                        if (reminder.getReminderTime() != null) {
                            String formattedDate = dateTimeFormat.format(new Date(reminder.getReminderTime()));
                            tvDateTime.setText(formattedDate);
                        } else {
                            tvDateTime.setText("Không có thời gian");
                        }
                    } catch (Exception e) {
                        tvDateTime.setText("Thời gian không hợp lệ");
                    }
                }

                // Set repeat type
                if (tvRepeatType != null) {
                    int repeatType = reminder.getRepeatType();
                    String repeatTypeText = getRepeatTypeText(repeatType);
                    tvRepeatType.setText(repeatTypeText);
                    tvRepeatType.setVisibility(View.VISIBLE);
                }

                // Set active state
                if (swActive != null) {
                    swActive.setOnCheckedChangeListener(null); // Remove listener temporarily
                    swActive.setChecked(reminder.isActive());
                    swActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION && listener != null &&
                            position < reminders.size()) {
                            Reminder currentReminder = reminders.get(position);
                            if (currentReminder != null) {
                                listener.onToggleClick(currentReminder);
                            }
                        }
                    });
                }

                // Update visual state based on active status
                updateVisualState(reminder, context);

            } catch (Exception e) {
                android.util.Log.e("ReminderAdapter", "Error binding reminder: " + e.getMessage());
            }
        }

        private void updateVisualState(Reminder reminder, Context context) {
            try {
                boolean isActive = reminder.isActive();

                // Update status indicator
                if (statusIndicator != null) {
                    if (isActive) {
                        statusIndicator.setBackgroundTintList(
                            ContextCompat.getColorStateList(context, R.color.success_color));
                        statusIndicator.setVisibility(View.VISIBLE);
                    } else {
                        statusIndicator.setBackgroundTintList(
                            ContextCompat.getColorStateList(context, R.color.error_color));
                        statusIndicator.setVisibility(View.VISIBLE);
                    }
                }

                // Update icon container
                if (iconContainer != null) {
                    if (isActive) {
                        iconContainer.setBackgroundTintList(
                            ContextCompat.getColorStateList(context, R.color.primary_color));
                    } else {
                        iconContainer.setBackgroundTintList(
                            ContextCompat.getColorStateList(context, R.color.surface_variant));
                    }
                }

                // Update icon
                if (ivReminderIcon != null) {
                    ivReminderIcon.setImageResource(isActive ? R.drawable.ic_alarm : R.drawable.ic_alarm_off);
                    if (isActive) {
                        ivReminderIcon.setImageTintList(
                            ContextCompat.getColorStateList(context, R.color.white));
                    } else {
                        ivReminderIcon.setImageTintList(
                            ContextCompat.getColorStateList(context, R.color.on_surface_variant));
                    }
                }

                // Update text colors based on state
                float alpha = isActive ? 1.0f : 0.6f;
                if (tvTitle != null) tvTitle.setAlpha(alpha);
                if (tvDescription != null) tvDescription.setAlpha(alpha);
                if (tvDateTime != null) tvDateTime.setAlpha(alpha);

            } catch (Exception e) {
                android.util.Log.e("ReminderAdapter", "Error updating visual state: " + e.getMessage());
            }
        }

        private String getRepeatTypeText(int repeatType) {
            switch (repeatType) {
                case Reminder.RepeatType.NO_REPEAT:
                    return "Không lặp lại";
                case Reminder.RepeatType.DAILY:
                    return "Hàng ngày";
                case Reminder.RepeatType.WEEKLY:
                    return "Hàng tuần";
                case Reminder.RepeatType.MONTHLY:
                    return "Hàng tháng";
                default:
                    return "Tùy chỉnh";
            }
        }
    }
}
