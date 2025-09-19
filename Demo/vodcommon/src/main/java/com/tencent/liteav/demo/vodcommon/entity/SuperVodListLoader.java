package com.tencent.liteav.demo.vodcommon.entity;

import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_MANUAL_PLAY;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.tencent.liteav.demo.superplayer.model.VipWatchModel;
import com.tencent.liteav.demo.superplayer.model.entity.DynamicWaterConfig;
import com.tencent.liteav.demo.vodcommon.R;
import com.tencent.rtmp.TXPlayerDrmBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Get VOD information
 *
 * 获取点播信息
 */
public class SuperVodListLoader {

    private static final String                TAG       = "SuperVodListLoader";
    private              Context               mContext;
    private final        int                   mAppId    = 1500005830;
    private final        Handler               mMainHandler = new Handler(Looper.getMainLooper());


    public SuperVodListLoader(Context context) {
        mContext = context;
    }

    public ArrayList<VideoModel> loadDefaultVodList(Context applicationContext) {
        ArrayList<VideoModel> list = new ArrayList<>();
        VideoModel model = new VideoModel();
        model.appid = mAppId;
        model.fileid = "387702299774251236";
        model.title = getTitleByFileId(model);
        model.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/48d0f1f9387702299774251236/coverBySnapshot/coverBySnapshot_10_0.jpg";
        list.add(model);

        model = new VideoModel();
        model.appid = mAppId;
        model.fileid = "387702299774390972";
        model.title = applicationContext.getString(R.string.superplayer_dynamic_watermark_title);
        model.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/4b6e0e84387702299774390972/387702299947629622.png";
        String tipStr = applicationContext.getString(R.string.superplayer_dynamic_watermark_tip);
        model.dynamicWaterConfig = new DynamicWaterConfig(tipStr, 30, Color.parseColor("#FF3333"));
        list.add(model);

        model = new VideoModel();
        model.appid = mAppId;
        model.fileid = "387702299774253670";
        model.title = getTitleByFileId(model);
        model.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/48d21c3d387702299774253670/387702299947604155.png";
        list.add(model);

        model = new VideoModel();
        model.appid = mAppId;
        model.fileid = "387702299774574470";
        model.title = getTitleByFileId(model);
        model.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/4ff64b01387702299774574470/387702304138941858.png";
        list.add(model);

        model = new VideoModel();
        model.appid = mAppId;
        model.fileid = "387702299774545556";
        model.title = applicationContext.getString(R.string.superplayer_vip_title);
        model.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/4fc091e4387702299774545556/387702299947278317.png";
        model.vipWatchModel = new VipWatchModel(applicationContext.getString(R.string.superplayer_vip_watch_tip), 15);
        list.add(model);

        model = new VideoModel();
        model.appid = mAppId;
        model.playAction = PLAY_ACTION_MANUAL_PLAY;
        model.fileid = "8602268011437356984";
        model.title = applicationContext.getString(R.string.superplayer_cover_title);
        model.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/cc1e28208602268011087336518/MXUW1a5I9TsA.png";
        list.add(model);

        model = new VideoModel();
        model.appid = 1500005830;
        model.title = applicationContext.getString(R.string.super_play_encrypt_video_introduction);
        model.placeholderImage = "https://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/35ab25fb243791578431393746/onEqUp.png";
        model.fileid = "243791578431393746";
        model.pSign = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6MTUwMDA"
                + "wNTgzMCwiZmlsZUlkIjoiMjQzNzkxNTc4NDMxMzkzNzQ2IiwiY3VycmVudFRpbWVTdGFtc"
                + "CI6MTY3MzQyNjIyNywiY29udGVudEluZm8iOnsiYXVkaW9WaWRlb1R5cGUiOiJQcm90ZWN0"
                + "ZWRBZGFwdGl2ZSIsImRybUFkYXB0aXZlSW5mbyI6eyJwcml2YXRlRW5jcnlwdGlvbkRlZmluaX"
                + "Rpb24iOjEyfX0sInVybEFjY2Vzc0luZm8iOnsiZG9tYWluIjoiMTUwMDAwNTgzMC52b2QyLm15cWNs"
                + "b3VkLmNvbSIsInNjaGVtZSI6IkhUVFBTIn19.q34pq7Bl0ryKDwUHGyzfXKP-CDI8vrm0k_y-IaxgF_U";
        list.add(model);

        model = new VideoModel();
        model.appid = 1500006438;
        model.title = applicationContext.getString(R.string.super_play_ghost_video);
        model.fileid = "387702307847129127";
        model.placeholderImage = "http://1500006438.vod2.myqcloud.com/4384ba25vodtranscq1500006438/558b62f3387702307847129127/coverBySnapshot_10_0.jpg";
        model.pSign = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6MTUwMDA"
                + "wNjQzOCwiZmlsZUlkIjoiMzg3NzAyMzA3ODQ3MTI5MTI3IiwiY29udG"
                + "VudEluZm8iOnsiYXVkaW9WaWRlb1R5cGUiOiJSYXdBZGFwdGl2ZSIsIn"
                + "Jhd0FkYXB0aXZlRGVmaW5pdGlvbiI6MTB9LCJjdXJyZW50VGltZVN0YW1w"
                + "IjoxNjg2ODgzMzYwLCJnaG9zdFdhdGVybWFya0luZm8iOnsidGV4dCI6I"
                + "mdob3N0IGlzIHdhdGNoaW5nIn19.0G2o4P5xVZ7zF"
                + "lFUgBLntfX03iGxK9ntD_AONClUUno";
        list.add(model);

        return list;
    }

