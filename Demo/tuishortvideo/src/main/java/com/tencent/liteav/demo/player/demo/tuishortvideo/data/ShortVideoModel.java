package com.tencent.liteav.demo.player.demo.tuishortvideo.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.tencent.liteav.demo.player.demo.tuishortvideo.model.DemoImgSource;
import com.tencent.liteav.demo.vodcommon.entity.ConfigBean;
import com.tencent.liteav.demo.vodcommon.entity.SuperVodListLoader;
import com.tencent.liteav.demo.vodcommon.entity.VideoListModel;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;
import com.tencent.qcloud.tuiplayer.core.api.model.TUILiveSource;
import com.tencent.qcloud.tuiplayer.core.api.model.TUIPlaySource;
import com.tencent.qcloud.tuiplayer.core.api.model.TUIVideoSource;
import com.tencent.rtmp.TXVodPlayConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShortVideoModel {
    private static final String TAG = "ShortVideoModel";
    private static volatile ShortVideoModel mInstance;
    private static final int APP_ID = 1500005830;
    private static final long NET_BASE_DELAY = 500;
    private Runnable mRefreshRunnable;
    private static final String[] FILE_IDS = new String[]{"387702294394366256", "387702294394228858",
            "387702294394228636", "387702294394228527", "387702294167066523",
            "387702294167066515", "387702294168748446", "387702294394227941"};
    private static final String[] VIDEO_COVER = new String[]{
            "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/3d98015b387702294394366256" +
                    "/coverBySnapshot/coverBySnapshot_10_0.jpg",
            "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/3afba900387702294394228858" +
                    "/coverBySnapshot/coverBySnapshot_10_0.jpg",
            "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/3afba900387702294394228858" +
                    "/coverBySnapshot/coverBySnapshot_10_0.jpg",
            "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/3afb9bd9387702294394228527" +
                    "/coverBySnapshot/coverBySnapshot_10_0.jpg",
            "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/3afba03a387702294394228636" +
                    "/coverBySnapshot/coverBySnapshot_10_0.jpg",
            "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/6fc8e973387702294167066523" +
                    "/coverBySnapshot/coverBySnapshot_10_0.jpg",
            "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/6fc8e954387702294167066515" +
                    "/coverBySnapshot/coverBySnapshot_10_0.jpg",
            "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/ccf4265f387702294168748446" +
                    "/coverBySnapshot/coverBySnapshot_10_0.jpg",
            "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/3afb20b8387702294394227941" +
                    "/coverBySnapshot/coverBySnapshot_10_0.jpg",
    };

    private static final DemoImgSource[] IMG_SOURCES = new DemoImgSource[]{
            new DemoImgSource("http://1500005830.vod2.myqcloud" +
                    ".com/43843ec0vodtranscq1500005830/3d98015b387702294394366256/coverBySnapshot" +
                    "/coverBySnapshot_10_0.jpg"),
            new DemoImgSource("http://1500005830.vod2.myqcloud" +
                    ".com/43843ec0vodtranscq1500005830/3afba900387702294394228858/coverBySnapshot" +
                    "/coverBySnapshot_10_0.jpg"),
            new DemoImgSource("http://1500005830.vod2.myqcloud" +
                    ".com/43843ec0vodtranscq1500005830/3afba03a387702294394228636/coverBySnapshot" +
                    "/coverBySnapshot_10_0.jpg"),
            new DemoImgSource("http://1500005830.vod2.myqcloud" +
                    ".com/43843ec0vodtranscq1500005830/3afb9bd9387702294394228527/coverBySnapshot" +
                    "/coverBySnapshot_10_0.jpg"),
    };

    private static final TUILiveSource[] LIVE_SOURCES = new TUILiveSource[]{
            new TUILiveSource() {{
                setUrl("http://liteavapp.qcloud.com/live/liteavdemoplayerstreamid.flv");
                setCoverPictureUrl("http://1500005830.vod2.myqcloud" +
                        ".com/6c9a5118vodcq1500005830/66bc542f387702300661648850/0RyP1rZfkdQA.png");
            }},
            new TUILiveSource() {{
                setUrl("webrtc://liteavapp.qcloud.com/live/liteavdemoplayerstreamid");
                setCoverPictureUrl("http://1500005830.vod2.myqcloud" +
                        ".com/6c9a5118vodcq1500005830/66bc542f387702300661648850/0RyP1rZfkdQA.png");
            }},
            new TUILiveSource() {{
                setUrl("rtmp://liteavapp.qcloud.com/live/liteavdemoplayerstreamid");
                setCoverPictureUrl("http://1500005830.vod2.myqcloud" +
                        ".com/6c9a5118vodcq1500005830/66bc542f387702300661648850/0RyP1rZfkdQA.png");
            }},
    };

    private final ArrayList<VideoModel> mSourceList = new ArrayList<>();
    private final List<TUIPlaySource> mTuiVideoSources = new ArrayList<>();
    private boolean mIsHttps = true;
    private IOnDataLoadFullListener mOnDataLoadFullListener;

    private boolean mUseThirdPartyData = false;

    private final Handler mMainHandler;

    public static ShortVideoModel getInstance() {
        if (mInstance == null) {
            synchronized (ShortVideoModel.class) {
                if (mInstance == null) {
                    mInstance = new ShortVideoModel();
                }
            }
        }
        return mInstance;
    }

    public ShortVideoModel() {
        mMainHandler = new Handler(Looper.getMainLooper());
    }


    public void release() {
        mInstance = null;
    }

    public void loadDefaultVideo() {
        mSourceList.clear();
        VideoModel videoModel;
        if (ConfigBean.getInstance().isIsUseDash()) {
            mSourceList.addAll(loadDashData());
        } else {
            for (int i = 0; i < FILE_IDS.length; i++) {
                videoModel = new VideoModel();
                videoModel.appid = APP_ID;
                videoModel.fileid = FILE_IDS[i];
                mSourceList.add(videoModel);
            }
        }
    }

    public List<VideoModel> loadVideoList() {
        List<VideoModel> videoModels;
        if (ConfigBean.getInstance().isIsUseDash()) {
            videoModels = loadDashData();
        } else {
            videoModels = new ArrayList<>();
            int i = 0;
            for (String fileId : FILE_IDS) {
                VideoModel videoModel = new VideoModel();
                videoModel.appid = APP_ID;
                videoModel.fileid = fileId;
                if (i < VIDEO_COVER.length) {
                    videoModel.coverPictureUrl = VIDEO_COVER[i++];
                }
                videoModels.add(videoModel);
            }
        }
        return videoModels;
    }


    private List<VideoModel> loadDashData() {
        List<VideoModel> tempList = new ArrayList<>();
        VideoModel model;
        model = new VideoModel();
        model.videoURL = "http://1500004424.vod2.myqcloud" +
                ".com/4383a13evodtranscq1500004424/baff45348602268011141077324/adp.20.mpd";
        model.title = "单分片 && 多码率";
        tempList.add(model);

        model = new VideoModel();
        model.videoURL = "https://dash.akamaized.net/akamai/test/caption_test/ElephantsDream" +
                "/elephants_dream_480p_heaac5_1_https.mpd";
        model.title = "with subtitle";
        tempList.add(model);

        model = new VideoModel();
        model.videoURL = "http://1500004424.vod2.myqcloud" +
                ".com/4383a13evodtranscq1500004424/baff45348602268011141077324/adp.22.mpd";
        model.title = "多分片 && 多码率";
        tempList.add(model);

        model = new VideoModel();
        model.videoURL = "https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd";
        model.title = "30fps";
        tempList.add(model);

        model = new VideoModel();
        model.videoURL = "http://1500004424.vod2.myqcloud" +
                ".com/4383a13evodtranscq1500004424/baff45348602268011141077324/adp.1163819.mpd";
        model.title = "单分片 && 单码率";
        tempList.add(model);

        model = new VideoModel();
        model.videoURL = "https://dash.akamaized.net/dash264/TestCases/2c/qualcomm/1/MultiResMPEG2.mpd";
        model.title = "time-based";
        tempList.add(model);

        model = new VideoModel();
        model.videoURL = "http://1500004424.vod2.myqcloud" +
                ".com/4383a13evodtranscq1500004424/baff45348602268011141077324/adp.1163820.mpd";
        model.title = "多分片 && 单码率";
        tempList.add(model);

        model = new VideoModel();
        model.videoURL = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899" +
                "-f0f6155f6efa.mpd";
        model.title = "number-based";
        tempList.add(model);

        return tempList;
    }

    public void getVideoByFileId() {
        getVideoByFileId(mSourceList, true);
    }

    public void getVideoByFileId(final List<VideoModel> models, final boolean isRefresh) {
        getVideoByFileId(models, isRefresh, null);
    }

    public void getVideoByFileId(final List<VideoModel> models, final boolean isRefresh,
                                 final IOnDataLoadFullListener callback) {
        if (ConfigBean.getInstance().isIsUseDash()) {
            // 模拟网络耗时
            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (null != callback) {
                        callback.onLoadedSuccess(transToSource(models), isRefresh);
                    } else if (null != mOnDataLoadFullListener) {
                        mOnDataLoadFullListener.onLoadedSuccess(transToSource(models), isRefresh);
                    }
                }
            }, 500);
        } else {
            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (null != callback) {
                        callback.onLoadedSuccess(transToSource(models), isRefresh);
                    } else if (null != mOnDataLoadFullListener) {
                        mOnDataLoadFullListener.onLoadedSuccess(transToSource(models),
                                isRefresh);
                    }
                }
            }, 500);
        }
    }

    private List<TUIPlaySource> transToSource(List<VideoModel> models) {
        List<TUIPlaySource> videoSources = new ArrayList<>();
        for (VideoModel model : models) {
            TUIVideoSource source = new TUIVideoSource();
            source.setCoverPictureUrl(model.placeholderImage);
            source.setAppId(model.appid);
            source.setPSign(model.pSign);
            source.setFileId(model.fileid);
            source.setVideoURL(model.videoURL);
            videoSources.add(source);
        }
        // live
        videoSources.addAll(Arrays.asList(LIVE_SOURCES));
        // custom
        videoSources.addAll(Arrays.asList(IMG_SOURCES));
        // shuffle
//        Collections.shuffle(videoSources);
        return videoSources;
    }

    public void preloadVideosIfNeed() {
        if (mTuiVideoSources.isEmpty()) {
            mTuiVideoSources.addAll(transToSource(loadVideoList()));
        }
    }

    public void loadMore(final boolean isRefresh) {
        final Runnable oldRunnable = mRefreshRunnable;
        if (null != oldRunnable) {
            mMainHandler.removeCallbacks(oldRunnable);
            mRefreshRunnable = null;
        }
        // 模拟网络随机延迟
        mMainHandler.postDelayed(mRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRefresh) {
                    if (mTuiVideoSources.isEmpty()) {
                        mTuiVideoSources.addAll(transToSource(loadVideoList()));
                    }
                    if (null != mOnDataLoadFullListener) {
                        mOnDataLoadFullListener.onLoadedSuccess(mTuiVideoSources, true);
                    }
                } else {
                    List<VideoModel> videoSources = loadVideoList();
                    getVideoByFileId(videoSources, false);
                }
            }
        }, (long) (Math.random() * NET_BASE_DELAY));
    }

    public void setOnDataLoadFullListener(IOnDataLoadFullListener listener) {
        mOnDataLoadFullListener = listener;
    }

    public void removeOnDataLoadFullListenerSafe(IOnDataLoadFullListener listener) {
        if (listener == mOnDataLoadFullListener) {
            mOnDataLoadFullListener = null;
        }
    }

    public interface IOnDataLoadFullListener {
        void onLoadedSuccess(List<TUIPlaySource> videoBeanList, boolean isRefresh);

        void onLoadedFailed(int errcode);
    }
}