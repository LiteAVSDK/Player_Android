package com.tencent.liteav.demo.player.feed.player;

import com.tencent.liteav.demo.player.expand.model.entity.VideoModel;

public interface FeedPlayer {

    void preparePlayVideo(int position, VideoModel videoModel);

    void play(VideoModel videoModel);

    void resume();

    void pause();

    void reset();

    void destroy();

    boolean isPlaying();

    boolean isFullScreenPlay();

    void setWindowPlayMode();

    void setFeedPlayerCallBack(FeedPlayerView.FeedPlayerCallBack callBack);

    FeedPlayerView.FeedPlayerCallBack getFeedPlayerCallBack();
}
