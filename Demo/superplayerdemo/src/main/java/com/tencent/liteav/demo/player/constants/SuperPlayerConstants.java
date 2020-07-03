package com.tencent.liteav.demo.player.constants;

/**
 * 静态函数
 */
public final class SuperPlayerConstants {

    // 上传常量
    public static final String  PLAYER_DEFAULT_VIDEO    = "play_default_video";
    public static final String  PLAYER_VIDEO_ID         = "video_id";

    // 点播的信息
    public static final int     VOD_APPID               = 1256468886;
    public static final String  VOD_APPKEY              = "1973fcc2b70445af8b51053d4f9022bb";

    public static final String  SERVER_IP               = "http://demo.vod2.myqcloud.com/shortvideo";
    public static final String  ADDRESS_VIDEO_LIST      = SERVER_IP + "/api/v1/resource/videos";

    public static final String  RTMP_URL                = "http://200024424.vod.myqcloud.com/200024424_709ae516bdf811e6ad39991f76a4df69.f20.mp4";

    public static class RetCode {
        // 服务器返回码
        public static final int CODE_SUCCESS            = 0;    // 接口请求成功
        public static final int CODE_PARAMS_ERR         = 1001; // 请求参数错误
        public static final int CODE_AUTH_ERR           = 1002; // 鉴权错误
        public static final int CODE_RES_ERR            = 1003; // 资源不存在
        public static final int CODE_REQ_TOO_FAST_ERR   = 1004; // 请求频率过快
        public static final int CODE_SERVER_ERR         = 1000; // 服务器错误
        // 客户端处理码
        public static final int CODE_REQUEST_ERR        = 1;    // 请求错误
        public static final int CODE_PARSE_ERR          = 2;    // 解析json错误
    }
}
