package com.tencent.liteav.demo.play.v3;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.play.bean.TCPlayImageSpriteInfo;
import com.tencent.liteav.demo.play.bean.TCPlayKeyFrameDescInfo;
import com.tencent.liteav.demo.play.net.TCHttpURLClient;
import com.tencent.liteav.demo.play.utils.PlayInfoResponseParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by hans on 2019/3/25.
 */

public class SuperVodInfoLoaderV3 {
    private static final String TAG = "SuperVodInfoLoaderV3";
    private Handler mMainHandler;

    private final String BASE_URL_V2 = "http://playvideo.qcloud.com/getplayinfo/v2";
    private final String BASE_URLS_V2 = "https://playvideo.qcloud.com/getplayinfo/v2";
    private final String BASE_URL_V3 = "http://playvideo.qcloud.com/getplayinfo/v3";
    private final String BASE_URLS_V3 = "https://playvideo.qcloud.com/getplayinfo/v3";

    //    private final String BASE_URLS_V3 = "https://adapter.vod.myqcloud.com/getplayinfo/v3";// TODO:测试地址
    public SuperVodInfoLoaderV3() {
        mMainHandler = new Handler(Looper.getMainLooper());
    }


    public void getVodByFileId(final SuperPlayerModelWrapper model, final SuperVodInfoLoaderV3.OnVodInfoLoadListener listener) {
        final SuperPlayerVideoId videoId = model.requestModel.videoId;
        if (videoId != null) {
            String urlStr = makeUrlString(model);
            Log.i(TAG, "getVodByFileId: url = " + urlStr);
            TCHttpURLClient.getInstance().get(urlStr, new TCHttpURLClient.OnHttpCallback() {
                @Override
                public void onSuccess(String result) {
                    Log.i(TAG, "onSuccess:  result = " + result);
//                    if (videoId.version == SuperPlayerVideoId.FILE_ID_V3) {
//                        parseJsonV3(model, result, listener);
//                    } else {
                    parseJsonV2(model, result, listener);
//                    }
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSuccess(model);
                        }
                    });
                }

                @Override
                public void onError() {
                    //获取请求信息失败
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onFail(-1, "http request error.");
                            }
                        }
                    });
                }
            });
        }
    }

    private void parseJsonV3(SuperPlayerModelWrapper model, String result, final SuperVodInfoLoaderV3.OnVodInfoLoadListener listener) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            final int code = jsonObject.getInt("code");
            final String msg = jsonObject.optString("message");
            if (code == 0) {
                JSONObject mediaJObj = jsonObject.optJSONObject("mediaInfo");

                JSONObject basicInfo = mediaJObj.optJSONObject("basicInfo");
                if (basicInfo != null) {
                    String name = basicInfo.optString("name");
                    long size = basicInfo.optLong("size");
                    long duration = basicInfo.optLong("duration");
                    String coverUrl = basicInfo.optString("coverUrl");
                    String description = basicInfo.optString("description");

                    TCVideoInfo videoInfo = new TCVideoInfo();
                    videoInfo.videoName = name;
                    videoInfo.size = size;
                    videoInfo.duration = duration;
                    videoInfo.coverUrl = coverUrl;
                    videoInfo.description = description;

                    model.videoInfo = videoInfo;
                }

                JSONObject dynamicStreamingInfo = mediaJObj.optJSONObject("dynamicStreamingInfo");
                if (dynamicStreamingInfo != null) {
                    JSONArray adaptiveStreamingInfoList = dynamicStreamingInfo.getJSONArray("adaptiveStreamingInfoList");
                    if (adaptiveStreamingInfoList != null && adaptiveStreamingInfoList.length() > 0) {
                        model.adaptiveStreamingInfoList = new ArrayList<>();

                        for (int i = 0; i < adaptiveStreamingInfoList.length(); i++) {
                            JSONObject jObj = adaptiveStreamingInfoList.optJSONObject(i);
                            int definition = jObj.getInt("definition");
                            String videoPackage = jObj.optString("package");
                            String drmType = jObj.optString("drmType");
                            String url = jObj.optString("url");
                            TCAdaptiveStreamingInfo info = new TCAdaptiveStreamingInfo();
                            info.definition = definition;
                            info.videoPackage = videoPackage;
                            info.drmType = drmType;
                            info.url = url;
                            model.adaptiveStreamingInfoList.add(info);
                        }
                    }
                }
                // 解析雪碧图
                JSONObject imageSpriteInfo = mediaJObj.optJSONObject("imageSpriteInfo");
                if (imageSpriteInfo != null) {
                    JSONArray imageSpriteList = imageSpriteInfo.getJSONArray("imageSpriteList");
                    if (imageSpriteList.length() > 0) {
                        model.imageInfo = new TCPlayImageSpriteInfo();
                        JSONObject imageSpriteObj = imageSpriteList.optJSONObject(0);// imageSpriteList返回的JSONARR，这里只处理第一个对象

                        JSONArray imageUrls = imageSpriteObj.getJSONArray("imageUrls");
                        if (imageUrls != null && imageUrls.length() > 0) {
                            model.imageInfo.imageUrls = new ArrayList<>();
                            for (int i = 0; i < imageUrls.length(); i++) {
                                String imagrURL = imageUrls.optString(i);
                                model.imageInfo.imageUrls.add(imagrURL);
                            }
                        }
                        model.imageInfo.webVttUrl = imageSpriteObj.optString("webVttUrl");
                    }
                }

                // 解析描述信息
                JSONObject keyFrameDescInfo = mediaJObj.optJSONObject("keyFrameDescInfo");
                if (keyFrameDescInfo != null) {
                    model.keyFrameDescInfos = new ArrayList<>();
                    JSONArray keyFrameDescList = keyFrameDescInfo.getJSONArray("keyFrameDescList");
                    if (keyFrameDescList != null && keyFrameDescList.length() > 0) {
                        for (int i = 0; i < keyFrameDescList.length(); i++) {
                            JSONObject jObj = keyFrameDescList.optJSONObject(i);
                            String content = jObj.optString("content");
                            long time = jObj.optLong("timeOffset");

                            TCPlayKeyFrameDescInfo info = new TCPlayKeyFrameDescInfo();
                            info.content = content;
                            info.time = time;

                            model.keyFrameDescInfos.add(info);
                        }
                    }
                }
            } else {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFail(code, msg);
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    listener.onFail(-1, "server error");
                }
            });
        }
    }


    private void parseJsonV2(SuperPlayerModelWrapper model, String content, final OnVodInfoLoadListener listener) {
        if (TextUtils.isEmpty(content)) {
            TXCLog.e(TAG, "parseJsonV2 err, content is empty!");
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(content);
            final int code = jsonObject.getInt("code");
            final String message = jsonObject.optString("message");
            TXCLog.e(TAG, message);
            if (code == 0) {
                final PlayInfoResponseParser playInfoResponse = new PlayInfoResponseParser(jsonObject);
                model.playInfoResponseParser = playInfoResponse;

                TCVideoInfo info = new TCVideoInfo();
                info.videoName = playInfoResponse.name();
                model.videoInfo = info;
            } else {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFail(code, message);
                    }
                });

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String makeUrlString(SuperPlayerModelWrapper modelWrapper) {
//        boolean isV3 = modelWrapper.requestModel.videoId.version == SuperPlayerVideoId.FILE_ID_V3;
//        String urlStr = String.format(isV3 ? "%s/%d/%s/%s" : "%s/%d/%s", isV3 ? BASE_URLS_V3 : BASE_URLS_V2,  modelWrapper.requestModel.appId,  modelWrapper.requestModel.videoId.fileId,  modelWrapper.requestModel.videoId.playDefinition);
//        String query = makeQueryString(modelWrapper.requestModel.videoId.timeout,  modelWrapper.requestModel.videoId.us,  modelWrapper.requestModel.videoId.exper,  modelWrapper.requestModel.videoId.sign, modelWrapper.requestModel.videoId.rlimit);


        String urlStr = String.format("%s/%d/%s", BASE_URLS_V2, modelWrapper.requestModel.appId, modelWrapper.requestModel.videoId.fileId);
        String query = makeQueryString(modelWrapper.requestModel.videoId.timeout, modelWrapper.requestModel.videoId.us, modelWrapper.requestModel.videoId.exper, modelWrapper.requestModel.videoId.sign, 0);
        if (query != null) {
            urlStr = urlStr + "?" + query;
        }
        return urlStr;
    }

    private String makeQueryString(String timeout, String us, int exper, String sign, int rlimit) {
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
        if (rlimit > 0) {
            str.append("rlimit=" + rlimit + "&");
        }
        if (str.length() > 1) {
            str.deleteCharAt(str.length() - 1);
        }
        return str.toString();
    }

    public interface OnVodInfoLoadListener {
        void onSuccess(SuperPlayerModelWrapper baseModel);

        void onFail(int errCode, String message);
    }


    private void runOnMainThread(Runnable r) {
        if (Looper.myLooper() == mMainHandler.getLooper()) {
            r.run();
        } else {
            mMainHandler.post(r);
        }
    }
}
