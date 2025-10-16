package com.vhn.doan.data;

import android.net.Uri;

/**
 * Model để lưu trữ thông tin âm thanh nhắc nhở
 */
public class ReminderSound {
    private String id;
    private String name;
    private String displayName;
    private Uri soundUri;
    private boolean isBuiltIn;
    private String category;
    private int iconResId;

    public ReminderSound() {
        // Constructor rỗng cho Firebase
    }

    public ReminderSound(String id, String name, String displayName, Uri soundUri, boolean isBuiltIn, String category, int iconResId) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.soundUri = soundUri;
        this.isBuiltIn = isBuiltIn;
        this.category = category;
        this.iconResId = iconResId;
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Uri getSoundUri() { return soundUri; }
    public void setSoundUri(Uri soundUri) { this.soundUri = soundUri; }

    public boolean isBuiltIn() { return isBuiltIn; }
    public void setBuiltIn(boolean builtIn) { isBuiltIn = builtIn; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
}
