package com.tencent.liteav.demo.player.demo.shortvideo.core;

import android.text.TextUtils;
import android.util.Log;

import com.tencent.liteav.demo.player.demo.shortvideo.bean.DataBeanParser;
import com.tencent.liteav.demo.player.demo.shortvideo.bean.ShortVideoBean;
import com.tencent.liteav.demo.player.demo.shortvideo.bean.SubStreamsDTO;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ShortVideoModel {
    private static final String TAG = "ShortVideoDemo:ShortVideoModel";
    private volatile static ShortVideoModel mInstance;
    private static final int APP_ID = 1500005830;
    private final String BASE_URL = "http://playvideo.qcloud.com/getplayinfo/v4";
    private final String BASE_URLS = "https://playvideo.qcloud.com/getplayinfo/v4";
    private final String BASE_URL_V2 = "http://playvideo.qcloud.com/getplayinfo/v2";
    private final String BASE_URLS_V2 = "https://playvideo.qcloud.com/getplayinfo/v2";
    private final String V2 = "v2";
    private final String V4 = "v4";
    private static final String[] FILE_IDS = new String[]{"387702294394366256", "387702294394228858",
            "387702294394228636", "387702294394228527", "387702294167066523",
            "387702294167066515", "387702294168748446", "387702294394227941"};

    private ArrayList<ShortVideoBean> source_list;
    private ArrayList<ShortVideoBean> data_list;
    private ExecutorService mExecutorService;
    private boolean mIsHttps = true;
    private OkHttpClient mHttpClient;
    private int mTotalSize;
    private IOnDataLoadFullListener mOnDataLoadFullListener;

    private ShortVideoModel() {
        mExecutorService = Executors.newSingleThreadExecutor();
        mHttpClient = new OkHttpClient();
        mHttpClient.newBuilder().connectTimeout(5, TimeUnit.SECONDS);
        source_list = new ArrayList<>();
        data_list = new ArrayList<>();
    }

    public static ShortVideoModel getInstance() {
        if (mInstance == null) {
            synchronized (ShortVideoModel.class) {
                if (mInstance == null) {
                    mInstance = new ShortVideoModel();
                }
            }
        }
        return mInstance;
    }

    public void release() {
        mInstance = null;
        mExecutorService.shutdown();
    }

    private int getFileIDSLength() {
        return FILE_IDS.length;
    }


    public void loadDefaultVideo() {
        source_list.clear();
        synchronized (data_list) {
            data_list.clear();
        }
        for (int i = 0; i < FILE_IDS.length; i++) {
            source_list.add(new ShortVideoBean(APP_ID, FILE_IDS[i], V4));
        }
    }


    public void setOnDataLoadFullListener(IOnDataLoadFullListener listener) {
        mOnDataLoadFullListener = listener;
    }

    public void getVideoByFileId() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                mTotalSize = 0;
                for (int i = 0, size = source_list.size(); i < size; i++) {
                    final ShortVideoBean model = source_list.get(i);
                    String urlStr = makeUrlString(model.appid, model.fileid, null, null, -1, null, model.appidType);
                    Request request = new Request.Builder().url(urlStr).build();
                    Call call = mHttpClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e(TAG, "onFailure");
                            //获取请求信息失败
                            checkIfReady();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String content = response.body().string();
                            parseJson(model, content);
                        }
                    });
                }
            }
        });
    }

    private void parseJson(ShortVideoBean videoModel, String content) {
        if (TextUtils.isEmpty(content)) {
            Log.e(TAG, "parseJson err, content is empty!");
            checkIfReady();
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(content);
            int code = jsonObject.getInt("code");
            if (code != 0) {
                String message = jsonObject.getString("message");
                Log.e(TAG, message + "");
                checkIfReady();
                return;
            }
            DataBeanParser dataBeanParser = new DataBeanParser(jsonObject);
            videoModel.placeholderImage = dataBeanParser.coverUrl();
            videoModel.duration = dataBeanParser.duration();
            videoModel.title = dataBeanParser.name();
            videoModel.videoURL = dataBeanParser.url();
            List<SubStreamsDTO> subStreamsDTOList = dataBeanParser.getSubStreamDTOArray();
            videoModel.bitRateIndex = findBitRateIndex(subStreamsDTOList);
            Log.i(TAG, "[parseJson] betterIndex " + videoModel.bitRateIndex);
            synchronized (data_list) {
                data_list.add(videoModel);
            }
            checkIfReady();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage() + "");
            checkIfReady();
        }
    }

    private int findBitRateIndex(List<SubStreamsDTO> subStreamsDTOList) {
        for (int i = subStreamsDTOList.size() - 1; i >= 0; i--) {
            if (subStreamsDTOList.get(i).type.equals("video")) {
                return i;
            }
        }
        return 0;
    }

    private String makeUrlString(int appId, String fileId, String timeout, String us, int exper, String sign, String appidType) {
        String urlStr;
        if (TextUtils.equals(appidType, V2)) {
            if (!mIsHttps) {
                urlStr = String.format("%s/%d/%s", BASE_URL_V2, appId, fileId);
            } else {
                urlStr = String.format("%s/%d/%s", BASE_URLS_V2, appId, fileId);
            }
        } else {
            if (!mIsHttps) {
                urlStr = String.format("%s/%d/%s", BASE_URL, appId, fileId);
            } else {
                urlStr = String.format("%s/%d/%s", BASE_URLS, appId, fileId);
            }
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

    private synchronized void checkIfReady() {
        mTotalSize++;
        Log.i(TAG, "mTotalSize" + mTotalSize);
        if (mTotalSize == getFileIDSLength()) {
            mOnDataLoadFullListener.onLoaded(data_list);
        }
    }

    public interface IOnDataLoadFullListener {
        void onLoaded(List<ShortVideoBean> videoBeanList);
    }
}
