package com.tencent.liteav.demo.play.v3;

/**
 * Created by hans on 2019/3/25.
 */

public class TCAdaptiveStreamingInfo {
    public int    definition;
    public String videoPackage;
    public String drmType;
    public String url;


    @Override
    public String toString() {
        return "TCAdaptiveStreamingInfo{" +
                "definition=" + definition +
                ", videoPackage='" + videoPackage + '\'' +
                ", drmType='" + drmType + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
