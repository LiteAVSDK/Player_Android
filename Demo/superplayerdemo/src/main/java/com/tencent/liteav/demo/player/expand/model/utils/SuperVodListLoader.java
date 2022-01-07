package com.tencent.liteav.demo.player.expand.model.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.superplayer.model.VipWatchModel;
import com.tencent.liteav.demo.superplayer.model.entity.DynamicWaterConfig;
import com.tencent.liteav.demo.superplayer.model.entity.PlayInfoStream;
import com.tencent.liteav.demo.player.expand.model.entity.VideoModel;
import com.tencent.liteav.demo.superplayer.model.entity.PlayInfoStream;

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

import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_AUTO_PLAY;
import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_MANUAL_PLAY;

/**
 * Created by liyuejiao on 2018/7/3.
 * 获取点播信息
 */

public class SuperVodListLoader {

    private static final String                TAG       = "SuperVodListLoader";
    private              Handler               mHandler;
    private              HandlerThread         mHandlerThread;
    private              boolean               mIsHttps  = true;
    private final        String                BASE_URL  = "http://playvideo.qcloud.com/getplayinfo/v4";
    private final        String                BASE_URLS = "https://playvideo.qcloud.com/getplayinfo/v4";
    private              OnVodInfoLoadListener mOnVodInfoLoadListener;
    private              OkHttpClient          mHttpClient;

    public SuperVodListLoader() {
        mHandlerThread = new HandlerThread("SuperVodListLoader");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        mHttpClient = new OkHttpClient();
        mHttpClient.newBuilder().connectTimeout(5, TimeUnit.SECONDS);
    }

    public void setOnVodInfoLoadListener(OnVodInfoLoadListener listener) {
        mOnVodInfoLoadListener = listener;
    }

    public ArrayList<VideoModel> loadDefaultVodList(Context applicationContext) {
        ArrayList<VideoModel> list = new ArrayList<>();
        VideoModel model = new VideoModel();
        model.appid = 1252463788;
        model.fileid = "5285890781763144364";
        list.add(model);

        model = new VideoModel();
        model.appid = 1252463788;
        model.fileid = "4564972819220421305";
        model.title = applicationContext.getString(R.string.superplayer_dynamic_watermark_title);
        String tipStr = applicationContext.getString(R.string.superplayer_dynamic_watermark_tip);
        model.dynamicWaterConfig = new DynamicWaterConfig(tipStr, 30, Color.parseColor("#80FFFFFF"));
        list.add(model);

        model = new VideoModel();
        model.appid = 1252463788;
        model.fileid = "4564972819219071568";
        list.add(model);

        model = new VideoModel();
        model.appid = 1252463788;
        model.fileid = "4564972819219071668";
        list.add(model);

        model = new VideoModel();
        model.appid = 1252463788;
        model.fileid = "4564972819219071679";
        list.add(model);

        model = new VideoModel();
        model.appid = 1252463788;
        model.fileid = "4564972819219081699";
        model.title = applicationContext.getString(R.string.superplayer_vip_title);
        model.vipWatchModel = new VipWatchModel(applicationContext.getString(R.string.superplayer_vip_watch_tip), 15);
        list.add(model);

        model = new VideoModel();
        model.appid = 1500005830;
        model.playAction = PLAY_ACTION_MANUAL_PLAY;
        model.fileid = "8602268011437356984";
        model.title = applicationContext.getString(R.string.superplayer_cover_title);
        model.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/6c9a5118vodcq1500005830/cc1e28208602268011087336518/MXUW1a5I9TsA.png";
        list.add(model);
        return list;
    }

    public ArrayList<VideoModel> loadCircleVodList() {
        VideoModel model = new VideoModel();
        model = new VideoModel();
        model.appid = 1252463788;
        model.fileid = "4564972819219071568";
        ArrayList<VideoModel> list = new ArrayList<>();
        list.add(model);

        model = new VideoModel();
        model.appid = 1252463788;
        model.fileid = "4564972819219071679";
        list.add(model);

        model = new VideoModel();
        model.appid = 1252463788;
        model.fileid = "4564972819219071668";
        list.add(model);
        return list;
    }

    public void getBatchVodList(final ArrayList<VideoModel> videoModels, final OnListLoadListener listener) {
        if (videoModels == null || videoModels.size() == 0) {
            return;
        }
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
                                listener.onSuccess(videoModels);
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
                            JSONObject data = jsonObject.getJSONObject("data");
                            JSONArray liveList = data.getJSONArray("list");
                            ArrayList<VideoModel> modelList = new ArrayList<>();
                            for (int i = 0; i < liveList.length(); i++) {
                                JSONObject playItem = liveList.getJSONObject(i);
                                VideoModel model = new VideoModel();
                                model.appid = playItem.optInt("appId", 0);
                                model.title = playItem.optString("name", "");
                                model.placeholderImage = playItem.optString("coverUrl", "");
                                JSONArray urlList = playItem.getJSONArray("playUrl");
                                if (urlList.length() > 0) {
                                    model.multiVideoURLs = new ArrayList<>(urlList.length());
                                    model.playDefaultIndex = 0;
                                    model.videoURL = urlList.getJSONObject(model.playDefaultIndex).optString("url", "");
                                    for (int j = 0; j < urlList.length(); j++) {
                                        JSONObject urlItem = urlList.getJSONObject(j);
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
            // 解析视频基础信息，雪碧图，视频名称，播放时长等
            if (version == 2) {
                PlayInfoResponseParser playInfoResponse = new PlayInfoResponseParser(jsonObject);
                videoModel.placeholderImage = playInfoResponse.coverURL();
                PlayInfoStream stream = playInfoResponse.getSource();
                if (stream != null) {
                    videoModel.duration = stream.getDuration();
                }
                String title = playInfoResponse.description();
                if (TextUtils.isEmpty(title)) {
                    title = playInfoResponse.name();
                }
                upDataTitle(videoModel, title);
                listener.onSuccess(videoModel);
            } else if (version == 4) {
                JSONObject media = jsonObject.getJSONObject("media");
                if (media != null) {
                    JSONObject basicInfo = media.optJSONObject("basicInfo");
                    if (basicInfo != null) {
                        String title = basicInfo.optString("description");
                        if (TextUtils.isEmpty(title)) {
                            title = basicInfo.optString("name");
                        }
                        upDataTitle(videoModel, title);
                        videoModel.placeholderImage = basicInfo.optString("coverUrl");
                        videoModel.duration = basicInfo.optInt("duration");
                    }
                    listener.onSuccess(videoModel);
                    return;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
}
