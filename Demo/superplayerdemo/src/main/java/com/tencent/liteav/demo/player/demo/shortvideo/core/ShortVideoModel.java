package com.tencent.liteav.demo.player.demo.shortvideo.core;

import android.content.Context;

import com.tencent.liteav.demo.player.expand.model.entity.VideoListModel;
import com.tencent.liteav.demo.player.expand.model.entity.VideoModel;
import com.tencent.liteav.demo.player.expand.model.utils.SuperVodListLoader;

import java.util.ArrayList;
import java.util.List;

public class ShortVideoModel {
    private static final String TAG = "ShortVideoDemo:ShortVideoModel";
    private volatile static ShortVideoModel mInstance;
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
        for (int i = 0; i < FILE_IDS.length; i++) {
            videoModel = new VideoModel();
            videoModel.appid = APP_ID;
            videoModel.fileid = FILE_IDS[i];
            mSourceList.add(videoModel);
        }
    }


    public void setOnDataLoadFullListener(IOnDataLoadFullListener listener) {
        mOnDataLoadFullListener = listener;
    }

    public void getVideoByFileId() {
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


    public interface IOnDataLoadFullListener {
        void onLoadedSuccess(List<VideoModel> videoBeanList);

        void onLoadedFailed(int errcode);
    }
}
