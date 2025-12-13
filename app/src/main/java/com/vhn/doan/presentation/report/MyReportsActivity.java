package com.vhn.doan.presentation.report;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.R;
import com.vhn.doan.data.model.Report;
import com.vhn.doan.data.repository.ReportRepository;
import com.vhn.doan.data.repository.ReportRepositoryImpl;
import com.vhn.doan.presentation.report.adapter.ReportAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Activity hiển thị danh sách Reports của người dùng
 * Cho phép lọc theo trạng thái và mở chat cho từng report
 */
public class MyReportsActivity extends AppCompatActivity implements ReportAdapter.OnReportClickListener {

    private static final String TAG = "MyReportsActivity";

    // Views
    private MaterialToolbar toolbar;
    private ChipGroup chipGroupFilter;
    private Chip chipAll, chipPending, chipInProgress, chipResolved;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvReports;
    private LinearLayout emptyState;
    private FrameLayout loadingState;
    private MaterialButton btnCreateFirstReport;
    private ExtendedFloatingActionButton fabCreateReport;

    // Data
    private ReportAdapter adapter;
    private ReportRepository reportRepository;
    private FirebaseAuth firebaseAuth;
    private List<Report> allReports = new ArrayList<>();
    private String currentFilter = null; // null = tất cả

    // Activity Result Launchers
    private final ActivityResultLauncher<Intent> createReportLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Reload reports sau khi tạo mới
                    loadReports();
                }
            }
    );

    private final ActivityResultLauncher<Intent> reportChatLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Reload reports sau khi chat (có thể có thay đổi status)
                loadReports();
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reports);

        initViews();
        initData();
        setupListeners();
        setupRecyclerView();
        loadReports();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        chipAll = findViewById(R.id.chipAll);
        chipPending = findViewById(R.id.chipPending);
        chipInProgress = findViewById(R.id.chipInProgress);
        chipResolved = findViewById(R.id.chipResolved);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvReports = findViewById(R.id.rvReports);
        emptyState = findViewById(R.id.emptyState);
        loadingState = findViewById(R.id.loadingState);
        btnCreateFirstReport = findViewById(R.id.btnCreateFirstReport);
        fabCreateReport = findViewById(R.id.fabCreateReport);
    }

    private void initData() {
        reportRepository = new ReportRepositoryImpl();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void setupListeners() {
        // Toolbar back button
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Filter chips
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                chipAll.setChecked(true);
                return;
            }

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) {
                currentFilter = null;
            } else if (checkedId == R.id.chipPending) {
                currentFilter = Report.STATUS_PENDING;
            } else if (checkedId == R.id.chipInProgress) {
                currentFilter = Report.STATUS_IN_PROGRESS;
            } else if (checkedId == R.id.chipResolved) {
                currentFilter = Report.STATUS_RESOLVED;
            }
            applyFilter();
        });

        // Swipe refresh
        swipeRefresh.setOnRefreshListener(this::loadReports);

        // Create report buttons
        btnCreateFirstReport.setOnClickListener(v -> openCreateReport());
        fabCreateReport.setOnClickListener(v -> openCreateReport());

        // RecyclerView scroll để ẩn/hiện FAB
        rvReports.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    fabCreateReport.shrink();
                } else if (dy < 0) {
                    fabCreateReport.extend();
                }
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ReportAdapter(this);
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        rvReports.setAdapter(adapter);
    }

    private void loadReports() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.please_login_first, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading();

        reportRepository.getReportsByUserId(currentUser.getUid(), new ReportRepository.Callback<List<Report>>() {
            @Override
            public void onSuccess(List<Report> reports) {
                runOnUiThread(() -> {
                    hideLoading();
                    swipeRefresh.setRefreshing(false);
                    allReports = reports;
                    applyFilter();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideLoading();
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(MyReportsActivity.this, 
                        getString(R.string.error_loading_reports, error), 
                        Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void applyFilter() {
        List<Report> filteredReports;
        
        if (currentFilter == null) {
            filteredReports = allReports;
        } else {
            filteredReports = allReports.stream()
                    .filter(r -> currentFilter.equals(r.getStatus()))
                    .collect(Collectors.toList());
        }

        adapter.submitList(filteredReports);
        updateEmptyState(filteredReports.isEmpty());
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            rvReports.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvReports.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private void showLoading() {
        loadingState.setVisibility(View.VISIBLE);
        rvReports.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingState.setVisibility(View.GONE);
    }

    private void openCreateReport() {
        Intent intent = new Intent(this, CreateReportActivity.class);
        createReportLauncher.launch(intent);
    }

    @Override
    public void onReportClick(Report report) {
        // Mở ReportChatActivity
        Intent intent = new Intent(this, ReportChatActivity.class);
        intent.putExtra(ReportChatActivity.EXTRA_REPORT_ID, report.getId());
        reportChatLauncher.launch(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Setup realtime listener
        setupRealtimeListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        reportRepository.removeAllListeners();
    }

    private void setupRealtimeListener() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) return;

        reportRepository.addUserReportsListener(currentUser.getUid(), 
            new ReportRepository.Callback<List<Report>>() {
                @Override
                public void onSuccess(List<Report> reports) {
                    runOnUiThread(() -> {
                        allReports = reports;
                        applyFilter();
                    });
                }

                @Override
                public void onError(String error) {
                    // Silent fail for realtime updates
                }
            }
        );
    }
}

