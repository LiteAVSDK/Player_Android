package com.tencent.liteav.demo.superplayer.model.entity;

/**
 * Video keyframe information
 *
 * 视频关键帧信息
 */
public class PlayKeyFrameDescInfo {

    public String content;    // Description information
    public float  time;       // Keyframe time (in seconds)

    @Override
    public String toString() {
        return "TCPlayKeyFrameDescInfo{"
                + "content='"
                + content
                + '\''
                + ", time="
                + time
                + '}';
    }
}
