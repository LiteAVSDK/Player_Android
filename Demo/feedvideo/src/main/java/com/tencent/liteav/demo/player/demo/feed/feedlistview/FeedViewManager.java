package com.tencent.liteav.demo.player.demo.feed.feedlistview;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.asynclayoutinflater.view.AsyncLayoutInflater;
import androidx.core.util.Pools;

import com.tencent.liteav.demo.feedvideo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * feedView管理
 */
public class FeedViewManager {

    private final int maxPoolSize;

    private final List<FeedListItemView>             mListItemViews = new ArrayList<>();
    private final Pools.SimplePool<FeedListItemView> mFeedViewPools;
    private final AsyncLayoutInflater                mAsyncLayoutInflater;

    private final Handler                mHandler;
    private       Context                mContext;
    private final ViewGroup.LayoutParams mLayoutParams;

    public FeedViewManager(Context context, final int listItemHeight, int maxPoolSize) {
        this.mContext = context;
        this.maxPoolSize = maxPoolSize;
        mFeedViewPools = new Pools.SimplePool<>(maxPoolSize);
        mAsyncLayoutInflater = new AsyncLayoutInflater(context);
        mLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, listItemHeight);
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void dispatchMessage(@NonNull Message msg) {
                asyncInflate();
            }
        };
        initCacheData();
    }

    private void asyncInflate() {
        mAsyncLayoutInflater.inflate(R.layout.feedview_list_item_view,
                null,
                new AsyncLayoutInflater.OnInflateFinishedListener() {
                    @Override
                    public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                        FeedListItemView feedListItemView = (FeedListItemView) view;
                        feedListItemView.setLayoutParams(mLayoutParams);
                        mFeedViewPools.release(feedListItemView);
                    }
                });
    }

    private void initCacheData() {
        int elementIndex = 0;
        while (elementIndex < maxPoolSize) {
            elementIndex++;
            mHandler.sendEmptyMessage(0);
        }
    }

    public FeedListItemView fetchFeedListItemView() {
        FeedListItemView feedListItemView = mFeedViewPools.acquire();
        if (null == feedListItemView) {
            feedListItemView = buildFeedListItemView(mContext);
        }
        mListItemViews.add(feedListItemView);
        return feedListItemView;
    }

    public void release() {
        for (FeedListItemView itemView : mListItemViews) {
            itemView.destroy();
        }
        mListItemViews.clear();
        FeedListItemView feedListItemView;
        while ((feedListItemView = mFeedViewPools.acquire()) != null) {
            feedListItemView.destroy();
        }
    }

    private FeedListItemView buildFeedListItemView(Context context) {
        FeedListItemView feedListItemView = new FeedListItemView(context);
        feedListItemView.setLayoutParams(mLayoutParams);
        return feedListItemView;
    }


}
