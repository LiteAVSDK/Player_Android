package com.tencent.liteav.demo.player;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
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

import com.tencent.liteav.demo.player.common.activity.QRCodeScanActivity;
import com.tencent.liteav.demo.player.view.BitrateView;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by annidy on 2017/9/22.
 */

public class VodPlayerActivity extends Activity implements ITXVodPlayListener {
    private static final String TAG = VodPlayerActivity.class.getSimpleName();

    private TXVodPlayer mVodPlayer = null;
    private TXVodPlayer mVodPlayerPreload = null;
    private TXCloudVideoView mPlayerView;
    private ImageView mLoadingView;
    private boolean          mHWDecode   = false;
    private LinearLayout mRootView;

    private Button mBtnLog;
    private Button           mBtnPlay;
    private Button           mBtnRenderRotation;
    private Button           mBtnRenderMode;
    private Button           mBtnHWDecode;
    private SeekBar mSeekBar;
    private TextView mTextDuration;
    private TextView         mTextStart;

    private Button           mBtnStop;
    private Button           mBtnCache;
    private Button           mBtnSpd;
    protected EditText mRtmpUrlView;

    private int              mCurrentRenderMode;
    private int              mCurrentRenderRotation;

    private boolean          mStartSeek = false;
    private boolean          mVideoPause = false;
    private TXVodPlayConfig mPlayConfig;
    private long             mStartPlayTS = 0;

    private boolean mEnableCache;
    private boolean mVideoPlay;
    private boolean mIsLogShow = false;
    private float   mPlayRate = 1.0f;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentRenderMode     = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
        mCurrentRenderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT;

        mPlayConfig = new TXVodPlayConfig();

        setContentView();

        LinearLayout backLL = (LinearLayout)findViewById(R.id.back_ll);
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

