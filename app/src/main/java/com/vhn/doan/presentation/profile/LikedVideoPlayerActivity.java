package com.vhn.doan.presentation.profile;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.vhn.doan.R;
import com.vhn.doan.presentation.shortvideo.ShortVideoFragment;

/**
 * Activity hiển thị danh sách video đã like theo dạng vuốt dọc giống TikTok.
 */
public class LikedVideoPlayerActivity extends AppCompatActivity {

    public static final String EXTRA_START_VIDEO_ID = "start_video_id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_videos_player);

        String startVideoId = getIntent().getStringExtra(EXTRA_START_VIDEO_ID);

        if (savedInstanceState == null) {
            ShortVideoFragment fragment = ShortVideoFragment.newInstanceForLiked(startVideoId);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }
}
