package com.vhn.doan.presentation.reminder;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vhn.doan.R;
import com.vhn.doan.data.Reminder;
import com.vhn.doan.data.repository.ReminderRepository;
import com.vhn.doan.data.repository.ReminderRepositoryImpl;
import com.vhn.doan.utils.UserSessionManager;
import com.vhn.doan.utils.PermissionHelper;
import com.vhn.doan.presentation.base.BaseFragment;
import com.vhn.doan.services.NotificationService;
import com.vhn.doan.services.ReminderService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Fragment hiển thị danh sách nhắc nhở theo kiến trúc MVP
 */
public class ReminderFragment extends BaseFragment implements ReminderContract.View {

    private ReminderPresenter presenter;

    // UI Components
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabAdd;
    private View emptyStateView;
    private View loadingView;

    // Adapter
    private ReminderAdapter adapter;

    // Search
    private SearchView searchView;
    private boolean showActiveOnly = false;

    // Permission handling
    private boolean isPermissionChecked = false;

    // Broadcast receiver cho reminder status changes
    private BroadcastReceiver reminderStatusReceiver;

    public static ReminderFragment newInstance() {
        return new ReminderFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Khởi tạo presenter thủ công thay vì dùng @Inject
        initPresenter();
    }

    /**
     * Khởi tạo presenter với các dependencies cần thiết
     */
    private void initPresenter() {
        ReminderRepository reminderRepository = new ReminderRepositoryImpl();
        UserSessionManager userSessionManager = new UserSessionManager(requireContext());
        presenter = new ReminderPresenter(reminderRepository, userSessionManager);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupFloatingActionButton();

        // Attach presenter và start
        presenter.attachView(this);
        presenter.start();

        // Kiểm tra và yêu cầu quyền cần thiết cho reminder
        checkReminderPermissions();

        // Đăng ký receiver để lắng nghe thay đổi trạng thái reminder
        registerReminderStatusReceiver();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Hủy đăng ký broadcast receiver
        unregisterReminderStatusReceiver();

        if (presenter != null) {
            presenter.detachView();
        }
    }

