package com.tencent.liteav.demo.play.controller;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.play.R;
import com.tencent.liteav.demo.play.SuperPlayerConst;
import com.tencent.liteav.demo.play.view.TCPointSeekBar;
import com.tencent.liteav.demo.play.view.TCVideoProgressLayout;
import com.tencent.liteav.demo.play.view.TCVolumeBrightnessProgressLayout;

/**
 * Created by liyuejiao on 2018/7/3.
 * <p>
 * 超级播放器小窗口控制界面
 */
public class TCVodControllerSmall extends TCVodControllerBase implements View.OnClickListener {
    private static final String TAG = "TCVodControllerSmall";
    private LinearLayout mLayoutTop;
    private LinearLayout mLayoutBottom;
    private ImageView mIvPause;
    private ImageView mIvFullScreen;
//    private TextView mTvCurrent;
//    private TextView mTvDuration;
//    private SeekBar mSeekBarProgress;
    private TextView mTvTitle;
//    private LinearLayout mLayoutReplay;
    private TextView mTvBackToLive;
//    private ProgressBar mPbLiveLoading;
    private ImageView mBackground;
    private Bitmap mBackgroundBmp;
    private ImageView mIvWatermark;

    public TCVodControllerSmall(Context context) {
        super(context);
        initViews();
    }

    public TCVodControllerSmall(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public TCVodControllerSmall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    /**
     * 显示播放控制界面
     */
    @Override
    void onShow() {
        mLayoutTop.setVisibility(View.VISIBLE);
        mLayoutBottom.setVisibility(View.VISIBLE);

        if (mPlayType == SuperPlayerConst.PLAYTYPE_LIVE_SHIFT) {
            mTvBackToLive.setVisibility(View.VISIBLE);
        }

    }



    /**
     * 隐藏播放控制界面
     */
    @Override
    void onHide() {
        mLayoutTop.setVisibility(View.GONE);
        mLayoutBottom.setVisibility(View.GONE);

        if (mPlayType == SuperPlayerConst.PLAYTYPE_LIVE_SHIFT) {
            mTvBackToLive.setVisibility(View.GONE);
        }
    }

    /**
     * 进度定时器
     */
//    @Override
//    void onTimerTicker() {
//        switch (mPlayType) {
//            case SuperPlayerConst.PLAYTYPE_VOD:
//                updateVodVideoProgress();
//                break;
//            case SuperPlayerConst.PLAYTYPE_LIVE:
//                mTvCurrent.setText(TCTimeUtils.formattedTime(mLivePlayTime));
//                break;
//            case SuperPlayerConst.PLAYTYPE_LIVE_SHIFT:
//                mTvCurrent.setText(TCTimeUtils.formattedTime(mLiveShiftTime));
//                break;
//        }
//    }

    private void initViews() {
        mLayoutInflater.inflate(R.layout.vod_controller_small, this);

        mLayoutTop = (LinearLayout) findViewById(R.id.layout_top);
        mLayoutTop.setOnClickListener(this);
        mLayoutBottom = (LinearLayout) findViewById(R.id.layout_bottom);
        mLayoutBottom.setOnClickListener(this);
        mLayoutReplay = (LinearLayout) findViewById(R.id.layout_replay);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mIvPause = (ImageView) findViewById(R.id.iv_pause);
        mTvCurrent = (TextView) findViewById(R.id.tv_current);
        mTvDuration = (TextView) findViewById(R.id.tv_duration);
        mSeekBarProgress = (TCPointSeekBar) findViewById(R.id.seekbar_progress);
        mSeekBarProgress.setProgress(0);
        mSeekBarProgress.setMax(100);
        mIvFullScreen = (ImageView) findViewById(R.id.iv_fullscreen);
        mTvBackToLive = (TextView) findViewById(R.id.tv_backToLive);
        mPbLiveLoading = (ProgressBar) findViewById(R.id.pb_live);

        mTvBackToLive.setOnClickListener(this);
        mIvPause.setOnClickListener(this);
        mIvFullScreen.setOnClickListener(this);
        mLayoutTop.setOnClickListener(this);
        mLayoutReplay.setOnClickListener(this);

        mSeekBarProgress.setOnSeekBarChangeListener(this);

        mGestureVolumeBrightnessProgressLayout = (TCVolumeBrightnessProgressLayout)findViewById(R.id.gesture_progress);

        mGestureVideoProgressLayout = (TCVideoProgressLayout) findViewById(R.id.video_progress_layout);

        mBackground = (ImageView)findViewById(R.id.small_iv_background);
        setBackground(mBackgroundBmp);

        mIvWatermark = (ImageView)findViewById(R.id.small_iv_water_mark);
    }


    public void setBackground(final Bitmap bitmap) {
        this.post(new Runnable() {
            @Override
            public void run() {
                if (bitmap == null) return;
                if (mBackground == null) {
                    mBackgroundBmp = bitmap;
                } else {
                    setBitmap(mBackground, mBackgroundBmp);
                }
            }
        });
    }

    public void dismissBackground() {
        this.post(new Runnable() {
            @Override
            public void run() {
                if (mBackground.getVisibility() != View.VISIBLE) return;
                ValueAnimator alpha = ValueAnimator.ofFloat(1.0f, 0.0f);
                alpha.setDuration(500);
                alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (Float) animation.getAnimatedValue();
                        mBackground.setAlpha(value);
                        if (value == 0) {
                            mBackground.setVisibility(GONE);
                        }
                    }
                });
                alpha.start();
            }
        });
    }

    public void showBackground() {
        this.post(new Runnable() {
            @Override
            public void run() {
                ValueAnimator alpha = ValueAnimator.ofFloat(0.0f, 1);
                alpha.setDuration(500);
                alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (Float) animation.getAnimatedValue();
                        mBackground.setAlpha(value);
                        if (value == 1) {
                            mBackground.setVisibility(VISIBLE);
                        }
                    }
                });
                alpha.start();
            }
        });
    }


    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.layout_top) {
            onBack();

        } else if (i == R.id.iv_pause) {
            changePlayState();

        } else if (i == R.id.iv_fullscreen) {
            fullScreen();

        } else if (i == R.id.layout_replay) {
            replay();

        } else if (i == R.id.tv_backToLive) {
            backToLive();

        }
    }

    /**
     * 返回直播
     */
    private void backToLive() {
        mVodController.resumeLive();
    }



    /**
     * 返回窗口模式
     */
    private void onBack() {
        mVodController.onBackPress(SuperPlayerConst.PLAYMODE_WINDOW);
    }

    /**
     * 全屏
     */
    private void fullScreen() {
        mVodController.onRequestPlayMode(SuperPlayerConst.PLAYMODE_FULLSCREEN);
    }



