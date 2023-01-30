package com.tencent.liteav.demo.vodcommon.entity;

import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_MANUAL_PLAY;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;


import com.tencent.liteav.demo.superplayer.model.VipWatchModel;
import com.tencent.liteav.demo.superplayer.model.entity.DynamicWaterConfig;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;
import com.tencent.liteav.demo.superplayer.model.protocol.PlayInfoParserV2;
import com.tencent.liteav.demo.superplayer.model.protocol.PlayInfoParserV4;
import com.tencent.liteav.demo.vodcommon.R;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by liyuejiao on 2018/7/3.
 * 获取点播信息
 */

public class SuperVodListLoader {

    private static final String                M3U8_SUFFIX = ".m3u8";
    private static final String                TAG       = "SuperVodListLoader";
    private              Context               mContext;
    private              Handler               mHandler;
    private              HandlerThread         mHandlerThread;
    private              boolean               mIsHttps  = true;
    private final        String                BASE_URL  = "http://playvideo.qcloud.com/getplayinfo/v4";
    private final        String                BASE_URLS = "https://playvideo.qcloud.com/getplayinfo/v4";
    private              OnVodInfoLoadListener mOnVodInfoLoadListener;
    private              OkHttpClient          mHttpClient;
    private              int                   mAppId    = 1500005830;
    private              Object mLock = new Object();


    public SuperVodListLoader(Context context) {
        mHandlerThread = new HandlerThread("SuperVodListLoader");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mContext = context;
        mHttpClient = new OkHttpClient();
        mHttpClient.newBuilder().connectTimeout(5, TimeUnit.SECONDS);
    }

    public void setOnVodInfoLoadListener(OnVodInfoLoadListener listener) {
        mOnVodInfoLoadListener = listener;
    }

    public ArrayList<VideoModel> loadDefaultVodList(Context applicationContext) {
        ArrayList<VideoModel> list = new ArrayList<>();
        VideoModel model = new VideoModel();
        model.appid = mAppId;
        model.fileid = "387702299774251236";
        list.add(model);

        model = new VideoModel();
        model.appid = mAppId;
        model.fileid = "387702299774390972";
        model.title = applicationContext.getString(R.string.superplayer_dynamic_watermark_title);
        String tipStr = applicationContext.getString(R.string.superplayer_dynamic_watermark_tip);
        model.dynamicWaterConfig = new DynamicWaterConfig(tipStr, 30, Color.parseColor("#FF3333"));
        list.add(model);

        model = new VideoModel();
        model.appid = mAppId;
        model.fileid = "387702299774253670";
        list.add(model);

        model = new VideoModel();
        model.appid = mAppId;
        model.fileid = "387702299774574470";
        list.add(model);

        model = new VideoModel();
        model.appid = mAppId;
        model.fileid = "387702299774545556";
        model.title = applicationContext.getString(R.string.superplayer_vip_title);
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
        model.appid = 1252463788;
        model.fileid = "5285890781763144364";
        list.add(model);

        return list;
    }

    public ArrayList<VideoModel> loadCircleVodList() {
        VideoModel model;
        model = new VideoModel();
        model.appid = mAppId;
        model.fileid = "387702299774211080";
        ArrayList<VideoModel> list = new ArrayList<>();
        list.add(model);

        model = new VideoModel();
        model.appid = mAppId;
        model.fileid = "387702299774644824";
        list.add(model);

        model = new VideoModel();
        model.appid = mAppId;
        model.fileid = "387702299774544650";
        list.add(model);
        return list;
    }

    public ArrayList<VideoModel> loadCacheVodList() {
        VideoModel model = new VideoModel();
        model.appid = 1500005830;
        model.fileid = "387702299773851453";
        model.isEnableDownload = true;
        ArrayList<VideoModel> list = new ArrayList<>();
        list.add(model);

        model = new VideoModel();
        model.appid = 1500005830;
        model.fileid = "387702299774155981";
        model.isEnableDownload = true;
        list.add(model);

        model = new VideoModel();
        model.appid = 1500005830;
        model.fileid = "387702299773830943";
        model.isEnableDownload = true;
        list.add(model);

        model = new VideoModel();
        model.appid = 1500005830;
        model.fileid = "387702299773823860";
        model.isEnableDownload = true;
        list.add(model);

        model = new VideoModel();
        model.appid = 1500005830;
        model.fileid = "387702299774156604";
        model.isEnableDownload = true;
        list.add(model);
        return list;
    }

