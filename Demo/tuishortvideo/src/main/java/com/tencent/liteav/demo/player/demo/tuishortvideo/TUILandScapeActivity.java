package com.tencent.liteav.demo.player.demo.tuishortvideo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.tencent.liteav.demo.player.demo.tuishortvideo.tools.Utils;
import com.tencent.liteav.demo.player.demo.tuishortvideo.view.VideoSeekBar;
import com.tencent.qcloud.tuiplayer.core.api.model.TUIFileVideoInfo;
import com.tencent.qcloud.tuiplayer.core.api.ui.player.ITUIBasePlayer;
import com.tencent.qcloud.tuiplayer.core.api.ui.player.ITUIVodPlayer;
import com.tencent.qcloud.tuiplayer.core.api.ui.player.TUIVodObserver;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.TUIVideoRenderView;
import com.tencent.rtmp.TXTrackInfo;

import java.util.List;

public class TUILandScapeActivity extends AppCompatActivity implements VideoSeekBar.VideoSeekListener {

    public static final int LAND_SCAPE_CALL_BACK = 1002;
    private static ITUIBasePlayer currentPlayer;
    private static boolean isStarting = false;
    private static final String TAG = "TUILandScapeActivity";

    public static void startLandScapeActivity(ITUIBasePlayer player, Context context) {
        if (!isStarting) {
            isStarting = true;
            currentPlayer = player;
            Intent intent = new Intent(context, TUILandScapeActivity.class);
            Activity activity = (Activity) context;
            activity.startActivityForResult(intent, 1);
            isStarting = false;
        }
    }

    private TUIVideoRenderView mLandScapeView;
    private ImageButton mIbBack;
    private VideoSeekBar mSeekBar;
    private TextView mTvProgress;
    private ImageView mIvPause;
    private DemoLandScapeVodObserver mVodObserver;
    private ITUIBasePlayer mPlayer;
    private boolean mIsEnterFullScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_tuiland_scape);
        mPlayer = currentPlayer;
        mIsEnterFullScreen = false;
        Log.v(TAG, "enter into TUILandScapeActivity, player:" + mPlayer);
        mLandScapeView = findViewById(R.id.txc_vv_landscape);
        mIbBack = findViewById(R.id.ib_back_play);
        mSeekBar = findViewById(R.id.vsb_tui_video_progress);
        mTvProgress = findViewById(R.id.tv_tui_progress_time);
        mIvPause = findViewById(R.id.iv_tui_pause);
        mPlayer.setDisplayView(mLandScapeView);
        if (!mPlayer.isPlaying()) {
            mPlayer.resumePlay();
        }
        mSeekBar.setListener(this);
        mLandScapeView.addListener(new TUIVideoRenderView.TUIRenderViewListener() {
            @Override
            public void onSurfaceTextureUpdated() {

            }

            @Override
            public void onSurfaceChanged(Surface surface, int width, int height) {
                if (null != surface && !mIsEnterFullScreen) {
                    mIsEnterFullScreen = true;
                    enterFullScreen();
                }
            }
        });
        mIbBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initPlayerUI(mPlayer);
    }

    private void initPlayerUI(ITUIBasePlayer player) {
        if (player instanceof ITUIVodPlayer) {
            final ITUIVodPlayer vodPlayer = (ITUIVodPlayer) player;
            vodPlayer.addPlayerObserver(mVodObserver = new DemoLandScapeVodObserver());
            mSeekBar.setVisibility(View.VISIBLE);
            mLandScapeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (vodPlayer.isPlaying()) {
                        vodPlayer.pause();
                    } else {
                        vodPlayer.resumePlay();
                    }
                }
            });
        }
    }

    private void enterFullScreen() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void finish() {
        setResult(LAND_SCAPE_CALL_BACK);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mVodObserver && mPlayer instanceof ITUIVodPlayer) {
            ((ITUIVodPlayer) mPlayer).removePlayerObserver(mVodObserver);
            mVodObserver = null;
        }
        if (null != currentPlayer) {
            currentPlayer = null;
            mPlayer = null;
        }
    }

    @Override
    public void onDragBarChanged(VideoSeekBar seekBar, float progress, final float barProgress) {
        if (mPlayer instanceof ITUIVodPlayer) {
            mTvProgress.post(new Runnable() {
                @Override
                public void run() {
                    ITUIVodPlayer vodPlayer = (ITUIVodPlayer) mPlayer;
                    float duration = vodPlayer.getDuration();
                    String timeStr = Utils.formattedTime((long) (duration * barProgress))
                            + "/"
                            + Utils.formattedTime((long) duration);
                    mTvProgress.setText(timeStr);
                }
            });
        }
    }

    @Override
    public void onStartDrag(VideoSeekBar seekBar) {
        if (null != mTvProgress) {
            mTvProgress.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDragDone(VideoSeekBar seekBar) {
        if (mPlayer instanceof ITUIVodPlayer) {
            ITUIVodPlayer vodPlayer = (ITUIVodPlayer) mPlayer;
            float duration = vodPlayer.getDuration();
            vodPlayer.seekTo(duration *seekBar.getBarProgress());
        }
        if (null != mTvProgress) {
            mTvProgress.setVisibility(View.GONE);
        }
    }

    private class DemoLandScapeVodObserver implements TUIVodObserver {

        private int lastProgressInt;

        @Override
        public void onPlayPrepare() {

        }

        @Override
        public void onPlayEnd() {
            if (null != mIvPause) {
                mIvPause.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onRetryConnect(int code, Bundle bundle) {

        }

        @Override
        public void onPlayProgress(long current, long duration, long playable) {
            if (mSeekBar.getVisibility() == View.VISIBLE) {
                // ensure a refresh at every percentage point
                int progressInt = (int) (((1.0F * current) / duration) * 100);
                if (lastProgressInt != progressInt) {
                    mSeekBar.setAllProgress(progressInt / 100F);
                    lastProgressInt = progressInt;
                }
            }
        }

        @Override
        public void onSeek(float position) {

        }

        @Override
        public void onRcvTrackInformation(List<TXTrackInfo> infoList) {

        }

        @Override
        public void onRcvSubTitleTrackInformation(List<TXTrackInfo> infoList) {

        }

        @Override
        public void onRecFileVideoInfo(TUIFileVideoInfo params) {

        }

        @Override
        public void onResolutionChanged(long width, long height) {

        }

        @Override
        public void onPlayEvent(ITUIVodPlayer player, int event, Bundle bundle) {

        }

        @Override
        public void onFirstFrameRendered() {

        }

        @Override
        public void onPlayLoadingEnd() {

        }

        @Override
        public void onPlayBegin() {
            if (null != mIvPause) {
                mIvPause.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPlayLoading() {

        }

        @Override
        public void onRcvFirstIframe() {

        }

        @Override
        public void onRcvAudioTrackInformation(List<TXTrackInfo> infoList) {

        }

        @Override
        public void onError(int code, String message, Bundle extraInfo) {

        }

        @Override
        public void onPlayPause() {
            if (null != mIvPause) {
                mIvPause.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPlayStop() {

        }
    }
}