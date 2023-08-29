package com.tencent.liteav.demo.superplayer.model.entity;

public class SuperPlayerVideoIdV2 {

    public String fileId;   // Tencent Cloud video fileId
    // [Optional] Encryption link timeout timestamp, converted to lowercase hexadecimal string.
    // Tencent Cloud CDN server will determine whether the link is valid based on this time
    public String timeout;
    // [Optional] Unique identifier for the request, increasing link uniqueness.
    public String us;
    // [Optional] Anti-theft link signature.
    public String sign;
    // [V2 Optional] Preview duration in seconds.
    public int    exper = -1;

    @Override
    public String toString() {
        return "SuperPlayerVideoId{"
                + ", fileId='"
                + fileId
                + '\''
                + ", timeout='"
                + timeout
                + '\''
                + ", exper="
                + exper
                + ", us='"
                + us
                + '\''
                + ", sign='"
                + sign
                + '\''
                + '}';
    }
}
