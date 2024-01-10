package com.tencent.liteav.demo.player.demo.tuishortvideo.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.tencent.liteav.demo.vodcommon.entity.ConfigBean;
import com.tencent.liteav.demo.vodcommon.entity.SuperVodListLoader;
import com.tencent.liteav.demo.vodcommon.entity.VideoListModel;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;
import com.tencent.qcloud.tuiplayer.core.api.model.TUIVideoSource;
import com.tencent.rtmp.TXVodPlayConfig;

import java.util.ArrayList;
import java.util.List;

public class ShortVideoModel {
    private static final String TAG = "ShortVideoDemo:ShortVideoModel";
    private static volatile ShortVideoModel mInstance;
    private static final int APP_ID = 1500005830;
    private static final String[] FILE_IDS = new String[]{"387702294394366256", "387702294394228858",
            "387702294394228636", "387702294394228527", "387702294167066523",
            "387702294167066515", "387702294168748446", "387702294394227941"};


    SuperVodListLoader mListLoader;

    private final ArrayList<VideoModel> mSourceList = new ArrayList<>();
    private final List<TUIVideoSource> mTuiVideoSources = new ArrayList<>();
    private boolean mIsHttps = true;
    private IOnDataLoadFullListener mOnDataLoadFullListener;

    private ShortVideoModel(Context context) {
        mListLoader = new SuperVodListLoader(context);
    }

    public static ShortVideoModel getInstance(Context context) {
        if (mInstance == null) {
            synchronized (ShortVideoModel.class) {
                if (mInstance == null) {
                    mInstance = new ShortVideoModel(context.getApplicationContext());
                }
            }
        }
        return mInstance;
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
            for (String fileId : FILE_IDS) {
                VideoModel videoModel = new VideoModel();
                videoModel.appid = APP_ID;
                videoModel.fileid = fileId;
                videoModels.add(videoModel);
            }
        }
        return videoModels;
    }


    private List<VideoModel> loadDashData() {
        final List<VideoModel> tempList = new ArrayList<>();
        VideoModel model;
        model = new VideoModel();
        model.videoURL = "http://1500004424.vod2.myqcloud"
                + ".com/4383a13evodtranscq1500004424/baff45348602268011141077324/adp.20.mpd";
        model.title = "单分片 && 多码率";
        tempList.add(model);

        model = new VideoModel();
        model.videoURL = "https://dash.akamaized.net/akamai/test/caption_test/ElephantsDream"
                + "/elephants_dream_480p_heaac5_1_https.mpd";
        model.title = "with subtitle";
        tempList.add(model);

        model = new VideoModel();
        model.videoURL = "http://1500004424.vod2.myqcloud"
                + ".com/4383a13evodtranscq1500004424/baff45348602268011141077324/adp.22.mpd";
        model.title = "多分片 && 多码率";
        tempList.add(model);

        model = new VideoModel();
        model.videoURL = "https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd";
        model.title = "30fps";
        tempList.add(model);

        model = new VideoModel();
        model.videoURL = "http://1500004424.vod2.myqcloud"
                + ".com/4383a13evodtranscq1500004424/baff45348602268011141077324/adp.1163819.mpd";
        model.title = "单分片 && 单码率";
        tempList.add(model);

        model = new VideoModel();
        model.videoURL = "https://dash.akamaized.net/dash264/TestCases/2c/qualcomm/1/MultiResMPEG2.mpd";
        model.title = "time-based";
        tempList.add(model);

        model = new VideoModel();
        model.videoURL = "http://1500004424.vod2.myqcloud"
                + ".com/4383a13evodtranscq1500004424/baff45348602268011141077324/adp.1163820.mpd";
        model.title = "多分片 && 单码率";
        tempList.add(model);

        model = new VideoModel();
        model.videoURL = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899"
                + "-f0f6155f6efa.mpd";
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
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (null != callback) {
                        callback.onLoadedSuccess(transToSource(models), isRefresh);
                    } else if (null != mOnDataLoadFullListener) {
                        mOnDataLoadFullListener.onLoadedSuccess(transToSource(models), isRefresh);
                    }
                }
            }, 1000);
        } else {
            mListLoader.getVideoListInfo(models, false, new SuperVodListLoader.OnVodListLoadListener() {
                @Override
                public void onSuccess(VideoListModel videoListModel) {
                    if (null != callback) {
                        callback.onLoadedSuccess(transToSource(videoListModel.videoModelList), isRefresh);
                    } else if (null != mOnDataLoadFullListener) {
                        mOnDataLoadFullListener.onLoadedSuccess(transToSource(videoListModel.videoModelList),
                                isRefresh);
                    }
                }

                @Override
                public void onFail(int errCode) {
                    if (null != callback) {
                        callback.onLoadedFailed(errCode);
                    } else if (null != mOnDataLoadFullListener) {
                        mOnDataLoadFullListener.onLoadedFailed(errCode);
                    }
                }
            });
        }
    }

    private List<TUIVideoSource> transToSource(List<VideoModel> models) {
        TXVodPlayConfig config = new TXVodPlayConfig();
        config.setProgressInterval(1);
        config.setSmoothSwitchBitrate(true);
        config.setMaxBufferSize(5);
        config.setPreferredResolution(720 * 1280);
        config.setProgressInterval(500);
        // set preload size
        config.setMaxPreloadSize(1);
        List<TUIVideoSource> videoSources = new ArrayList<>();
        for (VideoModel model : models) {
            TUIVideoSource source = new TUIVideoSource();
            source.coverPictureUrl = model.placeholderImage;
            source.appid = model.appid;
            source.duration = model.duration;
            source.pSign = model.pSign;
            source.fileid = model.fileid;
            source.videoURL = model.videoURL;
            videoSources.add(source);
        }
        return videoSources;
    }

    public void preloadVideosIfNeed() {
        if (mTuiVideoSources.isEmpty()) {
            List<VideoModel> videoSources = loadVideoList();
            getVideoByFileId(videoSources, true, new IOnDataLoadFullListener() {
                @Override
                public void onLoadedSuccess(List<TUIVideoSource> videoBeanList, boolean isRefresh) {
                    mTuiVideoSources.addAll(videoBeanList);
                }

                @Override
                public void onLoadedFailed(int errcode) {
                }
            });
        }
    }

    public void loadMore(boolean isRefresh) {
        if (mTuiVideoSources.isEmpty()) {
            List<VideoModel> videoSources = loadVideoList();
            getVideoByFileId(videoSources, isRefresh);
        } else {
            mOnDataLoadFullListener.onLoadedSuccess(mTuiVideoSources, isRefresh);
        }
    }

    public void setOnDataLoadFullListener(IOnDataLoadFullListener listener) {
        mOnDataLoadFullListener = listener;
    }

    public interface IOnDataLoadFullListener {
        void onLoadedSuccess(List<TUIVideoSource> videoBeanList, boolean isRefresh);

        void onLoadedFailed(int errcode);
    }
}