package com.tencent.liteav.demo.superplayer.model;

import com.tencent.liteav.demo.superplayer.SuperPlayerDef;
import com.tencent.liteav.demo.superplayer.model.entity.PlayImageSpriteInfo;
import com.tencent.liteav.demo.superplayer.model.entity.PlayKeyFrameDescInfo;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;
import com.tencent.rtmp.TXLivePlayer;

import java.util.List;

public abstract class SuperPlayerObserver {

    /**
     * 准备播放
     */
    public void onPlayPrepare() {

    }

    /**
     * 开始播放
     *
     * @param name 当前视频名称
     */
    public void onPlayBegin(String name) {
    }

    /**
     * 播放暂停
     */
    public void onPlayPause() {
    }

    /**
     * 播放器停止
     */
    public void onPlayStop() {
    }

    /**
     * 播放器进入Loading状态
     */
    public void onPlayLoading() {
    }

    /**
     * 播放进度回调
     *
     * @param current
     * @param duration
     */
    public void onPlayProgress(long current, long duration) {
    }

    public void onSeek(int position) {
    }

    public void onSwitchStreamStart(boolean success, SuperPlayerDef.PlayerType playerType, VideoQuality quality) {
    }

    public void onSwitchStreamEnd(boolean success, SuperPlayerDef.PlayerType playerType, VideoQuality quality) {
    }

    public void onError(int code, String message) {
    }

    public void onPlayerTypeChange(SuperPlayerDef.PlayerType playType) {
    }

    public void onPlayTimeShiftLive(TXLivePlayer player, String url) {
    }

    public void onVideoQualityListChange(List<VideoQuality> videoQualities, VideoQuality defaultVideoQuality) {
    }

    public void onVideoImageSpriteAndKeyFrameChanged(PlayImageSpriteInfo info, List<PlayKeyFrameDescInfo> list) {
    }

    public void onRcvFirstIframe(){

    }
}