        getMenuInflater().inflate(R.menu.player_menu, menu);
    }

    private boolean checkPublishPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this,
                        permissions.toArray(new String[0]),
                        100);
                return false;
            }
        }

        return true;
    }

    void initView() {
        mRtmpUrlView   = (EditText) findViewById(R.id.roomid);

        Button scanBtn = (Button)findViewById(R.id.btnScan);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VodPlayerActivity.this, QRCodeScanActivity.class);
                startActivityForResult(intent, 100);
            }
        });
        scanBtn.setEnabled(true);
    }

    public void setContentView() {
        super.setContentView(R.layout.activity_vod);
        initView();

        mRootView = (LinearLayout) findViewById(R.id.root);
        if (mVodPlayer == null){
            mVodPlayer = new TXVodPlayer(this);
        }


        mPhoneListener = new TXPhoneStateListener(this, mVodPlayer);
        mPhoneListener.startListen();

        mPlayerView = (TXCloudVideoView) findViewById(R.id.video_view);
        mPlayerView.showLog(false);
        mPlayerView.setLogMargin(12, 12, 110, 60);
        mLoadingView = (ImageView) findViewById(R.id.loadingImageView);


        mRtmpUrlView.setHint(" 请输入或扫二维码获取播放地址");
        mRtmpUrlView.setText("http://200024424.vod.myqcloud.com/200024424_709ae516bdf811e6ad39991f76a4df69.f20.mp4");

        mVideoPlay = false;

        mBtnPlay = (Button) findViewById(R.id.btnPlay);
        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "click playbtn isplay:" + mVideoPlay+" ispause:"+mVideoPause);
                if (mVideoPlay) {
                        if (!mVodPlayer.isPlaying()) {
                            mVodPlayer.resume();
                            mBtnPlay.setBackgroundResource(R.drawable.play_pause);
                            mRootView.setBackgroundColor(0xff000000);
                        } else {
                            mVodPlayer.pause();
                            mBtnPlay.setBackgroundResource(R.drawable.play_start);
                        }
                        mVideoPause = !mVideoPause;
                } else {
                    mVideoPlay = startPlayVod();
                }
            }
        });

        //停止按钮
        mBtnStop = (Button) findViewById(R.id.btnStop);
        mBtnStop.setOnClickListener(new View.OnClickListener() {
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

        mBtnLog = (Button) findViewById(R.id.btnLog);
        mBtnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsLogShow) {
                    mIsLogShow = false;
                    mBtnLog.setBackgroundResource(R.drawable.log_show);
                    mPlayerView.showLog(false);
                } else {
                    mIsLogShow = true;
                    mBtnLog.setBackgroundResource(R.drawable.log_hidden);
                    mPlayerView.showLog(true);
                }
            }
        });

        //横屏|竖屏
        mBtnRenderRotation = (Button) findViewById(R.id.btnOrientation);
        mBtnRenderRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVodPlayer == null) {
                    return;
                }

                if (mCurrentRenderRotation == TXLiveConstants.RENDER_ROTATION_PORTRAIT) {
                    mBtnRenderRotation.setBackgroundResource(R.drawable.portrait);
                    mCurrentRenderRotation = TXLiveConstants.RENDER_ROTATION_LANDSCAPE;
                } else if (mCurrentRenderRotation == TXLiveConstants.RENDER_ROTATION_LANDSCAPE) {
                    mBtnRenderRotation.setBackgroundResource(R.drawable.landscape);
                    mCurrentRenderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT;
                }

                mVodPlayer.setRenderRotation(mCurrentRenderRotation);
            }
        });

        //平铺模式
        mBtnRenderMode = (Button) findViewById(R.id.btnRenderMode);
        mBtnRenderMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVodPlayer == null) {
                    return;
                }

                if (mCurrentRenderMode == TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN) {
                    mVodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
                    mBtnRenderMode.setBackgroundResource(R.drawable.fill_mode);
                    mCurrentRenderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
                } else if (mCurrentRenderMode == TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION) {
                    mVodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
                    mBtnRenderMode.setBackgroundResource(R.drawable.adjust_mode);
                    mCurrentRenderMode = TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN;
                }
            }
        });

        //硬件解码
        mBtnHWDecode = (Button) findViewById(R.id.btnHWDecode);
        mBtnHWDecode.getBackground().setAlpha(mHWDecode ? 255 : 100);
        mBtnHWDecode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHWDecode = !mHWDecode;
                mBtnHWDecode.getBackground().setAlpha(mHWDecode ? 255 : 100);

                if (mHWDecode) {
                    Toast.makeText(getApplicationContext(), "已开启硬件解码加速，切换会重启播放流程!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "已关闭硬件解码加速，切换会重启播放流程!", Toast.LENGTH_SHORT).show();
                }

                if (mVideoPlay) {

                    stopPlayVod();
                    mVideoPlay = startPlayVod();
                    if (mVideoPause) {
                        if (mPlayerView != null){
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
                mTextStart.setText(String.format("%02d:%02d",progress/1000/60, progress/1000%60));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mStartSeek = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if ( mVodPlayer != null) {
                    mVodPlayer.seek(seekBar.getProgress()/1000.f);
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

        mTextDuration = (TextView) findViewById(R.id.duration);
        mTextStart = (TextView)findViewById(R.id.play_start);
        mTextDuration.setTextColor(Color.rgb(255, 255, 255));
        mTextStart.setTextColor(Color.rgb(255, 255, 255));

        mBtnCache = (Button)findViewById(R.id.btnCache);
        mBtnCache.getBackground().setAlpha(mEnableCache ? 255 : 100);
        mBtnCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEnableCache = !mEnableCache;
                mBtnCache.getBackground().setAlpha(mEnableCache ? 255 : 100);
            }
        });

//        Button help = (Button)findViewById(R.id.btnHelp);
//        help.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                jumpToHelpPage();
//            }
//        });

        mBtnSpd = (Button)findViewById(R.id.btnSpd);
        mBtnSpd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayRate == 1.0f) {
                    mPlayRate = 1.5f;
                    mBtnSpd.setBackgroundDrawable(getResources().getDrawable(R.drawable.spd1_5));
                } else if (mPlayRate == 1.5f) {
                    mPlayRate = 2.0f;
                    mBtnSpd.setBackgroundDrawable(getResources().getDrawable(R.drawable.spd2));
                } else {
                    mPlayRate = 1.0f;
                    mBtnSpd.setBackgroundDrawable(getResources().getDrawable(R.drawable.spd));
                }
                if (mVodPlayer != null)
                    mVodPlayer.setRate(mPlayRate);
            }
        });

        findViewById(R.id.webrtc_link_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://cloud.tencent.com/document/product/454/12148"));
                startActivity(intent);
            }
        });
    }

    /**
     * 获取内置SD卡路径
     * @return
     */
    public String getInnerSDCardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mVodPlayer != null) {
            mVodPlayer.stopPlay(true);
            mVodPlayer = null;
        }
        if (mPlayerView != null){
            mPlayerView.onDestroy();
            mPlayerView = null;
        }
        mPlayConfig = null;
        Log.d(TAG,"vrender onDestroy");
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
    public void onStop(){
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

        String playUrl = mRtmpUrlView.getText().toString();
        if (TextUtils.isEmpty(playUrl)) {
            Toast.makeText(getApplicationContext(), "无播放地址", Toast.LENGTH_SHORT).show();
            return false;
        }

        mBtnPlay.setBackgroundResource(R.drawable.play_pause);
        mRootView.setBackgroundColor(0xff000000);


//        测试自定义Surface，自定义Surface需要用户按一次开始播放才能真正开始
//        SurfaceView sfv = (SurfaceView)findViewById(R.id.testSurfaceView);
//        Surface sf = sfv.getHolder().getSurface();
//        mVodPlayer.setSurface(sf);
//        sfv.setVisibility(View.VISIBLE);

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
            mPlayConfig.setCacheFolderPath(getInnerSDCardPath()+"/txcache");
            mPlayConfig.setMaxCacheItems(1);
        } else {
            mPlayConfig.setCacheFolderPath(null);
        }
