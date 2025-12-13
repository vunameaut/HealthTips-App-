package com.vhn.doan.presentation.report;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.R;
import com.vhn.doan.data.model.Report;
import com.vhn.doan.data.model.ReportMessage;
import com.vhn.doan.data.repository.ReportRepository;
import com.vhn.doan.data.repository.ReportRepositoryImpl;
import com.vhn.doan.presentation.report.adapter.MessageAdapter;
import com.vhn.doan.utils.AdminNotificationSender;
import com.vhn.doan.utils.CloudinaryHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity chat cho từng Report
 * Cho phép người dùng trao đổi với admin
 */
public class ReportChatActivity extends AppCompatActivity implements MessageAdapter.OnImageClickListener {

    private static final String TAG = "ReportChatActivity";
    public static final String EXTRA_REPORT_ID = "report_id";
    private static final int MAX_IMAGE_SIZE_MB = 2;

    // Views
    private MaterialToolbar toolbar;
    private MaterialCardView cardReportInfo, cardReportImage, cardClosedNotice;
    private LinearLayout layoutReportHeader, layoutReportContent, layoutInput, layoutImagePreview;
    private TextView tvReportType, tvStatus, tvReportContent, tvCreatedAt;
    private ImageView ivExpandCollapse, ivReportImage, ivAttachPreview;
    private RecyclerView rvMessages;
    private TextInputEditText etMessage;
    private ImageButton btnAttachImage, btnRemoveAttach;
    private FloatingActionButton fabSend;
    private FrameLayout loadingOverlay;

