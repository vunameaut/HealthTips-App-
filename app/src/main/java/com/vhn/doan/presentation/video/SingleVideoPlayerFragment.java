package com.vhn.doan.presentation.video;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.repository.FirebaseVideoRepositoryImpl;
import com.vhn.doan.data.repository.VideoRepository;
import com.vhn.doan.utils.CloudinaryUrls;
import com.vhn.doan.utils.EventBus;
import com.vhn.doan.utils.FirebaseAuthHelper;

/**
 * Fragment phát video đơn lẻ từ search results
 * Không cho phép lướt xuống các video khác
 */
public class SingleVideoPlayerFragment extends Fragment {

    private static final String ARG_VIDEO_ID = "video_id";

    private String videoId;
    private ShortVideo currentVideo;
    private ExoPlayer player;

    // Views
    private StyledPlayerView playerView;
    private ProgressBar progressLoading;
    private TextView tvVideoTitle;
    private TextView tvVideoCaption;
    private TextView tvLikeCount;
    private TextView tvViewCount;
    private ImageButton btnLike;
    private ImageButton btnComment;
    private ImageButton btnShare;

    // Dependencies
    private VideoRepository videoRepository;
    private FirebaseAuthHelper authHelper;
    private EventBus eventBus;

    public static SingleVideoPlayerFragment newInstance(String videoId) {
        SingleVideoPlayerFragment fragment = new SingleVideoPlayerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_ID, videoId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            videoId = getArguments().getString(ARG_VIDEO_ID);
        }

        // Khởi tạo dependencies
        videoRepository = new FirebaseVideoRepositoryImpl();
        authHelper = new FirebaseAuthHelper();
        eventBus = EventBus.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_single_video_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupPlayer();

