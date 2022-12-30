package com.tencent.liteav.demo.player.demo.feed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.tencent.liteav.demo.player.demo.FeedActivity;
import com.tencent.liteav.demo.player.demo.FeedDetailActivity;
import com.tencent.liteav.demo.player.demo.feed.feedlistview.FeedListCallBack;
import com.tencent.liteav.demo.player.demo.feed.feedlistview.FeedListItemView;
import com.tencent.liteav.demo.player.demo.feed.feedlistview.FeedListView;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;

import java.io.Serializable;
import java.util.List;


public class FeedView extends FrameLayout implements FeedListCallBack {

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

    /**
     * 初始化界面元素
     */
    private void initViews() {
        feedListView = new FeedListView(getContext());
        feedListView.setFeedListCallBack(this);
        addView(feedListView);
    }


    /**
     * 设置回调接口
     *
     * @param callBack
     */
    public void setFeedViewCallBack(FeedViewCallBack callBack) {
        feedViewCallBack = callBack;
    }


    /**
     * 添加数据
     *
     * @param videoModels 数据列表
     * @param isCleanData 是否清理之前的数据
     */
    public void addData(List<VideoModel> videoModels, boolean isCleanData) {
        feedListView.addData(videoModels, isCleanData);
    }


    /**
     * 结束下拉刷新
     *
     * @param success
     */
    public void finishRefresh(boolean success) {
        feedListView.finishRefresh(success);
    }

    /**
     * 结束上拉加载
     *
     * @param success
     * @param noMoreData
     */
    public void finishLoadMore(boolean success, boolean noMoreData) {
        feedListView.finishLoadMore(success, noMoreData);
    }

    /**
     * 播放
     */
    public void onResume() {
        if (feedListItemView != null) {
            feedListItemView.setIsPaused(false);
        }
        feedListView.onResume();
    }

    /**
     * 暂停
     */
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
        getContext().startActivity(intent);
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


    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (feedListView != null) {
            feedListView.onRequestPermissionsResult(requestCode,grantResults);
        }
    }

}
