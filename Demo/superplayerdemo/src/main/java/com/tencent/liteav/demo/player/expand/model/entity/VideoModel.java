package com.tencent.liteav.demo.player.expand.model.entity;


import com.tencent.liteav.demo.superplayer.model.VipWatchModel;
import java.util.List;

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
     * 视频封面本地图片
     */
    public String placeholderImage;

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

    /**
     * VIDEO 不同清晰度的URL链接
     */
    public List<VideoPlayerURL> multiVideoURLs;
    public int                  playDefaultIndex; // 指定多码率情况下，默认播放的连接Index
    public VipWatchModel        vipWatchModel = null;


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
}
