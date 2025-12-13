package com.vhn.doan.presentation.settings.support;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.R;
import com.vhn.doan.model.SupportTicket;
import com.vhn.doan.presentation.settings.support.adapter.SupportTicketAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MySupportTicketsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyView;
    private SupportTicketAdapter adapter;
    private List<SupportTicket> ticketList;

    private DatabaseReference issuesRef;
    private DatabaseReference userNotificationsRef;
    private FirebaseAuth firebaseAuth;
    private ValueEventListener issuesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_support_tickets);

        // Handle edge-to-edge display and notch
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        issuesRef = FirebaseDatabase.getInstance().getReference("support_tickets");
        userNotificationsRef = FirebaseDatabase.getInstance().getReference("user_notifications");

        setupViews();
        setupRecyclerView();
        loadTickets();
        listenForUserNotifications();
    }

    private void setupViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
    }

    private void setupRecyclerView() {
        ticketList = new ArrayList<>();
        adapter = new SupportTicketAdapter(this, ticketList);
        recyclerView.setAdapter(adapter);
    }

    private void loadTickets() {
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = firebaseAuth.getCurrentUser().getUid();
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        Query query = issuesRef.orderByChild("userId").equalTo(userId);

        issuesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ticketList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    SupportTicket ticket = child.getValue(SupportTicket.class);
                    if (ticket != null) {
                        ticket.setId(child.getKey());
                        ticketList.add(ticket);
                    }
                }

                // Sort by timestamp descending (newest first)
                Collections.sort(ticketList, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                if (ticketList.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    emptyView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MySupportTicketsActivity.this,
                        R.string.error_loading_tickets, Toast.LENGTH_SHORT).show();
            }
        };

        query.addValueEventListener(issuesListener);
    }

    private void listenForUserNotifications() {
        if (firebaseAuth.getCurrentUser() == null) return;

        String userId = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference userNotifRef = userNotificationsRef.child(userId);

        userNotifRef.orderByChild("createdAt").limitToLast(1)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Check for new admin responses
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Object readObj = child.child("read").getValue();
                        boolean isRead = readObj != null && (Boolean) readObj;

                        if (!isRead) {
                            String title = child.child("title").getValue(String.class);
                            String message = child.child("message").getValue(String.class);

                            if (title != null) {
                                Toast.makeText(MySupportTicketsActivity.this,
                                        title + ": " + message,
                                        Toast.LENGTH_LONG).show();

                                // Mark as read
                                child.getRef().child("read").setValue(true);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Ignore
                }
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (issuesListener != null && issuesRef != null) {
            issuesRef.removeEventListener(issuesListener);
        }
    }
}
