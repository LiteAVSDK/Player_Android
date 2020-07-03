package com.tencent.liteav.demo.play.bean;

/**
 * 视频关键帧信息
 */
public class TCPlayKeyFrameDescInfo {

    public String   content;    // 描述信息
    public float    time;       // 关键帧时间(秒)

    @Override
    public String toString() {
        return "TCPlayKeyFrameDescInfo{" +
                "content='" + content + '\'' +
                ", time=" + time +
                '}';
    }
}
