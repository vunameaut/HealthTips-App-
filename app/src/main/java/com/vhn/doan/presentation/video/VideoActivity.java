package com.vhn.doan.presentation.video;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.vhn.doan.R;

/**
 * VideoActivity hiển thị feed video short theo kiểu TikTok/Instagram Reels
 * Hỗ trợ fullscreen và gesture navigation
 */
public class VideoActivity extends AppCompatActivity {

    private static final String EXTRA_USER_ID = "extra_user_id";
    private static final String EXTRA_COUNTRY = "extra_country";
    private static final String EXTRA_START_POSITION = "extra_start_position";

    private VideoFragment videoFragment;

    /**
     * Intent factory method để start VideoActivity
     */
    public static Intent createIntent(Context context, String userId, String country) {
        Intent intent = new Intent(context, VideoActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        intent.putExtra(EXTRA_COUNTRY, country);
        return intent;
    }

    /**
     * Intent factory method để start VideoActivity với vị trí cụ thể
     */
    public static Intent createIntent(Context context, String userId, String country, int startPosition) {
        Intent intent = createIntent(context, userId, country);
        intent.putExtra(EXTRA_START_POSITION, startPosition);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        setupFullscreen();
        setupActionBar();
        setupVideoFragment();
        setupBackButton();
    }

    private void setupFullscreen() {
        // Ẩn status bar và navigation bar để có trải nghiệm fullscreen
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        // Ngăn màn hình sleep khi phát video
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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

    private void setupVideoFragment() {
        videoFragment = VideoFragment.newInstance();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, videoFragment);
        transaction.commit();
    }

    private void setupBackButton() {
        ImageButton btnBackVideo = findViewById(R.id.btn_back_video);
        btnBackVideo.setOnClickListener(v -> onBackPressed());
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
        // Pause video trước khi exit
        if (videoFragment != null) {
            // VideoFragment sẽ tự động pause video trong onPause()
        }

        // Kết thúc activity hiện tại để quay lại màn hình trước đó
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // VideoFragment sẽ tự động handle resume video
    }

    @Override
    protected void onPause() {
        super.onPause();
        // VideoFragment sẽ tự động handle pause video
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Xóa flag keep screen on khi activity bị destroy
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Public methods để control video từ bên ngoài (nếu cần)
     */
    public void nextVideo() {
        if (videoFragment != null) {
            videoFragment.nextVideo();
        }
    }

    public void previousVideo() {
        if (videoFragment != null) {
            videoFragment.previousVideo();
        }
    }

    public int getCurrentVideoPosition() {
        return videoFragment != null ? videoFragment.getCurrentPosition() : 0;
    }

    public int getVideoCount() {
        return videoFragment != null ? videoFragment.getVideoCount() : 0;
    }
}
