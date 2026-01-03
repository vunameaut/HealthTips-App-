package com.vhn.doan.presentation.video;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
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

import java.util.List;

/**
 * Fragment phát video đơn lẻ từ search results
 * Giao diện giống y hệt Video Short Fragment với đầy đủ tính năng tương tác
 */
public class SingleVideoPlayerFragment extends Fragment {

    private static final String ARG_VIDEO_ID = "video_id";
    private static final String ARG_VIDEO_OBJECT = "video_object";
    private static final String ARG_VIDEO_LIST = "video_list";
    private static final String ARG_START_POSITION = "start_position";

    private String videoId;
    private ShortVideo currentVideo;
    private List<ShortVideo> videoList;
    private int currentPosition = 0;
    private ExoPlayer player;
    private boolean isPlaying = false;

    // Views - giống y hệt Video Short Fragment
    private StyledPlayerView playerView;
    private ImageView ivPoster;
    private View videoTapArea;
    private ImageView playPauseOverlay;
    private ProgressBar progressLoading;
    private ImageView ivDoubleTapHeart;
    private TextView tvVideoTitle;
    private TextView tvVideoCaption;
    private TextView tvSeeMore;
    private TextView tvViewCount;
    private TextView tvUploadDate;
    private LinearLayout btnLike;
    private ImageView ivLikeIcon;
    private TextView tvLikeCount;
    private LinearLayout btnComment;
    private LinearLayout btnShare;

    // Fast forward/rewind indicators
    private TextView tvFastForward;
    private TextView tvRewind;
    private boolean isSeeking = false;

    // UI containers for visibility toggle
    private LinearLayout layoutVideoInfo;
    private LinearLayout layoutActionButtons;
    private boolean isUIVisible = true;

    // Dependencies
    private VideoRepository videoRepository;
    private FirebaseAuthHelper authHelper;
    private EventBus eventBus;
    private GestureDetector gestureDetector;
    private GestureDetector swipeGestureDetector; // Thêm gesture detector cho swipe
    private Handler handler = new Handler(Looper.getMainLooper());

