package com.vhn.doan.presentation.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
 * Activity để phát video đã like với khả năng swipe giống TikTok
 * Mở trong full-screen mode cho trải nghiệm tốt nhất
 */
public class LikedVideoPlayerActivity extends AppCompatActivity {

    private static final String EXTRA_VIDEO_LIST = "extra_video_list";
    private static final String EXTRA_CURRENT_POSITION = "extra_current_position";

    private RecyclerView recyclerViewVideos;
    private LinearProgressIndicator progressIndicator;
    private View emptyStateLayout;
    private ImageButton btnBack;
    private TextView tvTitle;

    private ShortVideoAdapter adapter;
    private LinearLayoutManager layoutManager;
    private PagerSnapHelper snapHelper;
    private ShortVideoRepository repository;

    private List<ShortVideo> likedVideos = new ArrayList<>();
    private int currentPosition = 0;
    private boolean isLoading = false;

    /**
     * Tạo Intent để mở LikedVideoPlayerActivity
     * @param context Context
     * @param videos Danh sách video đã like
     * @param position Vị trí video hiện tại
     * @return Intent
     */
    public static Intent createIntent(Context context, ArrayList<ShortVideo> videos, int position) {
        Intent intent = new Intent(context, LikedVideoPlayerActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_VIDEO_LIST, videos);
        intent.putExtra(EXTRA_CURRENT_POSITION, position);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_video_player);

        // Ẩn status bar và navigation bar để có trải nghiệm full-screen
        hideSystemUI();

        repository = new ShortVideoRepositoryImpl();

        // Lấy dữ liệu từ Intent
        getDataFromIntent();

        // Khởi tạo views và setup
        initViews();
        setupRecyclerView();
        setupListeners();

        // Load data
        if (likedVideos.isEmpty()) {
            android.util.Log.d("LikedVideoPlayer", "No videos from Intent, loading from Firebase");
            loadLikedVideos();
        } else {
            android.util.Log.d("LikedVideoPlayer", "Received " + likedVideos.size() + " videos from Intent");
            // Cập nhật adapter với dữ liệu từ Intent
            if (adapter != null) {
                adapter.updateData(likedVideos);
            }

            // Scroll đến vị trí được chỉ định sau khi adapter đã sẵn sàng
            recyclerViewVideos.post(() -> {
                if (currentPosition < likedVideos.size()) {
                    android.util.Log.d("LikedVideoPlayer", "Scrolling to position: " + currentPosition);
                    recyclerViewVideos.scrollToPosition(currentPosition);

                    // Đảm bảo video được phát sau khi scroll
                    recyclerViewVideos.postDelayed(() -> {
                        if (adapter != null) {
                            adapter.playVideoAt(currentPosition);
                        }
                    }, 500); // Delay 500ms để đảm bảo scroll hoàn tất
                }
            });
        }
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            likedVideos = intent.getParcelableArrayListExtra(EXTRA_VIDEO_LIST);
            currentPosition = intent.getIntExtra(EXTRA_CURRENT_POSITION, 0);
            if (likedVideos == null) {
                likedVideos = new ArrayList<>();
            }
        }
    }

    private void initViews() {
        recyclerViewVideos = findViewById(R.id.recyclerViewVideos);
        progressIndicator = findViewById(R.id.progressIndicator);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
    }

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewVideos.setLayoutManager(layoutManager);

        // Tạo adapter với logging để debug
        android.util.Log.d("LikedVideoPlayer", "Setting up RecyclerView with " + likedVideos.size() + " videos");

        adapter = new ShortVideoAdapter(this, new ArrayList<>(likedVideos));
        recyclerViewVideos.setAdapter(adapter);

        // PagerSnapHelper để tạo hiệu ứng swipe giống TikTok
        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerViewVideos);

        // Listener để theo dõi video hiện tại
        recyclerViewVideos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = snapHelper.findSnapView(layoutManager);
                    if (centerView != null) {
                        int position = layoutManager.getPosition(centerView);
                        if (position != currentPosition) {
                            currentPosition = position;
                            android.util.Log.d("LikedVideoPlayer", "Video position changed to: " + position);

                            // Tải thêm video nếu gần hết danh sách
                            if (position >= likedVideos.size() - 3 && !isLoading) {
                                loadMoreLikedVideos();
                            }
                        }
                    }
                }
            }
        });

        // Auto play video đầu tiên sau khi setup xong
        recyclerViewVideos.post(() -> {
            if (!likedVideos.isEmpty() && adapter != null) {
                android.util.Log.d("LikedVideoPlayer", "Auto playing video at position: " + currentPosition);
                adapter.playVideoAt(currentPosition);
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
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
                if (isFinishing()) return;

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
                    recyclerViewVideos.post(() -> {
                        if (currentPosition < likedVideos.size()) {
                            recyclerViewVideos.scrollToPosition(currentPosition);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (isFinishing()) return;

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
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void hideSystemUI() {
        // Ẩn status bar và navigation bar để có trải nghiệm full-screen
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume video player nếu cần
        if (adapter != null) {
            adapter.resumeCurrentVideo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause video player nếu cần
        if (adapter != null) {
            adapter.pauseCurrentVideo();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.releasePlayer();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Animation khi back
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
