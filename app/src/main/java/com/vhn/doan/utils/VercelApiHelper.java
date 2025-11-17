package com.vhn.doan.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Helper class để gọi Vercel API Backend cho notifications & recommendations
 * Backend URL: https://healthtips-admin.vercel.app
 */
public class VercelApiHelper {

    private static final String TAG = "VercelApiHelper";
    private static final String BASE_URL = "https://healthtips-admin.vercel.app";

    // API Endpoints
    private static final String ENDPOINT_COMMENT_REPLY = "/api/send-comment-reply";
    private static final String ENDPOINT_NEW_HEALTH_TIP = "/api/send-new-health-tip";
    private static final String ENDPOINT_QUEUE_RECOMMENDATION = "/api/queue-recommendation";
    private static final String ENDPOINT_GET_RECOMMENDATIONS = "/api/recommendations/generate-auto";

    private static VercelApiHelper instance;
    private final OkHttpClient httpClient;

    public interface ApiCallback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }

    private VercelApiHelper(Context context) {
        // Tạo OkHttpClient với timeout phù hợp
        httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();
    }

    public static synchronized VercelApiHelper getInstance(Context context) {
        if (instance == null) {
            instance = new VercelApiHelper(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Gửi notification khi có reply comment mới
     * @param videoId ID của video
     * @param parentCommentId ID của comment cha
     * @param replyCommentId ID của reply mới
     * @param senderUserId ID của người gửi reply
     * @param callback Callback để nhận kết quả
     */
    public void sendCommentReplyNotification(String videoId, String parentCommentId,
                                             String replyCommentId, String senderUserId,
                                             ApiCallback callback) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("videoId", videoId);
            payload.put("parentCommentId", parentCommentId);
            payload.put("replyCommentId", replyCommentId);
            payload.put("senderUserId", senderUserId);

            makePostRequest(ENDPOINT_COMMENT_REPLY, payload, callback);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating comment reply payload", e);
            if (callback != null) {
                callback.onError("Lỗi tạo dữ liệu: " + e.getMessage());
            }
        }
    }

    /**
     * Gửi notification broadcast cho bài viết mới (từ Admin)
     * @param healthTipId ID của bài viết
     * @param title Tiêu đề bài viết
     * @param category Category của bài viết
     * @param adminUserId ID của admin đăng bài
     * @param callback Callback để nhận kết quả
     */
    public void sendNewHealthTipNotification(String healthTipId, String title,
                                             String category, String adminUserId,
                                             ApiCallback callback) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("healthTipId", healthTipId);
            payload.put("title", title);
            payload.put("category", category);
            payload.put("adminUserId", adminUserId);
            payload.put("sendNotification", true);

            makePostRequest(ENDPOINT_NEW_HEALTH_TIP, payload, callback);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating new health tip payload", e);
            if (callback != null) {
                callback.onError("Lỗi tạo dữ liệu: " + e.getMessage());
            }
        }
    }

    /**
     * Thêm health tip vào hàng đợi để gửi recommendation hàng ngày
     * @param healthTipId ID của bài viết
     * @param categoryId Category ID
     * @param title Tiêu đề
     * @param callback Callback để nhận kết quả
     */
    public void queueHealthTipRecommendation(String healthTipId, String categoryId,
                                             String title, ApiCallback callback) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("healthTipId", healthTipId);
            payload.put("categoryId", categoryId);
            payload.put("title", title);

            makePostRequest(ENDPOINT_QUEUE_RECOMMENDATION, payload, callback);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating queue recommendation payload", e);
            if (callback != null) {
                callback.onError("Lỗi tạo dữ liệu: " + e.getMessage());
            }
        }
    }

    /**
     * Lấy personalized recommendations cho user
     * @param userId ID của user
     * @param limit Số lượng recommendations cần lấy
     * @param algorithm Thuật toán: "content", "collaborative", "trending", hoặc "hybrid" (mặc định)
     * @param callback Callback để nhận kết quả
     */
    public void getPersonalizedRecommendations(String userId, int limit, String algorithm, ApiCallback callback) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("userId", userId);
            payload.put("limit", limit);
            payload.put("sendNotification", false); // Không gửi notification khi load trang
            payload.put("algorithm", algorithm != null ? algorithm : "hybrid");

            makePostRequest(ENDPOINT_GET_RECOMMENDATIONS, payload, callback);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating recommendations payload", e);
            if (callback != null) {
                callback.onError("Lỗi tạo dữ liệu: " + e.getMessage());
            }
        }
    }

    /**
     * Thực hiện POST request đến Vercel API
     */
    private void makePostRequest(String endpoint, JSONObject payload, ApiCallback callback) {
        String url = BASE_URL + endpoint;

        RequestBody body = RequestBody.create(
            payload.toString(),
            MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build();

        Log.d(TAG, "Sending request to: " + url);
        Log.d(TAG, "Payload: " + payload.toString());

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "API request failed", e);
                if (callback != null) {
                    callback.onError("Lỗi kết nối: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    Log.e(TAG, "API request unsuccessful: " + response.code() + " - " + responseBody);
                    if (callback != null) {
                        callback.onError("Lỗi server: " + response.code());
                    }
                    return;
                }

                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    Log.d(TAG, "API response: " + jsonResponse.toString());

                    if (callback != null) {
                        callback.onSuccess(jsonResponse);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing response", e);
                    if (callback != null) {
                        callback.onError("Lỗi xử lý phản hồi: " + e.getMessage());
                    }
                }
            }
        });
    }
}
