package com.tencent.liteav.demo.superplayer.model;

import android.os.Bundle;

import com.tencent.rtmp.TXVodPlayer;

public interface ISuperPlayerListener {
    public void onVodPlayEvent(final TXVodPlayer player, final int event, final Bundle param);

    public void onVodNetStatus(final TXVodPlayer player, final Bundle status);

    public void onLivePlayEvent(final int event, final Bundle param);

    public void onLiveNetStatus(final Bundle status);
}