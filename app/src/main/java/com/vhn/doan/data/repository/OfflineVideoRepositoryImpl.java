package com.vhn.doan.data.repository;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.local.AppDatabase;
import com.vhn.doan.data.local.dao.VideoDao;
import com.vhn.doan.data.local.entity.VideoEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Repository wrapper hỗ trợ offline mode cho Videos
 * Kiểu TikTok: Cache videos khi online để xem khi offline
 */
public class OfflineVideoRepositoryImpl implements VideoRepository {

    private static final String TAG = "OfflineVideoRepo";
    private static final long CACHE_EXPIRY_MS = 7 * 24 * 60 * 60 * 1000L; // 7 ngày

    private final VideoRepository firebaseRepo;
    private final VideoDao videoDao;
    private final Context context;
    private final Executor executor;

    // Cache in-memory để giảm database access
    private List<VideoEntity> memoryCache;
    private long lastCacheTime = 0;

    public OfflineVideoRepositoryImpl(Context context) {
        this.context = context.getApplicationContext();
        this.firebaseRepo = new FirebaseVideoRepositoryImpl();
        this.videoDao = AppDatabase.getInstance(context).videoDao();
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void getFeed(String userId, String country, VideoCallback callback) {
        boolean isOnline = isNetworkAvailable();
        Log.d(TAG, "getFeed called - Network: " + (isOnline ? "ONLINE" : "OFFLINE"));

        // 1. Load cache ngay lập tức (nếu có) để UI nhanh
        loadCacheAsync(callback, isOnline);

        // 2. Nếu online, fetch từ Firebase và update cache
        if (isOnline) {
            fetchFromFirebaseAndCache(userId, country, callback);
        } else {
            Log.d(TAG, "OFFLINE mode - using cache only");
        }
    }

    /**
     * Load cache asynchronously
     */
    private void loadCacheAsync(VideoCallback callback, boolean isOnline) {
        executor.execute(() -> {
            try {
                // Kiểm tra memory cache trước
                if (memoryCache != null && !memoryCache.isEmpty() &&
                    (System.currentTimeMillis() - lastCacheTime < 60000)) { // 1 phút
                    Log.d(TAG, "✅ Using MEMORY cache: " + memoryCache.size() + " videos");
                    List<ShortVideo> videos = convertToShortVideos(memoryCache);
                    runOnMainThread(() -> callback.onSuccess(videos));
                    return;
                }

                // Load từ Room database
                List<VideoEntity> cachedVideos = videoDao.getAllVideosSync();

                if (cachedVideos != null && !cachedVideos.isEmpty()) {
                    Log.d(TAG, "✅ Cache loaded from Room: " + cachedVideos.size() + " videos");

                    // Update memory cache
                    memoryCache = cachedVideos;
                    lastCacheTime = System.currentTimeMillis();

                    List<ShortVideo> videos = convertToShortVideos(cachedVideos);
                    runOnMainThread(() -> {
                        callback.onSuccess(videos);
                        Log.d(TAG, "▶️ Cache displayed to UI");
                    });
                } else {
                    Log.d(TAG, "⚠️ No cache available");
                    if (!isOnline) {
                        // 🎯 FIX: Trả về empty list thay vì error khi offline không có cache
                        // Điều này cho phép UI hiển thị empty state thay vì error
                        runOnMainThread(() -> {
                            Log.d(TAG, "📭 Offline with no cache - returning empty list");
                            callback.onSuccess(new java.util.ArrayList<>());
                        });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error loading cache", e);
                if (!isOnline) {
                    // 🎯 FIX: Trả về empty list thay vì error
                    runOnMainThread(() -> {
                        Log.d(TAG, "📭 Cache error + offline - returning empty list");
                        callback.onSuccess(new java.util.ArrayList<>());
                    });
                }
            }
        });
    }

    /**
     * Fetch từ Firebase và cache
     */
    private void fetchFromFirebaseAndCache(String userId, String country, VideoCallback callback) {
        Log.d(TAG, "📡 Fetching from Firebase...");

        firebaseRepo.getFeed(userId, country, new VideoCallback() {
            @Override
            public void onSuccess(List<ShortVideo> videos) {
                Log.d(TAG, "✅ Firebase loaded: " + videos.size() + " videos");

                // Callback to UI
                callback.onSuccess(videos);

                // Cache in background
                cacheVideosAsync(videos);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "❌ Firebase error: " + errorMessage);
                // Không callback error nếu đã có cache
                // Chỉ callback error nếu chưa có data nào
                if (memoryCache == null || memoryCache.isEmpty()) {
                    callback.onError(errorMessage);
                }
            }
        });
    }

    /**
     * Cache videos to Room database
     */
    private void cacheVideosAsync(List<ShortVideo> videos) {
        executor.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                List<VideoEntity> entities = new ArrayList<>();

                for (ShortVideo video : videos) {
                    VideoEntity entity = convertToEntity(video);
                    entity.setCachedAt(now);
                    entities.add(entity);
                }

                // Clear old cache và insert mới
                videoDao.deleteAll();
                videoDao.insertAll(entities);

                // Update memory cache
                memoryCache = entities;
                lastCacheTime = now;

                Log.d(TAG, "💾 Cached " + entities.size() + " videos to database");

                // Cleanup old entries
                long expiryTime = now - CACHE_EXPIRY_MS;
                videoDao.deleteOldVideos(expiryTime);

            } catch (Exception e) {
                Log.e(TAG, "❌ Error caching videos", e);
            }
        });
    }

    @Override
    public List<ShortVideo> getFeed(String userId, String country) {
        // Synchronous version - try cache first, then Firebase
        try {
            List<VideoEntity> cached = videoDao.getAllVideosSync();
            if (cached != null && !cached.isEmpty()) {
                return convertToShortVideos(cached);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading sync cache", e);
        }

        // Fallback to Firebase (blocking)
        return firebaseRepo.getFeed(userId, country);
    }

    // ==================== DELEGATE TO FIREBASE REPO ====================

    @Override
    public void getVideoById(String videoId, SingleVideoCallback callback) {
        firebaseRepo.getVideoById(videoId, callback);
    }

    @Override
    public void likeVideo(String videoId, String userId, BooleanCallback callback) {
        firebaseRepo.likeVideo(videoId, userId, callback);
    }

    @Override
    public void unlikeVideo(String videoId, String userId, BooleanCallback callback) {
        firebaseRepo.unlikeVideo(videoId, userId, callback);
    }

    @Override
    public void isVideoLiked(String videoId, String userId, BooleanCallback callback) {
        firebaseRepo.isVideoLiked(videoId, userId, callback);
    }

    @Override
    public void listenToVideoLikeStatus(String videoId, String userId, BooleanCallback callback) {
        firebaseRepo.listenToVideoLikeStatus(videoId, userId, callback);
    }

    @Override
    public void getVideoComments(String videoId, CommentsCallback callback) {
        firebaseRepo.getVideoComments(videoId, callback);
    }

    @Override
    public void listenToVideoComments(String videoId, CommentsCallback callback) {
        firebaseRepo.listenToVideoComments(videoId, callback);
    }

    @Override
    public void getCommentReplies(String videoId, String parentCommentId, CommentsCallback callback) {
        firebaseRepo.getCommentReplies(videoId, parentCommentId, callback);
    }

    @Override
    public void listenToCommentReplies(String videoId, String parentCommentId, CommentsCallback callback) {
        firebaseRepo.listenToCommentReplies(videoId, parentCommentId, callback);
    }

    @Override
    public void addComment(String videoId, String userId, String text, String parentId, CommentCallback callback) {
        firebaseRepo.addComment(videoId, userId, text, parentId, callback);
    }

    @Override
    public void likeComment(String videoId, String commentId, String userId, BooleanCallback callback) {
        firebaseRepo.likeComment(videoId, commentId, userId, callback);
    }

    @Override
    public void unlikeComment(String videoId, String commentId, String userId, BooleanCallback callback) {
        firebaseRepo.unlikeComment(videoId, commentId, userId, callback);
    }

    @Override
    public void isCommentLiked(String videoId, String commentId, String userId, BooleanCallback callback) {
        firebaseRepo.isCommentLiked(videoId, commentId, userId, callback);
    }

    @Override
    public void incrementViewCount(String videoId, BooleanCallback callback) {
        firebaseRepo.incrementViewCount(videoId, callback);
    }

    @Override
    public void getLikedVideos(String userId, VideoCallback callback) {
        // Delegate to Firebase repo
        firebaseRepo.getLikedVideos(userId, callback);
    }

    @Override
    public void trackVideoView(String videoId, String userId) {
        // Delegate to Firebase repo
        firebaseRepo.trackVideoView(videoId, userId);
    }

    @Override
    public void trackVideoInteraction(String videoId, String userId, String interactionType, long watchTimeMs) {
        // Delegate to Firebase repo
        firebaseRepo.trackVideoInteraction(videoId, userId, interactionType, watchTimeMs);
    }

    @Override
    public void cleanup() {
        // Delegate to Firebase repo để cleanup listeners
        firebaseRepo.cleanup();

        // Clear memory cache khi cleanup
        if (memoryCache != null) {
            memoryCache.clear();
            memoryCache = null;
        }
        lastCacheTime = 0;

        Log.d(TAG, "🧹 Cleanup completed - cleared memory cache");
    }

    // ==================== HELPER METHODS ====================

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking network", e);
        }
        return false;
    }

    private void runOnMainThread(Runnable runnable) {
        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        mainHandler.post(runnable);
    }

    private List<ShortVideo> convertToShortVideos(List<VideoEntity> entities) {
        List<ShortVideo> videos = new ArrayList<>();
        for (VideoEntity entity : entities) {
            videos.add(convertToShortVideo(entity));
        }
        return videos;
    }

    private ShortVideo convertToShortVideo(VideoEntity entity) {
        ShortVideo video = new ShortVideo();
        video.setId(entity.getId());
        video.setTitle(entity.getTitle());
        video.setCaption(entity.getCaption());
        video.setThumbnailUrl(entity.getThumbnailUrl());
        video.setUserId(entity.getUploaderId());
        video.setUploadDate(entity.getUploadDate());
        video.setViewCount(entity.getViewCount());
        video.setLikeCount(entity.getLikeCount());
        video.setCommentCount(entity.getCommentCount());
        video.setDuration(entity.getDuration());
        video.setStatus("ready");
        video.setLiked(entity.isLiked());

        // 🎯 FIX: Ưu tiên set cldPublicId từ cache
        if (entity.getCldPublicId() != null && !entity.getCldPublicId().isEmpty()) {
            video.setCldPublicId(entity.getCldPublicId());
            Log.d(TAG, "✅ Restored cldPublicId from cache: " + entity.getCldPublicId());
        } else if (entity.getVideoUrl() != null && entity.getVideoUrl().contains("cloudinary.com")) {
            // Fallback: Extract từ videoUrl nếu không có cldPublicId
            String publicId = extractPublicIdFromUrl(entity.getVideoUrl());
            if (publicId != null) {
                video.setCldPublicId(publicId);
                Log.d(TAG, "📤 Extracted cldPublicId from URL: " + publicId);
            }
        }

        // Set videoUrl cho offline fallback (khi không có network)
        if (entity.getVideoUrl() != null && !entity.getVideoUrl().isEmpty()) {
            video.setVideoUrl(entity.getVideoUrl());
        }

        return video;
    }

    private VideoEntity convertToEntity(ShortVideo video) {
        VideoEntity entity = new VideoEntity();
        entity.setId(video.getId());
        entity.setTitle(video.getTitle());
        entity.setCaption(video.getCaption());

        // 🎯 Lưu cldPublicId để generate URL khi cần
        entity.setCldPublicId(video.getCldPublicId());

        // Lưu videoUrl (được generate từ getVideoUrl())
        entity.setVideoUrl(video.getVideoUrl());

        entity.setThumbnailUrl(video.getThumbnailUrl());
        entity.setUploaderId(video.getUserId());
        entity.setUploaderName(""); // ShortVideo không có field này
        entity.setUploaderAvatar(""); // ShortVideo không có field này
        entity.setUploadDate(video.getUploadDate());
        entity.setViewCount(video.getViewCount());
        entity.setLikeCount(video.getLikeCount());
        entity.setCommentCount(video.getCommentCount());
        entity.setShareCount(0L); // ShortVideo không có field này
        entity.setDuration(video.getDuration());
        entity.setLiked(video.isLiked());
        entity.setCachedAt(System.currentTimeMillis());

        return entity;
    }

    /**
     * Extract Cloudinary public ID từ URL
     */
    private String extractPublicIdFromUrl(String url) {
        if (url == null || !url.contains("cloudinary.com")) {
            return null;
        }
        try {
            // URL format: https://res.cloudinary.com/healthtips/video/upload/PUBLIC_ID
            String[] parts = url.split("/upload/");
            if (parts.length > 1) {
                return parts[1].split("\\.")[0]; // Remove extension
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting public ID", e);
        }
        return null;
    }
}

