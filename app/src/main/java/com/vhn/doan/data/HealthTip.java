package com.vhn.doan.data;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class đại diện cho một mẹo sức khỏe
 * Hỗ trợ load ảnh từ Cloudinary với tối ưu hóa cho mobile
 * Hỗ trợ định dạng nội dung mới với ContentBlock
 */
public class HealthTip {
    private String id;
    private String title;
    @Exclude
    private String content; // Đánh dấu là Exclude để tránh lỗi deserialize khi cấu trúc dữ liệu thay đổi
    private String categoryId;
    private String categoryName;
    private Integer viewCount;
    private Integer likeCount;
    private String imageUrl;
    private Long createdAt; // Thay đổi từ long thành Long để tương thích với Firebase
    private boolean isFavorite;
    private boolean isLiked;
    private Integer recommendationScore;

    // Thêm các trường mới
    private String excerpt;
    private String status; // 'draft', 'published', 'archived', 'review'
    private List<String> tags;
    private String author;
    private Long publishedAt;
    private Long updatedAt;
    private Boolean isFeature;
    private Boolean isPinned;
    private String seoTitle;
    private String seoDescription;
    private Long scheduledAt;
    private String slug;

    // Lưu trữ contentBlocks riêng biệt thay vì dùng trường contentObj chung
    private List<Map<String, Object>> contentBlocks;

    // Cache của nội dung đã được chuyển đổi
    @Exclude
    private transient List<ContentBlock> parsedContentBlocks;

    /**
     * Constructor rỗng cho Firebase
     */
    public HealthTip() {
        // Constructor rỗng cần thiết cho Firebase Realtime Database
    }

