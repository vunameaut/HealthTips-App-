package com.vhn.doan.presentation.video;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.repository.OfflineVideoRepositoryImpl;
import com.vhn.doan.presentation.base.BaseFragment;
import com.vhn.doan.presentation.base.FragmentVisibilityListener;
import com.vhn.doan.presentation.video.adapter.VideoAdapter;
import com.vhn.doan.utils.AnalyticsManager;
import com.vhn.doan.utils.EventBus;
import com.vhn.doan.utils.SharedPreferencesHelper;
import com.vhn.doan.utils.NetworkMonitor;

import java.util.List;
import java.util.Map;

/**
 * VideoFragment hiển thị feed video short theo kiểu TikTok/Instagram Reels
 * Tuân theo kiến trúc MVP và kế thừa từ BaseFragment
 * Implement FragmentVisibilityListener để kiểm soát video playback
 */
public class VideoFragment extends BaseFragment implements VideoView, FragmentVisibilityListener {

    private static final String TAG = "VideoFragment";

    private RecyclerView recyclerView;
    private View loadingLayout;
    private View emptyLayout;
    private Button retryButton;
    private View snackbarAnchor;

    private VideoAdapter videoAdapter;
    private VideoPresenter presenter;
    private PagerSnapHelper snapHelper;
    private int currentVisiblePosition = 0;

    // Firebase Authentication
    private FirebaseAuth firebaseAuth;

    // Analytics
    private AnalyticsManager analyticsManager;

    private Observer<Map<String, Boolean>> videoLikeObserver;
    private EventBus eventBus;

    // Network Monitor để theo dõi trạng thái mạng
    private NetworkMonitor networkMonitor;
    private boolean wasOffline = false;


    // Flag để kiểm soát video playback
    private boolean isFragmentVisible = false;
    private boolean isDataLoaded = false;
    private boolean shouldAutoPlayWhenVisible = false;

