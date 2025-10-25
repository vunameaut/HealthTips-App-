package com.vhn.doan.presentation.about;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.vhn.doan.R;

/**
 * Activity hiển thị thông tin về ứng dụng
 */
public class AboutActivity extends AppCompatActivity {

    private static final String TAG = "AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setupToolbar();
        setupVersionInfo();
        setupClickListeners();
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.about_app);
        }

        findViewById(R.id.toolbar).setOnClickListener(v -> onBackPressed());
    }

    private void setupVersionInfo() {
        TextView versionText = findViewById(R.id.versionText);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = "Phiên bản " + pInfo.versionName;
            versionText.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            versionText.setText("Phiên bản Unknown");
        }
    }

    private void setupClickListeners() {
        // Rate App
        MaterialCardView rateAppCard = findViewById(R.id.rateAppCard);
        rateAppCard.setOnClickListener(v -> openPlayStore());

        // Share App
        MaterialCardView shareAppCard = findViewById(R.id.shareAppCard);
        shareAppCard.setOnClickListener(v -> shareApp());

        // Terms of Service
        MaterialCardView termsCard = findViewById(R.id.termsCard);
        termsCard.setOnClickListener(v -> openTermsOfService());

        // Privacy Policy
        MaterialCardView privacyCard = findViewById(R.id.privacyCard);
        privacyCard.setOnClickListener(v -> openPrivacyPolicy());

        // Community Guidelines
        MaterialCardView communityGuidelinesCard = findViewById(R.id.communityGuidelinesCard);
        communityGuidelinesCard.setOnClickListener(v -> openCommunityGuidelines());
    }

    private void openPlayStore() {
        try {
            // Thử mở Play Store app
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + getPackageName()));
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            // Nếu không có Play Store app, mở bằng browser
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
            startActivity(intent);
        }
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        String shareMessage = getString(R.string.share_app_text) +
                            "\nhttps://play.google.com/store/apps/details?id=" + getPackageName();
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)));
    }

    private void openTermsOfService() {
        Intent intent = new Intent(this, LegalDocumentActivity.class);
        intent.putExtra(LegalDocumentActivity.EXTRA_DOCUMENT_TYPE, LegalDocumentActivity.TYPE_TERMS);
        startActivity(intent);
    }

    private void openPrivacyPolicy() {
        Intent intent = new Intent(this, LegalDocumentActivity.class);
        intent.putExtra(LegalDocumentActivity.EXTRA_DOCUMENT_TYPE, LegalDocumentActivity.TYPE_PRIVACY);
        startActivity(intent);
    }

    private void openCommunityGuidelines() {
        Intent intent = new Intent(this, LegalDocumentActivity.class);
        intent.putExtra(LegalDocumentActivity.EXTRA_DOCUMENT_TYPE, LegalDocumentActivity.TYPE_COMMUNITY);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

