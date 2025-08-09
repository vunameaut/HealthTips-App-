package com.vhn.doan.services;

import android.util.Log;

/**
 * Helper class để xử lý video URLs từ Cloudinary
 * Chỉ dùng để load video từ links có sẵn, không upload
 * Phù hợp với việc sử dụng Firebase Realtime Database để lưu metadata và Cloudinary URLs
 */
public class CloudinaryVideoHelper {
    private static final String TAG = "CloudinaryVideoHelper";
    
    /**
     * Tạo URL video được tối ưu cho mobile từ URL gốc
     * @param originalUrl URL gốc của video từ Cloudinary
     * @param quality Quality level (auto:low, auto:good, auto:best)
     * @return URL video được tối ưu cho mobile
     */
    public static String getOptimizedVideoUrl(String originalUrl, String quality) {
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            Log.w(TAG, "URL gốc null hoặc rỗng");
            return "";
        }
        
        if (!isCloudinaryVideoUrl(originalUrl)) {
            Log.d(TAG, "Không phải URL Cloudinary, trả về URL gốc: " + originalUrl);
            return originalUrl;
        }

        if (quality == null || quality.trim().isEmpty()) {
            quality = "auto:good"; // Chất lượng mặc định
        }
        
        try {
            // Tìm vị trí của "/upload/" để chèn transformations
            String uploadMarker = "/upload/";
            int uploadIndex = originalUrl.indexOf(uploadMarker);

            if (uploadIndex != -1) {
                String beforeUpload = originalUrl.substring(0, uploadIndex + uploadMarker.length());
                String afterUpload = originalUrl.substring(uploadIndex + uploadMarker.length());

                // Loại bỏ transformations cũ nếu có (bắt đầu bằng v_... hoặc q_... etc.)
                if (afterUpload.matches("^[a-z]_.*")) {
                    // Tìm dấu "/" đầu tiên sau transformations để lấy public ID
                    int firstSlash = afterUpload.indexOf('/');
                    if (firstSlash != -1) {
                        afterUpload = afterUpload.substring(firstSlash + 1);
                    }
                }

                // Tạo URL với optimization cho mobile
                String optimizedUrl = beforeUpload + "q_" + quality + ",w_720,h_1280,c_fill/" + afterUpload;
                Log.d(TAG, "URL được tối ưu: " + optimizedUrl);
                return optimizedUrl;
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tối ưu URL: " + e.getMessage());
        }

        return originalUrl; // Trả về URL gốc nếu có lỗi
    }

    /**
     * Tạo URL thumbnail từ URL video Cloudinary
     * @param videoUrl URL video gốc
     * @return URL thumbnail được tạo từ video
     */
    public static String getThumbnailFromVideoUrl(String videoUrl) {
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            Log.w(TAG, "Video URL null hoặc rỗng cho thumbnail");
            return "";
        }

        if (!isCloudinaryVideoUrl(videoUrl)) {
            Log.d(TAG, "Không phải URL Cloudinary video, không thể tạo thumbnail");
            return "";
        }

