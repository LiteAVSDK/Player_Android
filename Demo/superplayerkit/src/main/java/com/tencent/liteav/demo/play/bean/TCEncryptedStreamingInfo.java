package com.tencent.liteav.demo.play.bean;

/**
 * Created by hans on 2019/3/25.
 * <p>
 * 自适应码流信息
 */

public class TCEncryptedStreamingInfo {

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
