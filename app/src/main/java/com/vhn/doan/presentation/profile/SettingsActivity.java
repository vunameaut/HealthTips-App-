package com.vhn.doan.presentation.profile;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.vhn.doan.R;

/**
 * Activity cài đặt đơn giản.
 * Hiển thị layout mặc định với nút quay lại.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton btnBack = findViewById(R.id.buttonBack);
        btnBack.setOnClickListener(v -> onBackPressed());
    }
}
