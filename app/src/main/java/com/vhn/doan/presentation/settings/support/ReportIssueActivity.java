package com.vhn.doan.presentation.settings.support;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vhn.doan.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity báo cáo vấn đề
 */
public class ReportIssueActivity extends AppCompatActivity {

    private Spinner spinnerIssueType;
    private EditText etSubject, etDescription;
    private Button btnSubmit;
    private ProgressBar progressBar;
    private TextView tvDeviceInfo;

    private DatabaseReference issuesRef;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);

        firebaseAuth = FirebaseAuth.getInstance();
        issuesRef = FirebaseDatabase.getInstance().getReference("issues");

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

        btnSubmit.setOnClickListener(v -> submitReport());
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

        if (firebaseAuth.getCurrentUser() != null) {
            reportData.put("userId", firebaseAuth.getCurrentUser().getUid());
            reportData.put("userEmail", firebaseAuth.getCurrentUser().getEmail());
        }

        // Submit to Firebase
        String reportId = issuesRef.push().getKey();
        if (reportId != null) {
            issuesRef.child(reportId).setValue(reportData)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, R.string.report_success, Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show();
                });
        }
    }
}
