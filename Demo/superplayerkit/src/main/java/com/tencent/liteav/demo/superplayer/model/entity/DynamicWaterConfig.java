package com.tencent.liteav.demo.superplayer.model.entity;


public class DynamicWaterConfig {
    /**
     * 动态水印文本
     */
    public String dynamicWatermarkTip;
    public int    tipTextSize;
    public int    tipTextColor;

    public DynamicWaterConfig(String dynamicWatermarkTip, int tipTextSize, int tipTextColor) {
        this.dynamicWatermarkTip = dynamicWatermarkTip;
        this.tipTextSize = tipTextSize;
        this.tipTextColor = tipTextColor;
    }

}
