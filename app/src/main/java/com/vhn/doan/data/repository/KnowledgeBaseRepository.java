package com.vhn.doan.data.repository;

import com.vhn.doan.data.KnowledgeBaseArticle;
import java.util.List;

public interface KnowledgeBaseRepository {
    void findArticleByKeywords(List<String> keywords, RepositoryCallback<KnowledgeBaseArticle> callback);
}
