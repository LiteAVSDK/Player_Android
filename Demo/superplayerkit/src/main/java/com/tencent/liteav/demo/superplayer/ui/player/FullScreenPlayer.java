package com.tencent.liteav.demo.superplayer.ui.player;

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

import com.bumptech.glide.Glide;
import com.tencent.liteav.demo.superplayer.R;
import com.tencent.liteav.demo.superplayer.SuperPlayerDef;
import com.tencent.liteav.demo.superplayer.model.VipWatchModel;
import com.tencent.liteav.demo.superplayer.SuperPlayerModel;
import com.tencent.liteav.demo.superplayer.model.entity.PlayImageSpriteInfo;
import com.tencent.liteav.demo.superplayer.model.entity.PlayKeyFrameDescInfo;
import com.tencent.liteav.demo.superplayer.model.net.LogReport;
import com.tencent.liteav.demo.superplayer.model.utils.VideoGestureDetector;
import com.tencent.liteav.demo.superplayer.model.utils.VideoQualityUtils;
import com.tencent.liteav.demo.superplayer.ui.view.PointSeekBar;
import com.tencent.liteav.demo.superplayer.ui.view.VideoProgressLayout;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;
import com.tencent.liteav.demo.superplayer.ui.view.VipWatchView;
import com.tencent.liteav.demo.superplayer.ui.view.VodMoreView;
import com.tencent.liteav.demo.superplayer.ui.view.VodQualityView;
import com.tencent.liteav.demo.superplayer.ui.view.VolumeBrightnessProgressLayout;
import com.tencent.rtmp.TXImageSprite;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_AUTO_PLAY;
import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_MANUAL_PLAY;

/**
 * 全屏模式播放控件
 * <p>
 * 除{@link WindowPlayer}基本功能外，还包括进度条关键帧打点信息显示与跳转、快进快退时缩略图的显示、切换画质
 * 镜像播放、硬件加速、倍速播放、弹幕、截图等功能
 * <p>
 * 1、点击事件监听{@link #onClick(View)}
 * <p>
 * 2、触摸事件监听{@link #onTouchEvent(MotionEvent)}
 * <p>
 * 3、进度条滑动事件监听{@link #onProgressChanged(PointSeekBar, int, boolean)}
 * {@link #onStartTrackingTouch(PointSeekBar)}{@link #onStopTrackingTouch(PointSeekBar)}
 * <p>
 * 4、进度条打点信息点击监听{@link #onSeekBarPointClick(View, int)}
 * <p>
 * 5、切换画质监听{@link #onQualitySelect(VideoQuality)}
 * <p>
 * 6、倍速播放监听{@link #onSpeedChange(float)}
 * <p>
 * 7、镜像播放监听{@link #onMirrorChange(boolean)}
 * <p>
 * 8、硬件加速监听{@link #onHWAcceleration(boolean)}
 */
