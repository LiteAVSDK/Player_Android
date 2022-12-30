package com.tencent.liteav.demo.player.demo.shortvideo.view;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.blankj.utilcode.util.ToastUtils;

import com.tencent.liteav.demo.player.demo.shortvideo.base.AbsBaseActivity;
import com.tencent.liteav.demo.player.demo.shortvideo.core.ShortVideoModel;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;
import com.tencent.liteav.shortvideoplayerdemo.R;


import java.util.List;

public class ShortVideoActivity extends AbsBaseActivity implements  ShortVideoModel.IOnDataLoadFullListener {

    private static final String TAG = "ShortVideoDemo:ShortVideoActivity";

    private ShortVideoPlayFragment mPlayFragment;

    @Override
    protected void initLayout(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.player_activity_shortvideo);
    }

    @Override
    protected void initView() {
        ShortVideoModel.getInstance(this).setOnDataLoadFullListener(this);
        mPlayFragment = new ShortVideoPlayFragment();
        FragmentManager manager = getSupportFragmentManager();
        // 开始事务 得到事务
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        // 替换操作
        fragmentTransaction.replace(R.id.player_frame_layout, mPlayFragment);
        // 提交
        fragmentTransaction.commit();
    }

    @Override
    protected void initData() {
        ShortVideoModel.getInstance(this).setOnDataLoadFullListener(this);
        ShortVideoModel.getInstance(this).loadDefaultVideo();
        ShortVideoModel.getInstance(this).getVideoByFileId();
    }

    @Override
    public void onLoadedSuccess(List<VideoModel> videoBeanList) {
        mPlayFragment.onLoaded(videoBeanList);
    }

    @Override
    protected void onDestroy() {
        ShortVideoModel.getInstance(this).release();
        ShortVideoModel.getInstance(this).setOnDataLoadFullListener(null);
        super.onDestroy();
    }

    @Override
    public void onLoadedFailed(int errCode) {
        ToastUtils.showLong(getString(R.string.short_video_get_data_failed) + errCode);
    }

}
