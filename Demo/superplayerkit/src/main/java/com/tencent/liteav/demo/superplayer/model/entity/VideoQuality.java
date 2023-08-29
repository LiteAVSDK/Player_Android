package com.tencent.liteav.demo.superplayer.model.entity;

import java.io.Serializable;

/**
 * Video quality
 *
 * 清晰度
 */
public class VideoQuality implements Comparable<VideoQuality>, Serializable {

    public int    height;
    public int    width;
    public int    index;
    public int    bitrate;
    // Quality and dp value of the video quality list
    public String title;
    public String url;

    public VideoQuality() {
    }

    public VideoQuality(int index, String title, String url) {
        this.index = index;
        this.title = title;
        this.url = url;
    }

    @Override
    public int compareTo(VideoQuality o) {
        return o.bitrate - this.bitrate;
    }
}
