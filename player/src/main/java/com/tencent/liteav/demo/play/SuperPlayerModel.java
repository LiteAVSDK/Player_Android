package com.tencent.liteav.demo.play;


import com.tencent.liteav.demo.play.bean.TCPlayImageSpriteInfo;
import com.tencent.liteav.demo.play.bean.TCPlayKeyFrameDescInfo;

import java.util.List;

/**
 * Created by yuejiaoli on 2018/7/4.
 */

public class SuperPlayerModel {
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
    public int duration;

    /**
     * 播放器Model可选填上面地址或下面appid+fileid
     */
    public int appid;
    /**
     * 视频的fileid
     */
    public String fileid;

    /**
     * VIDEO 不同清晰度的URL链接
     */
    public List<SuperPlayerUrl> multiVideoURLs;

    /**
     * 缩略图信息（可为null）
     */
    public TCPlayImageSpriteInfo imageInfo;

    /**
     * 打点的关键帧描述信息（可为null）
     */
    public List<TCPlayKeyFrameDescInfo> keyFrameDescInfos;
}
