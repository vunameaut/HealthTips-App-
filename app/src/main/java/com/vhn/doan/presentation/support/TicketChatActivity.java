package com.vhn.doan.presentation.support;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.R;
import com.vhn.doan.model.SupportTicket;
import com.vhn.doan.model.SupportMessage;
import com.vhn.doan.utils.CloudinaryHelper;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Activity for support ticket chat with admin
 * - Displays ticket info and chat messages
 * - Allows user to send messages (text + image)
 * - Real-time message updates
 * - Notifies admin when user sends message
 */
public class TicketChatActivity extends AppCompatActivity {

    private static final String TAG = "TicketChatActivity";
    public static final String EXTRA_TICKET_ID = "extra_ticket_id";
    private static final int REQUEST_PICK_IMAGE = 1001;

    // Views
    private MaterialToolbar toolbar;
    private TextView tvTicketSubject;
    private TextView tvTicketId;
    private Chip chipStatus;
    private RecyclerView recyclerViewMessages;
    private LinearLayout layoutEmptyMessages;
    private ProgressBar progressBar;
    private EditText etMessage;
    private FloatingActionButton btnSend;
    private ImageButton btnAttachImage;
    private RelativeLayout layoutImagePreview;
    private ImageView ivImagePreview;
    private ImageButton btnRemoveImage;

    // Data
    private String ticketId;
    private SupportTicket ticket;
    private List<SupportMessage> messages;
    private SupportChatAdapter adapter;

    // Firebase
    private DatabaseReference ticketsRef;
    private DatabaseReference messagesRef;
    private ChildEventListener messagesListener;

