package com.vhn.doan.presentation.support;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.R;
import com.vhn.doan.data.SupportTicket;
import com.vhn.doan.data.repository.SupportRepository;

import android.widget.ImageView;

import java.util.List;

import javax.inject.Inject;

/**
 * Activity để tạo Support Ticket mới
 */
public class CreateSupportTicketActivity extends AppCompatActivity implements SupportContract.View {

    private static final String TAG = "CreateSupportTicket";

    // Views
    private MaterialAutoCompleteTextView ticketTypeSpinner;
    private TextInputEditText subjectEditText;
    private TextInputEditText descriptionEditText;
    private ImageView screenshotImageView;
    private MaterialButton addScreenshotButton;
    private MaterialButton removeScreenshotButton;
    private MaterialButton submitButton;
    private View loadingLayout;

    // Data
    private Uri selectedImageUri;
    private SupportTicket.TicketType selectedTicketType;

    @Inject
    SupportPresenter presenter;

    @Inject
    SupportRepository repository;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_support_ticket);

        // Inject dependencies
        // TODO: Setup Dagger injection
        repository = new SupportRepository();
        presenter = new SupportPresenter(repository);

        initViews();
        setupToolbar();
        setupTicketTypeSpinner();
        setupActivityResultLaunchers();
        setupClickListeners();

        presenter.attachView(this);
    }

    private void initViews() {
        ticketTypeSpinner = findViewById(R.id.ticketTypeSpinner);
        subjectEditText = findViewById(R.id.subjectEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        screenshotImageView = findViewById(R.id.screenshotImageView);
        addScreenshotButton = findViewById(R.id.addScreenshotButton);
        removeScreenshotButton = findViewById(R.id.removeScreenshotButton);
        submitButton = findViewById(R.id.submitButton);
        loadingLayout = findViewById(R.id.loadingLayout);
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.create_ticket);
        }

        findViewById(R.id.toolbar).setOnClickListener(v -> onBackPressed());
    }

    private void setupTicketTypeSpinner() {
        String[] ticketTypes = new String[] {
            getString(R.string.ticket_type_bug_report),
            getString(R.string.ticket_type_content_report),
            getString(R.string.ticket_type_feature_request),
            getString(R.string.ticket_type_account_issue),
            getString(R.string.ticket_type_general_inquiry),
            getString(R.string.ticket_type_other)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            ticketTypes
        );

        ticketTypeSpinner.setAdapter(adapter);
        ticketTypeSpinner.setOnItemClickListener((parent, view, position, id) -> {
            selectedTicketType = getTicketTypeFromPosition(position);
        });
    }

    private SupportTicket.TicketType getTicketTypeFromPosition(int position) {
        switch (position) {
            case 0: return SupportTicket.TicketType.BUG_REPORT;
            case 1: return SupportTicket.TicketType.CONTENT_REPORT;
            case 2: return SupportTicket.TicketType.FEATURE_REQUEST;
            case 3: return SupportTicket.TicketType.ACCOUNT_ISSUE;
            case 4: return SupportTicket.TicketType.GENERAL_INQUIRY;
            case 5: return SupportTicket.TicketType.OTHER;
            default: return SupportTicket.TicketType.OTHER;
        }
    }

    private void setupActivityResultLaunchers() {
        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    displaySelectedImage();
                }
            }
        );

        // Permission launcher
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, "Cần quyền truy cập ảnh để đính kèm ảnh chụp màn hình",
                                 Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void setupClickListeners() {
        addScreenshotButton.setOnClickListener(v -> checkPermissionAndPickImage());

        removeScreenshotButton.setOnClickListener(v -> {
            selectedImageUri = null;
            screenshotImageView.setVisibility(View.GONE);
            removeScreenshotButton.setVisibility(View.GONE);
            addScreenshotButton.setVisibility(View.VISIBLE);
        });

        submitButton.setOnClickListener(v -> submitTicket());
    }

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Android 12 and below
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
        imagePickerLauncher.launch(intent);
    }

    private void displaySelectedImage() {
        if (selectedImageUri != null) {
            screenshotImageView.setImageURI(selectedImageUri);
            screenshotImageView.setVisibility(View.VISIBLE);
            removeScreenshotButton.setVisibility(View.VISIBLE);
            addScreenshotButton.setVisibility(View.GONE);
        }
    }

    private void submitTicket() {
        String subject = subjectEditText.getText() != null ?
                        subjectEditText.getText().toString().trim() : "";
        String description = descriptionEditText.getText() != null ?
                           descriptionEditText.getText().toString().trim() : "";

        // Validate inputs
        if (selectedTicketType == null) {
            Toast.makeText(this, R.string.error_select_ticket_type, Toast.LENGTH_SHORT).show();
            return;
        }

        if (subject.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_subject, Toast.LENGTH_SHORT).show();
            subjectEditText.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_description, Toast.LENGTH_SHORT).show();
            descriptionEditText.requestFocus();
            return;
        }

        // Get current user info
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để gửi yêu cầu hỗ trợ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create ticket
        SupportTicket ticket = new SupportTicket(
            currentUser.getUid(),
            currentUser.getEmail(),
            currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User",
            selectedTicketType,
            subject,
            description
        );

        // Add device info
        ticket.setDeviceInfo(getDeviceInfo());
        ticket.setAppVersion(getAppVersion());

        // Submit ticket with or without screenshot
        if (selectedImageUri != null) {
            presenter.submitTicketWithScreenshot(ticket, selectedImageUri);
        } else {
            presenter.createTicket(ticket);
        }
    }

    private String getDeviceInfo() {
        return "Device: " + Build.MANUFACTURER + " " + Build.MODEL +
               ", OS: Android " + Build.VERSION.RELEASE +
               " (API " + Build.VERSION.SDK_INT + ")";
    }

    private String getAppVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Unknown";
        }
    }

    // SupportContract.View implementation

    @Override
    public void showLoading() {
        runOnUiThread(() -> {
            loadingLayout.setVisibility(View.VISIBLE);
            submitButton.setEnabled(false);
        });
    }

    @Override
    public void hideLoading() {
        runOnUiThread(() -> {
            loadingLayout.setVisibility(View.GONE);
            submitButton.setEnabled(true);
        });
    }

    @Override
    public void showError(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }

    @Override
    public void showTickets(List<SupportTicket> tickets) {
        // Not used in this activity
    }

    @Override
    public void showEmptyTickets() {
        // Not used in this activity
    }

    @Override
    public void showTicketCreatedSuccess(String ticketId) {
        runOnUiThread(() -> {
            Toast.makeText(this, R.string.ticket_submit_success, Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        });
    }

    @Override
    public void showTicketSubmitError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, getString(R.string.ticket_submit_error) + ": " + error,
                         Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void showScreenshotUploaded(String url) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Đã tải ảnh lên thành công", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void showScreenshotUploadError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Lỗi tải ảnh: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void navigateToTicketDetail(String ticketId) {
        // Not used in this activity
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

