package com.tencent.liteav.demo.player.demo;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.tencent.liteav.demo.common.utils.IntentUtils;
import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.player.view.BitrateView;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VodPlayerActivity extends Activity implements ITXVodPlayListener {
    private static final String TAG                   = "VodPlayerActivity";
    private static final String WEBRTC_LINK_URL       = "https://cloud.tencent.com/document/product/454/12148";
    private static final String DEFAULT_PLAY_URL      = "http://200024424.vod.myqcloud.com/200024424_709ae516bdf811e6ad39991f76a4df69.f20.mp4";
    private static final String SPEED_FORMAT_TEMPLATE = "%.2f";
    private static final int    MILLS_PER_SECOND      = 1000;
    private static final int    MILLS_PER_MINUTE      = 60000;
    private static final int    SEROND_PER_MINUTE     = 60;

    private   TXVodPlayer      mVodPlayer        = null;
    private   TXVodPlayer      mVodPlayerPreload = null;
    private   TXCloudVideoView mPlayerView;
    private   ImageView        mLoadingView;
    private   LinearLayout     mLinearRootView;
    private   Button           mButtonLog;
    private   Button           mButtonPlay;
    private   Button           mButtonRenderRotation;
    private   Button           mButtonRenderMode;
    private   Button           mButtonHWDecode;
    private   Button           mButtonMute;
    private   SeekBar          mSeekBar;
    private   SeekBar          mSeekBarSpeed;
    private   TextView         mTextDuration;
    private   TextView         mTextStart;
    private   TextView         mTextSpeed;
    private   Button           mButtonStop;
    private   Button           mButtonCache;
    protected EditText         mEditRtmpUrlView;
    private   Button           mButtonScan;

    private boolean         mHWDecode    = false;
    private int             mCurrentRenderMode;
    private int             mCurrentRenderRotation;
    private boolean         mEnableMute  = false;
    private boolean         mStartSeek   = false;
    private boolean         mVideoPause  = false;
    private long            mStartPlayTS = 0;
    private TXVodPlayConfig mPlayConfig;
    private boolean         mEnableCache;
    private boolean         mVideoPlay;
    private boolean         mIsLogShow   = false;
    private float           mPlayRate    = 1.0f;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentRenderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
        mCurrentRenderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT;
        mPlayConfig = new TXVodPlayConfig();
        setContentView();
        LinearLayout backLL = (LinearLayout) findViewById(R.id.back_ll);
        backLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayVod();
                finish();
            }
        });
        TextView titleTV = (TextView) findViewById(R.id.title_tv);
        titleTV.setText(getIntent().getStringExtra("TITLE"));
        checkPublishPermission();
        registerForContextMenu(findViewById(R.id.btnPlay));
        getWindow().addFlags(WindowManager.LayoutParams.
                FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onBackPressed() {
        stopPlayVod();
        super.onBackPressed();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.superplayer_menu, menu);
    }

    private boolean checkPublishPermission() {
        PermissionUtils.permission(PermissionConstants.CAMERA, PermissionConstants.STORAGE).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {

            }

            @Override
            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {

            }
        }).request();
        return true;
    }

    void initView() {
        mEditRtmpUrlView = findViewById(R.id.roomid);
        mButtonScan = findViewById(R.id.btnScan);

        mButtonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VodPlayerActivity.this, QRCodeScanActivity.class);
                startActivityForResult(intent, 100);
            }
        });
        mButtonScan.setEnabled(true);
    }

    public void setContentView() {
        super.setContentView(R.layout.superplayer_activity_vod);
        initView();

        mLinearRootView = (LinearLayout) findViewById(R.id.root);
        if (mVodPlayer == null) {
            mVodPlayer = new TXVodPlayer(this);
        }


        mPhoneListener = new TXPhoneStateListener(this, mVodPlayer);
        mPhoneListener.startListen();

        mPlayerView = (TXCloudVideoView) findViewById(R.id.video_view);
        mPlayerView.showLog(false);
        mPlayerView.setLogMargin(12, 12, 110, 60);
        mLoadingView = (ImageView) findViewById(R.id.loadingImageView);


        mEditRtmpUrlView.setHint(R.string.superplayer_please_scan_qrcode);
        mEditRtmpUrlView.setText(DEFAULT_PLAY_URL);

        mVideoPlay = false;

        mButtonPlay = (Button) findViewById(R.id.btnPlay);
        mButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "click playbtn isplay:" + mVideoPlay + " ispause:" + mVideoPause);
                if (mVideoPlay) {
                    if (!mVodPlayer.isPlaying()) {
                        mVodPlayer.resume();
                        mButtonPlay.setBackgroundResource(R.drawable.superplayer_play_pause);
                        mLinearRootView.setBackgroundColor(0xff000000);
                    } else {
                        mVodPlayer.pause();
                        mButtonPlay.setBackgroundResource(R.drawable.superplayer_play_start);
                    }
                    mVideoPause = !mVideoPause;
                } else {
                    mVideoPlay = startPlayVod();
                }
            }
        });

        //停止按钮
        mButtonStop = (Button) findViewById(R.id.btnStop);
        mButtonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayVod();
                mVideoPlay = false;
                mVideoPause = false;
                if (mTextStart != null) {
                    mTextStart.setText("00:00");
                }
                if (mSeekBar != null) {
                    mSeekBar.setProgress(0);
                    mSeekBar.setSecondaryProgress(0);
                }
            }
        });

        mButtonLog = (Button) findViewById(R.id.btnLog);
        mButtonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsLogShow) {
                    mIsLogShow = false;
                    mButtonLog.setBackgroundResource(R.drawable.superplayer_log_show);
                    mPlayerView.showLog(false);
                } else {
                    mIsLogShow = true;
                    mButtonLog.setBackgroundResource(R.drawable.superplayer_log_hidden);
                    mPlayerView.showLog(true);
                }
            }
        });

        //横屏|竖屏
        mButtonRenderRotation = (Button) findViewById(R.id.btnOrientation);
        mButtonRenderRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVodPlayer == null) {
                    return;
                }

                if (mCurrentRenderRotation == TXLiveConstants.RENDER_ROTATION_PORTRAIT) {
                    mButtonRenderRotation.setBackgroundResource(R.drawable.superplayer_portrait);
                    mCurrentRenderRotation = TXLiveConstants.RENDER_ROTATION_LANDSCAPE;
                } else if (mCurrentRenderRotation == TXLiveConstants.RENDER_ROTATION_LANDSCAPE) {
                    mButtonRenderRotation.setBackgroundResource(R.drawable.superplayer_landscape);
                    mCurrentRenderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT;
                }

                mVodPlayer.setRenderRotation(mCurrentRenderRotation);
            }
        });

        //平铺模式
        mButtonRenderMode = (Button) findViewById(R.id.btnRenderMode);
        mButtonRenderMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVodPlayer == null) {
                    return;
                }

                if (mCurrentRenderMode == TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN) {
                    mVodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
                    mButtonRenderMode.setBackgroundResource(R.drawable.superplayer_fill_mode);
                    mCurrentRenderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
                } else if (mCurrentRenderMode == TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION) {
                    mVodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
                    mButtonRenderMode.setBackgroundResource(R.drawable.superplayer_adjust_mode);
                    mCurrentRenderMode = TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN;
                }
            }
        });

        //静音
        mButtonMute = (Button) findViewById(R.id.btnMute);
        mButtonMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVodPlayer == null) {
                    return;
                }
                mEnableMute = !mEnableMute;
                mVodPlayer.setMute(mEnableMute);
                mButtonMute.setBackgroundResource(mEnableMute ? R.drawable.superplayer_mic_disable : R.drawable.superplayer_mic_enable);
            }
        });


        //硬件解码
        mButtonHWDecode = (Button) findViewById(R.id.btnHWDecode);
        mButtonHWDecode.getBackground().setAlpha(mHWDecode ? 255 : 100);
        mButtonHWDecode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHWDecode = !mHWDecode;
                mButtonHWDecode.getBackground().setAlpha(mHWDecode ? 255 : 100);

                if (mHWDecode) {
                    Toast.makeText(getApplicationContext(), getString(R.string.superplayer_open_hardware_speedup), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.superplayer_close_hardware_speedup), Toast.LENGTH_SHORT).show();
                }

                if (mVideoPlay) {

                    stopPlayVod();
                    mVideoPlay = startPlayVod();
                    if (mVideoPause) {
                        if (mPlayerView != null) {
                            mPlayerView.onResume();
                        }
                        mVideoPause = false;
                    }
                }
            }
        });

        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean bFromUser) {
                mTextStart.setText(String.format("%02d:%02d", progress / MILLS_PER_MINUTE, progress / MILLS_PER_SECOND % SEROND_PER_MINUTE));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mStartSeek = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mVodPlayer != null) {
                    mVodPlayer.seek(seekBar.getProgress() / MILLS_PER_SECOND);
                }
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mStartSeek = false;
                    }
                }, 500);
            }
        });

        mTextSpeed = (TextView) findViewById(R.id.text_speed);
        mSeekBarSpeed = (SeekBar) findViewById(R.id.seek_bar_speed);
        mTextSpeed.setText(String.format(SPEED_FORMAT_TEMPLATE, mPlayRate));
        mSeekBarSpeed.setMax(150);
        mSeekBarSpeed.setProgress(50);
        mSeekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float rate = progress * 1.50f / 150 + 0.50f;
                mTextSpeed.setText(String.format(SPEED_FORMAT_TEMPLATE, rate));
                mPlayRate = rate;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mVodPlayer != null) {
                    mVodPlayer.setRate(mPlayRate);
                }
            }
        });

        mTextDuration = (TextView) findViewById(R.id.duration);
        mTextStart = (TextView) findViewById(R.id.play_start);
        mTextDuration.setTextColor(Color.rgb(255, 255, 255));
        mTextStart.setTextColor(Color.rgb(255, 255, 255));

        mButtonCache = (Button) findViewById(R.id.btnCache);
        mButtonCache.getBackground().setAlpha(mEnableCache ? 255 : 100);
        mButtonCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEnableCache = !mEnableCache;
                mButtonCache.getBackground().setAlpha(mEnableCache ? 255 : 100);
            }
        });

        findViewById(R.id.webrtc_link_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(WEBRTC_LINK_URL));
                IntentUtils.safeStartActivity(VodPlayerActivity.this, intent);
            }
        });
    }

    /**
     * 获取内置SD卡路径
     *
     * @return
     */
    public String getInnerSDCardPath() {
        return getExternalFilesDir(null).getAbsolutePath();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mVodPlayer != null) {
            mVodPlayer.stopPlay(true);
            mVodPlayer = null;
        }
        if (mPlayerView != null) {
            mPlayerView.onDestroy();
            mPlayerView = null;
        }
        mPlayConfig = null;
        Log.d(TAG, "vrender onDestroy");
        mPhoneListener.stopListen();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
        if (mVodPlayer != null) {
            mVodPlayer.pause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        if (mVodPlayer != null) {
            mVodPlayer.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (mVideoPlay && !mVideoPause) {
            if (mVodPlayer != null) {
                mVodPlayer.resume();
            }
        }
    }

    protected void enableQRCodeBtn(boolean bEnable) {
        //disable qrcode
        Button btnScan = (Button) findViewById(R.id.btnScan);
        if (btnScan != null) {
            btnScan.setEnabled(bEnable);
        }
    }

    private boolean startPlayVod() {

        String playUrl = mEditRtmpUrlView.getText().toString();
        if (TextUtils.isEmpty(playUrl)) {
            Toast.makeText(getApplicationContext(), getString(R.string.superplayer_not_play_url), Toast.LENGTH_SHORT).show();
            return false;
        }

        mButtonPlay.setBackgroundResource(R.drawable.superplayer_play_pause);
        mLinearRootView.setBackgroundColor(0xff000000);
        mVodPlayer.setPlayerView(mPlayerView);
        mVodPlayer.setVodListener(this);
        mVodPlayer.setRate(mPlayRate);
        // 硬件加速在1080p解码场景下效果显著，但细节之处并不如想象的那么美好：
        // (1) 只有 4.3 以上android系统才支持
        // (2) 兼容性我们目前还仅过了小米华为等常见机型，故这里的返回值您先不要太当真
        mVodPlayer.enableHardwareDecode(mHWDecode);
        mVodPlayer.setRenderRotation(mCurrentRenderRotation);
        mVodPlayer.setRenderMode(mCurrentRenderMode);

        if (mEnableCache) {
            mPlayConfig.setCacheFolderPath(getInnerSDCardPath() + "/txcache");
            mPlayConfig.setMaxCacheItems(1);
        } else {
            mPlayConfig.setCacheFolderPath(null);
        }
        Map<String, String> header = new HashMap<>();
        mPlayConfig.setProgressInterval(200);
        mPlayConfig.setHeaders(header);
        mVodPlayer.setConfig(mPlayConfig);
        mVodPlayer.setAutoPlay(true);
        int result = mVodPlayer.startPlay(playUrl);
        if (result != 0) {
            mButtonPlay.setBackgroundResource(R.drawable.superplayer_play_start);
            mLinearRootView.setBackgroundResource(R.drawable.superplayer_content_bg);
            return false;
        }
        startLoadingAnimation();
        enableQRCodeBtn(false);
        mStartPlayTS = System.currentTimeMillis();
        findViewById(R.id.playerHeaderView).setVisibility(View.VISIBLE);
        BitrateView view = (BitrateView) findViewById(R.id.bitrate_view);
        view.setSelectedIndex(0);
        return true;
    }

    private void stopPlayVod() {
        enableQRCodeBtn(true);
        mButtonPlay.setBackgroundResource(R.drawable.superplayer_play_start);
        mLinearRootView.setBackgroundResource(R.drawable.superplayer_content_bg);
        stopLoadingAnimation();
        if (mVodPlayer != null) {
            mVodPlayer.setVodListener(null);
            mVodPlayer.stopPlay(true);
        }
        mVideoPause = false;
        mVideoPlay = false;
    }

    @Override
    public void onPlayEvent(TXVodPlayer player, int event, Bundle param) {
        if (event != TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
            String playEventLog = "receive event: " + event + ", " + param.getString(TXLiveConstants.EVT_DESCRIPTION);
            Log.d(TAG, playEventLog);
        }

        if (event == TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED || event == TXLiveConstants.PLAY_EVT_VOD_LOADING_END) {
            stopLoadingAnimation();


        }

        if (event == TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED) {
            BitrateView view = (BitrateView) findViewById(R.id.bitrate_view);
            view.setDataSource(mVodPlayer.getSupportedBitrates());
            view.setListener(new BitrateView.OnSelectBitrateListener() {
                @Override
                public void onBitrateIndex(int index) {
                    mVodPlayer.setBitrateIndex(index);
                }
            });
        }

        if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
            stopLoadingAnimation();
            Log.d("AutoMonitor", "PlayFirstRender,cost=" + (System.currentTimeMillis() - mStartPlayTS));
            if (mPhoneListener.isInBackground()) {
                mVodPlayer.pause();
            }


        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
            if (mStartSeek) {
                return;
            }
            int progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS_MS);
            int duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION_MS);
            int playable = param.getInt(TXLiveConstants.EVT_PLAYABLE_DURATION_MS);

            if (mSeekBar != null) {
                mSeekBar.setProgress(progress);
                mSeekBar.setSecondaryProgress(playable);
            }
            if (mTextStart != null) {
                mTextStart.setText(String.format("%02d:%02d", progress / MILLS_PER_MINUTE, progress / MILLS_PER_SECOND % SEROND_PER_MINUTE));
            }
            if (mTextDuration != null) {
                mTextDuration.setText(String.format("%02d:%02d", duration / MILLS_PER_MINUTE, duration / MILLS_PER_SECOND % SEROND_PER_MINUTE));
            }
            if (mSeekBar != null) {
                mSeekBar.setMax(duration);
            }
            return;
        } else if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT || event == TXLiveConstants.PLAY_EVT_PLAY_END || event == TXLiveConstants.PLAY_ERR_FILE_NOT_FOUND) {
            stopPlayVod();
            mVideoPlay = false;
            mVideoPause = false;
            if (mTextStart != null) {
                mTextStart.setText("00:00");
            }
            if (mSeekBar != null) {
                mSeekBar.setProgress(0);
            }
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_LOADING) {
            startLoadingAnimation();
        } else if (event == TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME) {
            stopLoadingAnimation();
            findViewById(R.id.playerHeaderView).setVisibility(View.GONE);
            if (mPhoneListener.isInBackground()) {
                mVodPlayer.pause();
            }
        } else if (event == TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION) {
        } else if (event == TXLiveConstants.PLAY_ERR_HLS_KEY) {//HLS 解密 key 获取失败
            stopPlayVod();
        } else if (event == TXLiveConstants.PLAY_WARNING_RECONNECT) {
            startLoadingAnimation();
        } else if (event == TXLiveConstants.PLAY_EVT_CHANGE_ROTATION) {
            return;
        }

        if (event < 0) {
            Toast.makeText(getApplicationContext(), param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onNetStatus(TXVodPlayer player, Bundle status) {

    }


    private void startLoadingAnimation() {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.VISIBLE);
            ((AnimationDrawable) mLoadingView.getDrawable()).start();
        }
    }

    private void stopLoadingAnimation() {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.GONE);
            ((AnimationDrawable) mLoadingView.getDrawable()).stop();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 100 || data == null || data.getExtras() == null || TextUtils.isEmpty(data.getExtras().getString("result"))) {
            return;
        }
        String result = data.getExtras().getString("result");
        if (mEditRtmpUrlView != null) {
            mEditRtmpUrlView.setText(result);
        }
    }


    static class TXPhoneStateListener extends PhoneStateListener implements Application.ActivityLifecycleCallbacks {
        WeakReference<TXVodPlayer> mPlayer;
        Context                    mContext;
        int                        activityCount;

        public TXPhoneStateListener(Context context, TXVodPlayer player) {
            mPlayer = new WeakReference<>(player);
            mContext = context.getApplicationContext();
        }

        public void startListen() {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(this, PhoneStateListener.LISTEN_CALL_STATE);
        }

        public void stopListen() {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(this, PhoneStateListener.LISTEN_NONE);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            TXVodPlayer player = mPlayer.get();
            switch (state) {
                //电话等待接听
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(TAG, "CALL_STATE_RINGING");
                    if (player != null) player.pause();
                    break;
                //电话接听
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d(TAG, "CALL_STATE_OFFHOOK");
                    if (player != null) player.pause();
                    break;
                //电话挂机
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d(TAG, "CALL_STATE_IDLE");
                    if (player != null && activityCount >= 0) player.resume();
                    break;
            }
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            activityCount++;
            Log.d(TAG, "onActivityResumed" + activityCount);
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            activityCount--;
            Log.d(TAG, "onActivityStopped" + activityCount);
        }

        boolean isInBackground() {
            return (activityCount < 0);
        }
    }

    private TXPhoneStateListener mPhoneListener = null;
}