    // Image selection
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_chat);

        initViews();
        setupToolbar();
        getTicketIdFromIntent();
        initFirebase();
        setupRecyclerView();
        setupClickListeners();
        loadTicketInfo();
        loadMessages();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTicketSubject = findViewById(R.id.tvTicketSubject);
        tvTicketId = findViewById(R.id.tvTicketId);
        chipStatus = findViewById(R.id.chipStatus);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        layoutEmptyMessages = findViewById(R.id.layoutEmptyMessages);
        progressBar = findViewById(R.id.progressBar);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnAttachImage = findViewById(R.id.btnAttachImage);
        layoutImagePreview = findViewById(R.id.layoutImagePreview);
        ivImagePreview = findViewById(R.id.ivImagePreview);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);

        messages = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void getTicketIdFromIntent() {
        try {
            ticketId = getIntent().getStringExtra(EXTRA_TICKET_ID);
            Log.d(TAG, "Received ticket ID: " + ticketId);
            if (TextUtils.isEmpty(ticketId)) {
                Toast.makeText(this, "Ticket ID is empty", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting ticket ID: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading ticket: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initFirebase() {
        ticketsRef = FirebaseDatabase.getInstance().getReference("support_tickets");
        messagesRef = FirebaseDatabase.getInstance()
                .getReference("support_tickets")
                .child(ticketId)
                .child("messages");
    }

    private void setupRecyclerView() {
        adapter = new SupportChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(adapter);

        // Handle image click
        adapter.setOnMessageImageClickListener(this::showFullscreenImage);
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
        btnAttachImage.setOnClickListener(v -> pickImage());
        btnRemoveImage.setOnClickListener(v -> removeSelectedImage());
    }

    private void loadTicketInfo() {
        showLoading(true);
        ticketsRef.child(ticketId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showLoading(false);
                if (snapshot.exists()) {
                    ticket = snapshot.getValue(SupportTicket.class);
                    if (ticket != null) {
                        ticket.setId(ticketId); // Set ID from the key
                        displayTicketInfo();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(TicketChatActivity.this,
                        "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayTicketInfo() {
        try {
            if (ticket == null) {
                Log.e(TAG, "Ticket is null");
                Toast.makeText(this, "Ticket data is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String subject = ticket.getSubject();
            if (subject != null && !subject.isEmpty()) {
                tvTicketSubject.setText(subject);
            } else {
                tvTicketSubject.setText("No subject");
            }

            String ticketIdStr = ticket.getId();
            if (ticketIdStr != null && !ticketIdStr.isEmpty()) {
                tvTicketId.setText("Ticket #" + ticketIdStr.substring(0, Math.min(8, ticketIdStr.length())).toUpperCase());
            } else {
                tvTicketId.setText("Ticket #UNKNOWN");
            }

            setupStatusChip(ticket.getStatus());
        } catch (Exception e) {
            Log.e(TAG, "Error displaying ticket info: " + e.getMessage(), e);
            Toast.makeText(this, "Error displaying ticket: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupStatusChip(String status) {
        if (status == null || status.isEmpty()) {
            status = "open";
        }

        // Set display text based on status
        switch (status.toLowerCase()) {
            case "open":
                chipStatus.setText("Open");
                chipStatus.setChipBackgroundColorResource(R.color.info_color);
                break;
            case "in_progress":
                chipStatus.setText("In Progress");
                chipStatus.setChipBackgroundColorResource(R.color.warning_color);
                break;
            case "resolved":
                chipStatus.setText("Resolved");
                chipStatus.setChipBackgroundColorResource(R.color.success_color);
                break;
            case "closed":
                chipStatus.setText("Closed");
                chipStatus.setChipBackgroundColorResource(R.color.grey_600);
                break;
            default:
                chipStatus.setText("Pending");
                chipStatus.setChipBackgroundColorResource(R.color.info_color);
                break;
        }
    }

    private void loadMessages() {
        // Add real-time listener for new messages
        messagesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                SupportMessage message = snapshot.getValue(SupportMessage.class);
                if (message != null) {
                    message.setId(snapshot.getKey());
                    messages.add(message);
                    adapter.setMessages(messages);
                    scrollToBottom();
                    updateEmptyState();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Handle message updates if needed
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Handle message removal if needed
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Not used
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load messages: " + error.getMessage());
            }
        };

        messagesRef.addChildEventListener(messagesListener);
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();

        // Validate: Must have either text or image
        if (TextUtils.isEmpty(messageText) && selectedImageUri == null) {
            Toast.makeText(this, "Please enter a message or select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable send button
        btnSend.setEnabled(false);
        showLoading(true);

        // If has image, upload to Cloudinary first
        if (selectedImageUri != null) {
            uploadImageAndSendMessage(messageText);
        } else {
            sendMessageToFirebase(messageText, null);
        }
    }

    private void uploadImageAndSendMessage(String messageText) {
        CloudinaryHelper.uploadSupportImage(this, selectedImageUri, new CloudinaryHelper.CloudinaryUploadCallback() {
            @Override
            public void onUploadStart() {
                // Upload started
            }

            @Override
            public void onUploadProgress(int progress) {
                // Can show progress if needed
            }

            @Override
            public void onUploadSuccess(String imageUrl) {
                runOnUiThread(() -> {
                    removeSelectedImage();
                    sendMessageToFirebase(messageText, imageUrl);
                });
            }

            @Override
            public void onUploadError(String errorMessage) {
                runOnUiThread(() -> {
                    showLoading(false);
                    btnSend.setEnabled(true);
                    Toast.makeText(TicketChatActivity.this,
                            "Image upload error: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void sendMessageToFirebase(String messageText, String imageUrl) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        String userName = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "User";

        if (TextUtils.isEmpty(userName)) {
            userName = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        }

        final String finalUserName = userName;

        SupportMessage message = new SupportMessage();
        message.setText(messageText);
        message.setImageUrl(imageUrl);
        message.setSenderId(userId);
        message.setSenderType("user");
        message.setSenderName(finalUserName);
        message.setTimestamp(System.currentTimeMillis());

        // Save to Firebase
        messagesRef.push().setValue(message)
                .addOnSuccessListener(aVoid -> {
                    etMessage.setText("");
                    showLoading(false);
                    btnSend.setEnabled(true);

                    // Notify admin
                    notifyAdmin(messageText, finalUserName);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    btnSend.setEnabled(true);
                    Toast.makeText(TicketChatActivity.this,
                            "Send message error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void notifyAdmin(String messageText, String userName) {
        // Call API to notify admin
        try {
            JSONObject json = new JSONObject();
            json.put("ticketId", ticketId);
            json.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            json.put("senderType", "user");
            json.put("message", messageText);
            json.put("senderName", userName);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            // Use your web admin API URL
            String apiUrl = "https://healthtips-admin-fxbnt4896-projects.vercel.app/api/support/send-message-notification";

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Failed to notify admin: " + e.getMessage());
                    // Don't show error to user - message already sent
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    Log.d(TAG, "Admin notification sent successfully");
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error creating admin notification: " + e.getMessage());
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                showImagePreview();
            }
        }
    }

    private void showImagePreview() {
        layoutImagePreview.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(selectedImageUri)
                .into(ivImagePreview);
    }

    private void removeSelectedImage() {
        selectedImageUri = null;
        layoutImagePreview.setVisibility(View.GONE);
    }

    private void showFullscreenImage(String imageUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_fullscreen_image, null);
        ImageView fullscreenImageView = dialogView.findViewById(R.id.fullscreenImageView);

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(fullscreenImageView);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        fullscreenImageView.setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.black);
        }

        dialog.show();
    }

    private void scrollToBottom() {
        if (messages.size() > 0) {
            recyclerViewMessages.post(() ->
                    recyclerViewMessages.smoothScrollToPosition(messages.size() - 1));
        }
    }

    private void updateEmptyState() {
        if (messages.isEmpty()) {
            layoutEmptyMessages.setVisibility(View.VISIBLE);
            recyclerViewMessages.setVisibility(View.GONE);
        } else {
            layoutEmptyMessages.setVisibility(View.GONE);
            recyclerViewMessages.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listener to prevent memory leaks
        if (messagesListener != null && messagesRef != null) {
            messagesRef.removeEventListener(messagesListener);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
