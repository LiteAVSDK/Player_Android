package com.tencent.liteav.demo.play.v3;

/**
 * Created by hans on 2019/3/25.
 */

public class TCVideoInfo {
//    name: "cnk6SNPfmt4A",
//    size: 354767384,
//    duration: 8504,
//    coverUrl: "https://1253039488.vod2.myqcloud.com/d8c9fd32vodtransgzp1253039488/5b8182d915517827183850370616/1552641772_3900657786.100_0.jpg",
//    description: ""

    public String   videoName;
    public long     size;
    public long     duration;
    public String   coverUrl;
    public String   description;

    @Override
    public String toString() {
        return "TCVideoInfo{" +
                "videoName='" + videoName + '\'' +
                ", size=" + size +
                ", duration=" + duration +
                ", coverUrl='" + coverUrl + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
