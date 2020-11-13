package com.tencent.liteav.demo.superplayer.model.entity;

/**
 * 视频关键帧信息
 */
public class PlayKeyFrameDescInfo {

    public String content;    // 描述信息
    public float  time;       // 关键帧时间(秒)

    @Override
    public String toString() {
        return "TCPlayKeyFrameDescInfo{" +
                "content='" + content + '\'' +
                ", time=" + time +
                '}';
    }
}
