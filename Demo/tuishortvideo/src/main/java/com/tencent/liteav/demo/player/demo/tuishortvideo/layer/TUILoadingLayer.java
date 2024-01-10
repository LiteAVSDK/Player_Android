package com.tencent.liteav.demo.player.demo.tuishortvideo.layer;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.tencent.qcloud.tuiplayer.core.api.TUIPlayerController;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.TUIBaseLayer;
import com.tencent.qcloud.tuiplayer.shortvideo.R;

public class TUILoadingLayer extends TUIBaseLayer {

    @Override
    public View createView(ViewGroup parent) {
        Context context = parent.getContext();
        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleInverse);
        progressBar.setIndeterminate(true);
        int progressSize = (int) context.getResources().getDimension(R.dimen.tui_loading_size);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(progressSize,progressSize);
        layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(layoutParams);
        return progressBar;
    }

    @Override
    public void onControllerUnBind(TUIPlayerController controller) {
        super.onControllerUnBind(controller);
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
    public String tag() {
        return null;
    }
}