//        mPlayConfig.setPlayerType(TXVodPlayer.PLAYER_TYPE_EXO);
        Map<String, String> header = new HashMap<>();
//        header.put("Referer", "http://demo.vod.qcloud.com/encryption/index.html");
//        header.put("Cookie", "UM_distinctid=15beZDk7GR4RQ");
//        mPlayConfig.setMaxBufferSize(100); // 100M
        mPlayConfig.setProgressInterval(200);
        mPlayConfig.setHeaders(header);
        mVodPlayer.setConfig(mPlayConfig);
        mVodPlayer.setAutoPlay(true);
//        mVodPlayer.setStartTime(224);
        int result = mVodPlayer.startPlay(playUrl); // result返回值：0 success;  -1 empty url;
        if (result != 0) {
            mBtnPlay.setBackgroundResource(R.drawable.play_start);
            mRootView.setBackgroundResource(R.drawable.main_bkg);
            return false;
        }

        Log.w("video render","timetrack start play");

        startLoadingAnimation();

        enableQRCodeBtn(false);

        mStartPlayTS = System.currentTimeMillis();

        findViewById(R.id.playerHeaderView).setVisibility(View.VISIBLE);

        BitrateView view = (BitrateView)findViewById(R.id.bitrate_view);
        view.setSelectedIndex(0);

        return true;
    }

    private  void stopPlayVod() {
        enableQRCodeBtn(true);
        mBtnPlay.setBackgroundResource(R.drawable.play_start);
        mRootView.setBackgroundResource(R.drawable.main_bkg);
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
            BitrateView view = (BitrateView)findViewById(R.id.bitrate_view);
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
            Log.d("AutoMonitor", "PlayFirstRender,cost=" +(System.currentTimeMillis()-mStartPlayTS));
            if (mPhoneListener.isInBackground()) {
                mVodPlayer.pause();
            }


        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_PROGRESS ) {
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
                mTextStart.setText(String.format("%02d:%02d",progress/1000/60,progress/1000%60));
            }
            if (mTextDuration != null) {
                mTextDuration.setText(String.format("%02d:%02d",duration/1000/60,duration/1000%60));
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
//            if (event == TXLiveConstants.PLAY_EVT_PLAY_END) {
//                Log.d(TAG, "mVodPlayerPreload resume");
//                mVodPlayerPreload.setConfig(mPlayConfig);
//                mVodPlayerPreload.setPlayerView(mPlayerView);
//                mVodPlayerPreload.resume();
//            }

        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_LOADING){
            startLoadingAnimation();
        } else if (event == TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME) {
            stopLoadingAnimation();
            findViewById(R.id.playerHeaderView).setVisibility(View.GONE);
            if (mPhoneListener.isInBackground()) {
                mVodPlayer.pause();
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
            ((AnimationDrawable)mLoadingView.getDrawable()).start();
        }
    }

    private void stopLoadingAnimation() {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.GONE);
            ((AnimationDrawable)mLoadingView.getDrawable()).stop();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 100 || data ==null || data.getExtras() == null || TextUtils.isEmpty(data.getExtras().getString("result"))) {
            return;
        }
        String result = data.getExtras().getString("result");
        if (mRtmpUrlView != null) {
            mRtmpUrlView.setText(result);
        }
    }



    static class TXPhoneStateListener extends PhoneStateListener implements Application.ActivityLifecycleCallbacks {
        WeakReference<TXVodPlayer> mPlayer;
        Context mContext;
        int activityCount;

        public TXPhoneStateListener(Context context, TXVodPlayer player) {
            mPlayer = new WeakReference<>(player);
            mContext = context.getApplicationContext();
        }

        public void startListen() {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(this, PhoneStateListener.LISTEN_CALL_STATE);

            PlayApplication.get().registerActivityLifecycleCallbacks(this);
        }

        public void stopListen() {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(this, PhoneStateListener.LISTEN_NONE);

            PlayApplication.get().unregisterActivityLifecycleCallbacks(this);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            TXVodPlayer player = mPlayer.get();
            switch(state){
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
            Log.d(TAG, "onActivityResumed"+activityCount);
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
            Log.d(TAG, "onActivityStopped"+activityCount);
        }

        boolean isInBackground() {
            return (activityCount < 0);
        }
    }
    private TXPhoneStateListener mPhoneListener = null;

    private void jumpToHelpPage() {
        Uri uri = Uri.parse("https://cloud.tencent.com/document/product/454/12148");
        startActivity(new Intent(Intent.ACTION_VIEW,uri));
    }
}