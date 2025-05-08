package com.tencent.liteav.demo.player.demo.tuishortvideo.layer.live;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.liteav.demo.player.demo.tuishortvideo.R;
import com.tencent.liteav.demo.player.demo.tuishortvideo.TUILandScapeActivity;
import com.tencent.liteav.demo.player.demo.tuishortvideo.TUIShortVideoLiveActivity;
import com.tencent.liteav.demo.player.demo.tuishortvideo.common.DemoSVGlobalConfig;
import com.tencent.liteav.demo.player.demo.tuishortvideo.common.SVDemoConstants;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.TUILayerBridge;
import com.tencent.qcloud.tuiplayer.core.api.TUIPlayerController;
import com.tencent.qcloud.tuiplayer.core.api.model.TUILiveSource;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.TUILiveLayer;
import com.tencent.qcloud.tuiplayer.shortvideo.ui.view.TUIShortVideoView;

import java.util.Map;

public class TUILiveEntranceLayer extends TUILiveLayer {

    private TextView mTvEntrance;
    private TUIShortVideoView mVideoView;
    private final TUILayerBridge mLayerBridge;
    private ImageView mFullScreenBtn;


    public TUILiveEntranceLayer(TUIShortVideoView videoView, TUILayerBridge bridge) {
        mVideoView = videoView;
        mLayerBridge = bridge;
    }

    @Override
    public View createView(final ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.tuiplayer_live_entrance_layer, parent, false);
        mTvEntrance = view.findViewById(R.id.tv_click_to_enter_live);
        mFullScreenBtn = view.findViewById(R.id.iv_tui_fullscreen);
        mFullScreenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != getPlayer() && null != getVideoView()) {
                    mLayerBridge.setNeedPauseOnce(false);
                    TUILandScapeActivity.startLandScapeActivity(getPlayer(), v.getContext());
                }
            }
        });
        mTvEntrance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayerBridge.setNeedPauseOnce(false);
                TUIShortVideoLiveActivity.startLiveActivity(getPlayer(), parent.getContext());
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != getPlayer()) {
                    if (getPlayer().isPlaying()) {
                        getPlayer().pause();
                    } else {
                        getPlayer().resumePlay();
                    }
                }
            }
        });
        return view;
    }

    @Override
    public void onControllerBind(TUIPlayerController controller) {
        super.onControllerBind(controller);
        show();
        DemoSVGlobalConfig.instance().applyPlayerGlobalConfig(getPlayer());
    }

    @Override
    public void onExtInfoChanged(TUILiveSource videoSource) {
        super.onExtInfoChanged(videoSource);
        int code = SVDemoConstants.getEmptyEvent((Map<String, Object>) videoSource.getExtInfo());
        if (code == SVDemoConstants.LayerEventCode.PLUG_RENDER_VIEW) {
            if (getPlayer() != null) {
                getVideoView().getDisplayView().handleRenderRecycle();
                getPlayer().setDisplayView(getVideoView().getDisplayView());
            }
        } else {
            DemoSVGlobalConfig.instance().applyExtInfoConfig(videoSource.getExtInfo(), getPlayer());
        }
    }

    @Override
    public String tag() {
        return "TUILiveEntranceLayer";
    }
}
