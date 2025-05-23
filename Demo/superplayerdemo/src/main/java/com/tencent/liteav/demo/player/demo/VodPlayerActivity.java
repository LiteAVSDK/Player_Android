package com.tencent.liteav.demo.player.demo;

import android.Manifest;
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

import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.superplayer.helper.IntentUtils;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;
import com.tencent.liteav.demo.superplayer.model.utils.VideoQualityUtils;
import com.tencent.liteav.demo.superplayer.ui.view.VodResolutionView;
import com.tencent.liteav.demo.vodcommon.entity.ConfigBean;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXBitrateItem;
import com.tencent.rtmp.TXLiveBase;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXPlayerGlobalSetting;
import com.tencent.rtmp.TXVodConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VodPlayerActivity extends Activity implements ITXVodPlayListener,
        VodResolutionView.OnClickResolutionItemListener {
    public static  final int    REQUEST_CODE_QRCODE_SCAN     = 100;
    public static  final int    REQUEST_CODE_CONFIG          = 101;
    public static  final int    REQUEST_CODE_SELECT_VIDEO    = 102;
    private static final String TAG                   = "VodPlayerActivity";
    private static final String WEBRTC_LINK_URL       = "https://cloud.tencent.com/document/product/454/12148";
    private static final String DEFAULT_PLAY_URL      = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/48d0f1f9387702299774251236/adp.10.m3u8";
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
    private   Button           mButtonLocal;

    private boolean         mHWDecode    = false;
    private int             mCurrentRenderMode;
    private int             mCurrentRenderRotation;
    private VodPlayerState  mPlayerState = VodPlayerState.INIT;
    private boolean         mEnableMute  = false;
    private boolean         mStartSeek   = false;
    private boolean         mVideoPause  = false;
    private boolean         mNeedToPause = false;
    private long            mStartPlayTS = 0;
    private TXVodPlayConfig mPlayConfig;
    private boolean         mEnableCache = false;
    private boolean         mVideoPlay;
    private boolean         mIsLogShow   = false;
    private float           mPlayRate    = 1.0f;
    private VodResolutionView  mVodResolutionView;
    private List<VideoQuality> mVideoQualityList;                      // 画质列表
    private VideoQuality    mDefaultVideoQuality;
    private boolean         mFirstShowQuality = false;
    private boolean         mIsStopped;
    private String          mUrl;
    private TextView        mMediaType;
    private ImageView       mIBSetting;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentRenderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
        mCurrentRenderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT;
        mPlayConfig = new TXVodPlayConfig();
        setContentView();
        setConfig();
        LinearLayout backLL = (LinearLayout) findViewById(R.id.back_ll);
        backLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayVod();
                finish();
            }
        });

        registerForContextMenu(findViewById(R.id.btnPlay));
        getWindow().addFlags(WindowManager.LayoutParams.
                FLAG_KEEP_SCREEN_ON);

        mPhoneListener = new TXPhoneStateListener(this, mVodPlayer);
        checkPublishPermission();
    }

    /**
     * Display the video quality list popup.
     *
     * 显示画质列表弹窗
     */
    private void showQualityView() {
        if (mVideoQualityList == null || mVideoQualityList.size() == 0) {
            return;
        }
        if (mVideoQualityList.size() == 1 && (mVideoQualityList.get(0) == null
                || TextUtils.isEmpty(mVideoQualityList.get(0).title))) {
            return;
        }
        if (mVideoQualityList.size() == 2 && !mUrl.endsWith(".mpd")) {
            return;
        }
        mVodResolutionView.setVisibility(View.VISIBLE);
        if (!mFirstShowQuality && mDefaultVideoQuality != null) {
            for (int i = 0; i < mVideoQualityList.size(); i++) {
                VideoQuality quality = mVideoQualityList.get(i);
                if (quality != null && quality.title != null && quality.title.equals(mDefaultVideoQuality.title)) {
                    mVodResolutionView.setCurrentPosition(i);
                    break;
                }
            }
            mFirstShowQuality = true;
        }
        mVodResolutionView.setModelList(mVideoQualityList);
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
        PermissionUtils.permission(PermissionConstants.CAMERA, PermissionConstants.STORAGE, Manifest.permission.READ_PHONE_STATE).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {
                if (permissionsGranted.contains(Manifest.permission.READ_PHONE_STATE)) {
                    mPhoneListener.startListen();
                }
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
                startActivityForResult(intent, REQUEST_CODE_QRCODE_SCAN);
            }
        });
        mButtonScan.setEnabled(true);

        mButtonLocal = findViewById(R.id.btnLocal);
        mButtonLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "选择视频"), REQUEST_CODE_SELECT_VIDEO);
            }
        });
        mButtonLocal.setEnabled(true);

        mLinearRootView = (LinearLayout) findViewById(R.id.root);
        if (mVodPlayer == null) {
            mVodPlayer = new TXVodPlayer(this);
        }



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
                        mNeedToPause = false;
                        mButtonPlay.setBackgroundResource(R.drawable.superplayer_play_pause);
                        mLinearRootView.setBackgroundColor(0xff000000);
                        enableQRCodeBtn(false);
                    } else {
                        mVodPlayer.pause();
                        mNeedToPause = false;
                        mPlayerState = VodPlayerState.PAUSE;
                        mButtonPlay.setBackgroundResource(R.drawable.superplayer_play_start);
                        enableQRCodeBtn(true);
                    }
                    mVideoPause = !mVideoPause;
                } else {
                    mVideoPlay = startPlayVod();
                }
            }
        });

        mButtonStop = (Button) findViewById(R.id.btnStop);
        mButtonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVodResolutionView.setVisibility(View.GONE);
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
        mButtonLog.setVisibility(View.GONE);
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

        mButtonMute = (Button) findViewById(R.id.btnMute);
        mButtonMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVodPlayer == null) {
                    return;
                }
                mEnableMute = !mEnableMute;
                mVodPlayer.setMute(mEnableMute);
                mButtonMute.setBackgroundResource(mEnableMute ? R.drawable.superplayer_mic_disable :
                        R.drawable.superplayer_mic_enable);
            }
        });
    }

    /**
     * Load layout and initialize view.
     *
     * 加载布局和初始化view
     */
    private void setContentView() {
        super.setContentView(R.layout.superplayer_activity_vod);
        initView();
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
        mMediaType = (TextView) findViewById(R.id.tv_vod_adaptive);
        mMediaType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaType.getText().equals(getString(R.string.super_player_tv_vod_self))) {
                    mMediaType.setText(getString(R.string.super_player_tv_live_self));
                    mPlayConfig.setMediaType(TXVodConstants.MEDIA_TYPE_HLS_LIVE);
                } else {
                    mMediaType.setText(getString(R.string.super_player_tv_vod_self));
                    mPlayConfig.setMediaType(TXVodConstants.MEDIA_TYPE_HLS_VOD);
                }
            }
        });
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

        mIBSetting = (ImageView) findViewById(R.id.superplayer_vod_setting);
        mIBSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VodPlayerActivity.this, PlayerSettingActivity.class);
                startActivityForResult(intent,REQUEST_CODE_CONFIG);
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
                if (mEnableCache) {
                    ConfigBean.getInstance().setCacheFolderPath(getInnerSDCardPath() + "/txcache");
                } else {
                    ConfigBean.getInstance().setCacheFolderPath(null);
                }
                TXPlayerGlobalSetting.setCacheFolderPath(ConfigBean.getInstance().getCacheFolderPath());
                mButtonCache.getBackground().setAlpha(mEnableCache ? 255 : 100);
            }
        });
        mVodResolutionView = findViewById(R.id.vod_quality_view);
        mVodResolutionView.setOnClickResolutionItemListener(this);

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
     * Get the internal SD card path.
     *
     * 获取内置SD卡路径
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
            if (mPlayerState == VodPlayerState.LOADING || mPlayerState == VodPlayerState.INIT) {
                mNeedToPause = true;
            } else {
                mVodPlayer.pause();
            }
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
                mNeedToPause = false;
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
        mUrl = playUrl;
        if (TextUtils.isEmpty(playUrl)) {
            Toast.makeText(getApplicationContext(), getString(R.string.superplayer_not_play_url), Toast.LENGTH_SHORT).show();
            return false;
        }

        mButtonPlay.setBackgroundResource(R.drawable.superplayer_play_pause);
        mLinearRootView.setBackgroundColor(0xff000000);
        mVodPlayer.setPlayerView(mPlayerView);
        mVodPlayer.setVodListener(this);
        mVodPlayer.setRate(mPlayRate);
        // Hardware acceleration has a significant effect in 1080p decoding scenarios,
        // but the details are not as good as imagined:
        // (1) Only Android systems above 4.3 are supported.
        // (2) Compatibility is currently only available for common models such as Xiaomi and Huawei, so you should
        // not take the return value here too seriously.
        mVodPlayer.enableHardwareDecode(mHWDecode);
        mVodPlayer.setRenderRotation(mCurrentRenderRotation);
        mVodPlayer.setRenderMode(mCurrentRenderMode);
        mVodPlayer.setConfig(mPlayConfig);
        mVodPlayer.setAutoPlay(true);
        int result = mVodPlayer.startVodPlay(playUrl);
        mNeedToPause = false;
        mIsStopped = false;
        if (result != 0) {
            mButtonPlay.setBackgroundResource(R.drawable.superplayer_play_start);
            mLinearRootView.setBackgroundResource(R.drawable.superplayer_content_bg);
            return false;
        }
        startLoadingAnimation();
        enableQRCodeBtn(false);
        mStartPlayTS = System.currentTimeMillis();
        findViewById(R.id.playerHeaderView).setVisibility(View.VISIBLE);

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
        mPlayerState = VodPlayerState.END;
    }

    @Override
    public void onPlayEvent(TXVodPlayer player, int event, Bundle param) {
        if (mVodPlayer == null) {
            return;
        }

        if (event != TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
            String playEventLog = "receive event: " + event + ", " + param.getString(TXLiveConstants.EVT_DESCRIPTION);
            Log.d(TAG, playEventLog);
        }

        if (event == TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED || event == TXLiveConstants.PLAY_EVT_VOD_LOADING_END) {
            stopLoadingAnimation();
        }

        if (event == TXLiveConstants.PLAY_EVT_VOD_LOADING_END) {
            mIsStopped = true;
        }

        if (event == TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED) {
            List<TXBitrateItem> bitrateItems = mVodPlayer.getSupportedBitrates();
            int bitrateItemSize = bitrateItems != null ? bitrateItems.size() : 0;
            if (bitrateItemSize > 0) {
                Collections.sort(bitrateItems);
                List<VideoQuality> videoQualities = new ArrayList<>();
                for (int i = 0; i < bitrateItemSize; i++) {
                    TXBitrateItem bitrateItem = bitrateItems.get(i);
                    VideoQuality quality = VideoQualityUtils.convertToVideoQuality(this, bitrateItem);
                    videoQualities.add(quality);
                }
                int bitrateIndex = mVodPlayer.getBitrateIndex();
                VideoQuality defaultQuality = null;
                for (VideoQuality quality : videoQualities) {
                    if (quality.index == bitrateIndex) {
                        defaultQuality = quality;
                    }
                }
                mVideoQualityList = videoQualities;
                if (!mUrl.endsWith(".mpd")) {
                    VideoQuality videoQuality = new VideoQuality();
                    videoQuality.index = -1;
                    videoQuality.title = getResources().getString(R.string.super_player_tv_screen_auto);
                    mVideoQualityList.add(videoQuality);
                }
                updateVideoQuality(defaultQuality);
            }
            if (mVideoQualityList != null && mVideoQualityList.size() > 2) {
                showQualityView();
            }
        }

        if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
            stopLoadingAnimation();
            mPlayerState = VodPlayerState.PLAYING;
            Log.d("AutoMonitor", "PlayFirstRender,cost=" + (System.currentTimeMillis() - mStartPlayTS));
            if (mPhoneListener.isInBackground()) {
                mVodPlayer.pause();
                mNeedToPause = false;
                mPlayerState = VodPlayerState.PAUSE;
            }
            if (mNeedToPause) {
                mVodPlayer.pause();
                mNeedToPause = false;
                mPlayerState = VodPlayerState.PAUSE;
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
            mPlayerState = VodPlayerState.LOADING;
        } else if (event == TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME) {
            stopLoadingAnimation();
            findViewById(R.id.playerHeaderView).setVisibility(View.GONE);
            if (mPhoneListener.isInBackground()) {
                mVodPlayer.pause();
                mPlayerState = VodPlayerState.PAUSE;
                mNeedToPause = false;
            }
        } else if (event == TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION) {
        } else if (event == TXLiveConstants.PLAY_ERR_HLS_KEY) {
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

    private void setConfig() {
        if (ConfigBean.getInstance().isEnableSelfAdaption()) {
            mVodPlayer.setBitrateIndex(-1000);
        }
        mPlayConfig.setEnableAccurateSeek(ConfigBean.getInstance().isEnableAccurateSeek());
        mPlayConfig.setSmoothSwitchBitrate(ConfigBean.getInstance().isSmoothSwitchBitrate());
        mPlayConfig.setAutoRotate(ConfigBean.getInstance().isAutoRotate());
        mPlayConfig.setEnableRenderProcess(ConfigBean.getInstance().isEnableRenderProcess());
        mPlayConfig.setConnectRetryCount(ConfigBean.getInstance().getConnectRetryCount());
        mPlayConfig.setConnectRetryInterval(ConfigBean.getInstance().getConnectRetryInterval());
        mPlayConfig.setTimeout(ConfigBean.getInstance().getTimeout());
        mPlayConfig.setProgressInterval(ConfigBean.getInstance().getProgressInterval());
        TXPlayerGlobalSetting.setCacheFolderPath(ConfigBean.getInstance().getCacheFolderPath());
        TXPlayerGlobalSetting.setMaxCacheSize(ConfigBean.getInstance().getMaxCacheItems());
        mPlayConfig.setMaxBufferSize(ConfigBean.getInstance().getMaxBufferSize());
        mPlayConfig.setPreferredResolution(ConfigBean.getInstance().getPreferredResolution());
        mPlayConfig.setMediaType(ConfigBean.getInstance().getMediaType());
        mVodPlayer.enableHardwareDecode(ConfigBean.getInstance().isEnableHardWareDecode());
        TXLiveBase.setLogLevel(ConfigBean.getInstance().getLogLevel());
        String cacheFolderPath = ConfigBean.getInstance().getCacheFolderPath();
        mEnableCache = (cacheFolderPath != null && !cacheFolderPath.equals(""));
        mButtonCache.getBackground().setAlpha(mEnableCache ? 255 : 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case REQUEST_CODE_CONFIG: {
                setConfig();
            }
                break;
            case REQUEST_CODE_SELECT_VIDEO: {
                if (data != null && data.getData() != null) {
                    Uri videoUri = data.getData();
                    if (mEditRtmpUrlView != null) {
                        mEditRtmpUrlView.setText(videoUri.toString());
                        return;
                    }
                }
            }
                break;
            case REQUEST_CODE_QRCODE_SCAN: {
                if (requestCode != 100 || data == null || data.getExtras() == null ||
                        TextUtils.isEmpty(data.getExtras().getString("result"))) {
                    return;
                }
                String result = data.getExtras().getString("result");
                if (mEditRtmpUrlView != null) {
                    mEditRtmpUrlView.setText(result);
                }
            }
                break;
        }
    }

    static class TXPhoneStateListener extends PhoneStateListener implements Application.ActivityLifecycleCallbacks {
        WeakReference<TXVodPlayer> mPlayer;
        Context                    mContext;
        int                        activityCount;
        private boolean hasListened = false;

        public TXPhoneStateListener(Context context, TXVodPlayer player) {
            mPlayer = new WeakReference<>(player);
            mContext = context.getApplicationContext();
        }

        public void startListen() {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(this, PhoneStateListener.LISTEN_CALL_STATE);
            hasListened = true;
        }

        public void stopListen() {
            if(!hasListened){
                return;
            }
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(this, PhoneStateListener.LISTEN_NONE);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            TXVodPlayer player = mPlayer.get();
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(TAG, "CALL_STATE_RINGING");
                    if (player != null) {
                        player.pause();
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d(TAG, "CALL_STATE_OFFHOOK");
                    if (player != null) {
                        player.pause();
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d(TAG, "CALL_STATE_IDLE");
                    if (player != null && activityCount >= 0) {
                        player.resume();
                    }
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


    @Override
    public void onClickResolutionItem(VideoQuality quality) {
        updateVideoQuality(quality);
        if (mVodPlayer != null) {
            if (quality.url != null) { // br!=0;index=-1;url!=null   //br=0;index!=-1;url!=null
                // Indicates that it is a non-multi-bitrate m3u8 sub-stream and requires manual seek.
                if (!mIsStopped) {
                    float currentTime = mVodPlayer.getCurrentPlaybackTime();
                    mVodPlayer.stopPlay(true);
                    Log.i(TAG, "onQualitySelect quality.url:" + quality.url);
                    mVodPlayer.setStartTime(currentTime);
                    mVodPlayer.startVodPlay(quality.url);
                    mNeedToPause = false;
                }
            } else { //br!=0;index!=-1;url=null
                Log.i(TAG, "setBitrateIndex quality.index:" + quality.index);
                // Indicates that it is a multi-bitrate m3u8 sub-stream, and will automatically seek seamlessly
                mVodPlayer.setBitrateIndex(quality.index);
            }
        }
    }


    public void updateVideoQuality(VideoQuality videoQuality) {
        mDefaultVideoQuality = videoQuality;
        if (mVideoQualityList != null && mVideoQualityList.size() != 0) {
            for (int i = 0; i < mVideoQualityList.size(); i++) {
                VideoQuality quality = mVideoQualityList.get(i);
                if (quality != null && quality.title != null && mDefaultVideoQuality != null
                        && quality.title.equals(mDefaultVideoQuality.title)) {
                    mVodResolutionView.setCurrentPosition(i);
                    break;
                }
            }
        }
    }

    enum VodPlayerState {
        INIT,
        PLAYING,
        PAUSE,
        LOADING,
        END,
    }
}