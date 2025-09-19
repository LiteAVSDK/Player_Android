package com.tencent.liteav.demo.player.demo.tuishortvideo.layer.vod;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.tencent.liteav.demo.player.demo.tuishortvideo.R;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.vod.message.DemoVodLayerEvent;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.vod.message.DemoVodLayerEventConstants;
import com.tencent.qcloud.tuiplayer.core.api.TUIPlayerController;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.TUIVodLayer;

public class TUILoadingLayer extends TUIVodLayer implements DemoVodLayerEvent {

    @Override
    public View createView(ViewGroup parent) {
        Context context = parent.getContext();
        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleInverse);
        progressBar.setIndeterminate(true);
        int progressSize = (int) context.getResources().getDimension(R.dimen.tui_loading_size);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(progressSize, progressSize);
        layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(layoutParams);
        return progressBar;
    }

    @Override
    public void onControllerBind(TUIPlayerController controller) {
        super.onControllerBind(controller);
        show();
    }

    @Override
    public void onControllerUnBind(TUIPlayerController controller) {
        hidden();
    }

    @Override
    public void onPlayLoading() {
        super.onPlayLoading();
        show();
    }

    @Override
    public void onPlayLoadingEnd() {
        super.onPlayLoadingEnd();
        hidden();
    }

    @Override
    public void onPlayBegin() {
        super.onPlayBegin();
        hidden();
    }

    @Override
    public void onError(int code, String message, Bundle extraInfo) {
        super.onError(code, message, extraInfo);
        hidden();
    }

    @Override
    public String tag() {
        return "TUILoadingLayer";
    }

    @Override
    public void onLayerEvent(int codeEvent) {
        if (codeEvent == DemoVodLayerEventConstants.SHOW_LOADING) {
            show();
        }
    }
}
