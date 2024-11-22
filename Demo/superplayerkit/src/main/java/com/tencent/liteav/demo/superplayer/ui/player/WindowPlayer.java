package com.tencent.liteav.demo.superplayer.ui.player;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tencent.liteav.demo.superplayer.R;
import com.tencent.liteav.demo.superplayer.SuperPlayerDef;
import com.tencent.liteav.demo.superplayer.SuperPlayerGlobalConfig;
import com.tencent.liteav.demo.superplayer.SuperPlayerModel;
import com.tencent.liteav.demo.superplayer.helper.PictureInPictureHelper;
import com.tencent.liteav.demo.superplayer.model.utils.VideoGestureDetector;
import com.tencent.liteav.demo.superplayer.ui.view.PointSeekBar;
import com.tencent.liteav.demo.superplayer.ui.view.VideoProgressLayout;
import com.tencent.liteav.demo.superplayer.ui.view.VipWatchView;
import com.tencent.liteav.demo.superplayer.ui.view.VolumeBrightnessProgressLayout;

/**
 * Window mode playback control
 * <p>
 * In addition to basic playback control, there are also gesture controls for fast forwarding and rewinding,
 * and gesture controls for adjusting brightness and volume.
 * <p>
 * 1、Click event listener {@link #onClick(View)}.
 * <p>
 * 2、Touch event listener {@link #onTouchEvent(MotionEvent)}.
 * <p>
 * 3、Progress bar event listener {@link #onProgressChanged(PointSeekBar, int, boolean)},
 * {@link #onStartTrackingTouch(PointSeekBar)}, {@link #onStopTrackingTouch(PointSeekBar)}.
 *
 * 窗口模式播放控件
 * <p>
 * 除基本播放控制外，还有手势控制快进快退、手势调节亮度音量等
 * <p>
 * 1、点击事件监听{@link #onClick(View)}
 * <p>
 * 2、触摸事件监听{@link #onTouchEvent(MotionEvent)}
 * <p>
 * 2、进度条事件监听{@link #onProgressChanged(PointSeekBar, int, boolean)}
 * {@link #onStartTrackingTouch(PointSeekBar)}
 * {@link #onStopTrackingTouch(PointSeekBar)}
 */
