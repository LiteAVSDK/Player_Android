package com.tencent.liteav.demo.superplayer;

import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_AUTO_PLAY;
import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_MANUAL_PLAY;
import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_PRELOAD;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cloud.tencent.liteav.demo.comon.TUIBuild;
import com.tencent.liteav.demo.common.utils.IntentUtils;
import com.tencent.liteav.demo.superplayer.model.ISuperPlayerListener;
import com.tencent.liteav.demo.superplayer.model.SuperPlayer;
import com.tencent.liteav.demo.superplayer.model.SuperPlayerImpl;
import com.tencent.liteav.demo.superplayer.model.SuperPlayerObserver;
import com.tencent.liteav.demo.superplayer.model.VipWatchModel;
import com.tencent.liteav.demo.superplayer.model.entity.DynamicWaterConfig;
import com.tencent.liteav.demo.superplayer.model.entity.PlayImageSpriteInfo;
import com.tencent.liteav.demo.superplayer.model.entity.PlayKeyFrameDescInfo;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;
import com.tencent.liteav.demo.superplayer.model.net.LogReport;
import com.tencent.liteav.demo.superplayer.model.utils.NetWatcher;
import com.tencent.liteav.demo.superplayer.ui.player.FloatPlayer;
import com.tencent.liteav.demo.superplayer.ui.player.FullScreenPlayer;
import com.tencent.liteav.demo.superplayer.ui.player.Player;
import com.tencent.liteav.demo.superplayer.ui.player.WindowPlayer;
import com.tencent.liteav.demo.superplayer.ui.view.DanmuView;
import com.tencent.liteav.demo.superplayer.ui.view.DynamicWatermarkView;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 超级播放器view
 * <p>
 * 具备播放器基本功能，此外还包括横竖屏切换、悬浮窗播放、画质切换、硬件加速、倍速播放、镜像播放、手势控制等功能，同时支持直播与点播
 * 使用方式极为简单，只需要在布局文件中引入并获取到该控件，通过{@link #playWithModel(SuperPlayerModel)}传入{@link SuperPlayerModel}即可实现视频播放
 * <p>
 * 1、播放视频{@link #playWithModel(SuperPlayerModel)}
 * 2、设置回调{@link #setPlayerViewCallback(OnSuperPlayerViewCallback)}
 * 3、controller回调实现{@link #mControllerCallback}
 * 4、退出播放释放内存{@link #resetPlayer()}
 */
public class SuperPlayerView extends RelativeLayout {
    private static final String TAG                    = "SuperPlayerView";
    private final        int    OP_SYSTEM_ALERT_WINDOW = 24;                      // 支持TYPE_TOAST悬浮窗的最高API版本

    private Context                    mContext;
    private ViewGroup                  mRootView;                                 // SuperPlayerView的根view
    private TXCloudVideoView           mTXCloudVideoView;                         // 腾讯云视频播放view
    private FullScreenPlayer           mFullScreenPlayer;                         // 全屏模式控制view
    private WindowPlayer               mWindowPlayer;                             // 窗口模式控制view
    private FloatPlayer                mFloatPlayer;                              // 悬浮窗模式控制view
    private DanmuView                  mDanmuView;                                // 弹幕
    private ViewGroup.LayoutParams     mLayoutParamWindowMode;          // 窗口播放时SuperPlayerView的布局参数
    private ViewGroup.LayoutParams     mLayoutParamFullScreenMode;      // 全屏播放时SuperPlayerView的布局参数
    private LayoutParams               mVodControllerWindowParams;      // 窗口controller的布局参数
    private LayoutParams               mVodControllerFullScreenParams;  // 全屏controller的布局参数
    private WindowManager              mWindowManager;                  // 悬浮窗窗口管理器
    private WindowManager.LayoutParams mWindowParams;                   // 悬浮窗布局参数
    private OnSuperPlayerViewCallback  mPlayerViewCallback;             // SuperPlayerView回调
    private NetWatcher                 mWatcher;                        // 网络质量监视器
    private SuperPlayer                mSuperPlayer;                    // 超级播放器
    private SuperPlayerModel           mCurrentSuperPlayerModel;        // 当前正在播放的SuperPlayerModel
    private int                        mPlayAction;                     // 播放模式
    private int                        mPlayIndex;                      // 正在播放model的索引
    private boolean                    mIsLoopPlayList;                 // 是否循环
    private List<SuperPlayerModel>     mSuperPlayerModelList;           // SuperPlayerModel列表
    private long                       mDuration;                       // 时长
    private long                       mProgress;                       // 进度
    private boolean                    mIsPlayInit;                     // 防止mSuperPlayer.stop()继续调用playNextVideo的变量
    private boolean                    isCallResume = false;            //resume方法时候被调用，在预加载模式使用
    private LinearLayout               mDynamicWatermarkLayout;
    private DynamicWatermarkView       mDynamicWatermarkView;
    private ISuperPlayerListener mSuperPlayerListener;

    public SuperPlayerView(Context context) {
        super(context);
        initialize(context);
    }

    public SuperPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public SuperPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        mContext = context;
        initView();
        initPlayer();
    }

    /**
     * 初始化view
     */
    private void initView() {
        mRootView = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.superplayer_vod_view, null);
        mTXCloudVideoView = (TXCloudVideoView) mRootView.findViewById(R.id.superplayer_cloud_video_view);
        mFullScreenPlayer = (FullScreenPlayer) mRootView.findViewById(R.id.superplayer_controller_large);
        mWindowPlayer = (WindowPlayer) mRootView.findViewById(R.id.superplayer_controller_small);
        mFloatPlayer = (FloatPlayer) mRootView.findViewById(R.id.superplayer_controller_float);
        mDanmuView = (DanmuView) mRootView.findViewById(R.id.superplayer_danmuku_view);
        //防止stop中空指针异常
        mSuperPlayerModelList = new ArrayList<>();
        mDynamicWatermarkLayout = mRootView.findViewById(R.id.superplayer_dynamic_watermark_layout);
        mDynamicWatermarkView = mRootView.findViewById(R.id.superplayer_dynamic_watermark);


        mVodControllerWindowParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mVodControllerFullScreenParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        mFullScreenPlayer.setCallback(mControllerCallback);
        mWindowPlayer.setCallback(mControllerCallback);
        mFloatPlayer.setCallback(mControllerCallback);

        removeAllViews();
        mRootView.removeView(mDanmuView);
        mRootView.removeView(mTXCloudVideoView);
        mRootView.removeView(mWindowPlayer);
        mRootView.removeView(mFullScreenPlayer);
        mRootView.removeView(mFloatPlayer);
        mRootView.removeView(mDynamicWatermarkLayout);

        addView(mTXCloudVideoView);
        addView(mDynamicWatermarkLayout);
        addView(mDanmuView);
    }

    private void initPlayer() {
        mSuperPlayer = new SuperPlayerImpl(mContext, mTXCloudVideoView);
        mSuperPlayer.setObserver(new PlayerObserver());

        if (mSuperPlayer.getPlayerMode() == SuperPlayerDef.PlayerMode.FULLSCREEN) {
            addView(mFullScreenPlayer);
            mFullScreenPlayer.hide();
        } else if (mSuperPlayer.getPlayerMode() == SuperPlayerDef.PlayerMode.WINDOW) {
            addView(mWindowPlayer);
            mWindowPlayer.hide();
        }

        post(new Runnable() {
            @Override
            public void run() {
                if (mSuperPlayer.getPlayerMode() == SuperPlayerDef.PlayerMode.WINDOW) {
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
        LogReport.getInstance().setAppName(mContext);
        LogReport.getInstance().setPackageName(mContext);

        if (mWatcher == null) {
            mWatcher = new NetWatcher(mContext);
        }

    }

    /**
     * 播放视频列表
     *
     * @param models superPlayerModel列表
     * @param isLoopPlayList 是否循环
     * @param index 开始播放的视频索引
     */
    public void playWithModelList(List<SuperPlayerModel> models, boolean isLoopPlayList, int index) {
        mSuperPlayerModelList = models;
        mIsLoopPlayList = isLoopPlayList;
        playModelInList(index);
    }

    private void playModelInList(int index) {
        mIsPlayInit = false;
        mSuperPlayer.stop();
        mPlayIndex = index;
        if (mSuperPlayerModelList.size() > 1) {
            mWindowPlayer.setPlayNextButtonVisibility(true);
            mFullScreenPlayer.setPlayNextButtonVisibility(true);
        } else if (mSuperPlayerModelList.size() == 1) {
            mWindowPlayer.setPlayNextButtonVisibility(false);
            mFullScreenPlayer.setPlayNextButtonVisibility(false);
        }
        mCurrentSuperPlayerModel = mSuperPlayerModelList.get(mPlayIndex);
        playWithModelInner(mCurrentSuperPlayerModel);
        mIsPlayInit = true;
    }

    /**
     * 播放视频
     *
     * @param model
     */
    public void playWithModel(SuperPlayerModel model) {
        isCallResume = false;
        mIsPlayInit = false;
        mSuperPlayer.stop();
        mIsLoopPlayList = false;
        mWindowPlayer.setPlayNextButtonVisibility(false);
        mFullScreenPlayer.setPlayNextButtonVisibility(false);
        //防止点击循环列表后再次回到其他列表后依然循环
        mSuperPlayerModelList.clear();
        mCurrentSuperPlayerModel = model;
        playWithModelInner(mCurrentSuperPlayerModel);
        mIsPlayInit = true;
    }

    private void playWithModelInner(SuperPlayerModel model) {
        mPlayAction = mCurrentSuperPlayerModel.playAction;
        if (mPlayAction == PLAY_ACTION_AUTO_PLAY || mPlayAction == PLAY_ACTION_PRELOAD) {
            mSuperPlayer.play(model);
        } else {
            mSuperPlayer.reset();
        }
        mFullScreenPlayer.preparePlayVideo(model);
        mWindowPlayer.preparePlayVideo(model);

        mFullScreenPlayer.setVipWatchModel(model.vipWatchMode);
        mWindowPlayer.setVipWatchModel(model.vipWatchMode);
        mFloatPlayer.setVipWatchModel(model.vipWatchMode);
        //设置动态水印的数据
        mDynamicWatermarkView.setData(model.dynamicWaterConfig);
        mDynamicWatermarkView.hide();
    }

    /**
     * 设置VipWatchModel 数据，传入null可隐藏掉展示的VIP页面
     *
     * @param vipWatchModel
     */
    public void setVipWatchModel(VipWatchModel vipWatchModel) {
        mFullScreenPlayer.setVipWatchModel(vipWatchModel);
        mWindowPlayer.setVipWatchModel(vipWatchModel);
        mFloatPlayer.setVipWatchModel(vipWatchModel);
    }

    /**
     * 设置动态水印的配置信息
     * @param dynamicWaterConfig
     */
    public void setDynamicWatermarkConfig(DynamicWaterConfig dynamicWaterConfig) {
        mDynamicWatermarkView.setData(dynamicWaterConfig);
        mDynamicWatermarkView.hide();
    }

    /**
     * 更新标题
     *
     * @param title 视频名称
     */
    private void updateTitle(String title) {
        mWindowPlayer.updateTitle(title);
        mFullScreenPlayer.updateTitle(title);
    }

    /**
     * 用于判断VIP试看页面是否已经展示出来了
     *
     * @return
     */
    public boolean isShowingVipView() {
        return mFullScreenPlayer.isShowingVipView() || mWindowPlayer.isShowingVipView() || mFloatPlayer.isShowingVipView();
    }


    /**
     * resume生命周期回调
     */
    public void onResume() {
        if (mDanmuView != null && mDanmuView.isPrepared() && mDanmuView.isPaused()) {
            mDanmuView.resume();
        }
        if (mPlayAction == PLAY_ACTION_MANUAL_PLAY && mSuperPlayer.getPlayerState() == SuperPlayerDef.PlayerState.INIT) {
            return;
        }
        mSuperPlayer.resume();
        isCallResume = true;
    }

    /**
     * pause生命周期回调
     */
    public void onPause() {
        if (mDanmuView != null && mDanmuView.isPrepared()) {
            mDanmuView.pause();
        }
        if (mPlayAction == PLAY_ACTION_MANUAL_PLAY && mSuperPlayer.getPlayerState() == SuperPlayerDef.PlayerState.INIT) {
            return;
        }
        mSuperPlayer.pauseVod();
    }

    /**
     * 重置播放器
     */
    public void resetPlayer() {
        mSuperPlayerModelList.clear();
        if (mDanmuView != null) {
            mDanmuView.release();
            mDanmuView = null;
        }
        stopPlay();
    }

    /**
     * 在feed 流需求中使用
     * 将弹幕 暂停
     */
    public void revertUI() {
        if (mDanmuView != null) {
            mDanmuView.toggle(false);
            mDanmuView.removeAllDanmakus(true);
        }
        mSuperPlayer.revertSettings();
        mFullScreenPlayer.revertUI();
        if (mDynamicWatermarkView != null) {
            mDynamicWatermarkView.hide();
        }
    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        mSuperPlayer.stop();
        if (mWatcher != null) {
            mWatcher.stop();
        }
    }

    /**
     * 设置超级播放器的回掉
     *
     * @param callback
     */
    public void setPlayerViewCallback(OnSuperPlayerViewCallback callback) {
        mPlayerViewCallback = callback;
    }

    /**
     * 设置超级播放器中点播播放器和直播播放器的回调
     *
     * @param superPlayerListener
     */
    public void setSuperPlayerListener(ISuperPlayerListener superPlayerListener) {
        mSuperPlayerListener = superPlayerListener;
        if (mSuperPlayer != null) {
            mSuperPlayer.setSuperPlayerListener(mSuperPlayerListener);
        }
    }

    /**
     * 控制是否全屏显示
     */
    private void fullScreen(boolean isFull) {
        if (getContext() instanceof Activity) {
            Activity activity = (Activity) getContext();
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
                    ((Activity) getContext()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
     * 隐藏或展示窗口模式下的返回按钮，默认是展示的
     *
     * @param isShow
     */
    public void showOrHideBackBtn(boolean isShow) {
        if (mWindowPlayer != null) {
            mWindowPlayer.showOrHideBackBtn(isShow);
        }
    }


    private void onSwitchFullMode(SuperPlayerDef.PlayerMode playerMode) {
        if (mLayoutParamFullScreenMode == null) {
            return;
        }
        removeView(mWindowPlayer);
        addView(mFullScreenPlayer, mVodControllerFullScreenParams);
        setLayoutParams(mLayoutParamFullScreenMode);
        if (mPlayerViewCallback != null) {
            mPlayerViewCallback.onStartFullScreenPlay();
        }
        rotateScreenOrientation(SuperPlayerDef.Orientation.LANDSCAPE);
        mSuperPlayer.switchPlayMode(playerMode);
    }

    private void onSwitchWindowMode(SuperPlayerDef.PlayerMode playerMode) {
        // 当前是悬浮窗
        if (mSuperPlayer.getPlayerMode() == SuperPlayerDef.PlayerMode.FLOAT) {
            try {
                Context viewContext = getContext();
                Intent intent = null;
                if (viewContext instanceof Activity) {
                    intent = new Intent(viewContext, viewContext.getClass());
                } else {
                    showToast(R.string.superplayer_float_play_fail);
                    return;
                }
                IntentUtils.safeStartActivity(mContext, intent);
                mSuperPlayer.pause();
                if (mLayoutParamWindowMode == null) {
                    return;
                }
                mFloatPlayer.removeDynamicWatermarkView();
                mDynamicWatermarkLayout.addView(mDynamicWatermarkView);
                mWindowManager.removeView(mFloatPlayer);
                mSuperPlayer.setPlayerView(mTXCloudVideoView);
                if (!isShowingVipView()) {    //当展示了试看功能的时候，不进行resume操作
                    mSuperPlayer.resume();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (mSuperPlayer.getPlayerMode() == SuperPlayerDef.PlayerMode.FULLSCREEN) { // 当前是全屏模式
            if (mLayoutParamWindowMode == null) {
                return;
            }
            WindowManager.LayoutParams attrs =  ((Activity) getContext()).getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            ((Activity) getContext()).getWindow().setAttributes(attrs);
            ((Activity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            removeView(mFullScreenPlayer);
            addView(mWindowPlayer, mVodControllerWindowParams);
            setLayoutParams(mLayoutParamWindowMode);
            rotateScreenOrientation(SuperPlayerDef.Orientation.PORTRAIT);
            if (mPlayerViewCallback != null) {
                mPlayerViewCallback.onStopFullScreenPlay();
            }
        }
        mSuperPlayer.switchPlayMode(playerMode);
    }

    private void onSwitchFloatMode(SuperPlayerDef.PlayerMode playerMode) {
        Log.i(TAG, "requestPlayMode Float :" + TUIBuild.getManufacturer());
        SuperPlayerGlobalConfig prefs = SuperPlayerGlobalConfig.getInstance();
        if (!prefs.enableFloatWindow) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 6.0动态申请悬浮窗权限
            if (!Settings.canDrawOverlays(mContext)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + mContext.getPackageName()));
                IntentUtils.safeStartActivity(mContext, intent);
                return;
            }
        } else {
            if (!checkOp(mContext, OP_SYSTEM_ALERT_WINDOW)) {
                showToast(R.string.superplayer_enter_setting_fail);
                return;
            }
        }
        mSuperPlayer.pause();

        mWindowManager = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        mWindowParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.gravity = Gravity.LEFT | Gravity.TOP;

        SuperPlayerGlobalConfig.TXRect rect = prefs.floatViewRect;
        mWindowParams.x = rect.x;
        mWindowParams.y = rect.y;
        mWindowParams.width = rect.width;
        mWindowParams.height = rect.height;
        try {
            mWindowManager.addView(mFloatPlayer, mWindowParams);
        } catch (Exception e) {
            showToast(R.string.superplayer_float_play_fail);
            return;
        }
        mDynamicWatermarkLayout.removeAllViews();
        mFloatPlayer.addDynamicWatermarkView(mDynamicWatermarkView);
        TXCloudVideoView videoView = mFloatPlayer.getFloatVideoView();
        if (videoView != null) {
            mSuperPlayer.setPlayerView(videoView);
            mSuperPlayer.resume();
        }
        // 悬浮窗上报
        LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_FLOATMOE, 0, 0);
        mSuperPlayer.switchPlayMode(playerMode);
    }

    private void handleSwitchPlayMode(SuperPlayerDef.PlayerMode playerMode) {
        fullScreen(playerMode == SuperPlayerDef.PlayerMode.FULLSCREEN);
        mFullScreenPlayer.hide();
        mWindowPlayer.hide();
        mFloatPlayer.hide();
        //请求全屏模式
        if (playerMode == SuperPlayerDef.PlayerMode.FULLSCREEN) {
            onSwitchFullMode(playerMode);
        } else if (playerMode == SuperPlayerDef.PlayerMode.WINDOW) { // 请求窗口模式
            onSwitchWindowMode(playerMode);
        } else if (playerMode == SuperPlayerDef.PlayerMode.FLOAT) { // 请求悬浮窗模式
            onSwitchFloatMode(playerMode);
        }
    }

    /**
     * 初始化controller回调
     */
    private Player.Callback mControllerCallback = new Player.Callback() {
        @Override
        public void onSwitchPlayMode(SuperPlayerDef.PlayerMode playerMode) {
            handleSwitchPlayMode(playerMode);
        }

        @Override
        public void onBackPressed(SuperPlayerDef.PlayerMode playMode) {
            switch (playMode) {
                case FULLSCREEN:// 当前是全屏模式，返回切换成窗口模式
                    onSwitchPlayMode(SuperPlayerDef.PlayerMode.WINDOW);
                    break;
                case WINDOW:// 当前是窗口模式，返回退出播放器
                    if (mPlayerViewCallback != null) {
                        mPlayerViewCallback.onClickSmallReturnBtn();
                    }
                    break;
                case FLOAT:// 当前是悬浮窗，退出
                    mWindowManager.removeView(mFloatPlayer);
                    if (mPlayerViewCallback != null) {
                        mPlayerViewCallback.onClickFloatCloseBtn();
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onFloatPositionChange(int x, int y) {
            mWindowParams.x = x;
            mWindowParams.y = y;
            mWindowManager.updateViewLayout(mFloatPlayer, mWindowParams);
        }

        @Override
        public void onPause() {
            mSuperPlayer.pause();
            if (mSuperPlayer.getPlayerType() != SuperPlayerDef.PlayerType.VOD) {
                if (mWatcher != null) {
                    mWatcher.stop();
                }
            }
        }

        @Override
        public void onResume() {
            if (mSuperPlayer.getPlayerState() == SuperPlayerDef.PlayerState.LOADING
                    && mPlayAction == PLAY_ACTION_PRELOAD) {
                mSuperPlayer.resume();
            } else if (mSuperPlayer.getPlayerState() == SuperPlayerDef.PlayerState.INIT) {
                if (mPlayAction == PLAY_ACTION_PRELOAD) {
                    mSuperPlayer.resume();
                } else if (mPlayAction == PLAY_ACTION_MANUAL_PLAY) {
                    mSuperPlayer.play(mCurrentSuperPlayerModel);
                }
            } else if (mSuperPlayer.getPlayerState() == SuperPlayerDef.PlayerState.END) { //重播
                mSuperPlayer.reStart();
            } else if (mSuperPlayer.getPlayerState() == SuperPlayerDef.PlayerState.PAUSE) { //继续播放
                mSuperPlayer.resume();
            }
        }


        @Override
        public void onSeekTo(int position) {
            mSuperPlayer.seek(position);
        }

        @Override
        public void onResumeLive() {
            mSuperPlayer.resumeLive();
        }

        @Override
        public void onDanmuToggle(boolean isOpen) {
            if (mDanmuView != null) {
                mDanmuView.toggle(isOpen);
            }
        }

        @Override
        public void onSnapshot() {
            mSuperPlayer.snapshot(new TXLivePlayer.ITXSnapshotListener() {
                @Override
                public void onSnapshot(Bitmap bitmap) {
                    if (bitmap != null) {
                        showSnapshotWindow(bitmap);
                    } else {
                        showToast(R.string.superplayer_screenshot_fail);
                    }
                }
            });
        }

        @Override
        public void onQualityChange(VideoQuality quality) {
            mFullScreenPlayer.updateVideoQuality(quality);
            mSuperPlayer.switchStream(quality);
        }

        @Override
        public void onSpeedChange(float speedLevel) {
            mSuperPlayer.setRate(speedLevel);
        }

        @Override
        public void onMirrorToggle(boolean isMirror) {
            mSuperPlayer.setMirror(isMirror);
        }

        @Override
        public void onHWAccelerationToggle(boolean isAccelerate) {
            mSuperPlayer.enableHardwareDecode(isAccelerate);
        }

        @Override
        public void onClickHandleVip() {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://cloud.tencent.com/document/product/454/18872"));
            if (getContext() instanceof Activity) {
                IntentUtils.safeStartActivity(getContext(), intent);
            }
        }

        @Override
        public void onClickVipTitleBack(SuperPlayerDef.PlayerMode playerMode) {
            if (playerMode == SuperPlayerDef.PlayerMode.FULLSCREEN) {
                mFullScreenPlayer.hideVipView();
                return;
            }
            mFullScreenPlayer.hideVipView();
            mWindowPlayer.hideVipView();
            mFloatPlayer.hideVipView();
        }

        @Override
        public void onClickVipRetry() {
            mControllerCallback.onSeekTo(0);
            mControllerCallback.onResume();
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    //隐藏VIP View
                    mFullScreenPlayer.hideVipView();
                    mWindowPlayer.hideVipView();
                    mFloatPlayer.hideVipView();
                }
            }, 500);

        }

        @Override
        public void onCloseVipTip() {
            mFullScreenPlayer.hideTipView();
            mWindowPlayer.hideTipView();
            mFloatPlayer.hideTipView();
        }

        @Override
        public void playNext() {
            playNextVideo();
        }
    };

    private void playNextVideo() {
        if (!mIsLoopPlayList && (mPlayIndex == mSuperPlayerModelList.size() - 1)) {
            return;
        }
        mPlayIndex = (++mPlayIndex) % mSuperPlayerModelList.size();
        playModelInList(mPlayIndex);
    }

    /**
     * 显示截图窗口
     *
     * @param bmp
     */
    private void showSnapshotWindow(final Bitmap bmp) {
        final PopupWindow popupWindow = new PopupWindow(mContext);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        View view = LayoutInflater.from(mContext).inflate(R.layout.superplayer_layout_new_vod_snap, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.superplayer_iv_snap);
        imageView.setImageBitmap(bmp);
        popupWindow.setContentView(view);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAtLocation(mRootView, Gravity.TOP, 1800, 300);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                save2MediaStore(mContext, bmp);
            }
        });
        postDelayed(new Runnable() {
            @Override
            public void run() {
                popupWindow.dismiss();
            }
        }, 3000);
    }

    /**
     * 旋转屏幕方向
     *
     * @param orientation
     */
    private void rotateScreenOrientation(SuperPlayerDef.Orientation orientation) {
        switch (orientation) {
            case LANDSCAPE:
                ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case PORTRAIT:
                ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
        }
    }

    /**
     * 检查悬浮窗权限
     * <p>
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

    public void setNeedToPause(boolean value) {
        mSuperPlayer.setNeedToPause(value);
    }

    /**
     * SuperPlayerView的回调接口
     */
    public interface OnSuperPlayerViewCallback {

        /**
         * 开始全屏播放
         */
        void onStartFullScreenPlay();

        /**
         * 结束全屏播放
         */
        void onStopFullScreenPlay();

        /**
         * 点击悬浮窗模式下的x按钮
         */
        void onClickFloatCloseBtn();

        /**
         * 点击小播放模式的返回按钮
         */
        void onClickSmallReturnBtn();

        /**
         * 开始悬浮窗播放
         */
        void onStartFloatWindowPlay();

        /**
         * 开始播放回调
         */
        void onPlaying();

        /**
         * 播放结束
         */
        void onPlayEnd();

        /**
         * 当播放失败的时候回调
         *
         * @param code
         */
        void onError(int code);
    }

    public void release() {
        if (mWindowPlayer != null) {
            mWindowPlayer.release();
        }
        if (mFullScreenPlayer != null) {
            mFullScreenPlayer.release();
        }
        if (mFloatPlayer != null) {
            mFloatPlayer.release();
        }
        if (mDynamicWatermarkView != null) {
            mDynamicWatermarkView.release();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            release();
        } catch (Throwable e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void switchPlayMode(SuperPlayerDef.PlayerMode playerMode) {
        if (playerMode == SuperPlayerDef.PlayerMode.WINDOW) {
            if (mControllerCallback != null) {
                mControllerCallback.onSwitchPlayMode(SuperPlayerDef.PlayerMode.WINDOW);
            }
        } else if (playerMode == SuperPlayerDef.PlayerMode.FLOAT) {
            if (mPlayerViewCallback != null) {
                mPlayerViewCallback.onStartFloatWindowPlay();
            }
            if (mControllerCallback != null) {
                mControllerCallback.onSwitchPlayMode(SuperPlayerDef.PlayerMode.FLOAT);
            }
        }
    }

    public SuperPlayerDef.PlayerMode getPlayerMode() {
        return mSuperPlayer.getPlayerMode();
    }

    public SuperPlayerDef.PlayerState getPlayerState() {
        return mSuperPlayer.getPlayerState();
    }

    private void actonOfPreloadOnPlayPrepare() {
        if (mPlayAction != PLAY_ACTION_PRELOAD) {
            mWindowPlayer.prepareLoading();
            mFullScreenPlayer.prepareLoading();
        }
    }


    class PlayerObserver extends SuperPlayerObserver {

        @Override
        public void onPlayPrepare() {
            mWindowPlayer.updatePlayState(SuperPlayerDef.PlayerState.INIT);
            mFullScreenPlayer.updatePlayState(SuperPlayerDef.PlayerState.INIT);
            actonOfPreloadOnPlayPrepare();
            // 清空关键帧和视频打点信息
            if (mWatcher != null) {
                mWatcher.stop();
            }
        }

        @Override
        public void onPlayBegin(String name) {
            mWindowPlayer.updatePlayState(SuperPlayerDef.PlayerState.PLAYING);
            mFullScreenPlayer.updatePlayState(SuperPlayerDef.PlayerState.PLAYING);
            updateTitle(name);
            mWindowPlayer.hideBackground();
            if (mDanmuView != null && mDanmuView.isPrepared() && mDanmuView.isPaused()) {
                mDanmuView.resume();
            }
            if (mWatcher != null) {
                mWatcher.exitLoading();
            }
            notifyCallbackPlaying();
        }

        @Override
        public void onPlayPause() {
            mWindowPlayer.updatePlayState(SuperPlayerDef.PlayerState.PAUSE);
            mFullScreenPlayer.updatePlayState(SuperPlayerDef.PlayerState.PAUSE);
        }

        @Override
        public void onPlayStop() {
            if (mSuperPlayerModelList.size() >= 1 && mIsPlayInit && mIsLoopPlayList) {
                playNextVideo();
            } else {
                mWindowPlayer.updatePlayState(SuperPlayerDef.PlayerState.END);
                mFullScreenPlayer.updatePlayState(SuperPlayerDef.PlayerState.END);
                // 清空关键帧和视频打点信息
                if (mWatcher != null) {
                    mWatcher.stop();
                }
            }
            notifyCallbackPlayEnd();
        }

        @Override
        public void onPlayLoading() {
            //预加载模式进行特殊处理
            if (mPlayAction == PLAY_ACTION_PRELOAD) {
                if (isCallResume) {
                    mWindowPlayer.updatePlayState(SuperPlayerDef.PlayerState.LOADING);
                    mFullScreenPlayer.updatePlayState(SuperPlayerDef.PlayerState.LOADING);
                }
            } else {
                mWindowPlayer.updatePlayState(SuperPlayerDef.PlayerState.LOADING);
                mFullScreenPlayer.updatePlayState(SuperPlayerDef.PlayerState.LOADING);
            }
            if (mWatcher != null) {
                mWatcher.enterLoading();
            }
        }

        @Override
        public void onPlayProgress(long current, long duration) {
            mProgress = current;
            mDuration = duration;
            mWindowPlayer.updateVideoProgress(current, duration);
            mFullScreenPlayer.updateVideoProgress(current, duration);
            mFloatPlayer.updateVideoProgress(current, duration);
        }

        @Override
        public void onSeek(int position) {
            if (mSuperPlayer.getPlayerType() != SuperPlayerDef.PlayerType.VOD) {
                if (mWatcher != null) {
                    mWatcher.stop();
                }
            }
        }

        @Override
        public void onSwitchStreamStart(boolean success, SuperPlayerDef.PlayerType playerType, VideoQuality quality) {
            if (playerType == SuperPlayerDef.PlayerType.LIVE) {
                if (success) {
                    Toast.makeText(mContext, "正在切换到" + quality.title + "...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "切换" + quality.title + "清晰度失败，请稍候重试", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onSwitchStreamEnd(boolean success, SuperPlayerDef.PlayerType playerType, VideoQuality quality) {
            if (playerType == SuperPlayerDef.PlayerType.LIVE) {
                if (success) {
                    Toast.makeText(mContext, "清晰度切换成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "清晰度切换失败", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onPlayerTypeChange(SuperPlayerDef.PlayerType playType) {
            mWindowPlayer.updatePlayType(playType);
            mFullScreenPlayer.updatePlayType(playType);
            mFloatPlayer.updatePlayType(playType);
        }

        @Override
        public void onPlayTimeShiftLive(TXLivePlayer player, String url) {
            if (mWatcher == null) {
                mWatcher = new NetWatcher(mContext);
            }
            mWatcher.start(url, player);
        }

        @Override
        public void onVideoQualityListChange(List<VideoQuality> videoQualities, VideoQuality defaultVideoQuality) {
            mFullScreenPlayer.setVideoQualityList(videoQualities);
            mFullScreenPlayer.updateVideoQuality(defaultVideoQuality);
        }

        @Override
        public void onVideoImageSpriteAndKeyFrameChanged(PlayImageSpriteInfo info, List<PlayKeyFrameDescInfo> list) {
            mFullScreenPlayer.updateImageSpriteInfo(info);
            mFullScreenPlayer.updateKeyFrameDescInfo(list);
        }

        @Override
        public void onError(int code, String message) {
            showToast(message);
            notifyCallbackPlayError(code);
        }

        @Override
        public void onRcvFirstIframe() {
            super.onRcvFirstIframe();
            mWindowPlayer.toggleCoverView(false);
            mFullScreenPlayer.toggleCoverView(false);
            if (mDynamicWatermarkView != null) {
                mDynamicWatermarkView.show();
            }
        }
    };

    private void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    private void showToast(int resId) {
        Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show();
    }

    /**
     * 通知播放开始，降低圈复杂度，单独提取成一个方法
     */
    private void notifyCallbackPlaying() {
        if (mPlayerViewCallback != null) {
            mPlayerViewCallback.onPlaying();
        }
    }

    /**
     * 通知播放结束，降低圈复杂度，单独提取成一个方法
     */
    private void notifyCallbackPlayEnd() {
        if (mPlayerViewCallback != null) {
            mPlayerViewCallback.onPlayEnd();
        }
    }

    /**
     * 通知播放错误，降低圈复杂度，单独提取成一个方法
     */
    private void notifyCallbackPlayError(int code) {
        if (mPlayerViewCallback != null) {
            mPlayerViewCallback.onError(code);
        }
    }

    public static void save2MediaStore(Context context, Bitmap image) {
        File sdcardDir = context.getExternalFilesDir(null);
        if (sdcardDir == null) {
            Log.e(TAG, "sdcardDir is null");
            return;
        }
        File appDir = new File(sdcardDir, "superplayer");
        if (!appDir.exists()) {
            appDir.mkdir();
        }

        long dateSeconds = System.currentTimeMillis() / 1000;
        String fileName = dateSeconds + ".jpg";
        File file = new File(appDir, fileName);

        String filePath = file.getAbsolutePath();

        File f = new File(filePath);
        if (f.exists()) {
            f.delete();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            // Save the screenshot to the MediaStore
            ContentValues values = new ContentValues();
            ContentResolver resolver = context.getContentResolver();
            values.put(MediaStore.Images.ImageColumns.DATA, filePath);
            values.put(MediaStore.Images.ImageColumns.TITLE, fileName);
            values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.ImageColumns.DATE_ADDED, dateSeconds);
            values.put(MediaStore.Images.ImageColumns.DATE_MODIFIED, dateSeconds);
            values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg");
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
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void disableGesture(boolean flag) {
        if (null != mFullScreenPlayer) {
            mFullScreenPlayer.disableGesture(flag);
        }
        if (null != mWindowPlayer) {
            mWindowPlayer.disableGesture(flag);
        }
    }

    public void setIsAutoPlay(boolean b) {
        mSuperPlayer.setAutoPlay(b);
    }

    public void setStartTime(double startTime) {
        mSuperPlayer.setStartTime((float) startTime);
    }

    public void setLoop(boolean b) {
        mSuperPlayer.setLoop(b);
    }

}
