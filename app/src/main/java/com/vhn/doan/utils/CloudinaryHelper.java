package com.vhn.doan.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Lớp tiện ích để xử lý tải ảnh lên Cloudinary
 */
public class CloudinaryHelper {
    private static final String TAG = "CloudinaryHelper";
    private static boolean isInitialized = false;

    /**
     * Khởi tạo Cloudinary nếu chưa được khởi tạo
     * @param context Context của ứng dụng
     */
    public static void initCloudinary(Context context) {
        if (isInitialized) return;

        try {
            Map<String, Object> config = new HashMap<>();
            // Sử dụng cloud name đã được xác định từ CloudinaryUrls.java
            config.put("cloud_name", "dazo6ypwt");

            // API key và secret mới cho cloud name dazo6ypwt
            String apiKey = "927714775247856";
            String apiSecret = "esenGxBrjluyPRmHtFdDpJY9n-Q";

            config.put("api_key", apiKey);
            config.put("api_secret", apiSecret);
            config.put("secure", true);

            MediaManager.init(context, config);
            isInitialized = true;
            Log.d(TAG, "Cloudinary đã được khởi tạo thành công");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi khởi tạo Cloudinary: " + e.getMessage());
        }
    }

    /**
     * Upload ảnh lên Cloudinary
     * @param context Context của ứng dụng
     * @param imageUri Uri của ảnh cần upload
     * @param userId ID của người dùng để tạo thư mục
     * @param callback callback để nhận kết quả
     */
    public static void uploadUserAvatar(Context context, Uri imageUri, String userId,
                                        CloudinaryUploadCallback callback) {
        if (!isInitialized) {
            initCloudinary(context);
        }

        String requestId = MediaManager.get().upload(imageUri)
                .option("folder", "users/" + userId)
                .option("public_id", "avatar_" + System.currentTimeMillis())
                .option("overwrite", true)
                .option("resource_type", "image")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        callback.onUploadStart();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        double progress = (double) bytes / totalBytes;
                        callback.onUploadProgress((int) (progress * 100));
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String secureUrl = (String) resultData.get("secure_url");
                        callback.onUploadSuccess(secureUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        callback.onUploadError("Lỗi khi tải ảnh lên: " + error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        callback.onUploadError("Upload bị trì hoãn: " + error.getDescription());
                    }
                })
                .dispatch();
    }

    /**
     * Interface callback cho việc upload ảnh lên Cloudinary
     */
    public interface CloudinaryUploadCallback {
        void onUploadStart();
        void onUploadProgress(int progress);
        void onUploadSuccess(String imageUrl);
        void onUploadError(String errorMessage);
    }
}