    /**
     * Factory method để tạo instance mới
     */
    public static VideoFragment newInstance() {
        return new VideoFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Khởi tạo Analytics Manager
        analyticsManager = AnalyticsManager.getInstance(requireContext());

        // Khởi tạo EventBus
        eventBus = EventBus.getInstance();

        // Sử dụng OfflineVideoRepositoryImpl để hỗ trợ offline mode (TikTok style)
        presenter = new VideoPresenter(new OfflineVideoRepositoryImpl(requireContext()));
        presenter.attachView(this);

        // Khởi tạo adapter
        videoAdapter = new VideoAdapter();
        setupVideoAdapterListener();

        // Khởi tạo NetworkMonitor để theo dõi trạng thái mạng
        networkMonitor = NetworkMonitor.getInstance(requireContext());
        networkMonitor.startMonitoring();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    @Override
    protected void initViews(View view) {
        recyclerView = view.findViewById(R.id.rv_videos);
        loadingLayout = view.findViewById(R.id.loading_layout);
        emptyLayout = view.findViewById(R.id.empty_layout);
        retryButton = view.findViewById(R.id.btn_retry);
        snackbarAnchor = view.findViewById(R.id.snackbar_anchor);
    }

    @Override
    protected void setupListeners() {
        setupVideoAdapterListener();
        setupRetryButton();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup network observer để theo dõi trạng thái mạng
        setupNetworkObserver();

        setupRecyclerView();

        // Set RecyclerView reference cho adapter
        videoAdapter.setRecyclerView(recyclerView);

        // Đăng ký lắng nghe sự kiện thay đổi trạng thái like từ EventBus
        registerLikeStatusObserver();

        // QUAN TRỌNG: KHÔNG load video ở đây
        // Video chỉ được load khi fragment được show thực sự (onFragmentVisible)
        android.util.Log.d(TAG, "⚠️ VideoFragment view created but NOT loading videos yet");
    }

    private void setupRecyclerView() {
        // Setup LinearLayoutManager for vertical scrolling
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(videoAdapter);

        // Add PagerSnapHelper for snapping to full screen
        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        // Listen for scroll changes to manage video playback
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Get currently visible item and play video
                    View centerView = snapHelper.findSnapView(layoutManager);
                    if (centerView != null) {
                        int position = layoutManager.getPosition(centerView);
                        if (position != currentVisiblePosition) {
                            currentVisiblePosition = position;
                            // Sử dụng API mới của VideoAdapter
                            videoAdapter.playVideoAt(position, recyclerView);
                            presenter.incrementViewCount(position);

                            // 📊 Log Analytics Event: Xem video
                            List<ShortVideo> videos = videoAdapter.getVideos();
                            if (analyticsManager != null && videos != null && position < videos.size()) {
                                ShortVideo video = videos.get(position);
                                analyticsManager.logVideoView(video.getId(), video.getTitle(), position);
                            }

                            // Kiểm tra và cập nhật trạng thái like của video hiện tại
                            presenter.checkLikeStatusForVideo(position);
                        }
                    }
                }
            }
        });
    }

    private void setupVideoAdapterListener() {
        videoAdapter.setOnVideoInteractionListener(new VideoAdapter.OnVideoInteractionListener() {
            @Override
            public void onVideoClick(ShortVideo video, int position) {
                // Có thể mở video detail hoặc pause/play
            }

            @Override
            public void onLikeClick(ShortVideo video, int position) {
                // 📊 Log Analytics Event: Like video
                if (analyticsManager != null && video != null) {
                    analyticsManager.logVideoLike(video.getId(), video.getTitle());
                }

                // Gọi presenter để xử lý like/unlike
                presenter.toggleLike(position);
            }

            @Override
            public void onShareClick(ShortVideo video, int position) {
                // 📊 Log Analytics Event: Share video
                if (analyticsManager != null && video != null) {
                    analyticsManager.logVideoShare(video.getId(), video.getTitle());
                }

                // Gọi presenter để xử lý share
                presenter.onShareClick(position);
            }

            @Override
            public void onCommentClick(ShortVideo video, int position) {
                // Gọi presenter để xử lý comment
                presenter.onCommentClick(position);
            }

            @Override
            public void onVideoVisible(int position) {
                currentVisiblePosition = position;
                // Kiểm tra trạng thái like của video khi nó trở nên visible
                presenter.checkLikeStatusForVideo(position);
            }

            @Override
            public void onVideoInvisible(int position) {
                // Có thể thêm logic khi video invisible
            }
        });
    }

    private void setupRetryButton() {
        retryButton.setOnClickListener(v -> {
            loadVideoFeed();
        });
    }

    private void loadVideoFeed() {
        // Lấy user ID từ Firebase Auth trước, sau đó từ SharedPreferences
        String userId = getCurrentUserId();
        String country = SharedPreferencesHelper.getUserCountry(getContext());

        // Default country nếu chưa có
        if (country == null || country.isEmpty()) {
            country = "VN"; // Default to Vietnam
        }

        presenter.loadVideoFeed(userId, country);
    }

    /**
     * Lấy User ID hiện tại từ Firebase Auth hoặc SharedPreferences
     */
    private String getCurrentUserId() {
        // Ưu tiên lấy từ Firebase Auth
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            // Đồng bộ với SharedPreferences
            SharedPreferencesHelper helper = new SharedPreferencesHelper(getContext());
            helper.setCurrentUserId(uid);
            return uid;
        }

        // Fallback về SharedPreferences
        return SharedPreferencesHelper.getUserId(getContext());
    }

    /**
     * Đăng ký lắng nghe sự kiện thay đổi trạng thái like từ EventBus
     */
    private void registerLikeStatusObserver() {
        if (videoLikeObserver != null) {
            return; // Tránh đăng ký nhiều lần
        }

        videoLikeObserver = likeStatusMap -> {
            if (videoAdapter == null || likeStatusMap == null || likeStatusMap.isEmpty()) {
                return;
            }

            List<ShortVideo> videos = videoAdapter.getVideos();
            if (videos == null || videos.isEmpty()) {
                return;
            }

            // Cập nhật trạng thái like cho các video trong adapter
            for (int i = 0; i < videos.size(); i++) {
                ShortVideo video = videos.get(i);
                Boolean isLiked = likeStatusMap.get(video.getId());
                if (isLiked != null) {
                    int position = i;
                    updateVideoLikeStatus(position, isLiked);
                }
            }
        };

        // Đăng ký lắng nghe sự kiện từ EventBus
        eventBus.getVideoLikeStatusLiveData().observe(getViewLifecycleOwner(), videoLikeObserver);
    }

    /**
     * Thiết lập observer cho network status
     */
    private void setupNetworkObserver() {
        if (networkMonitor != null) {
            networkMonitor.getConnectionStatus().observe(getViewLifecycleOwner(), isConnected -> {
                if (isConnected != null) {
                    android.util.Log.d(TAG, "🌐 Network status changed: " + (isConnected ? "ONLINE" : "OFFLINE"));

                    if (isConnected) {
                        // Có mạng trở lại
                        if (wasOffline) {
                            showMessage("✅ Đã kết nối lại mạng - Videos sẽ được cập nhật");
                            wasOffline = false;

                            // Reload video feed để sync với server
                            if (isFragmentVisible && presenter != null) {
                                android.util.Log.d(TAG, "📡 Reloading videos after network restored");
                                loadVideoFeed();
                            }
                        }
                    } else {
                        // Mất mạng
                        wasOffline = true;
                        showMessage("⚠️ Đang ở chế độ ngoại tuyến - Chỉ xem được videos đã cache");
                    }
                }
            });
        }
    }


    @Override
    public void onDestroy() {
        // EventBus observer sẽ tự động được hủy đăng ký nhờ getViewLifecycleOwner()
        super.onDestroy();
    }

    // VideoView Interface Implementation

    @Override
    public void showVideoFeed(List<ShortVideo> videos) {
        // Đảm bảo chỉ cập nhật UI khi Fragment vẫn còn hoạt động
        if (getActivity() == null || !isAdded()) return;

        videoAdapter.updateVideos(videos);
        isDataLoaded = true;

        // QUAN TRỌNG: CHỈ auto play nếu fragment đang visible
        if (!videos.isEmpty() && isFragmentVisible) {
            currentVisiblePosition = 0;
            videoAdapter.playVideoAt(0, recyclerView);
            android.util.Log.d(TAG, "▶️ Auto-playing first video because fragment is visible");
        } else if (!videos.isEmpty()) {
            // Nếu chưa visible, đánh dấu để phát sau khi visible
            shouldAutoPlayWhenVisible = true;
            android.util.Log.d(TAG, "⏸️ Videos loaded but fragment not visible - will auto-play when shown");
        }
    }

    @Override
    public void showError(String message) {
        // Đảm bảo Fragment vẫn còn hoạt động trước khi hiển thị Snackbar
        if (getView() != null && getActivity() != null && isAdded()) {
            Snackbar.make(snackbarAnchor, message, Snackbar.LENGTH_LONG)
                    .setAction("Thử lại", v -> loadVideoFeed())
                    .show();
        }
    }

    @Override
    public void showLoadingVideos() {
        // Đảm bảo Views tồn tại trước khi cập nhật visibility
        if (loadingLayout != null && recyclerView != null && emptyLayout != null) {
            loadingLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideLoadingVideos() {
        // Đảm bảo Views tồn tại trước khi cập nhật visibility
        if (loadingLayout != null && recyclerView != null) {
            loadingLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void playVideoAtPosition(int position) {
        // Sử dụng API mới của VideoAdapter
        videoAdapter.playVideoAt(position, recyclerView);

        // Scroll to position if needed
        if (Math.abs(currentVisiblePosition - position) > 1) {
            recyclerView.scrollToPosition(position);
        }

        currentVisiblePosition = position;
    }

    @Override
    public void pauseCurrentVideo() {
        videoAdapter.pauseAllVideos();
    }

    @Override
    public void updateVideoInfo(ShortVideo video, int position) {
        videoAdapter.updateVideo(video, position);
    }

    @Override
    public void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideEmptyState() {
        emptyLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void refreshVideoFeed() {
        presenter.refreshVideoFeed();
    }

    /**
     * Cập nhật trạng thái like của video
     */
    @Override
    public void updateVideoLikeStatus(int position, boolean isLiked) {
        if (videoAdapter != null) {
            videoAdapter.updateLikeStatus(position, isLiked);
        }
    }

    @Override
    public void revertVideoLikeUI(int position) {
        if (videoAdapter != null) {
            videoAdapter.revertLikeUI(position);
        }
    }

    @Override
    public void showCommentBottomSheet(String videoId) {
        if (videoId != null && !videoId.isEmpty()) {
            CommentBottomSheetFragment commentFragment = CommentBottomSheetFragment.newInstance(videoId);
            commentFragment.show(getChildFragmentManager(), "CommentBottomSheet");
        } else {
            showMessage("Không thể mở bình luận: Video ID không hợp lệ");
        }
    }

    @Override
    public void shareVideo(ShortVideo video) {
        shareVideoInternal(video);
    }

    @Override
    public void showMessage(String message) {
        if (getView() != null && getActivity() != null && isAdded()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    // Helper Methods

    private void shareVideoInternal(ShortVideo video) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            // Tạo nội dung share với deep link
            StringBuilder shareText = new StringBuilder();
            shareText.append("🎥 ").append(video.getTitle()).append("\n\n");

            // Thêm caption nếu có
            if (video.getCaption() != null && !video.getCaption().isEmpty()) {
                // Giới hạn caption tối đa 150 ký tự
                String caption = video.getCaption();
                if (caption.length() > 150) {
                    shareText.append(caption.substring(0, 150)).append("...");
                } else {
                    shareText.append(caption);
                }
                shareText.append("\n\n");
            }

            // Thêm deep link để mở video trong app
            shareText.append("📱 Mở trong app: healthtips://video/").append(video.getId());
            shareText.append("\n\n💚 Tải app HealthTips để xem thêm video sức khỏe hữu ích!");

            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ video"));

        } catch (Exception e) {
            showError("Không thể chia sẻ video");
        }
    }

    private void showComingSoon() {
        Toast.makeText(getContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
    }

    // Lifecycle Methods

    @Override
    public void onResume() {
        super.onResume();

        // Set user ID cho presenter
        String userId = SharedPreferencesHelper.getUserId(getContext());
        if (presenter != null && userId != null) {
            presenter.setCurrentUserId(userId);

            // Kiểm tra trạng thái like cho tất cả video hiện tại
            presenter.checkLikeStatusForVisibleVideos();
        }

        // QUAN TRỌNG: CHỈ resume video nếu fragment đang visible
        if (isFragmentVisible && currentVisiblePosition >= 0 && recyclerView != null) {
            videoAdapter.playVideoAt(currentVisiblePosition, recyclerView);
            android.util.Log.d(TAG, "▶️ Resumed video playback at position " + currentVisiblePosition);
        } else {
            android.util.Log.d(TAG, "⏸️ Fragment resumed but not visible - NOT playing video");
        }

        // Giữ màn hình sáng chỉ khi fragment visible
        if (isFragmentVisible && getActivity() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pause all videos when fragment is not visible - theo hướng dẫn API mới
        videoAdapter.pauseAllVideos();

        // Cho phép màn hình tắt khi fragment không còn hiển thị
        if (getActivity() != null) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Release all players khi hủy view - theo hướng dẫn API mới
        if (videoAdapter != null) {
            videoAdapter.releaseAllPlayers();
        }
    }


    // Public Methods for Navigation

    /**
     * Scroll to next video
     */
    public void nextVideo() {
        if (presenter != null) {
            presenter.nextVideo();
        }
    }

    /**
     * Scroll to previous video
     */
    public void previousVideo() {
        if (presenter != null) {
            presenter.previousVideo();
        }
    }

    /**
     * Get current video count
     */
    public int getVideoCount() {
        return presenter != null ? presenter.getVideoCount() : 0;
    }

    /**
     * Get current playing position
     */
    public int getCurrentPosition() {
        return presenter != null ? presenter.getCurrentPosition() : 0;
    }

    // ============================================================
    // FragmentVisibilityListener Implementation
    // ============================================================

    /**
     * Được gọi khi fragment được hiển thị (visible to user)
     * Đây là lúc load dữ liệu và phát video
     */
    @Override
    public void onFragmentVisible() {
        android.util.Log.d(TAG, "🟢 onFragmentVisible() called");
        isFragmentVisible = true;

        // Load dữ liệu lần đầu tiên khi fragment được show
        if (!isDataLoaded) {
            android.util.Log.d(TAG, "📥 Loading video feed for the first time...");
            loadVideoFeed();
        }
        // Nếu đã có dữ liệu và đang đợi để phát
        else if (shouldAutoPlayWhenVisible && videoAdapter != null) {
            android.util.Log.d(TAG, "▶️ Playing first video after becoming visible");
            videoAdapter.playVideoAt(currentVisiblePosition, recyclerView);
            shouldAutoPlayWhenVisible = false;
        }
        // Resume video hiện tại nếu đã có dữ liệu
        else if (isDataLoaded && currentVisiblePosition >= 0 && recyclerView != null) {
            android.util.Log.d(TAG, "▶️ Resuming video at position " + currentVisiblePosition);
            videoAdapter.playVideoAt(currentVisiblePosition, recyclerView);
        }

        // Giữ màn hình sáng
        if (getActivity() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * Được gọi khi fragment bị ẩn (hidden from user)
     * Pause tất cả video
     */
    @Override
    public void onFragmentHidden() {
        android.util.Log.d(TAG, "🔴 onFragmentHidden() called - pausing all videos");
        isFragmentVisible = false;

        // Pause tất cả video khi fragment bị ẩn
        if (videoAdapter != null) {
            videoAdapter.pauseAllVideos();
        }

        // Cho phép màn hình tắt
        if (getActivity() != null) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}
