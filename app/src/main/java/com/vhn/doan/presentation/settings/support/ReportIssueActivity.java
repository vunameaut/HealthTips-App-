package com.vhn.doan.presentation.settings.support;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vhn.doan.R;
import com.vhn.doan.utils.AdminNotificationSender;
import com.vhn.doan.utils.CloudinaryHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity báo cáo vấn đề
 */
public class ReportIssueActivity extends AppCompatActivity {

    private static final String TAG = "ReportIssueActivity";

    private Spinner spinnerIssueType;
    private EditText etSubject, etDescription;
    private Button btnSubmit, btnAttachImage, btnRemoveImage;
    private ProgressBar progressBar;
    private TextView tvDeviceInfo;
    private ImageView imagePreview;

    private DatabaseReference issuesRef;
    private FirebaseAuth firebaseAuth;
    private AdminNotificationSender adminNotificationSender;

    private Uri selectedImageUri = null;
    private String uploadedImageUrl = null;
    private boolean isUploadingImage = false;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);

        // Handle edge-to-edge display and notch
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        issuesRef = FirebaseDatabase.getInstance().getReference("support_tickets");
        adminNotificationSender = new AdminNotificationSender(this);

        // Initialize Cloudinary
        CloudinaryHelper.initCloudinary(this);

        // Setup image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            onImageSelected(imageUri);
                        }
                    }
                }
        );

        setupViews();
        setupIssueTypeSpinner();
        displayDeviceInfo();
    }

    private void setupViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        spinnerIssueType = findViewById(R.id.spinnerIssueType);
        etSubject = findViewById(R.id.etSubject);
        etDescription = findViewById(R.id.etDescription);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
        tvDeviceInfo = findViewById(R.id.tvDeviceInfo);

        // Image attachment views
        btnAttachImage = findViewById(R.id.btnAttachImage);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);
        imagePreview = findViewById(R.id.imagePreview);

        btnSubmit.setOnClickListener(v -> submitReport());
        btnAttachImage.setOnClickListener(v -> openImagePicker());
        btnRemoveImage.setOnClickListener(v -> removeImage());
    }

    private void setupIssueTypeSpinner() {
        String[] issueTypes = {
            getString(R.string.select_ticket_type),
            getString(R.string.report_spam),
            getString(R.string.report_inappropriate),
            getString(R.string.report_misleading),
            getString(R.string.report_harassment),
            getString(R.string.report_violence),
            getString(R.string.report_hate_speech),
            getString(R.string.report_copyright),
            getString(R.string.report_other_reason)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            issueTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIssueType.setAdapter(adapter);
    }

    private void displayDeviceInfo() {
        String deviceInfo = "Device: " + Build.MANUFACTURER + " " + Build.MODEL + "\n" +
                           "Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")";
        tvDeviceInfo.setText(deviceInfo);
    }

    private void submitReport() {
        String issueType = spinnerIssueType.getSelectedItem().toString();
        String subject = etSubject.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Validation
        if (spinnerIssueType.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.error_select_ticket_type, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(subject)) {
            etSubject.setError(getString(R.string.error_empty_subject));
            etSubject.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            etDescription.setError(getString(R.string.error_empty_description));
            etDescription.requestFocus();
            return;
        }

        // Check if image is being uploaded
        if (isUploadingImage) {
            Toast.makeText(this, R.string.uploading_image, Toast.LENGTH_SHORT).show();
            return;
        }

        // If image is selected but not uploaded, upload it first
        if (selectedImageUri != null && uploadedImageUrl == null) {
            uploadImageAndSubmit(issueType, subject, description);
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        // Prepare report data
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("issueType", issueType);
        reportData.put("subject", subject);
        reportData.put("description", description);
        reportData.put("deviceManufacturer", Build.MANUFACTURER);
        reportData.put("deviceModel", Build.MODEL);
        reportData.put("androidVersion", Build.VERSION.RELEASE);
        reportData.put("apiLevel", Build.VERSION.SDK_INT);
        reportData.put("timestamp", System.currentTimeMillis());
        reportData.put("status", "pending");

        // Add image URL if available
        if (uploadedImageUrl != null) {
            reportData.put("imageUrl", uploadedImageUrl);
        }

        if (firebaseAuth.getCurrentUser() != null) {
            reportData.put("userId", firebaseAuth.getCurrentUser().getUid());
            reportData.put("userEmail", firebaseAuth.getCurrentUser().getEmail());
        }

        // Submit to Firebase
        String reportId = issuesRef.push().getKey();
        if (reportId != null) {
            final String finalReportId = reportId;
            issuesRef.child(reportId).setValue(reportData)
                .addOnSuccessListener(aVoid -> {
                    // Send automatic acknowledgment message
                    sendAutoAcknowledgment(finalReportId);

                    // Also send to web admin
                    sendToAdminPanel(issueType, subject, description);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show();
                });
        }
    }

    /**
     * Send automatic acknowledgment message
     */
    private void sendAutoAcknowledgment(String ticketId) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance()
                .getReference("support_tickets")
                .child(ticketId)
                .child("messages");

        String messageId = messagesRef.push().getKey();
        if (messageId != null) {
            Map<String, Object> autoMessage = new HashMap<>();
            autoMessage.put("text", getString(R.string.auto_acknowledgment_message));
            autoMessage.put("senderId", "system");
            autoMessage.put("senderType", "admin");
            autoMessage.put("senderName", "HealthTips Support");
            autoMessage.put("timestamp", System.currentTimeMillis());

            messagesRef.child(messageId).setValue(autoMessage)
                    .addOnFailureListener(e -> {
                        // Silent fail - not critical
                        Log.e(TAG, "Failed to send auto acknowledgment", e);
                    });
        }
    }

    /**
     * Send report to web admin panel
     */
    private void sendToAdminPanel(String issueType, String subject, String description) {
        // Map issue type to report type
        String reportType = mapIssueTypeToReportType(issueType);

        adminNotificationSender.sendUserReport(
                reportType,
                subject,
                description,
                null,  // No content ID for general reports
                null,  // No content type
                new AdminNotificationSender.NotificationCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnSubmit.setEnabled(true);
                            Toast.makeText(ReportIssueActivity.this,
                                    R.string.report_success, Toast.LENGTH_LONG).show();
                            finish();
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Even if admin notification fails, report was saved to Firebase
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnSubmit.setEnabled(true);
                            Toast.makeText(ReportIssueActivity.this,
                                    R.string.report_success, Toast.LENGTH_LONG).show();
                            finish();
                        });
                    }
                }
        );
    }

    /**
     * Map issue type string to report type
     */
    private String mapIssueTypeToReportType(String issueType) {
        if (issueType.contains("spam") || issueType.contains("Spam")) {
            return "spam";
        } else if (issueType.contains("inappropriate") || issueType.contains("Inappropriate")) {
            return "inappropriate";
        } else if (issueType.contains("harassment") || issueType.contains("Harassment")) {
            return "abuse";
        } else if (issueType.contains("violence") || issueType.contains("Violence")) {
            return "abuse";
        } else {
            return "other";
        }
    }

    /**
     * Open image picker
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    /**
     * Handle image selection
     */
    private void onImageSelected(Uri imageUri) {
        selectedImageUri = imageUri;

        // Show preview
        imagePreview.setVisibility(View.VISIBLE);
        btnRemoveImage.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(imagePreview);
    }

    /**
     * Remove selected image
     */
    private void removeImage() {
        selectedImageUri = null;
        uploadedImageUrl = null;
        imagePreview.setVisibility(View.GONE);
        btnRemoveImage.setVisibility(View.GONE);
        imagePreview.setImageDrawable(null);
    }

    /**
     * Upload image and then submit report
     */
    private void uploadImageAndSubmit(String issueType, String subject, String description) {
        isUploadingImage = true;
        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        Toast.makeText(this, R.string.uploading_image, Toast.LENGTH_SHORT).show();

        CloudinaryHelper.uploadSupportImage(this, selectedImageUri, new CloudinaryHelper.CloudinaryUploadCallback() {
            @Override
            public void onUploadStart() {
                // Already showing progress
            }

            @Override
            public void onUploadProgress(int progress) {
                // Could update progress bar here if needed
            }

            @Override
            public void onUploadSuccess(String imageUrl) {
                isUploadingImage = false;
                uploadedImageUrl = imageUrl;
                Toast.makeText(ReportIssueActivity.this, R.string.image_upload_success, Toast.LENGTH_SHORT).show();

                // Now submit the report
                submitReport();
            }

            @Override
            public void onUploadError(String errorMessage) {
                isUploadingImage = false;
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                Toast.makeText(ReportIssueActivity.this,
                        R.string.image_upload_failed + ": " + errorMessage,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
