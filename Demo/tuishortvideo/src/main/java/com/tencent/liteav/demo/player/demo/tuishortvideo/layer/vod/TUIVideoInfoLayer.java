package com.tencent.liteav.demo.player.demo.tuishortvideo.layer.vod;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.demo.player.demo.tuishortvideo.R;
import com.tencent.liteav.demo.player.demo.tuishortvideo.TUILandScapeActivity;
import com.tencent.liteav.demo.player.demo.tuishortvideo.common.DemoSVGlobalConfig;
import com.tencent.liteav.demo.player.demo.tuishortvideo.common.SVDemoConstants;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.TUILayerBridge;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.adapter.TUIQualityListAdapter;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.comment.TUICommentDemoMenu;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.vod.message.DemoVodLayerEventConstants;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.vod.message.DemoVodLayerMessenger;
import com.tencent.liteav.demo.player.demo.tuishortvideo.tools.Utils;
import com.tencent.liteav.demo.player.demo.tuishortvideo.view.VideoSeekBar;
import com.tencent.qcloud.tuiplayer.core.api.TUIPlayerController;
import com.tencent.qcloud.tuiplayer.core.api.common.TUIConstants;
import com.tencent.qcloud.tuiplayer.core.api.model.TUIPlayerBitrateItem;
import com.tencent.qcloud.tuiplayer.core.api.model.TUIVideoSource;
import com.tencent.qcloud.tuiplayer.core.api.ui.player.ITUIVodPlayer;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.TUIVodLayer;
import com.tencent.qcloud.tuiplayer.shortvideo.common.TUIVideoConst;
import com.tencent.qcloud.tuiplayer.shortvideo.ui.view.TUIShortVideoView;

import java.util.List;
import java.util.Map;

