package com.vhn.doan.presentation.shortvideo;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.vhn.doan.R;
import com.vhn.doan.data.ShortVideo;

import java.util.ArrayList;

/**
 * Activity hiển thị danh sách video ngắn toàn màn hình.
 * Nhận danh sách video và vị trí bắt đầu từ Intent.
 */
public class ShortVideoPlayerActivity extends AppCompatActivity {

    public static final String EXTRA_VIDEOS = "extra_videos";
    public static final String EXTRA_START_POSITION = "extra_start_position";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_short_video_player);

        if (savedInstanceState == null) {
            ArrayList<ShortVideo> videos = (ArrayList<ShortVideo>) getIntent().getSerializableExtra(EXTRA_VIDEOS);
            int startPosition = getIntent().getIntExtra(EXTRA_START_POSITION, 0);

            ShortVideoFragment fragment = ShortVideoFragment.newInstance(videos, startPosition);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }
}
