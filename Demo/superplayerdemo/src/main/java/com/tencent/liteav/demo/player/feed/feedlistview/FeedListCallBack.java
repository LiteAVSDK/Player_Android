package com.tencent.liteav.demo.player.feed.feedlistview;

import com.tencent.liteav.demo.player.expand.model.entity.VideoModel;
import com.tencent.liteav.demo.player.feed.FeedPlayerManager;

public interface FeedListCallBack {

    void onLoadMore();

    void onRefresh();

    void onListItemClick(FeedPlayerManager feedPlayerManager,FeedListItemView itemView, VideoModel videoModel, int position);

    void onStartFullScreenPlay();

    void onStopFullScreenPlay();

}
