package com.tencent.liteav.demo.player.demo;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.superplayer.SuperPlayerDef;
import com.tencent.liteav.demo.superplayer.SuperPlayerGlobalConfig;
import com.tencent.liteav.demo.superplayer.SuperPlayerModel;
import com.tencent.liteav.demo.superplayer.SuperPlayerVideoId;
import com.tencent.liteav.demo.superplayer.SuperPlayerView;
import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.player.expand.model.SuperPlayerConstants;
import com.tencent.liteav.demo.player.expand.model.GetVideoInfoListListener;
import com.tencent.liteav.demo.player.expand.model.VideoDataMgr;
import com.tencent.liteav.demo.player.expand.model.entity.VideoInfo;
import com.tencent.liteav.demo.player.expand.model.utils.SuperVodListLoader;
import com.tencent.liteav.demo.player.expand.ui.TCVodPlayerListAdapter;
import com.tencent.liteav.demo.player.expand.model.entity.VideoModel;
import com.tencent.rtmp.TXLiveBase;
import com.tencent.rtmp.TXLiveConstants;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by liyuejiao on 2018/7/3.
 * 超级播放器主Activity
 */

public class SuperPlayerActivity extends Activity implements View.OnClickListener,
        SuperVodListLoader.OnVodInfoLoadListener, SuperPlayerView.OnSuperPlayerViewCallback,
        TCVodPlayerListAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "SuperPlayerActivity";

    // 新手引导的标记
    private static final String SHARE_PREFERENCE_NAME   = "tx_super_player_guide_setting";
    private static final String KEY_GUIDE_ONE           = "is_guide_one_finish";
    private static final String KEY_GUIDE_TWO           = "is_guide_two_finish";

    private static final String DEFAULT_IMAGHOLDER      = "http://xiaozhibo-10055601.file.myqcloud.com/coverImg.jpg";

    private static final int LIST_TYPE_LIVE = 0;
    private static final int LIST_TYPE_VOD  = 1;

    private static final int REQUEST_CODE_QR_SCAN = 100;

    private Context             mContext;
    //标题
    private RelativeLayout      mLayoutTitle;
    private RelativeLayout      mRelativeMaskOne;
    private RelativeLayout      mRelativeMaskTwo;
    private ImageView           mImageBack;
    private ImageView           mBtnScan;
    private ImageView           mImageAdd;
    private ImageButton         mImageLink;
    private View                mTitleMask;
    private View                mListMask;
    private TextView            mTextOne;
    private TextView            mTextTwo;
    private SwipeRefreshLayout  mSwipeRefreshLayout;
    //超级播放器View
    private SuperPlayerView     mSuperPlayerView;
    //播放列表
    private RecyclerView        mVodPlayerListView;

    //进入默认播放的视频
    private int DEFAULT_APPID   = 1252463788;
    private int mDataType       = LIST_TYPE_LIVE;
    private int mVideoCount;
    //获取点播信息接口
    private SuperVodListLoader          mSuperVodListLoader;
    private TCVodPlayerListAdapter      mVodPlayerListAdapter;
    private GetVideoInfoListListener    mGetVideoInfoListListener;

    private ArrayList<VideoModel>       mLiveList;
    private ArrayList<VideoModel>       mVodList;
    private ArrayList<ListTabItem>      mListTabs;

    private boolean mVideoHasPlay;
    private boolean mDefaultVideo;
    private String  mVideoId;

    private static class ListTabItem {
        public ListTabItem(int type, TextView textView, ImageView imageView, View.OnClickListener listener) {
            this.type = type;
            this.textView = textView;
            this.imageView = imageView;
            this.textView.setOnClickListener(listener);
        }

        public int          type;
        public TextView     textView;
        public ImageView    imageView;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.superplayer_activity_supervod_player);

        mContext = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        checkPermission();
        initView();
        initData();

        mDataType = mDefaultVideo ? LIST_TYPE_LIVE : LIST_TYPE_VOD;
        updateList(mDataType);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String from = intent.getStringExtra("from");
        if (!TextUtils.isEmpty(from)) {
            playExternalVideo();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public void finish() {
        if (getActivityCount() == 1) {
            startMainActivity();
        }
        super.finish();
    }

    private int getActivityCount() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfoList = am.getRunningTasks(10);
        return taskInfoList.size();
    }

    private void startMainActivity() {
        Intent intent = new Intent();
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setAction("com.tencent.liteav.action.liteavapp");
        startActivity(intent);
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), 100);
            }
        }
    }

    private void initView() {
        mLayoutTitle = (RelativeLayout) findViewById(R.id.superplayer_rl_title);
        mImageBack = (ImageView) findViewById(R.id.superplayer_iv_back);
        mImageBack.setOnClickListener(this);
        mBtnScan = (ImageView) findViewById(R.id.superplayer_btn_scan);
        mBtnScan.setOnClickListener(this);
        mImageLink = (ImageButton) findViewById(R.id.superplayer_ib_webrtc_link_button);
        mImageLink.setOnClickListener(this);

        mSuperPlayerView = (SuperPlayerView) findViewById(R.id.superVodPlayerView);
        mSuperPlayerView.setPlayerViewCallback(this);

        mVodPlayerListView = (RecyclerView) findViewById(R.id.superplayer_recycler_view);
        mVodPlayerListView.setLayoutManager(new LinearLayoutManager(this));
        mVodPlayerListAdapter = new TCVodPlayerListAdapter(this);
        mVodPlayerListAdapter.setOnItemClickListener(this);
        mVodPlayerListView.setAdapter(mVodPlayerListAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.superplayer_swipe_refresh_layout_list);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mImageAdd = (ImageView) findViewById(R.id.superplayer_iv_add);
        mImageAdd.setOnClickListener(this);

        mListTabs = new ArrayList<>();
        mListTabs.add(LIST_TYPE_LIVE, new ListTabItem(LIST_TYPE_LIVE, (TextView) findViewById(R.id.superplayer_tv_live), null, this));
        mListTabs.add(LIST_TYPE_VOD, new ListTabItem(LIST_TYPE_VOD, (TextView) findViewById(R.id.superplayer_tv_vod), null, this));

        initNewGuideLayout();
        initMaskLayout();
    }

    /**
     * 初始化新手引导布局
     */
    private void initNewGuideLayout() {
        mRelativeMaskOne = (RelativeLayout) findViewById(R.id.superplayer_small_rl_mask_one);
        mRelativeMaskOne.setOnTouchListener(new View.OnTouchListener() { // 拦截事件
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mRelativeMaskTwo = (RelativeLayout) findViewById(R.id.superplayer_small_rl_mask_two);
        mRelativeMaskTwo.setOnTouchListener(new View.OnTouchListener() { // 拦截事件
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mTextOne = (TextView) findViewById(R.id.superplayer_small_tv_btn1);
        mTextTwo = (TextView) findViewById(R.id.superplayer_small_tv_btn2);

        boolean isFinishOne = getBoolean(KEY_GUIDE_ONE);
        boolean isFinishTwo = getBoolean(KEY_GUIDE_TWO);

        if (isFinishOne) {
            mRelativeMaskOne.setVisibility(GONE);
            if (isFinishTwo) {
                //ignore
            } else {
                mRelativeMaskTwo.setVisibility(VISIBLE);
            }
        } else {
            mRelativeMaskOne.setVisibility(VISIBLE);
            mRelativeMaskTwo.setVisibility(GONE);
        }

        mTextOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRelativeMaskOne.setVisibility(GONE);
                mRelativeMaskTwo.setVisibility(VISIBLE);
                putBoolean(KEY_GUIDE_ONE, true);
                putBoolean(KEY_GUIDE_TWO, false);
            }
        });
        mTextTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRelativeMaskOne.setVisibility(GONE);
                mRelativeMaskTwo.setVisibility(GONE);
                mTitleMask.setVisibility(GONE);
                mListMask.setVisibility(GONE);
                putBoolean(KEY_GUIDE_ONE, true);
                putBoolean(KEY_GUIDE_TWO, true);
            }
        });

    }

    private void initMaskLayout() {
        mTitleMask = findViewById(R.id.superplayer_view_title_mask);
        mTitleMask.setOnClickListener(new View.OnClickListener() {// 拦截所有事件
            @Override
            public void onClick(View v) {

            }
        });
        mListMask = findViewById(R.id.superplayer_view_list_mask);
        mListMask.setOnClickListener(new View.OnClickListener() { // 拦截所有事件
            @Override
            public void onClick(View v) {

            }
        });
        boolean isFinishOne = getBoolean(KEY_GUIDE_ONE);
        boolean isFinishTwo = getBoolean(KEY_GUIDE_TWO);
        if (!isFinishOne || !isFinishTwo) {
            mTitleMask.setVisibility(VISIBLE);
            mListMask.setVisibility(VISIBLE);
        } else {
            mTitleMask.setVisibility(GONE);
            mListMask.setVisibility(GONE);
        }
    }


    private void initData() {
        mLiveList = new ArrayList<>();
        mVodList = new ArrayList<>();
        mDefaultVideo = getIntent().getBooleanExtra(SuperPlayerConstants.PLAYER_DEFAULT_VIDEO, true);
        mSuperVodListLoader = new SuperVodListLoader();
        mSuperVodListLoader.setOnVodInfoLoadListener(this);

        initSuperVodGlobalSetting();

        mVideoHasPlay = false;

        mVideoCount = 0;

        TXLiveBase.setAppID("1253131631");
    }

    private void updateLiveList() {
        mLiveList.clear();
        mSuperVodListLoader.getLiveList(new SuperVodListLoader.OnListLoadListener() {
            @Override
            public void onSuccess(final ArrayList<VideoModel> superPlayerModelList) {

                SuperPlayerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mDataType != LIST_TYPE_LIVE) return;
                        mVodPlayerListAdapter.clear();
                        for (VideoModel videoModel :
                                superPlayerModelList) {
                            mVodPlayerListAdapter.addSuperPlayerModel(videoModel);
                            mLiveList.add(videoModel);
                        }
                        if (!mVideoHasPlay && !mLiveList.isEmpty()) {
                            if (mLiveList.get(0).appid > 0) {
                                TXLiveBase.setAppID("" + mLiveList.get(0).appid);
                            }
                            String from = getIntent().getStringExtra("from");
                            if (TextUtils.isEmpty(from)) {
                                playVideoModel(mLiveList.get(0));
                            } else {
                                playExternalVideo();
                            }
                            mVideoHasPlay = true;
                        }
                        mVodPlayerListAdapter.notifyDataSetChanged();

                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFail(int errCode) {
                TXCLog.e(TAG, "updateLiveList error");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        mVodPlayerListAdapter.notifyDataSetChanged();
    }

    private void updateVodList() {
        if (mDefaultVideo) {
            ArrayList<VideoModel> superPlayerModels = mSuperVodListLoader.loadDefaultVodList();
            mSuperVodListLoader.getVodInfoOneByOne(superPlayerModels);
            mImageAdd.setVisibility(VISIBLE);
        } else {
            mVideoId = getIntent().getStringExtra(SuperPlayerConstants.PLAYER_VIDEO_ID);
            if (!TextUtils.isEmpty(mVideoId)) {
                playDefaultVideo(SuperPlayerConstants.VOD_APPID, mVideoId);
                mVideoHasPlay = true;
            }
            mGetVideoInfoListListener = new GetVideoInfoListListener() {
                @Override
                public void onGetVideoInfoList(final List<VideoInfo> videoInfoList) {
                    if (mDataType != LIST_TYPE_VOD) return;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mVodPlayerListAdapter.clear();
                            mVodPlayerListAdapter.notifyDataSetChanged();
                            mSwipeRefreshLayout.setRefreshing(false);
                            ArrayList<VideoModel> videoModels = VideoDataMgr.getInstance().loadVideoInfoList(videoInfoList);
                            if (videoModels != null && videoModels.size() != 0) {
                                mSuperVodListLoader.getVodInfoOneByOne(videoModels);
                            }
                        }
                    });
                }

                @Override
                public void onFail(int errCode) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, getString(R.string.superplayer_fetch_upload_list_video_fail), Toast.LENGTH_SHORT).show();
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            };
            VideoDataMgr.getInstance().setGetVideoInfoListListener(mGetVideoInfoListListener);
            VideoDataMgr.getInstance().getVideoList();

            mBtnScan.setVisibility(GONE);
            mImageAdd.setVisibility(GONE);
        }
    }

    private void playDefaultVideo(int appid, String fileid) {
        VideoModel videoModel = new VideoModel();
        videoModel.appid = appid;
        videoModel.fileid = fileid;
        videoModel.title = getString(R.string.superplayer_small_video_special_effects_editing);
        if (videoModel.appid > 0) {
            TXLiveBase.setAppID("" + videoModel.appid);
        }
        playVideoModel(videoModel);
    }

    /**
     * 初始化超级播放器全局配置
     */
    private void initSuperVodGlobalSetting() {
        SuperPlayerGlobalConfig prefs = SuperPlayerGlobalConfig.getInstance();
        // 开启悬浮窗播放
        prefs.enableFloatWindow = true;
        // 设置悬浮窗的初始位置和宽高
        SuperPlayerGlobalConfig.TXRect rect = new SuperPlayerGlobalConfig.TXRect();
        rect.x = 0;
        rect.y = 0;
        rect.width = 810;
        rect.height = 540;
        prefs.floatViewRect = rect;
        // 播放器默认缓存个数
        prefs.maxCacheItem = 5;
        // 设置播放器渲染模式
        prefs.enableHWAcceleration = true;
        prefs.renderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
        //需要修改为自己的时移域名
        prefs.playShiftDomain = "liteavapp.timeshift.qcloud.com";
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSuperPlayerView.getPlayerState() == SuperPlayerDef.PlayerState.PLAYING
        || mSuperPlayerView.getPlayerState() == SuperPlayerDef.PlayerState.PAUSE) {
            Log.i(TAG, "onResume state :" + mSuperPlayerView.getPlayerState());
            mSuperPlayerView.onResume();
            if (mSuperPlayerView.getPlayerMode() == SuperPlayerDef.PlayerMode.FLOAT) {
                mSuperPlayerView.switchPlayMode(SuperPlayerDef.PlayerMode.WINDOW);
            }
        }
        if (mSuperPlayerView.getPlayerMode() == SuperPlayerDef.PlayerMode.FULLSCREEN) {
            //隐藏虚拟按键，并且全屏
            View decorView = getWindow().getDecorView();
            if (decorView == null) return;
            if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
                decorView.setSystemUiVisibility(View.GONE);
            } else if (Build.VERSION.SDK_INT >= 19) {
                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause state :" + mSuperPlayerView.getPlayerState());
        if (mSuperPlayerView.getPlayerMode() != SuperPlayerDef.PlayerMode.FLOAT) {
            mSuperPlayerView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSuperPlayerView.release();
        if (mSuperPlayerView.getPlayerMode() != SuperPlayerDef.PlayerMode.FLOAT) {
            mSuperPlayerView.resetPlayer();
        }
        VideoDataMgr.getInstance().setGetVideoInfoListListener(null);
    }

    /**
     * 获取点播信息成功
     */
    @Override
    public void onSuccess(final VideoModel videoModel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDataType != LIST_TYPE_VOD) return;
                mVodPlayerListAdapter.addSuperPlayerModel(videoModel);
                mVodList.add(videoModel);
            }
        });
    }

    /**
     * 获取点播信息失败
     *
     * @param errCode
     */
    @Override
    public void onFail(int errCode) {
        TXCLog.i(TAG, "onFail errCode:" + errCode);
    }

    @Override
    public void onItemClick(int position, final VideoModel videoModel) {
        if (videoModel.appid > 0) {
            TXLiveBase.setAppID("" + videoModel.appid);
        }
        playVideoModel(videoModel);
    }

    private void playVideoModel(VideoModel videoModel) {
        final SuperPlayerModel superPlayerModelV3 = new SuperPlayerModel();
        superPlayerModelV3.appId = videoModel.appid;
        if (!TextUtils.isEmpty(videoModel.videoURL)) {
            if (isSuperPlayerVideo(videoModel)) {
                playSuperPlayerVideo(videoModel);
                return;
            } else {
                superPlayerModelV3.title = videoModel.title;
                superPlayerModelV3.url = videoModel.videoURL;

                superPlayerModelV3.multiURLs = new ArrayList<>();
                if (videoModel.multiVideoURLs != null) {
                    for (VideoModel.VideoPlayerURL modelURL : videoModel.multiVideoURLs) {
                        superPlayerModelV3.multiURLs.add(new SuperPlayerModel.SuperPlayerURL(modelURL.url, modelURL.title));
                    }
                }
            }
        } else if (!TextUtils.isEmpty(videoModel.fileid)) {
            superPlayerModelV3.videoId = new SuperPlayerVideoId();
            superPlayerModelV3.videoId.fileId = videoModel.fileid;
        }
        mSuperPlayerView.playWithModel(superPlayerModelV3);
    }

    private boolean playSuperPlayerVideo(VideoModel videoModel) {
        final SuperPlayerModel model = new SuperPlayerModel();
        String videoUrl = videoModel.videoURL;
        String appIdStr = getValueByName(videoUrl, "appId");
        boolean rst = true;
        try {
            model.appId = appIdStr.equals("") ? 0 : Integer.valueOf(appIdStr);
            SuperPlayerVideoId videoId = new SuperPlayerVideoId();
            videoId.fileId = getValueByName(videoUrl, "fileId");
            videoId.pSign = getValueByName(videoUrl, "psign");
            model.videoId = videoId;
            mSuperPlayerView.playWithModel(model);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.superplayer_scancode_tip, Toast.LENGTH_SHORT).show();
            rst = false;
        }
        return rst;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.superplayer_iv_add) {            // 点击+添加一个点播列表项
            showAddVideoDialog();
        } else if (id == R.id.superplayer_btn_scan) {   // 扫描二维码播放一个视频
            scanQRCode();
        } else if (id == R.id.superplayer_iv_back) {    // 悬浮窗播放
            showFloatWindow();
        } else if (id == R.id.superplayer_tv_live) {
            mDataType = LIST_TYPE_LIVE;
            updateList(mDataType);
        } else if (id == R.id.superplayer_tv_vod) {
            mDataType = LIST_TYPE_VOD;
            updateList(mDataType);
        } else if (id == R.id.superplayer_ib_webrtc_link_button) {
            showCloudLink();
        }
    }

    private void showCloudLink() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://cloud.tencent.com/document/product/454/18872"));
        startActivity(intent);
    }

    private void updateList(int dataType) {
        for (ListTabItem item : mListTabs) {
            if (item.type == dataType) {
                item.textView.setTextColor(Color.rgb(255, 255, 255));
            } else {
                item.textView.setTextColor(Color.rgb(119, 119, 119));
            }
        }

        mVodPlayerListAdapter.clear();
        switch (mDataType) {
            case LIST_TYPE_LIVE:
                if (mLiveList.isEmpty()) {
                    updateLiveList();
                } else {
                    for (VideoModel videoModel :
                            mLiveList) {
                        mVodPlayerListAdapter.addSuperPlayerModel(videoModel);
                    }
                }
                break;
            case LIST_TYPE_VOD:
                if (isNeedUpdateVodList()) {
                    if (mVodList != null && !mVodList.isEmpty()) {
                        for (VideoModel videoModel : mVodList) {
                            mVodPlayerListAdapter.addSuperPlayerModel(videoModel);
                        }
                    }
                    updateVodList();
                } else {
                    for (VideoModel videoModel : mVodList) {
                        mVodPlayerListAdapter.addSuperPlayerModel(videoModel);
                    }
                }
                break;
        }

        mVodPlayerListAdapter.notifyDataSetChanged();
    }

    private boolean isNeedUpdateVodList() {
        if (mVodList == null || mVodList.isEmpty()) {
            return true;
        }
        for (VideoModel videoModel : mVodList) {
            if (DEFAULT_IMAGHOLDER != videoModel.placeholderImage) {
                return false;
            }
        }
        return true;
    }

    /**
     * 悬浮窗播放
     */
    private void showFloatWindow() {
        if (mSuperPlayerView.getPlayerState() == SuperPlayerDef.PlayerState.PLAYING) {
            mSuperPlayerView.switchPlayMode(SuperPlayerDef.PlayerMode.FLOAT);
        } else {
            mSuperPlayerView.resetPlayer();
            finish();
        }
    }

    /**
     * 扫描二维码
     */
    private void scanQRCode() {
        Intent intent = new Intent(this, QRCodeScanActivity.class);
        startActivityForResult(intent, REQUEST_CODE_QR_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null || TextUtils.isEmpty(data.getStringExtra("result"))) {
            return;
        }
        String result = data.getStringExtra("result");
        if (REQUEST_CODE_QR_SCAN == requestCode) {
            // 二维码播放视频
            playExternalVideo(result);
        }
    }

    private boolean isLivePlay(VideoModel videoModel) {
        String videoURL = videoModel.videoURL;
        if (TextUtils.isEmpty(videoModel.videoURL)) {
            return false;
        }
        if (videoURL.startsWith("rtmp://")) {
            return true;
        } else if ((videoURL.startsWith("http://") || videoURL.startsWith("https://")) && videoURL.contains(".flv")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isSuperPlayerVideo(VideoModel videoModel) {
        return videoModel.videoURL.startsWith("txsuperplayer://play_vod");
    }

    private String getValueByName(String url, String name) { //txsuperplayer://play_vod?v=4&appId=1400295357&fileId=5285890796599775084&pcfg=Default
        String result = "";
        int index = url.indexOf("?");
        String temp = url.substring(index + 1);
        String[] keyValue = temp.split("&");
        for (String str : keyValue) {
            if (str.startsWith(name + "=")) {
                result = str.replace(name + "=", "");
                break;
            }
        }
        return result;
    }

    /**
     * 点击+添加一个点播列表项
     */
    private void showAddVideoDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        final View dialogView = LayoutInflater.from(this).inflate(R.layout.superplayer_dialog_new_vod_player_fileid, null);

        dialog.setView(dialogView);

        final EditText etAppId = (EditText) dialogView.findViewById(R.id.superplayer_et_appid);
        final EditText etFileId = (EditText) dialogView.findViewById(R.id.superplayer_et_fileid);

        if (mDataType == LIST_TYPE_VOD) {
            dialog.setTitle(getString(R.string.superplayer_set_appid_fileid));
        } else {
            dialog.setTitle(getString(R.string.superplayer_set_play_url));
            dialogView.findViewById(R.id.superplayer_tv_appid_text).setVisibility(GONE);
            dialogView.findViewById(R.id.superplayer_tv_fileid_text).setVisibility(GONE);
            etFileId.setVisibility(GONE);
        }

        dialog.setNegativeButton(getString(R.string.superplayer_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setPositiveButton(getString(R.string.superplayer_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (mDataType == LIST_TYPE_VOD) {
                            String appId = etAppId.getText().toString();
                            String fileId = etFileId.getText().toString();

                            if (TextUtils.isEmpty(appId)) {
                                Toast.makeText(mContext, getString(R.string.superplayer_input_correct_appid), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (TextUtils.isEmpty(fileId)) {
                                Toast.makeText(mContext, getString(R.string.superplayer_input_correct_fileid), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            int appid;
                            try {
                                appid = Integer.parseInt(appId);
                            } catch (NumberFormatException e) {
                                Toast.makeText(mContext, getString(R.string.superplayer_input_correct_appid), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            VideoModel videoModel = new VideoModel();
                            videoModel.appid = appid;
                            videoModel.fileid = fileId;

                            // 尝试请求fileid信息
                            SuperVodListLoader loader = new SuperVodListLoader();
                            loader.setOnVodInfoLoadListener(new SuperVodListLoader.OnVodInfoLoadListener() {
                                @Override
                                public void onSuccess(final VideoModel videoModel) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mVodPlayerListAdapter.addSuperPlayerModel(videoModel);
                                        }
                                    });
                                }

                                @Override
                                public void onFail(int errCode) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(mContext, getString(R.string.superplayer_request_fail), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                            loader.getVodByFileId(videoModel);
                        } else {
                            String playUrl = etAppId.getText().toString();
                            if (TextUtils.isEmpty(playUrl)) {
                                Toast.makeText(mContext, getString(R.string.superplayer_input_correct_play_url), Toast.LENGTH_SHORT).show();
                            } else {
                                playNewVideo(playUrl);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSwipeRefreshLayout.setRefreshing(false);
                                    }
                                });
                            }
                        }

                    }
                });
        dialog.show();
    }

    private void playNewVideo(String result) {
        mVideoCount++;
        VideoModel videoModel = new VideoModel();
        videoModel.title = getString(R.string.superplayer_test_video) + mVideoCount;
        videoModel.videoURL = result;
        videoModel.placeholderImage = DEFAULT_IMAGHOLDER;
        videoModel.appid = DEFAULT_APPID;
        if (!TextUtils.isEmpty(videoModel.videoURL) && videoModel.videoURL.contains("liteavapp.qcloud.com")) {
            videoModel.appid = 1253131631;
            TXLiveBase.setAppID("1253131631");
            videoModel.multiVideoURLs = new ArrayList<>(3);
            videoModel.multiVideoURLs.add(new VideoModel.VideoPlayerURL(getString(R.string.superplayer_definition_super), videoModel.videoURL));
            videoModel.multiVideoURLs.add(new VideoModel.VideoPlayerURL(getString(R.string.superplayer_definition_high), videoModel.videoURL.replace(".flv", "_900.flv")));
            videoModel.multiVideoURLs.add(new VideoModel.VideoPlayerURL(getString(R.string.superplayer_definition_standard), videoModel.videoURL.replace(".flv", "_550.flv")));
        }
        if (!TextUtils.isEmpty(videoModel.videoURL) && videoModel.videoURL.contains("3891.liveplay.myqcloud.com")) {
            videoModel.appid = 1252463788;
            TXLiveBase.setAppID("1252463788");
        }

        boolean needRefreshList = false;
        if (isSuperPlayerVideo(videoModel)) {
            boolean rst = playSuperPlayerVideo(videoModel);
            if (rst) {
                mVodList.add(videoModel);
                needRefreshList = mDataType == LIST_TYPE_VOD;
            }
        } else if (isLivePlay(videoModel)) {
            mLiveList.add(videoModel);
            needRefreshList = mDataType == LIST_TYPE_LIVE;
            playVideoModel(videoModel);
        } else {
            mVodList.add(videoModel);
            needRefreshList = mDataType == LIST_TYPE_VOD;
            playVideoModel(videoModel);
        }
        if (needRefreshList) {
            mVodPlayerListAdapter.addSuperPlayerModel(videoModel);
            mVodPlayerListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 播放外部传入的视频
     * 通过Intent传入的数据
     */
    private void playExternalVideo() {
        Intent intent = getIntent();
        String appId = intent.getStringExtra("appId");
        String fileId = intent.getStringExtra("fileId");
        String psign = intent.getStringExtra("psign");
        playExternalVideo(appId, fileId, psign);
    }

    /**
     * 播放外部传入的数据
     * 通过扫码返回的url数据
     * @param result
     */
    private void playExternalVideo(String result) {
        if (result.contains("protocol=v4vodplay")) { // 优先解析包含v4协议字段的特殊食品
            Uri uri = Uri.parse(result);
            String appId = uri.getQueryParameter("appId");
            String fileId = uri.getQueryParameter("fileId");
            String psign = uri.getQueryParameter("psign");
            playExternalVideo(appId, fileId, psign);
        } else {
            playNewVideo(result);
        }
    }

    private void playExternalVideo(String appId, String fileId, String psign) {
//        txsuperplayer://play_vod?v=4&appId=1400295357&fileId=5285890796599775084&pcfg=Default
        String videoURL = "txsuperplayer://play_vod?appId=" + appId + "&fileId=" + fileId + "&psign=" + psign;
        Log.d(TAG, "playExternalVideo: videoURL -> " + videoURL);
        playNewVideo(videoURL);
    }

    @Override
    public void onStartFullScreenPlay() {
        // 隐藏其他元素实现全屏
        mLayoutTitle.setVisibility(GONE);
        if (mImageAdd != null) {
            mImageAdd.setVisibility(GONE);
        }
    }

    @Override
    public void onStopFullScreenPlay() {
        // 恢复原有元素
        mLayoutTitle.setVisibility(VISIBLE);
        if (mImageAdd != null) {
            mImageAdd.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onClickFloatCloseBtn() {
        // 点击悬浮窗关闭按钮，那么结束整个播放
        mSuperPlayerView.resetPlayer();
        finish();
    }

    @Override
    public void onClickSmallReturnBtn() {
        // 点击小窗模式下返回按钮，开始悬浮播放
        showFloatWindow();
    }

    @Override
    public void onStartFloatWindowPlay() {
        // 开始悬浮播放后，直接返回到桌面，进行悬浮播放
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        if (mDefaultVideo) {
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }
        if (mDataType == LIST_TYPE_VOD) {
            mVodList.clear();
            VideoDataMgr.getInstance().getVideoList();
        } else {
            updateLiveList();
        }
    }

    private void putBoolean(String key, boolean value) {
        getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putBoolean(key, value).apply();
    }

    private boolean getBoolean(String key) {
        return getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE).getBoolean(key, false);
    }
}
