package com.tencent.liteav.demo.play.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.liteav.demo.play.R;
import com.tencent.liteav.demo.play.SuperPlayerConst;
import com.tencent.liteav.demo.play.bean.TCPlayImageSpriteInfo;
import com.tencent.liteav.demo.play.bean.TCPlayKeyFrameDescInfo;
import com.tencent.liteav.demo.play.net.TCLogReport;
import com.tencent.liteav.demo.play.utils.TCTimeUtil;
import com.tencent.liteav.demo.play.utils.TCVideoGestureUtil;
import com.tencent.liteav.demo.play.view.TCPointSeekBar;
import com.tencent.liteav.demo.play.view.TCVideoProgressLayout;
import com.tencent.liteav.demo.play.bean.TCVideoQuality;
import com.tencent.liteav.demo.play.view.TCVodMoreView;
import com.tencent.liteav.demo.play.view.TCVodQualityView;
import com.tencent.liteav.demo.play.view.TCVolumeBrightnessProgressLayout;
import com.tencent.rtmp.TXImageSprite;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 全屏模式播放控件
 *
 * 除{@link TCControllerWindow}基本功能外，还包括进度条关键帧打点信息显示与跳转、快进快退时缩略图的显示、切换画质
 * 镜像播放、硬件加速、倍速播放、弹幕、截图等功能
 *
 * 1、点击事件监听{@link #onClick(View)}
 *
 * 2、触摸事件监听{@link #onTouchEvent(MotionEvent)}
 *
 * 3、进度条滑动事件监听{@link #onProgressChanged(TCPointSeekBar, int, boolean)}
 *                    {@link #onStartTrackingTouch(TCPointSeekBar)}{@link #onStopTrackingTouch(TCPointSeekBar)}
 *
 * 4、进度条打点信息点击监听{@link #onSeekBarPointClick(View, int)}
 *
 * 5、切换画质监听{@link #onQualitySelect(TCVideoQuality)}
 *
 * 6、倍速播放监听{@link #onSpeedChange(float)}
 *
 * 7、镜像播放监听{@link #onMirrorChange(boolean)}
 *
 * 8、硬件加速监听{@link #onHWAcceleration(boolean)}
 *
 */
public class TCControllerFullScreen extends RelativeLayout implements IController, View.OnClickListener,
        TCVodMoreView.Callback, TCVodQualityView.Callback, TCPointSeekBar.OnSeekBarChangeListener, TCPointSeekBar.OnSeekBarPointClickListener{

    // UI控件
    private RelativeLayout                      mLayoutTop;                             // 顶部标题栏布局
    private LinearLayout                        mLayoutBottom;                          // 底部进度条所在布局
    private ImageView                           mIvPause;                               // 暂停播放按钮
    private TextView                            mTvTitle;                               // 视频名称文本
    private TextView                            mTvBackToLive;                          // 返回直播文本
    private ImageView                           mIvWatermark;                           // 水印
    private TextView                            mTvCurrent;                             // 当前进度文本
    private TextView                            mTvDuration;                            // 总时长文本
    private TCPointSeekBar                      mSeekBarProgress;                       // 播放进度条
    private LinearLayout                        mLayoutReplay;                          // 重播按钮所在布局
    private ProgressBar                         mPbLiveLoading;                         // 加载圈
    private TCVolumeBrightnessProgressLayout    mGestureVolumeBrightnessProgressLayout; // 音量亮度调节布局
    private TCVideoProgressLayout               mGestureVideoProgressLayout;            // 手势快进提示布局

    private TextView                            mTvQuality;                             // 当前画质文本
    private ImageView                           mIvBack;                                // 顶部标题栏中的返回按钮
    private ImageView                           mIvDanmu;                               // 弹幕按钮
    private ImageView                           mIvSnapshot;                            // 截屏按钮
    private ImageView                           mIvLock;                                // 锁屏按钮
    private ImageView                           mIvMore;                                // 更多设置弹窗按钮
    private TCVodQualityView                    mVodQualityView;                        // 画质列表弹窗
    private TCVodMoreView                       mVodMoreView;                           // 更多设置弹窗
    private TextView                            mTvVttText;                             // 关键帧打点信息文本

    private IControllerCallback                 mControllerCallback;                    // 播放控制回调
    private HideViewControllerViewRunnable      mHideViewRunnable;                      // 隐藏控件子线程
    private HideLockViewRunnable                mHideLockViewRunnable;                  // 隐藏锁屏按钮子线程
    private GestureDetector                     mGestureDetector;                       // 手势检测监听器
    private TCVideoGestureUtil                  mVideoGestureUtil;                      // 手势控制工具

    private boolean                             isShowing;                              // 自身是否可见
    private boolean                             mIsChangingSeekBarProgress;             // 进度条是否正在拖动，避免SeekBar由于视频播放的update而跳动
    private int                                 mPlayType;                              // 当前播放视频类型
    private int                                 mCurrentPlayState = -1;                 // 当前播放状态
    private long                                mDuration;                              // 视频总时长
    private long                                mLivePushDuration;                      // 直播推流总时长
    private long                                mProgress;                              // 当前播放进度

    private Bitmap                              mBackgroundBmp;                         // 背景图
    private Bitmap                              mWaterMarkBmp;                          // 水印图
    private float                               mWaterMarkBmpX;                         // 水印x坐标
    private float                               mWaterMarkBmpY;                         // 水印y坐标

    private boolean                             mBarrageOn;                             // 弹幕是否开启
    private boolean                             mLockScreen;                            // 是否锁屏
    private TXImageSprite                       mTXImageSprite;                         // 雪碧图信息
    private List<TCPlayKeyFrameDescInfo>        mTXPlayKeyFrameDescInfoList;            // 关键帧信息
    private int                                 mSelectedPos = -1;                      // 点击的关键帧时间点

    private TCVideoQuality                      mDefaultVideoQuality;                   // 默认画质
    private List<TCVideoQuality>                mVideoQualityList;                      // 画质列表
    private boolean                             mFirstShowQuality;                      // 是都是首次显示画质信息

    public TCControllerFullScreen(Context context) {
        super(context);
        init(context);
    }

    public TCControllerFullScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TCControllerFullScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化控件、手势检测监听器、亮度/音量/播放进度的回调
     */
    private void init(Context context) {
        initView(context);
        mHideViewRunnable = new HideViewControllerViewRunnable(this);
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (mLockScreen) return false;
                togglePlayState();
                show();
                if (mHideViewRunnable != null) {
                    TCControllerFullScreen.this.getHandler().removeCallbacks(mHideViewRunnable);
                    TCControllerFullScreen.this.getHandler().postDelayed(mHideViewRunnable, 7000);
                }
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                toggle();
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
                if (mLockScreen) return false;
                if (downEvent == null || moveEvent == null) {
                    return false;
                }
                if (mVideoGestureUtil != null && mGestureVolumeBrightnessProgressLayout != null) {
                    mVideoGestureUtil.check(mGestureVolumeBrightnessProgressLayout.getHeight(), downEvent, moveEvent, distanceX, distanceY);
                }
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                if (mLockScreen) return true;
                if (mVideoGestureUtil != null) {
                    mVideoGestureUtil.reset(getWidth(), mSeekBarProgress.getProgress());
                }
                return true;
            }

        });
        mGestureDetector.setIsLongpressEnabled(false);

        mVideoGestureUtil = new TCVideoGestureUtil(getContext());
        mVideoGestureUtil.setVideoGestureListener(new TCVideoGestureUtil.VideoGestureListener() {
            @Override
            public void onBrightnessGesture(float newBrightness) {
                if (mGestureVolumeBrightnessProgressLayout != null) {
                    mGestureVolumeBrightnessProgressLayout.setProgress((int) (newBrightness * 100));
                    mGestureVolumeBrightnessProgressLayout.setImageResource(R.drawable.superplayer_ic_light_max);
                    mGestureVolumeBrightnessProgressLayout.show();
                }
            }

            @Override
            public void onVolumeGesture(float volumeProgress) {
                if (mGestureVolumeBrightnessProgressLayout != null) {
                    mGestureVolumeBrightnessProgressLayout.setImageResource(R.drawable.superplayer_ic_volume_max);
                    mGestureVolumeBrightnessProgressLayout.setProgress((int) volumeProgress);
                    mGestureVolumeBrightnessProgressLayout.show();
                }
            }

            @Override
            public void onSeekGesture(int progress) {
                mIsChangingSeekBarProgress = true;
                if (mGestureVideoProgressLayout != null) {

                    if (progress > mSeekBarProgress.getMax()) {
                        progress = mSeekBarProgress.getMax();
                    }
                    if (progress < 0) {
                        progress = 0;
                    }
                    mGestureVideoProgressLayout.setProgress(progress);
                    mGestureVideoProgressLayout.show();

                    float percentage = ((float) progress) / mSeekBarProgress.getMax();
                    float currentTime = (mDuration * percentage);
                    if (mPlayType == SuperPlayerConst.PLAYTYPE_LIVE || mPlayType == SuperPlayerConst.PLAYTYPE_LIVE_SHIFT) {
                        if (mLivePushDuration > SuperPlayerConst.MAX_SHIFT_TIME) {
                            currentTime = (int) (mLivePushDuration - SuperPlayerConst.MAX_SHIFT_TIME *  (1 - percentage));
                        } else {
                            currentTime  = mLivePushDuration * percentage;
                        }
                        mGestureVideoProgressLayout.setTimeText(TCTimeUtil.formattedTime((long) currentTime));
                    } else {
                        mGestureVideoProgressLayout.setTimeText(TCTimeUtil.formattedTime((long) currentTime) + " / " + TCTimeUtil.formattedTime((long) mDuration));
                    }
                    setThumbnail(progress);
                }
                if (mSeekBarProgress!= null)
                    mSeekBarProgress.setProgress(progress);
            }
        });
    }

    /**
     * 初始化view
     */
    private void initView(Context context) {
        mHideLockViewRunnable = new HideLockViewRunnable(this);
        LayoutInflater.from(context).inflate(R.layout.superplayer_vod_controller_fullscreen, this);

        mLayoutTop = (RelativeLayout) findViewById(R.id.superplayer_rl_top);
        mLayoutTop.setOnClickListener(this);
        mLayoutBottom = (LinearLayout) findViewById(R.id.superplayer_ll_bottom);
        mLayoutBottom.setOnClickListener(this);
        mLayoutReplay = (LinearLayout) findViewById(R.id.superplayer_ll_replay);

        mIvBack = (ImageView) findViewById(R.id.superplayer_iv_back);
        mIvLock = (ImageView) findViewById(R.id.superplayer_iv_lock);
        mTvTitle = (TextView) findViewById(R.id.superplayer_tv_title);
        mIvPause = (ImageView) findViewById(R.id.superplayer_iv_pause);
        mIvDanmu = (ImageView) findViewById(R.id.superplayer_iv_danmuku);
        mIvMore = (ImageView) findViewById(R.id.superplayer_iv_more);
        mIvSnapshot = (ImageView) findViewById(R.id.superplayer_iv_snapshot);
        mTvCurrent = (TextView) findViewById(R.id.superplayer_tv_current);
        mTvDuration = (TextView) findViewById(R.id.superplayer_tv_duration);

        mSeekBarProgress = (TCPointSeekBar) findViewById(R.id.superplayer_seekbar_progress);
        mSeekBarProgress.setProgress(0);
        mSeekBarProgress.setOnPointClickListener(this);
        mSeekBarProgress.setOnSeekBarChangeListener(this);
        mTvQuality = (TextView) findViewById(R.id.superplayer_tv_quality);
        mTvBackToLive = (TextView) findViewById(R.id.superplayer_tv_back_to_live);
        mPbLiveLoading = (ProgressBar) findViewById(R.id.superplayer_pb_live);

        mVodQualityView = (TCVodQualityView) findViewById(R.id.superplayer_vod_quality);
        mVodQualityView.setCallback(this);
        mVodMoreView = (TCVodMoreView) findViewById(R.id.superplayer_vod_more);
        mVodMoreView.setCallback(this);

        mTvBackToLive.setOnClickListener(this);
        mLayoutReplay.setOnClickListener(this);
        mIvLock.setOnClickListener(this);
        mIvBack.setOnClickListener(this);
        mIvPause.setOnClickListener(this);
        mIvDanmu.setOnClickListener(this);
        mIvSnapshot.setOnClickListener(this);
        mIvMore.setOnClickListener(this);
        mTvQuality.setOnClickListener(this);
        mTvVttText = (TextView) findViewById(R.id.superplayer_large_tv_vtt_text);
        mTvVttText.setOnClickListener(this);
        if (mDefaultVideoQuality != null) {
            mTvQuality.setText(mDefaultVideoQuality.title);
        }
        mGestureVolumeBrightnessProgressLayout = (TCVolumeBrightnessProgressLayout) findViewById(R.id.superplayer_gesture_progress);
        mGestureVideoProgressLayout = (TCVideoProgressLayout) findViewById(R.id.superplayer_video_progress_layout);
        mIvWatermark = (ImageView) findViewById(R.id.superplayer_large_iv_water_mark);
    }

    /**
     * 设置控件的可见性
     *
     * @param view      目标控件
     * @param isVisible 显示：true 隐藏：false
     */
    private void toggleView(View view, boolean isVisible) {
        view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * 切换播放状态
     *
     * 双击和点击播放/暂停按钮会触发此方法
     */
    private void togglePlayState() {
        switch (mCurrentPlayState) {
            case SuperPlayerConst.PLAYSTATE_PAUSE:
            case SuperPlayerConst.PLAYSTATE_END:
                if (mControllerCallback != null) {
                    mControllerCallback.onResume();
                }
                break;
            case SuperPlayerConst.PLAYSTATE_PLAYING:
            case SuperPlayerConst.PLAYSTATE_LOADING:
                if (mControllerCallback != null) {
                    mControllerCallback.onPause();
                }
                mLayoutReplay.setVisibility(View.GONE);
                break;
        }
        show();
    }


    /**
     * 切换自身的可见性
     */
    private void toggle() {
        if (!mLockScreen) {
            if (isShowing) {
                hide();
            } else {
                show();
                if (mHideViewRunnable != null) {
                    getHandler().removeCallbacks(mHideViewRunnable);
                    getHandler().postDelayed(mHideViewRunnable, 7000);
                }
            }
        } else {
            mIvLock.setVisibility(VISIBLE);
            if (mHideLockViewRunnable!=null) {
                getHandler().removeCallbacks(mHideLockViewRunnable);
                getHandler().postDelayed(mHideLockViewRunnable, 7000);
            }
        }
        if (mVodMoreView.getVisibility() == VISIBLE) {
            mVodMoreView.setVisibility(GONE);
        }
    }

    /**
     * 设置回调
     *
     * @param callback 回调接口实现对象
     */
    @Override
    public void setCallback(IControllerCallback callback) {
        mControllerCallback = callback;
    }

    /**
     * 设置水印
     *
     * @param bmp 水印图
     * @param x   水印的x坐标
     * @param y   水印的y坐标
     */
    @Override
    public void setWatermark(Bitmap bmp, float x, float y) {
        mWaterMarkBmp = bmp;
        mWaterMarkBmpY = y;
        mWaterMarkBmpX = x;
    }

    /**
     * 显示控件
     */
    @Override
    public void show() {
        isShowing = true;
        mLayoutTop.setVisibility(View.VISIBLE);
        mLayoutBottom.setVisibility(View.VISIBLE);
        if (mHideLockViewRunnable!=null) {
            this.getHandler().removeCallbacks(mHideLockViewRunnable);
        }
        mIvLock.setVisibility(VISIBLE);
        if (mPlayType == SuperPlayerConst.PLAYTYPE_LIVE_SHIFT) {
            if (mLayoutBottom.getVisibility() == VISIBLE)
                mTvBackToLive.setVisibility(View.VISIBLE);
        }
        List<TCPointSeekBar.PointParams> pointParams = new ArrayList<>();
        if (mTXPlayKeyFrameDescInfoList != null)
            for (TCPlayKeyFrameDescInfo info : mTXPlayKeyFrameDescInfoList) {
                int progress = (int) (info.time / mDuration * mSeekBarProgress.getMax());
                pointParams.add(new TCPointSeekBar.PointParams(progress, Color.WHITE));
            }
        mSeekBarProgress.setPointList(pointParams);
    }

    /**
     * 隐藏控件
     */
    @Override
    public void hide() {
        isShowing = false;
        mLayoutTop.setVisibility(View.GONE);
        mLayoutBottom.setVisibility(View.GONE);
        mVodQualityView.setVisibility(View.GONE);
        mTvVttText.setVisibility(GONE);
        mIvLock.setVisibility(GONE);
        if (mPlayType == SuperPlayerConst.PLAYTYPE_LIVE_SHIFT) {
            mTvBackToLive.setVisibility(View.GONE);
        }
    }

    /**
     * 释放控件的内存
     */
    @Override
    public void release() {
        releaseTXImageSprite();
    }

    /**
     * 更新播放状态
     *
     * @param playState 正在播放{@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYSTATE_PLAYING}
     *                  正在加载{@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYSTATE_LOADING}
     *                  暂停   {@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYSTATE_PAUSE}
     *                  播放结束{@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYSTATE_END}
     */
    @Override
    public void updatePlayState(int playState) {
        switch (playState) {
            case SuperPlayerConst.PLAYSTATE_PLAYING:
                mIvPause.setImageResource(R.drawable.superplayer_ic_vod_pause_normal);
                toggleView(mPbLiveLoading, false);
                toggleView(mLayoutReplay, false);
                break;
            case SuperPlayerConst.PLAYSTATE_LOADING:
                mIvPause.setImageResource(R.drawable.superplayer_ic_vod_pause_normal);
                toggleView(mPbLiveLoading, true);
                toggleView(mLayoutReplay, false);
                break;
            case SuperPlayerConst.PLAYSTATE_PAUSE:
                mIvPause.setImageResource(R.drawable.superplayer_ic_vod_play_normal);
                toggleView(mLayoutReplay, false);
                break;
            case SuperPlayerConst.PLAYSTATE_END:
                mIvPause.setImageResource(R.drawable.superplayer_ic_vod_play_normal);
                toggleView(mLayoutReplay, true);
                break;
        }
        mCurrentPlayState = playState;
    }

    /**
     * 设置视频画质信息
     *
     * @param list 画质列表
     */
    @Override
    public void setVideoQualityList(List<TCVideoQuality> list) {
        mVideoQualityList = list;
        mFirstShowQuality = false;
    }

    /**
     * 更新视频名称
     *
     * @param title 视频名称
     */
    @Override
    public void updateTitle(String title) {
        mTvTitle.setText(title);
    }

    /**
     * 更新是屁播放进度
     *
     * @param current  当前进度(秒)
     * @param duration 视频总时长(秒)
     */
    @Override
    public void updateVideoProgress(long current, long duration) {
        mProgress = current < 0 ? 0 : current;
        mDuration = duration < 0 ? 0 : duration;
        mTvCurrent.setText(TCTimeUtil.formattedTime(mProgress));

        float percentage = mDuration > 0 ? ((float) mProgress / (float) mDuration) : 1.0f;
        if (mProgress == 0) {
            mLivePushDuration = 0;
            percentage = 0;
        }
        if (mPlayType == SuperPlayerConst.PLAYTYPE_LIVE || mPlayType == SuperPlayerConst.PLAYTYPE_LIVE_SHIFT) {
            mLivePushDuration = mLivePushDuration > mProgress ? mLivePushDuration : mProgress;
            long leftTime = mDuration - mProgress;
            mDuration = mDuration > SuperPlayerConst.MAX_SHIFT_TIME ? SuperPlayerConst.MAX_SHIFT_TIME : mDuration;
            percentage = 1 - (float) leftTime / (float) mDuration;
        }

        if (percentage >= 0 && percentage <= 1) {
            int progress = Math.round(percentage * mSeekBarProgress.getMax());
            if (!mIsChangingSeekBarProgress)
                mSeekBarProgress.setProgress(progress);
            mTvDuration.setText(TCTimeUtil.formattedTime(mDuration));
        }
    }

    /**
     * 更新播放类型
     *
     * @param type 点播     {@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYTYPE_VOD}
     *             点播     {@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYTYPE_LIVE}
     *             直播回看  {@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYTYPE_LIVE_SHIFT}
     */
    @Override
    public void updatePlayType(int type) {
        mPlayType = type;
        switch (type) {
            case SuperPlayerConst.PLAYTYPE_VOD:
                mTvBackToLive.setVisibility(View.GONE);
                mVodMoreView.updatePlayType(SuperPlayerConst.PLAYTYPE_VOD);
                mTvDuration.setVisibility(View.VISIBLE);
                break;
            case SuperPlayerConst.PLAYTYPE_LIVE:
                mTvBackToLive.setVisibility(View.GONE);
                mTvDuration.setVisibility(View.GONE);
                mVodMoreView.updatePlayType(SuperPlayerConst.PLAYTYPE_LIVE);
                mSeekBarProgress.setProgress(100);
                break;
            case SuperPlayerConst.PLAYTYPE_LIVE_SHIFT:
                if (mLayoutBottom.getVisibility() == VISIBLE) {
                    mTvBackToLive.setVisibility(View.VISIBLE);
                }
                mTvDuration.setVisibility(View.GONE);
                mVodMoreView.updatePlayType(SuperPlayerConst.PLAYTYPE_LIVE_SHIFT);
                break;
        }
    }

    /**
     * 设置背景
     *
     * @param bitmap 背景图
     */
    @Override
    public void setBackground(Bitmap bitmap) {

    }

    /**
     * 显示背景
     */
    @Override
    public void showBackground() {

    }

    /**
     * 隐藏背景
     */
    @Override
    public void hideBackground() {

    }

    /**
     * 更新视频播放画质
     *
     * @param videoQuality 画质
     */
    @Override
    public void updateVideoQuality(TCVideoQuality videoQuality) {
        if(videoQuality==null){
            mTvQuality.setText("");
            return;
        }
        mDefaultVideoQuality = videoQuality;
        if (mTvQuality != null) {
            mTvQuality.setText(videoQuality.title);
        }
        if (mVideoQualityList != null && mVideoQualityList.size() != 0) {
            for (int i = 0 ; i  < mVideoQualityList.size(); i++) {
                TCVideoQuality quality = mVideoQualityList.get(i);
                if (quality!=null && quality.title!=null &&quality.title.equals(mDefaultVideoQuality.title)) {
                    mVodQualityView.setDefaultSelectedQuality(i);
                    break;
                }
            }
        }
    }

    /**
     * 更新雪碧图信息
     *
     * @param info 雪碧图信息
     */
    @Override
    public void updateImageSpriteInfo(TCPlayImageSpriteInfo info) {
        if (mTXImageSprite != null) {
            releaseTXImageSprite();
        }
        // 有缩略图的时候不显示进度
        mGestureVideoProgressLayout.setProgressVisibility(info == null || info.imageUrls == null || info.imageUrls.size() == 0);
        if (mPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
            mTXImageSprite = new TXImageSprite(getContext());
            if (info != null) {
                // 雪碧图ELK上报
                TCLogReport.getInstance().uploadLogs(TCLogReport.ELK_ACTION_IMAGE_SPRITE, 0, 0);
                mTXImageSprite.setVTTUrlAndImageUrls(info.webVttUrl, info.imageUrls);
            } else {
                mTXImageSprite.setVTTUrlAndImageUrls(null, null);
            }
        }
    }

    private void releaseTXImageSprite() {
        if (mTXImageSprite != null) {
            mTXImageSprite.release();
            mTXImageSprite = null;
        }
    }

    /**
     * 更新关键帧信息
     *
     * @param list 关键帧信息列表
     */
    @Override
    public void updateKeyFrameDescInfo(List<TCPlayKeyFrameDescInfo> list) {
        mTXPlayKeyFrameDescInfoList = list;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector != null)
            mGestureDetector.onTouchEvent(event);

        if (!mLockScreen) {
            if (event.getAction() == MotionEvent.ACTION_UP && mVideoGestureUtil != null && mVideoGestureUtil.isVideoProgressModel()) {
                int progress = mVideoGestureUtil.getVideoProgress();
                if (progress > mSeekBarProgress.getMax()) {
                    progress = mSeekBarProgress.getMax();
                }
                if (progress < 0) {
                    progress = 0;
                }
                mSeekBarProgress.setProgress(progress);

                int seekTime = 0;
                float percentage = progress * 1.0f / mSeekBarProgress.getMax();
                if (mPlayType == SuperPlayerConst.PLAYTYPE_LIVE || mPlayType == SuperPlayerConst.PLAYTYPE_LIVE_SHIFT) {
                    if (mLivePushDuration > SuperPlayerConst.MAX_SHIFT_TIME) {
                        seekTime = (int) (mLivePushDuration - SuperPlayerConst.MAX_SHIFT_TIME *  (1 - percentage));
                    } else {
                        seekTime  = (int) (mLivePushDuration * percentage);
                    }
                }else {
                    seekTime = (int) (percentage * mDuration);
                }
                if (mControllerCallback != null) {
                    mControllerCallback.onSeekTo(seekTime);
                }
                mIsChangingSeekBarProgress = false;
            }
        }

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            this.getHandler().removeCallbacks(mHideViewRunnable);
        } else if(event.getAction() == MotionEvent.ACTION_UP) {
            this.getHandler().postDelayed(mHideViewRunnable, 7000);
        }
        return true;
    }

    /**
     * 设置点击事件监听
     */
    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.superplayer_iv_back || i == R.id.superplayer_tv_title) { //顶部标题栏
            if (mControllerCallback != null) {
                mControllerCallback.onBackPressed(SuperPlayerConst.PLAYMODE_FULLSCREEN);
            }
        } else if (i == R.id.superplayer_iv_pause) {            //暂停\播放按钮
            togglePlayState();
        } else if (i == R.id.superplayer_iv_danmuku) {          //弹幕按钮
            toggleBarrage();
        } else if (i == R.id.superplayer_iv_snapshot) {         //截屏按钮
            if (mControllerCallback != null) {
                mControllerCallback.onSnapshot();
            }
        } else if (i == R.id.superplayer_iv_more) {             //更多设置按钮
            showMoreView();
        } else if (i == R.id.superplayer_tv_quality) {          //画质按钮
            showQualityView();
        } else if (i == R.id.superplayer_iv_lock) {             //锁屏按钮
            toggleLockState();
        } else if (i == R.id.superplayer_ll_replay) {           //重播按钮
            replay();
        } else if (i == R.id.superplayer_tv_back_to_live) {     //返回直播按钮
            if (mControllerCallback != null) {
                mControllerCallback.onResumeLive();
            }
        } else if (i == R.id.superplayer_large_tv_vtt_text) {   //关键帧打点信息按钮
            seekToKeyFramePos();
        }
    }

    /**
     * 开关弹幕
     */
    private void toggleBarrage() {
        mBarrageOn = !mBarrageOn;
        if (mBarrageOn) {
            mIvDanmu.setImageResource(R.drawable.superplayer_ic_danmuku_on);
        } else {
            mIvDanmu.setImageResource(R.drawable.superplayer_ic_danmuku_off);
        }
        if (mControllerCallback != null) {
            mControllerCallback.onDanmuToggle(mBarrageOn);
        }
    }

    /**
     * 显示更多设置弹窗
     */
    private void showMoreView() {
        hide();
        mVodMoreView.setVisibility(View.VISIBLE);
    }

    /**
     * 显示画质列表弹窗
     */
    private void showQualityView() {
        if (mVideoQualityList == null || mVideoQualityList.size() == 0) {
            return;
        }
        if(mVideoQualityList.size()==1 && (mVideoQualityList.get(0)==null || TextUtils.isEmpty(mVideoQualityList.get(0).title))){
            return;
        }
        // 设置默认显示分辨率文字
        mVodQualityView.setVisibility(View.VISIBLE);
        if (!mFirstShowQuality && mDefaultVideoQuality != null) {
            for (int i = 0 ; i  < mVideoQualityList.size(); i++) {
                TCVideoQuality quality = mVideoQualityList.get(i);
                if (quality!=null && quality.title!=null &&quality.title.equals(mDefaultVideoQuality.title)) {
                    mVodQualityView.setDefaultSelectedQuality(i);
                    break;
                }
            }
            mFirstShowQuality = true;
        }
        mVodQualityView.setVideoQualityList(mVideoQualityList);
    }

    /**
     * 切换锁屏状态
     */
    private void toggleLockState() {
        mLockScreen = !mLockScreen;
        mIvLock.setVisibility(VISIBLE);
        if (mHideLockViewRunnable!=null) {
            this.getHandler().removeCallbacks(mHideLockViewRunnable);
            this.getHandler().postDelayed(mHideLockViewRunnable, 7000);
        }
        if (mLockScreen) {
            mIvLock.setImageResource(R.drawable.superplayer_ic_player_lock);
            hide();
            mIvLock.setVisibility(VISIBLE);
        } else {
            mIvLock.setImageResource(R.drawable.superplayer_ic_player_unlock);
            show();
        }
    }

    /**
     * 重播
     */
    private void replay() {
        toggleView(mLayoutReplay, false);
        if (mControllerCallback != null) {
            mControllerCallback.onResume();
        }
    }

    /**
     * 跳转至关键帧打点处
     */
    private void seekToKeyFramePos() {
        float time = mTXPlayKeyFrameDescInfoList != null ? mTXPlayKeyFrameDescInfoList.get(mSelectedPos).time : 0;
        if (mControllerCallback != null) {
            mControllerCallback.onSeekTo((int) time);
            mControllerCallback.onResume();
        }
        mTvVttText.setVisibility(GONE);
        toggleView(mLayoutReplay, false);
    }

    @Override
    public void onProgressChanged(TCPointSeekBar seekBar, int progress, boolean isFromUser) {
        if (mGestureVideoProgressLayout != null && isFromUser) {
            mGestureVideoProgressLayout.show();
            float percentage = ((float) progress) / seekBar.getMax();
            float currentTime = (mDuration * percentage);
            if (mPlayType == SuperPlayerConst.PLAYTYPE_LIVE || mPlayType == SuperPlayerConst.PLAYTYPE_LIVE_SHIFT) {
                if (mLivePushDuration > SuperPlayerConst.MAX_SHIFT_TIME) {
                    currentTime = (int) (mLivePushDuration - SuperPlayerConst.MAX_SHIFT_TIME *  (1 - percentage));
                } else {
                    currentTime  = mLivePushDuration * percentage;
                }
                mGestureVideoProgressLayout.setTimeText(TCTimeUtil.formattedTime((long) currentTime));
            } else {
                mGestureVideoProgressLayout.setTimeText(TCTimeUtil.formattedTime((long) currentTime) + " / " + TCTimeUtil.formattedTime((long) mDuration));
            }
            mGestureVideoProgressLayout.setProgress(progress);
        }
        // 加载点播缩略图
        if (isFromUser && mPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
            setThumbnail(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(TCPointSeekBar seekBar) {
        this.getHandler().removeCallbacks(mHideViewRunnable);
    }

    @Override
    public void onStopTrackingTouch(TCPointSeekBar seekBar) {
        int curProgress = seekBar.getProgress();
        int maxProgress = seekBar.getMax();

        switch (mPlayType) {
            case SuperPlayerConst.PLAYTYPE_VOD:
                if (curProgress >= 0 && curProgress <= maxProgress) {
                    // 关闭重播按钮
                    toggleView(mLayoutReplay, false);
                    float percentage = ((float) curProgress) / maxProgress;
                    int position = (int) (mDuration * percentage);
                    if (mControllerCallback != null) {
                        mControllerCallback.onSeekTo(position);
                        mControllerCallback.onResume();
                    }
                }
                break;
            case SuperPlayerConst.PLAYTYPE_LIVE:
            case SuperPlayerConst.PLAYTYPE_LIVE_SHIFT:
                toggleView(mPbLiveLoading, true);
                int seekTime = (int) (mLivePushDuration * curProgress * 1.0f / maxProgress);
                if (mLivePushDuration > SuperPlayerConst.MAX_SHIFT_TIME) {
                    seekTime = (int) (mLivePushDuration - SuperPlayerConst.MAX_SHIFT_TIME *  (maxProgress - curProgress) * 1.0f / maxProgress);
                }
                if (mControllerCallback != null) {
                    mControllerCallback.onSeekTo(seekTime);
                }
                break;
        }
        this.getHandler().postDelayed(mHideViewRunnable, 7000);
    }

    @Override
    public void onSeekBarPointClick(final View view, final int pos) {
        if (mHideLockViewRunnable!=null) {
            this.getHandler().removeCallbacks(mHideViewRunnable);
            this.getHandler().postDelayed(mHideViewRunnable, 7000);
        }
        if (mTXPlayKeyFrameDescInfoList != null) {
            //ELK点击上报
            TCLogReport.getInstance().uploadLogs(TCLogReport.ELK_ACTION_PLAYER_POINT, 0, 0);
            mSelectedPos = pos;
            view.post(new Runnable() {
                @Override
                public void run() {
                    int[] location = new int[2];
                    view.getLocationInWindow(location);

                    int viewX = location[0];
                    TCPlayKeyFrameDescInfo info = mTXPlayKeyFrameDescInfoList.get(pos);
                    String content = info.content;

                    mTvVttText.setText(TCTimeUtil.formattedTime((long) info.time) + " " + content);
                    mTvVttText.setVisibility(VISIBLE);
                    adjustVttTextViewPos(viewX);
                }
            });
        }
    }

    /**
     * 设置播放进度所对应的缩略图
     *
     * @param progress 播放进度
     */
    private void setThumbnail(int progress) {
        float percentage = ((float) progress) / mSeekBarProgress.getMax();
        float seekTime = (mDuration * percentage);
        if (mTXImageSprite != null) {
            Bitmap bitmap = mTXImageSprite.getThumbnail(seekTime);
            if (bitmap != null) {
                mGestureVideoProgressLayout.setThumbnail(bitmap);
            }
        }
    }

    /**
     * 计算并设置关键帧打点信息文本显示的位置
     *
     * @param viewX 点击的打点view
     */
    private void adjustVttTextViewPos(final int viewX) {
        mTvVttText.post(new Runnable() {
            @Override
            public void run() {
                int width = mTvVttText.getWidth();

                int marginLeft = viewX - width / 2;

                LayoutParams params = (LayoutParams) mTvVttText.getLayoutParams();
                params.leftMargin = marginLeft;

                if (marginLeft < 0) {
                    params.leftMargin = 0;
                }

                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                if (marginLeft + width > screenWidth) {
                    params.leftMargin = screenWidth - width;
                }

                mTvVttText.setLayoutParams(params);
            }
        });
    }

    @Override
    public void onSpeedChange(float speedLevel) {
        if (mControllerCallback != null) {
            mControllerCallback.onSpeedChange(speedLevel);
        }
    }

    @Override
    public void onMirrorChange(boolean isMirror) {
        if (mControllerCallback != null) {
            mControllerCallback.onMirrorToggle(isMirror);
        }
    }

    @Override
    public void onHWAcceleration(boolean isAccelerate) {
        if (mControllerCallback != null) {
            mControllerCallback.onHWAccelerationToggle(isAccelerate);
        }
    }

    @Override
    public void onQualitySelect(TCVideoQuality quality) {
        if (mControllerCallback != null) {
            mControllerCallback.onQualityChange(quality);
        }
        mVodQualityView.setVisibility(View.GONE);
    }

    /**
     * 隐藏锁屏按钮的runnable
     */
    private static class HideLockViewRunnable implements Runnable{
        private WeakReference<TCControllerFullScreen> mWefControllerFullScreen;

        public HideLockViewRunnable(TCControllerFullScreen controller) {
            mWefControllerFullScreen = new WeakReference<>(controller);
        }
        @Override
        public void run() {
            if (mWefControllerFullScreen!=null && mWefControllerFullScreen.get()!=null) {
                mWefControllerFullScreen.get().mIvLock.setVisibility(GONE);
            }
        }
    }
}
