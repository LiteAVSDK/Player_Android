package com.tencent.liteav.demo.play;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.play.bean.TCPlayImageSpriteInfo;
import com.tencent.liteav.demo.play.bean.TCPlayInfoStream;
import com.tencent.liteav.demo.play.bean.TCVideoConfig;
import com.tencent.liteav.demo.play.common.TCPlayerConstants;
import com.tencent.liteav.demo.play.controller.TCVodControllerBase;
import com.tencent.liteav.demo.play.controller.TCVodControllerFloat;
import com.tencent.liteav.demo.play.controller.TCVodControllerLarge;
import com.tencent.liteav.demo.play.controller.TCVodControllerSmall;
import com.tencent.liteav.demo.play.net.LogReport;
import com.tencent.liteav.demo.play.net.SuperVodInfoLoader;
import com.tencent.liteav.demo.play.net.TCHttpURLClient;
import com.tencent.liteav.demo.play.utils.NetWatcher;
import com.tencent.liteav.demo.play.utils.PlayInfoResponseParser;
import com.tencent.liteav.demo.play.utils.SuperPlayerUtil;
import com.tencent.liteav.demo.play.view.TCDanmuView;
import com.tencent.liteav.demo.play.view.TCVideoQulity;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXBitrateItem;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by liyuejiao on 2018/7/3.
 */

public class SuperPlayerView extends RelativeLayout implements ITXVodPlayListener, ITXLivePlayListener {
    private static final String TAG = "SuperVodPlayerView";
    private Context mContext;

    private int mPlayMode = SuperPlayerConst.PLAYMODE_WINDOW;
    private boolean mLockScreen = false;

    // UI
    private ViewGroup mRootView;
    private TXCloudVideoView mTXCloudVideoView;
    private TCVodControllerLarge mVodControllerLarge;
    private TCVodControllerSmall mVodControllerSmall;
    private TCVodControllerFloat mVodControllerFloat;

    private TCDanmuView mDanmuView;
    private ViewGroup.LayoutParams mLayoutParamWindowMode;
    private ViewGroup.LayoutParams mLayoutParamFullScreenMode;
    private LayoutParams mVodControllerSmallParams;
    private LayoutParams mVodControllerLargeParams;
    // 点播播放器
    private TXVodPlayer mVodPlayer;
    private TXVodPlayConfig mVodPlayConfig;
    // 直播播放器
    private TXLivePlayer mLivePlayer;
    private TXLivePlayConfig mLivePlayConfig;
    private int mPlayType;

    private PlayerViewCallback mPlayerViewCallback;
    private int mCurrentPlayState = SuperPlayerConst.PLAYSTATE_PLAY;
    private boolean mDefaultSet;
    private TCVideoConfig mTXVideoConfig;
    private SuperPlayerModel mCurrentSuperPlayerModel;
    private long mReportLiveStartTime = -1;
    private long mReportVodStartTime = -1;
    private int mCurrentPlayType;

    private NetWatcher mWatcher;
    private boolean mIsMultiBitrateStream;
    private boolean mIsPlayWithFileid;

    public SuperPlayerView(Context context) {
        super(context);
        initView(context);
    }