//    @Override
//    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//
//    }
//
//    @Override
//    public void onStartTrackingTouch(SeekBar seekBar) {
//
//    }
//
//    @Override
//    public void onStopTrackingTouch(SeekBar seekBar) {
//        // 拖动seekbar结束时,获取seekbar当前进度,进行seek操作,最后更新seekbar进度
//        int curProgress = seekBar.getProgress();
//        int maxProgress = seekBar.getMax();
//
//        switch (mPlayType) {
//            case SuperPlayerConst.PLAYTYPE_VOD:
//                if (curProgress >= 0 && curProgress < maxProgress) {
//                    // 关闭重播按钮
//                    updateReplay(false);
//                    float percentage = ((float) curProgress) / maxProgress;
//                    int position = (int) (mVodController.getDuration() * percentage);
//                    mVodController.seekTo(position);
//                    mVodController.resume();
//                }
//                break;
//            case SuperPlayerConst.PLAYTYPE_LIVE:
//            case SuperPlayerConst.PLAYTYPE_LIVE_SHIFT:
//                updateLiveLoadingState(true);
////                mTrackTime = mLivePlayTime * curProgress / maxProgress;
//                TXCLog.i(TAG, "onStopTrackingTouch time:" + mTrackTime);
////                mVodController.onBackToRecord(mLiveBaseTime, mTrackTime);
//                break;
//        }
//    }

    /**
     * 更新直播加载ProgressBar状态
     *
     * @param loading
     */
