package com.tencent.liteav.demo.superplayer.model;

public class VipWatchModel { //展示的提示语信息
    private String tipStr       = null;
    //试看的时间   默认值是long的最大值，表示不展示VIP试看内容的时间，单位为秒
    private long   canWatchTime = Long.MAX_VALUE;


    public VipWatchModel(String tipStr, long canWatchTime) {
        this.tipStr = String.format(tipStr, canWatchTime);
        this.canWatchTime = canWatchTime;
    }

    public String getTipStr() {
        return tipStr;
    }

    public void setTipStr(String tipStr) {
        this.tipStr = tipStr;
    }

    public long getCanWatchTime() {
        return canWatchTime;
    }

    public void setCanWatchTime(long canWatchTime) {
        this.canWatchTime = canWatchTime;
    }
}