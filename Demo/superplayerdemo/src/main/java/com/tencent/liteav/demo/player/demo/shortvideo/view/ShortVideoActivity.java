package com.tencent.liteav.demo.player.demo.shortvideo.view;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.player.demo.shortvideo.adapter.ShortVideoListAdapter;
import com.tencent.liteav.demo.player.demo.shortvideo.adapter.ShortVideoPageAdapter;
import com.tencent.liteav.demo.player.demo.shortvideo.base.AbsBaseActivity;
import com.tencent.liteav.demo.player.demo.shortvideo.bean.ShortVideoBean;
import com.tencent.liteav.demo.player.demo.shortvideo.core.ShortVideoModel;
import com.tencent.rtmp.TXLog;

import java.util.ArrayList;
import java.util.List;

public class ShortVideoActivity extends AbsBaseActivity implements ViewPager.OnPageChangeListener, ShortVideoListAdapter.IOnItemClickListener, ShortVideoModel.IOnDataLoadFullListener {

    private static final String TAG = "ShortVideoDemo:ShortVideoActivity";

    private static final int PLAY_FRAGMENT = 0;

    private static final int LIST_FRAGMENT = 1;


    private ViewPager mViewPager;

    private List<Fragment> mFragmentList;

    private IOnItemClickListener mItemClickListener;

    private IOnListPageScrolledListener mIOnListPageScrolledListener;

    private List<ShortVideoBean> mShortVideoBeanList;

    private List<IOnListDataLoadedListener> mOnListDataLoadedList;

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
        mFragmentList.add(new ShortVideoPlayFragment());
        ShortVideoListFragment shortVideoListFragment = new ShortVideoListFragment(this);
        mFragmentList.add(shortVideoListFragment);
        mViewPager.setAdapter(new ShortVideoPageAdapter(getSupportFragmentManager(), mFragmentList));
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setCurrentItem(PLAY_FRAGMENT);
        mShortVideoBeanList = new ArrayList<>();
        mOnListDataLoadedList = new ArrayList<>();
        ShortVideoModel.getInstance().loadDefaultVideo();
        ShortVideoModel.getInstance().getVideoByFileId();
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (position == LIST_FRAGMENT) {
            mIOnListPageScrolledListener.onListPageScrolled();
        } else if (position == PLAY_FRAGMENT) {
            TXLog.i(TAG, "onPageScrolled of play fragment");
        } else {
            TXLog.i(TAG, "onPageScrolled other case");
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
        mItemClickListener.onItemClick(position);
        TXLog.i(TAG, "from list position " + position);
    }


    public void setCurrentItemPlayFragment() {
        mViewPager.setCurrentItem(PLAY_FRAGMENT);
    }

    interface IOnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(IOnItemClickListener listener) {
        mItemClickListener = listener;
    }

    interface IOnListPageScrolledListener {
        void onListPageScrolled();
    }

    public void setOnListPageScrolledListener(IOnListPageScrolledListener listener) {
        mIOnListPageScrolledListener = listener;
    }

    interface IOnListDataLoadedListener {
        void onLoaded(List<ShortVideoBean> shortVideoBeanList);
    }

    public void setOnListDataLoadedListener(IOnListDataLoadedListener listener) {
        mOnListDataLoadedList.add(listener);
    }

    @Override
    public void onLoaded(List<ShortVideoBean> videoBeanList) {
        for (int i = 0; i < mOnListDataLoadedList.size(); i++) {
            mOnListDataLoadedList.get(i).onLoaded(videoBeanList);
        }
        ShortVideoModel.getInstance().release();
    }
}
