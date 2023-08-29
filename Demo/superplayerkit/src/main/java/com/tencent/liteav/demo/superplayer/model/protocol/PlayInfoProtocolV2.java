package com.tencent.liteav.demo.superplayer.model.protocol;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.liteav.demo.superplayer.model.entity.PlayImageSpriteInfo;
import com.tencent.liteav.demo.superplayer.model.entity.PlayKeyFrameDescInfo;
import com.tencent.liteav.demo.superplayer.model.entity.ResolutionName;
import com.tencent.liteav.demo.superplayer.model.net.HttpURLClient;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * V2 video information protocol implementation class
 * <p>
 * Responsible for controlling the V2 video information protocol request and data retrieval
 *
 * V2视频信息协议实现类
 * <p>
 * 负责V2视频信息协议的请求控制与数据获取
 */
public class PlayInfoProtocolV2 implements IPlayInfoProtocol {

    private static final String          TAG          = "TCPlayInfoProtocolV2";
    private final        String          BASE_URLS_V2 = "https://playvideo.qcloud.com/getplayinfo/v2";
    private              Handler         mMainHandler;
    private              PlayInfoParams  mParams;
    private              IPlayInfoParser mParser;

    public PlayInfoProtocolV2(PlayInfoParams params) {
        mParams = params;
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Send a network request for video information protocol
     *
     * 发送视频信息协议网络请求
     */
    @Override
    public void sendRequest(final IPlayInfoRequestCallback callback) {
        if (mParams.fileId == null) {
            return;
        }
        String urlStr = makeUrlString();
        Log.i(TAG, "getVodByFileId: url = " + urlStr);
        HttpURLClient.getInstance().get(urlStr, new HttpURLClient.OnHttpCallback() {
            @Override
            public void onSuccess(String result) {
                Log.i(TAG, "http request success:  result = " + result);
                parseJson(result, callback);
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(PlayInfoProtocolV2.this, mParams);
                    }
                });
            }

            @Override
            public void onError() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onError(-1, "http request error.");
                        }
                    }
                });
            }
        });
    }

    /**
     * Assemble the protocol request URL
     *
     * 拼装协议请求url
     */
    private String makeUrlString() {
        String urlStr = String.format("%s/%d/%s", BASE_URLS_V2, mParams.appId, mParams.fileId);
        if (mParams.videoIdV2 != null) {
            String query = makeQueryString(mParams.videoIdV2.timeout, mParams.videoIdV2.us, mParams.videoIdV2.exper, mParams.videoIdV2.sign);
            if (query != null) {
                urlStr = urlStr + "?" + query;
            }
        }
        return urlStr;
    }

    /**
     * Assemble the query field in the protocol request URL
     *
     * 拼装协议请求url中的query字段
     *
     * @param timeout Encryption link timeout timestamp
     *                加密链接超时时间戳
     * @param us      Unique identifier for the request
     *                唯一标识请求
     * @param exper   Preview duration in seconds, decimal value
     *                试看时长，单位：秒，十进制数值
     * @param sign    Signature string
     *                签名字符串
     * @return Query field string.
     *         query字段字符串
     */
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

    /**
     * Cancel the request midway
     *
     * 中途取消请求
     */
    @Override
    public void cancelRequest() {

    }

    /**
     * Get the video playback URL
     *
     * 获取视频播放url
     */
    @Override
    public String getUrl() {
        return mParser == null ? null : mParser.getURL();
    }

    @Override
    public String getEncyptedUrl(PlayInfoConstant.EncryptedURLType type) {
        return mParser.getEncryptedURL(type);
    }

    @Override
    public String getToken() {
        return mParser.getToken();
    }

    /**
     * Get the video name
     *
     * 获取视频名称
     */
    @Override
    public String getName() {
        return mParser == null ? null : mParser.getName();
    }

    /**
     * Get the sprite information
     *
     * 获取雪碧图信息
     */
    @Override
    public PlayImageSpriteInfo getImageSpriteInfo() {
        return mParser == null ? null : mParser.getImageSpriteInfo();
    }

    /**
     * Get the keyframe information
     *
     * 获取关键帧信息
     */
    @Override
    public List<PlayKeyFrameDescInfo> getKeyFrameDescInfo() {
        return mParser == null ? null : mParser.getKeyFrameDescInfo();
    }

    /**
     * Get the video quality information
     *
     * 获取画质信息
     */
    @Override
    public List<VideoQuality> getVideoQualityList() {
        return mParser == null ? null : mParser.getVideoQualityList();
    }

    /**
     * Get the default video quality
     *
     * 获取默认画质
     */
    @Override
    public VideoQuality getDefaultVideoQuality() {
        return mParser == null ? null : mParser.getDefaultVideoQuality();
    }

    /**
     * Parse the JSON data of the video information protocol request response
     *
     * 解析视频信息协议请求响应的Json数据
     */
    private boolean parseJson(String content, final IPlayInfoRequestCallback callback) {
        if (TextUtils.isEmpty(content)) {
            Log.e(TAG, "parseJsonV2 err, content is empty!");
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    callback.onError(-1, "request return error!");
                }
            });
            return false;
        }
        try {
            JSONObject jsonObject = new JSONObject(content);
            final int code = jsonObject.getInt("code");
            final String message = jsonObject.optString("message");
            Log.e(TAG, message);
            if (code == 0) {
                mParser = new PlayInfoParserV2(jsonObject);
            } else {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(code, message);
                    }
                });
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "parseJson err");
        }
        return true;
    }

    /**
     * Switch to the main thread
     * <p>
     * Switch back to the main thread from the sub-thread of the video protocol request callback.
     *
     * 切换到主线程
     * <p>
     * 从视频协议请求回调的子线程切换回主线程
     *
     * @param r Tasks that need to be executed on the main thread
     *          需要在主线程中执行的任务
     */
    private void runOnMainThread(Runnable r) {
        if (Looper.myLooper() == mMainHandler.getLooper()) {
            r.run();
        } else {
            mMainHandler.post(r);
        }
    }

    /**
     * Get the video quality alias list
     *
     * 获取视频画质别名列表
     */
    @Override
    public List<ResolutionName> getResolutionNameList() {
        return mParser == null ? null : mParser.getResolutionNameList();
    }

    @Override
    public String getPenetrateContext() {
        return null;
    }

    @Override
    public String getDRMType() {
        return mParser != null ? mParser.getDRMType() : "";
    }
}
