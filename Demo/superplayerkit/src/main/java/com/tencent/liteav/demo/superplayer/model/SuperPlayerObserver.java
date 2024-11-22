package com.tencent.liteav.demo.superplayer.model;

import com.tencent.liteav.demo.superplayer.SuperPlayerDef;
import com.tencent.liteav.demo.superplayer.model.entity.PlayImageSpriteInfo;
import com.tencent.liteav.demo.superplayer.model.entity.PlayKeyFrameDescInfo;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXTrackInfo;

import java.util.List;

public abstract class SuperPlayerObserver {

    /**
     * Preparing to play
     *
     * 准备播放
     */
    public void onPlayPrepare() {

    }

    /**
     * Start playing
     *
     * 开始播放
     *
     * @param name Current video name
     *             当前视频名称
     */
    public void onPlayBegin(String name) {
    }

    /**
     * Playback paused
     *
     * 播放暂停
     */
    public void onPlayPause() {
    }

    /**
     * Player stopped
     *
     * 播放器停止
     */
    public void onPlayStop() {
    }

    /**
     * Player error
     *
     * 播放器错误
     */
    public void onPlayError() {
    }

    /**
     * Player enters loading state
     *
     * 播放器进入Loading状态
     */
    public void onPlayLoading() {
    }

    /**
     * Playback progress callback
     *
     * 播放进度回调
     */
    public void onPlayProgress(long current, long duration, long playable) {
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

    public void onRcvTrackInformation(List<TXTrackInfo> infoList, TXTrackInfo lastSelected) {

    }

    public void onRcvSubTitleTrackInformation(List<TXTrackInfo> infoList) {

    }

    public void onRcvWaterMark(String text, long duration) {

    }
}