    /**
     * Constructor đầy đủ
     * @param id ID của mẹo sức khỏe
     * @param title Tiêu đề mẹo sức khỏe
     * @param content Nội dung chi tiết dạng text
     * @param categoryId ID của danh mục chứa mẹo này
     * @param viewCount Số lượt xem
     * @param likeCount Số lượt thích
     */
    public HealthTip(String id, String title, String content, String categoryId, int viewCount, int likeCount) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.createdAt = System.currentTimeMillis();
        this.isFavorite = false;
        this.isLiked = false;
    }

    /**
     * Constructor đầy đủ với ảnh và thời gian tạo
     */
    public HealthTip(String id, String title, String content, String categoryId, int viewCount,
                     int likeCount, String imageUrl, long createdAt, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.isFavorite = isFavorite;
        this.isLiked = false;
    }

    // Getters và Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter cho trường content
     * Được đánh dấu @Exclude để không được serialize/deserialize trực tiếp từ Firebase
     * @return Nội dung dạng text
     */
    @Exclude
    public String getContent() {
        return content;
    }

    /**
     * Setter cho trường content
     * Được đánh dấu @Exclude để không được serialize/deserialize trực tiếp từ Firebase
     * @param content Nội dung dạng text
     */
    @Exclude
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Lấy nội dung dưới dạng List<ContentBlock>
     * Chuyển đổi từ contentBlocks (Map) sang ContentBlock
     */
    @Exclude
    public List<ContentBlock> getContentBlockObjects() {
        // Nếu đã được cache
        if (parsedContentBlocks != null) {
            return parsedContentBlocks;
        }

        // Nếu contentBlocks là List<Map> (format mới)
        if (contentBlocks != null && !contentBlocks.isEmpty()) {
            parsedContentBlocks = new ArrayList<>();

            for (Map<String, Object> item : contentBlocks) {
                ContentBlock block = ContentBlock.fromJson(item);
                if (block != null) {
                    parsedContentBlocks.add(block);
                }
            }
            return parsedContentBlocks;
        }

        // Nếu content là String (format cũ) - tự động chuyển đổi sang ContentBlock
        if (content != null && !content.isEmpty()) {
            parsedContentBlocks = new ArrayList<>();

            ContentBlock block = new ContentBlock(
                "legacy_" + System.currentTimeMillis(),
                "text",
                content,
                null
            );
            parsedContentBlocks.add(block);
            return parsedContentBlocks;
        }

        // Nếu không có nội dung
        return new ArrayList<>();
    }

    /**
     * Set nội dung dưới dạng List<ContentBlock>
     */
    @Exclude
    public void setContentBlockObjects(List<ContentBlock> contentBlocks) {
        List<Map<String, Object>> mapList = new ArrayList<>();

        // Chuyển đổi từ ContentBlock sang Map để lưu trữ
        for (ContentBlock block : contentBlocks) {
            Map<String, Object> blockMap = new java.util.HashMap<>();
            blockMap.put("id", block.getId());
            blockMap.put("type", block.getType());
            blockMap.put("value", block.getValue());

            if (block.getMetadata() != null) {
                Map<String, Object> metaMap = new java.util.HashMap<>();
                if (block.getMetadata().getLevel() != null) {
                    metaMap.put("level", block.getMetadata().getLevel());
                }
                if (block.getMetadata().getAlt() != null) {
                    metaMap.put("alt", block.getMetadata().getAlt());
                }
                if (block.getMetadata().getCaption() != null) {
                    metaMap.put("caption", block.getMetadata().getCaption());
                }
                blockMap.put("metadata", metaMap);
            }

            mapList.add(blockMap);
        }

        this.contentBlocks = mapList;
        this.parsedContentBlocks = contentBlocks; // Cache

        // Cập nhật content để tương thích ngược
        if (!contentBlocks.isEmpty()) {
            StringBuilder textContent = new StringBuilder();
            for (ContentBlock block : contentBlocks) {
                if ("text".equals(block.getType())) {
                    textContent.append(block.getValue()).append("\n\n");
                } else if ("heading".equals(block.getType())) {
                    textContent.append(block.getValue()).append("\n\n");
                }
            }
            this.content = textContent.toString().trim();
        }
    }

    public List<Map<String, Object>> getContentBlocks() {
        return contentBlocks;
    }

    public void setContentBlocks(List<Map<String, Object>> contentBlocks) {
        this.contentBlocks = contentBlocks;
        this.parsedContentBlocks = null; // Reset cache
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getViewCount() {
        return viewCount != null ? viewCount : 0;
    }

    /**
     * Setter duy nhất để xử lý conversion từ Object sang Integer an toàn
     * Khắc phục lỗi Firebase DatabaseException với conflicting setters
     */
    public void setViewCount(Object viewCount) {
        if (viewCount == null) {
            this.viewCount = 0;
        } else if (viewCount instanceof Integer) {
            this.viewCount = (Integer) viewCount;
        } else if (viewCount instanceof Long) {
            this.viewCount = ((Long) viewCount).intValue();
        } else if (viewCount instanceof String) {
            try {
                this.viewCount = Integer.parseInt((String) viewCount);
            } catch (NumberFormatException e) {
                this.viewCount = 0;
            }
        } else {
            this.viewCount = 0;
        }
    }

    public Integer getLikeCount() {
        return likeCount != null ? likeCount : 0;
    }

    /**
     * Setter duy nhất để xử lý conversion từ Object sang Integer an toàn
     * Khắc phục lỗi Firebase DatabaseException với conflicting setters
     */
    public void setLikeCount(Object likeCount) {
        if (likeCount == null) {
            this.likeCount = 0;
        } else if (likeCount instanceof Integer) {
            this.likeCount = (Integer) likeCount;
        } else if (likeCount instanceof Long) {
            this.likeCount = ((Long) likeCount).intValue();
        } else if (likeCount instanceof String) {
            try {
                this.likeCount = Integer.parseInt((String) likeCount);
            } catch (NumberFormatException e) {
                this.likeCount = 0;
            }
        } else {
            this.likeCount = 0;
        }
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getCreatedAt() {
        return createdAt != null ? createdAt : System.currentTimeMillis();
    }

    public void setCreatedAt(Object createdAt) {
        if (createdAt == null) {
            this.createdAt = System.currentTimeMillis();
        } else if (createdAt instanceof Long) {
            this.createdAt = (Long) createdAt;
        } else if (createdAt instanceof Integer) {
            this.createdAt = ((Integer) createdAt).longValue();
        } else if (createdAt instanceof String) {
            try {
                this.createdAt = Long.parseLong((String) createdAt);
            } catch (NumberFormatException e) {
                this.createdAt = System.currentTimeMillis();
            }
        } else {
            this.createdAt = System.currentTimeMillis();
        }
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public Integer getRecommendationScore() {
        return recommendationScore;
    }

    /**
     * Setter an toàn để xử lý conversion từ Object sang Integer
     * Khắc phục lỗi Firebase DatabaseException với conversion
     */
    public void setRecommendationScore(Object recommendationScore) {
        if (recommendationScore == null) {
            this.recommendationScore = null;
        } else if (recommendationScore instanceof Integer) {
            this.recommendationScore = (Integer) recommendationScore;
        } else if (recommendationScore instanceof Long) {
            this.recommendationScore = ((Long) recommendationScore).intValue();
        } else if (recommendationScore instanceof String) {
            try {
                this.recommendationScore = Integer.parseInt((String) recommendationScore);
            } catch (NumberFormatException e) {
                this.recommendationScore = null;
            }
        } else {
            this.recommendationScore = null;
        }
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Long getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Object publishedAt) {
        if (publishedAt == null) {
            this.publishedAt = null;
        } else if (publishedAt instanceof Long) {
            this.publishedAt = (Long) publishedAt;
        } else if (publishedAt instanceof Integer) {
            this.publishedAt = ((Integer) publishedAt).longValue();
        } else if (publishedAt instanceof String) {
            try {
                this.publishedAt = Long.parseLong((String) publishedAt);
            } catch (NumberFormatException e) {
                this.publishedAt = null;
            }
        } else {
            this.publishedAt = null;
        }
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Object updatedAt) {
        if (updatedAt == null) {
            this.updatedAt = null;
        } else if (updatedAt instanceof Long) {
            this.updatedAt = (Long) updatedAt;
        } else if (updatedAt instanceof Integer) {
            this.updatedAt = ((Integer) updatedAt).longValue();
        } else if (updatedAt instanceof String) {
            try {
                this.updatedAt = Long.parseLong((String) updatedAt);
            } catch (NumberFormatException e) {
                this.updatedAt = null;
            }
        } else {
            this.updatedAt = null;
        }
    }

    public Boolean getIsFeature() {
        return isFeature;
    }

    public void setIsFeature(Boolean isFeature) {
        this.isFeature = isFeature;
    }

    public Boolean getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }

    public String getSeoTitle() {
        return seoTitle;
    }

    public void setSeoTitle(String seoTitle) {
        this.seoTitle = seoTitle;
    }

    public String getSeoDescription() {
        return seoDescription;
    }

    public void setSeoDescription(String seoDescription) {
        this.seoDescription = seoDescription;
    }

    public Long getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Object scheduledAt) {
        if (scheduledAt == null) {
            this.scheduledAt = null;
        } else if (scheduledAt instanceof Long) {
            this.scheduledAt = (Long) scheduledAt;
        } else if (scheduledAt instanceof Integer) {
            this.scheduledAt = ((Integer) scheduledAt).longValue();
        } else if (scheduledAt instanceof String) {
            try {
                this.scheduledAt = Long.parseLong((String) scheduledAt);
            } catch (NumberFormatException e) {
                this.scheduledAt = null;
            }
        } else {
            this.scheduledAt = null;
        }
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