    public ArrayList<VideoModel> loadCircleVodList() {
        VideoModel model;
        model = new VideoModel();
        model.appid = mAppId;
        model.fileid = "387702299774211080";
        model.title = getTitleByFileId(model);
        model.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/48888812387702299774211080/coverBySnapshot/coverBySnapshot_10_0.jpg";
        ArrayList<VideoModel> list = new ArrayList<>();
        list.add(model);

        model = new VideoModel();
        model.appid = mAppId;
        model.fileid = "387702299774644824";
        model.title = getTitleByFileId(model);
        model.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/52153a82387702299774644824/coverBySnapshot/coverBySnapshot_10_0.jpg";
        list.add(model);

        model = new VideoModel();
        model.appid = mAppId;
        model.fileid = "387702299774544650";
        model.title = getTitleByFileId(model);
        model.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/4fc009be387702299774544650/coverBySnapshot/coverBySnapshot_10_0.jpg";
        list.add(model);
        return list;
    }

    public ArrayList<VideoModel> loadCacheVodList() {
        VideoModel model = new VideoModel();
        model.appid = 1500005830;
        model.fileid = "387702299773851453";
        model.title = getTitleByFileId(model);
        model.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/09d5b1bf387702299773851453/coverBySnapshot/coverBySnapshot_10_0.jpg";
        model.isEnableDownload = true;
        ArrayList<VideoModel> list = new ArrayList<>();
        list.add(model);

        model = new VideoModel();
        model.appid = 1500005830;
        model.fileid = "387702299774155981";
        model.title = getTitleByFileId(model);
        model.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/467e1943387702299774155981/coverBySnapshot/coverBySnapshot_10_0.jpg";
        model.isEnableDownload = true;
        list.add(model);

        model = new VideoModel();
        model.appid = 1500005830;
        model.fileid = "387702299773830943";
        model.title = getTitleByFileId(model);
        model.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/09b10980387702299773830943/coverBySnapshot/coverBySnapshot_10_0.jpg";
        model.isEnableDownload = true;
        list.add(model);

        model = new VideoModel();
        model.appid = 1500005830;
        model.fileid = "387702299773823860";
        model.title = getTitleByFileId(model);
        model.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/09a09220387702299773823860/coverBySnapshot/coverBySnapshot_10_0.jpg";
        model.isEnableDownload = true;
        list.add(model);

        model = new VideoModel();
        model.appid = 1500005830;
        model.fileid = "387702299774156604";
        model.title = getTitleByFileId(model);
        model.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/467e97dc387702299774156604/coverBySnapshot/coverBySnapshot_10_0.jpg";
        model.isEnableDownload = true;
        list.add(model);
        return list;
    }

    public List<VideoModel> loadDrmVodList() {
        final ArrayList<VideoModel> list = new ArrayList<>();

        VideoModel model = new VideoModel();
        model.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/4ff64b01387702299774574470/387702304138941858.png";
        model.drmBuilder = new TXPlayerDrmBuilder();
        model.title = mContext.getString(R.string.super_play_drm_video);
        model.drmBuilder.setPlayUrl("https://1500017640.vod2.myqcloud.com/439767a2vodtranscq1500017640/30eb640e243791578648828779/adp.1434415.mpd");
        model.drmBuilder.setKeyLicenseUrl("https://widevine.drm.vod-qcloud.com/widevine/getlicense/v2?drmToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9~eyJ0eXBlIjoiRHJtVG9rZW4iLCJhcHBJZCI6MTUwMDAxNzY0MCwiZmlsZUlkIjoiMjQzNzkxNTc4NjQ4ODI4Nzc5IiwiY3VycmVudFRpbWVTdGFtcCI6MCwiZXhwaXJlVGltZVN0YW1wIjoyMTQ3NDgzNjQ3LCJyYW5kb20iOjAsIm92ZXJsYXlLZXkiOiIiLCJvdmVybGF5SXYiOiIiLCJjaXBoZXJlZE92ZXJsYXlLZXkiOiIiLCJjaXBoZXJlZE92ZXJsYXlJdiI6IiIsImtleUlkIjowLCJzdHJpY3RNb2RlIjowLCJwZXJzaXN0ZW50IjoiT04iLCJyZW50YWxEdXJhdGlvbiI6MCwiZm9yY2VMMVRyYWNrVHlwZXMiOm51bGx9~bTRTEni3j96XeRa17olRo6KT_dvSNrjJCZQ4b7Wb-qw");
        model.isEnableDownload = true;

        list.add(model);
        return list;
    }