//    public void updateLiveLoadingState(boolean loading) {
//        if (loading) {
//            mPbLiveLoading.setVisibility(View.VISIBLE);
//        } else {
//            mPbLiveLoading.setVisibility(View.GONE);
//        }
//    }

    /**
     * 更新点播播放进度
     */
//    public void updateVodVideoProgress() {
//        float curTime = mVodController.getCurrentPlaybackTime();
//        float durTime = mVodController.getDuration();
//
//        if (durTime > 0 && curTime <= durTime) {
//            float percentage = curTime / durTime;
//            if (percentage >= 0 && percentage <= 1) {
//                int progress = Math.round(percentage * mSeekBarProgress.getMax());
//                mSeekBarProgress.setProgress(progress);
//
//                if (durTime >= 0 && curTime <= durTime) {
//                    mTvCurrent.setText(TCTimeUtils.formattedTime((long) curTime));
//                    mTvDuration.setText(TCTimeUtils.formattedTime((long) durTime));
//                }
//            }
//        }
//    }

    /**
     * 更新播放UI
     *
     * @param isStart
     */
    public void updatePlayState(boolean isStart) {
        // 播放中
        if (isStart) {
            mIvPause.setImageResource(R.drawable.ic_vod_pause_normal);
        }
        // 未播放
        else {
            mIvPause.setImageResource(R.drawable.ic_vod_play_normal);
        }
    }

    /**
     * 更新标题
     *
     * @param title
     */
    public void updateTitle(String title) {
        super.updateTitle(title);
        mTvTitle.setText(mTitle);
    }

    /**
     * 更新重新播放按钮状态
     *
     * @param replay
     */
//    public void updateReplay(boolean replay) {
//        if (replay) {
//            mLayoutReplay.setVisibility(View.VISIBLE);
//        } else {
//            mLayoutReplay.setVisibility(View.GONE);
//        }
//    }

//    /**
//     * 更新直播播放时间和进度
//     *
//     * @param baseTime
//     */
//    public void updateLivePlayTime(long baseTime) {
//        super.updateLivePlayTime(baseTime);
//        mTvCurrent.setText(TCTimeUtils.formattedTime(mLivePlayTime));
//    }

    /**
     * 更新直播回看播放时间
     *
     * @param liveshiftTime
     */
//    public void updateLiveShiftPlayTime(long liveshiftTime) {
//        super.updateLiveShiftPlayTime(liveshiftTime);
//        mTvCurrent.setText(TCTimeUtils.formattedTime(mLiveShiftTime));
//    }

    /**
     * 更新播放类型
     *
     * @param playType
     */
    public void updatePlayType(int playType) {
        TXCLog.i(TAG, "updatePlayType playType:" + playType);
        super.updatePlayType(playType);
        switch (playType) {
            case SuperPlayerConst.PLAYTYPE_VOD:
                mTvBackToLive.setVisibility(View.GONE);
                mTvDuration.setVisibility(View.VISIBLE);
                break;
            case SuperPlayerConst.PLAYTYPE_LIVE:
                mTvBackToLive.setVisibility(View.GONE);
                mTvDuration.setVisibility(View.GONE);
                mSeekBarProgress.setProgress(100);
                break;
            case SuperPlayerConst.PLAYTYPE_LIVE_SHIFT:
                if (mLayoutBottom.getVisibility() == VISIBLE)
                    mTvBackToLive.setVisibility(View.VISIBLE);
                mTvDuration.setVisibility(View.GONE);
                break;
        }
    }



    @Override
    public void setWaterMarkBmp(final Bitmap bmp, final float xR, final float yR) {
        super.setWaterMarkBmp(bmp, xR, yR);
        if (bmp != null) {
            this.post(new Runnable() {
                @Override
                public void run() {
                    int width = TCVodControllerSmall.this.getWidth();
                    int height = TCVodControllerSmall.this.getHeight();

                    int x = (int) (width * xR) - bmp.getWidth() / 2;
                    int y = (int) (height * yR) - bmp.getHeight() / 2;

                    mIvWatermark.setX(x);
                    mIvWatermark.setY(y);

                    mIvWatermark.setVisibility(VISIBLE);
                    setBitmap(mIvWatermark, bmp);
                }
            });
        } else {
            mIvWatermark.setVisibility(GONE);
        }
    }

}
