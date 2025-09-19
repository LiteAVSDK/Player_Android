package com.tencent.liteav.demo.player.demo;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.tencent.liteav.demo.player.expand.SuperPlayerConstants.LIST_TYPE_LIVE;
import static com.tencent.liteav.demo.player.expand.SuperPlayerConstants.LIST_TYPE_VOD;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.player.expand.model.SuperPlayerConstants;
import com.tencent.liteav.demo.player.expand.model.VideoDataMgr;
import com.tencent.liteav.demo.player.expand.ui.TCVodPlayerListAdapter;
import com.tencent.liteav.demo.player.view.dialog.PlayerAddVideoDialog;
import com.tencent.liteav.demo.superplayer.SubtitleSourceModel;
import com.tencent.liteav.demo.superplayer.SuperPlayerDef;
import com.tencent.liteav.demo.superplayer.SuperPlayerGlobalConfig;
import com.tencent.liteav.demo.superplayer.SuperPlayerModel;
import com.tencent.liteav.demo.superplayer.SuperPlayerVideoId;
import com.tencent.liteav.demo.superplayer.SuperPlayerView;
import com.tencent.liteav.demo.superplayer.helper.IntentUtils;
import com.tencent.liteav.demo.superplayer.helper.PictureInPictureHelper;
import com.tencent.liteav.demo.superplayer.model.ISuperPlayerListener;
import com.tencent.liteav.demo.vodcommon.entity.GetVideoInfoListListener;
import com.tencent.liteav.demo.vodcommon.entity.SuperVodListLoader;
import com.tencent.liteav.demo.vodcommon.entity.VideoInfo;
import com.tencent.liteav.demo.vodcommon.entity.VideoListModel;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;
import com.tencent.rtmp.TXLiveBase;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXPlayerGlobalSetting;
import com.tencent.rtmp.TXVodConstants;
import com.tencent.rtmp.TXVodPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SuperPlayer main activity.
 *
 * 超级播放器主Activity
 */

