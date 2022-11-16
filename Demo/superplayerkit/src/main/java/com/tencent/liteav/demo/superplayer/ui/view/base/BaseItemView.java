package com.tencent.liteav.demo.superplayer.ui.view.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.liteav.demo.superplayer.R;

public abstract class BaseItemView<DATA> extends RelativeLayout {

    public Context         mContext;

    public RelativeLayout  mRootView;

    public TextView       mTextView;

    public RelativeLayout mBackGround;

    public BaseItemView(Context context) {
        super(context);
        init(context);
    }

    public BaseItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BaseItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mRootView = (RelativeLayout) LayoutInflater.from(getContext())
                .inflate(R.layout.superplayer_vod_base_item_view, this, false);
        mBackGround = (RelativeLayout) mRootView.findViewById(R.id.rv_item_data);
        mTextView = (TextView) mRootView.findViewById(R.id.tv_item);
        addView(mRootView);
    }


    protected abstract void bindItemData(DATA data,int currentPosition,int position);
}
