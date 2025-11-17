package com.vhn.doan.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Helper để generate câu hỏi gợi ý CÁ NHÂN HÓA dựa trên:
 * - Categories yêu thích của user
 * - Tips đã xem gần đây
 * - Thời gian trong ngày
 */
public class PersonalizedQuestionHelper {

    private static final String TAG = "PersonalizedQuestionHelper";

    public interface QuestionCallback {
        void onQuestionsGenerated(List<String> questions);
        void onError(String error);
    }

    /**
     * Generate 3 câu hỏi cá nhân hóa cho user
     */
    public static void generatePersonalizedQuestions(QuestionCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            generateDefaultQuestions(callback);
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Lấy favorite categories
        userRef.child("favoriteCategories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> favoriteCategories = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String categoryName = child.getValue(String.class);
                    if (categoryName != null) {
                        favoriteCategories.add(categoryName);
                    }
                }

                // Generate questions based on favorites
                List<String> questions = generateQuestionsFromCategories(favoriteCategories);
                callback.onQuestionsGenerated(questions);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error loading favorites: " + error.getMessage());
                generateDefaultQuestions(callback);
            }
        });
    }

    /**
     * Generate questions dựa trên categories yêu thích
     */
    private static List<String> generateQuestionsFromCategories(List<String> categories) {
        if (categories.isEmpty()) {
            return generateTimeBasedQuestions();
        }

        Map<String, List<String>> questionBank = getQuestionBank();
        List<String> allQuestions = new ArrayList<>();
        Random random = new Random();

        // Lấy questions từ mỗi category
        for (String category : categories) {
            String normalizedCategory = normalizeCategory(category);
            List<String> categoryQuestions = questionBank.get(normalizedCategory);
            if (categoryQuestions != null && !categoryQuestions.isEmpty()) {
                allQuestions.addAll(categoryQuestions);
            }
        }

        // Nếu không có câu hỏi nào, dùng time-based
        if (allQuestions.isEmpty()) {
            return generateTimeBasedQuestions();
        }

        // Shuffle và chọn 3 câu
        Collections.shuffle(allQuestions, random);
        return allQuestions.subList(0, Math.min(3, allQuestions.size()));
    }

    /**
     * Generate questions dựa trên thời gian trong ngày
     */
    private static List<String> generateTimeBasedQuestions() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        List<String> questions = new ArrayList<>();

        if (hour >= 5 && hour < 12) {
            // Buổi sáng
            questions.add("Những bữa sáng nào giàu dinh dưỡng và nhanh gọn?");
            questions.add("Các bài tập buổi sáng nào hiệu quả nhất?");
            questions.add("Làm thế nào để bắt đầu ngày mới đầy năng lượng?");
        } else if (hour >= 12 && hour < 18) {
            // Buổi chiều
            questions.add("Làm thế nào để tránh buồn ngủ sau bữa trưa?");
            questions.add("Những món ăn nhẹ lành mạnh cho buổi chiều là gì?");
            questions.add("Thời điểm nào trong ngày tốt nhất để tập thể dục?");
        } else {
            // Buổi tối
            questions.add("Các thói quen buổi tối giúp ngủ ngon?");
            questions.add("Nên ăn gì vào buổi tối để không ảnh hưởng đến giấc ngủ?");
            questions.add("Làm thế nào để thư giãn sau một ngày làm việc?");
        }

        Collections.shuffle(questions);
        return questions.subList(0, Math.min(3, questions.size()));
    }

    /**
     * Generate default questions khi không có data
     */
    private static void generateDefaultQuestions(QuestionCallback callback) {
        List<String> questions = generateTimeBasedQuestions();
        callback.onQuestionsGenerated(questions);
    }

    /**
     * Normalize category name
     */
    private static String normalizeCategory(String category) {
        String lower = category.toLowerCase();
        if (lower.contains("dinh dưỡng") || lower.contains("nutrition")) return "nutrition";
        if (lower.contains("tập") || lower.contains("exercise")) return "exercise";
        if (lower.contains("giảm cân") || lower.contains("weight")) return "weightloss";
        if (lower.contains("tinh thần") || lower.contains("mental")) return "mental";
        if (lower.contains("ngủ") || lower.contains("sleep")) return "sleep";
        if (lower.contains("vitamin")) return "supplements";
        if (lower.contains("tim") || lower.contains("heart")) return "cardiovascular";
        if (lower.contains("miễn dịch") || lower.contains("immune")) return "immune";
        return "general";
    }

    /**
     * Question bank theo từng category
     */
    private static Map<String, List<String>> getQuestionBank() {
        Map<String, List<String>> bank = new HashMap<>();

        // Nutrition
        List<String> nutrition = new ArrayList<>();
        nutrition.add("Những thực phẩm nào tốt cho sức khỏe tim mạch?");
        nutrition.add("Làm thế nào để cân bằng dinh dưỡng trong bữa ăn hàng ngày?");
        nutrition.add("Chất béo lành mạnh có trong những thực phẩm nào?");
        nutrition.add("Nên ăn bao nhiêu rau quả mỗi ngày?");
        bank.put("nutrition", nutrition);

        // Exercise
        List<String> exercise = new ArrayList<>();
        exercise.add("Các bài tập thể dục nào tốt nhất cho người bận rộn?");
        exercise.add("Bao nhiêu phút tập thể dục mỗi ngày là đủ?");
        exercise.add("Làm thế nào để duy trì động lực tập thể dục hàng ngày?");
        exercise.add("Tập yoga có lợi ích gì cho sức khỏe tinh thần?");
        bank.put("exercise", exercise);

        // Weight Loss
        List<String> weightloss = new ArrayList<>();
        weightloss.add("Làm thế nào để giảm cân lành mạnh và bền vững?");
        weightloss.add("Làm thế nào để giảm mỡ bụng hiệu quả?");
        weightloss.add("Làm thế nào để kiểm soát cơn thèm ăn?");
        bank.put("weightloss", weightloss);

        // Mental Health
        List<String> mental = new ArrayList<>();
        mental.add("Làm thế nào để giảm căng thẳng và lo âu hàng ngày?");
        mental.add("Thiền có tác dụng gì đối với sức khỏe tinh thần?");
        mental.add("Làm thế nào để cải thiện khả năng tập trung?");
        bank.put("mental", mental);

        // Sleep
        List<String> sleep = new ArrayList<>();
        sleep.add("Cách cải thiện chất lượng giấc ngủ tự nhiên?");
        sleep.add("Bao nhiêu giờ ngủ một ngày là đủ cho người trưởng thành?");
        sleep.add("Làm thế nào để thiết lập thói quen ngủ lành mạnh?");
        bank.put("sleep", sleep);

        // General
        List<String> general = new ArrayList<>();
        general.add("Làm thế nào để tăng cường sức đề kháng tự nhiên?");
        general.add("Những thói quen hàng ngày nào giúp sống khỏe mạnh hơn?");
        general.add("Tại sao uống đủ nước lại quan trọng đối với sức khỏe?");
        bank.put("general", general);

        return bank;
    }
}
