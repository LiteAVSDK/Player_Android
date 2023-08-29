package com.tencent.liteav.demo.superplayer.model.protocol;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.liteav.demo.superplayer.model.entity.PlayImageSpriteInfo;
import com.tencent.liteav.demo.superplayer.model.entity.PlayKeyFrameDescInfo;
import com.tencent.liteav.demo.superplayer.model.entity.ResolutionName;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;
import com.tencent.liteav.demo.superplayer.model.net.HttpURLClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

/**
 * V4 video information protocol implementation class
 * <p>
 * Responsible for controlling the V4 video information protocol request and data retrieval
 *
 * V4视频信息协议实现类
 * <p>
 * 负责V4视频信息协议的请求控制与数据获取
 */
public class PlayInfoProtocolV4 implements IPlayInfoProtocol {
    private static final String          TAG          = "TCPlayInfoProtocolV4";
    private final        String          baseUrlsV4 = "https://playvideo.qcloud.com/getplayinfo/v4";
    private              Handler         mMainHandler;
    private              PlayInfoParams  mParams;
    private              IPlayInfoParser mParser;
    private              String          mRequestContext;

    public PlayInfoProtocolV4(PlayInfoParams params) {
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
        String urlString = makeUrlString();
        HttpURLClient.getInstance().get(urlString, new HttpURLClient.OnHttpCallback() {
            @Override
            public void onSuccess(String result) {
                boolean ret = parseJson(result, callback);
                if (ret) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(PlayInfoProtocolV4.this, mParams);
                        }
                    });
                }
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
     * Parse the JSON data of the video information protocol request response
     *
     * 解析视频信息协议请求响应的Json数据
     */
    private boolean parseJson(String content, final IPlayInfoRequestCallback callback) {
        if (TextUtils.isEmpty(content)) {
            Log.e(TAG, "parseJson err, content is empty!");
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
            final String warning = jsonObject.optString("warning");
            mRequestContext = jsonObject.optString("context");
            Log.i(TAG, "context : " + mRequestContext);
            Log.i(TAG, "message: " + message);
            Log.i(TAG, "warning: " + warning);
            if (code == 0) {
                int version = jsonObject.getInt("version");
                if (version == 2) {
                    mParser = new PlayInfoParserV2(jsonObject);
                } else if (version == 4) {
                    mParser = new PlayInfoParserV4(jsonObject);
                }
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
     * Assemble the protocol request URL
     *
     * 拼装协议请求url
     */
    private String makeUrlString() {
        String urlStr = String.format("%s/%d/%s", baseUrlsV4, mParams.appId, mParams.fileId);
        String psign = makeJWTSignature(mParams);
        String query = null;
        if (mParams.videoId != null) {
            query = makeQueryString(null, psign, null);
        }

        if (!TextUtils.isEmpty(query)) {
            urlStr = urlStr + "?" + query;
        }
        Log.d(TAG, "request url: " + urlStr);
        return urlStr;
    }

    public static String makeJWTSignature(PlayInfoParams params) {
        if (params.videoId != null && !TextUtils.isEmpty(params.videoId.pSign)) {
            return params.videoId.pSign;
        }
        return null;
    }


    /**
     * Assemble the query field in the protocol request URL
     *
     * 拼装协议请求url中的query字段
     */
    private String makeQueryString(String pcfg, String psign, String content) {
        StringBuilder str = new StringBuilder();
        // V4 protocol sub-version number, with a value of 1 indicating V4.1 version
        str.append("subversion=1" +  "&");
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

    /**
     * Get a 32-bit random string
     *
     * 获取32位随机字符串
     */
    private String genRandomHexString() {
        final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        int keyLen = 32;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyLen; i++) {
            char randomChar = HEX_ARRAY[new Random().nextInt(HEX_ARRAY.length)];
            sb.append(randomChar);
        }
        return sb.toString();
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
        return mParser == null ? null : mParser.getEncryptedURL(type);
    }

    @Override
    public String getToken() {
        return mParser == null ? null : mParser.getToken();
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
     * Switch to the main thread.
     * <p>
     * Switch back to the main thread from the sub-thread of the video protocol request callback.
     *
     * 切换到主线程
     * <p>
     * 从视频协议请求回调的子线程切换回主线程
     *
     * @param r Tasks that need to be executed on the main thread.
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
     * Get the video quality alias list.
     *
     * 获取视频画质别名列表
     */
    @Override
    public List<ResolutionName> getResolutionNameList() {
        return mParser == null ? null : mParser.getResolutionNameList();
    }

    @Override
    public String getPenetrateContext() {
        return mRequestContext;
    }

    @Override
    public String getDRMType() {
        return mParser != null ? mParser.getDRMType() : "";
    }
}
