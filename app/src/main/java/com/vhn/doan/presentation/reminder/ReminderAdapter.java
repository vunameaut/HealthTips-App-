package com.vhn.doan.presentation.reminder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.vhn.doan.R;
import com.vhn.doan.data.Reminder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho RecyclerView hiển thị danh sách nhắc nhở
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
        this.reminders = reminders;
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
        Reminder reminder = reminders.get(position);
        holder.bind(reminder);
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    public void updateReminders(List<Reminder> newReminders) {
        this.reminders.clear();
        this.reminders.addAll(newReminders);
        notifyDataSetChanged();
    }

    public void addReminder(Reminder reminder) {
        reminders.add(0, reminder);
        notifyItemInserted(0);
    }

    public void updateReminder(Reminder updatedReminder) {
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getId().equals(updatedReminder.getId())) {
                reminders.set(i, updatedReminder);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeReminder(Reminder reminder) {
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getId().equals(reminder.getId())) {
                reminders.remove(i);
                notifyItemRemoved(i);
                break;
            }
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

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tv_reminder_title);
            tvDescription = itemView.findViewById(R.id.tv_reminder_description);
            tvDateTime = itemView.findViewById(R.id.tv_reminder_datetime);
            tvRepeatType = itemView.findViewById(R.id.tv_repeat_type);
            swActive = itemView.findViewById(R.id.sw_reminder_active);
            btnDelete = itemView.findViewById(R.id.btn_delete_reminder);
            statusIndicator = itemView.findViewById(R.id.view_status_indicator);

            setupClickListeners();
        }

        private void setupClickListeners() {
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onReminderClick(reminders.get(position));
                }
            });

            swActive.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onToggleClick(reminders.get(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteClick(reminders.get(position));
                }
            });
        }

        public void bind(Reminder reminder) {
            try {
                // Kiểm tra reminder không null
                if (reminder == null) {
                    tvTitle.setText("Nhắc nhở không hợp lệ");
                    return;
                }

                // Hiển thị tiêu đề với null check
                String title = reminder.getTitle();
                tvTitle.setText(title != null && !title.trim().isEmpty() ? title : "Nhắc nhở không có tiêu đề");

                // Hiển thị mô tả với null check
                String description = reminder.getDescription();
                if (description != null && !description.trim().isEmpty()) {
                    tvDescription.setText(description);
                    tvDescription.setVisibility(View.VISIBLE);
                } else {
                    tvDescription.setVisibility(View.GONE);
                }

                // Hiển thị thời gian với proper error handling
                try {
                    Date reminderDate = reminder.getReminderTimeAsDate();
                    if (reminderDate != null) {
                        tvDateTime.setText(dateTimeFormat.format(reminderDate));
                    } else {
                        tvDateTime.setText("Chưa đặt thời gian");
                    }
                } catch (Exception e) {
                    tvDateTime.setText("Chưa đặt thời gian");
                }

                // Hiển thị loại lặp lại với error handling
                try {
                    String repeatType = reminder.getRepeatTypeDisplayName();
                    tvRepeatType.setText(repeatType != null ? repeatType : "Không lặp lại");
                } catch (Exception e) {
                    tvRepeatType.setText("Không lặp lại");
                }

                // Cập nhật trạng thái switch
                swActive.setChecked(reminder.isActive());

                // Cập nhật indicator trạng thái với error handling
                updateStatusIndicator(reminder);

                // Cập nhật giao diện dựa trên trạng thái
                updateItemAppearance(reminder);

            } catch (Exception e) {
                // Log error và hiển thị thông tin cơ bản
                tvTitle.setText("Lỗi hiển thị nhắc nhở");
                tvDescription.setVisibility(View.GONE);
                tvDateTime.setText("--");
                tvRepeatType.setText("--");
                swActive.setChecked(false);
            }
        }

        private void updateStatusIndicator(Reminder reminder) {
            try {
                if (reminder.isActive()) {
                    try {
                        if (reminder.isDue()) {
                            // Nhắc nhở đã đến giờ
                            statusIndicator.setBackgroundResource(R.color.status_due);
                        } else {
                            // Nhắc nhở đang hoạt động
                            statusIndicator.setBackgroundResource(R.color.status_active);
                        }
                    } catch (Exception e) {
                        // Fallback nếu isDue() gây lỗi
                        statusIndicator.setBackgroundResource(R.color.status_active);
                    }
                } else {
                    // Nhắc nhở đã tắt
                    statusIndicator.setBackgroundResource(R.color.status_inactive);
                }
            } catch (Exception e) {
                // Fallback color nếu có lỗi
                statusIndicator.setBackgroundResource(android.R.color.darker_gray);
            }
        }

        private void updateItemAppearance(Reminder reminder) {
            try {
                float alpha = reminder.isActive() ? 1.0f : 0.6f;

                tvTitle.setAlpha(alpha);
                tvDescription.setAlpha(alpha);
                tvDateTime.setAlpha(alpha);
                tvRepeatType.setAlpha(alpha);

                // Highlight nếu nhắc nhở đã đến giờ và đang hoạt động với error handling
                try {
                    if (reminder.isActive() && reminder.isDue()) {
                        itemView.setBackgroundResource(R.drawable.bg_reminder_due);
                    } else {
                        itemView.setBackgroundResource(R.drawable.bg_reminder_normal);
                    }
                } catch (Exception e) {
                    // Fallback background
                    itemView.setBackgroundResource(android.R.drawable.list_selector_background);
                }
            } catch (Exception e) {
                // Fallback appearance
                tvTitle.setAlpha(1.0f);
                tvDescription.setAlpha(1.0f);
                tvDateTime.setAlpha(1.0f);
                tvRepeatType.setAlpha(1.0f);
            }
        }
    }
}
