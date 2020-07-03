package com.tencent.liteav.demo.play.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.tencent.liteav.demo.play.R;


/**
 * 滑动手势设置音量、亮度时显示的提示view
 */
public class TCVolumeBrightnessProgressLayout extends RelativeLayout {
    private ImageView       mImageCenter;       // 中心图片：亮度提示、音量提示
    private ProgressBar     mProgressBar;       // 进度条
    private HideRunnable    mHideRunnable;      // 隐藏view的runnable
    private int             mDuration = 1000;   // view消失延迟时间(秒)

    public TCVolumeBrightnessProgressLayout(Context context) {
        super(context);
        init(context);
    }

    public TCVolumeBrightnessProgressLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        LayoutInflater.from(context).inflate(R.layout.superplayer_video_volume_brightness_progress_layout,this);
        mImageCenter = (ImageView) findViewById(R.id.superplayer_iv_center);
        mProgressBar = (ProgressBar) findViewById(R.id.superplayer_pb_progress_bar);
        mHideRunnable = new HideRunnable();
        setVisibility(GONE);
    }

    /**
     * 显示
     */
    public void show(){
        setVisibility(VISIBLE);
        removeCallbacks(mHideRunnable);
        postDelayed(mHideRunnable, mDuration);
    }

    /**
     * 设置progressBar的进度值
     *
     * @param progress
     */
    public void setProgress(int progress){
        mProgressBar.setProgress(progress);
    }

    /**
     * 设置view消失的延迟时间
     *
     * @param duration
     */
    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    /**
     * 设置显示的图片，亮度提示图片或者音量提示图片
     *
     * @param resource
     */
    public void setImageResource(int resource){
        mImageCenter.setImageResource(resource);
    }

    /**
     * 隐藏view的runnable
     */
    private class HideRunnable implements Runnable{
        @Override
        public void run() {
            TCVolumeBrightnessProgressLayout.this.setVisibility(GONE);
        }
    }
}
