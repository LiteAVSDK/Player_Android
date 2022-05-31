package com.tencent.liteav.demo.superplayer;

/**
 * Created by hans on 2019/3/25.
 * 使用腾讯云fileId播放
 */
public class SuperPlayerVideoId {
    public String           fileId;                             // 腾讯云视频fileId
    public String           pSign;                              // v4 开启防盗链必填

    @Override
    public String toString() {
        return "SuperPlayerVideoId{" +
                ", fileId='" + fileId + '\'' +
                ", pSign='" + pSign + '}';
    }
}
