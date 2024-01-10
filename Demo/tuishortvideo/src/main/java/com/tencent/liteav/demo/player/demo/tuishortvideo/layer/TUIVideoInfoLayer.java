package com.tencent.liteav.demo.player.demo.tuishortvideo.layer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.liteav.demo.player.demo.tuishortvideo.R;
import com.tencent.liteav.demo.player.demo.tuishortvideo.view.VideoSeekBar;
import com.tencent.qcloud.tuiplayer.core.api.TUIPlayerController;
import com.tencent.qcloud.tuiplayer.core.api.tools.PlayerHelper;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.TUIBaseLayer;

public class TUIVideoInfoLayer extends TUIBaseLayer implements VideoSeekBar.VideoSeekListener {

    private VideoSeekBar mSeekBar;
    private TextView mTvProgress;
    private ImageView mIvPause;
    private long videoDuration = 0;
    private int lastProgressInt = 0;

    @Override
    public View createView(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.player_video_info_layer, parent, false);
        mSeekBar = view.findViewById(R.id.vsb_tui_video_progress);
        mTvProgress = view.findViewById(R.id.tv_tui_progress_time);
        mIvPause = view.findViewById(R.id.iv_tui_pause);
        mSeekBar.setListener(this);
        return view;
    }

    @Override
    public void onPlayBegin() {
        super.onPlayBegin();
        if (null != mIvPause) {
            mIvPause.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPlayPause() {
        super.onPlayPause();
        if (null != mIvPause) {
            mIvPause.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onControllerBind(TUIPlayerController controller) {
        super.onControllerBind(controller);
        show();
    }

    @Override
    public void onControllerUnBind(TUIPlayerController controller) {
        super.onControllerUnBind(controller);
        hidden();
    }

    @Override
    public void onPlayProgress(long current, long duration, long playable) {
        videoDuration = duration;
        if (null != mSeekBar && isShowing()) {
            // ensure a refresh at every percentage point
            int progressInt = (int) (((1.0F * current) / duration) * 100);
            if (lastProgressInt != progressInt) {
                setProgress(progressInt / 100F);
                lastProgressInt = progressInt;
            }
        }
    }

    private void setProgress(float progress) {
        mSeekBar.setAllProgress(progress);
    }

    @Override
    public void unBindLayerManager() {
        super.unBindLayerManager();
        if (null != mSeekBar) {
            mSeekBar.setListener(null);
        }
    }

    @Override
    public String tag() {
        return "TUIVideoInfoLayer";
    }

    @Override
    public void onDragBarChanged(VideoSeekBar seekBar, float progress, final float barProgress) {
        if (null != mTvProgress) {
            mTvProgress.post(new Runnable() {
                @Override
                public void run() {
                    String timeStr = PlayerHelper.formattedTime((long) (videoDuration * barProgress) / 1000)
                            + "/"
                            + PlayerHelper.formattedTime(videoDuration / 1000);
                    mTvProgress.setText(timeStr);
                }
            });
        }
    }

    @Override
    public void onStartDrag(VideoSeekBar seekBar) {
        if (null != mTvProgress) {
            mTvProgress.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDragDone(VideoSeekBar seekBar) {
        TUIPlayerController controller = getPlayerController();
        if (null != controller && videoDuration > 0) {
            controller.seekTo((int) ((videoDuration * seekBar.getBarProgress()) / 1000));
        }
        if (null != mTvProgress) {
            mTvProgress.setVisibility(View.GONE);
        }
    }
}
