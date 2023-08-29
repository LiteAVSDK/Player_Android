package com.tencent.liteav.demo.superplayer.model;

import java.io.Serializable;

public class VipWatchModel implements Serializable {
    // Display prompt message.
    private String tipStr       = null;
    // Trial viewing time. The default value is the maximum value of `long`, which indicates that the VIP trial
    // viewing content is not displayed, and the unit is seconds.
    // Display prompt message.
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