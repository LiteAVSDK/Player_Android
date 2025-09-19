package com.tencent.liteav.demo.superplayer.ui.player;

import android.view.View;

import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

public class TXCloudVideoViewController implements RenderViewController {

    private final TXCloudVideoView mTXCloudVideoView;

    public TXCloudVideoViewController(TXCloudVideoView cloudVideoView) {
        mTXCloudVideoView = cloudVideoView;
    }

    @Override
    public void bindVodPlayer(TXVodPlayer vodPlayer) {
        mTXCloudVideoView.setVisibility(View.VISIBLE);
        vodPlayer.setPlayerView(mTXCloudVideoView);
    }

    @Override
    public void bindLivePlayer(TXLivePlayer livePlayer) {
        mTXCloudVideoView.setVisibility(View.VISIBLE);
        livePlayer.setPlayerView(mTXCloudVideoView);
    }

    @Override
    public void notifyVideoResolution(int width, int height) {
        // do nothing
    }

    @Override
    public void updateRenderMode(int renderMode) {
        // do nothing
    }

    @Override
    public void handleLayoutChanged(int parentWidth, int parentHeight) {
        // do nothing
    }

    @Override
    public void clearLastImg() {
        mTXCloudVideoView.removeVideoView();
    }
}
