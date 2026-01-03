package com.vhn.doan.presentation.video.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.vhn.doan.R;

/**
 * Dialog hiển thị các tùy chọn cho video (Auto-scroll, Báo cáo, Tốc độ phát)
 */
public class VideoOptionsDialog extends Dialog {

    private SwitchMaterial switchAutoScroll;
    private LinearLayout btnHideUI;
    private LinearLayout btnReportVideo;
    private LinearLayout btnPlaybackSpeed;
    private TextView tvCurrentSpeed;
    private Button btnClose;

    private OnVideoOptionsListener listener;
    private boolean currentAutoScrollState;
    private float currentPlaybackSpeed = 1.0f;

    public interface OnVideoOptionsListener {
        void onAutoScrollChanged(boolean enabled);
        void onReportVideoClicked();
        void onPlaybackSpeedClicked();
        void onHideUIClicked();
    }

    public VideoOptionsDialog(@NonNull Context context, boolean autoScrollEnabled, boolean hideUIEnabled, float playbackSpeed, OnVideoOptionsListener listener) {
        super(context);
        this.currentAutoScrollState = autoScrollEnabled;
        this.currentPlaybackSpeed = playbackSpeed;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_video_options);

        initViews();
        setupListeners();
    }

    private void initViews() {
        switchAutoScroll = findViewById(R.id.switch_auto_scroll);
        btnHideUI = findViewById(R.id.btn_hide_ui);
        btnReportVideo = findViewById(R.id.btn_report_video);
        btnPlaybackSpeed = findViewById(R.id.btn_playback_speed);
        tvCurrentSpeed = findViewById(R.id.tv_current_speed);
        btnClose = findViewById(R.id.btn_close);

        // Set trạng thái auto-scroll hiện tại
        switchAutoScroll.setChecked(currentAutoScrollState);


        // Set tốc độ phát hiện tại
        updateSpeedText(currentPlaybackSpeed);
    }

    private void setupListeners() {
        // Auto-scroll switch
        switchAutoScroll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onAutoScrollChanged(isChecked);
            }
        });

        // Hide UI button - click để ẩn UI ngay lập tức (TikTok style)
        btnHideUI.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHideUIClicked();
            }
            // Đóng dialog ngay lập tức khi chọn ẩn UI
            dismiss();
        });

        // Tốc độ phát
        btnPlaybackSpeed.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlaybackSpeedClicked();
            }
        });

        // Báo cáo video
        btnReportVideo.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReportVideoClicked();
            }
            dismiss();
        });

        // Đóng dialog
        btnClose.setOnClickListener(v -> dismiss());
    }

    /**
     * Cập nhật trạng thái auto-scroll từ bên ngoài
     */
    public void updateAutoScrollState(boolean enabled) {
        if (switchAutoScroll != null) {
            switchAutoScroll.setChecked(enabled);
        }
    }


    /**
     * Cập nhật tốc độ phát hiện tại
     */
    public void updatePlaybackSpeed(float speed) {
        this.currentPlaybackSpeed = speed;
        updateSpeedText(speed);
    }

    /**
     * Cập nhật text hiển thị tốc độ phát
     */
    private void updateSpeedText(float speed) {
        if (tvCurrentSpeed == null) return;

        String speedText;
        if (speed == 0.25f) {
            speedText = getContext().getString(R.string.speed_0_25);
        } else if (speed == 0.5f) {
            speedText = getContext().getString(R.string.speed_0_5);
        } else if (speed == 0.75f) {
            speedText = getContext().getString(R.string.speed_0_75);
        } else if (speed == 1.0f) {
            speedText = getContext().getString(R.string.speed_normal);
        } else if (speed == 1.25f) {
            speedText = getContext().getString(R.string.speed_1_25);
        } else if (speed == 1.5f) {
            speedText = getContext().getString(R.string.speed_1_5);
        } else if (speed == 2.0f) {
            speedText = getContext().getString(R.string.speed_2_0);
        } else {
            speedText = speed + "x";
        }

        tvCurrentSpeed.setText(speedText);
    }
}

