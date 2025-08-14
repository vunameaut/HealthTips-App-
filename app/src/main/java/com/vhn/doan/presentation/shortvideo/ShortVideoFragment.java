package com.vhn.doan.presentation.shortvideo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.repository.ShortVideoRepositoryImpl;
import com.vhn.doan.data.repository.RepositoryCallback;
import com.vhn.doan.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị danh sách video ngắn theo phong cách TikTok/Facebook Reels
 * Sử dụng RecyclerView với PagerSnapHelper để tạo hiệu ứng vuốt video
 */
public class ShortVideoFragment extends Fragment implements ShortVideoContract.View {

    private RecyclerView recyclerViewVideos;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearProgressIndicator progressIndicatorLoading;
    private View viewLoading;
    private View viewEmptyState;

    private ShortVideoAdapter adapter;
    private ShortVideoPresenter presenter;
    private LinearLayoutManager layoutManager;

    private int currentPosition = 0;
    private boolean isFragmentVisible = false;
    private boolean isVideoPlayerReady = false;

    public static ShortVideoFragment newInstance() {
        return new ShortVideoFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo presenter
        SharedPreferencesHelper preferencesHelper = new SharedPreferencesHelper(requireContext());
        presenter = new ShortVideoPresenter(new ShortVideoRepositoryImpl(), preferencesHelper);
        presenter.attachView(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_short_video, container, false);
        initViews(view);
        setupRecyclerView();
        setupRefreshLayout();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<ShortVideo> cached = ShortVideoPreloadManager.getInstance().getCachedVideos();
        if (!cached.isEmpty()) {
            presenter.setInitialVideos(cached);
            showVideos(cached);
        } else {
            presenter.start();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isFragmentVisible = isVisibleToUser;
        // Chỉ xử lý khi fragment thực sự thay đổi trạng thái
        if (!isVisibleToUser) {
            // Fragment bị ẩn - dừng video ngay lập tức
            if (adapter != null) {
                adapter.pauseAllVideos();
                adapter.hideAllVideoViews();
            }
        }
        // Không gọi handleVideoPlayback() ở đây để tránh xung đột
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        isFragmentVisible = !hidden;
        // Chỉ xử lý khi fragment bị ẩn
        if (hidden) {
            if (adapter != null) {
                adapter.pauseAllVideos();
                adapter.hideAllVideoViews();
            }
        }
        // Không gọi handleVideoPlayback() ở đây để tránh xung đột
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFragmentVisible = false;
        if (adapter != null) {
            adapter.pauseAllVideos();
            adapter.hideAllVideoViews();
            adapter.releaseAllResources();
            recyclerViewVideos.setAdapter(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }

        // Release tất cả resources để tránh memory leak và vấn đề nháy video
        if (adapter != null) {
            adapter.releaseAllResources();
        }
        isVideoPlayerReady = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        isFragmentVisible = false;
        // Dừng và ẩn video NGAY LẬP TỨC không delay
        if (adapter != null) {
            adapter.pauseAllVideos();
            adapter.hideAllVideoViews();
            adapter.releaseAllResources();
        }
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        recyclerViewVideos.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        isFragmentVisible = true;
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        recyclerViewVideos.setVisibility(View.VISIBLE);
        // Không gọi handleVideoPlayback() ngay - để cho onVideoPlayerReady callback xử lý
    }

    @Override
    public void onStart() {
        super.onStart();
        isFragmentVisible = true;
        // Không làm gì cả để tránh xung đột
    }

    @Override
    public void onStop() {
        super.onStop();
        isFragmentVisible = false;
        // Dừng và ẩn video NGAY LẬP TỨC không delay
        if (adapter != null) {
            adapter.pauseAllVideos();
            adapter.hideAllVideoViews();
            adapter.releaseAllResources();
        }
    }

    /**
     * Xử lý logic phát/dừng video dựa trên trạng thái fragment
     * CHỈ được gọi từ onVideoPlayerReady callback
     */
    private void handleVideoPlayback() {
        if (adapter == null || !isVideoPlayerReady) return;

        if (isFragmentVisible) {
            // Fragment visible -> hiện video và tiếp tục phát
            adapter.showAllVideoViews();
            adapter.resumeCurrentVideo();
        } else {
            // Fragment không visible -> dừng video
            adapter.pauseCurrentVideo();
            adapter.hideAllVideoViews();
        }
    }

    private void initViews(View view) {
        recyclerViewVideos = view.findViewById(R.id.recyclerViewVideos);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressIndicatorLoading = view.findViewById(R.id.progressIndicatorLoading);
        viewLoading = view.findViewById(R.id.viewLoading);
        viewEmptyState = view.findViewById(R.id.viewEmptyState);
    }

    private void setupRecyclerView() {
        // Sử dụng LinearLayoutManager theo chiều dọc
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerViewVideos.setLayoutManager(layoutManager);
        // Giữ sẵn vài item kế tiếp để video chuyển mượt mà
        recyclerViewVideos.setItemViewCacheSize(5);

        // Khởi tạo adapter
        adapter = new ShortVideoAdapter(new ArrayList<>(), new ShortVideoAdapter.VideoInteractionListener() {
            @Override
            public void onVideoLiked(int position, String videoId, boolean isCurrentlyLiked) {
                presenter.onVideoLiked(position, videoId, isCurrentlyLiked);
            }

            @Override
            public void onVideoShared(int position, String videoId) {
                ShortVideo video = presenter.getVideoAt(position);
                if (video != null) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, video.getTitle());
                    shareIntent.putExtra(Intent.EXTRA_TEXT, video.getOptimizedVideoUrl());
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_video)));
                }
                presenter.onVideoShared(videoId);
            }