    public SuperPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public SuperPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        mRootView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.super_vod_player_view, null);
        mTXCloudVideoView = (TXCloudVideoView) mRootView.findViewById(R.id.cloud_video_view);
        mVodControllerLarge = (TCVodControllerLarge) mRootView.findViewById(R.id.controller_large);
        mVodControllerSmall = (TCVodControllerSmall) mRootView.findViewById(R.id.controller_small);
        mVodControllerFloat = (TCVodControllerFloat) mRootView.findViewById(R.id.controller_float);
        mDanmuView = (TCDanmuView) mRootView.findViewById(R.id.danmaku_view);

        mVodControllerSmallParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mVodControllerLargeParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        mVodControllerLarge.setVodController(mVodController);
        mVodControllerSmall.setVodController(mVodController);
        mVodControllerFloat.setVodController(mVodController);

        removeAllViews();
        mRootView.removeView(mDanmuView);
        mRootView.removeView(mTXCloudVideoView);
        mRootView.removeView(mVodControllerSmall);
        mRootView.removeView(mVodControllerLarge);
        mRootView.removeView(mVodControllerFloat);

        addView(mTXCloudVideoView);
        if (mPlayMode == SuperPlayerConst.PLAYMODE_FULLSCREEN) {
            addView(mVodControllerLarge);
            mVodControllerLarge.hide();
        } else if (mPlayMode == SuperPlayerConst.PLAYMODE_WINDOW) {
            addView(mVodControllerSmall);
            mVodControllerSmall.hide();
        }
        addView(mDanmuView);

        post(new Runnable() {
            @Override
            public void run() {
                if (mPlayMode == SuperPlayerConst.PLAYMODE_WINDOW) {
                    mLayoutParamWindowMode = getLayoutParams();
                }
                try {
                    // 依据上层Parent的LayoutParam类型来实例化一个新的fullscreen模式下的LayoutParam
                    Class parentLayoutParamClazz = getLayoutParams().getClass();
                    Constructor constructor = parentLayoutParamClazz.getDeclaredConstructor(int.class, int.class);
                    mLayoutParamFullScreenMode = (ViewGroup.LayoutParams) constructor.newInstance(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        LogReport.getInstance().setAppName(getApplicationName());
        LogReport.getInstance().setPackageName(getPackagename());
    }

    private String getApplicationName() {
        Context context = mContext.getApplicationContext();
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    private String getPackagename() {
            PackageInfo info;
            String packagename = "";
            if (mContext != null) {
                try {
                    info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);

                    // 当前版本的包名
                    packagename = info.packageName;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return packagename;
    }

    /**
     * 初始化点播播放器
     *
     * @param context
     */
    private void initVodPlayer(Context context) {
        if (mVodPlayer != null)
            return;
        mVodPlayer = new TXVodPlayer(context);

        SuperPlayerGlobalConfig config = SuperPlayerGlobalConfig.getInstance();

        mVodPlayConfig = new TXVodPlayConfig();
        mVodPlayConfig.setCacheFolderPath(Environment.getExternalStorageDirectory().getPath() + "/txcache");
        mVodPlayConfig.setMaxCacheItems(config.maxCacheItem);

        mVodPlayer.setConfig(mVodPlayConfig);
        mVodPlayer.setRenderMode(config.renderMode);
        mVodPlayer.setVodListener(this);
        mVodPlayer.enableHardwareDecode(config.enableHWAcceleration);
    }

    /**
     * 初始化直播播放器
     *
     * @param context
     */
    private void initLivePlayer(Context context) {
        if (mLivePlayer != null)
            return;
        mLivePlayer = new TXLivePlayer(context);

        SuperPlayerGlobalConfig config = SuperPlayerGlobalConfig.getInstance();

        mLivePlayConfig = new TXLivePlayConfig();
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Referer", "qcloud.com");
        mLivePlayConfig.setHeaders(headers);

        mLivePlayer.setConfig(mLivePlayConfig);
        mLivePlayer.setRenderMode(config.renderMode);
        mLivePlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
        mLivePlayer.setPlayListener(this);
        mLivePlayer.enableHardwareDecode(config.enableHWAcceleration);
    }

    /**
     * 播放一个点播或直播视频
     *
     * @param superPlayerModel
     */
    public void playWithMode(SuperPlayerModel superPlayerModel) {
        initLivePlayer(getContext());
        initVodPlayer(getContext());
        stopPlay();

        boolean isLivePlay = isLivePlay(superPlayerModel);
        TXCLog.i(TAG, "playWithMode isLivePlay:" + isLivePlay);

        if (isLivePlay) {
            mReportLiveStartTime = System.currentTimeMillis();
            mLivePlayer.setPlayerView(mTXCloudVideoView);
            if (mPlayType == TXLivePlayer.PLAY_TYPE_LIVE_FLV) { // flv流，再去判断是否支持时移

                playTimeShiftLiveURL(superPlayerModel);

                if (superPlayerModel.multiVideoURLs != null && !superPlayerModel.multiVideoURLs.isEmpty()) {
                    playMultiStreamLiveURL(superPlayerModel);
                }  else {
                    ArrayList<TCVideoQulity> videoQulities = new ArrayList<>();
                    TCVideoQulity quality = new TCVideoQulity();
                    quality.index = 2;
                    quality.name = "FHD";
                    quality.title = "超清";
                    quality.url = superPlayerModel.videoURL;
                    videoQulities.add(quality);
                    mVodControllerLarge.setVideoQualityList(videoQulities);
                    mVodControllerLarge.updateVideoQulity(quality);
                }

            } else { // 普通直播流，不支持时移，不支持多分辨率切换
                ArrayList<TCVideoQulity> videoQulities = new ArrayList<>();
                mVodControllerLarge.setVideoQualityList(videoQulities);
                playNormalLiveURL(superPlayerModel);
            }
        } else {
            mReportVodStartTime = System.currentTimeMillis();
            mVodPlayer.setPlayerView(mTXCloudVideoView);
            if (!TextUtils.isEmpty(superPlayerModel.videoURL)) {
                playWithURL(superPlayerModel);
            } else {
                playWithFileId(superPlayerModel);
            }
        }
        mCurrentPlayType = isLivePlay ? SuperPlayerConst.PLAYTYPE_LIVE : SuperPlayerConst.PLAYTYPE_VOD;

        mVodControllerSmall.updatePlayType(isLivePlay ? SuperPlayerConst.PLAYTYPE_LIVE : SuperPlayerConst.PLAYTYPE_VOD);
        mVodControllerLarge.updatePlayType(isLivePlay ? SuperPlayerConst.PLAYTYPE_LIVE : SuperPlayerConst.PLAYTYPE_VOD);

        mVodControllerSmall.updateTitle(superPlayerModel.title);
        mVodControllerLarge.updateTitle(superPlayerModel.title);

        mVodControllerSmall.updateVideoProgress(0, 0);
        mVodControllerLarge.updateVideoProgress(0, 0);

        TCPlayImageSpriteInfo info = superPlayerModel.imageInfo;
        mVodControllerLarge.updateVttAndImages(info);
        mVodControllerLarge.updateKeyFrameDescInfos(superPlayerModel.keyFrameDescInfos);
    }

    /**
     * 解析直播URL
     *
     * @param videoURL
     */
    private void playLiveURL(String videoURL) {
        if (mLivePlayer != null) {
            mLivePlayer.setPlayListener(this);
            int result = mLivePlayer.startPlay(videoURL, mPlayType); // result返回值：0 success;  -1 empty url; -2 invalid url; -3 invalid playType;
            if (result != 0) {
                TXCLog.e(TAG, "playLiveURL videoURL:" + videoURL + ",result:" + result);
            } else {
                mCurrentPlayState = SuperPlayerConst.PLAYSTATE_PLAY;
                TXCLog.e(TAG, "playLiveURL mCurrentPlayState:" + mCurrentPlayState);
            }
        }
    }

    /**
     * 判断是否直播
     *
     * @param superPlayerModel
     * @return
     */
    private boolean isLivePlay(SuperPlayerModel superPlayerModel) {
        String videoURL = superPlayerModel.videoURL;
        if (TextUtils.isEmpty(superPlayerModel.videoURL)) {
            return false;
        }
        if (videoURL.startsWith("rtmp://")) {
            mPlayType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP;
            return true;
        } else if ((videoURL.startsWith("http://") || videoURL.startsWith("https://")) && videoURL.contains(".flv")) {
            mPlayType = TXLivePlayer.PLAY_TYPE_LIVE_FLV;
            return true;
        } else {
            return false;
        }
    }

    /**
     * 播放点播
     *
     * @param superPlayerModel
     */
    private void playWithURL(SuperPlayerModel superPlayerModel) {
        mCurrentSuperPlayerModel = superPlayerModel;
        TXCLog.i(TAG, "playWithURL videoURL:" + superPlayerModel.videoURL);
        String videoURL = parseVodURL(superPlayerModel);

        if (videoURL.endsWith(".m3u8")) {
            mIsMultiBitrateStream = true;
        }
        if (mVodPlayer != null) {
            mDefaultSet = false;
            mVodPlayer.setAutoPlay(true);
            mVodPlayer.setVodListener(this);
            int ret = mVodPlayer.startPlay(videoURL);
            if (ret == 0) {
                mCurrentPlayState = SuperPlayerConst.PLAYSTATE_PLAY;
                TXCLog.e(TAG, "playWithURL mCurrentPlayState:" + mCurrentPlayState);
            }
        }
        mIsPlayWithFileid = false;
    }

    /**
     * 解析点播地址
     *
     * @param superPlayerModel
     * @return
     */
    private String parseVodURL(SuperPlayerModel superPlayerModel) {
        return superPlayerModel.videoURL;
    }

    /**
     * 解析直播地址
     *
     * @param superPlayerModel
     * @return
     */
    private void playTimeShiftLiveURL(final SuperPlayerModel superPlayerModel) {
        mCurrentSuperPlayerModel = superPlayerModel;
        final String liveURL = superPlayerModel.videoURL;
        final String bizid = liveURL.substring(liveURL.indexOf("//") + 2, liveURL.indexOf("."));
        final String domian = SuperPlayerGlobalConfig.getInstance().playShiftDomain;
        final String streamid = liveURL.substring(liveURL.lastIndexOf("/") + 1, liveURL.lastIndexOf("."));
        final int appid = superPlayerModel.appid;
        TXCLog.i(TAG, "bizid:" + bizid + ",streamid:" + streamid + ",appid:" + appid);

        mTXVideoConfig = new TCVideoConfig();
        mTXVideoConfig.isLive = true;
        mTXVideoConfig.appid = appid;
        mTXVideoConfig.streamid = streamid;
        mTXVideoConfig.bizid = bizid;
        mTXVideoConfig.videoURL = liveURL;
        mTXVideoConfig.isNormalLive = false;

        playLiveURL(liveURL);


        try{
            int bizidNum = Integer.valueOf(bizid);
            mLivePlayer.prepareLiveSeek(domian, bizidNum);
        }catch (NumberFormatException e) {
            e.printStackTrace();
            Log.e(TAG, "playTimeShiftLiveURL: bizidNum 错误 = %s " + mTXVideoConfig.bizid);
        }
    }

    private void playNormalLiveURL(SuperPlayerModel superPlayerModel) {
        mCurrentSuperPlayerModel = superPlayerModel;
        TXCLog.i(TAG, "playNormalLiveURL videoURL:" + superPlayerModel.videoURL);

        mTXVideoConfig = new TCVideoConfig();
        mTXVideoConfig.isLive = true;
        mTXVideoConfig.videoURL = superPlayerModel.videoURL;
        mTXVideoConfig.isNormalLive = true;
        playLiveURL(mTXVideoConfig.videoURL);
    }

    private void playMultiStreamLiveURL(SuperPlayerModel superPlayerModel) {
        mLivePlayConfig.setAutoAdjustCacheTime(false);
        mLivePlayConfig.setMaxAutoAdjustCacheTime(5);
        mLivePlayConfig.setMinAutoAdjustCacheTime(5);
        mLivePlayer.setConfig(mLivePlayConfig);

        ArrayList<TCVideoQulity> videoQulities = new ArrayList<>();

        TCVideoQulity quality = new TCVideoQulity();
        if (!TextUtils.isEmpty(superPlayerModel.videoURL) && superPlayerModel.videoURL.contains("5815.liveplay.myqcloud.com")) {
            quality.index = 0;
            quality.name = "SD";
            quality.title = "标清";
            quality.url = superPlayerModel.videoURL.replace(".flv", "_550.flv");
            videoQulities.add(quality);

            quality = new TCVideoQulity();
            quality.index = 1;
            quality.name = "HD";
            quality.title = "高清";
            quality.url = superPlayerModel.videoURL.replace(".flv", "_900.flv");
            videoQulities.add(quality);
        }
        quality = new TCVideoQulity();
        quality.index = 2;
        quality.name = "FHD";
        quality.title = "超清";
        quality.url = superPlayerModel.videoURL;
        videoQulities.add(quality);

        mVodControllerLarge.setVideoQualityList(videoQulities);
        mVodControllerLarge.updateVideoQulity(quality);
        if (mWatcher == null) mWatcher = new NetWatcher(mContext);
        mWatcher.start(superPlayerModel.videoURL, mLivePlayer);
    }


    /**
     * 播放点播fileId
     *
     * @param superPlayerModel
     */
    private void playWithFileId(final SuperPlayerModel superPlayerModel) {
        SuperVodInfoLoader loader = new SuperVodInfoLoader();
        loader.setOnVodInfoLoadListener(new SuperVodInfoLoader.OnVodInfoLoadListener() {
            @Override
            public void onSuccess(PlayInfoResponseParser response) {
                SuperPlayerModel playerModel = new SuperPlayerModel();
                TCPlayInfoStream masterPlayList = response.getMasterPlayList();
                playerModel.imageInfo = response.getImageSpriteInfo();
                playerModel.keyFrameDescInfos = response.getKeyFrameDescInfos();
                if (masterPlayList != null) { //有masterPlaylist
                    String videoURL = masterPlayList.getUrl();
                    playerModel.videoURL = videoURL;
                    playWithURL(playerModel);
                    mIsMultiBitrateStream = true;
                    mIsPlayWithFileid = true;
                    return;
                }

                LinkedHashMap<String, TCPlayInfoStream> transcodeList = response.getTranscodePlayList();
                if (transcodeList != null && transcodeList.size() != 0) { //没有transcodePlaylist
                    String defaultClassification = response.getDefaultVideoClassification();
                    TCPlayInfoStream stream = transcodeList.get(defaultClassification);
                    String videoURL = stream.getUrl();
                    playerModel.videoURL = videoURL;
                    playWithURL(playerModel);

                    TCVideoQulity defaultVideoQulity = SuperPlayerUtil.convertToVideoQuality(stream);
                    mVodControllerLarge.updateVideoQulity(defaultVideoQulity);

                    ArrayList<TCVideoQulity> videoQulities = SuperPlayerUtil.convertToVideoQualityList(transcodeList);
                    mVodControllerLarge.setVideoQualityList(videoQulities);
                    mIsMultiBitrateStream = false;
                    mIsPlayWithFileid = true;
                    return;
                }
                TCPlayInfoStream sourceStream = response.getSource();
                if (sourceStream != null) {
                    String videoURL = sourceStream.getUrl();
                    playerModel.videoURL = videoURL;
                    playWithURL(playerModel);
                    String defaultClassification = response.getDefaultVideoClassification();

                    if (defaultClassification != null) {
                        TCVideoQulity defaultVideoQulity = SuperPlayerUtil.convertToVideoQuality(sourceStream, defaultClassification);
                        mVodControllerLarge.updateVideoQulity(defaultVideoQulity);

                        ArrayList<TCVideoQulity> videoQulities = new ArrayList<>();
                        videoQulities.add(defaultVideoQulity);
                        mVodControllerLarge.setVideoQualityList(videoQulities);
                        mIsMultiBitrateStream = false;
                    }
                }

            }

            @Override
            public void onFail(int errCode) {

            }
        });
        loader.getVodByFileId(superPlayerModel);
    }

    public void onResume() {
        if (mDanmuView != null && mDanmuView.isPrepared() && mDanmuView.isPaused()) {
            mDanmuView.resume();
        }
        resume();
    }

    private void resume() {
        if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
            if (mVodPlayer != null) {
                mVodPlayer.resume();
            }

        } else {
//            if (mLivePlayer != null) {
//                mLivePlayer.resume();
//            }
        }
    }

    public void onPause() {
        if (mDanmuView != null && mDanmuView.isPrepared()) {
            mDanmuView.pause();
        }
        pause();
    }

    private void pause() {
        if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
            if (mVodPlayer != null) {
                mVodPlayer.pause();
            }
        } else {
//            if (mLivePlayer != null) {
//                mLivePlayer.pause();
//            }
        }
    }

    public void resetPlayer() {
        if (mDanmuView != null) {
            mDanmuView.release();
            mDanmuView = null;
        }
        stopPlay();
    }

    private void stopPlay() {
        if (mVodPlayer != null) {
            mVodPlayer.setVodListener(null);
            mVodPlayer.stopPlay(false);
        }
        if (mLivePlayer != null) {
            mLivePlayer.setPlayListener(null);
            mLivePlayer.stopPlay(false);
            mTXCloudVideoView.removeVideoView();
        }
        if (mWatcher != null) {
            mWatcher.stop();
        }
        mCurrentPlayState = SuperPlayerConst.PLAYSTATE_PAUSE;
        TXCLog.e(TAG, "stopPlay mCurrentPlayState:" + mCurrentPlayState);
        reportPlayTime();
    }

    private void reportPlayTime() {
        if (mReportLiveStartTime != -1) {
            long reportEndTime = System.currentTimeMillis();
            long diff = (reportEndTime - mReportLiveStartTime) / 1000;
            LogReport.getInstance().uploadLogs(TCPlayerConstants.ELK_ACTION_LIVE_TIME, diff, 0);
            mReportLiveStartTime = -1;
        }
        if (mReportVodStartTime != -1) {
            long reportEndTime = System.currentTimeMillis();
            long diff = (reportEndTime - mReportVodStartTime) / 1000;
            LogReport.getInstance().uploadLogs(TCPlayerConstants.ELK_ACTION_VOD_TIME, diff, mIsPlayWithFileid?1:0);
            mReportVodStartTime = -1;
        }
    }

    /**
     * 设置超级播放器的回掉
     *
     * @param callback
     */
    public void setPlayerViewCallback(PlayerViewCallback callback) {
        mPlayerViewCallback = callback;
    }

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private boolean mChangeHWAcceleration;
    private int mSeekPos;

    private void fullScreen(boolean isFull) {//控制是否全屏显示
        if (getContext() instanceof Activity) {
            Activity activity  = (Activity) getContext();
            if (isFull) {
                //隐藏虚拟按键，并且全屏
                View decorView = activity.getWindow().getDecorView();
                if (decorView == null) return;
                if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
                    decorView.setSystemUiVisibility(View.GONE);
                } else if (Build.VERSION.SDK_INT >= 19) {
                    int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
                    decorView.setSystemUiVisibility(uiOptions);
                }
            } else {
                View decorView = activity.getWindow().getDecorView();
                if (decorView == null) return;

                if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
                    decorView.setSystemUiVisibility(View.VISIBLE);
                } else if (Build.VERSION.SDK_INT >= 19) {
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }
            }

        }
    }
    /**
     * 播放器控制
     */
    private TCVodControllerBase.VodController mVodController = new TCVodControllerBase.VodController() {
        /**
         * 请求播放模式：窗口/全屏/悬浮窗
         * @param requestPlayMode
         */
        @Override
        public void onRequestPlayMode(int requestPlayMode) {
            if (mPlayMode == requestPlayMode)
                return;

            if (mLockScreen) //锁屏
                return;

            if (requestPlayMode ==SuperPlayerConst.PLAYMODE_FULLSCREEN) {
                fullScreen(true);
            } else {
                fullScreen(false);
            }
            mVodControllerFloat.hide();
            mVodControllerSmall.hide();
            mVodControllerLarge.hide();
            //请求全屏模式
            if (requestPlayMode == SuperPlayerConst.PLAYMODE_FULLSCREEN) {
                TXCLog.i(TAG, "requestPlayMode FullScreen");

                if (mLayoutParamFullScreenMode == null)
                    return;

                removeView(mVodControllerSmall);
                addView(mVodControllerLarge, mVodControllerLargeParams);
                setLayoutParams(mLayoutParamFullScreenMode);
                rotateScreenOrientation(SuperPlayerConst.ORIENTATION_LANDSCAPE);

                if (mPlayerViewCallback != null) {
                    mPlayerViewCallback.hideViews();
                }
            }
            // 请求窗口模式
            else if (requestPlayMode == SuperPlayerConst.PLAYMODE_WINDOW) {
                TXCLog.i(TAG, "requestPlayMode Window");

                // 当前是悬浮窗
                if (mPlayMode == SuperPlayerConst.PLAYMODE_FLOAT) {
                    try{
                        Intent intent = new Intent();
                        // 这里要与Activity的Intent Filter对应起来，否则会crash
                        intent.setAction("com.tencent.liteav.demo.play.action.float.click");
                        mContext.startActivity(intent);

                        pause();
                        if (mLayoutParamWindowMode == null)
                            return;
                        mWindowManager.removeView(mVodControllerFloat);

                        if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
                            mVodPlayer.setPlayerView(mTXCloudVideoView);
                        } else {
                            mLivePlayer.setPlayerView(mTXCloudVideoView);
                        }
                        resume();
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // 当前是全屏模式
                else if (mPlayMode == SuperPlayerConst.PLAYMODE_FULLSCREEN) {
                    if (mLayoutParamWindowMode == null)
                        return;

                    removeView(mVodControllerLarge);
                    addView(mVodControllerSmall, mVodControllerSmallParams);
                    setLayoutParams(mLayoutParamWindowMode);
                    rotateScreenOrientation(SuperPlayerConst.ORIENTATION_PORTRAIT);

                    if (mPlayerViewCallback != null) {
                        mPlayerViewCallback.showViews();
                    }
                }
            }
            //请求悬浮窗模式
            else if (requestPlayMode == SuperPlayerConst.PLAYMODE_FLOAT) {
                TXCLog.i(TAG, "requestPlayMode Float :" + Build.MANUFACTURER);

                SuperPlayerGlobalConfig prefs = SuperPlayerGlobalConfig.getInstance();
                if (!prefs.enableFloatWindow) {
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 6.0动态申请悬浮窗权限
                    if (!Settings.canDrawOverlays(mContext)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        intent.setData(Uri.parse("package:" + mContext.getPackageName()));
                        mContext.startActivity(intent);
                        return;
                    }
                } else {
                    if (!checkOp(mContext, OP_SYSTEM_ALERT_WINDOW)) {
                        Toast.makeText(mContext, "进入设置页面失败,请手动开启悬浮窗权限", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                pause();

                mWindowManager = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                mWindowParams = new WindowManager.LayoutParams();
                mWindowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                mWindowParams.format = PixelFormat.TRANSLUCENT;
                mWindowParams.gravity = Gravity.LEFT | Gravity.TOP;

                SuperPlayerGlobalConfig.TXRect rect = prefs.floatViewRect;
                mWindowParams.x = rect.x;
                mWindowParams.y = rect.y;
                mWindowParams.width = rect.width;
                mWindowParams.height = rect.height;

                mWindowManager.addView(mVodControllerFloat, mWindowParams);

                TXCloudVideoView videoView = mVodControllerFloat.getFloatVideoView();
                if (videoView != null) {
                    if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
                        mVodPlayer.setPlayerView(videoView);
                    } else {
                        mLivePlayer.setPlayerView(videoView);
                    }
                    resume();
                }
                // 悬浮窗上报
                LogReport.getInstance().uploadLogs(TCPlayerConstants.ELK_ACTION_FLOATMOE, 0, 0);
            }
            mPlayMode = requestPlayMode;
        }

        /**
         * 返回
         * @param playMode
         */
        @Override
        public void onBackPress(int playMode) {
            // 当前是全屏模式，返回切换成窗口模式
            if (playMode == SuperPlayerConst.PLAYMODE_FULLSCREEN) {
                onRequestPlayMode(SuperPlayerConst.PLAYMODE_WINDOW);
            }
            // 当前是窗口模式，返回退出播放器
            else if (playMode == SuperPlayerConst.PLAYMODE_WINDOW) {
                if (mPlayerViewCallback != null) {
                    mPlayerViewCallback.onQuit(SuperPlayerConst.PLAYMODE_WINDOW);
                }
                if (mCurrentPlayState == SuperPlayerConst.PLAYSTATE_PLAY) {
                    onRequestPlayMode(SuperPlayerConst.PLAYMODE_FLOAT);
                }
            }
            // 当前是悬浮窗，退出
            else if (playMode == SuperPlayerConst.PLAYMODE_FLOAT) {
                mWindowManager.removeView(mVodControllerFloat);
                if (mPlayerViewCallback != null) {
                    mPlayerViewCallback.onQuit(SuperPlayerConst.PLAYMODE_FLOAT);
                }
            }
        }

        @Override
        public void resume() {
            if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
                if (mVodPlayer != null) {
                    mVodPlayer.resume();
                }
            } else {
                if (mLivePlayer != null) {
                    mLivePlayer.resume();
                }
            }
            mCurrentPlayState = SuperPlayerConst.PLAYSTATE_PLAY;
            mVodControllerSmall.updatePlayState(true);
            mVodControllerLarge.updatePlayState(true);

            mVodControllerLarge.updateReplay(false);
            mVodControllerSmall.updateReplay(false);
        }

        @Override
        public void pause() {
            if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
                if (mVodPlayer != null) {
                    mVodPlayer.pause();
                }
            } else {
                if (mLivePlayer != null) {
                    mLivePlayer.pause();
                }
                if (mWatcher != null) {
                    mWatcher.stop();
                }
            }
            mCurrentPlayState = SuperPlayerConst.PLAYSTATE_PAUSE;
            TXCLog.e("lyj", "pause mCurrentPlayState:" + mCurrentPlayState);
            mVodControllerSmall.updatePlayState(false);
            mVodControllerLarge.updatePlayState(false);
        }

        @Override
        public float getDuration() {
            return mVodPlayer.getDuration();
        }

        @Override
        public float getCurrentPlaybackTime() {
            return mVodPlayer.getCurrentPlaybackTime();
        }

        @Override
        public void seekTo(int position) {
            if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
                if (mVodPlayer != null) {
                    mVodPlayer.seek(position);
                }
            } else {
                mCurrentPlayType = SuperPlayerConst.PLAYTYPE_LIVE_SHIFT;
                mVodControllerSmall.updatePlayType(SuperPlayerConst.PLAYTYPE_LIVE_SHIFT);
                mVodControllerLarge.updatePlayType(SuperPlayerConst.PLAYTYPE_LIVE_SHIFT);
                LogReport.getInstance().uploadLogs(TCPlayerConstants.ELK_ACTION_TIMESHIFT, 0, 0);
                if (mLivePlayer != null) {
                    mLivePlayer.seek(position);
                }
                if (mWatcher != null) {
                    mWatcher.stop();
                }
            }

        }

        @Override
        public boolean isPlaying() {
            if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
                return mVodPlayer.isPlaying();
            } else {
                return mCurrentPlayState == SuperPlayerConst.PLAYSTATE_PLAY;
            }
        }

        /**
         * 切换弹幕开关
         * @param on
         */
        @Override
        public void onDanmuku(boolean on) {
            if (mDanmuView != null) {
                mDanmuView.toggle(on);
            }
        }

        /**
         * 截屏
         */
        @Override
        public void onSnapshot() {
            if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
                if (mVodPlayer != null) {
                    mVodPlayer.snapshot(new TXLivePlayer.ITXSnapshotListener() {
                        @Override
                        public void onSnapshot(Bitmap bmp) {
                            showSnapshotWindow(bmp);
                        }
                    });
                }
            } else {
                if (mLivePlayer != null) {
                    mLivePlayer.snapshot(new TXLivePlayer.ITXSnapshotListener() {
                        @Override
                        public void onSnapshot(Bitmap bmp) {
                            showSnapshotWindow(bmp);
                        }
                    });
                }
            }

        }

        /**
         * 清晰度选择
         * @param quality
         */
        @Override
        public void onQualitySelect(TCVideoQulity quality) {
            mVodControllerLarge.updateVideoQulity(quality);
            if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
                if (mVodPlayer != null) {
                    if (quality.index == -1) {
                        // 说明是非多bitrate的m3u8子流，需要手动seek
                        float currentTime = mVodPlayer.getCurrentPlaybackTime();
                        mVodPlayer.stopPlay(true);
                        TXCLog.i(TAG, "onQualitySelect quality.url:" + quality.url);
                        mVodPlayer.setStartTime(currentTime);
                        mVodPlayer.startPlay(quality.url);
                    } else {
                        TXCLog.i(TAG, "setBitrateIndex quality.index:" + quality.index);
                        // 说明是多bitrate的m3u8子流，会自动无缝seek
                        mVodPlayer.setBitrateIndex(quality.index);
                    }
                }
            } else {
                if (mLivePlayer != null && !TextUtils.isEmpty(quality.url)) {
                    int result = mLivePlayer.switchStream(quality.url);
                    if (result < 0) {
                        Toast.makeText(getContext(), "切换" + quality.title + "清晰度失败，请稍候重试", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "正在切换到" + quality.title + "...", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            //清晰度上报
            LogReport.getInstance().uploadLogs(TCPlayerConstants.ELK_ACTION_CHANGE_RESOLUTION, 0, 0);
        }

        /**
         * 速度改变
         * @param speedLevel
         */
        @Override
        public void onSpeedChange(float speedLevel) {
            if (mVodPlayer != null) {
                mVodPlayer.setRate(speedLevel);
            }

            //速度改变上报
            LogReport.getInstance().uploadLogs(TCPlayerConstants.ELK_ACTION_CHANGE_SPEED, 0, 0);
        }

        /**
         * 是否镜像
         * @param isMirror
         */
        @Override
        public void onMirrorChange(boolean isMirror) {
            if (mVodPlayer != null) {
                mVodPlayer.setMirror(isMirror);
            }

            if (isMirror) {
                //镜像上报
                LogReport.getInstance().uploadLogs(TCPlayerConstants.ELK_ACTION_MIRROR, 0, 0);
            }
        }

        /**
         * 是否启用硬件加速
         * @param isAccelerate
         */
        @Override
        public void onHWAcceleration(boolean isAccelerate) {
            if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
                mChangeHWAcceleration = true;
                if (mVodPlayer != null) {
                    mVodPlayer.enableHardwareDecode(isAccelerate);

                    mSeekPos = (int) mVodPlayer.getCurrentPlaybackTime();
                    TXCLog.i(TAG, "save pos:" + mSeekPos);

                    stopPlay();
                    playWithURL(mCurrentSuperPlayerModel);
                }
            } else {
                if (mLivePlayer != null) {
                    mLivePlayer.enableHardwareDecode(isAccelerate);

                    stopPlay();
                    playWithMode(mCurrentSuperPlayerModel);
                }
            }
            // 硬件加速上报
            if (isAccelerate) {
                LogReport.getInstance().uploadLogs(TCPlayerConstants.ELK_ACTION_HW_DECODE, 0, 0);
            } else {
                LogReport.getInstance().uploadLogs(TCPlayerConstants.ELK_ACTION_SOFT_DECODE, 0, 0);
            }
        }

        /**
         * 悬浮窗位置更新
         * @param x
         * @param y
         */
        @Override
        public void onFloatUpdate(int x, int y) {
            mWindowParams.x = x;
            mWindowParams.y = y;
            mWindowManager.updateViewLayout(mVodControllerFloat, mWindowParams);
        }

        /**
         * 重新播放
         */
        @Override
        public void onReplay() {
            if (!TextUtils.isEmpty(mCurrentSuperPlayerModel.videoURL)) {
                playWithMode(mCurrentSuperPlayerModel);
            }

            if (mVodControllerLarge != null) {
                mVodControllerLarge.updateReplay(false);
            }
            if (mVodControllerSmall != null) {
                mVodControllerSmall.updateReplay(false);
            }
        }

        @Override
        public void resumeLive() {
            if (mLivePlayer != null) {
                mLivePlayer.resumeLive();
            }
            mVodControllerSmall.updatePlayType(SuperPlayerConst.PLAYTYPE_LIVE);
            mVodControllerLarge.updatePlayType(SuperPlayerConst.PLAYTYPE_LIVE);
        }

    };


    /**
     * 显示截图窗口
     *
     * @param bmp
     */
    private void showSnapshotWindow(final Bitmap bmp) {
        final PopupWindow popupWindow = new PopupWindow(mContext);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_new_vod_snap, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.iv_snap);
        imageView.setImageBitmap(bmp);
        popupWindow.setContentView(view);
        popupWindow.setOutsideTouchable(true);
//        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(mRootView, Gravity.TOP, 1800, 300);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                save2MediaStore(bmp);
            }
        });

        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                popupWindow.dismiss();
            }
        }, 3000);
    }

    private void save2MediaStore(Bitmap image) {

        try {
            File appDir = new File(Environment.getExternalStorageDirectory(), "superplayer");
            if (!appDir.exists()) {
                appDir.mkdir();
            }

            long dateSeconds = System.currentTimeMillis() / 1000;
            String fileName = System.currentTimeMillis() + ".jpg";
            File file = new File(appDir, fileName);

            String filePath = file.getAbsolutePath();
            // Save the screenshot to the MediaStore
            ContentValues values = new ContentValues();
            ContentResolver resolver = mContext.getContentResolver();
            values.put(MediaStore.Images.ImageColumns.DATA, filePath);
            values.put(MediaStore.Images.ImageColumns.TITLE, fileName);
            values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.ImageColumns.DATE_ADDED, dateSeconds);
            values.put(MediaStore.Images.ImageColumns.DATE_MODIFIED, dateSeconds);
            values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.ImageColumns.WIDTH, image.getWidth());
            values.put(MediaStore.Images.ImageColumns.HEIGHT, image.getHeight());
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            OutputStream out = resolver.openOutputStream(uri);
            image.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            // update file size in the database
            values.clear();
            values.put(MediaStore.Images.ImageColumns.SIZE, new File(filePath).length());
            resolver.update(uri, values, null, null);

        } catch (Exception e) {

        }
    }

    /**
     * 旋转屏幕方向
     *
     * @param orientation
     */
    private void rotateScreenOrientation(int orientation) {
        switch (orientation) {
            case SuperPlayerConst.ORIENTATION_LANDSCAPE:
                ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case SuperPlayerConst.ORIENTATION_PORTRAIT:
                ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
        }
    }

    /**
     * 点播播放器回调
     *
     * @param player
     * @param event  事件id.id类型请参考 {@linkplain TXLiveConstants#PLAY_EVT_CONNECT_SUCC 播放事件列表}.
     * @param param
     */
    @Override
    public void onPlayEvent(TXVodPlayer player, int event, Bundle param) {
        if (event != TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
            String playEventLog = "TXVodPlayer onPlayEvent event: " + event + ", " + param.getString(TXLiveConstants.EVT_DESCRIPTION);
            TXCLog.d(TAG, playEventLog);
        }

        if (event == TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED) { //视频播放开始
            mVodControllerSmall.updateLiveLoadingState(false);
            mVodControllerLarge.updateLiveLoadingState(false);

            mVodControllerSmall.updatePlayState(true);
            mVodControllerLarge.updatePlayState(true);

            mVodControllerSmall.updateReplay(false);
            mVodControllerLarge.updateReplay(false);

            if (mIsMultiBitrateStream) {
                ArrayList<TXBitrateItem> bitrateItems = mVodPlayer.getSupportedBitrates();

                if (bitrateItems == null || bitrateItems.size() == 0)
                    return;
                Collections.sort(bitrateItems); //masterPlaylist多清晰度，按照码率排序，从低到高

                ArrayList<TCVideoQulity> videoQulities = new ArrayList<>();
                int size = bitrateItems.size();
                for (int i = 0; i < size; i++) {
                    TXBitrateItem bitrateItem = bitrateItems.get(i);
                    TCVideoQulity quality = SuperPlayerUtil.convertToVideoQuality(bitrateItem, i);
                    videoQulities.add(quality);
                }

                if (!mDefaultSet) {
                    TXBitrateItem defaultitem = bitrateItems.get(bitrateItems.size() - 1);
                    mVodPlayer.setBitrateIndex(defaultitem.index); //默认播放码率最高的
                    // 180x320 流畅, 360x640 标清, 720x1280 高清
                    TXBitrateItem bitrateItem = bitrateItems.get(bitrateItems.size() - 1);
                    TCVideoQulity defaultVideoQuality = SuperPlayerUtil.convertToVideoQuality(bitrateItem, bitrateItems.size() - 1);
                    mVodControllerLarge.updateVideoQulity(defaultVideoQuality);
                    mDefaultSet = true;
                }
                mVodControllerLarge.setVideoQualityList(videoQulities);
            }
        } else if (event == TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME) {
            if (mChangeHWAcceleration) { //切换软硬解码器后，重新seek位置
                TXCLog.i(TAG, "seek pos:" + mSeekPos);
                mVodController.seekTo(mSeekPos);
                mChangeHWAcceleration = false;
            }
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_END) {
            mCurrentPlayState = SuperPlayerConst.PLAYSTATE_PAUSE;
            mVodControllerSmall.updatePlayState(false);
            mVodControllerLarge.updatePlayState(false);

            mVodControllerSmall.updateReplay(true);
            mVodControllerLarge.updateReplay(true);
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
            int progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS_MS);
            int duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION_MS);
            mVodControllerSmall.updateVideoProgress(progress / 1000, duration / 1000);
            mVodControllerLarge.updateVideoProgress(progress / 1000, duration / 1000);
        }
        if (event < 0) {
            mVodPlayer.stopPlay(true);
            mVodControllerSmall.updatePlayState(false);
            mVodControllerLarge.updatePlayState(false);
            Toast.makeText(mContext, param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNetStatus(TXVodPlayer player, Bundle status) {

    }

    /**
     * 直播播放器回调
     *
     * @param event 事件id.id类型请参考 {@linkplain TXLiveConstants#PUSH_EVT_CONNECT_SUCC 播放事件列表}.
     * @param param
     */
    @Override
    public void onPlayEvent(int event, Bundle param) {
        if (event != TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
            String playEventLog = "TXLivePlayer onPlayEvent event: " + event + ", " + param.getString(TXLiveConstants.EVT_DESCRIPTION);
            TXCLog.d(TAG, playEventLog);
        }
        if (event == TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED) { //视频播放开始
            mVodControllerSmall.updateLiveLoadingState(false);
            mVodControllerLarge.updateLiveLoadingState(false);

            mVodControllerSmall.updatePlayState(true);
            mVodControllerLarge.updatePlayState(true);

            mVodControllerSmall.updateReplay(false);
            mVodControllerLarge.updateReplay(false);
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
            mVodControllerSmall.updateLiveLoadingState(false);
            mVodControllerLarge.updateLiveLoadingState(false);

            mVodControllerSmall.updatePlayState(true);
            mVodControllerLarge.updatePlayState(true);

            mVodControllerSmall.updateReplay(false);
            mVodControllerLarge.updateReplay(false);
            if (mWatcher != null) mWatcher.exitLoading();
        } else if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT || event == TXLiveConstants.PLAY_EVT_PLAY_END) {
            if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_LIVE_SHIFT) {  // 直播时移失败，返回直播
                mVodController.resumeLive();
                Toast.makeText(mContext, "时移失败,返回直播", Toast.LENGTH_SHORT).show();
                mVodControllerSmall.updateReplay(false);
                mVodControllerLarge.updateReplay(false);
                mVodControllerSmall.updateLiveLoadingState(false);
                mVodControllerLarge.updateLiveLoadingState(false);
            } else {
                stopPlay();
                mVodControllerSmall.updatePlayState(false);
                mVodControllerLarge.updatePlayState(false);
                mVodControllerSmall.updateReplay(true);
                mVodControllerLarge.updateReplay(true);
                if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT) {
                    Toast.makeText(mContext, "网络不给力,点击重试", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
                }
            }
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_LOADING) {
            mVodControllerSmall.updateLiveLoadingState(true);
            mVodControllerLarge.updateLiveLoadingState(true);
            if (mWatcher != null) mWatcher.enterLoading();
        } else if (event == TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME) {
        } else if (event == TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION) {
        } else if (event == TXLiveConstants.PLAY_EVT_CHANGE_ROTATION) {
            return;
        } else if (event == TXLiveConstants.PLAY_EVT_STREAM_SWITCH_SUCC) {
            Toast.makeText(mContext, "清晰度切换成功", Toast.LENGTH_SHORT).show();
            return;
        } else if (event == TXLiveConstants.PLAY_ERR_STREAM_SWITCH_FAIL) {
            Toast.makeText(mContext, "清晰度切换失败", Toast.LENGTH_SHORT).show();
            return;
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
            int progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS_MS);
            int duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION_MS);
            mVodControllerSmall.updateVideoProgress(progress / 1000, duration / 1000);
            mVodControllerLarge.updateVideoProgress(progress / 1000, duration / 1000);
        }
