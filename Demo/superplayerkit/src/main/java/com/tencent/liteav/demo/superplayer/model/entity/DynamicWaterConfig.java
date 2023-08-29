package com.tencent.liteav.demo.superplayer.model.entity;


import java.io.Serializable;

public class DynamicWaterConfig implements Serializable {
    /**
     * Dynamic watermark text
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
