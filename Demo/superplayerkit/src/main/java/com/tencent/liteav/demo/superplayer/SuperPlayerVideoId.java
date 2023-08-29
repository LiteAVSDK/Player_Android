package com.tencent.liteav.demo.superplayer;

/**
 * Play using Tencent Cloud `fileId`.
 *
 * 使用腾讯云fileId播放
 */
public class SuperPlayerVideoId {
    public String           fileId;
    public String           pSign;

    @Override
    public String toString() {
        return "SuperPlayerVideoId{"
                + ", fileId='"
                + fileId
                + '\''
                + ", pSign='"
                + pSign
                + '}';
    }
}
