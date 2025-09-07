package com.vhn.doan.presentation.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.repository.FirebaseVideoRepositoryImpl;
import com.vhn.doan.data.repository.VideoRepository;
import com.vhn.doan.presentation.base.BaseFragment;
import com.vhn.doan.presentation.video.CommentBottomSheetFragment;
import com.vhn.doan.presentation.video.VideoPresenter;
import com.vhn.doan.presentation.video.VideoView;
import com.vhn.doan.presentation.video.adapter.VideoAdapter;
import com.vhn.doan.utils.EventBus;

import java.util.List;
import java.util.Map;

/**
 * Fragment hiển thị video đã like theo kiểu có thể lướt giữa các video
 * Tương tự như VideoFragment nhưng dành riêng cho video đã like
 */
public class LikedVideosPlayerFragment extends BaseFragment implements VideoView {

    private static final String ARG_VIDEOS = "arg_videos";
    private static final String ARG_START_POSITION = "arg_start_position";

    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private PagerSnapHelper snapHelper;
    private List<ShortVideo> videos;
    private int currentPosition = 0;

    private FirebaseAuth firebaseAuth;
    private EventBus eventBus;
    private VideoPresenter presenter;
    private VideoRepository videoRepository;

    // Thêm import Observer để sử dụng
    private Observer<Map<String, Boolean>> videoLikeObserver;

    public static LikedVideosPlayerFragment newInstance(List<ShortVideo> videos, int startPosition) {
        LikedVideosPlayerFragment fragment = new LikedVideosPlayerFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_VIDEOS, (java.io.Serializable) videos);
        args.putInt(ARG_START_POSITION, startPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        eventBus = EventBus.getInstance();
        videoRepository = new FirebaseVideoRepositoryImpl();

        // Khởi tạo presenter để xử lý các tương tác video (like, comment, share)
        presenter = new VideoPresenter(videoRepository);
        presenter.attachView(this);

        if (getArguments() != null) {
            videos = (List<ShortVideo>) getArguments().getSerializable(ARG_VIDEOS);
            currentPosition = getArguments().getInt(ARG_START_POSITION, 0);
        }

        // Khởi tạo adapter với danh sách video đã like
        videoAdapter = new VideoAdapter();
        if (videos != null) {
            videoAdapter.updateVideos(videos);
        }

        setupVideoAdapterListener();
        // Chuyển setupEventBusListener() sang onViewCreated() để tránh lỗi lifecycle
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_liked_videos_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cập nhật danh sách video và user ID cho presenter để có thể xử lý like/comment/share
        if (presenter != null) {
            if (videos != null) {
                presenter.updateVideoList(videos);
            }
            // Set current user ID cho presenter
            if (firebaseAuth.getCurrentUser() != null) {
                presenter.setCurrentUserId(firebaseAuth.getCurrentUser().getUid());
            }
        }

        // Setup EventBus listener sau khi view đã được tạo
        setupEventBusListener();

        // *** FIX 1: Load trạng thái like cho tất cả video khi vào fragment ***
        loadVideoLikeStates();

        // Load dữ liệu sau khi view được tạo
        if (videos != null && !videos.isEmpty()) {
            // Scroll đến vị trí bắt đầu và phát video
            recyclerView.post(() -> {
                recyclerView.scrollToPosition(currentPosition);
                // Phát video tại vị trí bắt đầu sau khi scroll xong
                recyclerView.postDelayed(() -> {
                    videoAdapter.playVideoAt(currentPosition, recyclerView);
                    android.util.Log.d("LikedVideosPlayer", "Auto-playing video at start position: " + currentPosition);
                }, 300); // Delay nhẹ để đảm bảo scroll hoàn tất
            });
        }
    }

    @Override
    protected void initViews(View view) {
        recyclerView = view.findViewById(R.id.rv_liked_videos_player);
        setupRecyclerView();
    }

    @Override
    protected void setupListeners() {
        // Listeners đã được setup trong setupRecyclerView()
    }