public class SuperPlayerActivity extends FragmentActivity implements View.OnClickListener,
         SuperPlayerView.OnSuperPlayerViewCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        TCVodPlayerListAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG                   = "SuperPlayerActivity";
    private static final String SHARE_PREFERENCE_NAME = "tx_super_player_guide_setting";
    private static final String KEY_GUIDE_ONE         = "is_guide_one_finish";
    private static final String KEY_GUIDE_TWO         = "is_guide_two_finish";
    private static final String DEFAULT_FILE_ID       = "387702299774390972";
    private static final int    DEFAULT_APP_ID         = 1500005830;
    private static final String DEFAULT_IMAGHOLDER    = "http://xiaozhibo-10055601.file.myqcloud.com/coverImg.jpg";
    // The aspect ratio of the player view displayed on the current interface, using the mainstream 16:9.
    private static final float sPlayerViewDisplayRatio = (float) 720 / 1280;
    private static final int    REQUEST_CODE_QR_SCAN  = 100;

    private Context                  mContext;
    private RelativeLayout           mLayoutTitle;
    private RelativeLayout           mRelativeMaskOne;
    private RelativeLayout           mRelativeMaskTwo;
    private ImageView                mImageBack;
    private ImageView                mBtnScan;
    private ImageView                mImageAdd;
    private ImageButton              mImageLink;
    private View                     mTitleMask;
    private View                     mListMask;
    private TextView                 mTextOne;
    private TextView                 mTextTwo;
    private SwipeRefreshLayout       mSwipeRefreshLayout;
    private SuperPlayerView          mSuperPlayerView;
    private RecyclerView             mVodPlayerListView;
    private int                      DEFAULT_APPID = 1252463788;
    private int                      mDataType     = LIST_TYPE_LIVE;
    private int                      mVideoCount;
    private SuperVodListLoader mSuperVodListLoader;
    private TCVodPlayerListAdapter   mVodPlayerListAdapter;
    private GetVideoInfoListListener mGetVideoInfoListListener;
    private ArrayList<VideoModel>    mLiveList;
    private List<VideoListModel>     mVodList;
    private ArrayList<ListTabItem>   mListTabs;
    private boolean                  mVideoHasPlay;
    private boolean                  mDefaultVideo;
    private String                   mVideoId;

    private boolean                  mIsManualPause = false;
    private boolean                  mUseLocalLiveData = true;
    private boolean                  mIsEnteredPIPMode = false;
    private LocalExitPIPBroadcastReceiver localExitPIPBroadcastReceiver;

    private static class ListTabItem {
        public ListTabItem(int type, TextView textView, ImageView imageView, View.OnClickListener listener) {
            this.type = type;
            this.textView = textView;
            this.imageView = imageView;
            this.textView.setOnClickListener(listener);
        }

        public int       type;
        public TextView  textView;
        public ImageView imageView;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.superplayer_activity_supervod_player);

        mContext = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
            if (TextUtils.equals(from, SuperPlayerConstants.SuperPlayerIntent.FROM_URL)) {
                String url = intent.getStringExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_URL);
                playNewVideo(url);
            } else if (TextUtils.equals(from, SuperPlayerConstants.SuperPlayerIntent.FROM_CACHE)) {
                playExternalDownloadVideo();
            } else {
                playExternalVideo();
            }
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

    /**
     * Display the player view in a 16:9 aspect ratio, with priority given to fully filling the width.
     *
     * 以16：9 比例显示播放器view，优先保证宽度完全填充
     */
    private void adjustSuperPlayerViewAndMaskHeight() {
        final int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        ViewGroup.LayoutParams layoutParams = mSuperPlayerView.getLayoutParams();
        layoutParams.width = screenWidth;
        layoutParams.height = (int) (screenWidth * sPlayerViewDisplayRatio);
        mSuperPlayerView.setLayoutParams(layoutParams);
        mRelativeMaskOne.setLayoutParams(layoutParams);
        mRelativeMaskTwo.setLayoutParams(layoutParams);
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
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        IntentUtils.safeStartActivity(this, intent);

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
        mSuperPlayerView.setSuperPlayerListener(new ISuperPlayerListener() {
            @Override
            public void onVodPlayEvent(TXVodPlayer player, int event, Bundle param) {
                switch (event) {
                    case TXVodConstants.VOD_PLAY_EVT_GET_PLAYINFO_SUCC:
                        SuperPlayerModel currentSuperPlayerModel = mSuperPlayerView.getCurrentSuperPlayerModel();
                        if (currentSuperPlayerModel == null || currentSuperPlayerModel.videoId == null) {
                            return;
                        }
                        ArrayList<VideoListModel> videoListModelList = mVodPlayerListAdapter.getVideoListModelList();
                        if (videoListModelList == null || videoListModelList.isEmpty()) {
                            return;
                        }
                        boolean needNotifyDataChange = false;

                        for (VideoListModel videoListModel : videoListModelList) {
                            if (videoListModel.videoModelList == null || videoListModel.videoModelList.isEmpty()) {
                                continue;
                            }
                            for (VideoModel videoModel : videoListModel.videoModelList) {
                                if (TextUtils.isEmpty(videoModel.fileid)) {
                                    continue;
                                }
                                if (videoModel.appid == currentSuperPlayerModel.appId && videoModel.fileid.equals(currentSuperPlayerModel.videoId.fileId)) {
                                    if (TextUtils.isEmpty(videoModel.placeholderImage)) {
                                        videoModel.placeholderImage = param.getString(TXVodConstants.EVT_PLAY_COVER_URL);
                                        needNotifyDataChange = true;
                                    }
                                    if (TextUtils.isEmpty(videoModel.title)) {
                                        videoModel.title = param.getString(TXVodConstants.EVT_PLAY_NAME);
                                        needNotifyDataChange = true;
                                    }
                                    break;
                                }
                            }
                            if (needNotifyDataChange) {
                                break;
                            }
                        }
                        if (needNotifyDataChange) {
                            mVodPlayerListAdapter.notifyDataSetChanged();
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onVodNetStatus(TXVodPlayer player, Bundle status) {
            }

            @Override
            public void onLivePlayEvent(int event, Bundle param) {
            }

            @Override
            public void onLiveNetStatus(Bundle status) {
            }
        });

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
        mListTabs.add(LIST_TYPE_LIVE, new ListTabItem(LIST_TYPE_LIVE,
                (TextView) findViewById(R.id.superplayer_tv_live), null, this));
        mListTabs.add(LIST_TYPE_VOD, new ListTabItem(LIST_TYPE_VOD,
                (TextView) findViewById(R.id.superplayer_tv_vod), null, this));

        initNewGuideLayout();
        initMaskLayout();
    }

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
        adjustSuperPlayerViewAndMaskHeight();
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
        mTitleMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mListMask = findViewById(R.id.superplayer_view_list_mask);
        mListMask.setOnClickListener(new View.OnClickListener() {
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
        mVodList = new CopyOnWriteArrayList();
        mDefaultVideo = getIntent().getBooleanExtra(SuperPlayerConstants.PLAYER_DEFAULT_VIDEO, true);
        mSuperVodListLoader = new SuperVodListLoader(this);

        initSuperVodGlobalSetting();
        mVideoHasPlay = false;
        mVideoCount = 0;
    }

    private void addVideoModelIntoVodPlayerListAdapter(VideoModel videoModel) {
        VideoListModel videoListModel = new VideoListModel();
        videoListModel.addVideoModel(videoModel);
        mVodPlayerListAdapter.addSuperPlayerModel(videoListModel);
    }

    private void updateLiveList() {
        if (mUseLocalLiveData) {
            if (mLiveList.isEmpty()) {
                VideoModel liveModel = new VideoModel();
                liveModel.title = getResources().getString(R.string.superplayer_flv_live_video);
                liveModel.placeholderImage = "http://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/66bc542f387702300661648850/0RyP1rZfkdQA.png";
                liveModel.videoURL =  "http://liteavapp.qcloud.com/live/liteavdemoplayerstreamid.flv";
                liveModel.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/66bc542f387702300661648850/0RyP1rZfkdQA.png";
                addVideoModelIntoVodPlayerListAdapter(liveModel);
                mLiveList.add(liveModel);

                liveModel = new VideoModel();
                liveModel.title = getResources().getString(R.string.superplayer_rtmp_live_video);
                liveModel.placeholderImage = "http://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/66bc542f387702300661648850/0RyP1rZfkdQA.png";
                liveModel.videoURL =  "rtmp://liteavapp.qcloud.com/live/liteavdemoplayerstreamid";
                liveModel.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/66bc542f387702300661648850/0RyP1rZfkdQA.png";
                addVideoModelIntoVodPlayerListAdapter(liveModel);
                mLiveList.add(liveModel);

                liveModel = new VideoModel();
                liveModel.title = getResources().getString(R.string.superplayer_webrtc_live_video);
                liveModel.placeholderImage = "http://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/66bc542f387702300661648850/0RyP1rZfkdQA.png";
                liveModel.videoURL =  "webrtc://liteavapp.qcloud.com/live/liteavdemoplayerstreamid";
                liveModel.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/66bc542f387702300661648850/0RyP1rZfkdQA.png";
                addVideoModelIntoVodPlayerListAdapter(liveModel);
                mLiveList.add(liveModel);

                liveModel = new VideoModel();
                liveModel.title = getResources().getString(R.string.superplayer_hls_live_video);
                liveModel.placeholderImage = "http://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/66bc542f387702300661648850/0RyP1rZfkdQA.png";
                liveModel.videoURL =  "http://liteavapp.qcloud.com/live/liteavdemoplayerstreamid.m3u8";
                liveModel.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/66bc542f387702300661648850/0RyP1rZfkdQA.png";
                addVideoModelIntoVodPlayerListAdapter(liveModel);
                mLiveList.add(liveModel);
            }
            if (!mVideoHasPlay) {
                TXLiveBase.setAppID(String.valueOf(DEFAULT_APPID));
                playVideoModel(mLiveList.get(0));
                mVideoHasPlay = true;
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void onGetVodInfoOnebyOneOnSuccess(final VideoModel videoModel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDataType != LIST_TYPE_VOD) {
                    return;
                }
                addVideoModelIntoVodPlayerListAdapter(videoModel);
                addVideoModelIntoVodList(videoModel);
            }
        });
    }

    private List<VideoModel> getSubtitleVideoData() {
        VideoModel model = null;
        model = new VideoModel();
        model.placeholderImage = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/dc455d1d387702306937256938/coverBySnapshot_10_0.jpg";
        model.videoURL = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/dc455d1d387702306937256938/adp.10.m3u8";
        SubtitleSourceModel subtitleSourceModel = null;
        subtitleSourceModel = new SubtitleSourceModel();
        subtitleSourceModel.name = "ex-cn-srt";
        subtitleSourceModel.url = "https://mediacloud-76607.gzc.vod.tencent-cloud.com/DemoResource/TED-CN.srt";
        subtitleSourceModel.mimeType = TXVodConstants.VOD_PLAY_MIMETYPE_TEXT_SRT;
        model.subtitleSourceModelList.add(subtitleSourceModel);

        subtitleSourceModel = new SubtitleSourceModel();
        subtitleSourceModel.name = "ex-in-srt";
        subtitleSourceModel.url = "https://mediacloud-76607.gzc.vod.tencent-cloud.com/DemoResource/TED-IN.srt";
        subtitleSourceModel.mimeType = TXVodConstants.VOD_PLAY_MIMETYPE_TEXT_SRT;

        model.subtitleSourceModelList.add(subtitleSourceModel);

        subtitleSourceModel = new SubtitleSourceModel();
        subtitleSourceModel.name = "ex-en-vtt";
        subtitleSourceModel.url = "https://mediacloud-76607.gzc.vod.tencent-cloud.com/DemoResource/TED-EN.vtt";
        subtitleSourceModel.mimeType = TXVodConstants.VOD_PLAY_MIMETYPE_TEXT_VTT;
        model.subtitleSourceModelList.add(subtitleSourceModel);
        model.title = getResources().getString(R.string.super_player_multi_subtitle_video);
        List<VideoModel> list = new ArrayList<>();
        list.add(model);

        model = new VideoModel();
        model.placeholderImage = "http://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/3a76d6ac387702303793151471/387702307093360124.png";
        model.videoURL = "http://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/3a76d6ac387702303793151471/iP3rnDdxMH4A.mov";
        model.title = getResources().getString(R.string.super_player_multi_sound_track_video);
        list.add(model);

        return list;
    }

    private void updateVodList() {
        if (mDefaultVideo) {
            ArrayList<VideoModel> superPlayerModels = mSuperVodListLoader.loadDefaultVodList(this.getApplicationContext());
            superPlayerModels.addAll(getSubtitleVideoData());
            for (VideoModel videoModel : superPlayerModels) {
                addVideoModelIntoVodPlayerListAdapter(videoModel);
                addVideoModelIntoVodList(videoModel);
            }

            ArrayList<VideoModel> circleModels = mSuperVodListLoader.loadCircleVodList();
            final VideoListModel circleVideoListModel = new VideoListModel();
            circleVideoListModel.videoModelList = circleModels;
            circleVideoListModel.isEnableDownload = false;
            circleVideoListModel.title = getString(R.string.superplayer_carousel_list_title);
            circleVideoListModel.icon = "http://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/6f443f33387702302226773793/2luyOg7pOR0A.png";
            mVodPlayerListAdapter.addSuperPlayerModel(circleVideoListModel);
            mVodList.add(circleVideoListModel);

            ArrayList<VideoModel> cacheModels = mSuperVodListLoader.loadCacheVodList();
            final VideoListModel cacheVideoListModel = new VideoListModel();
            cacheVideoListModel.videoModelList = cacheModels;
            cacheVideoListModel.isEnableDownload = true;
            cacheVideoListModel.title = getString(R.string.superplayer_offline_cache_title);
            cacheVideoListModel.icon = "http://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/ae6444ab387702302227194724/n6SJb2ORhQMA.png";
            mVodPlayerListAdapter.addSuperPlayerModel(cacheVideoListModel);
            mVodList.add(cacheVideoListModel);

            List<VideoModel> drmVideos = mSuperVodListLoader.loadDrmVodList();
            for (VideoModel videoModel : drmVideos) {
                addVideoModelIntoVodPlayerListAdapter(videoModel);
                addVideoModelIntoVodList(videoModel);
            }

            mImageAdd.setVisibility(VISIBLE);
        } else {
            mVideoId = getIntent().getStringExtra(SuperPlayerConstants.PLAYER_VIDEO_ID);
            if (TextUtils.isEmpty(mVideoId)) {
                playDefaultVideo(DEFAULT_APP_ID, DEFAULT_FILE_ID);
            } else {
                playDefaultVideo(SuperPlayerConstants.VOD_APPID, mVideoId);
            }
            mVideoHasPlay = true;
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
                                for (VideoModel videoModel : videoModels) {
                                    onGetVodInfoOnebyOneOnSuccess(videoModel);
                                }
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

    private void initSuperVodGlobalSetting() {
        SuperPlayerGlobalConfig prefs = SuperPlayerGlobalConfig.getInstance();
        prefs.enableFloatWindow = true;
        SuperPlayerGlobalConfig.TXRect rect = new SuperPlayerGlobalConfig.TXRect();
        rect.x = 0;
        rect.y = 0;
        rect.width = 810;
        rect.height = 540;
        prefs.floatViewRect = rect;
        prefs.enableHWAcceleration = true;
        prefs.renderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
        prefs.playShiftDomain = "liteavapp.timeshift.qcloud.com";
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (PictureInPictureHelper.hasPipPermission(this)
                && (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                && isInPictureInPictureMode()) {
            mSuperPlayerView.onResume();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSuperPlayerView.onPageResume();
        mIsEnteredPIPMode = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause state :" + mSuperPlayerView.getPlayerState());
        if (PictureInPictureHelper.hasPipPermission(this)
                && (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                && isInPictureInPictureMode()) {
            mIsEnteredPIPMode = true;
            return;
        }
        mSuperPlayerView.onPagePause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSuperPlayerView.release();
        if (mSuperPlayerView.getPlayerMode() != SuperPlayerDef.PlayerMode.FLOAT) {
            mSuperPlayerView.resetPlayer();
        }
        unRegisterLocalExitPipReceiver();
        VideoDataMgr.getInstance().setGetVideoInfoListListener(null);
    }

    @Override
    public void onItemClick(List<VideoModel> videoModelArrayList) {
        if (videoModelArrayList.size() == 1) {
            if (videoModelArrayList.get(0).appid > 0) {
                TXLiveBase.setAppID("" + videoModelArrayList.get(0).appid);
            }
            playVideoModel(videoModelArrayList.get(0));
        } else {
            playWithModelList(videoModelArrayList);
        }
    }

    private void playWithModelList(List<VideoModel> videoModelArrayList) {
        List<SuperPlayerModel> superPlayerModelList = new ArrayList<>();
        for (VideoModel videoModel : videoModelArrayList) {
            superPlayerModelList.add(videoModel.convertToSuperPlayerModel());
        }
        mSuperPlayerView.setQualityVisible(true);
        mSuperPlayerView.playWithModelListNeedLicence(superPlayerModelList, true, 0);
    }

    private void playVideoModel(VideoModel videoModel) {
        mSuperPlayerView.setQualityVisible(true);
        final SuperPlayerModel superPlayerModelV3 = new SuperPlayerModel();
        superPlayerModelV3.appId = videoModel.appid;
        superPlayerModelV3.vipWatchMode = videoModel.vipWatchModel;
        if (videoModel.dynamicWaterConfig != null) {
            superPlayerModelV3.dynamicWaterConfig = videoModel.dynamicWaterConfig;
        }
        if (!TextUtils.isEmpty(videoModel.videoURL)) {
            if (isSuperPlayerVideo(videoModel)) {
                playSuperPlayerVideo(videoModel);
                return;
            } else {
                superPlayerModelV3.title = videoModel.title;
                superPlayerModelV3.url = videoModel.videoURL;
            }
        }
        if (videoModel.multiVideoURLs != null && !videoModel.multiVideoURLs.isEmpty()) {
            superPlayerModelV3.multiURLs = new ArrayList<>();
            for (VideoModel.VideoPlayerURL modelURL : videoModel.multiVideoURLs) {
                superPlayerModelV3.multiURLs.add(new SuperPlayerModel.SuperPlayerURL(modelURL.url, modelURL.title));
            }
        }

        if (!TextUtils.isEmpty(videoModel.fileid)) {
            superPlayerModelV3.videoId = new SuperPlayerVideoId();
            superPlayerModelV3.videoId.fileId = videoModel.fileid;
            superPlayerModelV3.videoId.pSign = videoModel.pSign;
        }
        if (null != videoModel.drmBuilder) {
            superPlayerModelV3.drmBuilder = videoModel.drmBuilder;
        }
        superPlayerModelV3.playDefaultIndex = videoModel.playDefaultIndex;
        superPlayerModelV3.subtitleSourceModelList = videoModel.subtitleSourceModelList;
        superPlayerModelV3.title = videoModel.title;
        superPlayerModelV3.playAction = videoModel.playAction;
        superPlayerModelV3.placeholderImage = videoModel.placeholderImage;
        superPlayerModelV3.coverPictureUrl = videoModel.coverPictureUrl;
        superPlayerModelV3.duration = videoModel.duration;
        superPlayerModelV3.videoQualityList = videoModel.videoQualityList;
        superPlayerModelV3.isEnableCache = videoModel.isEnableDownload;
        mSuperPlayerView.playWithModelNeedLicence(superPlayerModelV3);
    }

    private boolean playSuperPlayerVideo(VideoModel videoModel) {
        mSuperPlayerView.setQualityVisible(true);
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
            mSuperPlayerView.playWithModelNeedLicence(model);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.superplayer_scancode_tip, Toast.LENGTH_SHORT).show();
            rst = false;
        }
        return rst;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.superplayer_iv_add) {
            showAddVideoDialog();
        } else if (id == R.id.superplayer_btn_scan) {
            scanQRCode();
        } else if (id == R.id.superplayer_iv_back) {
            finish();
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
        IntentUtils.safeStartActivity(this, intent);
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
                    for (VideoModel videoModel : mLiveList) {
                        addVideoModelIntoVodPlayerListAdapter(videoModel);
                    }
                }
                break;
            case LIST_TYPE_VOD:
                if (isNeedUpdateVodList()) {
                    if (mVodList != null && !mVodList.isEmpty()) {
                        for (VideoListModel videoListModel : mVodList) {
                            mVodPlayerListAdapter.addSuperPlayerModel(videoListModel);
                        }
                    }
                    updateVodList();
                } else {
                    for (VideoListModel videoListModel : mVodList) {
                        mVodPlayerListAdapter.addSuperPlayerModel(videoListModel);
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
        for (VideoListModel videoListModel : mVodList) {
            if (videoListModel.videoModelList.size() > 1) {
                if (DEFAULT_IMAGHOLDER != videoListModel.icon) {
                    return false;
                }
            } else if (videoListModel.videoModelList.size() == 1) {
                if (DEFAULT_IMAGHOLDER != videoListModel.videoModelList.get(0).placeholderImage) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Floating window playback.
     *
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
     * Scan QR code.
     *
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

    private String getValueByName(String url, String name) {
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

    private void showAddVideoDialog() {
        TXPlayerGlobalSetting.setDrmProvisionEnv(TXPlayerGlobalSetting.DrmProvisionEnv.DRM_PROVISION_ENV_CN);
        PlayerAddVideoDialog addVideoDialog = new PlayerAddVideoDialog(this, mDataType);
        addVideoDialog.setOnAddVideoListener(new PlayerAddVideoDialog.OnAddVideoListener() {
            @Override
            public void onAddVideo(VideoModel videoModel) {
                onGetVodInfoOnebyOneOnSuccess(videoModel);
                if (TextUtils.isEmpty(videoModel.title)) {
                    videoModel.title = getString(R.string.superplayer_test_video) + (mVideoCount++);
                }
                playVideoModel(videoModel);
            }
        });
        addVideoDialog.show();
    }

    private void addVideoModelIntoVodList(VideoModel videoModel) {
        VideoListModel videoListModel = new VideoListModel();
        videoListModel.addVideoModel(videoModel);
        mVodList.add(videoListModel);
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
            videoModel.multiVideoURLs.add(new VideoModel.VideoPlayerURL(getString(R.string.superplayer_definition_high),
                    videoModel.videoURL.replace(".flv", "_900.flv")));
            videoModel.multiVideoURLs.add(
                    new VideoModel.VideoPlayerURL(getString(R.string.superplayer_definition_standard),
                            videoModel.videoURL.replace(".flv", "_550.flv")));
        }
        if (!TextUtils.isEmpty(videoModel.videoURL) && videoModel.videoURL.contains("3891.liveplay.myqcloud.com")) {
            videoModel.appid = 1252463788;
            TXLiveBase.setAppID("1252463788");
        }

        boolean needRefreshList = false;
        if (isSuperPlayerVideo(videoModel)) {
            boolean rst = playSuperPlayerVideo(videoModel);
            if (rst) {
                addVideoModelIntoVodList(videoModel);
                needRefreshList = mDataType == LIST_TYPE_VOD;
            }
        } else if (isLivePlay(videoModel)) {
            mLiveList.add(videoModel);
            needRefreshList = mDataType == LIST_TYPE_LIVE;
            playVideoModel(videoModel);
        } else {
            addVideoModelIntoVodList(videoModel);
            needRefreshList = mDataType == LIST_TYPE_VOD;
            playVideoModel(videoModel);
        }
        if (needRefreshList) {
            addVideoModelIntoVodPlayerListAdapter(videoModel);
            mVodPlayerListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Play cached videos passed in externally.
     *
     * 播放外部传入的缓存视频
     */
    private void playExternalDownloadVideo() {
        final SuperPlayerModel videoModel = new SuperPlayerModel();

        Intent data = getIntent();
        videoModel.title = data.getStringExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_TITLE);
        if (TextUtils.isEmpty(videoModel.title)) {
            videoModel.title = getString(R.string.superplayer_test_video) + mVideoCount;
        }

        videoModel.placeholderImage = data.getStringExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_COVER_IMG);
        videoModel.appId = data.getIntExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_APP_ID, 0);
        videoModel.videoId = new SuperPlayerVideoId();
        videoModel.videoId.fileId = data.getStringExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_FILE_ID);
        videoModel.videoId.pSign = data.getStringExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_P_SIGN);
        videoModel.url = data.getStringExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_URL);

        // offline video play not support quality show
        mSuperPlayerView.setQualityVisible(false);
        mSuperPlayerView.playWithModelNeedLicence(videoModel);
    }

    /**
     * Play videos passed in externally.
     * Data passed in through Intent.
     *
     * 播放外部传入的视频
     * 通过Intent传入的数据
     */
    private void playExternalVideo() {
        Intent intent = getIntent();
        String appId = intent.getStringExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_APP_ID);
        String fileId = intent.getStringExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_FILE_ID);
        String psign = intent.getStringExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_P_SIGN);
        playExternalVideo(appId, fileId, psign);
    }

    /**
     * Play external data.
     * URL data returned through scanning.
     *
     * 播放外部传入的数据
     * 通过扫码返回的url数据
     */
    private void playExternalVideo(String result) {
        if (result.contains("protocol=v4vodplay")) {
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
        String videoURL = "txsuperplayer://play_vod?appId=" + appId + "&fileId=" + fileId + "&psign=" + psign;
        Log.d(TAG, "playExternalVideo: videoURL -> " + videoURL);
        playNewVideo(videoURL);
    }

    @Override
    public void onStartFullScreenPlay() {
        mLayoutTitle.setVisibility(GONE);
        if (mImageAdd != null) {
            mImageAdd.setVisibility(GONE);
        }
    }

    @Override
    public void onStopFullScreenPlay() {
        mLayoutTitle.setVisibility(VISIBLE);
        if (mDefaultVideo && mImageAdd != null) {
            mImageAdd.setVisibility(VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (PictureInPictureHelper.hasPipPermission(this) && mIsEnteredPIPMode) {
            PowerManager manager = (PowerManager)getSystemService(POWER_SERVICE);
            if ((android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                    && !manager.isInteractive()) {
                  mSuperPlayerView.onPause();
            }
        }
    }

    @Override
    public void onClickFloatCloseBtn() {
        // If the close button in the floating window is clicked, the entire playback is ended.
        mSuperPlayerView.resetPlayer();
        finish();
    }

    @Override
    public void onClickSmallReturnBtn() {
        // If the back button is clicked in small window mode, start floating playback.
        showFloatWindow();
    }

    @Override
    public void onStartFloatWindowPlay() {
        // After starting floating playback, return directly to the desktop for floating playback.
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        IntentUtils.safeStartActivity(this, intent);
    }

    @Override
    public void onPlaying() {

    }

    @Override
    public void onPlayEnd() {

    }

    @Override
    public void onError(int code) {

    }

    @Override
    public void onShowCacheListClick() {
        Intent intent = new Intent(this, VideoDownloadListActivity.class);
        IntentUtils.safeStartActivity(this, intent);
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

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mSuperPlayerView.onRequestPermissionsResult(requestCode, grantResults);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        mSuperPlayerView.onPictureInPictureModeChanged(isInPictureInPictureMode);
        if (isInPictureInPictureMode) {
            mLayoutTitle.setVisibility(GONE);
            mSuperPlayerView.showPIPIV(false);
            registerLocalExitPipReceiver();
        } else {
            mLayoutTitle.setVisibility(VISIBLE);
            mSuperPlayerView.showPIPIV(true);
            unRegisterLocalExitPipReceiver();
        }
        if (getLifecycle().getCurrentState() == Lifecycle.State.CREATED
                && (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)) {
            finishAndRemoveTask();
        }
    }

    private void registerLocalExitPipReceiver() {
        if (localExitPIPBroadcastReceiver == null) {
            localExitPIPBroadcastReceiver = new LocalExitPIPBroadcastReceiver(this);
        }
        IntentFilter intentFilter = new IntentFilter(LocalExitPIPBroadcastReceiver.EXIT_PIP);
        LocalBroadcastManager.getInstance(this).registerReceiver(localExitPIPBroadcastReceiver, intentFilter);
    }

    private void unRegisterLocalExitPipReceiver() {
        if (localExitPIPBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(localExitPIPBroadcastReceiver);
            localExitPIPBroadcastReceiver = null;
        }
    }

    public static void exitPIP(Context context){
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(LocalExitPIPBroadcastReceiver.EXIT_PIP));
    }

    private static class LocalExitPIPBroadcastReceiver extends BroadcastReceiver {
        public static final String EXIT_PIP = "com.tencent.liteav.demo.player.demo.SuperPlayerActivity.LocalExitPIPBroadcastReceiver.EXIT_PIP";
        private final WeakReference<SuperPlayerActivity> activityRef;

        LocalExitPIPBroadcastReceiver(SuperPlayerActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if (EXIT_PIP.equals(intent.getAction()) && activityRef.get() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    activityRef.get().finishAndRemoveTask();
                } else {
                    activityRef.get().finish();
                }
            }
        }
    }
}
