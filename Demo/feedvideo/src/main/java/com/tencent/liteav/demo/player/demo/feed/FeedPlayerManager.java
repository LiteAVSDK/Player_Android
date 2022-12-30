package com.tencent.liteav.demo.player.demo.feed;

import androidx.annotation.NonNull;

import com.tencent.liteav.demo.player.demo.feed.player.FeedPlayerView;

public class FeedPlayerManager {

    private boolean        isPlaying      = false;
    private FeedPlayerView feedPlayerView = null;
    private int            lastPosition   = -1;


    /**
     * 将正在播放的item添加进来
     *
     * @param playerView
     * @param position
     */
    public void setPlayingFeedPlayerView(FeedPlayerView playerView, int position) {
        if (lastPosition == position) {
            return;
        }
        if (feedPlayerView != null) {
            feedPlayerView.pause();
        }
        lastPosition = position;
        feedPlayerView = playerView;
    }


    public void removePlayingFeedPlayerView(int position) {
        if (lastPosition == position) {
            isPlaying = false;
            lastPosition = -1;
            feedPlayerView = null;
        }
    }


    public int getLastPosition() {
        return lastPosition;
    }


    public void onResume() {
        if (isPlaying && feedPlayerView != null) {
            isPlaying = false;
            feedPlayerView.resume();
        }
    }

    public void onPause() {
        if (feedPlayerView == null) {
            return;
        }
        if (feedPlayerView.isPlaying()) {
            isPlaying = true;
        }
        feedPlayerView.pause();
    }

    public void reset() {
        isPlaying = false;
        lastPosition = -1;
        feedPlayerView = null;
    }

    public void destroy() {
        if (feedPlayerView != null) {
            feedPlayerView.stop();
            feedPlayerView.destroy();
        }
        reset();
    }

    public boolean isPlaying() {
        if (feedPlayerView != null) {
            return feedPlayerView.isPlaying();
        }
        return false;
    }


    /**
     * 将全屏模式设置为窗口模式
     *
     * @return true 表示消费了此次事件，
     */
    public boolean setWindowPlayMode() {
        if (feedPlayerView != null && feedPlayerView.isFullScreenPlay()) {
            feedPlayerView.setWindowPlayMode();
            return true;
        }
        return false;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        feedPlayerView.onRequestPermissionsResult(requestCode,grantResults);
    }

}
