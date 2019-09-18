package com.tencent.liteav.demo.play.superplayer;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.play.SuperPlayerModel;
import com.tencent.liteav.demo.play.SuperPlayerUrl;
import com.tencent.liteav.demo.play.bean.TCPlayInfoStream;
import com.tencent.liteav.demo.play.utils.PlayInfoResponseParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private static SuperVodListLoader sInstance;
    private AtomicBoolean mLoadDemoList;
    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private boolean mIsHttps = true;
    private final String BASE_URL = "http://playvideo.qcloud.com/getplayinfo/v2";
    private final String BASE_URLS = "https://playvideo.qcloud.com/getplayinfo/v2";
    private OnVodInfoLoadListener mOnVodInfoLoadListener;
    private ArrayList<SuperPlayerModel> mDefaultList;

    private OkHttpClient mHttpClient;

    public SuperVodListLoader() {
        mHandlerThread = new HandlerThread("SuperVodListLoader");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        mDefaultList = new ArrayList();

        mHttpClient = new OkHttpClient();
        mHttpClient.newBuilder().connectTimeout(5, TimeUnit.SECONDS);

        mLoadDemoList = new AtomicBoolean(false);
    }

//    public static SuperVodListLoader getInstance() {
//        if (sInstance == null) {
//            sInstance = new SuperVodListLoader();
//        }
//        return sInstance;
//    }

    public void setOnVodInfoLoadListener(OnVodInfoLoadListener listener) {
        mOnVodInfoLoadListener = listener;
    }

    public ArrayList<SuperPlayerModel> loadDefaultVodList() {
        mDefaultList.clear();
        SuperPlayerModel model = null;

        model = new SuperPlayerModel();
        model.appid = 1252463788;
        model.fileid = "5285890781763144364";
        mDefaultList.add(model);

        model = new SuperPlayerModel();
        model.appid = 1252463788;
        model.fileid = "4564972819220421305";
        mDefaultList.add(model);

        model = new SuperPlayerModel();
        model.appid = 1252463788;
        model.fileid = "4564972819219071568";
        mDefaultList.add(model);

        model= new SuperPlayerModel();
        model.appid = 1252463788;
        model.fileid = "4564972819219071668";
        mDefaultList.add(model);

        model = new SuperPlayerModel();
        model.appid = 1252463788;
        model.fileid = "4564972819219071679";
        mDefaultList.add(model);

        model = new SuperPlayerModel();
        model.appid = 1252463788;
        model.fileid = "4564972819219081699";
        mDefaultList.add(model);

        return mDefaultList;
    }

    public SuperPlayerModel loadDefaultLiveVideo() {
        SuperPlayerModel model1 = new SuperPlayerModel();
        model1.appid = 1253131631;
        model1.title = "游戏直播-支持时移播放，清晰度无缝切换";
        model1.placeholderImage = "http://xiaozhibo-10055601.file.myqcloud.com/coverImg.jpg";
        model1.videoURL = "http://5815.liveplay.myqcloud.com/live/5815_89aad37e06ff11e892905cb9018cf0d4.flv";

        model1.multiVideoURLs = new ArrayList<>(3);
        model1.multiVideoURLs.add(new SuperPlayerUrl("超清","http://5815.liveplay.myqcloud.com/live/5815_89aad37e06ff11e892905cb9018cf0d4.flv"));
        model1.multiVideoURLs.add(new SuperPlayerUrl("高清","http://5815.liveplay.myqcloud.com/live/5815_89aad37e06ff11e892905cb9018cf0d4_900.flv"));
        model1.multiVideoURLs.add(new SuperPlayerUrl("标清","http://5815.liveplay.myqcloud.com/live/5815_89aad37e06ff11e892905cb9018cf0d4_550.flv"));

        return model1;
    }
    public void getVodInfoOneByOne(ArrayList<SuperPlayerModel> superPlayerModels) {
        mLoadDemoList.set(true);
        if (superPlayerModels == null || superPlayerModels.size() == 0)
            return;

        for (SuperPlayerModel model : superPlayerModels) {
            getVodByFileId(model);
        }
    }

    // 加载默认列表仅一次
    public boolean isLoadDemoList(){
        return mLoadDemoList.get();
    }

    public void getVodByFileId(final SuperPlayerModel model) {
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
                String urlStr = "http://xzb.qcloud.com/get_live_list";

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
                            ArrayList<SuperPlayerModel> modelList = new ArrayList<>();
                            for(int i = 0; i < liveList.length(); i++) {
                                JSONObject playItem = liveList.getJSONObject(i);
                                SuperPlayerModel model = new SuperPlayerModel();
                                model.appid = playItem.optInt("appId",0);
                                model.title = playItem.optString("name","");
                                model.placeholderImage = playItem.optString("coverUrl","");

                                JSONArray urlList = playItem.getJSONArray("playUrl");
                                if (urlList.length() > 0) {

                                    model.multiVideoURLs = new ArrayList<>(urlList.length());
                                    model.videoURL = urlList.getJSONObject(0).optString("url","");
                                    for(int j = 0; j < urlList.length(); j++) {
                                        JSONObject urlItem = urlList.getJSONObject(j);
                                        model.multiVideoURLs.add(new SuperPlayerUrl(urlItem.optString("title",""),urlItem.optString("url","")));
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

    private void parseJson(SuperPlayerModel superPlayerModel, String content) {
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
            superPlayerModel.placeholderImage = playInfoResponse.coverUrl();

            TCPlayInfoStream stream = playInfoResponse.getSource();
            if (stream != null) {
                superPlayerModel.duration = stream.getDuration();
            }
            superPlayerModel.title = playInfoResponse.description();
            if (superPlayerModel.title == null || superPlayerModel.title.length() == 0) {
                superPlayerModel.title = playInfoResponse.name();
            }
            superPlayerModel.imageInfo = playInfoResponse.getImageSpriteInfo();
            superPlayerModel.keyFrameDescInfos = playInfoResponse.getKeyFrameDescInfos();
            if (mOnVodInfoLoadListener != null) {
                mOnVodInfoLoadListener.onSuccess(superPlayerModel);
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
        void onSuccess(SuperPlayerModel superPlayerModelList);

        void onFail(int errCode);
    }

    public interface OnListLoadListener {
        void onSuccess(ArrayList<SuperPlayerModel> superPlayerModelList);

        void onFail(int errCode);
    }
}
