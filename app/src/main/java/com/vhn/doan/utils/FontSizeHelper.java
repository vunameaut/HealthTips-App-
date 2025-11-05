package com.vhn.doan.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

/**
 * Helper class for managing app-wide font size
 */
public class FontSizeHelper {

    private static final String PREFS_NAME = "DisplaySettings";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final int DEFAULT_FONT_SIZE = 16; // Default 16sp

    /**
     * Apply saved font size configuration to context
     * @param context Context
     * @return Updated context with font size applied
     */
    public static Context applyFontSize(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int fontSize = prefs.getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE);

        // Calculate scale based on default (16sp)
        float fontScale = fontSize / 16f;

        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.fontScale = fontScale;

        // Use createConfigurationContext instead of deprecated updateConfiguration
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return context.createConfigurationContext(configuration);
        } else {
            // For older Android versions, still use updateConfiguration
            Resources resources = context.getResources();
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            return context;
        }
    }

    /**
     * Get current font size
     * @param context Context
     * @return Current font size in sp
     */
    public static int getFontSize(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE);
    }

    /**
     * Save font size
     * @param context Context
     * @param fontSize Font size in sp
     */
    public static void saveFontSize(Context context, int fontSize) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_FONT_SIZE, fontSize).apply();
    }
}
