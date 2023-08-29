package com.tencent.liteav.demo.superplayer.model.protocol;

import com.tencent.liteav.demo.superplayer.model.entity.PlayImageSpriteInfo;
import com.tencent.liteav.demo.superplayer.model.entity.PlayKeyFrameDescInfo;
import com.tencent.liteav.demo.superplayer.model.entity.ResolutionName;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;

import java.util.List;

/**
 * Video information protocol interface
 *
 * 视频信息协议接口
 */
public interface IPlayInfoProtocol {
    /**
     * Send a network request for video information protocol
     *
     * 发送视频信息协议网络请求
     */
    void sendRequest(IPlayInfoRequestCallback callback);

    /**
     * Cancel the request midway
     *
     * 中途取消请求
     */
    void cancelRequest();

    /**
     * Get the video playback URL
     *
     * 获取视频播放url
     */
    String getUrl();

    /**
     * Get the encrypted video playback URL
     *
     * 获取加密视频播放url
     */
    String getEncyptedUrl(PlayInfoConstant.EncryptedURLType type);

    /**
     * Get the encryption token
     *
     * 获取加密token
     */
    String getToken();

    /**
     * Get the video name
     *
     * 获取视频名称
     */
    String getName();

    /**
     * Get the sprite information
     *
     * 获取雪碧图信息
     */
    PlayImageSpriteInfo getImageSpriteInfo();

    /**
     * Get the keyframe information
     *
     * 获取关键帧信息
     */
    List<PlayKeyFrameDescInfo> getKeyFrameDescInfo();

    /**
     * Get the video quality information
     *
     * 获取画质信息
     */
    List<VideoQuality> getVideoQualityList();

    /**
     * Get the default video quality
     *
     * 获取默认画质
     */
    VideoQuality getDefaultVideoQuality();

    /**
     * Get the video quality alias list
     *
     * 获取视频画质别名列表
     */
    List<ResolutionName> getResolutionNameList();

    /**
     * Pass-through content
     *
     * 透传内容
     */
    String getPenetrateContext();


    /**
     * Get the DRM encryption type
     *
     * 获取 DRM 加密类型
     */
    String getDRMType();
}
