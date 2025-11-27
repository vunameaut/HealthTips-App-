package com.vhn.doan.presentation.settings.account;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.R;

import java.util.ArrayList;
import java.util.List;

/**
 * ✅ IMPLEMENTED: Activity để quản lý danh sách người dùng bị chặn
 */
public class BlockedUsersActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView recyclerViewBlockedUsers;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private BlockedUsersAdapter adapter;
    private List<BlockedUser> blockedUsersList;

    private FirebaseAuth mAuth;
    private DatabaseReference blockedUsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_users);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            blockedUsersRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("blocked_users");
        }

        setupViews();
        loadBlockedUsers();
    }

    private void setupViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewBlockedUsers = findViewById(R.id.recyclerViewBlockedUsers);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Setup RecyclerView
        blockedUsersList = new ArrayList<>();
        adapter = new BlockedUsersAdapter(blockedUsersList, new BlockedUsersAdapter.OnUnblockListener() {
            @Override
            public void onUnblock(BlockedUser user) {
                showUnblockConfirmDialog(user);
            }
        });

        recyclerViewBlockedUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBlockedUsers.setAdapter(adapter);
    }

    private void loadBlockedUsers() {
        if (blockedUsersRef == null) {
            showEmptyState();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        recyclerViewBlockedUsers.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);

        blockedUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                blockedUsersList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BlockedUser user = snapshot.getValue(BlockedUser.class);
                    if (user != null) {
                        user.setUserId(snapshot.getKey());
                        blockedUsersList.add(user);
                    }
                }

                progressBar.setVisibility(View.GONE);

                if (blockedUsersList.isEmpty()) {
                    showEmptyState();
                } else {
                    recyclerViewBlockedUsers.setVisibility(View.VISIBLE);
                    tvEmptyState.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(BlockedUsersActivity.this,
                        "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        recyclerViewBlockedUsers.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.VISIBLE);
        tvEmptyState.setText("Bạn chưa chặn người dùng nào");
    }

    private void showUnblockConfirmDialog(BlockedUser user) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Bỏ chặn người dùng")
            .setMessage("Bạn có chắc chắn muốn bỏ chặn " + user.getDisplayName() + "?")
            .setPositiveButton("Bỏ chặn", (dialog, which) -> {
                unblockUser(user);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void unblockUser(BlockedUser user) {
        if (blockedUsersRef == null || user.getUserId() == null) {
            return;
        }

        blockedUsersRef.child(user.getUserId()).removeValue()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Đã bỏ chặn " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    /**
     * Model class cho blocked user
     */
    public static class BlockedUser {
        private String userId;
        private String displayName;
        private String email;
        private long blockedAt;

        public BlockedUser() {
            // Required for Firebase
        }

        public BlockedUser(String userId, String displayName, String email, long blockedAt) {
            this.userId = userId;
            this.displayName = displayName;
            this.email = email;
            this.blockedAt = blockedAt;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getDisplayName() {
            return displayName != null ? displayName : "Unknown User";
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public long getBlockedAt() {
            return blockedAt;
        }

        public void setBlockedAt(long blockedAt) {
            this.blockedAt = blockedAt;
        }

        public String getFormattedDate() {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
            return sdf.format(new java.util.Date(blockedAt));
        }
    }
}
