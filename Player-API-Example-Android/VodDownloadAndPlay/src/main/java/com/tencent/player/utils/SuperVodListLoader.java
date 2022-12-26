package com.tencent.player.utils;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.telecom.Call;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.player.entity.VideoListModel;
import com.tencent.player.entity.VideoModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    private OkHttpClient mHttpClient;
    private              int                   mAppId    = 1500005830;

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
                final AtomicInteger integer = new AtomicInteger(1);
                for (VideoModel model : videoModels) {
                    getVodByFileId(model, new OnVodInfoLoadListener() {
                        @Override
                        public void onSuccess(VideoModel videoModel) {
                            integer.getAndAdd(1);
                            if (integer.get() == loadSize) {
                                VideoListModel videoListModel = new VideoListModel();
                                videoListModel.videoModelList = videoModels;
                                videoListModel.isEnableDownload = isCacheModel;
                                listener.onSuccess(videoListModel);
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

    public void getVodByFileId(final VideoModel model, final OnVodInfoLoadListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String urlStr = makeUrlString(model.appid, model.fileid, model.pSign);
                Request request = new Request.Builder().url(urlStr).build();
                okhttp3.Call call = mHttpClient.newCall(request);
                call.enqueue(new Callback() {

                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {
                        listener.onFail(-1);
                    }

                    @Override
                    public void onResponse(okhttp3.Call call, Response response) throws IOException {
                        String content = response.body().string();
                        parseJson(model, content, listener);
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
//            if (version == 2) {
//                PlayInfoParserV2 parserV2 = new PlayInfoParserV2(jsonObject);
//
//                videoModel.placeholderImage = parserV2.getCoverUrl();
//                videoModel.duration = parserV2.getDuration();
//                upDataTitle(videoModel, parserV2.getName());
//
//                String url = parserV2.getURL();
//                if ((url != null && url.contains(M3U8_SUFFIX)) || parserV2.getVideoQualityList().isEmpty()) {
//                    videoModel.videoURL = url;
//                    if (videoModel.multiVideoURLs != null) {
//                        videoModel.multiVideoURLs.clear();
//                    }
//                } else {
//                    videoModel.videoURL = null;
//                    videoModel.multiVideoURLs = new ArrayList<>();
//                    List<VideoQuality> videoQualityList = parserV2.getVideoQualityList();
//                    Collections.sort(videoQualityList); // 码率从高到底排序
//                    for (int i = 0; i < videoQualityList.size(); i++) {
//                        VideoQuality videoQuality = videoQualityList.get(i);
//                        videoModel.multiVideoURLs.add(
//                                new VideoModel.VideoPlayerURL(videoQuality.title, videoQuality.url));
//                    }
//                }
//                videoModel.title = getTitleByFileId(videoModel.fileid);
//                listener.onSuccess(videoModel);
//            } else if (version == 4) {
//                PlayInfoParserV4 parserV4 = new PlayInfoParserV4(jsonObject);
//                if (TextUtils.isEmpty(parserV4.getDRMType())) {
//                    videoModel.videoURL = parserV4.getURL();
//                }
//                String title = parserV4.getDescription();
//                if (TextUtils.isEmpty(title)) {
//                    title = parserV4.getName();
//                }
//                upDataTitle(videoModel, title);
//                videoModel.placeholderImage = parserV4.getCoverUrl();
//                videoModel.duration = parserV4.getDuration();
//                videoModel.title = getTitleByFileId(videoModel.fileid);
//                listener.onSuccess(videoModel);
//            }

        } catch (JSONException e) {
            e.printStackTrace();
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

    public interface OnVodListLoadListener {
        void onSuccess(VideoListModel videoListModel);

        void onFail(int errCode);
    }

}
