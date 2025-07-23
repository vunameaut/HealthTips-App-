package com.vhn.doan.presentation.healthtip.detail;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Custom FrameLayout hỗ trợ swipe-to-dismiss functionality được cải thiện
 * Tạo hiệu ứng mượt mà như Instagram/Facebook stories với khả năng nhìn thấy activity phía sau
 */
public class SwipeToDismissLayout extends FrameLayout {

    private static final float DISMISS_THRESHOLD = 0.5f; // Tăng ngưỡng lên 50% để dễ dismiss hơn
    private static final float VELOCITY_THRESHOLD = 1200f; // Giảm ngưỡng velocity để dễ dismiss hơn
    private static final int ANIMATION_DURATION = 200; // Giảm thời gian animation để nhanh hơn
    private static final float DRAG_RESISTANCE = 0.85f; // Tăng resistance để cảm giác tự nhiên hơn

    private VelocityTracker velocityTracker;
    private float initialY;
    private boolean isDragging = false;
    private int touchSlop;
    private float dismissThresholdPx;
    private ValueAnimator currentAnimator;

    private OnDismissListener dismissListener;

    public interface OnDismissListener {
        void onDismiss();
        void onDragProgress(float progress); // progress từ 0.0 đến 1.0
    }

    public SwipeToDismissLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SwipeToDismissLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SwipeToDismissLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        ViewConfiguration configuration = ViewConfiguration.get(context);
        touchSlop = configuration.getScaledTouchSlop();

        // Thiết lập cho hardware acceleration
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        dismissThresholdPx = h * DISMISS_THRESHOLD;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Dừng animation hiện tại nếu có
                if (currentAnimator != null && currentAnimator.isRunning()) {
                    currentAnimator.cancel();
                }

                initialY = ev.getY();
                isDragging = false;

                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                } else {
                    velocityTracker.clear();
                }
                velocityTracker.addMovement(ev);
                return false;

            case MotionEvent.ACTION_MOVE:
                float deltaY = ev.getY() - initialY;
                velocityTracker.addMovement(ev);

                // Chỉ intercept khi kéo xuống và vượt qua touchSlop
                if (deltaY > touchSlop && !isDragging) {
                    isDragging = true;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                cleanupVelocityTracker();
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (velocityTracker != null) {
            velocityTracker.addMovement(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    float deltaY = event.getY() - initialY;
                    if (deltaY > 0) { // Chỉ cho phép kéo xuống
                        // Áp dụng resistance để tạo cảm giác tự nhiên
                        float resistedDeltaY = deltaY * DRAG_RESISTANCE;
                        setTranslationY(resistedDeltaY);

                        // Tính toán progress cho callback
                        float progress = Math.min(resistedDeltaY / dismissThresholdPx, 1.0f);

                        // Cập nhật alpha để tạo hiệu ứng fade mượt
                        float alpha = 1.0f - (progress * 0.3f); // Chỉ fade 30% thay vì quá nhiều
                        setAlpha(alpha);

                        // Thông báo progress cho listener
                        if (dismissListener != null) {
                            dismissListener.onDragProgress(progress);
                        }
                    }
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (isDragging) {
                    handleActionUp();
                    return true;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                if (isDragging) {
                    resetPosition();
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void handleActionUp() {
        float currentY = getTranslationY();
        float yVelocity = 0;

        // Tính toán velocity
        if (velocityTracker != null) {
            velocityTracker.computeCurrentVelocity(1000);
            yVelocity = velocityTracker.getYVelocity();
        }

        boolean shouldDismiss = false;

        // Kiểm tra điều kiện dismiss với ngưỡng thấp hơn
        if (currentY > dismissThresholdPx || yVelocity > VELOCITY_THRESHOLD) {
            shouldDismiss = true;
        }

        if (shouldDismiss) {
            dismissView();
        } else {
            resetPosition();
        }

        isDragging = false;
        getParent().requestDisallowInterceptTouchEvent(false);
        cleanupVelocityTracker();
    }

    private void dismissView() {
        if (currentAnimator != null && currentAnimator.isRunning()) {
            currentAnimator.cancel();
        }

        currentAnimator = ValueAnimator.ofFloat(getTranslationY(), getHeight());
        currentAnimator.setDuration(ANIMATION_DURATION);
        currentAnimator.setInterpolator(new DecelerateInterpolator());

        currentAnimator.addUpdateListener(animation -> {
            float value = (Float) animation.getAnimatedValue();
            setTranslationY(value);

            // Fade out effect
            float progress = value / getHeight();
            setAlpha(1.0f - progress);
        });

        currentAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (dismissListener != null) {
                    dismissListener.onDismiss();
                }
            }
        });

        currentAnimator.start();
    }

    private void resetPosition() {
        if (currentAnimator != null && currentAnimator.isRunning()) {
            currentAnimator.cancel();
        }

        currentAnimator = ValueAnimator.ofFloat(getTranslationY(), 0f);
        currentAnimator.setDuration(ANIMATION_DURATION);
        currentAnimator.setInterpolator(new DecelerateInterpolator());

        currentAnimator.addUpdateListener(animation -> {
            float value = (Float) animation.getAnimatedValue();
            setTranslationY(value);

            // Restore alpha
            float progress = 1.0f - (Math.abs(value) / dismissThresholdPx);
            setAlpha(0.7f + (progress * 0.3f)); // Smooth transition back to full opacity
        });

        currentAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                setAlpha(1.0f); // Đảm bảo alpha về 1.0
                // Reset progress
                if (dismissListener != null) {
                    dismissListener.onDragProgress(0.0f);
                }
            }
        });

        currentAnimator.start();
    }

    private void cleanupVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (currentAnimator != null && currentAnimator.isRunning()) {
            currentAnimator.cancel();
        }
        cleanupVelocityTracker();
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.dismissListener = listener;
    }
}
