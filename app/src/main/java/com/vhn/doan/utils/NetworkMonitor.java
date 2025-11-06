package com.vhn.doan.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * NetworkMonitor - Singleton class để giám sát trạng thái kết nối mạng
 * Sử dụng ConnectivityManager.NetworkCallback để theo dõi thay đổi
 * Cung cấp LiveData để các component khác observe
 */
public class NetworkMonitor {
    private static NetworkMonitor instance;
    private final ConnectivityManager connectivityManager;
    private final MutableLiveData<Boolean> isConnected = new MutableLiveData<>(true);
    private final MutableLiveData<NetworkType> networkType = new MutableLiveData<>(NetworkType.NONE);
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean isMonitoring = false;

    public enum NetworkType {
        WIFI,
        CELLULAR,
        ETHERNET,
        NONE
    }

    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            handler.post(() -> {
                isConnected.setValue(true);
                updateNetworkType();
            });
        }

        @Override
        public void onLost(@NonNull Network network) {
            handler.post(() -> {
                // Kiểm tra xem còn network nào khác không
                if (!hasActiveNetwork()) {
                    isConnected.setValue(false);
                    networkType.setValue(NetworkType.NONE);
                }
            });
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities capabilities) {
            handler.post(() -> {
                updateNetworkType();
            });
        }
    };

    private NetworkMonitor(Context context) {
        connectivityManager = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        // Khởi tạo trạng thái ban đầu
        updateConnectionStatus();
    }

    public static synchronized NetworkMonitor getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkMonitor(context);
        }
        return instance;
    }

    /**
     * Bắt đầu theo dõi kết nối mạng
     */
    public void startMonitoring() {
        if (isMonitoring) {
            return;
        }

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                .build();

        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
            isMonitoring = true;
            updateConnectionStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Dừng theo dõi kết nối mạng
     */
    public void stopMonitoring() {
        if (!isMonitoring) {
            return;
        }

        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
            isMonitoring = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Lấy LiveData để observe trạng thái kết nối
     */
    public LiveData<Boolean> getConnectionStatus() {
        return isConnected;
    }

    /**
     * Lấy LiveData để observe loại mạng
     */
    public LiveData<NetworkType> getNetworkType() {
        return networkType;
    }

    /**
     * Kiểm tra trạng thái kết nối hiện tại (synchronous)
     */
    public boolean isConnectedNow() {
        return hasActiveNetwork();
    }

    /**
     * Kiểm tra loại mạng hiện tại
     */
    public NetworkType getCurrentNetworkType() {
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) {
            return NetworkType.NONE;
        }

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        if (capabilities == null) {
            return NetworkType.NONE;
        }

        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return NetworkType.WIFI;
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return NetworkType.CELLULAR;
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return NetworkType.ETHERNET;
        }

        return NetworkType.NONE;
    }

    /**
     * Kiểm tra xem có kết nối mạng active không
     */
    private boolean hasActiveNetwork() {
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) {
            return false;
        }

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    /**
     * Cập nhật trạng thái kết nối
     */
    private void updateConnectionStatus() {
        boolean connected = hasActiveNetwork();
        isConnected.postValue(connected);

        if (connected) {
            updateNetworkType();
        } else {
            networkType.postValue(NetworkType.NONE);
        }
    }

    /**
     * Cập nhật loại mạng
     */
    private void updateNetworkType() {
        NetworkType type = getCurrentNetworkType();
        networkType.postValue(type);
    }

    /**
     * Kiểm tra có phải WiFi không
     */
    public boolean isWiFi() {
        return getCurrentNetworkType() == NetworkType.WIFI;
    }

    /**
     * Kiểm tra có phải Cellular không
     */
    public boolean isCellular() {
        return getCurrentNetworkType() == NetworkType.CELLULAR;
    }

    /**
     * Reset instance (dùng cho testing)
     */
    public static synchronized void resetInstance() {
        if (instance != null && instance.isMonitoring) {
            instance.stopMonitoring();
        }
        instance = null;
    }
}
