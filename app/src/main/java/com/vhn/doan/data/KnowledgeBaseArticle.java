package com.vhn.doan.data;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class KnowledgeBaseArticle {
    @SerializedName("id")
    private String id;
    @SerializedName("question")
    private String question;
    @SerializedName("keywords")
    private List<String> keywords;
    @SerializedName("answer")
    private String answer;
    @SerializedName("related_feature")
    private String relatedFeature;
    @SerializedName("last_updated")
    private long lastUpdated;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getRelatedFeature() { return relatedFeature; }
    public void setRelatedFeature(String relatedFeature) { this.relatedFeature = relatedFeature; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
}
