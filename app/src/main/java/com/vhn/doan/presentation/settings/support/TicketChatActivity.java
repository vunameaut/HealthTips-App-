package com.vhn.doan.presentation.settings.support;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.R;
import com.vhn.doan.model.SupportMessage;
import com.vhn.doan.model.SupportTicket;
import com.vhn.doan.presentation.settings.support.adapter.MessageAdapter;
import com.vhn.doan.utils.CloudinaryHelper;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TicketChatActivity extends AppCompatActivity {

    public static final String EXTRA_TICKET_ID = "ticket_id";

    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton btnSend, btnAttachImage;
    private ProgressBar progressBar;
    private LinearLayout emptyView;
    private TextView ticketTitle, ticketStatus;
    private ImageButton btnBack;

    private MessageAdapter messageAdapter;
    private List<SupportMessage> messagesList;

    private DatabaseReference ticketRef;
    private DatabaseReference messagesRef;
    private FirebaseAuth firebaseAuth;
    private ValueEventListener messagesListener;

    private String ticketId;
    private SupportTicket currentTicket;

    private Uri selectedImageUri = null;
    private String uploadedImageUrl = null;
    private boolean isUploadingImage = false;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_chat);

        // Handle edge-to-edge display
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        }

        // Get ticket ID from intent
        ticketId = getIntent().getStringExtra(EXTRA_TICKET_ID);
        if (ticketId == null) {
            Toast.makeText(this, "Invalid ticket ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ticketRef = FirebaseDatabase.getInstance().getReference("issues").child(ticketId);
        messagesRef = ticketRef.child("messages");

        // Initialize ExecutorService for background tasks
        executorService = Executors.newSingleThreadExecutor();

        // Initialize Cloudinary
        CloudinaryHelper.initCloudinary(this);

        // Setup image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            selectedImageUri = imageUri;
                            uploadImageAndSend();
                        }
                    }
                }
        );

        setupViews();
        setupRecyclerView();
        loadTicketInfo();
        loadMessages();
    }

    private void setupViews() {
        btnBack = findViewById(R.id.btnBack);
        ticketTitle = findViewById(R.id.ticketTitle);
        ticketStatus = findViewById(R.id.ticketStatus);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        btnSend = findViewById(R.id.btnSend);
        btnAttachImage = findViewById(R.id.btnAttachImage);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendMessage());
        btnAttachImage.setOnClickListener(v -> openImagePicker());
    }

    private void setupRecyclerView() {
        messagesList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messagesList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void loadTicketInfo() {
        ticketRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentTicket = snapshot.getValue(SupportTicket.class);
                if (currentTicket != null) {
                    currentTicket.setId(ticketId);
                    updateUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TicketChatActivity.this,
                        "Error loading ticket", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (currentTicket != null) {
            ticketTitle.setText(currentTicket.getSubject());

            String status = currentTicket.getStatus();
            if ("resolved".equals(status)) {
                ticketStatus.setText(R.string.status_resolved);
            } else if ("in_progress".equals(status)) {
                ticketStatus.setText(R.string.status_in_progress);
            } else {
                ticketStatus.setText(R.string.status_pending);
            }
        }
    }

    private void loadMessages() {
        progressBar.setVisibility(View.VISIBLE);

        messagesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    SupportMessage message = child.getValue(SupportMessage.class);
                    if (message != null) {
                        message.setId(child.getKey());
                        messagesList.add(message);
                    }
                }

                // Sort by timestamp
                Collections.sort(messagesList, (a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));

                messageAdapter.updateMessages(messagesList);
                progressBar.setVisibility(View.GONE);

                if (messagesList.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    messagesRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    messagesRecyclerView.setVisibility(View.VISIBLE);
                    // Scroll to bottom
                    messagesRecyclerView.smoothScrollToPosition(messagesList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TicketChatActivity.this,
                        "Error loading messages", Toast.LENGTH_SHORT).show();
            }
        };

        messagesRef.addValueEventListener(messagesListener);
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();

        if (TextUtils.isEmpty(messageText) && uploadedImageUrl == null) {
            return;
        }

        if (isUploadingImage) {
            Toast.makeText(this, R.string.uploading_image, Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare message data
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("text", messageText);
        messageData.put("senderId", firebaseAuth.getCurrentUser().getUid());
        messageData.put("senderType", "user");
        messageData.put("senderName", firebaseAuth.getCurrentUser().getEmail());
        messageData.put("timestamp", System.currentTimeMillis());

        if (uploadedImageUrl != null) {
            messageData.put("imageUrl", uploadedImageUrl);
        }

        // Send to Firebase
        messagesRef.push().setValue(messageData)
                .addOnSuccessListener(aVoid -> {
                    messageInput.setText("");
                    uploadedImageUrl = null;
                    selectedImageUri = null;
                    // Scroll to bottom
                    messagesRecyclerView.smoothScrollToPosition(messagesList.size() - 1);

                    // Send notification to admin (in background)
                    sendNotificationToAdmin(messageText);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadImageAndSend() {
        if (selectedImageUri == null) return;

        isUploadingImage = true;
        progressBar.setVisibility(View.VISIBLE);

        CloudinaryHelper.uploadSupportImage(this, selectedImageUri, new CloudinaryHelper.CloudinaryUploadCallback() {
            @Override
            public void onUploadStart() {
                // Already showing progress
            }

            @Override
            public void onUploadProgress(int progress) {
                // Could update progress here
            }

            @Override
            public void onUploadSuccess(String imageUrl) {
                isUploadingImage = false;
                uploadedImageUrl = imageUrl;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TicketChatActivity.this,
                        R.string.image_upload_success, Toast.LENGTH_SHORT).show();

                // Auto send message with image
                sendMessage();
            }

            @Override
            public void onUploadError(String errorMessage) {
                isUploadingImage = false;
                progressBar.setVisibility(View.GONE);
                selectedImageUri = null;
                Toast.makeText(TicketChatActivity.this,
                        R.string.image_upload_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotificationToAdmin(String messageText) {
        if (firebaseAuth.getCurrentUser() == null || currentTicket == null) {
            return;
        }

        executorService.execute(() -> {
            try {
                // API endpoint
                URL url = new URL("https://healthtips-admin-5bs6s22wr-vunams-projects-d3582d4f.vercel.app/api/support/send-message-notification");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000); // 10 seconds timeout
                conn.setReadTimeout(10000);

                // Create JSON payload
                JSONObject payload = new JSONObject();
                payload.put("ticketId", ticketId);
                payload.put("userId", firebaseAuth.getCurrentUser().getUid());
                payload.put("senderType", "user");
                payload.put("message", messageText);
                payload.put("senderName", firebaseAuth.getCurrentUser().getEmail() != null ?
                        firebaseAuth.getCurrentUser().getEmail() : "User");

                // Send request
                OutputStream os = conn.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                // Get response
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Success - notification sent
                    android.util.Log.d("TicketChat", "Admin notification sent successfully");
                } else {
                    // Failed but don't show error to user
                    android.util.Log.e("TicketChat", "Failed to send admin notification: " + responseCode);
                }

                conn.disconnect();

            } catch (Exception e) {
                // Don't show error to user, just log it
                android.util.Log.e("TicketChat", "Error sending admin notification", e);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null && messagesRef != null) {
            messagesRef.removeEventListener(messagesListener);
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