    private void setupRecyclerView() {
        // Setup LinearLayoutManager theo chiều dọc
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // Setup PagerSnapHelper để snap từng video
        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        // Set adapter
        recyclerView.setAdapter(videoAdapter);
        videoAdapter.setRecyclerView(recyclerView);

        // Theo dõi scroll để cập nhật current position và quản lý video playback
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Get currently visible item and play video
                    View centerView = snapHelper.findSnapView(layoutManager);
                    if (centerView != null) {
                        int position = layoutManager.getPosition(centerView);
                        if (position != currentPosition) {
                            // Cập nhật vị trí hiện tại
                            currentPosition = position;
                            // Phát video tại vị trí mới
                            videoAdapter.playVideoAt(currentPosition, recyclerView);

                            android.util.Log.d("LikedVideosPlayer", "Playing video at position: " + currentPosition);
                        }
                    }
                }
            }
        });

        // Đảm bảo màn hình không sleep khi fragment được tạo
        if (getActivity() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void setupVideoAdapterListener() {
        if (videoAdapter == null) return;

        videoAdapter.setOnVideoInteractionListener(new VideoAdapter.OnVideoInteractionListener() {
            @Override
            public void onVideoClick(ShortVideo video, int position) {
                // Handle video click if needed
            }

            @Override
            public void onLikeClick(ShortVideo video, int position) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(getContext(), "Vui lòng đăng nhập để thích video", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Sử dụng presenter để xử lý like/unlike thay vì EventBus trực tiếp
                presenter.toggleLike(position);
            }

            @Override
            public void onShareClick(ShortVideo video, int position) {
                // Sử dụng presenter để xử lý share
                presenter.onShareClick(position);
            }

            @Override
            public void onCommentClick(ShortVideo video, int position) {
                // *** FIX 2: Triển khai chức năng comment thay vì hiển thị "sẽ phát triển trong tương lai" ***
                openCommentBottomSheet(video.getId());
            }

            @Override
            public void onVideoVisible(int position) {
                // Video hiện tại visible
                currentPosition = position;
            }

            @Override
            public void onVideoInvisible(int position) {
                // Video không còn visible
            }
        });
    }

    private void removeVideoFromList(int position) {
        if (videos != null && position >= 0 && position < videos.size()) {
            videos.remove(position);
            videoAdapter.updateVideos(videos);

            // Nếu không còn video nào, đóng activity
            if (videos.isEmpty()) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        }
    }

    /**
     * *** THÊM MỚI: Load trạng thái like cho tất cả video khi vào fragment ***
     */
    private void loadVideoLikeStates() {
        if (firebaseAuth.getCurrentUser() == null || videos == null || videos.isEmpty()) {
            return;
        }

        String currentUserId = firebaseAuth.getCurrentUser().getUid();

        for (int i = 0; i < videos.size(); i++) {
            ShortVideo video = videos.get(i);
            final int position = i;

            // Kiểm tra trạng thái like cho từng video
            videoRepository.isVideoLiked(video.getId(), currentUserId, new VideoRepository.BooleanCallback() {
                @Override
                public void onSuccess(boolean isLiked) {
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        // Cập nhật trạng thái like trong video object
                        video.setLiked(isLiked);

                        // Cập nhật UI trong adapter
                        if (videoAdapter != null) {
                            videoAdapter.updateVideoLikeStatus(position, isLiked);
                        }
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    android.util.Log.e("LikedVideosPlayer", "Error loading like status for video " + video.getId() + ": " + errorMessage);
                }
            });
        }
    }

    /**
     * *** THÊM MỚI: Hiển thị bottom sheet comment cho video (private helper method) ***
     */
    private void openCommentBottomSheet(String videoId) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        try {
            CommentBottomSheetFragment commentBottomSheet = CommentBottomSheetFragment.newInstance(videoId);
            commentBottomSheet.show(getChildFragmentManager(), "comment_bottom_sheet");
        } catch (Exception e) {
            android.util.Log.e("LikedVideosPlayer", "Error showing comment bottom sheet", e);
            Toast.makeText(getContext(), "Không thể hiển thị bình luận", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupEventBusListener() {
        // Lắng nghe sự thay đổi trạng thái like từ EventBus
        videoLikeObserver = likeStatusMap -> {
            if (likeStatusMap == null || likeStatusMap.isEmpty()) {
                return;
            }

            // Cập nhật trạng thái like cho các video trong adapter
            if (videos != null) {
                for (int i = 0; i < videos.size(); i++) {
                    ShortVideo video = videos.get(i);
                    Boolean isLiked = likeStatusMap.get(video.getId());
                    if (isLiked != null) {
                        video.setLiked(isLiked);
                        videoAdapter.updateVideoLikeStatus(i, isLiked);

                        // *** CẢI TIẾN: Nếu video bị unlike, remove khỏi danh sách liked videos ***
                        if (!isLiked) {
                            removeVideoFromList(i);
                            break; // Thoát loop vì danh sách đã thay đổi
                        }
                    }
                }
            }
        };

        if (getViewLifecycleOwner() != null) {
            eventBus.getVideoLikeStatusLiveData().observe(getViewLifecycleOwner(), videoLikeObserver);
        }
    }

    /**
     * Pause video hiện tại
     */
    public void pauseCurrentVideo() {
        if (videoAdapter != null) {
            videoAdapter.pauseAllVideos();
        }
    }

    /**
     * Resume video hiện tại
     */
    public void resumeCurrentVideo() {
        if (videoAdapter != null && recyclerView != null && currentPosition >= 0) {
            videoAdapter.playVideoAt(currentPosition, recyclerView);
        }
    }

    /**
     * Chuyển đến video tiếp theo
     */
    public void nextVideo() {
        if (videos != null && currentPosition < videos.size() - 1) {
            recyclerView.smoothScrollToPosition(currentPosition + 1);
        }
    }

    /**
     * Quay lại video trước đó
     */
    public void previousVideo() {
        if (currentPosition > 0) {
            recyclerView.smoothScrollToPosition(currentPosition - 1);
        }
    }

    /**
     * Lấy vị trí video hiện tại
     */
    public int getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Lấy tổng số video
     */
    public int getVideoCount() {
        return videos != null ? videos.size() : 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Đảm bảo màn hình không sleep
        if (getActivity() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        resumeCurrentVideo();
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseCurrentVideo();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Xóa flag keep screen on khi fragment bị destroy
        if (getActivity() != null) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // Release video adapter resources
        if (videoAdapter != null) {
            videoAdapter.releaseAllPlayers();
        }

        // Ngắt kết nối presenter khi view bị hủy
        presenter.detachView();
    }


    // Implementation đầy đủ các method từ VideoView interface
    @Override
    public void showLoadingVideos() {
        // Hiển thị loading state nếu cần
    }

    @Override
    public void hideLoadingVideos() {
        // Ẩn loading state nếu cần
    }

    @Override
    public void showVideoFeed(List<ShortVideo> videos) {
        // Cập nhật danh sách video
        this.videos = videos;
        videoAdapter.updateVideos(videos);

        // *** CẢI TIẾN: Load lại trạng thái like cho videos mới ***
        loadVideoLikeStates();
    }

    @Override
    public void showEmptyState() {
        // Hiển thị trạng thái rỗng nếu không có video
        Toast.makeText(getContext(), "Không có video nào", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void hideEmptyState() {
        // Ẩn trạng thái rỗng
    }

    @Override
    public void playVideoAtPosition(int position) {
        // Phát video tại vị trí chỉ định
        if (videoAdapter != null && recyclerView != null) {
            videoAdapter.playVideoAt(position, recyclerView);
        }
    }

    @Override
    public void updateVideoInfo(ShortVideo video, int position) {
        // Cập nhật thông tin video (view count, like count)
        if (videos != null && position >= 0 && position < videos.size()) {
            videos.set(position, video);
            videoAdapter.updateVideo(video, position);
        }
    }

    @Override
    public void updateVideoLikeStatus(int position, boolean isLiked) {
        // Cập nhật trạng thái like cho video tại vị trí
        if (videoAdapter != null) {
            videoAdapter.updateVideoLikeStatus(position, isLiked);
        }

        // *** CẢI TIẾN: Nếu video bị unlike trong danh sách liked videos, remove nó ***
        if (!isLiked && videos != null && position >= 0 && position < videos.size()) {
            removeVideoFromList(position);
        }
    }

    @Override
    public void revertVideoLikeUI(int position) {
        // Revert UI khi like operation thất bại
        if (videoAdapter != null) {
            videoAdapter.revertLikeUI(position);
        }
    }

    @Override
    public void showCommentBottomSheet(String videoId) {
        // *** FIX 2: Triển khai chức năng comment thay vì hiển thị "sẽ phát triển trong tương lai" ***
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        try {
            CommentBottomSheetFragment commentBottomSheet = CommentBottomSheetFragment.newInstance(videoId);
            commentBottomSheet.show(getChildFragmentManager(), "comment_bottom_sheet");
        } catch (Exception e) {
            android.util.Log.e("LikedVideosPlayer", "Error showing comment bottom sheet", e);
            Toast.makeText(getContext(), "Không thể hiển thị bình luận", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void shareVideo(ShortVideo video) {
        // Chia sẻ video - method này phải public theo interface
        try {
            android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                "Xem video này: " + video.getTitle() + "\n" +
                (video.getThumbnailUrl() != null ? video.getThumbnailUrl() : ""));
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Chia sẻ video");

            startActivity(android.content.Intent.createChooser(shareIntent, "Chia sẻ video"));
        } catch (Exception e) {
            android.util.Log.e("LikedVideosPlayerFragment", "Lỗi khi chia sẻ video", e);
            Toast.makeText(getContext(), "Không thể chia sẻ video", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showMessage(String message) {
        // Hiển thị thông báo ngắn
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void refreshVideoFeed() {
        // Refresh video feed - method bắt buộc từ VideoView interface
        if (videos != null && !videos.isEmpty()) {
            videoAdapter.updateVideos(videos);
        }
    }

    // Các methods không có trong VideoView interface - loại bỏ @Override
    public void showLikeError(int position) {
        // Hiển thị lỗi khi like/unlike thất bại
        if (videoAdapter != null) {
            videoAdapter.revertLikeUI(position);
        }
        Toast.makeText(getContext(), "Không thể cập nhật trạng thái thích", Toast.LENGTH_SHORT).show();
    }

    public void onVideoLiked(ShortVideo video) {
        // Xử lý khi video được like thành công
        eventBus.updateVideoLikeStatus(video.getId(), true);
    }

    public void onVideoUnliked(ShortVideo video) {
        // Xử lý khi video bị bỏ thích thành công - xóa khỏi danh sách liked videos
        eventBus.updateVideoLikeStatus(video.getId(), false);

        // Xóa video khỏi danh sách vì đây là màn hình liked videos
        for (int i = 0; i < videos.size(); i++) {
            if (videos.get(i).getId().equals(video.getId())) {
                removeVideoFromList(i);
                break;
            }
        }
    }

    public void onError(String message) {
        // Xử lý lỗi chung
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