    public void getBatchVodList(final OnVodListLoadListener listener) {
        ArrayList<VideoModel> circleModels = loadCircleVodList();
        ArrayList<VideoModel> cacheModels = loadCacheVodList();

        getVideoListInfo(circleModels, false, listener);
        getVideoListInfo(cacheModels, true, listener);
    }

    public void getVideoListInfo(final ArrayList<VideoModel> videoModels, final boolean isCacheModel,
                                 final OnVodListLoadListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final int loadSize = videoModels.size();
                final AtomicInteger integer = new AtomicInteger(0);
                for (VideoModel model : videoModels) {
                    getVodByFileId(model, new OnVodInfoLoadListener() {
                        @Override
                        public void onSuccess(VideoModel videoModel) {
                            synchronized (mLock) {
                                integer.getAndAdd(1);
                                if (integer.get() == loadSize) {
                                    VideoListModel videoListModel = new VideoListModel();
                                    videoListModel.videoModelList = videoModels;
                                    videoListModel.isEnableDownload = isCacheModel;
                                    listener.onSuccess(videoListModel);
                                }
                            }
                        }

                        @Override
                        public void onFail(int errCode) {
                            listener.onFail(-1);
                        }
                    });
                }
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
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String urlStr = makeUrlString(model.appid, model.fileid, model.pSign);
                Request request = new Request.Builder().url(urlStr).build();
                Call call = mHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //获取请求信息失败
                        listener.onFail(-1);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String content = response.body().string();
                        parseJson(model, content, listener);
                    }
                });
            }
        });
    }

    public void getLiveList(final OnListLoadListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String urlStr = "http://xzb.qcloud.com/get_live_list2";

                OkHttpClient okHttpClient = new OkHttpClient();
                okHttpClient.newBuilder().connectTimeout(5, TimeUnit.SECONDS);
                Request request = new Request.Builder().url(urlStr).build();
                Call call = okHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //获取请求信息失败
                        if (listener != null) {
                            listener.onFail(-1);
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String content = response.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(content);
                            int code = jsonObject.getInt("code");
                            if (code != 200) {
                                String message = jsonObject.getString("message");
                                if (listener != null) {
                                    listener.onFail(-1);
                                }
                                Log.e(TAG, message);
                                return;
                            }
                            JSONObject data = jsonObject.optJSONObject("data");
                            JSONArray liveList = data.getJSONArray("list");
                            ArrayList<VideoModel> modelList = new ArrayList<>();
                            for (int i = 0; i < liveList.length(); i++) {
                                JSONObject playItem = liveList.optJSONObject(i);
                                VideoModel model = new VideoModel();
                                model.appid = playItem.optInt("appId", 0);
                                model.title = playItem.optString("name", "");
                                model.placeholderImage = playItem.optString("coverUrl", "");
                                JSONArray urlList = playItem.getJSONArray("playUrl");
                                if (urlList.length() > 0) {
                                    model.multiVideoURLs = new ArrayList<>(urlList.length());
                                    model.playDefaultIndex = 0;
                                    model.videoURL = urlList.optJSONObject(model.playDefaultIndex).optString("url", "");
                                    for (int j = 0; j < urlList.length(); j++) {
                                        JSONObject urlItem = urlList.optJSONObject(j);
                                        model.multiVideoURLs.add(new VideoModel.VideoPlayerURL(urlItem.optString("title", ""), urlItem.optString("url", "")));
                                    }
                                }

                                modelList.add(model);
                            }
                            if (listener != null) {
                                listener.onSuccess(modelList);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void parseJson(VideoModel videoModel, String content, OnVodInfoLoadListener listener) {
        if (TextUtils.isEmpty(content)) {
            Log.e(TAG, "parseJson err, content is empty!");
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(content);
            int code = jsonObject.getInt("code");
            if (code != 0) {
                String message = jsonObject.getString("message");
                listener.onFail(-1);
                Log.e(TAG, message);
                return;
            }

            int version = jsonObject.getInt("version");
            if (version == 2) {
                PlayInfoParserV2 parserV2 = new PlayInfoParserV2(jsonObject);

                videoModel.placeholderImage = parserV2.getCoverUrl();
                videoModel.duration = parserV2.getDuration();
                upDataTitle(videoModel, parserV2.getName());

                String url = parserV2.getURL();
                if ((url != null && url.contains(M3U8_SUFFIX)) || parserV2.getVideoQualityList().isEmpty()) {
                    videoModel.videoURL = url;
                    if (videoModel.multiVideoURLs != null) {
                        videoModel.multiVideoURLs.clear();
                    }
                } else {
                    videoModel.videoURL = null;
                    videoModel.multiVideoURLs = new ArrayList<>();
                    List<VideoQuality> videoQualityList = parserV2.getVideoQualityList();
                    Collections.sort(videoQualityList); // 码率从高到底排序
                    for (int i = 0; i < videoQualityList.size(); i++) {
                        VideoQuality videoQuality = videoQualityList.get(i);
                        videoModel.multiVideoURLs.add(
                                new VideoModel.VideoPlayerURL(videoQuality.title, videoQuality.url));
                    }
                    videoModel.videoQualityList.addAll(videoQualityList);
                    if (parserV2.getDefaultVideoQuality() != null &&  parserV2.getDefaultVideoQuality().index >= 0) {
                        videoModel.playDefaultIndex = parserV2.getDefaultVideoQuality().index;
                    }
                }
            } else if (version == 4) {
                PlayInfoParserV4 parserV4 = new PlayInfoParserV4(jsonObject);
                if (TextUtils.isEmpty(parserV4.getDRMType())) {
                    videoModel.videoURL = parserV4.getURL();
                }
                String title = parserV4.getDescription();
                if (TextUtils.isEmpty(title)) {
                    title = parserV4.getName();
                }
                upDataTitle(videoModel, title);
                videoModel.placeholderImage = parserV4.getCoverUrl();
                videoModel.duration = parserV4.getDuration();
            }
            videoModel.title = getTitleByFileId(videoModel);
            listener.onSuccess(videoModel);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据视频ID 获取视频标题
     *
     * @param model
     * @return
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

    private void upDataTitle(VideoModel videoModel, String newTitle) {
        if (TextUtils.isEmpty(videoModel.title)) {
            videoModel.title = newTitle;
        }
    }

    /**
     * 拼装协议请求url
     *
     * @return 协议请求url字符串
     */
    private String makeUrlString(int appId, String fileId, String pSign) {
        String urlStr;
        if (mIsHttps) {
            // 默认用https
            urlStr = String.format("%s/%d/%s", BASE_URLS, appId, fileId);
        } else {
            urlStr = String.format("%s/%d/%s", BASE_URL, appId, fileId);
        }
        String query = makeQueryString(null, pSign, null);
        if (query != null) {
            urlStr = urlStr + "?" + query;
        }
        return urlStr;
    }

    /**
     * 拼装协议请求url中的query字段
     *
     * @return query字段字符串
     */
    private String makeQueryString(String pcfg, String psign, String content) {
        StringBuilder str = new StringBuilder();
        if (!TextUtils.isEmpty(pcfg)) {
            str.append("pcfg=" + pcfg + "&");
        }

        if (!TextUtils.isEmpty(psign)) {
            str.append("psign=" + psign + "&");
        }

        if (!TextUtils.isEmpty(content)) {
            str.append("context=" + content + "&");
        }
        if (str.length() > 1) {
            str.deleteCharAt(str.length() - 1);
        }
        return str.toString();
    }

    public interface OnVodInfoLoadListener {
        void onSuccess(VideoModel videoModel);

        void onFail(int errCode);
    }


    public interface OnListLoadListener {
        void onSuccess(List<VideoModel> videoModels);

        void onFail(int errCode);
    }


    public interface OnVodListLoadListener {
        void onSuccess(VideoListModel videoListModel);

        void onFail(int errCode);
    }
}
