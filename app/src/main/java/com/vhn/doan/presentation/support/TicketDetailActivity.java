package com.vhn.doan.presentation.support;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.R;
import com.vhn.doan.data.SupportTicket;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Activity hiển thị chi tiết của một ticket hỗ trợ
 */
public class TicketDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TICKET_ID = "extra_ticket_id";

    private MaterialToolbar toolbar;
    private TextView tvTicketId;
    private TextView tvTicketSubject;
    private TextView tvDescription;
    private TextView tvCreatedDate;
    private TextView tvDeviceInfo;
    private TextView tvAdminResponse;
    private TextView tvResolvedDate;
    private Chip chipStatus;
    private Chip chipPriority;
    private Chip chipType;
    private ImageView ivScreenshot;
    private MaterialCardView cardDeviceInfo;
    private MaterialCardView cardAdminResponse;
    private ProgressBar progressBar;

    private DatabaseReference ticketsRef;
    private String ticketId;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

        initViews();
        setupToolbar();
        getTicketIdFromIntent();
        initFirebase();
        loadTicketDetails();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTicketId = findViewById(R.id.tvTicketId);
        tvTicketSubject = findViewById(R.id.tvTicketSubject);
        tvDescription = findViewById(R.id.tvDescription);
        tvCreatedDate = findViewById(R.id.tvCreatedDate);
        tvDeviceInfo = findViewById(R.id.tvDeviceInfo);
        tvAdminResponse = findViewById(R.id.tvAdminResponse);
        tvResolvedDate = findViewById(R.id.tvResolvedDate);
        chipStatus = findViewById(R.id.chipStatus);
        chipPriority = findViewById(R.id.chipPriority);
        chipType = findViewById(R.id.chipType);
        ivScreenshot = findViewById(R.id.ivScreenshot);
        cardDeviceInfo = findViewById(R.id.cardDeviceInfo);
        cardAdminResponse = findViewById(R.id.cardAdminResponse);
        progressBar = findViewById(R.id.progressBar);

        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void getTicketIdFromIntent() {
        ticketId = getIntent().getStringExtra(EXTRA_TICKET_ID);
        if (TextUtils.isEmpty(ticketId)) {
            Toast.makeText(this, "Không tìm thấy thông tin ticket", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initFirebase() {
        ticketsRef = FirebaseDatabase.getInstance()
                .getReference("support_tickets");
    }

    private void loadTicketDetails() {
        showLoading(true);

        // Load ticket và verify nó thuộc về user hiện tại
        ticketsRef.child(ticketId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showLoading(false);
                if (snapshot.exists()) {
                    // Parse từ Map để xử lý đúng Enum
                    Object value = snapshot.getValue();
                    SupportTicket ticket = null;

                    if (value instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> map = (java.util.Map<String, Object>) value;
                        ticket = SupportTicket.fromMap(map);
                    } else {
                        ticket = snapshot.getValue(SupportTicket.class);
                    }

                    if (ticket != null) {
                        // Verify ticket thuộc về user hiện tại (bảo mật)
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

                        if (!currentUserId.isEmpty() && currentUserId.equals(ticket.getUserId())) {
                            displayTicketDetails(ticket);
                        } else {
                            Toast.makeText(TicketDetailActivity.this,
                                    "Bạn không có quyền xem ticket này", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(TicketDetailActivity.this,
                                "Không thể đọc dữ liệu ticket", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(TicketDetailActivity.this,
                            "Không tìm thấy ticket", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(TicketDetailActivity.this,
                        "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayTicketDetails(SupportTicket ticket) {
        // Ticket ID
        tvTicketId.setText(getString(R.string.ticket_id) + ": #" + ticket.getTicketId());

        // Subject
        tvTicketSubject.setText(ticket.getSubject());

        // Description
        tvDescription.setText(ticket.getDescription());

        // Created Date
        if (ticket.getCreatedAt() != null) {
            tvCreatedDate.setText(getString(R.string.ticket_created_at) + ": " +
                    dateFormat.format(ticket.getCreatedAt()));
        }

        // Status Chip
        setupStatusChip(ticket.getStatus());

        // Priority Chip
        setupPriorityChip(ticket.getPriority());

        // Type Chip
        setupTypeChip(ticket.getType());

        // Screenshot
        if (!TextUtils.isEmpty(ticket.getScreenshotUrl())) {
            ivScreenshot.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(ticket.getScreenshotUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(ivScreenshot);

            // Click to view fullscreen
            ivScreenshot.setOnClickListener(v -> showFullscreenImage(ticket.getScreenshotUrl()));
        } else {
            ivScreenshot.setVisibility(View.GONE);
        }

        // Device Info
        if (!TextUtils.isEmpty(ticket.getDeviceInfo()) || !TextUtils.isEmpty(ticket.getAppVersion())) {
            cardDeviceInfo.setVisibility(View.VISIBLE);
            StringBuilder deviceInfo = new StringBuilder();
            if (!TextUtils.isEmpty(ticket.getDeviceInfo())) {
                deviceInfo.append(ticket.getDeviceInfo());
            }
            if (!TextUtils.isEmpty(ticket.getAppVersion())) {
                if (deviceInfo.length() > 0) {
                    deviceInfo.append("\n");
                }
                deviceInfo.append("App version: ").append(ticket.getAppVersion());
            }
            tvDeviceInfo.setText(deviceInfo.toString());
        } else {
            cardDeviceInfo.setVisibility(View.GONE);
        }

        // Admin Response
        if (!TextUtils.isEmpty(ticket.getAdminResponse())) {
            cardAdminResponse.setVisibility(View.VISIBLE);
            tvAdminResponse.setText(ticket.getAdminResponse());
            if (ticket.getResolvedAt() != null) {
                tvResolvedDate.setText(dateFormat.format(ticket.getResolvedAt()));
            }
        } else {
            cardAdminResponse.setVisibility(View.GONE);
        }
    }

    private void setupStatusChip(SupportTicket.TicketStatus status) {
        if (status == null) {
            status = SupportTicket.TicketStatus.OPEN;
        }

        chipStatus.setText(status.getDisplayName());

        switch (status) {
            case OPEN:
                chipStatus.setChipBackgroundColorResource(R.color.info_color);
                break;
            case IN_PROGRESS:
                chipStatus.setChipBackgroundColorResource(R.color.warning_color);
                break;
            case RESOLVED:
                chipStatus.setChipBackgroundColorResource(R.color.success_color);
                break;
            case CLOSED:
                chipStatus.setChipBackgroundColorResource(R.color.grey_600);
                break;
        }
    }

    private void setupPriorityChip(SupportTicket.Priority priority) {
        if (priority == null) {
            priority = SupportTicket.Priority.MEDIUM;
        }

        chipPriority.setText(priority.getDisplayName());

        switch (priority) {
            case LOW:
                chipPriority.setChipBackgroundColorResource(R.color.grey_500);
                break;
            case MEDIUM:
                chipPriority.setChipBackgroundColorResource(R.color.warning_color);
                break;
            case HIGH:
                chipPriority.setChipBackgroundColorResource(R.color.error_color);
                break;
            case URGENT:
                chipPriority.setChipBackgroundColorResource(R.color.status_due);
                break;
        }
    }

    private void setupTypeChip(SupportTicket.TicketType type) {
        if (type == null) {
            type = SupportTicket.TicketType.OTHER;
        }

        chipType.setText(type.getDisplayName());
        chipType.setChipBackgroundColorResource(R.color.grey_600);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showFullscreenImage(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            return;
        }

        // Tạo dialog để hiển thị ảnh fullscreen
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_fullscreen_image, null);
        ImageView fullscreenImageView = dialogView.findViewById(R.id.fullscreenImageView);

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(fullscreenImageView);

        builder.setView(dialogView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();

        // Click vào ảnh để đóng dialog
        fullscreenImageView.setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.black);
        }

        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}

