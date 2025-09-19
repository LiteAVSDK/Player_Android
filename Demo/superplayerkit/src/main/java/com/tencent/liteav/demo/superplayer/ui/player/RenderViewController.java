package com.tencent.liteav.demo.superplayer.ui.player;

import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXVodPlayer;

public interface RenderViewController {

    void bindVodPlayer(TXVodPlayer vodPlayer);

    void bindLivePlayer(TXLivePlayer livePlayer);

    void notifyVideoResolution(int width, int height);

    void updateRenderMode(int renderMode);

    void handleLayoutChanged(int parentWidth, int parentHeight);

    void clearLastImg();

    interface FixMode {
        int FIX_WIDTH = 1;
        int FIX_HEIGHT = 2;
        int FIX_NONE = 3;
    }
}
