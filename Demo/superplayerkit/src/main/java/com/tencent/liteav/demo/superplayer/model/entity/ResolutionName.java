package com.tencent.liteav.demo.superplayer.model.entity;

/**
 * 自适应码流视频画质别名
 */
public class ResolutionName {

    public String name; // 画质名称
    public String type; // 类型 可能的取值有 video 和 audio
    public int    width;
    public int    height;

    @Override
    public String toString() {
        return "TCResolutionName{" +
                "width='" + width + '\'' +
                "height='" + height + '\'' +
                "type='" + type + '\'' +
                ", name=" + name +
                '}';
    }
}
