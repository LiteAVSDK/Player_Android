package com.tencent.liteav.demo.superplayer.model.entity;

/**
 * Created by annidy on 2017/12/20.
 * <p>
 * 视频播放信息
 */

public class PlayInfoStream {
    public int    height;
    public int    width;
    public int    size;
    public int    duration;
    public int    bitrate;
    public int    definition;
    public String id;
    public String name;
    public String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getHeight() {
        return height;
    }

    public int getDuration() {
        return duration;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }
}
