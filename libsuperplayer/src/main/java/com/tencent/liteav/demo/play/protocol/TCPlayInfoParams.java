package com.tencent.liteav.demo.play.protocol;

/**
 * 视频信息协议解析需要传入的参数
 */
public class TCPlayInfoParams {
    //必选
    public int              appId;                  // 腾讯云视频appId
    public String           fileId;                 // 腾讯云视频fileId
    //可选 防盗链参数
    public String           timeout;                // 加密链接超时时间戳
    public int              exper = -1;             // 试看时长，单位：秒。可选
    public String           us;                     // 唯一标识请求，增加链接唯一性
    public String           sign;                   // 防盗链签名

    public TCPlayInfoParams() {
    }

    public TCPlayInfoParams(int appId, String fileId, String timeout, int exper, String us, String sign) {
        this.appId = appId;
        this.fileId = fileId;
        this.timeout = timeout;
        this.exper = exper;
        this.us = us;
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "TCPlayInfoParams{" +
                ", appId='" + appId + '\'' +
                ", fileId='" + fileId + '\'' +
                ", timeout='" + timeout + '\'' +
                ", exper=" + exper +
                ", us='" + us + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
