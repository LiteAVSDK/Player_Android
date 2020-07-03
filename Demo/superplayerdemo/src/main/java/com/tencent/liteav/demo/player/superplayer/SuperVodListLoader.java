package com.tencent.liteav.demo.player.superplayer;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.play.bean.TCPlayInfoStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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

    private static final String TAG = "SuperVodListLoader";
    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private boolean mIsHttps = true;
    private final String BASE_URL = "http://playvideo.qcloud.com/getplayinfo/v4";
    private final String BASE_URLS = "https://playvideo.qcloud.com/getplayinfo/v4";
    private OnVodInfoLoadListener mOnVodInfoLoadListener;

    private OkHttpClient mHttpClient;

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

    public ArrayList<VideoModel> loadDefaultVodList() {
        ArrayList<VideoModel> list = new ArrayList<>();
        VideoModel model = new VideoModel();
        model.appid = 1252463788;
        model.fileid = "5285890781763144364";
        list.add(model);

        model = new VideoModel();
        model.appid = 1252463788;
        model.fileid = "4564972819220421305";
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
        list.add(model);

        return list;
    }

    public void getVodInfoOneByOne(ArrayList<VideoModel> videoModels) {
        if (videoModels == null || videoModels.size() == 0) {
            return;
        }

        for (VideoModel model : videoModels) {
            getVodByFileId(model);
        }
    }

    public void getVodByFileId(final VideoModel model) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String urlStr = makeUrlString(model.appid, model.fileid, null, null, -1, null);
                Request request = new Request.Builder().url(urlStr).build();
                Call call = mHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //获取请求信息失败
                        if (mOnVodInfoLoadListener != null) {
                            mOnVodInfoLoadListener.onFail(-1);
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String content = response.body().string();
                        parseJson(model, content);
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
                                TXCLog.e(TAG, message);
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
                                    model.videoURL = urlList.getJSONObject(0).optString("url", "");
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

    private void parseJson(VideoModel videoModel, String content) {
        if (TextUtils.isEmpty(content)) {
            TXCLog.e(TAG, "parseJson err, content is empty!");
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(content);
            int code = jsonObject.getInt("code");
            if (code != 0) {
                String message = jsonObject.getString("message");
                if (mOnVodInfoLoadListener != null) {
                    mOnVodInfoLoadListener.onFail(-1);
                }
                TXCLog.e(TAG, message);
                return;
            }
            PlayInfoResponseParser playInfoResponse = new PlayInfoResponseParser(jsonObject);
            videoModel.placeholderImage = playInfoResponse.coverUrl();

            TCPlayInfoStream stream = playInfoResponse.getSource();
            if (stream != null) {
                videoModel.duration = stream.getDuration();
            }
            videoModel.title = playInfoResponse.description();
            if (videoModel.title == null || videoModel.title.length() == 0) {
                videoModel.title = playInfoResponse.name();
            }
            if (mOnVodInfoLoadListener != null) {
                mOnVodInfoLoadListener.onSuccess(videoModel);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String makeUrlString(int appId, String fileId, String timeout, String us, int exper, String sign) {
        String urlStr;
        if (mIsHttps) {
            urlStr = String.format("%s/%d/%s", BASE_URL, appId, fileId);
        } else {
            urlStr = String.format("%s/%d/%s", BASE_URLS, appId, fileId);
        }
        String query = makeQueryString(timeout, us, exper, sign);
        if (query != null) {
            urlStr = urlStr + "?" + query;
        }
        return urlStr;
    }

    private String makeQueryString(String timeout, String us, int exper, String sign) {
        StringBuilder str = new StringBuilder();
        if (timeout != null) {
            str.append("t=" + timeout + "&");
        }
        if (us != null) {
            str.append("us=" + us + "&");
        }
        if (sign != null) {
            str.append("sign=" + sign + "&");
        }
        if (exper >= 0) {
            str.append("exper=" + exper + "&");
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
        void onSuccess(ArrayList<VideoModel> videoModels);

        void onFail(int errCode);
    }
}