public class FullScreenPlayer extends AbsPlayer implements View.OnClickListener,
        VodMoreView.Callback, VodQualityView.Callback, PointSeekBar.OnSeekBarChangeListener, PointSeekBar.OnSeekBarPointClickListener, VipWatchView.VipWatchViewClickListener {

    // UI控件
    private RelativeLayout                 mLayoutTop;                             // 顶部标题栏布局
    private LinearLayout                   mLayoutBottom;                          // 底部进度条所在布局
    private ImageView                      mIvPause;                               // 暂停播放按钮
    private TextView                       mTvTitle;                               // 视频名称文本
    private LinearLayout                   mLlTitle;                               // 返回按键 和 标题的布局
    private TextView                       mTvBackToLive;                          // 返回直播文本
    private ImageView                      mIvWatermark;                           // 水印
    private TextView                       mTvCurrent;                             // 当前进度文本
    private TextView                       mTvDuration;                            // 总时长文本
    private ImageView                      mIvPlayNext;                            // 播放下一个按钮
    private PointSeekBar                   mSeekBarProgress;                       // 播放进度条
    private LinearLayout                   mLayoutReplay;                          // 重播按钮所在布局
    private ProgressBar                    mPbLiveLoading;                         // 加载圈
    private VolumeBrightnessProgressLayout mGestureVolumeBrightnessProgressLayout; // 音量亮度调节布局
    private VideoProgressLayout            mGestureVideoProgressLayout;            // 手势快进提示布局
    private TextView                       mTvQuality;                             // 当前画质文本
    private ImageView                      mIvBack;                                // 顶部标题栏中的返回按钮
    private ImageView                      mIvDanmu;                               // 弹幕按钮
    private ImageView                      mIvSnapshot;                            // 截屏按钮
    private ImageView                      mIvLock;                                // 锁屏按钮
    private ImageView                      mIvMore;                                // 更多设置弹窗按钮
    private ImageView                      mImageStartAndResume;                   // 开始播放的三角
    private ImageView                      mImageCover;                            // 封面图
    private VodQualityView                 mVodQualityView;                        // 画质列表弹窗
    private VodMoreView                    mVodMoreView;                           // 更多设置弹窗
    private TextView                       mTvVttText;                             // 关键帧打点信息文本
    private HideLockViewRunnable           mHideLockViewRunnable;                  // 隐藏锁屏按钮子线程
    private GestureDetector                mGestureDetector;                       // 手势检测监听器
    private VideoGestureDetector           mVideoGestureDetector;                      // 手势控制工具
    private boolean                        isShowing;                              // 自身是否可见
    private boolean                        mIsChangingSeekBarProgress;             // 进度条是否正在拖动，避免SeekBar由于视频播放的update而跳动
    private SuperPlayerDef.PlayerType      mPlayType;                              // 当前播放视频类型
    private SuperPlayerDef.PlayerState     mCurrentPlayState = SuperPlayerDef.PlayerState.END;                 // 当前播放状态
    private long                           mDuration;                              // 视频总时长
    private long                           mLivePushDuration;                      // 直播推流总时长
    private long                           mProgress;                              // 当前播放进度
    private Bitmap                         mBackgroundBmp;                         // 背景图
    private Bitmap                         mWaterMarkBmp;                          // 水印图
    private float                          mWaterMarkBmpX;                         // 水印x坐标
    private float                          mWaterMarkBmpY;                         // 水印y坐标
    private boolean                        mBarrageOn;                             // 弹幕是否开启
    private boolean                        mLockScreen;                            // 是否锁屏
    private TXImageSprite                  mTXImageSprite;                         // 雪碧图信息
    private List<PlayKeyFrameDescInfo>     mTXPlayKeyFrameDescInfoList;            // 关键帧信息
    private int                            mSelectedPos      = -1;                      // 点击的关键帧时间点
    private VideoQuality                   mDefaultVideoQuality;                   // 默认画质
    private List<VideoQuality>             mVideoQualityList;                      // 画质列表
    private boolean                        mFirstShowQuality;                      // 是都是首次显示画质信息
    private boolean                        mIsOpenGesture    = true;                  // 是否开启手势
    private boolean                        isDestroy         = false;              // Activity是否被销毁

    public FullScreenPlayer(Context context) {
        super(context);
        initialize(context);
    }

    public FullScreenPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public FullScreenPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    /**
     * 初始化控件、手势检测监听器、亮度/音量/播放进度的回调
     */
    private void initialize(Context context) {
        initView(context);
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isShowingVipView()) {   //当展示了试看页面的时候，不处理双击事件
                    return true;
                }
                if (mLockScreen) return false;
                togglePlayState();
                show();
                if (mHideViewRunnable != null) {
                    removeCallbacks(mHideViewRunnable);
                    postDelayed(mHideViewRunnable, 7000);
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
                if (mVideoGestureDetector != null && mGestureVolumeBrightnessProgressLayout != null) {
                    mVideoGestureDetector.check(mGestureVolumeBrightnessProgressLayout.getHeight(), downEvent, moveEvent, distanceX, distanceY);
                }
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                if (mLockScreen) return true;
                if (mVideoGestureDetector != null) {
                    mVideoGestureDetector.reset(getWidth(), mSeekBarProgress.getProgress());
                }
                return true;
            }

        });
        mGestureDetector.setIsLongpressEnabled(false);

        mVideoGestureDetector = new VideoGestureDetector(getContext());
        mVideoGestureDetector.setVideoGestureListener(new VideoGestureDetector.VideoGestureListener() {
            @Override
            public void onBrightnessGesture(float newBrightness) {
                if (mGestureVolumeBrightnessProgressLayout != null) {
                    mGestureVolumeBrightnessProgressLayout.setProgress((int) (newBrightness * 100));
                    mVodMoreView.setBrightProgress((int) (newBrightness * 100));
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
                    if (mPlayType == SuperPlayerDef.PlayerType.LIVE || mPlayType == SuperPlayerDef.PlayerType.LIVE_SHIFT) {
                        if (mLivePushDuration > MAX_SHIFT_TIME) {
                            currentTime = (int) (mLivePushDuration - MAX_SHIFT_TIME * (1 - percentage));
                        } else {
                            currentTime = mLivePushDuration * percentage;
                        }
                        mGestureVideoProgressLayout.setTimeText(formattedTime((long) currentTime));
                    } else {
                        mGestureVideoProgressLayout.setTimeText(formattedTime((long) currentTime) + " / " + formattedTime((long) mDuration));
                    }
                    setThumbnail(progress);
                }
                if (mSeekBarProgress != null)
                    mSeekBarProgress.setProgress(progress);
            }
        });
    }

    /**
     * 初始化view
     */
    private void initView(Context context) {
        mHideLockViewRunnable = new HideLockViewRunnable(this);
        LayoutInflater.from(context).inflate(R.layout.superplayer_vod_player_fullscreen, this);
        mLlTitle = (LinearLayout) findViewById(R.id.superplayer_ll_title);
        mLayoutTop = (RelativeLayout) findViewById(R.id.superplayer_rl_top);
        mLayoutTop.setOnClickListener(this);
        mLayoutBottom = (LinearLayout) findViewById(R.id.superplayer_ll_bottom);
        mLayoutBottom.setOnClickListener(this);
        mLayoutReplay = (LinearLayout) findViewById(R.id.superplayer_ll_replay);

        mIvBack = (ImageView) findViewById(R.id.superplayer_iv_back);
        mIvLock = (ImageView) findViewById(R.id.superplayer_iv_lock);
        mTvTitle = (TextView) findViewById(R.id.superplayer_tv_title_full_screen);
        mIvPause = (ImageView) findViewById(R.id.superplayer_iv_pause);
        mIvDanmu = (ImageView) findViewById(R.id.superplayer_iv_danmuku);
        mIvMore = (ImageView) findViewById(R.id.superplayer_iv_more);
        mIvSnapshot = (ImageView) findViewById(R.id.superplayer_iv_snapshot);
        mTvCurrent = (TextView) findViewById(R.id.superplayer_tv_current);
        mTvDuration = (TextView) findViewById(R.id.superplayer_tv_duration);
        mImageCover = (ImageView) findViewById(R.id.superplayer_cover_view);
        mImageStartAndResume = (ImageView) findViewById(R.id.superplayer_resume);
        mIvPlayNext = (ImageView) findViewById(R.id.superplayer_iv_play_next);

        mSeekBarProgress = (PointSeekBar) findViewById(R.id.superplayer_seekbar_progress);
        mSeekBarProgress.setProgress(0);
        mSeekBarProgress.setOnPointClickListener(this);
        mSeekBarProgress.setOnSeekBarChangeListener(this);
        mTvQuality = (TextView) findViewById(R.id.superplayer_tv_quality);
        mTvBackToLive = (TextView) findViewById(R.id.superplayer_tv_back_to_live);
        mPbLiveLoading = (ProgressBar) findViewById(R.id.superplayer_pb_live);

        mVodQualityView = (VodQualityView) findViewById(R.id.superplayer_vod_quality);
        mVodQualityView.setCallback(this);
        mVodMoreView = (VodMoreView) findViewById(R.id.superplayer_vod_more);
        mVodMoreView.setCallback(this);

        mImageStartAndResume.setOnClickListener(this);
        mIvPlayNext.setOnClickListener(this);
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
        mGestureVolumeBrightnessProgressLayout = (VolumeBrightnessProgressLayout) findViewById(R.id.superplayer_gesture_progress);
        mGestureVideoProgressLayout = (VideoProgressLayout) findViewById(R.id.superplayer_video_progress_layout);
        mIvWatermark = (ImageView) findViewById(R.id.superplayer_large_iv_water_mark);
        mVipWatchView = findViewById(R.id.superplayer_vip_watch_view);
        mVipWatchView.setVipWatchViewClickListener(this);
    }

    public void setPlayNextButtonVisibility(boolean isShowing) {
        toggleView(mIvPlayNext, isShowing);
    }

    /**
     * 切换播放状态
     * <p>
     * 双击和点击播放/暂停按钮会触发此方法
     */
    private void togglePlayState() {
        switch (mCurrentPlayState) {
            case INIT:
            case PAUSE:
            case END:
                if (mControllerCallback != null) {
                    mControllerCallback.onResume();
                }
                break;
            case PLAYING:
            case LOADING:
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
                    removeCallbacks(mHideViewRunnable);
                    postDelayed(mHideViewRunnable, 7000);
                }
            }
        } else {
            mIvLock.setVisibility(VISIBLE);
            if (mHideLockViewRunnable != null) {
                removeCallbacks(mHideLockViewRunnable);
                postDelayed(mHideLockViewRunnable, 7000);
            }
        }
        if (mVodMoreView.getVisibility() == VISIBLE) {
            mVodMoreView.setVisibility(GONE);
        }
    }

    private void updateStartUI(boolean isAutoPlay) {
        if (isAutoPlay) {
            toggleView(mImageStartAndResume, false);
            toggleView(mPbLiveLoading, true);
        } else {
            toggleView(mImageStartAndResume, true);
            toggleView(mPbLiveLoading, false);
        }
        toggleView(mLayoutReplay, false);
    }

    public void preparePlayVideo(SuperPlayerModel superPlayerModel) {
        updateTitle(superPlayerModel.title);
        if (!isDestroy) {
            if (superPlayerModel.coverPictureUrl != null) {
                Glide.with(getContext()).load(superPlayerModel.coverPictureUrl)
                        .placeholder(R.drawable.superplayer_default).into(mImageCover);
            } else {
                Glide.with(getContext()).load(superPlayerModel.placeholderImage)
                        .placeholder(R.drawable.superplayer_default).into(mImageCover);
            }
        }
        mLivePushDuration = 0;
        toggleView(mImageCover, true);
        mIvPause.setImageResource(R.drawable.superplayer_ic_vod_play_normal);
        updateVideoProgress(0, superPlayerModel.duration);
        mSeekBarProgress.setEnabled(superPlayerModel.playAction != SuperPlayerModel.PLAY_ACTION_MANUAL_PLAY);
        updateStartUI(superPlayerModel.playAction == SuperPlayerModel.PLAY_ACTION_AUTO_PLAY);
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
        mLlTitle.setVisibility(View.VISIBLE);
        if (mHideLockViewRunnable != null) {
            removeCallbacks(mHideLockViewRunnable);
        }
        mIvLock.setVisibility(VISIBLE);
        if (mPlayType == SuperPlayerDef.PlayerType.LIVE_SHIFT) {
            if (mLayoutBottom.getVisibility() == VISIBLE)
                mTvBackToLive.setVisibility(View.VISIBLE);
        }
        List<PointSeekBar.PointParams> pointParams = new ArrayList<>();
        if (mTXPlayKeyFrameDescInfoList != null)
            for (PlayKeyFrameDescInfo info : mTXPlayKeyFrameDescInfoList) {
                int progress = (int) (info.time / mDuration * mSeekBarProgress.getMax());
                pointParams.add(new PointSeekBar.PointParams(progress, Color.WHITE));
            }
        mSeekBarProgress.setPointList(pointParams);
    }

    /**
     * 隐藏控件
     */
    @Override
    public void hide() {
        isShowing = false;
        mLlTitle.setVisibility(View.GONE);
        mLayoutTop.setVisibility(View.GONE);
        mLayoutBottom.setVisibility(View.GONE);
        mVodQualityView.setVisibility(View.GONE);
        mTvVttText.setVisibility(GONE);
        mIvLock.setVisibility(GONE);
        if (mPlayType == SuperPlayerDef.PlayerType.LIVE_SHIFT) {
            mTvBackToLive.setVisibility(View.GONE);
        }
    }

    /**
     * 释放控件的内存
     */
    @Override
    public void release() {
        isDestroy = true;
        releaseTXImageSprite();
    }


    public void toggleCoverView(boolean isVisible) {
        toggleView(mImageCover, isVisible);
    }

    public void prepareLoading() {
        toggleView(mPbLiveLoading, true);
        toggleView(mImageStartAndResume, false);
    }

    @Override
    public void updatePlayState(SuperPlayerDef.PlayerState playState) {
        switch (playState) {
            case INIT:
                mIvPause.setImageResource(R.drawable.superplayer_ic_vod_play_normal);
                break;
            case PLAYING:
                mSeekBarProgress.setEnabled(true);
                mIvPause.setImageResource(R.drawable.superplayer_ic_vod_pause_normal);
                toggleView(mImageStartAndResume, false);
                toggleView(mPbLiveLoading, false);
                toggleView(mLayoutReplay, false);
                break;
            case LOADING:
                mSeekBarProgress.setEnabled(true);
                mIvPause.setImageResource(R.drawable.superplayer_ic_vod_pause_normal);
                toggleView(mPbLiveLoading, true);
                toggleView(mLayoutReplay, false);
                break;
            case PAUSE:
                mIvPause.setImageResource(R.drawable.superplayer_ic_vod_play_normal);
                toggleView(mLayoutReplay, false);
                toggleView(mImageStartAndResume, true);
                break;
            case END:
                mIvPause.setImageResource(R.drawable.superplayer_ic_vod_play_normal);
                toggleView(mPbLiveLoading, false);
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
    public void setVideoQualityList(List<VideoQuality> list) {
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
        if (title != null) {
            mTvTitle.setText(title);
        }
    }

    /**
     * 更新实时播放进度
     *
     * @param current  当前进度(秒)
     * @param duration 视频总时长(秒)
     */
    @Override
    public void updateVideoProgress(long current, long duration) {
        mProgress = current < 0 ? 0 : current;
        mDuration = duration < 0 ? 0 : duration;
        mTvCurrent.setText(formattedTime(mProgress));

        float percentage = mDuration > 0 ? ((float) mProgress / (float) mDuration) : 1.0f;
        if (mProgress == 0) {
            percentage = 0;
        }
        if (mPlayType == SuperPlayerDef.PlayerType.LIVE || mPlayType == SuperPlayerDef.PlayerType.LIVE_SHIFT) {
            mLivePushDuration = mLivePushDuration > mProgress ? mLivePushDuration : mProgress;
            long leftTime = mDuration - mProgress;
            mDuration = mDuration > MAX_SHIFT_TIME ? MAX_SHIFT_TIME : mDuration;
            percentage = 1 - (float) leftTime / (float) mDuration;
        } else {
            mVipWatchView.setCurrentTime(current);
        }

        if (percentage >= 0 && percentage <= 1) {
            int progress = Math.round(percentage * mSeekBarProgress.getMax());
            if (!mIsChangingSeekBarProgress)
                mSeekBarProgress.setProgress(progress);
            mTvDuration.setText(formattedTime(mDuration));
        }
    }

    @Override
    public void updatePlayType(SuperPlayerDef.PlayerType type) {
        mPlayType = type;
        switch (type) {
            case VOD:
                mTvBackToLive.setVisibility(View.GONE);
                mVodMoreView.updatePlayType(SuperPlayerDef.PlayerType.VOD);
                mTvDuration.setVisibility(View.VISIBLE);
                break;
            case LIVE:
                mTvBackToLive.setVisibility(View.GONE);
                mTvDuration.setVisibility(View.GONE);
                mVodMoreView.updatePlayType(SuperPlayerDef.PlayerType.LIVE);
                mSeekBarProgress.setProgress(100);
                break;
            case LIVE_SHIFT:
                if (mLayoutBottom.getVisibility() == VISIBLE) {
                    mTvBackToLive.setVisibility(View.VISIBLE);
                }
                mTvDuration.setVisibility(View.GONE);
                mVodMoreView.updatePlayType(SuperPlayerDef.PlayerType.LIVE_SHIFT);
                break;
        }
    }

    /**
     * 更新视频播放画质
     *
     * @param videoQuality 画质
     */
    @Override
    public void updateVideoQuality(VideoQuality videoQuality) {
        if (videoQuality == null) {
            mTvQuality.setText("");
            return;
        }
        mDefaultVideoQuality = videoQuality;
        if (mTvQuality != null) {
            mTvQuality.setText(VideoQualityUtils.transformToQualityName(videoQuality.title));
        }
        if (mVideoQualityList != null && mVideoQualityList.size() != 0) {
            for (int i = 0; i < mVideoQualityList.size(); i++) {
                VideoQuality quality = mVideoQualityList.get(i);
                if (quality != null && quality.title != null && quality.title.equals(mDefaultVideoQuality.title)) {
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
    public void updateImageSpriteInfo(PlayImageSpriteInfo info) {
        if (mTXImageSprite != null) {
            releaseTXImageSprite();
        }
        // 有缩略图的时候不显示进度
        mGestureVideoProgressLayout.setProgressVisibility(info == null || info.imageUrls == null || info.imageUrls.size() == 0);
        if (mPlayType == SuperPlayerDef.PlayerType.VOD) {
            mTXImageSprite = new TXImageSprite(getContext());
            if (info != null) {
                // 雪碧图ELK上报
                LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_IMAGE_SPRITE, 0, 0);
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
    public void updateKeyFrameDescInfo(List<PlayKeyFrameDescInfo> list) {
        mTXPlayKeyFrameDescInfoList = list;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsOpenGesture && mGestureDetector != null) {
            mGestureDetector.onTouchEvent(event);
        }

        if (!mLockScreen) {
            if (event.getAction() == MotionEvent.ACTION_UP && mVideoGestureDetector != null && mVideoGestureDetector.isVideoProgressModel()) {
                int progress = mVideoGestureDetector.getVideoProgress();
                if (progress > mSeekBarProgress.getMax()) {
                    progress = mSeekBarProgress.getMax();
                }
                if (progress < 0) {
                    progress = 0;
                }
                mSeekBarProgress.setProgress(progress);

                int seekTime = 0;
                float percentage = progress * 1.0f / mSeekBarProgress.getMax();
                if (mPlayType == SuperPlayerDef.PlayerType.LIVE || mPlayType == SuperPlayerDef.PlayerType.LIVE_SHIFT) {
                    if (mLivePushDuration > MAX_SHIFT_TIME) {
                        seekTime = (int) (mLivePushDuration - MAX_SHIFT_TIME * (1 - percentage));
                    } else {
                        seekTime = (int) (mLivePushDuration * percentage);
                    }
                } else {
                    seekTime = (int) (percentage * mDuration);
                }
                if (mControllerCallback != null) {
                    mControllerCallback.onSeekTo(seekTime);
                }
                mIsChangingSeekBarProgress = false;
                if (mPlayType == SuperPlayerDef.PlayerType.VOD) {
                    mVipWatchView.setCurrentTime(seekTime);
                }
            }
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            removeCallbacks(mHideViewRunnable);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            postDelayed(mHideViewRunnable, 7000);
        }
        return true;
    }

    /**
     * 设置点击事件监听
     */
    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.superplayer_iv_back || i == R.id.superplayer_tv_title_full_screen) { //顶部标题栏
            if (mControllerCallback != null) {
                mControllerCallback.onBackPressed(SuperPlayerDef.PlayerMode.FULLSCREEN);
            }
        } else if (i == R.id.superplayer_iv_pause || i == R.id.superplayer_resume) {            //暂停\播放按钮
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
        } else if (i == R.id.superplayer_iv_play_next) {
            if (mControllerCallback != null) {
                mControllerCallback.playNext();
            }
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
     * 还原界面上的信息
     * 关闭弹幕信息
     * 关闭镜像
     * 还原播放速度UI
     */
    public void revertUI() {
        //关闭弹幕
        if (mBarrageOn) {
            mBarrageOn = false;
            mIvDanmu.setImageResource(R.drawable.superplayer_ic_danmuku_off);
        }
        if (mVodMoreView != null) {
            mVodMoreView.revertUI();
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
        if (mVideoQualityList.size() == 1 && (mVideoQualityList.get(0) == null || TextUtils.isEmpty(mVideoQualityList.get(0).title))) {
            return;
        }
        // 设置默认显示分辨率文字
        mVodQualityView.setVisibility(View.VISIBLE);
        if (!mFirstShowQuality && mDefaultVideoQuality != null) {
            for (int i = 0; i < mVideoQualityList.size(); i++) {
                VideoQuality quality = mVideoQualityList.get(i);
                if (quality != null && quality.title != null && quality.title.equals(mDefaultVideoQuality.title)) {
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
        if (mHideLockViewRunnable != null) {
            removeCallbacks(mHideLockViewRunnable);
            postDelayed(mHideLockViewRunnable, 7000);
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
    public void onProgressChanged(PointSeekBar seekBar, int progress, boolean isFromUser) {
        if (mGestureVideoProgressLayout != null && isFromUser) {
            mGestureVideoProgressLayout.show();
            float percentage = ((float) progress) / seekBar.getMax();
            float currentTime = (mDuration * percentage);
            if (mPlayType == SuperPlayerDef.PlayerType.LIVE || mPlayType == SuperPlayerDef.PlayerType.LIVE_SHIFT) {
                if (mLivePushDuration > MAX_SHIFT_TIME) {
                    currentTime = (int) (mLivePushDuration - MAX_SHIFT_TIME * (1 - percentage));
                } else {
                    currentTime = mLivePushDuration * percentage;
                }
                mGestureVideoProgressLayout.setTimeText(formattedTime((long) currentTime));
            } else {
                mGestureVideoProgressLayout.setTimeText(formattedTime((long) currentTime) + " / " + formattedTime((long) mDuration));
            }
            mGestureVideoProgressLayout.setProgress(progress);
        }
        // 加载点播缩略图
        if (isFromUser && mPlayType == SuperPlayerDef.PlayerType.VOD) {
            setThumbnail(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(PointSeekBar seekBar) {
        removeCallbacks(mHideViewRunnable);
    }

    @Override
    public void onStopTrackingTouch(PointSeekBar seekBar) {
        int curProgress = seekBar.getProgress();
        int maxProgress = seekBar.getMax();

        switch (mPlayType) {
            case VOD:
                if (curProgress >= 0 && curProgress <= maxProgress) {
                    // 关闭重播按钮
                    toggleView(mLayoutReplay, false);
                    float percentage = ((float) curProgress) / maxProgress;
                    int position = (int) (mDuration * percentage);
                    boolean showResult = mVipWatchView.canShowVipWatchView(position);
                    if (mControllerCallback != null) {
                        mControllerCallback.onSeekTo(position);
                    }
                    if (showResult) {
                        mVipWatchView.setCurrentTime(position);
                    }
                }
                break;
            case LIVE:
            case LIVE_SHIFT:
                toggleView(mPbLiveLoading, true);
                int seekTime = (int) (mLivePushDuration * curProgress * 1.0f / maxProgress);
                if (mLivePushDuration > MAX_SHIFT_TIME) {
                    seekTime = (int) (mLivePushDuration - MAX_SHIFT_TIME * (maxProgress - curProgress) * 1.0f / maxProgress);
                }
                if (mControllerCallback != null) {
                    mControllerCallback.onSeekTo(seekTime);
                }
                break;
        }
        postDelayed(mHideViewRunnable, 7000);
    }

    @Override
    public void onSeekBarPointClick(final View view, final int pos) {
        if (mHideLockViewRunnable != null) {
            removeCallbacks(mHideViewRunnable);
            postDelayed(mHideViewRunnable, 7000);
        }
        if (mTXPlayKeyFrameDescInfoList != null) {
            //ELK点击上报
            LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_PLAYER_POINT, 0, 0);
            mSelectedPos = pos;
            view.post(new Runnable() {
                @Override
                public void run() {
                    int[] location = new int[2];
                    view.getLocationInWindow(location);

                    int viewX = location[0];
                    PlayKeyFrameDescInfo info = mTXPlayKeyFrameDescInfoList.get(pos);
                    String content = info.content;

                    mTvVttText.setText(formattedTime((long) info.time) + " " + content);
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
        if (mVipWatchView.canShowVipWatchView(seekTime)) {
            mGestureVideoProgressLayout.hideThumbnail();
        } else {
            if (mTXImageSprite != null) {
                Bitmap bitmap = mTXImageSprite.getThumbnail(seekTime);
                if (bitmap != null) {
                    mGestureVideoProgressLayout.setThumbnail(bitmap);
                }
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
    public void onQualitySelect(VideoQuality quality) {
        if (mControllerCallback != null) {
            mControllerCallback.onQualityChange(quality);
        }
        mVodQualityView.setVisibility(View.GONE);
    }


    public void disableGesture(boolean flag) {
        this.mIsOpenGesture = !flag;
    }

    @Override
    public void onClickVipTitleBack() {
        if (mControllerCallback != null) {
            mControllerCallback.onBackPressed(SuperPlayerDef.PlayerMode.FULLSCREEN);
            mControllerCallback.onClickVipTitleBack(SuperPlayerDef.PlayerMode.FULLSCREEN);
        }
    }

    @Override
    public void onClickVipRetry() {
        if (mControllerCallback != null) {
            mControllerCallback.onClickVipRetry();
        }
    }

    @Override
    public void onShowVipView() {
        if (mControllerCallback != null) {
            mControllerCallback.onPause();
        }
    }

    @Override
    public void onClickVipBtn() {
        if (mControllerCallback != null) {
            mControllerCallback.onClickHandleVip();
        }
    }

    @Override
    public void onCloseVipTip() {
        if (mControllerCallback != null) {
            mControllerCallback.onCloseVipTip();
        }
    }

    /**
     * 隐藏锁屏按钮的runnable
     */
    private static class HideLockViewRunnable implements Runnable {
        private WeakReference<FullScreenPlayer> mWefControllerFullScreen;

        public HideLockViewRunnable(FullScreenPlayer controller) {
            mWefControllerFullScreen = new WeakReference<>(controller);
        }

        @Override
        public void run() {
            if (mWefControllerFullScreen != null && mWefControllerFullScreen.get() != null) {
                mWefControllerFullScreen.get().mIvLock.setVisibility(GONE);
            }
        }
    }
}
