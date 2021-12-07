package com.tencent.liteav.demo.player.feed;


import com.tencent.liteav.demo.player.expand.model.entity.VideoModel;

public interface FeedViewCallBack {


    void onLoadMore();

    void onRefresh();

    void onStartDetailPage();

    void onStopDetailPage();

    void onLoadDetailData(VideoModel videoModel);

    void onStartFullScreenPlay();

    void onStopFullScreenPlay();

}
