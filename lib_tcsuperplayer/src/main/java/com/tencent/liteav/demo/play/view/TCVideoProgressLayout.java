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


public class TCVideoProgressLayout extends RelativeLayout {
    private static final String TAG = "SuperPlayerProgressLayout";
    private ImageView mIvThumbnail;
    private TextView mTvTime;
    private ProgressBar mProgressBar;
    private HideRunnable mHideRunnable;
    private int duration = 1000;

    public TCVideoProgressLayout(Context context) {
        super(context);
        init(context);
    }

    public TCVideoProgressLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.video_progress_layout, this);
        mIvThumbnail = (ImageView) findViewById(R.id.progress_iv_thumbnail);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_pb_bar);
        mTvTime = (TextView) findViewById(R.id.progress_tv_time);
        setVisibility(GONE);
        mHideRunnable = new HideRunnable();
    }

    //显示
    public void show() {
        setVisibility(VISIBLE);
        removeCallbacks(mHideRunnable);
        postDelayed(mHideRunnable, duration);
    }

    public void setTimeText(String text) {
        mTvTime.setText(text);
    }

    //设置进度
    public void setProgress(int progress) {
        mProgressBar.setProgress(progress);
    }

    //设置持续时间
    public void setDuration(int duration) {
        this.duration = duration;
    }

    //设置显示图片
    public void setThumbnail(Bitmap bitmap) {
        mIvThumbnail.setVisibility(VISIBLE);
        mIvThumbnail.setImageBitmap(bitmap);
    }

    public void setProgressVisibility(boolean enable) {
        mProgressBar.setVisibility(enable ? VISIBLE : GONE);
    }

    //隐藏自己的Runnable
    private class HideRunnable implements Runnable {
        @Override
        public void run() {
            mIvThumbnail.setImageBitmap(null);
            mIvThumbnail.setVisibility(GONE);
            TCVideoProgressLayout.this.setVisibility(GONE);
        }
    }
}
