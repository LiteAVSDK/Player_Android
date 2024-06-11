package com.tencent.liteav.demo.player.demo.shortvideo.core;

import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_PRELOAD;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.tencent.liteav.demo.vodcommon.entity.ConfigBean;
import com.tencent.liteav.demo.vodcommon.entity.SuperVodListLoader;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;
import com.tencent.liteav.shortvideoplayerdemo.R;

import java.util.ArrayList;
import java.util.List;

public class ShortVideoModel {
    private static final String TAG = "ShortVideoDemo:ShortVideoModel";
    private static volatile ShortVideoModel mInstance;
    SuperVodListLoader mListLoader;
    private ArrayList<VideoModel> mSourceList;
    private Context mContext;
    private IOnDataLoadFullListener mOnDataLoadFullListener;

    private ShortVideoModel(Context context) {
        mContext = context;
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
        if (ConfigBean.getInstance().isIsUseDash()) {
            mSourceList.addAll(loadDashData());
        } else {
            mSourceList.addAll(loadHlsData());
        }
    }

    private List<VideoModel> loadHlsData() {
        List<VideoModel> dataList = new ArrayList<>();
        VideoModel model = new VideoModel();
        model.videoURL = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/3d98015b387702294394366256/adp.10.m3u8";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.placeholderImage = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/3d98015b387702294394366256/coverBySnapshot/coverBySnapshot_10_0.jpg";
        dataList.add(model);

        model = new VideoModel();
        model.videoURL = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/3afba900387702294394228858/adp.10.m3u8";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.placeholderImage = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/3afba900387702294394228858/coverBySnapshot/coverBySnapshot_10_0.jpg";
        dataList.add(model);

        model = new VideoModel();
        model.videoURL = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/3afba03a387702294394228636/adp.10.m3u8";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.placeholderImage = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/3afba03a387702294394228636/coverBySnapshot/coverBySnapshot_10_0.jpg";
        dataList.add(model);

        model = new VideoModel();
        model.videoURL = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/3afb9bd9387702294394228527/adp.10.m3u8";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.placeholderImage = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/3afb9bd9387702294394228527/coverBySnapshot/coverBySnapshot_10_0.jpg";
        dataList.add(model);

        model = new VideoModel();
        model.videoURL = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/6fc8e973387702294167066523/adp.10.m3u8";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.placeholderImage = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/6fc8e973387702294167066523/coverBySnapshot/coverBySnapshot_10_0.jpg";
        dataList.add(model);

        model = new VideoModel();
        model.videoURL = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/6fc8e954387702294167066515/adp.10.m3u8";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.placeholderImage = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/6fc8e954387702294167066515/coverBySnapshot/coverBySnapshot_10_0.jpg";
        dataList.add(model);

        model = new VideoModel();
        model.videoURL = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/ccf4265f387702294168748446/adp.10.m3u8";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.placeholderImage = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/ccf4265f387702294168748446/coverBySnapshot/coverBySnapshot_10_0.jpg";
        dataList.add(model);

        model = new VideoModel();
        model.videoURL = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/3afb20b8387702294394227941/adp.10.m3u8";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.placeholderImage = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/3afb20b8387702294394227941/coverBySnapshot/coverBySnapshot_10_0.jpg";
        dataList.add(model);
        return dataList;
    }

    private List<VideoModel> loadDashData() {
        VideoModel model = new VideoModel();
        model.videoURL = "http://1500004424.vod2.myqcloud.com/4383a13evodtranscq1500004424/baff45348602268011141077324/adp.20.mpd";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.title = mContext.getString(R.string.superplayer_one_segment_multi_bitrate);
        List<VideoModel> dashData = new ArrayList<>();
        dashData.add(model);

        model = new VideoModel();
        model.videoURL = "https://dash.akamaized.net/akamai/test/caption_test/ElephantsDream/elephants_dream_480p_heaac5_1_https.mpd";
        model.title = mContext.getString(R.string.superplayer_video_subtitle);
        model.playAction = PLAY_ACTION_PRELOAD;
        dashData.add(model);


        model = new VideoModel();
        model.videoURL = "http://1500004424.vod2.myqcloud.com/4383a13evodtranscq1500004424/baff45348602268011141077324/adp.22.mpd";
        model.title = mContext.getString(R.string.superplayer_multi_segment_multi_bitrate);
        model.playAction = PLAY_ACTION_PRELOAD;
        dashData.add(model);

        model = new VideoModel();
        model.videoURL = "https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd";
        model.title = mContext.getString(R.string.superplayer_video_30fps);
        model.playAction = PLAY_ACTION_PRELOAD;
        dashData.add(model);

        model = new VideoModel();
        model.videoURL = "http://1500004424.vod2.myqcloud.com/4383a13evodtranscq1500004424/baff45348602268011141077324/adp.1163819.mpd";
        model.title = mContext.getString(R.string.superplayer_one_segment_one_bitrate);
        model.playAction = PLAY_ACTION_PRELOAD;
        dashData.add(model);

        model = new VideoModel();
        model.videoURL = "https://dash.akamaized.net/dash264/TestCases/2c/qualcomm/1/MultiResMPEG2.mpd";
        model.title = mContext.getString(R.string.superplayer_time_based);
        model.playAction = PLAY_ACTION_PRELOAD;
        dashData.add(model);

        model = new VideoModel();
        model.videoURL = "http://1500004424.vod2.myqcloud.com/4383a13evodtranscq1500004424/baff45348602268011141077324/adp.1163820.mpd";
        model.title = mContext.getString(R.string.superplayer_multi_segment_one_bitrate);
        model.playAction = PLAY_ACTION_PRELOAD;
        dashData.add(model);

        model = new VideoModel();
        model.videoURL = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd";
        model.title = mContext.getString(R.string.superplayer_number_based);
        model.playAction = PLAY_ACTION_PRELOAD;
        dashData.add(model);

        return dashData;
    }

    public void getVideoByFileId() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mOnDataLoadFullListener.onLoadedSuccess(mSourceList);
            }
        });
    }

    public void setOnDataLoadFullListener(IOnDataLoadFullListener listener) {
        mOnDataLoadFullListener = listener;
    }

    public interface IOnDataLoadFullListener {
        void onLoadedSuccess(List<VideoModel> videoBeanList);

        void onLoadedFailed(int errcode);
    }
}