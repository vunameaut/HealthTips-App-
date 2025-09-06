package com.vhn.doan.presentation.video;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;

/**
 * Activity phát video đơn lẻ từ search results
 * Không cho phép lướt xuống các video khác như VideoActivity
 */
public class SingleVideoPlayerActivity extends AppCompatActivity {

    private static final String EXTRA_VIDEO_ID = "extra_video_id";
    private static final String EXTRA_VIDEO_OBJECT = "extra_video_object";

    private SingleVideoPlayerFragment videoPlayerFragment;

    /**
     * Intent factory method để start SingleVideoPlayerActivity với video ID
     * @deprecated Sử dụng createIntent(Context, ShortVideo) để đảm bảo trạng thái like được truyền
     */
    @Deprecated
    public static Intent createIntent(Context context, String videoId) {
        Intent intent = new Intent(context, SingleVideoPlayerActivity.class);
        intent.putExtra(EXTRA_VIDEO_ID, videoId);
        return intent;
    }

    /**
     * Intent factory method để start SingleVideoPlayerActivity với video object đầy đủ
     * Đảm bảo trạng thái like được truyền chính xác
     */
    public static Intent createIntent(Context context, ShortVideo video) {
        Intent intent = new Intent(context, SingleVideoPlayerActivity.class);
        intent.putExtra(EXTRA_VIDEO_ID, video.getId());
        intent.putExtra(EXTRA_VIDEO_OBJECT, video);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_video_player);

        setupFullscreen();
        setupActionBar();
        setupVideoPlayerFragment();
        setupBackButton();
    }

    private void setupFullscreen() {
        // Ẩn status bar và navigation bar để có trải nghiệm fullscreen
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        // Ẩn action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(""); // Ẩn title
        }
    }

    private void setupVideoPlayerFragment() {
        String videoId = getIntent().getStringExtra(EXTRA_VIDEO_ID);
        ShortVideo videoObject = (ShortVideo) getIntent().getSerializableExtra(EXTRA_VIDEO_OBJECT);

        if (videoId != null) {
            // Nếu có video object đầy đủ, truyền nó vào fragment
            if (videoObject != null) {
                videoPlayerFragment = SingleVideoPlayerFragment.newInstance(videoId, videoObject);
            } else {
                // Fallback về method cũ nếu chỉ có videoId
                videoPlayerFragment = SingleVideoPlayerFragment.newInstance(videoId);
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, videoPlayerFragment);
            transaction.commit();
        }
    }

    private void setupBackButton() {
        ImageButton btnBackVideo = findViewById(R.id.btn_back_video);
        if (btnBackVideo != null) {
            btnBackVideo.setOnClickListener(v -> onBackPressed());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // Dừng video trước khi quay lại
        if (videoPlayerFragment != null) {
            videoPlayerFragment.pauseVideo();
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Tạm dừng video khi activity bị pause
        if (videoPlayerFragment != null) {
            videoPlayerFragment.pauseVideo();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Giải phóng resources khi destroy
        if (videoPlayerFragment != null) {
            videoPlayerFragment.releasePlayer();
        }
    }
}
