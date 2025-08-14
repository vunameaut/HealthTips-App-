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
        if (TextUtils.isEmpty(cldPublicId)) {
            Log.e(TAG, "publicId không được để trống");
            return null;
        }

        StringBuilder urlBuilder = new StringBuilder(BASE_VIDEO_URL);

        // Thêm các tham số tối ưu hóa
        urlBuilder.append("q_").append(quality != null ? quality : "auto:good").append("/");
        urlBuilder.append("f_auto/"); // Tự động chọn format

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
        Log.d(TAG, "Đã tạo URL tối ưu: " + finalUrl);

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
}
