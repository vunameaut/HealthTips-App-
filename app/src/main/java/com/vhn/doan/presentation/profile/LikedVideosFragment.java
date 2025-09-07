package com.vhn.doan.presentation.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.repository.FirebaseVideoRepositoryImpl;
import com.vhn.doan.presentation.profile.adapter.LikedVideoGridAdapter;
import com.vhn.doan.presentation.profile.LikedVideosPlayerActivity;
import com.vhn.doan.utils.EventBus;

import java.util.List;

/**
 * Fragment hiển thị danh sách video đã like dạng lưới 3 cột
 * Giao diện tương tự TikTok với tỷ lệ video dọc
 */
public class LikedVideosFragment extends Fragment implements LikedVideosView {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View loadingLayout;
    private View emptyLayout;
    private TextView emptyMessageTextView;

    private LikedVideoGridAdapter adapter;
    private LikedVideosPresenter presenter;
    private FirebaseAuth firebaseAuth;
    private EventBus eventBus;

    public static LikedVideosFragment newInstance() {
        return new LikedVideosFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Khởi tạo EventBus để lắng nghe sự thay đổi trạng thái like
        eventBus = EventBus.getInstance();

        // Khởi tạo presenter
        presenter = new LikedVideosPresenter(new FirebaseVideoRepositoryImpl());
        presenter.attachView(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_liked_videos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupEventBusListener();

        // Tải danh sách video đã like
        loadLikedVideos();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.rv_liked_videos);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        loadingLayout = view.findViewById(R.id.loading_layout);
        emptyLayout = view.findViewById(R.id.empty_layout);
        emptyMessageTextView = view.findViewById(R.id.txt_empty_message);
    }

    private void setupRecyclerView() {
        // Setup GridLayoutManager với 3 cột
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);

        // Khởi tạo adapter
        adapter = new LikedVideoGridAdapter(getContext());
        recyclerView.setAdapter(adapter);

        // Thiết lập click listener
        adapter.setOnVideoClickListener((position, video) -> {
            presenter.onVideoClick(position);
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            presenter.refreshLikedVideos();
        });

        // Thiết lập màu sắc cho SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        );
    }

    private void setupEventBusListener() {
        // Lắng nghe sự thay đổi trạng thái like từ EventBus
        eventBus.getVideoLikeStatusLiveData().observe(getViewLifecycleOwner(), likeStatusMap -> {
            if (likeStatusMap == null || likeStatusMap.isEmpty()) {
                return;
            }

            // Cập nhật trạng thái like cho các video trong adapter
            for (String videoId : likeStatusMap.keySet()) {
                Boolean isLiked = likeStatusMap.get(videoId);
                if (isLiked != null) {
                    presenter.updateVideoLikeStatus(videoId, isLiked);
                }
            }
        });
    }

    private void loadLikedVideos() {
        android.util.Log.d("LikedVideosFragment", "loadLikedVideos được gọi");

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            android.util.Log.d("LikedVideosFragment", "User đã đăng nhập, UID: " + currentUser.getUid());
            presenter.loadLikedVideos(currentUser.getUid());
        } else {
            android.util.Log.e("LikedVideosFragment", "User chưa đăng nhập");
            showError("Vui lòng đăng nhập để xem video đã like");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    // Implement LikedVideosView interface

    @Override
    public void showLikedVideos(List<ShortVideo> videos) {
        android.util.Log.d("LikedVideosFragment", "showLikedVideos được g��i với " + (videos != null ? videos.size() : 0) + " videos");

        if (getActivity() == null || !isAdded()) {
            android.util.Log.w("LikedVideosFragment", "Fragment không còn attached, bỏ qua showLikedVideos");
            return;
        }

        // Log trạng thái các view trước khi thay đổi
        android.util.Log.d("LikedVideosFragment", "Trạng thái views trước khi update:");
        android.util.Log.d("LikedVideosFragment", "- recyclerView visibility: " + (recyclerView != null ? recyclerView.getVisibility() : "null"));
        android.util.Log.d("LikedVideosFragment", "- loadingLayout visibility: " + (loadingLayout != null ? loadingLayout.getVisibility() : "null"));
        android.util.Log.d("LikedVideosFragment", "- emptyLayout visibility: " + (emptyLayout != null ? emptyLayout.getVisibility() : "null"));

        hideEmptyState();
        hideLoading(); // Thêm dòng này để đảm bảo loading layout được ẩn
        recyclerView.setVisibility(View.VISIBLE);

        android.util.Log.d("LikedVideosFragment", "Trạng thái views sau khi update:");
        android.util.Log.d("LikedVideosFragment", "- recyclerView visibility: " + recyclerView.getVisibility());
        android.util.Log.d("LikedVideosFragment", "- loadingLayout visibility: " + (loadingLayout != null ? loadingLayout.getVisibility() : "null"));
        android.util.Log.d("LikedVideosFragment", "- emptyLayout visibility: " + (emptyLayout != null ? emptyLayout.getVisibility() : "null"));

        adapter.updateVideos(videos);
        android.util.Log.d("LikedVideosFragment", "Đã cập nhật adapter với videos");

        // Thêm kiểm tra adapter
        if (recyclerView.getAdapter() != null) {
            android.util.Log.d("LikedVideosFragment", "RecyclerView có adapter với " + recyclerView.getAdapter().getItemCount() + " items");
        } else {
            android.util.Log.e("LikedVideosFragment", "RecyclerView KHÔNG có adapter!");
        }
    }

    @Override
    public void showLoading() {
        android.util.Log.d("LikedVideosFragment", "showLoading được gọi");
        if (loadingLayout != null) {
            loadingLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideLoading() {
        android.util.Log.d("LikedVideosFragment", "hideLoading được gọi");
        if (loadingLayout != null) {
            loadingLayout.setVisibility(View.GONE);
        }
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void showEmptyState() {
        android.util.Log.d("LikedVideosFragment", "showEmptyState được gọi");
        if (getActivity() == null || !isAdded()) {
            android.util.Log.w("LikedVideosFragment", "Fragment không còn attached, bỏ qua showEmptyState");
            return;
        }

        recyclerView.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);

        if (emptyMessageTextView != null) {
            emptyMessageTextView.setText("Chưa có video nào được thích\n\nHãy khám phá và thích những video yêu thích của bạn!");
        }
    }

    @Override
    public void hideEmptyState() {
        if (emptyLayout != null) {
            emptyLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void showError(String message) {
        if (getContext() != null && isAdded()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void openVideoPlayer(List<ShortVideo> videos, int startPosition) {
        if (getActivity() == null || !isAdded()) return;

        // Mở LikedVideosPlayerActivity với danh sách video đã like
        Intent intent = LikedVideosPlayerActivity.createIntent(getContext(), videos, startPosition);
        startActivity(intent);
    }

    @Override
    public void removeVideoFromGrid(int position) {
        if (adapter != null) {
            adapter.removeVideo(position);
        }
    }

    @Override
    public void refreshGrid() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
