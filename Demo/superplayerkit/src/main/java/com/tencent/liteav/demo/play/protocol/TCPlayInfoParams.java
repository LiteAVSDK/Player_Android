package com.tencent.liteav.demo.play.protocol;

import com.tencent.liteav.demo.play.SuperPlayerVideoId;
import com.tencent.liteav.demo.play.SuperPlayerVideoIdV2;

/**
 * 视频信息协议解析需要传入的参数
 */
public class TCPlayInfoParams {
    //必选
    public int      appId;                  // 腾讯云视频appId
    public String   fileId;                 // 腾讯云视频fileId

    public SuperPlayerVideoId   videoId;    //v4 协议参数
    public SuperPlayerVideoIdV2 videoIdV2;  //v2 协议参数

    public TCPlayInfoParams() {
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