        if (videoId != null) {
            loadVideoData();
        }
    }

    private void initViews(View view) {
        playerView = view.findViewById(R.id.player_view);
        progressLoading = view.findViewById(R.id.progress_loading);
        tvVideoTitle = view.findViewById(R.id.tv_video_title);
        tvVideoCaption = view.findViewById(R.id.tv_video_caption);
        tvLikeCount = view.findViewById(R.id.tv_like_count);
        tvViewCount = view.findViewById(R.id.tv_view_count);
        btnLike = view.findViewById(R.id.btn_like);
        btnComment = view.findViewById(R.id.btn_comment);
        btnShare = view.findViewById(R.id.btn_share);

        setupClickListeners();
    }

    private void setupClickListeners() {
        if (btnLike != null) {
            btnLike.setOnClickListener(v -> toggleLike());
        }

        if (btnComment != null) {
            btnComment.setOnClickListener(v -> {
                // TODO: Mở màn hình comment
                Toast.makeText(getContext(), "Tính năng bình luận đang được phát triển", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(v -> shareVideo());
        }
    }

    private void setupPlayer() {
        if (getContext() != null) {
            player = new ExoPlayer.Builder(getContext()).build();
            playerView.setPlayer(player);

            // Thiết lập listener cho player
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == Player.STATE_BUFFERING) {
                        showLoading(true);
                    } else if (playbackState == Player.STATE_READY) {
                        showLoading(false);
                        // Tăng view count khi video sẵn sàng phát
                        incrementViewCount();
                    }
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    showLoading(false);
                    Toast.makeText(getContext(), "Lỗi phát video", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadVideoData() {
        showLoading(true);

        videoRepository.getVideoById(videoId, new VideoRepository.SingleVideoCallback() {
            @Override
            public void onSuccess(ShortVideo video) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        currentVideo = video;
                        displayVideoInfo();
                        playVideo();
                        showLoading(false);
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(getContext(), "Không thể tải video: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void displayVideoInfo() {
        if (currentVideo == null) return;

        // Hiển thị thông tin video
        if (tvVideoTitle != null) {
            tvVideoTitle.setText(currentVideo.getTitle());
        }

        if (tvVideoCaption != null) {
            tvVideoCaption.setText(currentVideo.getCaption());
        }

        updateLikeButton();
        updateCounts();
    }

    private void playVideo() {
        if (currentVideo == null || player == null) return;

        String videoUrl = getVideoUrl();
        if (videoUrl != null) {
            MediaItem mediaItem = MediaItem.fromUri(videoUrl);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.setPlayWhenReady(true);
        }
    }

    private String getVideoUrl() {
        if (currentVideo == null) return null;

        // ShortVideo không có method getVideoUrl(), chỉ có Cloudinary public ID
        if (currentVideo.getCldPublicId() != null && !currentVideo.getCldPublicId().isEmpty()) {
            return CloudinaryUrls.mp4(currentVideo.getCldPublicId(), currentVideo.getCldVersion());
        }

        return null;
    }

    private void toggleLike() {
        if (currentVideo == null || authHelper == null) return;

        String userId = authHelper.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để thích video", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean newLikeState = !currentVideo.isLiked();

        // Sử dụng likeVideo hoặc unlikeVideo thay vì toggleVideoLike
        if (newLikeState) {
            videoRepository.likeVideo(currentVideo.getId(), userId, new VideoRepository.BooleanCallback() {
                @Override
                public void onSuccess(boolean result) {
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            currentVideo.setLiked(true);
                            currentVideo.setLikeCount(currentVideo.getLikeCount() + 1);
                            updateLikeButton();
                            updateCounts();

                            // Cập nhật EventBus để đồng bộ với các màn hình khác
                            eventBus.updateVideoLikeStatus(currentVideo.getId(), true);
                        });
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        } else {
            videoRepository.unlikeVideo(currentVideo.getId(), userId, new VideoRepository.BooleanCallback() {
                @Override
                public void onSuccess(boolean result) {
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            currentVideo.setLiked(false);
                            currentVideo.setLikeCount(Math.max(0, currentVideo.getLikeCount() - 1));
                            updateLikeButton();
                            updateCounts();

                            // Cập nhật EventBus để đồng bộ với các màn hình khác
                            eventBus.updateVideoLikeStatus(currentVideo.getId(), false);
                        });
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        }
    }

    private void updateLikeButton() {
        if (btnLike == null || currentVideo == null) return;

        if (currentVideo.isLiked()) {
            btnLike.setImageResource(R.drawable.ic_heart_filled);
            btnLike.setColorFilter(ContextCompat.getColor(requireContext(), R.color.like_color_active));
        } else {
            btnLike.setImageResource(R.drawable.ic_heart_outline);
            btnLike.setColorFilter(ContextCompat.getColor(requireContext(), R.color.like_color_inactive));
        }
    }

    private void updateCounts() {
        if (currentVideo == null) return;

        if (tvLikeCount != null) {
            tvLikeCount.setText(String.valueOf(currentVideo.getLikeCount()));
        }

        if (tvViewCount != null) {
            tvViewCount.setText(String.valueOf(currentVideo.getViewCount()));
        }
    }

    private void incrementViewCount() {
        if (currentVideo == null) return;

        videoRepository.incrementViewCount(currentVideo.getId(), new VideoRepository.BooleanCallback() {
            @Override
            public void onSuccess(boolean result) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        // Tăng view count locally
                        currentVideo.setViewCount(currentVideo.getViewCount() + 1);
                        updateCounts();
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Không hiển thị lỗi cho view count vì đây không phải là tính năng quan trọng
            }
        });
    }

    private void shareVideo() {
        if (currentVideo == null) return;

        // TODO: Implement share functionality
        Toast.makeText(getContext(), "Tính năng chia sẻ đang được phát triển", Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean show) {
        if (progressLoading != null) {
            progressLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public void pauseVideo() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    public void resumeVideo() {
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    public void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeVideo();
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseVideo();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releasePlayer();
    }
}