    public static SingleVideoPlayerFragment newInstance(String videoId) {
        SingleVideoPlayerFragment fragment = new SingleVideoPlayerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_ID, videoId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Factory method mới nhận video object đầy đủ để đảm bảo trạng thái like được hiển thị chính xác
     */
    public static SingleVideoPlayerFragment newInstance(String videoId, ShortVideo videoObject) {
        SingleVideoPlayerFragment fragment = new SingleVideoPlayerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_ID, videoId);
        args.putSerializable(ARG_VIDEO_OBJECT, videoObject);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Factory method cho danh sách video từ LikedVideosFragment
     * Hỗ trợ swipe để chuyển video như TikTok
     */
    public static SingleVideoPlayerFragment newInstance(List<ShortVideo> videos, int startPosition) {
        SingleVideoPlayerFragment fragment = new SingleVideoPlayerFragment();
        Bundle args = new Bundle();

        if (videos != null && !videos.isEmpty() && startPosition >= 0 && startPosition < videos.size()) {
            // Lưu danh sách video và vị trí bắt đầu
            args.putSerializable(ARG_VIDEO_LIST, new java.util.ArrayList<>(videos));
            args.putInt(ARG_START_POSITION, startPosition);

            // Lưu video hiện tại để hiển thị ngay lập tức
            ShortVideo currentVideo = videos.get(startPosition);
            args.putString(ARG_VIDEO_ID, currentVideo.getId());
            args.putSerializable(ARG_VIDEO_OBJECT, currentVideo);
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            videoId = getArguments().getString(ARG_VIDEO_ID);
            currentVideo = (ShortVideo) getArguments().getSerializable(ARG_VIDEO_OBJECT);

            // Xử lý danh sách video từ LikedVideosFragment
            videoList = (List<ShortVideo>) getArguments().getSerializable(ARG_VIDEO_LIST);
            currentPosition = getArguments().getInt(ARG_START_POSITION, 0);
        }

        // Khởi tạo dependencies
        videoRepository = new FirebaseVideoRepositoryImpl();
        authHelper = new FirebaseAuthHelper();
        eventBus = EventBus.getInstance();

        // Khởi tạo gesture detector cho double tap
        gestureDetector = new GestureDetector(getContext(), new DoubleTapGestureListener());
        // Khởi tạo gesture detector cho swipe
        swipeGestureDetector = new GestureDetector(getContext(), new SwipeGestureListener());
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
        setupTouchListeners();

        if (videoId != null) {
            loadVideoData();
        }
    }

    private void initViews(View view) {
        // Khởi tạo tất cả views giống y hệt Video Short Fragment
        playerView = view.findViewById(R.id.player_view);
        ivPoster = view.findViewById(R.id.iv_poster);
        videoTapArea = view.findViewById(R.id.video_tap_area);
        playPauseOverlay = view.findViewById(R.id.play_pause_overlay);
        progressLoading = view.findViewById(R.id.progress_loading);
        ivDoubleTapHeart = view.findViewById(R.id.iv_double_tap_heart);

        // Video info views
        tvVideoTitle = view.findViewById(R.id.tv_video_title);
        tvVideoCaption = view.findViewById(R.id.tv_video_caption);
        tvSeeMore = view.findViewById(R.id.tv_see_more);
        tvViewCount = view.findViewById(R.id.tv_view_count);
        tvUploadDate = view.findViewById(R.id.tv_upload_date);

        // Action button views
        btnLike = view.findViewById(R.id.btn_like);
        ivLikeIcon = view.findViewById(R.id.iv_like_icon);
        tvLikeCount = view.findViewById(R.id.tv_like_count);
        btnComment = view.findViewById(R.id.btn_comment);
        btnShare = view.findViewById(R.id.btn_share);

        // Fast forward/rewind indicators
        tvFastForward = view.findViewById(R.id.tv_fast_forward);
        tvRewind = view.findViewById(R.id.tv_rewind);

        // UI containers for visibility toggle
        layoutVideoInfo = view.findViewById(R.id.layout_video_info);
        layoutActionButtons = view.findViewById(R.id.layout_action_buttons);

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Like button click
        if (btnLike != null) {
            btnLike.setOnClickListener(v -> toggleLike());
        }

        // Comment button click
        if (btnComment != null) {
            btnComment.setOnClickListener(v -> {
                if (currentVideo != null && currentVideo.getId() != null) {
                    // Mở CommentBottomSheetFragment
                    CommentBottomSheetFragment commentFragment = CommentBottomSheetFragment.newInstance(currentVideo.getId());
                    commentFragment.show(getChildFragmentManager(), "CommentBottomSheet");
                } else {
                    Toast.makeText(getContext(), "Không thể mở bình luận: Video ID không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Share button click
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> shareVideo());
        }

        // See more caption click
        if (tvSeeMore != null) {
            tvSeeMore.setOnClickListener(v -> toggleCaptionExpansion());
        }
    }

    private void setupTouchListeners() {
        // Tap area cho play/pause và double tap
        if (videoTapArea != null) {
            videoTapArea.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                swipeGestureDetector.onTouchEvent(event); // Xử lý swipe gesture
                return true;
            });
        }
    }

    private void setupPlayer() {
        if (getContext() != null) {
            player = new ExoPlayer.Builder(getContext()).build();

            // Thiết lập repeat mode để video phát lại tự động
            player.setRepeatMode(Player.REPEAT_MODE_ONE);

            playerView.setPlayer(player);

            // Thiết lập listener cho player
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    switch (playbackState) {
                        case Player.STATE_BUFFERING:
                            showLoading(true);
                            break;
                        case Player.STATE_READY:
                            showLoading(false);
                            hidePoster();
                            // Tăng view count khi video sẵn sàng phát
                            incrementViewCount();
                            break;
                        case Player.STATE_ENDED:
                            // Video kết thúc, nhưng sẽ tự động lặp lại do REPEAT_MODE_ONE
                            // Không cần hiển thị play button vì sẽ tự động phát lại
                            break;
                    }
                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    SingleVideoPlayerFragment.this.isPlaying = isPlaying;
                    updatePlayPauseOverlay();
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

    private void togglePlayPause() {
        if (player == null) return;

        if (isPlaying) {
            player.setPlayWhenReady(false);
            showPlayPauseOverlay(true);
            // Ẩn overlay sau 1 giây
            handler.postDelayed(() -> showPlayPauseOverlay(false), 1000);
        } else {
            player.setPlayWhenReady(true);
            showPlayPauseOverlay(true);
            // Ẩn overlay sau 1 giây
            handler.postDelayed(() -> showPlayPauseOverlay(false), 1000);
        }
    }

    private void setupPosterImage() {
        if (currentVideo == null || ivPoster == null) return;

        // Hiển thị poster từ thumbnail hoặc Cloudinary
        String thumbnailUrl = null;
        if (currentVideo.getThumbnailUrl() != null && !currentVideo.getThumbnailUrl().isEmpty()) {
            thumbnailUrl = currentVideo.getThumbnailUrl();
        } else if (currentVideo.getCldPublicId() != null && !currentVideo.getCldPublicId().isEmpty()) {
            thumbnailUrl = CloudinaryUrls.poster(currentVideo.getCldPublicId(), currentVideo.getCldVersion());
        }

        if (thumbnailUrl != null && getContext() != null) {
            ivPoster.setVisibility(View.VISIBLE);
            Glide.with(getContext())
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.ic_video_placeholder)
                    .error(R.drawable.ic_video_error)
                    .centerCrop()
                    .into(ivPoster);
        }
    }

    private void formatAndDisplayUploadDate() {
        if (currentVideo == null || tvUploadDate == null) return;

        String formattedDate = formatUploadDate(currentVideo.getUploadDate());
        tvUploadDate.setText(formattedDate);
    }

    private String formatUploadDate(long uploadTimestamp) {
        long currentTime = System.currentTimeMillis();
        long diffTime = currentTime - uploadTimestamp;

        long seconds = diffTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long months = days / 30;
        long years = days / 365;

        if (years > 0) {
            return years + " năm trước";
        } else if (months > 0) {
            return months + " tháng trước";
        } else if (days > 0) {
            return days + " ngày tr��ớc";
        } else if (hours > 0) {
            return hours + " giờ trước";
        } else if (minutes > 0) {
            return minutes + " phút trước";
        } else {
            return "Vừa xong";
        }
    }

    private String formatViewCount(long viewCount) {
        if (viewCount < 1000) {
            return viewCount + " lượt xem";
        } else if (viewCount < 1000000) {
            float k = viewCount / 1000f;
            return String.format("%.1fK lượt xem", k);
        } else {
            float m = viewCount / 1000000f;
            return String.format("%.1fM lượt xem", m);
        }
    }

    private void checkCaptionLength() {
        if (currentVideo == null || tvVideoCaption == null || tvSeeMore == null) return;

        // Kiểm tra xem caption có dài hơn 2 dòng không
        tvVideoCaption.post(() -> {
            if (tvVideoCaption.getLineCount() > 2) {
                tvSeeMore.setVisibility(View.VISIBLE);
            } else {
                tvSeeMore.setVisibility(View.GONE);
            }
        });
    }

    // Cập nhật lại method displayVideoInfo để sử d��ng các helper methods mới
    private void displayVideoInfo() {
        if (currentVideo == null) return;

        // Hiển thị poster image
        setupPosterImage();

        // Hiển thị thông tin video
        if (tvVideoTitle != null) {
            tvVideoTitle.setText(currentVideo.getTitle());
        }

        if (tvVideoCaption != null) {
            tvVideoCaption.setText(currentVideo.getCaption());
            tvVideoCaption.setMaxLines(2);
            checkCaptionLength();
        }

        // Format và hiển thị view count
        if (tvViewCount != null) {
            tvViewCount.setText(formatViewCount(currentVideo.getViewCount()));
        }

        // Format và hiển thị upload date
        formatAndDisplayUploadDate();

        // Kiểm tra trạng thái like thực từ Firebase trước khi hiển thị
        checkAndUpdateLikeStatus();

        updateCounts();
    }

    /**
     * Kiểm tra và cập nhật trạng thái like thực từ Firebase
     */
    private void checkAndUpdateLikeStatus() {
        if (currentVideo == null || authHelper == null) return;

        String userId = authHelper.getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            // Người dùng chưa đăng nhập, hiển thị trạng thái mặc định
            currentVideo.setLiked(false);
            updateLikeButton();
            return;
        }

        // Kiểm tra trạng thái like thực từ Firebase
        videoRepository.isVideoLiked(currentVideo.getId(), userId, new VideoRepository.BooleanCallback() {
            @Override
            public void onSuccess(boolean isLiked) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        currentVideo.setLiked(isLiked);
                        updateLikeButton();

                        // Cập nhật EventBus với trạng thái thực
                        eventBus.updateVideoLikeStatus(currentVideo.getId(), isLiked);
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        // Nếu có lỗi khi kiểm tra, sử dụng trạng thái hiện tại
                        updateLikeButton();
                    });
                }
            }
        });
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

    private class DoubleTapGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // Xử lý single tap - toggle UI visibility (như TikTok)
            toggleUIVisibility();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // Xử lý double tap - like video
            if (currentVideo != null) {
                toggleLike();
                performDoubleTapAnimation();
            }
            return true;
        }
    }

    /**
     * Toggle visibility của UI elements (caption, icons) như TikTok
     */
    private void toggleUIVisibility() {
        isUIVisible = !isUIVisible;

        // Animate fade in/out
        if (layoutVideoInfo != null) {
            if (isUIVisible) {
                layoutVideoInfo.setAlpha(0f);
                layoutVideoInfo.setVisibility(View.VISIBLE);
                layoutVideoInfo.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();
            } else {
                layoutVideoInfo.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> layoutVideoInfo.setVisibility(View.GONE))
                    .start();
            }
        }

        if (layoutActionButtons != null) {
            if (isUIVisible) {
                layoutActionButtons.setAlpha(0f);
                layoutActionButtons.setVisibility(View.VISIBLE);
                layoutActionButtons.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();
            } else {
                layoutActionButtons.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> layoutActionButtons.setVisibility(View.GONE))
                    .start();
            }
        }
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Xử lý swipe giữa các video
            if (e1 == null || e2 == null) return false;

            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                // Swipe ngang
                if (diffX > 100) {
                    // Swipe sang phải
                    onSwipeRight();
                } else if (diffX < -100) {
                    // Swipe sang trái
                    onSwipeLeft();
                }
                return true;
            }
            return false;
        }
    }

    // Cập nhật lại method updateLikeButton để sử dụng đúng resources
    private void updateLikeButton() {
        if (ivLikeIcon == null || currentVideo == null) return;

        if (currentVideo.isLiked()) {
            ivLikeIcon.setImageResource(R.drawable.ic_heart_filled);
            ivLikeIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.like_color_active));
        } else {
            ivLikeIcon.setImageResource(R.drawable.ic_heart_outline);
            ivLikeIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.like_color_inactive));
        }
    }

    // Cập nhật lại method updateCounts để format số đẹp hơn
    private void updateCounts() {
        if (currentVideo == null) return;

        if (tvLikeCount != null) {
            long likeCount = currentVideo.getLikeCount();
            if (likeCount < 1000) {
                tvLikeCount.setText(String.valueOf(likeCount));
            } else if (likeCount < 1000000) {
                float k = likeCount / 1000f;
                tvLikeCount.setText(String.format("%.1fK", k));
            } else {
                float m = likeCount / 1000000f;
                tvLikeCount.setText(String.format("%.1fM", m));
            }
        }

        if (tvViewCount != null) {
            tvViewCount.setText(formatViewCount(currentVideo.getViewCount()));
        }
    }

    private void performDoubleTapAnimation() {
        if (ivDoubleTapHeart == null) return;

        // Hiển thị icon heart fill trong 300ms rồi biến mất
        ivDoubleTapHeart.setVisibility(View.VISIBLE);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivDoubleTapHeart, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivDoubleTapHeart, "scaleY", 1f, 1.2f, 1f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                ivDoubleTapHeart.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ivDoubleTapHeart.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                ivDoubleTapHeart.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animatorSet.start();
    }

    private void showLoading(boolean show) {
        if (progressLoading != null) {
            progressLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void hidePoster() {
        if (ivPoster != null) {
            ivPoster.setVisibility(View.GONE);
        }
    }

    private void showPlayPauseOverlay(boolean show) {
        if (playPauseOverlay != null) {
            playPauseOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void updatePlayPauseOverlay() {
        if (playPauseOverlay == null) return;

        if (isPlaying) {
            playPauseOverlay.setImageResource(R.drawable.ic_pause_circle_filled);
        } else {
            playPauseOverlay.setImageResource(R.drawable.ic_play_circle_filled);
        }
    }

    private void toggleCaptionExpansion() {
        if (tvVideoCaption == null || currentVideo == null) return;

        boolean isExpanded = tvVideoCaption.getMaxLines() == Integer.MAX_VALUE;

        if (isExpanded) {
            // Thu gọn caption
            tvVideoCaption.setMaxLines(3);
            tvSeeMore.setText("Xem thêm");
        } else {
            // Mở rộng caption
            tvVideoCaption.setMaxLines(Integer.MAX_VALUE);
            tvSeeMore.setText("Thu gọn");
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

        try {
            String shareText = "Xem video này: " + currentVideo.getTitle();
            if (currentVideo.getCaption() != null && !currentVideo.getCaption().isEmpty()) {
                shareText += "\n" + currentVideo.getCaption();
            }

            // Tạo Intent chia sẻ
            android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Video từ HealthTips");

            // Hiển thị chooser
            android.content.Intent chooser = android.content.Intent.createChooser(shareIntent, "Chia sẻ video");
            if (chooser.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(chooser);
            } else {
                Toast.makeText(getContext(), "Không tìm thấy ứng dụng để chia sẻ", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi khi chia sẻ video", Toast.LENGTH_SHORT).show();
        }
    }

    private void onSwipeLeft() {
        if (videoList == null || videoList.isEmpty()) return;

        // Tìm vị trí video tiếp theo
        currentPosition = (currentPosition + 1) % videoList.size();
        ShortVideo nextVideo = videoList.get(currentPosition);

        // Chuyển sang video tiếp theo
        videoId = nextVideo.getId();
        currentVideo = nextVideo;
        displayVideoInfo();
        playVideo();
    }

    private void onSwipeRight() {
        if (videoList == null || videoList.isEmpty()) return;

        // Tìm vị trí video trước đó
        currentPosition = (currentPosition - 1 + videoList.size()) % videoList.size();
        ShortVideo prevVideo = videoList.get(currentPosition);

        // Chuyển sang video trước đó
        videoId = prevVideo.getId();
        currentVideo = prevVideo;
        displayVideoInfo();
        playVideo();
    }

}
