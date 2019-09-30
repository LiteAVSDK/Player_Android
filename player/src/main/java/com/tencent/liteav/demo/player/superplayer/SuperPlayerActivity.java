package com.tencent.liteav.demo.player.superplayer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.player.common.activity.QRCodeScanActivity;
import com.tencent.liteav.demo.player.common.utils.TCConstants;
import com.tencent.liteav.demo.player.server.GetVideoInfoListListener;
import com.tencent.liteav.demo.player.server.VideoDataMgr;
import com.tencent.liteav.demo.player.server.VideoInfo;
import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.play.SuperPlayerConst;
import com.tencent.liteav.demo.play.SuperPlayerGlobalConfig;
import com.tencent.liteav.demo.play.SuperPlayerModel;
import com.tencent.liteav.demo.play.SuperPlayerView;
import com.tencent.liteav.demo.play.v3.SuperPlayerVideoId;
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
        TCVodPlayerListAdapter.OnItemClickLitener, SwipeRefreshLayout.OnRefreshListener {
    // 新手引导的标记
    private static final String SHARE_PREFERENCE_NAME = "tx_super_player_guide_setting";
    private static final String KEY_GUIDE_ONE = "is_guide_one_finish";
    private static final String KEY_GUIDE_TWO = "is_guide_two_finish";

    private static final String TAG = "SuperPlayerActivity";
    private static final int LIST_TYPE_LIVE = 0;
    private static final int LIST_TYPE_VOD  = 1;

    private Context mContext;
    //标题
    private RelativeLayout mLayoutTitle;
    private ImageView mIvBack;
    private ImageView mBtnScan;
    private ImageButton mBtnLink;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    //超级播放器View
    private SuperPlayerView mSuperPlayerView;
    //播放列表
    private RecyclerView mVodPlayerListView;
    private TCVodPlayerListAdapter mVodPlayerListAdapter;

    private ImageView mIvAdd;
    //进入默认播放的视频
    private int DEFAULT_APPID = 1252463788;
    private String DEFAULT_FILEID = "4564972819220421305";
    //获取点播信息接口
    private SuperVodListLoader mSuperVodListLoader;

    //上传文件列表
    private boolean mDefaultVideo;
    private String mVideoId;
    private GetVideoInfoListListener mGetVideoInfoListListener;

    private ArrayList<VideoModel> mLiveList;
    private ArrayList<VideoModel> mVodList;
    private int mDataType = LIST_TYPE_LIVE;
    private List<ListTabItem> mListTabs;
    private int mVideoCount;
    private boolean mVideoHasPlay;

    private View mTitleMask,mListMask;
    private RelativeLayout mRlMaskOne,mRlMaskTwo;
    private TextView mTvBtnOne,mTvBtnTwo;

    private static class ListTabItem {
        public ListTabItem(int type, TextView textView, ImageView imageView, View.OnClickListener listener) {
            this.type = type;
            this.textView = textView;
            this.imageView = imageView;
            this.textView.setOnClickListener(listener);
        }
        public int type;
        public TextView textView;
        public ImageView imageView;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervod_player);

        mContext = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        checkPermission();
        initView();
        initData();

        mDataType = mDefaultVideo?LIST_TYPE_LIVE:LIST_TYPE_VOD;
        updateList(mDataType);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
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
        mLayoutTitle = (RelativeLayout) findViewById(R.id.layout_title);
        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(this);
        mBtnScan = (ImageView) findViewById(R.id.btnScan);
        mBtnScan.setOnClickListener(this);
        mBtnLink = (ImageButton) findViewById(R.id.webrtc_link_button);
        mBtnLink.setOnClickListener(this);

        mSuperPlayerView = (SuperPlayerView) findViewById(R.id.superVodPlayerView);
        mSuperPlayerView.setPlayerViewCallback(this);

        mVodPlayerListView = (RecyclerView) findViewById(R.id.recycler_view);
        mVodPlayerListView.setLayoutManager(new LinearLayoutManager(this));
        mVodPlayerListAdapter = new TCVodPlayerListAdapter(this);
        mVodPlayerListAdapter.setOnItemClickLitener(this);
        mVodPlayerListView.setAdapter(mVodPlayerListAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout_list);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mIvAdd = (ImageView) findViewById(R.id.iv_add);
        mIvAdd.setOnClickListener(this);

        mListTabs = new ArrayList<>();
        mListTabs.add(LIST_TYPE_LIVE, new ListTabItem(LIST_TYPE_LIVE, (TextView) findViewById(R.id.text_live), null, this));
        mListTabs.add(LIST_TYPE_VOD, new ListTabItem(LIST_TYPE_VOD, (TextView) findViewById(R.id.text_vod), null, this));

        initNewGuideLayout();
        initMaskLayout();
    }

    /**
     * 初始化新手引导布局
     */
    private void initNewGuideLayout() {
        mRlMaskOne = (RelativeLayout) findViewById(R.id.small_rl_mask_one);
        mRlMaskOne.setOnTouchListener(new View.OnTouchListener() { // 拦截事件
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mRlMaskTwo = (RelativeLayout) findViewById(R.id.small_rl_mask_two);
        mRlMaskTwo.setOnTouchListener(new View.OnTouchListener() { // 拦截事件
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mTvBtnOne = (TextView) findViewById(R.id.small_tv_btn1);
        mTvBtnTwo = (TextView) findViewById(R.id.small_tv_btn2);

        final SharedPreferences s = SharePreferenceUtils.newInstance(this, SHARE_PREFERENCE_NAME);
        boolean isFinishOne = SharePreferenceUtils.getBoolean(s, KEY_GUIDE_ONE);
        boolean isFinishTwo = SharePreferenceUtils.getBoolean(s, KEY_GUIDE_TWO);

        if (isFinishOne) {
            mRlMaskOne.setVisibility(GONE);
            if (isFinishTwo) {
                //ignore
            } else {
                mRlMaskTwo.setVisibility(VISIBLE);
            }
        } else {
            mRlMaskOne.setVisibility(VISIBLE);
            mRlMaskTwo.setVisibility(GONE);
        }

        mTvBtnOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRlMaskOne.setVisibility(GONE);
                mRlMaskTwo.setVisibility(VISIBLE);
                SharePreferenceUtils.putBoolean(s, KEY_GUIDE_ONE, true);
                SharePreferenceUtils.putBoolean(s, KEY_GUIDE_TWO, false);
            }
        });
        mTvBtnTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRlMaskOne.setVisibility(GONE);
                mRlMaskTwo.setVisibility(GONE);
                mTitleMask.setVisibility(GONE);
                mListMask.setVisibility(GONE);
                SharePreferenceUtils.putBoolean(s, KEY_GUIDE_ONE, true);
                SharePreferenceUtils.putBoolean(s, KEY_GUIDE_TWO, true);
            }
        });

    }

    private void initMaskLayout() {
        mTitleMask  = findViewById(R.id.super_view_title_mask);
        mTitleMask.setOnClickListener(new View.OnClickListener() {// 拦截所有事件
            @Override
            public void onClick(View v) {

            }
        });
        mListMask = findViewById(R.id.super_view_list_mask);
        mListMask.setOnClickListener(new View.OnClickListener() { // 拦截所有事件
            @Override
            public void onClick(View v) {

            }
        });
        final SharedPreferences s = SharePreferenceUtils.newInstance(this, SHARE_PREFERENCE_NAME);
        boolean isFinishOne = SharePreferenceUtils.getBoolean(s, KEY_GUIDE_ONE);
        boolean isFinishTwo = SharePreferenceUtils.getBoolean(s, KEY_GUIDE_TWO);
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
        mDefaultVideo = getIntent().getBooleanExtra(TCConstants.PLAYER_DEFAULT_VIDEO, true);
        mSuperVodListLoader = new SuperVodListLoader();
        mSuperVodListLoader.setOnVodInfoLoadListener(this);

        initSuperVodGlobalSetting();

        mVideoHasPlay = false;

        mVideoCount = 0;

        TXLiveBase.setAppID("1253131631");
    }

    private void updateData() {
        switch (mDataType) {
            case LIST_TYPE_LIVE:
                updateLiveList();
                break;
            case LIST_TYPE_VOD:
                updateVodList();
                break;
        }
    }

    private void updateLiveList() {
//        if (mDefaultVideo) {
//            ArrayList<SuperPlayerModel> superPlayerModels = mSuperVodListLoader.loadDefaultLiveList();
//            for(int i = 0; i<superPlayerModels.size(); i++) {
//                SuperPlayerModel requestModel = superPlayerModels.get(i);
//                mVodPlayerListAdapter.addSuperPlayerModel(requestModel);
//            }
//        } else {
        mLiveList.clear();
        mSuperVodListLoader.getLiveList(new SuperVodListLoader.OnListLoadListener() {
            @Override
            public void onSuccess(final ArrayList<VideoModel> superPlayerModelList) {

                SuperPlayerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mDataType != LIST_TYPE_LIVE) return;
                        mVodPlayerListAdapter.clear();
                        for (VideoModel videoModel:
                                superPlayerModelList) {
                            mVodPlayerListAdapter.addSuperPlayerModel(videoModel);
                            mLiveList.add(videoModel);
                        }
                        if (!mVideoHasPlay && !mLiveList.isEmpty()) {
                            if (mLiveList.get(0).appid > 0) {
                                TXLiveBase.setAppID(""+mLiveList.get(0).appid);
                            }
//                            mSuperPlayerView.playWithMode(mLiveList.get(0));
                            playVideoModel(mLiveList.get(0));
                            mVideoHasPlay = true;
                        }
                        mVodPlayerListAdapter.notifyDataSetChanged();

                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFail(int errCode) {
                TXCLog.e(TAG,"updateLiveList error");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
//        }
        mVodPlayerListAdapter.notifyDataSetChanged();

//        if (mIvAdd != null) {
//            mIvAdd.setVisibility(View.GONE);
//        }
    }

    private void updateVodList() {
        if (mDefaultVideo) {
            mVodList.clear();
            ArrayList<VideoModel> superPlayerModels = mSuperVodListLoader.loadDefaultVodList();
            mSuperVodListLoader.getVodInfoOneByOne(superPlayerModels);

            mIvAdd.setVisibility(VISIBLE);
        } else {
            mVideoId = getIntent().getStringExtra(TCConstants.PLAYER_VIDEO_ID);
            if (!TextUtils.isEmpty(mVideoId)) {
                playDefaultVideo(TCConstants.VOD_APPID, mVideoId);
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
                            Toast.makeText(mContext, "获取已上传的视频列表失败", Toast.LENGTH_SHORT).show();
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            };

            mVodList.clear();
            VideoDataMgr.getInstance().setGetVideoInfoListListener(mGetVideoInfoListListener);
            VideoDataMgr.getInstance().getVideoList();

            mBtnScan.setVisibility(GONE);
            mIvAdd.setVisibility(GONE);
        }
    }
    private void playDefaultVideo(int appid, String fileid) {
        VideoModel videoModel = new VideoModel();
        videoModel.appid = appid;
        videoModel.fileid = fileid;
        videoModel.title = "小视频-特效剪辑";
        if (videoModel.appid > 0) {
            TXLiveBase.setAppID(""+videoModel.appid);
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
        prefs.playShiftDomain = "playtimeshift.live.myqcloud.com";//需要修改为自己的时移域名
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSuperPlayerView.getPlayState() == SuperPlayerConst.PLAYSTATE_PLAY) {
            Log.i(TAG, "onResume state :" + mSuperPlayerView.getPlayState());
            mSuperPlayerView.onResume();
            if (mSuperPlayerView.getPlayMode() == SuperPlayerConst.PLAYMODE_FLOAT) {
                mSuperPlayerView.requestPlayMode(SuperPlayerConst.PLAYMODE_WINDOW);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause state :" + mSuperPlayerView.getPlayState());
        if (mSuperPlayerView.getPlayMode() != SuperPlayerConst.PLAYMODE_FLOAT) {
            mSuperPlayerView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSuperPlayerView.release();
        if (mSuperPlayerView.getPlayMode() != SuperPlayerConst.PLAYMODE_FLOAT) {
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
//                if (!mVodList.contains(mSecDRMVideoModel)) {
//                    mVodList.add(mSecDRMVideoModel);
//                    mVodPlayerListAdapter.addSuperPlayerModel(mSecDRMVideoModel);
//                }
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
            superPlayerModelV3.title = videoModel.title;
            superPlayerModelV3.url = videoModel.videoURL;
            superPlayerModelV3.qualityName = "原画";

            superPlayerModelV3.multiURLs = new ArrayList<>();
            if (videoModel.multiVideoURLs != null) {
                for (VideoModel.VideoPlayerURL modelURL : videoModel.multiVideoURLs) {
                    superPlayerModelV3.multiURLs.add(new SuperPlayerModel.SuperPlayerURL(modelURL.url, modelURL.title));
                }
            }
        } else if (!TextUtils.isEmpty(videoModel.fileid)) {
            superPlayerModelV3.videoId = new SuperPlayerVideoId();
            superPlayerModelV3.videoId.fileId = videoModel.fileid;
        }
        mSuperPlayerView.playWithModel(superPlayerModelV3);


//        superPlayerModelV3.appId = 1253039488;
//        superPlayerModelV3.videoId = new SuperPlayerVideoId();
//        superPlayerModelV3.videoId.fileId = "15517827183920333646";
//        superPlayerModelV3.videoId.playDefinition = "10";
//        superPlayerModelV3.videoId.version = SuperPlayerVideoId.FILE_ID_V3;
//        String fileId = "15517827183920333646";
//        String testTokenURL = "http://129.204.177.142/gettoken?fileId=" + fileId;
//        // 发起网络请求，获取Token
//        TCHttpURLClient.getInstance().get(testTokenURL, new TCHttpURLClient.OnHttpCallback() {
//            @Override
//            public void onSuccess(String token) {
//                try {
//                    Log.i(TAG, "onSuccess: token = " + token);
//                    // Token需要进行URLEncoder
//                    String encodedToken = URLEncoder.encode(token, "UTF-8");
//                    superPlayerModelV3.videoId.token = encodedToken;
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//                mSuperPlayerView.playWithModel(superPlayerModelV3);
//            }
//
//            @Override
//            public void onError() {
//
//            }
//        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_add) {   //[点击+添加一个点播列表项]
            showAddVideoDialog();
        } else if (id == R.id.btnScan) {  //[扫描二维码播放一个视频]
            scanQRCode();
        } else if (id == R.id.iv_back) {  //悬浮窗播放
            showFloatWindow();
        } else if (id == R.id.text_live) {
            mDataType = LIST_TYPE_LIVE;
            updateList(mDataType);
        } else if (id == R.id.text_vod) {
            mDataType = LIST_TYPE_VOD;
            updateList(mDataType);
        } else if (id == R.id.webrtc_link_button) {
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
//                item.imageView.setVisibility(View.VISIBLE);
            } else {
                item.textView.setTextColor(Color.rgb(119, 119, 119));
//                item.imageView.setVisibility(View.INVISIBLE);
            }
        }

        mVodPlayerListAdapter.clear();
        switch (mDataType) {
            case LIST_TYPE_LIVE:
                if (mLiveList.isEmpty()) {
                    updateLiveList();
                } else {
                    for (VideoModel videoModel:
                            mLiveList) {
                        mVodPlayerListAdapter.addSuperPlayerModel(videoModel);
                    }
                }
                break;
            case LIST_TYPE_VOD:
                if (mVodList.isEmpty()) {
                    updateVodList();
                } else {
                    for (VideoModel videoModel:
                            mVodList) {
                        mVodPlayerListAdapter.addSuperPlayerModel(videoModel);
                    }
                }
                break;
        }

        mVodPlayerListAdapter.notifyDataSetChanged();
    }
    /**
     * 悬浮窗播放
     */
    private void showFloatWindow() {
        if (mSuperPlayerView.getPlayState() == SuperPlayerConst.PLAYSTATE_PLAY) {
            mSuperPlayerView.requestPlayMode(SuperPlayerConst.PLAYMODE_FLOAT);
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
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null || data.getExtras() == null || TextUtils.isEmpty(data.getExtras().getString("result"))) {
            return;
        }
        String result = data.getExtras().getString("result");
        if (requestCode == 200) {
//            EditText editText = (EditText) findViewById(R.id.editText);
//            editText.setText(result);
        } else if (requestCode == 100) {
            // 二维码播放视频
            playNewVideo(result);
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

    /**
     * 点击+添加一个点播列表项
     */
    private void showAddVideoDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        final View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_vod_player_fileid, null);

        dialog.setView(dialogView);

        final EditText etAppId = (EditText) dialogView.findViewById(R.id.et_appid);
        final EditText etFileId = (EditText) dialogView.findViewById(R.id.et_fileid);

        if (mDataType == LIST_TYPE_VOD) {
            dialog.setTitle("请设置AppID和FileID");
        } else {
            dialog.setTitle("请设置播放地址");
            dialogView.findViewById(R.id.et_appid_text).setVisibility(GONE);
            dialogView.findViewById(R.id.et_fileid_text).setVisibility(GONE);
            etFileId.setVisibility(GONE);
        }

        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (mDataType == LIST_TYPE_VOD) {
                            String appId = etAppId.getText().toString();
                            String fileId = etFileId.getText().toString();

                            if (TextUtils.isEmpty(appId)) {
                                Toast.makeText(mContext, "请输入正确的AppId", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (TextUtils.isEmpty(fileId)) {
                                Toast.makeText(mContext, "请输入正确的FileId", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            int appid;
                            try {
                                appid = Integer.parseInt(appId);
                            } catch (NumberFormatException e) {
                                Toast.makeText(mContext, "请输入正确的AppId", Toast.LENGTH_SHORT).show();
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
                                            Toast.makeText(mContext, "fileid请求失败", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                            loader.getVodByFileId(videoModel);
                        } else {
                            String playUrl = etAppId.getText().toString();
                            if (TextUtils.isEmpty(playUrl)) {
                                Toast.makeText(mContext, "请输入正确的播放地址", Toast.LENGTH_SHORT).show();
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
        videoModel.title = "测试视频"+mVideoCount;
        videoModel.videoURL = result;
        videoModel.placeholderImage = "http://xiaozhibo-10055601.file.myqcloud.com/coverImg.jpg";
        videoModel.appid = DEFAULT_APPID;
        if (!TextUtils.isEmpty(videoModel.videoURL) && videoModel.videoURL.contains("5815.liveplay.myqcloud.com")) {
            videoModel.appid = 1253131631;
            TXLiveBase.setAppID("1253131631");
            videoModel.multiVideoURLs = new ArrayList<>(3);
            videoModel.multiVideoURLs.add(new VideoModel.VideoPlayerURL("超清",videoModel.videoURL));
            videoModel.multiVideoURLs.add(new VideoModel.VideoPlayerURL("高清",videoModel.videoURL.replace(".flv","_900.flv")));
            videoModel.multiVideoURLs.add(new VideoModel.VideoPlayerURL("标清",videoModel.videoURL.replace(".flv","_550.flv")));
        }
        if (!TextUtils.isEmpty(videoModel.videoURL) && videoModel.videoURL.contains("3891.liveplay.myqcloud.com")) {
            videoModel.appid = 1252463788;
            TXLiveBase.setAppID("1252463788");
        }
//        mSuperPlayerView.playWithMode(videoModel);
        playVideoModel(videoModel);

        boolean needRefreshList = false;
        if (isLivePlay(videoModel)) {
            mLiveList.add(videoModel);
            needRefreshList = mDataType == LIST_TYPE_LIVE;
        } else {
            mVodList.add(videoModel);
            needRefreshList = mDataType == LIST_TYPE_VOD;
        }
        if (needRefreshList) {
            mVodPlayerListAdapter.addSuperPlayerModel(videoModel);
            mVodPlayerListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStartFullScreenPlay() {
        // 隐藏其他元素实现全屏
        mLayoutTitle.setVisibility(GONE);
        if (mIvAdd != null) {
            mIvAdd.setVisibility(GONE);
        }
    }

    @Override
    public void onStopFullScreenPlay() {
        // 恢复原有元素
        mLayoutTitle.setVisibility(VISIBLE);
        if (mIvAdd != null) {
            mIvAdd.setVisibility(VISIBLE);
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

}
