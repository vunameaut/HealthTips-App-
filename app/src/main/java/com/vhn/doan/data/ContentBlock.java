package com.vhn.doan.data;

/**
 * Class đại diện cho một khối nội dung trong bài viết HealthTip
 * Hỗ trợ các loại nội dung: text, image, heading, quote
 */
public class ContentBlock {
    private String id;
    private String type; // Loại nội dung: text, image, heading, quote
    private String value; // Nội dung (text/URL ảnh)
    private ContentMetadata metadata; // Metadata bổ sung

    /**
     * Constructor rỗng cho Firebase
     */
    public ContentBlock() {
        // Constructor rỗng cần thiết cho Firebase Realtime Database
    }

    /**
     * Constructor đầy đủ
     * @param id ID duy nhất của block
     * @param type Loại nội dung (text, image, heading, quote)
     * @param value Nội dung (text/URL ảnh)
     * @param metadata Metadata bổ sung
     */
    public ContentBlock(String id, String type, String value, ContentMetadata metadata) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.metadata = metadata;
    }

    // Getters và Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ContentMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ContentMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Tạo ContentBlock từ JSON
     */
    public static ContentBlock fromJson(Object item) {
        if (item instanceof java.util.Map) {
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) item;

            // Xử lý an toàn cho id
            String id = null;
            Object idObj = map.get("id");
            if (idObj instanceof String) {
                id = (String) idObj;
            } else if (idObj != null) {
                id = idObj.toString();
            }

            // Xử lý an toàn cho type
            String type = null;
            Object typeObj = map.get("type");
            if (typeObj instanceof String) {
                type = (String) typeObj;
            } else if (typeObj != null) {
                type = typeObj.toString();
            }

            // Xử lý an toàn cho value - có thể là String hoặc ArrayList
            String value = null;
            Object valueObj = map.get("value");
            if (valueObj instanceof String) {
                value = (String) valueObj;
            } else if (valueObj instanceof java.util.ArrayList) {
                // Nếu là ArrayList, chuyển thành chuỗi hoặc bỏ qua
                value = "";
            } else if (valueObj != null) {
                // Nếu là kiểu khác, chuyển thành String
                value = valueObj.toString();
            }

            ContentMetadata metadata = null;
            if (map.containsKey("metadata") && map.get("metadata") != null) {
                Object metadataObj = map.get("metadata");
                if (metadataObj instanceof java.util.Map) {
                    java.util.Map<String, Object> metaMap = (java.util.Map<String, Object>) metadataObj;

                    Integer level = null;
                    if (metaMap.containsKey("level") && metaMap.get("level") != null) {
                        if (metaMap.get("level") instanceof Long) {
                            level = ((Long) metaMap.get("level")).intValue();
                        } else if (metaMap.get("level") instanceof Integer) {
                            level = (Integer) metaMap.get("level");
                        }
                    }

                    // Xử lý an toàn cho alt
                    String alt = null;
                    Object altObj = metaMap.get("alt");
                    if (altObj instanceof String) {
                        alt = (String) altObj;
                    } else if (altObj != null) {
                        alt = altObj.toString();
                    }

                    // Xử lý an toàn cho caption
                    String caption = null;
                    Object captionObj = metaMap.get("caption");
                    if (captionObj instanceof String) {
                        caption = (String) captionObj;
                    } else if (captionObj != null) {
                        caption = captionObj.toString();
                    }

                    metadata = new ContentMetadata(level, alt, caption);
                }
            }

            return new ContentBlock(id, type, value, metadata);
        }

        return null;
    }

    @Override
    public String toString() {
        return "ContentBlock{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