public class TUIVideoInfoLayer extends TUIVodLayer implements VideoSeekBar.VideoSeekListener,
        TUIQualityListAdapter.QualitySelectedListener {

    private VideoSeekBar mSeekBar;
    private TextView mTvProgress;
    private ImageView mIvPause;
    private RecyclerView mQualityListView;
    private ConstraintLayout mQualityContainer;
    private TextView mResolutionSwitchView;
    private SwitchCompat mGlobalSwitcher;
    private long videoDuration = 0;
    private int lastProgressInt = 0;
    private TUIQualityListAdapter mQualityListAdapter;
    private boolean mWaitResolutionChanged = false;
    private final TUIShortVideoView mVideoView;
    private TUICommentDemoMenu mCommentContainer;
    private ImageView mFullScreenBtn;
    private final TUILayerBridge mLayerBridge;
    private final DemoVodLayerMessenger mMessenger;

    public TUIVideoInfoLayer(TUIShortVideoView view, DemoVodLayerMessenger messenger, TUILayerBridge bridge) {
        mVideoView = view;
        mLayerBridge = bridge;
        mMessenger = messenger;
    }

    @Override
    public View createView(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.tuiplayer_vod_info_layer, parent, false);
        mSeekBar = view.findViewById(R.id.vsb_tui_video_progress);
        mTvProgress = view.findViewById(R.id.tv_tui_progress_time);
        mIvPause = view.findViewById(R.id.iv_tui_pause);
        mQualityContainer = view.findViewById(R.id.cl_quality_container);
        mQualityListView = view.findViewById(R.id.rv_quality_list);
        mResolutionSwitchView = view.findViewById(R.id.tv_call_resolution);
        mGlobalSwitcher = view.findViewById(R.id.st_global);
        mCommentContainer = view.findViewById(R.id.tdm_comment_list);
        mFullScreenBtn = view.findViewById(R.id.iv_tui_fullscreen);
        ImageView clComment = view.findViewById(R.id.iv_tui_comment);

        mQualityListView.setAdapter(mQualityListAdapter = new TUIQualityListAdapter());
        mQualityListAdapter.setSelectedListener(this);
        mFullScreenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != getPlayer() && null != getVideoView()) {
                    mLayerBridge.setNeedPauseOnce(false);
                    // prevent TUI list play next when video int landscapeActivity
                    mVideoView.setPlayMode(TUIVideoConst.ListPlayMode.MODE_ONE_LOOP);
                    TUILandScapeActivity.startLandScapeActivity(getPlayer(), v.getContext());
                }
            }
        });
        mGlobalSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SVDemoConstants.setQualityGlobalSwitch(isChecked);
            }
        });
        clComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommentContainer.toggle(getVideoView(), getPlayer());
            }
        });

        mResolutionSwitchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResolutionSwitchView.setEnabled(false);
                mQualityContainer.setVisibility(View.VISIBLE);
                refreshQualityData();
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isIdle = true;
                if (!mResolutionSwitchView.isEnabled()) {
                    isIdle = false;
                    mResolutionSwitchView.setEnabled(true);
                    mQualityContainer.setVisibility(View.GONE);
                }
                if (mCommentContainer.isShow()) {
                    isIdle = false;
                    mCommentContainer.dismiss(getVideoView(), getPlayer());
                }
                ITUIVodPlayer player = getPlayer();
                if (isIdle && null != player) {
                    if (player.isPlaying()) {
                        player.pause();
                    } else {
                        mMessenger.sendEmptyMsg(DemoVodLayerEventConstants.SHOW_LOADING);
                        player.resumePlay();
                    }
                }
            }
        });
        return view;
    }

    private void refreshQualityData() {
        ITUIVodPlayer player = getPlayer();
        if (null != player) {
            List<TUIPlayerBitrateItem> resulutionList = player.getSupportResolution();
            mQualityListAdapter.setData(resulutionList);
            int currentResolutionIndex = player.getBitrateIndex();
            int qualityIndex = -1;
            for (int i = 0; i < resulutionList.size(); i++) {
                TUIPlayerBitrateItem item = resulutionList.get(i);
                if (item.getIndex() == currentResolutionIndex) {
                    qualityIndex = i;
                    break;
                }
            }
            mQualityListAdapter.setQualityIndex(qualityIndex);
        }
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
        show();
        mSeekBar.setListener(this);
        mGlobalSwitcher.setChecked(SVDemoConstants.isQualityGlobalOpen());
        DemoSVGlobalConfig.instance().applyPlayerGlobalConfig(getPlayer());
    }

    @Override
    public void onControllerUnBind(TUIPlayerController controller) {
        resetView();
    }

    private void resetView() {
        mWaitResolutionChanged = false;
        mQualityContainer.setVisibility(View.GONE);
        mResolutionSwitchView.setEnabled(true);
        mCommentContainer.dismiss(getVideoView(), getPlayer());
        if (null != mSeekBar) {
            mSeekBar.setListener(null);
        }
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
    public String tag() {
        return "TUIVideoInfoLayer";
    }

    @Override
    public void onDragBarChanged(VideoSeekBar seekBar, float progress, final float barProgress) {
        if (null != mTvProgress) {
            mTvProgress.post(new Runnable() {
                @Override
                public void run() {
                    String timeStr = Utils.formattedTime((long) (videoDuration * barProgress) / 1000)
                            + "/"
                            + Utils.formattedTime(videoDuration / 1000);
                    mTvProgress.setText(timeStr);
                }
            });
        }
    }

    @Override
    public void onResolutionChanged(long width, long height) {
        super.onResolutionChanged(width, height);
        if (mWaitResolutionChanged) {
            mWaitResolutionChanged = false;
            Toast.makeText(getView().getContext().getApplicationContext(), "resolution switch suc:" + width + "*" + height,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStartDrag(VideoSeekBar seekBar) {
        if (null != mTvProgress) {
            mTvProgress.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPlayEnd() {
        if (null != mIvPause) {
            mIvPause.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onExtInfoChanged(TUIVideoSource videoSource) {
        super.onExtInfoChanged(videoSource);
        int code = SVDemoConstants.getEmptyEvent((Map<String, Object>) videoSource.getExtInfo());
        if (code == SVDemoConstants.LayerEventCode.PLUG_RENDER_VIEW) {
            if (getPlayer() != null) {
                getPlayer().setDisplayView(getVideoView().getDisplayView());
                getPlayer().resumePlay();
            }
            mVideoView.setPlayMode(DemoSVGlobalConfig.instance().getPlayMode());
        }  else {
            DemoSVGlobalConfig.instance().applyExtInfoConfig(videoSource.getExtInfo(), getPlayer());
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

    @Override
    public void onSelected(TUIPlayerBitrateItem item, int pos) {
        long resolution = item.getWidth() * (long) item.getHeight();
        if (mGlobalSwitcher.isChecked()) {
            mVideoView.switchResolution(resolution, TUIConstants.TUIResolutionType.GLOBAL);
        } else {
            mVideoView.switchResolution(resolution, TUIConstants.TUIResolutionType.CURRENT);
        }
        Toast.makeText(getView().getContext().getApplicationContext(), "start switch resolution",
                Toast.LENGTH_LONG).show();
        mWaitResolutionChanged = true;
    }
}
