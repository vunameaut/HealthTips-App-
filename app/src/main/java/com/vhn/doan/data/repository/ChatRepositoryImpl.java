package com.vhn.doan.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.vhn.doan.data.ChatMessage;
import com.vhn.doan.data.ChatApiResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Implementation của ChatRepository
 */
public class ChatRepositoryImpl implements ChatRepository {

    private static final String TAG = "ChatRepositoryImpl";
    private static final String OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions";
    // Thử với API key mới hoặc kiểm tra lại format
    private static final String API_KEY = "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";
    private static final String CHAT_MESSAGES_PATH = "chat_messages";
    private static final String USER_TOPICS_PATH = "user_topics";

    private final DatabaseReference database;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final Handler mainHandler;

    public ChatRepositoryImpl() {
        this.database = FirebaseDatabase.getInstance().getReference();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void sendMessageToAI(String message, RepositoryCallback<String> callback) {
        try {
            // Tạo JSON request body theo format chính xác của OpenRouter
            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("model", "openai/gpt-3.5-turbo");

            // Thêm các parameters bổ sung theo yêu cầu OpenRouter
            requestJson.addProperty("max_tokens", 1000);
            requestJson.addProperty("temperature", 0.7);

            JsonArray messagesArray = new JsonArray();

            // System message
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", "Bạn là trợ lý AI chuyên về sức khỏe. Chỉ trả lời các câu hỏi liên quan đến sức khỏe, y tế, dinh dưỡng, thể dục thể thao. Nếu người dùng hỏi ngoài lĩnh vực sức khỏe thì từ chối một cách lịch sự và đề nghị họ hỏi về sức khỏe.");
            messagesArray.add(systemMessage);

            // User message
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", message);
            messagesArray.add(userMessage);

            requestJson.add("messages", messagesArray);

            String jsonString = gson.toJson(requestJson);
            Log.d(TAG, "Request JSON: " + jsonString);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    jsonString
            );

            Request request = new Request.Builder()
                    .url(OPENROUTER_API_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("HTTP-Referer", "https://healthtips-vn.app")
                    .addHeader("X-Title", "HealthTips Vietnam")
                    .build();

            Log.d(TAG, "Sending request to: " + OPENROUTER_API_URL);
            Log.d(TAG, "Authorization header: Bearer " + API_KEY.substring(0, 20) + "...");

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Network request failed", e);
                    mainHandler.post(() -> {
                        callback.onError("Không thể kết nối đến máy chủ AI. Kiểm tra kết nối mạng của bạn.");
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = "";
                    try {
                        if (response.body() != null) {
                            responseBody = response.body().string();
                        }

                        Log.d(TAG, "Response Code: " + response.code());
                        Log.d(TAG, "Response Headers: " + response.headers().toString());
                        Log.d(TAG, "Response Body: " + responseBody);

                        if (response.isSuccessful()) {
                            ChatApiResponse apiResponse = gson.fromJson(responseBody, ChatApiResponse.class);

                            if (apiResponse != null &&
                                apiResponse.getChoices() != null &&
                                !apiResponse.getChoices().isEmpty() &&
                                apiResponse.getChoices().get(0).getMessage() != null) {

                                String aiMessage = apiResponse.getChoices().get(0).getMessage().getContent();
                                if (aiMessage != null && !aiMessage.trim().isEmpty()) {
                                    mainHandler.post(() -> callback.onSuccess(aiMessage.trim()));
                                } else {
                                    mainHandler.post(() -> callback.onError("AI trả về phản hồi trống."));
                                }
                            } else {
                                mainHandler.post(() -> callback.onError("Định dạng phản hồi từ AI không hợp lệ."));
                            }
                        } else {
                            // Xử lý các lỗi HTTP cụ thể
                            String errorMessage = parseErrorMessage(response.code(), responseBody);
                            Log.e(TAG, "API Error: " + errorMessage);

                            mainHandler.post(() -> callback.onError(errorMessage));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response", e);
                        Log.e(TAG, "Response body was: " + responseBody);

                        mainHandler.post(() -> {
                            callback.onError("Lỗi xử lý phản hồi từ AI. Vui lòng thử lại.");
                        });
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error creating request", e);
            callback.onError("Lỗi tạo yêu cầu gửi đến AI.");
        }
    }

    /**
     * Phân tích thông báo lỗi từ API response
     */
    private String parseErrorMessage(int responseCode, String responseBody) {
        try {
            // Thử parse JSON error response
            JsonObject errorResponse = gson.fromJson(responseBody, JsonObject.class);
            if (errorResponse.has("error")) {
                JsonObject error = errorResponse.getAsJsonObject("error");
                if (error.has("message")) {
                    String apiErrorMessage = error.get("message").getAsString();
                    Log.d(TAG, "API Error Message: " + apiErrorMessage);

                    // Dịch một số lỗi phổ biến
                    if (apiErrorMessage.contains("No auth credentials")) {
                        return "Lỗi xác thực API. Vui lòng liên hệ quản trị viên.";
                    } else if (apiErrorMessage.contains("Rate limit")) {
                        return "Đã vượt quá giới hạn yêu cầu. Vui lòng thử lại sau ít phút.";
                    } else if (apiErrorMessage.contains("Invalid model")) {
                        return "Mô hình AI không hợp lệ. Vui lòng thử lại.";
                    }
                    return "Lỗi từ AI: " + apiErrorMessage;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error response", e);
        }

        // Fallback cho các HTTP status codes
        switch (responseCode) {
            case 401:
                return "Lỗi xác thực. API key có thể đã hết hạn hoặc không hợp lệ.";
            case 403:
                return "Không có quyền truy cập. Vui lòng kiểm tra cấu hình API.";
            case 429:
                return "Quá nhiều yêu cầu. Vui lòng chờ một chút rồi thử lại.";
            case 500:
                return "Lỗi máy chủ AI. Vui lòng thử lại sau.";
            case 502:
            case 503:
            case 504:
                return "Máy chủ AI tạm thời không khả dụng. Vui lòng thử lại sau.";
            default:
                return "Lỗi không xác định từ AI (Mã: " + responseCode + "). Vui lòng thử lại.";
        }
    }

    @Override
    public void saveChatMessage(ChatMessage chatMessage, RepositoryCallback<ChatMessage> callback) {
        try {
            String messageId = database.child(CHAT_MESSAGES_PATH)
                    .child(chatMessage.getUserId())
                    .push().getKey();

            if (messageId != null) {
                chatMessage.setId(messageId);

                database.child(CHAT_MESSAGES_PATH)
                        .child(chatMessage.getUserId())
                        .child(messageId)
                        .setValue(chatMessage.toMap())
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Message saved successfully");

                            // Lưu chủ đề nếu có
                            if (chatMessage.getTopic() != null && !chatMessage.getTopic().isEmpty()) {
                                saveUserTopic(chatMessage.getUserId(), chatMessage.getTopic());
                            }

                            callback.onSuccess(chatMessage);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to save message", e);
                            callback.onError("Không thể lưu tin nhắn: " + e.getMessage());
                        });
            } else {
                callback.onError("Không thể tạo ID cho tin nhắn");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving chat message", e);
            callback.onError("Có lỗi xảy ra khi lưu tin nhắn: " + e.getMessage());
        }
    }

    @Override
    public void getChatMessages(String userId, RepositoryCallback<List<ChatMessage>> callback) {
        try {
            database.child(CHAT_MESSAGES_PATH)
                    .child(userId)
                    .orderByChild("timestamp")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            List<ChatMessage> messages = new ArrayList<>();

                            for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                                try {
                                    ChatMessage message = new ChatMessage();
                                    message.setId(messageSnapshot.getKey());
                                    message.setUserId(messageSnapshot.child("userId").getValue(String.class));
                                    message.setContent(messageSnapshot.child("content").getValue(String.class));
                                    message.setFromUser(messageSnapshot.child("isFromUser").getValue(Boolean.class));
                                    message.setTimestamp(messageSnapshot.child("timestamp").getValue(Long.class));
                                    message.setTopic(messageSnapshot.child("topic").getValue(String.class));

                                    messages.add(message);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing message", e);
                                }
                            }

                            // Sắp xếp theo thời gian
                            Collections.sort(messages, (m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));

                            Log.d(TAG, "Loaded " + messages.size() + " messages");
                            callback.onSuccess(messages);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "Failed to load messages", databaseError.toException());
                            callback.onError("Không thể tải tin nhắn: " + databaseError.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error loading chat messages", e);
            callback.onError("Có lỗi xảy ra khi tải tin nhắn: " + e.getMessage());
        }
    }

    @Override
    public void clearChatHistory(String userId, RepositoryCallback<Boolean> callback) {
        try {
            database.child(CHAT_MESSAGES_PATH)
                    .child(userId)
                    .removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Chat history cleared successfully");
                        callback.onSuccess(true);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to clear chat history", e);
                        callback.onError("Không thể xóa lịch sử chat: " + e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error clearing chat history", e);
            callback.onError("Có lỗi xảy ra khi xóa lịch sử chat: " + e.getMessage());
        }
    }

    @Override
    public String extractTopic(String content) {
        try {
            // Các từ khóa chủ đề về sức khỏe
            String[] healthTopics = {
                "tim mạch", "huyết áp", "cholesterol", "đường huyết", "tiểu đường",
                "dinh dưỡng", "vitamin", "protein", "carb", "chất béo",
                "tập luyện", "thể dục", "yoga", "cardio", "cơ bắp",
                "giảm cân", "tăng cân", "béo phì", "ăn kiêng",
                "stress", "lo âu", "trầm cảm", "tâm lý", "tinh thần",
                "giấc ngủ", "mất ngủ", "ngủ", "nghỉ ngơi",
                "da", "tóc", "móng", "mỹ phẩm", "chăm sóc da",
                "mang thai", "sinh sản", "kinh nguyệt", "phụ khoa",
                "trẻ em", "em bé", "sức khỏe trẻ", "phát triển",
                "người cao tuổi", "lão hóa", "xương khớp", "cột sống",
                "mắt", "thị lực", "tai", "thính giác",
                "răng", "miệng", "nha khoa", "vệ sinh răng miệng",
                "cảm cúm", "sốt", "ho", "viêm họng", "virus",
                "thuốc", "dược phẩm", "tác dụng phụ", "liều dùng"
            };

            content = content.toLowerCase();

            for (String topic : healthTopics) {
                if (content.contains(topic)) {
                    return topic;
                }
            }

            // Nếu không tìm thấy chủ đề cụ thể, trả về chủ đề chung
            return "sức khỏe tổng quát";

        } catch (Exception e) {
            Log.e(TAG, "Error extracting topic", e);
            return "sức khỏe tổng quát";
        }
    }

    /**
     * Lưu chủ đề mà người dùng quan tâm vào Firebase
     */
    private void saveUserTopic(String userId, String topic) {
        try {
            database.child(USER_TOPICS_PATH)
                    .child(userId)
                    .child(topic)
                    .setValue(System.currentTimeMillis())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User topic saved: " + topic))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to save user topic", e));
        } catch (Exception e) {
            Log.e(TAG, "Error saving user topic", e);
        }
    }
}
