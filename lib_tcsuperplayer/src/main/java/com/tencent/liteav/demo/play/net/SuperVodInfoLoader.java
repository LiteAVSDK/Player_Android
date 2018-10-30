package com.tencent.liteav.demo.play.net;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.play.SuperPlayerModel;
import com.tencent.liteav.demo.play.utils.PlayInfoResponseParser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by liyuejiao on 2018/7/3.
 * 超级播放器内部获取点播信息
 */

public class SuperVodInfoLoader {

    private static final String TAG = "SuperVodInfoLoader";
    private Handler mMainHandler;

    private boolean mIsHttps;
    private final String BASE_URL = "http://playvideo.qcloud.com/getplayinfo/v2";
    private final String BASE_URLS = "https://playvideo.qcloud.com/getplayinfo/v2";
    private OnVodInfoLoadListener mOnVodInfoLoadListener;

    public SuperVodInfoLoader() {
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    public void setOnVodInfoLoadListener(OnVodInfoLoadListener listener) {
        mOnVodInfoLoadListener = listener;
    }

    public void getVodByFileId(final SuperPlayerModel model) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                String urlStr = makeUrlString(model.appid, model.fileid, null, null, -1, null);
                TCHttpURLClient.getInstance().get(urlStr, new TCHttpURLClient.OnHttpCallback() {
                    @Override
                    public void onSuccess(String result) {
                        parseJson(result);
                    }

                    @Override
                    public void onError() {
                        //获取请求信息失败
                        if (mOnVodInfoLoadListener != null) {
                            mOnVodInfoLoadListener.onFail(-1);
                        }
                    }
                });
            }
        });
    }

    private void parseJson(String content) {
        if (TextUtils.isEmpty(content)) {
            TXCLog.e(TAG, "parseJson err, content is empty!");
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(content);
            int code = jsonObject.getInt("code");
            if (code != 0) {
                String message = jsonObject.getString("message");
                TXCLog.e(TAG, message);
                return;
            }
            final PlayInfoResponseParser playInfoResponse = new PlayInfoResponseParser(jsonObject);
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mOnVodInfoLoadListener != null) {
                        mOnVodInfoLoadListener.onSuccess(playInfoResponse);
                    }
                }
            });
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
        void onSuccess(PlayInfoResponseParser response);

        void onFail(int errCode);
    }



}
