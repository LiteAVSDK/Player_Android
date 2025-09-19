package com.tencent.liteav.demo.player.demo.tuishortvideo.layer.live;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.tencent.liteav.demo.player.demo.tuishortvideo.R;
import com.tencent.qcloud.tuiplayer.core.api.TUIPlayerController;
import com.tencent.qcloud.tuiplayer.core.api.model.TUILiveSource;
import com.tencent.qcloud.tuiplayer.core.api.ui.player.ITUILivePlayer;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.TUILiveLayer;

public class TUILiveLoadingLayer extends TUILiveLayer {

    @Override
    public View createView(ViewGroup parent) {
        Context context = parent.getContext();
        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleInverse);
        progressBar.setIndeterminate(true);
        int progressSize = (int) context.getResources().getDimension(R.dimen.tui_loading_size);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(progressSize, progressSize);
        layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(layoutParams);
        progressBar.setVisibility(View.GONE);
        return progressBar;
    }

    @Override
    public void onBindData(TUILiveSource videoSource) {
        super.onBindData(videoSource);
    }

    @Override
    public void onControllerBind(TUIPlayerController controller) {
        super.onControllerBind(controller);
        if (getPlayer().isPlaying()) {
            hidden();
        }
    }

    @Override
    public void onControllerUnBind(TUIPlayerController controller) {
        show();
    }

    @Override
    public void onStreamSwitched(ITUILivePlayer player, String url, int code) {
        super.onStreamSwitched(player, url, code);
        hidden();
    }

    @Override
    public void onVideoLoading(ITUILivePlayer player, Bundle extraInfo) {
        super.onVideoLoading(player, extraInfo);
        show();
    }

    @Override
    public void onVideoPlaying(ITUILivePlayer player, boolean firstPlay, Bundle extraInfo) {
        super.onVideoPlaying(player, firstPlay, extraInfo);
        hidden();
    }

    @Override
    public String tag() {
        return "TUILiveLoadingLayer";
    }
}
