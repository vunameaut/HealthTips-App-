package com.vhn.doan.presentation.report;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.R;
import com.vhn.doan.data.model.Report;
import com.vhn.doan.data.repository.ReportRepository;
import com.vhn.doan.data.repository.ReportRepositoryImpl;
import com.vhn.doan.utils.CloudinaryHelper;
import com.vhn.doan.utils.AdminNotificationSender;

/**
 * Activity để tạo report mới
 * Cho phép người dùng gửi báo cáo lỗi, góp ý, câu hỏi
 */
public class CreateReportActivity extends AppCompatActivity {

    private static final String TAG = "CreateReportActivity";
    private static final int MAX_IMAGE_SIZE_MB = 5;

    // Views
    private MaterialToolbar toolbar;
    private TextInputLayout tilReportType, tilContent;
    private AutoCompleteTextView actvReportType;
    private TextInputEditText etContent;
    private MaterialButton btnAttachImage, btnSubmit;
    private FrameLayout imagePreviewContainer;
    private ImageView ivImagePreview;
    private FloatingActionButton fabRemoveImage;
    private FrameLayout loadingOverlay;
    private TextView tvLoadingMessage, tvDeviceInfo;

    // Data
    private Uri selectedImageUri;
    private String uploadedImageUrl;
    private ReportRepository reportRepository;
    private FirebaseAuth firebaseAuth;

    // Report types
    private final String[] reportTypes = new String[]{
            "Báo cáo lỗi",
            "Góp ý",
            "Câu hỏi",
            "Khác"
    };

    private final String[] reportTypeValues = new String[]{
            Report.TYPE_BUG,
            Report.TYPE_FEEDBACK,
            Report.TYPE_QUESTION,
            Report.TYPE_OTHER
    };

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
        setContentView(R.layout.activity_create_report);

