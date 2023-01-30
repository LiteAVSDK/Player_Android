package com.tencent.liteav.demo.player.demo.shortvideo.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tencent.liteav.demo.player.demo.shortvideo.core.TXVodPlayerWrapper;
import com.tencent.liteav.shortvideoplayerdemo.R;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.util.Locale;

/**
 * 沉浸式播放组件
 */
public class TXVideoBaseView extends RelativeLayout implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, TXVodPlayerWrapper.OnPlayEventChangedListener {
    private static final String TAG = "TXVideoBaseView";
    private View mRootView;
    private SeekBar mSeekBar;
    private TXCloudVideoView mTXCloudVideoView;
    private ImageView mIvCover;
    private ImageView mPauseImageView;
    private TextView mProgressTime;

    private TXVodPlayerWrapper mTXVodPlayerWrapper;
    private boolean mStartSeek = false;
    private long mTrackingTouchTS = 0;


    public TXVideoBaseView(Context context) {
        super(context);
        init(context);
    }

    public TXVideoBaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TXVideoBaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setTXVodPlayer(TXVodPlayerWrapper TXVodPlayerWrapper) {
        mTXVodPlayerWrapper = TXVodPlayerWrapper;
        mTXVodPlayerWrapper.setPlayerView(mTXCloudVideoView);
        mTXCloudVideoView.requestLayout();
        Log.i(TAG, "[setTXVodPlayer] , PLAY_EVT_PLAY_PROGRESS，" + mTXVodPlayerWrapper.getVodPlayer().hashCode() + " url " + TXVodPlayerWrapper.getUrl());
    }

    private void init(Context context) {
        mRootView = LayoutInflater.from(context).inflate(R.layout.player_item_base_view, null);
        addView(mRootView);
        mSeekBar = mRootView.findViewById(R.id.seekbar_short_video);
        mSeekBar.setOnSeekBarChangeListener(this);
        mIvCover = mRootView.findViewById(R.id.iv_cover);
        mPauseImageView = mRootView.findViewById(R.id.iv_pause);
        mPauseImageView.setOnClickListener(this);
        mTXCloudVideoView = mRootView.findViewById(R.id.tcv_video_view);
        mTXCloudVideoView.setOnClickListener(this);
        mProgressTime = mRootView.findViewById(R.id.tv_progress_time);
        setProgressTimeColor(mProgressTime.getText().toString());
    }

    public void addCustomView(View customView, LayoutParams params) {
        addView(customView, params);
    }

    private void handlePlayProgress(Bundle param) {
        if (mStartSeek) {
            return;
        }

        int progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS);
        int duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION);//单位为s

        int progressMS = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS_MS);
        int durationMS = param.getInt(TXLiveConstants.EVT_PLAY_DURATION_MS);

        long curTS = System.currentTimeMillis();
        // 避免滑动进度条松开的瞬间可能出现滑动条瞬间跳到上一个位置
        if (Math.abs(curTS - mTrackingTouchTS) < 500) {
            return;
        }
        mTrackingTouchTS = curTS;

        if (mSeekBar != null) {
            mSeekBar.setMax(durationMS);
            mSeekBar.setProgress(progressMS);
        }
        if (mProgressTime != null) {
            String tempString = String.format(Locale.CHINA, "%02d:%02d/%02d:%02d", (progress) / 60, progress % 60, (duration) / 60, duration % 60);
            setProgressTimeColor(tempString);
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tcv_video_view || id == R.id.iv_pause) {
            if (mTXVodPlayerWrapper == null) {
                return;
            }
            if (!mTXVodPlayerWrapper.isPlaying()) {
                mTXVodPlayerWrapper.resumePlay();
                mPauseImageView.setVisibility(View.GONE);
            } else {
                pausePlayer();
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mProgressTime != null) {
            String tempString = String.format(Locale.CHINA, "%02d:%02d/%02d:%02d", (progress / 1000) / 60, (progress / 1000) % 60, (seekBar.getMax() / 1000) / 60, (seekBar.getMax() / 1000) % 60);
            setProgressTimeColor(tempString);
        }
    }

    private void setProgressTimeColor(String value) {
        SpannableStringBuilder builder = new SpannableStringBuilder(value);
        ForegroundColorSpan gray = new ForegroundColorSpan(Color.GRAY);
        ForegroundColorSpan white = new ForegroundColorSpan(Color.WHITE);
        builder.setSpan(gray, 5, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(white, 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mProgressTime.setText(builder);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mStartSeek = true;
        // 解决viewPager和的滑动冲突问题
        getParent().requestDisallowInterceptTouchEvent(true);
    }


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // 解决viewPager和的滑动冲突问题
        getParent().requestDisallowInterceptTouchEvent(false);
        if (mTXVodPlayerWrapper != null) {
            Log.i(TAG, "[onStopTrackingTouch] seekBar.getProgress() " + seekBar.getProgress());
            mTXVodPlayerWrapper.seekTo(seekBar.getProgress() / 1000);
        }
        mTrackingTouchTS = System.currentTimeMillis();
        mStartSeek = false;
    }


    public void pausePlayer() {
        if (mTXVodPlayerWrapper != null) {
            mTXVodPlayerWrapper.pausePlay();
            mPauseImageView.setVisibility(View.VISIBLE);
        }
    }


    public void startPlay() {
        if (mTXVodPlayerWrapper != null) {
            mPauseImageView.setVisibility(View.GONE);
            mTXVodPlayerWrapper.setVodChangeListener(this);
            mTXVodPlayerWrapper.resumePlay();
            Log.i(TAG, "[startPlay] mTXVodPlayerWrapper.url " + mTXVodPlayerWrapper.getUrl());
        }
    }

    public void stopPlayer() {
        if (mTXVodPlayerWrapper != null) {
            mTXVodPlayerWrapper.stopPlay();
            mTXVodPlayerWrapper.setVodChangeListener(null);
            Log.i(TAG, "[stopPlayer] mTXVodPlayerWrapper.url " + mTXVodPlayerWrapper.getUrl());
            mPauseImageView.setVisibility(View.GONE);
        }
    }

    public void stopForPlaying() {
        if (mTXVodPlayerWrapper != null) {
            mTXVodPlayerWrapper.stopForPlaying();
            mTXVodPlayerWrapper.setVodChangeListener(null);
            Log.i(TAG, "[stopForPlaying] mTXVodPlayerWrapper.url " + mTXVodPlayerWrapper.getUrl());
            mPauseImageView.setVisibility(View.GONE);
            mIvCover.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onProgress(Bundle bundle) {
        handlePlayProgress(bundle);
    }

    @Override
    public void onRcvFirstFrame() {
        Log.i(TAG,"[onPrepared in TXVideoBaseView]");
        mIvCover.setVisibility(GONE);
    }
}
