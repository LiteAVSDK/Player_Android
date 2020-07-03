package com.tencent.liteav.demo.player.webdata;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.tencent.liteav.demo.player.activity.SuperPlayerActivity;

import java.util.Map;

/**
 * 外部调起入口
 * URI 格式如下：
 * liteav://com.tencent.liteav.demo?from=wechat&target=superplayer&protocol=v4vodplay&data={"appId":"1400295357","fileId":"5285890803757278095","psign":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"}
 *  from： 来源，例如：微信、浏览器等；暂时不用，防止以后需要确定调起方
 *  target： 目标页面，主要用于跳转对应页面，例如：超级播放器页，直播等；
 *  data： 数据，目标页面所需数据，具体字段按目标页面所使用字段为准，以上仅为事例，最终会透传给目标页面
 */
public class WebDataParser {

    private static final String TAG = "WebDataParser";

    public interface Callback {
        void onSuccess(WebDataInfo webDataInfo);
        void onFailure(Exception e);
    }

    public static WebDataParser get() {
        return WebDataParser.Singleton.sSingleton;
    }

    private static class Singleton {
        private static WebDataParser sSingleton = new WebDataParser();
    }

    private WebDataParser() {
    }

    public WebDataInfo build(Uri uri) {
        return parse(uri);
    }

    public void start(Context context, WebDataInfo webDataInfo, Callback callback) {
        if (webDataInfo == null) {
            onFailure(callback, "Data invalid, please check the data.");
            return;
        }
        if (!webDataInfo.isLegal()) {
            onFailure(callback, "Data invalid, please check from, target and protocol.");
            return;
        }
        if (webDataInfo.getData() == null) {
            onFailure(callback, "Data invalid, please check the data.");
            return;
        }
        if (callback == null) {
            navigation(context, webDataInfo);
            return;
        }
        navigation(context, webDataInfo, callback);
    }

    private void onFailure(Callback callback, String message) {
        if (callback != null) {
            callback.onFailure(new Exception(message));
        }
    }

    private void navigation(Context context, WebDataInfo webDataInfo) {
        navigation(context, webDataInfo, null);
    }

    private void navigation(Context context, WebDataInfo webDataInfo, Callback callback) {
        try {
            Intent intent = assembleIntent(context, webDataInfo);
            if (intent != null) {
                context.startActivity(intent);
                if (callback != null) {
                    callback.onSuccess(webDataInfo);
                }
            } else {
                if (callback != null) {
                    callback.onFailure(new Exception("Target not found, please check target."));
                }
            }
        } catch (Exception e) {
            if (callback != null) {
                callback.onFailure(new Exception(e));
            }
        }
    }

    private WebDataInfo parse(Uri uri) {
        String from = uri.getQueryParameter(WebDataInfo.EXTRA_FROM);
        String target = uri.getQueryParameter(WebDataInfo.EXTRA_TARGET);
        String protocol = uri.getQueryParameter(WebDataInfo.EXTRA_PROTOCOL);
        String data = uri.getQueryParameter(WebDataInfo.EXTRA_DATA);
        WebDataInfo webDataInfo = new WebDataInfo(from, target, protocol);
        try {
            if (!TextUtils.isEmpty(data) && !"null".equals(data)) {
                Log.d(TAG, "data -> " + data);
                if (data.startsWith("\"") && data.endsWith("\"")) {
                    data = data.substring(1, data.length() - 1);
                }
                Log.d(TAG, "eData -> " + data);
                Map<String, String> parseData = new Gson().fromJson(data, Map.class);
                if (parseData != null && !parseData.isEmpty()) {
                    webDataInfo.setData(parseData);
                }
            }
        } catch (Exception e) {
            webDataInfo.setData(null);
            e.printStackTrace();
        }
        Log.d(TAG, "WebDataInfo -> " + webDataInfo);
        return webDataInfo;
    }

    private Intent assembleIntent(Context context, WebDataInfo webDataInfo) {
        if (!WebDataInfo.TARGET_SUPERPLAYER.equals(webDataInfo.getTarget())) {
            return null;
        }
        Intent intent = new Intent(context, SuperPlayerActivity.class);
        Map<String, String> data = webDataInfo.getData();
        if (data != null && !data.isEmpty()) {
            for (String key : data.keySet()) {
                intent.putExtra(key, data.get(key));
            }
        }
        intent.putExtra(WebDataInfo.EXTRA_FROM, webDataInfo.getFrom());
        intent.putExtra(WebDataInfo.EXTRA_PROTOCOL, webDataInfo.getProtocol());
        return intent;
    }
}
