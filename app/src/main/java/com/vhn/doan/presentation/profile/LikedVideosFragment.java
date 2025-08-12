package com.vhn.doan.presentation.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.widget.ProgressBar;

import com.vhn.doan.R;
import com.vhn.doan.presentation.base.BaseFragment;
import com.vhn.doan.presentation.profile.adapter.GridShortVideoAdapter;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.repository.ShortVideoRepository;
import com.vhn.doan.data.repository.ShortVideoRepositoryImpl;
import com.vhn.doan.data.repository.RepositoryCallback;
import com.vhn.doan.utils.FirebaseAuthHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị danh sách video đã like của người dùng
 */
public class LikedVideosFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private GridShortVideoAdapter adapter;
    private ProgressBar progressBar;
    private ConstraintLayout emptyStateLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ShortVideoRepository repository;
    private List<ShortVideo> likedVideos = new ArrayList<>();

    public static LikedVideosFragment newInstance() {
        return new LikedVideosFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grid_content, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new ShortVideoRepositoryImpl();
        setupRecyclerView();
        setupSwipeRefresh();
        loadLikedVideos();
    }

    @Override
    protected void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
    }

    @Override
    protected void setupListeners() {
        // Không cần thiết lập listener ở đây
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);

        // Tạo adapter với listener để xử lý click vào video
        adapter = new GridShortVideoAdapter(requireContext(), video -> {
            // Khi click vào video, mở LikedVideoPlayerFragment
            openVideoPlayer(video);
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadLikedVideos);
        }
    }

    private void loadLikedVideos() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.GONE);
        }

        String currentUserId = FirebaseAuthHelper.getCurrentUserId();
        if (currentUserId == null) {
            showError("Vui lòng đăng nhập để xem video đã like");
            return;
        }

        repository.getLikedVideos(currentUserId, new RepositoryCallback<List<ShortVideo>>() {
            @Override
            public void onSuccess(List<ShortVideo> result) {
                if (!isAdded()) return;

                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                if (result == null || result.isEmpty()) {
                    if (emptyStateLayout != null) {
                        emptyStateLayout.setVisibility(View.VISIBLE);
                    }
                    likedVideos.clear();
                } else {
                    if (emptyStateLayout != null) {
                        emptyStateLayout.setVisibility(View.GONE);
                    }
                    likedVideos.clear();
                    likedVideos.addAll(result);
                    adapter.updateData(likedVideos);
                }
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;

                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                showError(error);
            }
        });
    }

    /**
     * Mở video player với khả năng swipe giống TikTok
     */
    private void openVideoPlayer(ShortVideo selectedVideo) {
        if (likedVideos.isEmpty()) {
            showError("Không có video để phát");
            return;
        }

        // Tìm vị trí của video được chọn
        int selectedPosition = 0;
        for (int i = 0; i < likedVideos.size(); i++) {
            if (likedVideos.get(i).getId().equals(selectedVideo.getId())) {
                selectedPosition = i;
                break;
            }
        }

        // Mở LikedVideoPlayerActivity với Intent
        Intent intent = LikedVideoPlayerActivity.createIntent(
            getContext(),
            new ArrayList<>(likedVideos),
            selectedPosition
        );
        startActivity(intent);

        // Thêm animation chuyển màn hình
        if (getActivity() != null) {
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
