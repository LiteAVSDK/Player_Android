package com.tencent.liteav.demo.player.expand.model.entity;


import com.tencent.liteav.demo.superplayer.SuperPlayerModel;
import com.tencent.liteav.demo.superplayer.SuperPlayerVideoId;
import com.tencent.liteav.demo.superplayer.model.SuperPlayer;
import com.tencent.liteav.demo.superplayer.model.VipWatchModel;
import com.tencent.liteav.demo.superplayer.model.entity.DynamicWaterConfig;
import java.util.List;

import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_AUTO_PLAY;

/**
 * Created by yuejiaoli on 2018/7/4.
 */

public class VideoModel {

    /**
     * 视频标题
     */
    public String title;

    /**
     * 视频URL
     */
    public String videoURL;


    /**
     *  从服务器拉取的封面图片
     */
    public String placeholderImage;


    /**
     * 用户设置图片的接口 如果是本地图片前面加file://
     */
    public String coverPictureUrl;

    /**
     * 视频时长
     */
    public int duration;

    /**
     * appId
     */
    public int appid;

    /**
     * 视频的fileid
     */
    public String fileid;

    /**
     * 签名字串
     */
    public String pSign;

    public int playAction = PLAY_ACTION_AUTO_PLAY;


    /**
     * VIDEO 不同清晰度的URL链接
     */
    public List<VideoPlayerURL> multiVideoURLs;
    public int                  playDefaultIndex; // 指定多码率情况下，默认播放的连接Index
    public VipWatchModel        vipWatchModel = null;

    //feed流视频描述信息
    public String videoDescription     = null;
    public String videoMoreDescription = null;
    /**
     * 动态水印文本
     */
    public DynamicWaterConfig dynamicWaterConfig = null;

    public static class VideoPlayerURL {

        public VideoPlayerURL() {
        }

        public VideoPlayerURL(String title, String url) {
            this.title = title;
            this.url = url;
        }

        /**
         * 视频标题
         */
        public String title;

        /**
         * 视频URL
         */
        public String url;

        @Override
        public String toString() {
            return "SuperPlayerUrl{" +
                    "title='" + title + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }

    public SuperPlayerModel convertToSuperPlayerModel() {
        SuperPlayerModel superPlayerModel = new SuperPlayerModel();
        superPlayerModel.appId = appid;
        superPlayerModel.vipWatchMode = vipWatchModel;
        superPlayerModel.videoId = new SuperPlayerVideoId();
        superPlayerModel.videoId.fileId = fileid;
        superPlayerModel.videoId.pSign = pSign;
        superPlayerModel.playAction = playAction;
        superPlayerModel.placeholderImage = placeholderImage;
        superPlayerModel.coverPictureUrl = coverPictureUrl;
        superPlayerModel.duration = duration;
        if (dynamicWaterConfig != null) {
            superPlayerModel.dynamicWaterConfig = dynamicWaterConfig;
        }
        return superPlayerModel;
    }

}
