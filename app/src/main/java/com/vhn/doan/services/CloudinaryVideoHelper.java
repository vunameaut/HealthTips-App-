package com.vhn.doan.services;

import android.text.TextUtils;
import android.util.Log;

/**
 * Helper class để xử lý Cloudinary video URLs và image URLs
 * Hỗ trợ tạo URL từ publicId và version thay vì lưu trữ URL đầy đủ
 */
public class CloudinaryVideoHelper {
    private static final String TAG = "CloudinaryVideoHelper";
    
    // Thông tin Cloudinary được cung cấp bởi PM
    private static final String CLOUD_NAME = "dazo6ypwt";
    private static final String BASE_VIDEO_URL = "https://res.cloudinary.com/" + CLOUD_NAME + "/video/upload/";
    private static final String BASE_IMAGE_URL = "https://res.cloudinary.com/" + CLOUD_NAME + "/image/upload/";

    /**
     * Tạo URL video đầy đủ từ publicId và version
     * @param cldPublicId Public ID của video trên Cloudinary
     * @param cldVersion Version của video (có thể null)
     * @return URL đầy đ��� của video
     */
    public static String buildVideoUrl(String cldPublicId, String cldVersion) {
        if (TextUtils.isEmpty(cldPublicId)) {
            Log.e(TAG, "publicId không được để trống");
            return null;
        }

        StringBuilder urlBuilder = new StringBuilder(BASE_VIDEO_URL);

        // Thêm version nếu có
        if (!TextUtils.isEmpty(cldVersion)) {
            urlBuilder.append("v").append(cldVersion).append("/");
        }

        // Thêm publicId và định dạng
        urlBuilder.append(cldPublicId);
        if (!cldPublicId.endsWith(".mp4")) {
            urlBuilder.append(".mp4");
        }

        String finalUrl = urlBuilder.toString();
        Log.d(TAG, "Đã tạo URL: " + finalUrl + " từ publicId: " + cldPublicId + ", version: " + cldVersion);

        return finalUrl;
    }

    /**
     * Tạo URL video được tối ưu hóa cho mobile
     * @param cldPublicId Public ID của video
     * @param cldVersion Version của video
     * @param quality Chất lượng video (auto:good, auto:low, auto:eco)
     * @return URL video được tối ưu hóa
     */
    public static String buildOptimizedVideoUrl(String cldPublicId, String cldVersion, String quality) {
        Log.d(TAG, "=== Building optimized video URL ===");
        Log.d(TAG, "Input - PublicId: " + cldPublicId);
        Log.d(TAG, "Input - Version: " + cldVersion);
        Log.d(TAG, "Input - Quality: " + quality);

        if (TextUtils.isEmpty(cldPublicId)) {
            Log.e(TAG, "❌ publicId không được để trống");
            return null;
        }

        // Validation: đảm bảo publicId không chứa extension không mong muốn
        String cleanPublicId = cldPublicId;
        if (cleanPublicId.contains(".")) {
            // Loại bỏ extension nếu có (Cloudinary sẽ tự thêm .mp4)
            cleanPublicId = cleanPublicId.substring(0, cleanPublicId.lastIndexOf("."));
            Log.d(TAG, "Cleaned publicId (removed extension): " + cleanPublicId);
        }

        StringBuilder urlBuilder = new StringBuilder(BASE_VIDEO_URL);
        Log.d(TAG, "Base URL: " + BASE_VIDEO_URL);

        // Thêm các tham số tối ưu hóa
        String finalQuality = quality != null ? quality : "auto:good";
        urlBuilder.append("q_").append(finalQuality).append("/");
        Log.d(TAG, "Added quality parameter: q_" + finalQuality);

        urlBuilder.append("f_auto/"); // Tự động chọn format
        Log.d(TAG, "Added auto format: f_auto");

        // Thêm version nếu có
        if (!TextUtils.isEmpty(cldVersion)) {
            urlBuilder.append("v").append(cldVersion).append("/");
            Log.d(TAG, "Added version: v" + cldVersion);
        } else {
            Log.d(TAG, "No version specified");
        }

        // Thêm publicId và định dạng
        urlBuilder.append(cleanPublicId);
        if (!cleanPublicId.endsWith(".mp4")) {
            urlBuilder.append(".mp4");
            Log.d(TAG, "Added .mp4 extension");
        }

        String finalUrl = urlBuilder.toString();
        Log.d(TAG, "✅ Generated optimized URL: " + finalUrl);

        // Validation cuối cùng
        if (!isValidCloudinaryVideoUrl(finalUrl)) {
            Log.e(TAG, "❌ Generated URL failed validation: " + finalUrl);
            return null;
        }

        return finalUrl;
    }

