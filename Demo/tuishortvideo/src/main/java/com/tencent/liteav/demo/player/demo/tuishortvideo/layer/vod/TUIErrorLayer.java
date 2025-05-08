package com.tencent.liteav.demo.player.demo.tuishortvideo.layer.vod;

import static com.tencent.rtmp.TXLiveConstants.PLAY_ERR_GET_PLAYINFO_FAIL;
import static com.tencent.rtmp.TXLiveConstants.PLAY_ERR_NET_DISCONNECT;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.demo.player.demo.tuishortvideo.R;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.vod.message.DemoVodLayerEvent;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.vod.message.DemoVodLayerEventConstants;
import com.tencent.liteav.demo.player.demo.tuishortvideo.tools.MyNetTools;
import com.tencent.qcloud.tuiplayer.core.api.TUIPlayerController;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.TUIVodLayer;

public class TUIErrorLayer extends TUIVodLayer implements DemoVodLayerEvent {

    private TextView mRetryTip;

    @Override
    public View createView(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.tuiplayer_error_layer, parent, false);
        mRetryTip = view.findViewById(R.id.tv_retry);
        return view;
    }

    @Override
    public void onControllerBind(TUIPlayerController controller) {
        super.onControllerBind(controller);
        mRetryTip.setVisibility(View.GONE);
    }

    @Override
    public void onError(int code, String message, Bundle extraInfo) {
        Context context = getView().getContext().getApplicationContext();
        if (null != getView()) {
            Toast.makeText(context, "playError, errorCode:" + code
                    + ",message:" + message, Toast.LENGTH_SHORT).show();
        }
        if (code == PLAY_ERR_GET_PLAYINFO_FAIL || code == PLAY_ERR_NET_DISCONNECT) {
            if (!MyNetTools.isNetworkConnected(context)) {
                mRetryTip.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onPlayBegin() {
        super.onPlayBegin();
        mRetryTip.setVisibility(View.GONE);
    }

    @Override
    public void onPlayPrepare() {
        super.onPlayPrepare();
        mRetryTip.setVisibility(View.GONE);
    }

    @Override
    public void onPlayLoading() {
        super.onPlayLoading();
        mRetryTip.setVisibility(View.GONE);
    }

    @Override
    public void onPlayLoadingEnd() {
        super.onPlayLoadingEnd();
        mRetryTip.setVisibility(View.GONE);
    }

    @Override
    public String tag() {
        return "TUILiveErrorLayer";
    }

    @Override
    public void onLayerEvent(int codeEvent) {
        if (codeEvent == DemoVodLayerEventConstants.SHOW_LOADING) {
            mRetryTip.setVisibility(View.GONE);
        }
    }
}
