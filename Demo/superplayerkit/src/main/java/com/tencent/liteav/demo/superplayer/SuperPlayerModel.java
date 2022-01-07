package com.tencent.liteav.demo.superplayer;


import com.tencent.liteav.demo.superplayer.model.VipWatchModel;
import com.tencent.liteav.demo.superplayer.model.entity.DynamicWaterConfig;
import com.tencent.liteav.demo.superplayer.model.entity.SuperPlayerVideoIdV2;

import java.util.List;


/**
 * 超级播放器支持三种方式播放视频:
 * 1. 视频 URL
 * 填写视频 URL, 如需使用直播时移功能，还需填写appId
 * 2. 腾讯云点播 File ID 播放
 * 填写 appId 及 videoId (如果使用旧版本V2, 请填写videoIdV2)
 * 3. 多码率视频播放
 * 是URL播放方式扩展，可同时传入多条URL，用于进行码率切换
 */
public class SuperPlayerModel {

    /**
     * 自动播放
     */
    public static final int PLAY_ACTION_AUTO_PLAY = 0;

    /**
     * 手动播放
     */
    public static final int PLAY_ACTION_MANUAL_PLAY = 1;

    /**
     * 预加载
     */
    public static final int PLAY_ACTION_PRELOAD = 2;
    public int appId;              // AppId 用于腾讯云点播 File ID 播放及腾讯云直播时移功能

    /**
     * ------------------------------------------------------------------
     * 直接使用URL播放
     * <p>
     * 支持 RTMP、FLV、MP4、HLS 封装格式
     * 使用腾讯云直播时移功能则需要填写appId
     * ------------------------------------------------------------------
     */
    public String url = "";      // 视频URL

    /**
     * ------------------------------------------------------------------
     * 多码率视频 URL
     * <p>
     * 用于拥有多个播放地址的多清晰度视频播放
     * ------------------------------------------------------------------
     */
    public List<SuperPlayerURL> multiURLs;

    public int playDefaultIndex; // 指定多码率情况下，默认播放的连接Index


    /**
     * ------------------------------------------------------------------
     * 腾讯云点播 File ID 播放参数
     * ------------------------------------------------------------------
     */
    public SuperPlayerVideoId videoId;

    /*
     * 用于兼容旧版本(V2)腾讯云点播 File ID 播放参数（即将废弃，不推荐使用）
     */
    @Deprecated
    public SuperPlayerVideoIdV2 videoIdV2;

    public String title = "";             // 视频文件名 （用于显示在UI层);使用file id播放，若未指定title，则使用FileId返回的Title；使用url播放需要指定title，否则title显示为空

    public int playAction = PLAY_ACTION_AUTO_PLAY;

    /**
     * 从腾讯服务器拉取的封面图片
     */
    public String placeholderImage;

    /**
     * 用户设置的封面图片
     */
    public String coverPictureUrl;

    public VipWatchModel vipWatchMode = null;
    /**
     * 动态水印配置
     */
    public DynamicWaterConfig dynamicWaterConfig = null;

    public int duration;

    public static class SuperPlayerURL {
        public SuperPlayerURL(String url, String qualityName) {
            this.qualityName = qualityName;
            this.url = url;
        }

        public SuperPlayerURL() {
        }

        public String qualityName = "原画"; // 清晰度名称（用于显示在UI层）

        public String url = ""; // 该清晰度对应的地址

    }
}