        try {
            // Thay đổi resource type từ video sang image và thêm transformations
            String thumbnailUrl = videoUrl.replace("/video/upload/", "/video/upload/w_480,h_854,c_fill,f_jpg/")
                                           .replaceAll("\\.(mp4|mov|avi)$", ".jpg");

            Log.d(TAG, "URL thumbnail được tạo: " + thumbnailUrl);
            return thumbnailUrl;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo thumbnail URL: " + e.getMessage());
            return "";
        }
    }

    /**
     * Kiểm tra xem URL có phải là Cloudinary video URL không
     * @param url URL cần kiểm tra
     * @return true nếu là Cloudinary video URL
     */
    public static boolean isCloudinaryVideoUrl(String url) {
        return url != null &&
               url.contains("cloudinary.com") &&
               url.contains("/video/upload/");
    }

    /**
     * Kiểm tra xem URL có phải là Cloudinary image URL không
     * @param url URL cần kiểm tra
     * @return true nếu là Cloudinary image URL
     */
    public static boolean isCloudinaryImageUrl(String url) {
        return url != null &&
               url.contains("cloudinary.com") &&
               url.contains("/image/upload/");
    }

    /**
     * Lấy URL với chất lượng thấp cho preview hoặc loading nhanh
     * @param originalUrl URL gốc
     * @return URL với chất lượng thấp
     */
    public static String getLowQualityUrl(String originalUrl) {
        return getOptimizedVideoUrl(originalUrl, "auto:low");
    }

    /**
     * Lấy URL với chất lượng cao
     * @param originalUrl URL gốc
     * @return URL với chất lượng cao
     */
    public static String getHighQualityUrl(String originalUrl) {
        return getOptimizedVideoUrl(originalUrl, "auto:best");
    }
    
    /**
     * Tạo URL video responsive dựa trên kích thước màn hình
     * @param originalUrl URL gốc
     * @param screenWidth Chiều rộng màn hình (px)
     * @param screenHeight Chiều cao màn hình (px)
     * @return URL video phù hợp với màn hình
     */
    public static String getResponsiveVideoUrl(String originalUrl, int screenWidth, int screenHeight) {
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            return "";
        }
        
        if (!isCloudinaryVideoUrl(originalUrl)) {
            return originalUrl;
        }

        try {
            String uploadMarker = "/upload/";
            int uploadIndex = originalUrl.indexOf(uploadMarker);

            if (uploadIndex != -1) {
                String beforeUpload = originalUrl.substring(0, uploadIndex + uploadMarker.length());
                String afterUpload = originalUrl.substring(uploadIndex + uploadMarker.length());

                // Loại bỏ transformations cũ
                if (afterUpload.matches("^[a-z]_.*")) {
                    int firstSlash = afterUpload.indexOf('/');
                    if (firstSlash != -1) {
                        afterUpload = afterUpload.substring(firstSlash + 1);
                    }
                }

                // Tính toán kích thước phù hợp (giới hạn tối đa để tiết kiệm bandwidth)
                int maxWidth = Math.min(screenWidth, 1080);
                int maxHeight = Math.min(screenHeight, 1920);

                String responsiveUrl = beforeUpload +
                    "q_auto:good,w_" + maxWidth + ",h_" + maxHeight + ",c_fill/" + afterUpload;

                Log.d(TAG, "URL responsive: " + responsiveUrl + " (Màn hình: " + screenWidth + "x" + screenHeight + ")");
                return responsiveUrl;
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo responsive URL: " + e.getMessage());
        }
        
        return originalUrl;
    }
    
    /**
     * Kiểm tra tính hợp lệ của Cloudinary URL
     * @param url URL cần kiểm tra
     * @return true nếu URL hợp lệ và có thể truy cập được
     */
    public static boolean isValidCloudinaryUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        // Kiểm tra format cơ bản
        return (isCloudinaryVideoUrl(url) || isCloudinaryImageUrl(url)) &&
               url.startsWith("https://") &&
               !url.contains(" ") && // Không có khoảng trắng
               url.length() > 50; // Độ dài tối thiểu hợp lý
    }

    /**
     * Log thông tin debug về URL Cloudinary
     * @param url URL cần debug
     * @param context Ngữ cảnh sử dụng
     */
    public static void debugUrl(String url, String context) {
        Log.d(TAG, "=== DEBUG URL ===");
        Log.d(TAG, "Context: " + context);
        Log.d(TAG, "URL: " + url);
        Log.d(TAG, "Is Cloudinary Video: " + isCloudinaryVideoUrl(url));
        Log.d(TAG, "Is Cloudinary Image: " + isCloudinaryImageUrl(url));
        Log.d(TAG, "Is Valid: " + isValidCloudinaryUrl(url));
        Log.d(TAG, "================");
    }
}
