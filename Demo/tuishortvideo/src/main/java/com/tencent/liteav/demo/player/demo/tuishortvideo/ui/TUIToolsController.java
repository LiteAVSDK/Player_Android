package com.tencent.liteav.demo.player.demo.tuishortvideo.ui;

import android.widget.FrameLayout;

import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.TUILayerBridge;
import com.tencent.qcloud.tuiplayer.core.api.TUIPlayerLiveStrategy;
import com.tencent.qcloud.tuiplayer.core.api.TUIPlayerVodStrategy;
import com.tencent.qcloud.tuiplayer.shortvideo.ui.view.TUIShortVideoView;

public class TUIToolsController {

    private static final String TAG = "TUIToolsController";

    private final TUIToolsHandler mToolsHandler;

    public TUIToolsController(FrameLayout containerView, TUIShortVideoView videoView,
                              TUIPlayerVodStrategy initVodStrategy, TUIPlayerLiveStrategy initLiveStrategy
     , TUILayerBridge layerBridge) {
        mToolsHandler = new TUIToolsHandler(containerView, videoView, initVodStrategy, initLiveStrategy, layerBridge);
    }

    public void show() {
        mToolsHandler.show();
    }

    public void hide() {
        mToolsHandler.hide();
    }

    public void toggleVisible() {
        mToolsHandler.toggleVisible();
    }

    public boolean isShow() {
        return mToolsHandler.isShow();
    }

}
