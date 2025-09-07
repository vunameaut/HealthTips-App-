package com.vhn.doan.presentation.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;

import java.io.Serializable;
import java.util.List;

/**
 * Activity phát video từ danh sách video đã like
 * Cho phép lướt giữa các video và ngăn màn hình sleep
 */
public class LikedVideosPlayerActivity extends AppCompatActivity {

    private static final String EXTRA_VIDEOS = "extra_videos";
    private static final String EXTRA_START_POSITION = "extra_start_position";

    private LikedVideosPlayerFragment playerFragment;

    /**
     * Intent factory method để start LikedVideosPlayerActivity
     */
    public static Intent createIntent(Context context, List<ShortVideo> videos, int startPosition) {
        Intent intent = new Intent(context, LikedVideosPlayerActivity.class);
        intent.putExtra(EXTRA_VIDEOS, (Serializable) videos);
        intent.putExtra(EXTRA_START_POSITION, startPosition);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_videos_player);

        setupFullscreen();
        setupKeepScreenOn();
        setupActionBar();
        setupPlayerFragment();
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

    /**
     * Ngăn màn hình sleep khi phát video
     */
    private void setupKeepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    @SuppressWarnings("unchecked")
    private void setupPlayerFragment() {
        // Lấy dữ liệu từ Intent
        List<ShortVideo> videos = (List<ShortVideo>) getIntent().getSerializableExtra(EXTRA_VIDEOS);
        int startPosition = getIntent().getIntExtra(EXTRA_START_POSITION, 0);

        // Tạo fragment player
        playerFragment = LikedVideosPlayerFragment.newInstance(videos, startPosition);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, playerFragment)
                .commit();
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btn_back_liked_videos);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Pause video trước khi exit
        if (playerFragment != null) {
            playerFragment.pauseCurrentVideo();
        }

        // Xóa flag keep screen on
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Đảm bảo màn hình không sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (playerFragment != null) {
            playerFragment.resumeCurrentVideo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (playerFragment != null) {
            playerFragment.pauseCurrentVideo();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Xóa flag keep screen on
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
