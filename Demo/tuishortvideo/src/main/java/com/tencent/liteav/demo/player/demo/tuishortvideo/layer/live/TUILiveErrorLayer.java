package com.tencent.liteav.demo.player.demo.tuishortvideo.layer.live;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tencent.qcloud.tuiplayer.core.api.ui.player.ITUILivePlayer;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.TUILiveLayer;

public class TUILiveErrorLayer extends TUILiveLayer {

    @Override
    public View createView(ViewGroup parent) {
        return new View(parent.getContext());
    }

    @Override
    public void onError(ITUILivePlayer player, int code, String msg, Bundle extraInfo) {
        if (null != getView()) {
            Context context = getView().getContext();
            Toast.makeText(context, "playError, errorCode:" + code + ",message:" + msg, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public String tag() {
        return "TUILiveErrorLayer";
    }
}
