package com.vhn.doan.presentation.reminder;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.vhn.doan.R;
import com.vhn.doan.data.ReminderSound;

import java.util.List;

/**
 * Adapter cho danh sách âm thanh nhắc nhở với giao diện Material Design 3
 */
public class SoundSelectionAdapter extends RecyclerView.Adapter<SoundSelectionAdapter.SoundViewHolder> {

    private List<ReminderSound> soundList;
    private String selectedSoundId;
    private int playingPosition = -1;
    private OnSoundClickListener listener;

    public interface OnSoundClickListener {
        void onSoundClick(ReminderSound sound, int position);
        void onPreviewClick(ReminderSound sound, int position);
    }

    public SoundSelectionAdapter(List<ReminderSound> soundList, String selectedSoundId, OnSoundClickListener listener) {
        this.soundList = soundList;
        this.selectedSoundId = selectedSoundId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SoundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sound_selection, parent, false);
        return new SoundViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SoundViewHolder holder, int position) {
        ReminderSound sound = soundList.get(position);
        holder.bind(sound, position);
    }

    @Override
    public int getItemCount() {
        return soundList != null ? soundList.size() : 0;
    }

    public void setSelectedSound(String soundId) {
        this.selectedSoundId = soundId;
        notifyDataSetChanged();
    }

    public void setPlayingPosition(int position) {
        int previousPlaying = playingPosition;
        this.playingPosition = position;

        // Cập nhật UI cho item trước đó
        if (previousPlaying != -1) {
            notifyItemChanged(previousPlaying);
        }

        // Cập nhật UI cho item hiện tại
        if (position != -1) {
            notifyItemChanged(position);
        }
    }

    class SoundViewHolder extends RecyclerView.ViewHolder {

        private MaterialCardView cardSoundItem;
        private ImageView ivSoundIcon;
        private TextView tvSoundName;
        private TextView tvSoundDescription;
        private MaterialButton btnPreviewSound;
        private ImageView ivSelectedIndicator;
        private LinearLayout llWaveIndicator;

        public SoundViewHolder(@NonNull View itemView) {
            super(itemView);
            initializeViews(itemView);
            setupClickListeners();
        }

        private void initializeViews(View itemView) {
            cardSoundItem = itemView.findViewById(R.id.card_sound_item);
            ivSoundIcon = itemView.findViewById(R.id.iv_sound_icon);
            tvSoundName = itemView.findViewById(R.id.tv_sound_name);
            tvSoundDescription = itemView.findViewById(R.id.tv_sound_description);
            btnPreviewSound = itemView.findViewById(R.id.btn_preview_sound);
            ivSelectedIndicator = itemView.findViewById(R.id.iv_selected_indicator);
            llWaveIndicator = itemView.findViewById(R.id.ll_wave_indicator);
        }

        private void setupClickListeners() {
            // Click vào card để chọn âm thanh
            cardSoundItem.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSoundClick(soundList.get(position), position);
                }
            });

            // Click vào nút preview để nghe thử
            btnPreviewSound.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPreviewClick(soundList.get(position), position);
                }
            });
        }

        public void bind(ReminderSound sound, int position) {
            // Thiết lập thông tin âm thanh
            tvSoundName.setText(sound.getDisplayName());
            tvSoundDescription.setText(sound.getCategory());

            // Thiết lập icon âm thanh
            if (sound.getIconResId() != 0) {
                ivSoundIcon.setImageResource(sound.getIconResId());
            } else {
                ivSoundIcon.setImageResource(R.drawable.ic_music_note);
            }

            // Thiết lập trạng thái đã chọn với theme attributes
            boolean isSelected = sound.getId().equals(selectedSoundId);
            ivSelectedIndicator.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);

            // ✅ SỬA LỖI: Sử dụng theme attributes thay vì màu cố định
            Context context = itemView.getContext();
            if (isSelected) {
                // Sử dụng theme attributes cho màu được chọn
                int primaryColor = androidx.core.content.ContextCompat.getColor(context, R.color.primary_color);
                int primaryContainerColor = androidx.appcompat.content.res.AppCompatResources.getColorStateList(context, R.color.primary_container).getDefaultColor();

                cardSoundItem.setStrokeColor(primaryColor);
                cardSoundItem.setStrokeWidth(3);
                cardSoundItem.setCardBackgroundColor(primaryContainerColor);
            } else {
                // Sử dụng theme attributes cho màu bình thường
                int outlineColor = androidx.appcompat.content.res.AppCompatResources.getColorStateList(context, R.color.outline_variant).getDefaultColor();

                // Tự động lấy màu surface từ theme hiện tại
                android.util.TypedValue typedValue = new android.util.TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.colorBackground, typedValue, true);
                int surfaceColor = typedValue.data;

                cardSoundItem.setStrokeColor(outlineColor);
                cardSoundItem.setStrokeWidth(1);
                cardSoundItem.setCardBackgroundColor(surfaceColor);
            }

            // Thiết lập trạng thái đang phát
            boolean isPlaying = (position == playingPosition);
            llWaveIndicator.setVisibility(isPlaying ? View.VISIBLE : View.GONE);

            // Cập nhật icon nút preview
            if (isPlaying) {
                btnPreviewSound.setIcon(itemView.getContext().getDrawable(R.drawable.ic_pause));
                btnPreviewSound.setContentDescription("Dừng phát");
                startWaveAnimation();
            } else {
                btnPreviewSound.setIcon(itemView.getContext().getDrawable(R.drawable.ic_play_arrow));
                btnPreviewSound.setContentDescription("Nghe thử");
                stopWaveAnimation();
            }

            // Xử lý trường hợp âm thanh "Không âm thanh"
            if ("none".equals(sound.getId())) {
                btnPreviewSound.setVisibility(View.GONE);
                ivSoundIcon.setImageResource(R.drawable.ic_volume_off);
                tvSoundDescription.setText("Im lặng");
            } else {
                btnPreviewSound.setVisibility(View.VISIBLE);
            }
        }

        private void startWaveAnimation() {
            if (llWaveIndicator.getVisibility() == View.VISIBLE) {
                // Animation cho các thanh wave
                for (int i = 0; i < llWaveIndicator.getChildCount(); i++) {
                    View bar = llWaveIndicator.getChildAt(i);
                    ObjectAnimator animator = ObjectAnimator.ofFloat(bar, "scaleY", 0.3f, 1.0f, 0.3f);
                    animator.setDuration(800);
                    animator.setStartDelay(i * 100);
                    animator.setRepeatCount(ObjectAnimator.INFINITE);
                    animator.start();
                }
            }
        }

        private void stopWaveAnimation() {
            if (llWaveIndicator != null) {
                for (int i = 0; i < llWaveIndicator.getChildCount(); i++) {
                    View bar = llWaveIndicator.getChildAt(i);
                    bar.clearAnimation();
                    bar.setScaleY(1.0f);
                }
            }
        }
    }
}
