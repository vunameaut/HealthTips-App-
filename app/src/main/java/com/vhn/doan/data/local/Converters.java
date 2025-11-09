package com.vhn.doan.data.local;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * TypeConverter cho Room Database
 * Chuyển đổi các kiểu dữ liệu phức tạp sang dạng có thể lưu trong SQLite
 */
public class Converters {
    private static final Gson gson = new Gson();

    /**
     * Chuyển đổi List<String> sang JSON String
     */
    @TypeConverter
    public static String fromStringList(List<String> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list);
    }

    /**
     * Chuyển đổi JSON String sang List<String>
     */
    @TypeConverter
    public static List<String> toStringList(String json) {
        if (json == null) {
            return null;
        }
        Type listType = new TypeToken<List<String>>(){}.getType();
        return gson.fromJson(json, listType);
    }

    /**
     * Chuyển đổi List<Map<String, Object>> sang JSON String
     */
    @TypeConverter
    public static String fromContentBlocks(List<Map<String, Object>> contentBlocks) {
        if (contentBlocks == null) {
            return null;
        }
        return gson.toJson(contentBlocks);
    }

    /**
     * Chuyển đổi JSON String sang List<Map<String, Object>>
     */
    @TypeConverter
    public static List<Map<String, Object>> toContentBlocks(String json) {
        if (json == null) {
            return null;
        }
        Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
        return gson.fromJson(json, listType);
    }
}
