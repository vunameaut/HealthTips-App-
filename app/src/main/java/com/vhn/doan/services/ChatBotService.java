package com.vhn.doan.services;

import android.content.Context;

import com.vhn.doan.data.KnowledgeBaseArticle;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.repository.ChatRepository;
import com.vhn.doan.data.repository.ChatRepositoryImpl;
import com.vhn.doan.data.repository.FirebaseVideoRepositoryImpl;
import com.vhn.doan.data.repository.KnowledgeBaseRepository;
import com.vhn.doan.data.repository.KnowledgeBaseRepositoryImpl;
import com.vhn.doan.data.repository.RepositoryCallback;
import com.vhn.doan.data.repository.VideoRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChatBotService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final VideoRepository videoRepository;
    private final ChatRepository chatRepository; // For fallback to OpenAI

    public ChatBotService(Context context) {
        this.knowledgeBaseRepository = new KnowledgeBaseRepositoryImpl(context);
        this.videoRepository = new FirebaseVideoRepositoryImpl();
        this.chatRepository = new ChatRepositoryImpl();
    }

    public CompletableFuture<String> getResponse(String message, String conversationId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        String lowerCaseMessage = message.toLowerCase();

        // 1. Check for data-driven questions
        if (lowerCaseMessage.contains("video nhiều tim nhất") || lowerCaseMessage.contains("video yêu thích nhất")) {
            videoRepository.getFeed(null, "vietnam", new VideoRepository.VideoCallback() {
                @Override
                public void onSuccess(List<ShortVideo> videos) {
                    if (videos != null && !videos.isEmpty()) {
                        // Sort by like count descending
                        Collections.sort(videos, (v1, v2) -> Long.compare(v2.getLikeCount(), v1.getLikeCount()));
                        ShortVideo topVideo = videos.get(0);
                        future.complete("Video được yêu thích nhất là '" + topVideo.getTitle() + "' với " + topVideo.getLikeCount() + " lượt thích.");
                    } else {
                        future.complete("Xin lỗi, tôi không tìm thấy video nào.");
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    future.complete("Xin lỗi, đã có lỗi xảy ra khi tìm video: " + errorMessage);
                }
            });
            return future;
        }

        // 2. Check knowledge base for how-to questions
        List<String> keywords = extractKeywords(lowerCaseMessage);
        knowledgeBaseRepository.findArticleByKeywords(keywords, new RepositoryCallback<KnowledgeBaseArticle>() {
            @Override
            public void onSuccess(KnowledgeBaseArticle article) {
                if (article != null) {
                    future.complete(article.getAnswer());
                } else {
                    // 3. Fallback to OpenAI
                    chatRepository.sendMessageToAI(message, conversationId, 10, new RepositoryCallback<String>() {
                        @Override
                        public void onSuccess(String data) {
                            future.complete(data);
                        }

                        @Override
                        public void onError(String error) {
                            future.complete("Xin lỗi, tôi không thể trả lời câu hỏi của bạn lúc này.");
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                future.complete("Xin lỗi, đã có lỗi xảy ra với chatbot: " + error);
            }
        });

        return future;
    }

    private List<String> extractKeywords(String message) {
        // Simple keyword extraction. In a real app, this would be more sophisticated.
        return Arrays.asList(message.split("\\s+"));
    }
}
