package com.tencent.liteav.demo.superplayer;


import com.tencent.liteav.demo.superplayer.model.VipWatchModel;
import com.tencent.liteav.demo.superplayer.model.entity.DynamicWaterConfig;
import com.tencent.liteav.demo.superplayer.model.entity.SuperPlayerVideoIdV2;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;

import java.util.ArrayList;
import java.util.List;


/**
 * The SuperPlayer supports three ways to play videos:
 * 1、Video URL: Fill in the video URL. If you need to use the live time-shifting function,
 * you also need to fill in the appId.
 * 2、Tencent Cloud VOD File ID playback: Fill in the appId and videoId (if using the old version V2,
 * please fill in videoIdV2).
 * 3、Multiple bitrate video playback: An extension of the URL playback method, multiple URLs can be
 * passed in at the same time for bitrate switching.
 *
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
     * Automatic playback.
     *
     * 自动播放
     */
    public static final int PLAY_ACTION_AUTO_PLAY = 0;

    /**
     * Manual playback
     *
     * 手动播放
     */
    public static final int PLAY_ACTION_MANUAL_PLAY = 1;

    /**
     * Preloading
     *
     * 预加载
     */
    public static final int PLAY_ACTION_PRELOAD = 2;

    /**
     * The AppId is used for Tencent Cloud VOD File ID playback and Tencent Cloud live time-shifting function.
     */
    public int appId;

    /**
     * External subtitles.
     *
     * 外挂字幕
     */
    public List<SubtitleSourceModel> subtitleSourceModelList = null;

    /**
     * ------------------------------------------------------------------
     * Direct use of URL playback.
     * <p>
     * Supports RTMP, FLV, MP4, and HLS encapsulation formats
     * If using Tencent Cloud live time-shifting function, you need to fill in the appId.
     *
     * 直接使用URL播放
     * <p>
     * 支持 RTMP、FLV、MP4、HLS 封装格式
     * 使用腾讯云直播时移功能则需要填写appId
     * ------------------------------------------------------------------
     */
    public String url = "";

    /**
     * ------------------------------------------------------------------
     * Multiple bitrate video URL.
     * <p>
     * Used for multi-bitrate video playback with multiple playback addresses.
     *
     * 多码率视频 URL
     * <p>
     * 用于拥有多个播放地址的多清晰度视频播放
     * ------------------------------------------------------------------
     */
    public List<SuperPlayerURL> multiURLs;

    public List<VideoQuality> videoQualityList = new ArrayList<>();

    public int playDefaultIndex;


    /**
     * ------------------------------------------------------------------
     * Tencent Cloud VOD File ID playback parameters.
     *
     * 腾讯云点播 File ID 播放参数
     * ------------------------------------------------------------------
     */
    public SuperPlayerVideoId videoId;

    /*
     * Used to be compatible with the old version (V2) Tencent Cloud VOD File ID playback parameters
     *  (to be deprecated, not recommended for use).
     * 用于兼容旧版本(V2)腾讯云点播 File ID 播放参数（即将废弃，不推荐使用）
     */
    @Deprecated
    public SuperPlayerVideoIdV2 videoIdV2;

    // Video file name (used to display on the UI layer); when playing with file id, if title is not specified,
    // the title returned by FileId will be used; when playing with url, title needs to be specified,
    // otherwise the title will be empty.
    public String title = "";

    public int playAction = PLAY_ACTION_AUTO_PLAY;

    /**
     * Cover image pulled from Tencent server.
     *
     * 从腾讯服务器拉取的封面图片
     */
    public String placeholderImage;

    /**
     * User-set cover image.
     *
     * 用户设置的封面图片
     */
    public String coverPictureUrl;

    public VipWatchModel vipWatchMode = null;

    /**
     * Dynamic watermark configuration.
     *
     * 动态水印配置
     */
    public DynamicWaterConfig dynamicWaterConfig = null;

    public int duration;

    public boolean isEnableCache = false; // Whether to enable caching, off by default.

    public static class SuperPlayerURL {
        public SuperPlayerURL(String url, String qualityName) {
            this.qualityName = qualityName;
            this.url = url;
        }

        public SuperPlayerURL() {
        }

        public String qualityName = "原画"; // Clear name (used to display on the UI layer).

        public String url = ""; // The address corresponding to this clarity.

    }
}
