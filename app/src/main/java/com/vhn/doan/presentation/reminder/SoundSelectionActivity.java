package com.vhn.doan.presentation.reminder;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.ReminderSound;
import com.vhn.doan.presentation.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity để chọn âm thanh cho nhắc nhở
 */
public class SoundSelectionActivity extends BaseActivity implements SoundSelectionAdapter.OnSoundClickListener {

    public static final String EXTRA_SELECTED_SOUND_ID = "selected_sound_id";
    public static final String EXTRA_SELECTED_SOUND_NAME = "selected_sound_name";
    public static final String EXTRA_SELECTED_SOUND_URI = "selected_sound_uri";
    public static final String EXTRA_CURRENT_SOUND_ID = "current_sound_id";

    private RecyclerView recyclerView;
    private SoundSelectionAdapter adapter;
    private List<ReminderSound> soundList;
    private MediaPlayer mediaPlayer;
    private String currentSoundId;
    private int currentPlayingPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_selection);

        setupToolbar();
        initializeViews();
        loadSounds();
        setupRecyclerView();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.select_sound));
        }
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_view_sounds);
        currentSoundId = getIntent().getStringExtra(EXTRA_CURRENT_SOUND_ID);
    }

    private void loadSounds() {
        soundList = new ArrayList<>();

        // Âm thanh mặc định
        soundList.add(new ReminderSound(
            "default_alarm",
            "default_alarm",
            getString(R.string.sound_default),
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
            true,
            getString(R.string.sound_alarm),
            R.drawable.ic_alarm
        ));

        // Âm thanh thông báo
        soundList.add(new ReminderSound(
            "default_notification",
            "default_notification",
            getString(R.string.sound_notification),
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
            true,
            getString(R.string.sound_notification),
            R.drawable.ic_notification
        ));

        // Không âm thanh
        soundList.add(new ReminderSound(
            "none",
            "none",
            getString(R.string.sound_none),
            null,
            true,
            getString(R.string.sound_none),
            R.drawable.ic_volume_off
        ));

        // Các âm thanh tùy chỉnh có thể thêm ở đây
        addCustomSounds();
    }

    private void addCustomSounds() {
        // Âm thanh báo thức khác
        try {
            Uri alarmUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
            if (alarmUri != null) {
                soundList.add(new ReminderSound(
                    "system_alarm",
                    "system_alarm",
                    "Báo thức hệ thống",
                    alarmUri,
                    true,
                    getString(R.string.sound_alarm),
                    R.drawable.ic_alarm
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Âm thanh nhạc chuông
        try {
            Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE);
            if (ringtoneUri != null) {
                soundList.add(new ReminderSound(
                    "default_ringtone",
                    "default_ringtone",
                    "Nhạc chuông mặc định",
                    ringtoneUri,
                    true,
                    getString(R.string.sound_music),
                    R.drawable.ic_music_note
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupRecyclerView() {
        adapter = new SoundSelectionAdapter(soundList, currentSoundId, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSoundClick(ReminderSound sound, int position) {
        // Dừng âm thanh hiện tại nếu có
        stopCurrentSound();

        // Cập nhật selection
        adapter.setSelectedSound(sound.getId());

        // Trả kết quả
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SELECTED_SOUND_ID, sound.getId());
        resultIntent.putExtra(EXTRA_SELECTED_SOUND_NAME, sound.getDisplayName());
        if (sound.getSoundUri() != null) {
            resultIntent.putExtra(EXTRA_SELECTED_SOUND_URI, sound.getSoundUri().toString());
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onPreviewClick(ReminderSound sound, int position) {
        if (currentPlayingPosition == position) {
            // Đang phát âm thanh này, dừng lại
            stopCurrentSound();
        } else {
            // Phát âm thanh mới
            playSound(sound, position);
        }
    }

    private void playSound(ReminderSound sound, int position) {
        try {
            stopCurrentSound();

            if (sound.getSoundUri() == null) {
                Toast.makeText(this, "Không có âm thanh để phát", Toast.LENGTH_SHORT).show();
                return;
            }

            mediaPlayer = new MediaPlayer();

            // ✅ SỬA LỖI: Thiết lập các listener trước khi setDataSource
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                android.util.Log.e("SoundSelection", "❌ Lỗi MediaPlayer: what=" + what + ", extra=" + extra);
                Toast.makeText(this, "Không thể phát âm thanh này. Vui lòng thử âm thanh khác.", Toast.LENGTH_SHORT).show();
                currentPlayingPosition = -1;
                if (adapter != null) {
                    adapter.setPlayingPosition(-1);
                }
                return true; // Đã xử lý lỗi
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                android.util.Log.d("SoundSelection", "✅ Phát âm thanh hoàn tất");
                currentPlayingPosition = -1;
                if (adapter != null) {
                    adapter.setPlayingPosition(-1);
                }
            });

            // ✅ SỬA LỖI: Thiết lập OnPreparedListener để đảm bảo MediaPlayer sẵn sàng
            mediaPlayer.setOnPreparedListener(mp -> {
                try {
                    android.util.Log.d("SoundSelection", "✅ MediaPlayer đã sẵn sàng, bắt đầu phát âm thanh");
                    currentPlayingPosition = position;
                    if (adapter != null) {
                        adapter.setPlayingPosition(position);
                    }
                    mp.start();
                } catch (Exception e) {
                    android.util.Log.e("SoundSelection", "❌ Lỗi khi start MediaPlayer", e);
                    Toast.makeText(this, "Lỗi khi phát âm thanh", Toast.LENGTH_SHORT).show();
                    currentPlayingPosition = -1;
                    if (adapter != null) {
                        adapter.setPlayingPosition(-1);
                    }
                }
            });

            // Thiết lập AudioStreamType trước khi setDataSource
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);

            // ✅ SỬA LỖI: Xử lý các URI khác nhau một cách an toàn
            try {
                mediaPlayer.setDataSource(this, sound.getSoundUri());
                android.util.Log.d("SoundSelection", "🔊 Chuẩn bị phát âm thanh: " + sound.getDisplayName() + " - URI: " + sound.getSoundUri());

                // ✅ QUAN TRỌNG: Sử dụng prepareAsync() thay vì prepare() đồng bộ
                mediaPlayer.prepareAsync();

            } catch (Exception e) {
                android.util.Log.e("SoundSelection", "❌ Lỗi khi thiết lập DataSource", e);
                Toast.makeText(this, "Không thể tải âm thanh này: " + e.getMessage(), Toast.LENGTH_LONG).show();

                if (mediaPlayer != null) {
                    try {
                        mediaPlayer.release();
                    } catch (Exception ex) {
                        // Ignore
                    }
                    mediaPlayer = null;
                }
            }

        } catch (Exception e) {
            android.util.Log.e("SoundSelection", "❌ Lỗi tổng quát khi phát âm thanh", e);
            Toast.makeText(this, "Lỗi không xác định khi phát âm thanh", Toast.LENGTH_SHORT).show();
            currentPlayingPosition = -1;
            if (adapter != null) {
                adapter.setPlayingPosition(-1);
            }
        }
    }

    private void stopCurrentSound() {
        try {
            if (mediaPlayer != null) {
                android.util.Log.d("SoundSelection", "🛑 Dừng phát âm thanh hiện tại");
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            }

            currentPlayingPosition = -1;
            if (adapter != null) {
                adapter.setPlayingPosition(-1);
            }
        } catch (Exception e) {
            android.util.Log.e("SoundSelection", "❌ Lỗi khi dừng MediaPlayer", e);
            // Đảm bảo cleanup dù có lỗi
            mediaPlayer = null;
            currentPlayingPosition = -1;
            if (adapter != null) {
                adapter.setPlayingPosition(-1);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        stopCurrentSound();
        onBackPressed();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Dừng phát âm thanh khi activity pause
        stopCurrentSound();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Đảm bảo cleanup MediaPlayer khi destroy
        stopCurrentSound();
    }
}
