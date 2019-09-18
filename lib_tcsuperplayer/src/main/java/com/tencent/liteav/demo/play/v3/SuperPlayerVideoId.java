package com.tencent.liteav.demo.play.v3;


/**
 * Created by hans on 2019/3/25.
 * 使用腾讯云fileId播放
 */
public class SuperPlayerVideoId {
//    public static final int FILE_ID_V2 = 0;                     // 腾讯云interface版本【普通转码】
//    public static final int FILE_ID_V3 = 1;                     // 腾讯云interface版本【DRM视频】

    public String           fileId;                             // 腾讯云视频fileId
//    public int              version         = FILE_ID_V2;       // 请求腾讯云的interface版本：【普通转码】 FileIdV2、【DRM视频】 FileIdV3。
//    public String           playDefinition;                     // 【V3协议必填】使用fileId时候填写，模板ID

    /**
     * 防盗链参数 具体可参考{@link com.tencent.liteav.demo.play.SuperPlayerSignUtils}
     */
    public String           timeout;                            // 【可选】加密链接超时时间戳，转换为16进制小写字符串，腾讯云 CDN 服务器会根据该时间判断该链接是否有效。
    public int              exper           = -1;               // 【V2可选】试看时长，单位：秒。可选
    public String           us;                                 // 【可选】唯一标识请求，增加链接唯一性
    public String           sign;                               // 【可选】防盗链签名
//    public String           playerId;                           // 【V3可选】播放器 ID默认使用文件绑定的播放器 ID 或默认播放器 ID
//    public int              rlimit          = -1;               // 【V3可选】允许不同 IP 的播放次数，仅当开启防盗链且需要开启试看时填写

    @Override
    public String toString() {
        return "SuperPlayerVideoId{" +
                ", fileId='" + fileId + '\'' +
//                ", version=" + version +
//                ", playDefinition='" + playDefinition + '\'' +
                ", timeout='" + timeout + '\'' +
                ", exper=" + exper +
                ", us='" + us + '\'' +
                ", sign='" + sign + '\'' +
//                ", playerId='" + playerId + '\'' +
//                ", rlimit=" + rlimit +
                '}';
    }
}
