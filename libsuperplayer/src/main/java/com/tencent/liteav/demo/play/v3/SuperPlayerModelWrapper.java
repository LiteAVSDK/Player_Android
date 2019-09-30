package com.tencent.liteav.demo.play.v3;

import android.text.TextUtils;
import android.util.Pair;

import com.tencent.liteav.demo.play.bean.TCPlayImageSpriteInfo;
import com.tencent.liteav.demo.play.bean.TCPlayKeyFrameDescInfo;
import com.tencent.liteav.demo.play.utils.PlayInfoResponseParser;
import com.tencent.liteav.demo.play.SuperPlayerModel;

import java.util.List;

/**
 * Created by hans on 2019/3/25.
 *
 * Base包含的是SDK内部需要使用到的相关结构体，外部无需关注
 */

public class SuperPlayerModelWrapper {
    public SuperPlayerModel requestModel;

    public SuperPlayerModelWrapper(SuperPlayerModel model) {
        this.requestModel = model;
    }

    /**
     * v2
     */
    public PlayInfoResponseParser playInfoResponseParser;

    /**
     * 缩略图信息（可为null）
     */
    public TCPlayImageSpriteInfo imageInfo;

    /**
     * 打点的关键帧描述信息（可为null）
     */
    public List<TCPlayKeyFrameDescInfo> keyFrameDescInfos;


    /**
     * V2 只有Name有值
     *
     * V3 视频信息其他都有
     */
    public TCVideoInfo videoInfo;

    /**
     * V3 各路视频流
     */
    public List<TCAdaptiveStreamingInfo> adaptiveStreamingInfoList;

    public int currentPlayingType; // 正在播放的
    public static final int URL_DASH_WIDE_VINE = 0;
    public static final int URL_HLS_SIMPLE_AES = 1;
    public static final int URL_DASH = 2;
    public static final int URL_HLS = 3;

    public String getSampleAESURL() {
        String url = getV3VideoURL("hls", "SimpleAES");
        return url;
    }

    public String getDashWidevineURL() {
        String url = getV3VideoURL("dash", "Widevine");
        return url;
    }

    public String getHLSURL() {
        String url = getV3VideoURL("hls", "");
        return url;
    }

    public String getDashURL() {
//        String url = getV3VideoURL("dash", "");
//        return url;
        return null;// 降级忽略Dash的连接
    }

    public Pair<Integer, String> getNextURL(int currentType) {
        int type = currentType;
        String url = null;
        if (currentType == URL_DASH_WIDE_VINE) {
            url = getSampleAESURL();
            type = URL_HLS_SIMPLE_AES;
            if (url != null) {
                return new Pair<>(type, url);
            }
        }

        if (type == URL_HLS_SIMPLE_AES) {
            url = getDashURL();
            type = URL_DASH;
            if (url != null) {
                return new Pair<>(type, url);
            }
        }

        if (type == URL_DASH) {
            url = getHLSURL();
            type = URL_HLS;
            if (url != null) {
                return new Pair<>(type, url);
            }
        }
        return null;
    }

    private String getV3VideoURL(String videoPackage, String drmType) {
        if (adaptiveStreamingInfoList == null)
            return null;
        for (TCAdaptiveStreamingInfo info : adaptiveStreamingInfoList) {
            if (info.videoPackage.toLowerCase().equals(videoPackage.toLowerCase()) && info.drmType.toLowerCase().equals(drmType.toLowerCase())) {
                return info.url;
            }
        }
        return null;
    }

    public boolean isV3Protocol() {
//        return requestModel != null && requestModel.videoId != null && requestModel.videoId.version == SuperPlayerVideoId.FILE_ID_V3;
        return false;
    }
}
