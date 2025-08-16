package com.vhn.doan.data;

import com.google.firebase.database.DataSnapshot;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class để deserialize ShortVideo từ Firebase DataSnapshot
 * Xử lý robust các kiểu dữ liệu khác nhau từ Firebase
 */
public class ShortVideoDeserializer {

    /**
     * Tạo ShortVideo object từ Firebase DataSnapshot với xử lý lỗi robust
     * @param snapshot DataSnapshot từ Firebase
     * @return ShortVideo object hoặc null nếu có lỗi
     */
    public static ShortVideo fromDataSnapshot(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }

        try {
            ShortVideo video = new ShortVideo();

            // Set ID từ key
            video.setId(snapshot.getKey());

            // Xử lý String fields
            video.setTitle(getStringValue(snapshot, "title", ""));
            video.setCaption(getStringValue(snapshot, "caption", ""));
            video.setCategoryId(getStringValue(snapshot, "categoryId", ""));
            video.setUserId(getStringValue(snapshot, "userId", ""));
            video.setCldPublicId(getStringValue(snapshot, "cldPublicId", ""));
            video.setStatus(getStringValue(snapshot, "status", "ready"));

            // Xử lý Long fields (có thể là Number hoặc String)
            video.setUploadDate(getLongValue(snapshot, "uploadDate", System.currentTimeMillis()));
            video.setViewCount(getLongValue(snapshot, "viewCount", 0L));
            video.setLikeCount(getLongValue(snapshot, "likeCount", 0L));
            video.setCldVersion(getLongValue(snapshot, "cldVersion", 1L));

            // Xử lý Tags Map
            video.setTags(getTagsMap(snapshot, "tags"));

            return video;

        } catch (Exception e) {
            android.util.Log.e("ShortVideoDeserializer",
                "Lỗi khi deserialize video ID: " + snapshot.getKey(), e);
            return null;
        }
    }

    /**
     * Lấy giá trị String từ DataSnapshot
     */
    private static String getStringValue(DataSnapshot snapshot, String field, String defaultValue) {
        try {
            DataSnapshot fieldSnapshot = snapshot.child(field);
            if (fieldSnapshot.exists()) {
                Object value = fieldSnapshot.getValue();
                if (value != null) {
                    return value.toString();
                }
            }
        } catch (Exception e) {
            android.util.Log.w("ShortVideoDeserializer",
                "Không thể đọc string field: " + field, e);
        }
        return defaultValue;
    }

    /**
     * Lấy giá trị Long từ DataSnapshot (xử lý cả Number và String)
     */
    private static long getLongValue(DataSnapshot snapshot, String field, long defaultValue) {
        try {
            DataSnapshot fieldSnapshot = snapshot.child(field);
            if (fieldSnapshot.exists()) {
                Object value = fieldSnapshot.getValue();
                if (value instanceof Number) {
                    return ((Number) value).longValue();
                } else if (value instanceof String) {
                    try {
                        return Long.parseLong((String) value);
                    } catch (NumberFormatException e) {
                        android.util.Log.w("ShortVideoDeserializer",
                            "Không thể parse string thành long cho field: " + field +
                            ", value: " + value);
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.w("ShortVideoDeserializer",
                "Không thể đọc long field: " + field, e);
        }
        return defaultValue;
    }

    /**
     * Lấy Tags Map từ DataSnapshot
     */
    private static Map<String, Boolean> getTagsMap(DataSnapshot snapshot, String field) {
        Map<String, Boolean> tags = new HashMap<>();
        try {
            DataSnapshot tagsSnapshot = snapshot.child(field);
            if (tagsSnapshot.exists()) {
                for (DataSnapshot tagSnapshot : tagsSnapshot.getChildren()) {
                    String tagName = tagSnapshot.getKey();
                    Object tagValue = tagSnapshot.getValue();

                    if (tagName != null) {
                        boolean isEnabled = false;
                        if (tagValue instanceof Boolean) {
                            isEnabled = (Boolean) tagValue;
                        } else if (tagValue instanceof String) {
                            isEnabled = Boolean.parseBoolean((String) tagValue);
                        } else if (tagValue instanceof Number) {
                            isEnabled = ((Number) tagValue).intValue() != 0;
                        }
                        tags.put(tagName, isEnabled);
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.w("ShortVideoDeserializer",
                "Không thể đọc tags map", e);
        }
        return tags;
    }
}
