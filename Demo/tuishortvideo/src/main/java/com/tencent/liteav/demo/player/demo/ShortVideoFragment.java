package com.tencent.liteav.demo.player.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.tencent.liteav.demo.player.demo.tuishortvideo.R;
import com.tencent.liteav.demo.player.demo.tuishortvideo.TUILandScapeActivity;
import com.tencent.liteav.demo.player.demo.tuishortvideo.TUIShortVideoLiveActivity;
import com.tencent.liteav.demo.player.demo.tuishortvideo.common.DemoSVGlobalConfig;
import com.tencent.liteav.demo.player.demo.tuishortvideo.common.SVDemoConstants;
import com.tencent.liteav.demo.player.demo.tuishortvideo.data.ShortVideoModel;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.TUILayerBridge;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.custom.img.PicDisplayLayer;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.live.TUILiveEntranceLayer;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.live.TUILiveErrorLayer;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.live.TUILiveLoadingLayer;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.refresh.TUIFooterView;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.refresh.TUIHeaderView;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.refresh.TUIRefreshLayout;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.vod.TUICoverLayer;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.vod.TUIErrorLayer;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.vod.TUILoadingLayer;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.vod.TUIVideoInfoLayer;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.vod.message.DemoVodLayerMessenger;
import com.tencent.liteav.demo.player.demo.tuishortvideo.ui.TUIToolsController;
import com.tencent.qcloud.tuiplayer.core.api.TUIPlayerLiveStrategy;
import com.tencent.qcloud.tuiplayer.core.api.TUIPlayerVodStrategy;
import com.tencent.qcloud.tuiplayer.core.api.common.TUIConstants;
import com.tencent.qcloud.tuiplayer.core.api.common.TUIErrorCode;
import com.tencent.qcloud.tuiplayer.core.api.model.TUIPlaySource;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.custom.TUICustomLayerManager;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.live.TUILiveLayerManager;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.vod.TUIVodLayerManager;
import com.tencent.qcloud.tuiplayer.shortvideo.common.TUIVideoConst;
import com.tencent.qcloud.tuiplayer.shortvideo.ui.view.TUIShortVideoListener;
import com.tencent.qcloud.tuiplayer.shortvideo.ui.view.TUIShortVideoView;

import java.util.List;

public class ShortVideoFragment extends Fragment implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener, TUILayerBridge {
    private static final String TAG = "ShortVideoFragment";

    private ImageButton mBack;
    private TUIShortVideoView mShortVideoView;
    private TUIToolsController mToolsController;
    private TUIRefreshLayout mRefreshLayout;

    private boolean mNeedPause = true;
    private boolean isPausedBeforeBackground = false;

