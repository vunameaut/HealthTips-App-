package com.vhn.doan.presentation.support;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.vhn.doan.R;
import com.vhn.doan.data.SupportTicket;
import com.vhn.doan.data.repository.SupportRepository;
import com.vhn.doan.utils.FirebaseDebugHelper;

import java.util.List;

import javax.inject.Inject;

/**
 * Activity hiển thị danh sách Support Tickets của người dùng
 */
public class SupportTicketsActivity extends AppCompatActivity implements SupportContract.View {

    private static final String TAG = "SupportTicketsActivity";
    private static final int REQUEST_CREATE_TICKET = 1001;

    // Views
    private RecyclerView ticketsRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyStateLayout;
    private View loadingLayout;
    private ExtendedFloatingActionButton createTicketFab;

    // Adapter
    private SupportTicketsAdapter adapter;

    @Inject
    SupportPresenter presenter;

    @Inject
    SupportRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_tickets);

        // Debug Firebase data
        FirebaseDebugHelper.logCurrentUser();
        FirebaseDebugHelper.debugAllSupportTickets();
        FirebaseDebugHelper.debugUserSupportTickets();

        // Inject dependencies
        // TODO: Setup Dagger injection
        repository = new SupportRepository();
        presenter = new SupportPresenter(repository);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();

        presenter.attachView(this);
        presenter.loadUserTickets();
    }

    private void initViews() {
        ticketsRecyclerView = findViewById(R.id.ticketsRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        loadingLayout = findViewById(R.id.loadingLayout);
        createTicketFab = findViewById(R.id.createTicketFab);
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.my_tickets);
        }

        findViewById(R.id.toolbar).setOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new SupportTicketsAdapter();
        ticketsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ticketsRecyclerView.setAdapter(adapter);

        adapter.setOnTicketClickListener(ticket -> {
            navigateToTicketDetail(ticket.getTicketId());
        });
    }

    private void setupClickListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            presenter.loadUserTickets();
        });

        createTicketFab.setOnClickListener(v -> openCreateTicketScreen());

        View createFirstTicketButton = emptyStateLayout.findViewById(R.id.createFirstTicketButton);
        if (createFirstTicketButton != null) {
            createFirstTicketButton.setOnClickListener(v -> openCreateTicketScreen());
        }
    }

    private void openCreateTicketScreen() {
        Intent intent = new Intent(this, CreateSupportTicketActivity.class);
        startActivityForResult(intent, REQUEST_CREATE_TICKET);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CREATE_TICKET && resultCode == RESULT_OK) {
            // Reload tickets sau khi tạo ticket mới
            presenter.loadUserTickets();
        }
    }

    // SupportContract.View implementation

    @Override
    public void showLoading() {
        runOnUiThread(() -> {
            if (!swipeRefreshLayout.isRefreshing()) {
                loadingLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void hideLoading() {
        runOnUiThread(() -> {
            loadingLayout.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    @Override
    public void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void showTickets(List<SupportTicket> tickets) {
        runOnUiThread(() -> {
            adapter.setTickets(tickets);
            ticketsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        });
    }

    @Override
    public void showEmptyTickets() {
        runOnUiThread(() -> {
            ticketsRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void showTicketCreatedSuccess(String ticketId) {
        // Not used in this activity
    }

    @Override
    public void showTicketSubmitError(String error) {
        // Not used in this activity
    }

    @Override
    public void showScreenshotUploaded(String url) {
        // Not used in this activity
    }

    @Override
    public void showScreenshotUploadError(String error) {
        // Not used in this activity
    }

    @Override
    public void navigateToTicketDetail(String ticketId) {
        Intent intent = new Intent(this, TicketChatActivity.class);
        intent.putExtra(TicketChatActivity.EXTRA_TICKET_ID, ticketId);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload tickets khi quay lại activity
        if (presenter != null) {
            presenter.loadUserTickets();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

