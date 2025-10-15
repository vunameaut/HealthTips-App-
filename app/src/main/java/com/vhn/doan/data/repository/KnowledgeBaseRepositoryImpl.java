package com.vhn.doan.data.repository;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vhn.doan.data.KnowledgeBaseArticle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KnowledgeBaseRepositoryImpl implements KnowledgeBaseRepository {

    private static final String KNOWLEDGE_BASE_FILE = "knowledge_base_data.json";
    private final Context context;
    private List<KnowledgeBaseArticle> articles;

    public KnowledgeBaseRepositoryImpl(Context context) {
        this.context = context;
        loadKnowledgeBase();
    }

    private void loadKnowledgeBase() {
        articles = new ArrayList<>();
        // In a real app, this should be loaded from res/raw.
        // For this implementation, we read from the assets folder.
        try (InputStream is = context.getAssets().open(KNOWLEDGE_BASE_FILE)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Map<String, KnowledgeBaseArticle>>>() {}.getType();
            Map<String, Map<String, KnowledgeBaseArticle>> data = gson.fromJson(reader, type);
            if (data != null && data.containsKey("knowledge_base")) {
                articles.addAll(data.get("knowledge_base").values());
            }
        } catch (IOException e) {
            e.printStackTrace();
            // In a real app, handle this error properly
        }
    }

    @Override
    public void findArticleByKeywords(List<String> keywords, RepositoryCallback<KnowledgeBaseArticle> callback) {
        if (articles == null || articles.isEmpty()) {
            callback.onError("Knowledge base is not loaded.");
            return;
        }

        KnowledgeBaseArticle bestMatch = null;
        int maxScore = 0;

        for (KnowledgeBaseArticle article : articles) {
            int currentScore = 0;
            if (article.getKeywords() != null) {
                for (String keyword : keywords) {
                    for (String articleKeyword : article.getKeywords()) {
                        if (articleKeyword.equalsIgnoreCase(keyword)) {
                            currentScore++;
                        }
                    }
                }
            }
            
            if (currentScore > maxScore) {
                maxScore = currentScore;
                bestMatch = article;
            }
        }

        // Only return a match if the score is above a certain threshold.
        // A score of 1 could be a coincidence. Let's use a threshold of 2,
        // or 1 if the user's query only has a few keywords.
        int threshold = (keywords.size() <= 2) ? 1 : 2;

        if (maxScore >= threshold) {
            callback.onSuccess(bestMatch);
        } else {
            callback.onSuccess(null); // Not found
        }
    }
}
