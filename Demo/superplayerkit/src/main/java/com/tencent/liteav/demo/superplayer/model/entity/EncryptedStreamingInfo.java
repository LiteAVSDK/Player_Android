package com.tencent.liteav.demo.superplayer.model.entity;

/**
 * Adaptive bitrate information.
 *
 * 自适应码流信息
 */
public class EncryptedStreamingInfo {

    public String drmType;
    public String url;

    @Override
    public String toString() {
        return "TCEncryptedStreamingInfo{" +
                ", drmType='" + drmType + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
