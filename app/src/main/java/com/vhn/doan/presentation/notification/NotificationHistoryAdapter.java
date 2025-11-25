package com.vhn.doan.presentation.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.NotificationHistory;
import com.vhn.doan.data.NotificationType;
import com.vhn.doan.utils.NotificationTimeUtils;

/**
 * Adapter cho danh sách notification history với DiffUtil
 * Hỗ trợ cả notification items và section headers
 */
public class NotificationHistoryAdapter extends ListAdapter<NotificationHistoryAdapter.NotificationItem, RecyclerView.ViewHolder> {

    private static final int TYPE_SECTION_HEADER = 0;
    private static final int TYPE_NOTIFICATION = 1;

    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationHistory notification);
    }

    public NotificationHistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<NotificationItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<NotificationItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull NotificationItem oldItem, @NonNull NotificationItem newItem) {
                    if (oldItem.getType() != newItem.getType()) {
                        return false;
                    }
                    if (oldItem.getType() == TYPE_SECTION_HEADER) {
                        return oldItem.getSectionTitle().equals(newItem.getSectionTitle());
                    } else {
                        return oldItem.getNotification().getId().equals(newItem.getNotification().getId());
                    }
                }

                @Override
                public boolean areContentsTheSame(@NonNull NotificationItem oldItem, @NonNull NotificationItem newItem) {
                    if (oldItem.getType() != newItem.getType()) {
                        return false;
                    }
                    if (oldItem.getType() == TYPE_SECTION_HEADER) {
                        return oldItem.getSectionTitle().equals(newItem.getSectionTitle());
                    } else {
                        NotificationHistory oldNotif = oldItem.getNotification();
                        NotificationHistory newNotif = newItem.getNotification();
                        return oldNotif.getId().equals(newNotif.getId()) &&
                               oldNotif.isRead() == newNotif.isRead() &&
                               oldNotif.getTitle().equals(newNotif.getTitle());
                    }
                }
            };

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SECTION_HEADER) {
            View view = inflater.inflate(R.layout.item_notification_section_header, parent, false);
            return new SectionHeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_notification_history, parent, false);
            return new NotificationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotificationItem item = getItem(position);
        if (holder instanceof SectionHeaderViewHolder) {
            ((SectionHeaderViewHolder) holder).bind(item.getSectionTitle());
        } else if (holder instanceof NotificationViewHolder) {
            ((NotificationViewHolder) holder).bind(item.getNotification(), listener);
        }
    }

    // Section Header ViewHolder
    static class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView textSectionHeader;

        SectionHeaderViewHolder(View itemView) {
            super(itemView);
            textSectionHeader = itemView.findViewById(R.id.textSectionHeader);
        }

        void bind(String sectionTitle) {
            textSectionHeader.setText(sectionTitle);
        }
    }

    // Notification ViewHolder
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private View unreadIndicator;
        private ImageView iconNotification;
        private TextView textTitle;
        private TextView textBody;
        private TextView textTime;
        private TextView textTypeBadge;

        NotificationViewHolder(View itemView) {
            super(itemView);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
            iconNotification = itemView.findViewById(R.id.iconNotification);
            textTitle = itemView.findViewById(R.id.textTitle);
            textBody = itemView.findViewById(R.id.textBody);
            textTime = itemView.findViewById(R.id.textTime);
            textTypeBadge = itemView.findViewById(R.id.textTypeBadge);
        }

        void bind(NotificationHistory notification, OnNotificationClickListener listener) {
            // Set unread indicator visibility
            unreadIndicator.setVisibility(notification.isUnread() ? View.VISIBLE : View.GONE);

            // Set title and body
            textTitle.setText(notification.getTitle());
            textBody.setText(notification.getBody());

            // Set time
            String timeText = NotificationTimeUtils.formatNotificationTime(notification.getReceivedAt());
            textTime.setText(timeText);

            // Set type badge
            String typeText = getTypeDisplayName(notification.getType());
            textTypeBadge.setText(typeText);

            // Set icon based on type
            int iconRes = getTypeIcon(notification.getType());
            iconNotification.setImageResource(iconRes);

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
        }

        private String getTypeDisplayName(NotificationType type) {
            if (type == null) return "KHÁC";
            return type.getDisplayName().toUpperCase();
        }

        private int getTypeIcon(NotificationType type) {
            if (type == null) return R.drawable.ic_notifications;

            switch (type) {
                case COMMENT_REPLY:
                case COMMENT_LIKE:
                    return R.drawable.ic_comment;
                case NEW_HEALTH_TIP:
                case HEALTH_TIP_RECOMMENDATION:
                    return R.drawable.ic_health_tips;
                case NEW_VIDEO:
                    return R.drawable.ic_video;
                case REMINDER_ALERT:
                case REMINDER_ALARM:
                    return R.drawable.ic_notification_reminder;
                case SYSTEM_UPDATE:
                case SYSTEM_MESSAGE:
                    return R.drawable.ic_info;
                default:
                    return R.drawable.ic_notifications;
            }
        }
    }

    // Wrapper class for items (section header or notification)
    public static class NotificationItem {
        private int type;
        private String sectionTitle;
        private NotificationHistory notification;

        public static NotificationItem sectionHeader(String title) {
            NotificationItem item = new NotificationItem();
            item.type = TYPE_SECTION_HEADER;
            item.sectionTitle = title;
            return item;
        }

        public static NotificationItem notification(NotificationHistory notification) {
            NotificationItem item = new NotificationItem();
            item.type = TYPE_NOTIFICATION;
            item.notification = notification;
            return item;
        }

        public int getType() {
            return type;
        }

        public String getSectionTitle() {
            return sectionTitle;
        }

        public NotificationHistory getNotification() {
            return notification;
        }
    }
}
