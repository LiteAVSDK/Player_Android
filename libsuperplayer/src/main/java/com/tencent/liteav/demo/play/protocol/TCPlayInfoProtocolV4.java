package com.tencent.liteav.demo.play.protocol;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.play.bean.TCPlayImageSpriteInfo;
import com.tencent.liteav.demo.play.bean.TCPlayKeyFrameDescInfo;
import com.tencent.liteav.demo.play.net.TCHttpURLClient;
import com.tencent.liteav.demo.play.bean.TCVideoQuality;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * V4视频信息协议实现类
 *
 * 负责V4视频信息协议的请求控制与数据获取
 */
public class TCPlayInfoProtocolV4 implements IPlayInfoProtocol {
    private static final String TAG = "TCPlayInfoProtocolV4";

    private final String BASE_URLS_V4 = "https://adapter.vod.myqcloud.com/getplayinfo/v4";  // V4协议请求地址
    //mock json api测试地址
//    private final String MOCK_URL = "http://www.mocky.io/v2/5dc91faa2f0000560073e9c0";
//    private final String MOCK_URL_HLS = "http://www.mocky.io/v2/5dc93c1b2f0000560073eae7";

    private Handler             mMainHandler;   // 用于切换线程

    private TCPlayInfoParams    mParams;        // 协议请求输入的参数
    private IPlayInfoParser     mParser;        // 协议请求返回Json的解析对象

    public TCPlayInfoProtocolV4(TCPlayInfoParams params) {
        mParams = params;
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 发送视频信息协议网络请求
     *
     * @param callback 协议请求回调
     */
    @Override
    public void sendRequest(final IPlayInfoRequestCallback callback) {
        if (mParams.fileId == null) {
            return;
        }
        String urlString = makeUrlString();
//        String urlString = MOCK_URL;
//        String urlString = MOCK_URL_HLS;
        TCHttpURLClient.getInstance().get(urlString, new TCHttpURLClient.OnHttpCallback() {
            @Override
            public void onSuccess(String result) {
                parseJson(result, callback);
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(TCPlayInfoProtocolV4.this, mParams);
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
     * 解析视频信息协议请求响应的Json数据
     *
     * @param content  响应Json字符串
     * @param callback 协议请求回调
     */
    private void parseJson(String content, final IPlayInfoRequestCallback callback) {
        if (TextUtils.isEmpty(content)) {
            TXCLog.e(TAG, "parseJson err, content is empty!");
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(content);
            final int code = jsonObject.getInt("code");
            final String message = jsonObject.optString("message");
            TXCLog.e(TAG, message);
            if (code == 0) {
                int version = jsonObject.getInt("version");
                if (version == 2) {
                    mParser = new TCPlayInfoParserV2(jsonObject);
                } else if (version == 4) {
                    mParser = new TCPlayInfoParserV4(jsonObject);
                }
            } else {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(code, message);
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
            TXCLog.e(TAG, "parseJson err");
        }
    }

    /**
     * 拼装协议请求url
     *
     * @return 协议请求url字符串
     */
    private String makeUrlString() {
        String urlStr = String.format("%s/%d/%s", BASE_URLS_V4, mParams.appId, mParams.fileId);
        String query = makeQueryString(mParams.timeout, mParams.us, mParams.exper, mParams.sign);
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
            str.append("exper=" + exper);
        }
        return str.toString();
    }

    /**
     * 中途取消请求
     */
    @Override
    public void cancelRequest() {

    }

    /**
     * 获取视频播放url
     *
     * @return 视频播放url字符串
     */
    @Override
    public String getUrl() {
        return mParser == null ? null : mParser.getUrl();
    }

    /**
     * 获取视频名称
     *
     * @return 视频名称字符串
     */
    @Override
    public String getName() {
        return mParser == null ? null : mParser.getName();
    }

    /**
     * 获取略缩图信息
     *
     * @return 略缩图信息对象
     */
    @Override
    public TCPlayImageSpriteInfo getImageSpriteInfo() {
        return mParser == null ? null : mParser.getImageSpriteInfo();
    }

    /**
     * 获取关键帧信息
     *
     * @return 关键帧信息数组
     */
    @Override
    public List<TCPlayKeyFrameDescInfo> getKeyFrameDescInfo() {
        return mParser == null ? null : mParser.getKeyFrameDescInfo();
    }

    /**
     * 获取画质信息
     *
     * @return 画质信息数组
     */
    @Override
    public List<TCVideoQuality> getVideoQualityList() {
        return mParser == null ? null : mParser.getVideoQualityList();
    }

    /**
     * 获取默认画质
     *
     * @return 默认画质信息对象
     */
    @Override
    public TCVideoQuality getDefaultVideoQuality() {
        return mParser == null ? null : mParser.getDefaultVideoQuality();
    }

    /**
     * 切换到主线程
     *
     * 从视频协议请求回调的子线程切换回主线程
     *
     * @param r 需要在主线程中执行的任务
     */
    private void runOnMainThread(Runnable r) {
        if (Looper.myLooper() == mMainHandler.getLooper()) {
            r.run();
        } else {
            mMainHandler.post(r);
        }
    }
}
