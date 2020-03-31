package com.tencent.liteav.demo.play.bean;

/**
 * 自适应码流视频画质别名
 */
public class TCResolutionName {
    public String name;       // 画质名称
    public int    width;      //
    public int    height;
    public String    type;      //类型 可能的取值有 video 和 audio

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
