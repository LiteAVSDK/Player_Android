package com.tencent.liteav.demo.player.feed;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.tencent.liteav.demo.player.expand.model.entity.VideoModel;
import com.tencent.liteav.demo.player.feed.feeddetailview.FeedDetailView;
import com.tencent.liteav.demo.player.feed.feeddetailview.FeedDetailViewCallBack;
import com.tencent.liteav.demo.player.feed.feedlistview.FeedListCallBack;
import com.tencent.liteav.demo.player.feed.feedlistview.FeedListItemView;
import com.tencent.liteav.demo.player.feed.feedlistview.FeedListView;
import com.tencent.liteav.demo.player.feed.player.FeedPlayerView;

import java.util.List;


public class FeedView extends FrameLayout implements FeedListCallBack, FeedDetailViewCallBack {

    private FeedListView     feedListView     = null;
    private FeedDetailView   feedDetailView   = null;
    private FeedViewCallBack feedViewCallBack = null;
    private FeedListItemView feedListItemView = null;   //被点击的item
    private boolean          isShowDetailView = false;   //是否展示了详情页面


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
        feedDetailView = new FeedDetailView(getContext());
        feedDetailView.setFeedDetailViewCallBack(this);
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
        feedListView.onResume();
    }

    /**
     * 暂停
     */
    public void onPause() {
        feedListView.onPause();
    }

    public void onDestroy() {
        if (feedListView != null) {
            feedListView.destroy();
        }
        if (feedDetailView != null) {
            feedDetailView.destroy();
        }
    }

    /**
     * 添加详情页列表数据
     */
    public void addDetailListData(List<VideoModel> videoModels) {
        if (isShowDetailView) {
            feedDetailView.addDetailListData(videoModels);
        }
    }

    /**
     * 是否消费返回事件
     *
     * @return TRUE 表示feedView响应了此事件
     */
    public boolean goBack() {
        if (feedListView.setWindowPlayMode()) {
            return true;
        }
        if (isShowDetailView) {
            isShowDetailView = false;
            feedDetailView.removeFeedDetailView(feedListItemView == null ? 0 : feedListItemView.getTop());
            return true;
        }
        return false;
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
    public void onListItemClick(FeedPlayerManager feedPlayerManager, FeedListItemView itemView, VideoModel videoModel, int position) {
        if (isShowDetailView) {
            return;
        }
        isShowDetailView = true;
        feedListItemView = itemView;
        feedListItemView.removeFeedPlayFromItem();
        feedDetailView.showDetailView(this, videoModel, itemView.getFeedPlayerView(), Math.max(itemView.getTop(), 0));
        if (!itemView.getFeedPlayerView().isPlaying()) {
            feedPlayerManager.setPlayingFeedPlayerView(itemView.getFeedPlayerView(), position);
            itemView.resume();
        }
        if (feedViewCallBack != null) {
            feedViewCallBack.onStartDetailPage();
        }
    }

    @Override
    public void onRemoveDetailView(FeedPlayerView feedPlayerView, boolean isChangeVideo) {
        if (feedViewCallBack != null) {
            feedViewCallBack.onStopDetailPage();
        }
        if (isChangeVideo) {
            feedListItemView.play(feedListItemView.getVideoModel());
        }
        feedListItemView.addFeedPlayToItem();
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

    @Override
    public void onLoadDetailData(VideoModel videoModel) {
        if (feedViewCallBack != null) {
            feedViewCallBack.onLoadDetailData(videoModel);
        }
    }

    @Override
    public void onClickSmallReturnBtn() {
        goBack();
    }

    @Override
    public void onStartDetailFullScreenPlay(int position) {
        feedListView.scrollToPosition(position);
        if (feedViewCallBack != null) {
            feedViewCallBack.onStartFullScreenPlay();
        }
    }

    @Override
    public void onStopDetailFullScreenPlay() {
        if (feedViewCallBack != null) {
            feedViewCallBack.onStopFullScreenPlay();
        }
    }


}
