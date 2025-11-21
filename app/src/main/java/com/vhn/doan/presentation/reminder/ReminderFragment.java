package com.vhn.doan.presentation.reminder;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
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
import com.vhn.doan.presentation.base.FragmentVisibilityListener;
import com.vhn.doan.services.NotificationService;
import com.vhn.doan.services.ReminderService;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị danh sách nhắc nhở theo kiến trúc MVP
 */
public class ReminderFragment extends BaseFragment implements ReminderContract.View, FragmentVisibilityListener {

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
    public void onResume() {
        super.onResume();
        // IMPORTANT: Refresh lại danh sách mỗi khi quay lại fragment
        // Đảm bảo UI luôn sync với database (đặc biệt sau khi dismiss alarm)
        // Auto-disable expired reminders sẽ được gọi tự động trong loadReminders()
        android.util.Log.d("ReminderFragment", "🔄 onResume: Force refresh danh sách và auto-check expired reminders");
        if (presenter != null) {
            presenter.refreshReminders();
        }
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

            // FIX: Không refresh ngay từ Firebase vì có thể chưa sync
            // Chỉ update UI local, để onResume() hoặc handleForceRefresh() xử lý refresh từ Firebase
            android.util.Log.d("ReminderFragment", "🔄 Chỉ update UI local, không refresh từ Firebase ngay");

            // Force update adapter ngay lập tức với data local đã update
            if (adapter != null) {
                android.util.Log.d("ReminderFragment", "🔄 Force notify adapter...");
                adapter.notifyDataSetChanged();
                android.util.Log.d("ReminderFragment", "✅ Đã gọi adapter.notifyDataSetChanged()");
            } else {
                android.util.Log.e("ReminderFragment", "❌ Adapter is null!");
            }

            // Delay refresh từ Firebase để đảm bảo sync
            if (presenter != null) {
                new Handler().postDelayed(() -> {
                    if (isAdded() && presenter != null) {
                        android.util.Log.d("ReminderFragment", "🔄 Bắt đầu refresh từ Firebase sau khi dismiss...");
                        presenter.refreshReminders();
                        android.util.Log.d("ReminderFragment", "✅ Đã gọi presenter.refreshReminders() sau delay");
                    }
                }, 1500); // Đợi 1500ms để Firebase sync
            }

        } catch (Exception e) {
            android.util.Log.e("ReminderFragment", "❌ Lỗi khi xử lý broadcast: " + e.getMessage(), e);
        }
    }

    /**
     * Xử lý broadcast force refresh danh sách
     * SIMPLIFIED: Chỉ đơn giản refresh lại danh sách
     * Auto-disable logic sẽ tự động xử lý việc tắt reminders đã hết hạn
     */
    private void handleForceRefresh(Intent intent) {
        try {
            String refreshReason = intent.getStringExtra("refresh_reason");
            String reminderId = intent.getStringExtra("reminder_id");
            android.util.Log.d("ReminderFragment", "🔄 Force refresh UI - Lý do: " + refreshReason + ", ID: " + reminderId);

            // Đơn giản chỉ cần refresh - auto-disable sẽ tự động xử lý
            if (presenter != null) {
                android.util.Log.d("ReminderFragment", "🔄 Refresh ngay - auto-disable sẽ tự động check và tắt expired reminders");
                presenter.refreshReminders();
            }

        } catch (Exception e) {
            android.util.Log.e("ReminderFragment", "❌ Lỗi khi force refresh: " + e.getMessage(), e);
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

            // Setup Sort Button với null check
            android.widget.ImageButton btnSort = view.findViewById(R.id.btn_sort_reminders);
            if (btnSort != null) {
                btnSort.setOnClickListener(v -> showSortDialog());
            }

            // Setup Filter Chips
            setupFilterChips(view);

            // Setup Button "Thêm nhắc nhở đầu tiên" trong empty state
            com.google.android.material.button.MaterialButton btnAddFirst = view.findViewById(R.id.btn_add_first_reminder);
            if (btnAddFirst != null) {
                btnAddFirst.setOnClickListener(v -> {
                    if (presenter != null) {
                        presenter.createReminder();
                    }
                });
            }

            android.util.Log.d("ReminderFragment", "✅ Views initialized successfully");
        } catch (Exception e) {
            android.util.Log.e("ReminderFragment", "❌ Error initializing views: " + e.getMessage());
            showError("Lỗi khởi tạo giao diện: " + e.getMessage());
        }
    }

    /**
     * Setup filter chips (Tất cả, Hoạt động, Không hoạt động)
     */
    private void setupFilterChips(View view) {
        com.google.android.material.chip.Chip chipAll = view.findViewById(R.id.chip_all);
        com.google.android.material.chip.Chip chipActive = view.findViewById(R.id.chip_active);
        com.google.android.material.chip.Chip chipInactive = view.findViewById(R.id.chip_inactive);

        if (chipAll != null) {
            chipAll.setOnClickListener(v -> {
                showActiveOnly = false;
                if (presenter != null) {
                    presenter.loadReminders(); // Load tất cả
                }
            });
        }

        if (chipActive != null) {
            chipActive.setOnClickListener(v -> {
                showActiveOnly = true;
                if (presenter != null) {
                    presenter.filterReminders(true); // Chỉ hiển thị active
                }
            });
        }

        if (chipInactive != null) {
            chipInactive.setOnClickListener(v -> {
                showActiveOnly = false;
                if (presenter != null) {
                    presenter.filterReminders(false); // Chỉ hiển thị inactive
                }
            });
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

                @Override
                public void onEditClick(Reminder reminder) {
                    if (presenter != null && reminder != null) {
                        presenter.editReminder(reminder);
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
                    // Không show toast để tránh duplicate khi recreate activity
                    com.vhn.doan.utils.ReminderPermissionHelper.startReminderService(requireContext());
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
    public void showExpiredReminderDialog(Reminder reminder) {
        if (getContext() == null) return;

        // Format thời gian để hiển thị
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("HH:mm - dd/MM/yyyy", java.util.Locale.getDefault());
        String expiredTime = dateFormat.format(new java.util.Date(reminder.getReminderTime()));

        new AlertDialog.Builder(getContext())
            .setTitle("⚠️ Nhắc nhở đã qua thời gian")
            .setMessage("Nhắc nhở \"" + reminder.getTitle() + "\" có thời gian đã qua:\n\n" +
                       "⏰ " + expiredTime + "\n\n" +
                       "Vui lòng chỉnh lại thời gian mới để bật nhắc nhở này.")
            .setPositiveButton("Chỉnh sửa ngay", (dialog, which) -> {
                if (presenter != null) {
                    // Mở màn hình chỉnh sửa để người dùng cập nhật thời gian
                    presenter.editReminder(reminder);
                }
            })
            .setNegativeButton("Để sau", (dialog, which) -> {
                // Refresh lại item để đảm bảo switch về trạng thái cũ (không bật)
                if (adapter != null) {
                    adapter.updateReminder(reminder);
                }
            })
            .setOnCancelListener(dialog -> {
                // Refresh lại item khi user nhấn back
                if (adapter != null) {
                    adapter.updateReminder(reminder);
                }
            })
            .setCancelable(true)
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
     * Update số lượng nhắc nhở active từ adapter ngay lập tức
     * Dùng khi cần update count mà không cần load lại từ presenter
     */
    private void updateActiveCountFromAdapter() {
        try {
            if (adapter == null) {
                android.util.Log.w("ReminderFragment", "⚠️ Adapter is null, cannot update count");
                return;
            }

            List<Reminder> reminders = adapter.getReminders();
            int activeCount = 0;
            for (Reminder r : reminders) {
                if (r != null && r.isActive()) {
                    activeCount++;
                }
            }

            // Cập nhật UI
            TextView tvActiveCount = getView() != null ? getView().findViewById(R.id.tv_active_count) : null;
            if (tvActiveCount != null) {
                tvActiveCount.setText(String.valueOf(activeCount));
                android.util.Log.d("ReminderFragment", "✅ Đã cập nhật count ngay lập tức: " + activeCount);
            } else {
                android.util.Log.w("ReminderFragment", "⚠️ TextView tv_active_count not found");
            }
        } catch (Exception e) {
            android.util.Log.e("ReminderFragment", "❌ Lỗi khi update count: " + e.getMessage(), e);
        }
    }


    @Override
    public void onFragmentVisible() {
        // Được gọi khi fragment được hiển thị
        // Tải lại danh sách reminder để cập nhật UI
        if (presenter != null) {
            presenter.loadReminders();
        }
    }

    @Override
    public void onFragmentHidden() {
        // Được gọi khi fragment bị ẩn
        // Có thể dừng các tác vụ đang chạy nếu cần
    }
}