    // Data
    private String reportId;
    private Report currentReport;
    private MessageAdapter messageAdapter;
    private ReportRepository reportRepository;
    private FirebaseAuth firebaseAuth;
    private Uri selectedImageUri;
    private boolean isReportInfoExpanded = false;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    // Activity Result Launchers
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        handleImageSelected(imageUri);
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_chat);

        // Lấy reportId từ intent
        reportId = getIntent().getStringExtra(EXTRA_REPORT_ID);
        if (reportId == null || reportId.isEmpty()) {
            Toast.makeText(this, R.string.report_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initData();
        setupListeners();
        setupRecyclerView();
        loadReport();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        cardReportInfo = findViewById(R.id.cardReportInfo);
        cardReportImage = findViewById(R.id.cardReportImage);
        cardClosedNotice = findViewById(R.id.cardClosedNotice);
        layoutReportHeader = findViewById(R.id.layoutReportHeader);
        layoutReportContent = findViewById(R.id.layoutReportContent);
        layoutInput = findViewById(R.id.layoutInput);
        layoutImagePreview = findViewById(R.id.layoutImagePreview);
        tvReportType = findViewById(R.id.tvReportType);
        tvStatus = findViewById(R.id.tvStatus);
        tvReportContent = findViewById(R.id.tvReportContent);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        ivExpandCollapse = findViewById(R.id.ivExpandCollapse);
        ivReportImage = findViewById(R.id.ivReportImage);
        ivAttachPreview = findViewById(R.id.ivAttachPreview);
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnAttachImage = findViewById(R.id.btnAttachImage);
        btnRemoveAttach = findViewById(R.id.btnRemoveAttach);
        fabSend = findViewById(R.id.fabSend);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void initData() {
        reportRepository = new ReportRepositoryImpl();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void setupListeners() {
        // Toolbar back
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Expand/collapse report info
        layoutReportHeader.setOnClickListener(v -> toggleReportInfo());

        // Attach image
        btnAttachImage.setOnClickListener(v -> checkPermissionAndPickImage());
        btnRemoveAttach.setOnClickListener(v -> removeAttachedImage());

        // Send message
        fabSend.setOnClickListener(v -> sendMessage());

        // Enable/disable send button based on text input
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Report image click
        cardReportImage.setOnClickListener(v -> {
            if (currentReport != null && currentReport.hasImage()) {
                onImageClick(currentReport.getImageUrl());
            }
        });
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Scroll to bottom
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);
    }

    private void loadReport() {
        showLoading();

        reportRepository.getReportById(reportId, new ReportRepository.Callback<Report>() {
            @Override
            public void onSuccess(Report report) {
                runOnUiThread(() -> {
                    hideLoading();
                    currentReport = report;
                    displayReport(report);
                    loadMessages();
                    setupRealtimeListeners();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(ReportChatActivity.this,
                            getString(R.string.error_loading_report, error),
                            Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }

    private void displayReport(Report report) {
        // Toolbar
        toolbar.setTitle(report.getTitleDisplayName());
        toolbar.setSubtitle("ID: " + report.getId().substring(0, 8).toUpperCase());

        // Report type badge
        tvReportType.setText(report.getTitleDisplayName());
        setReportTypeColor(report.getTitle());

        // Status badge
        tvStatus.setText(report.getStatusDisplayName());
        tvStatus.setTextColor(report.getStatusColor());
        tvStatus.setBackgroundTintList(ColorStateList.valueOf(
                adjustAlpha(report.getStatusColor(), 0.15f)
        ));

        // Content
        tvReportContent.setText(report.getContent());

        // Created at
        tvCreatedAt.setText(getString(R.string.created_at_format, 
                dateFormat.format(new Date(report.getCreatedAt()))));

        // Image
        if (report.hasImage()) {
            cardReportImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(report.getImageUrl())
                    .centerCrop()
                    .into(ivReportImage);
        } else {
            cardReportImage.setVisibility(View.GONE);
        }

        // Check if report is closed
        updateInputState(report);
    }

    private void updateInputState(Report report) {
        if (!report.canChat()) {
            // Report đã đóng - không cho phép chat
            layoutInput.setVisibility(View.GONE);
            cardClosedNotice.setVisibility(View.VISIBLE);
        } else {
            layoutInput.setVisibility(View.VISIBLE);
            cardClosedNotice.setVisibility(View.GONE);
        }
    }

    private void setReportTypeColor(String type) {
        int color;
        switch (type) {
            case Report.TYPE_BUG:
                color = Color.parseColor("#F44336");
                break;
            case Report.TYPE_FEEDBACK:
                color = Color.parseColor("#2196F3");
                break;
            case Report.TYPE_QUESTION:
                color = Color.parseColor("#9C27B0");
                break;
            default:
                color = Color.parseColor("#607D8B");
                break;
        }
        tvReportType.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private void loadMessages() {
        reportRepository.getMessages(reportId, new ReportRepository.Callback<List<ReportMessage>>() {
            @Override
            public void onSuccess(List<ReportMessage> messages) {
                runOnUiThread(() -> {
                    messageAdapter.submitList(messages);
                    if (!messages.isEmpty()) {
                        rvMessages.scrollToPosition(messages.size() - 1);
                    }

                    // Đánh dấu tin nhắn từ admin đã đọc
                    reportRepository.markAllMessagesAsRead(reportId, "user", 
                        new ReportRepository.Callback<Void>() {
                            @Override
                            public void onSuccess(Void result) {}
                            @Override
                            public void onError(String error) {}
                        });
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading messages: " + error);
            }
        });
    }

    private void setupRealtimeListeners() {
        // Listener cho report updates (status changes)
        reportRepository.addReportListener(reportId, new ReportRepository.ReportListener() {
            @Override
            public void onReportUpdated(Report report) {
                runOnUiThread(() -> {
                    currentReport = report;
                    displayReport(report);
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Report listener error: " + error);
            }
        });

        // Listener cho messages
        reportRepository.addMessagesListener(reportId, new ReportRepository.MessagesListener() {
            @Override
            public void onMessagesUpdated(List<ReportMessage> messages) {
                runOnUiThread(() -> {
                    messageAdapter.submitList(messages);
                    if (!messages.isEmpty()) {
                        rvMessages.smoothScrollToPosition(messages.size() - 1);
                    }
                });
            }

            @Override
            public void onNewMessage(ReportMessage message) {
                // Đánh dấu tin nhắn đã đọc nếu từ admin
                if (message.isFromAdmin()) {
                    reportRepository.markMessageAsRead(reportId, message.getId(),
                        new ReportRepository.Callback<Void>() {
                            @Override
                            public void onSuccess(Void result) {}
                            @Override
                            public void onError(String error) {}
                        });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Messages listener error: " + error);
            }
        });
    }

    private void toggleReportInfo() {
        isReportInfoExpanded = !isReportInfoExpanded;
        
        if (isReportInfoExpanded) {
            layoutReportContent.setVisibility(View.VISIBLE);
            ivExpandCollapse.setRotation(180f);
        } else {
            layoutReportContent.setVisibility(View.GONE);
            ivExpandCollapse.setRotation(0f);
        }
    }

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void handleImageSelected(Uri imageUri) {
        try {
            long fileSize = getContentResolver().openFileDescriptor(imageUri, "r").getStatSize();
            long maxSize = MAX_IMAGE_SIZE_MB * 1024 * 1024;

            if (fileSize > maxSize) {
                Toast.makeText(this,
                        getString(R.string.image_too_large, MAX_IMAGE_SIZE_MB),
                        Toast.LENGTH_LONG).show();
                return;
            }

            selectedImageUri = imageUri;
            layoutImagePreview.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imageUri)
                    .centerCrop()
                    .into(ivAttachPreview);

            updateSendButtonState();

        } catch (Exception e) {
            Log.e(TAG, "Error reading image", e);
            Toast.makeText(this, R.string.error_reading_image, Toast.LENGTH_SHORT).show();
        }
    }

    private void removeAttachedImage() {
        selectedImageUri = null;
        layoutImagePreview.setVisibility(View.GONE);
        updateSendButtonState();
    }

    private void updateSendButtonState() {
        String text = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
        boolean canSend = !text.isEmpty() || selectedImageUri != null;
        fabSend.setEnabled(canSend);
        fabSend.setAlpha(canSend ? 1.0f : 0.5f);
    }

    private void sendMessage() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.please_login_first, Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentReport != null && !currentReport.canChat()) {
            Toast.makeText(this, R.string.report_closed_cannot_chat, Toast.LENGTH_SHORT).show();
            return;
        }

        String text = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
        
        if (text.isEmpty() && selectedImageUri == null) {
            return;
        }

        // Disable send button
        fabSend.setEnabled(false);

        if (selectedImageUri != null) {
            // Upload image first, then send message
            uploadImageAndSend(currentUser, text);
        } else {
            // Send text-only message
            sendTextMessage(currentUser, text, null);
        }
    }

    private void uploadImageAndSend(FirebaseUser user, String text) {
        showLoading();

        CloudinaryHelper.uploadChatImage(this, selectedImageUri, new CloudinaryHelper.ReportUploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                sendTextMessage(user, text, imageUrl);
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideLoading();
                    fabSend.setEnabled(true);
                    Toast.makeText(ReportChatActivity.this,
                            getString(R.string.upload_image_failed, error),
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onProgress(int progress) {
                // Optional: show progress
            }
        });
    }

    private void sendTextMessage(FirebaseUser user, String text, String imageUrl) {
        ReportMessage message = ReportMessage.createUserMessage(
                null, // ID will be generated by Firebase
                user.getUid(),
                user.getDisplayName() != null ? user.getDisplayName() : user.getEmail(),
                text
        );
        
        if (imageUrl != null) {
            message.setImageUrl(imageUrl);
        }

        reportRepository.sendMessage(reportId, message, new ReportRepository.Callback<String>() {
            @Override
            public void onSuccess(String messageId) {
                runOnUiThread(() -> {
                    hideLoading();
                    
                    // Clear input
                    etMessage.setText("");
                    removeAttachedImage();
                    fabSend.setEnabled(true);

                    // Notify admin about new message
                    sendAdminNotification(text);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideLoading();
                    fabSend.setEnabled(true);
                    Toast.makeText(ReportChatActivity.this,
                            getString(R.string.send_message_failed, error),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void sendAdminNotification(String messageText) {
        if (currentReport == null) return;

        // Gửi notification tới admin
        AdminNotificationSender.sendUserReplyNotification(
                this,
                reportId,
                currentReport.getUserId(),
                currentReport.getUserName(),
                messageText
        );
    }

    @Override
    public void onImageClick(String imageUrl) {
        // TODO: Open full screen image viewer
        // For now, open in browser
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
        startActivity(intent);
    }

    private void showLoading() {
        loadingOverlay.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        loadingOverlay.setVisibility(View.GONE);
    }

    private int adjustAlpha(int color, float factor) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(Math.min(255, Math.max(0, (int)(255 * factor))), red, green, blue);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        reportRepository.removeAllListeners();
    }
}

