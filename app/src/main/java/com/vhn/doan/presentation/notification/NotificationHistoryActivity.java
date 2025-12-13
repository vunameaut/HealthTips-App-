package com.vhn.doan.presentation.notification;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.vhn.doan.R;
import com.vhn.doan.data.NotificationHistory;
import com.vhn.doan.data.NotificationType;
import com.vhn.doan.data.repository.NotificationHistoryRepositoryImpl;
import com.vhn.doan.presentation.base.BaseActivity;
import com.vhn.doan.utils.NotificationTimeUtils;
import com.vhn.doan.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity hiển thị lịch sử thông báo
 * Features: Pull-to-refresh, Swipe-to-delete, Mark all as read, Delete operations, Pagination
 */
public class NotificationHistoryActivity extends BaseActivity implements NotificationHistoryContract.View {

    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private ExtendedFloatingActionButton fabMarkAllRead;

    private NotificationHistoryAdapter adapter;
    private NotificationHistoryPresenter presenter;
    private List<NotificationHistoryAdapter.NotificationItem> displayItems = new ArrayList<>();

    private boolean isLoadingMore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_history);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        setupFAB();
        setupPresenter();

        presenter.loadNotifications();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerViewNotifications);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);
        fabMarkAllRead = findViewById(R.id.fabMarkAllRead);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_delete_read) {
                presenter.deleteReadNotifications();
                return true;
            } else if (id == R.id.action_delete_all) {
                presenter.deleteAllNotifications();
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new NotificationHistoryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnNotificationClickListener(notification -> {
            presenter.onNotificationClicked(notification);
        });

        // Setup swipe to delete
        setupSwipeToDelete();

        // Setup infinite scroll
        setupInfiniteScroll();
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < displayItems.size()) {
                    NotificationHistoryAdapter.NotificationItem item = displayItems.get(position);
                    if (item.getType() == 1) { // Only delete notifications, not headers
                        NotificationHistory notification = item.getNotification();
                        presenter.deleteNotification(notification);
                    } else {
                        // Re-bind the item if it's a header
                        adapter.notifyItemChanged(position);
                    }
                }
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                // Only allow swipe for notification items, not headers
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < displayItems.size()) {
                    NotificationHistoryAdapter.NotificationItem item = displayItems.get(position);
                    if (item.getType() == 0) { // Section header
                        return 0; // No swipe
                    }
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void setupInfiniteScroll() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoadingMore) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                            && firstVisibleItemPosition >= 0) {
                        isLoadingMore = true;
                        presenter.loadMoreNotifications();
                    }
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            presenter.refreshNotifications();
        });
    }

    private void setupFAB() {
        fabMarkAllRead.setOnClickListener(v -> {
            presenter.markAllAsRead();
        });
    }

    private void setupPresenter() {
        NotificationHistoryRepositoryImpl repository = NotificationHistoryRepositoryImpl.getInstance(this);
        SessionManager sessionManager = new SessionManager(this);
        presenter = new NotificationHistoryPresenter(repository, sessionManager, this);
        presenter.attachView(this);
    }

    @Override
    public void showNotifications(List<NotificationHistory> notifications) {
        runOnUiThread(() -> {
            displayItems.clear();
            displayItems.addAll(groupNotificationsBySection(notifications));
            adapter.submitList(new ArrayList<>(displayItems));

            if (notifications.isEmpty()) {
                showEmptyState();
            } else {
                hideEmptyState();
            }
        });
    }

    @Override
    public void appendNotifications(List<NotificationHistory> notifications) {
        runOnUiThread(() -> {
            if (notifications != null && !notifications.isEmpty()) {
                int oldSize = displayItems.size();
                displayItems.addAll(groupNotificationsBySection(notifications));
                adapter.submitList(new ArrayList<>(displayItems));
                adapter.notifyItemRangeInserted(oldSize, notifications.size());
            }
        });
    }

    private List<NotificationHistoryAdapter.NotificationItem> groupNotificationsBySection(List<NotificationHistory> notifications) {
        List<NotificationHistoryAdapter.NotificationItem> items = new ArrayList<>();
        Map<String, List<NotificationHistory>> grouped = new HashMap<>();

        // Group by section
        for (NotificationHistory notif : notifications) {
            String section = NotificationTimeUtils.getSectionHeader(notif.getReceivedAt());
            if (!grouped.containsKey(section)) {
                grouped.put(section, new ArrayList<>());
            }
            grouped.get(section).add(notif);
        }

        // Add sections in order
        String[] sectionOrder = {"Hôm nay", "Hôm qua", "Tuần này", "Tháng này", "Cũ hơn"};
        for (String section : sectionOrder) {
            if (grouped.containsKey(section)) {
                items.add(NotificationHistoryAdapter.NotificationItem.sectionHeader(section));
                for (NotificationHistory notif : grouped.get(section)) {
                    items.add(NotificationHistoryAdapter.NotificationItem.notification(notif));
                }
            }
        }

        return items;
    }

    @Override
    public void showSuccess(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void showEmptyState() {
        runOnUiThread(() -> {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            fabMarkAllRead.hide();
        });
    }

    @Override
    public void hideEmptyState() {
        runOnUiThread(() -> {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            fabMarkAllRead.show();
        });
    }

    @Override
    public void updateUnreadCount(int count) {
        runOnUiThread(() -> {
            // Could update toolbar subtitle or badge
            if (getSupportActionBar() != null) {
                if (count > 0) {
                    getSupportActionBar().setSubtitle(count + " chưa đọc");
                } else {
                    getSupportActionBar().setSubtitle(null);
                }
            }
        });
    }

    @Override
    public void updateNotificationItem(NotificationHistory notification) {
        runOnUiThread(() -> {
            // LiveData tự động update, không cần gọi refresh
            // Chỉ cần update adapter nếu cần
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void removeNotificationItem(NotificationHistory notification) {
        // LiveData tự động update khi xóa trong database
        // Không cần gọi refresh để tránh tạo Observer mới
    }

    @Override
    public void showDeleteConfirmDialog(NotificationHistory notification) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.notification_history_delete_confirm_title)
            .setMessage(R.string.notification_history_delete_confirm_message)
            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                presenter.deleteNotification(notification);
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    @Override
    public void showDeleteAllConfirmDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.notification_history_delete_confirm_title)
            .setMessage(R.string.notification_history_delete_all_confirm)
            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                ((NotificationHistoryPresenter) presenter).confirmDeleteAll();
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    @Override
    public void showDeleteReadConfirmDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.notification_history_delete_confirm_title)
            .setMessage(R.string.notification_history_delete_read_confirm)
            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                ((NotificationHistoryPresenter) presenter).confirmDeleteRead();
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    @Override
    public void navigateToContent(NotificationHistory notification) {
        NotificationType type = notification.getType();
        String targetId = notification.getTargetId();

        Log.d("NotificationHistory", "Navigate to content - Type: " + type + ", TargetId: " + targetId);

        if (type == null) {
            Toast.makeText(this, "Không thể mở nội dung", Toast.LENGTH_SHORT).show();
            return;
        }

        if (targetId == null || targetId.isEmpty()) {
            Toast.makeText(this, "Không có thông tin chi tiết", Toast.LENGTH_SHORT).show();
            Log.w("NotificationHistory", "TargetId is null or empty for notification type: " + type);
            return;
        }

        Intent intent = null;

        switch (type) {
            case NEW_HEALTH_TIP:
            case HEALTH_TIP_RECOMMENDATION:
                if (targetId != null && !targetId.isEmpty()) {
                    intent = new Intent(this, com.vhn.doan.presentation.healthtip.detail.HealthTipDetailActivity.class);
                    intent.putExtra("health_tip_id", targetId);
                }
                break;

            case NEW_VIDEO:
                if (targetId != null && !targetId.isEmpty()) {
                    intent = new Intent(this, com.vhn.doan.presentation.video.SingleVideoPlayerActivity.class);
                    intent.putExtra("video_id", targetId);
                }
                break;

            case COMMENT_REPLY:
            case COMMENT_LIKE:
                String videoId = notification.getTargetId();
                if (videoId != null && !videoId.isEmpty()) {
                    intent = new Intent(this, com.vhn.doan.presentation.video.SingleVideoPlayerActivity.class);
                    intent.putExtra("video_id", videoId);
                    intent.putExtra("open_comments", true);

                    // If we have extra data for specific comment
                    String extraData = notification.getExtraData();
                    if (extraData != null && !extraData.isEmpty()) {
                        try {
                            org.json.JSONObject json = new org.json.JSONObject(extraData);
                            if (json.has("comment_id")) {
                                intent.putExtra("scroll_to_comment", json.getString("comment_id"));
                            }
                        } catch (Exception e) {
                            Log.e("NotificationHistory", "Error parsing extra data", e);
                        }
                    }
                }
                break;

            case REMINDER_ALERT:
            case REMINDER_ALARM:
                if (targetId != null && !targetId.isEmpty()) {
                    intent = new Intent(this, com.vhn.doan.presentation.reminder.ReminderEditorActivity.class);
                    intent.putExtra("reminder_id", targetId);
                    intent.putExtra("mode", "edit");
                }
                break;

            case SYSTEM_UPDATE:
            case SYSTEM_MESSAGE:
                // Check if there's a deep link for system notifications
                String deepLink = notification.getDeepLink();
                if (deepLink != null && !deepLink.isEmpty()) {
                    try {
                        intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(deepLink));
                    } catch (Exception e) {
                        Toast.makeText(this, "Liên kết không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(this, notification.getBody(), Toast.LENGTH_LONG).show();
                    return;
                }
                break;

            case SUPPORT_REPLY:
                if (targetId != null && !targetId.isEmpty()) {
                    intent = new Intent(this, com.vhn.doan.presentation.support.TicketChatActivity.class);
                    intent.putExtra(com.vhn.doan.presentation.support.TicketChatActivity.EXTRA_TICKET_ID, targetId);
                }
                break;

            case ADMIN_REPLY:
                // New Report System
                if (targetId != null && !targetId.isEmpty()) {
                    intent = new Intent(this, com.vhn.doan.presentation.report.ReportChatActivity.class);
                    intent.putExtra(com.vhn.doan.presentation.report.ReportChatActivity.EXTRA_REPORT_ID, targetId);
                } else {
                    // Try to get reportId from extraData or deepLink
                    String extraData = notification.getExtraData();
                    if (extraData != null && !extraData.isEmpty()) {
                        try {
                            org.json.JSONObject json = new org.json.JSONObject(extraData);
                            if (json.has("reportId")) {
                                String reportId = json.getString("reportId");
                                intent = new Intent(this, com.vhn.doan.presentation.report.ReportChatActivity.class);
                                intent.putExtra(com.vhn.doan.presentation.report.ReportChatActivity.EXTRA_REPORT_ID, reportId);
                            }
                        } catch (Exception e) {
                            Log.e("NotificationHistory", "Error parsing extra data for ADMIN_REPLY", e);
                        }
                    }
                    if (intent == null) {
                        Toast.makeText(this, "Không tìm thấy ID báo cáo", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;

            default:
                Toast.makeText(this, "Không thể mở nội dung này", Toast.LENGTH_SHORT).show();
                return;
        }

        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public void stopRefreshing() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showLoadingMore() {
        isLoadingMore = true;
    }

    @Override
    public void hideLoadingMore() {
        isLoadingMore = false;
    }

    @Override
    public void showUndoSnackbar(String message, NotificationHistory notification) {
        runOnUiThread(() -> {
            Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.notification_history_undo, v -> {
                    presenter.undoDeleteNotification(notification);
                })
                .show();
        });
    }

    @Override
    public void showLoading() {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void hideLoading() {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            stopRefreshing();
        });
    }

    @Override
    public void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }
}
