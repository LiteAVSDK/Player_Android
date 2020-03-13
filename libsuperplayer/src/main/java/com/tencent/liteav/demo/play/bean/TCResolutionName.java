package com.tencent.liteav.demo.play.bean;

/**
 * 自适应码流视频画质别名
 */
public class TCResolutionName {
    public int minEdgeLength; // 最小边长px
    public String name;       // 画质名称

    @Override
    public String toString() {
        return "TCResolutionName{" +
                "minEdgeLength='" + minEdgeLength + '\'' +
                ", name=" + name +
                '}';
    }
}
