package com.vhn.doan.presentation.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.repository.FirebaseVideoRepositoryImpl;
import com.vhn.doan.presentation.video.CommentBottomSheetFragment;
import com.vhn.doan.presentation.video.VideoPresenter;
import com.vhn.doan.presentation.video.VideoView;
import com.vhn.doan.utils.EventBus;
import com.vhn.doan.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fragment hiển thị kết quả tìm kiếm video với đầy đủ chức năng tương tác
 */
public class VideoSearchResultsFragment extends Fragment implements VideoView {
    private RecyclerView rvVideoResults;
    private View layoutNoVideoResults;
    private View loadingLayout;
    private Button retryButton;
    private View snackbarAnchor;

    private VideoSearchResultAdapter adapter;
    private List<ShortVideo> videoResults = new ArrayList<>();

    // Interface lắng nghe sự kiện click vào video
    private VideoItemClickListener listener;

    // EventBus để đồng bộ trạng thái like giữa các màn hình
    private EventBus eventBus;
    private Observer<Map<String, Boolean>> videoLikeObserver;

    // Presenter và Firebase Auth
    private VideoPresenter presenter;
    private FirebaseAuth firebaseAuth;

    public static VideoSearchResultsFragment newInstance() {
        return new VideoSearchResultsFragment();
    }

    public void setVideoItemClickListener(VideoItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Khởi tạo EventBus
        eventBus = EventBus.getInstance();

        // Khởi tạo Presenter với repository
        presenter = new VideoPresenter(new FirebaseVideoRepositoryImpl());
        presenter.attachView(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_search_results, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo view
        initViews(view);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập listeners
        setupListeners();

        // Đăng ký lắng nghe sự kiện thay đổi trạng thái like từ EventBus
        registerLikeStatusObserver();

        // Hiển thị trạng thái ban đầu
        updateUI();
    }

    private void initViews(View view) {
        rvVideoResults = view.findViewById(R.id.rv_video_results);
        layoutNoVideoResults = view.findViewById(R.id.layout_no_video_results);
        loadingLayout = view.findViewById(R.id.loading_layout);
        retryButton = view.findViewById(R.id.btn_retry);
        snackbarAnchor = view.findViewById(R.id.snackbar_anchor);
    }

    private void setupRecyclerView() {
        rvVideoResults.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VideoSearchResultAdapter(getContext(), videoResults);

        // Thiết lập listener cho click vào video
        adapter.setOnVideoClickListener(position -> {
            if (listener != null && position < videoResults.size()) {
                listener.onVideoClicked(videoResults.get(position));
            }
        });

        // Thiết lập listener cho các tương tác video
        adapter.setOnVideoInteractionListener(new VideoSearchResultAdapter.OnVideoInteractionListener() {
            @Override
            public void onLikeClicked(ShortVideo video, int position, boolean isLiked) {
                // Gọi presenter để xử lý like/unlike
                presenter.toggleLike(position);
            }

            @Override
            public void onShareClicked(ShortVideo video, int position) {
                // Gọi presenter để xử lý share
                presenter.onShareClick(position);
            }

            @Override
            public void onCommentClicked(ShortVideo video, int position) {
                // Gọi presenter để xử lý comment
                presenter.onCommentClick(position);
            }
        });

        rvVideoResults.setAdapter(adapter);
    }

    private void setupListeners() {
        if (retryButton != null) {
            retryButton.setOnClickListener(v -> {
                // Có thể thêm logic retry nếu cần
                updateUI();
            });
        }
    }

    /**
     * Lấy User ID hiện tại từ Firebase Auth hoặc SharedPreferences
     */
    private String getCurrentUserId() {
        try {
            // Đảm bảo firebaseAuth được khởi tạo
            if (firebaseAuth == null) {
                firebaseAuth = FirebaseAuth.getInstance();
            }

            // Ưu tiên lấy từ Firebase Auth
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                String uid = currentUser.getUid();
                // Đồng bộ với SharedPreferences nếu context tồn tại
                if (getContext() != null) {
                    SharedPreferencesHelper helper = new SharedPreferencesHelper(getContext());
                    helper.setCurrentUserId(uid);
                }
                return uid;
            }
        } catch (Exception e) {
            // Log lỗi và fallback về SharedPreferences
            e.printStackTrace();
        }

        // Fallback về SharedPreferences
        if (getContext() != null) {
            return SharedPreferencesHelper.getUserId(getContext());
        }

        // Trường hợp cuối cùng - return null nếu không thể lấy user ID
        return null;
    }

