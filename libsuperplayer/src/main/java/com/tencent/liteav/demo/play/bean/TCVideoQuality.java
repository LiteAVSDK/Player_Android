package com.tencent.liteav.demo.play.bean;

/**
 * Created by yuejiaoli on 2018/7/7.
 *
 * 清晰度
 */

public class TCVideoQuality {

    public int index;
    public String name;
    public String title;
    public int bitrate;
    public String url;

    public TCVideoQuality() {
    }

    public TCVideoQuality(int index, String title, String url) {
        this.index = index;
        this.title = title;
        this.url = url;
    }
}