    private final TUIShortVideoListener mListener = new TUIShortVideoListener() {

        @Override
        public void onCreateVodLayer(TUIVodLayerManager layerManger, int viewType) {
            DemoVodLayerMessenger mVodLayerMessenger = new DemoVodLayerMessenger();
            TUILoadingLayer loadingLayer = new TUILoadingLayer();
            mVodLayerMessenger.addEvent(loadingLayer);
            TUIErrorLayer errorLayer = new TUIErrorLayer();
            mVodLayerMessenger.addEvent(errorLayer);
            layerManger.addLayer(errorLayer);
            layerManger.addLayer(new TUIVideoInfoLayer(mShortVideoView, mVodLayerMessenger,
                    ShortVideoFragment.this));
            layerManger.addLayer(loadingLayer);
            layerManger.addLayer(new TUICoverLayer());
        }

        @Override
        public void onCreateLiveLayer(TUILiveLayerManager layerManager, int viewType) {
            layerManager.addLayer(new TUILiveEntranceLayer(mShortVideoView, ShortVideoFragment.this));
            layerManager.addLayer(new TUILiveLoadingLayer());
            layerManager.addLayer(new TUILiveErrorLayer());
        }

        @Override
        public void onCreateCustomLayer(TUICustomLayerManager layerManager, int viewType) {
            if (viewType == SVDemoConstants.CustomSourceType.SINGLE_IMG_TYPE) {
                layerManager.addLayer(new PicDisplayLayer());
            }
        }

        @Override
        public void onPageChanged(int index, TUIPlaySource videoSource) {
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.player_fragment_tui_short_video_play, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(savedInstanceState);
    }

    private void initViews(@Nullable Bundle savedInstanceState) {
        mShortVideoView = requireActivity().findViewById(R.id.super_short_video_view_play_fragment);
        mRefreshLayout = requireActivity().findViewById(R.id.rl_refresh);
        FrameLayout settingContainer = requireActivity().findViewById(R.id.fl_setting_container);
        mBack = requireActivity().findViewById(R.id.ib_back_play);
        mBack.setOnClickListener(this);
        mShortVideoView.setListener(mListener);
        mRefreshLayout.setAutoLoadMore(false);
        mRefreshLayout.setHeaderView(new TUIHeaderView(requireActivity()));
        mRefreshLayout.setFooterView(new TUIFooterView(requireActivity()));
        mRefreshLayout.setOnLoadMoreListener(new TUIRefreshLayout.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                ShortVideoModel.getInstance().loadMore(false);
            }
        });
        mRefreshLayout.setOnRefreshListener(new TUIRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ShortVideoModel.getInstance().loadMore(true);
            }
        });

        // set strategy of shortVideo
        TUIPlayerVodStrategy vodStrategy = new TUIPlayerVodStrategy.Builder()
                .setIsRetainPreVod(true)
                .setRenderMode(TUIConstants.TUIRenderMode.FULL_FILL_SCREEN)
                .build();
        mShortVideoView.setVodStrategy(vodStrategy);

        TUIPlayerLiveStrategy liveStrategy = new TUIPlayerLiveStrategy.Builder()
                .setIsRetainPreLive(false)
                .setRenderMode(TUIConstants.TUIRenderMode.ADJUST_RESOLUTION)
                .build();
        mShortVideoView.setLiveStrategy(liveStrategy);
        mShortVideoView.setItemAnimator(null);
        DemoSVGlobalConfig.instance().initParams(0, false, false,
                TUIVideoConst.ListPlayMode.MODE_ONE_LOOP);
        mToolsController = new TUIToolsController(settingContainer, mShortVideoView, vodStrategy, liveStrategy,
                this);
        ShortVideoModel.getInstance().loadMore(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == TUIShortVideoLiveActivity.LIVE_CALL_BACK || resultCode == TUILandScapeActivity.LAND_SCAPE_CALL_BACK) {
            TUIPlaySource playSource = mShortVideoView.getDataManager().getCurrentModel();
            if (null != playSource) {
                playSource.setExtInfoAndNotify(SVDemoConstants.obtainEmptyEvent(SVDemoConstants.LayerEventCode.PLUG_RENDER_VIEW));
            }
        }
    }

    @Override
    public void onClick(View v) {
        final int vId = v.getId();
        if (vId == R.id.ib_back_play) {
            requireActivity().finish();
        } else {
            Log.i(TAG, "onClick in other case");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!mShortVideoView.isPlaying()) {
            isPausedBeforeBackground = true;
        } else if (mNeedPause) {
            mShortVideoView.pause();
        } else {
            mNeedPause = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isPausedBeforeBackground) {
            mShortVideoView.resume();
        }
        isPausedBeforeBackground = false;
    }

    public void onLoaded(List<TUIPlaySource> shortVideoBeanList, boolean isRefresh) {
        if (mShortVideoView != null) {
            mRefreshLayout.finishRefresh(true);
            mRefreshLayout.finishLoadMore(true, true);
            int ret;
            if (isRefresh) {
                ret = mShortVideoView.setModels(shortVideoBeanList);
            } else {
                ret = mShortVideoView.appendModels(shortVideoBeanList);
            }
            if (ret != TUIErrorCode.TUI_ERROR_NONE) {
                Toast.makeText(requireActivity().getApplicationContext(),
                        "fill data error, ret:" + ret, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRefresh() {
        ShortVideoModel.getInstance().loadMore(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        // prevent RefreshLayout memory leak
        mRefreshLayout.finishRefresh(true);
        mRefreshLayout.finishLoadMore(true, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // clear glide memory
        mShortVideoView.release();
        Glide.get(requireActivity()).clearMemory();
    }


    @Override
    public void setNeedPauseOnce(boolean isNeed) {
        mNeedPause = isNeed;
    }

    @Override
    public void postExtInfoToCurLayer(Object obj) {
        TUIPlaySource playSource = mShortVideoView.getDataManager().getCurrentModel();
        if (null != playSource) {
            playSource.setExtInfoAndNotify(obj);
        }
    }
}