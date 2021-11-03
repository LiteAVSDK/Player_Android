package com.tencent.liteav.demo.player.demo.shortvideo.bean;


import android.graphics.Bitmap;

import java.util.List;


public class ShortVideoBean {

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
     * 等于v2或者v4或者为空
     */
    public String appidType;

    /**
     * 缓存第一帧图片用于快速显示
     */
    public Bitmap bitmap;

    public int bitRateIndex;

    public ShortVideoBean(int appid, String fileid, String appidType) {
        this.appid = appid;
        this.fileid = fileid;
        this.appidType = appidType;
    }

    /**
     * VIDEO 不同清晰度的URL链接
     */
    public List<VideoPlayerURL> multiVideoURLs;
    public int playDefaultIndex; // 指定多码率情况下，默认播放的连接Index

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
