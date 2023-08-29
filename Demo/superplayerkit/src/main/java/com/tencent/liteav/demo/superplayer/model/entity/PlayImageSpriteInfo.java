package com.tencent.liteav.demo.superplayer.model.entity;

import java.util.List;

/**
 * Video sprite information
 * <p>
 * 视频雪碧图信息
 */
public class PlayImageSpriteInfo {

    public List<String> imageUrls; // Image link URL
    public String webVttUrl; // Web VTT description file download URL

    @Override
    public String toString() {
        return "TCPlayImageSpriteInfo{"
                + "imageUrls="
                + imageUrls
                + ", webVttUrl='"
                + webVttUrl
                + '\''
                + '}';
    }
}
