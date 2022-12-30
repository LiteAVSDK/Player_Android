package com.tencent.liteav.demo.player.demo.feed.feedlistview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.tencent.liteav.demo.feedvideo.R;
import com.tencent.liteav.demo.player.demo.feed.FeedPlayerManager;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;

import java.util.List;

/**
 * feed流主界面使用
 * 在FrameLayout中添加SmartRefreshLayout 在SmartRefreshLayout中添加一个RecycleView
 */
public class FeedListView extends FrameLayout implements FeedListItemView.FeedListItemViewCallBack {

    private RecyclerView           recyclerView           = null;
    private FeedListCallBack feedListCallBack       = null;
    private SmartRefreshLayout     refreshLayout          = null;
    private FeedListAdapter feedListAdapter        = null;
    private FeedListScrollListener feedListScrollListener = null;
    private LinearSmoothScroller   linearSmoothScroller   = null;
    private FeedPlayerManager feedPlayerManager      = null;
    private boolean           isPaused               = false;

    public FeedListView(Context context) {
        super(context);
        initViews();
    }

    public FeedListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public FeedListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    /**
     * 初始化界面元素
     */
    private void initViews() {
        feedPlayerManager = new FeedPlayerManager();
        refreshLayout = new SmartRefreshLayout(getContext());
        refreshLayout.setBackgroundResource(R.color.feed_page_bg);
        refreshLayout.setRefreshHeader(new ClassicsHeader(getContext()));
        refreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));
        refreshLayout.setEnableAutoLoadMore(false);
        refreshLayout.setEnableOverScrollBounce(false);//是否启用越界回弹
        recyclerView = new RecyclerView(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(5);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        feedListAdapter = new FeedListAdapter(getContext(), this, feedPlayerManager);
        recyclerView.setAdapter(feedListAdapter);
        feedListScrollListener = new FeedListScrollListener(feedPlayerManager);
        recyclerView.addOnScrollListener(feedListScrollListener);
        refreshLayout.setRefreshContent(recyclerView);
        addView(refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshlayout) {
                if (feedListCallBack != null) {
                    feedListCallBack.onRefresh();
                }
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshlayout) {
                if (feedListCallBack != null) {
                    feedListCallBack.onLoadMore();
                }
            }
        });
        linearSmoothScroller = new FeedLinearSmoothScroller(getContext(), feedListAdapter.getListItemHeight());
    }


    /**
     * 设置feedlistview 回调接口
     *
     * @param callBack
     */
    public void setFeedListCallBack(FeedListCallBack callBack) {
        feedListCallBack = callBack;
    }


    /**
     * 添加列表数据
     *
     * @param videoModels 数据列表
     * @param isCleanData 是否清理之前的数据
     */
    public void addData(List<VideoModel> videoModels, boolean isCleanData) {
        feedListAdapter.addVideoData(videoModels, isCleanData);
        if (isCleanData && videoModels != null && videoModels.size() > 0) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    feedListScrollListener.firstPlayItem(recyclerView, RecyclerView.SCROLL_STATE_IDLE);
                }
            }, 500);
        }
    }

    /**
     * 结束下拉刷新动画
     *
     * @param success
     */
    public void finishRefresh(boolean success) {
        refreshLayout.finishRefresh(success);
    }

    /**
     * 结束上拉加载动画
     *
     * @param success
     * @param noMoreData
     */
    public void finishLoadMore(boolean success, boolean noMoreData) {
        refreshLayout.finishLoadMore(0, success, noMoreData);
    }


    public void onResume() {
        feedPlayerManager.onResume();
    }

    public void onPause() {
        isPaused = true;
        feedPlayerManager.onPause();
    }

    /**
     * 销毁时调用
     */
    public void destroy() {
        if (feedPlayerManager != null) {
            feedPlayerManager.destroy();
        }
        //清理掉列表数据
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
            recyclerView = null;
            refreshLayout.removeAllViews();
        }
    }


    /**
     * 点击RecycleView item 回调方法
     *
     * @param itemView
     * @param position
     */
    @Override
    public void onItemClick(FeedListItemView itemView, VideoModel videoModel, int position) {
        linearSmoothScroller.setTargetPosition(position);
        recyclerView.getLayoutManager().startSmoothScroll(linearSmoothScroller);
        if (feedListCallBack != null) {
            feedListCallBack.onListItemClick(feedPlayerManager, itemView, videoModel, position);
        }
    }

    /**
     * 播放器全屏回调方法，在此方法中将全屏事件通知给feedView
     *
     * @param feedListItemView
     * @param position
     */
    @Override
    public void onStartFullScreenPlay(FeedListItemView feedListItemView, int position) {
        recyclerView.removeOnScrollListener(feedListScrollListener);
        scrollToPosition(position);
        if (feedListCallBack != null) {
            feedListCallBack.onStartFullScreenPlay();
        }
        feedListItemView.removeFeedPlayFromItem();
        addView(feedListItemView.getFeedPlayerView()
                , new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                        , ViewGroup.LayoutParams.MATCH_PARENT));
        if (!feedListItemView.getFeedPlayerView().isPlaying() && !feedListItemView.getFeedPlayerView().isEnd()) {
            feedPlayerManager.setPlayingFeedPlayerView(feedListItemView.getFeedPlayerView(), position);
            feedListItemView.resume();
        }
    }

    public void scrollToPosition(int position) {
        if (recyclerView != null) {
            recyclerView.scrollToPosition(position);
        }
    }


    /**
     * 播放器小窗口事件，在此方法中将此事件通知feedView
     *
     * @param feedListItemView
     */
    @Override
    public void onStopFullScreenPlay(FeedListItemView feedListItemView) {
        recyclerView.addOnScrollListener(feedListScrollListener);
        if (feedListCallBack != null) {
            feedListCallBack.onStopFullScreenPlay();
        }
        feedListItemView.addFeedPlayToItem();
    }


    /**
     * 将全屏模式设置为窗口模式
     *
     * @return true 表示消费了此次事件，
     */
    public boolean setWindowPlayMode() {
        if (feedPlayerManager != null) {
            return feedPlayerManager.setWindowPlayMode();
        } else {
            return false;
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (feedPlayerManager != null) {
            feedPlayerManager.onRequestPermissionsResult(requestCode,grantResults);
        }
    }
}
