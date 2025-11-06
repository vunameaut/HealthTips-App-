package com.vhn.doan.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * HttpClientManager - Singleton class quản lý OkHttpClient với caching
 * Cung cấp HTTP client có cache để tối ưu network requests
 */
public class HttpClientManager {
    private static final String TAG = "HttpClientManager";
    private static HttpClientManager instance;

    private final OkHttpClient httpClient;
    private final OkHttpClient httpClientNoCache;
    private final Context applicationContext;

    // Cache settings
    private static final long CACHE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final String CACHE_DIR = "http_cache";

    // Timeout settings
    private static final int CONNECT_TIMEOUT = 30; // seconds
    private static final int READ_TIMEOUT = 30; // seconds
    private static final int WRITE_TIMEOUT = 30; // seconds

    // Cache control settings
    private static final int MAX_AGE_ONLINE = 60; // 1 minute online cache
    private static final int MAX_STALE_OFFLINE = 7 * 24 * 60 * 60; // 7 days offline cache

    private HttpClientManager(Context context) {
        this.applicationContext = context.getApplicationContext();

        // Khởi tạo cache
        Cache cache = createCache();

        // Khởi tạo client với cache
        this.httpClient = createHttpClient(cache);

        // Khởi tạo client không cache (cho các request không cần cache)
        this.httpClientNoCache = createHttpClient(null);
    }

    public static synchronized HttpClientManager getInstance(Context context) {
        if (instance == null) {
            instance = new HttpClientManager(context);
        }
        return instance;
    }

    /**
     * Lấy HTTP client với cache
     */
    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Lấy HTTP client không cache (cho POST, PUT, DELETE requests)
     */
    public OkHttpClient getHttpClientNoCache() {
        return httpClientNoCache;
    }

    /**
     * Tạo Cache object
     */
    private Cache createCache() {
        try {
            File cacheDir = new File(applicationContext.getCacheDir(), CACHE_DIR);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            return new Cache(cacheDir, CACHE_SIZE);
        } catch (Exception e) {
            Log.e(TAG, "Failed to create cache", e);
            return null;
        }
    }

    /**
     * Tạo OkHttpClient với các interceptors
     */
    private OkHttpClient createHttpClient(Cache cache) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);

        // Thêm cache nếu có
        if (cache != null) {
            builder.cache(cache);

            // Thêm Online Cache Interceptor
            builder.addInterceptor(onlineCacheInterceptor());

            // Thêm Offline Cache Interceptor
            builder.addNetworkInterceptor(offlineCacheInterceptor());
        }

        // Thêm Logging Interceptor (chỉ trong debug mode)
        if (isDebugMode()) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }

        return builder.build();
    }

    /**
     * Online Cache Interceptor - Cache khi có internet
     */
    private Interceptor onlineCacheInterceptor() {
        return new Interceptor() {
            @NonNull
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());

                // Chỉ cache GET requests
                if (chain.request().method().equals("GET")) {
                    CacheControl cacheControl = new CacheControl.Builder()
                            .maxAge(MAX_AGE_ONLINE, TimeUnit.SECONDS)
                            .build();

                    return response.newBuilder()
                            .header("Cache-Control", cacheControl.toString())
                            .removeHeader("Pragma")
                            .build();
                }

                return response;
            }
        };
    }

    /**
     * Offline Cache Interceptor - Sử dụng cache khi mất internet
     */
    private Interceptor offlineCacheInterceptor() {
        return new Interceptor() {
            @NonNull
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {
                Request request = chain.request();

                // Kiểm tra network connectivity
                NetworkMonitor networkMonitor = NetworkMonitor.getInstance(applicationContext);

                if (!networkMonitor.isConnectedNow()) {
                    // Nếu không có internet, sử dụng cache cũ
                    CacheControl cacheControl = new CacheControl.Builder()
                            .maxStale(MAX_STALE_OFFLINE, TimeUnit.SECONDS)
                            .onlyIfCached()
                            .build();

                    request = request.newBuilder()
                            .cacheControl(cacheControl)
                            .build();
                }

                return chain.proceed(request);
            }
        };
    }

    /**
     * Kiểm tra debug mode
     */
    private boolean isDebugMode() {
        return (applicationContext.getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    /**
     * Clear cache
     */
    public void clearCache() {
        try {
            if (httpClient.cache() != null) {
                httpClient.cache().evictAll();
                Log.d(TAG, "Cache cleared successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear cache", e);
        }
    }

    /**
     * Lấy kích thước cache hiện tại (bytes)
     */
    public long getCacheSize() {
        try {
            if (httpClient.cache() != null) {
                return httpClient.cache().size();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get cache size", e);
        }
        return 0;
    }

    /**
     * Lấy số lượng requests đã cache
     */
    public int getCacheRequestCount() {
        try {
            if (httpClient.cache() != null) {
                return httpClient.cache().requestCount();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get cache request count", e);
        }
        return 0;
    }

    /**
     * Lấy số lượng network hits
     */
    public int getCacheNetworkCount() {
        try {
            if (httpClient.cache() != null) {
                return httpClient.cache().networkCount();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get cache network count", e);
        }
        return 0;
    }

    /**
     * Lấy số lượng cache hits
     */
    public int getCacheHitCount() {
        try {
            if (httpClient.cache() != null) {
                return httpClient.cache().hitCount();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get cache hit count", e);
        }
        return 0;
    }

    /**
     * Reset instance (cho testing)
     */
    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.clearCache();
        }
        instance = null;
    }
}
