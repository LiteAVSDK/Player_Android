package com.tencent.liteav.demo.play;

/**
 * Created by liyuejiao on 2018/1/31.
 */

public class TXPlayerAuthParam {

    public String appId;      //应用appId。必填
    public String fileId;  //文件id。必填
    public String timeout; //加密链接超时时间戳，转换为16进制小写字符串，腾讯云 CDN 服务器会根据该时间判断该链接是否有效。可选
    public int exper;      //试看时长，单位：秒。可选
    public String us;      //唯一标识请求，增加链接唯一性
    public String sign;

    @Override
    public String toString() {
        return "TXPlayerAuthParam{" +
                "appId='" + appId + '\'' +
                ", fileId='" + fileId + '\'' +
                ", timeout='" + timeout + '\'' +
                ", exper=" + exper +
                ", us='" + us + '\'' +
                '}';
    }
}
