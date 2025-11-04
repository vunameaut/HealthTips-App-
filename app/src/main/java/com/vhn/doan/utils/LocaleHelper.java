package com.vhn.doan.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

/**
 * Helper class for managing app locale/language
 */
public class LocaleHelper {

    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_LANGUAGE = "app_language";
    private static final String DEFAULT_LANGUAGE = "vi"; // Vietnamese as default

    /**
     * Set and apply new locale to the app
     * @param context Context
     * @param languageCode Language code (vi, en, zh, ja, ko, fr, de, es)
     * @return Updated context with new locale
     */
    public static Context setLocale(Context context, String languageCode) {
        // Save to preferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();

        // Apply locale
        return updateResources(context, languageCode);
    }

    /**
     * Load saved locale and apply it
     * @param context Context
     * @return Updated context with saved locale
     */
    public static Context loadLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String languageCode = prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);
        return updateResources(context, languageCode);
    }

    /**
     * Get current language code
     * @param context Context
     * @return Current language code
     */
    public static String getCurrentLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }

    /**
     * Update app resources with new locale
     * @param context Context
     * @param languageCode Language code
     * @return Updated context
     */
    private static Context updateResources(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            return context.createConfigurationContext(configuration);
        } else {
            configuration.locale = locale;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            return context;
        }
    }

    /**
     * Restart activity to apply language changes immediately
     * @param activity Activity to restart
     */
    public static void restartActivity(Activity activity) {
        Intent intent = activity.getIntent();
        activity.finish();
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Apply locale to configuration (for use in attachBaseContext)
     * @param context Base context
     * @return Context with applied locale
     */
    public static Context onAttach(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String languageCode = prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);
        return updateResources(context, languageCode);
    }
}
