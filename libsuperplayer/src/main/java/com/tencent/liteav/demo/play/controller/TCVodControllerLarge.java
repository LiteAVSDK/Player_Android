package com.tencent.liteav.demo.play.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
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
import com.tencent.liteav.demo.play.common.TCPlayerConstants;
import com.tencent.liteav.demo.play.net.LogReport;
import com.tencent.liteav.demo.play.utils.TCTimeUtils;
import com.tencent.liteav.demo.play.view.TCPointSeekBar;
import com.tencent.liteav.demo.play.view.TCVideoProgressLayout;
import com.tencent.liteav.demo.play.view.TCVideoQulity;
import com.tencent.liteav.demo.play.view.TCVodMoreView;
import com.tencent.liteav.demo.play.view.TCVodQualityView;
import com.tencent.liteav.demo.play.view.TCVolumeBrightnessProgressLayout;
import com.tencent.rtmp.TXImageSprite;
import com.tencent.rtmp.TXLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyuejiao on 2018/7/3.
 * <p>
 * 超级播放器全屏控制界面
 */
public class TCVodControllerLarge extends TCVodControllerBase
        implements View.OnClickListener, TCVodMoreView.Callback, TCVodQualityView.Callback, TCPointSeekBar.OnSeekBarPointClickListener {
    private static final String TAG = "TCVodControllerLarge";
    private RelativeLayout mLayoutTop;
    private LinearLayout mLayoutBottom;
    private Context mContext;
    private ImageView mIvBack;
    private ImageView mIvPause;
    //    private TextView mTvCurrent;
//    private TextView mTvDuration;
//    private SeekBar mSeekBarProgress;
    private TextView mTvQuality;
    private TextView mTvTitle;
    private ImageView mIvDanmuku;
    private ImageView mIvSnapshot;
    private ImageView mIvLock;
    private ImageView mIvMore;
    private TCVodQualityView mVodQualityView;
    private TCVodMoreView mVodMoreView;
    private boolean mDanmukuOn;
    //    private LinearLayout mLayoutReplay;
    private TextView mTvBackToLive;
//    private ProgressBar mPbLiveLoading;

    private TXImageSprite mTXImageSprite;
    private List<TCPlayKeyFrameDescInfo> mTXPlayKeyFrameDescInfos;
    private TextView mTvVttText;
    private int mSelectedPos = -1;
    private HideLockViewRunnable mHideLockViewRunnable;
    private ImageView mIvWatermark;

    public TCVodControllerLarge(Context context) {
        super(context);
        initViews(context);
    }

    public TCVodControllerLarge(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public TCVodControllerLarge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }

    /**
     * 显示播放控制界面
     */
    @Override
    void onShow() {
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
    }

    /**
     * 隐藏播放控制界面
     */
    @Override
    void onHide() {
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

//    public void updateVideoProgress(long current, long duration) {
//        mTvCurrent.setText(TCTimeUtils.formattedTime(current));
//        if (duration > 0) {
//            float percentage = current / duration;
//            if (percentage >= 0 && percentage <= 1) {
//                int progress = Math.round(percentage * mSeekBarProgress.getMax());
//                mSeekBarProgress.setProgress(progress);
//                mTvDuration.setText(TCTimeUtils.formattedTime(current);
//            }
//        }
//    }

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
    private void initViews(Context context) {
        mHideLockViewRunnable = new HideLockViewRunnable(this);
        mContext = context;
        mLayoutInflater.inflate(R.layout.vod_controller_large, this);

        mLayoutTop = (RelativeLayout) findViewById(R.id.layout_top);
        mLayoutTop.setOnClickListener(this);
        mLayoutBottom = (LinearLayout) findViewById(R.id.layout_bottom);
        mLayoutBottom.setOnClickListener(this);
        mLayoutReplay = (LinearLayout) findViewById(R.id.layout_replay);

        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mIvLock = (ImageView) findViewById(R.id.iv_lock);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mIvPause = (ImageView) findViewById(R.id.iv_pause);
        mIvDanmuku = (ImageView) findViewById(R.id.iv_danmuku);
        mIvMore = (ImageView) findViewById(R.id.iv_more);
        mIvSnapshot = (ImageView) findViewById(R.id.iv_snapshot);
        mTvCurrent = (TextView) findViewById(R.id.tv_current);
        mTvDuration = (TextView) findViewById(R.id.tv_duration);

        mSeekBarProgress = (TCPointSeekBar) findViewById(R.id.seekbar_progress);
        mSeekBarProgress.setProgress(0);
        mSeekBarProgress.setOnPointClickListener(this);
        mSeekBarProgress.setOnSeekBarChangeListener(this);
        mTvQuality = (TextView) findViewById(R.id.tv_quality);
        mTvBackToLive = (TextView) findViewById(R.id.tv_backToLive);
        mPbLiveLoading = (ProgressBar) findViewById(R.id.pb_live);

        mVodQualityView = (TCVodQualityView) findViewById(R.id.vodQualityView);
        mVodQualityView.setCallback(this);
        mVodMoreView = (TCVodMoreView) findViewById(R.id.vodMoreView);
        mVodMoreView.setCallback(this);

        mTvBackToLive.setOnClickListener(this);
        mLayoutReplay.setOnClickListener(this);
        mIvLock.setOnClickListener(this);
        mIvBack.setOnClickListener(this);
        mIvPause.setOnClickListener(this);
        mIvDanmuku.setOnClickListener(this);
        mIvSnapshot.setOnClickListener(this);
        mIvMore.setOnClickListener(this);
        mTvQuality.setOnClickListener(this);
        mTvVttText = (TextView) findViewById(R.id.large_tv_vtt_text);
        mTvVttText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                float time = mTXPlayKeyFrameDescInfos != null ? mTXPlayKeyFrameDescInfos.get(mSelectedPos).time : 0;
                mVodController.seekTo((int) time);
                mVodController.resume();
                mTvVttText.setVisibility(GONE);
                updateReplay(false);
            }
        });
        if (mDefaultVideoQuality != null) {
            mTvQuality.setText(mDefaultVideoQuality.title);
        }
        mGestureVolumeBrightnessProgressLayout = (TCVolumeBrightnessProgressLayout) findViewById(R.id.gesture_progress);
        mGestureVideoProgressLayout = (TCVideoProgressLayout) findViewById(R.id.video_progress_layout);