        initViews();
        initData();
        setupListeners();
        setupReportTypeDropdown();
        displayDeviceInfo();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tilReportType = findViewById(R.id.tilReportType);
        actvReportType = findViewById(R.id.actvReportType);
        tilContent = findViewById(R.id.tilContent);
        etContent = findViewById(R.id.etContent);
        btnAttachImage = findViewById(R.id.btnAttachImage);
        btnSubmit = findViewById(R.id.btnSubmit);
        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);
        ivImagePreview = findViewById(R.id.ivImagePreview);
        fabRemoveImage = findViewById(R.id.fabRemoveImage);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        tvLoadingMessage = findViewById(R.id.tvLoadingMessage);
        tvDeviceInfo = findViewById(R.id.tvDeviceInfo);
    }

    private void initData() {
        reportRepository = new ReportRepositoryImpl();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void setupListeners() {
        // Toolbar back button
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Attach image button
        btnAttachImage.setOnClickListener(v -> checkPermissionAndPickImage());

        // Remove image button
        fabRemoveImage.setOnClickListener(v -> removeSelectedImage());

        // Submit button
        btnSubmit.setOnClickListener(v -> validateAndSubmit());
    }

    private void setupReportTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                reportTypes
        );
        actvReportType.setAdapter(adapter);
        
        // Mặc định chọn "Báo cáo lỗi"
        actvReportType.setText(reportTypes[0], false);
    }

    private void displayDeviceInfo() {
        String deviceInfo = String.format(
                "Thiết bị: %s %s\nAndroid: %s (API %d)\nỨng dụng: %s",
                Build.MANUFACTURER,
                Build.MODEL,
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT,
                getAppVersion()
        );
        tvDeviceInfo.setText(deviceInfo);
    }

    private String getAppVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "1.0.0";
        }
    }

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: sử dụng READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Android 12 trở xuống
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
        // Kiểm tra kích thước file
        try {
            long fileSize = getContentResolver().openFileDescriptor(imageUri, "r").getStatSize();
            long maxSize = MAX_IMAGE_SIZE_MB * 1024 * 1024; // 5MB

            if (fileSize > maxSize) {
                Toast.makeText(this, 
                    getString(R.string.image_too_large, MAX_IMAGE_SIZE_MB), 
                    Toast.LENGTH_LONG).show();
                return;
            }

            selectedImageUri = imageUri;
            uploadedImageUrl = null; // Reset uploaded URL

            // Hiển thị preview
            imagePreviewContainer.setVisibility(View.VISIBLE);
            btnAttachImage.setVisibility(View.GONE);
            
            Glide.with(this)
                    .load(imageUri)
                    .centerCrop()
                    .into(ivImagePreview);

        } catch (Exception e) {
            Log.e(TAG, "Error reading image file", e);
            Toast.makeText(this, R.string.error_reading_image, Toast.LENGTH_SHORT).show();
        }
    }

    private void removeSelectedImage() {
        selectedImageUri = null;
        uploadedImageUrl = null;
        imagePreviewContainer.setVisibility(View.GONE);
        btnAttachImage.setVisibility(View.VISIBLE);
        ivImagePreview.setImageDrawable(null);
    }

    private void validateAndSubmit() {
        // Kiểm tra đăng nhập
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.please_login_first, Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra loại báo cáo
        String selectedType = actvReportType.getText().toString().trim();
        if (TextUtils.isEmpty(selectedType)) {
            tilReportType.setError(getString(R.string.please_select_report_type));
            return;
        }
        tilReportType.setError(null);

        // Kiểm tra nội dung
        String content = etContent.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            tilContent.setError(getString(R.string.please_enter_content));
            return;
        }
        if (content.length() < 10) {
            tilContent.setError(getString(R.string.content_too_short));
            return;
        }
        tilContent.setError(null);

        // Bắt đầu submit
        if (selectedImageUri != null) {
            uploadImageAndSubmit(currentUser, selectedType, content);
        } else {
            submitReport(currentUser, selectedType, content, null);
        }
    }

    private void uploadImageAndSubmit(FirebaseUser user, String type, String content) {
        showLoading(getString(R.string.uploading_image));

        CloudinaryHelper.uploadReportImage(this, selectedImageUri, new CloudinaryHelper.ReportUploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                uploadedImageUrl = imageUrl;
                submitReport(user, type, content, imageUrl);
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(CreateReportActivity.this, 
                        getString(R.string.upload_image_failed, error), 
                        Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onProgress(int progress) {
                runOnUiThread(() -> 
                    tvLoadingMessage.setText(getString(R.string.uploading_progress, progress))
                );
            }
        });
    }

    private void submitReport(FirebaseUser user, String type, String content, String imageUrl) {
        showLoading(getString(R.string.sending_report));

        // Chuyển đổi tên loại báo cáo sang giá trị
        String typeValue = getReportTypeValue(type);

        // Tạo report object
        Report report = new Report();
        report.setUserId(user.getUid());
        report.setUserEmail(user.getEmail());
        report.setUserName(user.getDisplayName() != null ? user.getDisplayName() : user.getEmail());
        report.setTitle(typeValue);
        report.setContent(content);
        report.setImageUrl(imageUrl);
        report.setDeviceInfo(tvDeviceInfo.getText().toString());
        report.setStatus(Report.STATUS_PENDING);

        // Gửi report
        reportRepository.createReport(report, new ReportRepository.Callback<String>() {
            @Override
            public void onSuccess(String reportId) {
                // Gửi thông báo tới admin
                sendAdminNotification(report, reportId);

                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(CreateReportActivity.this, 
                        R.string.report_sent_success, Toast.LENGTH_SHORT).show();
                    
                    // Quay về màn hình trước
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(CreateReportActivity.this, 
                        getString(R.string.report_send_failed, error), 
                        Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private String getReportTypeValue(String displayName) {
        for (int i = 0; i < reportTypes.length; i++) {
            if (reportTypes[i].equals(displayName)) {
                return reportTypeValues[i];
            }
        }
        return Report.TYPE_OTHER;
    }

    private void sendAdminNotification(Report report, String reportId) {
        // Gửi thông báo tới admin panel
        AdminNotificationSender.sendNewReportNotification(
                this,
                reportId,
                report.getUserId(),
                report.getUserName(),
                report.getTitleDisplayName(),
                report.getContent()
        );
    }

    private void showLoading(String message) {
        loadingOverlay.setVisibility(View.VISIBLE);
        tvLoadingMessage.setText(message);
        btnSubmit.setEnabled(false);
    }

    private void hideLoading() {
        loadingOverlay.setVisibility(View.GONE);
        btnSubmit.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        // Cảnh báo nếu có dữ liệu chưa lưu
        String content = etContent.getText().toString().trim();
        if (!TextUtils.isEmpty(content) || selectedImageUri != null) {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.confirm_exit)
                    .setMessage(R.string.discard_report_message)
                    .setPositiveButton(R.string.discard, (d, w) -> super.onBackPressed())
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}

