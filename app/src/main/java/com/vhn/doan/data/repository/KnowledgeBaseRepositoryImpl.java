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

        for (String keyword : keywords) {
            for (KnowledgeBaseArticle article : articles) {
                if (article.getKeywords() != null) {
                    for (String articleKeyword : article.getKeywords()) {
                        if (articleKeyword.equalsIgnoreCase(keyword)) {
                            callback.onSuccess(article);
                            return;
                        }
                    }
                }
                // Also check in question
                if (article.getQuestion() != null && article.getQuestion().toLowerCase().contains(keyword.toLowerCase())) {
                    callback.onSuccess(article);
                    return;
                }
            }
        }

        callback.onSuccess(null); // Not found
    }
}
