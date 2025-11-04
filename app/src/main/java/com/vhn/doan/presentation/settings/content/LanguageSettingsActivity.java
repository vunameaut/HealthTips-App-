package com.vhn.doan.presentation.settings.content;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vhn.doan.R;
import com.vhn.doan.utils.LocaleHelper;

/**
 * Activity cài đặt ngôn ngữ
 */
public class LanguageSettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private RadioGroup radioGroupLanguage;
    private RadioButton rbVietnamese, rbEnglish, rbChinese, rbJapanese, rbKorean, rbFrench, rbGerman, rbSpanish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_settings);

        preferences = getSharedPreferences("AppSettings", MODE_PRIVATE);

        setupViews();
        loadCurrentLanguage();
        setupListeners();
    }

    private void setupViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        radioGroupLanguage = findViewById(R.id.radioGroupLanguage);
        rbVietnamese = findViewById(R.id.rbVietnamese);
        rbEnglish = findViewById(R.id.rbEnglish);
        rbChinese = findViewById(R.id.rbChinese);
        rbJapanese = findViewById(R.id.rbJapanese);
        rbKorean = findViewById(R.id.rbKorean);
        rbFrench = findViewById(R.id.rbFrench);
        rbGerman = findViewById(R.id.rbGerman);
        rbSpanish = findViewById(R.id.rbSpanish);
    }

    private void loadCurrentLanguage() {
        String currentLanguage = preferences.getString("app_language", "vi");

        switch (currentLanguage) {
            case "vi":
                rbVietnamese.setChecked(true);
                break;
            case "en":
                rbEnglish.setChecked(true);
                break;
            case "zh":
                rbChinese.setChecked(true);
                break;
            case "ja":
                rbJapanese.setChecked(true);
                break;
            case "ko":
                rbKorean.setChecked(true);
                break;
            case "fr":
                rbFrench.setChecked(true);
                break;
            case "de":
                rbGerman.setChecked(true);
                break;
            case "es":
                rbSpanish.setChecked(true);
                break;
        }
    }

    private void setupListeners() {
        radioGroupLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            String languageCode = "";
            String languageName = "";

            if (checkedId == R.id.rbVietnamese) {
                languageCode = "vi";
                languageName = "Tiếng Việt";
            } else if (checkedId == R.id.rbEnglish) {
                languageCode = "en";
                languageName = "English";
            } else if (checkedId == R.id.rbChinese) {
                languageCode = "zh";
                languageName = "中文";
            } else if (checkedId == R.id.rbJapanese) {
                languageCode = "ja";
                languageName = "日本語";
            } else if (checkedId == R.id.rbKorean) {
                languageCode = "ko";
                languageName = "한국어";
            } else if (checkedId == R.id.rbFrench) {
                languageCode = "fr";
                languageName = "Français";
            } else if (checkedId == R.id.rbGerman) {
                languageCode = "de";
                languageName = "Deutsch";
            } else if (checkedId == R.id.rbSpanish) {
                languageCode = "es";
                languageName = "Español";
            }

            // Apply locale using LocaleHelper
            LocaleHelper.setLocale(this, languageCode);

            Toast.makeText(this,
                "Đã chọn ngôn ngữ: " + languageName + ". Đang áp dụng...",
                Toast.LENGTH_SHORT).show();

            // Restart activity to apply changes immediately
            LocaleHelper.restartActivity(this);
        });
    }
}