    /**
     * Đăng ký BroadcastReceiver để lắng nghe thay đổi trạng thái reminder
     */
    private void registerReminderStatusReceiver() {
        if (getContext() == null) return;

        reminderStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("REMINDER_STATUS_CHANGED".equals(intent.getAction())) {
                    handleReminderStatusChanged(intent);
                }
            }
        };

        IntentFilter filter = new IntentFilter("REMINDER_STATUS_CHANGED");

        // Sửa lỗi SecurityException cho Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ yêu cầu chỉ định RECEIVER_EXPORTED hoặc RECEIVER_NOT_EXPORTED
            // Sử dụng RECEIVER_NOT_EXPORTED vì đây là broadcast nội bộ app
            getContext().registerReceiver(reminderStatusReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            // Android cũ hơn sử dụng cách đăng ký truyền thống
            getContext().registerReceiver(reminderStatusReceiver, filter);
        }

        android.util.Log.d("ReminderFragment", "📡 Đã đăng ký lắng nghe broadcast reminder status");
    }

    /**
     * Hủy đăng ký BroadcastReceiver
     */
    private void unregisterReminderStatusReceiver() {
        if (getContext() != null && reminderStatusReceiver != null) {
            try {
                getContext().unregisterReceiver(reminderStatusReceiver);
                android.util.Log.d("ReminderFragment", "📡 Đã hủy đăng ký broadcast receiver");
            } catch (IllegalArgumentException e) {
                // Receiver đã được hủy đăng ký trước đó
                android.util.Log.w("ReminderFragment", "Receiver đã được hủy đăng ký: " + e.getMessage());
            }
        }
        reminderStatusReceiver = null;
    }

    /**
     * Xử lý khi nhận được broadcast thay đổi trạng thái reminder
     */
    private void handleReminderStatusChanged(Intent intent) {
        try {
            String reminderId = intent.getStringExtra("reminder_id");
            String reminderTitle = intent.getStringExtra("reminder_title");
            boolean isActive = intent.getBooleanExtra("is_active", true);
            String reason = intent.getStringExtra("reason");

            android.util.Log.d("ReminderFragment", "🔄 Nhận broadcast: " + reminderId + " - Active: " + isActive + " - Reason: " + reason);

            if ("auto_disabled_after_notification".equals(reason)) {
                // Hiển thị thông báo cho người dùng biết reminder đã tự động tắt
                showSuccess("Nhắc nhở \"" + reminderTitle + "\" đã hoàn thành và tự động tắt");
            }

            // Refresh danh sách để cập nhật UI
            if (presenter != null) {
                presenter.refreshReminders();
            }

        } catch (Exception e) {
            android.util.Log.e("ReminderFragment", "❌ Lỗi khi xử lý broadcast: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra quyền và bắt đầu load dữ liệu
     */
    private void checkPermissionsAndStart() {
        if (isPermissionChecked) {
            // Đã kiểm tra quyền rồi, chỉ start presenter
            presenter.start();
            return;
        }

        if (PermissionHelper.hasReminderPermissions(requireContext())) {
            // Đã có đủ quyền
            isPermissionChecked = true;
            presenter.start();
        } else {
            // Chưa có đủ quyền, hiển thị dialog yêu cầu
            showPermissionDialog();
        }
    }

    /**
     * Hiển thị dialog yêu cầu cấp quyền
     */
    private void showPermissionDialog() {
        PermissionHelper.showPermissionExplanationDialog(this, new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionsGranted() {
                isPermissionChecked = true;
                showSuccess("Đã cấp quyền thành công!");
                presenter.start();
            }

            @Override
            public void onPermissionsDenied(List<String> deniedPermissions) {
                isPermissionChecked = true;
                showWarningAboutMissingPermissions(deniedPermissions);
                // Vẫn cho phép sử dụng app nhưng cảnh báo tính năng sẽ bị hạn chế
                presenter.start();
            }
        });
    }

    /**
     * Hiển thị cảnh báo về quyền bị thiếu
     */
    private void showWarningAboutMissingPermissions(List<String> deniedPermissions) {
        if (getContext() == null) return;

        String message = "Quyền thông báo chưa được cấp. Tính năng nhắc nhở có thể không hoạt động đúng:\n\n" +
                "• Không thể hiển thị thông báo nhắc nhở\n\n" +
                "Bạn có thể cấp quyền sau bằng cách vào Cài đặt > Ứng dụng > HealthTips > Quyền";

        new AlertDialog.Builder(getContext())
                .setTitle("Cảnh báo quyền")
                .setMessage(message)
                .setPositiveButton("Đã hiểu", null)
                .setNeutralButton("Thử lại", (dialog, which) -> {
                    isPermissionChecked = false;
                    checkPermissionsAndStart();
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.handlePermissionResult(this, requestCode, permissions, grantResults);
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_reminder, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();

        setupSearchView();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_filter_active) {
            showActiveOnly = !showActiveOnly;
            updateFilterMenuItem(item);
            presenter.filterReminders(showActiveOnly);
            return true;
        } else if (itemId == R.id.action_refresh) {
            presenter.refreshReminders();
            return true;
        } else if (itemId == R.id.action_debug_notifications) {
            openDebugActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_reminders);
        swipeRefresh = view.findViewById(R.id.swipe_refresh_reminders);
        fabAdd = view.findViewById(R.id.fab_add_reminder);
        emptyStateView = view.findViewById(R.id.layout_empty_state);
        loadingView = view.findViewById(R.id.layout_loading);

        // Setup button trong Empty State
        Button btnCreateFirstReminder = view.findViewById(R.id.btn_create_first_reminder);
        if (btnCreateFirstReminder != null) {
            btnCreateFirstReminder.setOnClickListener(v -> presenter.createReminder());
        }

        // Setup Debug Button
        com.google.android.material.button.MaterialButton btnDebug = view.findViewById(R.id.btn_debug_notifications);
        if (btnDebug != null) {
            btnDebug.setOnClickListener(v -> openDebugActivity());
        }
    }

    private void setupRecyclerView() {
        adapter = new ReminderAdapter(new ArrayList<>(), new ReminderAdapter.OnReminderItemClickListener() {
            @Override
            public void onReminderClick(Reminder reminder) {
                presenter.editReminder(reminder);
            }

            @Override
            public void onToggleClick(Reminder reminder) {
                presenter.toggleReminder(reminder);
            }

            @Override
            public void onDeleteClick(Reminder reminder) {
                presenter.deleteReminder(reminder);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> presenter.refreshReminders());
        swipeRefresh.setColorSchemeResources(
            R.color.primary_color,
            R.color.primary_dark,
            R.color.accent_color
        );
    }

    private void setupFloatingActionButton() {
        fabAdd.setOnClickListener(v -> presenter.createReminder());
    }

    private void setupSearchView() {
        if (searchView != null) {
            searchView.setQueryHint("Tìm kiếm nhắc nhở...");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    presenter.searchReminders(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    presenter.searchReminders(newText);
                    return true;
                }
            });
        }
    }

    private void updateFilterMenuItem(MenuItem item) {
        if (showActiveOnly) {
            item.setTitle("Hiển thị tất cả");
            item.setIcon(R.drawable.ic_filter_list);
        } else {
            item.setTitle("Chỉ hiển thị đang hoạt động");
            item.setIcon(R.drawable.ic_filter_list_off);
        }
    }

    /**
     * Mở ReminderTestActivity để debug hệ thống thông báo
     */
    private void openDebugActivity() {
        if (getContext() != null) {
            android.content.Intent intent = new android.content.Intent(getContext(),
                com.vhn.doan.presentation.debug.ReminderTestActivity.class);
            startActivity(intent);
        }
    }

    // Implement ReminderContract.View methods

    @Override
    public void showReminders(List<Reminder> reminders) {
        if (adapter != null) {
            adapter.updateReminders(reminders);
        }
    }

    @Override
    public void showLoading() {
        if (loadingView != null) {
            loadingView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideLoading() {
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }

        if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void showSuccess(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showReminderDialog(Reminder reminder) {
        ReminderDialog dialog = new ReminderDialog(getContext(), new ReminderDialog.OnReminderDialogListener() {
            @Override
            public void onReminderSaved(Reminder savedReminder) {
                presenter.saveReminder(savedReminder);
            }

            @Override
            public void onReminderDeleted(String reminderId) {
                // Xử lý xóa reminder nếu cần
            }
        });

        if (reminder == null) {
            dialog.showCreateDialog();
        } else {
            dialog.showEditDialog(reminder);
        }
    }

    @Override
    public void showDeleteConfirmDialog(Reminder reminder) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa nhắc nhở \"" + reminder.getTitle() + "\"?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                if (presenter != null) {
                    ((ReminderPresenter) presenter).confirmDeleteReminder(reminder);
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    @Override
    public void updateReminderItem(Reminder reminder) {
        if (adapter != null) {
            adapter.updateReminder(reminder);
        }
    }

    @Override
    public void removeReminderItem(Reminder reminder) {
        if (adapter != null) {
            adapter.removeReminder(reminder);
        }
    }

    @Override
    public void addReminderItem(Reminder reminder) {
        if (adapter != null) {
            adapter.addReminder(reminder);
        }
    }

    @Override
    public void showEmptyState() {
        if (emptyStateView != null) {
            emptyStateView.setVisibility(View.VISIBLE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideEmptyState() {
        if (emptyStateView != null) {
            emptyStateView.setVisibility(View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Method public để Activity có thể gọi khi click button từ XML
     * Sửa lỗi: IllegalStateException khi click nút tạo reminder
     */
    public void onCreateReminderClick() {
        if (presenter != null) {
            presenter.createReminder();
        }
    }

    /**
     * ✅ THÊM: Khôi phục lại tất cả nhắc nhở khi mở app
     */
    private void restoreRemindersIfNeeded() {
        try {
            android.util.Log.d("ReminderFragment", "🔄 Khôi phục nhắc nhở khi mở fragment...");

            // Sử dụng BootReceiver để khôi phục lại tất cả nhắc nhở
            if (getContext() != null) {
                com.vhn.doan.receivers.BootReceiver.rescheduleAllReminders(getContext());
                android.util.Log.d("ReminderFragment", "✅ Đã yêu cầu khôi phục nhắc nhở");
            }
        } catch (Exception e) {
            android.util.Log.e("ReminderFragment", "❌ Lỗi khi khôi phục nhắc nhở", e);
        }
    }

    /**
     * Kiểm tra và yêu cầu tất cả quyền cần thiết cho reminder
     */
    private void checkReminderPermissions() {
        com.vhn.doan.utils.ReminderPermissionHelper.checkAndRequestAllPermissions(this,
            new com.vhn.doan.utils.ReminderPermissionHelper.ReminderPermissionCallback() {
                @Override
                public void onAllPermissionsGranted() {
                    android.util.Log.d("ReminderFragment", "✅ Tất cả quyền đã được cấp");
                    // Khởi động foreground service để duy trì hoạt động
                    com.vhn.doan.utils.ReminderPermissionHelper.startReminderService(requireContext());
                    showSuccess("Hệ thống nhắc nhở đã sẵn sàng!");
                }

                @Override
                public void onNotificationPermissionResult(boolean granted) {
                    if (granted) {
                        android.util.Log.d("ReminderFragment", "✅ Quyền thông báo đã được cấp");
                        // Tiếp tục kiểm tra quyền khác
                        checkReminderPermissions();
                    } else {
                        android.util.Log.w("ReminderFragment", "❌ Quyền thông báo bị từ chối");
                        showError("Cần cấp quyền thông báo để nhắc nhở hoạt động");
                    }
                }

                @Override
                public void onBatteryOptimizationDenied() {
                    android.util.Log.w("ReminderFragment", "⚠️ Battery optimization không được tắt");
                    showError("Nhắc nhở có thể không hoạt động khi app bị tắt hoàn toàn");
                    // Vẫn khởi động service
                    com.vhn.doan.utils.ReminderPermissionHelper.startReminderService(requireContext());
                }

                @Override
                public void onError(String error) {
                    android.util.Log.e("ReminderFragment", "❌ Lỗi khi kiểm tra quyền: " + error);
                    showError("Lỗi khi thiết lập quyền: " + error);
                }
            });
    }

    @Override
    protected void setupListeners() {
        // Setup listeners cho các UI components
        setupSwipeRefresh();
        setupFloatingActionButton();
    }
}
