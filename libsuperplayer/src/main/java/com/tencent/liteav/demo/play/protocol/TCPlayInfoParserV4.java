package com.tencent.liteav.demo.play.protocol;

import android.util.Log;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.play.bean.TCPlayImageSpriteInfo;
import com.tencent.liteav.demo.play.bean.TCPlayKeyFrameDescInfo;
import com.tencent.liteav.demo.play.bean.TCAdaptiveStreamingInfo;
import com.tencent.liteav.demo.play.bean.TCVideoQuality;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * V4视频协议解析实现类
 *
 * 负责解析V4视频信息协议请求响应的Json数据
 */
public class TCPlayInfoParserV4 implements IPlayInfoParser {
    private static final String TAG = "TCPlayInfoParserV4";

    private JSONObject                          mResponse;                  // 协议请求返回的Json数据

    private String                              mName;                      // 视频名称
    private String                              mUrl;                       // 视频播放url
    private List<TCAdaptiveStreamingInfo>       mAdaptiveStreamingInfoList; // 多码率视频信息
    private TCPlayImageSpriteInfo               mImageSpriteInfo;           // 略缩图信息
    private List<TCPlayKeyFrameDescInfo>        mKeyFrameDescInfo;          // 关键帧信息

    public TCPlayInfoParserV4(JSONObject response) {
        mResponse = response;
        parsePlayInfo();
    }

    /**
     * 从视频信息协议请求响应的Json数据中解析出视频信息
     */
    private void parsePlayInfo() {
        try {
            JSONObject media = mResponse.getJSONObject("media");
            if (media != null) {
                //解析视频名称
                JSONObject basicInfo = media.getJSONObject("basicInfo");
                if (basicInfo != null) {
                    mName = basicInfo.getString("mName");
                }
                //解析视频播放url
                JSONObject adaptiveDynamicStreamingInfo = media.getJSONObject("adaptiveDynamicStreamingInfo");
                if (adaptiveDynamicStreamingInfo != null) {
                    //未加密直接解析出视频url
                    mUrl = adaptiveDynamicStreamingInfo.getString("mUrl");
                    //有加密，url为空，则解析drm加密的url信息
                    if (mUrl == null) {
                        JSONArray drmUrls = adaptiveDynamicStreamingInfo.getJSONArray("drmUrls");
                        if (drmUrls != null && drmUrls.length() > 0) {
                            mAdaptiveStreamingInfoList = new ArrayList<>();
                            for (int i = 0; i < drmUrls.length(); i++) {
                                JSONObject jsonObject = drmUrls.optJSONObject(i);
                                String drmType = jsonObject.optString("type");
                                String url = jsonObject.optString("mUrl");
                                TCAdaptiveStreamingInfo info = new TCAdaptiveStreamingInfo();
                                info.drmType = drmType;
                                info.url = url;
                                mAdaptiveStreamingInfoList.add(info);
                            }
                        }
                    }
                }
                //解析略缩图信息
                JSONObject imageSpriteInfo = media.optJSONObject("mImageSpriteInfo");
                if (imageSpriteInfo != null) {
                    mImageSpriteInfo = new TCPlayImageSpriteInfo();
                    mImageSpriteInfo.webVttUrl = imageSpriteInfo.getString("webVttUrl");
                }
                //解析关键帧信息
                JSONObject keyFrameDescInfo = media.optJSONObject("mKeyFrameDescInfo");
                if (keyFrameDescInfo != null) {
                    mKeyFrameDescInfo = new ArrayList<>();
                    JSONArray keyFrameDescList = keyFrameDescInfo.optJSONArray("keyFrameDescList");
                    if (keyFrameDescList != null && keyFrameDescList.length() > 0) {
                        for (int i=0; i<keyFrameDescList.length(); i++) {
                            JSONObject jsonObject = keyFrameDescList.getJSONObject(i);
                            TCPlayKeyFrameDescInfo info = new TCPlayKeyFrameDescInfo();
                            info.time = jsonObject.optLong("timeOffset");
                            info.content = jsonObject.optString("content");
                            mKeyFrameDescInfo.add(info);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            TXCLog.e(TAG, Log.getStackTraceString(e));
        }
    }

    /**
     * 获取视频播放url
     *
     * @return url字符串
     */
    @Override
    public String getUrl() {
        return mUrl != null ? mUrl : mAdaptiveStreamingInfoList.get(0).url;
    }

    /**
     * 获取视频名称
     *
     * @return 视频名称字符串
     */
    @Override
    public String getName() {
        return mName;
    }

    /**
     * 获取略缩图信息
     *
     * @return 略缩图信息对象
     */
    @Override
    public TCPlayImageSpriteInfo getImageSpriteInfo() {
        return mImageSpriteInfo;
    }

    /**
     * 获取关键帧信息
     *
     * @return 关键帧信息数组
     */
    @Override
    public List<TCPlayKeyFrameDescInfo> getKeyFrameDescInfo() {
        return mKeyFrameDescInfo;
    }

    /**
     * 获取画质信息
     *
     * @return 画质信息数组
     */
    @Override
    public List<TCVideoQuality> getVideoQualityList() {
        return null;
    }

    /**
     * 获取默认画质信息
     *
     * @return 默认画质信息对象
     */
    @Override
    public TCVideoQuality getDefaultVideoQuality() {
        return null;
    }
}
