package com.vhn.doan.presentation.settings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.vhn.doan.R;
import com.vhn.doan.presentation.settings.account.AccountManagementActivity;
import com.vhn.doan.presentation.settings.account.PrivacySettingsActivity;
import com.vhn.doan.presentation.settings.account.SecuritySettingsActivity;
import com.vhn.doan.presentation.settings.account.PermissionsSettingsActivity;
import com.vhn.doan.presentation.settings.content.NotificationSettingsActivity;
import com.vhn.doan.presentation.settings.content.LanguageSettingsActivity;
import com.vhn.doan.presentation.settings.content.DisplaySettingsActivity;
import com.vhn.doan.presentation.settings.content.TermsPolicyActivity;
import com.vhn.doan.presentation.settings.support.ReportIssueActivity;
import com.vhn.doan.presentation.settings.support.SupportHelpActivity;

/**
 * Activity chính cho "Cài đặt và quyền riêng tư"
 * Hiển thị các nhóm cài đặt: Tài khoản, Nội dung và hiển thị, Hỗ trợ và giới thiệu
 */
public class SettingsAndPrivacyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_and_privacy);

        setupViews();
        setupClickListeners();
    }

    private void setupViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupClickListeners() {
        // Phần Tài khoản
        findViewById(R.id.layoutAccountManagement).setOnClickListener(v ->
            startActivity(new Intent(this, AccountManagementActivity.class)));

        findViewById(R.id.layoutPrivacySettings).setOnClickListener(v ->
            startActivity(new Intent(this, PrivacySettingsActivity.class)));

        findViewById(R.id.layoutSecuritySettings).setOnClickListener(v ->
            startActivity(new Intent(this, SecuritySettingsActivity.class)));

        findViewById(R.id.layoutPermissionsSettings).setOnClickListener(v ->
            startActivity(new Intent(this, PermissionsSettingsActivity.class)));

        // Phần Nội dung và hiển thị
        findViewById(R.id.layoutNotificationSettings).setOnClickListener(v ->
            startActivity(new Intent(this, NotificationSettingsActivity.class)));

        findViewById(R.id.layoutLanguageSettings).setOnClickListener(v ->
            startActivity(new Intent(this, LanguageSettingsActivity.class)));

        findViewById(R.id.layoutDisplaySettings).setOnClickListener(v ->
            startActivity(new Intent(this, DisplaySettingsActivity.class)));

        // Phần Hỗ trợ và giới thiệu
        findViewById(R.id.layoutReportIssue).setOnClickListener(v ->
            startActivity(new Intent(this, ReportIssueActivity.class)));

        findViewById(R.id.layoutSupportHelp).setOnClickListener(v ->
            startActivity(new Intent(this, SupportHelpActivity.class)));

        findViewById(R.id.layoutTermsPolicy).setOnClickListener(v ->
            startActivity(new Intent(this, TermsPolicyActivity.class)));
    }
}
