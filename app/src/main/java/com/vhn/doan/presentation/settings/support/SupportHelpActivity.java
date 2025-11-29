package com.vhn.doan.presentation.settings.support;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vhn.doan.R;

/**
 * Activity hỗ trợ
 */
public class SupportHelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_help);

        setupViews();
        setupClickListeners();
    }

    private void setupViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        // Mở màn hình Support Tickets
        LinearLayout layoutSupportTickets = findViewById(R.id.layoutSupportTickets);
        layoutSupportTickets.setOnClickListener(v -> {
            Intent intent = new Intent(this, MySupportTicketsActivity.class);
            startActivity(intent);
        });

        // FAQ
        LinearLayout layoutFAQ = findViewById(R.id.layoutFAQ);
        layoutFAQ.setOnClickListener(v -> startActivity(new Intent(this, FAQActivity.class)));

        // Liên hệ qua email
        LinearLayout layoutContact = findViewById(R.id.layoutContact);
        layoutContact.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:vuhoainam.dev@gmail.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Hỗ trợ HealthTips App");

            try {
                startActivity(Intent.createChooser(emailIntent, "Gửi email..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "Không có ứng dụng email được cài đặt.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
