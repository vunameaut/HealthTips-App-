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
import com.vhn.doan.data.ReminderSortType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private ReminderSortType currentSortType = ReminderSortType.CREATED_TIME_DESC;

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
            android.util.Log.e("ReminderAdapter", "❌ Invalid position: " + position + ", size: " + reminders.size());
            return;
        }

        Reminder reminder = reminders.get(position);
        if (reminder != null) {
            android.util.Log.d("ReminderAdapter", "📋 onBindViewHolder: position=" + position +
                ", reminder title=" + reminder.getTitle() +
                ", reminder id=" + reminder.getId());
            holder.bind(reminder);
        } else {
            android.util.Log.e("ReminderAdapter", "❌ Reminder is null at position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return reminders != null ? reminders.size() : 0;
    }

    /**
     * Lấy danh sách reminders hiện tại
     */
    public List<Reminder> getReminders() {
        return reminders != null ? reminders : new ArrayList<>();
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

    /**
     * Sắp xếp danh sách nhắc nhở theo kiểu được chỉ định
     */
    public void sortReminders(ReminderSortType sortType) {
        if (reminders == null || reminders.isEmpty()) {
            android.util.Log.w("ReminderAdapter", "⚠️ Không có nhắc nhở để sắp xếp");
            return;
        }

        try {
            this.currentSortType = sortType;
            android.util.Log.d("ReminderAdapter", "🔄 Sắp xếp nhắc nhở theo: " + sortType.getDisplayName());

            Comparator<Reminder> comparator = getComparatorForSortType(sortType);
            Collections.sort(reminders, comparator);

            notifyDataSetChanged();
            android.util.Log.d("ReminderAdapter", "✅ Sắp xếp hoàn thành: " + reminders.size() + " nhắc nhở");
        } catch (Exception e) {
            android.util.Log.e("ReminderAdapter", "❌ Lỗi khi sắp xếp nhắc nhở: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy Comparator tương ứng với kiểu sắp xếp
     */
    private Comparator<Reminder> getComparatorForSortType(ReminderSortType sortType) {
        switch (sortType) {
            case CREATED_TIME_DESC:
                return (r1, r2) -> {
                    Long time1 = r1.getCreatedAt();
                    Long time2 = r2.getCreatedAt();
                    if (time1 == null && time2 == null) return 0;
                    if (time1 == null) return 1;
                    if (time2 == null) return -1;
                    return time2.compareTo(time1); // Mới nhất trước
                };

            case CREATED_TIME_ASC:
                return (r1, r2) -> {
                    Long time1 = r1.getCreatedAt();
                    Long time2 = r2.getCreatedAt();
                    if (time1 == null && time2 == null) return 0;
                    if (time1 == null) return 1;
                    if (time2 == null) return -1;
                    return time1.compareTo(time2); // Cũ nhất trước
                };

            case REMINDER_TIME_ASC:
                return (r1, r2) -> {
                    Long time1 = r1.getReminderTime();
                    Long time2 = r2.getReminderTime();
                    if (time1 == null && time2 == null) return 0;
                    if (time1 == null) return 1;
                    if (time2 == null) return -1;
                    return time1.compareTo(time2); // Sớm nhất trước
                };

            case REMINDER_TIME_DESC:
                return (r1, r2) -> {
                    Long time1 = r1.getReminderTime();
                    Long time2 = r2.getReminderTime();
                    if (time1 == null && time2 == null) return 0;
                    if (time1 == null) return 1;
                    if (time2 == null) return -1;
                    return time2.compareTo(time1); // Muộn nhất trước
                };

            case TITLE_ASC:
                return (r1, r2) -> {
                    String title1 = r1.getTitle();
                    String title2 = r2.getTitle();
                    if (title1 == null && title2 == null) return 0;
                    if (title1 == null) return 1;
                    if (title2 == null) return -1;
                    return title1.compareToIgnoreCase(title2); // A-Z
                };

            case TITLE_DESC:
                return (r1, r2) -> {
                    String title1 = r1.getTitle();
                    String title2 = r2.getTitle();
                    if (title1 == null && title2 == null) return 0;
                    if (title1 == null) return 1;
                    if (title2 == null) return -1;
                    return title2.compareToIgnoreCase(title1); // Z-A
                };

            case STATUS_ACTIVE_FIRST:
                return (r1, r2) -> {
                    boolean active1 = r1.isActive();
                    boolean active2 = r2.isActive();
                    if (active1 == active2) {
                        // Nếu cùng trạng thái, sắp xếp theo thời gian tạo mới nhất
                        Long time1 = r1.getCreatedAt();
                        Long time2 = r2.getCreatedAt();
                        if (time1 == null || time2 == null) return 0;
                        return time2.compareTo(time1);
                    }
                    return active1 ? -1 : 1; // Active trước
                };

            case STATUS_INACTIVE_FIRST:
                return (r1, r2) -> {
                    boolean active1 = r1.isActive();
                    boolean active2 = r2.isActive();
                    if (active1 == active2) {
                        // Nếu cùng trạng thái, sắp xếp theo thời gian tạo mới nhất
                        Long time1 = r1.getCreatedAt();
                        Long time2 = r2.getCreatedAt();
                        if (time1 == null || time2 == null) return 0;
                        return time2.compareTo(time1);
                    }
                    return active1 ? 1 : -1; // Inactive trước
                };

            default:
                return getComparatorForSortType(ReminderSortType.CREATED_TIME_DESC);
        }
    }

    /**
     * Lấy kiểu sắp xếp hiện tại
     */
    public ReminderSortType getCurrentSortType() {
        return currentSortType;
    }

    /**
     * Cập nhật danh sách với sắp xếp theo kiểu hiện tại
     */
    public void updateRemindersWithSort(List<Reminder> newReminders) {
        try {
            if (this.reminders == null) {
                this.reminders = new ArrayList<>();
            }

            this.reminders.clear();
            if (newReminders != null) {
                this.reminders.addAll(newReminders);
                // Tự động sắp xếp theo kiểu hiện tại
                sortReminders(currentSortType);
            } else {
                notifyDataSetChanged();
            }
        } catch (Exception e) {
            android.util.Log.e("ReminderAdapter", "❌ Lỗi khi cập nhật và sắp xếp nhắc nhở: " + e.getMessage());
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

                // Debug log để kiểm tra view có được tìm thấy không
                android.util.Log.d("ReminderAdapter", "🔍 View initialization: " +
                    "tvTitle=" + (tvTitle != null) +
                    ", tvDescription=" + (tvDescription != null) +
                    ", tvDateTime=" + (tvDateTime != null));

                if (tvTitle == null) {
                    android.util.Log.e("ReminderAdapter", "❌ CRITICAL: tvTitle is null after findViewById!");
                }
            } catch (Exception e) {
                android.util.Log.e("ReminderAdapter", "❌ Error initializing views: " + e.getMessage(), e);
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
                android.util.Log.w("ReminderAdapter", "❌ Attempted to bind null reminder");
                return;
            }

            try {
                Context context = itemView.getContext();

                // Debug log để kiểm tra dữ liệu
                android.util.Log.d("ReminderAdapter", "🔄 Binding reminder: " +
                    "Title=" + reminder.getTitle() +
                    ", Description=" + reminder.getDescription() +
                    ", Active=" + reminder.isActive());

                // QUAN TRỌNG: Set title với kiểm tra null và đảm bảo luôn hiển thị
                if (tvTitle != null) {
                    String title = reminder.getTitle();
                    if (title != null && !title.trim().isEmpty()) {
                        tvTitle.setText(title.trim());
                        android.util.Log.d("ReminderAdapter", "✅ Title set: '" + title.trim() + "'");
                    } else {
                        tvTitle.setText("Nhắc nhở không có tiêu đề");
                        android.util.Log.w("ReminderAdapter", "⚠️ Title is null or empty, using default");
                    }

                    // ĐẢM BẢO TextView luôn visible và có style đúng
                    tvTitle.setVisibility(View.VISIBLE);
                    tvTitle.setAlpha(1.0f); // Đảm bảo không bị mờ

                    // Debug: Kiểm tra style và layout
                    android.util.Log.d("ReminderAdapter", "🔍 Title TextView state: " +
                        "visibility=" + tvTitle.getVisibility() +
                        ", alpha=" + tvTitle.getAlpha() +
                        ", text='" + tvTitle.getText() + "'");
                } else {
                    android.util.Log.e("ReminderAdapter", "❌ CRITICAL: tvTitle is null! Check layout R.id.tv_reminder_title");
                }

                // Set description với cải thiện
                if (tvDescription != null) {
                    String description = reminder.getDescription();
                    if (description != null && !description.trim().isEmpty()) {
                        tvDescription.setText(description.trim());
                        tvDescription.setVisibility(View.VISIBLE);
                        android.util.Log.d("ReminderAdapter", "✅ Description set: '" + description.trim() + "'");
                    } else {
                        tvDescription.setText("Không có mô tả");
                        tvDescription.setVisibility(View.VISIBLE);
                        android.util.Log.d("ReminderAdapter", "📝 Description empty, showing default message");
                    }
                } else {
                    android.util.Log.w("ReminderAdapter", "⚠️ tvDescription is null");
                }

                // Set date time với cải thiện format
                if (tvDateTime != null) {
                    try {
                        if (reminder.getReminderTime() != null && reminder.getReminderTime() > 0) {
                            String formattedDate = dateTimeFormat.format(new Date(reminder.getReminderTime()));
                            tvDateTime.setText(formattedDate);
                            tvDateTime.setVisibility(View.VISIBLE);
                            android.util.Log.d("ReminderAdapter", "🕐 DateTime set: " + formattedDate);
                        } else {
                            tvDateTime.setText("Chưa đặt thời gian");
                            tvDateTime.setVisibility(View.VISIBLE);
                            android.util.Log.w("ReminderAdapter", "⚠️ ReminderTime is null or invalid");
                        }
                    } catch (Exception e) {
                        tvDateTime.setText("Thời gian không hợp lệ");
                        tvDateTime.setVisibility(View.VISIBLE);
                        android.util.Log.e("ReminderAdapter", "❌ Error formatting date: " + e.getMessage());
                    }
                } else {
                    android.util.Log.w("ReminderAdapter", "⚠️ tvDateTime is null");
                }

                // Set repeat type với cải thiện
                if (tvRepeatType != null) {
                    int repeatType = reminder.getRepeatType();
                    String repeatTypeText = getRepeatTypeText(repeatType);
                    tvRepeatType.setText(repeatTypeText);
                    tvRepeatType.setVisibility(View.VISIBLE);
                    android.util.Log.d("ReminderAdapter", "🔄 RepeatType set: " + repeatTypeText);
                } else {
                    android.util.Log.w("ReminderAdapter", "⚠️ tvRepeatType is null");
                }

                // Set active state với cải thiện
                if (swActive != null) {
                    swActive.setOnCheckedChangeListener(null); // Remove listener temporarily
                    swActive.setChecked(reminder.isActive());
                    android.util.Log.d("ReminderAdapter", "🎛️ Switch set to: " + reminder.isActive());

                    // Re-attach listener
                    swActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION && listener != null &&
                            position < reminders.size()) {
                            Reminder currentReminder = reminders.get(position);
                            if (currentReminder != null) {
                                android.util.Log.d("ReminderAdapter", "🔄 Toggle clicked: " + isChecked + " for " + currentReminder.getTitle());
                                listener.onToggleClick(currentReminder);
                            }
                        }
                    });
                } else {
                    android.util.Log.w("ReminderAdapter", "⚠️ swActive is null");
                }

                // Update visual state based on active status - GỌI SAU CÙNG
                updateVisualState(reminder, context);

                android.util.Log.d("ReminderAdapter", "✅ Binding completed successfully for: " + reminder.getTitle());

            } catch (Exception e) {
                android.util.Log.e("ReminderAdapter", "❌ CRITICAL Error binding reminder: " + e.getMessage(), e);

                // Fallback: Hiển thị thông tin cơ bản nếu có lỗi
                if (tvTitle != null) {
                    tvTitle.setText("Lỗi hiển thị nhắc nhở");
                    tvTitle.setVisibility(View.VISIBLE);
                }
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

                // QUAN TRỌNG: Cải thiện text alpha để đảm bảo title luôn rõ ràng
                // Title luôn được hiển thị rõ ràng, chỉ description và datetime bị mờ nhẹ khi inactive
                if (isActive) {
                    // Khi active: tất cả text đều rõ ràng
                    if (tvTitle != null) tvTitle.setAlpha(1.0f);
                    if (tvDescription != null) tvDescription.setAlpha(1.0f);
                    if (tvDateTime != null) tvDateTime.setAlpha(1.0f);
                    if (tvRepeatType != null) tvRepeatType.setAlpha(1.0f);

                    android.util.Log.d("ReminderAdapter", "🔆 Visual state: ACTIVE - all text visible");
                } else {
                    // Khi inactive: Title vẫn rõ, chỉ các thông tin phụ bị mờ nhẹ
                    if (tvTitle != null) tvTitle.setAlpha(0.9f); // Title vẫn rõ ràng 90%
                    if (tvDescription != null) tvDescription.setAlpha(0.7f); // Mô tả mờ hơn
                    if (tvDateTime != null) tvDateTime.setAlpha(0.8f); // Thời gian mờ nhẹ
                    if (tvRepeatType != null) tvRepeatType.setAlpha(0.8f); // Repeat type mờ nhẹ

                    android.util.Log.d("ReminderAdapter", "🔅 Visual state: INACTIVE - title still visible, details dimmed");
                }

            } catch (Exception e) {
                android.util.Log.e("ReminderAdapter", "❌ Error updating visual state: " + e.getMessage());

                // Fallback: Đảm bảo title luôn visible nếu có lỗi
                if (tvTitle != null) {
                    tvTitle.setAlpha(1.0f);
                    tvTitle.setVisibility(View.VISIBLE);
                }
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
