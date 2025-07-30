package com.vhn.doan.presentation.reminder;

import android.app.AlertDialog;
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
import com.vhn.doan.presentation.base.BaseFragment;

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
    private FloatingActionButton fabAdd;
    private View emptyStateView;
    private View loadingView;

    // Adapter
    private ReminderAdapter adapter;

    // Search
    private SearchView searchView;
    private boolean showActiveOnly = false;

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
        setupListeners();

        presenter.attachView(this);
        presenter.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
        }
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

    @Override
    protected void setupListeners() {
        // Setup các listeners cụ thể
        setupSwipeRefresh();
        setupFab();
        if (searchView != null) {
            setupSearchView();
        }
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> presenter.refreshReminders());
        swipeRefresh.setColorSchemeResources(
            R.color.primary_color,
            R.color.primary_dark,
            R.color.accent_color
        );
    }

    private void setupFab() {
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
        ReminderDialog dialog = new ReminderDialog(getContext(), reminder, new ReminderDialog.OnReminderDialogListener() {
            @Override
            public void onReminderSaved(Reminder savedReminder) {
                presenter.saveReminder(savedReminder);
            }

            @Override
            public void onReminderCanceled() {
                // Không làm gì
            }
        });
        dialog.show();
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
}
