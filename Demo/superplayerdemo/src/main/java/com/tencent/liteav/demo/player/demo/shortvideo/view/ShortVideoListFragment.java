package com.tencent.liteav.demo.player.demo.shortvideo.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.player.demo.shortvideo.adapter.ShortVideoListAdapter;
import com.tencent.liteav.demo.player.demo.shortvideo.base.AbsBaseFragment;
import com.tencent.liteav.demo.player.demo.shortvideo.bean.ShortVideoBean;

import java.util.ArrayList;
import java.util.List;


public class ShortVideoListFragment extends AbsBaseFragment {
    private static final String TAG = "ShortVideoDemo:ShortVideoListFragment";
    private RecyclerView mRecyclerView;

    private ShortVideoListAdapter mAdapter;

    private ImageButton mBackList;

    private ShortVideoListAdapter.IOnItemClickListener mIOnItemClickListener;

    private List<ShortVideoBean> mShortVideoBeanList;

    public ShortVideoListFragment() {
    }

    public ShortVideoListFragment(ShortVideoListAdapter.IOnItemClickListener listener) {
        mIOnItemClickListener = listener;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.player_fragment_short_video_list;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        mShortVideoBeanList = new ArrayList<>();
        mBackList = getActivity().findViewById(R.id.ib_back);
        mRecyclerView = getActivity().findViewById(R.id.recycler_view_short_video_list);
        mBackList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ShortVideoActivity) getActivity()).setCurrentItemPlayFragment();
            }
        });
    }


    @Override
    protected void initData() {
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void onLoaded(List<ShortVideoBean> shortVideoBeanList) {
        mShortVideoBeanList = shortVideoBeanList;
        mAdapter = new ShortVideoListAdapter(getContext(), mIOnItemClickListener, mShortVideoBeanList);
        final RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRecyclerView.setLayoutManager(layoutManager);
                    mRecyclerView.setAdapter(mAdapter);
                }
            });
        }
    }
}
