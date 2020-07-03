package com.tencent.liteav.demo.play.bean;

import java.util.List;

/**
 * 视频雪碧图信息
 */
public class TCPlayImageSpriteInfo {

    public List<String> imageUrls; // 图片链接URL
    public String       webVttUrl; // web vtt描述文件下载URL

    @Override
    public String toString() {
        return "TCPlayImageSpriteInfo{" +
                "imageUrls=" + imageUrls +
                ", webVttUrl='" + webVttUrl + '\'' +
                '}';
    }
}
