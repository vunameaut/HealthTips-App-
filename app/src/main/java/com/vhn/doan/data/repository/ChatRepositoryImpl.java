package com.vhn.doan.data.repository;

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
    private static final String API_KEY = "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";
    private static final String CHAT_MESSAGES_PATH = "chat_messages";
    private static final String USER_TOPICS_PATH = "user_topics";

    private final DatabaseReference database;
    private final OkHttpClient httpClient;
    private final Gson gson;

    public ChatRepositoryImpl() {
        this.database = FirebaseDatabase.getInstance().getReference();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    @Override
    public void sendMessageToAI(String message, RepositoryCallback<String> callback) {
        try {
            // Tạo JSON request body
            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("model", "openai/gpt-3.5-turbo");

            JsonArray messagesArray = new JsonArray();

            // System message
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", "Bạn là trợ lý chuyên về sức khỏe. Chỉ trả lời các câu hỏi liên quan đến sức khỏe, y tế, dinh dưỡng, tập luyện thể dục. Nếu người dùng hỏi ngoài lĩnh vực sức khỏe thì từ chối trả lời một cách lịch sự và gợi ý họ hỏi về sức khỏe.");
            messagesArray.add(systemMessage);

            // User message
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", message);
            messagesArray.add(userMessage);

            requestJson.add("messages", messagesArray);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    requestJson.toString()
            );

            Request request = new Request.Builder()
                    .url(OPENROUTER_API_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "API call failed", e);
                    callback.onError("Không thể kết nối đến AI. Vui lòng kiểm tra kết nối mạng.");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String responseBody = response.body().string();
                        Log.d(TAG, "API Response: " + responseBody);

                        if (response.isSuccessful()) {
                            ChatApiResponse apiResponse = gson.fromJson(responseBody, ChatApiResponse.class);

                            if (apiResponse.getChoices() != null && !apiResponse.getChoices().isEmpty()) {
                                String aiMessage = apiResponse.getChoices().get(0).getMessage().getContent();
                                callback.onSuccess(aiMessage);
                            } else {
                                callback.onError("AI không thể trả lời câu hỏi này.");
                            }
                        } else {
                            Log.e(TAG, "API Error: " + responseBody);
                            callback.onError("Có lỗi xảy ra khi gọi AI. Mã lỗi: " + response.code());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing API response", e);
                        callback.onError("Có lỗi xảy ra khi xử lý phản hồi từ AI.");
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error creating API request", e);
            callback.onError("Có lỗi xảy ra khi tạo yêu cầu gửi đến AI.");
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
