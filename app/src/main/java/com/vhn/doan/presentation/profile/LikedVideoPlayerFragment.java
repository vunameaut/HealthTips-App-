package com.vhn.doan.presentation.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.repository.ShortVideoRepository;
import com.vhn.doan.data.repository.ShortVideoRepositoryImpl;
import com.vhn.doan.data.repository.RepositoryCallback;
import com.vhn.doan.presentation.shortvideo.ShortVideoAdapter;
import com.vhn.doan.utils.FirebaseAuthHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment phát video đã like với khả năng swipe giống TikTok
 */
public class LikedVideoPlayerFragment extends Fragment {

    private static final String ARG_VIDEO_LIST = "video_list";
    private static final String ARG_CURRENT_POSITION = "current_position";

    private RecyclerView recyclerViewVideos;
    private LinearProgressIndicator progressIndicator;
    private View emptyStateLayout;

    private ShortVideoAdapter adapter;
    private LinearLayoutManager layoutManager;
    private PagerSnapHelper snapHelper;
    private ShortVideoRepository repository;

    private List<ShortVideo> likedVideos = new ArrayList<>();
    private int currentPosition = 0;
    private boolean isLoading = false;

    public static LikedVideoPlayerFragment newInstance(ArrayList<ShortVideo> videos, int position) {
        LikedVideoPlayerFragment fragment = new LikedVideoPlayerFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_VIDEO_LIST, videos);
        args.putInt(ARG_CURRENT_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new ShortVideoRepositoryImpl();

        if (getArguments() != null) {
            likedVideos = getArguments().getParcelableArrayList(ARG_VIDEO_LIST);
            currentPosition = getArguments().getInt(ARG_CURRENT_POSITION, 0);
            if (likedVideos == null) {
                likedVideos = new ArrayList<>();
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_liked_video_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        setupBackButton();

        if (likedVideos.isEmpty()) {
            loadLikedVideos();
        } else {
            adapter.updateData(likedVideos);
            recyclerViewVideos.scrollToPosition(currentPosition);
        }
    }

    private void initViews(View view) {
        recyclerViewVideos = view.findViewById(R.id.recyclerViewVideos);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
    }

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(getContext());
        recyclerViewVideos.setLayoutManager(layoutManager);

        adapter = new ShortVideoAdapter(getContext(), new ArrayList<>());
        recyclerViewVideos.setAdapter(adapter);

        // PagerSnapHelper để tạo hiệu ứng swipe giống TikTok
        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerViewVideos);

        // Listener để theo dõi video hiện tại
        recyclerViewVideos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = snapHelper.findSnapView(layoutManager);
                    if (centerView != null) {
                        int position = layoutManager.getPosition(centerView);
                        if (position != currentPosition) {
                            currentPosition = position;
                            // Tải thêm video nếu gần hết danh sách
                            if (position >= likedVideos.size() - 3 && !isLoading) {
                                loadMoreLikedVideos();
                            }
                        }
                    }
                }
            }
        });
    }

    private void setupBackButton() {
        View btnBack = getView().findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }
    }

    private void loadLikedVideos() {
        if (isLoading) return;

        isLoading = true;
        progressIndicator.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);

        String currentUserId = FirebaseAuthHelper.getCurrentUserId();
        if (currentUserId == null) {
            showError("Vui lòng đăng nhập để xem video đã like");
            return;
        }

        repository.getLikedVideos(currentUserId, new RepositoryCallback<List<ShortVideo>>() {
            @Override
            public void onSuccess(List<ShortVideo> result) {
                if (!isAdded()) return;

                isLoading = false;
                progressIndicator.setVisibility(View.GONE);

                if (result == null || result.isEmpty()) {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                } else {
                    likedVideos.clear();
                    likedVideos.addAll(result);
                    adapter.updateData(likedVideos);
                    emptyStateLayout.setVisibility(View.GONE);

                    // Scroll đến vị trí được chỉ định
                    if (currentPosition < likedVideos.size()) {
                        recyclerViewVideos.scrollToPosition(currentPosition);
                    }
                }
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;

                isLoading = false;
                progressIndicator.setVisibility(View.GONE);
                showError(error);
            }
        });
    }

    private void loadMoreLikedVideos() {
        // Implement pagination nếu cần
        // Hiện tại chỉ load tất cả video đã like
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume video player nếu cần
        if (adapter != null) {
            adapter.resumeCurrentVideo();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pause video player nếu cần
        if (adapter != null) {
            adapter.pauseCurrentVideo();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) {
            adapter.releasePlayer();
        }
    }
}