    /**
     * Đăng ký lắng nghe sự kiện thay đổi trạng thái like từ EventBus
     */
    private void registerLikeStatusObserver() {
        if (videoLikeObserver != null) {
            return; // Tránh đăng ký nhiều lần
        }

        videoLikeObserver = likeStatusMap -> {
            if (adapter == null || likeStatusMap == null || likeStatusMap.isEmpty() || videoResults.isEmpty()) {
                return;
            }

            // Cập nhật trạng thái like cho các video trong adapter ngay lập tức
            for (int i = 0; i < videoResults.size(); i++) {
                ShortVideo video = videoResults.get(i);
                Boolean isLiked = likeStatusMap.get(video.getId());
                if (isLiked != null) {
                    // Cập nhật trạng thái trong data model
                    video.setLiked(isLiked);
                    // Cập nhật UI ngay lập tức
                    adapter.updateVideoLikeStatus(i, isLiked);
                }
            }
        };

        // Đăng ký lắng nghe sự kiện từ EventBus
        eventBus.getVideoLikeStatusLiveData().observe(getViewLifecycleOwner(), videoLikeObserver);
    }

    /**
     * Cập nhật danh sách kết quả tìm kiếm video
     * @param results Danh sách kết quả tìm kiếm mới
     */
    public void updateResults(List<ShortVideo> results) {
        videoResults.clear();
        if (results != null) {
            videoResults.addAll(results);
        }

        // Cập nhật presenter với danh sách video mới và user ID
        if (presenter != null) {
            presenter.updateVideoList(videoResults);
            // Đảm bảo presenter có user ID để kiểm tra trạng thái like
            presenter.setCurrentUserId(getCurrentUserId());
        }

        updateUI();

        // Đồng bộ trạng thái like cho các video vừa tải về
        syncVideoLikeStatus();

        // Kiểm tra trạng thái like thực từ Firebase cho tất cả video mới
        checkLikeStatusForAllVideos();
    }

    /**
     * Đồng bộ trạng thái like của video từ EventBus
     */
    private void syncVideoLikeStatus() {
        if (videoResults.isEmpty() || adapter == null) {
            return;
        }

        Map<String, Boolean> likeStatusMap = eventBus.getVideoLikeStatusLiveData().getValue();
        if (likeStatusMap == null || likeStatusMap.isEmpty()) {
            return;
        }

        for (int i = 0; i < videoResults.size(); i++) {
            ShortVideo video = videoResults.get(i);
            Boolean isLiked = likeStatusMap.get(video.getId());
            if (isLiked != null) {
                adapter.updateVideoLikeStatus(i, isLiked);
            }
        }
    }

