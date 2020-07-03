package com.tencent.liteav.demo.play.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.liteav.demo.play.R;

/**
 * 滑动手势控制播放进度时显示的进度提示view
 */

public class TCVideoProgressLayout extends RelativeLayout {
    private ImageView       mIvThumbnail;       // 视频缩略图
    private TextView        mTvTime;            // 视频进度文本
    private ProgressBar     mProgressBar;       // 进度条
    private HideRunnable    mHideRunnable;      // 隐藏自身的线程
    private int             duration = 1000;    // 自身消失的延迟事件ms

    public TCVideoProgressLayout(Context context) {
        super(context);
        init(context);
    }

    public TCVideoProgressLayout(Context context, AttributeSet attrs) {
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

    /**
     * 显示view
     */
    public void show() {
        setVisibility(VISIBLE);
        removeCallbacks(mHideRunnable);
        postDelayed(mHideRunnable, duration);
    }

    /**
     * 设置视频进度事件文本
     *
     * @param text
     */
    public void setTimeText(String text) {
        mTvTime.setText(text);
    }

    /**
     * 设置progressbar的进度值
     *
     * @param progress
     */
    public void setProgress(int progress) {
        mProgressBar.setProgress(progress);
    }

    /**
     * 设置view消失延迟的时间
     *
     * @param duration
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * 设置缩略图图片
     *
     * @param bitmap
     */
    public void setThumbnail(Bitmap bitmap) {
        mIvThumbnail.setVisibility(VISIBLE);
        mIvThumbnail.setImageBitmap(bitmap);
    }

    /**
     * 设置progressbar的可见性
     *
     * @param enable
     */
    public void setProgressVisibility(boolean enable) {
        mProgressBar.setVisibility(enable ? VISIBLE : GONE);
    }

    /**
     * 隐藏view的线程
     */
    private class HideRunnable implements Runnable {
        @Override
        public void run() {
            mIvThumbnail.setImageBitmap(null);
            mIvThumbnail.setVisibility(GONE);
            TCVideoProgressLayout.this.setVisibility(GONE);
        }
    }
}
