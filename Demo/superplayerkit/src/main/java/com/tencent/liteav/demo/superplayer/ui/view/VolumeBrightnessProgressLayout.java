package com.tencent.liteav.demo.superplayer.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.tencent.liteav.demo.superplayer.R;


/**
 * The prompt view displayed when using swipe gestures to adjust volume and brightness
 *
 * 滑动手势设置音量、亮度时显示的提示view
 */
public class VolumeBrightnessProgressLayout extends RelativeLayout {
    private ImageView    mImageCenter;
    private ProgressBar  mProgressBar;
    private HideRunnable mHideRunnable;
    private int          mDuration = 1000;

    public VolumeBrightnessProgressLayout(Context context) {
        super(context);
        init(context);
    }

    public VolumeBrightnessProgressLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.superplayer_video_volume_brightness_progress_layout, this);
        mImageCenter = (ImageView) findViewById(R.id.superplayer_iv_center);
        mProgressBar = (ProgressBar) findViewById(R.id.superplayer_pb_progress_bar);
        mHideRunnable = new HideRunnable();
        setVisibility(GONE);
    }

    public void show() {
        setVisibility(VISIBLE);
        removeCallbacks(mHideRunnable);
        postDelayed(mHideRunnable, mDuration);
    }

    /**
     * Set the progress value of the progress bar
     *
     * 设置progressBar的进度值
     */
    public void setProgress(int progress) {
        mProgressBar.setProgress(progress);
    }

    /**
     * Set the delay time for the view to disappear.
     *
     * 设置view消失的延迟时间
     */
    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    /**
     * Set the displayed image, brightness prompt image or volume prompt image
     *
     * 设置显示的图片，亮度提示图片或者音量提示图片
     */
    public void setImageResource(int resource) {
        mImageCenter.setImageResource(resource);
    }

    /**
     * Runnable for hiding the view
     *
     * 隐藏view的runnable
     */
    private class HideRunnable implements Runnable {
        @Override
        public void run() {
            VolumeBrightnessProgressLayout.this.setVisibility(GONE);
        }
    }
}
