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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.vhn.doan.R;
import com.vhn.doan.data.Reminder;
import com.vhn.doan.data.ReminderSortType;
import com.vhn.doan.data.repository.ReminderRepository;
import com.vhn.doan.data.repository.ReminderRepositoryImpl;
import com.vhn.doan.utils.UserSessionManager;
import com.vhn.doan.utils.PermissionHelper;
import com.vhn.doan.presentation.base.BaseFragment;
import com.vhn.doan.services.NotificationService;
import com.vhn.doan.services.ReminderService;

import java.util.ArrayList;
import java.util.List;

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
                String action = intent.getAction();
                android.util.Log.d("ReminderFragment", "📡 Nhận broadcast: " + action);

                if ("REMINDER_STATUS_CHANGED".equals(action)) {
                    handleReminderStatusChanged(intent);
                } else if ("REMINDER_LIST_REFRESH".equals(action)) {
                    // Force refresh toàn bộ danh sách nhắc nhở
                    handleForceRefresh(intent);
                } else if ("REMINDER_ERROR".equals(action)) {
                    handleReminderError(intent);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("REMINDER_STATUS_CHANGED");
        filter.addAction("REMINDER_LIST_REFRESH");
        filter.addAction("REMINDER_ERROR");

        // Sửa lỗi SecurityException cho Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            getContext().registerReceiver(reminderStatusReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
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

            android.util.Log.d("ReminderFragment", "🔄 ✅ NHẬN ĐƯỢC BROADCAST: " + reminderId + " - Active: " + isActive + " - Reason: " + reason);

            if ("auto_disabled_after_notification".equals(reason)) {
                // Hiển thị thông báo cho người dùng biết reminder ��ã tự động tắt
                showSuccess("Nhắc nhở \"" + reminderTitle + "\" đã hoàn thành và tự động tắt");
                android.util.Log.d("ReminderFragment", "✅ Đã hiển thị thông báo tự động tắt");
            }

            // QUAN TRỌNG: Force refresh ngay lập tức
            android.util.Log.d("ReminderFragment", "🔄 Bắt đầu force refresh presenter...");

            // Refresh danh sách để cập nhật UI
            if (presenter != null) {
                presenter.refreshReminders();
                android.util.Log.d("ReminderFragment", "✅ Đã gọi presenter.refreshReminders()");
            } else {
                android.util.Log.e("ReminderFragment", "❌ Presenter is null!");
            }

            // Force update adapter ngay lập tức
            if (adapter != null) {
                android.util.Log.d("ReminderFragment", "🔄 Force notify adapter...");
                adapter.notifyDataSetChanged();
                android.util.Log.d("ReminderFragment", "✅ Đã gọi adapter.notifyDataSetChanged()");
            } else {
                android.util.Log.e("ReminderFragment", "❌ Adapter is null!");
            }

        } catch (Exception e) {
            android.util.Log.e("ReminderFragment", "❌ Lỗi khi xử lý broadcast: " + e.getMessage(), e);
        }
    }

    /**
     * Xử lý broadcast force refresh danh sách
     */
    private void handleForceRefresh(Intent intent) {
        try {
            String refreshReason = intent.getStringExtra("refresh_reason");
            android.util.Log.d("ReminderFragment", "🔄 Force refresh UI - Lý do: " + refreshReason);

            // Force refresh danh sách nhắc nhở ngay lập tức
            if (presenter != null) {
                presenter.refreshReminders();
                android.util.Log.d("ReminderFragment", "✅ Đã trigger refresh presenter");
            }

            // Cập nhật UI ngay lập tức nếu có adapter
            if (adapter != null) {
                adapter.notifyDataSetChanged();
                android.util.Log.d("ReminderFragment", "✅ Đã notify adapter update");
            }

        } catch (Exception e) {
            android.util.Log.e("ReminderFragment", "❌ Lỗi khi force refresh: " + e.getMessage());
        }
    }

    /**
     * Xử lý broadcast lỗi reminder
     */
    private void handleReminderError(Intent intent) {
        try {
            String reminderId = intent.getStringExtra("reminder_id");
            String errorMessage = intent.getStringExtra("error_message");

            android.util.Log.e("ReminderFragment", "❌ Nhận lỗi reminder: " + reminderId + " - " + errorMessage);

            // Hiển thị thông báo lỗi cho người dùng
            showError("Lỗi với nhắc nhở: " + errorMessage);

        } catch (Exception e) {
            android.util.Log.e("ReminderFragment", "❌ Lỗi khi xử lý error broadcast: " + e.getMessage());
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
        try {
            recyclerView = view.findViewById(R.id.recycler_view_reminders);
            swipeRefresh = view.findViewById(R.id.swipe_refresh_reminders);
            fabAdd = view.findViewById(R.id.fab_add_reminder);
            emptyStateView = view.findViewById(R.id.layout_empty_state);
            loadingView = view.findViewById(R.id.layout_loading);

            // Setup Debug Button với null check
            com.google.android.material.button.MaterialButton btnDebug = view.findViewById(R.id.btn_debug_notifications);
            if (btnDebug != null) {
                btnDebug.setOnClickListener(v -> openDebugActivity());
            }

            // Setup Sort Button với null check
            com.google.android.material.button.MaterialButton btnSort = view.findViewById(R.id.btn_sort_reminders);
            if (btnSort != null) {
                btnSort.setOnClickListener(v -> showSortDialog());
            }

            // ĐÃ BỎ TẤT CẢ CÁC NÚT THÊM NHẮC NHỞ VÀ CÀI ĐẶT - CHỈ SỬ DỤNG FAB

            android.util.Log.d("ReminderFragment", "✅ Views initialized successfully");
        } catch (Exception e) {
            android.util.Log.e("ReminderFragment", "❌ Error initializing views: " + e.getMessage());
            showError("Lỗi khởi tạo giao diện: " + e.getMessage());
        }
    }

    private void setupRecyclerView() {
        try {
            if (recyclerView == null) {
                android.util.Log.e("ReminderFragment", "RecyclerView is null!");
                return;
            }

            adapter = new ReminderAdapter(new ArrayList<>(), new ReminderAdapter.OnReminderItemClickListener() {
                @Override
                public void onReminderClick(Reminder reminder) {
                    if (presenter != null && reminder != null) {
                        presenter.editReminder(reminder);
                    }
                }

                @Override
                public void onToggleClick(Reminder reminder) {
                    if (presenter != null && reminder != null) {
                        presenter.toggleReminder(reminder);
                    }
                }

                @Override
                public void onDeleteClick(Reminder reminder) {
                    if (presenter != null && reminder != null) {
                        presenter.deleteReminder(reminder);
                    }
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
            recyclerView.setHasFixedSize(true);

            android.util.Log.d("ReminderFragment", "✅ RecyclerView setup successfully");
        } catch (Exception e) {
            android.util.Log.e("ReminderFragment", "❌ Error setting up RecyclerView: " + e.getMessage());
            showError("Lỗi thiết lập danh sách: " + e.getMessage());
        }
    }

    private void setupSwipeRefresh() {
        try {
            if (swipeRefresh == null) {
                android.util.Log.w("ReminderFragment", "SwipeRefreshLayout is null!");
                return;
            }

            swipeRefresh.setOnRefreshListener(() -> {
                if (presenter != null) {
                    presenter.refreshReminders();
                } else {
                    swipeRefresh.setRefreshing(false);
                }
            });

            swipeRefresh.setColorSchemeResources(
                R.color.primary_color,
                R.color.primary_dark,
                R.color.accent_color
            );

            android.util.Log.d("ReminderFragment", "✅ SwipeRefresh setup successfully");
        } catch (Exception e) {
            android.util.Log.e("ReminderFragment", "❌ Error setting up SwipeRefresh: " + e.getMessage());
        }
    }

    private void setupFloatingActionButton() {
        try {
            if (fabAdd == null) {
                android.util.Log.w("ReminderFragment", "FAB is null!");
                return;
            }

            fabAdd.setOnClickListener(v -> {
                if (presenter != null) {
                    presenter.createReminder();
                } else {
                    showError("Hệ thống chưa sẵn sàng, vui lòng thử lại sau");
                }
            });

            android.util.Log.d("ReminderFragment", "✅ FAB setup successfully");
        } catch (Exception e) {
            android.util.Log.e("ReminderFragment", "❌ Error setting up FAB: " + e.getMessage());
        }
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
            // Thêm tùy chọn debug với test data
            new AlertDialog.Builder(getContext())
                .setTitle("🔧 Debug Options")
                .setItems(new String[]{
                    "🧪 Tạo dữ liệu test (3 nhắc nhở mẫu)",
                    "🔄 Force Refresh UI",
                    "📊 Check Active Count",
                    "💾 Refresh từ Database",
                    "🚨 Test Notifications",
                    "🗑️ Xóa tất cả test data"
                }, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // Tạo dữ liệu test
                            createTestReminderData();
                            break;
                        case 1:
                            // Force refresh UI
                            android.util.Log.d("ReminderFragment", "🔄 DEBUG: Force refresh UI");
                            if (presenter != null) {
                                presenter.refreshReminders();
                            }
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            }
                            showSuccess("Đã force refresh UI");
                            break;
                        case 2:
                            // Check active count
                            checkDebugInfo();
                            break;
                        case 3:
                            // Refresh từ database
                            android.util.Log.d("ReminderFragment", "🔄 DEBUG: Refresh từ database");
                            if (presenter != null) {
                                presenter.loadReminders(); // Load lại từ database
                            }
                            showSuccess("Đã refresh từ database");
                            break;
                        case 4:
                            // Test notifications
                            testNotifications();
                            break;
                        case 5:
                            // Xóa test data
                            clearTestData();
                            break;
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
        }
    }

    /**
     * Kiểm tra thông tin debug chi tiết
     */
    private void checkDebugInfo() {
        try {
            StringBuilder info = new StringBuilder("🔍 Debug Information:\n\n");

            // Kiểm tra adapter
            if (adapter != null) {
                info.append("📋 Adapter: OK\n");
                info.append("📊 Item count: ").append(adapter.getItemCount()).append("\n");
            } else {
                info.append("❌ Adapter: NULL\n");
            }

            // Kiểm tra RecyclerView
            if (recyclerView != null) {
                info.append("📱 RecyclerView: OK\n");
                info.append("👀 Visibility: ").append(recyclerView.getVisibility() == View.VISIBLE ? "VISIBLE" : "HIDDEN").append("\n");
            } else {
                info.append("❌ RecyclerView: NULL\n");
            }

            // Kiểm tra Presenter
            if (presenter != null) {
                info.append("🧠 Presenter: OK\n");
                try {
                    int activeCount = presenter.getActiveReminderCount();
                    int totalCount = presenter.getTotalReminderCount();
                    info.append("📈 Active/Total: ").append(activeCount).append("/").append(totalCount).append("\n");
                } catch (Exception e) {
                    info.append("⚠️ Presenter count error: ").append(e.getMessage()).append("\n");
                }
            } else {
                info.append("❌ Presenter: NULL\n");
            }

            // Kiểm tra View states
            if (emptyStateView != null) {
                info.append("📭 Empty State: ").append(emptyStateView.getVisibility() == View.VISIBLE ? "VISIBLE" : "HIDDEN").append("\n");
            }

            if (loadingView != null) {
                info.append("⏳ Loading View: ").append(loadingView.getVisibility() == View.VISIBLE ? "VISIBLE" : "HIDDEN").append("\n");
            }

            android.util.Log.d("ReminderFragment", info.toString());

            new AlertDialog.Builder(getContext())
                .setTitle("🔍 Debug Info")
                .setMessage(info.toString())
                .setPositiveButton("OK", null)
                .show();

        } catch (Exception e) {
            android.util.Log.e("ReminderFragment", "❌ Lỗi khi check debug info: " + e.getMessage());
            showError("Lỗi debug: " + e.getMessage());
        }
    }

    /**
     * Test notifications
     */
    private void testNotifications() {
        try {
            android.content.Intent intent = new android.content.Intent(getContext(),
                com.vhn.doan.presentation.debug.ReminderTestActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            android.util.Log.e("ReminderFragment", "❌ ReminderTestActivity không tồn tại: " + e.getMessage());
            showError("ReminderTestActivity không tồn tại");
        }
    }

    /**
     * Xóa tất cả dữ liệu test
     */
    private void clearTestData() {
        new AlertDialog.Builder(getContext())
            .setTitle("⚠️ Xóa Test Data")
            .setMessage("Bạn có chắc muốn xóa tất cả dữ liệu test không?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                // Tạo danh sách rỗng để xóa test data
                showReminders(new ArrayList<>());
                showSuccess("Đã xóa tất cả test data");
                android.util.Log.d("ReminderFragment", "🗑️ Đã xóa test data");
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    /**
     * Khôi phục nhắc nhở khi mở app
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
                        // Ti��p tục kiểm tra quyền khác
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

    /**
     * Hiển thị dialog sắp xếp danh sách nhắc nhở
     */
    private void showSortDialog() {
        if (getContext() == null || adapter == null) return;

        // Lấy tất cả các kiểu sắp xếp có sẵn
        ReminderSortType[] sortTypes = ReminderSortType.values();
        String[] sortOptions = new String[sortTypes.length];

        for (int i = 0; i < sortTypes.length; i++) {
            sortOptions[i] = sortTypes[i].getDisplayName();
        }

        // Tìm kiểu sắp xếp hiện tại
        ReminderSortType currentSort = adapter.getCurrentSortType();
        int currentIndex = 0;
        for (int i = 0; i < sortTypes.length; i++) {
            if (sortTypes[i] == currentSort) {
                currentIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(getContext())
            .setTitle("🔄 Sắp xếp danh sách nhắc nhở")
            .setSingleChoiceItems(sortOptions, currentIndex, null)
            .setPositiveButton("Áp dụng", (dialog, which) -> {
                // Lấy lựa chọn của người dùng
                int selectedIndex = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                if (selectedIndex >= 0 && selectedIndex < sortTypes.length) {
                    ReminderSortType selectedSort = sortTypes[selectedIndex];

                    // Áp dụng sắp xếp
                    adapter.sortReminders(selectedSort);

                    // Hiển thị thông báo thành công
                    showSuccess("Đã sắp xếp theo: " + selectedSort.getDisplayName());

                    android.util.Log.d("ReminderFragment", "✅ Đã sắp xếp nhắc nhở theo: " + selectedSort.getDisplayName());
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    /**
     * Mở màn hình cài đặt nhắc nhở
     */
    private void openReminderSettings() {
        if (getContext() == null) return;

        // Vì ReminderSettingsActivity chưa tồn tại, sử dụng fallback dialog
        android.util.Log.i("ReminderFragment", "ReminderSettingsActivity chưa được triển khai, sử dụng dialog cài đặt cơ bản");
        showBasicSettingsDialog();
    }

    /**
     * Hiển thị dialog cài đặt cơ bản khi không có ReminderSettingsActivity
     */
    private void showBasicSettingsDialog() {
        if (getContext() == null) return;

        String[] settings = {
            "Cài đặt âm thanh thông báo",
            "Cài đặt thời gian báo trước",
            "Cài đặt t�� động tắt nhắc nhở",
            "Cài đặt quyền ứng dụng",
            "Xuất danh sách nhắc nhở"
        };

        new AlertDialog.Builder(getContext())
            .setTitle("Cài đặt nhắc nhở")
            .setItems(settings, (dialog, which) -> {
                switch (which) {
                    case 0:
                        openSoundSettings();
                        break;
                    case 1:
                        showAdvanceTimeSettings();
                        break;
                    case 2:
                        showAutoDisableSettings();
                        break;
                    case 3:
                        openAppPermissionSettings();
                        break;
                    case 4:
                        exportReminders();
                        break;
                }
            })
            .setNegativeButton("Đóng", null)
            .show();
    }

    /**
     * Mở cài đặt âm thanh thông báo
     */
    private void openSoundSettings() {
        try {
            // Thay vì sử dụng SoundSelectionActivity không tồn tại,
            // mở cài đặt âm thanh hệ thống
            Intent intent = new Intent(android.provider.Settings.ACTION_SOUND_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            android.util.Log.e("ReminderFragment", "❌ Không thể mở cài đặt âm thanh hệ thống: " + e.getMessage());

            // Fallback: Hiển thị dialog cài đặt âm thanh cơ bản
            showSoundSettingsDialog();
        }
    }

    /**
     * Hiển thị dialog cài đặt âm thanh cơ bản
     */
    private void showSoundSettingsDialog() {
        if (getContext() == null) return;

        String[] soundOptions = {
            "Âm thanh mặc định",
            "Âm thanh nhẹ nhàng",
            "Âm thanh cảnh báo",
            "Chỉ rung",
            "Im lặng"
        };

        android.content.SharedPreferences prefs = getContext()
            .getSharedPreferences("reminder_settings", Context.MODE_PRIVATE);
        int currentSound = prefs.getInt("notification_sound_type", 0);

        new AlertDialog.Builder(getContext())
            .setTitle("Cài đặt âm thanh thông báo")
            .setSingleChoiceItems(soundOptions, currentSound, null)
            .setPositiveButton("Lưu", (dialog, which) -> {
                int selectedIndex = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                prefs.edit().putInt("notification_sound_type", selectedIndex).apply();
                showSuccess("Đã lưu cài đặt âm thanh: " + soundOptions[selectedIndex]);
                android.util.Log.d("ReminderFragment", "✅ Đã lưu âm thanh: " + selectedIndex);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    /**
     * Hiển thị cài đặt thời gian báo trước
     */
    private void showAdvanceTimeSettings() {
        if (getContext() == null) return;

        String[] timeOptions = {
            "5 phút trước",
            "10 phút trước",
            "15 phút trước",
            "30 phút trước",
            "1 giờ trước",
            "1 ngày trước"
        };

        new AlertDialog.Builder(getContext())
            .setTitle("Thời gian báo trước")
            .setSingleChoiceItems(timeOptions, 2, null) // Default: 15 phút
            .setPositiveButton("Lưu", (dialog, which) -> {
                // Lưu cài đặt thời gian báo trước
                int selectedIndex = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                saveAdvanceTimeSetting(selectedIndex);
                showSuccess("Đã lưu cài đặt thời gian báo trước");
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    /**
     * Lưu cài đặt thời gian báo trước
     */
    private void saveAdvanceTimeSetting(int selectedIndex) {
        if (getContext() == null) return;

        int[] timeInMinutes = {5, 10, 15, 30, 60, 1440}; // 1440 = 24 hours
        int advanceTime = timeInMinutes[selectedIndex];

        // Lưu vào SharedPreferences
        android.content.SharedPreferences prefs = getContext()
            .getSharedPreferences("reminder_settings", Context.MODE_PRIVATE);
        prefs.edit()
            .putInt("advance_time_minutes", advanceTime)
            .apply();

        android.util.Log.d("ReminderFragment", "✅ Đã lưu thời gian báo trước: " + advanceTime + " phút");
    }

    /**
     * Hiển thị cài đặt tự động tắt nhắc nhở
     */
    private void showAutoDisableSettings() {
        if (getContext() == null) return;

        android.content.SharedPreferences prefs = getContext()
            .getSharedPreferences("reminder_settings", Context.MODE_PRIVATE);
        boolean currentAutoDisable = prefs.getBoolean("auto_disable_after_notification", true);

        new AlertDialog.Builder(getContext())
            .setTitle("Tự động tắt nhắc nhở")
            .setMessage("Tự động tắt nhắc nhở sau khi hiển thị thông báo?\n\n" +
                      "• Bật: Nhắc nhở sẽ tự động tắt sau khi thông báo\n" +
                      "• Tắt: Nhắc nhở sẽ tiếp tục hoạt động theo lịch")
            .setPositiveButton("Bật", (dialog, which) -> {
                prefs.edit().putBoolean("auto_disable_after_notification", true).apply();
                showSuccess("Đã bật tự động tắt nhắc nhở");
            })
            .setNegativeButton("Tắt", (dialog, which) -> {
                prefs.edit().putBoolean("auto_disable_after_notification", false).apply();
                showSuccess("Đã tắt tự động tắt nhắc nhở");
            })
            .setNeutralButton("Hủy", null)
            .show();
    }

    /**
     * Mở cài đặt quyền ứng dụng
     */
    private void openAppPermissionSettings() {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            android.net.Uri uri = android.net.Uri.fromParts("package", getContext().getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        } catch (Exception e) {
            android.util.Log.e("ReminderFragment", "❌ Không thể mở cài đặt quyền: " + e.getMessage());
            showError("Không thể mở cài đặt quyền");
        }
    }

    /**
     * Xuất danh sách nhắc nhở
     */
    private void exportReminders() {
        if (presenter != null) {
            presenter.exportReminders();
            showSuccess("Đang xuất danh sách nhắc nhở...");
        } else {
            showError("Không thể xuất dữ liệu lúc này");
        }
    }

    // Implement ReminderContract.View methods

    @Override
    public void showReminders(List<Reminder> reminders) {
        android.util.Log.d("ReminderFragment", "📋 showReminders called with " +
            (reminders != null ? reminders.size() : 0) + " items");

        if (reminders != null && !reminders.isEmpty()) {
            for (int i = 0; i < reminders.size(); i++) {
                Reminder reminder = reminders.get(i);
                android.util.Log.d("ReminderFragment", "📋 Reminder " + i + ": " +
                    "Title=" + (reminder != null ? reminder.getTitle() : "null") +
                    ", ID=" + (reminder != null ? reminder.getId() : "null"));
            }
        } else {
            android.util.Log.w("ReminderFragment", "⚠️ Reminders list is null or empty!");
        }

        // Ẩn loading và empty state trước khi hiển thị dữ liệu
        hideLoading();
        if (emptyStateView != null) {
            emptyStateView.setVisibility(View.GONE);
        }

        if (adapter != null) {
            adapter.updateReminders(reminders);
            android.util.Log.d("ReminderFragment", "✅ Adapter updated with reminders");

            // Hiển thị empty state nếu không có dữ liệu
            if (reminders == null || reminders.isEmpty()) {
                if (emptyStateView != null) {
                    emptyStateView.setVisibility(View.VISIBLE);
                }
                android.util.Log.d("ReminderFragment", "📭 Showing empty state");
            }
        } else {
            android.util.Log.e("ReminderFragment", "❌ Adapter is null!");
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
        // Sử dụng ReminderEditorActivity thay vì ReminderDialog
        Intent intent = new Intent(getContext(), ReminderEditorActivity.class);

        if (reminder != null) {
            // Edit mode
            intent.putExtra(ReminderEditorActivity.EXTRA_IS_EDIT_MODE, true);
            intent.putExtra(ReminderEditorActivity.EXTRA_REMINDER_ID, reminder.getId());
        } else {
            // Create mode
            intent.putExtra(ReminderEditorActivity.EXTRA_IS_EDIT_MODE, false);
        }

        startActivityForResult(intent, REQUEST_CODE_REMINDER_EDITOR);
    }

    private static final int REQUEST_CODE_REMINDER_EDITOR = 1001;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_REMINDER_EDITOR && resultCode == android.app.Activity.RESULT_OK) {
            // Refresh danh sách sau khi lưu reminder
            android.util.Log.d("ReminderFragment", "✅ Reminder đã được lưu, refresh danh sách");
            if (presenter != null) {
                presenter.refreshReminders();
            }
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

    @Override
    public void updateActiveReminderCount(int activeCount) {
        // Cập nhật số lượng nhắc nhở đang hoạt động trên header
        TextView tvActiveCount = getView() != null ? getView().findViewById(R.id.tv_active_count) : null;
        if (tvActiveCount != null) {
            tvActiveCount.setText(String.valueOf(activeCount));
            android.util.Log.d("ReminderFragment", "📊 Đã cập nhật UI: " + activeCount + " nhắc nhở đang hoạt động");
        } else {
            android.util.Log.w("ReminderFragment", "⚠️ Không tìm thấy TextView tv_active_count để cập nhật số l��ợng");
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
     * DEBUG: Tạo dữ liệu nhắc nhở mẫu để test
     */
    private void createTestReminderData() {
        android.util.Log.d("ReminderFragment", "🧪 Tạo dữ liệu nhắc nhở mẫu để test...");

        try {
            // Tạo reminder mẫu
            Reminder testReminder1 = new Reminder();
            testReminder1.setId("test_1_" + System.currentTimeMillis());
            testReminder1.setTitle("Uống thuốc huyết áp");
            testReminder1.setDescription("Uống thuốc huyết áp vào buổi sáng sau bữa ăn");
            testReminder1.setReminderTime(System.currentTimeMillis() + (2 * 60 * 1000)); // 2 phút nữa
            testReminder1.setActive(true);
            testReminder1.setRepeatType(Reminder.RepeatType.DAILY);
            testReminder1.setCreatedAt(System.currentTimeMillis());
            testReminder1.setUpdatedAt(System.currentTimeMillis());

            Reminder testReminder2 = new Reminder();
            testReminder2.setId("test_2_" + System.currentTimeMillis());
            testReminder2.setTitle("Tập thể dục");
            testReminder2.setDescription("Đi bộ 30 phút vào buổi chiều");
            testReminder2.setReminderTime(System.currentTimeMillis() + (5 * 60 * 1000)); // 5 phút nữa
            testReminder2.setActive(true);
            testReminder2.setRepeatType(Reminder.RepeatType.DAILY);
            testReminder2.setCreatedAt(System.currentTimeMillis());
            testReminder2.setUpdatedAt(System.currentTimeMillis());

            Reminder testReminder3 = new Reminder();
            testReminder3.setId("test_3_" + System.currentTimeMillis());
            testReminder3.setTitle("Kiểm tra huyết áp");
            testReminder3.setDescription("Đo huyết áp và ghi vào sổ theo dõi");
            testReminder3.setReminderTime(System.currentTimeMillis() + (10 * 60 * 1000)); // 10 phút nữa
            testReminder3.setActive(false); // Reminder này tắt
            testReminder3.setRepeatType(Reminder.RepeatType.WEEKLY);
            testReminder3.setCreatedAt(System.currentTimeMillis());
            testReminder3.setUpdatedAt(System.currentTimeMillis());

            // Tạo danh sách test
            List<Reminder> testReminders = new ArrayList<>();
            testReminders.add(testReminder1);
            testReminders.add(testReminder2);
            testReminders.add(testReminder3);

            android.util.Log.d("ReminderFragment", "✅ Đã tạo " + testReminders.size() + " reminder mẫu");

            // Gọi trực tiếp showReminders để test UI
            showReminders(testReminders);

            showSuccess("Đã tạo " + testReminders.size() + " nhắc nhở mẫu cho test!");

        } catch (Exception e) {
            android.util.Log.e("ReminderFragment", "❌ Lỗi khi tạo test data: " + e.getMessage(), e);
            showError("Lỗi khi tạo dữ liệu test: " + e.getMessage());
        }
    }
}
