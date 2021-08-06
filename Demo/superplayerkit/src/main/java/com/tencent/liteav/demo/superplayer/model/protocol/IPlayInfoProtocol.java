package com.tencent.liteav.demo.superplayer.model.protocol;

import com.tencent.liteav.demo.superplayer.model.entity.PlayImageSpriteInfo;
import com.tencent.liteav.demo.superplayer.model.entity.PlayKeyFrameDescInfo;
import com.tencent.liteav.demo.superplayer.model.entity.ResolutionName;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;

import java.util.List;

/**
 * 视频信息协议接口
 */
public interface IPlayInfoProtocol {
    /**
     * 发送视频信息协议网络请求
     *
     * @param callback 协议请求回调
     */
    void sendRequest(IPlayInfoRequestCallback callback);

    /**
     * 中途取消请求
     */
    void cancelRequest();

    /**
     * 获取视频播放url
     *
     * @return 视频播放url字符串
     */
    String getUrl();

    /**
     * 获取加密视频播放url
     *
     * @return url字符串
     */
    String getEncyptedUrl(PlayInfoConstant.EncryptedURLType type);

    /**
     * 获取加密token
     *
     * @return token字符串
     */
    String getToken();

    /**
     * 获取视频名称
     *
     * @return 视频名称字符串
     */
    String getName();

    /**
     * 获取雪碧图信息
     *
     * @return 雪碧图信息对象
     */
    PlayImageSpriteInfo getImageSpriteInfo();

    /**
     * 获取关键帧信息
     *
     * @return 关键帧信息数组
     */
    List<PlayKeyFrameDescInfo> getKeyFrameDescInfo();

    /**
     * 获取画质信息
     *
     * @return 画质信息数组
     */
    List<VideoQuality> getVideoQualityList();

    /**
     * 获取默认画质
     *
     * @return 默认画质信息对象
     */
    VideoQuality getDefaultVideoQuality();

    /**
     * 获取视频画质别名列表
     *
     * @return 画质别名数组
     */
    List<ResolutionName> getResolutionNameList();

    /**
     * 透传内容
     *
     * @return 透传内容
     */
    String getPenetrateContext();


    /**
     * 获取 DRM 加密类型
     *
     * @return
     */
    String getDRMType();
}
