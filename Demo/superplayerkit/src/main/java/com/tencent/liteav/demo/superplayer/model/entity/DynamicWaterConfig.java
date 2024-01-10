package com.tencent.liteav.demo.superplayer.model.entity;


import java.io.Serializable;

public class DynamicWaterConfig implements Serializable {
    /**
     * Dynamic watermark text
     */
    public String dynamicWatermarkTip;
    public int    tipTextSize;
    public int    tipTextColor;
    public int showType = 0;
    public long durationInSecond = 0;

    public DynamicWaterConfig(String dynamicWatermarkTip, int tipTextSize, int tipTextColor) {
        this.dynamicWatermarkTip = dynamicWatermarkTip;
        this.tipTextSize = tipTextSize;
        this.tipTextColor = tipTextColor;
    }

    /**
     * Dynamic watermark display mode,
     * the watermark will always be displayed on the screen during playback.
     * 动态水印展示方式，播放过程中水印一直显示在屏幕上
     */
    public static final int DYNAMIC_RUNNING = 0;

    /**
     * Ghost watermark display mode,the watermark will sometimes be
     * displayed on the screen during playback and sometimes hidden.
     * 幽灵水印展示方式，播放过程中水印事儿显示在屏幕上，时而隐藏
     */
    public static final int GHOST_RUNNING = 1;

    /**
     * Set the watermark display mode
     * @param showType  {@link DynamicWaterConfig#DYNAMIC_RUNNING}
     */
    public void setShowType(int showType) {
        this.showType = showType;
    }

    public int getShowType() {
        return showType;
    }


}
