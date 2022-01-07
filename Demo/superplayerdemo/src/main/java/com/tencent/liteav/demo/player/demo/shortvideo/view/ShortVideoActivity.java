package com.tencent.liteav.demo.player.demo.shortvideo.view;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.player.demo.shortvideo.adapter.ShortVideoListAdapter;
import com.tencent.liteav.demo.player.demo.shortvideo.adapter.ShortVideoPageAdapter;
import com.tencent.liteav.demo.player.demo.shortvideo.base.AbsBaseActivity;
import com.tencent.liteav.demo.player.demo.shortvideo.bean.ShortVideoBean;
import com.tencent.liteav.demo.player.demo.shortvideo.core.ShortVideoModel;

import java.util.ArrayList;
import java.util.List;

public class ShortVideoActivity extends AbsBaseActivity implements ViewPager.OnPageChangeListener, ShortVideoListAdapter.IOnItemClickListener, ShortVideoModel.IOnDataLoadFullListener {

    private static final String TAG = "ShortVideoDemo:ShortVideoActivity";

    private static final int PLAY_FRAGMENT = 0;

    private static final int LIST_FRAGMENT = 1;


    private ViewPager mViewPager;

    private List<Fragment> mFragmentList;

    private ShortVideoPlayFragment mPlayFragment;

    private ShortVideoListFragment mListFragment;

    @Override
    protected void initLayout(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.player_activity_shortvideo);
    }

    @Override
    protected void initView() {
        ShortVideoModel.getInstance().setOnDataLoadFullListener(this);
        mViewPager = findViewById(R.id.viewpager_short_video);
    }

    @Override
    protected void initData() {
        mFragmentList = new ArrayList<>();
        mPlayFragment = new ShortVideoPlayFragment();
        mListFragment = new ShortVideoListFragment();
        mFragmentList.add(mPlayFragment);
        mListFragment = new ShortVideoListFragment(this);
        mFragmentList.add(mListFragment);
        mViewPager.setAdapter(new ShortVideoPageAdapter(getSupportFragmentManager(), mFragmentList));
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setCurrentItem(PLAY_FRAGMENT);
        ShortVideoModel.getInstance().loadDefaultVideo();
        ShortVideoModel.getInstance().getVideoByFileId();
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (position == LIST_FRAGMENT) {
            mPlayFragment.onListPageScrolled();
        } else if (position == PLAY_FRAGMENT) {
            Log.i(TAG, "onPageScrolled of play fragment");
        } else {
            Log.i(TAG, "onPageScrolled other case");
        }
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onItemClick(int position) {
        mViewPager.setCurrentItem(PLAY_FRAGMENT);
        mPlayFragment.onItemClick(position);
        Log.i(TAG, "from list position " + position);
    }


    public void setCurrentItemPlayFragment() {
        mViewPager.setCurrentItem(PLAY_FRAGMENT);
    }

    @Override
    public void onLoaded(List<ShortVideoBean> videoBeanList) {
        mListFragment.onLoaded(videoBeanList);
        mPlayFragment.onLoaded(videoBeanList);
        ShortVideoModel.getInstance().release();
    }
}
