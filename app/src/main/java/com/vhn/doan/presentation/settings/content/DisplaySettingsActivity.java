package com.vhn.doan.presentation.settings.content;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.vhn.doan.R;
import com.vhn.doan.presentation.base.BaseActivity;
import com.vhn.doan.utils.FontSizeHelper;

import android.widget.Toast;

/**
 * Activity cài đặt hiển thị
 */
public class DisplaySettingsActivity extends BaseActivity {

    private SharedPreferences preferences;

    private SwitchCompat switchDarkMode;
    private RadioGroup radioGroupTheme;
    private RadioButton rbSystemDefault, rbLightMode, rbDarkMode;
    private SeekBar seekBarFontSize;
    private TextView tvFontSizeValue;
    private TextView tvPreviewText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_settings);

        preferences = getSharedPreferences("DisplaySettings", MODE_PRIVATE);

        setupViews();
        loadSettings();
        setupListeners();
    }

    private void setupViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        switchDarkMode = findViewById(R.id.switchDarkMode);
        radioGroupTheme = findViewById(R.id.radioGroupTheme);
        rbSystemDefault = findViewById(R.id.rbSystemDefault);
        rbLightMode = findViewById(R.id.rbLightMode);
        rbDarkMode = findViewById(R.id.rbDarkMode);
        seekBarFontSize = findViewById(R.id.seekBarFontSize);
        tvFontSizeValue = findViewById(R.id.tvFontSizeValue);
        tvPreviewText = findViewById(R.id.tvPreviewText);
    }

    private void loadSettings() {
        // Load dark mode setting
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDarkMode);

        // Load theme mode
        String themeMode = preferences.getString("theme_mode", "system");
        switch (themeMode) {
            case "system":
                rbSystemDefault.setChecked(true);
                break;
            case "light":
                rbLightMode.setChecked(true);
                break;
            case "dark":
                rbDarkMode.setChecked(true);
                break;
        }

        // Load font size
        int fontSize = preferences.getInt("font_size", 16);
        seekBarFontSize.setProgress(fontSize - 12); // Min 12, Max 24
        tvFontSizeValue.setText(fontSize + "sp");
        tvPreviewText.setTextSize(fontSize);
    }

    private void setupListeners() {
        // Dark mode switch with smooth animation
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Animation mượt mà khi chuyển đổi
            buttonView.animate()
                    .scaleX(0.92f)
                    .scaleY(0.92f)
                    .setDuration(100)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .withEndAction(() -> {
                        buttonView.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(120)
                                .setInterpolator(new android.view.animation.OvershootInterpolator())
                                .start();
                    })
                    .start();

            // Thêm hiệu ứng alpha cho transition
            buttonView.animate()
                    .alpha(0.85f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        buttonView.animate()
                                .alpha(1.0f)
                                .setDuration(100)
                                .start();
                    })
                    .start();

            preferences.edit().putBoolean("dark_mode", isChecked).apply();
            applyDarkMode(isChecked);
        });

        // Theme mode radio group
        radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
            String themeMode;
            int nightMode;

            if (checkedId == R.id.rbSystemDefault) {
                themeMode = "system";
                nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            } else if (checkedId == R.id.rbLightMode) {
                themeMode = "light";
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else {
                themeMode = "dark";
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
            }

            preferences.edit().putString("theme_mode", themeMode).apply();
            AppCompatDelegate.setDefaultNightMode(nightMode);
        });

        // Font size seekbar
        seekBarFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int fontSize = progress + 12; // Min 12, Max 24
                tvFontSizeValue.setText(fontSize + "sp");
                tvPreviewText.setTextSize(fontSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int fontSize = seekBar.getProgress() + 12;

                // Save using helper
                FontSizeHelper.saveFontSize(DisplaySettingsActivity.this, fontSize);

                // Also save to local prefs for backwards compatibility
                preferences.edit().putInt("font_size", fontSize).apply();

                // Notify user
                Toast.makeText(DisplaySettingsActivity.this,
                    "Đang áp dụng kích thước chữ...",
                    Toast.LENGTH_SHORT).show();

                // Recreate activity to apply font size immediately
                recreate();
            }
        });
    }

    private void applyDarkMode(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
