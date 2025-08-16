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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.repository.FirebaseVideoRepositoryImpl;
import com.vhn.doan.presentation.base.BaseFragment;
import com.vhn.doan.presentation.video.adapter.VideoAdapter;
import com.vhn.doan.utils.SharedPreferencesHelper;

import java.util.List;

/**
 * VideoFragment hiển thị feed video short theo kiểu TikTok/Instagram Reels
 * Tuân theo kiến trúc MVP và kế thừa từ BaseFragment
 */
public class VideoFragment extends BaseFragment implements VideoView {

    private RecyclerView recyclerView;
    private View loadingLayout;
    private View emptyLayout;
    private Button retryButton;
    private View snackbarAnchor;

    private VideoAdapter videoAdapter;
    private VideoPresenter presenter;
    private PagerSnapHelper snapHelper;
    private int currentVisiblePosition = 0;

    /**
     * Factory method để tạo instance mới
     */
    public static VideoFragment newInstance() {
        return new VideoFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sử dụng FirebaseVideoRepositoryImpl để lấy dữ liệu thực từ Firebase
        presenter = new VideoPresenter(new FirebaseVideoRepositoryImpl());
        presenter.attachView(this);

        // Khởi tạo adapter
        videoAdapter = new VideoAdapter();
        setupVideoAdapterListener();
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

        setupRecyclerView();

        // Set RecyclerView reference cho adapter
        videoAdapter.setRecyclerView(recyclerView);

        // Load video feed
        loadVideoFeed();
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
                // Gọi presenter để xử lý like/unlike
                presenter.toggleLike(position);
            }

            @Override
            public void onShareClick(ShortVideo video, int position) {
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
                // Có thể thêm logic khi video visible
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
        // Lấy user ID và country từ SharedPreferences
        String userId = SharedPreferencesHelper.getUserId(getContext());
        String country = SharedPreferencesHelper.getUserCountry(getContext());

        // Default country nếu chưa có
        if (country == null || country.isEmpty()) {
            country = "VN"; // Default to Vietnam
        }

        presenter.loadVideoFeed(userId, country);
    }

    // VideoView Interface Implementation

    @Override
    public void showVideoFeed(List<ShortVideo> videos) {
        // Đảm bảo chỉ cập nhật UI khi Fragment vẫn còn hoạt động
        if (getActivity() == null || !isAdded()) return;

        videoAdapter.updateVideos(videos);

        // Auto play first video sử dụng API mới
        if (!videos.isEmpty()) {
            currentVisiblePosition = 0;
            videoAdapter.playVideoAt(0, recyclerView);
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

    @Override
    public void updateVideoLikeStatus(int position, boolean isLiked) {
        if (videoAdapter != null) {
            videoAdapter.updateVideoLikeStatus(position, isLiked);
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

            String shareText = "Xem video: " + video.getTitle() + "\n" +
                             video.getCaption() + "\n\n" +
                             "Chia sẻ từ HealthTips App";

            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
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

        // Resume current video nếu có
        if (currentVisiblePosition >= 0 && recyclerView != null) {
            videoAdapter.playVideoAt(currentVisiblePosition, recyclerView);
        }

        // Giữ màn hình sáng khi fragment hiển thị
        if (getActivity() != null) {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
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
}