//        mIvWatermark = findViewById(R.id.large_iv_water_mark);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.iv_back || i == R.id.tv_title) {
            mVodController.onBackPress(SuperPlayerConst.PLAYMODE_FULLSCREEN);

        } else if (i == R.id.iv_pause) {
            changePlayState();

        } else if (i == R.id.iv_danmuku) {
            toggleDanmu();

        } else if (i == R.id.iv_snapshot) {
            mVodController.onSnapshot();

        } else if (i == R.id.iv_more) {
            showMoreView();

        } else if (i == R.id.tv_quality) {
            showQualityView();

        } else if (i == R.id.iv_lock) {
            changeLockState();

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
     * 改变锁屏状态
     */
    private void changeLockState() {
        mLockScreen = !mLockScreen;
        mIvLock.setVisibility(VISIBLE);
        if (mHideLockViewRunnable!=null) {
            this.getHandler().removeCallbacks(mHideLockViewRunnable);
            this.getHandler().postDelayed(mHideLockViewRunnable, 7000);
        }
        if (mLockScreen) {
            mIvLock.setImageResource(R.drawable.ic_player_lock);
            hide();
            mIvLock.setVisibility(VISIBLE);
        } else {
            mIvLock.setImageResource(R.drawable.ic_player_unlock);
            show();
        }
    }

    /**
     * 打开/关闭 弹幕
     */
    private void toggleDanmu() {
        mDanmukuOn = !mDanmukuOn;
        if (mDanmukuOn) {
            mIvDanmuku.setImageResource(R.drawable.ic_danmuku_on);
        } else {
            mIvDanmuku.setImageResource(R.drawable.ic_danmuku_off);
        }
        mVodController.onDanmuku(mDanmukuOn);
    }


    /**
     * 显示右侧更多设置
     */
    private void showMoreView() {
        hide();
        mVodMoreView.setVisibility(View.VISIBLE);
    }

    /**
     * 显示多分辨率UI
     */
    private void showQualityView() {
        if (mVideoQualityList == null || mVideoQualityList.size() == 0) {
            TXLog.i(TAG, "showQualityView mVideoQualityList null");
            return;
        }
        // 设置默认显示分辨率文字
        mVodQualityView.setVisibility(View.VISIBLE);
        if (!mFirstShowQuality && mDefaultVideoQuality != null) {
            for (int i = 0 ; i  < mVideoQualityList.size(); i++) {
                TCVideoQulity quality = mVideoQualityList.get(i);
                if (quality!=null && quality.title!=null &&quality.title.equals(mDefaultVideoQuality.title)) {
                    mVodQualityView.setDefaultSelectedQuality(i);
                    break;
                }
            }
            mFirstShowQuality = true;
        }
        mVodQualityView.setVideoQualityList(mVideoQualityList);
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
     * 更新默认清晰度
     *
     * @param videoQulity
     */
    public void updateVideoQulity(TCVideoQulity videoQulity) {
        mDefaultVideoQuality = videoQulity;
        if (mTvQuality != null) {
            mTvQuality.setText(videoQulity.title);
        }
        if (mVideoQualityList != null && mVideoQualityList.size() != 0) {
            for (int i = 0 ; i  < mVideoQualityList.size(); i++) {
                TCVideoQulity quality = mVideoQualityList.get(i);
                if (quality!=null && quality.title!=null &&quality.title.equals(mDefaultVideoQuality.title)) {
                    mVodQualityView.setDefaultSelectedQuality(i);
                    break;
                }
            }
        }
    }

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


    public void updateVttAndImages(TCPlayImageSpriteInfo info) {
        if (mTXImageSprite != null) {
            releaseTXImageSprite();
        }
        // 有缩略图的时候不显示进度
        mGestureVideoProgressLayout.setProgressVisibility(info == null || info.imageUrls == null || info.imageUrls.size() == 0);
        if (mPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
            mTXImageSprite = new TXImageSprite(getContext());
            if (info != null) {
                // 雪碧图ELK上报
                LogReport.getInstance().uploadLogs(TCPlayerConstants.ELK_ACTION_IMAGE_SPRITE, 0, 0);
                mTXImageSprite.setVTTUrlAndImageUrls(info.webVttUrl, info.imageUrls);
            } else {
                mTXImageSprite.setVTTUrlAndImageUrls(null, null);
            }
        }
    }


    public void updateKeyFrameDescInfos(List<TCPlayKeyFrameDescInfo> list) {
        mTXPlayKeyFrameDescInfos = list;
    }

    @Override
    public void release() {
        super.release();
        releaseTXImageSprite();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            release();
        } catch (Exception e) {
        } catch (Error e) {
        }
    }

    private void releaseTXImageSprite() {
        if (mTXImageSprite != null) {
            Log.i(TAG, "releaseTXImageSprite: release");
            mTXImageSprite.release();
            mTXImageSprite = null;
        }
    }


    @Override
    public void onProgressChanged(TCPointSeekBar seekBar, int progress, boolean isFromUser) {
        super.onProgressChanged(seekBar, progress, isFromUser);
        // 加载点播缩略图
        if (isFromUser && mPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
            setThumbnail(progress);
        }
    }

    @Override
    protected void onGestureVideoProgress(int progress) {
        super.onGestureVideoProgress(progress);
        setThumbnail(progress);
    }


    private void setThumbnail(int progress) {
        float percentage = ((float) progress) / mSeekBarProgress.getMax();
        float seekTime = (mVodController.getDuration() * percentage);
        if (mTXImageSprite != null) {
            Bitmap bitmap = mTXImageSprite.getThumbnail(seekTime);
            if (bitmap != null) {
                mGestureVideoProgressLayout.setThumbnail(bitmap);
            }
        }
    }

    //
    @Override
    public void onSeekBarPointClick(final View view, final int pos) {
        if (mHideLockViewRunnable!=null) {
            this.getHandler().removeCallbacks(mHideViewRunnable);
            this.getHandler().postDelayed(mHideViewRunnable, 7000);
        }
        if (mTXPlayKeyFrameDescInfos != null) {
            //ELK点击上报
            LogReport.getInstance().uploadLogs(TCPlayerConstants.ELK_ACTION_PLAYER_POINT, 0, 0);
            mSelectedPos = pos;
            view.post(new Runnable() {
                @Override
                public void run() {
                    int[] location = new int[2];
                    view.getLocationInWindow(location);

                    int viewX = location[0];
                    TCPlayKeyFrameDescInfo info = mTXPlayKeyFrameDescInfos.get(pos);
                    String content = info.content;

                    mTvVttText.setText(TCTimeUtils.formattedTime((long) info.time) + " " + content);
                    mTvVttText.setVisibility(VISIBLE);
                    adjustVttTextViewPos(viewX);
                }
            });
        }
    }

    /**
     * 根据 PointView x坐标计算出 TextView应该显示出来的位置
     *
     * @param viewX
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

                if (marginLeft + width > getScreenWidth()) {
                    params.leftMargin = getScreenWidth() - width;
                }

                mTvVttText.setLayoutParams(params);
            }
        });
    }

    private int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
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

    /**
     * 更新直播播放时间和进度
     *
     * @param baseTime
     */
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
        super.updatePlayType(playType);
        switch (playType) {
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
                if (mLayoutBottom.getVisibility() == VISIBLE)
                    mTvBackToLive.setVisibility(View.VISIBLE);
                mTvDuration.setVisibility(View.GONE);
                mVodMoreView.updatePlayType(SuperPlayerConst.PLAYTYPE_LIVE_SHIFT);
                break;
        }
    }

    @Override
    public void show() {
        super.show();
        List<TCPointSeekBar.PointParams> pointParams = new ArrayList<>();
        if (mTXPlayKeyFrameDescInfos != null)
            for (TCPlayKeyFrameDescInfo info : mTXPlayKeyFrameDescInfos) {
                int progress = (int) (info.time / mVodController.getDuration() * mSeekBarProgress.getMax());
                pointParams.add(new TCPointSeekBar.PointParams(progress, Color.WHITE));
            }
        mSeekBarProgress.setPointList(pointParams);
    }

    @Override
    public void onQualitySelect(TCVideoQulity quality) {
        mVodController.onQualitySelect(quality);
        mVodQualityView.setVisibility(View.GONE);
    }

    @Override
    public void onSpeedChange(float speedLevel) {
        mVodController.onSpeedChange(speedLevel);
    }

    @Override
    public void onMirrorChange(boolean isMirror) {
        mVodController.onMirrorChange(isMirror);
    }

    @Override
    public void onHWAcceleration(boolean isAccelerate) {
        mVodController.onHWAcceleration(isAccelerate);
    }

    @Override
    protected void onToggleControllerView() {
        super.onToggleControllerView();
        if (mLockScreen) {
            mIvLock.setVisibility(VISIBLE);
            if (mHideLockViewRunnable!=null) {
                this.getHandler().removeCallbacks(mHideLockViewRunnable);
                this.getHandler().postDelayed(mHideLockViewRunnable, 7000);
            }
        }

        if (mVodMoreView.getVisibility() == VISIBLE) {
            mVodMoreView.setVisibility(GONE);
        }
    }

    private static class HideLockViewRunnable implements Runnable{
        private WeakReference<TCVodControllerLarge> mWefControllerLarge;

        public HideLockViewRunnable(TCVodControllerLarge controller) {
            mWefControllerLarge = new WeakReference<>(controller);
        }
        @Override
        public void run() {
            if (mWefControllerLarge!=null && mWefControllerLarge.get()!=null) {
                mWefControllerLarge.get().mIvLock.setVisibility(GONE);
            }
        }
    }


    @Override
    public void setWaterMarkBmp(final Bitmap bmp, final float xR, final float yR) {
        super.setWaterMarkBmp(bmp, xR, yR);
//        if (bmp != null) {
//            this.post(new Runnable() {
//                @Override
//                public void run() {
//                    int width = TCVodControllerLarge.this.getWidth();
//                    int height = TCVodControllerLarge.this.getHeight();
//
//                    int x = (int) (width * xR) - bmp.getWidth() / 2;
//                    int y = (int) (height * yR) - bmp.getHeight() / 2;
//
//                    mIvWatermark.setX(x);
//                    mIvWatermark.setY(y);
//
//                    mIvWatermark.setVisibility(VISIBLE);
//                    setBitmap(mIvWatermark, bmp);
//                }
//            });
//        } else {
//            mIvWatermark.setVisibility(GONE);
//        }
    }
}
