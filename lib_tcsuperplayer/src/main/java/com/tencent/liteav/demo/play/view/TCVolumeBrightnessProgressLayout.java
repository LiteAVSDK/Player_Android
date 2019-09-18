package com.tencent.liteav.demo.play.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.tencent.liteav.demo.play.R;


/**
 *
 */
public class TCVolumeBrightnessProgressLayout extends RelativeLayout {
    private static final String TAG = "SuperPlayerProgressLayout";
    private ImageView iv_center;
    private ProgressBar pb;
    private HideRunnable mHideRunnable;
    private int duration = 1000;

    public TCVolumeBrightnessProgressLayout(Context context) {
        super(context);
        init(context);
    }

    public TCVolumeBrightnessProgressLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        LayoutInflater.from(context).inflate(R.layout.video_volume_brightness_progress_layout,this);
        iv_center = (ImageView) findViewById(R.id.iv_center);
        pb = (ProgressBar) findViewById(R.id.progress_pb_bar);
        mHideRunnable = new HideRunnable();
        setVisibility(GONE);
    }

    //显示
    public void show(){
        setVisibility(VISIBLE);
        removeCallbacks(mHideRunnable);
        postDelayed(mHideRunnable,duration);
    }

    //设置进度
    public void setProgress(int progress){
        pb.setProgress(progress);
    }

    //设置持续时间
    public void setDuration(int duration) {
        this.duration = duration;
    }

    //设置显示图片
    public void setImageResource(int resource){
        iv_center.setImageResource(resource);
    }

    //隐藏自己的Runnable
    private class HideRunnable implements Runnable{
        @Override
        public void run() {
            TCVolumeBrightnessProgressLayout.this.setVisibility(GONE);
        }
    }
}
