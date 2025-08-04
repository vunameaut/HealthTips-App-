package com.vhn.doan.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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
    // Thay đổi sang RapidAPI ChatGPT-42
    private static final String RAPIDAPI_URL = "https://chatgpt-42.p.rapidapi.com/chatgpt";
    private static final String RAPIDAPI_KEY = "062f6aca45mshe36de3f71654aecp1590e4jsn0d0d26277bde";
    private static final String RAPIDAPI_HOST = "chatgpt-42.p.rapidapi.com";

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
        try {
            if (conversationId == null || conversationId.isEmpty()) {
                callback.onError("ID cuộc trò chuyện không hợp lệ");
                return;
            }

            // Đầu tiên, xóa tất cả tin nhắn trong cuộc trò chuyện
            clearChatMessages(conversationId, new RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    // Sau khi xóa tin nhắn thành công, tìm và xóa cuộc trò chuyện
                    deleteConversationFromAllUsers(conversationId, callback);
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Failed to clear messages before deleting conversation: " + error);
                    callback.onError("Không thể xóa tin nhắn: " + error);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error deleting conversation", e);
            callback.onError("Có lỗi xảy ra khi xóa cuộc trò chuyện: " + e.getMessage());
        }
    }

    /**
     * Tìm và xóa cuộc trò chuyện từ tất cả người dùng
     */
    private void deleteConversationFromAllUsers(String conversationId, RepositoryCallback<Boolean> callback) {
        // Query tất cả users để tìm cuộc trò chuyện
        DatabaseReference conversationsRef = database.child(CONVERSATIONS_PATH);

        conversationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean conversationFound = false;

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();

                    if (userSnapshot.hasChild(conversationId)) {
                        conversationFound = true;

                        // Xóa cuộc trò chuyện từ user này
                        database.child(CONVERSATIONS_PATH)
                                .child(userId)
                                .child(conversationId)
                                .removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Conversation deleted successfully: " + conversationId);
                                    callback.onSuccess(true);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to delete conversation: " + conversationId, e);
                                    callback.onError("Không thể xóa cuộc trò chuyện: " + e.getMessage());
                                });
                        break;
                    }
                }

                if (!conversationFound) {
                    callback.onError("Không tìm thấy cuộc trò chuyện để xóa");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error finding conversation to delete", databaseError.toException());
                callback.onError("Lỗi tìm kiếm cuộc trò chuyện: " + databaseError.getMessage());
            }
        });
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
            // Tạo JSON request body theo format của RapidAPI ChatGPT-42
            JsonObject requestJson = new JsonObject();

            JsonArray messagesArray = new JsonArray();

            // System message để định hướng AI về sức khỏe
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
            requestJson.addProperty("web_access", false);

            String jsonString = gson.toJson(requestJson);
            Log.d(TAG, "RapidAPI Request JSON: " + jsonString);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    jsonString
            );

            Request request = new Request.Builder()
                    .url(RAPIDAPI_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("x-rapidapi-host", RAPIDAPI_HOST)
                    .addHeader("x-rapidapi-key", RAPIDAPI_KEY)
                    .build();

            Log.d(TAG, "Sending request to RapidAPI: " + RAPIDAPI_URL);
            Log.d(TAG, "RapidAPI key: " + RAPIDAPI_KEY.substring(0, 20) + "...");

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

                        Log.d(TAG, "RapidAPI Response Code: " + response.code());
                        Log.d(TAG, "RapidAPI Response Headers: " + response.headers().toString());
                        Log.d(TAG, "RapidAPI Response Body: " + responseBody);

                        if (response.isSuccessful()) {
                            // Parse response theo format của RapidAPI ChatGPT-42
                            JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);

                            if (responseJson != null && responseJson.has("result")) {
                                String aiMessage = responseJson.get("result").getAsString();
                                if (aiMessage != null && !aiMessage.trim().isEmpty()) {
                                    mainHandler.post(() -> callback.onSuccess(aiMessage.trim()));
                                } else {
                                    mainHandler.post(() -> callback.onError("AI trả về phản hồi trống."));
                                }
                            } else {
                                // Fallback: thử parse theo format OpenAI cũ nếu có
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
                            }
                        } else {
                            // Xử lý các lỗi HTTP cụ thể cho RapidAPI
                            String errorMessage = parseRapidAPIErrorMessage(response.code(), responseBody);
                            Log.e(TAG, "RapidAPI Error: " + errorMessage);

                            mainHandler.post(() -> callback.onError(errorMessage));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing RapidAPI response", e);
                        Log.e(TAG, "Response body was: " + responseBody);

                        mainHandler.post(() -> {
                            callback.onError("Lỗi xử lý phản hồi từ AI. Vui lòng thử lại.");
                        });
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error creating RapidAPI request", e);
            callback.onError("Lỗi tạo yêu cầu gửi đến AI.");
        }
    }

    /**
     * Phân tích thông báo lỗi từ RapidAPI response
     */
    private String parseRapidAPIErrorMessage(int responseCode, String responseBody) {
        try {
            // Thử parse JSON error response từ RapidAPI
            JsonObject errorResponse = gson.fromJson(responseBody, JsonObject.class);
            if (errorResponse.has("error")) {
                String apiErrorMessage = errorResponse.get("error").getAsString();
                Log.d(TAG, "RapidAPI Error Message: " + apiErrorMessage);

                // Dịch một số lỗi phổ biến của RapidAPI
                if (apiErrorMessage.contains("invalid key")) {
                    return "Lỗi xác thực RapidAPI. API key không hợp lệ.";
                } else if (apiErrorMessage.contains("quota exceeded")) {
                    return "Đã vượt quá hạn mức sử dụng API. Vui lòng thử lại sau.";
                } else if (apiErrorMessage.contains("rate limit")) {
                    return "Đã vượt quá giới hạn yêu cầu. Vui lòng thử lại sau ít phút.";
                }
                return "Lỗi từ RapidAPI: " + apiErrorMessage;
            } else if (errorResponse.has("message")) {
                String message = errorResponse.get("message").getAsString();
                return "Lỗi từ AI: " + message;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing RapidAPI error response", e);
        }

        // Fallback cho các HTTP status codes của RapidAPI
        switch (responseCode) {
            case 401:
                return "Lỗi xác thực RapidAPI. API key có thể đã hết hạn hoặc không hợp lệ.";
            case 403:
                return "Không có quyền truy cập RapidAPI. Vui lòng kiểm tra subscription.";
            case 429:
                return "Quá nhiều yêu cầu tới RapidAPI. Vui lòng chờ một chút rồi thử lại.";
            case 500:
                return "Lỗi máy chủ RapidAPI. Vui lòng thử lại sau.";
            case 502:
            case 503:
            case 504:
                return "RapidAPI tạm thời không khả dụng. Vui lòng thử lại sau.";
            default:
                return "Lỗi không xác định từ RapidAPI (Mã: " + responseCode + "). Vui lòng thử lại.";
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

    // ========== HELPER METHODS ==========

    @Override
    public void saveChatMessage(ChatMessage chatMessage, RepositoryCallback<ChatMessage> callback) {
        try {
            if (chatMessage.getConversationId() == null || chatMessage.getUserId() == null || chatMessage.getContent() == null) {
                callback.onError("Thông tin tin nhắn không hợp lệ");
                return;
            }

            String messageId = database.child(CHAT_MESSAGES_PATH)
                    .child(chatMessage.getConversationId())
                    .push().getKey();

            if (messageId != null) {
                chatMessage.setId(messageId);

                database.child(CHAT_MESSAGES_PATH)
                        .child(chatMessage.getConversationId())
                        .child(messageId)
                        .setValue(chatMessage.toMap())
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Chat message saved successfully: " + messageId);
                            callback.onSuccess(chatMessage);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to save chat message", e);
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
    public void getChatMessages(String conversationId, RepositoryCallback<List<ChatMessage>> callback) {
        try {
            DatabaseReference messagesRef = database.child(CHAT_MESSAGES_PATH).child(conversationId);

            messagesRef.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<ChatMessage> messages = new ArrayList<>();

                    for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                        try {
                            ChatMessage message = parseChatMessageFromSnapshot(messageSnapshot);
                            if (message != null) {
                                messages.add(message);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing chat message", e);
                        }
                    }

                    Log.d(TAG, "Loaded " + messages.size() + " chat messages");
                    callback.onSuccess(messages);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Failed to load chat messages", databaseError.toException());
                    callback.onError("Không thể tải tin nhắn: " + databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error loading chat messages", e);
            callback.onError("Có lỗi xảy ra khi tải tin nhắn: " + e.getMessage());
        }
    }

    @Override
    public void clearChatMessages(String conversationId, RepositoryCallback<Boolean> callback) {
        try {
            database.child(CHAT_MESSAGES_PATH)
                    .child(conversationId)
                    .removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Chat messages cleared successfully for conversation: " + conversationId);
                        callback.onSuccess(true);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to clear chat messages", e);
                        callback.onError("Không thể xóa tin nhắn: " + e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error clearing chat messages", e);
            callback.onError("Có lỗi xảy ra khi xóa tin nhắn: " + e.getMessage());
        }
    }

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

            // Parse các thuộc tính mới cho menu ngữ cảnh
            Boolean isPinned = snapshot.child("isPinned").getValue(Boolean.class);
            if (isPinned != null) {
                conversation.setPinned(isPinned);
            }

            Boolean isMuted = snapshot.child("isMuted").getValue(Boolean.class);
            if (isMuted != null) {
                conversation.setMuted(isMuted);
            }

            Boolean isRead = snapshot.child("isRead").getValue(Boolean.class);
            if (isRead != null) {
                conversation.setRead(isRead);
            }

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

    @Override
    public void clearChatHistory(String userId, RepositoryCallback<Boolean> callback) {
        try {
            // Xóa tất cả cuộc trò chuyện của user
            DatabaseReference userConversationsRef = database.child(CONVERSATIONS_PATH).child(userId);

            userConversationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        List<String> conversationIds = new ArrayList<>();

                        // Lấy danh sách ID các cuộc trò chuyện
                        for (DataSnapshot conversationSnapshot : dataSnapshot.getChildren()) {
                            conversationIds.add(conversationSnapshot.getKey());
                        }

                        if (conversationIds.isEmpty()) {
                            callback.onSuccess(true);
                            return;
                        }

                        // Xóa từng cuộc trò chuyện và tin nhắn của nó
                        deleteConversationsRecursively(conversationIds, 0, userId, callback);

                    } catch (Exception e) {
                        Log.e(TAG, "Error processing chat history deletion", e);
                        callback.onError("Lỗi xử lý xóa lịch sử chat: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Error clearing chat history", databaseError.toException());
                    callback.onError("Lỗi xóa lịch sử chat: " + databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initiating chat history clear", e);
            callback.onError("Lỗi khởi tạo xóa lịch sử chat: " + e.getMessage());
        }
    }

    /**
     * Xóa các cuộc trò chuyện một cách đệ quy
     */
    private void deleteConversationsRecursively(List<String> conversationIds, int index, String userId, RepositoryCallback<Boolean> callback) {
        if (index >= conversationIds.size()) {
            // Xóa xong tất cả cuộc trò chuyện, xóa thư mục user trong conversations
            database.child(CONVERSATIONS_PATH).child(userId).removeValue()
                    .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error removing user conversations folder", e);
                        callback.onError("Lỗi xóa thư mục cuộc trò chuyện: " + e.getMessage());
                    });
            return;
        }

        String conversationId = conversationIds.get(index);

        // Xóa tin nhắn của cuộc trò chuyện này trước
        clearChatMessages(conversationId, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                // Sau khi xóa tin nhắn, xóa cuộc trò chuyện
                database.child(CONVERSATIONS_PATH).child(userId).child(conversationId).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            // Tiếp tục xóa cuộc trò chuyện tiếp theo
                            deleteConversationsRecursively(conversationIds, index + 1, userId, callback);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error deleting conversation: " + conversationId, e);
                            callback.onError("Lỗi xóa cuộc trò chuyện: " + e.getMessage());
                        });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error clearing messages for conversation: " + conversationId + ", error: " + error);
                callback.onError("Lỗi xóa tin nhắn cuộc trò chuyện: " + error);
            }
        });
    }
}
