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
import com.vhn.doan.data.Conversation;

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
 * Implementation của ChatRepository với hỗ trợ multiple conversations
 */
public class ChatRepositoryImpl implements ChatRepository {

    private static final String TAG = "ChatRepositoryImpl";
    private static final String OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions";
    // Sử dụng API key mới
    private static final String API_KEY = "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";

    // Firebase paths
    private static final String CONVERSATIONS_PATH = "conversations";
    private static final String CHAT_MESSAGES_PATH = "chat_messages";
    private static final String USER_TOPICS_PATH = "user_topics";

    // Pagination constants
    private static final int DEFAULT_CONVERSATIONS_LIMIT = 8;
    private static final int LOAD_MORE_CONVERSATIONS_LIMIT = 3;

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

    // ========== CONVERSATION MANAGEMENT ==========

    @Override
    public void createConversation(String userId, String firstMessage, RepositoryCallback<Conversation> callback) {
        try {
            long currentTime = System.currentTimeMillis();
            String title = Conversation.generateTitle(firstMessage);
            String topic = extractTopic(firstMessage);

            Conversation conversation = new Conversation(userId, title, currentTime);
            conversation.setTopic(topic);

            String conversationId = database.child(CONVERSATIONS_PATH)
                    .child(userId)
                    .push().getKey();

            if (conversationId != null) {
                conversation.setId(conversationId);

                database.child(CONVERSATIONS_PATH)
                        .child(userId)
                        .child(conversationId)
                        .setValue(conversation.toMap())
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Conversation created successfully: " + conversationId);
                            callback.onSuccess(conversation);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to create conversation", e);
                            callback.onError("Không thể tạo cuộc trò chuyện: " + e.getMessage());
                        });
            } else {
                callback.onError("Không thể tạo ID cho cuộc trò chuyện");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating conversation", e);
            callback.onError("Có lỗi xảy ra khi tạo cuộc trò chuyện: " + e.getMessage());
        }
    }

    @Override
    public void getConversations(String userId, int limit, Long lastConversationTime, RepositoryCallback<List<Conversation>> callback) {
        try {
            DatabaseReference conversationsRef = database.child(CONVERSATIONS_PATH).child(userId);

            // Sắp xếp theo lastMessageTime giảm dần (mới nhất trước)
            Query query = conversationsRef.orderByChild("lastMessageTime");

            // Nếu có lastConversationTime, query từ thời điểm đó trở về trước
            if (lastConversationTime != null) {
                query = query.endBefore(lastConversationTime);
            }

            // Giới hạn số lượng kết quả
            query = query.limitToLast(limit);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Conversation> conversations = new ArrayList<>();

                    for (DataSnapshot conversationSnapshot : dataSnapshot.getChildren()) {
                        try {
                            Conversation conversation = parseConversationFromSnapshot(conversationSnapshot);
                            if (conversation != null) {
                                conversations.add(conversation);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing conversation", e);
                        }
                    }

                    // Sắp xếp lại theo thời gian giảm dần (mới nhất trước)
                    Collections.sort(conversations, (c1, c2) ->
                        Long.compare(c2.getLastMessageTime(), c1.getLastMessageTime()));

                    Log.d(TAG, "Loaded " + conversations.size() + " conversations");
                    callback.onSuccess(conversations);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Failed to load conversations", databaseError.toException());
                    callback.onError("Không thể tải danh sách cuộc trò chuyện: " + databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error loading conversations", e);
            callback.onError("Có lỗi xảy ra khi tải danh sách cuộc trò chuyện: " + e.getMessage());
        }
    }

    @Override
    public void updateConversation(Conversation conversation, RepositoryCallback<Conversation> callback) {
        try {
            if (conversation.getId() == null || conversation.getUserId() == null) {
                callback.onError("Thông tin cuộc trò chuyện không hợp lệ");
                return;
            }

            database.child(CONVERSATIONS_PATH)
                    .child(conversation.getUserId())
                    .child(conversation.getId())
                    .setValue(conversation.toMap())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Conversation updated successfully: " + conversation.getId());
                        callback.onSuccess(conversation);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update conversation", e);
                        callback.onError("Không thể cập nhật cuộc trò chuyện: " + e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error updating conversation", e);
            callback.onError("Có lỗi xảy ra khi cập nhật cuộc trò chuyện: " + e.getMessage());
        }
    }

    @Override
    public void deleteConversation(String conversationId, RepositoryCallback<Boolean> callback) {
        // TODO: Implement delete conversation and all its messages
        callback.onError("Chức năng xóa cuộc trò chuyện sẽ được triển khai sau");
    }

    @Override
    public void hasMoreConversations(String userId, Long lastConversationTime, RepositoryCallback<Boolean> callback) {
        try {
            DatabaseReference conversationsRef = database.child(CONVERSATIONS_PATH).child(userId);

            Query query = conversationsRef.orderByChild("lastMessageTime");
            if (lastConversationTime != null) {
                query = query.endBefore(lastConversationTime);
            }
            query = query.limitToLast(1);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean hasMore = dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0;
                    callback.onSuccess(hasMore);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Failed to check more conversations", databaseError.toException());
                    callback.onError("Không thể kiểm tra cuộc trò chuyện: " + databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error checking more conversations", e);
            callback.onError("Có lỗi xảy ra khi kiểm tra cuộc trò chuyện: " + e.getMessage());
        }
    }

    // ========== MESSAGE MANAGEMENT ==========

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

    // ========== HELPER METHODS ==========

    /**
     * Parse Conversation từ Firebase DataSnapshot
     */
    private Conversation parseConversationFromSnapshot(DataSnapshot snapshot) {
        try {
            Conversation conversation = new Conversation();
            conversation.setId(snapshot.getKey());
            conversation.setUserId(snapshot.child("userId").getValue(String.class));
            conversation.setTitle(snapshot.child("title").getValue(String.class));
            conversation.setLastMessage(snapshot.child("lastMessage").getValue(String.class));

            Long lastMessageTime = snapshot.child("lastMessageTime").getValue(Long.class);
            if (lastMessageTime != null) {
                conversation.setLastMessageTime(lastMessageTime);
            }

            Boolean isFromUser = snapshot.child("isFromUser").getValue(Boolean.class);
            if (isFromUser != null) {
                conversation.setFromUser(isFromUser);
            }

            Integer messageCount = snapshot.child("messageCount").getValue(Integer.class);
            if (messageCount != null) {
                conversation.setMessageCount(messageCount);
            }

            Long createdTime = snapshot.child("createdTime").getValue(Long.class);
            if (createdTime != null) {
                conversation.setCreatedTime(createdTime);
            }

            conversation.setTopic(snapshot.child("topic").getValue(String.class));
            return conversation;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing conversation from snapshot", e);
            return null;
        }
    }

    /**
     * Parse ChatMessage từ Firebase DataSnapshot
     */
    private ChatMessage parseChatMessageFromSnapshot(DataSnapshot snapshot) {
        try {
            ChatMessage message = new ChatMessage();
            message.setId(snapshot.getKey());
            message.setConversationId(snapshot.child("conversationId").getValue(String.class));
            message.setUserId(snapshot.child("userId").getValue(String.class));
            message.setContent(snapshot.child("content").getValue(String.class));

            Boolean isFromUser = snapshot.child("isFromUser").getValue(Boolean.class);
            if (isFromUser != null) {
                message.setFromUser(isFromUser);
            }

            Long timestamp = snapshot.child("timestamp").getValue(Long.class);
            if (timestamp != null) {
                message.setTimestamp(timestamp);
            }

            message.setTopic(snapshot.child("topic").getValue(String.class));
            return message;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing message from snapshot", e);
            return null;
        }
    }
}
