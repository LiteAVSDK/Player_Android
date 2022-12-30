package com.tencent.liteav.demo.player.demo.feed.feedlistview;

import com.tencent.liteav.demo.player.demo.feed.FeedPlayerManager;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;

public interface FeedListCallBack {

    void onLoadMore();

    void onRefresh();

    void onListItemClick(FeedPlayerManager feedPlayerManager,
                         FeedListItemView itemView, VideoModel videoModel, int position);

    void onStartFullScreenPlay();

    void onStopFullScreenPlay();

}
