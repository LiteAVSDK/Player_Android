package com.tencent.liteav.demo.player.demo.feed;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.liteav.demo.player.demo.FeedActivity;
import com.tencent.liteav.demo.player.demo.FeedDetailActivity;
import com.tencent.liteav.demo.player.demo.feed.feedlistview.FeedListCallBack;
import com.tencent.liteav.demo.player.demo.feed.feedlistview.FeedListItemView;
import com.tencent.liteav.demo.player.demo.feed.feedlistview.FeedListView;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;

import java.io.Serializable;
import java.util.List;


public class FeedView extends FrameLayout implements FeedListCallBack {
    private static final int REQ_FEED_DETAIL = 1000;

    private FeedListView feedListView     = null;
    private FeedViewCallBack feedViewCallBack = null;
    private FeedListItemView feedListItemView = null;   //被点击的item


    public FeedView(Context context) {
        super(context);
        initViews();
    }

    public FeedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public FeedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        feedListView = new FeedListView(getContext());
        feedListView.setFeedListCallBack(this);
        addView(feedListView);
    }

    public void setFeedViewCallBack(FeedViewCallBack callBack) {
        feedViewCallBack = callBack;
    }

    public void addData(List<VideoModel> videoModels, boolean isCleanData) {
        feedListView.addData(videoModels, isCleanData);
    }


    /**
     * End pull-down refresh.
     *
     * 结束下拉刷新
     *
     * @param success Whether to mark this refresh as successful.
     *                本次刷新是否置为成功
     */
    public void finishRefresh(boolean success) {
        feedListView.finishRefresh(success);
    }

    /**
     * End pull-up load.
     *
     * 结束上拉加载
     */
    public void finishLoadMore(boolean success, boolean noMoreData) {
        feedListView.finishLoadMore(success, noMoreData);
    }

    public void onResume() {
        if (feedListItemView != null) {
            feedListItemView.setIsPaused(false);
        }
        feedListView.onResume();
    }

    public void onPause() {
        if (feedListItemView != null) {
            feedListItemView.setIsPaused(true);
        }
        feedListView.onPause();
    }

    public void onDestroy() {
        if (feedListView != null) {
            feedListView.destroy();
        }
    }

    @Override
    public void onLoadMore() {
        if (feedViewCallBack != null) {
            feedViewCallBack.onLoadMore();
        }
    }

    @Override
    public void onRefresh() {
        if (feedViewCallBack != null) {
            feedViewCallBack.onRefresh();
        }
    }


    @Override
    public void onListItemClick(FeedPlayerManager feedPlayerManager,
                                FeedListItemView itemView,
                                VideoModel videoModel, int position) {
        feedListItemView = itemView;
        Intent intent = new Intent(getContext(), FeedDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(FeedActivity.KEY_VIDEO_MODEL,(Serializable) videoModel);
        intent.putExtras(bundle);
        intent.putExtra(FeedActivity.KEY_CURRENT_TIME,feedListItemView.getProgress());
        if (getContext() instanceof Activity) {
            ((Activity) getContext()).startActivityForResult(intent, REQ_FEED_DETAIL);
        } else {
            getContext().startActivity(intent);
        }
    }


    @Override
    public void onStartFullScreenPlay() {
        if (feedViewCallBack != null) {
            feedViewCallBack.onStartFullScreenPlay();
        }
    }

    @Override
    public void onStopFullScreenPlay() {
        if (feedViewCallBack != null) {
            feedViewCallBack.onStopFullScreenPlay();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQ_FEED_DETAIL && data != null) {
            long curPlayedProgress = data.getLongExtra(FeedActivity.KEY_CURRENT_TIME, -1L);
            if (curPlayedProgress >= 0) {
                feedListItemView.seek((int) curPlayedProgress);
            }
        }
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (feedListView != null) {
            feedListView.onRequestPermissionsResult(requestCode,grantResults);
        }
    }

}
