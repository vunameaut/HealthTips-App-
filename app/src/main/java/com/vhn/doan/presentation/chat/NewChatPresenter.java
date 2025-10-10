package com.vhn.doan.presentation.chat;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vhn.doan.data.ChatMessage;
import com.vhn.doan.data.Conversation;
import com.vhn.doan.data.repository.ChatRepository;
import com.vhn.doan.data.repository.RepositoryCallback;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Presenter cho NewChat feature - Tạo cuộc trò chuyện mới
 */
public class NewChatPresenter implements NewChatContract.Presenter {

    private static final String TAG = "NewChatPresenter";

    private final ChatRepository chatRepository;
    private final FirebaseAuth firebaseAuth;
    private NewChatContract.View view;

    public NewChatPresenter(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void attachView(NewChatContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    private boolean isViewAttached() {
        return view != null;
    }

    @Override
    public void createConversationAndSendMessage(String firstMessage) {
        if (firstMessage == null || firstMessage.trim().isEmpty()) {
            if (isViewAttached()) {
                view.showSendMessageError("Vui lòng nhập nội dung tin nhắn");
            }
            return;
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (isViewAttached()) {
                view.showCreateConversationError("Bạn cần đăng nhập để tạo cuộc trò chuyện");
            }
            return;
        }

        String trimmedMessage = firstMessage.trim();
        String userId = currentUser.getUid();

        if (isViewAttached()) {
            view.showCreatingConversation();
            view.clearMessageInput();
        }

        // Bước 1: Tạo cuộc trò chuyện mới
        chatRepository.createConversation(userId, trimmedMessage, new RepositoryCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                Log.d(TAG, "Conversation created successfully: " + conversation.getId());

                if (isViewAttached()) {
                    view.hideCreatingConversation();
                    view.showSendingMessage();
                }

                // Bước 2: Lưu tin nhắn đầu tiên của người dùng
                long timestamp = System.currentTimeMillis();
                ChatMessage userMessage = new ChatMessage(conversation.getId(), userId, trimmedMessage, true, timestamp);
                String topic = chatRepository.extractTopic(trimmedMessage);
                userMessage.setTopic(topic);

                chatRepository.saveChatMessage(userMessage, new RepositoryCallback<ChatMessage>() {
                    @Override
                    public void onSuccess(ChatMessage savedUserMessage) {
                        Log.d(TAG, "User message saved successfully");

                        if (isViewAttached()) {
                            view.hideSendingMessage();
                            view.showAiTyping();
                        }

                        // Bước 3: Gửi tin nhắn tới AI
                        chatRepository.sendMessageToAI(trimmedMessage, new RepositoryCallback<String>() {
                            @Override
                            public void onSuccess(String aiResponse) {
                                Log.d(TAG, "AI response received: " + aiResponse);

                                // Bước 4: Lưu phản hồi AI
                                ChatMessage aiMessage = new ChatMessage(conversation.getId(), userId, aiResponse, false, System.currentTimeMillis());
                                aiMessage.setTopic(topic);

                                chatRepository.saveChatMessage(aiMessage, new RepositoryCallback<ChatMessage>() {
                                    @Override
                                    public void onSuccess(ChatMessage savedAiMessage) {
                                        Log.d(TAG, "AI message saved successfully");

                                        // Bước 5: Cập nhật thông tin cuộc trò chuyện
                                        conversation.setLastMessage(aiResponse);
                                        conversation.setLastMessageTime(savedAiMessage.getTimestamp());
                                        conversation.setFromUser(false);
                                        conversation.setMessageCount(2); // User message + AI message

                                        chatRepository.updateConversation(conversation, new RepositoryCallback<Conversation>() {
                                            @Override
                                            public void onSuccess(Conversation updatedConversation) {
                                                Log.d(TAG, "Conversation updated successfully");

                                                if (isViewAttached()) {
                                                    view.hideAiTyping();
                                                    view.showMessage("Cuộc trò chuyện đã được tạo thành công!");

                                                    // Chuyển đến màn hình chat detail
                                                    view.navigateToChatDetail(updatedConversation.getId(), updatedConversation.getTitle());
                                                }
                                            }

                                            @Override
                                            public void onError(String error) {
                                                Log.e(TAG, "Failed to update conversation: " + error);

                                                if (isViewAttached()) {
                                                    view.hideAiTyping();
                                                    // Vẫn chuyển đến chat detail dù cập nhật conversation lỗi
                                                    view.navigateToChatDetail(conversation.getId(), conversation.getTitle());
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(String error) {
                                        Log.e(TAG, "Failed to save AI message: " + error);

                                        if (isViewAttached()) {
                                            view.hideAiTyping();
                                            view.showSendMessageError("Không thể lưu phản hồi AI: " + error);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "Failed to get AI response: " + error);

                                if (isViewAttached()) {
                                    view.hideAiTyping();
                                    view.showSendMessageError("Không thể nhận phản hồi từ AI: " + error);
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Failed to save user message: " + error);

                        if (isViewAttached()) {
                            view.hideSendingMessage();
                            view.showSendMessageError("Không thể lưu tin nhắn: " + error);
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to create conversation: " + error);

                if (isViewAttached()) {
                    view.hideCreatingConversation();
                    view.showCreateConversationError("Không thể tạo cuộc trò chuyện: " + error);
                }
            }
        });
    }

    @Override
    public void loadSuggestedQuestions() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (isViewAttached()) {
                view.hideLoadingSuggestedQuestions();
                // Sử dụng câu hỏi mặc định nếu người dùng chưa đăng nhập
                setDefaultSuggestedQuestions();
            }
            return;
        }

        String userId = currentUser.getUid();
        if (isViewAttached()) {
            view.showLoadingSuggestedQuestions();
        }

        // Lấy dữ liệu từ userPreferences trong Firebase
        chatRepository.getUserPreferences(userId, new RepositoryCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> keywords) {
                if (isViewAttached()) {
                    view.hideLoadingSuggestedQuestions();

                    if (keywords != null && !keywords.isEmpty()) {
                        // Tạo câu hỏi gợi ý dựa trên từ khóa người dùng
                        List<String> suggestedQuestions = generateQuestionsFromKeywords(keywords);
                        view.updateSuggestedQuestions(suggestedQuestions);
                    } else {
                        // Sử dụng câu hỏi mặc định nếu không có từ khóa
                        setDefaultSuggestedQuestions();
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load user preferences: " + error);
                if (isViewAttached()) {
                    view.hideLoadingSuggestedQuestions();
                    setDefaultSuggestedQuestions();
                }
            }
        });
    }

    /**
     * Tạo câu hỏi gợi ý dựa trên từ khóa người dùng
     */
    private List<String> generateQuestionsFromKeywords(List<String> keywords) {
        List<String> allPossibleQuestions = new java.util.ArrayList<>();
        java.util.Random random = new java.util.Random();

        // Lấy ngày trong tuần và giờ trong ngày để đa dạng hóa câu hỏi
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        long dateInMillis = calendar.getTimeInMillis();

        // Seed random với ngày hiện tại để đảm bảo câu hỏi khác nhau mỗi ngày nhưng nhất quán trong cùng một ngày
        random.setSeed(dateInMillis / (24 * 60 * 60 * 1000));

        // Lưu trữ lịch sử câu hỏi đã hiển thị để tránh lặp lại
        SharedPreferences prefs = view.getSharedPreferences();
        Gson gson = new Gson();
        Set<String> recentQuestions = new HashSet<>();
        String recentQuestionsJson = prefs.getString("recent_questions", "");
        if (!recentQuestionsJson.isEmpty()) {
            Type type = new TypeToken<Set<String>>(){}.getType();
            recentQuestions = gson.fromJson(recentQuestionsJson, type);
        }

        // Xáo trộn từ khóa để đa dạng kết quả
        java.util.Collections.shuffle(keywords, random);

        // Map lưu trữ ngân hàng câu hỏi phong phú theo từng chủ đề
        Map<String, List<String>> questionBank = new HashMap<>();

        // DINH DƯỠNG - Ngân hàng câu hỏi phong phú hơn
        List<String> nutritionQuestions = new java.util.ArrayList<>();
        nutritionQuestions.add("Những thực phẩm nào tốt cho sức khỏe tim mạch?");
        nutritionQuestions.add("Chế độ dinh dưỡng nào tốt nhất cho người tập thể thao?");
        nutritionQuestions.add("Làm thế nào để cân đối dinh dưỡng trong bữa ăn hàng ngày?");
        nutritionQuestions.add("Các loại rau nào có nhiều chất dinh dưỡng nhất?");
        nutritionQuestions.add("Làm thế nào để bổ sung protein đầy đủ cho người ăn chay?");
        nutritionQuestions.add("Những loại thực phẩm nào giàu chất chống oxy hóa?");
        nutritionQuestions.add("Chất béo lành mạnh có trong những thực phẩm nào?");
        nutritionQuestions.add("Làm thế nào để giảm lượng đường trong chế độ ăn uống hàng ngày?");
        nutritionQuestions.add("Nên ăn bao nhiêu rau quả mỗi ngày?");
        nutritionQuestions.add("Những thực phẩm nào giàu kẽm và sắt?");
        nutritionQuestions.add("Chế độ ăn Địa Trung Hải có lợi ích gì cho sức khỏe?");
        nutritionQuestions.add("Ăn uống như thế nào để tăng cường sức đề kháng tự nhiên?");
        questionBank.put("nutrition", nutritionQuestions);

        // TẬP THỂ DỤC - Ngân hàng câu hỏi phong phú hơn
        List<String> exerciseQuestions = new java.util.ArrayList<>();
        exerciseQuestions.add("Các bài tập thể dục nào tốt nhất cho người bận rộn?");
        exerciseQuestions.add("Bao nhiêu phút tập thể dục mỗi ngày là đủ?");
        exerciseQuestions.add("Những bài tập cardio hiệu quả nhất để đốt cháy mỡ thừa?");
        exerciseQuestions.add("Làm thế nào để duy trì động lực tập thể dục hàng ngày?");
        exerciseQuestions.add("Lịch tập luyện tối ưu cho người mới bắt đầu tập gym?");
        exerciseQuestions.add("Tập luyện cường độ cao HIIT có lợi ích gì?");
        exerciseQuestions.add("Làm thế nào để tăng cơ hiệu quả mà không cần đến phòng tập?");
        exerciseQuestions.add("Những bài tập nào tốt nhất để cải thiện sức bền?");
        exerciseQuestions.add("Tại sao việc khởi động trước khi tập luyện lại quan trọng?");
        exerciseQuestions.add("Làm thế nào để tránh chấn thương khi tập thể dục?");
        exerciseQuestions.add("Bài tập nào hiệu quả nhất để cải thiện vóc dáng?");
        exerciseQuestions.add("Tập yoga có lợi ích gì cho sức khỏe tinh thần?");
        questionBank.put("exercise", exerciseQuestions);

        // GIẢM CÂN - Ngân hàng câu hỏi phong phú hơn
        List<String> weightLossQuestions = new java.util.ArrayList<>();
        weightLossQuestions.add("Làm thế nào để giảm cân lành mạnh và bền vững?");
        weightLossQuestions.add("Những thói quen ăn uống nào giúp giảm cân hiệu quả?");
        weightLossQuestions.add("Tại sao tôi tập thể dục đều đặn nhưng vẫn không giảm cân?");
        weightLossQuestions.add("Làm thế nào để giảm mỡ bụng hiệu quả?");
        weightLossQuestions.add("Liệu nhịn ăn gián đoạn có thực sự hiệu quả để giảm cân?");
        weightLossQuestions.add("Những loại thực phẩm nào nên hạn chế khi đang giảm cân?");
        weightLossQuestions.add("Làm thế nào để kiểm soát cơn thèm ăn?");
        weightLossQuestions.add("Vai trò của protein trong việc giảm cân?");
        weightLossQuestions.add("Tại sao việc uống đủ nước lại quan trọng khi giảm cân?");
        weightLossQuestions.add("Làm thế nào để duy trì cân nặng sau khi giảm cân thành công?");
        weightLossQuestions.add("Những lỗi phổ biến nhất khi giảm cân là gì?");
        weightLossQuestions.add("Làm thế nào để tăng tốc độ trao đổi chất của cơ thể?");
        questionBank.put("weightloss", weightLossQuestions);

        // SỨC KHỎE TINH THẦN - Ngân hàng câu hỏi phong phú hơn
        List<String> mentalHealthQuestions = new java.util.ArrayList<>();
        mentalHealthQuestions.add("Làm thế nào để giảm căng thẳng và lo âu hàng ngày?");
        mentalHealthQuestions.add("Các kỹ thuật thư giãn tốt nhất cho người bận rộn?");
        mentalHealthQuestions.add("Làm thế nào để cải thiện sức khỏe tinh thần trong thời đại công nghệ?");
        mentalHealthQuestions.add("Những dấu hiệu của trầm cảm mà tôi nên để ý?");
        mentalHealthQuestions.add("Thiền có tác dụng gì đối với sức khỏe tinh thần?");
        mentalHealthQuestions.add("Làm thế nào để cải thiện khả năng tập trung?");
        mentalHealthQuestions.add("Mối liên hệ giữa chế độ ăn uống và sức khỏe tinh thần?");
        mentalHealthQuestions.add("Cách đối phó với lo âu xã hội?");
        mentalHealthQuestions.add("Những thói quen tốt nhất để cải thiện tâm trạng mỗi ngày?");
        mentalHealthQuestions.add("Làm thế nào để xây dựng khả năng phục hồi tinh thần?");
        mentalHealthQuestions.add("Khi nào tôi nên tìm sự giúp đỡ chuyên môn cho vấn đề tâm lý?");
        mentalHealthQuestions.add("Cách xây dựng lòng tự trọng và sự tự tin?");
        questionBank.put("mentalhealth", mentalHealthQuestions);

        // GIẤC NGỦ - Ngân hàng câu hỏi phong phú hơn
        List<String> sleepQuestions = new java.util.ArrayList<>();
        sleepQuestions.add("Cách cải thiện chất lượng giấc ngủ tự nhiên?");
        sleepQuestions.add("Tại sao tôi thường thức giấc vào giữa đêm?");
        sleepQuestions.add("Những thực phẩm nào giúp cải thiện giấc ngủ?");
        sleepQuestions.add("Bao nhiêu giờ ngủ một ngày là đủ cho người trưởng thành?");
        sleepQuestions.add("Tại sao tôi vẫn cảm thấy mệt mỏi sau khi ngủ đủ 8 tiếng?");
        sleepQuestions.add("Làm thế nào để thiết lập thói quen ngủ lành mạnh?");
        sleepQuestions.add("Ánh sáng xanh từ thiết bị điện tử ảnh hưởng thế nào đến giấc ngủ?");
        sleepQuestions.add("Những phương pháp tự nhiên để điều trị chứng mất ngủ?");
        sleepQuestions.add("Thiền và thư giãn trước khi ngủ có hiệu quả không?");
        sleepQuestions.add("Vị trí ngủ nào tốt nhất cho cột sống?");
        sleepQuestions.add("Cách chọn gối và nệm phù hợp để có giấc ngủ ngon?");
        sleepQuestions.add("Làm thế nào để đồng bộ lại đồng hồ sinh học khi bị jet lag?");
        questionBank.put("sleep", sleepQuestions);

        // VITAMIN & THỰC PHẨM BỔ SUNG - Ngân hàng câu hỏi phong phú hơn
        List<String> supplementQuestions = new java.util.ArrayList<>();
        supplementQuestions.add("Tôi có nên bổ sung vitamin hàng ngày không?");
        supplementQuestions.add("Những loại vitamin nào cần thiết trong mùa đông?");
        supplementQuestions.add("Các dấu hiệu của việc thiếu vitamin D?");
        supplementQuestions.add("Tác dụng của omega-3 đối với sức khỏe tim mạch?");
        supplementQuestions.add("Những thực phẩm bổ sung nào cần thiết cho người ăn chay?");
        supplementQuestions.add("Làm thế nào để biết cơ thể thiếu vitamin gì?");
        supplementQuestions.add("Probiotics có lợi ích gì cho hệ tiêu hóa?");
        supplementQuestions.add("Collagen bổ sung có thực sự cải thiện sức khỏe da không?");
        supplementQuestions.add("Liệu tôi có nên uống các thực phẩm bổ sung tăng sức đề kháng?");
        supplementQuestions.add("Các vitamin nào cần thiết cho sức khỏe mắt?");
        supplementQuestions.add("Sự khác biệt giữa vitamin tổng hợp và vitamin từ thực phẩm là gì?");
        supplementQuestions.add("Cách phân biệt thực phẩm bổ sung chất lượng cao?");
        questionBank.put("supplements", supplementQuestions);

        // MẠCH MÁU & TIM - Ngân hàng câu hỏi mới
        List<String> cardiovascularQuestions = new java.util.ArrayList<>();
        cardiovascularQuestions.add("Làm thế nào để giảm huyết áp một cách tự nhiên?");
        cardiovascularQuestions.add("Những dấu hiệu cảnh báo sớm của bệnh tim mạch?");
        cardiovascularQuestions.add("Chế độ ăn nào tốt nhất cho sức khỏe tim mạch?");
        cardiovascularQuestions.add("Tập thể dục như thế nào để tăng cường sức khỏe tim?");
        cardiovascularQuestions.add("Làm thế nào để kiểm soát cholesterol cao?");
        cardiovascularQuestions.add("Stress ảnh hưởng như thế nào đến sức khỏe tim mạch?");
        cardiovascularQuestions.add("Những thực phẩm nào nên tránh để bảo vệ tim mạch?");
        cardiovascularQuestions.add("Làm thế nào để biết khi nào cần đi khám tim mạch?");
        cardiovascularQuestions.add("Uống rượu ảnh hưởng thế nào đến tim mạch?");
        cardiovascularQuestions.add("Làm thế nào để tăng tuần hoàn máu tự nhiên?");
        cardiovascularQuestions.add("Có nên bổ sung CoQ10 cho sức khỏe tim mạch không?");
        cardiovascularQuestions.add("Tại sao lượng đường trong máu cao lại ảnh hưởng đến tim mạch?");
        questionBank.put("cardiovascular", cardiovascularQuestions);

        // HỆ MIỄN DỊCH - Ngân hàng câu hỏi mới
        List<String> immuneQuestions = new java.util.ArrayList<>();
        immuneQuestions.add("Làm thế nào để tăng cường hệ miễn dịch tự nhiên?");
        immuneQuestions.add("Những thực phẩm nào tốt nhất cho hệ miễn dịch?");
        immuneQuestions.add("Stress ảnh hưởng như thế nào đến hệ miễn dịch?");
        immuneQuestions.add("Làm thế nào để cơ thể phục hồi nhanh hơn sau ốm?");
        immuneQuestions.add("Vi khuẩn probiotics có tác dụng gì với hệ miễn dịch?");
        immuneQuestions.add("Vitamin C và kẽm thực sự giúp tăng cường miễn dịch không?");
        immuneQuestions.add("Giấc ngủ ảnh hưởng thế nào đến khả năng miễn dịch?");
        immuneQuestions.add("Làm thế nào để biết hệ miễn dịch của bạn đang yếu?");
        immuneQuestions.add("Những thói quen hàng ngày có thể làm suy yếu hệ miễn dịch?");
        immuneQuestions.add("Lợi ích của việc tắm nước lạnh đối với hệ miễn dịch?");
        immuneQuestions.add("Tập luyện quá sức ảnh hưởng thế nào đến hệ miễn dịch?");
        immuneQuestions.add("Làm thế nào để cân bằng hệ miễn dịch quá mẫn?");
        questionBank.put("immune", immuneQuestions);

        // Duyệt qua từng từ khóa để sinh câu hỏi
        for (String keyword : keywords) {
            String lowerKeyword = keyword.toLowerCase();
            List<String> questionsForKeyword = new java.util.ArrayList<>();

            // Xác định danh sách câu hỏi phù hợp dựa trên từ khóa
            if (lowerKeyword.contains("dinh dưỡng") || lowerKeyword.contains("dinh duong") || lowerKeyword.contains("nutrition")) {
                questionsForKeyword.addAll(questionBank.get("nutrition"));
            } else if (lowerKeyword.contains("tập") || lowerKeyword.contains("tap") || lowerKeyword.contains("exercise")) {
                questionsForKeyword.addAll(questionBank.get("exercise"));
            } else if (lowerKeyword.contains("giảm cân") || lowerKeyword.contains("giam can") || lowerKeyword.contains("weight")) {
                questionsForKeyword.addAll(questionBank.get("weightloss"));
            } else if (lowerKeyword.contains("tinh thần") || lowerKeyword.contains("tinh than") || lowerKeyword.contains("mental")) {
                questionsForKeyword.addAll(questionBank.get("mentalhealth"));
            } else if (lowerKeyword.contains("ngủ") || lowerKeyword.contains("ngu") || lowerKeyword.contains("sleep")) {
                questionsForKeyword.addAll(questionBank.get("sleep"));
            } else if (lowerKeyword.contains("vitamin") || lowerKeyword.contains("bổ sung") || lowerKeyword.contains("bo sung")) {
                questionsForKeyword.addAll(questionBank.get("supplements"));
            } else if (lowerKeyword.contains("tim") || lowerKeyword.contains("mạch") || lowerKeyword.contains("mach") || lowerKeyword.contains("heart")) {
                questionsForKeyword.addAll(questionBank.get("cardiovascular"));
            } else if (lowerKeyword.contains("miễn dịch") || lowerKeyword.contains("mien dich") || lowerKeyword.contains("immune")) {
                questionsForKeyword.addAll(questionBank.get("immune"));
            } else {
                // Tạo câu hỏi tùy chỉnh dựa trên từ khóa
                questionsForKeyword.add("Làm thế nào để cải thiện sức khỏe liên quan đến " + keyword + "?");
                questionsForKeyword.add("Những lợi ích sức khỏe của " + keyword + " là gì?");
                questionsForKeyword.add("Tôi nên biết gì về " + keyword + " để sống khỏe mạnh hơn?");
                questionsForKeyword.add("Những thực phẩm nào tốt cho " + keyword + "?");
                questionsForKeyword.add("Làm thế nào để đưa " + keyword + " vào lối sống hàng ngày?");
            }

            // Lọc bỏ những câu hỏi đã hiển thị gần đây
            questionsForKeyword.removeAll(recentQuestions);

            // Nếu sau khi lọc mà không còn câu hỏi nào, sử dụng lại toàn bộ câu hỏi
            if (questionsForKeyword.isEmpty()) {
                if (lowerKeyword.contains("dinh dưỡng") || lowerKeyword.contains("dinh duong") || lowerKeyword.contains("nutrition")) {
                    questionsForKeyword.addAll(questionBank.get("nutrition"));
                } else if (lowerKeyword.contains("tập") || lowerKeyword.contains("tap") || lowerKeyword.contains("exercise")) {
                    questionsForKeyword.addAll(questionBank.get("exercise"));
                } else if (lowerKeyword.contains("giảm cân") || lowerKeyword.contains("giam can") || lowerKeyword.contains("weight")) {
                    questionsForKeyword.addAll(questionBank.get("weightloss"));
                } else if (lowerKeyword.contains("tinh thần") || lowerKeyword.contains("tinh than") || lowerKeyword.contains("mental")) {
                    questionsForKeyword.addAll(questionBank.get("mentalhealth"));
                } else if (lowerKeyword.contains("ngủ") || lowerKeyword.contains("ngu") || lowerKeyword.contains("sleep")) {
                    questionsForKeyword.addAll(questionBank.get("sleep"));
                } else if (lowerKeyword.contains("vitamin") || lowerKeyword.contains("bổ sung") || lowerKeyword.contains("bo sung")) {
                    questionsForKeyword.addAll(questionBank.get("supplements"));
                } else if (lowerKeyword.contains("tim") || lowerKeyword.contains("mạch") || lowerKeyword.contains("mach") || lowerKeyword.contains("heart")) {
                    questionsForKeyword.addAll(questionBank.get("cardiovascular"));
                } else if (lowerKeyword.contains("miễn dịch") || lowerKeyword.contains("mien dich") || lowerKeyword.contains("immune")) {
                    questionsForKeyword.addAll(questionBank.get("immune"));
                }
            }

            // Thêm yếu tố ngày trong tuần và giờ trong ngày để đa dạng hóa
            if (dayOfWeek == Calendar.MONDAY || dayOfWeek == Calendar.TUESDAY) {
                // Đầu tuần: ưu tiên câu hỏi về động lực và kế hoạch
                questionsForKeyword.add("Làm thế nào để lập kế hoạch dinh dưỡng cho cả tuần?");
                questionsForKeyword.add("Cách xây dựng thói quen tập luyện bền vững?");
            } else if (dayOfWeek == Calendar.WEDNESDAY || dayOfWeek == Calendar.THURSDAY) {
                // Giữa tuần: ưu tiên câu hỏi về duy trì năng lượng
                questionsForKeyword.add("Những thực phẩm nào giúp duy trì năng lượng cả ngày?");
                questionsForKeyword.add("Làm thế nào để tránh kiệt sức giữa tuần?");
            } else {
                // Cuối tuần: ưu tiên câu hỏi về thư giãn và hồi phục
                questionsForKeyword.add("Các hoạt động cuối tuần nào tốt cho sức khỏe?");
                questionsForKeyword.add("Làm thế nào để thư giãn hiệu quả sau một tuần làm việc?");
            }

            if (hourOfDay >= 5 && hourOfDay < 10) {
                // Buổi sáng: câu hỏi về bữa sáng, năng lượng
                questionsForKeyword.add("Những bữa sáng nào giàu dinh dưỡng và nhanh gọn?");
                questionsForKeyword.add("Các bài tập buổi sáng nào hiệu quả nhất?");
            } else if (hourOfDay >= 10 && hourOfDay < 14) {
                // Buổi trưa: câu hỏi về bữa trưa, duy trì năng lượng
                questionsForKeyword.add("Làm thế nào để tránh buồn ngủ sau bữa trưa?");
                questionsForKeyword.add("Những món ăn trưa nào vừa lành mạnh vừa tiện lợi?");
            } else if (hourOfDay >= 14 && hourOfDay < 18) {
                // Buổi chiều: câu hỏi về tập luyện, ăn nhẹ
                questionsForKeyword.add("Những món ăn nhẹ lành mạnh cho buổi chiều là gì?");
                questionsForKeyword.add("Thời điểm nào trong ngày tốt nhất để tập thể dục?");
            } else {
                // Buổi tối: câu hỏi về thư giãn, giấc ngủ
                questionsForKeyword.add("Các thói quen buổi tối giúp ngủ ngon?");
                questionsForKeyword.add("Nên ăn gì vào buổi tối để không ảnh hưởng đến giấc ngủ?");
            }

            // Xáo trộn câu hỏi và chọn ngẫu nhiên
            java.util.Collections.shuffle(questionsForKeyword, random);
            if (!questionsForKeyword.isEmpty()) {
                String selectedQuestion = questionsForKeyword.get(0);
                allPossibleQuestions.add(selectedQuestion);
            }
        }

        // Xáo trộn tất cả câu hỏi để đa dạng hóa
        java.util.Collections.shuffle(allPossibleQuestions, random);

        // Chọn tối đa 3 câu hỏi từ danh sách đã xáo trộn
        List<String> finalQuestions = new java.util.ArrayList<>();
        Set<String> newRecentQuestions = new HashSet<>();

        for (String question : allPossibleQuestions) {
            if (!finalQuestions.contains(question) && !recentQuestions.contains(question)) {
                finalQuestions.add(question);
                newRecentQuestions.add(question);
                if (finalQuestions.size() >= 3) {
                    break;
                }
            }
        }

        // Nếu không đủ 3 câu hỏi, thêm câu hỏi từ danh sách chung
        if (finalQuestions.size() < 3) {
            List<String> generalQuestions = getGeneralHealthQuestions(dayOfWeek, hourOfDay);
            java.util.Collections.shuffle(generalQuestions, random);

            for (String question : generalQuestions) {
                if (!finalQuestions.contains(question) && !recentQuestions.contains(question)) {
                    finalQuestions.add(question);
                    newRecentQuestions.add(question);
                    if (finalQuestions.size() >= 3) {
                        break;
                    }
                }
            }

            // Nếu vẫn không đủ, dùng các câu hỏi theo chủ đề ngày
            if (finalQuestions.size() < 3) {
                List<String> daySpecificQuestions = getDaySpecificQuestions(dayOfWeek);
                java.util.Collections.shuffle(daySpecificQuestions, random);

                for (String question : daySpecificQuestions) {
                    if (!finalQuestions.contains(question)) {
                        finalQuestions.add(question);
                        if (finalQuestions.size() >= 3) {
                            break;
                        }
                    }
                }
            }
        }

        // Lưu các câu hỏi đã hiển thị vào lịch sử (giới hạn 20 câu hỏi gần nhất)
        recentQuestions.addAll(newRecentQuestions);
        if (recentQuestions.size() > 20) {
            // Nếu có quá nhiều câu hỏi trong lịch sử, chỉ giữ lại 10 câu gần nhất
            Set<String> trimmedHistory = new HashSet<>();
            int i = 0;
            for (String q : recentQuestions) {
                trimmedHistory.add(q);
                i++;
                if (i >= 10) break;
            }
            recentQuestions = trimmedHistory;
        }

        // Lưu lại lịch sử câu hỏi
        String updatedRecentQuestionsJson = gson.toJson(recentQuestions);
        prefs.edit().putString("recent_questions", updatedRecentQuestionsJson).apply();

        return finalQuestions;
    }

    /**
     * Trả về danh sách câu hỏi chung về sức khỏe
     */
    private List<String> getGeneralHealthQuestions(int dayOfWeek, int hourOfDay) {
        List<String> questions = new java.util.ArrayList<>();

        questions.add("Làm thế nào để tăng cường sức đề kháng tự nhiên?");
        questions.add("Chế độ ăn nào cân bằng nhất cho người trưởng thành?");
        questions.add("Cách cải thiện chất lượng giấc ngủ của bạn?");
        questions.add("Những thói quen hàng ngày nào giúp sống khỏe mạnh hơn?");
        questions.add("Làm thế nào để duy trì cân nặng hợp lý?");
        questions.add("Tại sao uống đủ nước lại quan trọng đối với sức khỏe?");
        questions.add("Những loại trái cây nào có nhiều chất chống oxy hóa nhất?");
        questions.add("Cách giảm căng thẳng và lo âu trong cuộc sống hàng ngày?");
        questions.add("Làm thế nào để tăng cường sức khỏe não bộ?");
        questions.add("Những dấu hiệu cảnh báo cơ thể đang thiếu vitamin?");
        questions.add("Tác dụng của việc tập thể dục đều đặn với sức khỏe?");
        questions.add("Nên ăn những loại hạt nào để bổ sung dinh dưỡng?");
        questions.add("Làm thế nào để tăng cường sức khỏe hệ tiêu hóa?");
        questions.add("Những phương pháp tự nhiên để tăng cường miễn dịch?");
        questions.add("Cách phòng ngừa các bệnh tim mạch thường gặp?");
        questions.add("Những thực phẩm nào giàu omega-3 tốt cho não bộ?");
        questions.add("Làm thế nào để cải thiện sức khỏe da từ bên trong?");
        questions.add("Có nên uống cà phê mỗi ngày không?");
        questions.add("Những thực phẩm fermented có lợi gì cho sức khỏe?");
        questions.add("Bao nhiêu giờ ngủ là đủ cho một người trưởng thành?");

        return questions;
    }

    /**
     * Trả về danh sách câu hỏi dựa theo ngày trong tuần
     */
    private List<String> getDaySpecificQuestions(int dayOfWeek) {
        List<String> questions = new java.util.ArrayList<>();

        switch (dayOfWeek) {
            case Calendar.MONDAY:
                questions.add("Làm thế nào để khởi động một tuần mới đầy năng lượng?");
                questions.add("Những thói quen buổi sáng nào giúp bạn năng suất hơn?");
                questions.add("Kế hoạch ăn uống cho cả tuần nên chuẩn bị như thế nào?");
                questions.add("Làm thế nào để duy trì động lực tập thể dục vào đầu tuần?");
                break;

            case Calendar.TUESDAY:
                questions.add("Các bài tập nào tốt nhất cho ngày thứ Ba?");
                questions.add("Làm thế nào để duy trì năng lượng sau ngày đầu tuần?");
                questions.add("Những món ăn nào giàu protein phù hợp cho bữa tối thứ Ba?");
                questions.add("Cách cân bằng công việc và sức khỏe vào giữa tuần?");
                break;

            case Calendar.WEDNESDAY:
                questions.add("Làm thế nào để vượt qua mệt mỏi giữa tuần?");
                questions.add("Những bài tập ngắn nào hiệu quả cho ngày bận rộn?");
                questions.add("Các loại thực phẩm nào giúp tăng năng lượng vào giữa tuần?");
                questions.add("Cách thư giãn nhanh chóng sau một ngày làm việc căng thẳng?");
                break;

            case Calendar.THURSDAY:
                questions.add("Làm thế nào để duy trì năng suất khi gần cuối tuần?");
                questions.add("Những thực phẩm nào giúp tăng cường tập trung?");
                questions.add("Các kỹ thuật thư giãn nhanh cho ngày làm việc căng thẳng?");
                questions.add("Làm thế nào để chuẩn bị cơ thể cho hoạt động cuối tuần?");
                break;

            case Calendar.FRIDAY:
                questions.add("Làm thế nào để thư giãn sau một tuần làm việc?");
                questions.add("Những hoạt động cuối tuần nào có lợi cho sức khỏe?");
                questions.add("Nên ăn gì vào tối thứ Sáu đ��� không ảnh hưởng đến giấc ngủ?");
                questions.add("Cách detox cơ thể sau một tuần làm việc căng thẳng?");
                break;

            case Calendar.SATURDAY:
                questions.add("Những hoạt động ngoài trời nào tốt cho sức khỏe vào cuối tuần?");
                questions.add("Các món ăn lành mạnh nào dễ chế biến cho bữa sáng cuối tuần?");
                questions.add("Làm thế nào để tận dụng thời gian cuối tuần để cải thiện sức khỏe?");
                questions.add("Các bài tập nào phù hợp khi bạn có nhiều thời gian hơn vào cuối tuần?");
                break;

            case Calendar.SUNDAY:
                questions.add("Làm thế nào để chuẩn bị cho một tuần mới khỏe mạnh?");
                questions.add("Những món ăn nào tốt cho việc meal prep đầu tuần?");
                questions.add("Các hoạt động thư giãn nào giúp giảm stress trước tuần mới?");
                questions.add("Làm thế nào để có giấc ngủ chất lượng vào Chủ nhật tối?");
                break;
        }

        return questions;
    }

    /**
     * Đặt các câu hỏi gợi ý mặc định
     */
    private void setDefaultSuggestedQuestions() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        // Lấy ngày tháng hiện tại để tạo câu hỏi phù hợp với mùa
        int month = calendar.get(Calendar.MONTH); // 0-11, với 0 là tháng 1

        List<String> defaultQuestions = new java.util.ArrayList<>();

        // Câu hỏi theo mùa - tháng 10 là mùa thu
        if (month >= 9 && month <= 11) { // Tháng 10-12 mùa thu đông
            defaultQuestions.add("Làm thế nào để tăng cường sức đề kháng trong mùa thu?");
            defaultQuestions.add("Những thực phẩm nào tốt để bổ sung vitamin trong mùa lạnh?");
            defaultQuestions.add("Cách phòng ngừa cảm cúm khi thời tiết giao mùa?");
            defaultQuestions.add("Chế độ dinh dưỡng nào cân bằng cho mùa thu đông?");
            defaultQuestions.add("Làm thế nào để duy trì động lực tập thể dục khi trời lạnh?");
        } else if (month >= 0 && month <= 2) { // Tháng 1-3 mùa đông xuân
            defaultQuestions.add("Làm thế nào để giữ ấm cơ thể trong mùa đông?");
            defaultQuestions.add("Những loại trà thảo mộc nào tốt cho sức khỏe mùa lạnh?");
            defaultQuestions.add("Cách tăng cường hệ miễn dịch trong mùa đông?");
            defaultQuestions.add("Làm thế nào để chống khô da trong mùa lạnh?");
            defaultQuestions.add("Các bài tập thể dục phù hợp với thời tiết lạnh?");
        } else if (month >= 3 && month <= 5) { // Tháng 4-6 mùa xuân hè
            defaultQuestions.add("Làm thế nào để giảm các triệu chứng dị ứng mùa xuân?");
            defaultQuestions.add("Những loại rau quả nào tốt nhất cho mùa xuân?");
            defaultQuestions.add("Cách detox cơ thể tự nhiên sau mùa đông?");
            defaultQuestions.add("Làm thế nào để chuẩn bị cho mùa hè sắp tới?");
            defaultQuestions.add("Các hoạt động ngoài trời phù hợp với thời tiết xuân?");
        } else { // Tháng 7-9 mùa hè thu
            defaultQuestions.add("Làm thế nào để giữ cơ thể đủ nước trong những ngày nóng?");
            defaultQuestions.add("Những loại thực phẩm giúp làm mát cơ thể vào mùa hè?");
            defaultQuestions.add("Cách phòng ngừa say nắng khi thời tiết quá nóng?");
            defaultQuestions.add("Làm thế nào để tập luyện hiệu quả trong thời tiết nóng?");
            defaultQuestions.add("Các cách tự nhiên để cải thiện giấc ngủ vào những đêm nóng?");
        }

        // Thêm câu hỏi theo ngày trong tuần
        List<String> dayQuestions = getDaySpecificQuestions(dayOfWeek);
        defaultQuestions.addAll(dayQuestions);

        // Thêm câu hỏi chung về sức khỏe
        defaultQuestions.add("Làm thế nào để tăng cường sức đề kháng tự nhiên?");
        defaultQuestions.add("Chế độ ăn nào tốt cho tim mạch?");
        defaultQuestions.add("Cách cải thiện chất lượng giấc ngủ?");
        defaultQuestions.add("Những thực phẩm nào giàu protein cho người ăn chay?");
        defaultQuestions.add("Làm thế nào để duy trì cân nặng hợp lý?");

        // Xáo trộn danh sách câu hỏi
        java.util.Collections.shuffle(defaultQuestions);

        // Chọn 3 câu đầu tiên để hiển thị
        List<String> finalQuestions = defaultQuestions.subList(0, Math.min(3, defaultQuestions.size()));

        if (isViewAttached()) {
            view.updateSuggestedQuestions(finalQuestions);
        }
    }
}