    /**
     * Tạo URL image tối ưu hóa cho mobile
     * @param cldPublicId Public ID của image
     * @param cldVersion Version của image
     * @param width Chiều rộng mong muốn
     * @param height Chiều cao mong muốn
     * @param quality Chất lượng ảnh (auto:good, auto:low, auto:eco)
     * @return URL image được tối ưu hóa
     */
    public static String buildOptimizedImageUrl(String cldPublicId, String cldVersion, int width, int height, String quality) {
        if (TextUtils.isEmpty(cldPublicId)) {
            Log.e(TAG, "publicId không được để trống");
            return null;
        }

        StringBuilder urlBuilder = new StringBuilder(BASE_IMAGE_URL);

        // Thêm các tham số tối ưu hóa
        urlBuilder.append("w_").append(width).append(",h_").append(height).append(",c_fill/");
        urlBuilder.append("q_").append(quality != null ? quality : "auto:good").append("/");
        urlBuilder.append("f_auto/"); // Tự động chọn format

        // Thêm version nếu có
        if (!TextUtils.isEmpty(cldVersion)) {
            urlBuilder.append("v").append(cldVersion).append("/");
        }

        // Thêm publicId
        urlBuilder.append(cldPublicId);

        String finalUrl = urlBuilder.toString();
        Log.d(TAG, "Đã tạo URL image tối ưu: " + finalUrl);

        return finalUrl;
    }

    /**
     * Kiểm tra xem URL có phải từ Cloudinary video không
     * @param url URL cần kiểm tra
     * @return true nếu là URL Cloudinary video
     */
    public static boolean isCloudinaryVideoUrl(String url) {
        return !TextUtils.isEmpty(url) && url.contains("res.cloudinary.com") && url.contains("video/upload");
    }

    /**
     * Kiểm tra xem URL có phải từ Cloudinary image không
     * @param url URL cần kiểm tra
     * @return true nếu là URL Cloudinary image
     */
    public static boolean isCloudinaryImageUrl(String url) {
        return !TextUtils.isEmpty(url) && url.contains("res.cloudinary.com") && url.contains("image/upload");
    }

