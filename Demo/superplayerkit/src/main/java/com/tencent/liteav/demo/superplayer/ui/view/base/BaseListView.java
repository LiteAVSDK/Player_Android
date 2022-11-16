package com.tencent.liteav.demo.superplayer.ui.view.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.demo.superplayer.R;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseListView<ADAPTER extends BaseAdapter,DATA>  extends RelativeLayout {

    public Context mContext;

    public TextView mTextView;

    public RecyclerView mBaseRecyclerView;

    public ADAPTER mAdapter;

    public List<DATA> mData;

    public RelativeLayout  mRootView;


    public BaseListView(Context context) {
        super(context);
        init(context);
    }

    public BaseListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BaseListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        mContext = context;
        mData = new ArrayList<>();
        mRootView = (RelativeLayout) LayoutInflater.from(mContext)
                .inflate(R.layout.superplayer_vod_base_list_view, this);
        mTextView = mRootView.findViewById(R.id.base_title);
        mTextView.setText(getTitle());
        mBaseRecyclerView = mRootView.findViewById(R.id.base_data_recycler_view);
        mBaseRecyclerView.setHasFixedSize(true);
        mBaseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = getAdapter();
        mBaseRecyclerView.setAdapter(mAdapter);
    }


    public void setCurrentPosition(int position) {
        mAdapter.mCurrentPositionInAdapter = position;
    }

    /**
     * 设置画质列表
     *
     * @param list
     */
    public void setModelList(List<DATA> list) {
        mData.clear();
        mData.addAll(list);
        mAdapter.setData(list);
    }

    protected abstract String getTitle();

    protected abstract ADAPTER getAdapter();

}
