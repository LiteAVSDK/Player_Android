package com.tencent.liteav.demo.superplayer.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.liteav.demo.superplayer.R;

/**
 * The progress prompt view displayed when using swipe gestures to control playback progress
 *
 * 滑动手势控制播放进度时显示的进度提示view
 */
public class VideoProgressLayout extends RelativeLayout {
    private ImageView    mIvThumbnail;
    private TextView     mTvTime;
    private ProgressBar  mProgressBar;
    private HideRunnable mHideRunnable;
    private int          duration = 1000;

    public VideoProgressLayout(Context context) {
        super(context);
        init(context);
    }

    public VideoProgressLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.superplayer_video_progress_layout, this);
        mIvThumbnail = (ImageView) findViewById(R.id.superplayer_iv_progress_thumbnail);
        mProgressBar = (ProgressBar) findViewById(R.id.superplayer_pb_progress_bar);
        mTvTime = (TextView) findViewById(R.id.superplayer_tv_progress_time);
        setVisibility(GONE);
        mHideRunnable = new HideRunnable();
    }

    public void show() {
        setVisibility(VISIBLE);
        removeCallbacks(mHideRunnable);
        postDelayed(mHideRunnable, duration);
    }

    /**
     * Set the video progress event text
     *
     * 设置视频进度事件文本
     */
    public void setTimeText(String text) {
        mTvTime.setText(text);
    }

    /**
     * Set the progress value of the progress bar
     *
     * 设置progressbar的进度值
     */
    public void setProgress(int progress) {
        mProgressBar.setProgress(progress);
    }

    /**
     * Set the delay time for the view to disappear.
     *
     * 设置view消失延迟的时间
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Set the thumbnail image
     *
     * 设置缩略图图片
     */
    public void setThumbnail(Bitmap bitmap) {
        mIvThumbnail.setVisibility(VISIBLE);
        mIvThumbnail.setImageBitmap(bitmap);
    }

    /**
     * Set the thumbnail.
     *
     * 设置缩略图
     */
    public void hideThumbnail() {
        mIvThumbnail.setVisibility(GONE);
    }

    /**
     * Set the visibility of the progress bar.
     *
     * 设置progressbar的可见性
     */
    public void setProgressVisibility(boolean enable) {
        mProgressBar.setVisibility(enable ? VISIBLE : GONE);
    }

    /**
     * Thread for hiding the view.
     *
     * 隐藏view的线程
     */
    private class HideRunnable implements Runnable {
        @Override
        public void run() {
            mIvThumbnail.setImageBitmap(null);
            mIvThumbnail.setVisibility(GONE);
            VideoProgressLayout.this.setVisibility(GONE);
        }
    }
}
