package com.vhn.doan.presentation.reminder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.Reminder;

import java.text.SimpleDateFormat;
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
        private Switch swActive;
        private ImageButton btnDelete;
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
            // Hiển thị tiêu đề
            tvTitle.setText(reminder.getTitle());

            // Hiển thị mô tả
            if (reminder.getDescription() != null && !reminder.getDescription().trim().isEmpty()) {
                tvDescription.setText(reminder.getDescription());
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            // Hiển thị thời gian
            if (reminder.getReminderTime() != null) {
                tvDateTime.setText(dateTimeFormat.format(reminder.getReminderTime()));
            } else {
                tvDateTime.setText("Chưa đặt thời gian");
            }

            // Hiển thị loại lặp lại
            tvRepeatType.setText(reminder.getRepeatTypeDisplayName());

            // Cập nhật trạng thái switch
            swActive.setChecked(reminder.isActive());

            // Cập nhật indicator trạng thái
            updateStatusIndicator(reminder);

            // Cập nhật giao diện dựa trên trạng thái
            updateItemAppearance(reminder);
        }

        private void updateStatusIndicator(Reminder reminder) {
            if (reminder.isActive()) {
                if (reminder.isDue()) {
                    // Nhắc nhở đã đến giờ
                    statusIndicator.setBackgroundResource(R.color.status_due);
                } else {
                    // Nhắc nhở đang hoạt động
                    statusIndicator.setBackgroundResource(R.color.status_active);
                }
            } else {
                // Nhắc nhở đã tắt
                statusIndicator.setBackgroundResource(R.color.status_inactive);
            }
        }

        private void updateItemAppearance(Reminder reminder) {
            float alpha = reminder.isActive() ? 1.0f : 0.6f;

            tvTitle.setAlpha(alpha);
            tvDescription.setAlpha(alpha);
            tvDateTime.setAlpha(alpha);
            tvRepeatType.setAlpha(alpha);

            // Highlight nếu nhắc nhở đã đến giờ và đang hoạt động
            if (reminder.isActive() && reminder.isDue()) {
                itemView.setBackgroundResource(R.drawable.bg_reminder_due);
            } else {
                itemView.setBackgroundResource(R.drawable.bg_reminder_normal);
            }
        }
    }
}