    /**
     * Cập nhật giao diện dựa trên kết quả tìm kiếm
     */
    private void updateUI() {
        // Nếu fragment chưa được gắn view hoặc đã bị detach, không thực hiện cập nhật UI
        if (!isAdded() || getView() == null) {
            return;
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        try {
            // Phải đảm bảo các View đã được khởi tạo trước khi sử dụng
            if (rvVideoResults == null) {
                rvVideoResults = getView().findViewById(R.id.rv_video_results);
            }

            if (layoutNoVideoResults == null) {
                layoutNoVideoResults = getView().findViewById(R.id.layout_no_video_results);
            }

            // Kiểm tra null cho các view trước khi thiết lập trạng thái hiển thị
            if (rvVideoResults != null && layoutNoVideoResults != null) {
                // Hiển thị thông báo khi không có kết quả
                if (videoResults.isEmpty()) {
                    rvVideoResults.setVisibility(View.GONE);
                    layoutNoVideoResults.setVisibility(View.VISIBLE);
                } else {
                    rvVideoResults.setVisibility(View.VISIBLE);
                    layoutNoVideoResults.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            // Xử lý bất kỳ ngoại lệ nào có thể xảy ra khi truy cập các view
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        if (presenter != null) {
            presenter.detachView();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Khi fragment resume, kiểm tra và cập nhật lại trạng thái like từ EventBus
        syncVideoLikeStatusFromEventBus();

        // Kiểm tra trạng thái like thực từ Firebase cho user hiện tại
        checkLikeStatusForAllVideos();
    }

    /**
     * Kiểm tra trạng thái like thực từ Firebase cho tất cả video
     */
    private void checkLikeStatusForAllVideos() {
        String userId = getCurrentUserId();
        if (userId == null || userId.isEmpty() || videoResults.isEmpty()) {
            return;
        }

        for (int i = 0; i < videoResults.size(); i++) {
            final int position = i;
            final ShortVideo video = videoResults.get(position);

            // Sử dụng presenter để kiểm tra trạng thái like
            if (presenter != null) {
                presenter.checkLikeStatusForVideo(position);
            }
        }
    }

    /**
     * Đồng bộ trạng thái like từ EventBus khi resume
     */
    private void syncVideoLikeStatusFromEventBus() {
        if (eventBus == null || videoResults.isEmpty() || adapter == null) {
            return;
        }

        Map<String, Boolean> likeStatusMap = eventBus.getVideoLikeStatusLiveData().getValue();
        if (likeStatusMap == null || likeStatusMap.isEmpty()) {
            return;
        }

        for (int i = 0; i < videoResults.size(); i++) {
            ShortVideo video = videoResults.get(i);
            Boolean isLiked = likeStatusMap.get(video.getId());
            if (isLiked != null && video.isLiked() != isLiked) {
                // Cập nhật trạng thái trong data model
                video.setLiked(isLiked);
                // Cập nhật UI
                adapter.updateVideoLikeStatus(i, isLiked);
            }
        }
    }

    // Implement các method từ VideoView interface

    @Override
    public void showVideoFeed(List<ShortVideo> videos) {
        // Không sử dụng trong search fragment
    }

    @Override
    public void showError(String message) {
        if (getView() != null && getActivity() != null && isAdded()) {
            Snackbar.make(snackbarAnchor != null ? snackbarAnchor : rvVideoResults,
                    message, Snackbar.LENGTH_LONG)
                    .setAction("Thử lại", v -> updateUI())
                    .show();
        }
    }

    @Override
    public void showLoadingVideos() {
        if (loadingLayout != null && rvVideoResults != null && layoutNoVideoResults != null) {
            loadingLayout.setVisibility(View.VISIBLE);
            rvVideoResults.setVisibility(View.GONE);
            layoutNoVideoResults.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideLoadingVideos() {
        if (loadingLayout != null && rvVideoResults != null) {
            loadingLayout.setVisibility(View.GONE);
            rvVideoResults.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void playVideoAtPosition(int position) {
        // Không cần thiết trong search fragment - video không tự động phát
    }

    @Override
    public void pauseCurrentVideo() {
        // Không cần thiết trong search fragment
    }

    @Override
    public void updateVideoInfo(ShortVideo video, int position) {
        if (adapter != null && position >= 0 && position < videoResults.size()) {
            videoResults.set(position, video);
            adapter.notifyItemChanged(position);
        }
    }

    @Override
    public void showEmptyState() {
        if (rvVideoResults != null && layoutNoVideoResults != null) {
            rvVideoResults.setVisibility(View.GONE);
            layoutNoVideoResults.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideEmptyState() {
        if (layoutNoVideoResults != null && rvVideoResults != null) {
            layoutNoVideoResults.setVisibility(View.GONE);
            rvVideoResults.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void refreshVideoFeed() {
        // Không cần thiết trong search fragment
    }

    @Override
    public void updateVideoLikeStatus(int position, boolean isLiked) {
        if (adapter != null && position >= 0 && position < videoResults.size()) {
            // Cập nhật trạng thái like trong data
            videoResults.get(position).setLiked(isLiked);
            // Cập nhật UI
            adapter.updateVideoLikeStatus(position, isLiked);
        }
    }

    @Override
    public void revertVideoLikeUI(int position) {
        if (adapter != null && position >= 0 && position < videoResults.size()) {
            adapter.revertLikeUI(position);
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

            String shareText = "Xem video này: " + video.getTitle();
            if (video.getCaption() != null && !video.getCaption().isEmpty()) {
                shareText += "\n" + video.getCaption();
            }

            // Thêm URL video nếu có
            if (video.getVideoUrl() != null && !video.getVideoUrl().isEmpty()) {
                shareText += "\n" + video.getVideoUrl();
            }

            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Chia sẻ video");

            Intent chooserIntent = Intent.createChooser(shareIntent, "Chia sẻ video qua...");
            if (chooserIntent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(chooserIntent);
            } else {
                showMessage("Không tìm thấy ứng dụng để chia sẻ");
            }
        } catch (Exception e) {
            showMessage("Lỗi khi chia sẻ video: " + e.getMessage());
        }
    }

    /**
     * Interface lắng nghe sự kiện click vào video
     */
    public interface VideoItemClickListener {
        void onVideoClicked(ShortVideo video);
    }

    /**
     * Kiểm tra xem kết quả tìm kiếm có trống hay không
     * @return true nếu không có kết quả, false nếu có kết quả
     */
    public boolean isResultsEmpty() {
        return videoResults == null || videoResults.isEmpty();
    }
}