    public void getVideoListInfo(final List<VideoModel> videoModels, final boolean isCacheModel,
                                 final OnVodListLoadListener listener) {
        if (listener == null) {
            return;
        }
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                final VideoListModel videoListModel = new VideoListModel();
                videoListModel.videoModelList = videoModels;
                videoListModel.isEnableDownload = isCacheModel;
                listener.onSuccess(videoListModel);
            }
        });
    }

    public void getVodInfoOneByOne(List<VideoModel> videoModels, OnVodInfoLoadListener listener) {
        if (videoModels == null || videoModels.size() == 0) {
            return;
        }

        for (VideoModel model : videoModels) {
            getVodByFileId(model, listener);
        }
    }

    public void getVodByFileId(final VideoModel model, final OnVodInfoLoadListener listener) {
        if (listener == null) {
            return;
        }
        model.title = getTitleByFileId(model);
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onSuccess(model);
            }
        });
    }

    /**
     * Get the video title based on the video ID
     *
     * 根据视频ID 获取视频标题
     */
    private String getTitleByFileId(VideoModel model) {
        String fileId = model.fileid;
        String title = "";
        switch (fileId) {
            case "387702299774251236":
                title = mContext.getString(R.string.tencent_cloud_audio_and_video_achievements_title);
                break;
            case "387702299774544650":
                title = mContext.getString(R.string.tencent_cloud_audio_and_video_steady_title);
                break;
            case "387702299774644824":
                title = mContext.getString(R.string.tencent_cloud_audio_and_video_real_title);
                break;
            case "387702299774211080":
                title = mContext.getString(R.string.tencent_cloud_audio_and_video_complete_title);
                break;
            case "387702299774545556":
                title = mContext.getString(R.string.tencent_cloud_business_introduction_title);
                break;
            case "387702299774574470":
                title = mContext.getString(R.string.what_are_numbers_title);
                break;
            case "387702299774253670":
                title = mContext.getString(R.string.simplify_complexity_and_build_big_from_small_title);
                break;
            case "387702299774390972":
                title = mContext.getString(R.string.superplayer_dynamic_watermark_title);
                break;
            case "387702299773851453":
                title = String.format(mContext.getString(R.string.super_player_cache_video_title),1);
                break;
            case "387702299774155981":
                title = String.format(mContext.getString(R.string.super_player_cache_video_title),2);
                break;
            case "387702299773830943":
                title = String.format(mContext.getString(R.string.super_player_cache_video_title),3);
                break;
            case "387702299773823860":
                title = String.format(mContext.getString(R.string.super_player_cache_video_title),4);
                break;
            case "387702299774156604":
                title = String.format(mContext.getString(R.string.super_player_cache_video_title),5);
                break;
            default:
                title = model.title;
                break;
        }
        return title;
    }


    public static final class VideoInfoHolder {
        private final SharedPreferences sharedPreferences;
        private final SharedPreferences.Editor editor;

        private volatile static VideoInfoHolder INSTANCE;

        private VideoInfoHolder(Context context) {
            sharedPreferences = context.getSharedPreferences("video_cache_info", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }

        public static VideoInfoHolder getInstance(Context context) {
            if (INSTANCE == null) {
                synchronized (VideoInfoHolder.class) {
                    if (INSTANCE == null) {
                        INSTANCE = new VideoInfoHolder(context.getApplicationContext());
                    }
                }
            }
            return INSTANCE;
        }

        public void cache(String fileId, String title) {
            if (!TextUtils.isEmpty(fileId) && !TextUtils.isEmpty(title)) {
                editor.putString(fileId, title);
                editor.apply();
            }
        }

        public void cache(List<VideoModel> modelList) {
            if (modelList == null || modelList.isEmpty()) {
                return;
            }
            for (VideoModel model : modelList) {
                if (model == null || TextUtils.isEmpty(model.fileid) || TextUtils.isEmpty(model.title)) {
                    continue;
                }
                editor.putString(model.fileid, model.title);
            }
            editor.apply();
        }

        public String get(String fileId) {
            if (TextUtils.isEmpty(fileId)) {
                return "";
            }
            return sharedPreferences.getString(fileId, "");
        }
    }


    public interface OnVodInfoLoadListener {
        void onSuccess(VideoModel videoModel);

        void onFail(int errCode);
    }

    public interface OnVodListLoadListener {
        void onSuccess(VideoListModel videoListModel);

        void onFail(int errCode);
    }
}
