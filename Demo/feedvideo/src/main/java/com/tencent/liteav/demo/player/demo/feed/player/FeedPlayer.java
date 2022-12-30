package com.tencent.liteav.demo.player.demo.feed.player;

import com.tencent.liteav.demo.vodcommon.entity.VideoModel;

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

    boolean isEnd();

    void setStartTime(int progress);

    long getProgress();

    FeedPlayerView.FeedPlayerCallBack getFeedPlayerCallBack();
}
