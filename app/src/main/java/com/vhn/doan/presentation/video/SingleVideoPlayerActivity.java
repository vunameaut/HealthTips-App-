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

/**
 * Activity phát video đơn lẻ từ search results
 * Không cho phép lướt xuống các video khác như VideoActivity
 */
public class SingleVideoPlayerActivity extends AppCompatActivity {

    private static final String EXTRA_VIDEO_ID = "extra_video_id";

    private SingleVideoPlayerFragment videoPlayerFragment;

    /**
     * Intent factory method để start SingleVideoPlayerActivity
     */
    public static Intent createIntent(Context context, String videoId) {
        Intent intent = new Intent(context, SingleVideoPlayerActivity.class);
        intent.putExtra(EXTRA_VIDEO_ID, videoId);
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
        if (videoId != null) {
            videoPlayerFragment = SingleVideoPlayerFragment.newInstance(videoId);

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
