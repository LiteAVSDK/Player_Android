package com.tencent.liteav.demo.play.bean;

/**
 * Created by hans on 2019/3/25.
 *
 * 自适应码流信息
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
