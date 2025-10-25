package com.vhn.doan.data;

/**
 * Model class cho FAQ Item
 */
public class FAQItem {
    private String question;
    private String answer;
    private String category;
    private int iconResId;
    private boolean isExpanded;

    public FAQItem() {
        // Constructor mặc định cho Firebase
    }

    public FAQItem(String question, String answer, String category, int iconResId) {
        this.question = question;
        this.answer = answer;
        this.category = category;
        this.iconResId = iconResId;
        this.isExpanded = false;
    }

    // Getters
    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public String getCategory() {
        return category;
    }

    public int getIconResId() {
        return iconResId;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    // Setters
    public void setQuestion(String question) {
        this.question = question;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}

