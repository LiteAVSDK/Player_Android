package com.tencent.liteav.demo.superplayer.model.entity;

/**
 * Adaptive bitrate video quality alias
 *
 * 自适应码流视频画质别名
 */
public class ResolutionName {

    public String name; // Quality name
    public String type; // Type. Possible values are video and audio.
    public int    width;
    public int    height;

    @Override
    public String toString() {
        return "TCResolutionName{"
                + "width='"
                + width
                + '\''
                + "height='"
                + height
                + '\''
                + "type='"
                + type
                + '\''
                + ", name="
                + name
                + '}';
    }
}
