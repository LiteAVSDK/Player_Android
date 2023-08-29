package com.tencent.liteav.demo.superplayer.model.protocol;

import com.tencent.liteav.demo.superplayer.SuperPlayerVideoId;
import com.tencent.liteav.demo.superplayer.model.entity.SuperPlayerVideoIdV2;

/**
 * Parameters required for video information protocol parsing
 *
 * 视频信息协议解析需要传入的参数
 */
public class PlayInfoParams {
    public int                  appId;
    public String               fileId;
    public SuperPlayerVideoId   videoId;    // v4 protocol parameters
    public SuperPlayerVideoIdV2 videoIdV2;  // v2 protocol parameters

    public PlayInfoParams() {
    }

    @Override
    public String toString() {
        return "TCPlayInfoParams{" +
                ", appId='" + appId + '\'' +
                ", fileId='" + fileId + '\'' +
                ", v4='" + (videoId != null ? videoId.toString() : "") + '\'' +
                ", v2='" + (videoIdV2 != null ? videoIdV2.toString() : "") + '\'' +
                '}';
    }
}
