package com.tencent.liteav.demo.play;

public class SuperPlayerVideoIdV2 {

    public String fileId;   // 腾讯云视频fileId
    public String timeout;  // 【可选】加密链接超时时间戳，转换为16进制小写字符串，腾讯云 CDN 服务器会根据该时间判断该链接是否有效。
    public String us;       // 【可选】唯一标识请求，增加链接唯一性
    public String sign;     // 【可选】防盗链签名

    public int exper = -1;  // 【V2可选】试看时长，单位：秒。可选

    @Override
    public String toString() {
        return "SuperPlayerVideoId{" +
                ", fileId='" + fileId + '\'' +
                ", timeout='" + timeout + '\'' +
                ", exper=" + exper +
                ", us='" + us + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