    /**
     * Lấy optimized video URL từ URL gốc (backward compatibility)
     * @param originalUrl URL gốc
     * @param quality Chất lượng mong muốn
     * @return URL được tối ưu hóa
     */
    public static String getOptimizedVideoUrl(String originalUrl, String quality) {
        if (TextUtils.isEmpty(originalUrl) || !isCloudinaryVideoUrl(originalUrl)) {
            return originalUrl;
        }

        try {
            // Trích xuất publicId và version từ URL gốc
            String[] parts = originalUrl.split("/");
            String publicIdWithExt = parts[parts.length - 1];
            String publicId = publicIdWithExt.replace(".mp4", "");

            // Tìm version trong URL
            String version = null;
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].startsWith("v") && parts[i].length() > 1) {
                    version = parts[i].substring(1); // Bỏ ký tự 'v'
                    break;
                }
            }

            return buildOptimizedVideoUrl(publicId, version, quality);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tối ưu hóa URL: " + originalUrl, e);
            return originalUrl;
        }
    }

    /**
     * Lấy optimized image URL từ URL gốc với kích thước và chất lượng mong muốn
     * @param originalUrl URL gốc
     * @param width Chiều rộng mong muốn
     * @param height Chiều cao mong muốn
     * @param quality Chất lượng mong muốn
     * @return URL được tối ưu hóa
     */
    public static String getOptimizedImageUrl(String originalUrl, int width, int height, String quality) {
        if (TextUtils.isEmpty(originalUrl) || !isCloudinaryImageUrl(originalUrl)) {
            return originalUrl;
        }

        try {
            // Trích xuất publicId và version từ URL gốc
            String[] parts = originalUrl.split("/");
            String publicIdWithExt = parts[parts.length - 1];

            // Loại bỏ extension nếu có
            String publicId = publicIdWithExt;
            int dotIndex = publicIdWithExt.lastIndexOf('.');
            if (dotIndex > 0) {
                publicId = publicIdWithExt.substring(0, dotIndex);
            }

            // Tìm version trong URL
            String version = null;
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].startsWith("v") && parts[i].length() > 1) {
                    version = parts[i].substring(1); // Bỏ ký tự 'v'
                    break;
                }
            }

            return buildOptimizedImageUrl(publicId, version, width, height, quality);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tối ưu hóa image URL: " + originalUrl, e);
            return originalUrl;
        }
    }

    /**
     * Trích xuất publicId từ URL Cloudinary
     * @param url URL Cloudinary
     * @return publicId hoặc null nếu không trích xuất được
     */
    public static String extractPublicId(String url) {
        if (TextUtils.isEmpty(url) || (!isCloudinaryVideoUrl(url) && !isCloudinaryImageUrl(url))) {
            return null;
        }

        try {
            String[] parts = url.split("/");
            String publicIdWithExt = parts[parts.length - 1];

            // Loại bỏ extension
            int dotIndex = publicIdWithExt.lastIndexOf('.');
            if (dotIndex > 0) {
                return publicIdWithExt.substring(0, dotIndex);
            }
            return publicIdWithExt;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi trích xuất publicId từ URL: " + url, e);
            return null;
        }
    }

    /**
     * Trích xuất version từ URL Cloudinary
     * @param url URL Cloudinary
     * @return version hoặc null nếu không có
     */
    public static String extractVersion(String url) {
        if (TextUtils.isEmpty(url) || (!isCloudinaryVideoUrl(url) && !isCloudinaryImageUrl(url))) {
            return null;
        }

        try {
            String[] parts = url.split("/");
            for (String part : parts) {
                if (part.startsWith("v") && part.length() > 1) {
                    return part.substring(1); // Bỏ ký tự 'v'
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi trích xuất version từ URL: " + url, e);
        }
        
        return null;
    }

    /**
     * Validate URL Cloudinary video
     */
    private static boolean isValidCloudinaryVideoUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            Log.e(TAG, "URL validation failed: URL is empty");
            return false;
        }

        if (!url.startsWith("https://res.cloudinary.com/")) {
            Log.e(TAG, "URL validation failed: Not a valid Cloudinary URL");
            return false;
        }

        if (!url.contains("/video/upload/")) {
            Log.e(TAG, "URL validation failed: Not a video URL (missing /video/upload/)");
            return false;
        }

        if (!url.endsWith(".mp4")) {
            Log.e(TAG, "URL validation failed: Does not end with .mp4");
            return false;
        }

        Log.d(TAG, "✅ URL validation passed");
        return true;
    }

    /**
     * Method debug để test URL video
     * Gọi method này để kiểm tra URL có accessible không
     */
    public static void testVideoUrl(String videoUrl, String videoId) {
        Log.d(TAG, "=== TESTING VIDEO URL ===");
        Log.d(TAG, "Video ID: " + videoId);
        Log.d(TAG, "Video URL: " + videoUrl);

        if (TextUtils.isEmpty(videoUrl)) {
            Log.e(TAG, "❌ URL is null or empty");
            return;
        }

        // Test các thành phần của URL
        if (!videoUrl.startsWith("https://")) {
            Log.e(TAG, "❌ URL does not start with https://");
        } else {
            Log.d(TAG, "✅ URL starts with https://");
        }

        if (!videoUrl.contains("cloudinary.com")) {
            Log.e(TAG, "❌ URL is not from Cloudinary");
        } else {
            Log.d(TAG, "✅ URL is from Cloudinary");
        }

        if (!videoUrl.contains("video/upload")) {
            Log.e(TAG, "❌ URL is not a video URL (missing video/upload)");
            Log.e(TAG, "❌ This might be an IMAGE URL instead of VIDEO URL!");
        } else {
            Log.d(TAG, "✅ URL contains video/upload");
        }

        if (!videoUrl.endsWith(".mp4")) {
            Log.e(TAG, "❌ URL does not end with .mp4");
        } else {
            Log.d(TAG, "✅ URL ends with .mp4");
        }

        // Phân tích các component
        try {
            String[] parts = videoUrl.split("/");
            Log.d(TAG, "URL parts count: " + parts.length);
            for (int i = 0; i < parts.length; i++) {
                Log.d(TAG, "Part[" + i + "]: " + parts[i]);
            }

            // Tìm cloud name
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("res.cloudinary.com") && i + 1 < parts.length) {
                    Log.d(TAG, "Cloud name: " + parts[i + 1]);
                    break;
                }
            }

            // Tìm publicId
            if (parts.length > 0) {
                String lastPart = parts[parts.length - 1];
                String publicId = lastPart.replace(".mp4", "");
                Log.d(TAG, "Extracted publicId: " + publicId);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error analyzing URL: " + e.getMessage());
        }

        Log.d(TAG, "=== END TEST VIDEO URL ===");
    }
}