            @Override
            public void onVideoCommented(int position, String videoId) {
                showCommentDialog(videoId);
            }

            @Override
            public void onVideoViewed(int position, String videoId) {
                presenter.onVideoViewed(position, videoId);
            }

            @Override
            public void onVideoProfileClicked(int position, String userId) {
                // TODO: Navigate to user profile
                showSuccess(getString(R.string.view_user_profile));
            }

            @Override
            public void onVideoPlayerReady() {
                // Callback khi video player sẵn sàng
                isVideoPlayerReady = true;
                handleVideoPlayback();
            }
        });

        recyclerViewVideos.setAdapter(adapter);

        // Thêm PagerSnapHelper để tạo hiệu ứng snap giống TikTok
        final PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerViewVideos);

        // Lắng nghe sự kiện scroll để auto-play video
        recyclerViewVideos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Khi scroll dừng lại, sử dụng snapHelper để xác định item trung tâm
                    View snapView = snapHelper.findSnapView(layoutManager);
                    int newPosition = (snapView != null)
                            ? recyclerView.getChildAdapterPosition(snapView)
                            : RecyclerView.NO_POSITION;
                    if (newPosition != RecyclerView.NO_POSITION && newPosition != currentPosition) {
                        // Pause video cũ
                        adapter.pauseVideoAt(currentPosition);

                        // Play video mới (chỉ khi fragment visible)
                        currentPosition = newPosition;
                        if (isFragmentVisible && isVideoPlayerReady) {
                            // Delay playback until RecyclerView has completed layout for the new item
                            recyclerView.post(() -> adapter.playVideoAt(currentPosition));
                        }

                        // Thông báo presenter về việc xem video
                        ShortVideo video = presenter.getVideoAt(currentPosition);
                        if (video != null) {
                            presenter.onVideoViewed(currentPosition, video.getId());
                        }
                    }

                    // Khi người dùng xem đến video thứ 5 cuối cùng, preload thêm 10 video
                    int total = ShortVideoPreloadManager.getInstance().getCachedVideos().size();
                    if (newPosition >= total - 5) {
                        ShortVideoPreloadManager.getInstance().ensurePreloaded(total + 10, new RepositoryCallback<List<ShortVideo>>() {
                            @Override
                            public void onSuccess(List<ShortVideo> videos) {
                                int oldSize = adapter.getItemCount();
                                if (videos.size() > oldSize) {
                                    List<ShortVideo> newList = videos.subList(oldSize, videos.size());
                                    presenter.appendVideos(newList);
                                    adapter.addVideos(newList);
                                }
                            }

                            @Override
                            public void onError(String error) {
                                // Bỏ qua lỗi preload để không ảnh hưởng tới trải nghiệm hiện tại
                            }
                        });
                    }
                }
            }
        });
    }

    private void setupRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            presenter.refreshData();
        });

        // Thiết lập màu sắc cho refresh indicator theo Dark Mode theme
        swipeRefreshLayout.setColorSchemeResources(
            R.color.white,
            R.color.primary_green,
            R.color.accent_orange
        );

        // Thiết lập background color cho refresh indicator
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.black);
    }

    private void showCommentDialog(String videoId) {
        VideoCommentsBottomSheet sheet = VideoCommentsBottomSheet.newInstance(videoId);
        sheet.show(getChildFragmentManager(), "video_comments");
    }

    // Implement ShortVideoContract.View

    @Override
    public void showVideos(List<ShortVideo> videos) {
        viewEmptyState.setVisibility(View.GONE);
        recyclerViewVideos.setVisibility(View.VISIBLE);

        adapter.updateVideos(videos);

        // Auto-play video đầu tiên
        if (!videos.isEmpty() && currentPosition == 0) {
            recyclerViewVideos.post(() -> {
                adapter.playVideoAt(0);

                // Thông báo presenter về việc xem video đầu tiên
                presenter.onVideoViewed(0, videos.get(0).getId());
            });
        }
    }

    @Override
    public void showLoading() {
        // Hiển thị Linear Progress Indicator ở đầu màn hình
        progressIndicatorLoading.setVisibility(View.VISIBLE);

        // Chỉ hiển thị loading overlay nếu chưa có dữ liệu
        if (adapter == null || adapter.getItemCount() == 0) {
            viewLoading.setVisibility(View.VISIBLE);
            recyclerViewVideos.setVisibility(View.GONE);
        }

        viewEmptyState.setVisibility(View.GONE);
    }

    @Override
    public void hideLoading() {
        // Ẩn Linear Progress Indicator
        progressIndicatorLoading.setVisibility(View.GONE);

        // Ẩn loading overlay
        viewLoading.setVisibility(View.GONE);

        // Tắt refresh indicator nếu đang hiển thị
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

        // Hiển thị empty state nếu không có video nào
        if (adapter.getItemCount() == 0) {
            showEmptyState();
        }
    }

    @Override
    public void showEmptyState() {
        recyclerViewVideos.setVisibility(View.GONE);
        viewEmptyState.setVisibility(View.VISIBLE);
    }

    @Override
    public void updateVideoLike(int position, boolean isLiked, int newLikeCount) {
        adapter.updateVideoLike(position, isLiked, newLikeCount);
    }

    @Override
    public void updateVideoView(int position, int newViewCount) {
        adapter.updateVideoView(position, newViewCount);
    }

    @Override
    public void showSuccess(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void refreshVideoList() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void setPresenter(ShortVideoContract.Presenter presenter) {
        this.presenter = (ShortVideoPresenter) presenter;
    }

    /**
     * Public methods để các Activity/Fragment khác có thể tương tác
     */

    public void loadTrendingVideos() {
        if (presenter != null) {
            presenter.loadTrendingVideos();
        }
    }

    public void loadVideosByCategory(String categoryId) {
        if (presenter != null) {
            presenter.loadVideosByCategory(categoryId);
        }
    }

    public void filterByTag(String tag) {
        if (presenter != null) {
            presenter.filterVideosByTag(tag);
        }
    }
}
