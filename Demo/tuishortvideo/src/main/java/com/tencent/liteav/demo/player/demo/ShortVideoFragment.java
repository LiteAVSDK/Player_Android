package com.tencent.liteav.demo.player.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.tencent.liteav.demo.player.demo.tuishortvideo.R;
import com.tencent.liteav.demo.player.demo.tuishortvideo.data.ShortVideoModel;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.TUICoverLayer;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.TUIErrorLayer;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.TUILoadingLayer;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.TUIVideoInfoLayer;
import com.tencent.qcloud.tuiplayer.core.api.TUIVideoStrategy;
import com.tencent.qcloud.tuiplayer.core.api.model.TUIVideoSource;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.TUILayerManger;
import com.tencent.qcloud.tuiplayer.shortvideo.ui.view.TUIShortVideoListener;
import com.tencent.qcloud.tuiplayer.shortvideo.ui.view.TUIShortVideoView;

import java.util.List;

public class ShortVideoFragment extends Fragment implements View.OnClickListener,
        TUIShortVideoListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "ShortVideoFragment";

    private ImageButton mBack;
    private TUIShortVideoView mSuperShortVideoView;
    private SwipeRefreshLayout mShortViewRefresh;

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
        mSuperShortVideoView = requireActivity().findViewById(R.id.super_short_video_view_play_fragment);
        mShortViewRefresh = requireActivity().findViewById(R.id.spl_tui_refresh);
        mBack = requireActivity().findViewById(R.id.ib_back_play);
        mBack.setOnClickListener(this);
        mSuperShortVideoView.setActivityLifecycle(getLifecycle());
        mSuperShortVideoView.setListener(this);
        // set strategy of shortVideo
        mSuperShortVideoView.setStrategy(new TUIVideoStrategy.Builder().build());
        mShortViewRefresh.setOnRefreshListener(this);
        mShortViewRefresh.setRefreshing(true);
        ShortVideoModel.getInstance(requireActivity()).loadMore(true);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ib_back_play) {
            requireActivity().finish();
        } else {
            Log.i(TAG, "onClick in other case");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onLoaded(List<TUIVideoSource> shortVideoBeanList, boolean isRefresh) {
        if (mSuperShortVideoView != null) {
            mShortViewRefresh.setRefreshing(false);
            if (isRefresh) {
                int ret = mSuperShortVideoView.setModels(shortVideoBeanList);
            } else {
                int ret = mSuperShortVideoView.appendModels(shortVideoBeanList);
            }
        }
    }

    @Override
    public void onCreateItemLayer(TUILayerManger layerManger, int viewType) {
        layerManger.addLayer(new TUICoverLayer());
        layerManger.addLayer(new TUIVideoInfoLayer());
        layerManger.addLayer(new TUILoadingLayer());
        layerManger.addLayer(new TUIErrorLayer());
    }


    @Override
    public void onPageChanged(int index, TUIVideoSource videoSource) {
        if (index >= mSuperShortVideoView.getCurrentDataCount() - 1) {
            mShortViewRefresh.setRefreshing(true);
            ShortVideoModel.getInstance(getContext()).loadMore(false);
        }
    }

    @Override
    public void onNetStatus(TUIVideoSource model, Bundle bundle) {
    }

    @Override
    public void onRefresh() {
        ShortVideoModel.getInstance(getContext()).loadMore(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // clear glide memory
        Glide.get(requireActivity()).clearMemory();
    }
}
