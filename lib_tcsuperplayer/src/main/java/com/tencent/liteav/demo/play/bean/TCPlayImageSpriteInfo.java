package com.tencent.liteav.demo.play.bean;

import java.util.List;

public class TCPlayImageSpriteInfo {
    public List<String> imageUrls; // 图片链接URL
    public String webVttUrl; // web vtt描述文件下载URL

    @Override
    public String toString() {
        return "TCPlayImageSpriteInfo{" +
                "imageUrls=" + imageUrls +
                ", webVttUrl='" + webVttUrl + '\'' +
                '}';
    }
}
