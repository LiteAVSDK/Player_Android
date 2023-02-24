package com.tencent.liteav.demo.player.demo.shortvideo.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.tencent.liteav.demo.vodcommon.entity.ConfigBean;
import com.tencent.liteav.demo.vodcommon.entity.SuperVodListLoader;
import com.tencent.liteav.demo.vodcommon.entity.VideoListModel;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;

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

    private ArrayList<VideoModel> mSourceList;
    private boolean mIsHttps = true;
    private IOnDataLoadFullListener mOnDataLoadFullListener;

    private ShortVideoModel(Context context) {
        mListLoader = new SuperVodListLoader(context);
        mSourceList = new ArrayList<>();
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
            loadDashData();
        } else {
            for (int i = 0; i < FILE_IDS.length; i++) {
                videoModel = new VideoModel();
                videoModel.appid = APP_ID;
                videoModel.fileid = FILE_IDS[i];
                mSourceList.add(videoModel);
            }
        }
    }


    private void loadDashData() {
        VideoModel model = null;
        model = new VideoModel();
        model.videoURL = "http://1500004424.vod2.myqcloud.com/4383a13evodtranscq1500004424/baff45348602268011141077324/adp.20.mpd";
        model.title = "单分片 && 多码率";
        mSourceList.add(model);

        model = new VideoModel();
        model.videoURL = "https://dash.akamaized.net/akamai/test/caption_test/ElephantsDream/elephants_dream_480p_heaac5_1_https.mpd";
        model.title = "with subtitle";
        mSourceList.add(model);

        model = new VideoModel();
        model.videoURL = "http://1500004424.vod2.myqcloud.com/4383a13evodtranscq1500004424/baff45348602268011141077324/adp.22.mpd";
        model.title = "多分片 && 多码率";
        mSourceList.add(model);

        model = new VideoModel();
        model.videoURL = "https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd";
        model.title = "30fps";
        mSourceList.add(model);

        model = new VideoModel();
        model.videoURL = "http://1500004424.vod2.myqcloud.com/4383a13evodtranscq1500004424/baff45348602268011141077324/adp.1163819.mpd";
        model.title = "单分片 && 单码率";
        mSourceList.add(model);

        model = new VideoModel();
        model.videoURL = "https://dash.akamaized.net/dash264/TestCases/2c/qualcomm/1/MultiResMPEG2.mpd";
        model.title = "time-based";
        mSourceList.add(model);

        model = new VideoModel();
        model.videoURL = "http://1500004424.vod2.myqcloud.com/4383a13evodtranscq1500004424/baff45348602268011141077324/adp.1163820.mpd";
        model.title = "多分片 && 单码率";
        mSourceList.add(model);

        model = new VideoModel();
        model.videoURL = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd";
        model.title = "number-based";
        mSourceList.add(model);

    }

    public void getVideoByFileId() {
        if (ConfigBean.getInstance().isIsUseDash()) {

            // 模拟网络耗时
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mOnDataLoadFullListener.onLoadedSuccess(mSourceList);
                }
            },1000);
        } else {
            mListLoader.getVideoListInfo(mSourceList, false, new SuperVodListLoader.OnVodListLoadListener() {
                @Override
                public void onSuccess(VideoListModel videoListModel) {
                    mOnDataLoadFullListener.onLoadedSuccess(videoListModel.videoModelList);
                }

                @Override
                public void onFail(int errCode) {
                    mOnDataLoadFullListener.onLoadedFailed(errCode);
                }
            });
        }
    }

    public void setOnDataLoadFullListener(IOnDataLoadFullListener listener) {
        mOnDataLoadFullListener = listener;
    }

    public interface IOnDataLoadFullListener {
        void onLoadedSuccess(List<VideoModel> videoBeanList);

        void onLoadedFailed(int errcode);
    }
}