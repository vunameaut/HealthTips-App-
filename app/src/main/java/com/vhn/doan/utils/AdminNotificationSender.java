package com.vhn.doan.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Utility class for sending notifications to web admin
 */
public class AdminNotificationSender {

    private static final String TAG = "AdminNotificationSender";

    // Production web admin URL (stable domain that doesn't change with each deployment)
    private static final String ADMIN_API_BASE_URL = "https://healthtips-admin.vercel.app/api";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final Context context;

    public AdminNotificationSender(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
    }

    /**
     * Send user report to admin
     */
    public void sendUserReport(
            @NonNull String reportType,
            @NonNull String reason,
            String description,
            String contentId,
            String contentType,
            final NotificationCallback callback
    ) {
        Log.d(TAG, "sendUserReport called - reportType: " + reportType);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Log.e(TAG, "User not authenticated");
            if (callback != null) {
                callback.onFailure(new Exception("User not authenticated"));
            }
            return;
        }

        Log.d(TAG, "Getting Firebase ID token for user: " + currentUser.getUid());
        // Get Firebase ID token for authentication
        currentUser.getIdToken(true).addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                Log.e(TAG, "Failed to get ID token", task.getException());
                if (callback != null) {
                    callback.onFailure(new Exception("Failed to get authentication token"));
                }
                return;
            }

            String idToken = task.getResult().getToken();
            Log.d(TAG, "Got Firebase ID token successfully");

            try {
                JSONObject json = new JSONObject();
                json.put("userId", currentUser.getUid());
                json.put("userName", currentUser.getDisplayName() != null ?
                        currentUser.getDisplayName() : currentUser.getEmail());
                json.put("reportType", reportType);
                json.put("reason", reason);

                if (description != null && !description.isEmpty()) {
                    json.put("description", description);
                }

                if (contentId != null && !contentId.isEmpty()) {
                    json.put("contentId", contentId);
                    json.put("contentType", contentType != null ? contentType : "other");
                }

                // Add device info
                JSONObject additionalData = new JSONObject();
                additionalData.put("device", Build.MANUFACTURER + " " + Build.MODEL);
                additionalData.put("osVersion", "Android " + Build.VERSION.RELEASE);
                additionalData.put("apiLevel", Build.VERSION.SDK_INT);
                json.put("additionalData", additionalData);

                RequestBody body = RequestBody.create(json.toString(), JSON);

                String url = ADMIN_API_BASE_URL + "/admin-notifications/user-report";
                Log.d(TAG, "Sending request to: " + url);
                Log.d(TAG, "Request body: " + json.toString());

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + idToken)
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e(TAG, "Failed to send admin notification", e);
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        try {
                            if (response.isSuccessful()) {
                                Log.d(TAG, "Admin notification sent successfully");
                                if (callback != null) {
                                    callback.onSuccess();
                                }
                            } else {
                                String errorBody = response.body() != null ? response.body().string() : "No error details";
                                Log.e(TAG, "Admin notification failed: " + response.code() + " - " + errorBody);
                                if (callback != null) {
                                    callback.onFailure(new Exception("Server error: " + response.code()));
                                }
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading response", e);
                            if (callback != null) {
                                callback.onFailure(e);
                            }
                        } finally {
                            response.close();
                        }
                    }
                });

            } catch (JSONException e) {
                Log.e(TAG, "JSON error", e);
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });
    }

    /**
     * Send content report (spam, inappropriate, etc.)
     */
    public void reportContent(
            @NonNull String contentId,
            @NonNull String contentType,
            @NonNull String reportType,
            @NonNull String reason,
            String description,
            final NotificationCallback callback
    ) {
        Log.d(TAG, "reportContent called - contentId: " + contentId + ", contentType: " + contentType + ", reportType: " + reportType);
        sendUserReport(reportType, reason, description, contentId, contentType, callback);
    }

    /**
     * Send bug report
     */
    public void reportBug(
            @NonNull String description,
            String steps,
            final NotificationCallback callback
    ) {
        String detailedDescription = description;
        if (steps != null && !steps.isEmpty()) {
            detailedDescription += "\n\nSteps to reproduce:\n" + steps;
        }

        sendUserReport("bug", "Bug report", detailedDescription, null, null, callback);
    }

    /**
     * Send feedback
     */
    public void sendFeedback(
            @NonNull String feedbackText,
            int rating,
            final NotificationCallback callback
    ) {
        String description = feedbackText;
        if (rating > 0) {
            description += "\n\nRating: " + rating + "/5";
        }

        sendUserReport("other", "User feedback", description, null, null, callback);
    }

    /**
     * Callback interface for notification sending
     */
    public interface NotificationCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    // ==================== NEW REPORT SYSTEM METHODS ====================

    /**
     * Gửi thông báo khi có report mới (static method)
     */
    public static void sendNewReportNotification(
            Context context,
            String reportId,
            String userId,
            String userName,
            String reportType,
            String content
    ) {
        Log.d(TAG, "sendNewReportNotification - reportId: " + reportId);
        
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated");
            return;
        }

        currentUser.getIdToken(true).addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                Log.e(TAG, "Failed to get ID token", task.getException());
                return;
            }

            String idToken = task.getResult().getToken();

            try {
                JSONObject json = new JSONObject();
                json.put("type", "NEW_REPORT");
                json.put("reportId", reportId);
                json.put("userId", userId);
                json.put("userName", userName);
                json.put("reportType", reportType);
                json.put("content", content.length() > 100 ? 
                        content.substring(0, 100) + "..." : content);

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(json.toString(), JSON);

                String url = ADMIN_API_BASE_URL + "/admin-notifications/new-report";
                Log.d(TAG, "Sending new report notification to: " + url);

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + idToken)
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e(TAG, "Failed to send new report notification", e);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "New report notification sent successfully");
                        } else {
                            Log.e(TAG, "New report notification failed: " + response.code());
                        }
                        response.close();
                    }
                });

            } catch (JSONException e) {
                Log.e(TAG, "JSON error", e);
            }
        });
    }

    /**
     * Gửi thông báo khi user reply trong report chat (static method)
     */
    public static void sendUserReplyNotification(
            Context context,
            String reportId,
            String userId,
            String userName,
            String message
    ) {
        Log.d(TAG, "sendUserReplyNotification - reportId: " + reportId);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated");
            return;
        }

        currentUser.getIdToken(true).addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                Log.e(TAG, "Failed to get ID token", task.getException());
                return;
            }

            String idToken = task.getResult().getToken();

            try {
                JSONObject json = new JSONObject();
                json.put("type", "USER_REPLY");
                json.put("reportId", reportId);
                json.put("userId", userId);
                json.put("userName", userName);
                json.put("message", message.length() > 100 ? 
                        message.substring(0, 100) + "..." : message);

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(json.toString(), JSON);

                String url = ADMIN_API_BASE_URL + "/admin-notifications/user-reply";
                Log.d(TAG, "Sending user reply notification to: " + url);

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + idToken)
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e(TAG, "Failed to send user reply notification", e);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "User reply notification sent successfully");
                        } else {
                            Log.e(TAG, "User reply notification failed: " + response.code());
                        }
                        response.close();
                    }
                });

            } catch (JSONException e) {
                Log.e(TAG, "JSON error", e);
            }
        });
    }
}