//        if (event < 0) {
//            mVodControllerSmall.updateReplay(false);
//            mVodControllerLarge.updateReplay(false);
//            if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_LIVE_SHIFT) {  // 直播时移失败，返回直播
//                mVodController.resumeLive();
//                Toast.makeText(mContext, "时移失败,返回直播", Toast.LENGTH_SHORT).show();
//            } else {
//                mLivePlayer.stopPlay(true);
//                mVodControllerSmall.updatePlayState(false);
//                mVodControllerLarge.updatePlayState(false);
//                if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT) {
//                    Toast.makeText(mContext, "网络不给力,点击重试", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(mContext, param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
    }

    @Override
    public void onNetStatus(Bundle status) {

    }

    public void requestPlayMode(int playMode) {
        if (playMode == SuperPlayerConst.PLAYMODE_WINDOW) {
            if (mVodController != null) {
                mVodController.onRequestPlayMode(SuperPlayerConst.PLAYMODE_WINDOW);
            }
        } else if (playMode == SuperPlayerConst.PLAYMODE_FLOAT) {
            if (mPlayerViewCallback != null) {
                mPlayerViewCallback.onQuit(SuperPlayerConst.PLAYMODE_WINDOW);
            }
            if (mVodController != null) {
                mVodController.onRequestPlayMode(SuperPlayerConst.PLAYMODE_FLOAT);
            }
        }
    }


    private final int OP_SYSTEM_ALERT_WINDOW = 24;

    /**
     * API <18，默认有悬浮窗权限，不需要处理。无法接收无法接收触摸和按键事件，不需要权限和无法接受触摸事件的源码分析
     * API >= 19 ，可以接收触摸和按键事件
     * API >=23，需要在manifest中申请权限，并在每次需要用到权限的时候检查是否已有该权限，因为用户随时可以取消掉。
     * API >25，TYPE_TOAST 已经被谷歌制裁了，会出现自动消失的情况
     */
    private boolean checkOp(Context context, int op) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Method method = AppOpsManager.class.getDeclaredMethod("checkOp", int.class, int.class, String.class);
                return AppOpsManager.MODE_ALLOWED == (int) method.invoke(manager, op, Binder.getCallingUid(), context.getPackageName());
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        return true;
    }

    /**
     * 获取当前播放模式
     *
     * @return
     */
    public int getPlayMode() {
        return mPlayMode;
    }

    /**
     * 获取当前播放状态
     *
     * @return
     */
    public int getPlayState() {
        return mCurrentPlayState;
    }

    /**
     * SuperVodPlayerActivity的回调接口
     */
    public interface PlayerViewCallback {

        void hideViews();

        void showViews();

        void onQuit(int playMode);
    }

    public void release() {
        if (mVodControllerSmall != null) {
            mVodControllerSmall.release();
        }
        if (mVodControllerLarge != null) {
            mVodControllerLarge.release();
        }
        if (mVodControllerFloat != null) {
            mVodControllerFloat.release();
        }
        // 释放网络请求资源
        TCHttpURLClient.getInstance().release();
    }
}