public class WindowPlayer extends AbsPlayer implements View.OnClickListener,
        PointSeekBar.OnSeekBarChangeListener, VipWatchView.VipWatchViewClickListener {

    private LinearLayout                   mLayoutTop;                             // Top title bar layout
    private LinearLayout                   mLayoutBottom;                          // Bottom progress bar layout
    private ImageView                      mIvPause;
    private ImageView                      mIvPlayNext;
    private ImageView                      mIvFullScreen;
    private TextView                       mTvTitle;
    private TextView                       mTvBackToLive;
    private ImageView                      mBackground;
    private ImageView                      mIvBack;
    private ImageView                      mIvWatermark;
    private TextView                       mTvCurrent;
    private TextView                       mTvDuration;
    private PointSeekBar                   mSeekBarProgress;
    private LinearLayout                   mLayoutReplay;
    private ProgressBar                    mPbLiveLoading;
    private ImageView                      mImageStartAndResume;
    private ImageView                      mImageCover;
    // Volume and brightness adjustment layout
    private VolumeBrightnessProgressLayout mGestureVolumeBrightnessProgressLayout;
    // Gesture fast forward prompt layout
    private VideoProgressLayout            mGestureVideoProgressLayout;
    private GestureDetector                mGestureDetector;                       // Gesture detection listener
    private VideoGestureDetector           mVideoGestureDetector;                  // Gesture control tool
    private boolean                        isShowing;
    private boolean                        mIsChangingSeekBarProgress;
    private SuperPlayerDef.PlayerType      mPlayType         = SuperPlayerDef.PlayerType.VOD;
    private SuperPlayerDef.PlayerState     mCurrentPlayState = SuperPlayerDef.PlayerState.END;
    private long                           mDuration;
    private long                           mLivePushDuration;
    private long                           mProgress;
    private Bitmap                         mBackgroundBmp;
    private Bitmap                         mWaterMarkBmp;
    private float                          mWaterMarkBmpX;
    private float                          mWaterMarkBmpY;
    private long                           mLastClickTime;
    private boolean                        mIsOpenGesture    = true;
    private boolean                        isDestroy         = false;
    private VideoGestureDetector.VideoGestureListener mVideoGestureListener;
    private ImageView                      mPiPIV;
    private Context                        mContext;
    private boolean                        mIsShowPIPIv = true;

    public WindowPlayer(Context context) {
        super(context);
        initialize(context);
    }

    public WindowPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public WindowPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    /**
     * Initialize control, gesture detection listener, brightness/volume/playback progress callback
     *
     * 初始化控件、手势检测监听器、亮度/音量/播放进度的回调
     */
    private void initialize(Context context) {
        initView(context);
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isShowingVipView()) {   //When the preview page is displayed, do not handle double-click events
                    return true;
                }
                togglePlayState();
                show();
                if (mHideViewRunnable != null) {
                    removeCallbacks(mHideViewRunnable);
                    postDelayed(mHideViewRunnable, 7000);
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
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
                if (mVideoGestureDetector != null) {
                    mVideoGestureDetector.reset(getWidth(), mSeekBarProgress.getProgress());
                }
                return true;
            }

        });
        mGestureDetector.setIsLongpressEnabled(false);

        mVideoGestureDetector = new VideoGestureDetector(getContext());
        mVideoGestureListener = new VideoGestureDetector.VideoGestureListener() {
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

                }
                if (mSeekBarProgress != null)
                    mSeekBarProgress.setProgress(progress);
            }
        };
        mVideoGestureDetector.setVideoGestureListener(mVideoGestureListener);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.superplayer_vod_player_window, this);
        mLayoutTop = (LinearLayout) findViewById(R.id.superplayer_rl_top);
        mLayoutTop.setOnClickListener(this);
        mLayoutBottom = (LinearLayout) findViewById(R.id.superplayer_ll_bottom);
        mLayoutBottom.setOnClickListener(this);
        mLayoutReplay = (LinearLayout) findViewById(R.id.superplayer_ll_replay);
        mTvTitle = (TextView) findViewById(R.id.superplayer_tv_title);
        mIvPause = (ImageView) findViewById(R.id.superplayer_iv_pause);
        mIvBack = (ImageView) findViewById(R.id.superplayer_iv_back);
        mTvCurrent = (TextView) findViewById(R.id.superplayer_tv_current);
        mTvDuration = (TextView) findViewById(R.id.superplayer_tv_duration);

        mSeekBarProgress = (PointSeekBar) findViewById(R.id.superplayer_seekbar_progress);
        mSeekBarProgress.setProgress(0);
        mSeekBarProgress.setMax(100);
        mIvFullScreen = (ImageView) findViewById(R.id.superplayer_iv_fullscreen);
        mTvBackToLive = (TextView) findViewById(R.id.superplayer_tv_back_to_live);
        mPbLiveLoading = (ProgressBar) findViewById(R.id.superplayer_pb_live);
        mImageCover = (ImageView) findViewById(R.id.superplayer_cover_view);
        mImageStartAndResume = (ImageView) findViewById(R.id.superplayer_resume);
        mIvPlayNext = (ImageView) findViewById(R.id.superplayer_iv_play_next);
        mPiPIV = (ImageView) findViewById(R.id.superplayer_iv_pip);
        mImageStartAndResume.setOnClickListener(this);

        mIvBack.setOnClickListener(this);
        mTvBackToLive.setOnClickListener(this);
        mIvPause.setOnClickListener(this);
        mIvPlayNext.setOnClickListener(this);
        mIvFullScreen.setOnClickListener(this);
        mLayoutTop.setOnClickListener(this);
        mLayoutReplay.setOnClickListener(this);
        mPiPIV.setOnClickListener(this);

        mSeekBarProgress.setOnSeekBarChangeListener(this);

        mGestureVolumeBrightnessProgressLayout =
                (VolumeBrightnessProgressLayout) findViewById(R.id.superplayer_gesture_progress);

        mGestureVideoProgressLayout = (VideoProgressLayout) findViewById(R.id.superplayer_video_progress_layout);

        mBackground = (ImageView) findViewById(R.id.superplayer_small_iv_background);
        setBackground(mBackgroundBmp);

        mIvWatermark = (ImageView) findViewById(R.id.superplayer_small_iv_water_mark);
        mVipWatchView = findViewById(R.id.superplayer_vip_watch_view);
        mVipWatchView.setVipWatchViewClickListener(this);
        mContext = context;
    }

    /**
     * Switch playback status
     * <p>
     * Double-clicking or clicking the play/pause button will trigger this method
     *
     * 切换播放状态
     * <p>
     * 双击和点击播放/暂停按钮会触发此方法
     */
    private void togglePlayState() {
        switch (mCurrentPlayState) {
            case INIT:
            case PAUSE:
            case END:
            case ERROR:
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
     * Switch the visibility of itself
     *
     * 切换自身的可见性
     */
    private void toggle() {
        if (isShowing) {
            hide();
        } else {
            show();
            if (mHideViewRunnable != null) {
                removeCallbacks(mHideViewRunnable);
                postDelayed(mHideViewRunnable, 7000);
            }
        }
    }

    public void setPlayNextButtonVisibility(boolean isShowing) {
        toggleView(mIvPlayNext, isShowing);
    }

    private void updateStartUI(boolean isAutoPlay) {
        mPiPIV.setVisibility((mIsShowPIPIv && PictureInPictureHelper
                .hasPipPermission((Activity) mContext)) ? VISIBLE : GONE);
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
        updateVideoProgress(0, superPlayerModel.duration,0);
        mSeekBarProgress.setEnabled(superPlayerModel.playAction != SuperPlayerModel.PLAY_ACTION_MANUAL_PLAY);
        updateStartUI(superPlayerModel.playAction == SuperPlayerModel.PLAY_ACTION_AUTO_PLAY);
    }

    /**
     * Set watermark
     *
     * 设置水印
     *
     * @param bmp Watermark image.
     *            水印图
     * @param x   X coordinate of watermark
     *            水印的x坐标
     * @param y   Y coordinate of watermark
     *            水印的y坐标
     */
    @Override
    public void setWatermark(final Bitmap bmp, float x, float y) {
        mWaterMarkBmp = bmp;
        mWaterMarkBmpX = x;
        mWaterMarkBmpY = y;
        if (bmp != null) {
            this.post(new Runnable() {
                @Override
                public void run() {
                    int width = WindowPlayer.this.getWidth();
                    int height = WindowPlayer.this.getHeight();

                    int x = (int) (width * mWaterMarkBmpX) - bmp.getWidth() / 2;
                    int y = (int) (height * mWaterMarkBmpY) - bmp.getHeight() / 2;

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

    /**
     * Show control
     *
     * 显示控件
     */
    @Override
    public void show() {
        isShowing = true;
        mLayoutTop.setVisibility(View.VISIBLE);
        mLayoutBottom.setVisibility(View.VISIBLE);

        if (mPlayType == SuperPlayerDef.PlayerType.LIVE_SHIFT) {
            mTvBackToLive.setVisibility(View.VISIBLE);
        }
    }

    public void showOrHideBackBtn(boolean isShow) {
        mIvBack.setVisibility(isShow ? VISIBLE : GONE);
    }

    /**
     * Hide control
     *
     * 隐藏控件
     */
    @Override
    public void hide() {
        isShowing = false;
        mLayoutTop.setVisibility(View.GONE);
        mLayoutBottom.setVisibility(View.GONE);

        if (mPlayType == SuperPlayerDef.PlayerType.LIVE_SHIFT) {
            mTvBackToLive.setVisibility(View.GONE);
        }
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
            case ERROR:
                mIvPause.setImageResource(R.drawable.superplayer_ic_vod_play_normal);
                toggleView(mPbLiveLoading, false);
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
     * Update video name
     *
     * 更新视频名称
     */
    @Override
    public void updateTitle(String title) {
        mTvTitle.setText(title);
    }

    /**
     * Update video playback progress
     *
     * 更新视频播放进度
     */
    @Override
    public void updateVideoProgress(long current, long duration, long playable) {
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
            if (!mIsChangingSeekBarProgress) {
                if (mPlayType == SuperPlayerDef.PlayerType.LIVE) {
                    mSeekBarProgress.setProgress(mSeekBarProgress.getMax());
                } else {
                    mSeekBarProgress.setProgress(progress);
                }
            }
            mTvDuration.setText(formattedTime(mDuration));
        }

        float playAblePercentage = playable > 0 ? ((float) playable / (float) mDuration) : 1.0f;
        if (playable == 0) {
            playAblePercentage = 0;
        }

        if (playAblePercentage >= 0 && playAblePercentage <= 1) {
            int progress = Math.round(playAblePercentage * mSeekBarProgress.getMax());
            if (!mIsChangingSeekBarProgress) {
                if (mPlayType == SuperPlayerDef.PlayerType.VOD) {
                    mSeekBarProgress.setSecondaryProgress(progress);
                } else {
                    mSeekBarProgress.setSecondaryProgress(100);
                }
            }
        }
    }

    @Override
    public void updatePlayType(SuperPlayerDef.PlayerType type) {
        mPlayType = type;
        switch (type) {
            case VOD:
                mTvBackToLive.setVisibility(View.GONE);
                mTvDuration.setVisibility(View.VISIBLE);
                break;
            case LIVE:
                mTvBackToLive.setVisibility(View.GONE);
                mTvDuration.setVisibility(View.GONE);
                mSeekBarProgress.setProgress(100);
                break;
            case LIVE_SHIFT:
                if (mLayoutBottom.getVisibility() == VISIBLE)
                    mTvBackToLive.setVisibility(View.VISIBLE);
                mTvDuration.setVisibility(View.GONE);
                break;
        }
    }


    @Override
    public void updateVipInfo(int position) {
        super.updateVipInfo(position);
        if (mPlayType == SuperPlayerDef.PlayerType.VOD) {
            mVipWatchView.setCurrentTime(position);
        }
    }

    /**
     * Set background
     *
     * 设置背景
     */
    @Override
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

    /**
     * Set the image displayed by the target ImageView
     *
     * 设置目标ImageView显示的图片
     */
    private void setBitmap(ImageView view, Bitmap bitmap) {
        if (view == null || bitmap == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(new BitmapDrawable(getContext().getResources(), bitmap));
        } else {
            view.setBackgroundDrawable(new BitmapDrawable(getContext().getResources(), bitmap));
        }
    }

    /**
     * Show background
     *
     * 显示背景
     */
    @Override
    public void showBackground() {
        post(new Runnable() {
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
    public void release() {
        isDestroy = true;
    }

    /**
     * Hide background
     *
     * 隐藏背景
     */
    @Override
    public void hideBackground() {
        post(new Runnable() {
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

    @Override
    public void setVideoQualityVisible(boolean isShow) {
    }

    /**
     * Override touch event listener to implement gesture control for adjusting brightness,
     * volume, and playback progress
     *
     * 重写触摸事件监听，实现手势调节亮度、音量以及播放进度
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsOpenGesture && mGestureDetector != null) {
            mGestureDetector.onTouchEvent(event);
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            toggle();
        }

        boolean isCall = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
        if (isCall && mVideoGestureDetector != null && mVideoGestureDetector.isVideoProgressModel()) {
            int progress = mVideoGestureDetector.getVideoProgress();
            if (progress > mSeekBarProgress.getMax()) {
                progress = mSeekBarProgress.getMax();
            }
            if (progress < 0) {
                progress = 0;
            }
            mSeekBarProgress.setProgress(progress);

            int seekTime;
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

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            removeCallbacks(mHideViewRunnable);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            postDelayed(mHideViewRunnable, 7000);
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        if (System.currentTimeMillis() - mLastClickTime < 300) { // Limit click frequency
            return;
        }
        mLastClickTime = System.currentTimeMillis();
        int id = view.getId();
        if (id == R.id.superplayer_iv_back) {
            if (mControllerCallback != null) {
                mControllerCallback.onBackPressed(SuperPlayerDef.PlayerMode.WINDOW);
            }
        } else if (id == R.id.superplayer_iv_pause || id == R.id.superplayer_resume) {
            togglePlayState();
        } else if (id == R.id.superplayer_iv_fullscreen) {
            if (mControllerCallback != null) {
                mControllerCallback.onSwitchPlayMode(SuperPlayerDef.PlayerMode.FULLSCREEN);
            }
        } else if (id == R.id.superplayer_ll_replay) {
            if (mControllerCallback != null) {
                mControllerCallback.onResume();
            }
        } else if (id == R.id.superplayer_tv_back_to_live) {
            if (mControllerCallback != null) {
                mControllerCallback.onResumeLive();
            }
        } else if (id == R.id.superplayer_iv_play_next) {
            if (mControllerCallback != null) {
                mControllerCallback.playNext();
            }
        } else if (id == R.id.superplayer_iv_pip) {
            if (mControllerCallback != null) {
                mControllerCallback.enterPictureInPictureMode();
            }
        }
    }

    @Override
    public void onProgressChanged(PointSeekBar seekBar, int progress, boolean fromUser) {
        if (mGestureVideoProgressLayout != null && fromUser) {
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
                    // Close replay button
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


    public void disableGesture(boolean flag) {
        this.mIsOpenGesture = !flag;
    }


    @Override
    public void onClickVipTitleBack() {
        if (mControllerCallback != null) {
            mControllerCallback.onClickVipTitleBack(SuperPlayerDef.PlayerMode.WINDOW);
            mControllerCallback.onSeekTo(0);
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

    public void onVolumeChange(int volume) {
        mVideoGestureListener.onVolumeGesture((float) volume / (float) mVideoGestureDetector.getMaxVolume() * 100);
    }

    public void showPIPIV(boolean isShow) {
        mIsShowPIPIv = isShow;
        mPiPIV.setVisibility((mIsShowPIPIv) && PictureInPictureHelper
                .hasPipPermission((Activity) mContext) ? VISIBLE : GONE);
    }
}
