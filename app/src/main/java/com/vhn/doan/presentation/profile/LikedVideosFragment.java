package com.vhn.doan.presentation.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.R;
import com.vhn.doan.presentation.base.BaseFragment;
import com.vhn.doan.presentation.profile.adapter.GridShortVideoAdapter;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.repository.RepositoryCallback;
import com.vhn.doan.data.repository.ShortVideoRepository;
import com.vhn.doan.data.repository.ShortVideoRepositoryImpl;

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
        adapter = new GridShortVideoAdapter(requireContext(), position -> {
            ShortVideo video = adapter.getVideoAt(position);
            if (video != null && getContext() != null) {
                Intent intent = new Intent(getContext(), LikedVideoPlayerActivity.class);
                intent.putExtra(LikedVideoPlayerActivity.EXTRA_START_VIDEO_ID, video.getId());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadLikedVideos);
        }
    }

    private void loadLikedVideos() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            emptyStateLayout.setVisibility(View.VISIBLE);
            showError("Bạn cần đăng nhập");
            return;
        }

        ShortVideoRepository repository = new ShortVideoRepositoryImpl();
        repository.getLikedVideos(user.getUid(), 50, new RepositoryCallback<List<ShortVideo>>() {
            @Override
            public void onSuccess(List<ShortVideo> result) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (result == null || result.isEmpty()) {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                } else {
                    emptyStateLayout.setVisibility(View.GONE);
                    adapter.updateData(result);
                }
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                emptyStateLayout.setVisibility(View.VISIBLE);
                showError("Không thể tải video: " + error);
            }
        });
    }
}
